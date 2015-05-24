/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/FormattedText.java $
 * $Id: FormattedText.java 97738 2011-08-31 17:30:03Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util.impl;

import java.util.regex.Pattern;

import junit.framework.TestCase;
import java.util.regex.Pattern;

import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.BasicConfigurationService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.id.impl.UuidV4IdComponent;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.thread_local.impl.ThreadLocalComponent;
import org.sakaiproject.tool.api.RebuildBreakdownService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.impl.SessionComponent;
import org.sakaiproject.util.BasicConfigItem;
import org.sakaiproject.util.api.FormattedText.Level;

import java.util.regex.Pattern;

public class FormattedTextTest extends TestCase {

	private static final String [] SITE_TITLES = new String[] {
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"Short",
		"Not so long title"
	};
	
	private static final String [] CUT_METHODS = new String[]{"100:0","50:50","0:100","70:30","-1","100:100","a:b"};
	private static final int [] MAX_LENGTHS = new int[]{25,50,15,8,25,50,125};
	private static final String [] CUT_SEPARATORS = new String[]{" ...","...","{..}"," ...","...","{..}","......"};
	
    private static final boolean BLANK_DEFAULT = true;
    public static String TEST1 = "<a href=\"blah.html\" style=\"font-weight:bold;\">blah</a><div>hello there</div>";
    public static String TEST2 = "<span>this is my span</span><script>alert('oh noes, a XSS attack!');</script><div>hello there from a div</div>";
    FormattedTextImpl formattedText;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;

    @Override
    protected void setUp() throws Exception {
        // instantiate the services we need for our test
        final IdManager idManager = new UuidV4IdComponent();
        final ThreadLocalManager threadLocalManager = new ThreadLocalComponent();
        serverConfigurationService = new BasicConfigurationService(); // cannot use home or server methods
        sessionManager = new SessionComponent() {
            @Override
            protected ToolManager toolManager() {
                return null; // not needed for this test
            }
            @Override
            protected ThreadLocalManager threadLocalManager() {
                return threadLocalManager;
            }
            @Override
            protected IdManager idManager() {
                return idManager;
            }
            @Override
            protected RebuildBreakdownService rebuildBreakdownService() {
                return null;
            }

            @Override
            protected ClusterService clusterManager() {
                return null;
            }
        };

        // add in the config so we can test it
        serverConfigurationService.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("content.cleaner.errors.handling", "return", "FormattedTextTest"));

        ComponentManager.testingMode = true;
        // instantiate what we are testing
        formattedText = new FormattedTextImpl();
        formattedText.setServerConfigurationService(serverConfigurationService);
        formattedText.setSessionManager(sessionManager);
        formattedText.setDefaultAddBlankTargetToLinks(BLANK_DEFAULT);

        formattedText.init();
    }

    @Override
    protected void tearDown() throws Exception {
        ComponentManager.shutdown();
    }

    // TESTS

    public void testProcessAnchor() {
        // Check we add the target attribute
        assertEquals("<a  href=\"http://sakaiproject.org/\" target=\"_blank\">", formattedText
                .processAnchor("<a href=\"http://sakaiproject.org/\">"));
    }

    public void testProcessAnchorRelative() {
        // Check we add the target attribute
        assertEquals("<a  href=\"other.html\" target=\"_blank\">", formattedText
                .processAnchor("<a href=\"other.html\">"));
    }

    public void testProcessAnchorMailto() {
        assertEquals("<a  href=\"mailto:someone@example.com\" target=\"_blank\">", formattedText
                .processAnchor("<a href=\"mailto:someone@example.com\">"));
    }

    public void testProcessAnchorName() {
        assertEquals("<a  href=\"#anchor\" target=\"_blank\">", formattedText
                .processAnchor("<a href=\"#anchor\">"));
    }

    public void testRegexTargetMatch() {
        Pattern patternAnchorTagWithOutTarget = formattedText.M_patternAnchorTagWithOutTarget;
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
                formattedText.processAnchor("<a href=\"other.html\">") );
        assertEquals("<a  href=\"other.html\" target=\"_blank\">", 
                formattedText.processAnchor("<a target=\"_blank\" href=\"other.html\">") );

        assertEquals("<a  href=\"other.html\" target=\"_AZ\">", 
                formattedText.processAnchor("<a href=\"other.html\" target=\"_AZ\">"));
        // destroys other attributes though...
        assertEquals("<a  href=\"other.html\" target=\"_AZ\">", 
                formattedText.processAnchor("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">"));

        // method 2 - escapeHtmlFormattedText (saves other A attribs)
        assertEquals("<a href=\"other.html\" target=\"_blank\">link</a>", 
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\">link</a>") );
        assertEquals("<a href=\"other.html\" class=\"azeckoski\" target=\"_blank\">link</a>", 
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" class=\"azeckoski\">link</a>") );
        assertEquals("<b>simple</b><b class=\"AZ\">bold</b>", 
                formattedText.escapeHtmlFormattedText("<b>simple</b><b class=\"AZ\">bold</b>") );

        assertEquals("<a href=\"other.html\" target=\"_AZ\">link</a>", 
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"_AZ\">link</a>") );
        assertEquals("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>", 
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );

        assertEquals("<a href=\"other.html\" class=\"azeckoski\" target=\"_blank\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>", 
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" class=\"azeckoski\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );
        assertEquals("<b>simple</b><b class=\"AZ\">bold</b><a href=\"other.html\" class=\"azeckoski\" target=\"_blank\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>", 
                formattedText.escapeHtmlFormattedText("<b>simple</b><b class=\"AZ\">bold</b><a href=\"other.html\" class=\"azeckoski\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );
    }

    public void testAntisamyProcessFormattedText() {
        // TESTS using the antiSamy library
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = TEST1;
        errorMessages = new StringBuilder();
        formattedText.setDefaultAddBlankTargetToLinks(false);
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        assertNotNull(result);
        assertTrue( result.contains("href=\"blah.html\""));
        //assertFalse( result.contains("style=\"font-weight:bold;\"")); // strips this out
        //assertTrue( result.contains("target=\"_blank\"")); // adds target in
        assertTrue( result.contains("<div>hello there</div>"));
        assertEquals("<a href=\"blah.html\" style=\"font-weight: bold;\">blah</a><div>hello there</div>", result);

        strFromBrowser = TEST1;
        errorMessages = new StringBuilder();
        formattedText.setDefaultAddBlankTargetToLinks(true);
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        assertNotNull(result);
        assertTrue( result.contains("href=\"blah.html\""));
        //assertFalse( result.contains("style=\"font-weight:bold;\"")); // strips this out
        //assertTrue( result.contains("target=\"_blank\"")); // adds target in
        assertTrue( result.contains("<div>hello there</div>"));
        assertEquals("<a href=\"blah.html\" style=\"font-weight: bold;\" target=\"_blank\">blah</a><div>hello there</div>", result);

        strFromBrowser = TEST2;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        assertNotNull(result);
        assertEquals("<span>this is my span</span><div>hello there from a div</div>", result);

        String SVG_BAD = "<div>hello</div><embed allowscriptaccess=\"always\" type=\"image/svg+xml\" src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"></embed>";
        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        assertNotNull(result);
        assertEquals("<div>hello</div>", result);
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
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 50 );
        assertTrue( result.contains("<div"));
        assertFalse( result.contains("<embed"));

        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 50 );
        assertTrue( result.contains("<div"));
        assertFalse( result.contains("<embed"));

        /* CHANGED BEHAVIOR
         * Antisamy strips the entire embed tag
        strFromBrowser = SVG_GOOD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<embed"));
        assertTrue( result.contains("src="));
        assertTrue( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<embed"));
        assertFalse( result.contains("src="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));
         */

    }

    public void testDataAttributes() {
        String[] passTests;
        String[] failTests;
        String[] failResults;
        String result;
        StringBuilder errors;

        String oneKV      = "<span class=\"one\"></span>";
        String twoKV      = "<span class=\"one\" id=\"two\"></span>";
        String selfClose  = "<hr class=\"section\" title=\"Contents\" />";
        String subAttr    = "<span id=\"name\" title=\"http://example.com/data-src\"></span>";
        String subAttrs   = "<span class=\"data-name\" id=\"name\" title=\"http://example.com/\"></span>";

        String repeatK    = "<span class class></span>";
        String repeatKV   = "<span class=\"one\" class=\"two\"></span>";
        String badK       = "<span class=\"foo\" class-></span>";
        String badK2      = "<span class=\"foo\" data-></span>";
        String badK3      = "<span class=\"foo\" data--></span>";
        String badKV      = "<span class=\"foo\" class-=\"one\"></span>";

        String resultRepeatK    = "<span></span>";
        String resultRepeatKV   = "<span class=\"two\"></span>";
        String resultBadK       = "<span class=\"foo\"></span>";
        String resultBadKV      = "<span class=\"foo\"></span>";

        // antisamy will not allow empty attributes OR unknown attributes
        passTests   = new String[] { oneKV, twoKV, selfClose, subAttr, subAttrs };
        failTests   = new String[] { repeatK, badK, badK2, badK3, badKV };
        failResults = new String[] { resultRepeatK, resultBadK, resultBadK, resultBadK, resultBadKV };

        result = formattedText.processFormattedText(repeatKV, new StringBuilder());
        assertEquals(resultRepeatKV, result);

        for (String passTest : passTests) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(passTest, errors);
            assertEquals(passTest+" != "+result, 0, errors.length());
            assertEquals(passTest+" != "+result, passTest, result);
        }

        for (int i = 0; i < failTests.length; i++) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(failTests[i], errors);
            assertEquals(failResults[i]+" != "+result, failResults[i], result);
            assertTrue(failTests[i]+": "+failResults[i]+" != "+result, errors.length() > 10);
        }

        // LEGACY tests
        /*
        passTests   = new String[] { oneK, oneKV, twoK, twoKV, mixed, selfCloseL, subAttr, subAttrs };
        failTests   = new String[] { repeatK, repeatKV, badK, badK2, badK3, badKV };
        failResults = new String[] { resultRepeatKL, resultRepeatKVL, resultBadK, resultBadK, resultBadK, resultBadKV };

        for (String passTest : passTests) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(passTest, errors, true);
            assertEquals(passTest, result);
            assertTrue(errors.length() == 0);
        }

        for (int i = 0; i < failTests.length; i++) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(failTests[i], errors, true);
            assertEquals(failResults[i], result);
            assertTrue(result, errors.length() > 10);
        }
         */
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
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertFalse( result.contains("<embed"));
        assertFalse( result.contains("src="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

        strFromBrowser = SVG_BAD_CAPS;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertFalse( result.contains("<EMBED"));
        assertFalse( result.contains("SRC="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

        /* CDATA is ignored so it will not be cleaned
        String TRICKY = "<div><![CDATA[<EMBED SRC=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></EMBED>]]></div>";
        String CDATA_TRICKY = "<div><![CDATA[<embed src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></embed>]]></div>";

        strFromBrowser = CDATA_TRICKY;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div"));
        assertTrue( result.contains("<embed"));
        assertFalse( result.contains("src="));
        assertFalse( result.contains("data:image/svg+xml;base64"));
        assertFalse( result.contains("<script"));

        strFromBrowser = TRICKY;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
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

        String result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertEquals("A", result);
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
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT2;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT3;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT4;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));

    }

    public void testHighLowNoneScanning() {
        // KNL-1048 KNL-1009
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        String SCRIPT1 = "<div>testing</div><embed src=\"DANGER.swf\"><SCRIPT>alert(\"XSS\");//</SCRIPT>";
        String SCRIPT2 = "<div>testing</div><script>alert(\"XSS\");<BR>";

        // Test KNL-1009
        strFromBrowser = SCRIPT2;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, Level.HIGH);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<script"));

        // check the options
        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, null); // default: high
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));
        assertFalse( result.contains("DANGER"));
        assertFalse( result.contains("<embed"));

        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, Level.HIGH);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));
        assertFalse( result.contains("DANGER"));
        assertFalse( result.contains("<embed"));

        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, Level.LOW);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("<div>testing</div>"));
        assertFalse( result.contains("XSS"));
        assertFalse( result.contains("<SCRIPT"));
        assertTrue( result.contains("DANGER"));
        assertTrue( result.contains("<embed"));

        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, Level.NONE);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<div>testing</div>"));
        assertTrue( result.contains("XSS"));
        assertTrue( result.contains("<SCRIPT"));
        assertTrue( result.contains("DANGER"));
        assertTrue( result.contains("<embed"));
    }

    public void testKNL_1019() {
        // https://jira.sakaiproject.org/browse/KNL-1029
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        String youTubeObject = "<object width=\"560\" height=\"315\"><param name=\"movie\" value=\"http://www.youtube.com/v/1yqVD0swvWU?hl=en_US&amp;version=3&amp;rel=0\"></param><param name=\"allowFullScreen\" value=\"true\"></param><param name=\"allowscriptaccess\" value=\"always\"></param><embed src=\"http://www.youtube.com/v/1yqVD0swvWU?hl=en_US&amp;version=3&amp;rel=0\" type=\"application/x-shockwave-flash\" width=\"560\" height=\"315\" allowscriptaccess=\"always\" allowfullscreen=\"true\"></embed></object>";
        String youTubeIframe = "<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/1yqVD0swvWU?rel=0\" frameborder=\"0\" allowfullscreen></iframe>";
        String youTubeIframeOptions = "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube-nocookie.com/embed/3pAnRKD4raY?rel=0\" frameborder=\"0\" allowfullscreen></iframe>";
        String youTubeCK = "<object data=\"/library/editor/ckextraplugins/movieplayer/StrobeMediaPlayback.swf\" height=\"240\" id=\"movie941276\" type=\"application/x-shockwave-flash\" width=\"320\"><param name=\"movie\" value=\"http://www.youtube.com/v/1yqVD0swvWU\" /><param name=\"FlashVars\" value=\"src=http://www.youtube.com/v/1yqVD0swvWU&amp;showplayer=always&amp;width=320&amp;height=240&amp;showiconplay=true&amp;autoplay=0&amp;plugin_YouTubePlugin=/library/editor/ckextraplugins/movieplayer/YouTubePlugin.swf\" /><param name=\"allowFullScreen\" value=\"true\" /></object>";
        String youTubeSpecialCK = "<object data=\"http://youtu.be/1yqVD0swvWU\" height=\"240\" id=\"movie791812\" type=\"video/x-ms-wmv\" width=\"320\"><param name=\"src\" value=\"http://youtu.be/1yqVD0swvWU\" /><param name=\"autostart\" value=\"0\" /><param name=\"controller\" value=\"true\" /></object>";

        String dangerEmbed = "<div>SAFE</div><object data=\"/access/library/hacked/DANGER.swf\" height=\"240\" id=\"movie941276\" type=\"application/x-shockwave-flash\" width=\"320\"><param name=\"movie\" value=\"http://www.youtube.com/v/1yqVD0swvWU\" /><param name=\"FlashVars\" value=\"src=http://www.youtube.com/v/1yqVD0swvWU&amp;showplayer=always&amp;width=320&amp;height=240&amp;showiconplay=true&amp;autoplay=0&amp;plugin_YouTubePlugin=/library/editor/ckextraplugins/movieplayer/YouTubePlugin.swf\" /><param name=\"allowFullScreen\" value=\"true\" /></object>";
        String dangerLibraryPath = "<div>SAFE</div><object data=\"/library/../access/content/user/myUser/DANGER.swf\" type=\"application/x-shockwave-flash\"><param name=\"FlashVars\" value=\"hacked=true\" /></object>";
        String dangerLibraryPath2 = "<div>SAFE</div><object data=\"/library/happy/../../access/content/user/myUser/DANGER.swf\" type=\"application/x-shockwave-flash\"><param name=\"FlashVars\" value=\"hacked=true\" /></object>";
        String dangerLibraryPath3 = "<div>SAFE</div><object data=\"/access/content/user/myUser/library/test/DANGER.swf\" type=\"application/x-shockwave-flash\"><param name=\"FlashVars\" value=\"hacked=true\" /></object>";
        String dangerLibraryPath4 = "<div>SAFE</div><object data=\"/library\\../access/content/user/myUser/DANGER..swf\" type=\"application/x-shockwave-flash\"><param name=\"FlashVars\" value=\"hacked=true\" /></object>";
        String dangerLibraryPath5 = "<div>SAFE</div><object data=\"/libraryAnyString/path/DANGER.swf\" type=\"application/x-shockwave-flash\"><param name=\"FlashVars\" value=\"hacked=true\" /></object>";
        String dangerLibraryPath6 = "<div>SAFE</div><object data=\"/library/aaa\\..\\..\\access/content/user//myUser/DANGER..swf\" type=\"application/x-shockwave-flash\"><param name=\"FlashVars\" value=\"hacked=true\" /></object>";

        strFromBrowser = youTubeObject;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<object"));
        assertTrue( result.contains("<embed"));
        assertTrue( result.contains("www.youtube.com/v/1yqVD0swvWU"));

        strFromBrowser = youTubeIframe;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<iframe"));
        assertTrue( result.contains("allowfullscreen"));
        assertTrue( result.contains("www.youtube.com/embed/1yqVD0swvWU"));

        strFromBrowser = youTubeIframeOptions;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<iframe"));
        assertTrue( result.contains("allowfullscreen"));
        assertTrue( result.contains("www.youtube-nocookie.com/embed/3pAnRKD4raY"));

        strFromBrowser = youTubeCK;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<object"));
        assertTrue( result.contains("/library/editor/ckextraplugins"));
        assertTrue( result.contains("www.youtube.com/v/1yqVD0swvWU"));

        strFromBrowser = youTubeSpecialCK;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() == 0 );
        assertTrue( result.contains("<object"));
        assertTrue( result.contains("<param"));
        assertTrue( result.contains("youtu.be/1yqVD0swvWU"));

        // test bad stuff
        strFromBrowser = dangerEmbed;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("SAFE"));
        assertFalse( result.contains("<object"));
        assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("SAFE"));
        assertFalse( result.contains("<object"));
        assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath2;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("SAFE"));
        assertFalse( result.contains("<object"));
        assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath3;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("SAFE"));
        assertFalse( result.contains("<object"));
        assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath4;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("SAFE"));
        assertFalse( result.contains("<object"));
        assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath5;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("SAFE"));
        assertFalse( result.contains("<object"));
        assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath6;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertTrue( errorMessages.length() > 10 );
        assertTrue( result.contains("SAFE"));
        assertFalse( result.contains("<object"));
        assertFalse( result.contains("DANGER"));

    }

    public void testNullParams() {
        //KNL-862 test we don't NPE if a null string is passed with Newlines == true - DH
        try {
            formattedText.escapeHtml(null, true);
        } catch (Exception e) {
            fail();
        }
    }

    public void testKNL_1065() {
        // https://jira.sakaiproject.org/browse/KNL-1065
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<span class=\"kaltura-media classValue\" rel=\"relValue::video\"><img src=\"https://cdnsecakmi.kaltura.com/p/999999/imgValue\" /></span>";
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertFalse( errorMessages.length() > 10 );
        assertTrue( result.contains("classValue"));
        assertTrue( result.contains("relValue"));
        assertTrue( result.contains("imgValue"));

        strFromBrowser = "<div class=\"classValue\">divValue<ins>insValue</ins><ins datetime=\"2013-10-29\" cite=\"/url/to/cite.html\">insComplexValue</ins><del>delValue</del></div>";
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertFalse( errorMessages.length() > 10 );
        assertTrue( result.contains("<div "));
        assertTrue( result.contains("<ins>"));
        assertTrue( result.contains("<ins "));
        assertTrue( result.contains("<del>"));
        assertTrue( result.contains("divValue"));
        assertTrue( result.contains("insValue"));
        assertTrue( result.contains("delValue"));
        assertTrue( result.contains("insComplexValue"));
        assertTrue( result.contains("2013-10-29"));
        assertTrue( result.contains("/url/to/cite.html"));
    }

    public void testKNL_1061() {
        // https://jira.sakaiproject.org/browse/KNL-1061
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<div class=\"classValue\">divValue</div><img border=\"0\" data-mathml=\"%3Cmrow%3E%0A%20%20%20%20%20%20%20%20%3Cmo%20selected%3D%22true%22%3E%26frac23%3B%3C/mo%3E%0A%3C/mrow%3E\" id=\"MathMLEq1\" src=\"http://nightly2.sakaiproject.org:8085/access/content/group/mercury/fmath-equation-94BDA89D-E911-283D-53C1-32D6CCE53EB0.png\" />";
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertFalse( errorMessages.length() > 10 );
        assertTrue( result.contains("<div "));
        assertTrue( result.contains("divValue"));
        assertTrue( result.contains("<img"));
        assertTrue( result.contains("data-mathml"));
        assertTrue( result.contains("%20selected%3D"));
    }

    public void testKNL_1071() {
        // https://jira.sakaiproject.org/browse/KNL-1071
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<p><span style=\"background-color:yellow;\">aaa </span><tt>bbb </tt><code>ccc </code><kbd>ddd </kbd><del>eee </del><span dir=\"rtl\">fff </span><cite>ggg</cite></p>";

        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertFalse( errorMessages.length() > 0 );
        assertTrue( result.contains("<tt>"));
        assertTrue( result.contains("ddd"));
        assertTrue( result.contains("<cite>"));
        assertTrue( result.contains("<kbd>"));
        assertTrue( result.contains("<span dir=\"rtl\""));
    }

    public void testKNL_1096() {
        // https://jira.sakaiproject.org/browse/KNL-1096
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<object width=\"560\" height=\"315\"><param name=\"movie\" value=\"//www.youtube.com/v/JNSK0647wJI?version=3&amp;hl=en_US\"></param><param name=\"allowFullScreen\" value=\"true\"></param><param name=\"allowscriptaccess\" value=\"always\"></param><embed src=\"//www.youtube.com/v/JNSK0647wJI?version=3&amp;hl=en_US\" type=\"application/x-shockwave-flash\" width=\"560\" height=\"315\" allowscriptaccess=\"always\" allowfullscreen=\"true\"></embed></object>";

        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        assertNotNull(result);
        assertFalse( errorMessages.length() > 0 );
        assertTrue( result.contains("<object"));
        assertTrue( result.contains("<param"));
        assertTrue( result.contains("value=\"//www.youtube.com/v/JNSK0647wJI"));
        assertTrue( result.contains("src=\"//www.youtube.com/v/JNSK0647wJI"));
    }

    public void testValidateURL() {
        // https://jira.sakaiproject.org/browse/KNL-1100
        boolean result = false;
        result = formattedText.validateURL("http://www.vt.edu/");
        assertTrue(result);
        result = formattedText.validateURL("http://localhost:8080/access/site/634cce7b-96da-4997-90b1-f99ea3c3973d");
        assertTrue(result);
        result = formattedText.validateURL("http://127.0.0.1:8080/access/site/634cce7b-f99ea3c3973d");
        assertTrue(result);
        result = formattedText.validateURL("//www.dr-chuck.edu/");
        assertTrue(result);
        result = formattedText.validateURL("/access/zap123");
        assertTrue(result);
        result = formattedText.validateURL("about:blank");
        assertTrue(result);
        result = formattedText.validateURL("ftp://ftp.umich.edu/");
        assertTrue(result);

        // Some false things...
        result = formattedText.validateURL("www.dr-chuck.com"); 
        assertFalse(result);
        result = formattedText.validateURL("XXXXXXXX");
        assertFalse(result);
    }

    public void testEscapeHrefUrl() {
        // https://jira.sakaiproject.org/browse/KNL-1105
        String output = null;
        output = formattedText.sanitizeHrefURL("http://www.sakaiproject.org/?x=Hello World&y=12");
        assertTrue(output,"http://www.sakaiproject.org/?x=Hello%20World&y=12".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es#\"><script>");
        assertTrue(output,"http://www.abc.es#%22%3E%3Cscript%3E".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es/page#anchor");
        assertTrue(output,"http://www.abc.es/page#anchor".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es?x=2&y=3&#12;");
        assertTrue(output,"http://www.abc.es?x=2&y=312;".equals(output));
        output = formattedText.sanitizeHrefURL("http://nightly2.sakaiproject.org/portal/site/!gateway/page/!gateway-200/\"onmouseover='alert(\"xss\")'\"");
        assertTrue(output,"http://nightly2.sakaiproject.org/portal/site/!gateway/page/!gateway-200/%22onmouseover=%27alert(%22xss%22)%27%22".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es/');alert('yo');");
        assertTrue(output,"http://www.abc.es/%27);alert(%27yo%27);".equals(output));
        output = formattedText.sanitizeHrefURL("about:blank");
        assertTrue(output,"about:blank".equals(output));
        output = formattedText.sanitizeHrefURL("ftp://ftp.umich.edu/");
        assertTrue(output,"ftp://ftp.umich.edu/".equals(output));

        // Try the prefixless ones...
        output = formattedText.sanitizeHrefURL("//www.dr-chuck.com/");
        assertTrue(output,"//www.dr-chuck.com/".equals(output));
        output = formattedText.sanitizeHrefURL("/access/zap123");
        assertTrue(output,"/access/zap123".equals(output));

        // Things that fail
        output = formattedText.sanitizeHrefURL("http://www.abc.es');alert('yo');");
        assertNull(output);
        output = formattedText.sanitizeHrefURL("www.abc.es");
        assertNull(output);
        output = formattedText.sanitizeHrefURL("wwwwwwwwww");
        assertNull(output);
        
        //% charactesrs are valid and sanitize method should maintain them
        output = formattedText.sanitizeHrefURL("http://www.server.com/path%20with%20whitespaces/");
        assertTrue(output,"http://www.server.com/path%20with%20whitespaces/".equals(output));

        output = formattedText.sanitizeHrefURL("http://www.server.com/path%25with%2Epercent/");
        assertTrue(output,"http://www.server.com/path%25with%2Epercent/".equals(output));
        
    }


    public void testBasicUrlMatch() {
        assertEquals("I like <a href=\"http://www.apple.com\">http://www.apple.com</a> and stuff", formattedText.encodeUrlsAsHtml(formattedText.escapeHtml("I like http://www.apple.com and stuff")));
    }

    public void testCanDoSsl() {
        assertEquals("<a href=\"https://sakaiproject.org\">https://sakaiproject.org</a>", formattedText.encodeUrlsAsHtml("https://sakaiproject.org"));
    }

    public void testCanIgnoreTrailingExclamation() {
        assertEquals("Hey, it's <a href=\"http://sakaiproject.org\">http://sakaiproject.org</a>!", formattedText.encodeUrlsAsHtml("Hey, it's http://sakaiproject.org!"));
    }

    public void testCanIgnoreTrailingQuestion() {
        assertEquals("Have you ever seen <a href=\"http://sakaiproject.org\">http://sakaiproject.org</a>? Just wondering.", formattedText.encodeUrlsAsHtml("Have you ever seen http://sakaiproject.org? Just wondering."));
    }

    public void testCanEncodeQueryString() {
        assertEquals("See <a href=\"http://sakaiproject.org/index.php?task=blogcategory&id=181\">http://sakaiproject.org/index.php?task=blogcategory&amp;id=181</a> for more info.", formattedText.encodeUrlsAsHtml(formattedText.escapeHtml("See http://sakaiproject.org/index.php?task=blogcategory&id=181 for more info.")));
    }

    public void testCanTakePortNumber() {
        assertEquals("<a href=\"http://localhost:8080/portal\">http://localhost:8080/portal</a>", formattedText.encodeUrlsAsHtml("http://localhost:8080/portal"));
    }

    public void testCanTakePortNumberAndQueryString() {
        assertEquals("<a href=\"http://www.loco.com:3000/portal?person=224\">http://www.loco.com:3000/portal?person=224</a>", formattedText.encodeUrlsAsHtml("http://www.loco.com:3000/portal?person=224"));
    }

    public void testCanIgnoreExistingHref() {
        assertEquals("<a href=\"http://sakaiproject.org\">Sakai Project</a>", formattedText.encodeUrlsAsHtml("<a href=\"http://sakaiproject.org\">Sakai Project</a>"));
    }

    public void testALongUrlFromNyTimes() {
        assertEquals("<a href=\"http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&symb=LLNW\">http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&amp;symb=LLNW</a>",
                formattedText.encodeUrlsAsHtml(formattedText.escapeHtml("http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&symb=LLNW")));
    }

    public void testHrefWithSpace() {
      StringBuilder errorMessages = new StringBuilder();
      formattedText.setDefaultAddBlankTargetToLinks(false);

      assertEquals("<a href=\"http://localhost:8080/access/content/public/Home Page.htm\">Test Search Space</a>",
          formattedText.processFormattedText("<a href=\"http://localhost:8080/access/content/public/Home Page.htm\">Test Search Space</a>", errorMessages));
      
      assertEquals("<a href=\"https://www.example.com/access/content/public/Home Page.htm\">Test Search Space</a>",
          formattedText.processFormattedText("<a href=\"https://www.example.com/access/content/public/Home Page.htm\">Test Search Space</a>", errorMessages));

      formattedText.setDefaultAddBlankTargetToLinks(BLANK_DEFAULT);
    }

    public void testHrefWithPlus() {
      StringBuilder errorMessages = new StringBuilder();
      formattedText.setDefaultAddBlankTargetToLinks(false);
      
      assertEquals("<a href=\"http://localhost:8080/access/content/public/Home+Page.htm\">Test Search Plus</a>",
          formattedText.processFormattedText("<a href=\"http://localhost:8080/access/content/public/Home+Page.htm\">Test Search Plus</a>", errorMessages));

      assertEquals("<a href=\"https://www.example.com/access/content/public/Home+Page.htm\">Test Search Plus</a>",
          formattedText.processFormattedText("<a href=\"https://www.example.com/access/content/public/Home+Page.htm\">Test Search Plus</a>", errorMessages));

      formattedText.setDefaultAddBlankTargetToLinks(BLANK_DEFAULT);
    }

    public void testKNL_1253() {
        // https://jira.sakaiproject.org/browse/KNL-1253
        String text = null;
        String result = null;

        // no change
        text = null;
        result = formattedText.stripHtmlFromText(text,true);
        assertEquals(text, result);

        text = "";
        result = formattedText.stripHtmlFromText(text,true);
        assertEquals(text, result);

        text = "azeckoski";
        result = formattedText.stripHtmlFromText(text,true);
        assertEquals(text, result);

        // changed
        text = "<b>azeckoski</b>";
        result = formattedText.stripHtmlFromText(text,true);
        assertEquals("azeckoski", result);

        text = "<a href='www.vt.edu'><b>azeckoski</b> is AZ</a>";
        result = formattedText.stripHtmlFromText(text,true);
        assertEquals("azeckoski is AZ", result);

        text = "<table><tr><th>Column1</th></tr><tr><td>Row1</td></tr></table>";
        result = formattedText.stripHtmlFromText(text,true);
        assertEquals("Column1 Row1", result);

        text = "<table><tr><th>Column1</th></tr><tr><td>Row1</td></tr></table>";
        result = formattedText.stripHtmlFromText(text,false);
        assertEquals("Column1Row1", result);
    }

    public void testGetShortenedTitles() {
        for (String siteTitle:SITE_TITLES) {
            for (int k=0; k<CUT_METHODS.length; k++) {
                ServerConfigurationService scs = new BasicConfigurationService();
                scs.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("site.title.cut.method", CUT_METHODS[k], "FormattedTextTest"));
                scs.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("site.title.maxlength", MAX_LENGTHS[k], "FormattedTextTest"));
                scs.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("site.title.cut.separator", CUT_SEPARATORS[k], "FormattedTextTest"));
                formattedText.setServerConfigurationService(scs);
                String resumeTitle = formattedText.makeShortenedText(siteTitle, null, null, null);
                // The resume title has the defined length (if has enough length)
                assertEquals(Math.min(MAX_LENGTHS[k],siteTitle.length()),resumeTitle.length());
                if (siteTitle.length()>MAX_LENGTHS[k] && MAX_LENGTHS[k]>=10) {
                    // The title must to be cut, so it has to contains cut separator
                    assertEquals(true,resumeTitle.contains(CUT_SEPARATORS[k]));
                } else if (siteTitle.length()>MAX_LENGTHS[k]) {
                    // Title truncate
                    assertEquals(siteTitle.trim().substring(0,MAX_LENGTHS[k]),resumeTitle);
                } else {
                    // Title without change
                    assertEquals(siteTitle.trim(),resumeTitle);
                }
            }
        }
    }

}
