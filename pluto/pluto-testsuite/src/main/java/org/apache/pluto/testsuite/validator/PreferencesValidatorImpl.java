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
package org.apache.pluto.testsuite.validator;

import javax.portlet.PortletPreferences;
import javax.portlet.PreferencesValidator;
import javax.portlet.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the portlet preferences validator.
 */
public class PreferencesValidatorImpl implements PreferencesValidator {

	/** Logger. */
    private static final Log LOG = LogFactory.getLog(PreferencesValidatorImpl.class);

    public static final String CHECK_VALIDATOR_COUNT = "checkValidatorCount";

    /** Count of instances created. */
    private static final Map INSTANCE_COUNTER = new HashMap();

    /** Count of invocation number of method <code>validate()</code>. */
    private int validateInvoked = 0;

    // Constructor -------------------------------------------------------------

    /**
     * Default no-arg constructor.
     */
    public PreferencesValidatorImpl() {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Creating validator instance: " + getClass().getName());
    	}
    	Integer count = (Integer) INSTANCE_COUNTER.get(getClass().getName());
    	if (count == null) {
    		count = new Integer(1);
    	} else {
    		count = new Integer(count.intValue() + 1);
    	}
    	INSTANCE_COUNTER.put(getClass().getName(), count);
    }


    // PreferencesValidator Impl -----------------------------------------------

    public void validate(PortletPreferences preferences)
    throws ValidatorException {
    	validateInvoked++;
    	String value = preferences.getValue(CHECK_VALIDATOR_COUNT, null);
    	if (value != null && value.equalsIgnoreCase("true")) {
    		checkValidatorCount();
    	}

        //
        // TODO: Determine why we use this - I seem to remember it's a
        //   spec requirement, and fix it so that we don't have issues
        //   anymore.  When enabled, all preferences fail in testsuite.
        //
        final String[] DEFAULT_VALUES = new String[] { "no values" };
    	Collection failedNames = new ArrayList();
        for (Enumeration en = preferences.getNames(); en.hasMoreElements(); ) {
            String name = (String) en.nextElement();
            String[] values = preferences.getValues(name, DEFAULT_VALUES);
            if (values != null) { // null values are allowed
                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null) { // null values are allowed
                        // Validate that the preferences do not
                    	//   start or end with white spaces.
                        if (!values[i].equals(values[i].trim())) {
                        	if (LOG.isDebugEnabled()) {
                        		LOG.debug("Validation failed: "
                        				+ "value has white spaces: "
                        				+ "name=" + name
                        				+ "; value=|" + values[i] + "|");
                        	}
                        	failedNames.add(name);
                        }
                    }
                }
            }
        }

        if (!failedNames.isEmpty()) {
            throw new ValidatorException(
            		"One or more preferences do not pass the validation.",
            		failedNames);
        }
    }

    private void checkValidatorCount() throws ValidatorException {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Checking validator count...");
    	}
    	Integer instanceCreated = (Integer) INSTANCE_COUNTER.get(
    			getClass().getName());
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("Method validate() invoked " + validateInvoked + " times.");
    		LOG.debug("Validator created " + instanceCreated.intValue() + " times.");
    	}
    	if (instanceCreated.intValue() != 1) {
    		throw new ValidatorException(instanceCreated.toString()
    				+ " validator instances were created, "
    				+ "expected 1 validator instance per portlet definition.",
    				null);
    	}
    }

}
