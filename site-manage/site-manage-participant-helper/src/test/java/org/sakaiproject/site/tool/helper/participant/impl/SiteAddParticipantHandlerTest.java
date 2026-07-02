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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /** Fragments the parse flagged as skipped (each becomes a visible alert in the tool). */
    private List<String> skipped;

    /** normalizeSmart, collecting skipped fragments into {@link #skipped}. */
    private String smart(String raw) {
        skipped = new ArrayList<>();
        return handler.normalizeSmart(raw, skipped);
    }

    /** normalizeNonOfficial, collecting skipped fragments into {@link #skipped}. */
    private String nonOfficial(String raw) {
        skipped = new ArrayList<>();
        return handler.normalizeNonOfficial(raw, skipped);
    }

    // ---- normalizeSmart: extract emails from any blob, or split a username list --------------

    @Test
    public void nullInputReturnsNull() {
        assertNull(smart(null));
    }

    @Test
    public void smartExtractsEmailsFromMessyBlob() {
        String blob = "Please add John Doe <jdoe@yahoo.com>, and asmith@x.edu (Alice); "
                + "also \"bob@sub.domain.co.uk\" -- thanks!";
        String out = smart(blob);
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com", "asmith@x.edu", "bob@sub.domain.co.uk"},
                entries(out));
        // prose and display names around the addresses are not "skipped entries"
        assertTrue("expected no skipped fragments, got " + skipped, skipped.isEmpty());
    }

    @Test
    public void smartEmailLineFlagsNameFragmentsAsSkipped() {
        // a lone delimited token sharing a line with an email address is never dropped silently:
        // it is excluded from the entries but flagged so the user gets a visible alert
        String out = smart("jsmith, teacher01, real@x.com");
        assertArrayEquals(new String[] {"real@x.com"}, entries(out));
        assertEquals(Arrays.asList("jsmith", "teacher01"), skipped);
    }

    @Test
    public void smartFlagsMistypedEmailInsteadOfDroppingIt() {
        // regression guard for the silent-drop hole: a typo'd address (missing TLD) must surface
        // as a skipped fragment, not vanish while the rest of the paste is added
        String out = smart("a@x.com\r\njdoe@iu\r\nb@y.com");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com"}, entries(out));
        assertEquals(Arrays.asList("jdoe@iu"), skipped);
    }

    @Test
    public void smartFlagsMistypedEmailSharingALineWithAValidOne() {
        String out = smart("a@x.com jdoe@iu");
        assertArrayEquals(new String[] {"a@x.com"}, entries(out));
        assertEquals(Arrays.asList("jdoe@iu"), skipped);
    }

    @Test
    public void smartConsumesOutlookLastFirstMailboxes() {
        // Outlook recipient pastes: the display name may hold a comma ("Last, First"), quoted or
        // not — the name must be consumed with its <email>, not leak fragments like "Doe"
        String out = smart("\"Doe, John\" <jdoe@x.edu>; Roe, Jane <jroe@x.edu>");
        assertArrayEquals(new String[] {"jdoe@x.edu", "jroe@x.edu"}, entries(out));
        assertTrue("expected no skipped fragments, got " + skipped, skipped.isEmpty());
    }

    @Test
    public void smartUsernameListSplitsOnAnyDelimiter() {
        // no '@' anywhere: detected as a username list and split on any delimiter
        String out = smart("jsmith, teacher01; prof.x  admin1");
        assertArrayEquals(new String[] {"jsmith", "teacher01", "prof.x", "admin1"}, entries(out));
        assertTrue(skipped.isEmpty());
    }

    // ---- email extraction keeps RFC-valid characters ------------------------------------------

    @Test
    public void smartKeepsApostropheLocalParts() {
        // o'brien@x.edu must extract whole: truncating to brien@x.edu could silently add a
        // different, real user
        String out = smart("o'brien@x.edu, d'angelo.jr@y.org");
        assertArrayEquals(new String[] {"o'brien@x.edu", "d'angelo.jr@y.org"}, entries(out));
        assertTrue(skipped.isEmpty());
    }

    @Test
    public void smartDropsSurroundingSingleQuotes() {
        String out = smart("'jdoe@x.edu'");
        assertArrayEquals(new String[] {"jdoe@x.edu"}, entries(out));
        assertTrue(skipped.isEmpty());
    }

    // ---- normalizeSmart: legacy one-entry-per-line formats must keep working -----------------

    @Test
    public void smartKeepsLegacyMixedEmailAndUsernameLines() {
        // regression guard: the pre-smart tool split on line breaks and routed each line by the '@'
        // char, so a mixed list of usernames and emails, one per line, must still add ALL of them
        String out = smart("jsmith\r\njdoe@x.com\r\nteacher01\r\nasmith@y.edu");
        assertArrayEquals(
                new String[] {"jsmith", "jdoe@x.com", "teacher01", "asmith@y.edu"},
                entries(out));
    }

    @Test
    public void smartKeepsLegacyOneUsernamePerLine() {
        String out = smart("jsmith\r\nteacher01\r\nadmin1");
        assertArrayEquals(new String[] {"jsmith", "teacher01", "admin1"}, entries(out));
    }

    @Test
    public void smartKeepsLegacyOneEmailPerLine() {
        String out = smart("a@x.com\r\nb@y.com\r\nc@z.com");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    @Test
    public void smartExtractsMultipleEmailsFromASingleLine() {
        // improvement over legacy: a line with several addresses (any delimiter) is split, not
        // rejected as one invalid entry
        String out = smart("a@x.com b@y.com\r\nc@z.com");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    // ---- normalizeNonOfficial: guest box keeps email,lastName,firstName ----------------------

    @Test
    public void nonOfficialKeepsStructuredRowIntact() {
        // regression: a legacy guest row must NOT be flattened to the email
        String out = nonOfficial("jdoe@yahoo.com,Doe,John");
        assertArrayEquals(new String[] {"jdoe@yahoo.com,Doe,John"}, entries(out));
        // and the name fields still survive the downstream per-line parse
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "Doe", "John"},
                handler.parseAccountIntoParts(entries(out)[0]));
    }

    @Test
    public void nonOfficialKeepsMultipleStructuredLines() {
        String out = nonOfficial("jdoe@yahoo.com,Doe,John\r\nasmith@x.edu,Smith,Alice");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "asmith@x.edu,Smith,Alice"},
                entries(out));
    }

    @Test
    public void nonOfficialSeparatesPeopleOnSemicolonsKeepingNames() {
        // semicolons separate people; the comma inside each person stays as the field separator
        String out = nonOfficial("jdoe@yahoo.com,Doe,John; asmith@x.edu,Smith,Alice");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "asmith@x.edu,Smith,Alice"},
                entries(out));
    }

    @Test
    public void nonOfficialSplitsBareEmailBlob() {
        // no names: a comma/whitespace list of addresses on one line becomes one email per line
        String out = nonOfficial("a@x.com, b@y.com c@z.com");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
        assertTrue(skipped.isEmpty());
    }

    @Test
    public void nonOfficialFlagsMistypedEmailInBlob() {
        // a typo'd address inside a multi-address blob must surface as skipped, not vanish
        String out = nonOfficial("a@x.com, b@y c@z.net");
        assertArrayEquals(new String[] {"a@x.com", "c@z.net"}, entries(out));
        assertEquals(Arrays.asList("b@y"), skipped);
    }

    @Test
    public void nonOfficialKeepsApostropheLocalPartsInBlob() {
        // regression guard: truncating o'brien@x.edu to brien@x.edu would invite a stranger
        String out = nonOfficial("o'brien@x.edu, o'malley@y.edu");
        assertArrayEquals(new String[] {"o'brien@x.edu", "o'malley@y.edu"}, entries(out));
        assertTrue(skipped.isEmpty());
    }

    @Test
    public void nonOfficialHandlesMixOfStructuredRowsAndBlobLine() {
        String out = nonOfficial("jdoe@yahoo.com,Doe,John\r\na@x.com, b@y.com");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "a@x.com", "b@y.com"},
                entries(out));
    }

    @Test
    public void nonOfficialNullReturnsNull() {
        assertNull(nonOfficial(null));
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
        for (String eid : entries(smart(blob))) {
            assertTrue("expected extracted token to validate: " + eid, handler.isValidMail(eid));
        }
    }
}
