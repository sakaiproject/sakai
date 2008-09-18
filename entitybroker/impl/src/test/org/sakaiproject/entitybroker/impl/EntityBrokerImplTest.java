/**
 * EntityBrokerImplTest.java - 2007 Jul 21, 2007 3:04:55 PM - entity-broker - AZ
 */

package org.sakaiproject.entitybroker.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.*;

import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.impl.data.TestDataPreload;
import org.sakaiproject.entitybroker.impl.mocks.FakeEvent;
import org.sakaiproject.entitybroker.mocks.ActionsEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the entitybroker implementation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@SuppressWarnings("deprecation")
public class EntityBrokerImplTest extends AbstractTransactionalSpringContextTests {

    protected EntityBrokerImpl entityBroker;

    private EntityBrokerDao dao;
    private TestData td;
    private TestDataPreload tdp;

    private EntityManager entityManager;
    private EventTrackingService eventTrackingService;

    protected String[] getConfigLocations() {
        // point to the needed spring config files, must be on the classpath
        // (add component/src/webapp/WEB-INF to the build path in Eclipse),
        // they also need to be referenced in the project.xml file
        return new String[] { "database-test.xml", "spring-jdbc.xml" };
    }

    // run this before each test starts
    protected void onSetUpBeforeTransaction() throws Exception {
        // load the spring created dao class bean from the Spring Application Context
        dao = (EntityBrokerDao) applicationContext
        .getBean("org.sakaiproject.entitybroker.dao.EntityBrokerDao");
        if (dao == null) {
            throw new NullPointerException("Dao could not be retrieved from spring context");
        }

        // load up the test data preloader from spring
        tdp = (TestDataPreload) applicationContext
        .getBean("org.sakaiproject.entitybroker.impl.test.data.TestDataPreload");
        if (tdp == null) {
            throw new NullPointerException(
            "TestDatePreload could not be retrieved from spring context");
        }

        // load up any other needed spring beans

        // init the test data
        td = new TestData();

        // setup the mock objects if needed
        entityManager = createNiceMock(EntityManager.class);
        replay(entityManager);
        eventTrackingService = createMock(EventTrackingService.class);

        // setup the defaults for the mock objects (if there are any)
        // sessionManager.getCurrentSessionUserId(); // expect this to be called
        // sessionManagerControl.setDefaultMatcher(MockControl.ALWAYS_MATCHER);
        // sessionManagerControl.setReturnValue(TestDataPreload.USER_ID, MockControl.ZERO_OR_MORE);
        // sessionManagerControl.replay();
        expect(eventTrackingService.newEvent(isA(String.class), isA(String.class), anyBoolean(), anyInt())).andReturn(new FakeEvent()).anyTimes(); // expect this to be called
        eventTrackingService.post(isA(Event.class));
        expectLastCall().anyTimes();
        replay(eventTrackingService);

        // setup fake internal services
        ServiceTestManager tm = new ServiceTestManager(td, dao);

        // create and setup the object to be tested
        entityBroker = new EntityBrokerImpl();
        entityBroker.setEntityBrokerManager( tm.entityBrokerManager );
        entityBroker.setEntityEncodingManager( tm.entityEncodingManager );
        entityBroker.setEntityManager(entityManager);
        entityBroker.setEntityProviderManager( tm.entityProviderManager );
        entityBroker.setEntityRequestHandler( tm.entityRequestHandler );
        entityBroker.setEventTrackingService(eventTrackingService);
        entityBroker.setEntityActionsManager( tm.entityActionsManager );
        entityBroker.setEntityTaggingService( tm.entityTaggingService);
        entityBroker.setEntityMetaPropertiesService( tm.entityMetaPropertiesService);
        entityBroker.setRequestStorage( tm.requestStorage );
    }

    // run this before each test starts and as part of the transaction
    protected void onSetUpInTransaction() {
        // preload additional data if desired
    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test, Note that if a method
     * is overloaded you should include the arguments in the test name like so: testMethodClassInt
     * (for method(Class, int);
     */

    public void testValidTestData() {
        // ensure the test data is setup the way we think
        assertNotNull(td);
        assertNotNull(tdp);

        // assertEquals(new Long(1), tdp.pNode1.getId());
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#entityExists(java.lang.String)}.
     */
    public void testEntityExists() {
        boolean exists = false;

        exists = entityBroker.entityExists(TestData.REF1);
        assertTrue(exists);

        exists = entityBroker.entityExists(TestData.REF1_1);
        assertTrue(exists);

        exists = entityBroker.entityExists(TestData.REF2);
        assertTrue(exists);

        // test that invalid id with valid prefix does not pass
        exists = entityBroker.entityExists(TestData.REF1_INVALID);
        assertFalse(exists);

        // test that unregistered ref does not pass
        exists = entityBroker.entityExists(TestData.REF9);
        assertFalse(exists);

        // test that invalid ref causes exception
        try {
            exists = entityBroker.entityExists(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getEntityURL(java.lang.String)}.
     */
    public void testGetEntityURL() {
        String url = null;

        url = entityBroker.getEntityURL(TestData.REF1);
        assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF2);
        assertEquals(TestData.URL2, url);

        url = entityBroker.getEntityURL(TestData.REF1_INVALID);

        try {
            url = entityBroker.getEntityURL(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    public void testGetEntityURLStringStringString() {
        String url = null;

        url = entityBroker.getEntityURL(TestData.REF1, null, null);
        assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF2, null, null);
        assertEquals(TestData.URL2, url);

        // test adding viewkey
        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_SHOW, null);
        assertEquals(TestData.URL1, url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_LIST, null);
        assertEquals(TestData.SPACE_URL1, url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_NEW, null);
        assertEquals(TestData.SPACE_URL1 + "/new", url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, null);
        assertEquals(TestData.URL1 + "/edit", url);

        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_DELETE, null);
        assertEquals(TestData.URL1 + "/delete", url);

        // test extension
        url = entityBroker.getEntityURL(TestData.REF1, null, "xml");
        assertEquals(TestData.URL1 + ".xml", url);

        url = entityBroker.getEntityURL(TestData.REF2, null, "json");
        assertEquals(TestData.URL2 + ".json", url);

        // test both
        url = entityBroker.getEntityURL(TestData.REF1, EntityView.VIEW_EDIT, "xml");
        assertEquals(TestData.URL1 + "/edit.xml", url);
    }

    public void testIsPrefixRegistered() {
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX1) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX2) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX3) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX4) );
        assertTrue( entityBroker.isPrefixRegistered(TestData.PREFIX5) );

        assertFalse( entityBroker.isPrefixRegistered(TestData.PREFIX9) );
        assertFalse( entityBroker.isPrefixRegistered("XXXXXX") );
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getRegisteredPrefixes()}.
     */
    public void testGetRegisteredPrefixes() {
        Set<String> s = entityBroker.getRegisteredPrefixes();
        assertNotNull(s);
        assertTrue(s.contains(TestData.PREFIX1));
        assertTrue(s.contains(TestData.PREFIX2));
        assertTrue(s.contains(TestData.PREFIX3));
        assertFalse(s.contains(TestData.PREFIX9));
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#fireEvent(java.lang.String, java.lang.String)}.
     */
    public void testFireEvent() {
        // we are mostly handing this to a mocked service so we can only check to see if errors occur
        entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF1);
        entityBroker.fireEvent(TestData.EVENT1_NAME, TestData.REF2);
        entityBroker.fireEvent(TestData.EVENT2_NAME, TestData.REF1);
        entityBroker.fireEvent(TestData.EVENT2_NAME, "XXXXXXXXXXXXXX");

        // event with a null name should die
        try {
            entityBroker.fireEvent(null, TestData.REF1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            entityBroker.fireEvent("", TestData.REF1);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#parseReference(java.lang.String)}.
     */
    public void testParseReference() {
        EntityReference er = null;

        er = entityBroker.parseReference(TestData.REF1);
        assertNotNull(er);
        assertEquals(TestData.PREFIX1, er.getPrefix());
        assertEquals(TestData.IDS1[0], er.getId());

        er = entityBroker.parseReference(TestData.REF2);
        assertNotNull(er);
        assertEquals(TestData.PREFIX2, er.getPrefix());

        // test parsing a defined reference
        er = entityBroker.parseReference(TestData.REF3A);
        assertNotNull(er);
        assertEquals(TestData.PREFIX3, er.getPrefix());

        // parsing of unregistered entity references returns null
        er = entityBroker.parseReference(TestData.REF9);
        assertNull(er);

        // parsing with nonexistent prefix returns null
        er = entityBroker.parseReference("/totallyfake/notreal");
        assertNull(er);

        try {
            er = entityBroker.parseReference(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#fetchEntity(java.lang.String)}.
     */
    public void testFetchEntity() {
        Object obj = null;

        obj = entityBroker.fetchEntity(TestData.REF4);
        assertNotNull(obj);
        assertTrue(obj instanceof MyEntity);
        MyEntity entity = (MyEntity) obj;
        assertEquals(entity.getId(), TestData.IDS4[0]);

        obj = entityBroker.fetchEntity(TestData.REF4_two);
        assertNotNull(obj);
        assertTrue(obj instanceof MyEntity);
        MyEntity entity2 = (MyEntity) obj;
        assertEquals(entity2.getId(), TestData.IDS4[1]);

        // no object available should cause failure
        obj = entityBroker.fetchEntity(TestData.REF1);
        assertNull(obj);

        obj = entityBroker.fetchEntity(TestData.REF2);
        assertNull(obj);

        // use an unregistered provider to trigger the attempt to do a legacy lookup which will fail and return null
        obj = entityBroker.fetchEntity(TestData.REF9);
        assertNull(obj);

        // expect invalid reference to fail
        try {
            obj = entityBroker.fetchEntity(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            obj = entityBroker.fetchEntity(null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    public void testFormatAndOutputEntity() {

        String fo = null;
        String reference = null;
        OutputStream output = null;
        String format = Formats.XML;

        // XML test valid resolveable entity
        reference = TestData.REF4;
        output = new ByteArrayOutputStream();
        entityBroker.formatAndOutputEntity(reference, format, null, output, null);
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
        reference = TestData.SPACE4;
        output = new ByteArrayOutputStream();
        entityBroker.formatAndOutputEntity(reference, format, testEntities, output, null);
        fo = output.toString();
        assertNotNull(fo);
        assertTrue(fo.length() > 20);
        assertTrue(fo.contains(TestData.PREFIX4));
        assertTrue(fo.contains("<id>4-one</id>"));
        assertTrue(fo.contains("<id>4-two</id>"));
        assertFalse(fo.contains("<id>4-three</id>"));
        assertTrue(fo.contains(EntityEncodingManager.ENTITY_REFERENCE));

    }

    /**
     * Convenience method for making byte content encoded into UTF-8
     */
    private byte[] makeUTF8Bytes(String string) {
        byte[] bytes;
        try {
            bytes = string.getBytes(Formats.UTF_8);
        } catch (UnsupportedEncodingException e) {
            bytes = string.getBytes();
        }
        return bytes;
    }

    public void testTranslateInputToEntity() {
        InputStream input = null;
        MyEntity me = null;

        // test creating an entity
        String reference = TestData.SPACE6;
        String format = Formats.XML;
        input = new ByteArrayInputStream( makeUTF8Bytes("<"+TestData.PREFIX6+"><stuff>TEST</stuff><number>5</number></"+TestData.PREFIX6+">") );
        me = (MyEntity) entityBroker.translateInputToEntity(reference, format, input, null);
        assertNotNull(me);
        assertNull(me.getId());
        assertEquals("TEST", me.getStuff());
        assertEquals(5, me.getNumber());

        // test modifying an entity
        reference = TestData.REF6_2;
        input = new ByteArrayInputStream( makeUTF8Bytes("<"+TestData.PREFIX6+"><id>"+TestData.IDS6[1]+"</id><stuff>TEST-PUT</stuff><number>8</number></"+TestData.PREFIX6+">") );
        me = (MyEntity) entityBroker.translateInputToEntity(reference, format, input, null);
        assertNotNull(me);
        assertNotNull(me.getId());
        assertEquals(TestData.IDS6[1], me.getId());
        assertEquals("TEST-PUT", me.getStuff());
        assertEquals(8, me.getNumber());
    }

    public void testExecuteCustomAction() {
        ActionReturn actionReturn = null;

        // test the double/xxx/clear actions
        ActionsEntityProviderMock actionProvider = td.entityProviderA1;

        // double
        MyEntity me = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        int num = me.getNumber();
        actionReturn = entityBroker.executeCustomAction(TestData.REFA1, "double", null, null);
        //ActionReturn actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider, ref, "double", null, null);
        assertNotNull(actionReturn);
        assertNotNull(actionReturn.entityData);
        MyEntity doubleMe = (MyEntity) actionReturn.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // xxx
        MyEntity me1 = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertFalse("xxx".equals(me1.extra));
        assertFalse("xxx".equals(me1.getStuff()));
        actionReturn = entityBroker.executeCustomAction(TestData.REFA1, "xxx", null, null);
        assertNull(actionReturn);
        MyEntity xxxMe = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertEquals(me1.getId(), xxxMe.getId());
        assertTrue("xxx".equals(xxxMe.extra));
        assertTrue("xxx".equals(xxxMe.getStuff()));

        // clear
        assertEquals(2, actionProvider.myEntities.size());
        actionReturn = entityBroker.executeCustomAction(TestData.SPACEA1, "clear", null, null);
        assertEquals(0, actionProvider.myEntities.size());

        // check exception when try to execute invalid action
        try {
            entityBroker.executeCustomAction(TestData.REF3A, "action", null, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            entityBroker.executeCustomAction(TestData.INVALID_REF, "action", null, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefs(java.lang.String[], java.lang.String[], java.lang.String[], boolean)}.
     */
    public void testFindEntityRefs() {
        List<String> l = null;

        // test search with limit by prefix
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, null, null, true);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue(l.contains(TestData.REF5));
        assertTrue(l.contains(TestData.REF5_2));

        // test search with limit by prefix (check that no results ok)
        l = entityBroker.findEntityRefs(new String[] { TestData.INVALID_REF }, null, null, true);
        assertNotNull(l);
        assertEquals(0, l.size());

        // test searching with multiple prefixes
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5, TestData.PREFIX1 }, null,
                null, true);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue(l.contains(TestData.REF5));
        assertTrue(l.contains(TestData.REF5_2));

        // test searching by names
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.PROPERTY_NAME5A }, null, true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains(TestData.REF5));

        // test searching by invalid name (return no results)
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.INVALID_REF }, null, true);
        assertNotNull(l);
        assertEquals(0, l.size());

        // test searching with multiple names
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
                TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5C }, null, true);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue(l.contains(TestData.REF5));
        assertTrue(l.contains(TestData.REF5_2));

        // test search limit by values (long exact match)
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.PROPERTY_NAME5C }, new String[] { TestData.PROPERTY_VALUE5C },
                true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains(TestData.REF5_2));

        // test search limit by values (exact match)
        l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 },
                new String[] { TestData.PROPERTY_NAME5B }, new String[] { TestData.PROPERTY_VALUE5B },
                true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains(TestData.REF5));

        // cannot have empty or null prefix
        try {
            l = entityBroker.findEntityRefs(new String[] {},
                    new String[] { TestData.PROPERTY_NAME5A }, null, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            l = entityBroker.findEntityRefs(null, new String[] { TestData.PROPERTY_NAME5A }, null,
                    true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // test search limit by values cannot have name null or empty
        try {
            l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {},
                    new String[] { TestData.PROPERTY_VALUE5A }, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // test name and values arrays must match sizes
        try {
            l = entityBroker.findEntityRefs(new String[] { TestData.PREFIX5 }, new String[] {
                    TestData.PROPERTY_NAME5A, TestData.PROPERTY_NAME5B },
                    new String[] { TestData.PROPERTY_VALUE5A }, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // test search with all empty fields fail
        try {
            l = entityBroker.findEntityRefs(new String[] {}, new String[] {}, new String[] {}, false);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getProperties(java.lang.String)}.
     */
    public void testGetProperties() {
        Map<String, String> m = null;

        m = entityBroker.getProperties(TestData.REF5);
        assertNotNull(m);
        assertEquals(2, m.size());
        assertTrue(m.containsKey(TestData.PROPERTY_NAME5A));
        assertTrue(m.containsKey(TestData.PROPERTY_NAME5B));

        m = entityBroker.getProperties(TestData.REF5_2);
        assertNotNull(m);
        assertEquals(1, m.size());
        assertTrue(m.containsKey(TestData.PROPERTY_NAME5C));

        // ref with no properties should fetch none
        m = entityBroker.getProperties(TestData.REF1);
        assertNotNull(m);
        assertTrue(m.isEmpty());

        // make sure invalid ref causes failure
        try {
            m = entityBroker.getProperties(TestData.INVALID_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getPropertyValue(java.lang.String, java.lang.String)}.
     */
    public void testGetPropertyValue() {
        String value = null;

        value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
        assertNotNull(value);
        assertEquals(TestData.PROPERTY_VALUE5A, value);

        value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5B);
        assertNotNull(value);
        assertEquals(TestData.PROPERTY_VALUE5B, value);

        // test large value retrieval
        value = entityBroker.getPropertyValue(TestData.REF5_2, TestData.PROPERTY_NAME5C);
        assertNotNull(value);
        assertEquals(TestData.PROPERTY_VALUE5C, value);

        // nonexistent value property get retrieves null
        value = entityBroker.getPropertyValue(TestData.REF5, "XXXXXXXXXXXX");
        assertNull(value);

        // make sure invalid ref causes failure
        try {
            value = entityBroker.getPropertyValue(TestData.INVALID_REF, TestData.PROPERTY_NAME5A);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // null name causes failure
        try {
            value = entityBroker.getPropertyValue(TestData.REF5, null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // empty name causes failure
        try {
            value = entityBroker.getPropertyValue(TestData.REF5, "");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for
     * {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setPropertyValue(java.lang.String, java.lang.String, java.lang.String[])}.
     */
    public void testSetPropertyValue() {
        String value = null;

        // check that we can save a new property on an entity
        entityBroker.setPropertyValue(TestData.REF5, "newNameAlpha", "newValueAlpha");
        value = entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha");
        assertNotNull(value);
        assertEquals("newValueAlpha", value);

        // check that we can update an existing property on an entity
        entityBroker.setPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A, "AZnewValue");
        value = entityBroker.getPropertyValue(TestData.REF5, TestData.PROPERTY_NAME5A);
        assertNotNull(value);
        assertNotSame(TestData.PROPERTY_VALUE5A, value);
        assertEquals("AZnewValue", value);

        // check that we can remove a property on an entity
        entityBroker.setPropertyValue(TestData.REF5, "newNameAlpha", null);
        value = entityBroker.getPropertyValue(TestData.REF5, "newNameAlpha");
        assertNull(value);

        // check that we can remove all properties on an entity
        Map<String, String> m = entityBroker.getProperties(TestData.REF5);
        assertEquals(2, m.size());
        entityBroker.setPropertyValue(TestData.REF5, null, null);
        m = entityBroker.getProperties(TestData.REF5);
        assertEquals(0, m.size());

        // make sure invalid ref causes failure
        try {
            entityBroker.setPropertyValue(TestData.INVALID_REF, "newNameAlpha", "newValueAlpha");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // make sure invalid params cause failure
        try {
            entityBroker.setPropertyValue(TestData.REF1, null, "XXXXXXXXX");
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#getTags(java.lang.String)}.
     */
    public void testGetTags() {
        Set<String> tags = null;

        tags = entityBroker.getTags(TestData.REF1);
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains("test"));
        assertTrue(tags.contains("aaronz"));

        tags = entityBroker.getTags(TestData.REF1_1);
        assertNotNull(tags);
        assertEquals(0, tags.size());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#setTags(java.lang.String, java.util.Set)}.
     */
    public void testSetTags() {
        // test adding new tags
        entityBroker.setTags(TestData.REF1_1, new String[] {"test"});
        assertEquals(1, entityBroker.getTags(TestData.REF1_1).size() );

        // test clearing tags
        entityBroker.setTags(TestData.REF1, new String[] {});
        assertEquals(0, entityBroker.getTags(TestData.REF1).size() );
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.EntityBrokerImpl#findEntityRefsByTags(java.lang.String[])}.
     */
    public void testFindEntityRefsByTags() {
        List<String> refs = null;

        refs = entityBroker.findEntityRefsByTags( new String[] {"aaronz"} );
        assertNotNull(refs);
        assertEquals(1, refs.size());
        assertEquals(TestData.REF1, refs.get(0));

        refs = entityBroker.findEntityRefsByTags( new String[] {"test"} );
        assertNotNull(refs);
        assertEquals(2, refs.size());
        assertEquals(TestData.REFT1, refs.get(0));
        assertEquals(TestData.REF1, refs.get(1));

        entityBroker.setTags(TestData.REF1_1, new String[] {"test"});

        refs = entityBroker.findEntityRefsByTags( new String[] {"test"} );
        assertNotNull(refs);
        assertEquals(3, refs.size());
    }

}
