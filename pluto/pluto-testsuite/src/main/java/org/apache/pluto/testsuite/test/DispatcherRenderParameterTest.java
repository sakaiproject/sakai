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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @version 1.0
 * @since Mar 9, 2005
 */
public class DispatcherRenderParameterTest
extends AbstractReflectivePortletTest {

	/** Internal logger. */
	private static final Log LOG = LogFactory.getLog(
			DispatcherRenderParameterTest.class);

	// Static Final Constants --------------------------------------------------

	/** The path to the companion servlet. */
	private static final String SERVLET_PATH = "/test/DispatcherRenderParameterTest_Servlet";

	private static final String KEY_TARGET = "target";

	private static final String TARGET_PARAMS = "testParams";
	private static final String TARGET_SAME_NAME_PARAM = "testSameNameParam";
	private static final String TARGET_ADDED_SAME_NAME_PARAM = "testAddedSameNameParam";
	private static final String TARGET_INVALID_PARAMS = "testInvalidParams";

	private static final String KEY_RENDER = "renderParamKey";
	private static final String VALUE_RENDER = "renderParamValue";
	private static final String VALUE_ADDED1 = "addedParamValue1";
	private static final String VALUE_ADDED2 = "addedParamValue2";

    private static final String KEY_A = "includedTestKeyA";
    private static final String VALUE_A = "includedTestValueA";

    private static final String KEY_B = "includedTestKeyB";
    private static final String VALUE_B = "includedTestValueB";

    private static final String KEY_C = "includedTestKeyC";
    private static final String VALUE_C1 = "valueOneOfKeyC";
    private static final String VALUE_C2 = "valueTwoOfKeyC";
    private static final String VALUE_C3 = "valueThreeOfKeyC";

    public static final String RESULT_KEY =
    		DispatcherRenderParameterTest.class.getName() + ".RESULT_KEY";


    // AbstractReflectivePortletTest Impl --------------------------------------

    /**
     * Overwrites <code>super.getRenderParameters(..)</code> to set the
     * test-specific render parameter in the render URL.
     */
    public Map getRenderParameters(PortletRequest request) {
    	Map parameterMap = super.getRenderParameters(request);
    	parameterMap.put(KEY_RENDER, new String[] { VALUE_RENDER });
    	return parameterMap;
    }



    // Test Methods ------------------------------------------------------------

    protected TestResult checkParameters(PortletContext context,
                                         PortletRequest request,
                                         PortletResponse response)
    throws IOException, PortletException {

    	// Dispatch to the companion servlet: call checkParameters().
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(SERVLET_PATH).append("?")
    			.append(KEY_TARGET).append("=").append(TARGET_PARAMS)
    			.append("&").append(KEY_A).append("=").append(VALUE_A)
    			.append("&").append(KEY_B).append("=").append(VALUE_B);

    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Dispatching to: " + buffer.toString());
    	}
        PortletRequestDispatcher dispatcher = context.getRequestDispatcher(
        		buffer.toString());
        dispatcher.include((RenderRequest) request, (RenderResponse) response);

    	// Retrieve test result returned by the companion servlet.
        TestResult result = (TestResult) request.getAttribute(RESULT_KEY);
    	request.removeAttribute(RESULT_KEY);
        return result;
    }


    protected TestResult checkSameNameParameter(PortletContext context,
                                                PortletRequest request,
                                                PortletResponse response)
    throws IOException, PortletException {

    	// Dispatch to the companion servlet: call checkSameNameParameter().
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(SERVLET_PATH).append("?")
    			.append(KEY_TARGET).append("=").append(TARGET_SAME_NAME_PARAM)
    			.append("&").append(KEY_C).append("=").append(VALUE_C1)
    			.append("&").append(KEY_C).append("=").append(VALUE_C2)
    			.append("&").append(KEY_C).append("=").append(VALUE_C3);

    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Dispatching to: " + buffer.toString());
    	}
    	PortletRequestDispatcher dispatcher = context.getRequestDispatcher(
    			buffer.toString());
    	dispatcher.include((RenderRequest) request, (RenderResponse) response);

    	// Retrieve test result returned by the companion servlet.
        TestResult result = (TestResult) request.getAttribute(RESULT_KEY);
    	request.removeAttribute(RESULT_KEY);
    	return result;
    }

    protected TestResult checkAddedSameNameParameter(PortletContext context,
                                                     PortletRequest request,
                                                     PortletResponse response)
    throws IOException, PortletException {
    	// Dispatch to the companion servlet: call checkAddedSameNameParameter().
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(SERVLET_PATH).append("?")
    			.append(KEY_TARGET).append("=").append(TARGET_ADDED_SAME_NAME_PARAM)
    			.append("&").append(KEY_RENDER).append("=").append(VALUE_ADDED1)
    			.append("&").append(KEY_RENDER).append("=").append(VALUE_ADDED2);

    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Dispatching to: " + buffer.toString());
    	}
    	PortletRequestDispatcher dispatcher = context.getRequestDispatcher(
    			buffer.toString());
    	dispatcher.include((RenderRequest) request, (RenderResponse) response);

    	// Retrieve test result returned by the companion servlet.
        TestResult result = (TestResult) request.getAttribute(RESULT_KEY);
    	request.removeAttribute(RESULT_KEY);
    	return result;
    }

    protected TestResult checkInvalidParameters(PortletContext context,
                                                PortletRequest request,
                                                PortletResponse response)
    throws IOException, PortletException {

    	// Dispatch to the companion servlet: call checkInvalidParameters().
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(SERVLET_PATH).append("?")
    			.append(KEY_TARGET).append("=").append(TARGET_INVALID_PARAMS)
    			.append("&").append(KEY_A)
    			.append("&").append(KEY_B).append("=").append(VALUE_B)
    			.append("&").append(KEY_C).append("=");
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Dispatching to: " + buffer.toString());
    	}
    	PortletRequestDispatcher dispatcher = context.getRequestDispatcher(
    			buffer.toString());
    	dispatcher.include((RenderRequest) request, (RenderResponse) response);

    	// Retrieve test result returned by the companion servlet.
        TestResult result = (TestResult) request.getAttribute(RESULT_KEY);
    	request.removeAttribute(RESULT_KEY);
    	return result;
    }


    // Nested Companion Servlet Class ------------------------------------------

    /**
     * Nested static companion servlet class.
     */
    public static class CompanionServlet extends GenericServlet {

        // GenericServlet Impl -------------------------------------------------

        public String getServletInfo() {
        	return getClass().getName();
        }

    	/**
    	 * Services the servlet request dispatched from the test portlet.
    	 * This method checks the 'target' parameter to determine which test
    	 * to run, and saves the test result in the request scope, which will
    	 * be retrieved by the test portlet.
    	 * @param request  the incoming servlet request.
    	 * @param response  the incoming servlet response.
    	 */
        public void service(ServletRequest request, ServletResponse response)
        throws ServletException, IOException {
        	TestResult result = null;
        	String target = request.getParameter(KEY_TARGET);
        	if (TARGET_PARAMS.equals(target)) {
        		result = checkParameters(request);
        	} else if (TARGET_SAME_NAME_PARAM.equals(target)) {
        		result = checkSameNameParameter(request);
        	} else if (TARGET_ADDED_SAME_NAME_PARAM.equals(target)) {
        		result = checkAddedSameNameParameter(request);
        	} else if (TARGET_INVALID_PARAMS.equals(target)) {
        		result = checkInvalidParameters(request);
        	} else {
        		result = failOnUnknownTarget(request);
        	}
        	request.setAttribute(RESULT_KEY, result);
        }


        // Private Methods -----------------------------------------------------

        /**
         * Check that parameters A and B are available in the dispatching
         * request.
         * @param request  the servlet request.
         */
        private TestResult checkParameters(ServletRequest request) {
        	TestResult result = new TestResult();
            result.setDescription("Ensure query parameters added during "
            		+ "dispatching are attached to the request.");
            String valueA = request.getParameter(KEY_A);
            String valueB = request.getParameter(KEY_B);
            if (VALUE_A.equals(valueA) && VALUE_B.equals(valueB)) {
            	result.setReturnCode(TestResult.PASSED);
            } else if (!VALUE_A.equals(valueA)){
            	TestUtils.failOnAssertion("parameter", valueA, VALUE_A, result);
            } else {
            	TestUtils.failOnAssertion("parameter", valueB, VALUE_B, result);
            }
            return result;
        }

        /**
         * Check that parameter C has three values.
         * @param request  the servlet reqeust.
         */
        private TestResult checkSameNameParameter(ServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure query parameters with the same name "
        			+ "added during dispatching are attached to the request.");
        	String[] values = request.getParameterValues(KEY_C);
        	String[] expected = new String[] {
        			VALUE_C1, VALUE_C2, VALUE_C3, };
        	if (Arrays.equals(values, expected)) {
        		result.setReturnCode(TestResult.PASSED);
        	} else {
        		TestUtils.failOnAssertion("parameter", values, expected, result);
        	}
            return result;
        }

        /**
         * Check that parameter RENDER has three values: one is the render
         * parameter, while the other two are appended in the dispatch URI.
         * @param request  the servlet reqeust.
         */
        private TestResult checkAddedSameNameParameter(ServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure query parameters with the same name "
        			+ "added during dispatching are attached to the request "
        			+ "as well as render parameters.");
        	String[] values = request.getParameterValues(KEY_RENDER);
        	String[] expected = new String[] {
        			VALUE_ADDED1, VALUE_ADDED2, VALUE_RENDER, };
        	if (Arrays.equals(values, expected)) {
        		result.setReturnCode(TestResult.PASSED);
        	} else {
        		TestUtils.failOnAssertion("parameter", values, expected, result);
        	}
    		return result;
        }

        /**
         * Check that invalid parameter A is ignored, parameter B is attached
         * to the dispatching request with the correct value, and parameter C
         * is attached to the dispatching request with an empty string.
         * @param request  the servlet request.
         */
        private TestResult checkInvalidParameters(ServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure invalid query parameters added "
        			+ "during dispatching are ignored.");
        	String valueA = request.getParameter(KEY_A);
        	String valueB = request.getParameter(KEY_B);
        	String valueC = request.getParameter(KEY_C);
        	if (valueA == null && VALUE_B.equals(valueB) && "".equals(valueC)) {
        		result.setReturnCode(TestResult.PASSED);
        	} else if (valueA != null) {
        		TestUtils.failOnAssertion("parameter", valueA, null, result);
        	} else if (!VALUE_B.equals(valueB)) {
        		TestUtils.failOnAssertion("parameter", valueB, VALUE_B, result);
        	} else {
        		TestUtils.failOnAssertion("parameter", valueC, "", result);
        	}
    		return result;
        }

        private TestResult failOnUnknownTarget(ServletRequest request) {
        	TestResult result = new TestResult();
        	result.setReturnCode(TestResult.FAILED);
            result.setResultMessage("Unable to perform test for parameter "
            		+ KEY_TARGET + ": " + request.getParameter(KEY_TARGET));
            return result;
        }

    }

}

