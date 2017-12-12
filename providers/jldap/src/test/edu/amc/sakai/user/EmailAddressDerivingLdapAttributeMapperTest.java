/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

import lombok.extern.slf4j.Slf4j;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;

@Slf4j
public class EmailAddressDerivingLdapAttributeMapperTest extends MockObjectTestCase {

	private EmailAddressDerivingLdapAttributeMapper attribMapper;

	protected void setUp() throws Exception {
		attribMapper = new EmailAddressDerivingLdapAttributeMapper();
		super.setUp();
	}
	
	public void testValidatesEmailAddressesAgainstInjectedPattern() {
		attribMapper.setAddressPattern("(.*?@domain1.com$)|(.*?@domain2.com$)");
		assertTrue(attribMapper.validateAddress("acctId@domain1.com"));
		assertTrue(attribMapper.validateAddress("acctId@domain2.com"));
		assertFalse(attribMapper.validateAddress("acctId@domain3.com"));
		assertFalse(attribMapper.validateAddress("acctId@domain1"));
		assertTrue(attribMapper.validateAddress("@domain1.com"));
	}
	
	public void testValidatesAnyEmailAddressIfNoPatternInjected() {
		attribMapper.setAddressPattern(null);
		assertTrue(attribMapper.validateAddress("acctId@domain1.com"));
		assertTrue(attribMapper.validateAddress("acctId@domain2.com"));
		assertTrue(attribMapper.validateAddress("acctId@domain3.com"));
		assertTrue(attribMapper.validateAddress("acctId@domain1"));
		assertTrue(attribMapper.validateAddress("@domain1.com"));
	}
	
	public void testStripsDelimAndDomainToUnpackEid() {
		assertEquals("acctId", attribMapper.doUnpackEidFromAddress("acctId@domain1.com"));
		assertEquals("acctId", attribMapper.doUnpackEidFromAddress("acctId@domain2.com"));
		assertEquals("acctId", attribMapper.doUnpackEidFromAddress("acctId@domain1"));
		assertEquals("acctId", attribMapper.doUnpackEidFromAddress("acctId@domain1."));
		assertEquals("acctId", attribMapper.doUnpackEidFromAddress("acctId@"));
		assertEquals("acctId", attribMapper.doUnpackEidFromAddress("acctId"));
		
	}
	
	public void testUnpackEidFromAddressDispatch() {
		final Mock mockValidateAddress = mock(VarargsMethod.class);
        final VarargsMethod doValidateAddress = (VarargsMethod)mockValidateAddress.proxy();
        final Mock mockDoUnpackEid = mock(VarargsMethod.class);
        final VarargsMethod doDoUnpackEid = (VarargsMethod) mockDoUnpackEid.proxy();
		attribMapper = new EmailAddressDerivingLdapAttributeMapper() {
			@Override
			protected boolean validateAddress(String addr) {
				return (Boolean)doValidateAddress.call(addr);
			}
			@Override
			protected String doUnpackEidFromAddress(String addr) {
				return (String)doDoUnpackEid.call(addr);
			}
		};
		final String eid = "acctId";
		final String email = eid + "@domain1.com";
		mockValidateAddress.expects(once()).method("call").
			with(eq(new Object[] {email})).will(returnValue(true));
		mockDoUnpackEid.expects(once()).method("call").
			with(eq(new Object[] {email})).after(mockValidateAddress, "call").
			will(returnValue(eid));
		assertEquals(eid, attribMapper.unpackEidFromAddress(email));
		mockValidateAddress.verify();
		mockDoUnpackEid.verify();
	}
	
	public void testRaisesExceptionIfEmailAddressDoesNotValidate() {
		attribMapper = new EmailAddressDerivingLdapAttributeMapper() {
			@Override
			protected boolean validateAddress(String addr) {
				return false;
			}
		};
		try {
			attribMapper.unpackEidFromAddress("acctId@domain1.com");
			fail("Should have raised an InvalidEmailAddressException");
		} catch ( InvalidEmailAddressException e ) {
			// success
		}
	}
	
	public void testRaisesExceptionIfEidExtractionReturnsNull() {
		attribMapper = new EmailAddressDerivingLdapAttributeMapper() {
			@Override
			protected boolean validateAddress(String addr) {
				return true;
			}
			@Override
			protected String doUnpackEidFromAddress(String addr) {
				return null;
			}
		};
		try {
			attribMapper.unpackEidFromAddress("acctId@domain1.com");
			fail("Should have raised an InvalidEmailAddressException");
		} catch ( InvalidEmailAddressException e ) {
			// success
		}
	}
	
	public void testRaisesExceptionIfEidExtractionReturnsEmptyString() {
		attribMapper = new EmailAddressDerivingLdapAttributeMapper() {
			@Override
			protected boolean validateAddress(String addr) {
				return true;
			}
			@Override
			protected String doUnpackEidFromAddress(String addr) {
				return "";
			}
		};
		try {
			attribMapper.unpackEidFromAddress("acctId@domain1.com");
			fail("Should have raised an InvalidEmailAddressException");
		} catch ( InvalidEmailAddressException e ) {
			// success
		}
	}
	
	public void testAssignsDerivedEmailAddressToUserData() {
		Mock mockEntry = mock(LDAPEntry.class);
		LDAPEntry entry = (LDAPEntry) mockEntry.proxy();
		attribMapper = new EmailAddressDerivingLdapAttributeMapper() {
			@Override
			protected void setUserDataDn(LDAPEntry entry, LdapUserData targetUserData) {
				// nothing to do
			}
			@Override
			protected String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry) {
				return null;
			}
		};
		
		attribMapper.setDefaultAddressDomain("domain1.com");
		
		mockEntry.expects(once()).method("getAttributeSet").
			will(returnValue(new LDAPAttributeSet())); // simplifies anon overrides above
		
		final String eid = "acctId";
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		userData.setEmail(null); // just to be sure
		try {
		attribMapper.mapLdapEntryOntoUserData(entry, userData); // the code exercise
		} catch ( Throwable t ) {
			log.error(t.getMessage(), t);
		}
		// deriveEmailFromEid() behavior validated below
		assertEquals(userData.getEmail(), attribMapper.deriveAddressFromEid(eid));
		mockEntry.verify();
	}
	
	public void testSkipsEmailAddressDerivationIfUserDataHasEmail() {
		Mock mockEntry = mock(LDAPEntry.class);
		LDAPEntry entry = (LDAPEntry) mockEntry.proxy();
		attribMapper = new EmailAddressDerivingLdapAttributeMapper() {
			@Override
			protected void setUserDataDn(LDAPEntry entry, LdapUserData targetUserData) {
				// nothing to do
			}
			@Override
			protected String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry) {
				return null;
			}
		};
		
		attribMapper.setDefaultAddressDomain("domain1.com");
		
		mockEntry.expects(once()).method("getAttributeSet").
			will(returnValue(new LDAPAttributeSet())); // simplifies anon overrides above
		
		final String eid = "acctId";
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		// deliberately ensure that the default domain has nothing to do with "skipping"
		// email address derivation
		final String existingEmail = eid + "@" + attribMapper.getDefaultAddressDomain() + "X";
		userData.setEmail(existingEmail);
		attribMapper.mapLdapEntryOntoUserData(entry, userData); // the code exercise
		
		// deriveEmailFromEid() behavior validated below
		assertEquals(existingEmail, userData.getEmail());
		mockEntry.verify();
	}
	
	public void testSkipsEmailAddressDerivationIfNoDefaultDomainConfigured() {
		Mock mockEntry = mock(LDAPEntry.class);
		LDAPEntry entry = (LDAPEntry) mockEntry.proxy();
		attribMapper = new EmailAddressDerivingLdapAttributeMapper() {
			@Override
			protected void setUserDataDn(LDAPEntry entry, LdapUserData targetUserData) {
				// nothing to do
			}
			@Override
			protected String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry) {
				return null;
			}
		};
		
		attribMapper.setDefaultAddressDomain(null);
		assertNull(attribMapper.getDefaultAddressDomain()); // paranoia
		
		mockEntry.expects(once()).method("getAttributeSet").
			will(returnValue(new LDAPAttributeSet())); // simplifies anon overrides above
		
		final String eid = "acctId";
		LdapUserData userData = new LdapUserData();
		userData.setEid(eid);
		userData.setEmail(null); // just to be sure
		attribMapper.mapLdapEntryOntoUserData(entry, userData); // the code exercise
		
		// deriveEmailFromEid() behavior validated below
		assertNull(userData.getEmail());
		mockEntry.verify();
	}
	
	public void testDerivesEmailAddressFromUserDataEid() {
		attribMapper.setDefaultAddressDomain("domain1.com");
		assertEquals("acctId@domain1.com", attribMapper.deriveAddressFromEid("acctId"));
	}
	
	public void testDerivesNullEmailAddressFromNullEid() {
		attribMapper.setDefaultAddressDomain("domain1.com");
		assertEquals(null, attribMapper.deriveAddressFromEid(null));
	}
	
	public void testDerivesEmptyEmailAddressFromWhitespaceEid() {
		attribMapper.setDefaultAddressDomain("domain1.com");
		assertEquals(null, attribMapper.deriveAddressFromEid(" "));
	}
	
	public void testDerivesEmptyEmailAddressFromEmptyEid() {
		attribMapper.setDefaultAddressDomain("domain1.com");
		assertEquals(null, attribMapper.deriveAddressFromEid(""));
	}
	
}
