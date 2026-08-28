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
package org.apache.pluto.internal;

/**
 * This class represents a portlet preference, which is a name-value pair.
 *
 */
public interface InternalPortletPreference {

	/**
	 * Returns the name of this portlet preference.
	 * @return the name of this portlet preference.
	 */
    String getName();

    /**
     * Returns the values of this portlet preference, which is a string array.
     * @return the values of this portlet preference as a string array.
     */
    String[] getValues();

    /**
     * Sets values of this portlet preference.
     * @param values  values of this portlet preference to set.
     */
    void setValues(String[] values);

    /**
     * Returns true if this portlet preference is marked as read-only.
     * @return true if this portlet preference is marked as read-only.
     */
    boolean isReadOnly();

    /**
     * Clone a copy of itself.
     * @return a copy of itself.
     */
    Object clone();
}
