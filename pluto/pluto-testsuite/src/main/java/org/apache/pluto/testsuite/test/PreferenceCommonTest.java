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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;

/**
 * Common portlet preferences test.
 */
public class PreferenceCommonTest extends AbstractReflectivePortletTest {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(PreferenceCommonTest.class);

    protected static final String BOGUS_KEY = "org.apache.pluto.testsuite.BOGUS_KEY";

    protected static final String READ_ONLY_PREF_NAME = "readonly";

    protected static final String NO_VALUE_PREF_NAME = "nameWithNoValue";

    protected static final String PREF_NAME = "dummyName";
    protected static final String PREF_VALUE = "dummyValue";

    protected static final String DEF_VALUE = "Default";
    protected static final String NEW_VALUE = "notTheOriginal";


    // Test Methods ------------------------------------------------------------

    protected TestResult checkGetEmptyPreference(PortletRequest request) {
    	return doCheckDefaultPreference(request, "nonexistence!");
    }

    protected TestResult checkGetNoValuePreference(PortletRequest request) {
    	return doCheckDefaultPreference(request, NO_VALUE_PREF_NAME);
    }

    /**
     * Private method that checks if a preference is not defined or has no
     * value in <code>portlet.xml</code>, the default values are returned.
     * @param request  the portlet request.
     * @param preferenceName  the preference name which is not defined or has no
     *        value in <code>portlet.xml</code>.
     * @return the test result.
     */
    private TestResult doCheckDefaultPreference(PortletRequest request,
                                                String preferenceName) {
    	TestResult result = new TestResult();
    	result.setDescription("Ensure proper default is returned when "
    			+ "a non-existing/value-undefined preference is requested.");
    	result.setSpecPLT("14.1");

    	PortletPreferences preferences = request.getPreferences();
    	String value =  preferences.getValue(preferenceName, DEF_VALUE);
    	String[] values = preferences.getValues(preferenceName,
    	                                        new String[] { DEF_VALUE });
    	if (DEF_VALUE.equals(value)
    			&& values != null && values.length == 1
    			&& DEF_VALUE.equals(values[0])) {
    		result.setReturnCode(TestResult.PASSED);
    	} else if (!DEF_VALUE.equals(value)) {
    		TestUtils.failOnAssertion("preference value", value, DEF_VALUE, result);
    	} else {
    		TestUtils.failOnAssertion("preference values",
    		                          values,
    		                          new String[] { DEF_VALUE },
    		                          result);
    	}
    	return result;
    }


    protected TestResult checkGetPreferences(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that preferences defined "
        		+ "in the deployment descriptor may be retrieved.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        String value = preferences.getValue(PREF_NAME, DEF_VALUE);
        if (value != null && value.equals(PREF_VALUE)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("preference value", value, PREF_VALUE, result);
        }
        return result;
    }

    protected TestResult checkSetPreferenceSingleValue(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure a single preference value can be set.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        try {
            preferences.setValue("TEST", "TEST_VALUE");
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference value.", ex, result);
            return result;
        }

        String value = preferences.getValue("TEST", DEF_VALUE);
        if (value != null && value.equals("TEST_VALUE")) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("preference value", value, "TEST_VALUE", result);
        }
        return result;
    }

    protected TestResult checkSetPreferenceMultiValues(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure multiple preference values can be set.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        try {
            preferences.setValues("TEST", new String[] {"ONE", "ANOTHER"});
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference values.", ex, result);
        	return result;
        }

        String[] values = preferences.getValues("TEST", new String[] { DEF_VALUE });
        if (values != null && values.length == 2
        		&& values[0].equals("ONE") && values[1].equals("ANOTHER")) {
        	result.setReturnCode(TestResult.PASSED);
        } else if (values == null) {
        	TestUtils.failOnAssertion("preference values",
        	                          values,
        	                          new String[] { "ONE", "ANOTHER" },
        	                          result);
        } else if (values.length != 2) {
        	TestUtils.failOnAssertion("length of preference values",
        	                          String.valueOf(values.length),
        	                          String.valueOf(2),
        	                          result);
        } else {
        	TestUtils.failOnAssertion("preference values",
        	                          values,
        	                          new String[] { "ONE", "ANOTHER" },
        	                          result);
        }
        return result;
    }

    protected TestResult checkSetPreferenceNull(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure a preference value can be set to null.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        try {
            preferences.setValue("TEST", null);
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference value.", ex, result);
            return result;
        }

        String value = preferences.getValue("TEST", DEF_VALUE);
        if (DEF_VALUE.equals(value)) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("preference value", value, DEF_VALUE, result);
        }
        return result;
    }

    protected TestResult checkSetPreferencesReturnsFirst(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the first value set to a given "
        		+ "preference is returned first by PortletPreferences.getValue().");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        try {
            preferences.setValues("TEST", new String[] { "FIRST", "SECOND" });
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference values.", ex, result);
        	return result;
        }

        String value = preferences.getValue("TEST", DEF_VALUE);
        if (value != null && value.equals("FIRST")) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	TestUtils.failOnAssertion("preference value", value, "FIRST", result);
        }
        return result;
    }

    protected TestResult checkResetPreferenceToDefault(PortletRequest request) {
    	TestResult result = new TestResult();
    	result.setDescription("Ensure preferences are properly reset.");
    	result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        boolean setOccured = false;
        boolean resetOccured = false;

        try {
        	// Set new value to overwrite the default value.
            preferences.setValue(PREF_NAME, NEW_VALUE);
            String value = preferences.getValue(PREF_NAME, DEF_VALUE);
            if (NEW_VALUE.equals(value)) {
                setOccured = true;
            }
            // Reset the preference so that default value is restored.
            preferences.reset(PREF_NAME);
            value =  preferences.getValue(PREF_NAME, DEF_VALUE);
            if (PREF_VALUE.equals(value)) {
                resetOccured = true;
            }
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference value.", ex, result);
        	return result;
        }

        // Everything is OK.
        if (setOccured && resetOccured) {
        	result.setReturnCode(TestResult.PASSED);
        }
        // Error occurred when setting or storing preferences.
        else if (!setOccured) {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("A function upon which the reset test "
        			+ "depends failed to execute as expected. "
        			+ "Check the other test results in this test suite.");
        }
        // Error occurred when resetting preference.
        else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Preferences value was not successfully reset after store");
        }
        return result;
    }

    protected TestResult checkResetPreferenceWithoutDefault(PortletRequest request) {
    	TestResult result = new TestResult();
        result.setDescription("Ensure preferences are properly reset (removed) "
        		+ "when the default value is not defined.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        boolean setOccured = false;
        boolean resetOccured = false;

        try {
        	// Set preference value to overwrite the original (null).
            preferences.setValue(BOGUS_KEY, NEW_VALUE);
            String value = preferences.getValue(BOGUS_KEY, DEF_VALUE);
            if (NEW_VALUE.equals(value)) {
                setOccured = true;
            }
            // Reset preference value to null.
            preferences.reset(BOGUS_KEY);
            value =  preferences.getValue(BOGUS_KEY, DEF_VALUE);
            if (DEF_VALUE.equals(value)) {
                resetOccured = true;
            }
        } catch (ReadOnlyException ex) {
        	TestUtils.failOnException("Unable to set preference value.", ex, result);
        	return result;
        }

        // Everything is OK.
        if (setOccured && resetOccured) {
        	result.setReturnCode(TestResult.PASSED);
        }
        // Error occurred when setting or storing preferences.
        else if (!setOccured) {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("A function upon which the reset test "
        			+ "depends failed to execute as expected. "
        			+ "Check the other test results in this test suite.");
        }
        // Error occurred when resetting preference value.
        else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("Preferences value was not successfully "
        			+ "reset after store.");
        }
        return result;
    }

    protected TestResult checkModifyReadOnlyPreferences(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure that setValue() / setValues() / reset() "
        		+ "methods throw ReadOnlyException when invoked "
        		+ "on read-only preferences.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        if (!preferences.isReadOnly(READ_ONLY_PREF_NAME)) {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("Preference " + READ_ONLY_PREF_NAME
        			+ " is not a read-only preference. "
        			+ "This may be due to misconfiuration.");
        	return result;
        }

        boolean setValueOK = false;
        boolean setValuesOK = false;
        boolean resetOK = false;

        // Check setValue() method.
        try {
            preferences.setValue(READ_ONLY_PREF_NAME, "written");
        } catch (ReadOnlyException ex) {
            setValueOK = true;
        }

        // Check setValues() method.
        try {
        	preferences.setValues(READ_ONLY_PREF_NAME, new String[] { "written" });
        } catch (ReadOnlyException ex) {
        	setValuesOK = true;
        }

        // Check reset() method.
        try {
        	preferences.reset(READ_ONLY_PREF_NAME);
        } catch (ReadOnlyException ex) {
        	resetOK = true;
        }

        if (setValueOK && setValuesOK && resetOK) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	StringBuffer buffer = new StringBuffer();
        	if (!setValueOK) {
        		buffer.append("setValue(..), ");
        	}
        	if (!setValuesOK) {
        		buffer.append("setValues(..), ");
        	}
        	if (!resetOK) {
        		buffer.append("reset(..), ");
        	}
        	result.setResultMessage("Method(s) [" + buffer.toString()
        			+ "] invoked on read-only preference (" + READ_ONLY_PREF_NAME
        			+ ") without ReadOnlyException.");
        }
        return result;
    }

    protected TestResult checkGetPreferenceNames(PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure returned enumeration is valid.");
        result.setSpecPLT("14.1");

        PortletPreferences preferences = request.getPreferences();
        Map prefMap = preferences.getMap();
        boolean hasAll = true;
        for (Enumeration en = preferences.getNames(); en.hasMoreElements(); ) {
            if (!prefMap.containsKey(en.nextElement())) {
                hasAll = false;
                break;
            }
        }

        if (hasAll) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
        	result.setResultMessage("At least one name is not found "
        			+ "in the preference map.");
        }
        return result;
    }

    /**
     * FIXME:
     */
    protected TestResult checkGetPreferenceMap(PortletRequest request) {
        TestResult result = checkGetPreferenceNames(request);
        result.setDescription("Ensure returned map is valid.");
        result.setSpecPLT("14.1");
        return result;
    }

    /**
     * Check (xci) SPEC 91, PLT 14.1: Preferences values are not modified
     * if the values in the Map are altered.
     */
    protected TestResult checkPreferenceValueNotModified(PortletRequest request) {
    	TestResult result = new TestResult();
    	result.setDescription("Preferences values are not modified if "
    			+ "the values in the returned preference Map are altered.");
    	result.setSpecPLT("14.1");

    	PortletPreferences preferences = request.getPreferences();
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Original Preferences:");
        	logPreferences(preferences);
        }

        // Modify the returned preference map.
    	Map prefMap = preferences.getMap();
    	String[] values = (String[]) prefMap.get(PREF_NAME);
    	String originalValue = null;
    	String modifiedValue = "Value modified in preferences map.";
    	if (values != null && values.length == 1) {
    		originalValue = values[0];
    		values[0] = modifiedValue;
    	}

    	// Check if the value held by portlet preferences is modified.
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Modified Preferences:");
        	logPreferences(preferences);
        }
    	String newValue = preferences.getValue(PREF_NAME, DEF_VALUE);
    	if (newValue != null && newValue.equals(originalValue)) {
    		result.setReturnCode(TestResult.PASSED);
    	} else {
    		result.setReturnCode(TestResult.FAILED);
    		result.setResultMessage("Preference value modified according to "
    				+ "the preference map.");
    	}
    	return result;
    }


    // Debug Methods -----------------------------------------------------------


    /**
     * Logs out the portlet preferences.
     * @param preferences  PortletPreferences to log.
     */
    protected void logPreferences(PortletPreferences preferences) {
    	StringBuffer buffer = new StringBuffer();
    	Map map = preferences.getMap();
    	for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
    		String key = (String) it.next();
    		String[] values = (String[]) map.get(key);
    		buffer.append(key).append("=");
    		if (values != null) {
    			buffer.append("{");
    			for (int i = 0; i < values.length; i++) {
    				buffer.append(values[i]);
    				if (i < values.length - 1) {
    					buffer.append(",");
    				}
    			}
    			buffer.append("}");
    		} else {
    			// Spec allows null values.
    			buffer.append("NULL");
    		}
    		buffer.append(";");
    	}
    	LOG.debug("PortletPreferences: " + buffer.toString());
    }

}
