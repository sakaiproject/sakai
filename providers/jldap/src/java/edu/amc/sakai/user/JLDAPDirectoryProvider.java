/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.util.StringUtil;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;

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
public class JLDAPDirectoryProvider implements UserDirectoryProvider, LdapConnectionManagerConfig
{
	/** Default LDAP connection port */
	public static final int DEFAULT_LDAP_PORT = 389;

	/** Default secure/unsecure LDAP connection creation behavior */
	public static final boolean DEFAULT_IS_SECURE_CONNECTION = false;

	/**  Default LDAP access timeout in milliseconds */
	public static final int DEFAULT_OPERATION_TIMEOUT_MILLIS = 5000;

	/** Default referral following behavior */
	public static final boolean DEFAULT_IS_FOLLOW_REFERRALS = false;
	
	/** Default search scope for filters executed by 
	 * {@link #searchDirectory(String, LDAPConnection, LdapEntryMapper, String[], String, int)}
	 */
	public static final int DEFAULT_SEARCH_SCOPE = LDAPConnection.SCOPE_SUB;

	/** Default LDAP user entry cache TTL */
	public static final long DEFAULT_CACHE_TTL = 5 * 60 * 1000;

	/** Default LDAP use of connection pooling */
	public static final boolean DEFAULT_POOLING = false;

	/** Default LDAP maximum number of connections in the pool */
	public static final int DEFAULT_POOL_MAX_CONNS = 10;

	public static final boolean DEFAULT_CASE_SENSITIVE_CACHE_KEYS = false;
	
	public static final boolean DEFAULT_ALLOW_AUTHENTICATION = true;
	
	public static final boolean DEFAULT_AUTHENTICATE_WITH_PROVIDER_FIRST = false;

	/** Class-specific logger */
	private static Log M_log = LogFactory.getLog(JLDAPDirectoryProvider.class);

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

	/** Socket factory for secure connections. Only relevant if
	 * {@link #secureConnection} is true. Defaults to a new instance
	 * of {@link LDAPJSSESecureSocketFactory}.
	 */
	private LDAPSocketFactory secureSocketFactory = 
		new LDAPJSSESecureSocketFactory();

	/** LDAP referral following behavior. Defaults to {@link #DEFAULT_IS_FOLLOW_REFERRALS} */
	private boolean followReferrals = DEFAULT_IS_FOLLOW_REFERRALS;

	/** 
	 * Default timeout for operations in milliseconds. Defaults
	 * to {@link #DEFAULT_OPERATION_TIMEOUT_MILLIS}. Datatype
	 * matches arg type for 
	 * <code>LDAPConstraints.setTimeLimit(int)<code>.
	 */
	private int operationTimeout = DEFAULT_OPERATION_TIMEOUT_MILLIS;
	
	private int searchScope = DEFAULT_SEARCH_SCOPE;

	/** 
	 * User entry attribute mappings. Keys are logical attr names,
	 * values are physical attr names.
	 * 
	 * {@see LdapAttributeMapper}
	 */
	private Map<String,String> attributeMappings;

	/**
	 * Cache of {@link LdapUserData} objects, keyed by eid. 
	 * {@link cacheTtl} controls TTL. 
	 * 
	 * TODO: This is a naive implementation: cache
	 * is completely isolated on each app node.
	 */
	private Map<String,LdapUserData> userCache = 
		new ConcurrentHashMap<String, LdapUserData>();

	/** TTL for cachedUsers. Defaults to {@link #DEFAULT_CACHE_TTL} */
	private long cacheTtl = DEFAULT_CACHE_TTL;

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
	 * result of which is passed to {@link #cacheUserData(LdapUserData)}
	 * and returned;
	 */
	protected LdapEntryMapper defaultLdapEntryMapper = new LdapEntryMapper() {

		// doesn't update UserEdit in the off chance the search result actually
		// yields multiple records
		public Object mapLdapEntry(LDAPEntry searchResult, int resultNum) {
			LdapUserData cacheRecord = mapLdapEntryOntoUserData(searchResult);
			cacheUserData(cacheRecord);
			return cacheRecord;
		}

	};

	private boolean caseSensitiveCacheKeys = DEFAULT_CASE_SENSITIVE_CACHE_KEYS;
	
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
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("instantating JLDAPDirectoryProvider");
		}
	}

	/**
	 * Typically invoked by Spring to complete bean initialization.
	 * Ensures initialization of delegate {@link LdapConnectionManager}
	 * and {@link LdapAttributeMapper}
	 * 
	 * @see #initConnectionManager()
	 * @see #initLdapAttributeMapper()
	 */
	public void init()
	{

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("init()");
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

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("initLdapConnectionManager()");
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

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("initLdapAttributeMapper()");
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
			if ( M_log.isDebugEnabled() ) {
				M_log.debug(
				"newDefaultLdapConnectionManager(): returning a new PoolingLdapConnectionManager");
			}
			return new PoolingLdapConnectionManager();
		} else {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug(
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
		if ( M_log.isDebugEnabled() ) {
			M_log.debug(
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
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("destroy()");
		}

		clearCache();
	}

	/**
	 * Resets the internal {@link LdapUserData} cache
	 */
	public void clearCache() {
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("clearCache()");
		}

		userCache.clear();
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
	public boolean authenticateUser(String userLogin, UserEdit edit, String password)
	{
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("authenticateUser(): [userLogin = " + userLogin + "]");
		}

		if ( !(allowAuthentication) ) {
			M_log.debug("authenticateUser(): denying authentication attempt [userLogin = " + userLogin + "]. All authentication has been disabled via configuration");
			return false;
		}
		
		boolean isPassword = (password != null) && (password.trim().length() > 0);
		if ( !(isPassword) )
		{
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("authenticateUser(): returning false, blank password");
			}
			return false;
		}

		LDAPConnection conn = null;

		try
		{

			// conn is implicitly bound as manager, if necessary
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("authenticateUser(): allocating connection for login [userLogin = " + userLogin + "]");
			}
			conn = ldapConnectionManager.getConnection();

			// look up the end-user's DN, which could be nested at some 
			// arbitrary depth below getBasePath().
			// TODO: optimization opportunity if user entries are 
			// directly below getBasePath()
			String endUserDN = lookupUserBindDn(userLogin, conn);

			if ( endUserDN == null ) {
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("authenticateUser(): failed to find bind dn for login [userLogin = " + userLogin + "], returning false");
				}
				return false;
			}

			if ( M_log.isDebugEnabled() ) {
				M_log.debug("authenticateUser(): returning connection to pool [userLogin = " + userLogin + "]");
			}
			ldapConnectionManager.returnConnection(conn);
			conn = null;
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("authenticateUser(): attempting to allocate bound connection [userLogin = " + 
						userLogin + "][bind dn [" + endUserDN + "]");
			}
			conn = ldapConnectionManager.getBoundConnection(endUserDN, password);

			if ( M_log.isDebugEnabled() ) {
				M_log.debug("authenticateUser(): successfully allocated bound connection [userLogin = " + 
						userLogin + "][bind dn [" + endUserDN + "]");
			}
			return true;

		}
		catch (LDAPException e)
		{
			if (e.getResultCode() == LDAPException.INVALID_CREDENTIALS) {
				if ( M_log.isWarnEnabled() ) {
					M_log.warn("authenticateUser(): invalid credentials [userLogin = "
							+ userLogin + "]");
				}
				return false;
			} else {
				throw new RuntimeException(
						"authenticateUser(): LDAPException during authentication attempt [userLogin = "
						+ userLogin + "][result code = " + e.resultCodeToString() + 
						"][error message = "+ e.getLDAPErrorMessage() + "]", e);
			}
		} catch ( Exception e ) {
			throw new RuntimeException(
					"authenticateUser(): Exception during authentication attempt [userLogin = "
					+ userLogin + "]", e);
		} finally {
			if ( conn != null ) {
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("authenticateUser(): returning connection to connection manager");
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
						StringUtil.trimToNull(((EidDerivedEmailAddressHandler)ldapAttributeMapper).unpackEidFromAddress(email));
					if ( eid == null ) { // shouldn't happen (see unpackEidFromEmail() javadoc)
						throw new InvalidEmailAddressException("Attempting to unpack an EID from [" + email + 
								"] resulted in a null or empty string");
					}
					resolvedEntry = getUserByEid(eid, null);
				} catch ( InvalidEmailAddressException e ) {
					M_log.warn("findUserByEmail(): Attempted to look up user at an invalid email address [" + email + "]", e);
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
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("findUserByEmail(): failed to find user by email [email = " + email + "]");
				}
				return false;
			}

			if ( M_log.isDebugEnabled() ) {
				M_log.debug("findUserByEmail(): found user by email [email = " + email + "]");
			}

			if ( edit != null ) {
				mapUserDataOntoUserEdit(resolvedEntry, edit);
			}

			return true;
		
		} catch ( Exception e ) {
			M_log.error("findUserByEmail(): failed [email = " + email + "]", e);
			return false;
		}
		
		/*
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("findUserByEmail(): [email = " + email + "]");
		}

		try {

			String filter = 
				ldapAttributeMapper.getFindUserByEmailFilter(email);

			// takes care of caching and everything
			LdapUserData mappedEntry = 
				(LdapUserData)searchDirectoryForSingleEntry(filter, 
						null, null, null, null);

			if ( mappedEntry == null ) {
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("findUserByEmail(): failed to find user by email [email = " + email + "]");
				}
				return false;
			}

			if ( M_log.isDebugEnabled() ) {
				M_log.debug("findUserByEmail(): found user by email [email = " + email + "]");
			}

			if ( edit != null ) {
				mapUserDataOntoUserEdit(mappedEntry, edit);
			}

			return true;

		} catch (Exception e) {
			M_log.error("findUserByEmail(): failed [email = " + email + "]", e);
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
			M_log.error("getUser() failed [eid: " + edit.getEid() + "]", e);
			return false;
		}

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
	public void getUsers(Collection users)
	{

		//TODO: avoid the ripple loading here. should just need one query
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("getUsers(): [Collection size = " + users.size() + "]");
		}

		LDAPConnection conn = null;
		boolean abortiveSearch = false;
		UserEdit userEdit = null;
		try {

			conn = ldapConnectionManager.getConnection();

			for ( Iterator userEdits = users.iterator(); userEdits.hasNext(); ) {

				try {

					userEdit = (UserEdit) userEdits.next();
					boolean foundUser = getUserByEid(userEdit, userEdit.getEid(), conn);
					if ( !(foundUser) ) {
						userEdits.remove();
					}

				} catch ( LDAPException e ) {

					M_log.warn("getUsers(): search failed for user, retrying [eid = " + userEdit.getEid() + "]",
							e);

					// lets retry with a new connection, giving up
					// for good if the retry fails
					ldapConnectionManager.returnConnection(conn);

					conn = ldapConnectionManager.getConnection();

					// exactly the same calls as above
					boolean foundUser = getUserByEid(userEdit, userEdit.getEid(), conn);
					if ( !(foundUser) ) {
						userEdits.remove();
					}

				}

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
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("getUsers(): returning connection to connection manager");
				}
				ldapConnectionManager.returnConnection(conn);
			}

			// no sense in returning a partially complete search result
			if ( abortiveSearch ) {
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("getUsers(): abortive search, clearing received users collection");
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
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("userExists(): [eid = " + eid + "]");
		}

		try {

			return getUserByEid(null, eid, null);

		} catch ( LDAPException e ) {
			M_log.error("userExists() failed: [eid = " + eid + "]", e);
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
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("getUserByEid(): [eid = " + eid + "]");
		}

		LdapUserData cachedUserData = getCachedUserEntry(eid);
		boolean foundCachedUserData = cachedUserData != null;

		if ( foundCachedUserData ) {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("getUserByEid(): found cached user [eid = " + eid + "]");
			}
			return cachedUserData;
		}
		
		if ( !(isSearchableEid(eid)) ) {
			if ( M_log.isInfoEnabled() ) {
				M_log.info("User EID not searchable (possibly blacklisted or otherwise syntactically invalid) [" + eid + "]");
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

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("lookupUserEntryDN(): [eid = " + eid + 
					"][reusing conn = " + (conn != null) + "]");
		}

		LdapUserData foundUserData = getUserByEid(eid, conn);
		if ( foundUserData == null ) {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("lookupUserEntryDN(): no directory entried found [eid = " + 
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

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("searchDirectoryForSingleEntry(): [filter = " + filter + 
					"][reusing conn = " + (conn != null) + "]");
		}

		List results = searchDirectory(filter, conn,
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
	protected List searchDirectory(String filter, 
			LDAPConnection conn,
			LdapEntryMapper mapper,
			String[] searchResultPhysicalAttributeNames,
			String searchBaseDn, 
			int maxResults) 
	throws LDAPException {

		boolean receivedConn = conn != null;

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("searchDirectory(): [filter = " + filter + 
					"][reusing conn = " + receivedConn + "]");
		}

		try {
			if ( !(receivedConn) ) {
				conn = ldapConnectionManager.getConnection();
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
			constraints.setDereference(LDAPSearchConstraints.DEREF_ALWAYS);
			constraints.setTimeLimit(operationTimeout);
			constraints.setReferralFollowing(followReferrals); // TODO: Do we want to make an explicit set optional?
			constraints.setBatchSize(0);
			if ( maxResults > 0 ) {
				constraints.setMaxResults(maxResults);
			}

			if ( M_log.isDebugEnabled() ) {
				M_log.debug("searchDirectory(): [baseDN = " + 
						searchBaseDn + "][filter = " + filter + 
						"][return attribs = " + 
						Arrays.toString(searchResultPhysicalAttributeNames) + 
						"][max results = " + maxResults + "]" +
						"][search scope = " + searchScope + "]");
			}

			LDAPSearchResults searchResults = 
				conn.search(searchBaseDn, 
						searchScope, 
						filter, 
						searchResultPhysicalAttributeNames, 
						false, 
						constraints);

			List mappedResults = new ArrayList();
			int resultCnt = 0;
			while ( searchResults.hasMore() ) {
				LDAPEntry entry = searchResults.next();
				Object mappedResult = mapper.mapLdapEntry(entry, ++resultCnt);
				if ( mappedResult == null ) {
					continue;
				}
				mappedResults.add(mappedResult);
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
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("searchDirectory(): returning connection to connection manager");
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

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("mapLdapEntryOntoUserData() [dn = " + ldapEntry.getDN() + "]");
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

		if ( M_log.isDebugEnabled() ) {
			//  std. UserEdit impl has no meaningful toString() impl
			M_log.debug("mapUserDataOntoUserEdit() [cache record = " + userData + "]");
		}

		// delegate to the LdapAttributeMapper since it knows the most
		// about how the LdapUserData instance was originally populated
		ldapAttributeMapper.mapUserDataOntoUserEdit(userData, userEdit);
		
		// This is not an entirely satisfactory solution, but it's important
		// for all attribute mapping to respect this configuration (SAK-12705),
		// so we centralized the logic rather than rely on swappable attribute 
		// mapping plugins. We decided to override the EID casing when mapping
		// to UserEdits rather than when mapping to LdapUserDatas since we
		// felt it was better to keep the caching of LDAP data and the mapping
		// of that data to Sakai-consumable values as separate concerns.
		//
		// One wonders if a better solution might be to enforce case-sentivity 
		// rules where they matter, though, which is currenty in the UDS.
		if ( !(caseSensitiveCacheKeys) ) {
			userEdit.setEid(toCaseInsensitiveCacheKey(userData.getEid()));
		}
	}

	/**
	 * Retieve a user record from the cache, enforcing TTL rules.
	 * 
	 * @param eid the cache key
	 * @return a user cache record, or null if a cache miss
	 */
	protected LdapUserData getCachedUserEntry(String eid) {

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("getCachedUserEntry(): [eid = " + eid + "]");
		}
		if ( !(caseSensitiveCacheKeys) ) {
			eid = toCaseInsensitiveCacheKey(eid);
		}
		LdapUserData cachedUserEntry = (LdapUserData) userCache.get(eid);
		boolean foundCachedUserEntry = cachedUserEntry != null;
		boolean cachedUserEntryExpired = 
			foundCachedUserEntry && 
			((System.currentTimeMillis() - cachedUserEntry.getTimeStamp()) > cacheTtl);

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("getCachedUserEntry(): cache access [found entry = " + foundCachedUserEntry + 
					"][entry expired = " + cachedUserEntryExpired + "]");
		}

		if ( cachedUserEntryExpired ) {
			userCache.remove(eid);
			return null;
		}

		return cachedUserEntry;

	}

	/**
	 * Add a {@link LdapUserData} object to the cache. Responsible
	 * for the setting the freshness timestamp.
	 * 
	 * @param user the {@link LdapUserData} to add to the cache
	 */
	protected void cacheUserData(LdapUserData user){
		String eid = user.getEid();

		if ( eid == null ) {
			throw new IllegalArgumentException("Attempted to cache a user record without an eid [UserData = " + user + "]");
		}
		user.setTimeStamp(System.currentTimeMillis());

		if ( M_log.isDebugEnabled() ) {
			M_log.debug("cacheUserData(): [user record = " + user + "]");
		}

		if ( !(caseSensitiveCacheKeys) ) {
			eid = toCaseInsensitiveCacheKey(eid);
		}

		userCache.put(eid, user);
	}

	protected String toCaseInsensitiveCacheKey(String eid) {
		if ( eid == null ) {
			return null;
		}
		return eid.toLowerCase();
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
	public String getBasePath()
	{
		return basePath;
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * @return Returns the user entry cache TTL, in millis
	 */
	public long getCacheTTL()
	{
		return cacheTtl;
	}

	/**
	 * @param timeMs
	 *        The user entry cache TTL, in millis.
	 */
	public void setCacheTTL(long timeMs)
	{
		cacheTtl = timeMs;
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
	 * Set the cache key case-sensitivity behavior. Defaults to
	 * {@link #DEFAULT_CASE_SENSITIVE_CACHE_KEYS}. At this writing, 
	 * the cache is keyed exclusively by <code>User.eid</code> values.
	 * 
	 * @see #cacheUserData(LdapUserData)
	 * @see #getCachedUserEntry(String)
	 * @see #defaultLdapEntryMapper
	 * @param caseSensitive
	 */
	public void setCaseSensitiveCacheKeys(boolean caseSensitive) {
		this.caseSensitiveCacheKeys = caseSensitive;	
	}

	/**
	 * Access the cache key case-sensitivity behavior. Defaults to
	 * {@link #DEFAULT_CASE_SENSITIVE_CACHE_KEYS}. At this writing, 
	 * the cache is keyed exclusively by <code>User.eid</code> values.
	 * 
	 * @see #cacheUserData(LdapUserData)
	 * @see #getCachedUserEntry(String)
	 * @see #defaultLdapEntryMapper
	 * @return
	 */
	public boolean isCaseSensitiveCacheKeys() {
		return caseSensitiveCacheKeys;
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
	 * @return
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
	 * @return
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
	
}