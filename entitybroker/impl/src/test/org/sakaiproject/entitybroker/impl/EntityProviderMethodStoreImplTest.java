/**
 * $Id$
 * $URL$
 * EntityProviderMethodStoreImplTest.java - entity-broker - Jan 13, 2009 12:03:59 PM - azeckoski
 **********************************************************************************
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.URLRedirect;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.core.EntityProviderMethodStoreImpl;

/**
 * Testing the EPMSI
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityProviderMethodStoreImplTest {

    protected EntityProviderMethodStoreImpl entityProviderMethodStore;
    private TestData td;

    private URLRedirect valid1 = new URLRedirect("/{prefix}/one");
    private URLRedirect valid2 = new URLRedirect("/{prefix}/two", "/otherprefix/from/{prefix}");
    private URLRedirect valid3 = new URLRedirect("/{prefix}/three", "testMethod", new Class<?>[] {});

    @Before
    public void setUp() throws Exception {
       // setup things
       td = new TestData();

       entityProviderMethodStore = new ServiceTestManager(td).entityProviderMethodStore;
    }

    @Test
    public void testSetCustomActions() {
       Map<String, CustomAction> actions = new HashMap<String, CustomAction>();
       actions.put("test", new CustomAction("test", EntityView.VIEW_SHOW));
       entityProviderMethodStore.setCustomActions(TestData.PREFIXA1, actions);
       assertNotNull(entityProviderMethodStore.getCustomAction(TestData.PREFIXA1, "test"));

       // NOTE: can set custom actions for entities without the ability to process them
       entityProviderMethodStore.setCustomActions(TestData.PREFIX2, actions);

       // test using reserved word fails
       actions.clear();
       actions.put("describe", new CustomAction("describe", EntityView.VIEW_SHOW));
       try {
          entityProviderMethodStore.setCustomActions(TestData.PREFIXA1, actions);
          fail("should have thrown exeception");
       } catch (IllegalArgumentException e) {
          assertNotNull(e.getMessage());
       }
    }

    @Test
    public void testGetCustomAction() {
       assertNotNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA1, "xxx") );
       assertNotNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA1, "double") );

       assertNotNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA2, "xxx") );
       assertNotNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA2, "clear") );

       assertNotNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA3, "clear") );
       assertNotNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA3, "double") );

       assertNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA1, "apple") );
       assertNull( entityProviderMethodStore.getCustomAction(TestData.PREFIX2, "action") );
       assertNull( entityProviderMethodStore.getCustomAction(TestData.PREFIX5, "action") );
    }

    @Test
    public void testRemoveCustomActions() {
       assertNotNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA1, "xxx") );
       entityProviderMethodStore.removeCustomActions(TestData.PREFIXA1);
       assertNull( entityProviderMethodStore.getCustomAction(TestData.PREFIXA1, "xxx") );      
    }

    @Test
    public void testGetCustomActions() {
       List<CustomAction> actions = entityProviderMethodStore.getCustomActions(TestData.PREFIXA1);
       assertNotNull(actions);
       assertEquals(4, actions.size());

       actions = entityProviderMethodStore.getCustomActions(TestData.PREFIX3);
       assertNotNull(actions);
       assertEquals(0, actions.size());
    }

    @Test
    public void testFindURLRedirectMethods() {
       URLRedirect[] redirects = entityProviderMethodStore.findURLRedirectMethods(td.entityProviderU1);
       assertNotNull(redirects);
       assertEquals(4, redirects.length);
       assertEquals("/{prefix}/going/nowhere", redirects[0].template);
       assertEquals("returningRedirector", redirects[0].methodName);
       assertNotNull(redirects[0].methodArgTypes);
       assertEquals("/{prefix}/keep/moving", redirects[1].template);
       assertEquals("neverRedirector", redirects[1].methodName);
       assertNotNull(redirects[1].methodArgTypes);
       assertEquals("/{prefix}/xml/{id}", redirects[2].template);
       assertEquals("xmlRedirector", redirects[2].methodName);
       assertNotNull(redirects[2].methodArgTypes);
       assertEquals("/{prefix}/{id}/{thing}/go", redirects[3].template);
       assertEquals("outsideRedirector", redirects[3].methodName);
       assertNotNull(redirects[3].methodArgTypes);

       redirects = entityProviderMethodStore.findURLRedirectMethods(td.entityProvider3);
       assertNotNull(redirects);
       assertEquals(0, redirects.length);
    }

    @Test
    public void testValidateDefineableTemplates() {
       URLRedirect[] redirects = EntityProviderMethodStoreImpl.validateDefineableTemplates(td.entityProviderU2);
       assertNotNull(redirects);
       String[] templates = td.entityProviderU2.templates;
       assertEquals(templates[0], redirects[0].template);
       assertEquals(TemplateParseUtil.DIRECT_PREFIX+templates[1], redirects[0].outgoingTemplate);
       assertNotNull(redirects[0].preProcessedTemplate);
       assertNotNull(redirects[0].outgoingPreProcessedTemplate);
       assertEquals(templates[2], redirects[1].template);
       assertEquals(TemplateParseUtil.DIRECT_PREFIX+TemplateParseUtil.SEPARATOR+templates[3], redirects[1].outgoingTemplate);
       assertNotNull(redirects[1].preProcessedTemplate);
       assertNotNull(redirects[1].outgoingPreProcessedTemplate);
       assertEquals(templates[4], redirects[2].template);
       assertEquals(TemplateParseUtil.DIRECT_PREFIX+templates[5], redirects[2].outgoingTemplate);
       assertNotNull(redirects[2].preProcessedTemplate);
       assertNotNull(redirects[2].outgoingPreProcessedTemplate);
    }

    @Test
    public void testValidateControllableTemplates() {
       URLRedirect[] redirects = EntityProviderMethodStoreImpl.validateControllableTemplates(td.entityProviderU3);
       assertNotNull(redirects);
       String[] templates = td.entityProviderU3.templates;
       assertEquals(templates[0], redirects[0].template);
       assertTrue(redirects[0].controllable);
       assertEquals(templates[1], redirects[1].template);
       assertTrue(redirects[1].controllable);
       assertEquals(templates[2], redirects[2].template);
       assertTrue(redirects[2].controllable);
    }

    @Test
    public void testAddURLRedirects() {
       entityProviderMethodStore.addURLRedirects("testing", new URLRedirect[] {valid1, valid2});
       assertEquals(2, entityProviderMethodStore.getURLRedirects("testing").size());

       entityProviderMethodStore.addURLRedirects("testing", new URLRedirect[] {});
       assertEquals(2, entityProviderMethodStore.getURLRedirects("testing").size());

       entityProviderMethodStore.addURLRedirects("testing", null);
       assertEquals(2, entityProviderMethodStore.getURLRedirects("testing").size());

       entityProviderMethodStore.addURLRedirects("testing", new URLRedirect[] {valid3});
       assertEquals(3, entityProviderMethodStore.getURLRedirects("testing").size());

       // test adding the same one causes a failure
       try {
          entityProviderMethodStore.addURLRedirects("testing", new URLRedirect[] {valid3});
          fail("should have thrown exception");
       } catch (IllegalArgumentException e) {
          assertNotNull(e.getMessage());
       }
    }

    @Test
    public void testRemoveURLRedirects() {
       entityProviderMethodStore.addURLRedirects("testing", new URLRedirect[] {valid1, valid2});
       assertEquals(2, entityProviderMethodStore.getURLRedirects("testing").size());

       entityProviderMethodStore.removeURLRedirects("testing");
       assertEquals(0, entityProviderMethodStore.getURLRedirects("testing").size());
    }

    @Test
    public void testGetURLRedirects() {
       entityProviderMethodStore.addURLRedirects("testing", new URLRedirect[] {valid1, valid2});
       assertEquals(2, entityProviderMethodStore.getURLRedirects("testing").size());
       List<URLRedirect> redirects = entityProviderMethodStore.getURLRedirects("testing");
       assertEquals(valid1, redirects.get(0));
       assertEquals(valid2, redirects.get(1));
    }

}
