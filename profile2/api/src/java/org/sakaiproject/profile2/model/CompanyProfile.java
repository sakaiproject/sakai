/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.model;

import java.io.Serializable;

/**
 * <code>CompanyProfile</code> is a model for storing information about a
 * business user's company profile. Business users may have more than one
 * company profile.
 */
public class CompanyProfile implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String userUuid;
	private String companyName;
	private String companyDescription;
	private String companyWebAddress;
	
	public CompanyProfile() {
		
	}

	public CompanyProfile(String userUuid, String companyName,
			String companyDescription, String companyWebAddress) {

		this.userUuid = userUuid;
		this.companyName = companyName;
		this.companyDescription = companyDescription;
		this.companyWebAddress = companyWebAddress;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyDescription() {
		return companyDescription;
	}

	public void setCompanyDescription(String companyDescription) {
		this.companyDescription = companyDescription;
	}

	public String getCompanyWebAddress() {
		return companyWebAddress;
	}

	public void setCompanyWebAddress(String companyWebAddress) {
		this.companyWebAddress = companyWebAddress;
	}
	
}
