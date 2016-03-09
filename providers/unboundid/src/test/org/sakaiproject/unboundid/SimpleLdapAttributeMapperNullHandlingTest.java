package org.sakaiproject.unboundid;

import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPAttribute;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPAttributeSet;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.unboundid.LdapUserData;
import org.sakaiproject.unboundid.SimpleLdapAttributeMapper;

import java.text.MessageFormat;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.sakaiproject.unboundid.AttributeMappingConstants.*;

/**
 * These tests check that the null handling of the mapper is the same when independent of there
 * being a value map set.
 *
 * @author Matthew Buckett
 */
public class SimpleLdapAttributeMapperNullHandlingTest {


	private SimpleLdapAttributeMapper attributeMapper;

	@Before
	public void setUp() {
		attributeMapper = new SimpleLdapAttributeMapper();
	}

	@Test
	public void testFindUserByEidFilterNoValueMap() {
		attributeMapper.init();
		assertEquals("cn=null", attributeMapper.getFindUserByEidFilter(null));
		assertEquals("cn=eid", attributeMapper.getFindUserByEidFilter("eid"));
	}

	@Test
	public void testFindUserByEidFilterWithValueMap() {
		attributeMapper.setValueMappings(Collections.singletonMap(LOGIN_ATTR_MAPPING_KEY,
				new MessageFormat("{0}-example")));
		attributeMapper.init();
		assertEquals("cn=null-example", attributeMapper.getFindUserByEidFilter(null));
		assertEquals("cn=eid-example", attributeMapper.getFindUserByEidFilter("eid"));
	}

	@Test
	public void testFindUserByEmailFilterNoValueMap() {
		attributeMapper.init();
		assertEquals("email=null", attributeMapper.getFindUserByEmailFilter(null));
		assertEquals("email=user@example.com", attributeMapper.getFindUserByEmailFilter("user@example.com"));
	}

	@Test
	public void testFindUserByEmailFilterWithValueMap() {
		attributeMapper.setValueMappings(Collections.singletonMap(EMAIL_ATTR_MAPPING_KEY,
				new MessageFormat("{0}.au")));
		attributeMapper.init();
		assertEquals("email=null.au", attributeMapper.getFindUserByEmailFilter(null));
		assertEquals("email=user@example.com.au", attributeMapper.getFindUserByEmailFilter("user@example.com"));
	}

	@Test
	public void testMapLdapEntryOntoUserDataNoValueMap() {
		attributeMapper.init();
		LDAPAttributeSet attributes = new LDAPAttributeSet();
		attributes.add(new LDAPAttribute(DEFAULT_LOGIN_ATTR));
		attributes.add(new LDAPAttribute(DEFAULT_EMAIL_ATTR, "user@example.com"));
		LDAPEntry ldapEntry = new LDAPEntry("id=user,dc=example,dc=com", attributes);
		LdapUserData userData = new LdapUserData();
		attributeMapper.mapLdapEntryOntoUserData(ldapEntry, userData);
		assertNull(userData.getEid());
		assertEquals("user@example.com", userData.getEmail());
	}

	@Test
	public void testMapLdapEntryOntoUserDataWithValueMap() {
		attributeMapper.setValueMappings(Collections.singletonMap(LOGIN_ATTR_MAPPING_KEY,
				new MessageFormat("{0}@EXAMPLE.COM")));
		attributeMapper.init();
		LDAPAttributeSet attributes = new LDAPAttributeSet();
		attributes.add(new LDAPAttribute(DEFAULT_LOGIN_ATTR));
		attributes.add(new LDAPAttribute(DEFAULT_EMAIL_ATTR, "user@example.com"));
		LDAPEntry ldapEntry = new LDAPEntry("id=user,dc=example,dc=com", attributes);
		LdapUserData userData = new LdapUserData();
		attributeMapper.mapLdapEntryOntoUserData(ldapEntry, userData);
		assertNull(userData.getEid());
		assertEquals("user@example.com", userData.getEmail());
	}
}
