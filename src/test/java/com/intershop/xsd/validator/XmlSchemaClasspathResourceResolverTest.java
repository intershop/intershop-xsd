package com.intershop.xsd.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link XmlSchemaClasspathResourceResolver}.
 */
class XmlSchemaClasspathResourceResolverTest
{
    XmlSchemaClasspathResourceResolver underTest;

    @BeforeEach
    void setUp()
    {
        underTest = new XmlSchemaClasspathResourceResolver();
    }

    @Test
    void resolveLocalSystemIdRelativePath()
    {
        String expected = XmlSchemaClasspathResourceResolver.RESOURCE_SEARCH_PATH + "www.w3.org/2001/xml.xsd";
        assertEquals(expected, underTest.resolveSystemId("../../www.w3.org/2001/xml.xsd"));
        assertEquals(expected, underTest.resolveSystemId("./www.w3.org/2001/xml.xsd"));
    }

    @Test
    void resolveLocalSystemIdExternalUrl()
    {
        String expected = XmlSchemaClasspathResourceResolver.RESOURCE_SEARCH_PATH + "www.w3.org/2001/xml.xsd";
        assertEquals(expected, underTest.resolveSystemId("https://www.w3.org/2001/xml.xsd"));
    }
}
