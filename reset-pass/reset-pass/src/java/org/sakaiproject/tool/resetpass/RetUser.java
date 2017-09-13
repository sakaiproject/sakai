/**
 * Copyright (c) 2006-2007 The Apereo Foundation
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
package org.sakaiproject.tool.resetpass;

import org.sakaiproject.user.api.User;

public class RetUser {

	
	private String email;
	public void setEmail(String e) {
		this.email=e;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	private User user;
	public void setUser(User ue){
		this.user=ue;
	}
	public User getUser() {
		return user;
	}
	
}
