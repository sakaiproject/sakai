/**
 * Copyright (c) 2007-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
