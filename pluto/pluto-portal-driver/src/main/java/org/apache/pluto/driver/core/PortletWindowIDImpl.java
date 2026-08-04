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
package org.apache.pluto.driver.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.pluto.PortletWindowID;

/**
 * Wraps around the internal Object IDs. By holding both the string and the
 * integer version of an Object ID this class helps speed up the internal
 * processing.
 */
public class PortletWindowIDImpl implements PortletWindowID, Serializable {

	// Private Member Variables ------------------------------------------------

    private String stringId;
    private int intId;


    // Constructor -------------------------------------------------------------

    /**
     * Private constructor that prevents external instantiation.
     * @param intId  the integer ID.
     * @param stringId  the string ID.
     */
    private PortletWindowIDImpl(int intId, String stringId) {
        this.stringId = stringId;
        this.intId = intId;
    }


    // PortletWindowID Impl ----------------------------------------------------

    public String getStringId() {
        return stringId;
    }

    // Internal Methods --------------------------------------------------------

    private void readObject(ObjectInputStream stream) throws IOException {
    	intId = stream.readInt();
        stringId = String.valueOf(intId);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.write(intId);
    }

    // Common Object Methods ---------------------------------------------------

    /**
    public boolean equals(Object object) {
        boolean result = false;
        if (object instanceof PortletWindowIDImpl) {
            result = (intId == ((PortletWindowIDImpl) object).intId);
        } else if (object instanceof String) {
            result = stringId.equals(object);
        } else if (object instanceof Integer) {
            result = (intId == ((Integer) object).intValue());
        }
        return (result);
    }
    **/

    public int hashCode() {
        return intId;
    }


    // Additional Methods ------------------------------------------------------

    public int intValue() {
        return intId;
    }

    /**
     * Creates a portlet window ID instance from a string.
     * @param stringId  the string ID from which the instance is created.
     * @return a portlet window ID instance created from the string ID.
     */
    static public PortletWindowIDImpl createFromString(String stringId) {
        char[] id = stringId.toCharArray();
        int _id = 1;
        for (int i = 0; i < id.length; i++) {
            if ((i % 2) == 0) {
                _id *= id[i];
            } else {
                _id ^= id[i];
            }
            _id = Math.abs(_id);
        }
        return new PortletWindowIDImpl(_id, stringId);
    }

}
