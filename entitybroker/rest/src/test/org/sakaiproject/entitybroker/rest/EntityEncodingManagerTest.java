/**
 * $Id$
 * $URL$
 * EntityHandlerImplTest.java - entity-broker - Apr 6, 2008 12:08:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.rest.EntityEncodingManager;
import org.sakaiproject.entitybroker.rest.EntityHandlerImpl;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;

/**
 * Testing the central logic of the entity handler
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityEncodingManagerTest extends TestCase {

    protected EntityEncodingManager entityEncodingManager;
    protected EntityBrokerManager entityBrokerManager;
    private TestData td;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // setup things
        td = new TestData();
        ServiceTestManager tm = new ServiceTestManager(td);
        entityEncodingManager = tm.entityEncodingManager;
        entityBrokerManager = tm.entityBrokerManager;
    }

    public void testInternalInputTranslator() {

        String xml = "<"+TestData.PREFIX6+"><stuff>TEST</stuff><number>5</number></"+TestData.PREFIX6+">";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        MyEntity me = (MyEntity) entityEncodingManager.translateInputToEntity(
                new EntityReference(TestData.PREFIX6,""), Formats.XML, inputStream, null);
        assertNotNull(me);
        assertEquals("TEST", me.getStuff());
        assertEquals(5, me.getNumber());

        // old sucky way String json = "{\""+TestData.PREFIX6+"\" : { \"stuff\" : \"TEST\", \"number\" : 5 }}";
        String json = "{ \"stuff\" : \"TEST\", \"number\" : 5 }";
        inputStream = new ByteArrayInputStream(json.getBytes());
        MyEntity me2 = (MyEntity) entityEncodingManager.translateInputToEntity(
                new EntityReference(TestData.PREFIX6,""), Formats.JSON, inputStream, null);
        assertNotNull(me2);
        assertEquals("TEST", me2.getStuff());
        assertEquals(5, me2.getNumber());

     }

    public void testEncodeEntity() {
        String encoded = null;
        EntityData ed = null;

        ed = new EntityData(new EntityReference(TestData.REF4), "Aaron Title", TestData.entity4);
        encoded = entityEncodingManager.encodeEntity(TestData.PREFIX4, Formats.XML, ed, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains(TestData.PREFIX4));
        assertTrue(encoded.contains("Aaron Title"));
        assertTrue(encoded.contains("something0"));

        ed = new EntityData(new EntityReference(TestData.REF4), "Aaron Title2", TestData.entity4_two);
        encoded = entityEncodingManager.encodeEntity(TestData.PREFIX4, Formats.XML, ed, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains(TestData.PREFIX4));
        assertTrue(encoded.contains("Aaron Title2"));
        assertTrue(encoded.contains("something1"));

        // test encoding random stuff
        Map<String, Object> map = new ArrayOrderedMap<String, Object>();
        map.put("A", "aaron");
        map.put("B", "becky");
        map.put("C", "minerva");
        ed = new EntityData(map);
        encoded = entityEncodingManager.encodeEntity(TestData.PREFIX4, Formats.XML, ed, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains(TestData.PREFIX4));
        assertTrue(encoded.contains("aaron"));
        assertTrue(encoded.contains("becky"));

        List<String> l = new ArrayList<String>();
        l.add("AZ");
        l.add("BZ");
        l.add("CZ");
        ed = new EntityData(l);
        encoded = entityEncodingManager.encodeEntity(TestData.PREFIX4, Formats.XML, ed, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains(TestData.PREFIX4));
        assertTrue(encoded.contains("AZ"));
        assertTrue(encoded.contains("BZ"));

        String[] array = new String[] {"A","B","C"};
        ed = new EntityData(array);
        encoded = entityEncodingManager.encodeEntity(TestData.PREFIX4, Formats.XML, ed, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains(TestData.PREFIX4));
        assertTrue(encoded.contains("A"));
        assertTrue(encoded.contains("B"));

    }

    /**
     * Testing for http://jira.sakaiproject.org/browse/SAK-19197 (xml encoding)
     * Items with spaces will not encode correctly and will cause an exception, they 
     * have to be fixed at the provider level
     */
    public void testSpaceEncoding() {
        EntityData ed = null;

        // test encoding weird stuff
        Map<String, Object> map = new ArrayOrderedMap<String, Object>();
        map.put("A1", "aaron one");
        map.put("C&3", "minerva three");
        map.put("B 2", "becky two");
        ed = new EntityData(map);
        /* fixed in 0.9.15 reflectutils
        try {
            entityEncodingManager.encodeEntity(TestData.PREFIX4, Formats.XML, ed, null);
            fail("Could not encode spaces");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e.getMessage());
        }*/
        String encoded = entityEncodingManager.encodeEntity(TestData.PREFIX4, Formats.XML, ed, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains("aaron one"));
        assertTrue(encoded.contains("becky two"));
        assertTrue(encoded.contains("minerva three"));
    }

    /**
     * Test method for {@link EntityHandlerImpl#internalOutputFormatter(EntityView, javax.servlet.http.HttpServletRequest, HttpServletResponse)}
     **/
    public void testInternalOutputFormatter() {

        String fo = null;
        EntityView view = null;
        OutputStream output = null;

        // XML test valid resolveable entity
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.REF4 + "." + Formats.XML);
        assertNotNull(view);
        entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("<id>4-one</id>"));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

        // test null view
        output = new ByteArrayOutputStream();
        entityEncodingManager.internalOutputFormatter(new EntityReference(TestData.REF4), Formats.XML, null, null, output, null);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("<id>4-one</id>"));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

        // test list of entities
        ArrayList<EntityData> testEntities = new ArrayList<EntityData>();
        testEntities.add( new EntityData(TestData.REF4, null, TestData.entity4) );
        testEntities.add( new EntityData(TestData.REF4_two, null, TestData.entity4_two) );
        output = new ByteArrayOutputStream();
        entityEncodingManager.internalOutputFormatter(new EntityReference(TestData.PREFIX4, ""), Formats.XML, testEntities, null, output, null);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("<id>4-one</id>"));
        assertTrue(fo.contains("<id>4-two</id>"));
        assertFalse(fo.contains("<id>4-three</id>"));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

        // test single entity
        testEntities.clear();
        testEntities.add( new EntityData(TestData.REF4_3, null, TestData.entity4_3) );
        output = new ByteArrayOutputStream();
        entityEncodingManager.internalOutputFormatter(new EntityReference(TestData.REF4_3), Formats.XML, testEntities, null, output, null);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("<id>4-three</id>"));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));


        // JSON test valid resolveable entity
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.REF4 + "." + Formats.JSON);
        assertNotNull(view);
        entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("\"id\":"));
        assertTrue(fo.contains("\"4-one\","));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));
        
        // JSONP test valid resolveable entity, default callback
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.ENTITY_URL4_JSONP);
        assertNotNull(view);
        entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("\"id\":"));
        assertTrue(fo.contains("\"4-one\","));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));
        assertTrue(fo.contains(EntityEncodingManager.JSON_DEFAULT_CALLBACK + "("));
        
        // JSONP test valid resolveable entity, specified callback
        HashMap<String, Object> callbackParam = new HashMap<String, Object>();
        callbackParam.put(EntityEncodingManager.JSON_CALLBACK_PARAM, "customCallback");
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.ENTITY_URL4_JSONP);
        assertNotNull(view);
        entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, callbackParam, output, view);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("\"id\":"));
        assertTrue(fo.contains("\"4-one\","));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));
        assertTrue(fo.contains("customCallback("));

        // HTML test valid resolveable entity
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.REF4 + "." + Formats.HTML);
        assertNotNull(view);
        entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains(TestData.REF4));

        // test invalid format request
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.REF4 + "." + Formats.RSS);
        assertNotNull(view);
        try {
            entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
            fail("Should have thrown exception");
        } catch (FormatUnsupportedException e) {
            assertNotNull(e.getMessage());
        }

        // test for unresolvable entities

        // JSON test unresolvable entity
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.REF1 + "." + Formats.JSON);
        assertNotNull(view);
        try {
            entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
            fail("Should have thrown exception");
        } catch (EntityException e) {
            assertNotNull(e.getMessage());
            assertEquals(HttpServletResponse.SC_NOT_FOUND, e.responseCode);
        }

        // HTML test unresolvable entity
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.REF1); // blank
        assertNotNull(view);
        try {
            entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
            fail("Should have thrown exception");
        } catch (EntityException e) {
            assertNotNull(e.getMessage());
            assertEquals(HttpServletResponse.SC_NOT_FOUND, e.responseCode);
        }

        // test resolveable collections
        // XML
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.SPACE4 + "." + Formats.XML);
        assertNotNull(view);
        entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains(TestData.IDS4[0]));
        assertTrue(fo.contains(TestData.IDS4[1]));
        assertTrue(fo.contains(TestData.IDS4[2]));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

        // JSON
        output = new ByteArrayOutputStream();
        view = entityBrokerManager.parseEntityURL(TestData.SPACE4 + "." + Formats.JSON);
        assertNotNull(view);
        entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), null, null, output, view);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains(TestData.IDS4[0]));
        assertTrue(fo.contains(TestData.IDS4[1]));
        assertTrue(fo.contains(TestData.IDS4[2]));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

        // test for invalid refs
        try {
            entityEncodingManager.internalOutputFormatter( new EntityReference("/fakey/fake"), Formats.JSON, null, null, output, null);
            fail("Should have thrown exception");
        } catch (EntityException e) {
            assertNotNull(e.getMessage());
            assertEquals(HttpServletResponse.SC_NOT_FOUND, e.responseCode);
        }

    }

    // testing the internal encoder
    public void testJSONEncode() {
        String encoded = null;

        encoded = entityEncodingManager.encodeData(null, Formats.JSON, null, null);
        assertNotNull(encoded);
        assertEquals("", encoded);

        Map<String, Object> m = new ArrayOrderedMap<String, Object>();
        m.put("id", 123);
        m.put("thing", "AZ");
        encoded = entityEncodingManager.encodeData(m, Formats.JSON, null, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains("123"));
        assertTrue(encoded.contains("AZ"));

        Map<String, Object> m2 = new ArrayOrderedMap<String, Object>();
        m2.put("name", "aaron");
        m2.put("date", new Date());
        m2.put("num", 456);
        m2.put("array", new String[] {"A","B","C"});
        m.put("map", m2);

        encoded = entityEncodingManager.encodeData(m, Formats.JSON, null, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains("123"));
        assertTrue(encoded.contains("AZ"));
        assertTrue(encoded.contains("aaron"));
        assertTrue(encoded.contains("456"));

    }

    // testing the internal decoder
    @SuppressWarnings("unchecked")
    public void testJSONDecode() {
        Map<String, Object> decoded = null;

        String json = "{\"id\":123,\"thing\":\"AZ\"}";
        decoded = entityEncodingManager.decodeData(json, Formats.JSON);
        assertNotNull(decoded);
        assertEquals(123, decoded.get("id"));
        assertEquals("AZ", decoded.get("thing"));

        json = "{\"id\":123,\"thing\":\"AZ\",\"map\":{\"name\":\"aaron\",\"date\":1221493247004,\"num\":456,\"array\":[\"A\",\"B\",\"C\"]}}";
        decoded = entityEncodingManager.decodeData(json, Formats.JSON);
        assertNotNull(decoded);
        assertEquals(3, decoded.size());
        assertEquals(123, decoded.get("id"));
        assertEquals("AZ", decoded.get("thing"));
        Map<String, Object> m2d = (Map<String, Object>) decoded.get("map");
        assertEquals(4, m2d.size());
        assertEquals("aaron", m2d.get("name"));
        assertEquals(456, m2d.get("num"));

    }

    public void testXMLEncode() {
        String encoded = null;

        encoded = entityEncodingManager.encodeData(null, Formats.XML, null, null);
        assertNotNull(encoded);
        assertEquals("", encoded);

        Map<String, Object> m = new ArrayOrderedMap<String, Object>();
        m.put("id", 123);
        m.put("thing", "AZ");
        encoded = entityEncodingManager.encodeData(m, Formats.XML, null, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains("123"));
        assertTrue(encoded.contains("AZ"));

        Map<String, Object> m2 = new ArrayOrderedMap<String, Object>();
        m2.put("name", "aaron");
        m2.put("date", new Date());
        m2.put("num", 456);
        m2.put("array", new String[] {"A","B","C"});
        m.put("map", m2);

        encoded = entityEncodingManager.encodeData(m, Formats.XML, null, null);
        assertNotNull(encoded);
        assertTrue(encoded.contains("123"));
        assertTrue(encoded.contains("AZ"));
        assertTrue(encoded.contains("aaron"));
        assertTrue(encoded.contains("456"));

    }

    // testing the internal decoder
    @SuppressWarnings("unchecked")
    public void testXMLDecode() {
        Map<String, Object> decoded = null;

        String xml = "<data><id type='number'>123</id><thing>AZ</thing></data>";
        decoded = entityEncodingManager.decodeData(xml, Formats.XML);
        assertNotNull(decoded);
        assertEquals(123, decoded.get("id"));
        assertEquals("AZ", decoded.get("thing"));

        xml = "<data type='map' size='3' class='org.sakaiproject.genericdao.util.map.ArrayOrderedMap'><id type='number' class='java.lang.Integer'>123</id><thing>AZ</thing><map type='map' size='4' class='org.sakaiproject.genericdao.util.map.ArrayOrderedMap'><name>aaron</name><date type='date' date='2008-09-17T14:47:18+01:00'>1221659238015</date><num type='number' class='java.lang.Integer'>456</num><array type='array' length='3' component='java.lang.String'><string>A</string><string>B</string><string>C</string></array></map></data>";
        decoded = entityEncodingManager.decodeData(xml, Formats.XML);
        assertNotNull(decoded);
        assertEquals(3, decoded.size());
        assertEquals(123, decoded.get("id"));
        assertEquals("AZ", decoded.get("thing"));
        Map<String, Object> m2d = (Map<String, Object>) decoded.get("map");
        assertEquals(4, m2d.size());
        assertEquals("aaron", m2d.get("name"));
        assertEquals(456, m2d.get("num"));

    }

    public void testFormatAndOutputEntity() {
        String output = null;
        OutputStream outputStream = new ByteArrayOutputStream();
        EntityReference ref = null;
        String format = Formats.XML;
        List<EntityData> entities = null;

        outputStream = new ByteArrayOutputStream();
        ref = new EntityReference(TestData.PREFIX4, TestData.IDS4[0]);
        entityEncodingManager.formatAndOutputEntity(ref, format, entities, outputStream, null);
        output = outputStream.toString();
        assertNotNull(output);
        assertTrue(output.length() > 20);
        assertTrue(output.contains(TestData.PREFIX4));
        assertTrue(output.contains(TestData.REF4));

        outputStream = new ByteArrayOutputStream();
        ref = new EntityReference(TestData.PREFIX4, "");
        entityEncodingManager.formatAndOutputEntity(ref, format, entities, outputStream, null);
        output = outputStream.toString();
        assertNotNull(output);
        assertTrue(output.length() > 20);
        assertTrue(output.contains(TestData.PREFIX4));
        assertTrue(output.contains(TestData.REF4));
        assertTrue(output.contains(TestData.REF4_two));
        assertTrue(output.contains(TestData.REF4_3));

        entities = new ArrayList<EntityData>();
        entities.add( new EntityData(new EntityReference(TestData.REF4), "Hello1", td.entityProviderS1.myEntities.get(TestData.IDS4[0])) );
        entities.add( new EntityData(new EntityReference(TestData.REF4_two), "Hello2", td.entityProviderS1.myEntities.get(TestData.IDS4[1])) );
        outputStream = new ByteArrayOutputStream();
        ref = new EntityReference(TestData.PREFIX4, "");
        entityEncodingManager.formatAndOutputEntity(ref, format, entities, outputStream, null);
        output = outputStream.toString();
        assertNotNull(output);
        assertTrue(output.length() > 20);
        assertTrue(output.contains("Hello"));
        assertTrue(output.contains(TestData.PREFIX4));
        assertTrue(output.contains(TestData.REF4));
        assertTrue(output.contains(TestData.REF4_two));

        entities = null;
        outputStream = new ByteArrayOutputStream();
        ref = new EntityReference(TestData.PREFIXS1, TestData.IDSS1[0]);
        entityEncodingManager.formatAndOutputEntity(ref, format, entities, outputStream, null);
        output = outputStream.toString();
        assertNotNull(output);
        assertTrue(output.length() > 20);
        assertTrue(output.contains(TestData.PREFIXS1));
        assertTrue(output.contains("xtra"));
        assertTrue(output.contains(TestData.IDSS1[0]));

        entities = new ArrayList<EntityData>();
        entities.add( new EntityData(new EntityReference(TestData.PREFIXS1, TestData.IDSS1[0]), "Hello1", td.entityProviderS1.myEntities.get(TestData.IDSS1[0])) );
        entities.add( new EntityData(new EntityReference(TestData.PREFIXS1, TestData.IDSS1[2]), "Hello2", td.entityProviderS1.myEntities.get(TestData.IDSS1[2])) );
        outputStream = new ByteArrayOutputStream();
        ref = new EntityReference(TestData.PREFIXS1, "");
        entityEncodingManager.formatAndOutputEntity(ref, format, entities, outputStream, null);
        output = outputStream.toString();
        assertNotNull(output);
        assertTrue(output.length() > 20);
        assertTrue(output.contains(TestData.PREFIXS1));
        assertTrue(output.contains("Hello"));
        assertTrue(output.contains("xtra"));
        assertTrue(output.contains(TestData.IDSS1[0]));
        assertTrue(output.contains(TestData.IDSS1[2]));

    }

}
