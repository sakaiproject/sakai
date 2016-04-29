package org.sakaiproject.unboundid;

import com.unboundid.ldap.sdk.*;
import org.junit.Rule;
import org.junit.Test;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

import java.util.Arrays;

import static org.junit.Assert.*;

public class UnboundidProviderTest {

    public static final String DOMAIN_DSN = "dc=sakaiproject,dc=org";
    @Rule
    public EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
            .newInstance()
            .usingDomainDsn(DOMAIN_DSN)
            .importingLdifs("example.ldif")
            .build();

    @Test
    public void testLdapConnection() throws Exception {
        final LDAPInterface ldapConnection = embeddedLdapRule.ldapConnection();
        final SearchResult searchResult = ldapConnection.search(DOMAIN_DSN, SearchScope.SUB, "(objectClass=person)");
        assertEquals(1, searchResult.getEntryCount());
    }

    @Test
    public void testRawLdapConnection() throws Exception {
        final String commonName = "Test person";
        final String dn = String.format(
                "cn=%s,ou=people,dc=sakaiproject,dc=org",
                commonName);
        LDAPConnection ldapConnection = embeddedLdapRule.unsharedLdapConnection();
        try {
            ldapConnection.add(new AddRequest(dn, Arrays.asList(
                    new Attribute("objectclass", "top", "person", "organizationalPerson", "inetOrgPerson"),
                    new Attribute("cn", commonName), new Attribute("sn", "Person"), new Attribute("uid", "test"))));
        } finally {
            // Forces the LDAP connection to be closed. This is not necessary as the rule will usually close it for you.
            ldapConnection.close();
        }
        ldapConnection = embeddedLdapRule.unsharedLdapConnection();
        final SearchResultEntry entry = ldapConnection.searchForEntry(new SearchRequest(dn,
                                                                                        SearchScope.BASE,
                                                                                        "(objectClass=person)"));
        assertNotNull(entry);
    }

    @Test
    public void testEmbeddedServerPort() throws Exception {
        assertTrue(embeddedLdapRule.embeddedServerPort() > 0);

    }

    @Test(expected = IllegalStateException.class)
    public void testNoPortAssignedYet() throws Exception {
        final EmbeddedLdapRule embeddedLdapRule = new EmbeddedLdapRuleBuilder().build();
        embeddedLdapRule.embeddedServerPort();

    }
}
