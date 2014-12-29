/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import java.util.Date;
import java.util.Stack;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserEdit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Mock UserEdit class for testing purposes.
 * @see org.sakaiproject.user.api.UserEdit
 * @author Dan McCallum, Unicon
 * @author John A. Lewis, Unicon
 */
class UserEditStub implements UserEdit {

	private String eid;
	private String email;
	private String firstName;
	private String id;
	private String lastName;
	private String type;
	
	// non-std user field, useful for caching test config
	private String login;
	
	// a std user field, but normally not accessible with a public getter.
	// not included in user equality tests.
	private String password;
	
    // BaseResorcePropertiesEdit does not override toString or equals()
	private ResourcePropertiesEdit properties = new ResourcePropertiesEditStub();

	public UserEditStub() {
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

    public void restrictEditEid() {
    }
	public void setEid(String eid) {
		this.eid = eid;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String name) {
		this.firstName = name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLastName(String name) {
		this.lastName = name;
		
	}

	public void setPassword(String pw) {
		this.password = pw;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean checkPassword(String pw) {
		return new EqualsBuilder().append(pw, password).isEquals();
	}

	public User getCreatedBy() {
		return null;
	}

	public Time getCreatedTime() {
		return null;
	}
    
    public String getUrlEmbeddableId() {
        return getDisplayId();
    }

	public String getEid() {
		return this.eid;
	}

	public String getEmail() {
		return this.email;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public User getModifiedBy() {
		return null;
	}

	public Time getModifiedTime() {
		return null;
	}

	public String getSortName() {
		return null;
	}

	public String getType() {
		return this.type;
	}

	public String getId() {
		return this.id;
	}
    
    public String getDisplayId() {
        return this.eid;
    }
         
    public String getDisplayName() {

		// This was copied from BaseUserDirectoryService

		StringBuilder buf = new StringBuilder(128);
		if (firstName != null)
			buf.append(firstName);
		if (lastName != null) {
			buf.append(" ");
			buf.append(lastName);
		}

		if (buf.length() == 0) {
			return getEid();
		} else {
			return buf.toString();
		}

	}
    

	public ResourceProperties getProperties() {
		return this.properties;
	}

	public String getReference() {
		return null;
	}

	public String getReference(String rootProperty) {
		return null;
	}

	public String getUrl() {
		return null;
	}

	public String getUrl(String rootProperty) {
		return null;
	}

	public Element toXml(Document doc, Stack stack) {
		return null;
	}

	public int compareTo(Object o) {
		return 0;
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		return this.properties;
	}

	public boolean isActiveEdit() {
		return false;
	}
	
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
			.append("id",this.id)
			.append("eid",this.eid)
			.append("firstName", this.firstName)
			.append("lastName", this.lastName)
			.append("email", this.email)
			.append("type", this.type)
			.append("properties", this.properties)
			.toString();
	}
	
	public boolean equals(Object o) {
		
		if (o == this)
			return true;
		
		if (o == null)
			return false;
		
		if (!(o instanceof User))
			return false;
		
		User user = (User)o;

		// Note that we do not compare login or password fields,
		// nor do we compare display ID or display name. The former
		// are not compared b/c they are non-std fields. The latter
		// are not compared b/c they either echo other fields, or
		// or built by locally-specific algorithms, which should
		// really tested separately. All we care about here
		// are that properties are mapped to the appropriate fields.
		// We also don't compare ID fields because this field is typically
		// not available and/or is not relevant to a UserDirectoryProvider
		return new EqualsBuilder().
			append(eid, user.getEid()).
			append(firstName, user.getFirstName()).
			append(lastName, user.getLastName()).
			append(email, user.getEmail()).
			append(type, user.getType()).
            append(properties, user.getProperties()).
            isEquals();
	}
	
	// following are "non-std" User/UserEdit accessors

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPropertiesEdit(ResourcePropertiesEdit properties) {
		this.properties = properties;
	}

	public Date getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
