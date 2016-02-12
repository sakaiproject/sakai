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
