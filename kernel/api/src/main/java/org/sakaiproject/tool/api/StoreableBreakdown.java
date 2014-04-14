/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 The Apereo Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.api;

import org.sakaiproject.tool.api.Breakdownable.BreakdownableSize;

import java.io.Serializable;

/**
 * Holds the data which results from a Breakdown of object data.
 * Can be safely stored in anything which holds serializable objects
 */
public class StoreableBreakdown implements Serializable {

    private static final long serialVersionUID = 1L;

    private String className;
    private BreakdownableSize size;
    private Serializable data;

    /** Parent session ID
     * Populated when this is stashed
     */
    private transient String sessionId;
    /** Attribute key in the parent session
     * Populated when this is stashed
     */
    private transient String attributeKey;

    /**
     * Creates a POJO to place into a cache or other location which can be used to rebuild the
     * true object later as needed
     *
     * @param className the fully qualified classname (e.g. org.sakaiproject.tool.api.StoredBreakdownable)
     * @param size the size hint
     * @param data serialized data from the class
     */
    public StoreableBreakdown(String className, BreakdownableSize size, Serializable data) {
        this.size = size;
        this.className = className;
        this.data = data;
    }

    /**
     * Make a stash key from the input values
     * @param sessionId the session id
     * @param attributeKey the attribute key for the attribute in the session
     * @return the generated key OR null if the values are null
     */
    public static String makeStashKey(String sessionId, String attributeKey) {
        if (sessionId != null && attributeKey != null) {
            return sessionId+"_"+attributeKey;
        }
        return null;
    }

    /**
     * Adds stash data to this storable
     * Stash data is NOT serialized
     *
     * @param sessionId the session id
     * @param attributeKey the attribute key for the attribute in the session
     * @return the stash key OR null if the inputs were null
     */
    public String makeStash(String sessionId, String attributeKey) {
        this.sessionId = sessionId;
        this.attributeKey = attributeKey;
        return makeStashKey(sessionId, attributeKey);
    }

    /**
     * @return generated key OR null if this is not a stash
     */
    public String generateStashKey() {
        if (isStashed() && this.sessionId != null && this.attributeKey != null) {
            return this.sessionId+"_"+this.attributeKey;
        }
        throw new IllegalStateException("no stash data in this storeable");
    }

    /**
     * @return true if this is a stashed storeable
     */
    public boolean isStashed() {
        return attributeKey != null;
    }

    public BreakdownableSize getSize() {
        return size;
    }

    public void setSize(BreakdownableSize size) {
        this.size = size;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StoreableBreakdown that = (StoreableBreakdown) o;
        return className.equals(that.className)  && size == that.size && data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + size.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SB{" +
                "className='" + className + '\'' +
                ", size=" + size +
                ", data=" + data.hashCode() +
                '}';
    }

}
