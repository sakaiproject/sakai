/**
 * $Id$
 * $URL$
 * EntityActionsManagerTest.java - entity-broker - Jul 27, 2008 6:10:15 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.mocks.ActionsDefineableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ActionsEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.MockEBHttpServletRequest;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.springframework.mock.web.MockHttpServletResponse;


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
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityActionsManager#handleCustomActionRequest(org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable, org.sakaiproject.entitybroker.EntityView, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
    */
   public void testHandleCustomActionRequest() {
      MockEBHttpServletRequest request = null;
      MockHttpServletResponse res = null;
      ActionsEntityProviderMock actionProvider = td.entityProviderA1;
      String action = null;
      String URL = null;
      ActionReturn actionReturn = null;

      // double
      action = "double";
      URL = TestData.REFA1 + "/" + action;
      MyEntity me = (MyEntity) actionProvider.getEntity( new EntityReference(TestData.REFA1) );
      int num = me.getNumber();
      request = new MockEBHttpServletRequest("GET", URL);
      res = new MockHttpServletResponse();
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
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityActionsManager#handleCustomActionExecution(org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable, org.sakaiproject.entitybroker.EntityReference, java.lang.String, java.util.Map, java.io.OutputStream)}.
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

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#setCustomActions(java.lang.String, java.util.Map)}.
    */
   public void testSetCustomActions() {
      Map<String, CustomAction> actions = new HashMap<String, CustomAction>();
      actions.put("test", new CustomAction("test", EntityView.VIEW_SHOW));
      entityActionsManager.setCustomActions(TestData.PREFIXA1, actions);
      assertNotNull(entityActionsManager.getCustomAction(TestData.PREFIXA1, "test"));

      // NOTE: can set custom actions for entities without the ability to process them
      entityActionsManager.setCustomActions(TestData.PREFIX2, actions);

      // test using reserved word fails
      actions.clear();
      actions.put("describe", new CustomAction("describe", EntityView.VIEW_SHOW));
      try {
         entityActionsManager.setCustomActions(TestData.PREFIXA1, actions);
         fail("should have thrown exeception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#getCustomAction(java.lang.String, java.lang.String)}.
    */
   public void testGetCustomAction() {
      assertNotNull( entityActionsManager.getCustomAction(TestData.PREFIXA1, "xxx") );
      assertNotNull( entityActionsManager.getCustomAction(TestData.PREFIXA1, "double") );

      assertNotNull( entityActionsManager.getCustomAction(TestData.PREFIXA2, "xxx") );
      assertNotNull( entityActionsManager.getCustomAction(TestData.PREFIXA2, "clear") );

      assertNotNull( entityActionsManager.getCustomAction(TestData.PREFIXA3, "clear") );
      assertNotNull( entityActionsManager.getCustomAction(TestData.PREFIXA3, "double") );

      assertNull( entityActionsManager.getCustomAction(TestData.PREFIXA1, "apple") );
      assertNull( entityActionsManager.getCustomAction(TestData.PREFIX2, "action") );
      assertNull( entityActionsManager.getCustomAction(TestData.PREFIX5, "action") );
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.entityprovider.EntityProviderManagerImpl#removeCustomActions(java.lang.String)}.
    */
   public void testRemoveCustomActions() {
      assertNotNull( entityActionsManager.getCustomAction(TestData.PREFIXA1, "xxx") );
      entityActionsManager.removeCustomActions(TestData.PREFIXA1);
      assertNull( entityActionsManager.getCustomAction(TestData.PREFIXA1, "xxx") );      
   }

   public void testGetCustomActions() {
      List<CustomAction> actions = entityActionsManager.getCustomActions(TestData.PREFIXA1);
      assertNotNull(actions);
      assertEquals(3, actions.size());

      actions = entityActionsManager.getCustomActions(TestData.PREFIX3);
      assertNotNull(actions);
      assertEquals(0, actions.size());
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
