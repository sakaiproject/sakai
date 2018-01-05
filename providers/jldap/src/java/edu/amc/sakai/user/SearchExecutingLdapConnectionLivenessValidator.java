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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * Tests {@link LDAPConnection} liveness by executing a search filter
 * and verifying that the result contains at least one entry. This
 * strategy is appropriate when {@link LDAPConnection#isConnectionAlive()}
 * is not reliable, e.g. when running against a non-OpenLDAP service 
 * provider.
 * 
 * <p>This particular implementation makes an effort to generate
 * filters which will be easily traceable in LDAP logs. Generally,
 * filters take the form 
 * <code>(|(objectclass=*)({attr}={unique-string}))</code>. The
 * search itself is limited by {@link LDAPConnection#SCOPE_BASE},
 * where "BASE" is specified by <code>baseDn</code>. Often,
 * <code>baseDn</code> corresponds to the DN of the "system user"
 * as whom {@link JLDAPDirectoryProvider} binds when running
 * in "autoBind" mode.</p>
 * 
 * @author dmccallum@unicon.net
 */
@Slf4j
public class SearchExecutingLdapConnectionLivenessValidator 
implements LdapConnectionLivenessValidator {

	public static final String DEFAULT_SEARCH_ATTRIBUTE_NAME = "uid";
	
	public static final LDAPSearchConstraints DEFAULT_LDAP_CONSTRAINTS = 
		new LDAPSearchConstraints();
	
	{
		
		DEFAULT_LDAP_CONSTRAINTS.setDereference(LDAPSearchConstraints.DEREF_ALWAYS);
		DEFAULT_LDAP_CONSTRAINTS.setTimeLimit(5000);
		DEFAULT_LDAP_CONSTRAINTS.setReferralFollowing(false);
		DEFAULT_LDAP_CONSTRAINTS.setBatchSize(0);
		
	}
	
	public static final String DEFAULT_HOST_NAME = "UNKNOWN_HOST";

	/**
	 * An ID for this instance
	 */
	private String searchStamp = 
		System.currentTimeMillis() + "-" + (int)(1e6*Math.random());
	
	/**
	 * The attribute against which the search unique ID will
	 * be tested.
	 */
	private String searchAttributeName = DEFAULT_SEARCH_ATTRIBUTE_NAME;

	/**
	 * The searchFilter string with a placeholder for a unique
	 * key for a particular execution. Treated as a {@link MessageFormat}
	 * pattern. Depends on {@link #searchAttributeName}
	 * having already been initialized to some meaningful value.
	 */
	private String searchFilter = newUnformattedSearchFilter();
	
	/**
	 * Cached copy of constraints common to all searches
	 */
	private LDAPSearchConstraints searchConstraints = DEFAULT_LDAP_CONSTRAINTS;
	
	/**
	 * The DN to be searched by {@link #searchFilter}. Searches
	 * are restricted to search this DN only.
	 */
	private String baseDn;

	private String hostName = DEFAULT_HOST_NAME;
	
	private ServerConfigurationService serverConfigService;
	
	/**
	 * Invoke prior to testing any connections. Caches a host
	 * name to include in search terms.
	 */
	public void init() {
		if (hostName.equals(DEFAULT_HOST_NAME)) {
			hostName = null; // defaults again at bottom
			if (hostName == null) {
				try {
					hostName = getLocalhostName();
				} catch (UnknownHostException e) {
					if (log.isDebugEnabled()) {
						log.debug("Unable to get local host name", e);
					}
				}
			}
			if (hostName == null && serverConfigService != null) {
				hostName = serverConfigService.getServerName();
			}
			if (hostName == null) {
				hostName = DEFAULT_HOST_NAME;
			}
		}
		if ( log.isDebugEnabled() ) {
			log.debug("init(): cached hostName [" + hostName + "]");
		}
	}

	/**
	 * Returns localhost's name as reported by
	 * {@link InetAddress#getLocalHost()#toString()}. Factored
	 * into a method to enable override during testing.
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	protected String getLocalhostName() throws UnknownHostException {
	    return InetAddress.getLocalHost().toString();
	}

	public boolean isConnectionAlive(LDAPConnection connectionToTest) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("isConnectionAlive(): testing connection liveness via search");
		}
		
        String formattedSearchFilter = formatSearchFilter();
        	
        try
        {
        	
        	if ( log.isDebugEnabled() ) {
    			log.debug("isConnectionAlive(): executing connection liveness search [base dn = " +
    					baseDn + "][filter = " + formattedSearchFilter + "][return attrib = " +
    					searchAttributeName + "]");
    		}
            
            LDAPSearchResults searchResults =
            	connectionToTest.search(baseDn,
                        LDAPConnection.SCOPE_BASE,
                        formattedSearchFilter,
                        new String[] {searchAttributeName},
                        false,
                        searchConstraints);

            if ( log.isDebugEnabled() ) {
    			log.debug("isConnectionAlive(): executed search [base dn = " +
    					baseDn + "][filter = " + formattedSearchFilter + "][return attrib = " +
    					searchAttributeName + "]");
    		}
            
            if ( searchResults.hasMore() ) {
            	if ( log.isDebugEnabled() ) {
        			log.debug("isConnectionAlive(): search contained results [base dn = " +
        					baseDn + "][filter = " + formattedSearchFilter + "][return attrib = " +
        					searchAttributeName + "]");
        		}
            	LDAPEntry entry = searchResults.next();
            	boolean isNonNullEntry = entry != null;
            	if ( log.isDebugEnabled() ) {
        			log.debug("isConnectionAlive(): search [base dn = " +
        					baseDn + "][filter = " + formattedSearchFilter + "][return attrib = " +
        					searchAttributeName + "] had results, returning [" + 
        					isNonNullEntry + "]");
        		}
            	return isNonNullEntry;
            } else {
            	if ( log.isDebugEnabled() ) {
        			log.debug("isConnectionAlive(): search had no results [base dn = " +
        					baseDn + "][filter = " + formattedSearchFilter + "][return attrib = " +
        					searchAttributeName + "], returning false");
        		}
            	return false;
            }
            
        }
        catch (LDAPException le)
        {
        	if ( log.isDebugEnabled() ) {
        		log.debug("isConnectionAlive(): liveness test failed [base dn = " +
        			baseDn + "][filter = " + formattedSearchFilter + "][return attrib = " +
        			searchAttributeName + "]", le);
        	}
        	return false;
        }
        
	}
	
	protected String newUnformattedSearchFilter() {
		return new StringBuilder("(|(objectclass=*)(")
		.append(searchAttributeName)
		.append("=validateProbe-")
		.append(searchStamp)
		.append("-{0}))").toString();
	}

	/**
	 * Generates an executable search filter string by injecting
	 * the result of {@link #generateUniqueSearchFilterTerm()} into
	 * the current <code>searchFilter</code>. This term is usually
	 * treated as a portion of the value to match against
	 * <code>searchAttributeName</code>
	 * 
	 * @return an LDAP search filter
	 */
	protected String formatSearchFilter() {
		Object uniqueSearchFilterTerm = generateUniqueSearchFilterTerm();
		return MessageFormat.format(searchFilter, uniqueSearchFilterTerm);
	}

	/**
	 * Generates a portion of the search filter which will (likely) uniquely
	 * identify an execution of that filter. By default concatenates a 
	 * semi-unique token ({@link #generateUniqueToken()} and the local host 
	 * name, separated by a dash ("-"). 
	 * 
	 * @see #setHostName(String)
	 * @see #getHostName()
	 * @see #generateUniqueToken()
	 * @see #setServerConfigService(ServerConfigurationService)
	 * @return
	 */
	protected Object generateUniqueSearchFilterTerm() {
		return generateUniqueToken() + "-" + hostName;
	}
	
	/**
	 * Just returns the current system time in millis. This
	 * is factored into a method so it can be overriden
	 * in testing (otherwise unique tokens are quite difficult
	 * to verify).
	 * 
	 * @see System#currentTimeMillis()
	 * @return
	 */
	protected String generateUniqueToken() {
		return Long.toString(System.currentTimeMillis());
	}

	public String getSearchStamp() {
		return searchStamp;
	}

	public void setSearchStamp(String searchStamp) {
		this.searchStamp = searchStamp;
	}

	public String getSearchAttributeName() {
		return searchAttributeName;
	}

	public void setSearchAttributeName(String searchAttributeName) {
		if ( searchAttributeName == null ) {
			this.searchAttributeName = DEFAULT_SEARCH_ATTRIBUTE_NAME;
		} else {
			this.searchAttributeName = searchAttributeName;
		}
		this.searchFilter = newUnformattedSearchFilter();
	}

	public String getBaseDn() {
		return baseDn;
	}

	public void setBaseDn(String baseDn) {
		this.baseDn = baseDn;
	}

	public LDAPSearchConstraints getSearchConstraints() {
		return searchConstraints;
	}

	public void setSearchConstraints(LDAPSearchConstraints searchConstraints) {
		this.searchConstraints = searchConstraints;
	}

	public String getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}

	public ServerConfigurationService getServerConfigService() {
		return serverConfigService;
	}

	public void setServerConfigService(
			ServerConfigurationService serverConfigService) {
		this.serverConfigService = serverConfigService;
	}

	public String getHostName() {
		return hostName;
	}

	/**
	 * Assign the host name to be appended to (semi) invocation-unique
	 * search terms. Falls back to {@link #DEFAULT_HOST_NAME} if
	 * argument is <code>null</code>. If this setter is not ivoked,
	 * {@link #init()} will control the default value.
	 * 
	 * @see #init()
	 * @param hostName
	 */
	public void setHostName(String hostName) {
		if ( hostName == null ) {
			this.hostName = DEFAULT_HOST_NAME;
		} else {
			this.hostName = hostName;
		}
	}
	
}
