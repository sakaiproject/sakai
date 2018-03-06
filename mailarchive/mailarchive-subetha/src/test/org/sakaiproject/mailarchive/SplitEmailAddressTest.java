/**
 * Copyright (c) 2014-2017 The Apereo Foundation
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
package org.sakaiproject.mailarchive;

import org.junit.Test;
import org.sakaiproject.mailarchive.SplitEmailAddress;

import static org.junit.Assert.assertEquals;

public class SplitEmailAddressTest {

    @Test
    public void testGood() {
        SplitEmailAddress email = SplitEmailAddress.parse("me@example.com");
        assertEquals("me", email.getLocal());
        assertEquals("example.com", email.getDomain());
    }

    @Test
    public void testBATVGood() {
        // Check that our batv parsing works.
        SplitEmailAddress email = SplitEmailAddress.parse("prvs=2987A7B7C7=me@example.com");
        assertEquals("me", email.getLocal());
        assertEquals("example.com", email.getDomain());
    }

    @Test
    public void testBATVBad() {
        // Check that our batv parsing is strict along the lines of the RFC
        SplitEmailAddress email = SplitEmailAddress.parse("prvs=aaaaaaaaaa=me@example.com");
        assertEquals("prvs=aaaaaaaaaa=me", email.getLocal());
        assertEquals("example.com", email.getDomain());
    }

    @Test
    public void testBATVSubAddress() {
        // Check that we also catch the subaddressing style.
        SplitEmailAddress email = SplitEmailAddress.parse("me+prvs=2987A7B7C7@example.com");
        assertEquals("me", email.getLocal());
        assertEquals("example.com", email.getDomain());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoAt() {
        SplitEmailAddress email = SplitEmailAddress.parse("notavalidemail");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBeginWithAt() {
        SplitEmailAddress email = SplitEmailAddress.parse("@example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndWithAt() {
        SplitEmailAddress email = SplitEmailAddress.parse("john.smith@");
    }
}
