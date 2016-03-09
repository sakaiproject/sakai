/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.unboundid;

import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Snapshot of a user directory entry, typically used for
 * cache entries in {@link UnboundidDirectoryProvider}
 * 
 * @author David Ross, Albany Medical College
 * @author Rishi Pande, Virginia Tech
 * @author Dan McCallum, Unicon Inc
 */
public class LdapUserData
{

	private String eid;

	private String firstName;
	
	private String preferredFirstName;
	
	private String lastName;

	private String email;

	private String type;
	
	private Properties properties;
	

	/**
	 * @return Returns the email.
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email
	 *        The email to set.
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}

	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * @param firstName
	 *        The firstName to set.
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	
	public String getPreferredFirstName() {
		return preferredFirstName;
	}

	public void setPreferredFirstName(String preferredFirstName) {
		this.preferredFirstName = preferredFirstName;
	}

	/**
	 * @return Returns the eid.
	 */
	public String getEid()
	{
		return eid;
	}

	/**
	 * @param eid
	 *        The eid to set.
	 */
	public void setEid(String eid)
	{
		this.eid = eid;
	}

	/**
	 * @return Returns the lastName.
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * @param lastName
	 *        The lastName to set.
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type
	 *        The type to set.
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return Returns the user's properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties assign the user's properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * Assign a single property to the user, possibly
	 * overwriting and existing entry.
	 * 
	 * @param key the property's key
	 * @param value the property's value
	 */
	public void setProperty(String key, String value) {
		if ( properties == null ) {
			properties = new Properties();
		}
		properties.setProperty(key, value);
	}
	
	/**
	 * Output a multi-line String representation of this
	 * <code>LdapUserData</code> instance.
	 */
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
			.append("eid",eid)
			.append("firstName",firstName)
			.append("preferredFirstName",preferredFirstName)
			.append("lastName",lastName)
			.append("email",email)
			.append("type",type)
			.append("properties",properties)
			.toString();
	}
}
