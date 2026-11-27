package com.intershop.xsd.validator.impex.catalog;

import java.io.File;
import java.util.List;

import jakarta.inject.Inject;

import org.xml.sax.SAXParseException;

import com.intershop.xsd.validator.XmlSchemaFilesMissingException;
import com.intershop.xsd.validator.XmlSchemaValidator;

/**
 * Specific {@link XmlSchemaValidator} for process chains.
 */
public class ProductXmlSchemaValidator implements XmlSchemaValidator
{
    /**
     * Ordered list of schema resource paths which will be used for process chain schema validation.
     */
    private final List<String> validationSchemaResourcePaths = List.of(
        "/xml/ns/enfinity/7.1/xcs/impex/catalog.xsd"
    );

    private final XmlSchemaValidator xmlSchemaValidator;

    /**
     * @param xmlSchemaValidator XML schema validator
     * @throws XmlSchemaFilesMissingException If none of the XML schema files are existing
     *
     * @see ProductXmlSchemaValidator
     */
    @Inject
    public ProductXmlSchemaValidator(XmlSchemaValidator xmlSchemaValidator) throws XmlSchemaFilesMissingException
    {
        this.xmlSchemaValidator = xmlSchemaValidator;

        setSchemaResourcePaths(getSchemaResourcePaths());
    }

    List<String> getSchemaResourcePaths()
    {
        return validationSchemaResourcePaths;
    }

    @Override
    public void setSchemaResourcePaths(List<String> schemaDefinitionResourcePaths) throws XmlSchemaFilesMissingException
    {
        xmlSchemaValidator.setSchemaResourcePaths(schemaDefinitionResourcePaths);
    }

    @Override
    public boolean isValid(File chainXMLFile)
    {
        return xmlSchemaValidator.isValid(chainXMLFile);
    }

    @Override
    public List<SAXParseException> getParseErrors()
    {
        return xmlSchemaValidator.getParseErrors();
    }
}
