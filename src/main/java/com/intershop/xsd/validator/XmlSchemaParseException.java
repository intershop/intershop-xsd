package com.intershop.xsd.validator;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.XMLReader;

/**
 * This exception is thrown if the validator is processing a {@link SAXSource} and
 * the underlying {@link XMLReader} throws an {@link IOException} during XML schema parsing.
 */
public class XmlSchemaParseException extends RuntimeException
{
    /**
     * Creates the exception with the given message and the exception thrown by the {@link XMLReader}.
     *
     * @param msg Exception message
     * @param cause Exception cause
     *
     * @see XmlSchemaParseException
     */
    public XmlSchemaParseException(String msg, Exception cause)
    {
        super(msg, cause);
    }
}
