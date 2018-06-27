/**
 * $Id$
 * $URL$
 * RequestUtilsTest.java - entity-broker - Jul 28, 2008 7:55:59 AM - azeckoski
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

package org.sakaiproject.entitybroker.util.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Order;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;
import org.sakaiproject.entitybroker.util.request.RequestUtils;

/**
 * testing the request utilities
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class RequestUtilsTest extends TestCase {

   public void testMakeSearchFromRequestStorage() {
      Search search = null;
      Map<String, Object> params = new HashMap<String, Object>();

      params.clear();
      search = RequestUtils.makeSearchFromRequestParams(params);
      assertNotNull(search);
      assertTrue( search.isEmpty() );
      assertEquals(0, search.getRestrictions().length);
      search.addOrder( new Order("test") );

      params.clear();
      params.put("test", "stuff");

      search = RequestUtils.makeSearchFromRequestParams(params);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);

      // make sure _method is ignored
      params.clear();
      params.put("test", "stuff");
      params.put("_method", "PUT");

      search = RequestUtils.makeSearchFromRequestParams(params);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);

      params.clear();
      params.put("test", "stuff");
      params.put("other", 1000);

      search = RequestUtils.makeSearchFromRequestParams(params);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(2, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);
      assertNotNull( search.getRestrictionByProperty("other") );
      assertEquals(1000, search.getRestrictionByProperty("other").value);

      // test paging params
      params.clear();
      params.put("test", "stuff");
      params.put("_limit", "10");
      params.put("_start", "5");

      search = RequestUtils.makeSearchFromRequestParams(params);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);
      assertEquals(15, search.getLimit());
      assertEquals(5, search.getStart());

      params.clear();
      params.put("test", "stuff");
      params.put("_page", "3");
      params.put("_perpage", "5");

      search = RequestUtils.makeSearchFromRequestParams(params);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);
      assertEquals(15, search.getLimit());
      assertEquals(10, search.getStart());
   }

   public void testMakeSearchFromRequestStorageOrder() {
       Search search = null;
       Map<String, Object> params = new HashMap<String, Object>();

       params.clear();
       params.put("test", "stuff");
       params.put("_order", "name");

       search = RequestUtils.makeSearchFromRequestParams(params);
       assertNotNull(search);
       assertFalse( search.isEmpty() );
       assertEquals(1, search.getRestrictions().length);
       assertNotNull( search.getRestrictionByProperty("test") );
       assertEquals("stuff", search.getRestrictionByProperty("test").value);
       assertEquals(10, search.getLimit());
       assertEquals(0, search.getStart());
       assertEquals(1, search.getOrders().length);
       assertEquals("name", search.getOrders()[0].getProperty());
       assertEquals(true, search.getOrders()[0].isAscending());

       params.clear();
       params.put("test", "stuff");
       params.put("_order", "name_reverse");

       search = RequestUtils.makeSearchFromRequestParams(params);
       assertNotNull(search);
       assertFalse( search.isEmpty() );
       assertEquals(1, search.getRestrictions().length);
       assertNotNull( search.getRestrictionByProperty("test") );
       assertEquals("stuff", search.getRestrictionByProperty("test").value);
       assertEquals(10, search.getLimit());
       assertEquals(0, search.getStart());
       assertEquals(1, search.getOrders().length);
       assertEquals("name", search.getOrders()[0].getProperty());
       assertEquals(false, search.getOrders()[0].isAscending());

       params.clear();
       params.put("test", "stuff");
       params.put("_order", "name,email");

       search = RequestUtils.makeSearchFromRequestParams(params);
       assertNotNull(search);
       assertFalse( search.isEmpty() );
       assertEquals(1, search.getRestrictions().length);
       assertNotNull( search.getRestrictionByProperty("test") );
       assertEquals("stuff", search.getRestrictionByProperty("test").value);
       assertEquals(10, search.getLimit());
       assertEquals(0, search.getStart());
       assertEquals(2, search.getOrders().length);
       assertEquals("name", search.getOrders()[0].getProperty());
       assertEquals(true, search.getOrders()[0].isAscending());
       assertEquals("email", search.getOrders()[1].getProperty());
       assertEquals(true, search.getOrders()[1].isAscending());

       params.clear();
       params.put("test", "stuff");
       params.put("_order", "name,email_desc,phone_asc");

       search = RequestUtils.makeSearchFromRequestParams(params);
       assertNotNull(search);
       assertFalse( search.isEmpty() );
       assertEquals(1, search.getRestrictions().length);
       assertNotNull( search.getRestrictionByProperty("test") );
       assertEquals("stuff", search.getRestrictionByProperty("test").value);
       assertEquals(10, search.getLimit());
       assertEquals(0, search.getStart());
       assertEquals(3, search.getOrders().length);
       assertEquals("name", search.getOrders()[0].getProperty());
       assertEquals(true, search.getOrders()[0].isAscending());
       assertEquals("email", search.getOrders()[1].getProperty());
       assertEquals(false, search.getOrders()[1].isAscending());
       assertEquals("phone", search.getOrders()[2].getProperty());
       assertEquals(true, search.getOrders()[2].isAscending());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.util.request.RequestUtils#setResponseEncoding(java.lang.String, javax.servlet.http.HttpServletResponse)}.
    */
   public void testSetResponseEncoding() {
      EntityHttpServletResponse res = new EntityHttpServletResponse();

      RequestUtils.setResponseEncoding(Formats.XML, res);
      assertEquals(Formats.UTF_8, res.getCharacterEncoding());
      assertEquals(Formats.XML_MIME_TYPE, res.getContentType());
   }

   public void testShortComparator() {
       ArrayList<String> l = new ArrayList<String>();
       l.add("E");
       l.add("DD");
       l.add("BBBBB");
       l.add("CCC");
       l.add("AAAAAAAAAA");

       Collections.sort(l, new RequestUtils.ShortestStringLastComparator());

       assertNotNull(l);
       assertEquals("AAAAAAAAAA", l.get(0));
       assertEquals("BBBBB", l.get(1));
       assertEquals("CCC", l.get(2));
       assertEquals("DD", l.get(3));
       assertEquals("E", l.get(4));
   }

   public void testFindAndHandleFormat() {
       EntityHttpServletRequest req = new EntityHttpServletRequest("/stuff/111.xml");
       EntityHttpServletResponse res = new EntityHttpServletResponse();
       String format = null;

       req = new EntityHttpServletRequest("/stuff/111.xml");
       res = new EntityHttpServletResponse();
       format = RequestUtils.findAndHandleFormat(req, res, null);
       assertNotNull(format);
       assertEquals(Formats.XML, format);

       req = new EntityHttpServletRequest("/stuff/111.html");
       res = new EntityHttpServletResponse();
       format = RequestUtils.findAndHandleFormat(req, res, null);
       assertNotNull(format);
       assertEquals(Formats.HTML, format);

       req = new EntityHttpServletRequest("/stuff/111.json");
       res = new EntityHttpServletResponse();
       format = RequestUtils.findAndHandleFormat(req, res, null);
       assertNotNull(format);
       assertEquals(Formats.JSON, format);

       req = new EntityHttpServletRequest("/stuff/111");
       res = new EntityHttpServletResponse();
       format = RequestUtils.findAndHandleFormat(req, res, null);
       assertNotNull(format);
       assertEquals(Formats.HTML, format);

       // test the accept header works
       req = new EntityHttpServletRequest("/stuff/111");
       req.addHeader("Accept", Formats.XML_MIME_TYPE);
       res = new EntityHttpServletResponse();
       format = RequestUtils.findAndHandleFormat(req, res, null);
       assertNotNull(format);
       assertEquals(Formats.XML, format);

       req = new EntityHttpServletRequest("/stuff/111");
       req.addHeader("Accept", Formats.JSON_MIME_TYPE, Formats.TXT_MIME_TYPE, "text/*", "*/*");
       res = new EntityHttpServletResponse();
       format = RequestUtils.findAndHandleFormat(req, res, null);
       assertNotNull(format);
       assertEquals(Formats.JSON, format);

       req = new EntityHttpServletRequest("/stuff/111");
       req.addHeader("Accept", "fakey/*", "something/thing", "*/*");
       res = new EntityHttpServletResponse();
       format = RequestUtils.findAndHandleFormat(req, res, null);
       assertNotNull(format);
       assertEquals(Formats.HTML, format);
   }

}
