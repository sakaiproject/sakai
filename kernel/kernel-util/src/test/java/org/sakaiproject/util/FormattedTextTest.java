
package org.sakaiproject.util;

import java.util.regex.Pattern;

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

    public void testRegexTargetMatch() {
        Pattern patternAnchorTagWithOutTarget = FormattedText.M_patternAnchorTagWithOutTarget;
        /*  Pattern.compile("([<]a\\s)(?![^>]*target=)([^>]*?)[>]",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL); */
        assertTrue(patternAnchorTagWithOutTarget.matcher("<a href=\"other.html\">link</a>").find());
        assertFalse(patternAnchorTagWithOutTarget.matcher("<a target=\"AZ\" href=\"other.html\">link</a>").find());
        assertTrue(patternAnchorTagWithOutTarget.matcher("<a href=\"other.html\" class=\"AZ\">link</a>").find());
        assertFalse(patternAnchorTagWithOutTarget.matcher("<a target=\"AZ\" href=\"other.html\">link</a>").find());
        assertFalse(patternAnchorTagWithOutTarget.matcher("<a href=\"other.html\" target=\"AZ\">link</a>").find());
    }

    // KNL-526 - testing that targets are not destroyed or replaced
    public void testTargetNotOverridden() {
        // method 1 - processAnchor (kills all A attribs and only works on the first part of the tag
        assertEquals("<a  href=\"other.html\" target=\"_blank\">", 
                FormattedText.processAnchor("<a href=\"other.html\">") );
        assertEquals("<a  href=\"other.html\" target=\"_blank\">", 
                FormattedText.processAnchor("<a target=\"_blank\" href=\"other.html\">") );

        assertEquals("<a  href=\"other.html\" target=\"_AZ\">", 
                FormattedText.processAnchor("<a href=\"other.html\" target=\"_AZ\">"));
        // destroys other attributes though...
        assertEquals("<a  href=\"other.html\" target=\"_AZ\">", 
                FormattedText.processAnchor("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">"));

        // method 2 - escapeHtmlFormattedText (saves other A attribs)
        assertEquals("<a href=\"other.html\" target=\"_blank\">link</a>", 
                FormattedText.escapeHtmlFormattedText("<a href=\"other.html\">link</a>") );
        assertEquals("<a href=\"other.html\" class=\"azeckoski\" target=\"_blank\">link</a>", 
                FormattedText.escapeHtmlFormattedText("<a href=\"other.html\" class=\"azeckoski\">link</a>") );
        assertEquals("<b>simple</b><b class=\"AZ\">bold</b>", 
                FormattedText.escapeHtmlFormattedText("<b>simple</b><b class=\"AZ\">bold</b>") );

        assertEquals("<a href=\"other.html\" target=\"_AZ\">link</a>", 
                FormattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"_AZ\">link</a>") );
        assertEquals("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>", 
                FormattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );

        assertEquals("<a href=\"other.html\" class=\"azeckoski\" target=\"_blank\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>", 
                FormattedText.escapeHtmlFormattedText("<a href=\"other.html\" class=\"azeckoski\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );
        assertEquals("<b>simple</b><b class=\"AZ\">bold</b><a href=\"other.html\" class=\"azeckoski\" target=\"_blank\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>", 
                FormattedText.escapeHtmlFormattedText("<b>simple</b><b class=\"AZ\">bold</b><a href=\"other.html\" class=\"azeckoski\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );
    }


    // DISABLED TEST
    public void donottestAntisamyProcessFormattedText() {
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

        String SVG_BAD = "<div>hello</div><embed allowscriptaccess=\"always\" type=\"image/svg+xml\" src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"></embed>";
        strFromBrowser = SVG_BAD;
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
    }

    public void testSAK_18269() {
        // http://jira.sakaiproject.org/browse/SAK-18269
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        String SVG_GOOD = "<div>hello</div><embed allowscriptaccess=\"always\" type=\"image/svg+xml\" src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAwIiBpZD0ieHNzIj5pbWFnZTwvc3ZnPg==\"></embed>";
        String SVG_BAD = "<div>hello</div><embed allowscriptaccess=\"always\" type=\"image/svg+xml\" src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"></embed>";

        strFromBrowser = SVG_GOOD;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<embed"));
        assertTrue( result.contains("src="));
        assertTrue( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<embed"));
        assertFalse( result.contains("src="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));
    }

    public void testKNL_528() {
        // http://jira.sakaiproject.org/browse/KNL-528

        String SVG_BAD_CAPS = "<div>hello</div><EMBED ALLOWSCRIPTACCESS=\"always\" type=\"image/svg+xml\" SRC=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"></EMBED>";
        String SVG_BAD = "<div>hello</div><embed allowscriptaccess=\"always\" type=\"image/svg+xml\" src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"></embed>";

        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;
        
        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<embed"));
        assertFalse( result.contains("src="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

        strFromBrowser = SVG_BAD_CAPS;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<EMBED"));
        assertFalse( result.contains("SRC="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

/* CDATA is ignored so it will not be cleaned
        String TRICKY = "<div><![CDATA[<EMBED SRC=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></EMBED>]]></div>";
        String CDATA_TRICKY = "<div><![CDATA[<embed src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></embed>]]></div>";

        strFromBrowser = CDATA_TRICKY;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<embed"));
        assertFalse( result.contains("src="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

        strFromBrowser = TRICKY;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<EMBED"));
        assertFalse( result.contains("SRC="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));
*/
    }

    public void testUnbalancedMarkup() {
    	StringBuilder errorMessages = new StringBuilder();
    	String strFromBrowser = "A<B Test";
        
    	String result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNull(result);
    }

    public void testKNL_579() {
        // http://jira.sakaiproject.org/browse/KNL-579

        String SCRIPT1 = "<div>testing</div><SCRIPT>alert(\"XSS\");//</SCRIPT>";
        String SCRIPT2 = "<div>testing</div><SCRIPT>alert(\"XSS\");//<</SCRIPT>";
        String SCRIPT3 = "<div>testing</div><<SCRIPT>alert(\"XSS\");//<</SCRIPT>";
        String SCRIPT4 = "<div>testing</div><<SCRIPT>>alert(\"XSS\");//<</SCRIPT>";

        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;
        
        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertTrue( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT2;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertTrue( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT3;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertTrue( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT4;
        errorMessages = new StringBuilder();
        result = FormattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertTrue( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

    }

}
