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

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.UserEdit;

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
@Slf4j
public class SimpleLdapAttributeMapper implements LdapAttributeMapper {

	/**
	 * User entry attribute mappings. Keys are logical attr names,
	 * values are physical attr names.
	 */
	private Map<String,String> attributeMappings;
    
    /**
	 * Formatters used for manipulating attribute values sent to and returned from LDAP.
	 */
	private Map<String,MessageFormat> valueMappings;

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
		
		if ( log.isDebugEnabled() ) {
			log.debug("init()");
		}
		
		if ( attributeMappings == null || attributeMappings.isEmpty() ) {
			if ( log.isDebugEnabled() ) {
				log.debug("init(): creating default attribute mappings");
			}
			setAttributeMappings(AttributeMappingConstants.DEFAULT_ATTR_MAPPINGS);
		}
		 
		if ( userTypeMapper == null ) {
			userTypeMapper = new EmptyStringUserTypeMapper();
			if ( log.isDebugEnabled() ) {
				log.debug("init(): created default user type mapper [mapper = " + 
						userTypeMapper + "]");
			}
		}
		if ( valueMappings == null ) {
			valueMappings = Collections.EMPTY_MAP;
			if ( log.isDebugEnabled() ) {
				log.debug("init(): created default value mapper [mapper = " +
						valueMappings + "]");
			}
		} else {
			// Check we have good value mappings and throw any out that aren't (warning user).
			Iterator<Entry<String, MessageFormat>> iterator = valueMappings.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, MessageFormat> entry = iterator.next();
				if (entry.getValue().getFormats().length != 1) {
					iterator.remove();
					log.warn(String.format("Removed value mapping as it didn't have one format: %s -> %s",
							entry.getKey(), entry.getValue().toPattern()));
				}
			}
		}
	}
	
	/**
	 * Builds a filter of the form &lt;email-attr&gt;=&lt;<code>emailAddr</code>&gt;
	 */
	public String getFindUserByEmailFilter(String emailAddr) {
		
		String emailAttr = 
			attributeMappings.get(AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY);
		MessageFormat valueFormat = valueMappings.get(AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY);
		if (valueFormat == null) {
			return emailAttr + "=" + escapeSearchFilterTerm(emailAddr);
		} else {
			valueFormat = (MessageFormat) valueFormat.clone();
			return emailAttr + "=" + escapeSearchFilterTerm(valueFormat.format(new Object[]{emailAddr}));
		}
	}

	/**
	 * Builds a filter of the form &lt;login-attr&gt;=&lt;<code>eid</code>&gt;
	 */
	public String getFindUserByEidFilter(String eid) {
		
		String eidAttr = 
			attributeMappings.get(AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY);
		MessageFormat valueFormat = valueMappings.get(AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY);
		if (valueFormat == null) {
			return eidAttr + "=" + escapeSearchFilterTerm(eid);
		} else {
			valueFormat = (MessageFormat) valueFormat.clone();
			return eidAttr + "=" + escapeSearchFilterTerm(valueFormat.format(new Object[]{eid}));
		}
	}

	public String getFindUserByAidFilter(String aid) {
		String eidAttr = 
			attributeMappings.get(AttributeMappingConstants.AUTHENTICATION_ATTR_MAPPING_KEY);
		return eidAttr + "=" + escapeSearchFilterTerm(aid);
	}

	/**
	 * Performs {@link LDAPEntry}-to-{@Link LdapUserData} attribute
     * mappings. Assigns the given {@link LDAPEntry}'s DN to the
     * {@link LdapUserData} as a property keyed by 
     * {@link AttributeMappingConstants#USER_DN_PROPERTY}. Then iterates
     * over {@link LDAPEntry#getAttributeSet()}, handing each attribute
     * to {@link #mapLdapAttributeOntoUserData(LDAPAttribute, LdapUserData, Collection)}.  
     * Then enforces the preferred first name field, if it exists.
     * Finally, assigns a "type" to the {@link LdapUserData} as defined
     * by {@link #mapLdapEntryToSakaiUserType(LDAPEntry)}.
	 * 
     * @see UserTypeMapper
	 * @param ldapEntry the user's directory entry
	 * @param userData target {@link LdapUserData}
	 */
	public void mapLdapEntryOntoUserData(LDAPEntry ldapEntry, LdapUserData userData) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("mapLdapEntryOntoUserData(): mapping entry [dn = " + 
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
        
        //enforce use of firstNamePreferred if its set
        userData.setFirstName(usePreferredFirstName(userData));
        
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
            if ( log.isDebugEnabled() ) {
                log.debug("No logical name for attribute. [physical name = " + 
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
        MessageFormat format = valueMappings.get(logicalAttrName);
        if (format != null && attrValue != null) {
            format = (MessageFormat)format.clone();
            if ( log.isDebugEnabled() ) {
                log.debug("mapLdapAttributeOntoUserData(): value mapper [attrValue = " +
                        attrValue + "; format=" + format.toString() + "]");
            }
            attrValue = (String)(format.parse(attrValue, new ParsePosition(0))[0]);
        }
        
        if ( log.isDebugEnabled() ) {
        	log.debug("mapLdapAttributeOntoUserData() preparing to map: [logical attr name = " + logicalAttrName + 
        			"][physical attr name = " + attribute.getName() + "][value = " + attrValue + "]");
        }
        
        if ( logicalAttrName.equals(AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY) ) {
        	if ( log.isDebugEnabled() ) {
        		log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.eid: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setEid(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.FIRST_NAME_ATTR_MAPPING_KEY) ) {
        	if ( log.isDebugEnabled() ) {
        		log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.firstName: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setFirstName(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.PREFERRED_FIRST_NAME_ATTR_MAPPING_KEY) ) {
        	if ( log.isDebugEnabled() ) {
            	log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.firstNamePreferred: " +
            			"[logical attr name = " + logicalAttrName + 
            			"][physical attr name = " + attribute.getName() + 
            			"][value = " + attrValue + "]");
            }
        	userData.setPreferredFirstName(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.LAST_NAME_ATTR_MAPPING_KEY) ) {
        	if ( log.isDebugEnabled() ) {
        		log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.lastName: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setLastName(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY) ) {
        	if ( log.isDebugEnabled() ) {
        		log.debug("mapLdapAttributeOntoUserData() mapping attribute to User.email: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setEmail(attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.DISPLAY_ID_ATTR_MAPPING_KEY) ) {
        	if ( log.isDebugEnabled() ) {
        		log.debug("mapLdapAttributeOntoUserData() mapping attribute to User display Id: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
            userData.setProperty(JLDAPDirectoryProvider.DISPLAY_ID_PROPERTY, attrValue);
        } else if ( logicalAttrName.equals(AttributeMappingConstants.DISPLAY_NAME_ATTR_MAPPING_KEY) ) {
        	if ( log.isDebugEnabled() ) {
        		log.debug("mapLdapAttributeOntoUserData() mapping attribute to User display name: " +
        				"[logical attr name = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
        	userData.setProperty(JLDAPDirectoryProvider.DISPLAY_NAME_PROPERTY, attrValue);
        } else {
        	if ( log.isDebugEnabled() ) {
        		log.debug("mapLdapAttributeOntoUserData() mapping attribute to a User property: " +
        				"[logical attr name (and property name) = " + logicalAttrName + 
        				"][physical attr name = " + attribute.getName() + 
        				"][value = " + attrValue + "]");
        	}
        	// Support multivalue attributes.
        	String[] attrValues = attribute.getStringValueArray();
        	if (attrValues.length > 1) {
        		List<String> newList = Arrays.asList(attrValues);
        		userData.getProperties().put(logicalAttrName, newList);
        	} else {
        		userData.setProperty(logicalAttrName, attrValue);
        	}
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
		
		if ( log.isDebugEnabled() ) {
			log.debug("mapUserDataOntoUserEdit(): [userData = " + userData + "]");
		}
		
		userEdit.setEid(userData.getEid());
		userEdit.setFirstName(userData.getFirstName());
		userEdit.setLastName(userData.getLastName());
		userEdit.setEmail(userData.getEmail());
		userEdit.setType(userData.getType());
		Properties srcProps = userData.getProperties();
		ResourceProperties tgtProps = userEdit.getProperties();
		for ( Entry srcProp : srcProps.entrySet() ) {
			if (srcProp.getValue() instanceof String) {
				tgtProps.addProperty((String)srcProp.getKey(), 
						(String)srcProp.getValue());
			} else if (srcProp.getValue() instanceof List) {
				for(String value: (List<String>)srcProp.getValue()) {
					tgtProps.addPropertyToList((String) srcProp.getKey(), value);
				}
			}
		}
		
	}
    
    public String escapeSearchFilterTerm(String term) {
        if (term == null) return null;
        //From RFC 2254
        term = term.replaceAll("\\\\","\\\\5c");
        term = term.replaceAll("\\*","\\\\2a");
        term = term.replaceAll("\\(","\\\\28");
        term = term.replaceAll("\\)","\\\\29");
        term = term.replaceAll("\\"+Character.toString('\u0000'), "\\\\00");
        return term;
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
        
        if (log.isDebugEnabled()) {
        	log.debug("setAttributeMappings(): [attrib map = " + this.attributeMappings + "]");
        	log.debug("setAttributeMappings(): [reverse attrib map = " + this.reverseAttributeMappings + "]");
        	log.debug("setAttributeMappings(): [cached phys attrb names = " + Arrays.toString(this.physicalAttrNames) + "]");
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

		// filter out any duplicate values so we don't request them twice
		physicalAttrNames = attributeMappings.values().stream().distinct().toArray(String[]::new);
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
	
	/**
	 * Determines if a user has a preferredFirstName set and if so, returns it for use.
	 * Otherwise, returns their firstName as normal.
	 * 
	 * @param userData the <code>LdapUserData</code> for the user
	 * @return a String of the user's first name.
	 */
	protected String usePreferredFirstName(LdapUserData userData) {
		if(StringUtils.isNotBlank(userData.getPreferredFirstName())) {
			 if (log.isDebugEnabled()) {
				 log.debug("usePreferredFirstName() using firstNamePreferred.");
			 }
			return userData.getPreferredFirstName();
		} else {
			 if (log.isDebugEnabled()) {
				 log.debug("usePreferredFirstName() using firstName.");
			 }
			return userData.getFirstName();
		}
	}
	

	/**
	 * @inheritDoc
	 */
	public String getFindUserByCrossAttributeSearchFilter(String criteria) {
		String eidAttr = attributeMappings.get(AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY);
		String emailAttr = attributeMappings.get(AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY);
		String givenNameAttr = attributeMappings.get(AttributeMappingConstants.FIRST_NAME_ATTR_MAPPING_KEY);
		String lastNameAttr = attributeMappings.get(AttributeMappingConstants.LAST_NAME_ATTR_MAPPING_KEY);
		
		//This explicitly constructs the filter with wildcards in it.
		//However, we escape the given criteria to prevent any other injection
		criteria = escapeSearchFilterTerm(criteria);
		
		//(|(uid=criteria*)(mail=criteria*)(givenName=criteria*)(sn=criteria*))
		StringBuilder sb = new StringBuilder();
			sb.append("(|");
			
			sb.append("(");
			sb.append(eidAttr);
			sb.append("=");
			sb.append(criteria);
			sb.append("*)");
			
			sb.append("(");
			sb.append(emailAttr);
			sb.append("=");
			sb.append(criteria);
			sb.append("*)");
			
			sb.append("(");
			sb.append(givenNameAttr);
			sb.append("=");
			sb.append(criteria);
			sb.append("*)");
			
			sb.append("(");
			sb.append(lastNameAttr);
			sb.append("=");
			sb.append(criteria);
			sb.append("*)");
			
			sb.append(")");
		
		return sb.toString();
	}

	/**
	 * @inheritDoc
	 */
	public String getManyUsersInOneSearch(Set<String> criteria) {
		StringBuilder sb = new StringBuilder();
		sb.append("(|");

		for ( Iterator<String> eidIterator = criteria.iterator(); eidIterator.hasNext(); ) {
			sb.append("(");
			sb.append(getFindUserByEidFilter(eidIterator.next()));
			sb.append(")");
		}
		
		sb.append(")");
		
		if (log.isDebugEnabled()) {
			log.debug("getManyUsersInOneSearch() completed filter: " + sb.toString());
		}
		
		return sb.toString();
	}

	/**
	 * @return A Map of message formats used for extracting values from LDAP data.
	 */
	public Map<String, MessageFormat> getValueMappings() {
		return valueMappings;
	}

	/**
	 * @param valueMappings A Map of message formats used for extracting values from LDAP data.
	 */
	public void setValueMappings(Map<String, MessageFormat> valueMappings) {
		this.valueMappings = valueMappings;
	}

}
