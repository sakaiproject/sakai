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

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ValidatorException;

import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;

public class PreferenceInRenderTest extends PreferenceCommonTest {
	
	// Test Methods ------------------------------------------------------------
	
	protected TestResult checkStorePreferences(PortletRequest request) {
		TestResult result = new TestResult();
		result.setDescription("Ensure that if the store() method is invoked "
				+ "within render() method, an IllegalStateException will be "
				+ "thrown out.");
		result.setSpecPLT("14.1");
		
		PortletPreferences preferences = request.getPreferences();
        boolean exceptionThrown = false;
        
        // Store preferences and wait for IllegalStateException.
        try {
            preferences.store();
        } catch (ValidatorException ex) {
        	TestUtils.failOnException("Unable to store preferences.", ex, result);
        	return result;
        } catch (IOException ex) {
        	TestUtils.failOnException("Unable to store preferences.", ex, result);
        	return result;
        } catch (IllegalStateException ex) {
        	exceptionThrown = true;
        }
		
        if (exceptionThrown) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("IllegalStateException is not thrown out "
        			+ "when store() method is invoked within render() method.");
        }
		return result;
	}
	
}
