package org.sakaiproject.citation.impl;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BaseConfigureationServiceTest extends TestCase {

    public void testParseXml() throws ParserConfigurationException, IOException, SAXException {
        // This is a small test of the configuration parsing developed when upgrading the xstream library.
        BaseConfigurationService configurationService = new BaseConfigurationService();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document document = builder.parse(getClass().getResourceAsStream("config-example.xml"));
        configurationService.saveServletClientMappings(document);

        Map<String, List<Map<String, String>>> saveciteClients = configurationService.getSaveciteClients();
        assertEquals(1, saveciteClients.size());
        List<Map<String, String>> en = saveciteClients.get("en");
        assertNotNull(en);
        Map<String, String> first = en.get(0);
        assertNotNull(first);
        assertEquals("Test", first.get("test"));
        assertEquals("saveciteClients", first.get("id"));


    }
}
