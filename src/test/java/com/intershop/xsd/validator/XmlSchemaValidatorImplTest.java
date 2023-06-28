package com.intershop.xsd.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test class for {@link XmlSchemaValidatorImpl}.
 */
class XmlSchemaValidatorImplTest
{
    URL validXML = getClass().getResource("test-valid.xml");
    URL invalidXML = getClass().getResource("test-invalid.xml");

    Injector injector;

    XmlSchemaValidatorImpl underTest;

    @BeforeEach
    void setUp() throws XmlSchemaFilesMissingException
    {
        injector = Guice.createInjector();
        underTest = injector.getInstance(XmlSchemaValidatorImpl.class);
        underTest.setSchemaResourcePaths(List.of("test-schema.xsd"));
    }

    @Test
    void missingSchemaResourcePaths()
    {
        assertThrows(XmlSchemaFilesMissingException.class, () -> underTest.setSchemaResourcePaths(List.of()));
    }

    @Test
    void isValid()
    {
        assertNotNull(validXML, "Cannot find valid XML resource");
        File validXMLFile = new File(validXML.getFile());
        assertTrue(underTest.isValid(validXMLFile));
    }

    @Test
    void isInvalid()
    {
        assertNotNull(invalidXML, "Cannot find invalid XML resource");
        File invalidXMLFile = new File(invalidXML.getFile());
        assertFalse(underTest.isValid(invalidXMLFile));
        assertFalse(underTest.getParseErrors().isEmpty());
    }
}
