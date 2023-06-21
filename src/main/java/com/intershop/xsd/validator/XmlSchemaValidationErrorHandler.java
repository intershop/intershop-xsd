package com.intershop.xsd.validator;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Collects all validation errors during XML schema validation.
 */
public class XmlSchemaValidationErrorHandler implements ErrorHandler
{
    private final List<SAXParseException> exceptions;

    public XmlSchemaValidationErrorHandler()
    {
        this.exceptions = new ArrayList<>();
    }

    public List<SAXParseException> getErrors()
    {
        return exceptions;
    }

    @Override
    public void warning(SAXParseException exception)
    {
        exceptions.add(exception);
    }

    @Override
    public void error(SAXParseException exception)
    {
        exceptions.add(exception);
    }

    @Override
    public void fatalError(SAXParseException exception)
    {
        exceptions.add(exception);
    }
}
