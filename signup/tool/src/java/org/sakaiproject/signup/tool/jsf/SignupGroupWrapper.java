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

package org.sakaiproject.signup.tool.jsf;

import org.sakaiproject.signup.model.SignupGroup;

/**
 * <p>
 * This class is a wrapper class for SignupGroup for UI purpose
 * </P>
 */
public class SignupGroupWrapper {

	private SignupGroup signupGroup;

	private boolean selected = false;

	private boolean allowedToCreate = false;

	/**
	 * Constructor
	 * 
	 * @param signupGroup
	 *            a SignupGroup object.
	 * @param allowedToCreate
	 *            a boolean value.
	 */
	public SignupGroupWrapper(SignupGroup signupGroup, boolean allowedToCreate) {
		this.signupGroup = signupGroup;
		this.allowedToCreate = allowedToCreate;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if it is selected.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * This is a setter.
	 * 
	 * @param selected
	 *            a boolean value.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a Signupgroup object.
	 */
	public SignupGroup getSignupGroup() {
		return signupGroup;
	}

	/**
	 * This is a setter.
	 * 
	 * @param signupGroup
	 *            a SignupGroup object.
	 */
	public void setSignupGroup(SignupGroup signupGroup) {
		this.signupGroup = signupGroup;
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return true if user is allowed to create.
	 */
	public boolean isAllowedToCreate() {
		return allowedToCreate;
	}

}
