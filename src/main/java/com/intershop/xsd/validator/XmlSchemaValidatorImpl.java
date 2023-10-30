package com.intershop.xsd.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * Validates the given XML file with potentially multiple XML schema definitions for backwards compatible
 * schema validation.
 *
 * <p>
 * Uses {@link XmlSchemaClasspathResourceResolver} to provide validation
 * without the need to access external resources (e.g. from the internet).
 * </p>
 *
 * <p>
 * Denies own DOCTYPE declaration and access to external references to prevent attacks such as XXE.
 * </p>
 */
public class XmlSchemaValidatorImpl implements XmlSchemaValidator
{
    /**
     * Ordered list of schema resource paths which will be used for schema validation.
     */
    private final List<String> schemaResourcePaths;

    /**
     * List of SAX parse errors.
     */
    private final List<SAXParseException> parseErrors;

    /**
     * Schema validation error handler.
     */
    private final XmlSchemaValidationErrorHandler xmlSchemaValidationErrorHandler;

    /**
     * Resolves schemas via classpath resources.
     */
    private final XmlSchemaClasspathResourceResolver xmlSchemaClasspathResourceResolver;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param xmlSchemaValidationErrorHandler XML schema validator error handler
     * @param xmlSchemaClasspathResourceResolver XML schema classpath resource resolver
     *
     * @see XmlSchemaValidatorImpl
     */
    @Inject
    public XmlSchemaValidatorImpl(XmlSchemaValidationErrorHandler xmlSchemaValidationErrorHandler,
        XmlSchemaClasspathResourceResolver xmlSchemaClasspathResourceResolver)
    {
        this.schemaResourcePaths = Collections.synchronizedList(new ArrayList<>());
        this.parseErrors = Collections.synchronizedList(new ArrayList<>());

        this.xmlSchemaValidationErrorHandler = xmlSchemaValidationErrorHandler;
        this.xmlSchemaClasspathResourceResolver = xmlSchemaClasspathResourceResolver;
    }

    /**
     * @return The SAX parse errors after validating the XML file according to the schema file.
     *         If called before invoking {@link #isValid(File)}, empty list is returned.
     */
    @Override
    public List<SAXParseException> getParseErrors()
    {
        return this.parseErrors;
    }

    /**
     * Set parse errors.
     *
     * @param parseErrors List of {@link SAXParseException}
     */
    private void setParseErrors(List<SAXParseException> parseErrors)
    {
        this.parseErrors.clear();
        this.parseErrors.addAll(parseErrors);
    }

    /**
     * Clear parse errors.
     */
    private void clearParseErrors()
    {
        this.parseErrors.clear();
    }

    /**
     * @param schemaDefinitionResourcePaths List of schema definition resource paths
     * @return List of existing schema definition resource paths
     */
    private List<String> getExistingSchemaResourcePaths(List<String> schemaDefinitionResourcePaths)
    {
        return schemaDefinitionResourcePaths.stream()
            .filter(schemaResourcePath -> Objects.nonNull(getResourceAsStream(schemaResourcePath)))
            .toList();
    }

    /**
     * Set an ordered list of schema definition resource paths which will be used for validation in the
     * order of occurrence.
     *
     * @param schemaDefinitionResourcePaths List of schema definition resource paths
     * @throws XmlSchemaFilesMissingException If none of the XML schema files are existing
     */
    @Override
    public void setSchemaResourcePaths(List<String> schemaDefinitionResourcePaths) throws XmlSchemaFilesMissingException
    {
        // Paths of existing schema validation resources
        List<String> existingSchemaResourcePaths = getExistingSchemaResourcePaths(schemaDefinitionResourcePaths);

        // No schema validation resources available
        if (existingSchemaResourcePaths.isEmpty())
        {
            throw new XmlSchemaFilesMissingException(
                "No schema definition files found in classpath (checked path(s) %s) and therefore no schema validation possible"
                    .formatted(String.join(", ", schemaDefinitionResourcePaths))
            );
        }

        this.schemaResourcePaths.clear();
        this.schemaResourcePaths.addAll(existingSchemaResourcePaths);
    }

    /**
     * Validates the given XML file with the XML schema definitions.
     *
     * @param xmlFile XML file
     * @return Whether the given XML file is valid according to any of the schema definitions or not
     */
    @Override
    public boolean isValid(File xmlFile)
    {
        if (this.schemaResourcePaths.isEmpty())
        {
            logger.warn("No schema definition files provided");
            return false;
        }

        for (String schemaResourcePath : this.schemaResourcePaths)
        {
            logger.debug("Validating schema with schema definition '{}' of XML file '{}'", schemaResourcePath, xmlFile);
            InputStream schemaResourceInputStream = getResourceAsStream(schemaResourcePath);
            StreamSource schemaResourceStreamSource = new StreamSource(schemaResourceInputStream);
            StreamSource xmlStreamSource = new StreamSource(xmlFile);

            // Clear parse errors from potential previous iteration
            clearParseErrors();

            if (validate(schemaResourceStreamSource, xmlStreamSource))
            {
                return true;
            }
            else
            {
                logger.warn(
                    "Validation of XML file '{}' failed with schema resource file '{}' and the following errors: {}",
                    xmlFile, schemaResourcePath, getParseErrors());
            }
        }

        return false;
    }

    private boolean validate(StreamSource xsdStreamSource, StreamSource xmlStreamSource)
    {
        Validator validator = newValidator(xsdStreamSource);
        Objects.requireNonNull(validator, "Validator initialization failed");
        validator.setErrorHandler(xmlSchemaValidationErrorHandler);

        // Clear validation errors from potential previous validation
        xmlSchemaValidationErrorHandler.clearErrors();

        try
        {
            validator.validate(xmlStreamSource);

            if (!xmlSchemaValidationErrorHandler.getErrors().isEmpty())
            {
                throw new SAXException("Error handler detected SAX parse exceptions, therefore invalid schema");
            }
        }
        catch(SAXException e)
        {
            setParseErrors(xmlSchemaValidationErrorHandler.getErrors());
            return false;
        }
        catch(IOException e)
        {
            throw new XmlSchemaParseException("During processing the SAXSource the underlying XMLReader threw an exception", e);
        }

        return true;
    }

    private Validator newValidator(StreamSource xsdInputStream)
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try
        {
            // Restrict access to external references
            restrictExternalReferenceAccess(schemaFactory);

            // Resource resolver
            schemaFactory.setResourceResolver(xmlSchemaClasspathResourceResolver);

            // Parse specified source as a schema and return it
            Schema schema = schemaFactory.newSchema(xsdInputStream);

            // Creates new Validator for this schema
            return schema.newValidator();
        }
        catch(SAXException e)
        {
            logger.error("Cannot initialize a new validator", e);
        }

        return null;
    }

    private void restrictExternalReferenceAccess(SchemaFactory schemaFactory)
        throws SAXNotSupportedException, SAXNotRecognizedException
    {
        // Disable DOCTYPE declaration
        schemaFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
        // Deny access to external references to prevent attacks such as XXE
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    }

    private InputStream getResourceAsStream(String resourcePath)
    {
        return getClass().getResourceAsStream(resourcePath);
    }
}
