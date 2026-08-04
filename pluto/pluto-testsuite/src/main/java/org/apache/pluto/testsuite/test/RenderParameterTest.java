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
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;

/**
 */
public class RenderParameterTest extends AbstractReflectivePortletTest {

    private static final String ACTION_KEY = "org.apache.pluto.testsuite.PARAM_ACTION_KEY";

    private static final String RENDER_KEY = "org.apache.pluto.testsuite.PARAM_RENDER_KEY";
    private static final String RENDER_VALUE = "org.apache.pluto.testsuite.RENDER_VALUE";


    public Map getRenderParameters(PortletRequest request) {
        Map parameterMap = new HashMap();
        parameterMap.put(RENDER_KEY, new String[] { RENDER_VALUE });
        return parameterMap;
    }


    // Test Methods ------------------------------------------------------------

    protected TestResult checkActionParametersNotHere(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that action parameters are not available "
        		+ "in the following render request.");

        String value = request.getParameter(ACTION_KEY);
        if (value == null) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("parameter", value, null, result);
        }
        return result;
    }


    protected TestResult checkRenderParameterValue(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that render parameters set in action "
        		+ "response are available in the following render request.");

        String value = request.getParameter(RENDER_KEY);
        if (RENDER_VALUE.equals(value)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("parameter", value, RENDER_VALUE, result);
        }
        return result;
    }

    protected TestResult checkRenderParameterValues(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that render parameters set in action "
        		+ "response are available in the following render request.");

        String[] values = request.getParameterValues(RENDER_KEY);
        if (values != null && values.length == 1
        		&& RENDER_VALUE.equals(values[0])) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("parameter values",
        			values, new String[] { RENDER_VALUE }, result);
        }
        return result;
    }

    protected TestResult checkParameterMap(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that render request returns the correct "
        		+ "parameter map.");

        Map parameterMap = request.getParameterMap();
        String[] values = (String[]) parameterMap.get(RENDER_KEY);
        if (values != null && values.length == 1
        		&& RENDER_VALUE.equals(values[0])
        		&& !parameterMap.containsKey(ACTION_KEY)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	if (parameterMap.containsKey(ACTION_KEY)) {
            	result.setReturnCode(TestResult.FAILED);
        		result.setResultMessage("Action parameter " + ACTION_KEY
        				+ " was found in render request with value(s): "
        				+ parameterMap.get(ACTION_KEY));
        	} else {
        		TestUtils.failOnAssertion("parameter values",
        				values, new String[] { RENDER_VALUE }, result);
        	}
        }
        return result;
    }

    protected TestResult checkParameterNames(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that render request returns the correct "
        		+ "parameter names enumeration.");

        boolean hasActionParameter = false;
        boolean hasRenderParameter = false;
        for (Enumeration en = request.getParameterNames();
        		en.hasMoreElements(); ) {
            String name = (String) en.nextElement();
            if (ACTION_KEY.equals(name)) {
            	hasActionParameter = true;
            }
            if (RENDER_KEY.equals(name)) {
            	hasRenderParameter = true;
            }
        }

        if (!hasActionParameter && hasRenderParameter) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
            StringBuffer buffer = new StringBuffer();
            if (!hasRenderParameter) {
            	buffer.append("Render parameter not found. ");
            }
            if (!hasActionParameter) {
            	buffer.append("Action parameter found. ");
            }
            result.setResultMessage(buffer.toString());
        }
        return result;
    }
}
