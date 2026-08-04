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
import org.apache.pluto.testsuite.TestConfig;
import org.apache.pluto.testsuite.PortletTest;
import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestResults;

import javax.portlet.PortletResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletSession;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public abstract class AbstractReflectivePortletTest implements PortletTest {

	/** Logger. */
	private static final Log LOG = LogFactory.getLog(
			AbstractReflectivePortletTest.class);

    private Map initParameters;
    private TestConfig config;


    // PortletTest Impl --------------------------------------------------------

    public void init(TestConfig config) {
        this.config = config;
        this.initParameters = config.getInitParameters();
    }

    /**
     * Returns the render parameters that will be set into the render request.
     * The default implementation just returns an empty Map object. This method
     * may be overwritten by some concrete test to provide test-specific render
     * parameters.
     * @param request  the portlet request.
     * @return an empty Map object.
     */
    public Map getRenderParameters(PortletRequest request) {
        return new HashMap();
    }

    public TestConfig getConfig() {
        return config;
    }

    /**
     * Returns the test suite name. The test suite name is the portlet test
     * class name without package name prefix.
     * @return the test suite name.
     */
    public String getTestSuiteName() {
    	String className = getClass().getName();
    	int index = className.lastIndexOf(".");
    	if (index >= 0 && index < className.length() - 1) {
    		return className.substring(index + 1);
    	} else {
    		return className;
    	}
    }

    /**
     * Invoke test methods using java reflection. All 'check*' methods are
     * invoked and test results are saved into <code>TestResults</code> object.
     * @param config  the portlet config.
     * @param context  the portlet context.
     * @param request  the portlet request.
     * @param response  the portlet response.
     * @return the test results including several TestResult instances.
     */
    public TestResults doTest(PortletConfig config,
                              PortletContext context,
                              PortletRequest request,
                              PortletResponse response) {
        TestResults results = new TestResults(getTestSuiteName());

        for (Iterator it = getCheckMethods().iterator(); it.hasNext(); ) {
        	Method method = (Method) it.next();
        	debugWithName("Invoking test method: " + method.getName());
        	try {
        		TestResult result = invoke(method, config, context, request, response);
        		if (result.getName() == null) {
        			result.setName(method.getName());
        		}
        		results.add(result);
        		debugWithName("Result: " + result.getReturnCodeAsString());
        	} catch (Throwable th) {
        		String message = "Error invoking " + method.getName()
        				+ " (" + th.getClass().getName() + "): "
        				+ th.getMessage();
        		errorWithName(message, th);
        		TestResult result = new TestResult();
        		result.setName(method.getName());
        		result.setReturnCode(TestResult.FAILED);
        		result.setResultMessage(message);
        		results.add(result);
            }
        }

        return results;
    }


    // Protected Methods -------------------------------------------------------

    protected Map getInitParameters() {
        return initParameters;
    }


    // Private Methods ---------------------------------------------------------

    private void debugWithName(String message) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Test [" + getTestSuiteName() + "]: " + message);
    	}
    }

    private void errorWithName(String message, Throwable cause) {
    	if (LOG.isErrorEnabled()) {
    		LOG.error("Test [" + getTestSuiteName() + "]: " + message, cause);
    	}
    }

    /**
     * Returns check methods to run as tests using java reflection.
     * The following rules are applied to select check methods:
     * <ul>
     *   <li>methods declared in this class or inherited from super class</li>
     *   <li>methods with modifier 'public' or 'protected', but not 'abstract'</li>
     *   <li>methods that starts with <code>check</code></li>
     * </ul>
     * @return a list of check methods.
     */
    private List getCheckMethods() {
    	List checkMethods = new ArrayList();
    	for (Class clazz = getClass();
    			clazz != null && AbstractReflectivePortletTest.class.isAssignableFrom(clazz);
    			clazz = clazz.getSuperclass()) {
    		// debugWithName("Checking class: " + clazz.getName());
    		Method[] methods = clazz.getDeclaredMethods();
    		for (int i = 0; i < methods.length; i++) {
    			int mod = methods[i].getModifiers();
    			if ((Modifier.isPublic(mod) || Modifier.isProtected(mod))
    					&& !Modifier.isAbstract(mod)
    					&& methods[i].getName().startsWith("check")) {
    				// debugWithName(" - got check method: " + methods[i].getName());
    				checkMethods.add(methods[i]);
    			}
    		}
    	}
        return checkMethods;
    }

    /**
     * Invokes the test method ('<code>check*</code>') by preparing method
     * parameters. A test method may accept the following types of parameters:
     * <ul>
     *   <li><code>javax.portlet.PortletConfig</code></li>
     *   <li><code>javax.portlet.PortletContext</code></li>
     *   <li><code>javax.portlet.PortletRequest</code></li>
     *   <li><code>javax.portlet.PortletResponse</code></li>
     *   <li><code>javax.portlet.PortletSession</code></li>
     * </ul>
     */
    private TestResult invoke(Method method,
                              PortletConfig config,
                              PortletContext context,
                              PortletRequest request,
                              PortletResponse response)
    throws IllegalAccessException, InvocationTargetException {

        Class[] paramTypes = method.getParameterTypes();
        Object[] paramValues = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i].equals(PortletConfig.class)) {
                paramValues[i] = config;
            } else if (paramTypes[i].equals(PortletContext.class)) {
                paramValues[i] = context;
            } else if (paramTypes[i].equals(PortletRequest.class)) {
                paramValues[i] = request;
            } else if (paramTypes[i].equals(PortletResponse.class)) {
                paramValues[i] = response;
            } else if (paramTypes[i].equals(PortletSession.class)) {
                paramValues[i] = request.getPortletSession();
            }
        }
        TestResult result = (TestResult) method.invoke(this, paramValues);
        return result;
    }


    // Object Methods ----------------------------------------------------------

    /**
     * Override of toString() that prints out names and values of variables.
     * @see java.lang.Object#toString()
     */
    public String toString(){
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(getClass().getName());
    	buffer.append("[initParameters=").append(initParameters);
    	buffer.append(";config=").append(config).append("]");
    	return buffer.toString();
    }
}
