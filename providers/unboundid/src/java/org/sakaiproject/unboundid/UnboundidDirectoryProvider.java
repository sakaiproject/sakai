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

package org.sakaiproject.unboundid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLSocketFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.user.api.AuthenticationIdUDP;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.ExternalUserSearchUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UsersShareEmailUDP;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.DereferencePolicy;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.ServerSet;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.SingleServerSet;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPConnection;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;

/**
 * <p>
 * An implementation of a Sakai UserDirectoryProvider that authenticates/retrieves 
 * users from a LDAP directory. Forked from JLDAP in early 2016.
 * </p>
 * 
 */
@Slf4j
public class UnboundidDirectoryProvider implements UserDirectoryProvider, LdapConnectionManagerConfig, ExternalUserSearchUDP, UsersShareEmailUDP, DisplayAdvisorUDP, AuthenticationIdUDP
{

	/** Security Service */
	@Setter private SecurityService securityService;

	/** Default LDAP connection port */
	public static final int[] DEFAULT_LDAP_PORT = {389};

	/** Default secure/unsecure LDAP connection creation behavior */
	public static final boolean DEFAULT_IS_SECURE_CONNECTION = false;

	/**  Default LDAP access timeout in milliseconds */
	public static final int DEFAULT_OPERATION_TIMEOUT_MILLIS = 9000;

	/** Default referral following behavior */
	public static final boolean DEFAULT_IS_FOLLOW_REFERRALS = false;
	
	public static final boolean DEFAULT_IS_SEARCH_ALIASES = false;

	/** Default search scope for filters executed by 
	 * {@link #searchDirectory(String, LDAPConnection, LdapEntryMapper, String[], String, int)}
	 */
	public static final SearchScope DEFAULT_SEARCH_SCOPE = SearchScope.SUB;

	/** Default LDAP maximum number of connections in the pool */
	public static final int DEFAULT_POOL_MAX_CONNS = 10;
	
	/** Default LDAP maximum number of objects in a result */
	public static final int DEFAULT_MAX_RESULT_SIZE = 1000;

	/** Default LDAP maximum number of objects to query for */
	public static final int DEFAULT_BATCH_SIZE = 200;
	
	/** Property of the user object to store the display ID under */
	public static final String DISPLAY_ID_PROPERTY = UnboundidDirectoryProvider.class+"-displayId";

	/** Property of the user object to store the display Name under */
	public static final String DISPLAY_NAME_PROPERTY = UnboundidDirectoryProvider.class+"-displayName";

	public static final boolean DEFAULT_ALLOW_AUTHENTICATION = true;

	public static final boolean DEFAULT_ALLOW_AUTHENTICATION_EXTERNAL = true;

	public static final boolean DEFAULT_ALLOW_AUTHENTICATION_ADMIN = false;

	public static final boolean DEFAULT_ALLOW_SEARCH_EXTERNAL = true;

	public static final boolean DEFAULT_ALLOW_GET_EXTERNAL = true;
	
	public static final boolean DEFAULT_AUTHENTICATE_WITH_PROVIDER_FIRST = false;

	/** LDAP host address */
	private String[] ldapHost;

	/** LDAP connection port. Defaults to {@link #DEFAULT_LDAP_PORT} */
	private int[] ldapPort = DEFAULT_LDAP_PORT;

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

	/** Maximum number of physical connections in the pool */
	private int poolMaxConns = DEFAULT_POOL_MAX_CONNS;
	
	/** Maximum number of results from one LDAP query */
	private int maxResultSize = DEFAULT_MAX_RESULT_SIZE;

	/** The size of each batch to load from LDAP when loading multiple users. */
	private int batchSize = DEFAULT_BATCH_SIZE;

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
	
	private SearchScope searchScope = DEFAULT_SEARCH_SCOPE;

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
	private LDAPConnectionPool connectionPool;

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
	 * Flag for allowing/disallowing authentication for external users (who do not already exist).
	 * If false, only users who have existing accounts may authenticate via LDAP.
	 */
	@Getter @Setter private boolean allowAuthenticationExternal = DEFAULT_ALLOW_AUTHENTICATION_EXTERNAL;

	/**
	 * Flag for allowing/disallowing authentication for admin-equivalent users.
	 * If false, users who have admin-equivalent accounts may not authenticate via LDAP.
	 */
	@Getter @Setter private boolean allowAuthenticationAdmin = DEFAULT_ALLOW_AUTHENTICATION_ADMIN;

	/**
	 * Flag for allowing/disallowing searching external users
	 */
	@Getter @Setter private boolean allowSearchExternal = DEFAULT_ALLOW_SEARCH_EXTERNAL;

	/**
	 * Flag for allowing/disallowing getting an external user
	 */
	@Getter @Setter private boolean allowGetExternal = DEFAULT_ALLOW_GET_EXTERNAL;
	
	/**
	 * Flag for controlling the return value of 
	 * {@link #authenticateWithProviderFirst(String)} on a global basis.
	 */
	private boolean authenticateWithProviderFirst = DEFAULT_AUTHENTICATE_WITH_PROVIDER_FIRST;

	public UnboundidDirectoryProvider() {
		log.debug("instantating UnboundidDirectoryProvider");
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

		log.debug("init()");

		// We don't want to allow people to break their config by setting the batch size to be more than the maxResultsSize.
		if (batchSize > maxResultSize) {
			batchSize = maxResultSize;
			log.warn("Unboundid batchSize is larger than maxResultSize, batchSize has been reduced from: "+ batchSize + " to: "+ maxResultSize);
		}

		// Create a new LDAP connection pool with 10 connections
		ServerSet serverSet = null;

		// Set some sane defaults to better handle timeouts. Unboundid will wait 30 seconds by default on a hung connection.
		LDAPConnectionOptions connectOptions = new LDAPConnectionOptions();
		connectOptions.setAbandonOnTimeout(true);
		connectOptions.setConnectTimeoutMillis(operationTimeout);
		connectOptions.setResponseTimeoutMillis(operationTimeout); // Sakai should not be making any giant queries to LDAP

		if (isSecureConnection()) {
			try {
				// If testing locally only, could use `new TrustAllTrustManager()` as contructor parameter to SSLUtil
				SSLUtil sslUtil = new SSLUtil();
				SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();

				serverSet = new SingleServerSet(ldapHost[0], ldapPort[0], sslSocketFactory, connectOptions);
			} catch (GeneralSecurityException ex) {
				log.error("Error while initializing LDAP SSLSocketFactory");
				throw new RuntimeException(ex);
			}
		} else {
			serverSet = new SingleServerSet(ldapHost[0], ldapPort[0], connectOptions);
		}

		SimpleBindRequest bindRequest = new SimpleBindRequest(ldapUser, ldapPassword);
		try {
			log.info("Creating LDAP connection pool of size " + poolMaxConns);
			connectionPool = new LDAPConnectionPool(serverSet, bindRequest, poolMaxConns);
		} catch (com.unboundid.ldap.sdk.LDAPException e) {
			log.error("Could not init LDAP pool", e);
		}
		   
		initLdapAttributeMapper();
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

		log.debug("initLdapAttributeMapper()");

		if ( ldapAttributeMapper == null ) {
			// emulate what Spring should really be doing
			ldapAttributeMapper = newDefaultLdapAttributeMapper();
			ldapAttributeMapper.setAttributeMappings(attributeMappings);
			ldapAttributeMapper.init();
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
		log.debug("newDefaultLdapAttributeMapper(): returning a new SimpleLdapAttributeMapper");
		return new SimpleLdapAttributeMapper();
	}

	/**
	 * Typically called by Spring to signal bean destruction.
	 *
	 */
	public void destroy() {
		log.debug("destroy()");
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
		com.unboundid.ldap.sdk.LDAPConnection lc = null;

		log.debug("authenticateUser(): [userLogin = {}]", userLogin);

		if ( !(allowAuthentication) ) {
			log.debug("authenticateUser(): denying authentication attempt [userLogin = " + userLogin + "]. All authentication has been disabled via configuration");
			return false;
		}
		
		if ( StringUtils.isBlank(password) ) {
			log.debug("authenticateUser(): returning false, blank password");
			return false;
		}

		if ( !allowAuthenticationExternal && (edit.getId() == null)) {
			log.debug("authenticateUser(): returning false, not authenticating for external users");
			return false;
		}

		if ( !allowAuthenticationAdmin && securityService.isSuperUser(edit.getId())) {
			log.debug("authenticateUser(): returning false, not authenticating for superuser (admin) {}", edit.getEid());
			return false;
		}

		try
		{
			long start = System.currentTimeMillis();

			// look up the end-user's DN, which could be nested at some 
			// arbitrary depth below getBasePath().
			// TODO: optimization opportunity if user entries are 
			// directly below getBasePath()
			final String endUserDN = lookupUserBindDn(userLogin);

			if ( endUserDN == null ) {
				log.debug("authenticateUser(): failed to find bind dn for login [userLogin = {}], returning false", userLogin);
				return false;
			}

			log.debug("authenticateUser(): attempting to allocate bound connection [userLogin = {}][bind dn [{}]", userLogin, endUserDN);
			
			lc = connectionPool.getConnection();
			BindResult bindResult = lc.bind(endUserDN, password);
			if(bindResult.getResultCode().equals(ResultCode.SUCCESS)) {
				log.info("Authenticated {} ({}) from LDAP in {} ms", userLogin, endUserDN, System.currentTimeMillis() - start);
				return true;
			}

			log.debug("authenticateUser(): unsuccessfull bind attempt [userLogin = {}][bind dn [{}]", userLogin, endUserDN);
			return false;
		}
		catch (com.unboundid.ldap.sdk.LDAPException e)
		{
			if (e.getResultCode().intValue() == LDAPException.INVALID_CREDENTIALS) {
				log.info("authenticateUser(): invalid credentials [userLogin = {}]", userLogin);
				return false;
			} else {
				throw new RuntimeException(
						"authenticateUser(): LDAPException during authentication attempt [userLogin = "
						+ userLogin + "][result code = " + e.getResultCode().toString() + 
						"][error message = "+ e.getExceptionMessage() + "]", e);
			}
		} catch ( Exception e ) {
			throw new RuntimeException(
					"authenticateUser(): Exception during authentication attempt [userLogin = "
					+ userLogin + "]", e);
		} finally {
			// We don't want this user-bound LDAP connection returned to the pool
			connectionPool.releaseDefunctConnection(lc);
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
					resolvedEntry = getUserByEid(eid);
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
						null, null, null);
			}
		
			if ( resolvedEntry == null ) {
				log.debug("findUserByEmail(): failed to find user by email [email = {}]", email);
				return false;
			}

			log.debug("findUserByEmail(): found user by email [email = {}]", email);

			if ( edit != null ) {
				mapUserDataOntoUserEdit(resolvedEntry, edit);
			}

			return true;
		
		} catch ( Exception e ) {
			log.error("findUserByEmail(): failed [email = " + email + "]");
			log.debug("Exception: ", e);
			return false;
		}

	}

	/**
	 * Effectively the same as
	 * <code>getUserByEid(edit, edit.getEid())</code>.
	 * 
	 * @see #getUserByEid(UserEdit, String)
	 */
	public boolean getUser(UserEdit edit)
	{

		if (!allowGetExternal) {
			log.debug("getUser() external get not enabled");
			return false;
		}

		try {
			return getUserByEid(edit, edit.getEid());
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
		LdapUserData foundUserData = getUserByAid(aid);
		if ( foundUserData == null ) {
			return false;
		}
		if ( user != null ) {
			mapUserDataOntoUserEdit(foundUserData, user);
		}
		return true;
	}

	public LdapUserData getUserByAid(String aid) {
		String filter = ldapAttributeMapper.getFindUserByAidFilter(aid);
		LdapUserData mappedEntry = null;
		try {
			mappedEntry = (LdapUserData) searchDirectoryForSingleEntry(filter,
					null, null, null);
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
		log.debug("getUsers(): [Collection size = {}]", users.size());

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
					String filter = ldapAttributeMapper.getManyUsersInOneSearch(usersToSearchInLDAP.keySet());
					List<LdapUserData> ldapUsers = searchDirectory(filter, null, null, null, maxQuerySize);
				
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
				log.debug("Unboundid getUsers could not find user: {}", userRemove.getEid());
				users.remove(userRemove);
			}
			
		} catch (LDAPException e)	{
			abortiveSearch = true;
			throw new RuntimeException("getUsers(): LDAPException during search [eid = " + 
					(userEdit == null ? null : userEdit.getEid()) + 
					"][result code = " + e.errorCodeToString() + 
					"][error message = " + e.getLDAPErrorMessage() + "]", e);
		} catch ( Exception e ) {
			abortiveSearch = true;
			throw new RuntimeException("getUsers(): RuntimeException during search eid = " + 
					(userEdit == null ? null : userEdit.getEid()) + 
					"]", e);
		} finally {
			// no sense in returning a partially complete search result
			if ( abortiveSearch ) {
				log.debug("getUsers(): abortive search, clearing received users collection");
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
		log.debug("userExists(): [eid = {}]", eid);

		try {

			return getUserByEid(null, eid);

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
	protected boolean getUserByEid(UserEdit userToUpdate, String eid) 
	throws LDAPException {

		LdapUserData foundUserData = getUserByEid(eid);
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
	protected LdapUserData getUserByEid(String eid) 
	throws LDAPException {

		if ( !(isSearchableEid(eid)) ) {
			if (eid == null)
			{
				log.debug("User EID not searchable (eid is null)");
				return null;
			}

			log.info("User EID not searchable (possibly blacklisted or otherwise syntactically invalid) [{}]", eid);
			return null;
		}

		log.debug("getUserByEid(): [eid = {}]", eid);
		String filter = ldapAttributeMapper.getFindUserByEidFilter(eid);

		// takes care of caching and everything
		return (LdapUserData)searchDirectoryForSingleEntry(filter, 
				null, null, null);

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
	protected String lookupUserBindDn(String eid) 
	throws LDAPException {

			log.debug("lookupUserEntryDN(): [eid = {}]", eid);

		LdapUserData foundUserData;
		if (enableAid) {
			foundUserData = getUserByAid(eid);
		} else {
			foundUserData = getUserByEid(eid);
		}

		if ( foundUserData == null ) {
			log.debug("lookupUserEntryDN(): no directory entried found [eid = {}]", eid);
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
			LdapEntryMapper mapper,
			String[] searchResultPhysicalAttributeNames,
			String searchBaseDn)
	throws LDAPException {

		log.debug("searchDirectoryForSingleEntry(): [filter = {}]", filter);

		List<LdapUserData> results = searchDirectory(filter,
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
	protected List<LdapUserData> searchDirectory(final String filter, 
			final LdapEntryMapper passedMapper,
			final String[] searchResultPhysicalAttributeNames,
			final String unescapedSearchBaseDn, 
			final int maxResults) 
	throws LDAPException {

		log.debug("searchDirectory(): [filter = {}]", filter);

		try {

			final String[] scrubbedPhysicalAttributeNames = scrubSearchResultPhysicalAttributeNames(searchResultPhysicalAttributeNames);

			final String searchBaseDn = scrubSearchBaseDn(unescapedSearchBaseDn);

			LdapEntryMapper mapper = defaultLdapEntryMapper;
			if ( passedMapper != null ) {
				mapper = passedMapper;
			}

			DereferencePolicy dr = DereferencePolicy.NEVER;
			if (isSearchAliases()) { 
				dr = DereferencePolicy.ALWAYS;
			}

			log.debug("searchDirectory(): [baseDN = {}][filter = {}][return attribs = {}][max results = {}][search scope = {}]",
				searchBaseDn, filter, Arrays.toString(scrubbedPhysicalAttributeNames), maxResults, searchScope);
			long start = System.currentTimeMillis();
			
			SearchResult searchResult = null;

                        try {
                            searchResult = connectionPool.search(searchBaseDn, 
                                    searchScope,
                                    dr,
                                    maxResults,
                                    operationTimeout,
                                    false,
                                    filter,
                                    scrubbedPhysicalAttributeNames
                            );
                        } catch (LDAPSearchException e) {
                            if (e.getResultCode().equals(ResultCode.SIZE_LIMIT_EXCEEDED)) {
                                // We still want results even
                                // though we hit the max.  Just take what we
                                // were able to get.
                                searchResult = e.getSearchResult();
                                log.warn("Hit ResultCode.SIZE_LIMIT_EXCEEDED: {}", e.getDiagnosticMessage());
                            } else {
                                throw e;
                            }
                        }

			List<SearchResultEntry> searchResults = searchResult.getSearchEntries();
			
			List<LdapUserData> mappedResults = new ArrayList<LdapUserData>();
			int resultCnt = 0;
			for (SearchResultEntry sre : searchResults) {
				LDAPEntry entry = new LDAPEntry(sre);
				Object mappedResult = mapper.mapLdapEntry(entry, ++resultCnt);
				if ( mappedResult == null ) {
					continue;
				}
				mappedResults.add((LdapUserData) mappedResult);
			}
			log.debug("Query took: {}ms",  (System.currentTimeMillis() - start));
			
			return mappedResults;

		} catch ( Exception e ) {
			throw new RuntimeException("searchDirectory(): RuntimeException while executing search [baseDN = " + 
					unescapedSearchBaseDn + "][filter = " + filter + 
					"][return attribs = " + 
					Arrays.toString(searchResultPhysicalAttributeNames) + 
					"][max results = " + maxResults + "]", e);
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
	protected String scrubSearchBaseDn(final String searchBaseDn) {
		return searchBaseDn == null ? basePath : searchBaseDn;
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
	protected String[] scrubSearchResultPhysicalAttributeNames(final String[] searchResultPhysicalAttributeNames) {
		String[] scrubbedNames = searchResultPhysicalAttributeNames;

		if ( scrubbedNames == null ) {
			scrubbedNames = ldapAttributeMapper.getSearchResultAttributes();
		}

		if ( scrubbedNames == null ) {
			scrubbedNames = new String[0];
		}

		return scrubbedNames;

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

		log.debug("mapLdapEntryOntoUserData() [dn = {}]", ldapEntry.getDN());

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

		//  std. UserEdit impl has no meaningful toString() impl
		log.debug("mapUserDataOntoUserEdit() [userData = {}]", userData);

		// delegate to the LdapAttributeMapper since it knows the most
		// about how the LdapUserData instance was originally populated
		ldapAttributeMapper.mapUserDataOntoUserEdit(userData, userEdit);
		
			userEdit.setEid(StringUtils.lowerCase(userData.getEid()));
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getLdapHost()
	{
		return ldapHost;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLdapHost(String[] ldapHost)
	{
		this.ldapHost = ldapHost;
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getLdapPort()
	{
		return ldapPort;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLdapPort(int[] ldapPort)
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
	public SearchScope getSearchScope() {
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
				this.searchScope = SearchScope.BASE;
				return;
			case LDAPConnection.SCOPE_ONE :
				this.searchScope = SearchScope.ONE;
				return;
			case LDAPConnection.SCOPE_SUB :
				this.searchScope = SearchScope.SUB;
				return;
			default :
				throw new IllegalArgumentException("Invalid search scope [" + searchScope +"]");
		}
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

		if (!allowSearchExternal) {
			log.debug("External search is disabled");
			return null;
		}
		
		String filter = ldapAttributeMapper.getFindUserByCrossAttributeSearchFilter(criteria);
		List<UserEdit> users = new ArrayList<UserEdit>();
		
		try {
			//no limit to the number of search results, use the LDAP server's settings.
			List<LdapUserData> ldapUsers = searchDirectory(filter, null, null, null, maxResultSize);
			
			for(LdapUserData ldapUserData: ldapUsers) {
				
				//create a user object and map the data onto it
				//SAK-20625 ensure we have an id-eid mapping at this time
				UserEdit user = factory.newUser(ldapUserData.getEid());
				mapUserDataOntoUserEdit(ldapUserData, user);
				
				users.add(user);
			}

		} catch (LDAPException e) {
			log.warn("An error occurred searching for users: " + e.getClass().getName() + ": (" + e.getLDAPResultCode() + ") " + e.getMessage());
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

		List<User> users = new ArrayList<User>();

                if (!allowSearchExternal) {
                        log.debug("External search is disabled");
                        return users;
                }

		String filter = ldapAttributeMapper.getFindUserByEmailFilter(email);
		try {
			List<LdapUserData> ldapUsers = searchDirectory(filter, null, null, null, maxResultSize);

			for(LdapUserData ldapUserData: ldapUsers) {

				//SAK-20625 ensure we have an id-eid mapping at this time
				UserEdit user = factory.newUser(ldapUserData.getEid());
				mapUserDataOntoUserEdit(ldapUserData, user);

				users.add(user);
			}
		} catch (LDAPException e) {
			log.warn("An error occurred finding users by email: " + e.getClass().getName() + ": (" + e.getLDAPResultCode() + ") " + e.getMessage());
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
