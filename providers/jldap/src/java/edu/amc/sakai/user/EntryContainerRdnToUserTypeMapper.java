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
import java.util.Vector;
import java.util.Iterator;

import com.novell.ldap.LDAPEntry;
import com.novell.ldap.util.DN;
import com.novell.ldap.util.RDN;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps from a user entry's container's most-local RDN to a Sakai user type.
 * 
 * @author Dan McCallum, Unicon Inc
 *
 */
@Slf4j
public class EntryContainerRdnToUserTypeMapper implements UserTypeMapper {

	/** map of container RDN values to Sakai user types */
	private Map<String,String> rdnToSakaiUserTypeMap = new HashMap<String,String>();
	
	/** if <code>true</code>, will echo the container's
	 * most local RDN as its Sakai type if no mapping is 
	 * found. otherwise will return <code>null</code>
	 */
	private boolean returnLiteralRdnValueIfNoMapping;

	/** if <code>true</code>, will recurse through all
	 * RDN values to find a valid match.
	 * otherwise will return <code>null</code>
	 */
	private boolean recurseRdnIfNoMapping;
	
	/**
	 * Returns the user type associated with a matching
	 * RDN encountered when iterating through the specified 
	 * <code>LDAPEntry</code>'s containing <code>DN</code>'s 
	 * <code>RDN<code>s. If recurseRdnIfNoMapping is false, 
	 * the most local RDN will be used for matching. 
	 * {@link #mapRdn(String, String))} implements the actual 
	 * value mapping.
	 * 
	 * @param ldapEntry the user's <code>LDAPEntry</code>
	 * @param mapper a source of mapping configuration
	 * @return the entry's mapped RDN
	 */
	public String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry,
			LdapAttributeMapper mapper) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("mapLdapEntryToSakaiUserType(): [entry DN = " + 
					ldapEntry.getDN() + "]");
		}
		
		String dnString = ldapEntry.getDN();
		DN dn = new DN(dnString);
		DN containerDN = dn.getParent();
		Vector<RDN> containerRDNs = containerDN.getRDNs();
		Iterator<RDN> containerRDNsIterator = containerRDNs.iterator();
		RDN rdn = containerRDNsIterator.next();
		String mappedValue = mapRdn(rdn.getType(),rdn.getValue());

		if(mappedValue == null && recurseRdnIfNoMapping) {
				while ( containerRDNsIterator.hasNext() ) {
						rdn = containerRDNsIterator.next();
						mappedValue = mapRdn(rdn.getType(),rdn.getValue());
						if ( mappedValue != null) {
								return mappedValue;
						}
				}
		}
		return mappedValue;
	}
	
	/**
	 * Applies the current mapping configuration to the 
	 * recieved RDN value. If no mapping exists, will
	 * return null unless the 
	 * <code>returnLiteralRdnValueIfNoMapping</code> flag
	 * is raised, in which case the RDN value itself
	 * will be returned.
	 * 
	 * @param rdnType the RDN's type, primarily for debugging
	 *   purposes
	 * @param rdnValue the RDN value to map
	 * @return the mapped rdnValue
	 */
	protected String mapRdn(String rdnType, String rdnValue) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("mapRdn(): mapping [rdn type = " + 
					rdnType + "][rdn value = " +
					rdnValue + "]");
		}
		
		if ( rdnToSakaiUserTypeMap == null || rdnToSakaiUserTypeMap.isEmpty() ) {
			
			String mappedValue = returnLiteralRdnValueIfNoMapping ? rdnValue : null;
			if ( log.isDebugEnabled() ) {
				log.debug("mapRdn(): no mappings assigned [rdn type = " + 
					rdnType + "][rdn value = " + rdnValue + 
					"][returning = " + mappedValue + "]");
			}
			return mappedValue;
			
		}
		
		String mappedValue = rdnToSakaiUserTypeMap.get(rdnValue);
		if ( mappedValue == null ) {
			mappedValue = returnLiteralRdnValueIfNoMapping ? rdnValue : null;
			if ( log.isDebugEnabled() ) {
				log.debug("mapRdn(): no valid mapping [rdn type = " + 
					rdnType + "][rdn value = " + rdnValue + 
					"][returning = " + mappedValue + "]");
			}
		}
		return mappedValue;
		
	}

	/**
	 * {@see #mapRdn(String,String)}
	 * 
	 * @return the current RDN-to-Sakai user type map
	 */
	public Map<String, String> getRdnToSakaiUserTypeMap() {
		return rdnToSakaiUserTypeMap;
	}

	/**
	 * {@see mapRdn(String,String)}
	 * 
	 * @param rdnToSakaiUserTypeMap the RDN-to-Sakai
	 *   user type map
	 */
	public void setRdnToSakaiUserTypeMap(Map<String, String> rdnToSakaiUserTypeMap) {
		this.rdnToSakaiUserTypeMap = rdnToSakaiUserTypeMap;
	}

	/**
	 * {@see mapRdn(String,String)}
	 */
	public boolean isReturnLiteralRdnValueIfNoMapping() {
		return returnLiteralRdnValueIfNoMapping;
	}

	/**
	 * {@see mapRdn(String,String)}
	 */
	public void setReturnLiteralRdnIfNoMapping(boolean returnLiteralRdnValueIfNoMapping) {
		this.returnLiteralRdnValueIfNoMapping = returnLiteralRdnValueIfNoMapping;
	}

        /**
         * {@see mapRdn(String,String)}
         */
        public boolean isRecurseRdnIfNoMapping() {
                return recurseRdnIfNoMapping;
        }

        /**
         * {@see mapRdn(String,String)}
         */
        public void setRecurseRdnIfNoMapping(boolean recurseRdnIfNoMapping) {
                this.recurseRdnIfNoMapping = recurseRdnIfNoMapping;
        }

}
