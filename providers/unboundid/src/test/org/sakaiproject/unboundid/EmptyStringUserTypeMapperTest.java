package org.sakaiproject.unboundid;

import org.sakaiproject.unboundid.EmptyStringUserTypeMapper;
import org.sakaiproject.unboundid.SimpleLdapAttributeMapper;

import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;

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
