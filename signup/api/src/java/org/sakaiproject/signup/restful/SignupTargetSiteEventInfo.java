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

package org.sakaiproject.signup.restful;

import org.sakaiproject.signup.model.SignupMeeting;

/**
 * <p>
 * This class holds the information of sign-up meeting object and the permission
 * information regarding to the targeted siteId.
 * </p>
 * 
 * @author Peter Liu
 * 
 */
public class SignupTargetSiteEventInfo {

	private SignupMeeting signupMeeting;

	/*
	 * the permission info in signupMeeting object will be related to this
	 * targetSiteId
	 */
	private String targetSiteId;

	public SignupTargetSiteEventInfo(SignupMeeting sMeeting, String targetSiteId) {
		this.signupMeeting = sMeeting;
		this.targetSiteId = targetSiteId;
	}

	public SignupMeeting getSignupMeeting() {
		return signupMeeting;
	}

	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

	public String getTargetSiteId() {
		return targetSiteId;
	}

	public void setTargetSiteId(String targetSiteId) {
		this.targetSiteId = targetSiteId;
	}

}
