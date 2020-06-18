/**
 * Copyright (c) 2003-2020 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
