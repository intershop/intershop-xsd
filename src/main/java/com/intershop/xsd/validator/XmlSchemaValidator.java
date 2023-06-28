package com.intershop.xsd.validator;

import java.io.File;
import java.util.List;

import org.xml.sax.SAXParseException;

/**
 * Validator interface for XML schema definitions (XSD).
 *
 * <p>
 * Validates the given XML file with potentially multiple XML schema definitions for backwards compatible
 * schema validation.
 */
public interface XmlSchemaValidator
{
    /**
     * Set an ordered list of schema definition resource paths which will be used for validation in the
     * order of occurrence.
     *
     * @param schemaDefinitionResourcePaths List of schema definition resource paths
     * @throws XmlSchemaFilesMissingException If none of the XML schema files are existing
     */
    void setSchemaResourcePaths(List<String> schemaDefinitionResourcePaths) throws XmlSchemaFilesMissingException;

    /**
     * @param xmlFile XML file
     * @return Whether the given XML file is valid according to the schema definition files or not.
     */
    boolean isValid(File xmlFile);

    /**
     * @return The SAX parse errors from validating the XML file.
     */
    List<SAXParseException> getParseErrors();
}
