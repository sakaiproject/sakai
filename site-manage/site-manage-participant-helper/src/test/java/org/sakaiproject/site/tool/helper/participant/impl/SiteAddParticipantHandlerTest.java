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
 * Focused unit tests for the pasted-input handling in {@link SiteAddParticipantHandler}:
 * the input-format normalization ("line" / "delimited" / "smart"), the non-official
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

    // ---- normalizeDelimited: line mode (default, backward compatible) ----------------------

    @Test
    public void lineModeReturnsRawUnchanged() {
        String raw = "jdoe@yahoo.com,Doe,John\r\nasmith@x.edu";
        // line mode must not touch the text, preserving the email,last,first per-line format
        assertEquals(raw, handler.normalizeDelimited(raw, "line"));
    }

    @Test
    public void nullInputReturnsNullInEveryMode() {
        assertNull(handler.normalizeDelimited(null, "line"));
        assertNull(handler.normalizeDelimited(null, "delimited"));
        assertNull(handler.normalizeDelimited(null, "smart"));
    }

    @Test
    public void unknownModeFallsBackToRaw() {
        String raw = "a@x.com,b@y.com";
        assertEquals(raw, handler.normalizeDelimited(raw, "bogus"));
    }

    // ---- normalizeDelimited: delimited mode ------------------------------------------------

    @Test
    public void delimitedSplitsOnCommas() {
        String out = handler.normalizeDelimited("a@x.com,b@y.com,c@z.com", "delimited");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    @Test
    public void delimitedSplitsOnSemicolons() {
        String out = handler.normalizeDelimited("a@x.com;b@y.com;c@z.com", "delimited");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    @Test
    public void delimitedHandlesMixedDelimitersWhitespaceAndEmptyRuns() {
        // commas, semicolons, spaces, newlines and repeated/trailing delimiters all collapse
        String out = handler.normalizeDelimited("a@x.com, b@y.com;;\n  c@z.com , ", "delimited");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    @Test
    public void delimitedSplitsOnPlainWhitespace() {
        String out = handler.normalizeDelimited("a@x.com b@y.com\tc@z.com", "delimited");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    // ---- normalizeDelimited: smart mode (extract emails from any blob) ----------------------

    @Test
    public void smartExtractsEmailsFromMessyBlob() {
        String blob = "Please add John Doe <jdoe@yahoo.com>, and asmith@x.edu (Alice); "
                + "also \"bob@sub.domain.co.uk\" -- thanks!";
        String out = handler.normalizeDelimited(blob, "smart");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com", "asmith@x.edu", "bob@sub.domain.co.uk"},
                entries(out));
    }

    @Test
    public void smartEmailBlobExtractsEmailsAndDropsStrayUsernames() {
        // the blob contains an '@', so it is detected as an email blob: only email-format tokens
        // are kept (a bare username mixed into an email blob is dropped)
        String out = handler.normalizeDelimited("jsmith, teacher01, real@x.com", "smart");
        assertArrayEquals(new String[] {"real@x.com"}, entries(out));
    }

    @Test
    public void smartUsernameListSplitsOnAnyDelimiter() {
        // no '@' anywhere: detected as a username list and split on any delimiter
        String out = handler.normalizeDelimited("jsmith, teacher01; prof.x  admin1", "smart");
        assertArrayEquals(new String[] {"jsmith", "teacher01", "prof.x", "admin1"}, entries(out));
    }

    @Test
    public void smartDefaultsAreSelected() {
        // Smart parse is the default input format for both boxes
        SiteAddParticipantHandler fresh = new SiteAddParticipantHandler();
        assertEquals("smart", fresh.officialDelimiter);
        assertEquals("smart", fresh.nonOfficialDelimiter);
        assertEquals("auto", fresh.officialAccountType);
    }

    // ---- normalizeNonOfficial: guest box keeps email,lastName,firstName under smart -----------

    @Test
    public void nonOfficialSmartKeepsStructuredRowIntact() {
        // regression: with smart the default, a legacy guest row must NOT be flattened to the email
        String out = handler.normalizeNonOfficial("jdoe@yahoo.com,Doe,John", "smart");
        assertArrayEquals(new String[] {"jdoe@yahoo.com,Doe,John"}, entries(out));
        // and the name fields still survive the downstream per-line parse
        assertArrayEquals(new String[] {"jdoe@yahoo.com", "Doe", "John"},
                handler.parseAccountIntoParts(entries(out)[0]));
    }

    @Test
    public void nonOfficialSmartKeepsMultipleStructuredLines() {
        String out = handler.normalizeNonOfficial("jdoe@yahoo.com,Doe,John\r\nasmith@x.edu,Smith,Alice", "smart");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "asmith@x.edu,Smith,Alice"},
                entries(out));
    }

    @Test
    public void nonOfficialSmartSeparatesPeopleOnSemicolonsKeepingNames() {
        // semicolons separate people; the comma inside each person stays as the field separator
        String out = handler.normalizeNonOfficial("jdoe@yahoo.com,Doe,John; asmith@x.edu,Smith,Alice", "smart");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "asmith@x.edu,Smith,Alice"},
                entries(out));
    }

    @Test
    public void nonOfficialSmartSplitsBareEmailBlob() {
        // no names: a comma/whitespace list of addresses on one line becomes one email per line
        String out = handler.normalizeNonOfficial("a@x.com, b@y.com c@z.com", "smart");
        assertArrayEquals(new String[] {"a@x.com", "b@y.com", "c@z.com"}, entries(out));
    }

    @Test
    public void nonOfficialSmartHandlesMixOfStructuredRowsAndBlobLine() {
        String out = handler.normalizeNonOfficial(
                "jdoe@yahoo.com,Doe,John\r\na@x.com, b@y.com", "smart");
        assertArrayEquals(
                new String[] {"jdoe@yahoo.com,Doe,John", "a@x.com", "b@y.com"},
                entries(out));
    }

    @Test
    public void nonOfficialNonSmartModesDeferToNormalizeDelimited() {
        String raw = "jdoe@yahoo.com,Doe,John\r\nasmith@x.edu";
        assertEquals(handler.normalizeDelimited(raw, "line"),
                handler.normalizeNonOfficial(raw, "line"));
        assertEquals(handler.normalizeDelimited(raw, "delimited"),
                handler.normalizeNonOfficial(raw, "delimited"));
    }

    @Test
    public void nonOfficialNullReturnsNull() {
        assertNull(handler.normalizeNonOfficial(null, "smart"));
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

    // ---- effectiveOfficialMode: Account type vs input format interaction --------------------

    @Test
    public void usernameWithSmartFallsBackToDelimited() {
        // smart extraction is email-regex based and cannot match bare usernames
        assertEquals("delimited", handler.effectiveOfficialMode("username", "smart"));
    }

    @Test
    public void otherAccountTypesKeepChosenInputFormat() {
        assertEquals("smart", handler.effectiveOfficialMode("auto", "smart"));
        assertEquals("smart", handler.effectiveOfficialMode("email", "smart"));
        assertEquals("line", handler.effectiveOfficialMode("username", "line"));
        assertEquals("delimited", handler.effectiveOfficialMode("username", "delimited"));
        assertEquals("delimited", handler.effectiveOfficialMode("auto", "delimited"));
    }

    // ---- end-to-end of the parsing seam: smart extraction feeds email-only accounts ---------

    @Test
    public void smartExtractionProducesValidatableEmails() {
        String blob = "team: alice@x.edu; bob@y.org,  carol@z.net";
        for (String eid : entries(handler.normalizeDelimited(blob, "smart"))) {
            assertTrue("expected extracted token to validate: " + eid, handler.isValidMail(eid));
        }
    }
}
