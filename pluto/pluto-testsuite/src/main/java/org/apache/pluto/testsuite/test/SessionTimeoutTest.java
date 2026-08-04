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

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.testsuite.TestResult;

public class SessionTimeoutTest extends AbstractReflectivePortletTest {
	
	/** Logger. */
	private static final Log LOG = LogFactory.getLog(SessionTimeoutTest.class);
	
	/**
	 * Render parameter name indicating if the max inactive interval is set to
	 * the portlet session or not.
	 */
	private static final String MAX_INACTIVE_INTERVAL_SET = "maxInactiveIntervalSet";
	
	
	// Test Methods ------------------------------------------------------------
	
	protected TestResult checkSessionInvalidated(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure portlet session is invalidated after "
        		+ "the max inactive interval.");
        result.setSpecPLT("15.6");
        
        // Check if max inactive interval is set to the portlet session.
        String maxInactiveIntervalSet = request.getParameter(
        		MAX_INACTIVE_INTERVAL_SET);
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Retrieved render parameter: " + MAX_INACTIVE_INTERVAL_SET
        			+ " = " + maxInactiveIntervalSet);
        }
        
        // If the max inactive interval is set to portlet session, the portlet
        //   session should have been invalidated by the container.
        if (Boolean.TRUE.toString().equals(maxInactiveIntervalSet)) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Max inactive interval is set to portlet session: "
        				+ "portlet session should have expired "
        				+ "(current time millis: "
        				+ System.currentTimeMillis() + ")...");
        	}
            PortletSession session = request.getPortletSession(false);
            if (session == null) {
            	result.setReturnCode(TestResult.PASSED);
            } else {
            	result.setReturnCode(TestResult.FAILED);
            	result.setResultMessage("PortletSession should have expired "
            			+ "and have been invalidated, but is still available. "
            			+ "Make sure that other portlets did not create a new "
            			+ "portlet session.");
            }
        }
        
        // If the max inactive interval is not set to portlet session, set its
        //   value to 5 (seconds). In this way, next time the test portlet is
        //   rendered, the portlet session should have been invalidated.
        else {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Max inactive interval is not set to portlet session: "
        				+ "setting to 5 seconds (current time millis: "
        				+ System.currentTimeMillis() + ")...");
        	}
            PortletSession session = request.getPortletSession(true);
            session.setMaxInactiveInterval(5);
            result.setReturnCode(TestResult.WARNING);
            result.setResultMessage("Click the provided link to validate test.");
        }
        
        // Return the test result:
        //   PASSED - the test is passed.
        //   FAILED - the test is failed.
        //   WARNING - the test requires manual intervention.
        return result;
	}
	
}


