package com.intershop.xsd.processchain.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;

import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public class ProcessChainUnmarshalTest
{
    URL processChainXML = getClass().getResource("test-processchain.xml");

    @Test
    void unmarshalProcessChain() throws JAXBException
    {
        assertNotNull(processChainXML, "Cannot find process chain XML resource");

        JAXBContext chainContext = JAXBContext.newInstance(Chain.class);
        Unmarshaller chainUnmarshaller = chainContext.createUnmarshaller();
        Chain chain = (Chain)chainUnmarshaller.unmarshal(processChainXML);

        assertEquals("TestChain", chain.getName());
        assertEquals("TestConcurrent", chain.getConcurrent().getName());
        assertFalse(chain.getConcurrent().getTasksForConcurrent().isEmpty());
        assertEquals("TestJob", chain.getConcurrent().getTasksForConcurrent().get(0).getName());
    }
}
