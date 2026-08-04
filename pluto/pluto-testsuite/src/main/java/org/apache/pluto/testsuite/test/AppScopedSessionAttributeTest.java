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

import javax.portlet.PortletSession;

/**
 */
public class AppScopedSessionAttributeTest
extends AbstractReflectivePortletTest {

    private static final String BOGUS_KEY = "org.apache.pluto.testsuite.BOGUS_KEY";
    private static final String KEY = "org.apache.pluto.testsuite.KEY";
    private static final String VALUE = "VALUE";


    // Test Methods ------------------------------------------------------------

    protected TestResult checkGetEmptyAppScopedAttribute(PortletSession session) {
        TestResult result = new TestResult();
        result.setDescription("Retrieve an attribute that has not been set "
        		+ "in the session's application scope "
        		+ "and ensure it's value is null.");
        result.setSpecPLT("15.3");

        Object value = session.getAttribute(BOGUS_KEY, PortletSession.APPLICATION_SCOPE);
        if (value == null) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("session attribute", value, null, result);
        }
        return result;
    }

    protected TestResult checkSetAppScopedAttribute(PortletSession session) {
        TestResult result = new TestResult();
        result.setDescription("Set an application scoped session attribute "
        		+ "and ensure it's retrievable.");
        result.setSpecPLT("15.3");

        session.setAttribute(KEY, VALUE, PortletSession.APPLICATION_SCOPE);
        Object value = session.getAttribute(KEY, PortletSession.APPLICATION_SCOPE);
        if (VALUE.equals(value)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("session attribute", value, VALUE, result);
        }
        return result;
    }

    protected TestResult checkRemoveAppScopedAttribute(PortletSession session) {
        TestResult result = new TestResult();
        result.setDescription("Remove an application scoped session attribute "
        		+ "and ensure it's null.");
        result.setSpecPLT("15.3");

        session.setAttribute(KEY, VALUE, PortletSession.APPLICATION_SCOPE);
        session.removeAttribute(KEY, PortletSession.APPLICATION_SCOPE);
        Object value = session.getAttribute(KEY, PortletSession.APPLICATION_SCOPE);
        if (value == null) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("session attribute", value, null, result);
        }
        return result;
    }

}
