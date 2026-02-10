/*
 *
 * $URL$
 * $Id$
 *
 * Copyright (c) 2025 Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti.foorm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit tests for FoormBaseBean reflection, archive, and type handling.
 */
public class FoormBaseBeanTest {

    private Date testDate;

    /**
     * Minimal FoormBaseBean subclass for testing reflection and archive behavior.
     */
    public static class MinimalFoormBean extends FoormBaseBean {
        @FoormField(value = "id", type = FoormType.KEY, archive = true)
        public Long id;
        @FoormField(value = "title", type = FoormType.TEXT, archive = true)
        public String title;
        @FoormField(value = "count", type = FoormType.INTEGER, archive = true)
        public Integer count;
        @FoormField(value = "active", type = FoormType.CHECKBOX, archive = true)
        public Boolean active;
        @FoormField(value = "state", type = FoormType.RADIO, archive = true)
        public Integer state;
        @FoormField(value = "created", type = FoormType.AUTODATE, archive = true)
        public Date created;
        @FoormField(value = "notes", type = FoormType.TEXT)
        public String notes;
    }

    /**
     * Bean that overrides getExcludedArchiveFieldNames to exclude a field.
     */
    public static class BeanWithExclusion extends FoormBaseBean {
        @FoormField(value = "id", type = FoormType.KEY, archive = true)
        public Long id;
        @FoormField(value = "secret", type = FoormType.TEXT, archive = true)
        public String secret;

        @Override
        protected Set<String> getExcludedArchiveFieldNames() {
            return Set.of("secret");
        }
    }

    @Before
    public void setUp() {
        testDate = new Date();
    }

    // --- getValueByFieldName / getFoormFieldByFieldName ---

    @Test
    public void testGetValueByFieldName() {
        MinimalFoormBean bean = new MinimalFoormBean();
        bean.id = 42L;
        bean.title = "Test Title";
        bean.count = 99;
        bean.active = true;
        bean.notes = "not archived";

        assertEquals(Long.valueOf(42L), bean.getValueByFieldName("id"));
        assertEquals("Test Title", bean.getValueByFieldName("title"));
        assertEquals(Integer.valueOf(99), bean.getValueByFieldName("count"));
        assertEquals(Boolean.TRUE, bean.getValueByFieldName("active"));
        assertEquals("not archived", bean.getValueByFieldName("notes"));
    }

    @Test
    public void testGetValueByFieldNameUnknownField() {
        MinimalFoormBean bean = new MinimalFoormBean();
        bean.id = 1L;
        assertNull(bean.getValueByFieldName("unknown_field"));
    }

    @Test
    public void testGetValueByFieldNameNullValue() {
        MinimalFoormBean bean = new MinimalFoormBean();
        assertNull(bean.getValueByFieldName("id"));
        assertNull(bean.getValueByFieldName("title"));
    }

    @Test
    public void testGetFoormFieldByFieldName() {
        MinimalFoormBean bean = new MinimalFoormBean();
        FoormField idField = bean.getFoormFieldByFieldName("id");
        assertNotNull(idField);
        assertEquals("id", idField.value());
        assertEquals(FoormType.KEY, idField.type());
        assertTrue(idField.archive());

        FoormField notesField = bean.getFoormFieldByFieldName("notes");
        assertNotNull(notesField);
        assertEquals("notes", notesField.value());
        assertFalse(notesField.archive());
    }

    @Test
    public void testGetFoormFieldByFieldNameUnknown() {
        MinimalFoormBean bean = new MinimalFoormBean();
        assertNull(bean.getFoormFieldByFieldName("nonexistent"));
    }

    // --- toArchiveElement ---

    @Test
    public void testToArchiveElementNullDocument() {
        MinimalFoormBean bean = new MinimalFoormBean();
        bean.id = 1L;
        bean.title = "x";
        assertNull(bean.toArchiveElement(null, "root"));
    }

    @Test
    public void testToArchiveElementOnlyArchivableFields() throws ParserConfigurationException {
        MinimalFoormBean bean = new MinimalFoormBean();
        bean.id = 123L;
        bean.title = "Archive Me";
        bean.count = 7;
        bean.active = true;
        bean.state = 1;
        bean.created = testDate;
        bean.notes = "should not appear";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = bean.toArchiveElement(doc, "test-bean");

        assertNotNull(root);
        assertEquals("test-bean", root.getTagName());

        Map<String, String> children = getChildElementsAsMap(root);
        assertTrue(children.containsKey("id"));
        assertTrue(children.containsKey("title"));
        assertTrue(children.containsKey("count"));
        assertTrue(children.containsKey("active"));
        assertTrue(children.containsKey("state"));
        assertTrue(children.containsKey("created"));
        assertFalse(children.containsKey("notes"));

        assertEquals("123", children.get("id"));
        assertEquals("Archive Me", children.get("title"));
        assertEquals("7", children.get("count"));
        assertEquals("1", children.get("active"));
        assertEquals("1", children.get("state"));
        assertEquals(String.valueOf(testDate.getTime()), children.get("created"));
    }

    @Test
    public void testToArchiveElementSkipsNullFields() throws ParserConfigurationException {
        MinimalFoormBean bean = new MinimalFoormBean();
        bean.id = 1L;
        bean.title = null;
        bean.count = null;

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = bean.toArchiveElement(doc, "test-bean");

        Map<String, String> children = getChildElementsAsMap(root);
        assertEquals(1, children.size());
        assertEquals("1", children.get("id"));
    }

    @Test
    public void testToArchiveElementRespectsExcludedFields() throws ParserConfigurationException {
        BeanWithExclusion bean = new BeanWithExclusion();
        bean.id = 999L;
        bean.secret = "super-secret";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = bean.toArchiveElement(doc, "bean-with-exclusion");

        Map<String, String> children = getChildElementsAsMap(root);
        assertTrue(children.containsKey("id"));
        assertFalse(children.containsKey("secret"));
        assertEquals("999", children.get("id"));
    }

    // --- populateFromArchiveElement ---

    @Test
    public void testPopulateFromArchiveElementNullInput() {
        MinimalFoormBean bean = new MinimalFoormBean();
        bean.id = 1L;
        bean.populateFromArchiveElement(null);
        assertEquals(Long.valueOf(1L), bean.id);
    }

    @Test
    public void testPopulateFromArchiveElement() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("test-bean");
        appendChild(root, "id", "456");
        appendChild(root, "title", "Populated Title");
        appendChild(root, "count", "21");
        appendChild(root, "active", "1");
        appendChild(root, "state", "2");
        appendChild(root, "created", String.valueOf(testDate.getTime()));

        MinimalFoormBean bean = new MinimalFoormBean();
        bean.populateFromArchiveElement(root);

        assertEquals(Long.valueOf(456L), bean.id);
        assertEquals("Populated Title", bean.title);
        assertEquals(Integer.valueOf(21), bean.count);
        assertTrue(bean.active);
        assertEquals(Integer.valueOf(2), bean.state);
        assertEquals(testDate, bean.created);
    }

    @Test
    public void testPopulateFromArchiveElementIgnoresNonArchivable() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("test-bean");
        appendChild(root, "id", "1");
        appendChild(root, "notes", "should be ignored");  // notes has archive=false

        MinimalFoormBean bean = new MinimalFoormBean();
        bean.populateFromArchiveElement(root);

        assertEquals(Long.valueOf(1L), bean.id);
        assertNull(bean.notes);  // notes not archivable, so not parsed
    }

    @Test
    public void testPopulateFromArchiveElementIgnoresUnknownTags() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("test-bean");
        appendChild(root, "id", "1");
        appendChild(root, "unknown_tag", "value");

        MinimalFoormBean bean = new MinimalFoormBean();
        bean.populateFromArchiveElement(root);

        assertEquals(Long.valueOf(1L), bean.id);
    }

    // --- Archive round-trip ---

    @Test
    public void testArchiveRoundTrip() throws ParserConfigurationException {
        MinimalFoormBean original = new MinimalFoormBean();
        original.id = 777L;
        original.title = "Round Trip Test";
        original.count = 42;
        original.active = false;
        original.state = 0;
        original.created = testDate;

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element el = original.toArchiveElement(doc, "minimal");
        doc.appendChild(el);

        MinimalFoormBean restored = new MinimalFoormBean();
        restored.populateFromArchiveElement(el);

        assertEquals(original.id, restored.id);
        assertEquals(original.title, restored.title);
        assertEquals(original.count, restored.count);
        assertEquals(original.active, restored.active);
        assertEquals(original.state, restored.state);
        assertEquals(original.created, restored.created);
    }

    @Test
    public void testParseArchiveValueBooleanVariants() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("test-bean");
        appendChild(root, "id", "1");
        appendChild(root, "active", "true");

        MinimalFoormBean bean = new MinimalFoormBean();
        bean.populateFromArchiveElement(root);
        assertTrue(bean.active);

        root.getElementsByTagName("active").item(0).setTextContent("1");
        MinimalFoormBean bean2 = new MinimalFoormBean();
        bean2.populateFromArchiveElement(root);
        assertTrue(bean2.active);

        root.getElementsByTagName("active").item(0).setTextContent("yes");
        MinimalFoormBean bean3 = new MinimalFoormBean();
        bean3.populateFromArchiveElement(root);
        assertTrue(bean3.active);
    }

    @Test
    public void testParseArchiveValueDecimalTruncation() throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("test-bean");
        appendChild(root, "id", "123.99");
        appendChild(root, "count", "456.7");

        MinimalFoormBean bean = new MinimalFoormBean();
        bean.populateFromArchiveElement(root);

        assertEquals(Long.valueOf(123L), bean.id);
        assertEquals(Integer.valueOf(456), bean.count);
    }

    // --- Helpers ---

    private static Map<String, String> getChildElementsAsMap(Element parent) {
        Map<String, String> map = new HashMap<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                map.put(e.getTagName(), e.getTextContent());
            }
        }
        return map;
    }

    private static void appendChild(Element parent, String tagName, String textContent) {
        Document doc = parent.getOwnerDocument();
        Element child = doc.createElement(tagName);
        child.setTextContent(textContent);
        parent.appendChild(child);
    }
}
