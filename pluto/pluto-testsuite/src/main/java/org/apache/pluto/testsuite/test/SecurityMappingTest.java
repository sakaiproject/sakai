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

import org.apache.pluto.testsuite.TestResult;

/**
 */
public class SecurityMappingTest extends AbstractReflectivePortletTest {

    // Test Methods ------------------------------------------------------------

    protected TestResult checkIsUserInMappedRole(PortletRequest request) {
        TestResult result = isUserLoggedIn(request);
        result.setDescription("Test if user is in mapped role.");
        if (result.getReturnCode() == TestResult.WARNING) {
            return result;
        }

        ExpectedResults expectedResults = ExpectedResults.getInstance();
        String role = expectedResults.getMappedSecurityRole();
        if (request.isUserInRole(role)) {
            result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("User is not in the expected role: " + role
        			+ ". This may be due to misconfiuration.");
        }
        return result;
    }

    protected TestResult checkIsUserInUnmappedRole(PortletRequest request) {
        TestResult result = isUserLoggedIn(request);
        result.setDescription("Test if user is in unmapped role");
        if (result.getReturnCode() == TestResult.WARNING) {
            return result;
        }

        ExpectedResults expectedResults = ExpectedResults.getInstance();
        String role = expectedResults.getUnmappedSecurityRole();
        if (request.isUserInRole(role)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("User is not in the expected role: " + role
        			+ ". This may be due to misconfiuration.");
        }
        return result;
    }

    protected TestResult checkIsUserIndUndeclaredRole(PortletRequest request) {
        TestResult result = isUserLoggedIn(request);
        result.setDescription("Test if user is in undeclared role");
        if (result.getReturnCode() == TestResult.WARNING) {
            return result;
        }

        String fakeRole = "fakeTestRoleFooBar";
        if (!request.isUserInRole(fakeRole)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("User is in the fake role named " + fakeRole);
        }
        return result;
    }


    // Private Methods ---------------------------------------------------------

    private TestResult isUserLoggedIn(PortletRequest request) {
    	TestResult result = new TestResult();
        if (request.getRemoteUser() == null) {
            result.setReturnCode(TestResult.WARNING);
            result.setResultMessage("User is not logged in.");
        }
        return result;
    }
}
