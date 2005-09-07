/**********************************************************************************
*
* $Id: $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.api.section.facade;

import java.io.Serializable;

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
        StringBuffer result = new StringBuffer();
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



/**********************************************************************************
 * $Id: $
 *********************************************************************************/
