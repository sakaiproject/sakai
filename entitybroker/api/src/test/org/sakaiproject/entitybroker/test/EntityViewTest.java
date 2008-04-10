/**
 * $Id$
 * $URL$
 * EntityViewTest.java - entity-broker - Apr 10, 2008 7:20:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.test;

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
   private final String REF1 = EntityReference.SEPARATOR + PREFIX1 + EntityReference.SEPARATOR + "111";

   private final String PREFIX2 = "longprefix2";
   private final String REF2 = EntityReference.SEPARATOR + PREFIX2 + EntityReference.SEPARATOR + "222222";

   private final String PREFIX3 = "prefix3";
   private final String REF3 = EntityReference.SEPARATOR + PREFIX3;

   private final String INVALID_REF = "invalid_reference-1";

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getParseTemplate(java.lang.String)}.
    */
   public void testGetParseTemplate() {
      EntityView ev = null;

      ev = new EntityView(REF1);
      assertNotNull(ev);
      String template = ev.getParseTemplate(TemplateParseUtil.TEMPLATE_SHOW);
      assertNotNull(template);
      assertEquals(TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.TEMPLATE_SHOW), template);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#loadParseTemplates(java.util.List)}.
    */
   public void testLoadParseTemplates() {
      EntityView ev = null;

      ev = new EntityView(REF1);
      assertNotNull(ev);
      for (int i = 0; i < TemplateParseUtil.PARSE_TEMPLATE_KEYS.length; i++) {
         String template = ev.getParseTemplate(TemplateParseUtil.PARSE_TEMPLATE_KEYS[i]);
         assertNotNull( template );
      }
   }


   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getPathSegment(java.lang.String)}.
    */
   public void testGetPathSegment() {
      EntityView ev = null;

      ev = new EntityView(REF1);
      assertNotNull(ev);
      assertNotNull( ev.getPathSegment(EntityView.PREFIX) );
      assertEquals(PREFIX1, ev.getPathSegment(EntityView.PREFIX) );
      assertNotNull( ev.getPathSegment(EntityView.ID) );
      assertEquals("111", ev.getPathSegment(EntityView.ID) );
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getReference(java.lang.String)}.
    */
   public void testGetEntityUrl() {
      EntityView ev = null;

      ev = new EntityView(REF1);
      assertNotNull(ev);
      assertEquals("/" + PREFIX1, ev.getEntityUrl(TemplateParseUtil.TEMPLATE_LIST, null));
      assertEquals(REF1, ev.getEntityUrl(TemplateParseUtil.TEMPLATE_SHOW, null));
      assertEquals(REF1 + "/edit", ev.getEntityUrl(TemplateParseUtil.TEMPLATE_EDIT, null));
      assertEquals("/" + PREFIX1 + "/new", ev.getEntityUrl(TemplateParseUtil.TEMPLATE_NEW, null));

      ev = new EntityView(REF3);
      assertNotNull(ev);
      assertEquals(REF3, ev.getEntityUrl(TemplateParseUtil.TEMPLATE_LIST, null));
      try {
         assertEquals("/" + PREFIX3, ev.getEntityUrl(TemplateParseUtil.TEMPLATE_SHOW, null));
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // TODO test extension
      
      try {
         ev.getEntityUrl("xxxxxxxxxxxxxxxxxx", null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }

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

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#EntityView(java.lang.String)}.
    */
   public void testEntityViewString() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#EntityView(java.lang.String, java.util.Map, java.lang.String)}.
    */
   public void testEntityViewStringMapOfStringStringString() {
      EntityView ev = null;

      HashMap<String, String> m = new HashMap<String, String>();
      m.put(TemplateParseUtil.PREFIX, PREFIX1);
      m.put(TemplateParseUtil.ID, "111");
      ev = new EntityView(TemplateParseUtil.TEMPLATE_SHOW, m, null);
      assertNotNull(ev);
      assertEquals(PREFIX1, ev.getEntityReference().prefix);
      assertEquals("111", ev.getEntityReference().id);
      assertEquals(null, ev.getExtension());
      assertEquals(REF1, ev.toString());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#toString()}.
    */
   public void testToString() {
      //fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.EntityView#getOriginalEntityUrl()}.
    */
   public void testGetOriginalEntityUrl() {
      //fail("Not yet implemented");
   }

}
