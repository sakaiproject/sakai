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

package edu.amc.sakai.user;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for LDAP attribute mapping constants.
 * 
 * @author Dan McCallum, Unicon Inc
 *
 */
public abstract class AttributeMappingConstants {
	
	/** Key into {@link #DEFAULT_ATTR_MAPPINGS} representing the logical
	 * name of a user entry's login (aka Sakai "EID") attribute
	 */
	public static final String LOGIN_ATTR_MAPPING_KEY = "login";
	
	/** Key into {@link #DEFAULT_ATTR_MAPPINGS} representing the logical
	 * name of a user entry's authentication (aka Sakai "AID") attribute
	 */
	public static final String AUTHENTICATION_ATTR_MAPPING_KEY = "aid";
	
	/** Key into {@link #DEFAULT_ATTR_MAPPINGS} representing the logical
	 * name of a user entry's given name attribute
	 */
	public static final String FIRST_NAME_ATTR_MAPPING_KEY = "firstName";
	
	/** Key into {@link #DEFAULT_ATTR_MAPPINGS} representing the logical
	 * name of a user entry's preferred given name attribute
	 */
	public static final String PREFERRED_FIRST_NAME_ATTR_MAPPING_KEY = "preferredFirstName";
	
	/** Key into {@link #DEFAULT_ATTR_MAPPINGS} representing the logical
	 * name of a user entry's surname attribute
	 */
	public static final String LAST_NAME_ATTR_MAPPING_KEY = "lastName";
	
	/** Key into {@link #DEFAULT_ATTR_MAPPINGS} representing the logical
	 * name of a user entry's email attribute
	 */
	public static final String EMAIL_ATTR_MAPPING_KEY = "email";
	
	/** Key into {@link #DEFAULT_ATTR_MAPPINGS} representing the logical
	 * name of a user entry's group membership attribute
	 */
	public static final String GROUP_MEMBERSHIP_ATTR_MAPPING_KEY = "groupMembership";
	
	public static final String DISPLAY_ID_ATTR_MAPPING_KEY = "displayId";
	
	public static final String DISPLAY_NAME_ATTR_MAPPING_KEY = "displayName";
	
	public static final String CANDIDATE_ID_ATTR_MAPPING_KEY = "candidateID";
	public static final String ADDITIONAL_INFO_ATTR_MAPPING_KEY = "additionalInfo";
	public static final String STUDENT_NUMBER_ATTR_MAPPING_KEY = "studentNumber";
	
	/** Default value in {@link #DEFAULT_ATTR_MAPPINGS} representing
	 * the physical name of a user entry's login (aka Sakai "EID") attribute
	 */
	public static final String DEFAULT_LOGIN_ATTR = "cn";
	
	/** Default value in {@link #DEFAULT_ATTR_MAPPINGS} representing
	 * the physical name of a user entry's authentication (aka Sakai "AID") attribute
	 */
	public static final String DEFAULT_AUTHENTICATION_ATTR = "dn";
	
	/** Default value in {@link #DEFAULT_ATTR_MAPPINGS} representing
	 * the physical name of a user entry's given name attribute
	 */
	public static final String DEFAULT_FIRST_NAME_ATTR = "givenName";
	
	/** Default value in {@link #DEFAULT_ATTR_MAPPINGS} representing
	 * the physical name of a user entry's preferred given name attribute.
	 */
	public static final String DEFAULT_PREFERRED_FIRST_NAME_ATTR = "preferredName";
	
	/** Default value in {@link #DEFAULT_ATTR_MAPPINGS} representing
	 * the physical name of a user entry's surname attribute
	 */
	public static final String DEFAULT_LAST_NAME_ATTR = "sn";
	
	/** Default value in {@link #DEFAULT_ATTR_MAPPINGS} representing
	 * the physical name of a user entry's email attribute
	 */
	public static final String DEFAULT_EMAIL_ATTR = "email";
	
	/** Default value in {@link #DEFAULT_ATTR_MAPPINGS} representing
	 * the physical name of a user entry's group membership attribute
	 */
	public static final String DEFAULT_GROUP_MEMBERSHIP_ATTR = "groupMembership";
	
	public static final String DEFAULT_CANDIDATE_ID_ATTR = "employeeNumber";
	public static final String DEFAULT_ADDITIONAL_INFO_ATTR = "description";
	public static final String DEFAULT_STUDENT_NUMBER_ID_ATTR = "employeeNumber";
	
	public final static String SYSTEM_PROP_ENCRYPT_NUMERIC_ID = "encryptInstitutionalNumericID";
	
	/**
	 * Default set of user entry attribute mappings. Keys are
	 * logical names, values are physical names.
	 */
	public static final Map<String,String> DEFAULT_ATTR_MAPPINGS = 
			new HashMap<String,String>();
	
	/**
	 * Extension of DEFAULT_ATTR_MAPPINGS
	 */
	public static final Map<String,String> CANDIDATE_ATTR_MAPPINGS = 
		new HashMap<String,String>();
	
	static {
		
		DEFAULT_ATTR_MAPPINGS.put(LOGIN_ATTR_MAPPING_KEY, DEFAULT_LOGIN_ATTR);
		DEFAULT_ATTR_MAPPINGS.put(AUTHENTICATION_ATTR_MAPPING_KEY, DEFAULT_AUTHENTICATION_ATTR);
		DEFAULT_ATTR_MAPPINGS.put(FIRST_NAME_ATTR_MAPPING_KEY, DEFAULT_FIRST_NAME_ATTR);
		DEFAULT_ATTR_MAPPINGS.put(PREFERRED_FIRST_NAME_ATTR_MAPPING_KEY, DEFAULT_PREFERRED_FIRST_NAME_ATTR);
		DEFAULT_ATTR_MAPPINGS.put(LAST_NAME_ATTR_MAPPING_KEY, DEFAULT_LAST_NAME_ATTR);
		DEFAULT_ATTR_MAPPINGS.put(EMAIL_ATTR_MAPPING_KEY, DEFAULT_EMAIL_ATTR);
		DEFAULT_ATTR_MAPPINGS.put(GROUP_MEMBERSHIP_ATTR_MAPPING_KEY, DEFAULT_GROUP_MEMBERSHIP_ATTR);
		
		
		CANDIDATE_ATTR_MAPPINGS.putAll(DEFAULT_ATTR_MAPPINGS);
		CANDIDATE_ATTR_MAPPINGS.put(CANDIDATE_ID_ATTR_MAPPING_KEY, DEFAULT_CANDIDATE_ID_ATTR);
		CANDIDATE_ATTR_MAPPINGS.put(ADDITIONAL_INFO_ATTR_MAPPING_KEY, DEFAULT_ADDITIONAL_INFO_ATTR);
		CANDIDATE_ATTR_MAPPINGS.put(STUDENT_NUMBER_ATTR_MAPPING_KEY, DEFAULT_STUDENT_NUMBER_ID_ATTR);
	}
	
	/**
	 * Key into {@link LdapUserData} properties linking to the user's
	 * directory DN value.
	 * 
	 * TODO: probably not the best-factored place for this constant
	 */
	public static final String USER_DN_PROPERTY = "udp.dn";
	
}
