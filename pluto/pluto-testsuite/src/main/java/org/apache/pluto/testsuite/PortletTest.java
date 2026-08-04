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
package org.apache.pluto.testsuite;

import java.util.Map;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * Interface for pluto portlet test classes.
 *
 */
public interface PortletTest {

	/**
	 * Returns the test suite name.
	 * @return the test suite name.
	 */
    public String getTestSuiteName();

    /**
     * Initializes the portlet test using test configuration.
     * @param config  the test configuration.
     */
    public void init(TestConfig config);

    /**
     * Returns the render parameters. This method will be invoked in
     * <code>Portlet.processAction()</code> method. All parameters returned
     * by this method will be set as render parameters.
     *
     * @param request  the portlet request.
     * @return a map of render parameters, key is the string name of the
     *         parameter, value is a string array.
     */
    public Map getRenderParameters(PortletRequest request);

    /**
     * Runs the test.
     * @param config  the portlet config.
     * @param context  the portlet context.
     * @param request  the portlet request.
     * @param response  the portlet response.
     * @return the results of the test.
     */
    public TestResults doTest(PortletConfig config,
                              PortletContext context,
                              PortletRequest request,
                              PortletResponse response);

    /**
     * Returns the test configuration.
     * @return the test configuration.
     */
    public TestConfig getConfig();

}

