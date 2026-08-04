/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.testsuite.test;

import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;

import java.util.Enumeration;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

/**
 * Tests basic attribute retrieval and storage functions within the portlet
 * request, session, and context objects.
 *
 */
public class SimpleAttributeTest extends AbstractReflectivePortletTest {

    private static final String KEY = "org.apache.pluto.testsuite.BOGUS_KEY";
    private static final String VAL = "! TEST VAL !";


    // Test Methods ------------------------------------------------------------

    protected TestResult checkGetNullAttribute(PortletRequest req) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that if an attribute bound to an invalid "
        		+ "key is retrieved, null is returned.");

        Object val = req.getAttribute(KEY);
        if (val == null) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("unbound attribute", val, null, result);
        }
        return result;
    }


    protected TestResult checkSetAttribute(PortletRequest req) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that attributes can be set to "
        		+ "portlet request.");

        req.setAttribute(KEY, VAL);
        Object val = req.getAttribute(KEY);
        if (VAL.equals(val)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("attribute", val, VAL, result);
        }

        req.removeAttribute(KEY);
        return result;
    }

    protected TestResult checkRemoveAttribute(PortletRequest req) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that attributes can be removed from "
        		+ "portlet request.");

        req.setAttribute(KEY, VAL);
        req.removeAttribute(KEY);
        Object val = req.getAttribute(KEY);
        if (val == null) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("removed attribute", val, null, result);
        }
        return result;
    }

    protected TestResult checkEnumerateAttributes(PortletRequest req) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that all attribute names appear in the "
        		+ "attribute name enumeration returned by portlet request.");

        int count = 5;
        for (int i = 0; i < count; i++) {
            req.setAttribute(KEY + "." + i, VAL);
        }

        int found = 0;
        for (Enumeration en = req.getAttributeNames();
        		en.hasMoreElements(); ) {
            if (en.nextElement().toString().startsWith(KEY)) {
                found++;
            }
        }

        if (count == found) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("count of attribute names",
        			String.valueOf(found), String.valueOf(count), result);
        }
        return result;
    }


    // Test Methods for Session Attributes -------------------------------------

    protected TestResult checkGetNullAttribute(PortletSession session) {
        TestResult res = new TestResult();
        res.setName("Retrieve Missing Session Attribute Test");
        res.setDescription("Retrieves an attribute bound to an invalid key set are retrieved as null");

        Object val = session.getAttribute(KEY);
        if(val != null) {
            res.setReturnCode(TestResult.FAILED);
            res.setResultMessage("Retrieved value: '"+val+"' for attribute '"+KEY+"'");
        }
        else {
            res.setReturnCode(TestResult.PASSED);
        }
        return res;
    }

    protected TestResult checkSetAttribute(PortletSession session) {
        TestResult res = new TestResult();
        res.setName("Set Attribute Test");
        res.setDescription("Sets and retrieves portlet sessionuest attribute.");

        session.setAttribute(KEY, VAL);
        Object val = session.getAttribute(KEY);
        if(!VAL.equals(val)) {
            res.setReturnCode(TestResult.FAILED);
            res.setResultMessage("Retrieved value: '"+val+"' - Expected '"+VAL+"'");
        }
        else {
            res.setReturnCode(TestResult.PASSED);
        }

        session.removeAttribute(KEY);
        return res;
    }

    protected TestResult checkRemoveAttribute(PortletSession session) {
        TestResult res = new TestResult();
        res.setName("Remove Session Attribute Test");
        res.setDescription("Sets, removes and retrieves portlet request attribute.");

        session.setAttribute(KEY, VAL);
        session.removeAttribute(KEY);
        Object val = session.getAttribute(KEY);
        if(val!=null) {
            res.setReturnCode(TestResult.FAILED);
            res.setResultMessage("Retrieved value: '"+val+"' - Expected '"+VAL+"'");
        }
        else {
            res.setReturnCode(TestResult.PASSED);
        }

        return res;
    }

    protected TestResult checkEnumerateAttributes(PortletSession session) {

        TestResult result = new TestResult();
        result.setDescription("Sets session attributes and enumerates over them.");

        int count = 5;
        for (int i = 0; i < count; i++) {
        	session.setAttribute(KEY + "." + i, VAL);
        }

        int found = 0;
        for (Enumeration en = session.getAttributeNames();
        		en.hasMoreElements(); ) {
        	String name = (String) en.nextElement();
            if (name.startsWith(KEY)) {
                found++;
            }
        }

        if (count != found) {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Expected " + count + " attributes. "
        			+ "Found " + found);
        } else {
        	result.setReturnCode(TestResult.PASSED);
        }
        return result;
    }

//
// Context Tests
//

    protected TestResult checkGetNullAttribute(PortletContext context) {
        TestResult res = new TestResult();
        res.setName("Retrieve Missing Context Attribute Test");
        res.setDescription("Retrieves an attribute bound to an invalid key set are retrieved as null");

        Object val = context.getAttribute(KEY);
        if(val != null) {
            res.setReturnCode(TestResult.FAILED);
            res.setResultMessage("Retrieved value: '"+val+"' for attribute '"+KEY+"'");
        }
        else {
            res.setReturnCode(TestResult.PASSED);
        }
        return res;
    }

    protected TestResult checkSetAttribute(PortletContext context) {
        TestResult res = new TestResult();
        res.setName("Set Attribute Test");
        res.setDescription("Sets and retrieves portlet contextuest attribute.");

        context.setAttribute(KEY, VAL);
        Object val = context.getAttribute(KEY);
        if(!VAL.equals(val)) {
            res.setReturnCode(TestResult.FAILED);
            res.setResultMessage("Retrieved value: '"+val+"' - Expected '"+VAL+"'");
        }
        else {
            res.setReturnCode(TestResult.PASSED);
        }

        context.removeAttribute(KEY);
        return res;
    }

    protected TestResult checkRemoveAttribute(PortletContext context) {
        TestResult res = new TestResult();
        res.setName("Remove Context Attribute Test");
        res.setDescription("Sets, removes and retrieves portlet request attribute.");

        context.setAttribute(KEY, VAL);
        context.removeAttribute(KEY);
        Object val = context.getAttribute(KEY);
        if(val!=null) {
            res.setReturnCode(TestResult.FAILED);
            res.setResultMessage("Retrieved value: '"+val+"' - Expected '"+VAL+"'");
        }
        else {
            res.setReturnCode(TestResult.PASSED);
        }

        return res;
    }

    protected TestResult checkEnumerateAttributesInContext(
    		PortletContext context) {
        TestResult result = new TestResult();
        result.setDescription("Sets attributes in portlet context "
        		+ "and enumerates over them.");

        int count = 5;
        for (int i = 0; i < count; i++) {
        	context.setAttribute(KEY + "." + i, VAL);
        }

        int found = 0;
        for (Enumeration en = context.getAttributeNames();
        		en.hasMoreElements(); ) {
            if (en.nextElement().toString().startsWith(KEY)) {
                found++;
            }
        }

        if (count == found) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Expected " + count + " attributes. "
        			+ "Found " + found);
        }
        return result;
    }

}
