/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
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

package edu.amc.sakai.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.UserEdit;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;

/**
 * Implements LDAP attribute mappings and filter generations using
 * an attribute map keyed by constants in 
 * {@link AttributeMappingConstants}. The strategy for calculating
 * Sakai user type can be injected as a {@link UserTypeMapper}.
 * This strategy defaults to {@link EmptyStringUserTypeMapper}, which 
 * will match &lt;= 2.3.0 OOTB behavior.
 * 
 * @author Dan McCallum, Unicon Inc
 *
 */
public class SimpleLdapAttributeMapper implements LdapAttributeMapper {
	
	/** Class-specific logger */
	private static Log M_log = LogFactory.getLog(SimpleLdapAttributeMapper.class);
	
	/**
	 * User entry attribute mappings. Keys are logical attr names,
	 * values are physical attr names.
	 */
	private Map<String,String> attributeMappings;
    
    /**
     * Keys are physical attr names, values are collections of
     * logical attr names. Essentially an inverse copy of
     * {@link #attributeMappings}.
     */
    private Map<String,Collection<String>> reverseAttributeMappings;
	
	/** strategy for calculating the Sakai user type given a
	 * <code>LDAPEntry</code>
	 */
	private UserTypeMapper userTypeMapper;
	
	/** copy of {@link #attributeMappings<code>}.values()</code> */
	private String[] physicalAttrNames;
	
	/** 
	 * Completes configuration of this instance.
	 * 
	 * <p>Initializes internal mappings to a copy of 
	 * {@link AttributeMappingConstants#DEFAULT_ATTR_MAPPINGS} if 
	 * the current map is empty. Initializes user 
	 * type mapping strategy to a 
	 * {@link EmptyStringUserTypeMapper} if no strategy
	 * has been specified.
	 * </p>
	 * 
	 * <p>This defaulting enables UDP config 
	 * forward-compatibility.</p>
	 * 
	 */
	public void init() {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("init()");
		}
		
		if ( attributeMappings == null || attributeMappings.isEmpty() ) {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("init(): creating default attribute mappings");
			}
			setAttributeMappings(AttributeMappingConstants.DEFAULT_ATTR_MAPPINGS);
		}
		 
		if ( userTypeMapper == null ) {
			userTypeMapper = new EmptyStringUserTypeMapper();
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("init(): created default user type mapper [mapper = " + 
						userTypeMapper + "]");
			}
		}
				
	}
	
	/**
	 * Builds a filter of the form &lt;email-attr&gt;=&lt;<code>emailAddr</code>&gt;
	 */
	public String getFindUserByEmailFilter(String emailAddr) {
		
		String emailAttr = 
			attributeMappings.get(AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY);
		return emailAttr + "=" + escapeSearchFilterTerm(emailAddr);
		
	}

	/**
	 * Builds a filter of the form &lt;login-attr&gt;=&lt;<code>eid</code>&gt;
	 */
	public String getFindUserByEidFilter(String eid) {
		
		String eidAttr = 
			attributeMappings.get(AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY);
		return eidAttr + "=" + escapeSearchFilterTerm(eid);
		
	}

	/**
	 * Performs {@link LDAPEntry}-to-{@Link LdapUserData} attribute
     * mappings. Assigns the given {@link LDAPEntry}'s DN to the
     * {@link LdapUserData} as a property keyed by 
     * {@link AttributeMappingConstants#USER_DN_PROPERTY}. Then iterates
     * over {@link LDAPEntry#getAttributeSet()}, handing each attribute
     * to {@link #mapLdapAttributeOntoUserData(LDAPAttribute, LdapUserData, Collection)}.  
     * Finally, assigns a "type" to the {@link LdapUserData} as defined
     * by {@link #mapLdapEntryToSakaiUserType(LDAPEntry)}.
	 * 
     * @see UserTypeMapper
	 * @param ldapEntry the user's directory entry
	 * @param the target {@link LdapUserData}
	 */
	public void mapLdapEntryOntoUserData(LDAPEntry ldapEntry, LdapUserData userData) {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("mapLdapEntryOntoUserData(): mapping entry [dn = " + 
					ldapEntry.getDN() + "]");
		}
        
		setUserDataDn(ldapEntry, userData);
        
        Set<LDAPAttribute> ldapAttributeSet = ldapEntry.getAttributeSet();
        for (LDAPAttribute ldapAttribute : ldapAttributeSet) {
            // we do the reverse lookup here since it will always need to
            // be performed and we want to ensure it only happens once
            // per attribute, regardless of the complexity of the actual
            // mapping onto the user object
            Collection<String> logicalAttrNames = 
                getReverseAttributeMappings(ldapAttribute.getName());
            mapLdapAttributeOntoUserData(ldapAttribute, userData, logicalAttrNames);
        }
        
        // calculating a user's "type" potentially involves calculations
        // against the entire LDAPEntry
        userData.setType(mapLdapEntryToSakaiUserType(ldapEntry));
		
	}
	
	public String getUserBindDn(LdapUserData userData) {
		return getUserDataDn(userData);
	}
	
	protected String getUserDataDn(LdapUserData userData) {
		return userData.getProperties().getProperty(AttributeMappingConstants.USER_DN_PROPERTY);
	}
	
	protected void setUserDataDn(LDAPEntry entry, LdapUserData targetUserData) {
		targetUserData.setProperty(
                AttributeMappingConstants.USER_DN_PROPERTY, 
                entry.getDN());
	}
    
    /**
     * Map the given {@link LDAPAttribute} onto the given 
     * {@link LdapUserData}. Client can specify the logical attribute
     * name(s) which have been configured for the given {@link LDAPAttribute}.
     * This implementation has specific handling for the following
     * logical attribute names:
     * 
     * <ul>
     *   <li>{@link AttributeMappingConstants#LOGIN_ATTR_MAPPING_KEY} - {@link LdapUserData#setEid(String)}</li>
     *   <li>{@link AttributeMappingConstants#FIRST_NAME_ATTR_MAPPING_KEY} - {@link LdapUserData#setFirstName(String)}</li>
     *   <li>{@link AttributeMappingConstants#LAST_NAME_ATTR_MAPPING_KEY} - {@link LdapUserData#setLastName(String)}</li>
     *   <li>{@link AttributeMappingConstants#EMAIL_ATTR_MAPPING_KEY} - {@link LdapUserData#setEmail(String)}</li>
     * </ul>
     * 
     * Any other logical attribute names passed in <code>logicalAttrNames</code>
     * will be mapped onto <code>userData</code> as a property using
     * the logical attribute name as a key.
     * 
     * @param attribute the {@link LDAPAttribute} to map
     * @param userData the target {@link LdapUserData} instance
     * @param logicalAttrNames logical name(s) of the <code>attribute</code>. May
     *   be null or empty, indicating no configured logical name(s).
     */
    protected void mapLdapAttributeOntoUserData(LDAPAttribute attribute, 
            LdapUserData userData, Collection<String> logicalAttrNames) {
        
        if ( logicalAttrNames == null || logicalAttrNames.isEmpty() ) {
            if ( M_log.isDebugEnabled() ) {
                M_log.debug("No logical name for attribute. [physical name = " + 
                        attribute.getName() + "]");
            }
            return;
        }
        
        for ( String logicalAttrName : logicalAttrNames ) {
            
            mapLdapAttributeOntoUserData(attribute, userData, logicalAttrName);
            
        }
        
    }
    
    /**
     * A delegate of {@link #mapLdapAttributeOntoUserData(LDAPAttribute, LdapUserData, Collection)}
     * that allows for discrete handling of each logical attribute name associated with
     * the given {@link LDAPAttribute}
     * 
     * @param attribute
     * @param userData
     * @param logicalAttrName
     */
    protected void mapLdapAttributeOntoUserData(LDAPAttribute attribute, 
            LdapUserData userData, String logicalAttrName) {
        
        String attrValue = attribute.getStringValue();
        
        if ( M_log.isDebugEnabled() ) {
        	M_log.debug("mapLdapAttributeOntoUserData() preparing to map: [logical attr name = " + logicalAttrName + 
        			"][physical attr name = " + attribute.getName() + "][value = " + attrValue + "]");
        }
        
        if ( logicalAttrName.equals(AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY) ) {
        	if ( M_log.isDebugEnabled() ) {
        		M_log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.eid: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setEid(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.FIRST_NAME_ATTR_MAPPING_KEY) ) {
        	if ( M_log.isDebugEnabled() ) {
        		M_log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.firstName: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setFirstName(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.LAST_NAME_ATTR_MAPPING_KEY) ) {
        	if ( M_log.isDebugEnabled() ) {
        		M_log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.lastName: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setLastName(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY) ) {
        	if ( M_log.isDebugEnabled() ) {
        		M_log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.email: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setEmail(attrValue);
        } else {
        	if ( M_log.isDebugEnabled() ) {
        		M_log.debug("mapLdapAttributeOntoUserData() mapping attribute to a User property: " +
        				"[logical attr name (and property name) = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setProperty(logicalAttrName, attrValue);
        }
        
    }

	
	/**
	 * Passes the given <code>LDAPEntry</code> and a reference to this
	 * <code>SimpleLdapAttributeMapper</code> to
	 * {@link UserTypeMapper#mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}.
	 * By default, this will just return an empty String.
	 * 
	 * @param ldapEntry the <code>LDAPEntry</code> to map
	 * @return a String representing a Sakai user type. <code>null</code>s and
	 *   empty Strings are possible.
	 */
	protected String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry) {
		return userTypeMapper.mapLdapEntryToSakaiUserType(ldapEntry, this);
	}
	
	/**
	 * Straightforward {@link LdapUserData} to 
	 * {@link org.sakaiproject.user.api.UserEdit} field-to-field mapping, including
	 * properties.
	 */
	public void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit) {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("mapUserDataOntoUserEdit(): [cache record = " + 
					userData + "]");
		}
		
		userEdit.setEid(userData.getEid());
		userEdit.setFirstName(userData.getFirstName());
		userEdit.setLastName(userData.getLastName());
		userEdit.setEmail(userData.getEmail());
		userEdit.setType(userData.getType());
		Properties srcProps = userData.getProperties();
		ResourceProperties tgtProps = userEdit.getProperties();
		for ( Entry srcProp : srcProps.entrySet() ) {
			tgtProps.addProperty((String)srcProp.getKey(), 
					(String)srcProp.getValue());
		}
		
	}
    
    public String escapeSearchFilterTerm(String term) {
        if (term == null) return null;
        //From RFC 2254
        String escapedStr = new String(term);
        escapedStr = escapedStr.replaceAll("\\\\","\\\\5c");
        escapedStr = escapedStr.replaceAll("\\*","\\\\2a");
        escapedStr = escapedStr.replaceAll("\\(","\\\\28");
        escapedStr = escapedStr.replaceAll("\\)","\\\\29");
        escapedStr = escapedStr.replaceAll("\\"+Character.toString('\u0000'), "\\\\00");
        return escapedStr;
    }
    
    /**
     * Map the given logical attribute name to a physical attribute name.
     * 
     * @param key the logical attribute name
     * @return the corresponding physical attribute name, or null
     *   if no mapping exists.
     */
    public String getAttributeMapping(String key) {
        return attributeMappings.get(key);
    }
    
    /**
     * Access the configured logical names associated with the given
     * physical attribute name. May return <code>null</code>.
     * 
     * @param physicalAttrName a physical LDAP attribute name to reverse
     *   map to zero or more logical attribute names
     * @return a collection of logical attribute names; may be <code>null</code>
     *   or empty.
     */
    public Collection<String> getReverseAttributeMappings(String physicalAttrName) {
        return reverseAttributeMappings.get(physicalAttrName);
    }
    
    protected Map<String, Collection<String>> getReverseAttributeMap() {
    	return this.reverseAttributeMappings;
    }
	
	/**
	 * Implemented to return the current values of 
	 * {link {@link #getAttributeMappings().values()} as
	 * a String array.
	 */
	public String[] getSearchResultAttributes() {
		return physicalAttrNames;
	}
	
	/**
	 * Returns a direct reference to the currently
	 * cached mappings. Note that if this map is
	 * modified, the next call to 
	 * {@link #getSearchResultAttributes()} may
	 * return stale values.
	 */
	public Map<String,String> getAttributeMappings()
	{
		return attributeMappings;
	}

	/**
	 * Caches the given Map reference and takes a 
	 * snapshot of the values therein for future
	 * use by {@link #getSearchResultAttributes()}.
	 * 
	 * @see #getAttributeMappings()
	 */
	public void setAttributeMappings(Map<String,String> attributeMappings)
	{
		if ( attributeMappings == null || attributeMappings.isEmpty() ) {
			this.attributeMappings = AttributeMappingConstants.DEFAULT_ATTR_MAPPINGS;
		} else {
			this.attributeMappings = attributeMappings;
		}
        cachePhysicalAttributeNames();
        cacheReverseAttributeLookupMap();
        
        if (M_log.isDebugEnabled()) {
        	M_log.debug("setAttributeMappings(): [attrib map = " + this.attributeMappings + "]");
        	M_log.debug("setAttributeMappings(): [reverse attrib map = " + this.reverseAttributeMappings + "]");
        	M_log.debug("setAttributeMappings(): [cached phys attrb names = " + Arrays.toString(this.physicalAttrNames) + "]");
        }
        
	}
	
	/**
	 * Converts the current attribute map's values into
	 * a String array.
	 */
	private void cachePhysicalAttributeNames() {
		if ( attributeMappings == null ) {
			physicalAttrNames = new String[0];
			return;
		}
		physicalAttrNames = new String[attributeMappings.size()];
		int k = 0;
		for ( String name : attributeMappings.values() ) {
			physicalAttrNames[k++] = name;
		}
	}
    
    /**
     * Caches the result of {@link #reverseAttributeMap(Map)}, passing
     * the currently cached attribute map. The result completely
     * replaces any currently cached reverse attribute map.
     *
     */        
    private void cacheReverseAttributeLookupMap() {
        reverseAttributeMappings = reverseAttributeMap(attributeMappings);    
    }
    
    /**
     * Creates a reverse lookup map of a given attribute map's values.
     * That is, creates a map of physical to logical LDAP attribute names.
     * Since a multiple logical names may point to a single physical name,
     * values in this map are actually {@link Collection<String>}'s.
     * 
     * <p>Protected access control mainly to enable testing</p>
     * 
     * @param toReverse
     * @return
     */
    protected Map<String,Collection<String>> reverseAttributeMap(Map<String,String> toReverse) {
    	Map<String,Collection<String>> reversed = new HashMap<String,Collection<String>>();
    	for ( Map.Entry<String, String> entry : toReverse.entrySet()) {
            Collection<String> logicalAttrNames = 
            	reversed.get(entry.getValue());
            String logicalAttrName = entry.getKey();
            String physicalAttrName = entry.getValue();
            if (logicalAttrNames == null) {
                logicalAttrNames = new ArrayList<String>(1);
                logicalAttrNames.add(logicalAttrName);
                reversed.put(physicalAttrName, logicalAttrNames);
            } else {
                logicalAttrNames.add(logicalAttrName);
            }
        }
    	return reversed;
    }

	/** 
	 * Access the strategy for calculating the Sakai user type given a
	 * <code>LDAPEntry</code>
	 */
	public UserTypeMapper getUserTypeMapper() {
		return userTypeMapper;
	}

	/** Assign the strategy for calculating the Sakai user type given a
	 * <code>LDAPEntry</code>
	 */
	public void setUserTypeMapper(UserTypeMapper userTypeMapper) {
		this.userTypeMapper = userTypeMapper;
	}

}
