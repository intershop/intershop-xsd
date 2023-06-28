package com.intershop.xsd.validator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Integration test for {@link XmlSchemaValidator} with {@link XmlSchemaClasspathResourceResolver}.
 */
public class XmlSchemaValidatorResolverTest
{
    URL validXML = getClass().getResource("test-valid.xml");

    Injector injector;

    XmlSchemaValidator validator;

    XmlSchemaClasspathResourceResolver resourceResolver;

    @BeforeEach
    void setUp() throws XmlSchemaFilesMissingException
    {
        injector = Guice.createInjector(new TestModule());

        validator = injector.getInstance(XmlSchemaValidator.class);
        validator.setSchemaResourcePaths(List.of("test-schema.xsd"));

        resourceResolver = injector.getInstance(XmlSchemaClasspathResourceResolver.class);
    }

    @Test
    void resolveExternalSchemaResource()
    {
        assertNotNull(validXML, "Cannot find valid XML resource");
        File validXMLFile = new File(validXML.getFile());
        validator.isValid(validXMLFile);

        verify(resourceResolver).resolveSystemId("http://www.w3.org/2001/xml.xsd");
        verify(resourceResolver).resolveExternalSystemId("http://www.w3.org/2001/xml.xsd");
    }

    private static class TestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(XmlSchemaValidator.class).to(XmlSchemaValidatorImpl.class);
            bind(XmlSchemaClasspathResourceResolver.class).toInstance(spy(XmlSchemaClasspathResourceResolver.class));
        }
    }
}
