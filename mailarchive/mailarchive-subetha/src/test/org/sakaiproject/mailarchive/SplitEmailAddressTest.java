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
