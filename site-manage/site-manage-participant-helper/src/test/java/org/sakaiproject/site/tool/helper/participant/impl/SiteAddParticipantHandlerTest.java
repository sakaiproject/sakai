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
    public void smartIgnoresNonEmailTokensLikeBareUsernames() {
        // bare usernames have no '@' and are not extracted in smart mode
        String out = handler.normalizeDelimited("jsmith, teacher01, real@x.com", "smart");
        assertArrayEquals(new String[] {"real@x.com"}, entries(out));
    }

    @Test
    public void smartWithNoEmailsYieldsEmpty() {
        assertEquals(0, entries(handler.normalizeDelimited("no addresses here", "smart")).length);
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
        for (String eid : entries(handler.normalizeDelimited(blob, "smart"))) {
            assertTrue("expected extracted token to validate: " + eid, handler.isValidMail(eid));
        }
    }
}
