
package org.sakaiproject.util;

import org.sakaiproject.component.cover.ComponentManager;

import junit.framework.TestCase;

public class FormattedTextTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        ComponentManager.testingMode = true;
        
    }

    @Override
    protected void tearDown() throws Exception {
        ComponentManager.shutdown();
    }

    public static String TEST1 = "<a href=\"blah.html\" style=\"font-weight:bold;\">blah</a><div>hello there</div>";
    public static String TEST2 = "<span>this is my span</span><script>alert('oh noes, a XSS attack!');</script><div>hello there from a div</div>";
    public static String TEST3 = "<embed allowscriptaccess=\"always\" type=\"image/svg+xml\" src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"></embed>";

    // TESTS

    public void testProcessAnchor() {
        // Check we add the target attribute
        assertEquals("<a  href=\"http://sakaiproject.org/\" target=\"_blank\">", FormattedText
                .processAnchor("<a href=\"http://sakaiproject.org/\">"));
    }

    public void testProcessAnchorRelative() {
        // Check we add the target attribute
        assertEquals("<a  href=\"other.html\" target=\"_blank\">", FormattedText
                .processAnchor("<a href=\"other.html\">"));
    }

    public void testProcessAnchorMailto() {
        assertEquals("<a  href=\"mailto:someone@example.com\" target=\"_blank\">", FormattedText
                .processAnchor("<a href=\"mailto:someone@example.com\">"));
    }

    public void testProcessAnchorName() {
        assertEquals("<a  href=\"#anchor\" target=\"_blank\">", FormattedText
                .processAnchor("<a href=\"#anchor\">"));
    }

    public void testProcessFormattedText() {
        // TESTS using the antiSamy library
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = TEST1;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, false);
        assertNotNull(result);
        assertTrue( result.contains("href=\"blah.html\""));
        //assertFalse( result.contains("style=\"font-weight:bold;\"")); // strips this out
        //assertTrue( result.contains("target=\"_blank\"")); // adds target in
        assertTrue( result.contains("<div>hello there</div>"));
        assertEquals("<a href=\"blah.html\" style=\"font-weight: bold;\">blah</a>\n<div>hello there</div>", result);

        strFromBrowser = TEST2;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, false);
        assertNotNull(result);
        assertEquals("<span>this is my span</span>\n<div>hello there from a div</div>", result);

        strFromBrowser = TEST3;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, false);
        assertNotNull(result);
        assertEquals("", result);
    }

    public void testLegacyProcessFormattedText() {
        // TESTs using the legacy Sakai library
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = TEST1;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        // NOTE: FT adds a bunch of spaces so it is hard to predict the output
        assertTrue( result.contains("href=\"blah.html\""));
        assertFalse( result.contains("style=\"font-weight:bold;\"")); // strips this out
        assertTrue( result.contains("target=\"_blank\"")); // adds target in
        assertTrue( result.contains("<div>hello there</div>"));
        assertEquals("<a  href=\"blah.html\"  target=\"_blank\" >blah</a><div>hello there</div>", result);

        strFromBrowser = TEST2;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertEquals("<span>this is my span</span>&lt;script&gt;alert('oh noes, a XSS attack!');&lt;/script&gt;<div>hello there from a div</div>", result);

        strFromBrowser = TEST3;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertEquals("<embed src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"  type=\"image/svg+xml\"  allowscriptaccess=\"always\" ></embed>", result);
    }

}
