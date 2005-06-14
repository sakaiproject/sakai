/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/component/src/java/org/sakaiproject/tool/gradebook/facades/Role.java,v 1.2 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.gradebook.facades;

/**
 * This final class implements a type-safe enumeration
 * over the valid instances of a Role.
 * Instances of this class are immutable.
 */
public final class Role {

	/**
	 * Role with Value = 0
	 */
	public static final Role NONE = new Role("", 0);

	/**
	 * Role with Value = 1
	 */
	public static final Role INSTRUCTOR = new Role("instructor", 1);

	/**
	 * Role with Value = 2
	 */
	public static final Role STUDENT = new Role("student", 2);

	private static final Role[] ALL = new Role[] {
		NONE,
		INSTRUCTOR,
		STUDENT,
	};

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
	 * Returns a copy of the array with all instances of this class.
	 * <p>
	 * Modifying the returned array will not affect this class.
	 *
	 * @return an array with all instances of this class
	 */
	public static Role[] all() {

		Role[] result = new Role[ALL.length];
		System.arraycopy(ALL, 0, result, 0, ALL.length);
		return result;
	}

	/**
	 * Returns the <code>Role</code> for the specified key field(s),
	 * or returns <code>NONE</code>
	 * if no <code>Role</code> exists for the specified key field(s).
	 *
	 * @param value the value of the <code>Role</code> to find
	 * @return the <code>Role</code> for the specified key field(s)
	 */
	public static Role lookup(int value) {
		for (int i = 0; i < ALL.length; i++) {
			if (ALL[i].getValue() != value) {
				continue;
			}
			return ALL[i];
		}
		return NONE;
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

	/**
	 * Returns a String description of this <code>Role</code>.
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

	/*
	 * Non-javadoc.
	 * @see java.lang.Object#equals(Object)
	 */
	// Algorithm from "Effective Java" by Joshua Bloch.
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Role)) {
			return false;
		}
		Role other = (Role) o;
		return true
			&& getValue() == other.getValue()
			;
	}

	/*
	 * Non-javadoc.
	 * @see java.lang.Object#hashCode()
	 */
	// Algorithm from "Effective Java" by Joshua Bloch.
	public int hashCode() {
		int result = 17 * getClass().getName().hashCode();
		result = 37 * result + getValue();
		return result;
	}

	/**
	 * If this class implements <code>java.io.Serializable</code>,
	 * the Java serialization mechanism provides a "hidden constructor".
	 * To ensure that no other instances are created than the
	 * ones declared above, we implement <code>readResolve</code>.
	 * (This is not necessary if this class does not
	 * implement <code>java.io.Serializable</code>).
	 */
	// Algorithm from "Effective Java" by Joshua Bloch.
	private Object readResolve() throws java.io.ObjectStreamException {

		// look at the key attribute values of the instance
		// that was just deserialized,
		// and replace the deserialized instance
		// with one of the static objects
		return lookup(mValue);
	}
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/component/src/java/org/sakaiproject/tool/gradebook/facades/Role.java,v 1.2 2005/05/26 18:04:54 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
