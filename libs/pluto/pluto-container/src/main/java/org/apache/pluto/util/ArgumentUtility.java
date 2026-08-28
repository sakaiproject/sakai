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
package org.apache.pluto.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static class that provides utility static methods for argument validation.
 *
 */
public class ArgumentUtility {

	/** Logger. */
    public static final Log LOG = LogFactory.getLog(ArgumentUtility.class);


    // Static Utility Methods --------------------------------------------------

    /**
     * Validates that the passed-in argument value is not null.
     * @param argumentName  the argument name.
     * @param argument  the argument value.
     * @throws IllegalArgumentException  if the argument value is null.
     */
    public static void validateNotNull(String argumentName, Object argument)
    throws IllegalArgumentException {
        if (argument == null) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Validation failed for argument: " + argumentName
        				+ ": argument should not be null.");
        	}
        	throw new IllegalArgumentException(
        			"Illegal Argument: " + argumentName
        			+ " (argument should not be null)");
        }
    }

    /**
     * Validates that the passed-in string argument value is not null or empty.
     * @param argumentName  the argument name.
     * @param argument  the argument value.
     * @throws IllegalArgumentException  if the argument value is null or empty.
     */
    public static void validateNotEmpty(String argumentName, String argument)
    throws IllegalArgumentException {
        if (argument == null || "".equals(argument)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validation failed for argument: " + argumentName
                		+ ": argument should not be null or empty.");
            }
            throw new IllegalArgumentException(
            		"Illegal Argument: " + argumentName
            		+ " (argument should not be null or empty)");
        }
    }

}
