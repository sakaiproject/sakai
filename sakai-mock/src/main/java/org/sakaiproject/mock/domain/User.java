/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mock.domain;

import java.util.Date;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class User implements org.sakaiproject.user.api.UserEdit {
	String pw;
	String id;
	String eid;
	String displayId;

	String email;
	String firstName;
	String lastName;
	String sortName;
	String displayName;

	User createdBy;
	User modifiedBy;
	
	String type;
	String reference;
	ResourceProperties properties;
	String url;
	
	Time createdTime = new org.sakaiproject.mock.domain.Time(new Date());
	Time modifiedTime = new org.sakaiproject.mock.domain.Time(new Date());

	public User() {};
	
	public User(String pw, String id, String eid, String displayId, String email, String firstName, String lastName,
			String sortName, String displayName, User createdBy, User modifiedBy, String type, String reference,
			ResourceProperties properties, String url, Time createdTime, Time modifiedTime) {
		this.pw = pw;
		this.id = id;
		this.eid = eid;
		this.displayId = displayId;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.sortName = sortName;
		this.displayName = displayName;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
		this.type = type;
		this.reference = reference;
		this.properties = properties;
		this.url = url;
		this.createdTime = createdTime;
		this.modifiedTime = modifiedTime;
	}

	public boolean checkPassword(String pw) {
		return pw.equals(this.pw);
	}

	public String getReference(String rootProperty) {
		return reference;
	}

	public String getUrl(String rootProperty) {
		return url;
	}

	public Element toXml(Document doc, Stack stack) {
		return null;
	}

	public int compareTo(Object o) {
		return 0;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public String getDisplayId() {
		return displayId;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public User getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public ResourceProperties getProperties() {
		return properties;
	}

	public void setProperties(ResourceProperties resourceProperties) {
		this.properties = resourceProperties;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Time getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Time createdTime) {
		this.createdTime = createdTime;
	}

	public Time getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Time modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public void restrictEditEmail() {
	}

	public void restrictEditFirstName() {
	}

	public void restrictEditLastName() {
	}

	public void restrictEditPassword() {
	}

	public void restrictEditType() {
	}

	public void setPassword(String pw) {
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		return null;
	}

	public boolean isActiveEdit() {
		return false;
	}

}
