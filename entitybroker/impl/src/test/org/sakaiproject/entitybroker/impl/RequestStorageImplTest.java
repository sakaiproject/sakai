/**
 * $Id$
 * $URL$
 * RequestStorageImplTest.java - entity-broker - Aug 21, 2008 9:43:32 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import java.util.Map;

import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;
import org.sakaiproject.entitybroker.mocks.MockEBHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
        MockEBHttpServletRequest req = new MockEBHttpServletRequest("GET","/thing");
        req.setAttribute("attribNum", 135);
        req.setAttribute("attribStrNum", "135");
        req.setAttribute("attribBool", true);
        req.setAttribute("attribStrBool", "true");
        req.setAttribute("attribStr", "stuff");
        req.addParameter("paramStr", "param1");
        req.addParameter("paramArray", new String[] {"A","B","C"});
        req.addHeader("header", "Header1");
        rgi.setRequest(req);
        rgi.setResponse(new MockHttpServletResponse());
        requestStorage.setRequestGetter(rgi);
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl#getStorageMapCopy()}.
     */
    public void testGetStorageMapCopy() {
        Map<String, Object> m = requestStorage.getStorageMapCopy();
        assertNotNull(m);
        assertEquals(12, m.size()); // 8 + the 4 standard ones
        assertEquals(135, m.get("attribNum"));
        assertEquals(true, m.get("attribBool"));
        assertEquals("stuff", m.get("attribStr"));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl#getStoredValue(java.lang.String)}.
     */
    public void testGetStoredValue() {
        assertEquals(135, requestStorage.getStoredValue("attribNum"));
        assertEquals(true, requestStorage.getStoredValue("attribBool"));
        assertEquals("stuff", requestStorage.getStoredValue("attribStr"));
        assertEquals(null, requestStorage.getStoredValue("XXXXXXXXXX"));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl#getStoredValueAsType(java.lang.Class, java.lang.String)}.
     */
    public void testGetStoredValueAsType() {
        assertEquals((Integer) 135, requestStorage.getStoredValueAsType(Integer.class, "attribNum"));
        assertEquals("135", requestStorage.getStoredValueAsType(String.class, "attribNum"));
        assertEquals((Boolean) true, requestStorage.getStoredValueAsType(Boolean.class, "attribBool"));
        assertEquals((Integer) 135, requestStorage.getStoredValueAsType(Integer.class, "attribStrNum"));
        assertEquals((Boolean) true, requestStorage.getStoredValueAsType(Boolean.class, "attribStrBool"));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl#setStoredValue(java.lang.String, java.lang.Object)}.
     */
    public void testSetStoredValue() {
        assertEquals(null, requestStorage.getStoredValue("test"));
        requestStorage.setRequestValue("test", "thing");
        assertEquals("thing", requestStorage.getStoredValue("test"));
    }

}
