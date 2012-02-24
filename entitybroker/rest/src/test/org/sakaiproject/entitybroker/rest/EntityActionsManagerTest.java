/**
 * $Id$
 * $URL$
 * EntityActionsManagerTest.java - entity-broker - Jul 27, 2008 6:10:15 PM - azeckoski
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

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.mocks.ActionsDefineableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ActionsEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.rest.EntityActionsManager;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;


/**
 * Test the code which handled entity actions
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityActionsManagerTest extends TestCase {

    protected EntityActionsManager entityActionsManager;
    private TestData td;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // setup things
        td = new TestData();

        entityActionsManager = new ServiceTestManager(td).entityActionsManager;
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.rest.EntityActionsManager#handleCustomActionRequest(org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable, org.sakaiproject.entitybroker.EntityView, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    public void testHandleCustomActionRequest() {
        EntityHttpServletRequest request = null;
        EntityHttpServletResponse res = null;
        ActionsEntityProviderMock actionProvider = td.entityProviderA1;
        String action = null;
        String URL = null;
        ActionReturn actionReturn = null;

        // double
        action = "double";
        URL = TestData.REFA1 + "/" + action;
        MyEntity me = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        int num = me.getNumber();
        request = new EntityHttpServletRequest("GET", URL);
        res = new EntityHttpServletResponse();
        actionReturn = entityActionsManager.handleCustomActionRequest(actionProvider, 
                new EntityView(new EntityReference(URL), EntityView.VIEW_SHOW, null), action, request, res, null);
        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
        assertNotNull(actionReturn);
        assertNotNull(actionReturn.entityData);
        MyEntity doubleMe = (MyEntity) actionReturn.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // xxx
        action = "xxx";
        URL = TestData.REFA1 + "/" + action;
        MyEntity me1 = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertFalse("xxx".equals(me1.extra));
        assertFalse("xxx".equals(me1.getStuff()));
        actionReturn = entityActionsManager.handleCustomActionRequest(actionProvider, 
                new EntityView(new EntityReference(URL), EntityView.VIEW_EDIT, null), action, request, res, null);
        assertNull(actionReturn);
        MyEntity xxxMe = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertEquals(me1.getId(), xxxMe.getId());
        assertTrue("xxx".equals(xxxMe.extra));
        assertTrue("xxx".equals(xxxMe.getStuff()));

        // clear
        action = "clear";
        URL = TestData.SPACEA1 + "/" + action;
        assertEquals(2, actionProvider.myEntities.size());
        actionReturn = entityActionsManager.handleCustomActionRequest(actionProvider, 
                new EntityView(new EntityReference(URL), EntityView.VIEW_NEW, null), action, request, res, null);
        assertEquals(0, actionProvider.myEntities.size());

        // exceptions
        try {
            entityActionsManager.handleCustomActionRequest(actionProvider, null, action, request, res, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.rest.EntityActionsManager#handleCustomActionExecution(org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable, org.sakaiproject.entitybroker.EntityReference, java.lang.String, java.util.Map, java.io.OutputStream)}.
     */
    public void testHandleCustomActionExecution() {
        // test the double/xxx/clear actions
        ActionsEntityProviderMock actionProvider = td.entityProviderA1;
        EntityReference ref = new EntityReference(TestData.PREFIXA1, TestData.IDSA1[0]);

        // double
        MyEntity me = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        int num = me.getNumber();
        ActionReturn actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider, ref, "double", null, null, null, null);
        assertNotNull(actionReturn);
        assertNotNull(actionReturn.entityData);
        MyEntity doubleMe = (MyEntity) actionReturn.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // xxx
        MyEntity me1 = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertFalse("xxx".equals(me1.extra));
        assertFalse("xxx".equals(me1.getStuff()));
        actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider, ref, "xxx", null, null, null, null);
        assertNull(actionReturn);
        MyEntity xxxMe = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
        assertEquals(me1.getId(), xxxMe.getId());
        assertTrue("xxx".equals(xxxMe.extra));
        assertTrue("xxx".equals(xxxMe.getStuff()));

        // clear
        assertEquals(2, actionProvider.myEntities.size());
        actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider, new EntityReference(TestData.PREFIXA1, ""), "clear", null, null, null, null);
        assertEquals(0, actionProvider.myEntities.size());

        // check exception when try to execute invalid action
        try {
            entityActionsManager.handleCustomActionExecution(actionProvider, ref, "NOT_VALID_ACTION", null, null, null, null);
            fail("should have thrown exeception");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e.getMessage());
        }

        try {
            entityActionsManager.handleCustomActionExecution(null, ref, "xxx", null, null, null, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            entityActionsManager.handleCustomActionExecution(actionProvider, null, "xxx", null, null, null, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        try {
            entityActionsManager.handleCustomActionExecution(actionProvider, ref, "", null, null, null, null);
            fail("should have thrown exeception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Ensure that the mock for testing the custom actions works correctly
     */
    public void testActionsEntityProviderMock() {
        ActionsEntityProviderMock aep = td.entityProviderA1;

        // check double operation works
        MyEntity me = (MyEntity) aep.getEntity( new EntityReference(TestData.REFA1) );
        int num = me.getNumber();
        ActionReturn ar = (ActionReturn) aep.doubleCustomAction(new EntityView(new EntityReference(TestData.REFA1), null, null));
        MyEntity doubleMe = (MyEntity) ar.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // make sure it works twice
        ar = (ActionReturn) aep.doubleCustomAction(new EntityView(new EntityReference(TestData.REFA1), null, null));
        doubleMe = (MyEntity) ar.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);

        // test xxx operation
        MyEntity me1 = (MyEntity) aep.getEntity( new EntityReference(TestData.REFA1) );
        assertFalse("xxx".equals(me1.extra));
        assertFalse("xxx".equals(me1.getStuff()));
        aep.xxxAction( new EntityReference(TestData.REFA1) );
        MyEntity xxxMe = (MyEntity) aep.getEntity( new EntityReference(TestData.REFA1) );
        assertEquals(me1.getId(), xxxMe.getId());
        assertTrue("xxx".equals(xxxMe.extra));
        assertTrue("xxx".equals(xxxMe.getStuff()));

        // test clear
        assertEquals(2, aep.myEntities.size());
        aep.clear();
        assertEquals(0, aep.myEntities.size());      
    }

    public void testCustomActions() {
        ActionsDefineableEntityProviderMock aep = td.entityProviderA2;

        // check double operation works
        MyEntity me = (MyEntity) aep.getEntity( new EntityReference(TestData.REFA2) );
        int num = me.getNumber();
        ActionReturn ar = (ActionReturn) aep.doubleUp(new EntityView(new EntityReference(TestData.REFA2), null, null));
        MyEntity doubleMe = (MyEntity) ar.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // make sure it works twice
        ar = (ActionReturn) aep.doubleUp(new EntityView(new EntityReference(TestData.REFA2), null, null));
        doubleMe = (MyEntity) ar.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);

        // test xxx operation
        MyEntity me1 = (MyEntity) aep.getEntity( new EntityReference(TestData.REFA2) );
        assertFalse("xxx".equals(me1.extra));
        assertFalse("xxx".equals(me1.getStuff()));
        aep.xxxChange( new EntityReference(TestData.REFA2) );
        MyEntity xxxMe = (MyEntity) aep.getEntity( new EntityReference(TestData.REFA2) );
        assertEquals(me1.getId(), xxxMe.getId());
        assertTrue("xxx".equals(xxxMe.extra));
        assertTrue("xxx".equals(xxxMe.getStuff()));

        // test clear
        assertEquals(2, aep.myEntities.size());
        aep.clearAll();
        assertEquals(0, aep.myEntities.size());      
    }

}
