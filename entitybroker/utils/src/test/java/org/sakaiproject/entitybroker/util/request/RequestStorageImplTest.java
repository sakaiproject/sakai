/**
 * $Id$
 * $URL$
 * RequestStorageImplTest.java - entity-broker - Aug 21, 2008 9:43:32 PM - azeckoski
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

import java.util.Map;

import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;
import org.sakaiproject.entitybroker.util.request.RequestGetterImpl;
import org.sakaiproject.entitybroker.util.request.RequestStorageImpl;

import junit.framework.TestCase;


/**
 * Testing request storage 
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class RequestStorageImplTest extends TestCase {

    private RequestStorageImpl requestStorage;
    
    @Override
    protected void setUp() throws Exception {
        requestStorage = new RequestStorageImpl();
        RequestGetterImpl rgi = new RequestGetterImpl();
        EntityHttpServletRequest req = new EntityHttpServletRequest("GET", "/thing");
        req.setAttribute("attribNum", 135);
        req.setAttribute("attribStrNum", "135");
        req.setAttribute("attribBool", true);
        req.setAttribute("attribStrBool", "true");
        req.setAttribute("attribStr", "stuff");
        req.addParameter("paramStr", "param1");
        req.addParameter("paramArray", new String[] {"A","B","C"});
        req.addHeader("header", "Header1");
        rgi.setRequest(req);
        rgi.setResponse(new EntityHttpServletResponse());
        requestStorage.setRequestGetter(rgi);
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.request.RequestStorageImpl#getStorageMapCopy()}.
     */
    public void testGetStorageMapCopy() {
        Map<String, Object> m = requestStorage.getStorageMapCopy();
        assertNotNull(m);
        assertEquals(12, m.size()); // 8 + the 4 standard ones
        assertEquals(135, m.get("attribNum"));
        assertEquals(true, m.get("attribBool"));
        assertEquals("stuff", m.get("attribStr"));
    }

    public void testGetStorageMapCopyParams() {
        Map<String, Object> m = requestStorage.getStorageMapCopy(true, false, false, false);
        assertNotNull(m);
        assertEquals(4, m.size()); // 8 + the 4 standard ones

        m = requestStorage.getStorageMapCopy(true, true, false, false);
        assertNotNull(m);
        assertEquals(5, m.size()); // 8 + the 4 standard ones

        m = requestStorage.getStorageMapCopy(true, true, true, false);
        assertNotNull(m);
        assertEquals(7, m.size()); // 8 + the 4 standard ones

        m = requestStorage.getStorageMapCopy(true, true, true, true);
        assertNotNull(m);
        assertEquals(12, m.size()); // 8 + the 4 standard ones
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.request.RequestStorageImpl#getStoredValue(java.lang.String)}.
     */
    public void testGetStoredValue() {
        assertEquals(135, requestStorage.getStoredValue("attribNum"));
        assertEquals(true, requestStorage.getStoredValue("attribBool"));
        assertEquals("stuff", requestStorage.getStoredValue("attribStr"));
        assertEquals(null, requestStorage.getStoredValue("XXXXXXXXXX"));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.request.RequestStorageImpl#getStoredValueAsType(java.lang.Class, java.lang.String)}.
     */
    public void testGetStoredValueAsType() {
        assertEquals((Integer) 135, requestStorage.getStoredValueAsType(Integer.class, "attribNum"));
        assertEquals("135", requestStorage.getStoredValueAsType(String.class, "attribNum"));
        assertEquals((Boolean) true, requestStorage.getStoredValueAsType(Boolean.class, "attribBool"));
        assertEquals((Integer) 135, requestStorage.getStoredValueAsType(Integer.class, "attribStrNum"));
        assertEquals((Boolean) true, requestStorage.getStoredValueAsType(Boolean.class, "attribStrBool"));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.request.RequestStorageImpl#setStoredValue(java.lang.String, java.lang.Object)}.
     */
    public void testSetStoredValue() {
        assertEquals(null, requestStorage.getStoredValue("test"));
        requestStorage.setRequestValue("test", "thing");
        assertEquals("thing", requestStorage.getStoredValue("test"));
    }

}
