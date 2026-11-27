package com.intershop.xsd.validator.impex.catalog;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intershop.xsd.validator.XmlSchemaValidator;
import com.intershop.xsd.validator.XmlSchemaValidatorImpl;
import com.intershop.xsd.validator.processchain.v1.ProcessChainXmlSchemaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for {@link ProductXmlSchemaValidator} with {@link XmlSchemaValidatorImpl}.
 */
class ProductXmlSchemaValidatorTest
{
    URL sharedProducts = getClass().getResource("inSPIRED-SharedProducts.xml");
    URL intronicsProducts = getClass().getResource("inSPIRED-inTRONINCS-Products.xml");

    Injector injector;

    @BeforeEach
    void setUp()
    {
        injector = Guice.createInjector(new TestModule());
    }

    @Test
    void validatorFindsSchemaResources()
    {
        assertDoesNotThrow(() -> injector.getInstance(ProductXmlSchemaValidator.class));
    }

    @Test
    void validateSchema()
    {
        ProductXmlSchemaValidator underTest = injector.getInstance(ProductXmlSchemaValidator.class);

        File xmlFile = new File(sharedProducts.getFile());
        assertTrue(underTest.isValid(xmlFile));
        assertTrue(underTest.getParseErrors().isEmpty());
    }

    @Test
    void validateEnfinitySchema()
    {
        ProductXmlSchemaValidator underTest = injector.getInstance(ProductXmlSchemaValidator.class);

        File xmlFile = new File(intronicsProducts.getFile());
        assertTrue(underTest.isValid(xmlFile));
        assertTrue(underTest.getParseErrors().isEmpty());
    }

    private static class TestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(XmlSchemaValidator.class).to(XmlSchemaValidatorImpl.class);
        }
    }
}
