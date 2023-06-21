package com.intershop.xsd.enfinity.core.processchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public class EnfinityProcessChainUnmarshalTest
{
    URL enfinityProcessChainXML = getClass().getResource("test-processchain-enfinity.xml");

    @Test
    void unmarshalEnfinityProcessChain() throws JAXBException
    {
        assertNotNull(enfinityProcessChainXML, "Cannot find enfinity process chain XML resource");

        JAXBContext enfinityChainContext = JAXBContext.newInstance(EnfinityChain.class);
        Unmarshaller enfinityChainUnmarshaller = enfinityChainContext.createUnmarshaller();
        EnfinityChain enfinityChain = (EnfinityChain)enfinityChainUnmarshaller.unmarshal(enfinityProcessChainXML);

        assertEquals("TestChain", enfinityChain.getName());
        assertEquals("TestConcurrent", enfinityChain.getConcurrent().getName());
        assertFalse(enfinityChain.getConcurrent().getTasksForConcurrent().isEmpty());
        assertEquals("TestJob", enfinityChain.getConcurrent().getTasksForConcurrent().get(0).getName());
    }
}
