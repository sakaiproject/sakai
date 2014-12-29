/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.section.api.facade;

import java.io.Serializable;

/**
 * A type-safe enumeration of the roles a user can play in a LearningContext.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class Role implements Serializable {
	private static final long serialVersionUID = 1L;

    public static final Role NONE = new Role("", 0);
    public static final Role INSTRUCTOR = new Role("instructor", 1);
    public static final Role STUDENT = new Role("student", 2);
    public static final Role TA = new Role("ta", 3);

    private final String mName;
    private final int mValue;

    /**
     * Private constructor.
     */
    private Role(String name, int value) {
        mName = name;
        mValue = value;
    }
    
    /**
     * Returns whether this instance is the {@link Role#NONE} instance.
     *
     * @return whether this instance is the {@link Role#NONE} instance
     */
    public boolean isNone() {
        return this == Role.NONE;
    }
    /**
     * Returns whether this instance is the {@link Role#INSTRUCTOR} instance.
     *
     * @return whether this instance is the {@link Role#INSTRUCTOR} instance
     */
    public boolean isInstructor() {
        return this == Role.INSTRUCTOR;
    }
    /**
     * Returns whether this instance is the {@link Role#INSTRUCTOR} instance.
     *
     * @return whether this instance is the {@link Role#INSTRUCTOR} instance
     */
    public boolean isTeachingAssistant() {
        return this == Role.TA;
    }
    /**
     * Returns whether this instance is the {@link Role#STUDENT} instance.
     *
     * @return whether this instance is the {@link Role#STUDENT} instance
     */
    public boolean isStudent() {
        return this == Role.STUDENT;
    }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the value.
     *
     * @return the value.
     */
    public int getValue() {
        return mValue;
    }

	public String getDescription() {
		return mName;
	}

    /**
     * Returns a String description of this <code>Role</code>.
     * 
     * Do not use commons-lang ToStringBuilder, since this class will be
     * deployed to sakai's shared/lib.
     *
     * @return a String description of this object.
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Role[");
        result.append("name=").append(getName());
        result.append(", ");
        result.append("value=").append(getValue());
        result.append("]");
        return result.toString();
    }

    /**
     * This role is considered equal if it is a Role whos getValue() is equal
     * to this object's getValue().
     * 
     * Do not use commons-lang EqualsBuilder, since this class will be
     * deployed to sakai's shared/lib.
     * 
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object o) {
        // Algorithm from "Effective Java" by Joshua Bloch.
        if (o == this) {
            return true;
        }
        if (!(o instanceof Role)) {
            return false;
        }
        Role other = (Role) o;
        return getValue() == other.getValue();
    }

    /**
     * Do not use commons-lang HashCodeBuilder, since this class will be
     * deployed to sakai's shared/lib.
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int result = 17 * getClass().getName().hashCode();
        result = 37 * result + getValue();
        return result;
    }
}
