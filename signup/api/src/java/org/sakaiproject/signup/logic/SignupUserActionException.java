/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic;

/**
 * <P>
 * This class defines a specific exception caused by user action in Signup tool
 * </P>
 */
public class SignupUserActionException extends java.lang.Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of <code>SignupUserActionException</code>
	 * without detail message.
	 */
	public SignupUserActionException() {
	}

	/**
	 * Constructs an instance of <code>SignupUserActionException</code> with
	 * the specified detail message.
	 * 
	 * @param t
	 *            a Throwable object
	 */
	public SignupUserActionException(Throwable t) {
		super(t);
	}

	/**
	 * Constructs an instance of <code>SignupUserActionException</code> with
	 * the specified detail message.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	public SignupUserActionException(String msg) {
		super(msg);
	}
}
