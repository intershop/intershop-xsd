package com.intershop.xsd.validator;

/**
 * This exception is thrown if none of the XML schema definition files are existing.
 */
public class XmlSchemaFilesMissingException extends Exception
{
    public XmlSchemaFilesMissingException(String msg)
    {
        super(msg);
    }
}
