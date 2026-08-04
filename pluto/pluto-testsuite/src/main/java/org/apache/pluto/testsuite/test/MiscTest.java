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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;

/**
 */
public class MiscTest extends AbstractReflectivePortletTest {

	// Test Methods ------------------------------------------------------------

    protected TestResult checkContextMajorVersion(PortletContext context) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the expected major version number is returned.");

        String majorVersion = String.valueOf(context.getMajorVersion());
        ExpectedResults expectedResults = ExpectedResults.getInstance();
        String expected = expectedResults.getMajorVersion();
        if (majorVersion != null && majorVersion.equals(expected)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("major version", majorVersion, expected, result);
        }
        return result;
    }

    protected TestResult checkContextMinorVersion(PortletContext context) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the expected minor version number is returned.");

        String minorVersion = String.valueOf(context.getMinorVersion());
        ExpectedResults expectedResults = ExpectedResults.getInstance();
        String expected = expectedResults.getMinorVersion();
        if (minorVersion != null && minorVersion.equals(expected)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("minor version", minorVersion, expected, result);
        }
        return result;
    }

    protected TestResult checkContextServerInfo(PortletContext context) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the expected server info is returned.");

        String serverInfo = context.getServerInfo();
        ExpectedResults expectedResults = ExpectedResults.getInstance();
        String expected = expectedResults.getServerInfo();
        if (serverInfo != null && serverInfo.equals(expected)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("server info", serverInfo, expected, result);
        }
        return result;
    }

    protected TestResult checkPortalInfo(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the expected portal info is returned.");

        String portalInfo = request.getPortalContext().getPortalInfo();
        ExpectedResults expectedResults = ExpectedResults.getInstance();
        String expected = expectedResults.getPortalInfo();
        if (portalInfo != null && portalInfo.equals(expected)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("portal info", portalInfo, expected, result);
        }
        return result;
    }

    /**
     * Test to ensure that the basic modes are supported.
     * @todo Enhance to check for custom modes.
     * @param req
     * @return
     */
    protected TestResult checkSupportedModes(PortletRequest request)  {
        TestResult result = new TestResult();
        result.setDescription("Ensure the expected portlet modes are returned.");

        List requiredPortletModes = new ArrayList();
        requiredPortletModes.add(PortletMode.VIEW);
        requiredPortletModes.add(PortletMode.EDIT);
        requiredPortletModes.add(PortletMode.HELP);

        for (Enumeration en = request.getPortalContext().getSupportedPortletModes();
        		en.hasMoreElements(); ) {
            PortletMode portletMode = (PortletMode) en.nextElement();
            requiredPortletModes.remove(portletMode);
        }

        if (requiredPortletModes.isEmpty()) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	StringBuffer buffer = new StringBuffer();
        	for (Iterator it = requiredPortletModes.iterator();
        			it.hasNext(); ) {
        		buffer.append(it.next()).append(", ");
        	}
        	result.setResultMessage("Required portlet modes ["
        			+ buffer.toString() + "] are not supported.");
        }
        return result;
    }

    protected TestResult checkSupportedWindowSates(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the expected window states are returned.");

        List requiredWindowStates = new ArrayList();
        requiredWindowStates.add(WindowState.MINIMIZED);
        requiredWindowStates.add(WindowState.MAXIMIZED);
        requiredWindowStates.add(WindowState.NORMAL);

        for (Enumeration en = request.getPortalContext().getSupportedWindowStates();
        		en.hasMoreElements(); ) {
            WindowState windowState = (WindowState) en.nextElement();
            requiredWindowStates.remove(windowState);
        }

        if (requiredWindowStates.isEmpty()) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = requiredWindowStates.iterator();
            		it.hasNext(); ) {
            	buffer.append(it.next()).append(", ");
            }
            result.setResultMessage("Required window states ["
            		+ buffer.toString() + "] are not supported.");
        }
        return result;
    }

}
