/**
 * $Id$
 * $URL$
 * EntityViewTest.java - entity-broker - Apr 10, 2008 7:20:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker;

import java.util.HashMap;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;

import junit.framework.TestCase;

/**
 * testing the entity view object
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityViewTest extends TestCase {

   private final String PREFIX1 = "prefix1";
   private final String EXTENSION1 = null;
   private static final String ID1 = "111";
   private final String URL1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR + ID1;
   private final String INPUT_URL1 = URL1;

   private final String PREFIX2 = "longprefix2";
   private final String EXTENSION2 = "html";
   private static final String ID2 = "222222";
   private final String URL2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR + ID2 + "." + EXTENSION2;
   private final String INPUT_URL2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR + ID2 
         + EntityReference.SEPARATOR + "extra/junk" + "." + EXTENSION2;

   private final String PREFIX3 = "prefix3";
   private final String EXTENSION3 = "xml";
   private final String URL3 = EntityReference.SEPARATOR + PREFIX3 + "." + EXTENSION3;
   private final String INPUT_URL3 = URL3;

   private final String INVALID_URL = "invalid_reference-1";


   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#EntityView()}.
    */
   public void testEntityView() {
      EntityView ev = null;

      // make sure this does not die
      ev = new EntityView();
      assertNotNull(ev);
      // check it loaded the default templates
      assertNotNull(ev.getParseTemplate(TemplateParseUtil.TEMPLATE_SHOW));
   }

   public void testEntityViewReference() {
      EntityView ev = null;

      // make sure this does not die
      ev = new EntityView();
      ev.setEntityReference( new EntityReference("/mystuff") );
      assertNotNull(ev);
      assertEquals(EntityView.VIEW_LIST, ev.getViewKey());
      assertEquals("mystuff", ev.getEntityReference().getPrefix());
      assertEquals(null, ev.getEntityReference().getId());
      assertEquals(null, ev.getExtension());
      assertEquals("/mystuff", ev.toString());
      assertEquals(null, ev.getOriginalEntityUrl());

      ev = new EntityView();
      ev.setEntityReference( new EntityReference("/mystuff/myid") );
      assertNotNull(ev);
      assertEquals(EntityView.VIEW_SHOW, ev.getViewKey());
      assertEquals("mystuff", ev.getEntityReference().getPrefix());
      assertEquals("myid", ev.getEntityReference().getId());
      assertEquals(null, ev.getExtension());
      assertEquals("/mystuff/myid", ev.toString());
      assertEquals(null, ev.getOriginalEntityUrl());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#EntityView(java.lang.String, java.util.Map, java.lang.String)}.
    */
   public void testEntityViewStringMapOfStringStringString() {
      EntityView ev = null;

      HashMap<String, String> m = new HashMap<String, String>();
      m.put(TemplateParseUtil.PREFIX, PREFIX1);
      m.put(TemplateParseUtil.ID, ID1);
      ev = new EntityView(TemplateParseUtil.TEMPLATE_SHOW, m, EXTENSION1);
      assertNotNull(ev);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, ev.getViewKey());
      assertEquals(PREFIX1, ev.getEntityReference().getPrefix());
      assertEquals(ID1, ev.getEntityReference().getId());
      assertEquals(EXTENSION1, ev.getExtension());
      assertEquals(URL1, ev.toString());
      assertEquals(null, ev.getOriginalEntityUrl());

      m = new HashMap<String, String>();
      m.put(TemplateParseUtil.PREFIX, PREFIX2);
      m.put(TemplateParseUtil.ID, ID2);
      ev = new EntityView(TemplateParseUtil.TEMPLATE_SHOW, m, EXTENSION2);
      assertNotNull(ev);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, ev.getViewKey());
      assertEquals(PREFIX2, ev.getEntityReference().getPrefix());
      assertEquals(ID2, ev.getEntityReference().getId());
      assertEquals(EXTENSION2, ev.getExtension());
      assertEquals(URL2, ev.toString());
      assertEquals(null, ev.getOriginalEntityUrl());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#EntityView(java.lang.String)}.
    */
   public void testEntityViewString() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, ev.getViewKey());
      assertEquals(PREFIX1, ev.getEntityReference().getPrefix());
      assertEquals(ID1, ev.getEntityReference().getId());
      assertEquals(EXTENSION1, ev.getExtension());
      assertEquals(URL1, ev.toString());
      assertEquals(INPUT_URL1, ev.getOriginalEntityUrl());

      ev = new EntityView(INPUT_URL2);
      assertNotNull(ev);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, ev.getViewKey());
      assertEquals(PREFIX2, ev.getEntityReference().getPrefix());
      assertEquals(ID2, ev.getEntityReference().getId());
      assertEquals(EXTENSION2, ev.getExtension());
      assertEquals(URL2, ev.toString());
      assertEquals(INPUT_URL2, ev.getOriginalEntityUrl());

      ev = new EntityView(INPUT_URL3);
      assertNotNull(ev);
      assertEquals(TemplateParseUtil.TEMPLATE_LIST, ev.getViewKey());
      assertEquals(PREFIX3, ev.getEntityReference().getPrefix());
      assertEquals(null, ev.getEntityReference().getId());
      assertEquals(EXTENSION3, ev.getExtension());
      assertEquals(URL3, ev.toString());
      assertEquals(INPUT_URL3, ev.getOriginalEntityUrl());

      // invalid url causes exception
      try {
         ev = new EntityView(INVALID_URL);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#loadParseTemplates(java.util.List)}.
    */
   public void testLoadParseTemplates() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      for (int i = 0; i < TemplateParseUtil.PARSE_TEMPLATE_KEYS.length; i++) {
         String template = ev.getParseTemplate(TemplateParseUtil.PARSE_TEMPLATE_KEYS[i]);
         assertNotNull( template );
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#toString()}.
    */
   public void testToString() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      assertEquals("/" + PREFIX1, ev.getEntityURL(TemplateParseUtil.TEMPLATE_LIST, null));
      assertEquals(URL1, ev.getEntityURL(TemplateParseUtil.TEMPLATE_SHOW, null));
      assertEquals(URL1 + "/edit", ev.getEntityURL(TemplateParseUtil.TEMPLATE_EDIT, null));
      assertEquals("/" + PREFIX1 + "/new", ev.getEntityURL(TemplateParseUtil.TEMPLATE_NEW, null));
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getParseTemplate(java.lang.String)}.
    */
   public void testGetParseTemplate() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      String template = ev.getParseTemplate(TemplateParseUtil.TEMPLATE_SHOW);
      assertNotNull(template);
      assertEquals(TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.TEMPLATE_SHOW), template);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getPathSegment(java.lang.String)}.
    */
   public void testGetPathSegment() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      assertNotNull( ev.getPathSegment(EntityView.PREFIX) );
      assertEquals(PREFIX1, ev.getPathSegment(EntityView.PREFIX) );
      assertNotNull( ev.getPathSegment(EntityView.ID) );
      assertEquals(ID1, ev.getPathSegment(EntityView.ID) );
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getOriginalEntityUrl()}.
    */
   public void testGetOriginalEntityUrl() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL2);
      assertEquals(URL2, ev.toString());
      assertEquals(INPUT_URL2, ev.getOriginalEntityUrl());

      ev = new EntityView("/myprefix/myid/extra");
      assertEquals("/myprefix/myid", ev.toString());
      assertEquals("/myprefix/myid/extra", ev.getOriginalEntityUrl());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getReference(java.lang.String)}.
    */
   public void testGetEntityUrl() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      assertEquals("/" + PREFIX1, ev.getEntityURL(TemplateParseUtil.TEMPLATE_LIST, null));
      assertEquals(URL1, ev.getEntityURL(TemplateParseUtil.TEMPLATE_SHOW, null));
      assertEquals(URL1 + "/edit", ev.getEntityURL(TemplateParseUtil.TEMPLATE_EDIT, null));
      assertEquals("/" + PREFIX1 + "/new", ev.getEntityURL(TemplateParseUtil.TEMPLATE_NEW, null));

      ev = new EntityView(INPUT_URL3);
      assertNotNull(ev);
      assertEquals(URL3, ev.getEntityURL(TemplateParseUtil.TEMPLATE_LIST, EXTENSION3));
      assertEquals("/" + PREFIX3, ev.getEntityURL(TemplateParseUtil.TEMPLATE_LIST, null));
      assertEquals("/" + PREFIX3, ev.getEntityURL(TemplateParseUtil.TEMPLATE_SHOW, null));

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      ev.setViewKey(EntityView.VIEW_DELETE);
      assertEquals(URL1 + "/delete", ev.getEntityURL());
      ev.setViewKey(EntityView.VIEW_EDIT);
      assertEquals(URL1 + "/edit", ev.getEntityURL());
      ev.setViewKey(EntityView.VIEW_LIST);
      assertEquals("/" + PREFIX1, ev.getEntityURL());
      ev.setViewKey(EntityView.VIEW_NEW);
      assertEquals("/" + PREFIX1 + "/new", ev.getEntityURL());
      ev.setViewKey(EntityView.VIEW_SHOW);
      assertEquals(URL1, ev.getEntityURL());

      try {
         ev.getEntityURL("xxxxxxxxxxxxxxxxxx", null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   public void testGetPathSegmentInt() {
      EntityView ev = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      assertEquals(PREFIX1, ev.getPathSegment(0));
      assertEquals(ID1, ev.getPathSegment(1));
      assertEquals(null, ev.getPathSegment(2));

      ev = new EntityView(INPUT_URL2);
      assertNotNull(ev);
      assertEquals(PREFIX2, ev.getPathSegment(0));
      assertEquals(ID2, ev.getPathSegment(1));
      assertEquals("extra", ev.getPathSegment(2));
      assertEquals("junk", ev.getPathSegment(3));
      assertEquals(null, ev.getPathSegment(4));

      ev = new EntityView("/myprefix/action.xml");
      assertNotNull(ev);
      assertEquals("myprefix", ev.getPathSegment(0) );
      assertEquals("action", ev.getPathSegment(1) );
      assertEquals(null, ev.getPathSegment(2) );

      ev = new EntityView("/myprefix/myid/action.xml");
      assertNotNull(ev);
      assertEquals("myprefix", ev.getPathSegment(0) );
      assertEquals("myid", ev.getPathSegment(1) );
      assertEquals("action", ev.getPathSegment(2) );
      assertEquals(null, ev.getPathSegment(3) );

      // SAK-16975
      ev = new EntityView("/myprefix/myid/stuff/this has spaces.csv");
      assertNotNull(ev);
      assertEquals("myprefix", ev.getPathSegment(0) );
      assertEquals("myid", ev.getPathSegment(1) );
      assertEquals("stuff", ev.getPathSegment(2) );
      assertEquals("this has spaces", ev.getPathSegment(3) );
      assertEquals(null, ev.getPathSegment(4) );

      ev = new EntityView("/myprefix/myid/stuff/this_is_a_site.with.periods.in.the.name");
      assertNotNull(ev);
      assertEquals("myprefix", ev.getPathSegment(0) );
      assertEquals("myid", ev.getPathSegment(1) );
      assertEquals("stuff", ev.getPathSegment(2) );
      assertEquals("this_is_a_site.with.periods.in.the.name", ev.getPathSegment(3) );
      assertEquals(null, ev.getPathSegment(4) );

   }

   public void testGetPathSegments() {
      EntityView ev = null;
      String[] segments = null;

      ev = new EntityView(INPUT_URL1);
      assertNotNull(ev);
      segments = ev.getPathSegments();
      assertNotNull(segments);
      assertEquals(PREFIX1, segments[0]);
      assertEquals(ID1, segments[1]);
      
      ev = new EntityView("/myprefix/myid/action.xml");
      assertNotNull(ev);
      segments = ev.getPathSegments();
      assertNotNull(segments);
      assertEquals("myprefix", segments[0] );
      assertEquals("myid", segments[1] );
      assertEquals("action", segments[2] );
   }

   public void testNPEwhenGetSegments() {
       EntityView view = new EntityView(new EntityReference("test", ""), EntityView.VIEW_LIST, null);
       String[] segments = view.getPathSegments();
       assertNotNull(segments);
       assertEquals("test", view.getPathSegment(0));
       assertEquals(null, view.getPathSegment(1));
   }

}
