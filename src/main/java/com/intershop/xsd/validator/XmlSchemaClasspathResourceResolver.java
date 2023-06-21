package com.intershop.xsd.validator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Custom resource resolution when parsing schemas.
 */
public class XmlSchemaClasspathResourceResolver implements LSResourceResolver
{
    static final String RESOURCE_SEARCH_PATH = "/xml/ns/";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
    {
        LSInputImpl input = new LSInputImpl();
        String resolvedSystemId = resolveSystemId(systemId);
        InputStream stream = getClass().getResourceAsStream(resolvedSystemId);
        Objects.requireNonNull(stream, "Cannot resolve resource '" + resolvedSystemId + "' (original '" + systemId + "') from available classpath resources");

        input.setPublicId(publicId);
        input.setSystemId(systemId);
        input.setBaseURI(baseURI);
        input.setCharacterStream(new InputStreamReader(stream));

        return input;
    }

    String resolveSystemId(String systemId)
    {
        // External, contains protocol
        if (systemId.contains("://"))
        {
            return resolveExternalSystemId(systemId);
        }

        // Otherwise we assume relative path
        return resolveLocalSystemId(systemId);
    }

    /**
     * Resolves a local systemId to a resource name.
     *
     * @param systemId System ID
     * @return Yields "{@link #RESOURCE_SEARCH_PATH}/non-traversed path" where all directory traversals are removed
     */
    String resolveLocalSystemId(String systemId)
    {
        String nonTraversedSystemId = systemId.replace("../", "").replace("./", "");
        // Make sure systemId does not start with '/' as RESOURCE_SEARCH_PATH ends with it
        if (nonTraversedSystemId.startsWith("/"))
        {
            nonTraversedSystemId = nonTraversedSystemId.substring(1);
        }
        return RESOURCE_SEARCH_PATH + nonTraversedSystemId;
    }

    /**
     * Resolves an external systemId to a resource name.
     *
     * @param systemId System ID
     * @return Yields "{@link #RESOURCE_SEARCH_PATH}/domain/path" based on the parsed system ID URL
     */
    String resolveExternalSystemId(String systemId)
    {
        URL systemIdUrl;
        try
        {
            systemIdUrl = new URL(systemId);
            return RESOURCE_SEARCH_PATH + systemIdUrl.getAuthority() + systemIdUrl.getPath();
        }
        catch(MalformedURLException e)
        {
            logger.error("Malformed URL during resolving of system ID '{}'", systemId, e);
            return RESOURCE_SEARCH_PATH + systemId;
        }
    }

    /**
     * Responsible for holding the input content of the resolved schema.
     */
    private static class LSInputImpl implements LSInput
    {
        private Reader characterStream;
        private InputStream byteStream;
        private String stringData;
        private String systemId;
        private String publicId;
        private String baseURI;
        private String encoding;
        private boolean certifiedText;

        @Override
        public Reader getCharacterStream()
        {
            return characterStream;
        }

        @Override
        public void setCharacterStream(Reader characterStream)
        {
            this.characterStream = characterStream;
        }

        @Override
        public InputStream getByteStream()
        {
            return byteStream;
        }

        @Override
        public void setByteStream(InputStream byteStream)
        {
            this.byteStream = byteStream;
        }

        @Override
        public String getStringData()
        {
            return stringData;
        }

        @Override
        public void setStringData(String stringData)
        {
            this.stringData = stringData;
        }

        @Override
        public String getSystemId()
        {
            return systemId;
        }

        @Override
        public void setSystemId(String systemId)
        {
            this.systemId = systemId;
        }

        @Override
        public String getPublicId()
        {
            return publicId;
        }

        @Override
        public void setPublicId(String publicId)
        {
            this.publicId = publicId;
        }

        @Override
        public String getBaseURI()
        {
            return baseURI;
        }

        @Override
        public void setBaseURI(String baseURI)
        {
            this.baseURI = baseURI;
        }

        @Override
        public String getEncoding()
        {
            return encoding;
        }

        @Override
        public void setEncoding(String encoding)
        {
            this.encoding = encoding;
        }

        @Override
        public boolean getCertifiedText()
        {
            return certifiedText;
        }

        @Override
        public void setCertifiedText(boolean certifiedText)
        {
            this.certifiedText = certifiedText;
        }
    }
}
