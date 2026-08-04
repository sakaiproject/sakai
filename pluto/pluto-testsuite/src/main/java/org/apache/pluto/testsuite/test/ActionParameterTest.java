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

import java.util.Enumeration;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.pluto.testsuite.ActionTest;
import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;

/**
 */
public class ActionParameterTest extends AbstractReflectivePortletTest
implements ActionTest {

    public static final String KEY_NO_VAL = "org.apache.pluto.testsuite.PARAM_ACTION_KEY_NO_VAL";
    public static final String NO_VAL = "";
    
	/** Parameter key encoded in the action URL. */
    public static final String KEY = "org.apache.pluto.testsuite.PARAM_ACTION_KEY";
    /** Parameter value encoded in the action URL. */
    public static final String VALUE = "org.apache.pluto.testsuite.ACTION_VALUE";
    
    public static final String MULTI_KEY = "org.apache.pluto.testsuite.PARAM_ACTION_MULTI_KEY";
    public static final String VALUE_A = "org.apache.pluto.testsuite.ACTION_MULTI_VALUE_A";
    public static final String VALUE_B = "org.apache.pluto.testsuite.ACTION_MULTI_VALUE_B";


    // Test Methods ------------------------------------------------------------

    protected TestResult checkGetActionParameter(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure parameters encoded in action URL are "
        		+ "available in the action request.");

        String noValue = request.getParameter(KEY_NO_VAL);
        if (noValue != null && noValue.equals(NO_VAL)) {
            result.setReturnCode(TestResult.PASSED);
        } else {
            TestUtils.failOnAssertion(KEY_NO_VAL, noValue, NO_VAL, result);
        }
        
        String value = request.getParameter(KEY);
        if (value != null && value.equals(VALUE)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion(KEY, value, VALUE, result);
        }
        
        String singleValue = request.getParameter(MULTI_KEY);
        if (singleValue != null && (singleValue.equals(VALUE_A) || singleValue.equals(VALUE_B))) {
            result.setReturnCode(TestResult.PASSED);
        } else {
            TestUtils.failOnAssertion(MULTI_KEY, singleValue, VALUE_A + " or " + VALUE_B, result);
        }
        
        return result;
    }
    
    protected TestResult checkGetActionMultivaluedParameters(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure multivalued parameters encoded in action URL are "
                + "available in the action request.");

        String[] noValue = request.getParameterValues(KEY_NO_VAL);
        if (noValue != null && noValue.length == 1 && noValue[0].equals(NO_VAL)) {
            result.setReturnCode(TestResult.PASSED);
        } else {
            TestUtils.failOnAssertion(KEY_NO_VAL, noValue, new String[] {NO_VAL}, result);
        }
        
        String[] value = request.getParameterValues(KEY);
        if (value != null && value.length == 1 && value[0].equals(VALUE)) {
            result.setReturnCode(TestResult.PASSED);
        } else {
            TestUtils.failOnAssertion(KEY, value, new String[] {VALUE}, result);
        }
        
        String[] singleValue = request.getParameterValues(MULTI_KEY);
        if (singleValue != null && singleValue.length == 2 && 
                (singleValue[0].equals(VALUE_A) || singleValue[0].equals(VALUE_B)) &&
                (singleValue[1].equals(VALUE_A) || singleValue[1].equals(VALUE_B))) {
            result.setReturnCode(TestResult.PASSED);
        } else {
            TestUtils.failOnAssertion(MULTI_KEY, singleValue, new String[] {VALUE_A, VALUE_B}, result);
        }
        
        return result;
    }

    protected TestResult checkGetActionParamerMap(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure parameters encoded in action URL are "
        		+ "available in the action request parameter map.");

        Map parameterMap = request.getParameterMap();
        
        String[] noValue = (String[]) parameterMap.get(KEY_NO_VAL);
        if (noValue != null && noValue.length == 1 && noValue[0].equals(NO_VAL)) {
            result.setReturnCode(TestResult.PASSED);
        } else {
            TestUtils.failOnAssertion(KEY_NO_VAL, noValue, new String[] {NO_VAL}, result);
        }
        
        String[] values = (String[]) parameterMap.get(KEY);
        if (values != null && values.length == 1 && VALUE.equals(values[0])) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("parameter values",
        			values, new String[] { VALUE }, result);
        }
        
        String[] singleValue = (String[]) parameterMap.get(MULTI_KEY);
        if (singleValue != null && singleValue.length == 2 && 
                (singleValue[0].equals(VALUE_A) || singleValue[0].equals(VALUE_B)) &&
                (singleValue[1].equals(VALUE_A) || singleValue[1].equals(VALUE_B))) {
            result.setReturnCode(TestResult.PASSED);
        } else {
            TestUtils.failOnAssertion(MULTI_KEY, singleValue, new String[] {VALUE_A, VALUE_B}, result);
        }
        
        return result;
    }

    protected TestResult checkParameterNames(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure parameters encoded in action URL "
        		+ "exists in the parameter name enumeration.");
        
        boolean keyNoValue = false;
        boolean key = false;
        boolean multiKey = false;
        
        for (Enumeration en = request.getParameterNames();
        		(!keyNoValue || !key || !multiKey) && en.hasMoreElements(); ) {
        	String name = (String) en.nextElement();
        	if (KEY_NO_VAL.equals(name)) {
        	    keyNoValue = true;
        	}
        	else if (KEY.equals(name)) {
        	    key = true;
            }
            else if (MULTI_KEY.equals(name)) {
                multiKey = true;
            }
        }

        if (keyNoValue && key && multiKey) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	String msg = "Parameter name(s) [";
        	
        	if (keyNoValue) {
        	    msg += " '" + KEY_NO_VAL + "' ";
        	}
        	else if (keyNoValue) {
        	    msg += " '" + KEY + "' ";
            }
        	else if (keyNoValue) {
        	    msg += " '" + MULTI_KEY + "' ";
            }
        	
        	msg += "] not found in parameter name enumeration.";
        	
        	result.setResultMessage(msg);
        }
        return result;
    }

}
