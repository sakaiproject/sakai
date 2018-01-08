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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class FormattedTextTest {

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
    public static String TEST3 = "<a href=\"blah.html\" style=\"font-weight:bold;\" target=\"_blank\">blah</a><div>hello there</div>";
    FormattedTextImpl formattedText;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;

    @Before
    public void setUp() throws Exception {
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
        serverConfigurationService.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("content.cleaner.referrer-policy", "noopener", "FormattedTextTest"));

        ComponentManager.testingMode = true;
        // instantiate what we are testing
        formattedText = new FormattedTextImpl();
        formattedText.setServerConfigurationService(serverConfigurationService);
        formattedText.setSessionManager(sessionManager);
        formattedText.setDefaultAddBlankTargetToLinks(BLANK_DEFAULT);

        formattedText.init();
    }

    @After
    public void tearDown() throws Exception {
        ComponentManager.shutdown();
    }

    // TESTS
    @Test
    public void testProcessAnchor() {
        // Check we add the target attribute
    	Assert.assertEquals("<a  href=\"http://sakaiproject.org/\" target=\"_blank\" rel=\"noopener\">", formattedText
                .processAnchor("<a href=\"http://sakaiproject.org/\">"));
    }

    @Test
    public void testProcessAnchorRelative() {
        // Check we add the target attribute
    	Assert.assertEquals("<a  href=\"other.html\" target=\"_blank\" rel=\"noopener\">", formattedText
                .processAnchor("<a href=\"other.html\">"));
    }

    @Test
    public void testProcessAnchorMailto() {
    	Assert.assertEquals("<a  href=\"mailto:someone@example.com\" target=\"_blank\" rel=\"noopener\">", formattedText
                .processAnchor("<a href=\"mailto:someone@example.com\">"));
    }

    @Test
    public void testProcessAnchorName() {
    	Assert.assertEquals("<a  href=\"#anchor\" target=\"_blank\" rel=\"noopener\">", formattedText
                .processAnchor("<a href=\"#anchor\">"));
    }

    @Test
    public void testRegexTargetMatch() {
        Pattern patternAnchorTagWithOutTarget = formattedText.M_patternAnchorTagWithOutTarget;
        /*  Pattern.compile("([<]a\\s)(?![^>]*target=)([^>]*?)[>]",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL); */
        Assert.assertTrue(patternAnchorTagWithOutTarget.matcher("<a href=\"other.html\">link</a>").find());
        Assert.assertFalse(patternAnchorTagWithOutTarget.matcher("<a target=\"AZ\" href=\"other.html\">link</a>").find());
        Assert.assertTrue(patternAnchorTagWithOutTarget.matcher("<a href=\"other.html\" class=\"AZ\">link</a>").find());
        Assert.assertFalse(patternAnchorTagWithOutTarget.matcher("<a target=\"AZ\" href=\"other.html\">link</a>").find());
        Assert.assertFalse(patternAnchorTagWithOutTarget.matcher("<a href=\"other.html\" target=\"AZ\">link</a>").find());
    }

    @Test
    public void testTargetNotOverridden() {
    	// KNL-526 - testing that targets are not destroyed or replaced
        // method 1 - processAnchor (kills all A attribs and only works on the first part of the tag
    	Assert.assertEquals("<a  href=\"other.html\" target=\"_blank\" rel=\"noopener\">",
                formattedText.processAnchor("<a href=\"other.html\">") );
    	Assert.assertEquals("<a  href=\"other.html\" target=\"_blank\" rel=\"noopener\">",
                formattedText.processAnchor("<a target=\"_blank\" href=\"other.html\">") );

    	Assert.assertEquals("<a  href=\"other.html\" target=\"_AZ\">", 
                formattedText.processAnchor("<a href=\"other.html\" target=\"_AZ\">"));
        // destroys other attributes though...
    	Assert.assertEquals("<a  href=\"other.html\" target=\"_AZ\">", 
                formattedText.processAnchor("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">"));

        // method 2 - escapeHtmlFormattedText (saves other A attribs)
    	Assert.assertEquals("<a href=\"other.html\" target=\"_blank\" rel=\"noopener\">link</a>",
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\">link</a>") );
    	Assert.assertEquals("<a href=\"other.html\" class=\"azeckoski\" target=\"_blank\" rel=\"noopener\">link</a>",
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" class=\"azeckoski\">link</a>") );
        Assert.assertEquals("<a href=\"other.html\" target=\"_blank\" class=\"arbitrary\" rel=\"noopener\">link</a>",
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"_blank\" class=\"arbitrary\">link</a>") );
        Assert.assertEquals("<a href=\"other.html\" class=\"arbitrary\" target=\"_blank\" rel=\"noopener\">link</a>",
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" class=\"arbitrary\" target=\"_blank\">link</a>") );
        Assert.assertEquals("<a href=\"other.html\" target=\"arbitrary\" class=\"arbitrary\">link</a>",
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"arbitrary\" class=\"arbitrary\">link</a>") );
    	Assert.assertEquals("<b>simple</b><b class=\"AZ\">bold</b>", 
                formattedText.escapeHtmlFormattedText("<b>simple</b><b class=\"AZ\">bold</b>") );

    	Assert.assertEquals("<a href=\"other.html\" target=\"_AZ\">link</a>", 
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"_AZ\">link</a>") );
    	Assert.assertEquals("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>", 
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );

    	Assert.assertEquals("<a href=\"other.html\" class=\"azeckoski\" target=\"_blank\" rel=\"noopener\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>",
                formattedText.escapeHtmlFormattedText("<a href=\"other.html\" class=\"azeckoski\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );
    	Assert.assertEquals("<b>simple</b><b class=\"AZ\">bold</b><a href=\"other.html\" class=\"azeckoski\" target=\"_blank\" rel=\"noopener\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>",
                formattedText.escapeHtmlFormattedText("<b>simple</b><b class=\"AZ\">bold</b><a href=\"other.html\" class=\"azeckoski\">link</a><a href=\"other.html\" target=\"_AZ\" class=\"azeckoski\">link</a>") );
    }

    @Test
    public void testAntisamyProcessFormattedText() {
        // TESTS using the antiSamy library
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = TEST1;
        errorMessages = new StringBuilder();
        formattedText.setDefaultAddBlankTargetToLinks(false);
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        Assert.assertNotNull(result);
        Assert.assertTrue( result.contains("href=\"blah.html\""));
        //Assert.assertFalse( result.contains("style=\"font-weight:bold;\"")); // strips this out
        //Assert.assertTrue( result.contains("target=\"_blank\"")); // adds target in
        Assert.assertTrue( result.contains("<div>hello there</div>"));
        Assert.assertEquals("<a href=\"blah.html\" style=\"font-weight: bold;\">blah</a><div>hello there</div>", result);

        strFromBrowser = TEST1;
        errorMessages = new StringBuilder();
        formattedText.setDefaultAddBlankTargetToLinks(true);
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        Assert.assertNotNull(result);
        Assert.assertTrue( result.contains("href=\"blah.html\""));
        //Assert.assertFalse( result.contains("style=\"font-weight:bold;\"")); // strips this out
        //Assert.assertTrue( result.contains("target=\"_blank\"")); // adds target in
        Assert.assertTrue( result.contains("<div>hello there</div>"));
        Assert.assertEquals("<a href=\"blah.html\" style=\"font-weight: bold;\" target=\"_blank\" rel=\"noopener\">blah</a><div>hello there</div>", result);

        strFromBrowser = TEST3;
        errorMessages = new StringBuilder();
        formattedText.setDefaultAddBlankTargetToLinks(true);
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        Assert.assertNotNull(result);
        Assert.assertTrue( result.contains("href=\"blah.html\""));
        //Assert.assertFalse( result.contains("style=\"font-weight:bold;\"")); // strips this out
        //Assert.assertTrue( result.contains("target=\"_blank\"")); // adds target in
        Assert.assertTrue( result.contains("<div>hello there</div>"));
        Assert.assertEquals("<a href=\"blah.html\" style=\"font-weight: bold;\" target=\"_blank\" rel=\"noopener\">blah</a><div>hello there</div>", result);

        strFromBrowser = TEST2;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        Assert.assertNotNull(result);
        Assert.assertEquals("<span>this is my span</span><div>hello there from a div</div>", result);

        String SVG_BAD = "<div>hello</div><embed allowscriptaccess=\"always\" type=\"image/svg+xml\" src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\"></embed>";
        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, false);
        Assert.assertNotNull(result);
        Assert.assertEquals("<div>hello</div>", result);
    }

    @Test
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
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 50 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertFalse( result.contains("<embed"));

        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 50 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertFalse( result.contains("<embed"));

        /* CHANGED BEHAVIOR
         * Antisamy strips the entire embed tag
        strFromBrowser = SVG_GOOD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertTrue( result.contains("<embed"));
        Assert.assertTrue( result.contains("src="));
        Assert.assertTrue( result.contains("data:image/svg+xml;base64"));
        Assert.assertFalse( result.contains("<script"));

        strFromBrowser = SVG_BAD;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertTrue( result.contains("<embed"));
        Assert.assertFalse( result.contains("src="));
        Assert.assertFalse( result.contains("data:image/svg+xml;base64"));
        Assert.assertFalse( result.contains("<script"));
         */

    }

    @Test
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
        String badKV      = "<span class=\"foo\" class-=\"one\"></span>";

        String resultRepeatK    = "<span></span>";
        String resultRepeatKV   = "<span class=\"two\"></span>";
        String resultBadK       = "<span class=\"foo\"></span>";
        String resultBadKV      = "<span class=\"foo\"></span>";

        // antisamy will not allow empty attributes OR unknown attributes
        passTests   = new String[] { oneKV, twoKV, selfClose, subAttr, subAttrs};
        failTests   = new String[] { repeatK, badK, badKV };
        failResults = new String[] { resultRepeatK, resultBadK, resultBadKV };

        result = formattedText.processFormattedText(repeatKV, new StringBuilder());
        Assert.assertEquals(resultRepeatKV, result);

        for (String passTest : passTests) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(passTest, errors);
            Assert.assertEquals(passTest+" != "+result, 0, errors.length());
            Assert.assertEquals(passTest+" != "+result, passTest, result);
        }

        for (int i = 0; i < failTests.length; i++) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(failTests[i], errors);
            Assert.assertEquals(failResults[i]+" != "+result, failResults[i], result);
            Assert.assertTrue(failTests[i]+": "+failResults[i]+" != "+result, errors.length() > 10);
        }

        // LEGACY tests
        /*
        passTests   = new String[] { oneK, oneKV, twoK, twoKV, mixed, selfCloseL, subAttr, subAttrs };
        failTests   = new String[] { repeatK, repeatKV, badK, badK2, badK3, badKV };
        failResults = new String[] { resultRepeatKL, resultRepeatKVL, resultBadK, resultBadK, resultBadK, resultBadKV };

        for (String passTest : passTests) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(passTest, errors, true);
            Assert.assertEquals(passTest, result);
            Assert.assertTrue(errors.length() == 0);
        }

        for (int i = 0; i < failTests.length; i++) {
            errors = new StringBuilder();
            result = formattedText.processFormattedText(failTests[i], errors, true);
            Assert.assertEquals(failResults[i], result);
            Assert.assertTrue(result, errors.length() > 10);
        }
         */
    }

    @Test
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
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertFalse( result.contains("<embed"));
        Assert.assertFalse( result.contains("src="));
        Assert.assertFalse( result.contains("data:image/svg+xml;base64"));
        Assert.assertFalse( result.contains("<script"));

        strFromBrowser = SVG_BAD_CAPS;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertFalse( result.contains("<EMBED"));
        Assert.assertFalse( result.contains("SRC="));
        Assert.assertFalse( result.contains("data:image/svg+xml;base64"));
        Assert.assertFalse( result.contains("<script"));

        /* CDATA is ignored so it will not be cleaned
        String TRICKY = "<div><![CDATA[<EMBED SRC=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></EMBED>]]></div>";
        String CDATA_TRICKY = "<div><![CDATA[<embed src=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" type=\"image/svg+xml\" AllowScriptAccess=\"always\"></embed>]]></div>";

        strFromBrowser = CDATA_TRICKY;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertTrue( result.contains("<embed"));
        Assert.assertFalse( result.contains("src="));
        Assert.assertFalse( result.contains("data:image/svg+xml;base64"));
        Assert.assertFalse( result.contains("<script"));

        strFromBrowser = TRICKY;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div"));
        Assert.assertTrue( result.contains("<EMBED"));
        Assert.assertFalse( result.contains("SRC="));
        Assert.assertFalse( result.contains("data:image/svg+xml;base64"));
        Assert.assertFalse( result.contains("<script"));
         */
    }

    @Test
    public void testUnbalancedMarkup() {
        StringBuilder errorMessages = new StringBuilder();
        String strFromBrowser = "A<B Test";

        String result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        Assert.assertNotNull(result);
        Assert.assertEquals("A", result);
    }

    @Test
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
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT2;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT3;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<SCRIPT"));

        strFromBrowser = SCRIPT4;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, true);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<SCRIPT"));

    }

    @Test
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
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<script"));

        // check the options
        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, null); // default: high
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<SCRIPT"));
        Assert.assertFalse( result.contains("DANGER"));
        Assert.assertFalse( result.contains("<embed"));

        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, Level.HIGH);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<SCRIPT"));
        Assert.assertFalse( result.contains("DANGER"));
        Assert.assertFalse( result.contains("<embed"));

        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, Level.LOW);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertFalse( result.contains("XSS"));
        Assert.assertFalse( result.contains("<SCRIPT"));
        Assert.assertTrue( result.contains("DANGER"));
        Assert.assertTrue( result.contains("<embed"));

        strFromBrowser = SCRIPT1;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages, Level.NONE);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertTrue( result.contains("<div>testing</div>"));
        Assert.assertTrue( result.contains("XSS"));
        Assert.assertTrue( result.contains("<SCRIPT"));
        Assert.assertTrue( result.contains("DANGER"));
        Assert.assertTrue( result.contains("<embed"));
    }

    @Test
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
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertTrue( result.contains("<object"));
        Assert.assertTrue( result.contains("<embed"));
        Assert.assertTrue( result.contains("www.youtube.com/v/1yqVD0swvWU"));

        strFromBrowser = youTubeIframe;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertTrue( result.contains("<iframe"));
        Assert.assertTrue( result.contains("allowfullscreen"));
        Assert.assertTrue( result.contains("www.youtube.com/embed/1yqVD0swvWU"));

        strFromBrowser = youTubeIframeOptions;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertTrue( result.contains("<iframe"));
        Assert.assertTrue( result.contains("allowfullscreen"));
        Assert.assertTrue( result.contains("www.youtube-nocookie.com/embed/3pAnRKD4raY"));

        strFromBrowser = youTubeCK;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertTrue( result.contains("<object"));
        Assert.assertTrue( result.contains("/library/editor/ckextraplugins"));
        Assert.assertTrue( result.contains("www.youtube.com/v/1yqVD0swvWU"));

        strFromBrowser = youTubeSpecialCK;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertTrue( result.contains("<object"));
        Assert.assertTrue( result.contains("<param"));
        Assert.assertTrue( result.contains("youtu.be/1yqVD0swvWU"));

        // test bad stuff
        strFromBrowser = dangerEmbed;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("SAFE"));
        Assert.assertFalse( result.contains("<object"));
        Assert.assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("SAFE"));
        Assert.assertFalse( result.contains("<object"));
        Assert.assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath2;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("SAFE"));
        Assert.assertFalse( result.contains("<object"));
        Assert.assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath3;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("SAFE"));
        Assert.assertFalse( result.contains("<object"));
        Assert.assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath4;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("SAFE"));
        Assert.assertFalse( result.contains("<object"));
        Assert.assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath5;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("SAFE"));
        Assert.assertFalse( result.contains("<object"));
        Assert.assertFalse( result.contains("DANGER"));

        strFromBrowser = dangerLibraryPath6;
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertTrue( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("SAFE"));
        Assert.assertFalse( result.contains("<object"));
        Assert.assertFalse( result.contains("DANGER"));

    }

    @Test
    public void testNullParams() {
        //KNL-862 test we don't NPE if a null string is passed with Newlines == true - DH
        try {
            formattedText.escapeHtml(null, true);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testKNL_1065() {
        // https://jira.sakaiproject.org/browse/KNL-1065
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<span class=\"kaltura-media classValue\" rel=\"relValue::video\"><img src=\"https://cdnsecakmi.kaltura.com/p/999999/imgValue\" /></span>";
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertFalse( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("classValue"));
        Assert.assertTrue( result.contains("relValue"));
        Assert.assertTrue( result.contains("imgValue"));

        strFromBrowser = "<div class=\"classValue\">divValue<ins>insValue</ins><ins datetime=\"2013-10-29\" cite=\"/url/to/cite.html\">insComplexValue</ins><del>delValue</del></div>";
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertFalse( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div "));
        Assert.assertTrue( result.contains("<ins>"));
        Assert.assertTrue( result.contains("<ins "));
        Assert.assertTrue( result.contains("<del>"));
        Assert.assertTrue( result.contains("divValue"));
        Assert.assertTrue( result.contains("insValue"));
        Assert.assertTrue( result.contains("delValue"));
        Assert.assertTrue( result.contains("insComplexValue"));
        Assert.assertTrue( result.contains("2013-10-29"));
        Assert.assertTrue( result.contains("/url/to/cite.html"));
    }

    @Test
    public void testKNL_1531() {
        // https://jira.sakaiproject.org/browse/KNL-1061
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<div class=\"classValue\">divValue</div><img border=\"0\" aria-hidden=\"true\" aria-label=\"Close\" />";
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertFalse( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div "));
        Assert.assertTrue( result.contains("divValue"));
        Assert.assertTrue( result.contains("<img"));
        Assert.assertTrue( result.contains("aria-hidden"));
        Assert.assertTrue( result.contains("aria-label"));
        Assert.assertTrue( result.contains("Close"));
    }

    @Test
    public void testKNL_1061() {
        // https://jira.sakaiproject.org/browse/KNL-1061
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<div class=\"classValue\">divValue</div><img border=\"0\" data-mathml=\"%3Cmrow%3E%0A%20%20%20%20%20%20%20%20%3Cmo%20selected%3D%22true%22%3E%26frac23%3B%3C/mo%3E%0A%3C/mrow%3E\" id=\"MathMLEq1\" src=\"http://nightly2.sakaiproject.org:8085/access/content/group/mercury/fmath-equation-94BDA89D-E911-283D-53C1-32D6CCE53EB0.png\" />";
        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertFalse( errorMessages.length() > 10 );
        Assert.assertTrue( result.contains("<div "));
        Assert.assertTrue( result.contains("divValue"));
        Assert.assertTrue( result.contains("<img"));
        Assert.assertTrue( result.contains("data-mathml"));
        Assert.assertTrue( result.contains("%20selected%3D"));
    }

    @Test
    public void testKNL_1071() {
        // https://jira.sakaiproject.org/browse/KNL-1071
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<p><span style=\"background-color:yellow;\">aaa </span><tt>bbb </tt><code>ccc </code><kbd>ddd </kbd><del>eee </del><span dir=\"rtl\">fff </span><cite>ggg</cite></p>";

        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertFalse( errorMessages.length() > 0 );
        Assert.assertTrue( result.contains("<tt>"));
        Assert.assertTrue( result.contains("ddd"));
        Assert.assertTrue( result.contains("<cite>"));
        Assert.assertTrue( result.contains("<kbd>"));
        Assert.assertTrue( result.contains("<span dir=\"rtl\""));
    }

    @Test
    public void testKNL_1096() {
        // https://jira.sakaiproject.org/browse/KNL-1096
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;

        strFromBrowser = "<object width=\"560\" height=\"315\"><param name=\"movie\" value=\"//www.youtube.com/v/JNSK0647wJI?version=3&amp;hl=en_US\"></param><param name=\"allowFullScreen\" value=\"true\"></param><param name=\"allowscriptaccess\" value=\"always\"></param><embed src=\"//www.youtube.com/v/JNSK0647wJI?version=3&amp;hl=en_US\" type=\"application/x-shockwave-flash\" width=\"560\" height=\"315\" allowscriptaccess=\"always\" allowfullscreen=\"true\"></embed></object>";

        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertFalse( errorMessages.length() > 0 );
        Assert.assertTrue( result.contains("<object"));
        Assert.assertTrue( result.contains("<param"));
        Assert.assertTrue( result.contains("value=\"//www.youtube.com/v/JNSK0647wJI"));
        Assert.assertTrue( result.contains("src=\"//www.youtube.com/v/JNSK0647wJI"));
    }

    @Test
    public void testKNL_1407() {
        // https://jira.sakaiproject.org/browse/KNL-1407
        String strFromBrowser = null;
        String result = null;
        StringBuilder errorMessages = null;
        strFromBrowser = "<video width=\"320\" height=\"240\"> <source type=\"video/mp4\" src=\"http://localhost:8080/access/content/group/3c1af5f8-10f0-4e12-99b6-43a2427c5fc6/Test1/test1.mp4\" ><track src=\"http://localhost:8080/access/content/group/3c1af5f8-10f0-4e12-99b6-43a2427c5fc6/Test1/captions_file.vtt\" label=\"English\" kind=\"captions\" srclang=\"en-us\" default > </video>";

        errorMessages = new StringBuilder();
        result = formattedText.processFormattedText(strFromBrowser, errorMessages);
        Assert.assertNotNull(result);
        Assert.assertFalse(errorMessages.toString(), errorMessages.length() > 0);
        Assert.assertTrue( result.contains("<video"));
        Assert.assertTrue( result.contains("<track"));
        Assert.assertTrue( result.contains("src=\"http://localhost:8080/access/content/group/3c1af5f8-10f0-4e12-99b6-43a2427c5fc6/Test1/test1.mp4"));
        Assert.assertTrue( result.contains("src=\"http://localhost:8080/access/content/group/3c1af5f8-10f0-4e12-99b6-43a2427c5fc6/Test1/captions_file.vtt"));
        Assert.assertTrue( result.contains("kind=\"captions"));
        Assert.assertTrue( result.contains("srclang=\"en-us"));
        Assert.assertTrue( result.contains("label=\"English"));
        Assert.assertTrue( result.contains("default"));
    }


    @Test
    public void testValidateURL() {
        // https://jira.sakaiproject.org/browse/KNL-1100
        boolean result = false;
        result = formattedText.validateURL("http://www.vt.edu/");
        Assert.assertTrue(result);
        result = formattedText.validateURL("http://localhost:8080/access/site/634cce7b-96da-4997-90b1-f99ea3c3973d");
        Assert.assertTrue(result);
        result = formattedText.validateURL("http://127.0.0.1:8080/access/site/634cce7b-f99ea3c3973d");
        Assert.assertTrue(result);
        result = formattedText.validateURL("//www.dr-chuck.edu/");
        Assert.assertTrue(result);
        result = formattedText.validateURL("/access/zap123");
        Assert.assertTrue(result);
        result = formattedText.validateURL("about:blank");
        Assert.assertTrue(result);
        result = formattedText.validateURL("ftp://ftp.umich.edu/");
        Assert.assertTrue(result);

        // Some false things...
        result = formattedText.validateURL("www.dr-chuck.com"); 
        Assert.assertFalse(result);
        result = formattedText.validateURL("XXXXXXXX");
        Assert.assertFalse(result);
    }

    @Test
    public void testEscapeHrefUrl() {
        // https://jira.sakaiproject.org/browse/KNL-1105
        String output = null;
        output = formattedText.sanitizeHrefURL("http://www.sakaiproject.org/?x=Hello World&y=12");
        Assert.assertTrue(output,"http://www.sakaiproject.org/?x=Hello%20World&y=12".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es#\"><script>");
        Assert.assertTrue(output,"http://www.abc.es#%22%3E%3Cscript%3E".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es/page#anchor");
        Assert.assertTrue(output,"http://www.abc.es/page#anchor".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es?x=2&y=3&#12;");
        Assert.assertTrue(output,"http://www.abc.es?x=2&y=312;".equals(output));
        output = formattedText.sanitizeHrefURL("http://nightly2.sakaiproject.org/portal/site/!gateway/page/!gateway-200/\"onmouseover='alert(\"xss\")'\"");
        Assert.assertTrue(output,"http://nightly2.sakaiproject.org/portal/site/!gateway/page/!gateway-200/%22onmouseover=%27alert(%22xss%22)%27%22".equals(output));
        output = formattedText.sanitizeHrefURL("http://www.abc.es/');alert('yo');");
        Assert.assertTrue(output,"http://www.abc.es/%27);alert(%27yo%27);".equals(output));
        output = formattedText.sanitizeHrefURL("about:blank");
        Assert.assertTrue(output,"about:blank".equals(output));
        output = formattedText.sanitizeHrefURL("ftp://ftp.umich.edu/");
        Assert.assertTrue(output,"ftp://ftp.umich.edu/".equals(output));

        // Try the prefixless ones...
        output = formattedText.sanitizeHrefURL("//www.dr-chuck.com/");
        Assert.assertTrue(output,"//www.dr-chuck.com/".equals(output));
        output = formattedText.sanitizeHrefURL("/access/zap123");
        Assert.assertTrue(output,"/access/zap123".equals(output));

        // Things that fail
        output = formattedText.sanitizeHrefURL("http://www.abc.es');alert('yo');");
        Assert.assertNull(output);
        output = formattedText.sanitizeHrefURL("www.abc.es");
        Assert.assertNull(output);
        output = formattedText.sanitizeHrefURL("wwwwwwwwww");
        Assert.assertNull(output);
        
        //% charactesrs are valid and sanitize method should maintain them
        output = formattedText.sanitizeHrefURL("http://www.server.com/path%20with%20whitespaces/");
        Assert.assertTrue(output,"http://www.server.com/path%20with%20whitespaces/".equals(output));

        output = formattedText.sanitizeHrefURL("http://www.server.com/path%25with%2Epercent/");
        Assert.assertTrue(output,"http://www.server.com/path%25with%2Epercent/".equals(output));
        
    }

    @Test
    public void testBasicUrlMatch() {
        Assert.assertEquals("I like <a href=\"http://www.apple.com\">http://www.apple.com</a> and stuff", formattedText.encodeUrlsAsHtml(formattedText.escapeHtml("I like http://www.apple.com and stuff")));
    }

    @Test
    public void testCanDoSsl() {
        Assert.assertEquals("<a href=\"https://sakaiproject.org\">https://sakaiproject.org</a>", formattedText.encodeUrlsAsHtml("https://sakaiproject.org"));
    }

    @Test
    public void testCanIgnoreTrailingExclamation() {
        Assert.assertEquals("Hey, it's <a href=\"http://sakaiproject.org\">http://sakaiproject.org</a>!", formattedText.encodeUrlsAsHtml("Hey, it's http://sakaiproject.org!"));
    }

    @Test
    public void testCanIgnoreTrailingQuestion() {
        Assert.assertEquals("Have you ever seen <a href=\"http://sakaiproject.org\">http://sakaiproject.org</a>? Just wondering.", formattedText.encodeUrlsAsHtml("Have you ever seen http://sakaiproject.org? Just wondering."));
    }

    @Test
    public void testCanEncodeQueryString() {
        Assert.assertEquals("See <a href=\"http://sakaiproject.org/index.php?task=blogcategory&id=181\">http://sakaiproject.org/index.php?task=blogcategory&amp;id=181</a> for more info.", formattedText.encodeUrlsAsHtml(formattedText.escapeHtml("See http://sakaiproject.org/index.php?task=blogcategory&id=181 for more info.")));
    }

    @Test
    public void testCanTakePortNumber() {
        Assert.assertEquals("<a href=\"http://localhost:8080/portal\">http://localhost:8080/portal</a>", formattedText.encodeUrlsAsHtml("http://localhost:8080/portal"));
    }

    @Test
    public void testCanTakePortNumberAndQueryString() {
        Assert.assertEquals("<a href=\"http://www.loco.com:3000/portal?person=224\">http://www.loco.com:3000/portal?person=224</a>", formattedText.encodeUrlsAsHtml("http://www.loco.com:3000/portal?person=224"));
    }

    @Test
    public void testCanIgnoreExistingHref() {
        Assert.assertEquals("<a href=\"http://sakaiproject.org\">Sakai Project</a>", formattedText.encodeUrlsAsHtml("<a href=\"http://sakaiproject.org\">Sakai Project</a>"));
    }

    @Test
    public void testALongUrlFromNyTimes() {
        Assert.assertEquals("<a href=\"http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&symb=LLNW\">http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&amp;symb=LLNW</a>",
                formattedText.encodeUrlsAsHtml(formattedText.escapeHtml("http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&symb=LLNW")));
    }

    @Test
    public void testHrefWithSpace() {
      StringBuilder errorMessages = new StringBuilder();
      formattedText.setDefaultAddBlankTargetToLinks(false);

      Assert.assertEquals("<a href=\"http://localhost:8080/access/content/public/Home Page.htm\">Test Search Space</a>",
          formattedText.processFormattedText("<a href=\"http://localhost:8080/access/content/public/Home Page.htm\">Test Search Space</a>", errorMessages));
      
      Assert.assertEquals("<a href=\"https://www.example.com/access/content/public/Home Page.htm\">Test Search Space</a>",
          formattedText.processFormattedText("<a href=\"https://www.example.com/access/content/public/Home Page.htm\">Test Search Space</a>", errorMessages));

      formattedText.setDefaultAddBlankTargetToLinks(BLANK_DEFAULT);
    }

    @Test
    public void testHrefWithPlus() {
      StringBuilder errorMessages = new StringBuilder();
      formattedText.setDefaultAddBlankTargetToLinks(false);
      
      Assert.assertEquals("<a href=\"http://localhost:8080/access/content/public/Home+Page.htm\">Test Search Plus</a>",
          formattedText.processFormattedText("<a href=\"http://localhost:8080/access/content/public/Home+Page.htm\">Test Search Plus</a>", errorMessages));

      Assert.assertEquals("<a href=\"https://www.example.com/access/content/public/Home+Page.htm\">Test Search Plus</a>",
          formattedText.processFormattedText("<a href=\"https://www.example.com/access/content/public/Home+Page.htm\">Test Search Plus</a>", errorMessages));

      formattedText.setDefaultAddBlankTargetToLinks(BLANK_DEFAULT);
    }

    @Test 
    public void testEmoji() {
        StringBuilder errorMessages = new StringBuilder();
        String etext = new StringBuilder().appendCodePoint(0x1F600).append("smiley face").appendCodePoint(0x1F600).toString();
        //Retrict to utf8 only
        serverConfigurationService.registerConfigItem(BasicConfigItem.makeConfigItem("content.cleaner.filter.utf8", "true", "FormattedTextTest"));
        formattedText.init();
        
        Assert.assertEquals("smiley face",formattedText.processFormattedText(etext, errorMessages));

        //Test the replacement of ?
        serverConfigurationService.registerConfigItem(BasicConfigItem.makeConfigItem("content.cleaner.filter.utf8.replacement", "?", "FormattedTextTest"));
        formattedText.init();
        Assert.assertEquals("??smiley face??",formattedText.processFormattedText(etext, errorMessages));

        //Don't restrict to UTF8
        serverConfigurationService.registerConfigItem(BasicConfigItem.makeConfigItem("content.cleaner.filter.utf8", "false", "FormattedTextTest"));
        formattedText.init();
        Assert.assertEquals(etext,formattedText.processFormattedText(etext, errorMessages));
    }

    @Test
    public void testKNL_1253() {
        // https://jira.sakaiproject.org/browse/KNL-1253
        String text = null;
        String result = null;

        // no change
        text = null;
        result = formattedText.stripHtmlFromText(text,true);
        Assert.assertEquals(text, result);

        text = "";
        result = formattedText.stripHtmlFromText(text,true);
        Assert.assertEquals(text, result);

        text = "azeckoski";
        result = formattedText.stripHtmlFromText(text,true);
        Assert.assertEquals(text, result);

        // changed
        text = "<b>azeckoski</b>";
        result = formattedText.stripHtmlFromText(text,true);
        Assert.assertEquals("azeckoski", result);

        text = "<a href='www.vt.edu'><b>azeckoski</b> is AZ</a>";
        result = formattedText.stripHtmlFromText(text,true);
        Assert.assertEquals("azeckoski is AZ", result);

        text = "<table><tr><th>Column1</th></tr><tr><td>Row1</td></tr></table>";
        result = formattedText.stripHtmlFromText(text,true);
        Assert.assertEquals("Column1 Row1", result);

        text = "<table><tr><th>Column1</th></tr><tr><td>Row1</td></tr></table>";
        result = formattedText.stripHtmlFromText(text,false);
        Assert.assertEquals("Column1Row1", result);
    }

    @Test
    public void testKNL_1464() {
        // https://jira.sakaiproject.org/browse/KNL-1464
        String text = null;
        String result = null;
        StringBuilder errorMessages = new StringBuilder();

        //These are all expected to be an empty string as these tags are removed
        text = "<form>First name:<br><input type='text' name='firstname'><br>Last name:<br> <input type='text' name='lastname'></form>";
        result = formattedText.processFormattedText(text,errorMessages);
        Assert.assertTrue( errorMessages.length() > 1 );
        Assert.assertEquals(result, "");

        text = "<input type='text' name='firstname'>";
        result = formattedText.processFormattedText(text,errorMessages);
        Assert.assertTrue( errorMessages.length() > 1 );
        Assert.assertEquals(result, "");

        text = "<textarea rows='4' cols='50'>textarea</textarea>";
        result = formattedText.processFormattedText(text,errorMessages);
        Assert.assertTrue( errorMessages.length() > 1 );
        Assert.assertEquals(result, "");

        text = "<select><option value='sakai'>Sakai</option></select>";
        result = formattedText.processFormattedText(text,errorMessages);
        Assert.assertTrue( errorMessages.length() > 1 );
        Assert.assertEquals(result, "");

    }

    @Test
    public void testKNL_1487() {
        // https://jira.sakaiproject.org/browse/KNL-1487
        String text = null;
        String result = null;
        StringBuilder errorMessages = new StringBuilder();

        //These are all expected to be an empty string as these tags are removed
        text = "<img align=\"middle\" alt=\"square root of 5555\" class=\"Wirisformula\" data-mathml=\"(some mathml)\" role=\"math\" src=\"/pluginwiris_engine/app/showimage?formula=bf57cf5ace9b1e530d7221ee512cb429\" />";
        result = formattedText.processFormattedText(text,errorMessages);
        Assert.assertTrue( errorMessages.length() == 0 );
        //Verify nothing was removed
        Assert.assertEquals(result, text);
    }

    @Test
    public void testGetShortenedTitles() {
        for (String siteTitle:SITE_TITLES) {
            for (int k=0; k<CUT_METHODS.length; k++) {
                ServerConfigurationService scs = new BasicConfigurationService();
                scs.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("site.title.cut.method", CUT_METHODS[k], "FormattedTextTest"));
                scs.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("site.title.cut.maxlength", MAX_LENGTHS[k], "FormattedTextTest"));
                scs.registerConfigItem(BasicConfigItem.makeDefaultedConfigItem("site.title.cut.separator", CUT_SEPARATORS[k], "FormattedTextTest"));
                formattedText.setServerConfigurationService(scs);
                String resumeTitle = formattedText.makeShortenedText(siteTitle, null, null, null);
                // The resume title has the defined length (if has enough length)
                Assert.assertEquals(Math.min(MAX_LENGTHS[k],siteTitle.length()),resumeTitle.length());
                if (siteTitle.length()>MAX_LENGTHS[k] && MAX_LENGTHS[k]>=10) {
                    // The title must to be cut, so it has to contains cut separator
                    Assert.assertEquals(true,resumeTitle.contains(CUT_SEPARATORS[k]));
                } else if (siteTitle.length()>MAX_LENGTHS[k]) {
                    // Title truncate
                    Assert.assertEquals(siteTitle.trim().substring(0,MAX_LENGTHS[k]),resumeTitle);
                } else {
                    // Title without change
                    Assert.assertEquals(siteTitle.trim(),resumeTitle);
                }
            }
        }
    }

    @Test
    public void testStripHtmlFromText() {
        String text, result = null;

        result = formattedText.stripHtmlFromText(null, false, false);
        Assert.assertEquals(null, result);

        text = "<table><tr><th>Column1</th></tr><tr><td>Row1</td></tr></table>";
        result = formattedText.stripHtmlFromText(text, false, false);
        Assert.assertEquals("Column1Row1", result);

        result = formattedText.stripHtmlFromText(text, true, false);
        Assert.assertEquals("Column1 Row1", result);

        text = "<p>line one &amp;</br>newline</p>";
        result = formattedText.stripHtmlFromText(text, true, true);
        Assert.assertEquals("line one & newline", result);

        text = "<table><tr><th>Column1 </th></tr><tr><td>Row1&nbsp; </td></tr></table>";
        result = formattedText.stripHtmlFromText(text, false, true);
        Assert.assertEquals("Column1 Row1", result);

        text = "<table>this is a table?";
        result = formattedText.stripHtmlFromText(text, false, false);
        Assert.assertEquals("this is a table?", result);

        text = "a<b>d";
        result = formattedText.stripHtmlFromText(text, false, false);
        Assert.assertEquals("ad", result);
    }

    @Test
    public void testKNL_1530() {
        // https://jira.sakaiproject.org/browse/KNL-1530
        String text = null;
        String result = null;
        StringBuilder errorMessages = new StringBuilder();

        String anchor = "<a href=\"http://sakaiproject.org/\">sakaiproject</a>";
        String expectedAnchor = "<a href=\"http://sakaiproject.org/\" target=\"_blank\" rel=\"noopener\">sakaiproject</a>";

        //Process the anchor, there shouldn't be any error messages or changes, but it does insert target and noopener which should pass
        result = formattedText.processFormattedText(anchor,errorMessages);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertEquals(result, expectedAnchor);

        //Now reprocess the result
        result = formattedText.processFormattedText(result,errorMessages);
        Assert.assertTrue( errorMessages.length() == 0 );
        Assert.assertEquals(result, expectedAnchor);
    }
    
    @Test
    public void getHtmlBodyTest() {
    	String result;
        StringBuilder errorMessages = new StringBuilder();

        result = formattedText.getHtmlBody("<html><body><div>Text</div></body></html>");
        Assert.assertEquals("<div>Text</div>", result);
        
        result = formattedText.getHtmlBody("<div>Text</div>");
        Assert.assertEquals("<div>Text</div>", result);

        result = formattedText.getHtmlBody("");
        Assert.assertEquals("", result);
    }


}
