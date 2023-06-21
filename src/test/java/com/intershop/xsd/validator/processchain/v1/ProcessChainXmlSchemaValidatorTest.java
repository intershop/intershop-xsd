package com.intershop.xsd.validator.processchain.v1;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intershop.xsd.validator.XmlSchemaValidator;
import com.intershop.xsd.validator.XmlSchemaValidatorImpl;

/**
 * Integration test for {@link ProcessChainXmlSchemaValidator} with {@link XmlSchemaValidatorImpl}.
 */
class ProcessChainXmlSchemaValidatorTest
{
    URL processChainXML = getClass().getResource("test-processchain.xml");
    URL enfinityProcessChainXML = getClass().getResource("test-processchain-enfinity.xml");

    Injector injector;

    @BeforeEach
    void setUp()
    {
        injector = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(XmlSchemaValidator.class).toInstance(new XmlSchemaValidatorImpl());
            }
        });
    }

    @Test
    void validatorFindsSchemaResources()
    {
        assertDoesNotThrow(() -> injector.getInstance(ProcessChainXmlSchemaValidator.class));
    }

    @Test
    void validateSchema()
    {
        ProcessChainXmlSchemaValidator underTest = injector.getInstance(ProcessChainXmlSchemaValidator.class);

        File processChainXMLFile = new File(processChainXML.getFile());
        assertTrue(underTest.isValid(processChainXMLFile));
        assertTrue(underTest.getParseErrors().isEmpty());
    }

    @Test
    void validateEnfinitySchema()
    {
        ProcessChainXmlSchemaValidator underTest = injector.getInstance(ProcessChainXmlSchemaValidator.class);

        File processChainXMLFile = new File(enfinityProcessChainXML.getFile());
        assertTrue(underTest.isValid(processChainXMLFile));
        assertTrue(underTest.getParseErrors().isEmpty());
    }
}
