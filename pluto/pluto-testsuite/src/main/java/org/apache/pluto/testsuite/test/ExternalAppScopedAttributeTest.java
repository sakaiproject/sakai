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

import java.io.IOException;

import org.apache.pluto.testsuite.TestResult;

import javax.portlet.PortletSession;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 */
public class ExternalAppScopedAttributeTest extends AbstractReflectivePortletTest {

    public static final String INT_KEY = "org.apache.pluto.testsuite.INTERNALLY_SET_APP_SCOPED_SESSION_TEST_KEY";
    public static final String EXT_KEY = "org.apache.pluto.testsuite.EXTERNALLY_SET_APP_SCOPED_SESSION_TEST_KEY";
    public static final String VALUE = "Should be visible to all Portlets and Web Resources.";


    // Test Methods ------------------------------------------------------------

    protected TestResult checkSetAppScopedAttributeHereSeenElsewhere(
    		PortletSession session) {
        TestResult result = new TestResult();
        result.setDescription("Ensure application scoped attributes set here "
        		+ "in portlet session can be seen elsewhere.");

        session.setAttribute(INT_KEY, VALUE, PortletSession.APPLICATION_SCOPE);
        result.setReturnCode(TestResult.WARNING);
        result.setResultMessage("Click the provided link to validate test.");
        return result;
    }

    protected TestResult checkSetAppScopedAttributeElsewhereSeenHere(
    		PortletSession session) {
        TestResult result = new TestResult();
        result.setDescription("Ensure application scoped attributes set "
        		+ "elsewhere in portlet session can be seen here.");

        Object value = session.getAttribute(EXT_KEY,
                                            PortletSession.APPLICATION_SCOPE);
        if (VALUE.equals(value)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("This test will not pass until you have "
        			+ "opened the external resource using the link provided below.");
        }
        return result;
    }


    // Nested Servlet Class ----------------------------------------------------

    /**
     * The companion servlet that cooperates with this portlet test.
     */
    public static class CompanionServlet extends HttpServlet {

        public void doGet(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException, IOException {
            HttpSession session = request.getSession();
            String value = (String) session.getAttribute(INT_KEY);
            if (ExternalAppScopedAttributeTest.VALUE.equals(value)) {
            	request.setAttribute("passed", new Boolean(true));
                session.setAttribute(EXT_KEY, VALUE);
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher(
            		"/jsp/ExternalAppScopedAttributeTest_companion.jsp");
            dispatcher.forward(request, response);
        }

    }

}
