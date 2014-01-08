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

import java.util.List;

import org.sakaiproject.signup.model.SignupSite;

/**
 * <p>
 * This class is a wrapper class for SignupSite for UI purpose
 * </P>
 */
public class SignupSiteWrapper {

	private SignupSite signupSite;

	private boolean selected = false;

	private boolean allowedToCreate = false;

	private List<SignupGroupWrapper> signupGroupWrappers;

	/**
	 * Constructor
	 * 
	 * @param signupSite
	 *            a SignupSite object.
	 * @param allowedToCreate
	 *            a boolean value.
	 */
	public SignupSiteWrapper(SignupSite signupSite, boolean allowedToCreate) {
		this.signupSite = signupSite;
		this.allowedToCreate = allowedToCreate;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return true if it's selected.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * This is a setter for UI.
	 * 
	 * @param selected
	 *            a boolean value.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * This is getter for UI.
	 * 
	 * @return a SignupSite object.
	 */
	public SignupSite getSignupSite() {
		return signupSite;
	}

	/**
	 * This is a setter.
	 * 
	 * @param signupSite
	 *            a SignupSite object.
	 */
	public void setSignupSite(SignupSite signupSite) {
		this.signupSite = signupSite;
	}

	/**
	 * This is a getter for UI.
	 * 
	 * @return true if the user is allowed to create.
	 */
	public boolean isAllowedToCreate() {
		return allowedToCreate;
	}

	/**
	 * This is a setter.
	 * 
	 * @param groupWrappers
	 *            a list of SignupGroupWrapper objects.
	 */
	public void setSignupGroupWrappers(List<SignupGroupWrapper> groupWrappers) {
		this.signupGroupWrappers = groupWrappers;

	}

	/**
	 * This is a getter.
	 * 
	 * @return a list of SignupGroupWrapper objects.
	 */
	public List<SignupGroupWrapper> getSignupGroupWrappers() {
		return signupGroupWrappers;
	}

}
