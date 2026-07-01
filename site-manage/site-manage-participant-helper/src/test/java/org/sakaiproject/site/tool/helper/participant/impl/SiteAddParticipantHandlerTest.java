/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.participant.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Focused unit tests for the pasted-input handling in {@link SiteAddParticipantHandler}: the
 * smart-parse normalization of the official box, the non-official
 * {@code email,lastName,firstName} parsing, and email validation. These methods are pure
 * (no injected services), so the handler is exercised directly.
 */
public class SiteAddParticipantHandlerTest {

    private SiteAddParticipantHandler handler;

    @Before
    public void setUp() {
        handler = new SiteAddParticipantHandler();
    }

    /** Split a normalized blob the same way checkAddParticipant() does, dropping blank entries. */
    private String[] entries(String normalized) {
        return Arrays.stream(normalized.split("\r\n"))
                .map(s -> s.replaceAll("[\t\r\n]", "").trim())
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    // ---- normalizeSmart: extract emails from any blob, or split a username list --------------

    @Test
    public void nullInputReturnsNull() {
        assertNull(handler.normalizeSmart(null));
    }

    @Test
    public void smartExtractsEmailsFromMessyBlob() {
        String blob = "Please add John Doe <jdoe@yahoo.com>, and asmith@x.edu (Alice); "
                + "also \"bob@sub.domain.co.uk\" -- thanks!";
        String out = handler.normalizeSmart(blob);
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com", "asmith@x.edu", "bob@sub.domain.co.uk"},
                entries(out));
    }

    @Test
    public void smartEmailBlobExtractsEmailsAndDropsStrayUsernames() {
        // the blob contains an '@', so it is detected as an email blob: only email-format tokens
        // are kept (a bare username mixed into an email blob is dropped)
        String out = handler.normalizeSmart("jsmith, teacher01, real@x.com");
        assertArrayEquals(new String[] {"real@x.com"}, entries(out));
    }

    @Test
    public void smartUsernameListSplitsOnAnyDelimiter() {
        // no '@' anywhere: detected as a username list and split on any delimiter
        String out = handler.normalizeSmart("jsmith, teacher01; prof.x  admin1");
        assertArrayEquals(new String[] {"jsmith", "teacher01", "prof.x", "admin1"}, entries(out));
    }

    // ---- normalizeNonOfficial: guest box keeps email,lastName,firstName ----------------------

    @Test
    public void nonOfficialKeepsStructuredRowIntact() {
        // regression: a legacy guest row must NOT be flattened to the email
        String out = handler.normalizeNonOfficial("jdoe@yahoo.com,Doe,John");
        assertArrayEquals(new String[] {"jdoe@yahoo.com,Doe,John"}, entries(out));
        // and the name fields still survive the downstream per-line parse
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "Doe", "John"},
                handler.parseAccountIntoParts(entries(out)[0]));
    }

    @Test
    public void nonOfficialKeepsMultipleStructuredLines() {
        String out = handler.normalizeNonOfficial("jdoe@yahoo.com,Doe,John\r\nasmith@x.edu,Smith,Alice");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "asmith@x.edu,Smith,Alice"},
                entries(out));
    }

    @Test
    public void nonOfficialSeparatesPeopleOnSemicolonsKeepingNames() {
        // semicolons separate people; the comma inside each person stays as the field separator
        String out = handler.normalizeNonOfficial("jdoe@yahoo.com,Doe,John; asmith@x.edu,Smith,Alice");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "asmith@x.edu,Smith,Alice"},
                entries(out));
    }

    @Test
    public void nonOfficialSplitsBareEmailBlob() {
        // no names: a comma/whitespace list of addresses on one line becomes one email per line
        String out = handler.normalizeNonOfficial("a@x.com, b@y.com c@z.com");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    @Test
    public void nonOfficialHandlesMixOfStructuredRowsAndBlobLine() {
        String out = handler.normalizeNonOfficial("jdoe@yahoo.com,Doe,John\r\na@x.com, b@y.com");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "a@x.com", "b@y.com"},
                entries(out));
    }

    @Test
    public void nonOfficialNullReturnsNull() {
        assertNull(handler.normalizeNonOfficial(null));
    }

    // ---- parseAccountIntoParts: non-official email,lastName,firstName ------------------------

    @Test
    public void parseEmailOnly() {
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "", ""},
                handler.parseAccountIntoParts("jdoe@yahoo.com"));
    }

    @Test
    public void parseEmailWithLastName() {
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "Doe", ""},
                handler.parseAccountIntoParts("jdoe@yahoo.com,Doe"));
    }

    @Test
    public void parseEmailWithLastAndFirstName() {
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "Doe", "John"},
                handler.parseAccountIntoParts("jdoe@yahoo.com,Doe,John"));
    }

    @Test
    public void parseTrimsWhitespaceAndTrailingDotsOnEmail() {
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "Doe", "John"},
                handler.parseAccountIntoParts(" jdoe@yahoo.com. , Doe , John "));
    }

    @Test
    public void parseKeepsOnlyFirstThreeCommaParts() {
        // split(",", 3): anything past the second comma stays inside the firstName field
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "Doe", "John,Q"},
                handler.parseAccountIntoParts("jdoe@yahoo.com,Doe,John,Q"));
    }

    @Test
    public void parseBlankReturnsNull() {
        assertNull(handler.parseAccountIntoParts("   "));
        assertNull(handler.parseAccountIntoParts(""));
        assertNull(handler.parseAccountIntoParts(null));
    }

    // ---- isValidMail -----------------------------------------------------------------------

    @Test
    public void validEmailsPass() {
        assertTrue(handler.isValidMail("jdoe@yahoo.com"));
        assertTrue(handler.isValidMail("  first.last+tag@sub.domain.co.uk  "));
    }

    @Test
    public void invalidEmailsFail() {
        assertFalse(handler.isValidMail(null));
        assertFalse(handler.isValidMail(""));
        assertFalse(handler.isValidMail("not-an-email"));
        assertFalse(handler.isValidMail("missing@domain"));
        assertFalse(handler.isValidMail("@no-local.com"));
        assertFalse(handler.isValidMail("spaces in@email.com"));
    }

    // ---- end-to-end of the parsing seam: smart extraction feeds email-only accounts ---------

    @Test
    public void smartExtractionProducesValidatableEmails() {
        String blob = "team: alice@x.edu; bob@y.org,  carol@z.net";
        for (String eid : entries(handler.normalizeSmart(blob))) {
            assertTrue("expected extracted token to validate: " + eid, handler.isValidMail(eid));
        }
    }
}
