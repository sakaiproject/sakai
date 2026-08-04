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
import org.apache.pluto.testsuite.ActionTest;
import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;
import org.apache.pluto.testsuite.validator.PreferencesValidatorImpl;

import java.io.IOException;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

/**
 */
public class PreferenceInActionTest extends PreferenceCommonTest
implements ActionTest {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(PreferenceInActionTest.class);


    // Test Methods ------------------------------------------------------------

    protected TestResult checkPreferenceValidator(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the validator catches invalid preferences.");
        result.setSpecPLT("14.4");

        PortletPreferences preferences = request.getPreferences();
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Original preferences:");
        	logPreferences(preferences);
        }
        boolean exceptionThrown = false;
        try {
            preferences.setValue("TEST", " Spaces removed by trim ");
            if (LOG.isDebugEnabled()) {
            	LOG.debug("Modified VALIDATION_TEST_KEY preference:");
            	logPreferences(preferences);
            }
            // Call store() method to invoke the validator.
            preferences.store();

        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference value.", ex, result);
        	return result;

        } catch (IOException ex) {
        	TestUtils.failOnException("Unable to store preference value.", ex, result);
        	return result;

        } catch (ValidatorException ex) {
        	// We are expecting this exception!
            exceptionThrown = true;
            // FIXME: what is going on below?
            try {
            	//get rid of spaces because it causes problems with reset() call.
                preferences.setValue("TEST", "OK");
            	preferences.reset("TEST");
            } catch (Throwable th) {
            	LOG.error(th);
            }
        }

        if (exceptionThrown) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Illegal value not caught by validator.");
        }
        return result;
    }

    protected TestResult checkOnePreferenceValidatorPerPortletDefinition(
    		PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure only one validator instance is created "
        		+ "per portlet definition.");
        result.setSpecPLT("14.4");

        PortletPreferences preferences = request.getPreferences();
        try {
            preferences.setValue(
            		PreferencesValidatorImpl.CHECK_VALIDATOR_COUNT,
            		"true");
            // Call store() method to invoke the validator.
            preferences.store();
            result.setReturnCode(TestResult.PASSED);
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference value.", ex, result);
        } catch (IOException ex) {
        	TestUtils.failOnException("Unable to store preference value.", ex, result);
        } catch (ValidatorException ex) {
        	TestUtils.failOnException("Unable to store preference value.", ex, result);
        } finally {
        	try {
        		preferences.reset(PreferencesValidatorImpl.CHECK_VALIDATOR_COUNT);
        		preferences.store();
        	} catch (Exception ex) {
        		TestUtils.failOnException("Unable to reset preference value for "
        				+ PreferencesValidatorImpl.CHECK_VALIDATOR_COUNT,
        				ex, result);
        	}
        }
        return result;
    }

    protected TestResult checkStorePreferences(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure storage works for portlet preferences.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Preferences to store: " + preferences);
        }

        boolean setOccured = false;
        boolean storeOccured = false;

        try {
        	// Set new value for preference "dummyName".
            preferences.setValue(PREF_NAME, NEW_VALUE);
            String value = preferences.getValue(PREF_NAME, DEF_VALUE);
            if (NEW_VALUE.equals(value)) {
                setOccured = true;
            }
            // Store the preference and get value.
            preferences.store();
            value = preferences.getValue(PREF_NAME, DEF_VALUE);
            if (NEW_VALUE.equals(value)) {
                storeOccured = true;
            }
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference value.", ex, result);
        	return result;
        } catch (ValidatorException ex) {
        	TestUtils.failOnException("Unable to store preference value.", ex, result);
        	return result;
        } catch(IOException ex) {
        	TestUtils.failOnException("Unable to store preference value.", ex, result);
        	return result;
        } finally {
            // Reset preference to default value, and store!
        	try {
        		preferences.reset(PREF_NAME);
        		preferences.store();
        	} catch (Exception ex) {
            	TestUtils.failOnException("Unable to set preference value.", ex, result);
            	return result;
        	}
        }

        // Everything is OK.
        if (setOccured && storeOccured) {
        	result.setReturnCode(TestResult.PASSED);
        }
        // Error occurred when setting preference value.
        else if (!setOccured) {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("A function upon which the reset test "
        			+ "depends failed to execute as expected. "
        			+ "Check the other test results in this test suite.");
        }
        // Error occurred when storing preference value.
        else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Preferences not successfully stored.");
        }
        return result;
    }

}
