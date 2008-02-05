/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

/**
 * 
 * @author dmccallum@unicon.net
 *
 */
public class JLDAPDirectoryProviderTest extends MockObjectTestCase {

	private JLDAPDirectoryProvider provider;
	private EidValidator eidValidator;
	private Mock mockEidValidator;
	private LdapAttributeMapper attributeMapper;
	private Mock mockAttributeMapper;
	private LdapConnectionManager connManager;
	private Mock mockConnManager;
	private LDAPConnection conn;
	private Mock mockConn;
	private LDAPSearchResults searchResults;
	private Mock mockSearchResults;
	private LDAPEntry entry;
	private Mock mockEntry;
	private LdapUserData userData;
	
	protected void setUp() {
		// we need control over the LdapUserData returned from searches so
		// we can do things like assign EID values. Otherwise caching
		// completely blows up.
		userData = new LdapUserData();
		provider = new JLDAPDirectoryProvider() {
			protected LdapUserData newLdapUserData() {
				return userData;
			}
		};
		mockEidValidator = mock(EidValidator.class);
		eidValidator = (EidValidator)mockEidValidator.proxy();
		provider.setEidValidator(eidValidator);
		mockAttributeMapper = mock(LdapAttributeMapper.class);
		attributeMapper = (LdapAttributeMapper)mockAttributeMapper.proxy();
		provider.setLdapAttributeMapper(attributeMapper);
		mockConnManager = mock(LdapConnectionManager.class);
		connManager = (LdapConnectionManager)mockConnManager.proxy();
		provider.setLdapConnectionManager(connManager);
		mockConn = mock(LDAPConnection.class);
		conn = (LDAPConnection) mockConn.proxy();
		mockSearchResults = mock(LDAPSearchResults.class);
		searchResults = (LDAPSearchResults) mockSearchResults.proxy();
		mockEntry = mock(LDAPEntry.class);
		entry = (LDAPEntry)mockEntry.proxy();
		
		mockConnManager.expects(once()).method("setConfig").with(same(provider));
		mockConnManager.expects(once()).method("init");
		provider.init();
	}
	
	public void testRefusesToSearchOnInvalidEids() throws LDAPException {
		final String eid = "some-eid";
		mockEidValidator.expects(once()).method("isSearchableEid").with(eq(eid)).will(returnValue(false));
		assertNull(provider.getUserByEid(eid, null));
	}
	
	public void testAllowsSearchesOnAnyEidIfNoValidatorConfigured() throws LDAPException {
		final String eid = "some-eid";
		provider.setEidValidator(null);
		expectValidUserEidSearch(eid);
		assertNotNull(provider.getUserByEid(eid, null));
	}
	
	public void testAllowsSearchesOnValidEids() throws LDAPException {
		final String eid = "some-eid";
		mockEidValidator.expects(once()).method("isSearchableEid").with(eq(eid)).will(returnValue(true));
		expectValidUserEidSearch(eid);
		assertNotNull(provider.getUserByEid(eid, null));
	}
	
	protected void expectValidUserEidSearch(String eid) {
		final String filter = "(cn=" + eid + ")";
		mockAttributeMapper.expects(once()).method("getFindUserByEidFilter").
			with(eq(eid)).will(returnValue(filter));
		mockConnManager.expects(once()).method("getConnection").will(returnValue(conn));
		mockAttributeMapper.expects(once()).method("getSearchResultAttributes").will(returnValue(null));
		mockConn.expects(once()).method("search").
			// we're not interested in actually testing argument marshaling, so no with()
			will(returnValue(searchResults));
		mockSearchResults.expects(exactly(2)).method("hasMore").
			will(onConsecutiveCalls(returnValue(true), returnValue(false)));
		mockSearchResults.expects(once()).method("next").will(returnValue(entry));
		userData.setEid(eid); // otherwise caching operation will blow up
		mockAttributeMapper.expects(once()).method("mapLdapEntryOntoUserData").
			with(same(entry), same(userData));
		mockConnManager.expects(once()).method("returnConnection").with(same(conn));
	}

	public void testSupportsCaseSensitiveCacheKeys() {
		// some "local constants"
		final String EID = "some-eid";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(true);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(EID);
		
		assertSame("Should have failed to find cached objecct", cachedObject, 
				retrievedObject);
	
	}
	
	
	public void testSupportsCaseSensitiveCacheKeys_Negative() {
		
		// some "local constants"
		final String CACHED_EID = "some-eid";
		final String MIXED_CASE_EID = "sOmE-eId";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(true);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(CACHED_EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(MIXED_CASE_EID);
		
		assertNull("Should have failed to find cached objecct", retrievedObject);
	
	}
	
	public void testSupportsCaseInsensitiveCacheKeys() {
		
		// some "local constants"
		final String CACHED_EID = "some-eid";
		final String MIXED_CASE_EID = "sOmE-eId";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(false);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(CACHED_EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(MIXED_CASE_EID);
		
		assertSame("Should have ignored cache key case", 
				cachedObject, retrievedObject);
		
	}
	
	public void testSupportsCaseInsensitiveCacheKeys_Negative() {
		
		// some "local constants"
		final String CACHED_EID = "some-eid";
		
		// some additional fixture setup
		provider.setCaseSensitiveCacheKeys(false);
		LdapUserData cachedObject = new LdapUserData();
		cachedObject.setEid(CACHED_EID); // dont care about any other attribs
		provider.cacheUserData(cachedObject);
		
		// the code exercise
		LdapUserData retrievedObject = 
			provider.getCachedUserEntry(CACHED_EID + "X");
		
		assertNull("Should have ignored cache key case", retrievedObject);
		
	}
		
}
