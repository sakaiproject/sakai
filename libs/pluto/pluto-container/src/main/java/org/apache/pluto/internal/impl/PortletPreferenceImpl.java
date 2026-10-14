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
package org.apache.pluto.internal.impl;

import org.apache.pluto.internal.InternalPortletPreference;

/**
 * TODO: javadoc
 * @version 1.0
 * @since Sep 20, 2004
 */
public class PortletPreferenceImpl implements InternalPortletPreference {

	// Private Member Variables ------------------------------------------------

	/** The preference name. */
    private String name;

    /** The preference values. */
    private String[] values;

    /** Flag indicating if this preference is read-only. */
    private boolean readOnly = false;


    // Constructors ------------------------------------------------------------

    public PortletPreferenceImpl(String name, String[] values) {
        this.name = name;
        this.values = values;
        this.readOnly = false;
    }

    public PortletPreferenceImpl(String name,
                                 String[] values,
                                 boolean readOnly) {
        this.name = name;
        this.values = values;
        this.readOnly = readOnly;
    }

    /**
     * Private constructor that is used only within the <code>clone()</code>
     * method.
     * @see #clone()
     */
    private PortletPreferenceImpl() {
    	// Do nothing.
    }


    // PortletPreference Impl --------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Object clone() {
    	PortletPreferenceImpl copy = new PortletPreferenceImpl();
    	copy.name = this.name;
    	if (this.values != null) {
    		copy.values = (String[]) this.values.clone();
    	}
    	copy.readOnly = this.readOnly;
    	return copy;
    }


    // Object Methods ----------------------------------------------------------

    /**
     * Override of toString() that prints out name and values of fields.
     * @see java.lang.Object#toString()
     */
    public String toString(){
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(getClass().getName());
    	buffer.append("[name=").append(name).append(";");
    	if (values != null) {
    		for (int i = 0; i < values.length; i++) {
    			buffer.append("value[").append(i).append("]=")
    					.append(values[i]).append(";");
    		}
    	} else {
    		buffer.append("values=NULL;");
    	}
    	buffer.append("readOnly=").append(readOnly).append("]");
    	return buffer.toString();
    }

}

