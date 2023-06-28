package com.intershop.xsd.validator;

/**
 * This exception is thrown if none of the XML schema definition files are existing.
 */
public class XmlSchemaFilesMissingException extends Exception
{
    /**
     * @param msg Exception message
     *
     * @see XmlSchemaFilesMissingException
     */
    public XmlSchemaFilesMissingException(String msg)
    {
        super(msg);
    }
}
