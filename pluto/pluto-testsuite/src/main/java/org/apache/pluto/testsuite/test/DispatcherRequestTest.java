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

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;

public class DispatcherRequestTest extends AbstractReflectivePortletTest {
	
	/** Logger. */
	private static final Log LOG = LogFactory.getLog(DispatcherRequestTest.class);
	
	/** The path to the companion servlet. */
    private static final String SERVLET_PATH = "/test/DispatcherRequestTest_Servlet";
    
    private static final String CHECK_PATH_INFO = "/checkPathInfo";
    private static final String CHECK_REQUEST_URI = "/checkRequestURI";
    private static final String CHECK_CONTEXT_PATH = "/checkContextPath";
    private static final String CHECK_SERVLET_PATH = "/checkServletPath";
    private static final String CHECK_QUERY_STRING = "/checkQueryString";
    
    private static final String GET_REQUEST_URI = "/getRequestURI";
    private static final String GET_CONTEXT_PATH = "/getContextPath";
    private static final String GET_SERVLET_PATH = "/getServletPath";
    private static final String GET_QUERY_STRING = "/getQueryString";
    
    /** The query string appended to the dispatch URI. */
    private static final String QUERY_STRING = "paramName=paramValue";
    
    /** The request attribute key which associates the test result. */
    private static final String RESULT_KEY =
            DispatcherRequestTest.class.getName() + ".RESULT_KEY";
    
    /** The key for the expected request URI. */
    private static final String EXPECTED_REQUEST_URI =
            DispatcherRequestTest.class.getName() + ".REQUEST_URI";
    
    /** The key for the expected context path. */
    private static final String EXPECTED_CONTEXT_PATH =
            DispatcherRequestTest.class.getName() + ".CONTEXT_PATH";
    
	
    // Test Methods ------------------------------------------------------------
    
    protected TestResult checkPathInfo(PortletContext context,
                                       PortletRequest request,
                                       PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, CHECK_PATH_INFO);
    }
    
    protected TestResult checkRequestURI(PortletContext context,
                                         PortletRequest request,
                                         PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, CHECK_REQUEST_URI);
    }
    	
    protected TestResult checkGetRequestURI(PortletContext context,
                                            PortletRequest request,
                                            PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, GET_REQUEST_URI);
    }

    protected TestResult checkContextPath(PortletContext context,
                                          PortletRequest request,
                                          PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, CHECK_CONTEXT_PATH);
    }
    
    protected TestResult checkGetContextPath(PortletContext context,
                                             PortletRequest request,
                                             PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, GET_CONTEXT_PATH);
    }

    protected TestResult checkServletPath(PortletContext context,
                                          PortletRequest request,
                                          PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, CHECK_SERVLET_PATH);
    }
    
    protected TestResult checkGetServletPath(PortletContext context,
                                             PortletRequest request,
                                             PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, GET_SERVLET_PATH);
    }

    protected TestResult checkQueryString(PortletContext context,
                                          PortletRequest request,
                                          PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, CHECK_QUERY_STRING);
    }
    
    protected TestResult checkGetQueryString(PortletContext context,
                                             PortletRequest request,
                                             PortletResponse response)
    throws IOException, PortletException {
    	return doCheckIncludedAttribute(context, request, response, GET_QUERY_STRING);
    }

    
    // Private Methods ---------------------------------------------------------
    
    private TestResult doCheckIncludedAttribute(PortletContext context,
                                                PortletRequest request,
                                                PortletResponse response,
                                                String pathInfo)
    throws IOException, PortletException {
    	
    	// Save expected values as request attributes.
    	String contextPath = request.getContextPath();
    	String requestUri = contextPath + SERVLET_PATH + pathInfo;
    	request.setAttribute(EXPECTED_REQUEST_URI, requestUri);
    	request.setAttribute(EXPECTED_CONTEXT_PATH, contextPath);

    	// Dispatch to the companion servlet: call checkParameters().
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(SERVLET_PATH).append(pathInfo).append("?")
    			.append(QUERY_STRING);
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Dispatching to: " + buffer.toString());
    	}
    	PortletRequestDispatcher dispatcher = context.getRequestDispatcher(
    			buffer.toString());
    	dispatcher.include((RenderRequest) request, (RenderResponse) response);
    	
    	// Retrieve test result returned by the companion servlet.
    	TestResult result = (TestResult) request.getAttribute(RESULT_KEY);
    	request.removeAttribute(RESULT_KEY);
    	request.removeAttribute(EXPECTED_REQUEST_URI);
    	request.removeAttribute(EXPECTED_CONTEXT_PATH);
    	return result;
    }
    
    
    // Nested Companion Servlet Class ------------------------------------------
    
    public static class CompanionServlet extends HttpServlet {
    	
        private static final String KEY_REQUEST_URI = "javax.servlet.include.request_uri";
        private static final String KEY_CONTEXT_PATH = "javax.servlet.include.context_path";
        private static final String KEY_SERVLET_PATH = "javax.servlet.include.servlet_path";
        private static final String KEY_PATH_INFO = "javax.servlet.include.path_info";
        private static final String KEY_QUERY_STRING = "javax.servlet.include.query_string";
        
        
        // GenericServlet Impl -------------------------------------------------
        
        public String getServletInfo() {
        	return getClass().getName();
        }
        
        /**
         * This method uses <code>HttpServletRequest.getPathInfo</code> to
         * retrieve the requested path info, and forwards to the corresponding
         * check method to perform the test.
         */
        public void service(HttpServletRequest request,
                            HttpServletResponse response)
        throws ServletException, IOException {
        	TestResult result = null;
        	String pathInfo = getPathInfo(request);
        	if (CHECK_PATH_INFO.equals(pathInfo)) {
        		result = checkPathInfo(request, pathInfo);
        	} else if (CHECK_REQUEST_URI.equals(pathInfo)) {
        		result = checkRequestURI(request);
        	} else if (CHECK_CONTEXT_PATH.equals(pathInfo)) {
        		result = checkContextPath(request);
        	} else if (CHECK_SERVLET_PATH.equals(pathInfo)) {
        		result = checkServletPath(request);
        	} else if (CHECK_QUERY_STRING.equals(pathInfo)) {
        		result = checkQueryString(request);
        	} else if (GET_REQUEST_URI.equals(pathInfo)) {
        		result = checkGetRequestURI(request);
        	} else if (GET_CONTEXT_PATH.equals(pathInfo)) {
        		result = checkGetContextPath(request);
        	} else if (GET_SERVLET_PATH.equals(pathInfo)) {
        		result = checkGetServletPath(request);
        	} else if (GET_QUERY_STRING.equals(pathInfo)) {
        		result = checkGetQueryString(request);
        	} else {
        		result = failOnPathInfo(pathInfo);
        	}
            request.setAttribute(RESULT_KEY, result);
        }
        
        
        // Private Methods -----------------------------------------------------
        
        /**
         * Retrieves the path info from servlet request by invoking method
         * <code>HttpServletRequest.getPathInfo()</code>.
         * @param request  the servlet request dispatched from test portlet.
         * @return the path info retrieved from servlet request.
         */
        private String getPathInfo(HttpServletRequest request) {
            return request.getPathInfo();
        }
        
        private TestResult checkPathInfo(HttpServletRequest request,
                                         String expected) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that included attribute '"
        			+ KEY_PATH_INFO + "' is available in servlet request.");
        	result.setSpecPLT("16.3.1");
        	
            String pathInfo = (String) request.getAttribute(KEY_PATH_INFO);
            if (pathInfo != null && pathInfo.equals(expected)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion(KEY_PATH_INFO,
            			pathInfo, expected, result);
            }
            return result;
        }
        
        private TestResult checkRequestURI(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that included attribute '"
        			+ KEY_REQUEST_URI + "' is available in servlet request.");
        	result.setSpecPLT("16.3.1");
        	
            String expected = (String) request.getAttribute(EXPECTED_REQUEST_URI);
            String requestURI = (String) request.getAttribute(KEY_REQUEST_URI);
            if (requestURI != null && requestURI.equals(expected)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion(KEY_REQUEST_URI,
            			requestURI, expected, result);
            }
            return result;
        }
        
        private TestResult checkGetRequestURI(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that method request.getRequestURI() "
        			+ "returns correct value.");
        	result.setSpecPLT("16.3.3");
        	
        	String expected = (String) request.getAttribute(EXPECTED_REQUEST_URI);
        	String requestURI = request.getRequestURI();
        	if (requestURI != null && requestURI.equals(expected)) {
        		result.setReturnCode(TestResult.PASSED);
        	} else {
        		TestUtils.failOnAssertion("request.getRequestURI()",
        				requestURI, expected, result);
        	}
        	return result;
        }
        
        private TestResult checkContextPath(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that included attribute '"
        			+ KEY_CONTEXT_PATH + "' is available in servlet request.");
        	result.setSpecPLT("16.3.1");
        	
            String expected = (String) request.getAttribute(EXPECTED_CONTEXT_PATH);
            String contextPath = (String) request.getAttribute(KEY_CONTEXT_PATH);
            if (contextPath != null && contextPath.equals(expected)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion(KEY_CONTEXT_PATH,
            			contextPath, expected, result);
            }
            return result;
        }
        
        private TestResult checkGetContextPath(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that method request.getContextPath() "
        			+ "returns the correct value.");
        	result.setSpecPLT("16.3.3");
        	
            String expected = (String) request.getAttribute(EXPECTED_CONTEXT_PATH);
            String contextPath = request.getContextPath();
            if (contextPath != null && contextPath.equals(expected)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion("request.getContextPath()",
            			contextPath, expected, result);
            }
            return result;
        }

        private TestResult checkServletPath(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that included attribute '"
        			+ KEY_SERVLET_PATH + "' is available in servlet request.");
        	result.setSpecPLT("16.3.1");
        	
            String servletPath = (String) request.getAttribute(KEY_SERVLET_PATH);
            if (SERVLET_PATH.equals(servletPath)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion(KEY_SERVLET_PATH,
            			servletPath, SERVLET_PATH, result);
            }
            return result;
        }
        
        private TestResult checkGetServletPath(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that method request.getServletPath() "
        			+ "returns the correct value.");
        	result.setSpecPLT("16.3.3");
        	
            String servletPath = request.getServletPath();
            if (SERVLET_PATH.equals(servletPath)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion("request.getServletPath()",
            			servletPath, SERVLET_PATH, result);
            }
            return result;
        }
        
        private TestResult checkQueryString(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that included attribute '"
        			+ KEY_QUERY_STRING + "' is available in servlet request.");
        	result.setSpecPLT("16.3.1");
        	
            String queryString = (String) request.getAttribute(KEY_QUERY_STRING);
            if (QUERY_STRING.equals(queryString)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion(KEY_QUERY_STRING,
            			queryString, QUERY_STRING, result);
            }
            return result;
        }
        
        private TestResult checkGetQueryString(HttpServletRequest request) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that method request.getQueryString() "
        			+ "returns the correct value.");
        	result.setSpecPLT("16.3.3");
        	
            String queryString = request.getQueryString();
            if (QUERY_STRING.equals(queryString)) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	TestUtils.failOnAssertion("request.getQueryString()",
            			queryString, QUERY_STRING, result);
            }
            return result;
        }
        
        private TestResult failOnPathInfo(String pathInfo) {
        	TestResult result = new TestResult();
        	result.setDescription("Ensure that included attribute '"
        			+ KEY_PATH_INFO + "' is available in servlet request.");
        	result.setSpecPLT("16.3.1");
        	
        	String[] expectedPathInfos = new String[] {
        			CHECK_REQUEST_URI, CHECK_CONTEXT_PATH,
        			CHECK_SERVLET_PATH, CHECK_QUERY_STRING, };
        	TestUtils.failOnAssertion(KEY_PATH_INFO,
        	                          new String[] { pathInfo },
        	                          expectedPathInfos,
        	                          result);
        	return result;
        }

    }
    
}


