/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
