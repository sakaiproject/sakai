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

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import java.util.Enumeration;

/**
 * @version 1.0
 * @since Sep 15, 2004
 */
public class ContextInitParameterTest extends AbstractReflectivePortletTest  {

    private static final String TEST_PARAM_NAME = "test-parameter-name";
    private static final String TEST_PARAM_VALUE = "test-parameter-val";


    // Test Methods ------------------------------------------------------------

    protected TestResult checkEnumerationContainsNames(
    		PortletContext context) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that the expected init parameter name "
        		+ "exists in the portlet context's init parameters.");
        result.setSpecPLT("10.3.1");

        boolean found = false;
        for (Enumeration en = context.getInitParameterNames();
        		!found && en.hasMoreElements(); ) {
            String name = (String) en.nextElement();
            if (TEST_PARAM_NAME.equals(name)) {
                found = true;
            }
        }

        if (found) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Expected init parameter '"
        			+ TEST_PARAM_NAME + "' not found in portlet context.");
        }
        return result;
    }

    protected TestResult checkGetInitParameter(PortletContext context) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that init parameters are retrieveable.");
        result.setSpecPLT("10.3.1");

        String value = context.getInitParameter(TEST_PARAM_NAME);
        if (TEST_PARAM_VALUE.equals(value)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("init parameter", value, TEST_PARAM_VALUE, result);
        }
        return result;
    }

    /**
     * FIXME: should this test reside in this class?  -- ZHENG Zhong
     */
    protected TestResult checkGetContextFromSession(PortletSession session) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that the PortletContext can be retrieved "
        		+ "from the portlet session.");

        PortletContext context = session.getPortletContext();
        if (context != null) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Fail to retrieve PortletContext from "
        			+ "PortletSession: null returned.");
        }
        return result;
    }
}

