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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.sakaiproject.user.api.UserEdit;

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

	public void testGetUserDispatch() {
		// special treatment of the actual test impl so it
		// can be reused for other provider config tests, e.g
		// testDisallowingAuthenticationStillAllowsUserLookup()
		doTestGetUserDispatch(null);
	}
	
	protected void doTestGetUserDispatch(Runnable providerConfigCallback) {
		final Mock mockDoGetUserByEid = mock(VarargsMethod.class);
        final VarargsMethod doGetUserByEid = (VarargsMethod)mockDoGetUserByEid.proxy();
		provider = new JLDAPDirectoryProvider() {
			protected boolean getUserByEid(UserEdit userToUpdate, String eid, LDAPConnection conn) 
			throws LDAPException {
				return (Boolean)doGetUserByEid.call(userToUpdate, eid, conn);
			}
		};
		if ( providerConfigCallback != null ) {
			providerConfigCallback.run();
		}
		UserEditStub userEdit = new UserEditStub();
		userEdit.setEid("some-eid");
		mockDoGetUserByEid.expects(once()).method("call").
			with(eq(new Object[] {userEdit, userEdit.getEid(), null})).
			will(returnValue(Boolean.TRUE));
		assertTrue(provider.getUser(userEdit));
		mockDoGetUserByEid.verify();
	}

	public void testGetUserByEidDispatch() throws LDAPException {
		final Mock mockDoGetUserByEid = mock(VarargsMethod.class);
        final VarargsMethod doGetUserByEid = (VarargsMethod)mockDoGetUserByEid.proxy();
        final Mock mockDoMapUserDataOntoUserEdit = mock(VarargsMethod.class);
        final VarargsMethod doMapUserDataOntoUserEdit = (VarargsMethod)mockDoMapUserDataOntoUserEdit.proxy();
		provider = new JLDAPDirectoryProvider() {
			protected LdapUserData getUserByEid(String eid, LDAPConnection conn) 
			throws LDAPException {
				return (LdapUserData)doGetUserByEid.call(eid, conn);
			}
			protected void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit) {
				doMapUserDataOntoUserEdit.call(userData,userEdit);			
			}
		};
		UserEditStub userEdit = new UserEditStub();
		userEdit.setEid("some-eid");
		LdapUserData userData = new LdapUserData();
		userData.setEid(userEdit.getEid());
		mockDoGetUserByEid.expects(once()).method("call")
			.with(eq(new Object[] { userEdit.getEid(), conn }))
			.will(returnValue(userData));
		// this mapUserDataOntoUserEdit() expectation is important for 
		// guaranteeing the validity of tests like 
		// testForcesEIDsToUniformCaseIfConfiguredForCaseInsensitiveCacheKeys(),
		// i.e. that test is only valid b/c we ensure here that 
		// mapUserDataOntoUserEdit() is invoked for all user lookups via
		// getUserByEid(). Other test methods ensure it is invoked in other
		// code paths.
		mockDoMapUserDataOntoUserEdit.expects(once()).method("call")
			.with(eq(new Object[] {userData, userEdit}))
			.after(mockDoGetUserByEid, "call");
		assertTrue(provider.getUserByEid(userEdit, userEdit.getEid(), conn));
		mockDoGetUserByEid.verify();
		mockDoMapUserDataOntoUserEdit.verify();
	}
	
	/**
	 * Verifies the <code>getUserByEid()</code> overload which is
	 * just maps LDAP attributes to a cacheable bean but which does
	 * not map those bean attributes onto a <code>UserEdit</code>
	 * @throws LDAPException 
	 */
	public void testNonUserEditMappingGetUserByEidDispatch() throws LDAPException {
		final Mock mockDoIsSearchableEid = mock(VarargsMethod.class);
		final VarargsMethod doIsSearchableEid = (VarargsMethod)mockDoIsSearchableEid.proxy();
		final Mock mockDoSearchDirectoryForSingleEntry = mock(VarargsMethod.class);
		final VarargsMethod doSearchDirectoryForSingleEntry = (VarargsMethod)mockDoSearchDirectoryForSingleEntry.proxy();
		provider = new JLDAPDirectoryProvider() {
			protected boolean isSearchableEid(String eid) {
				return (Boolean)doIsSearchableEid.call(eid);
			}
			protected Object searchDirectoryForSingleEntry(String filter, 
					LDAPConnection conn,
					LdapEntryMapper mapper,
					String[] searchResultPhysicalAttributeNames,
					String searchBaseDn)
			throws LDAPException {
				return doSearchDirectoryForSingleEntry.call(filter,conn,mapper,searchResultPhysicalAttributeNames,searchBaseDn);
			}
		};
		provider.setLdapAttributeMapper(attributeMapper);
		
		String eid = "some-eid";
		String eidFilter = "(uid=" + eid + ")";
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		mockDoIsSearchableEid.expects(once()).method("call").
			with(eq(new Object[] {eid})).
			will(returnValue(Boolean.TRUE));
		mockAttributeMapper.expects(once()).method("getFindUserByEidFilter").
			with(eq(eid)).after(mockDoIsSearchableEid, "call").
			will(returnValue(eidFilter));
		mockDoSearchDirectoryForSingleEntry.expects(once()).method("call").
			with(eq(new Object[] {eidFilter, conn, null, null, null})).
			after(mockAttributeMapper, "getFindUserByEidFilter").
			will(returnValue(userData));
		
		assertSame(userData, provider.getUserByEid(eid, conn));
		
		mockDoIsSearchableEid.verify();
		mockDoSearchDirectoryForSingleEntry.verify();
	}
	
	/** Removed this test
	 * 
	 * It was previously testing how many time getUsers would call over to the single ldap fetch methods
	 * However, at this point, with the changes, it is actually testing our ability to create a mock object
	 * which represents an ldap connection and an ldap search and ldap entry, which I do not think is actually
	 * testing anything useful. Sure, we can make a bunch of mocks, but this only proves we can make a test
	 * which tests some mocks. Basically, pointless waste of effort. I left the code in here in case someone does
	 * eventually want to complete these mocks. -AZ
	 * 
	public void testGetUsersDispatch() {
		final Mock mockDoGetUserByEid = mock(VarargsMethod.class);
        final VarargsMethod doGetUserByEid = (VarargsMethod)mockDoGetUserByEid.proxy();
		
		provider = new JLDAPDirectoryProvider() {
			protected boolean getUserByEid(UserEdit userToUpdate, String eid, LDAPConnection conn) 
			throws LDAPException {
				return (Boolean) doGetUserByEid.call(userToUpdate, eid, conn);
			}
		};
		provider.setLdapConnectionManager(connManager);
		provider.setMemoryService( new TestMemoryService() );
		mockConnManager.expects(atLeastOnce()).method("returnConnection");
		mockConnManager.expects(atLeastOnce()).method("getConnection").will(returnValue(conn));
		mockConnManager.expects(atLeastOnce()).method("setConfig").with(same(provider));
		mockConnManager.expects(atLeastOnce()).method("init");
		provider.init();
		
		String eid1 = "some-eid-1";
		String eid2 = "some-eid-2";
		UserEdit userEdit1 = new UserEditStub();
		userEdit1.setEid(eid1);
		UserEdit userEdit2 = new UserEditStub();
		userEdit2.setEid(eid2);
		
		Collection<UserEdit> actualUserEdits = new ArrayList<UserEdit>(2);
		actualUserEdits.add(userEdit1);
		actualUserEdits.add(userEdit2);
		Collection<UserEdit> expectedUserEdits = new ArrayList<UserEdit>(actualUserEdits);
		
		LDAPEntry lde = new LDAPEntry();
		// need to mock the ldap entry to make this work
		final Stack<LDAPEntry> stack = new Stack<LDAPEntry>();
		stack.add(lde);
		LDAPSearchResults lsr = new LDAPSearchResults() {
		    @Override
		    public boolean hasMore() {
		        return stack.empty();
	        }
		    @Override
		    public LDAPEntry next() throws LDAPException {
		        return stack.pop();
	        }
		};

		mockConn.expects(once()).method("search").will(returnValue(lsr));
		provider.getUsers(actualUserEdits);
		assertEquals(expectedUserEdits, actualUserEdits);
	}
*******/
		/* OLD tests for above method
		mockConnManager.expects(once()).method("getConnection").will(returnValue(conn));
		mockDoGetUserByEid.expects(once()).method("call").
			with(eq(new Object[] {userEdit1, userEdit1.getEid(), conn})).
			after(mockConnManager, "getConnection").
			will(returnValue(true)).id("call_1");
		mockDoGetUserByEid.expects(once()).method("call").
			with(eq(new Object[] {userEdit2, userEdit2.getEid(), conn})).
			after(mockDoGetUserByEid, "call_1").
			will(returnValue(true)).id("call_2");
		mockConnManager.expects(once()).method("returnConnection").
			with(same(conn)).after(mockDoGetUserByEid, "call_2");
		
		provider.getUsers(actualUserEdits);
		assertEquals(expectedUserEdits, actualUserEdits);
		mockDoGetUserByEid.verify();
		*/
	
	public void testFindUserByEmailDispatch() {
		final Mock mockDoSearchDirectoryForSingleEntry = mock(VarargsMethod.class);
		final VarargsMethod doSearchDirectoryForSingleEntry = (VarargsMethod)mockDoSearchDirectoryForSingleEntry.proxy();
		final Mock mockDoMapUserDataOntoUserEdit = mock(VarargsMethod.class);
        final VarargsMethod doMapUserDataOntoUserEdit = (VarargsMethod)mockDoMapUserDataOntoUserEdit.proxy();
		
		provider = new JLDAPDirectoryProvider() {
			protected Object searchDirectoryForSingleEntry(String filter, 
					LDAPConnection conn,
					LdapEntryMapper mapper,
					String[] searchResultPhysicalAttributeNames,
					String searchBaseDn)
			throws LDAPException {
				return doSearchDirectoryForSingleEntry.call(filter,conn,mapper,searchResultPhysicalAttributeNames,searchBaseDn);
			}
			protected void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit) {
				doMapUserDataOntoUserEdit.call(userData,userEdit);			
			}
		};
		
		assertFalse(attributeMapper instanceof EidDerivedEmailAddressHandler); // sanity check 
		provider.setLdapAttributeMapper(attributeMapper);
		
		String eid = "some-eid";
		String email = "email@university.edu";
		String emailFilter = "(mail=" + email + ")";
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		userData.setEmail(email);
		UserEditStub userEdit = new UserEditStub();
		userEdit.setEid(eid);
		userEdit.setEmail(email);
		mockAttributeMapper.expects(once()).method("getFindUserByEmailFilter").
			with(eq(email)).will(returnValue(emailFilter));
		mockDoSearchDirectoryForSingleEntry.expects(once()).method("call").
			with(eq(new Object[] {emailFilter, null, null, null, null})).
			after(mockAttributeMapper, "getFindUserByEmailFilter").
			will(returnValue(userData));
		// see comments re mapUserDataOntoUserEdit() in testGetUserByEidDispatch()
		mockDoMapUserDataOntoUserEdit.expects(once()).method("call")
			.with(eq(new Object[] {userData, userEdit}));
		
		assertTrue(provider.findUserByEmail(userEdit, email));
		mockDoSearchDirectoryForSingleEntry.verify();
		mockDoMapUserDataOntoUserEdit.verify();
	}
	
	/**
	 * Exists to allow mocking of the {@link LdapAttributeMapper} and
	 * {@link EidDerivedEmailAddressHandler} simultaneously 
	 */
	private interface SyntheticEmailLdapAttributeMapper extends LdapAttributeMapper, EidDerivedEmailAddressHandler {}
	
	public void testFindUserByEidDerivedEmailDispatch() {
		final Mock mockDoGetUserByEid = mock(VarargsMethod.class);
        final VarargsMethod doGetUserByEid = (VarargsMethod)mockDoGetUserByEid.proxy();
		final Mock mockDoMapUserDataOntoUserEdit = mock(VarargsMethod.class);
        final VarargsMethod doMapUserDataOntoUserEdit = (VarargsMethod)mockDoMapUserDataOntoUserEdit.proxy();
        // "override" the default attrib mapper so we can implement two interfaces
        this.mockAttributeMapper = mock(SyntheticEmailLdapAttributeMapper.class);
        this.attributeMapper = (LdapAttributeMapper) this.mockAttributeMapper.proxy();
		
		provider = new JLDAPDirectoryProvider() {
			protected LdapUserData getUserByEid(String eid, LDAPConnection conn) 
			throws LDAPException {
				return (LdapUserData)doGetUserByEid.call(eid, conn);
			}
			protected void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit) {
				doMapUserDataOntoUserEdit.call(userData,userEdit);			
			}
		};
		provider.setLdapAttributeMapper(attributeMapper);
		
		String eid = "some-eid";
		String email = eid + "@university.edu"; // other tests explicitly cause the eid and account ID to differ
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		userData.setEmail(email);
		UserEditStub userEdit = new UserEditStub();
		userEdit.setEid(eid);
		userEdit.setEmail(email);
		mockAttributeMapper.expects(once()).method("unpackEidFromAddress").
			with(eq(email)).will(returnValue(eid));
		mockDoGetUserByEid.expects(once()).method("call")
			.with(eq(new Object[] { userEdit.getEid(), null
					}))
			.will(returnValue(userData));
		// see comments re mapUserDataOntoUserEdit() in testGetUserByEidDispatch()
		mockDoMapUserDataOntoUserEdit.expects(once()).method("call")
			.with(eq(new Object[] {userData, userEdit}));
		
		assertTrue(provider.findUserByEmail(userEdit, email));
		mockDoGetUserByEid.verify();
		mockDoMapUserDataOntoUserEdit.verify();
	}
	
	/**
	 * Verifies that {@link InvalidEmailAddressException}s thrown by a
	 * {@link EidDerivedEmailAddressHandler} are trapped and cause the 
	 * operation to fall back to "standard" find-by-email processing. This
	 * allows for situations where email addresses for users in some domain 
	 * are known to the LDAP but host, but others are not.
	 */
	public void testFindUserByEidDerivedEmailDispatchFallsBackToStandardSearchOnInvalidEmailAddressException() {
        // "override" the default attrib mapper so we can implement two interfaces
        this.mockAttributeMapper = mock(SyntheticEmailLdapAttributeMapper.class);
        this.attributeMapper = (LdapAttributeMapper) this.mockAttributeMapper.proxy();
		
        final Mock mockDoSearchDirectoryForSingleEntry = mock(VarargsMethod.class);
		final VarargsMethod doSearchDirectoryForSingleEntry = (VarargsMethod)mockDoSearchDirectoryForSingleEntry.proxy();
		final Mock mockDoMapUserDataOntoUserEdit = mock(VarargsMethod.class);
        final VarargsMethod doMapUserDataOntoUserEdit = (VarargsMethod)mockDoMapUserDataOntoUserEdit.proxy();
		
		provider = new JLDAPDirectoryProvider() {
			protected Object searchDirectoryForSingleEntry(String filter, 
					LDAPConnection conn,
					LdapEntryMapper mapper,
					String[] searchResultPhysicalAttributeNames,
					String searchBaseDn)
			throws LDAPException {
				return doSearchDirectoryForSingleEntry.call(filter,conn,mapper,searchResultPhysicalAttributeNames,searchBaseDn);
			}
			protected void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit) {
				doMapUserDataOntoUserEdit.call(userData,userEdit);			
			}
		};
        
		provider.setLdapAttributeMapper(attributeMapper);
		
		String eid = "some-eid";
		String email = eid + "@university.edu"; // other tests explicitly cause the eid and account ID to differ
		String emailFilter = "(mail=" + email + ")";
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		userData.setEmail(email);
		UserEditStub userEdit = new UserEditStub();
		userEdit.setEid(eid);
		userEdit.setEmail(email);
		
		mockAttributeMapper.expects(once()).method("unpackEidFromAddress").
			with(eq(email)).will(throwException(new InvalidEmailAddressException("Unable to unpack [" + email + "]")));
		mockAttributeMapper.expects(once()).method("getFindUserByEmailFilter").
			with(eq(email)).after(mockAttributeMapper, "unpackEidFromAddress").
			will(returnValue(emailFilter));
		mockDoSearchDirectoryForSingleEntry.expects(once()).method("call").
			with(eq(new Object[] {emailFilter, null, null, null, null})).
			after(mockAttributeMapper, "getFindUserByEmailFilter").
			will(returnValue(userData));
		// see comments re mapUserDataOntoUserEdit() in testGetUserByEidDispatch()
		mockDoMapUserDataOntoUserEdit.expects(once()).method("call")
			.with(eq(new Object[] {userData, userEdit})).
			after(mockDoSearchDirectoryForSingleEntry, "call");
		
		assertTrue(provider.findUserByEmail(userEdit, email));
		mockDoSearchDirectoryForSingleEntry.verify();
		mockDoMapUserDataOntoUserEdit.verify();
	}
	
	/**
	 * Tests a scenario that really shouldn't ever occur if a 
	 * {@link EidDerivedEmailAddressHandler} is well implemented. That is, that
	 * collaborator should never return null from 
	 * {@link EidDerivedEmailAddressHandler#unpackEidFromAddress(String)}, but throw
	 * a {@link InvalidEmailAddressException}. Here we verify that even if the
	 * handler is misbehaved in this way, we act as if a {@link InvalidEmailAddressException}
	 * has been thrown
	 */
	public void testFindUserByEidDerivedEmailDispatchExitsGracefullyOnNullEid() {
		// "override" the default attrib mapper so we can implement two interfaces
        this.mockAttributeMapper = mock(SyntheticEmailLdapAttributeMapper.class);
        this.attributeMapper = (LdapAttributeMapper) this.mockAttributeMapper.proxy();
		
        final Mock mockDoSearchDirectoryForSingleEntry = mock(VarargsMethod.class);
		final VarargsMethod doSearchDirectoryForSingleEntry = (VarargsMethod)mockDoSearchDirectoryForSingleEntry.proxy();
		final Mock mockDoMapUserDataOntoUserEdit = mock(VarargsMethod.class);
        final VarargsMethod doMapUserDataOntoUserEdit = (VarargsMethod)mockDoMapUserDataOntoUserEdit.proxy();
		
		provider = new JLDAPDirectoryProvider() {
			protected Object searchDirectoryForSingleEntry(String filter, 
					LDAPConnection conn,
					LdapEntryMapper mapper,
					String[] searchResultPhysicalAttributeNames,
					String searchBaseDn)
			throws LDAPException {
				return doSearchDirectoryForSingleEntry.call(filter,conn,mapper,searchResultPhysicalAttributeNames,searchBaseDn);
			}
			protected void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit) {
				doMapUserDataOntoUserEdit.call(userData,userEdit);			
			}
		};
        
		provider.setLdapAttributeMapper(attributeMapper);
		
		String eid = "some-eid";
		String email = eid + "@university.edu"; // other tests explicitly cause the eid and account ID to differ
		String emailFilter = "(mail=" + email + ")";
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		userData.setEmail(email);
		UserEditStub userEdit = new UserEditStub();
		userEdit.setEid(eid);
		userEdit.setEmail(email);
		
		mockAttributeMapper.expects(once()).method("unpackEidFromAddress").
			with(eq(email)).will(returnValue(null));
		mockAttributeMapper.expects(once()).method("getFindUserByEmailFilter").
			with(eq(email)).after(mockAttributeMapper, "unpackEidFromAddress").
			will(returnValue(emailFilter));
		mockDoSearchDirectoryForSingleEntry.expects(once()).method("call").
			with(eq(new Object[] {emailFilter, null, null, null, null})).
			after(mockAttributeMapper, "getFindUserByEmailFilter").
			will(returnValue(userData));
		// see comments re mapUserDataOntoUserEdit() in testGetUserByEidDispatch()
		mockDoMapUserDataOntoUserEdit.expects(once()).method("call")
			.with(eq(new Object[] {userData, userEdit})).
			after(mockDoSearchDirectoryForSingleEntry, "call");
		
		assertTrue(provider.findUserByEmail(userEdit, email));
		mockDoSearchDirectoryForSingleEntry.verify();
		mockDoMapUserDataOntoUserEdit.verify();
	}
	
	/**
	 * Very similar to {@link #testFindUserByEidDerivedEmailDispatchExitsGracefullyOnInvalidEmailAddressException()},
	 * but checks for null return values rather than exceptional exits.
	 */
	public void testFindUserByEidDerivedEmailDispatchExitsGracefullyOnNullSearchResults() {
		// "override" the default attrib mapper so we can implement two interfaces
        this.mockAttributeMapper = mock(SyntheticEmailLdapAttributeMapper.class);
        this.attributeMapper = (LdapAttributeMapper) this.mockAttributeMapper.proxy();
        
        provider = new JLDAPDirectoryProvider() {
			protected LdapUserData getUserByEid(String eid, LDAPConnection conn) 
			throws LDAPException {
				return null;
			}
		};
		provider.setLdapAttributeMapper(attributeMapper);
		
		String eid = "some-eid";
		String email = eid + "@university.edu"; // other tests explicitly cause the eid and account ID to differ
		UserEditStub userEdit = new UserEditStub();
		userEdit.setEid(eid);
		userEdit.setEmail(email);
		mockAttributeMapper.expects(once()).method("unpackEidFromAddress").
			with(eq(email)).will(returnValue(eid));
		assertFalse(provider.findUserByEmail(userEdit, email));
	}
	
	public void testAuthenticateUserDispatch() {
		final Mock mockLookupUserBindDn = mock(VarargsMethod.class);
		final VarargsMethod doLookupUserBindDn = (VarargsMethod)mockLookupUserBindDn.proxy();
		provider = new JLDAPDirectoryProvider() {
			protected String lookupUserBindDn(String eid, LDAPConnection conn) 
			throws LDAPException {
				return (String)doLookupUserBindDn.call(eid, conn);
			}
		};
		
		provider.setLdapConnectionManager(connManager);
		
		final String eid = "some-eid";
		final String dn = "cn=some-cn, ou=some-ou";
		final String password = "some-password";
		final UserEdit userEdit = new UserEditStub();
		userEdit.setEid(eid);
		mockConnManager.expects(once()).method("getConnection").will(returnValue(conn));
		mockLookupUserBindDn.expects(once()).method("call").
			with(eq(new Object[] {eid, conn})).
			after(mockConnManager, "getConnection").
			will(returnValue(dn));
		mockConnManager.expects(once()).method("returnConnection").with(same(conn)).
			after(mockLookupUserBindDn, "call");
		mockConnManager.expects(once()).method("getBoundConnection").
			with(eq(dn), eq(password)).after(mockConnManager, "returnConnection").
			will(returnValue(conn));
		mockConnManager.expects(once()).method("returnConnection").with(same(conn)).
			after(mockConnManager, "getBoundConnection");
		
		// implicitly tests that allowAuthentication defaults to true
		assertTrue(provider.authenticateUser(eid, userEdit, password));
		mockLookupUserBindDn.verify();
	}
	
	public void testDisallowingAuthenticationShortCircuitsAuthenticateUser() {
		provider.setAllowAuthentication(false);
		// the UserEdit arg could be null, but we go ahead and pass one
		// to ensure we're not accidentally testing the wrong behavior
		final String eid = "some-eid";
		final String password = "some-password";
		UserEdit userEdit = new UserEditStub();
		userEdit.setEid(eid);
		assertFalse("Should have refused to authenticate user",
				provider.authenticateUser(eid, userEdit, password));
		// we rely on mocks reacting angrily if the authentication attempt
		// actually attempts any sort of LDAP interaction
	}
	
	public void testDisallowingAuthenticationStillAllowsUserLookup() {
		doTestGetUserDispatch(new Runnable() {
			public void run() {
				provider.setAllowAuthentication(false);
			}
		});
	}
	
	public void testSetAuthenticateAllowedAliasesSetAllowAuthentication() {
		boolean prevState = provider.isAllowAuthentication();
		provider.setAuthenticateAllowed(!(prevState));
		boolean newState = provider.isAllowAuthentication();
		assertFalse(prevState == newState);
	}
	
	public void testSetAuthenticateWithProviderFirst() {
		final String eid1 = "some-eid-1";
		final String eid2 = "some-eid-2";
		provider.setAuthenticateWithProviderFirst(true);
		assertTrue(provider.authenticateWithProviderFirst(eid1));
		assertTrue(provider.authenticateWithProviderFirst(eid2));
		provider.setAuthenticateWithProviderFirst(false);
		assertFalse(provider.authenticateWithProviderFirst(eid1));
		assertFalse(provider.authenticateWithProviderFirst(eid2));
	}
	
	
	
	protected EidAssignmentStub setEidOnReceivedUserEdit() {
		return new EidAssignmentStub();
	}
	
	private static class EidAssignmentStub implements Stub {
		
        public Object invoke(Invocation invocation) throws Throwable {
        	LdapUserData eidSource = (LdapUserData)invocation.parameterValues.get(0);
        	UserEdit userToSetEidOn = (UserEdit)invocation.parameterValues.get(1);
        	userToSetEidOn.setEid(eidSource.getEid());
        	return null;
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            return buffer.append("assigns a LdapUserData's EID to a UserEdit");
        }
        
    }
}
