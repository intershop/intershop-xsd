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
    public XmlSchemaParseException(String msg, Exception exception)
    {
        super(msg, exception);
    }
}
