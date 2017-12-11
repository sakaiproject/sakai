/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.user.api.*;

/**
 * <p>
 * An implementation of a Sakai UserDirectoryProvider that authenticates/retrieves 
 * users from a LDAP directory.
 * </p>
 * 
 * @author Dan McCallum, Unicon Inc
 * @author David Ross, Albany Medical College
 * @author Rishi Pande, Virginia Tech
 */
@Slf4j
public class JLDAPDirectoryProvider implements UserDirectoryProvider, LdapConnectionManagerConfig, ExternalUserSearchUDP, UsersShareEmailUDP, DisplayAdvisorUDP, AuthenticationIdUDP
{
	/** Default LDAP connection port */
	public static final int DEFAULT_LDAP_PORT = 389;

	/** Default secure/unsecure LDAP connection creation behavior */
	public static final boolean DEFAULT_IS_SECURE_CONNECTION = false;

	/**  Default LDAP access timeout in milliseconds */
	public static final int DEFAULT_OPERATION_TIMEOUT_MILLIS = 5000;

	/** Default referral following behavior */
	public static final boolean DEFAULT_IS_FOLLOW_REFERRALS = false;
	
	public static final boolean DEFAULT_IS_SEARCH_ALIASES = false;

	/** Default search scope for filters executed by 
	 * {@link #searchDirectory(String, LDAPConnection, LdapEntryMapper, String[], String, int)}
	 */
	public static final int DEFAULT_SEARCH_SCOPE = LDAPConnection.SCOPE_SUB;

	/** Default LDAP use of connection pooling */
	public static final boolean DEFAULT_POOLING = false;

	/** Default LDAP maximum number of connections in the pool */
	public static final int DEFAULT_POOL_MAX_CONNS = 10;
	
	/** Default LDAP maximum number of objects in a result */
	public static final int DEFAULT_MAX_RESULT_SIZE = 1000;

	/** Default LDAP maximum number of objects to query for */
	public static final int DEFAULT_BATCH_SIZE = 200;
	
	/** Property of the user object to store the display ID under */
	public static final String DISPLAY_ID_PROPERTY = JLDAPDirectoryProvider.class+"-displayId";

	/** Property of the user object to store the display Name under */
	public static final String DISPLAY_NAME_PROPERTY = JLDAPDirectoryProvider.class+"-displayName";

	public static final boolean DEFAULT_ALLOW_AUTHENTICATION = true;
	
	public static final boolean DEFAULT_AUTHENTICATE_WITH_PROVIDER_FIRST = false;

	/** LDAP host address */
	private String ldapHost;

	/** LDAP connection port. Defaults to {@link #DEFAULT_LDAP_PORT} */
	private int ldapPort = DEFAULT_LDAP_PORT;

	/** SSL keystore location */
	private String keystoreLocation;

	/** SSL keystore password */
	private String keystorePassword;

	/** DN for LDAP manager user */
	private String ldapUser;

	/** Password for LDAP manager user */
	private String ldapPassword;

	/** Should connection allocation include a bind attempt */
	private boolean autoBind;

	/** Base DN for user lookups */
	private String basePath;

	/** Toggle SSL connections. Defaults to {@link #DEFAULT_IS_SECURE_CONNECTION} */
	private boolean secureConnection = DEFAULT_IS_SECURE_CONNECTION;

	/** Should connection pooling be used? */
	private boolean pooling = DEFAULT_POOLING;

	/** Maximum number of physical connections in the pool */
	private int poolMaxConns = DEFAULT_POOL_MAX_CONNS;
	
	/** Maximum number of results from one LDAP query */
	private int maxResultSize = DEFAULT_MAX_RESULT_SIZE;

	/** The size of each batch to load from LDAP when loading multiple users. */
	private int batchSize = DEFAULT_BATCH_SIZE;

	/** Socket factory for secure connections. Only relevant if
	 * {@link #secureConnection} is true. Defaults to a new instance
	 * of {@link LDAPJSSESecureSocketFactory}.
	 */
	private LDAPSocketFactory secureSocketFactory = 
		new LDAPJSSESecureSocketFactory();

	/**
	 * Sockect factory for unsecure connections. Only relevant if
	 * {@link #secureConnection} is <code>false</code>. Defaults to a new instance
	 * of {@link LDAPSimpleSocketFactory}
	 */
	private LDAPSocketFactory socketFactory =
		new LDAPSimpleSocketFactory();


	/** LDAP referral following behavior. Defaults to {@link #DEFAULT_IS_FOLLOW_REFERRALS} */
	private boolean followReferrals = DEFAULT_IS_FOLLOW_REFERRALS;

	private boolean searchAliases = DEFAULT_IS_SEARCH_ALIASES;

	/** 
	 * Default timeout for operations in milliseconds. Defaults
	 * to {@link #DEFAULT_OPERATION_TIMEOUT_MILLIS}. Datatype
	 * matches arg type for 
	 * <code>LDAPConstraints.setTimeLimit(int)<code>.
	 */
	private int operationTimeout = DEFAULT_OPERATION_TIMEOUT_MILLIS;
	
	private int searchScope = DEFAULT_SEARCH_SCOPE;

	/** Should the provider support searching by Authentication ID */
	private boolean enableAid = false;

	/** 
	 * User entry attribute mappings. Keys are logical attr names,
	 * values are physical attr names.
	 * 
	 * @see LdapAttributeMapper
	 */
	private Map<String,String> attributeMappings;

	/** Handles LDAPConnection allocation */
	private LdapConnectionManager ldapConnectionManager;

	/** Handles LDAP attribute mappings and encapsulates filter writing */
	private LdapAttributeMapper ldapAttributeMapper;
	
	/** Currently limited to allowing/disallowing searches for particular user EIDs.
	 * Implements things like user EID blacklists. */
	private EidValidator eidValidator;

	/**
	 * Defaults to an anon-inner class which handles {@link LDAPEntry}(ies)
	 * by passing them to {@link #mapLdapEntryOntoUserData(LDAPEntry)}, the
	 * result of which is returned.
	 */
	protected LdapEntryMapper defaultLdapEntryMapper = new LdapEntryMapper() {

		// doesn't update UserEdit in the off chance the search result actually
		// yields multiple records
		public Object mapLdapEntry(LDAPEntry searchResult, int resultNum) {
			LdapUserData cacheRecord = mapLdapEntryOntoUserData(searchResult);
			return cacheRecord;
		}

	};

	/**
	 * Flag for allowing/disallowing authentication on a global basis
	 */
	private boolean allowAuthentication = DEFAULT_ALLOW_AUTHENTICATION;
	
	/**
	 * Flag for controlling the return value of 
	 * {@link #authenticateWithProviderFirst(String)} on a global basis.
	 */
	private boolean authenticateWithProviderFirst = DEFAULT_AUTHENTICATE_WITH_PROVIDER_FIRST;

	public JLDAPDirectoryProvider() {
		if ( log.isDebugEnabled() ) {
			log.debug("instantating JLDAPDirectoryProvider");
		}
	}

	/**
	 * Typically invoked by Spring to complete bean initialization.
	 * Ensures initialization of delegate {@link LdapConnectionManager}
	 * and {@link LdapAttributeMapper}
	 * 
	 * @see #initLdapConnectionManager()
	 * @see #initLdapAttributeMapper()
	 */
	public void init()
	{

		if ( log.isDebugEnabled() ) {
			log.debug("init()");
		}

		// We don't want to allow people to break their config by setting the batch size to be more than the maxResultsSize.
		if (batchSize > maxResultSize) {
			batchSize = maxResultSize;
			log.warn("JLDAP batchSize is larger than maxResultSize, batchSize has been reduced from: "+ batchSize + " to: "+ maxResultSize);
		}

		initLdapConnectionManager();
		initLdapAttributeMapper();

	}

	/**
	 * Lazily "injects" a {@link LdapConnectionManager} if one
	 * has not been assigned already.
	 * 
	 * <p>
	 * Implementation note: this approach to initing the 
	 * connection mgr preserves forward compatibility of 
	 * existing config, but config should probably be 
	 * refactored to inject the appropriate config directly 
	 * into the connection mgr.
	 * </p>
	 */
	protected void initLdapConnectionManager() {

		if ( log.isDebugEnabled() ) {
			log.debug("initLdapConnectionManager()");
		}

		// all very awkward b/c of the mixed-in config implementation
		if ( ldapConnectionManager == null ) {
			ldapConnectionManager = newDefaultLdapConnectionManager();
		}
		ldapConnectionManager.setConfig(this);
		ldapConnectionManager.init(); // see javadoc
	}

	/**
	 * Lazily "injects" a {@link LdapAttributeMapper} if one
	 * has not been assigned already.
	 * 
	 * <p>
	 * Implementation note: this approach to initing the 
	 * attrib mgr preserves forward compatibility of 
	 * existing config, but config should probably be 
	 * refactored to inject the appropriate config directly 
	 * into the attrib mgr.
	 * </p>
	 */
	protected void initLdapAttributeMapper() {

		if ( log.isDebugEnabled() ) {
			log.debug("initLdapAttributeMapper()");
		}

		if ( ldapAttributeMapper == null ) {
			// emulate what Spring should really be doing
			ldapAttributeMapper = newDefaultLdapAttributeMapper();
			ldapAttributeMapper.setAttributeMappings(attributeMappings);
			ldapAttributeMapper.init();
		}
	}

	/**
	 * Factory method for default {@link LdapConnectionManager} instances.
	 * Ensures forward compatibility of existing config which
	 * does not specify a delegate {@link LdapConnectionManager}.
	 * 
	 * @return a new {@link SimpleLdapConnectionManager}
	 */
	protected LdapConnectionManager newDefaultLdapConnectionManager() {
		if (this.isPooling()) {
			if ( log.isDebugEnabled() ) {
				log.debug(
				"newDefaultLdapConnectionManager(): returning a new PoolingLdapConnectionManager");
			}
			return new PoolingLdapConnectionManager();
		} else {
			if ( log.isDebugEnabled() ) {
				log.debug(
				"newDefaultLdapConnectionManager(): returning a new SimpleLdapConnectionManager");
			}
			return new SimpleLdapConnectionManager();
		}
	}

	/**
	 * Factory method for default {@link LdapAttributeMapper} instances.
	 * Ensures forward compatibility of existing config which
	 * does not specify a delegate {@link LdapAttributeMapper}.
	 * 
	 * @return a new {@link LdapAttributeMapper}
	 */
	protected LdapAttributeMapper newDefaultLdapAttributeMapper() {
		if ( log.isDebugEnabled() ) {
			log.debug(
			"newDefaultLdapAttributeMapper(): returning a new SimpleLdapAttributeMapper");
		}
		return new SimpleLdapAttributeMapper();
	}

	/**
	 * Typically called by Spring to signal bean destruction.
	 *
	 */
	public void destroy()
	{
		if ( log.isDebugEnabled() ) {
			log.debug("destroy()");
		}

	}

	/**
	 * Authenticates the specified user login by recursively searching for 
	 * and binding to a DN below the configured base DN. Search results are 
	 * subsequently added to the cache. 
	 * 
	 * <p>Caching search results departs from 
	 * behavior in &lt;= 2.3.0 versions, which removed cache entries following
	 * authentication. If the intention is to ensure fresh user data at each
	 * login, the most natural approach is probably to clear the cache before
	 * executing the authentication process. At this writing, though, the
	 * default {@link org.sakaiproject.user.api.UserDirectoryService} impl
	 * will invoke {@link #getUser(UserEdit)} prior to 
	 * {{@link #authenticateUser(String, UserEdit, String)}} if the Sakai's
	 * local db does not recognize the specified EID. Therefore, clearing the
	 * cache at in {{@link #authenticateUser(String, UserEdit, String)}}
	 * at best leads to confusing mid-session attribute changes. In the future
	 * we may want to consider strategizing this behavior, or adding an eid
	 * parameter to {@link #destroyAuthentication()} so cache records can
	 * be invalidated on logout without ugly dependencies on the
	 * {@link org.sakaiproject.tool.api.SessionManager}
	 * 
	 * @see #lookupUserBindDn(String, LDAPConnection)
	 */
	public boolean authenticateUser(final String userLogin, final UserEdit edit, final String password)
	{
		if ( log.isDebugEnabled() ) {
			log.debug("authenticateUser(): [userLogin = " + userLogin + "]");
		}

		if ( !(allowAuthentication) ) {
			log.debug("authenticateUser(): denying authentication attempt [userLogin = " + userLogin + "]. All authentication has been disabled via configuration");
			return false;
		}
		
		if ( StringUtils.isBlank(password) )
		{
			if ( log.isDebugEnabled() ) {
				log.debug("authenticateUser(): returning false, blank password");
			}
			return false;
		}

		LDAPConnection conn = null;

		try
		{

			// conn is implicitly bound as manager, if necessary
			if ( log.isDebugEnabled() ) {
				log.debug("authenticateUser(): allocating connection for login [userLogin = " + userLogin + "]");
			}
			conn = ldapConnectionManager.getConnection();

			// look up the end-user's DN, which could be nested at some 
			// arbitrary depth below getBasePath().
			// TODO: optimization opportunity if user entries are 
			// directly below getBasePath()
			final String endUserDN = lookupUserBindDn(userLogin, conn);

			if ( endUserDN == null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("authenticateUser(): failed to find bind dn for login [userLogin = " + userLogin + "], returning false");
				}
				return false;
			}

			if ( log.isDebugEnabled() ) {
				log.debug("authenticateUser(): returning connection to pool [userLogin = " + userLogin + "]");
			}
			ldapConnectionManager.returnConnection(conn);
			conn = null;
			if ( log.isDebugEnabled() ) {
				log.debug("authenticateUser(): attempting to allocate bound connection [userLogin = " + 
						userLogin + "][bind dn [" + endUserDN + "]");
			}
			conn = ldapConnectionManager.getBoundConnection(endUserDN, password);

			if ( log.isDebugEnabled() ) {
				log.debug("authenticateUser(): successfully allocated bound connection [userLogin = " + 
						userLogin + "][bind dn [" + endUserDN + "]");
			}
			return true;

		}
		catch (LDAPException e)
		{
			switch (e.getResultCode()) {
				case LDAPException.INVALID_CREDENTIALS:
					log.warn("authenticateUser(): invalid credentials [userLogin = " + userLogin + "]");
					return false;
				case LDAPException.UNWILLING_TO_PERFORM:
					log.warn("authenticateUser(): ldap service is unwilling to authenticate [userLogin = " + userLogin + "][reason = " + e.getLDAPErrorMessage() + "]");
					return false;
				default:
					throw new RuntimeException(
							"authenticateUser(): LDAPException during authentication attempt [userLogin = " +
									userLogin + "][result code = " + e.resultCodeToString() +
									"][error message = " + e.getLDAPErrorMessage() + "]", e);
			}
		} catch ( Exception e ) {
			throw new RuntimeException(
					"authenticateUser(): Exception during authentication attempt [userLogin = "
					+ userLogin + "]", e);
		} finally {
			if ( conn != null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("authenticateUser(): returning connection to connection manager");
				}
				ldapConnectionManager.returnConnection(conn);
			}
		}
	}

	/**
	 * Locates a user directory entry using an email address
	 * as a key. Updates the specified {@link org.sakaiproject.user.api.UserEdit}
	 * with directory attributes if the search is successful.
	 * The {@link org.sakaiproject.user.api.UserEdit} param is 
	 * technically optional and will be ignored if <code>null</code>.
	 * 
	 * <p>
	 * All {@link java.lang.Exception}s are logged and result in
	 * a <code>false</code> return, as do searches which yield
	 * no results. (A concession to backward compat.)
	 * </p>
	 * 
	 * @param edit the {@link org.sakaiproject.user.api.UserEdit} to update
	 * @param email the search key
	 * @return boolean <code>true</code> if the search 
	 *   completed without error and found a directory entry
	 */
	public boolean findUserByEmail(UserEdit edit, String email)
	{
		try {
			
			boolean useStdFilter = !(ldapAttributeMapper instanceof EidDerivedEmailAddressHandler);
			LdapUserData resolvedEntry = null;
			if ( !(useStdFilter) ) {
				try {
					String eid = 
						StringUtils.trimToNull(((EidDerivedEmailAddressHandler)ldapAttributeMapper).unpackEidFromAddress(email));
					if ( eid == null ) { // shouldn't happen (see unpackEidFromEmail() javadoc)
						throw new InvalidEmailAddressException("Attempting to unpack an EID from [" + email + 
								"] resulted in a null or empty string");
					}
					resolvedEntry = getUserByEid(eid, null);
				} catch ( InvalidEmailAddressException e ) {
					log.error("findUserByEmail(): Attempted to look up user at an invalid email address [" + email + "]", e);
					useStdFilter = true; // fall back to std processing, we cant derive an EID from this addr
				}
			}
			
			// we do _only_ fall back to std processing if EidDerivedEmailAddressHandler actually
			// indicated it could not handle the given email addr. If it could handle the addr
			// but found no results, we honor that empty result set
			if ( useStdFilter ) { // value may have been changed in EidDerivedEmailAddressHandler block above
				String filter = 
					ldapAttributeMapper.getFindUserByEmailFilter(email);
				resolvedEntry = (LdapUserData)searchDirectoryForSingleEntry(filter, 
						null, null, null, null);
			}
		
			if ( resolvedEntry == null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("findUserByEmail(): failed to find user by email [email = " + email + "]");
				}
				return false;
			}

			if ( log.isDebugEnabled() ) {
				log.debug("findUserByEmail(): found user by email [email = " + email + "]");
			}

			if ( edit != null ) {
				mapUserDataOntoUserEdit(resolvedEntry, edit);
			}

			return true;
		
		} catch ( Exception e ) {
			log.error("findUserByEmail(): failed [email = " + email + "]");
			log.debug("Exception: ", e);
			return false;
		}
		
		/*
		
		if ( log.isDebugEnabled() ) {
			log.debug("findUserByEmail(): [email = " + email + "]");
		}

		try {

			String filter = 
				ldapAttributeMapper.getFindUserByEmailFilter(email);

			// takes care of caching and everything
			LdapUserData mappedEntry = 
				(LdapUserData)searchDirectoryForSingleEntry(filter, 
						null, null, null, null);

			if ( mappedEntry == null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("findUserByEmail(): failed to find user by email [email = " + email + "]");
				}
				return false;
			}

			if ( log.isDebugEnabled() ) {
				log.debug("findUserByEmail(): found user by email [email = " + email + "]");
			}

			if ( edit != null ) {
				mapUserDataOntoUserEdit(mappedEntry, edit);
			}

			return true;

		} catch (Exception e) {
			log.error("findUserByEmail(): failed [email = " + email + "]", e);
			return false;
		}
		
		*/

	}

	/**
	 * Effectively the same as
	 * <code>getUserByEid(edit, edit.getEid())</code>.
	 * 
	 * @see #getUserByEid(UserEdit, String)
	 */
	public boolean getUser(UserEdit edit)
	{

		try {
			return getUserByEid(edit, edit.getEid(), null);
		} catch ( LDAPException e ) {
			log.error("getUser() failed [eid: " + edit.getEid() + "]", e);
			return false;
		}

	}

	public boolean getUserbyAid(String aid, UserEdit user)
	{
		// Only do search if we're enabled.
		if (!(enableAid)) {
			return false;
		}
		LdapUserData foundUserData = getUserByAid(aid, null);
		if ( foundUserData == null ) {
			return false;
		}
		if ( user != null ) {
			mapUserDataOntoUserEdit(foundUserData, user);
		}
		return true;
	}

	public LdapUserData getUserByAid(String aid, LDAPConnection conn) {
		String filter = ldapAttributeMapper.getFindUserByAidFilter(aid);
		LdapUserData mappedEntry = null;
		try {
			mappedEntry = (LdapUserData) searchDirectoryForSingleEntry(filter,
					conn, null, null, null);
		} catch (LDAPException e) {
			log.error("Failed to find user for AID: " + aid, e);
		}
		return mappedEntry;
	}

	/**
	 * Similar to iterating over <code>users</code> passing
	 * each element to {@link #getUser(UserEdit)}, removing the
	 * {@link org.sakaiproject.user.api.UserEdit} if that method 
	 * returns <code>false</code>. 
	 * 
	 * <p>Adds search retry capability if any one lookup fails 
	 * with a directory error. Empties <code>users</code> and 
	 * returns if a retry exits exceptionally
	 * <p>
	 */
	public void getUsers(Collection<UserEdit> users)
	{
		if ( log.isDebugEnabled() ) {
			log.debug("getUsers(): [Collection size = " + users.size() + "]");
		}

		LDAPConnection conn = null;
		boolean abortiveSearch = false;
		int maxQuerySize = getMaxObjectsToQueryFor();
		UserEdit userEdit = null;
		
		HashMap<String, UserEdit> usersToSearchInLDAP = new HashMap<String, UserEdit>();
		List<UserEdit> usersToRemove = new ArrayList<UserEdit>();
		try {
			int cnt = 0;
			for ( Iterator<UserEdit> userEdits = users.iterator(); userEdits.hasNext(); ) {
				userEdit = (UserEdit) userEdits.next();
				String eid = userEdit.getEid();
				
				if ( !(isSearchableEid(eid)) ) {
					userEdits.remove();
					//proceed ahead with this (perhaps the final) iteration
					//usersToSearchInLDAP needs to be processed unless empty
				} else {
						usersToSearchInLDAP.put(eid, userEdit);
						cnt++;
				}
				
				// We need to make sure this query isn't larger than maxQuerySize
				if ((!userEdits.hasNext() || cnt == maxQuerySize) && !usersToSearchInLDAP.isEmpty()) {
					if (conn == null) {
						conn = ldapConnectionManager.getConnection();
					}
					
					String filter = ldapAttributeMapper.getManyUsersInOneSearch(usersToSearchInLDAP.keySet());
					List<LdapUserData> ldapUsers = searchDirectory(filter, null, null, null, null, maxQuerySize);
				
					for (LdapUserData ldapUserData : ldapUsers) {
						String ldapEid = ldapUserData.getEid();

						if (StringUtils.isEmpty(ldapEid)) {
							continue;
						}
							ldapEid = ldapEid.toLowerCase();

						UserEdit ue = usersToSearchInLDAP.get(ldapEid);
						mapUserDataOntoUserEdit(ldapUserData, ue);
						usersToSearchInLDAP.remove(ldapEid);
					}
					
					// see if there are any users that we could not find in the LDAP query
					for (Map.Entry<String, UserEdit> entry : usersToSearchInLDAP.entrySet()) {
						usersToRemove.add(entry.getValue());
					}
					
					// clear the HashMap and reset the counter
					usersToSearchInLDAP.clear();
					cnt = 0;
				}
			}
			
			// Finally clean up the original collection and remove and users we could not find
			for (UserEdit userRemove : usersToRemove) {
				if (log.isDebugEnabled()) {
					log.debug("JLDAP getUsers could not find user: " + userRemove.getEid());
				}
				users.remove(userRemove);
			}
			
		} catch (LDAPException e)	{
			abortiveSearch = true;
			throw new RuntimeException("getUsers(): LDAPException during search [eid = " + 
					(userEdit == null ? null : userEdit.getEid()) + 
					"][result code = " + e.resultCodeToString() + 
					"][error message = " + e.getLDAPErrorMessage() + "]", e);
		} catch ( Exception e ) {
			abortiveSearch = true;
			throw new RuntimeException("getUsers(): RuntimeException during search eid = " + 
					(userEdit == null ? null : userEdit.getEid()) + 
					"]", e);
		} finally {

			if ( conn != null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("getUsers(): returning connection to connection manager");
				}
				ldapConnectionManager.returnConnection(conn);
			}

			// no sense in returning a partially complete search result
			if ( abortiveSearch ) {
				if ( log.isDebugEnabled() ) {
					log.debug("getUsers(): abortive search, clearing received users collection");
				}
				users.clear();
			}
		}

	}

	/**
	 * By default returns the global boolean setting configured
	 * via {@link #setAuthenticateWithProviderFirst(boolean)}.
	 */
	public boolean authenticateWithProviderFirst(String id)
	{
		return authenticateWithProviderFirst;
	}

	/**
	 * Effectively the same as <code>getUserByEid(null,eid)</code>.
	 * 
	 * @see #getUserByEid(UserEdit, String)
	 */
	public boolean userExists(String eid)
	{
		if ( log.isDebugEnabled() ) {
			log.debug("userExists(): [eid = " + eid + "]");
		}

		try {

			return getUserByEid(null, eid, null);

		} catch ( LDAPException e ) {
			log.error("userExists() failed: [eid = " + eid + "]", e);
			return false;
		}
	}

	/**
	 * Finds a user record using an <code>eid</code> as an index.
	 * Updates the given {@link org.sakaiproject.user.api.UserEdit} 
	 * if a directory entry is found.
	 * 
	 * @see #getUserByEid(String, LDAPConnection)
	 * @param userToUpdate the {@link org.sakaiproject.user.api.UserEdit} 
	 *   to update, may be <code>null<code>
	 * @param eid the user ID
	 * @param conn a <code>LDAPConnection</code> to reuse. may be <code>null</code>
	 * @return <code>true</code> if the directory entry was found, false if the
	 *   search returns without error but without results
	 * @throws LDAPException if the search returns with a directory access error
	 */
	protected boolean getUserByEid(UserEdit userToUpdate, String eid, LDAPConnection conn) 
	throws LDAPException {

		LdapUserData foundUserData = getUserByEid(eid, conn);
		if ( foundUserData == null ) {
			return false;
		}
		if ( userToUpdate != null ) {
			mapUserDataOntoUserEdit(foundUserData, userToUpdate);
		}
		return true;

	}

	/**
	 * Finds a user record using an <code>eid</code> as an index.
	 * 
	 * @param eid the Sakai EID to search on
	 * @param conn an optional {@link LDAPConnection}
	 * @return object representing the found LDAP entry, or null if no results
	 * @throws LDAPException if the search returns with a directory access error
	 */
	protected LdapUserData getUserByEid(String eid, LDAPConnection conn) 
	throws LDAPException {
		if ( log.isDebugEnabled() ) {
			log.debug("getUserByEid(): [eid = " + eid + "]");
		}

		if ( !(isSearchableEid(eid)) ) {
			if (eid == null)
			{
				log.debug("User EID not searchable (eid is null)");
			}
			else if ( log.isInfoEnabled() ) {
				log.info("User EID not searchable (possibly blacklisted or otherwise syntactically invalid) [" + eid + "]");
			}
			return null;
		}

		String filter = 
			ldapAttributeMapper.getFindUserByEidFilter(eid);

		// takes care of caching and everything
		return (LdapUserData)searchDirectoryForSingleEntry(filter, 
				conn, null, null, null);

	}

	/**
	 * Consults the cached {@link EidValidator} to determine if the
	 * given {@link User} EID is searchable. Allows any EID if no
	 * {@link EidValidator} has been configured.
	 *  
	 * @param eid a user EID, possibly <code>null</code> or otherwise "empty"
	 * @return <code>true</code> if no {@link EidValidator} has been
	 *   set, or the result of {@link EidValidator#isSearchableEid(String)}
	 */
	protected boolean isSearchableEid(String eid) {
		if ( eidValidator == null ) {
			return true;
		}
		return eidValidator.isSearchableEid(eid);
	}

	/**
	 * Search the directory for a DN corresponding to a user's
	 * EID. Typically, this is the same as DN of the object
	 * from which the user's attributes are retrieved, but
	 * that need not necessarily be the case.
	 * 
	 * @see #getUserByEid(String, LDAPConnection)
	 * @see LdapAttributeMapper#getUserBindDn(LdapUserData)
	 * @param eid the user's Sakai EID
	 * @param conn an optional {@link LDAPConnection}
	 * @return the user's bindable DN or null if no matching directory entry
	 * @throws LDAPException if the directory query exits with an error
	 */
	protected String lookupUserBindDn(String eid, LDAPConnection conn) 
	throws LDAPException {

		if ( log.isDebugEnabled() ) {
			log.debug("lookupUserEntryDN(): [eid = " + eid + 
					"][reusing conn = " + (conn != null) + "]");
		}

		LdapUserData foundUserData;
		if (enableAid) {
			foundUserData = getUserByAid(eid, conn);
		} else {
			foundUserData = getUserByEid(eid, conn);
		}

		if ( foundUserData == null ) {
			if ( log.isDebugEnabled() ) {
				log.debug("lookupUserEntryDN(): no directory entried found [eid = " + 
						eid + "]");
			}
			return null;
		}
		return ldapAttributeMapper.getUserBindDn(foundUserData);

	}


	/**
	 * Searches the directory for at most one entry matching the
	 * specified filter. 
	 * 
	 * @param filter a search filter
	 * @param conn an optional {@link LDAPConnection}
	 * @param searchResultPhysicalAttributeNames
	 * @param searchBaseDn
	 * @return a matching <code>LDAPEntry</code> or <code>null</code> if no match
	 * @throws LDAPException if the search exits with an error
	 */
	protected Object searchDirectoryForSingleEntry(String filter, 
			LDAPConnection conn,
			LdapEntryMapper mapper,
			String[] searchResultPhysicalAttributeNames,
			String searchBaseDn)
	throws LDAPException {

		if ( log.isDebugEnabled() ) {
			log.debug("searchDirectoryForSingleEntry(): [filter = " + filter + 
					"][reusing conn = " + (conn != null) + "]");
		}

		List<LdapUserData> results = searchDirectory(filter, conn,
				mapper,
				searchResultPhysicalAttributeNames,
				searchBaseDn, 
				1);
		if ( results.isEmpty() ) {
			return null;
		}

		return results.iterator().next();

	}

	/**
	 * Execute a directory search using the specified filter
	 * and connection. Maps each resulting {@link LDAPEntry}
	 * to a {@link LdapUserData}, returning a {@link List}
	 * of the latter.
	 * 
	 * @param filter the search filter
	 * @param conn an optional {@link LDAPConnection}
	 * @param mapper result interpreter. Defaults to 
	 *   {@link #defaultLdapEntryMapper} if <code>null</code> 
	 * @param searchResultPhysicalAttributeNames attributes to retrieve. 
	 *   May be <code>null</code>, in which case defaults to 
	 *   {@link LdapAttributeMapper#getSearchResultAttributes()}.
	 * @param searchBaseDn base DN from which to begin search. 
	 *   May be <code>null</code>, in which case defaults to assigned
	 *   <code>basePath</code>
	 * @param maxResults maximum number of retrieved LDAP objects. Ignored
	 *   if &lt;= 0
	 * @return An empty {@link List} if no results. Will not return <code>null</code>
	 * @throws LDAPException if thrown by the search
	 * @throws RuntimeExction wrapping any non-{@link LDAPException} {@link Exception}
	 */
	protected List<LdapUserData> searchDirectory(String filter, 
			LDAPConnection conn,
			LdapEntryMapper mapper,
			String[] searchResultPhysicalAttributeNames,
			String searchBaseDn, 
			int maxResults) 
	throws LDAPException {

		boolean receivedConn = conn != null;

		if ( log.isDebugEnabled() ) {
			log.debug("searchDirectory(): [filter = " + filter + 
					"][reusing conn = " + receivedConn + "]");
		}

		try {
			if ( !(receivedConn) ) {
				conn = ldapConnectionManager.getConnection();
			}
			if (conn == null) {
			    throw new IllegalStateException("Unable to obtain a valid LDAP connection");
			}

			searchResultPhysicalAttributeNames = 
				scrubSearchResultPhysicalAttributeNames(
						searchResultPhysicalAttributeNames);

			searchBaseDn = 
				scrubSearchBaseDn(searchBaseDn);

			if ( mapper == null ) {
				mapper = defaultLdapEntryMapper;
			}

			// TODO search constraints should be configurable in their entirety
			LDAPSearchConstraints constraints = new LDAPSearchConstraints();
			if (isSearchAliases()) { 
				constraints.setDereference(LDAPSearchConstraints.DEREF_ALWAYS);
			} else {
				constraints.setDereference(LDAPSearchConstraints.DEREF_NEVER);
			}

			constraints.setTimeLimit(operationTimeout);
			constraints.setReferralFollowing(followReferrals); // TODO: Do we want to make an explicit set optional?
			// Batch size is zero because we don't process the results until they are all in.
			constraints.setBatchSize(0);
			constraints.setMaxResults(maxResults);

			if ( log.isDebugEnabled() ) {
				log.debug("searchDirectory(): [baseDN = " + 
						searchBaseDn + "][filter = " + filter + 
						"][return attribs = " + 
						Arrays.toString(searchResultPhysicalAttributeNames) + 
						"][max results = " + maxResults + "]" +
						"][search scope = " + searchScope + "]");
			}
			long start = System.currentTimeMillis();
			
			LDAPSearchResults searchResults = 
				conn.search(searchBaseDn, 
						searchScope, 
						filter, 
						searchResultPhysicalAttributeNames, 
						false, 
						constraints);
			
			List<LdapUserData> mappedResults = new ArrayList<LdapUserData>();
			int resultCnt = 0;
			while ( searchResults.hasMore() ) {
				LDAPEntry entry = searchResults.next();
				Object mappedResult = mapper.mapLdapEntry(entry, ++resultCnt);
				if ( mappedResult == null ) {
					continue;
				}
				mappedResults.add((LdapUserData) mappedResult);
			}
			if (log.isDebugEnabled()) {
				log.debug("Query took: "+ (System.currentTimeMillis() - start)+ "ms.");
			}
			
			return mappedResults;

		} catch (LDAPException e) {
			throw e;
		} catch ( Exception e ) {
			throw new RuntimeException("searchDirectory(): RuntimeException while executing search [baseDN = " + 
					searchBaseDn + "][filter = " + filter + 
					"][return attribs = " + 
					Arrays.toString(searchResultPhysicalAttributeNames) + 
					"][max results = " + maxResults + "]", e);
		} finally {
			if ( !(receivedConn) && conn != null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("searchDirectory(): returning connection to connection manager");
				}
				ldapConnectionManager.returnConnection(conn);
			}
		}
	}

	/**
	 * Responsible for pre-processing base DNs passed to 
	 * {@link #searchDirectory(String, LDAPConnection, String[], String, int)}.
	 * As implemented, simply checks for a <code>null</code> reference,
	 * in which case it returns the currently cached "basePath". Otherwise
	 * returns the received <code>String</code> as is.
	 *  
	 * @see #setBasePath(String)
	 * @param searchBaseDn a proposed base DN. May be <code>null</code>
	 * @return a default base DN or the received DN, if non <code>null</code>. Return
	 *   value may be <code>null</code> if no default base DN has been configured
	 */
	protected String scrubSearchBaseDn(String searchBaseDn) {
		searchBaseDn = searchBaseDn == null ? basePath : searchBaseDn;
		return searchBaseDn;
	}

	/**
	 * Responsible for pre-processing search result attribute names
	 * passed to 
	 * {@link #searchDirectory(String, LDAPConnection, String[], String, int)}.
	 * If the given <code>String[]></code> is <code>null</code>,
	 * will use {@link LdapAttributeMapper#getSearchResultAttributes()}.
	 * If that method returns <code>null</code> will return an empty
	 * <code>String[]></code>. Otherwise returns the received <code>String[]></code>
	 * as-is.
	 * 
	 * @param searchResultPhysicalAttributeNames
	 * @return
	 */
	protected String[] scrubSearchResultPhysicalAttributeNames(
			String[] searchResultPhysicalAttributeNames) {

		if ( searchResultPhysicalAttributeNames == null ) {
			searchResultPhysicalAttributeNames = 
				ldapAttributeMapper.getSearchResultAttributes();
		}

		if ( searchResultPhysicalAttributeNames == null ) {
			searchResultPhysicalAttributeNames = new String[0];
		}

		return searchResultPhysicalAttributeNames;

	}

	/**
	 * Maps attributes from the specified <code>LDAPEntry</code> onto
	 * a newly instantiated {@link LdapUserData}. Implemented to
	 * delegate to the currently assigned {@link LdapAttributeMapper}.
	 * 
	 * @see LdapAttributeMapper#mapLdapEntryOntoUserData(LDAPEntry, LdapUserData)
	 * @param ldapEntry a non-null directory entry to map
	 * @return a new {@link LdapUserData}, populated with directory
	 *   attributes
	 */
	protected LdapUserData mapLdapEntryOntoUserData(LDAPEntry ldapEntry) {

		if ( log.isDebugEnabled() ) {
			log.debug("mapLdapEntryOntoUserData() [dn = " + ldapEntry.getDN() + "]");
		}

		LdapUserData userData = newLdapUserData();
		ldapAttributeMapper.mapLdapEntryOntoUserData(ldapEntry, userData);
		return userData;
	}

	/**
	 * Instantiates a {@link LdapUserData}. This method exists primarily for
	 * overriding in test cases.
	 * 
	 * @return a new {@link LdapUserData}
	 */
	protected LdapUserData newLdapUserData() {
		return new LdapUserData();
	}

	/**
	 * Maps attribites from the specified {@link LdapUserData} onto
	 * a {@link org.sakaiproject.user.api.UserEdit}. Implemented to
	 * delegate to the currently assigned {@link LdapAttributeMapper}.
	 * 
	 * @see LdapAttributeMapper#mapUserDataOntoUserEdit(LdapUserData, UserEdit)
	 * @param userData a non-null user cache entry
	 * @param userEdit a non-null user domain object
	 */
	protected void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit) {

		if ( log.isDebugEnabled() ) {
			//  std. UserEdit impl has no meaningful toString() impl
			log.debug("mapUserDataOntoUserEdit() [userData = " + userData + "]");
		}

		// delegate to the LdapAttributeMapper since it knows the most
		// about how the LdapUserData instance was originally populated
		ldapAttributeMapper.mapUserDataOntoUserEdit(userData, userEdit);
		
			userEdit.setEid(StringUtils.lowerCase(userData.getEid()));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLdapHost()
	{
		return ldapHost;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLdapHost(String ldapHost)
	{
		this.ldapHost = ldapHost;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLdapPort()
	{
		return ldapPort;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLdapPort(int ldapPort)
	{
		this.ldapPort = ldapPort;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLdapUser() {
		return ldapUser;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLdapUser(String ldapUser) {
		this.ldapUser = ldapUser;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLdapPassword() {
		return ldapPassword;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLdapPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSecureConnection()
	{
		return secureConnection;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSecureConnection(boolean secureConnection)
	{
		this.secureConnection = secureConnection;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKeystoreLocation()
	{
		return keystoreLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKeystoreLocation(String keystoreLocation)
	{
		this.keystoreLocation = keystoreLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKeystorePassword()
	{
		return keystorePassword;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKeystorePassword(String keystorePassword)
	{
		this.keystorePassword = keystorePassword;
	}

	/**
	 * {@inheritDoc}
	 */
	public LDAPSocketFactory getSecureSocketFactory() 
	{
		return secureSocketFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSecureSocketFactory(LDAPSocketFactory secureSocketFactory) 
	{
		this.secureSocketFactory = secureSocketFactory;
	}

	/**
	 * {@inheritDoc}
     */
	public LDAPSocketFactory getSocketFactory()
	{
		return socketFactory;
	}


	/**
	 * {@inheritDoc}
     */
	public void setSocketFactory(LDAPSocketFactory socketFactory)
	{
		this.socketFactory = socketFactory;
	}

	public String getBasePath()
	{
		return basePath;
	}

	public void setBasePath(String basePath)
	{
		this.basePath = basePath;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getOperationTimeout()
	{
		return operationTimeout;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOperationTimeout(int operationTimeout)
	{
		this.operationTimeout = operationTimeout;
	}

	/**
	 * @return LDAP attribute map, keys are logical names,
	 *   values are physical names. may be null
	 */
	public Map<String, String> getAttributeMappings()
	{
		return attributeMappings;
	}

	/**
	 * @param attributeMappings LDAP attribute map, keys are logical names,
	 *   values are physical names. may be null
	 */
	public void setAttributeMappings(Map<String, String> attributeMappings)
	{
		this.attributeMappings = attributeMappings;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFollowReferrals() {
		return followReferrals;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFollowReferrals(boolean followReferrals) {
		this.followReferrals = followReferrals;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAutoBind() {
		return autoBind;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAutoBind(boolean autoBind) {
		this.autoBind = autoBind;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPooling() {
		return pooling;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPooling(boolean pooling) {
		this.pooling = pooling;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPoolMaxConns() {
		return poolMaxConns;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPoolMaxConns(int poolMaxConns) {
		this.poolMaxConns = poolMaxConns;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getMaxObjectsToQueryFor() {
		return getBatchSize();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setMaxObjectsToQueryFor (int maxObjectsToQueryFor) {
		log.info("maxObjectToQueryFor is deprecated please use " + "batchSize@org.sakaiproject.user.api.UserDirectoryProvider instead");
		setBatchSize(maxObjectsToQueryFor);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getBatchSize() {
		return batchSize;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnableAid(boolean enableAid) {
		this.enableAid = enableAid;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxResultSize() {
		return maxResultSize;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxResultSize(int maxResultSize) {
		this.maxResultSize = maxResultSize;
	}

	/**
	 * Access the currently assigned {@link LdapConnectionManager} delegate. 
	 * This delegate handles LDAPConnection allocation.
	 * 
	 * @return the current {@link LdapConnectionManager}. May be
	 *   null if {@link #init()} has not been called yet.
	 */
	public LdapConnectionManager getLdapConnectionManager() {
		return ldapConnectionManager;
	}

	/**
	 * Assign the {@link LdapConnectionManager} delegate. This
	 * delegate handles LDAPConnection allocation.
	 * 
	 * @param ldapConnectionManager a {@link LdapConnectionManager}. 
	 *   may be null
	 */
	public void setLdapConnectionManager(LdapConnectionManager ldapConnectionManager) {
		this.ldapConnectionManager = ldapConnectionManager;
	}


	/**
	 * Access the currently assigned {@link LdapAttributeMapper} delegate. 
	 * This delegate handles LDAP attribute mappings and encapsulates filter 
	 * writing.
	 * 
	 * @return the current {@link LdapAttributeMapper}. May be
	 *   null if {@link #init()} has not been called yet.
	 */
	public LdapAttributeMapper getLdapAttributeMapper() {
		return ldapAttributeMapper;
	}

	/**
	 * Assign the {@link LdapAttributeMapper} delegate. This delegate 
	 * handles LDAP attribute mappings and encapsulates filter 
	 * writing.
	 * 
	 * @param ldapAttributeMapper a {@link LdapAttributeMapper}. 
	 *   may be null
	 */
	public void setLdapAttributeMapper(LdapAttributeMapper ldapAttributeMapper) {
		this.ldapAttributeMapper = ldapAttributeMapper;
	}

	/**
	 * Access the service used to verify EIDs prior to executing
	 * searches on those values.
	 * 
	 * @see #isSearchableEid(String)
	 * @return an {@link EidValidator} or <code>null</code> if no
	 *   such dependency has been configured
	 */
	public EidValidator getEidValidator() {
		return eidValidator;
	}

	/**
	 * Assign the service used to verify EIDs prior to executing
	 * searches on those values. This field defaults to <code>null</code>
	 * indicating that all EIDs are searchable.
	 * 
	 * @param eidValidator an {@link EidValidator} or <code>null</code>
	 *   to indicate that all EIDs are searchable.
	 */
	public void setEidValidator(EidValidator eidValidator) {
		this.eidValidator = eidValidator;
	}

	/**
	 * Access the current global authentication "on/off"
	 * switch.
	 * 
	 * @see #setAllowAuthentication(boolean)
	 * 
	 * @return boolean
	 */
	public boolean isAllowAuthentication() {
		return allowAuthentication;
	}

	/**
	 * Access the current global authentication "on/off" switch.
	 * <code>false</code> completely disables 
	 * {@link #authenticateUser(String, UserEdit, String)} (regardless of
	 * the value returned from 
	 * {@link #authenticateWithProviderFirst(String)}). <code>true</code>
	 * enables the {@link #authenticateUser(String, UserEdit, String)}
	 * algorithm. To simply authenticate all users without
	 * checking credentials, e.g. in a test environment, consider overriding
	 * {@link #authenticateUser(String, UserEdit, String)} altogether.
	 * 
	 * <p>Defaults to {@link #DEFAULT_ALLOW_AUTHENTICATION}</p>
	 * 
	 * @param allowAuthentication
	 */
	public void setAllowAuthentication(boolean allowAuthentication) {
		this.allowAuthentication = allowAuthentication;
	}
	
	/**
	 * An alias of {@link #setAllowAuthentication(boolean)} for backward
	 * compatibility with existing customized deployments of this provider
	 * which had already implemented this feature.
	 * 
	 * @param authenticateAllowed
	 */
	public void setAuthenticateAllowed(boolean authenticateAllowed) {
		setAllowAuthentication(authenticateAllowed);
	}

	/**
	 * Access the configured global return value for 
	 * {@link #authenticateWithProviderFirst(String)}. See
	 * {@link #setAuthenticateWithProviderFirst(boolean)} for
	 * additional semantics.
	 * 
	 * @return boolean
	 */
	public boolean isAuthenticateWithProviderFirst() {
		return authenticateWithProviderFirst;
	}

	/**
	 * Configure the global return value of 
	 * {@link #authenticateWithProviderFirst(String)}. Be aware that
	 * future development may expose a first-class extension point
	 * for custom implementations of {@link #authenticateWithProviderFirst(String)},
	 * in which case the value configured here will be treated as a default
	 * rather than an override.
	 * 
	 * @param authenticateWithProviderFirst
	 */
	public void setAuthenticateWithProviderFirst(
			boolean authenticateWithProviderFirst) {
		this.authenticateWithProviderFirst = authenticateWithProviderFirst;
	}

	public String getDisplayId(User user) {
		String displayId = user.getProperties().getProperty(DISPLAY_ID_PROPERTY);
		if (displayId != null && displayId.length() > 0) {
				return displayId;
		}
		return null;
	}

	public String getDisplayName(User user) {
		String displayName = user.getProperties().getProperty(DISPLAY_NAME_PROPERTY);
		if (displayName != null && displayName.length() > 0) {
			return displayName;
		}
		return null;
	}

	
	/**
	 * Access the configured search scope for all filters executed by
	 * {@link #searchDirectory(String, LDAPConnection, LdapEntryMapper, String[], String, int)}. 
	 * int value corresponds to a constant in {@link LDAPConnection}:
	 * SCOPE_BASE = 0, SCOPE_ONE = 1, SCOPE_SUB = 2. Defaults to
	 * {@link #DEFAULT_SEARCH_SCOPE}.
	 * 
	 */
	public int getSearchScope() {
		return searchScope;
	}
	
	/**
	 * Set the configured search scope for all filters executed by
	 * {@link #searchDirectory(String, LDAPConnection, LdapEntryMapper, String[], String, int)}.
	 * Validated
	 * 
	 * @param searchScope
	 * @throws IllegalArgumentException if given scope value is invalid
	 */
	public void setSearchScope(int searchScope) throws IllegalArgumentException {
		switch ( searchScope ) {
			case LDAPConnection.SCOPE_BASE :
			case LDAPConnection.SCOPE_ONE :
			case LDAPConnection.SCOPE_SUB :
				this.searchScope = searchScope;
				return;
			default :
				throw new IllegalArgumentException("Invalid search scope [" + searchScope +"]");
		}
	}

	/**
	 * User caching is done centrally in the UserDirectoryService.callCache
	 * @deprecated
	**/
	public void setMemoryService(org.sakaiproject.memory.api.MemoryService ignore) {
		log.warn("DEPRECATION WARNING: memoryService is deprecated. Please remove it from your jldap-beans.xml configuration.");
	}

	/** 
     * Search all the externally provided users that match this criteria in eid, 
     * email, first or last name. 
     * 
     * @param criteria 
     * The search criteria. 
     * @param first 
     * The first record position to return. 
     * @param last 
     * The last record position to return. 
     * @return A list (User) of all the aliases matching the criteria, within the 
     * record range given (sorted by sort name). 
     */  
	//public List<User> searchUsers(String criteria, int first, int last) {
	//	log.error("Not yet implemented");
	//	return null;
	//}

	/** 
     * Search for externally provided users that match this criteria in eid, email, first or last name. 
     * 
     * <p>Returns a List of UserEdit objects. This list will be <b>empty</b> if no results are returned or <b>null</b>
     * if your external provider does not implement this interface.<br />
     * 
     * The list will also be null if the LDAP server returns an error, for example an '(11) Administrative Limit Exceeded' 
     * or '(4) Sizelimit Exceeded', due to a search term being too broad and returning too many results.</p>
     *
     * <p>See LdapAttributeMapper.getFindUserByCrossAttributeSearchFilter for the filter used.</p>
     * 
     * @param criteria 
     * 		The search criteria. 
     * @param first 
     * 		The first record position to return. LDAP does not support paging so this value is unused.
     * @param last 
     * 		The last record position to return. LDAP does not support paging so this value is unused.
     * @param factory 
     * 		Use this factory's newUser() method to create the UserEdit objects you populate and return in the List.
     * @return 
     * 		A list (UserEdit) of all the users matching the criteria.
     */ 
	public List<UserEdit> searchExternalUsers(String criteria, int first, int last, UserFactory factory) {
		
		String filter = ldapAttributeMapper.getFindUserByCrossAttributeSearchFilter(criteria);
		List<UserEdit> users = new ArrayList<UserEdit>();
		
		try {
			//no limit to the number of search results, use the LDAP server's settings.
			List<LdapUserData> ldapUsers = searchDirectory(filter, null, null, null, null, maxResultSize);
			
			for(LdapUserData ldapUserData: ldapUsers) {
				
				//create a user object and map the data onto it
				//SAK-20625 ensure we have an id-eid mapping at this time
				UserEdit user = factory.newUser(ldapUserData.getEid());
				mapUserDataOntoUserEdit(ldapUserData, user);
				
				users.add(user);
			}

		} catch (LDAPException e) {
			log.warn("An error occurred searching for users: " + e.getClass().getName() + ": (" + e.getResultCode() + ") " + e.getMessage());
			return null;
		}
		
		return users;
	}
	
	/**
	 * Find all user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @param factory
	 *        To create all the UserEdit objects you populate and return in the return collection.
	 * @return Collection (UserEdit) of user objects that have this email address, or an empty Collection if there are none.
	 */
	@SuppressWarnings("rawtypes")
    public Collection findUsersByEmail(String email, UserFactory factory) {

		String filter = ldapAttributeMapper.getFindUserByEmailFilter(email);
		List<User> users = new ArrayList<User>();
		try {
			List<LdapUserData> ldapUsers = searchDirectory(filter, null, null, null, null, maxResultSize);

			for(LdapUserData ldapUserData: ldapUsers) {

				//SAK-20625 ensure we have an id-eid mapping at this time
				UserEdit user = factory.newUser(ldapUserData.getEid());
				mapUserDataOntoUserEdit(ldapUserData, user);

				users.add(user);
			}
		} catch (LDAPException e) {
			log.warn("An error occurred finding users by email: " + e.getClass().getName() + ": (" + e.getResultCode() + ") " + e.getMessage());
			return null;
		}
		return users;
	}


	public boolean isSearchAliases()
	{
		return searchAliases;
	}

	public void setSearchAliases(boolean searchAliases)
	{
		this.searchAliases = searchAliases;
	}

}
