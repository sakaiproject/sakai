/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.entitybroker.rest;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.config.EntityRestTestConfiguration;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.mocks.ActionsDefineableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ActionsEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@ContextConfiguration(classes = {EntityRestTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityActionsManagerTest {

    @Autowired private EntityActionsManager entityActionsManager;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    private TestData td; // loads sample providers

    @Before
    public void onSetUp() {
        td = new TestData(entityProviderManager);
        Mockito.when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");
    }

    @After
    public void tearDown() {
        entityProviderManager.unRegistrarAllProvidersAndListeners();
        td = null;
    }

    @Test
    public void testHandleCustomActionRequest() {
        EntityHttpServletRequest request;
        EntityHttpServletResponse res;
        ActionsEntityProviderMock actionProvider = td.entityProviderA1;
        String URL;
        ActionReturn actionReturn;

        // double
        URL = TestData.REFA1 + "/" + "double";
        MyEntity me = (MyEntity) actionProvider.getEntity(new EntityReference(TestData.REFA1));
        int num = me.getNumber();
        request = new EntityHttpServletRequest("GET", URL);
        res = new EntityHttpServletResponse();
        actionReturn = entityActionsManager.handleCustomActionRequest(actionProvider, new EntityView(new EntityReference(URL), EntityView.VIEW_SHOW, null), "double", request, res, null);
        assertEquals(HttpServletResponse.SC_OK, res.getStatus());
        assertNotNull(actionReturn);
        assertNotNull(actionReturn.entityData);
        MyEntity doubleMe = (MyEntity) actionReturn.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // xxx
        URL = TestData.REFA1 + "/" + "xxx";
        MyEntity me1 = (MyEntity) actionProvider.getEntity(new EntityReference(TestData.REFA1));
        assertNotEquals("xxx", me1.extra);
        assertNotEquals("xxx", me1.getStuff());
        actionReturn = entityActionsManager.handleCustomActionRequest(actionProvider, new EntityView(new EntityReference(URL), EntityView.VIEW_EDIT, null), "xxx", request, res, null);
        assertNull(actionReturn);
        MyEntity xxxMe = (MyEntity) actionProvider.getEntity(new EntityReference(TestData.REFA1));
        assertEquals(me1.getId(), xxxMe.getId());
        assertEquals("xxx", xxxMe.extra);
        assertEquals("xxx", xxxMe.getStuff());

        // clear
        URL = TestData.SPACEA1 + "/" + "clear";
        assertEquals(2, actionProvider.myEntities.size());
        entityActionsManager.handleCustomActionRequest(
                actionProvider,
                new EntityView(new EntityReference(URL), EntityView.VIEW_NEW, null),
                "clear",
                request,
                res,
                null);
        assertEquals(0, actionProvider.myEntities.size());

        // exceptions
        assertThrows(IllegalArgumentException.class,
                () -> entityActionsManager.handleCustomActionRequest(actionProvider, null, "clear", request, res, null));
    }

    @Test
    public void testHandleCustomActionExecution() {
        // test the double/xxx/clear actions
        ActionsEntityProviderMock actionProvider = td.entityProviderA1;
        EntityReference ref = new EntityReference(TestData.PREFIXA1, TestData.IDSA1[0]);

        // double
        MyEntity me = (MyEntity) actionProvider.getEntity(new EntityReference(TestData.REFA1));
        int num = me.getNumber();
        ActionReturn actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider, ref, "double", null, null, null, null);
        assertNotNull(actionReturn);
        assertNotNull(actionReturn.entityData);
        MyEntity doubleMe = (MyEntity) actionReturn.entityData.getData();
        assertEquals(doubleMe.getNumber(), num * 2);
        assertEquals(me.getId(), doubleMe.getId());

        // xxx
        MyEntity me1 = (MyEntity) actionProvider.getEntity(new EntityReference(TestData.REFA1));
        assertNotEquals("xxx", me1.extra);
        assertNotEquals("xxx", me1.getStuff());
        actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider, ref, "xxx", null, null, null, null);
        assertNull(actionReturn);
        MyEntity xxxMe = (MyEntity) actionProvider.getEntity(new EntityReference(TestData.REFA1));
        assertEquals(me1.getId(), xxxMe.getId());
        assertEquals("xxx", xxxMe.extra);
        assertEquals("xxx", xxxMe.getStuff());

        // clear
        assertEquals(2, actionProvider.myEntities.size());
        actionReturn = entityActionsManager.handleCustomActionExecution(actionProvider,
                new EntityReference(TestData.PREFIXA1, ""),
                "clear",
                null,
                null,
                null,
                null);
        assertNull(actionReturn);
        assertEquals(0, actionProvider.myEntities.size());

        // check exception when try to execute invalid action
        assertThrows(UnsupportedOperationException.class,
                () -> entityActionsManager.handleCustomActionExecution(actionProvider, ref, "NOT_VALID_ACTION", null, null, null, null));

        assertThrows(IllegalArgumentException.class,
                () -> entityActionsManager.handleCustomActionExecution(null, ref, "xxx", null, null, null, null));

        assertThrows(IllegalArgumentException.class,
                () ->entityActionsManager.handleCustomActionExecution(actionProvider, null, "xxx", null, null, null, null));

        assertThrows(IllegalArgumentException.class,
                () -> entityActionsManager.handleCustomActionExecution(actionProvider, ref, "", null, null, null, null));
    }

    @Test
    public void testActionsEntityProviderMock() {
        ActionsEntityProviderMock aep = td.entityProviderA1;

        // check double operation works
        MyEntity me = (MyEntity) aep.getEntity(new EntityReference(TestData.REFA1));
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
        MyEntity me1 = (MyEntity) aep.getEntity(new EntityReference(TestData.REFA1));
        assertNotEquals("xxx", me1.extra);
        assertNotEquals("xxx", me1.getStuff());
        aep.xxxAction(new EntityReference(TestData.REFA1));
        MyEntity xxxMe = (MyEntity) aep.getEntity(new EntityReference(TestData.REFA1));
        assertEquals(me1.getId(), xxxMe.getId());
        assertEquals("xxx", xxxMe.extra);
        assertEquals("xxx", xxxMe.getStuff());

        // test clear
        assertEquals(2, aep.myEntities.size());
        aep.clear();
        assertEquals(0, aep.myEntities.size());
    }

    @Test
    public void testCustomActions() {
        ActionsDefineableEntityProviderMock aep = td.entityProviderA2;

        // check double operation works
        MyEntity me = (MyEntity) aep.getEntity(new EntityReference(TestData.REFA2));
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
        MyEntity me1 = (MyEntity) aep.getEntity(new EntityReference(TestData.REFA2));
        assertNotEquals("xxx", me1.extra);
        assertNotEquals("xxx", me1.getStuff());
        aep.xxxChange(new EntityReference(TestData.REFA2));
        MyEntity xxxMe = (MyEntity) aep.getEntity(new EntityReference(TestData.REFA2));
        assertEquals(me1.getId(), xxxMe.getId());
        assertEquals("xxx", xxxMe.extra);
        assertEquals("xxx", xxxMe.getStuff());

        // test clear
        assertEquals(2, aep.myEntities.size());
        aep.clearAll();
        assertEquals(0, aep.myEntities.size());
    }

}
