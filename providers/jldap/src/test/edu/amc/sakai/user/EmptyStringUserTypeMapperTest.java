package edu.amc.sakai.user;

import com.novell.ldap.LDAPEntry;

import junit.framework.TestCase;

public class EmptyStringUserTypeMapperTest extends TestCase {

	public void testDefaultsToEmptyStringConfiguration() {
		EmptyStringUserTypeMapper mapper = new EmptyStringUserTypeMapper();
		assertEquals("", mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertEquals("", mapper.getUserType());
	}
	
	public void testIgnoresSetUserTypeCalls() {
		EmptyStringUserTypeMapper mapper = new EmptyStringUserTypeMapper();
		mapper.setUserType("some-user-type");
		assertEquals("", mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertEquals("", mapper.getUserType());
	}
	
}
