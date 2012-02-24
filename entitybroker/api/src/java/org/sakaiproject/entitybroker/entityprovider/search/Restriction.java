/**
 * $Id$
 * $URL$
 * Restriction.java - genericdao - Aug 3, 2008 12:43:54 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */
package org.sakaiproject.entitybroker.entityprovider.search;

import java.util.Collection;

/**
 * A simple bean which defines a restriction in a search, this is like saying:
 * where userId = '123'; OR where userId like '%aaronz%';<br/>
 * Example usage:<br/>
 * <code>Restriction rteq = new Restriction("title", curTitle); // restrict search to title equals value of curTitle</code><br/>
 * <code>Restriction rtne = new Restriction("title", curTitle, Restriction.NOT_EQUALS); // restrict search to title not equals value of curTitle</code><br/>
 *
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class Restriction {

    public final static int EQUALS = 0;
    public final static int GREATER = 1;
    public final static int LESS = 2;
    public final static int LIKE = 3;
    public final static int NULL = 4;
    public final static int NOT_NULL = 5;
    public final static int NOT_EQUALS = 6;

    /**
     * the name of the field (property) in the search
     */
    public String property;
    /**
     * the name of the field (property) in the search
     */
    public String getProperty() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }

    // TODO this should probably be a weak reference
    /**
     * the value of the {@link #property} (can be an array of items)
     */
    public Object value;
    /**
     * the value of the {@link #property} (can be an array of items)
     */
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    // HELPER methods

    /**
     * @return the string value of the {@link #property},
     * converts arrays to strings automatically,
     * if null then return ''
     */
    public String getStringValue() {
        String s = "";
        if (value != null) {
            if (value.getClass().isArray()) {
                s = Search.arrayToString((Object[]) value);
            } else {
                s = value.toString();
            }
        }
        return s;
    }

    /**
     * @return the value as a boolean, will return false if it is null or cannot be converted to a boolean
     */
    public boolean getBooleanValue() {
        Object o = getSingleValue();
        if (o != null) {
            if (o instanceof Boolean) {
                return (Boolean) o;
            } else if (o instanceof String) {
                return Boolean.valueOf((String)o);
            }
        }
        return false;
    }

    /**
     * @return the value if it is not an array or collection,
     * if it is then return the first object in the array or collection,
     * otherwise return null (including when value is null)
     */
    @SuppressWarnings("unchecked")
    public Object getSingleValue() {
        Object result = null;
        if (value != null) {
            if (value.getClass().isArray()) {
                if (((Object[])value).length > 0) {
                    result = ((Object[])value)[0];
                }
            } else if (value.getClass().isAssignableFrom(Collection.class)) {
                if (! ((Collection)value).isEmpty()) {
                    result = ((Collection)value).iterator().next();
                }
            } else {
                result = value;
            }
        }
        return result;
    }

    /**
     * @return the value as an array if it is an array or a collection,
     * if it is a single value then return null (also when value is null)
     */
    @SuppressWarnings("unchecked")
    public Object[] getArrayValue() {
        Object[] result = null;
        if (value != null) {
            if (value.getClass().isArray()) {
                result = (Object[])value;
            } else if (value.getClass().isAssignableFrom(Collection.class)) {
                result = ((Collection)value).toArray();
            }
        }      
        return result;
    }


    /**
     * the comparison to make between the property and the value,
     * use the defined constants: e.g. EQUALS, LIKE, etc...
     */
    public int comparison = EQUALS;
    /**
     * the comparison to make between the property and the value,
     * use the defined constants: e.g. EQUALS, LIKE, etc...
     */
    public int getComparison() {
        return comparison;
    }
    public void setComparison(int comparison) {
        this.comparison = comparison;
    }

    /**
     * Simple restriction where the property must equal the value
     * @param property the name of the field (property) in the persisted object
     * @param value the value of the {@link #property} (can be an array of items)
     */
    public Restriction(String property, Object value) {
        this.property = property;
        this.value = value;
    }

    /**
     * Restriction which defines the type of comparison to make between a property and value
     * @param property the name of the field (property) in the persisted object
     * @param value the value of the {@link #property} (can be an array of items)
     * @param comparison the comparison to make between the property and the value,
     * use the defined constants: e.g. EQUALS, LIKE, etc...
     */
    public Restriction(String property, Object value, int comparison) {
        this.property = property;
        this.value = value;
        this.comparison = comparison;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (!(obj instanceof Restriction))
            return false;
        else {
            Restriction castObj = (Restriction) obj;
            boolean eq = (this.property == null ? castObj.property == null : this.property.equals(castObj.property))
            && (this.value == null ? castObj.value == null : this.value.equals(castObj.value))
            && this.comparison == castObj.comparison;
            return eq;
        }
    }

    @Override
    public int hashCode() {
        String hashStr = this.getClass().getName() + ":" + this.property + ":" + this.comparison + ":" + this.value;
        return hashStr.hashCode();
    }

    @Override
    public String toString() {
        return "restriction::prop:" + property + ",value:" + value + ",comp:" + comparison;
    }

}
