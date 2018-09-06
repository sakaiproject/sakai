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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPAttribute;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;

/**
 * Maps from a user entry's attribute to a Sakai user type. Will consider
 * attribute values in the order received, returning the first non-null
 * mapping. If no non-null mappings can be found, will return either a 
 * configurable default or the first attribute value, unmodified.
 * 
 * TODO: could probably refactor to reduce code duplication with 
 *   EntryContainerRdnToUserTypeMapper
 * 
 * @author Dan McCallum, Unicon Inc
 */
@Slf4j
public class EntryAttributeToUserTypeMapper implements UserTypeMapper {
	
	/** map of attribute values to Sakai user types */
	private Map<String,String> attributeValueToSakaiUserTypeMap = new HashMap<String,String>();
	
	/** A key into the current attribute map ({@link LdapAttributeMapper}) */
	private String logicalAttributeName;
	
	/** Controls behavior when no value mappings resolve. If <code>true</code>, will return 
	 * the first attribute value encountered. Otherwise returns {@link #defaultSakaiUserType} */
	private boolean returnLiteralAttributeValueIfNoMapping = false;
	
	/** Value returned if no mappings are found and {@link #returnLiteralAttributeValueIfNoMapping} 
	 * is <code>false</code> */
	private String defaultSakaiUserType;
	
	/**
	 * Maps the attribute values associated with the specified
	 * <code>LDAPEntry</code> to a Sakai user type.
	 * {@link #setLogicalAttributeName(String)} defines the
	 * attribute name to consult.
	 * {@link #setAttributeValueToSakaiUserTypeMap(Map)} controls
	 * the attribute value mappings.
	 * {@link #setReturnLiteralAttributeValueIfNoMapping(boolean)}
	 * controls behavior when attribute value mapping fails.
	 * {@link #setDefaultSakaiUserType(String)} assigns the String
	 * to return if attribute value mapping fails and literal value
	 * returns have been disabled. 
	 * 
	 * @see #getUserTypeAttribute(LDAPEntry, LdapAttributeMapper)
	 * @param ldapEntry the <code>LDAPEntry</code> to map
	 * @param mapper a container for attribute mapping config
	 * @return a Sakai user type, possibly null
	 */
	public String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry,
			LdapAttributeMapper mapper) {
		
			log.debug("mapLdapEntryToSakaiUserType(): [entry DN = {}]", ldapEntry.getDN());
		
		LDAPAttribute userTypeAttr = 
			getUserTypeAttribute(ldapEntry, mapper);
		
		if ( userTypeAttr == null ) {
			return null;
		}
		
		String[] userTypeAttrValues = 
			getUserTypeAttribute(ldapEntry, mapper).getStringValueArray();
		
		String userType = mapUserTypeAttributeValues(userTypeAttrValues);
		log.debug("mapLdapEntryToSakaiUserType(): finished mapping [user type = " + 
				"{}][entry values = {}][entry DN = {}]", userType, Arrays.toString(userTypeAttrValues), ldapEntry.getDN());
		return userType;
		
	}
	
	/**
	 * Extracts the <code>LDAPAttribute</code> which represents the
	 * <code>LDAPEntry</code>'s group membership. This is accomplished
	 * by querying the given {@link LdapAttributeMapper} for an
	 * attribute name corresponding to {@link #getLogicalAttributeName()}.
	 * 
	 * @param ldapEntry the <code>LDAPEntry</code> from which to extract a
	 *   <code>LDAPAttribute</code>
	 * @param mapper a container for attribute mapping config
	 * @return a user type <code>LDAPAttribute</code> or <code>null</code>
	 *   if none could be found on the given <code>LDAPEntry</code>
	 */
	protected LDAPAttribute getUserTypeAttribute(LDAPEntry ldapEntry, 
			LdapAttributeMapper mapper) {
		
		log.debug("getUserTypeAttribute(): [entry DN = {}]", ldapEntry.getDN());
		
		if ( StringUtils.isBlank(logicalAttributeName) ) {
			log.debug("getUserTypeAttribute(): no logical attribute name specified, returning null");
			return null;
		}
		
		Map<String,String> mappings = mapper.getAttributeMappings();
		String attrName = mappings.get(logicalAttributeName);
		
		if ( attrName == null ) {
			log.debug("getUserTypeAttribute(): failed to find attribute mapping [logical attr name = " +
					"{}][entry DN = {}]", logicalAttributeName, ldapEntry.getDN());
			return null;
		}
		
		LDAPAttribute attr = ldapEntry.getAttribute(attrName);
		if ( attr == null ) {
			log.debug("getUserTypeAttribute(): entry had no Sakai user type attr [physical attr name = " +
					"{}][entry DN = {}]", attrName, ldapEntry.getDN());
		}
		return attr;
	}
	
	/**
	 * Maps the given list of attribute values to a single Sakai
	 * user type by passing each received String value, in order, to 
	 * {@link #mapUserTypeAttributeValue(String)}. Exits with the first 
	 * non-<code>null</code> return from that method or a value
	 * dependent on current value of 
	 * {@link #isReturnLiteralAttributeValueIfNoMapping()}
	 * 
	 * @param attrValues String values to test
	 * @return a Sakai user type, possibly null
	 */
	protected String mapUserTypeAttributeValues(String[] attrValues) {
	for ( String value : attrValues ) {
			String userType = mapUserTypeAttributeValue(value);
			if ( userType != null ) {
				return userType;
			}
		}
		
		if ( isReturnLiteralAttributeValueIfNoMapping() ) {
			return attrValues.length > 0 ? attrValues[0] : null;
		}
		
		return defaultSakaiUserType;
	}
	

	/**
	 * Effectively the same as 
	 * <code>getAttributeValueToSakaiUserTypeMap().get(attrValue)</code>.
	 * Returns <code>null</code> if no mapping has been assigned.
	 * 
	 * @param attrValue the attribute value to map
	 * @return the mapped value or <code>null</code> if no map or mapping
	 */
	protected String mapUserTypeAttributeValue(String attrValue) {
		if ( attributeValueToSakaiUserTypeMap == null ) {
			return null;
		}
		
		return attributeValueToSakaiUserTypeMap.get(attrValue);
	}

	/**
	 * Retrieve the current attribute value-to-Sakai user type map.
	 * 
	 * @return a map of attribute values to Sakai user types
	 */
	public Map<String, String> getAttributeValueToSakaiUserTypeMap() {
		return attributeValueToSakaiUserTypeMap;
	}

	/**
	 * Assign the attribute value-to-Sakai user type map.
	 * 
	 * @param attributeValueToSakaiUserTypeMap a map of attribute 
	 *   values to Sakai user types
	 */
	public void setAttributeValueToSakaiUserTypeMap(
			Map<String, String> attributeValueToSakaiUserTypeMap) {
		this.attributeValueToSakaiUserTypeMap = attributeValueToSakaiUserTypeMap;
	}

	/**
	 * Retrieve the default Sakai user type, which will be returned from
	 * {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}
	 * if no mapping was found and 
	 * {@link #isReturnLiteralAttributeValueIfNoMapping()} is <code>false</code>
	 * 
	 * @return the default Sakai user type to return from 
	 *   {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}
	 */
	public String getDefaultSakaiUserType() {
		return defaultSakaiUserType;
	}

	/**
	 * Assign the default Sakai user type.
	 * 
	 * @see #getDefaultSakaiUserType()
	 * 
	 * @param defaultSakaiUserType the default Sakai user type to return from 
	 *   {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}
	 */
	public void setDefaultSakaiUserType(String defaultSakaiUserType) {
		this.defaultSakaiUserType = defaultSakaiUserType;
	}

	/**
	 * Access the key to a physical attribute name which will be used
	 * to resolve Sakai user types. Physical names are
	 * retrieved by querying the {@link LdapAttributeMapper} passed
	 * to {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}.
	 * 
	 * @return a logical attribute name
	 */
	public String getLogicalAttributeName() {
		return logicalAttributeName;
	}

	/**
	 * Assign the key to a physical attribute name which will be used
	 * to resolve Sakai user types.
	 * 
	 * @see #getLogicalAttributeName()
	 * 
	 * @param logicalAttributeName
	 */
	public void setLogicalAttributeName(String logicalAttributeName) {
		this.logicalAttributeName = logicalAttributeName;
	}

	/**
	 * Check the behavior when attribute values cannot be mapped
	 * to a Sakai user type. If <code>true</code>, 
	 * {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}
	 * will return the first attribute value encountered, otherwise will
	 * return the current value of {@link #getDefaultSakaiUserType()}.
	 * Defaults to <code>false</code>.
	 * 
	 * @return <code>true</code> if failed mappings should return
	 *   literal attribute values.
	 */
	public boolean isReturnLiteralAttributeValueIfNoMapping() {
		return returnLiteralAttributeValueIfNoMapping;
	}

	/**
	 * Assign the behavior for calculating Sakai user types when
	 * attribute values fail to map.
	 * 
	 * @see #isReturnLiteralAttributeValueIfNoMapping()
	 * @param returnLiteralAttributeValueIfNoMapping
	 */
	public void setReturnLiteralAttributeValueIfNoMapping(
			boolean returnLiteralAttributeValueIfNoMapping) {
		this.returnLiteralAttributeValueIfNoMapping = returnLiteralAttributeValueIfNoMapping;
	}
	
}
