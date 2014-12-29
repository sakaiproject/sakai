package edu.amc.sakai.user;

import com.novell.ldap.LDAPEntry;

import junit.framework.TestCase;

public class StringUserTypeMapperTest extends TestCase {
	
	public void testDefaultsToNullString() {
		StringUserTypeMapper mapper = new StringUserTypeMapper();
		assertNull(mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertNull(mapper.getUserType());
	}
	
	public void testEchoesEmptyStringConstructorConfiguration() {
		StringUserTypeMapper mapper = new StringUserTypeMapper("");
		assertEquals("",mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertEquals("",mapper.getUserType());
	}
	
	public void testEchoesNullStringConstructorConfiguration() {
		StringUserTypeMapper mapper = new StringUserTypeMapper(null);
		assertNull(mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertNull(mapper.getUserType());
	}
	
	public void testEchoesConstructorConfiguration() {
		StringUserTypeMapper mapper = new StringUserTypeMapper("some-user-type");
		assertEquals("some-user-type",mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertEquals("some-user-type",mapper.getUserType());
	}
	
	public void testEchoesEmptyStringSetterConfiguration() {
		StringUserTypeMapper mapper = new StringUserTypeMapper("some-user-type");
		mapper.setUserType("");
		assertEquals("",mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertEquals("",mapper.getUserType());
	}
	
	public void testEchoesNullStringSetterConfiguration() {
		StringUserTypeMapper mapper = new StringUserTypeMapper("some-user-type");
		mapper.setUserType(null);
		assertNull(mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertNull(mapper.getUserType());
	}
	
	public void testEchoesSetterConfiguration() {
		StringUserTypeMapper mapper = new StringUserTypeMapper("some-user-type");
		mapper.setUserType("some-other-user-type");
		assertEquals("some-other-user-type",mapper.mapLdapEntryToSakaiUserType(new LDAPEntry(), new SimpleLdapAttributeMapper()));
		assertEquals("some-other-user-type",mapper.getUserType());
	}

}
