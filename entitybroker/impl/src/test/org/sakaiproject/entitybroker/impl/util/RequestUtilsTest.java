/**
 * $Id$
 * $URL$
 * RequestUtilsTest.java - entity-broker - Jul 28, 2008 7:55:59 AM - azeckoski
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

package org.sakaiproject.entitybroker.impl.util;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Order;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 * testing the request utilities
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class RequestUtilsTest extends TestCase {

   public void testMakeSearchFromRequestStorage() {
      Search search = null;
      RequestStorageImpl requestStorage = new RequestStorageImpl();
      requestStorage.setRequestGetter(new RequestGetterImpl());

      requestStorage.reset();
      search = RequestUtils.makeSearchFromRequestStorage(requestStorage);
      assertNotNull(search);
      assertTrue( search.isEmpty() );
      assertEquals(0, search.getRestrictions().length);
      search.addOrder( new Order("test") );

      requestStorage.reset();
      requestStorage.setRequestValue("test", "stuff");

      search = RequestUtils.makeSearchFromRequestStorage(requestStorage);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);

      // make sure _method is ignored
      requestStorage.reset();
      requestStorage.setRequestValue("test", "stuff");
      requestStorage.setRequestValue("_method", "PUT");

      search = RequestUtils.makeSearchFromRequestStorage(requestStorage);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);

      requestStorage.reset();
      requestStorage.setRequestValue("test", "stuff");
      requestStorage.setRequestValue("other", 1000);

      search = RequestUtils.makeSearchFromRequestStorage(requestStorage);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(2, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);
      assertNotNull( search.getRestrictionByProperty("other") );
      assertEquals(1000, search.getRestrictionByProperty("other").value);

      // test paging params
      requestStorage.reset();
      requestStorage.setRequestValue("test", "stuff");
      requestStorage.setRequestValue("_limit", "10");
      requestStorage.setRequestValue("_start", "5");

      search = RequestUtils.makeSearchFromRequestStorage(requestStorage);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);
      assertEquals(10, search.getLimit());
      assertEquals(5, search.getStart());

      requestStorage.reset();
      requestStorage.setRequestValue("test", "stuff");
      requestStorage.setRequestValue("_page", "3");
      requestStorage.setRequestValue("_perpage", "5");

      search = RequestUtils.makeSearchFromRequestStorage(requestStorage);
      assertNotNull(search);
      assertFalse( search.isEmpty() );
      assertEquals(1, search.getRestrictions().length);
      assertNotNull( search.getRestrictionByProperty("test") );
      assertEquals("stuff", search.getRestrictionByProperty("test").value);
      assertEquals(5, search.getLimit());
      assertEquals(10, search.getStart());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.util.RequestUtils#setResponseEncoding(java.lang.String, javax.servlet.http.HttpServletResponse)}.
    */
   public void testSetResponseEncoding() {
      MockHttpServletResponse res = new MockHttpServletResponse();

      RequestUtils.setResponseEncoding(Formats.XML, res);
      assertEquals(Formats.UTF_8, res.getCharacterEncoding());
      assertEquals(Formats.XML_MIME_TYPE, res.getContentType());
   }

}
