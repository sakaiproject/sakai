/**********************************************************************************
 * $URL$
 * $Id$
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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URI;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.w3c.dom.Element;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.api.FormattedText;


/**
 * FormattedText provides support for user entry of formatted text; the formatted text is HTML. This includes text formatting in user input such as bold, underline, and fonts.
 */
public class FormattedTextImpl implements FormattedText
{
    /** Our log (commons). */
    private static final Logger M_log = LoggerFactory.getLogger(FormattedTextImpl.class);

    private ServerConfigurationService serverConfigurationService = null;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private SessionManager sessionManager = null;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }   

    /**
     * This is the high level html cleaner object
     */
    private AntiSamy antiSamyHigh = null;
    /**
     * This is the low level html cleaner object
     */
    private AntiSamy antiSamyLow = null;

    /* KNL-1075 - content.cleaner.errors.handling = none|logged|return|notify|display
     * - none - errors are completely ignored and not even stored at all
     * - logged - errors are output in the logs only
     * - return - errors are returned to the tool (legacy behavior)
     * - notify - user notified about errors using a non-blocking JS popup
     * - display - errors are displayed to the user using the new and fancy JS popup
     */
    private String errorsHandling = "notify"; // set this to the default
    private boolean showErrorToUser = false;
    private boolean showDetailedErrorToUser = false;
    private boolean returnErrorToTool = false;
    private boolean logErrors = false;
    private boolean cleanUTF8 = true;
    private String restrictReplacement = null;

    private String referrerPolicy = null;
    private static final String SAK_PROP_REFERRER_POLICY = "content.cleaner.referrer-policy";
    private static final String SAKAI_REFERRER_POLICY_DEFAULT = "noopener";

    private final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ContentProperties";
    protected final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.content.content";
    private final String RESOURCECLASS = "resource.class.content";
    protected final String RESOURCEBUNDLE = "resource.bundle.content";

    public void init() {
        boolean useLegacy = false;
        if (serverConfigurationService != null) { // this keeps the tests from dying
            useLegacy = serverConfigurationService.getBoolean("content.cleaner.use.legacy.html", useLegacy);

            //Filter content output to limited unicode characters KNL-1431
            cleanUTF8 = serverConfigurationService.getBoolean("content.cleaner.filter.utf8",cleanUTF8);
            restrictReplacement = serverConfigurationService.getString("content.cleaner.filter.utf8.replacement",restrictReplacement);

            /* KNL-1075 - content.cleaner.errors.handling = none|logged|return|notify|display
             * - none - errors are completely ignored and not even stored at all
             * - logged - errors are output in the logs only
             * - return - errors are returned to the tool (legacy behavior)
             * - notify - user notified about errors using a non-blocking JS popup
             * - display - errors are displayed to the user using the new and fancy JS popup
             */
            errorsHandling = serverConfigurationService.getString("content.cleaner.errors.handling", errorsHandling);
            // NONE is the case when the string is not matched as well
            if ("logged".equalsIgnoreCase(errorsHandling)) {
                logErrors = true;
            } else if ("return".equalsIgnoreCase(errorsHandling)) {
                returnErrorToTool = true;
            } else if ("notify".equalsIgnoreCase(errorsHandling)) {
                showErrorToUser = true;
            } else if ("display".equalsIgnoreCase(errorsHandling)) {
                showDetailedErrorToUser = true;
            } else {
                // probably the none case, but maybe also a case of invalid config....
                if (!"none".equalsIgnoreCase(errorsHandling)) {
                    M_log.warn("FormattedText error handling option invalid: "+errorsHandling+", defaulting to 'none'");
                }
            }
            // allow one extra option to control logging if desired
            logErrors = serverConfigurationService.getBoolean("content.cleaner.errors.logged", logErrors);
            M_log.info("FormattedText error handling: "+errorsHandling+
                    "; log errors=" + logErrors + 
                    "; return to tool=" + returnErrorToTool + 
                    "; notify user=" + showErrorToUser + 
                    "; details to user=" + showDetailedErrorToUser);

            referrerPolicy = serverConfigurationService.getString(SAK_PROP_REFERRER_POLICY, SAKAI_REFERRER_POLICY_DEFAULT);
        }
        if (useLegacy) {
            M_log.error(
                     "**************************************************\n"
                    +"* -----------<<<   WARNING   >>>---------------- *\n"
                    +"* The LEGACY Sakai content scanner is no longer  *\n"
                    +"* available. It has been deprecated and removed. *\n"
                    +"* Content scanning uses AntiSamy scanner now.    *\n"
                    +"* https://jira.sakaiproject.org/browse/KNL-1127  *\n"
                    +"**************************************************\n"
            );
        }

        /* INIT Antisamy
         * added in support for antisamy html cleaner - KNL-1015
         * https://www.owasp.org/index.php/Category:OWASP_AntiSamy_Project
         */
        try {
            ClassLoader current = FormattedTextImpl.class.getClassLoader();
            URL lowPolicyURL = current.getResource("antisamy/low-security-policy.xml");
            URL highPolicyURL = current.getResource("antisamy/high-security-policy.xml");
            // Allow lookup of the policy files in sakai home - KNL-1047
            String sakaiHomePath = getSakaiHomeDir();
            File lowFile = new File(sakaiHomePath, "antisamy"+File.separator+"low-security-policy.xml");
            if (lowFile.canRead()) {
                lowPolicyURL = lowFile.toURI().toURL();
                M_log.info("AntiSamy found override for low policy file at: "+lowPolicyURL);
            }
            File highFile = new File(sakaiHomePath, "antisamy"+File.separator+"high-security-policy.xml");
            if (highFile.canRead()) {
                highPolicyURL = highFile.toURI().toURL();
                M_log.info("AntiSamy found override for high policy file at: "+highPolicyURL);
            }
            Policy policyHigh = Policy.getInstance(highPolicyURL);
            antiSamyHigh = new AntiSamy(policyHigh);
            Policy policyLow = Policy.getInstance(lowPolicyURL);
            antiSamyLow = new AntiSamy(policyLow);
            // TODO should we attempt to fallback to internal files if the parsing/init fails of external ones?
            M_log.info("AntiSamy INIT default security level ("+(defaultLowSecurity()?"LOW":"high")+"), policy files: high="+highPolicyURL+", low="+lowPolicyURL);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to startup the antisamy html code cleanup handler (cannot complete startup): " + e, e);
        }

    }

    /*
        Removes surrogates from a string http://stackoverflow.com/a/12867139/3708872
        @param str Value to process
    */
    public String removeSurrogates(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isSurrogate(c)) {
                sb.append(c);
            }
            else if (restrictReplacement != null) {
                sb.append(restrictReplacement);
            }
        }
        return sb.toString();
    }

    boolean defaultAddBlankTargetToLinks = true;
    /**
     * For TESTING
     * Sets the default - if not set, this will be "true"
     * @param addBlankTargetToLinks if we should add ' target="_blank" ' to all A tags which contain no target, false if they should not be touched
     */
    void setDefaultAddBlankTargetToLinks(boolean addBlankTargetToLinks) {
        this.defaultAddBlankTargetToLinks = addBlankTargetToLinks;
    }
    /**
     * Asks SCS for the value of the "content.cleaner.add.blank.target", DEFAULT is true (match legacy)
     * @return true if we should add ' target="_blank" ' to all A tags which contain no target, false if they should not be touched
     */
    private boolean addBlankTargetToLinks() {
        boolean add = defaultAddBlankTargetToLinks;
        if (serverConfigurationService != null) { // this keeps the tests from dying
            add = serverConfigurationService.getBoolean("content.cleaner.add.blank.target", defaultAddBlankTargetToLinks);
        }
        return add;
    }

    /**
     * Asks SCS for the value of the "content.cleaner.default.low.security", DEFAULT is false
     * @return true if low security is on be default for the scanner OR false to use high security scan (no unsafe embeds or objects)
     */
    private boolean defaultLowSecurity() {
        boolean defaultLowSecurity = false;
        if (serverConfigurationService != null) { // this keeps the tests from dying
            defaultLowSecurity = serverConfigurationService.getBoolean("content.cleaner.default.low.security", defaultLowSecurity);
        }
        return defaultLowSecurity;
    }

    public ResourceLoader getResourceLoader() {
        String resourceClass = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
        String resourceBundle = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
        ResourceLoader loader = new Resource().getLoader(resourceClass, resourceBundle);
        return loader;
    }   

    /**
     * @return the path to the sakai home directory on the server
     */
    private String getSakaiHomeDir() {
        String sakaiHome = ""; // current dir (should be tomcat home) - this failsafe should not be used
        if (serverConfigurationService != null) { // this keeps the tests from dying
            String sh = serverConfigurationService.getSakaiHomePath();
            if (sh != null) {
                sakaiHome = sh;
            }
        }
        sakaiHome = new File(sakaiHome).getAbsolutePath(); // standardize
        return sakaiHome;
    }

    /** Matches HTML-style line breaks like &lt;br&gt; */
    private Pattern M_patternTagBr = Pattern.compile("<\\s*br\\s+?[^<>]*?>", Pattern.CASE_INSENSITIVE);

    /** Matches any HTML-style tag, like &lt;anything&gt; */
    private Pattern M_patternTag = Pattern.compile("<.*?>", Pattern.DOTALL);

    /** Matches newlines */
    private Pattern M_patternNewline = Pattern.compile("\\n");

    /** Matches all anchor tags that have a target attribute. */
    public final Pattern M_patternAnchorTagWithTarget = Pattern.compile("([<]a\\s[^<>]*?)target=[^<>\\s]*([^<>]*?)[>]",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /** Matches all anchor tags that have target="_blank" not accompanied by a rel attribute. */
    public final Pattern M_patternAnchorTagWithTargetBlankAndWithOutRel = Pattern.compile("([<]a\\s[^<>]*?)(?![^>]*rel[^<>\\s]*=)(target[^<>\\s]*=[^<>\\s]*_blank)([^<>]*?)[>]",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    /** Matches all anchor tags that do not have a target attribute. */
    public final Pattern M_patternAnchorTagWithOutTarget = 
            Pattern.compile("([<]a\\s)(?![^>]*target=)([^>]*?)[>]",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Matches href attribute */
    private Pattern M_patternHref = Pattern.compile("\\shref\\s*=\\s*(\".*?\"|'.*?')",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern M_patternHrefTarget = Pattern.compile("\\starget\\s*=\\s*(\".*?\"|'.*?')",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern M_patternHrefTitle = Pattern.compile("\\stitle\\s*=\\s*(\".*?\"|'.*?')",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern M_patternHrefRel = Pattern.compile("\\srel\\s*=\\s*(\".*?\"|'.*?')",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#processFormattedText(java.lang.String, java.lang.StringBuffer)
     */
    public String processFormattedText(final String strFromBrowser, StringBuffer errorMessages) {
        StringBuilder sb = new StringBuilder(errorMessages.toString());
        String fixed = processFormattedText(strFromBrowser, sb);
        errorMessages.setLength(0);
        errorMessages.append(sb.toString());
        return fixed;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#processFormattedText(java.lang.String, java.lang.StringBuilder)
     */
    public String processFormattedText(final String strFromBrowser, StringBuilder errorMessages)
    {
        boolean checkForEvilTags = true;
        boolean replaceWhitespaceTags = true;
        return processFormattedText(strFromBrowser, errorMessages, null, checkForEvilTags, replaceWhitespaceTags, false);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#processFormattedText(java.lang.String, java.lang.StringBuilder, org.sakaiproject.util.api.FormattedText.Level)
     */
    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages, Level level) {
        boolean checkForEvilTags = true;
        boolean replaceWhitespaceTags = true;
        return processFormattedText(strFromBrowser, errorMessages, level, checkForEvilTags, replaceWhitespaceTags, false);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#processFormattedText(java.lang.String, java.lang.StringBuilder, boolean)
     */
    public String processFormattedText(final String strFromBrowser,
            StringBuilder errorMessages, boolean useLegacySakaiCleaner) {
        boolean checkForEvilTags = true;
        boolean replaceWhitespaceTags = true;
        return processFormattedText(strFromBrowser, errorMessages, null, checkForEvilTags,
                replaceWhitespaceTags, useLegacySakaiCleaner);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#processHtmlDocument(java.lang.String, java.lang.StringBuilder)
     */
    public String processHtmlDocument(final String strFromBrowser, StringBuilder errorMessages)
    {
        return strFromBrowser;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#processFormattedText(java.lang.String, java.lang.StringBuilder, boolean, boolean)
     */
    public String processFormattedText(final String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
            boolean replaceWhitespaceTags)
    {
        return processFormattedText(strFromBrowser, errorMessages, null, checkForEvilTags, replaceWhitespaceTags, false);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#processFormattedText(java.lang.String, java.lang.StringBuilder, org.sakaiproject.util.api.FormattedText.Level, boolean, boolean, boolean)
     */
    public String processFormattedText(final String strFromBrowser, StringBuilder errorMessages, Level level,
            boolean checkForEvilTags, boolean replaceWhitespaceTags, boolean doNotUseLegacySakaiCleaner) {

        // KNL-1075: bypass the old error system and present our formatted text errors using growl notification
        StringBuilder formattedTextErrors = new StringBuilder();

        if (level == null || Level.DEFAULT.equals(level)) {
            // Select the default policy as high or low - KNL-1015
            level = defaultLowSecurity() ? Level.LOW : Level.HIGH; // default to system setting
        } else if (Level.NONE.equals(level)) {
            checkForEvilTags = false; // disable scan
        }

        String val = strFromBrowser;
        if (val == null || val.length() == 0) {
            return val;
        }

        try {
            if (cleanUTF8) {
                val = removeSurrogates(val);
            }
            if (replaceWhitespaceTags) {
                // normalize all variants of the "<br>" HTML tag to be "<br />\n"
                val = M_patternTagBr.matcher(val).replaceAll("<br />");

                // replace "<p>" with nothing. Replace "</p>" and "<p />" HTML tags with "<br />"
                // val = val.replaceAll("<p>", "");
                // val = val.replaceAll("</p>", "<br />\n");
                // val = val.replaceAll("<p />", "<br />\n");
            }

            if (checkForEvilTags) {
                // use the owasp antisamy processor
                AntiSamy as = antiSamyHigh;
                if (Level.LOW.equals(level)) {
                    as = antiSamyLow;
                }
                try {
                    CleanResults cr = as.scan(val);
                    if (cr.getNumberOfErrors() > 0) {
                        // TODO currently no way to get internationalized versions of error messages
                        for (String errorMsg : cr.getErrorMessages()) {
                            String i18nErrorMsg = new String(errorMsg.getBytes("ISO-8859-1"),"UTF8");
                            formattedTextErrors.append(i18nErrorMsg + "<br/>");
                        }
                    }
                    val = cr.getCleanHTML();

                    // now replace all the A tags WITHOUT a target with _blank (to match the old functionality)
                    if (addBlankTargetToLinks() && StringUtils.isNotBlank(val)) {
                        Matcher m = M_patternAnchorTagWithOutTarget.matcher(val);
                        if (m.find()) {
                            if (StringUtils.isNotBlank(referrerPolicy)) {
                                val = m.replaceAll("$1$2 target=\"_blank\" rel=\"" + referrerPolicy + "\">"); // adds a target and rel to A tags without one
                            } else {
                                val = m.replaceAll("$1$2 target=\"_blank\">"); // adds a target to A tags without one
                            }
                        }
                    }

                    // If there is a referrer policy defined...
                    if (StringUtils.isNotBlank(referrerPolicy) && StringUtils.isNotBlank(val) ) {
                        // If the A tag contains target="_blank" but is not accompanied by a rel attribute, add it
                        Matcher m = M_patternAnchorTagWithTargetBlankAndWithOutRel.matcher(val);
                        if (m.find()) {
                            val = m.replaceAll("$1$2$3 rel=\"" + referrerPolicy + "\">"); // adds a rel to A tags without one
                        }
                    }
                } catch (ScanException e) {
                    // this will match the legacy behavior
                    val = "";
                    M_log.error("processFormattedText: Failure during scan of input html: " + e, e);
                } catch (PolicyException e) {
                    // this is an unrecoverable failure
                    throw new RuntimeException("Unable to access the antiSamy policy file: "+e, e);
                }
            }

            // deal with hardcoded empty space character from Firefox 1.5
            if (val.equalsIgnoreCase("&nbsp;")) {
                val = "";
            }

        } catch (Exception e) {
            // We catch all exceptions here because doing so will usually give the user the
            // opportunity to work around the issue, rather than causing a tool stack trace

            M_log.error("Unexpected error processing text", e);
            formattedTextErrors.append(getResourceLoader().getString("unknown_error_markup") + "\n");
            val = null;
        }

        // KNL-1075: re-do the way error messages are handled
        if (formattedTextErrors.length() > 0) {
            // allow one extra option to control logging in real time if desired
            logErrors = serverConfigurationService.getBoolean("content.cleaner.errors.logged", logErrors);
            if (showErrorToUser || showDetailedErrorToUser) {
                Session session = sessionManager.getCurrentSession();
                if (showDetailedErrorToUser) {
                    session.setAttribute("userWarning", formattedTextErrors.toString());
                } else {
                    session.setAttribute("userWarning", getResourceLoader().getString("content_has_been_cleaned"));
                }
            }
            if (logErrors && M_log.isInfoEnabled()) {
                // KNL-1075 - Logger errors if desired so they can be easily found
                String user = "UNKNOWN";
                try {
                    user = sessionManager.getCurrentSession().getUserEid();
                } catch (Exception e) {
                    try {
                        user = "id="+sessionManager.getCurrentSessionUserId();
                    } catch (Exception e1) {
                        // nothing to do in this case
                    }
                }
                M_log.info("FormattedText Error: user=" + user + " : " + formattedTextErrors.toString()
                        +"\n  -- processing input:\n"+strFromBrowser
                        +"\n  -- resulting output:\n"+val
                        );
            }
            // KNL-1075 - Allows passing kernel tests and preserving legacy behavior
            if (returnErrorToTool) {
                errorMessages.append(formattedTextErrors);
            }
        }

        return val;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#escapeHtmlFormattedText(java.lang.String)
     */
    public String escapeHtmlFormattedText(String value)
    {
        boolean supressNewlines = false;
        return escapeHtmlFormattedText(value, supressNewlines);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#escapeHtmlFormattedTextSupressNewlines(java.lang.String)
     */
    public String escapeHtmlFormattedTextSupressNewlines(String value)
    {
        boolean supressNewlines = true;
        return escapeHtmlFormattedText(value, supressNewlines);
    }

    /**
     * Prepares the given HTML formatted text for output as part of an HTML document. Makes sure that links open up in a new window by setting 'target="_blank"' on anchor tags.
     * 
     * @param value
     *        The formatted text to escape
     * @param supressNewlines
     *        If true, remove newlines ("&lt;br /&gt;") when escaping.
     * @return The string to include in an HTML document.
     */
    private String escapeHtmlFormattedText(String value, boolean supressNewlines)
    {
        if (value == null) return "";
        if (value.length() == 0) return "";
        if (cleanUTF8) {
            value = removeSurrogates(value);
        }

        if (supressNewlines)
        {
            // zap HTML line breaks ("<br />") into plain-old whitespace
            value = M_patternTagBr.matcher(value).replaceAll(" ");
        }

        // make sure that links open up in a new window. This
        // makes sure every anchor tag has a blank target.
        // for example:
        // <a href="http://www.microsoft.com">Microsoft</a>
        // becomes:
        // <a href="http://www.microsoft.com" target="_blank">Microsoft</a>
        // removed for KNL-526
        //value = M_patternAnchorTagWithTarget.matcher(value).replaceAll("$1$2>"); // strips out targets
        //value = M_patternAnchorTag.matcher(value).replaceAll("$1$2$3 target=\"_blank\">"); // adds in blank targets
        // added for KNL-526

        if (addBlankTargetToLinks()) {
            Matcher m = M_patternAnchorTagWithOutTarget.matcher(value);
            if (m.find()) {
                if (StringUtils.isNotBlank(referrerPolicy)) {
                    value = m.replaceAll("$1$2 target=\"_blank\" rel=\"" + referrerPolicy + "\">"); // adds a target and rel to A tags without one
                } else {
                    value = m.replaceAll("$1$2 target=\"_blank\">"); // adds a target to A tags without one
                }
            }
        }

        // If there is a referrer policy defined...
        if (StringUtils.isNotBlank(referrerPolicy) && StringUtils.isNotBlank(value)) {
            // If the A tag contains target="_blank" but is not accompanied by a rel attribute, add it
            Matcher m = M_patternAnchorTagWithTargetBlankAndWithOutRel.matcher(value);
            if (m.find()) {
                value = m.replaceAll("$1$2$3 rel=\"" + referrerPolicy + "\">"); // adds a rel to A tags without one
            }
        }

        return value;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#escapeHtmlFormattedTextarea(java.lang.String)
     */
    public String escapeHtmlFormattedTextarea(String value)
    {
        return escapeHtml(value, false);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#convertPlaintextToFormattedText(java.lang.String)
     */
    public String convertPlaintextToFormattedText(String value)
    {
        return escapeHtml(value, true);
    }

    private final boolean LAZY_CONSTRUCTION = true;

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#escapeHtml(java.lang.String)
     */
    public String escapeHtml(String value) {
        return escapeHtml(value, true);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#escapeHtml(java.lang.String, boolean)
     */
    public String escapeHtml(String value, boolean escapeNewlines) {
        /*
         * Velocity tools depend on this returning empty string (and never null),
         * they also depend on this handling a null input and converting it to null
         */
        String val = "";
        if (value != null && !"".equals(value)) {
            val = StringEscapeUtils.escapeHtml(value);
            if (escapeNewlines && val != null) {
                val = val.replace("\n", "<br/>\n");
            }
        }
        return val;
    } // escapeHtml

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#encodeFormattedTextAttribute(org.w3c.dom.Element, java.lang.String, java.lang.String)
     */
    public void encodeFormattedTextAttribute(Element element, String baseAttributeName, String value)
    {
        // store the formatted text in an attribute called baseAttributeName-html
        Xml.encodeAttribute(element, baseAttributeName + "-html", value);

        // Store the non-formatted (plaintext) version as well
        Xml.encodeAttribute(element, baseAttributeName, convertFormattedTextToPlaintext(value));
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#encodeUnicode(java.lang.String)
     */
    public String encodeUnicode(String value)
    {
        // TODO call method in each process routine
        if (value == null) return "";

        try
        {
            // lazily allocate the StringBuilder
            // only if changes are actually made; otherwise
            // just return the given string without changing it.
            StringBuilder buf = (LAZY_CONSTRUCTION) ? null : new StringBuilder();
            final int len = value.length();
            for (int i = 0; i < len; i++)
            {
                char c = value.charAt(i);
                if (c < 128)
                {
                    if (buf != null) buf.append(c);
                }
                else
                {
                    // escape higher Unicode characters using an
                    // HTML numeric character entity reference like "&#15672;"
                    if (buf == null) buf = new StringBuilder(value.substring(0, i));
                    buf.append("&#");
                    buf.append(Integer.toString((int) c));
                    buf.append(";");
                }
            } // for

            return (buf == null) ? value : buf.toString();
        }
        catch (Exception e)
        {
            M_log.error("Validator.escapeHtml: ", e);
            return "";
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#unEscapeHtml(java.lang.String)
     */
    public String unEscapeHtml(String value)
    {
        if (value == null || value.equals("")) return "";
        value = value.replaceAll("&lt;", "<");
        value = value.replaceAll("&gt;", ">");
        value = value.replaceAll("&amp;", "&");
        value = value.replaceAll("&quot;", "\"");
        return value;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#processAnchor(java.lang.String)
     */
    public String processAnchor(String anchor) {
        String newAnchor = "";
        String href = null;
        String hrefTarget = null;
        String hrefTitle = null;
        String hrefRel = null;

        try {
            // get HREF value
            Matcher matcher = M_patternHref.matcher(anchor);
            if (matcher.find()) {
                href = matcher.group();
            }
            // get target value
            matcher = M_patternHrefTarget.matcher(anchor);
            if (matcher.find()) {
                hrefTarget = matcher.group();
            }
            // get title value
            matcher = M_patternHrefTitle.matcher(anchor);
            if (matcher.find()) {
                hrefTitle = matcher.group();
            }
            // get rel value
            matcher = M_patternHrefRel.matcher(anchor);
            if (matcher.find()) {
                hrefRel = matcher.group();
            }
        } catch (Exception e) {
            M_log.error("FormattedText.processAnchor ", e);
        }

        if (hrefTarget != null) {
            // use the existing one
            hrefTarget = hrefTarget.trim();
            hrefTarget = hrefTarget.replaceAll("\"", ""); // slightly paranoid
            hrefTarget = hrefTarget.replaceAll(">", ""); // slightly paranoid
            hrefTarget = hrefTarget.replaceFirst("target=", ""); // slightly paranoid
            hrefTarget = " target=\"" + hrefTarget + "\"";
        } else {
            // default to _blank if not set and configured to force
            if (addBlankTargetToLinks()) {
                hrefTarget = " target=\"_blank\"";
            }
        }

        if (hrefRel != null) {
            // use the existing one
            hrefRel = hrefRel.trim();
            hrefRel = hrefRel.replaceAll("\"", ""); // slightly paranoid
            hrefRel = hrefRel.replaceAll(">", ""); // slightly paranoid
            hrefRel = hrefRel.replaceFirst("rel=", ""); // slightly paranoid
            hrefRel = " rel=\"" + hrefRel + "\"";
        } else if (hrefRel == null && " target=\"_blank\"".equals(hrefTarget) && StringUtils.isNotBlank(referrerPolicy)) {
            // target is _blank but has no rel attribute
            hrefRel = " rel=\"" + referrerPolicy + "\"";
        }

        if (hrefTitle != null) {
            // use the existing one
            hrefTitle = hrefTitle.trim();
            hrefTitle = hrefTitle.replaceAll("\"", ""); // slightly paranoid
            hrefTitle = hrefTitle.replaceAll(">", ""); // slightly paranoid
            hrefTitle = hrefTitle.replaceFirst("title=", ""); // slightly paranoid
            hrefTitle = " title=\"" + hrefTitle + "\"";
        }

        // open in a new window
        if (href != null) {
            href = href.replaceAll("\"", "");
            href = href.replaceAll(">", "");
            href = href.replaceFirst("href=", "href=\"");
            newAnchor = "<a " + href + "\"" + hrefTarget;
            if (hrefRel != null) {
                newAnchor += hrefRel;
            }
            if (hrefTitle != null)
            {
                newAnchor += hrefTitle;
            }
            newAnchor += ">";
        } else {
            M_log.debug("FormattedText.processAnchor href == null");
            newAnchor = anchor; // default to the original one so we don't lose the anchor
        }
        return newAnchor;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#processEscapedHtml(java.lang.String)
     */
    public String processEscapedHtml(final String source) {
        if (source == null)
            return "";
        if (source.equals(""))
            return "";

        String html = null;
        try {
            // TODO call encodeUnicode in other process routine
            html = encodeUnicode(source);
        } catch (Exception e) {
            M_log.error("FormattedText.processEscapedHtml encodeUnicode(source):"+e, e);
        }
        try {
            // to use the FormattedText functions
            html = unEscapeHtml(html);
        } catch (Exception e) {
            M_log.error("FormattedText.processEscapedHtml unEscapeHtml(Html):"+e, e);
        }

        return processFormattedText(html, new StringBuilder());
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#decodeFormattedTextAttribute(org.w3c.dom.Element, java.lang.String)
     */
    public String decodeFormattedTextAttribute(Element element, String baseAttributeName)
    {
        String ret;

        // first check if an HTML-encoded attribute exists, for example "foo-html", and use it if available
        ret = StringUtils.trimToNull(Xml.decodeAttribute(element, baseAttributeName + "-html"));
        if (ret != null) return ret;

        // next try the older kind of formatted text like "foo-formatted", and convert it if found
        ret = StringUtils.trimToNull(Xml.decodeAttribute(element, baseAttributeName + "-formatted"));
        ret = convertOldFormattedText(ret);
        if (ret != null) return ret;

        // next try just a plaintext attribute and convert the plaintext to formatted text if found
        // convert from old plaintext instructions to new formatted text instruction
        ret = Xml.decodeAttribute(element, baseAttributeName);
        ret = convertPlaintextToFormattedText(ret);
        return ret;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#convertFormattedTextToPlaintext(java.lang.String)
     */
    public String convertFormattedTextToPlaintext(String value)
    {
        if (value == null) return null;
        if (value.length() == 0) return "";

        // strip out newlines
        value = M_patternNewline.matcher(value).replaceAll("");

        // convert "<br />" to newline
        value = M_patternTagBr.matcher(value).replaceAll("\n");

        // strip out all the HTML-style tags so that:
        // <font face="verdana">Something</font> <b>Something else</b>
        // becomes:
        // Something Something else
        value = M_patternTag.matcher(value).replaceAll("");

        // Replace HTML character entity references (like &gt;)
        // with the plain Unicode characters to which they refer.
        String ref;
        char val;
        for (int i = 0; i < M_htmlCharacterEntityReferences.length; i++)
        {
            ref = M_htmlCharacterEntityReferences[i];
            if (value.indexOf(ref) >= 0)
            {
                val = M_htmlCharacterEntityReferencesUnicode[i];
                // System.out.println("REPLACING "+ref+" WITH UNICODE CHARACTER #"+val+" WHICH IN JAVA IS "+Character.toString(val));
                value = value.replaceAll(ref, Character.toString(val));
            }
        }

        // Replace HTML numeric character entity references (like &#nnnn; or &#xnnnn;)
        // with the plain Unicode characters to which they refer.
        value = decodeNumericCharacterReferences(value);

        return value;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#convertOldFormattedText(java.lang.String)
     */
    public String convertOldFormattedText(String value)
    {
        // previously, formatted text used "\n" to indicate a line break.
        // now we use "<br />\n" as a line break. This code converts old
        // formatted text to new formatted text.
        if (value == null) return null;
        if (value.length() == 0) return "";

        value = M_patternNewline.matcher(value).replaceAll("<br />\n");
        return value;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#trimFormattedText(java.lang.String, int, java.lang.StringBuilder)
     */
    public boolean trimFormattedText(String formattedText, final int maxNumOfChars, StringBuilder strTrimmed)
    {
        // This should return a formatted text substring which contains formatting, but which
        // isn't broken in the middle of formatting, eg, "<strong>Hi there</stro" It also shouldn't
        // break HTML character entities such as "&gt;".

        String str = formattedText;
        strTrimmed.setLength(0);
        strTrimmed.append(str);
        if (str == null) return false;

        int count = 0; // number of displayed characters seen so far
        int pos = 0; // raw position within the formatted text string
        int len = str.length();
        Stack<String> tags = new Stack<String>(); // currently open tags (may need to be closed at the end)
        while (pos < len && count < maxNumOfChars)
        {
            while (pos < len && str.charAt(pos) == '<')
            {
                // currently parsing a tag
                pos++;

                if (pos < len && str.charAt(pos) == '!')
                {
                    // parsing an HTML comment
                    if (pos + 2 < len)
                    {
                        if (str.charAt(pos + 1) == '-' && str.charAt(pos + 2) == '-')
                        {
                            // skip past the close of the comment tag
                            int close = str.indexOf("-->", pos);
                            if (close != -1)
                            {
                                pos = close + 3;
                                continue;
                            }
                        }
                    }
                }

                if (pos < len && str.charAt(pos) == '/')
                {
                    // currently parsing an closing tag
                    if (!tags.isEmpty()) tags.pop();
                    while (pos < len && str.charAt(pos) != '>')
                        pos++;
                    pos++;
                    continue;
                }
                // capture the name of the opening tag and put it on the stack of open tags
                int taglen = 0;
                String tag;
                while (pos < len && str.charAt(pos) != '>' && !Character.isWhitespace(str.charAt(pos)))
                {
                    pos++;
                    taglen++;
                }
                tag = str.substring(pos - taglen, pos);
                tags.push(tag);

                while (pos < len && str.charAt(pos) != '>')
                    pos++;

                if (tag.length() == 0)
                {
                    if (!tags.isEmpty()) tags.pop();
                    continue;
                }
                if (str.charAt(pos - 1) == '/') if (!tags.isEmpty()) tags.pop(); // singleton tag like "<br />" has no closing tag
                if (tag.charAt(0) == '!') if (!tags.isEmpty()) tags.pop(); // comment tag like "<!-- comment -->", so just ignore it
                if ("br".equalsIgnoreCase(tag)) if (!tags.isEmpty()) tags.pop();
                if ("hr".equalsIgnoreCase(tag)) if (!tags.isEmpty()) tags.pop();
                if ("meta".equalsIgnoreCase(tag)) if (!tags.isEmpty()) tags.pop();
                if ("link".equalsIgnoreCase(tag)) if (!tags.isEmpty()) tags.pop();
                pos++;
            }

            if (pos < len && str.charAt(pos) == '&')
            {
                // HTML character entity references, like "&gt;"
                // count this as one single character
                while (pos < len && str.charAt(pos) != ';')
                    pos++;
            }

            if (pos < len)
            {
                count++;
                pos++;
            }
        }

        // close any unclosed tags
        strTrimmed.setLength(0);
        strTrimmed.append(str.substring(0, pos));
        while (tags.size() > 0)
        {
            strTrimmed.append("</");
            strTrimmed.append(tags.pop());
            strTrimmed.append(">");
        }

        boolean didTrim = (count == maxNumOfChars);
        return didTrim;

    } // trimFormattedText()

    /* (non-Javadoc)
     * @see org.sakaiproject.utils.impl.FormattedText#decodeNumericCharacterReferences(java.lang.String)
     */
    public String decodeNumericCharacterReferences(String value)
    {
        // lazily allocate StringBuilder only if needed
        // buf is not null ONLY when a numeric character reference
        // is found - otherwise, buf is not used at all
        StringBuilder buf = null;
        final int valuelength = value.length();
        for (int i = 0; i < valuelength; i++)
        {
            if ((value.charAt(i) == '&' || value.charAt(i) == '^') && (i + 2 < valuelength)
                    && (value.charAt(i + 1) == '#' || value.charAt(i + 1) == '^'))
            {
                int pos = i + 2;
                boolean hex = false;
                if ((value.charAt(pos) == 'x') || (value.charAt(pos) == 'X'))
                {
                    pos++;
                    hex = true;
                }
                StringBuilder num = new StringBuilder(6);
                while (pos < valuelength && value.charAt(pos) != ';' && value.charAt(pos) != '^')
                {
                    num.append(value.charAt(pos));
                    pos++;
                }
                if (pos < valuelength)
                {
                    try
                    {
                        int val = Integer.parseInt(num.toString(), (hex ? 16 : 10));
                        // Found an HTML numeric character reference!
                        if (buf == null)
                        {
                            buf = new StringBuilder();
                            buf.append(value.substring(0, i));
                        }

                        buf.append((char) val);
                        i = pos;
                    }
                    catch (Exception ignore)
                    {
                        if (buf != null) buf.append(value.charAt(i));
                    }
                }
                else
                {
                    if (buf != null) buf.append(value.charAt(i));
                }
            }
            else
            {
                if (buf != null) buf.append(value.charAt(i));
            }
        }

        if (buf != null) value = buf.toString();

        return value;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#encodeUrlsAsHtml(java.lang.String)
     */
    public String encodeUrlsAsHtml(String text) {
        // MOVED FROM Web
        Pattern p = Pattern.compile("(?<!href=['\"]{1})(((https?|s?ftp|ftps|file|smb|afp|nfs|(x-)?man|gopher|txmt)://|mailto:)[-:;@a-zA-Z0-9_.,~%+/?=&#]+(?<![.,?:]))");
        Matcher m = p.matcher(text);
        StringBuffer buf = new StringBuffer();
        while(m.find()) {
            String matchedUrl = m.group();
            m.appendReplacement(buf, "<a href=\"" + unEscapeHtml(matchedUrl) + "\">$1</a>");
        }
        m.appendTail(buf);
        return buf.toString();
    }

    public String escapeJavascript(String value) {
        if (value == null || "".equals(value)) return "";
        try
        {
            StringBuilder buf = new StringBuilder();

            // prepend 'i' if first character is not a letter
            if (!java.lang.Character.isLetter(value.charAt(0)))
            {
                buf.append("i");
            }

            // change non-alphanumeric characters to 'x'
            for (int i = 0; i < value.length(); i++)
            {
                char c = value.charAt(i);
                if (!java.lang.Character.isLetterOrDigit(c))
                {
                    buf.append("x");
                }
                else
                {
                    buf.append(c);
                }
            }

            String rv = buf.toString();
            return rv;
        }
        catch (Exception e)
        {
            M_log.error("escapeJavascript: ", e);
            return value;
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#escapeJsQuoted(java.lang.String)
     */
    public String escapeJsQuoted(String value) {
        return StringEscapeUtils.escapeJavaScript(value);
    }

    /** These characters are escaped when making a URL */
    // protected static final String ESCAPE_URL = "#%?&='\"+ ";
    // not '/' as that is assumed to be part of the path
    protected static final String ESCAPE_URL = "$&+,:;=?@ '\"<>#%{}|\\^~[]`";

    /**
     * These can't be encoded in URLs safely even using %nn notation, so encode them using our own custom URL encoding
     */
    protected static final String ESCAPE_URL_SPECIAL = "^?;";

    public String escapeUrl(String id) {
        if (id == null) return "";
        id = id.trim();
        try
        {
            // convert the string to bytes in UTF-8
            byte[] bytes = id.getBytes("UTF-8");

            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < bytes.length; i++)
            {
                byte b = bytes[i];
                // escape ascii control characters, ascii high bits, specials
                if (ESCAPE_URL_SPECIAL.indexOf((char) b) != -1)
                {
                    buf.append("^^x"); // special funky way to encode bad URL characters 
                    buf.append(toHex(b));
                    buf.append('^');
                }
                // 0x1F is the last control character
                // 0x7F is DEL chatecter
                // 0x80 is the start of the top of the 256bit set.
                else if ((ESCAPE_URL.indexOf((char) b) != -1) || (b <= 0x1F) || (b == 0x7F) || (b >= 0x80))
                {
                    buf.append("%");
                    buf.append(toHex(b));
                }
                else
                {
                    buf.append((char) b);
                }
            }

            String rv = buf.toString();
            return rv;
        }
        catch (UnsupportedEncodingException e)
        {
            M_log.error("Validator.escapeUrl: ", e);
            return "";
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#validateURL(java.lang.String)
     */

    private static final String PROTOCOL_PREFIX = "http:";
    private static final String HOST_PREFIX = "http://127.0.0.1";
    private static final String ABOUT_BLANK = "about:blank";

    public boolean validateURL(String urlToValidate) {
        if (StringUtils.isBlank(urlToValidate)) return false;

		if ( ABOUT_BLANK.equals(urlToValidate) ) return true;

        // Check if the url is "Escapable" - run through the URL-URI-URL gauntlet
        String escapedURL = sanitizeHrefURL(urlToValidate);
        if ( escapedURL == null ) return false;

        // For a protocol-relative URL, we validate with protocol attached 
        // RFC 1808 Section 4
        if ((urlToValidate.startsWith("//")) && (urlToValidate.indexOf("://") == -1))
        {
            urlToValidate = PROTOCOL_PREFIX + urlToValidate;
        }

        // For a site-relative URL, we validate with host name and protocol attached 
        // SAK-13787 SAK-23752
        if ((urlToValidate.startsWith("/")) && (urlToValidate.indexOf("://") == -1))
        {
            urlToValidate = HOST_PREFIX + urlToValidate;
        }

        // Validate the url
        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
        return urlValidator.isValid(urlToValidate);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#sanitizeHrefURL(java.lang.String)
     */
    public String sanitizeHrefURL(String urlToSanitize) {
        if ( urlToSanitize == null ) return null;
        if (StringUtils.isBlank(urlToSanitize)) return null;
		if ( ABOUT_BLANK.equals(urlToSanitize) ) return ABOUT_BLANK;

        boolean trimProtocol = false;
        boolean trimHost = false;
        // For a protocol-relative URL, we validate with protocol attached 
        // RFC 1808 Section 4
        if ((urlToSanitize.startsWith("//")) && (urlToSanitize.indexOf("://") == -1))
        {
            urlToSanitize = PROTOCOL_PREFIX + urlToSanitize;
            trimProtocol = true;
        }

        // For a site-relative URL, we validate with host name and protocol attached 
        // SAK-13787 SAK-23752
        if ((urlToSanitize.startsWith("/")) && (urlToSanitize.indexOf("://") == -1))
        {
            urlToSanitize = HOST_PREFIX + urlToSanitize;
            trimHost = true;
        }

        // KNL-1105
        try {
            URL rawUrl = new URL(urlToSanitize);
            URI uri = new URI(rawUrl.getProtocol(), rawUrl.getUserInfo(), rawUrl.getHost(), 
                rawUrl.getPort(), rawUrl.getPath(), rawUrl.getQuery(), rawUrl.getRef());
            URL encoded = uri.toURL();
            String retval = encoded.toString();

            // Un-trim the added bits
            if ( trimHost && retval.startsWith(HOST_PREFIX) ) 
            {
                retval = retval.substring(HOST_PREFIX.length());
            }

            if ( trimProtocol && retval.startsWith(PROTOCOL_PREFIX) ) 
            {
                retval = retval.substring(PROTOCOL_PREFIX.length());
            }

            // http://stackoverflow.com/questions/7731919/why-doesnt-uri-escape-escape-single-quotes
            // We want these to be usable in JavaScript string values so we map single quotes
            retval = retval.replace("'", "%27");
            // We want anchors to work
            retval = retval.replace("%23", "#");
            // Sorry - these just need to come out - they cause to much trouble
            // Note that ampersand is not encoded as it is used for parameters.
            retval = retval.replace("&#", "");
            retval = retval.replace("%25","%");
            return retval;
        } catch ( java.net.URISyntaxException e ) {
            M_log.info("Failure during encode of href url: " + e);
            return null;
        } catch ( java.net.MalformedURLException e ) {
            M_log.info("Failure during encode of href url: " + e);
            return null;
        }
    }

    @Override
    public String stripHtmlFromText(String text, boolean smartSpacing) {
        return stripHtmlFromText(text, smartSpacing, false);
    }

    @Override
    public String stripHtmlFromText(String text, boolean smartSpacing, boolean stripEscapeSequences)
    {
        if (StringUtils.isBlank(text)) return text;

        if (smartSpacing) {
            text = text.replaceAll("/br>", "/br> ").replaceAll("/p>", "/p> ").replaceAll("/tr>", "/tr> ");
        }

        if (stripEscapeSequences) {
            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(text);
            org.jsoup.nodes.Element body = document.body();
            //remove any html tags, unescape any escape characters
            text = body.text();
            //&nbsp; are converted to char code 160, java doesn't treat it like whitespace, so replace it with ' '
            text = text.replace((char)160, ' ');
        } else {
            text = org.jsoup.Jsoup.clean(text, "", org.jsoup.safety.Whitelist.none(), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false).outline(false));
        }

        if (smartSpacing || stripEscapeSequences) {
            text = text.replaceAll("\\s+", " ");
        }

        return text.trim();
    }

    public NumberFormat getNumberFormat(Integer maxFractionDigits, Integer minFractionDigits, Boolean groupingUsed) {
		NumberFormat nbFormat = NumberFormat.getInstance();				
		try {
			nbFormat = NumberFormat.getNumberInstance(new ResourceLoader().getLocale());
		} catch (Exception e) {
			M_log.error("Error while retrieving local number format, using default ", e);
		}
		if (maxFractionDigits!=null) nbFormat.setMaximumFractionDigits(maxFractionDigits);
		if (minFractionDigits!=null) nbFormat.setMinimumFractionDigits(minFractionDigits);
		if (groupingUsed!=null) nbFormat.setGroupingUsed(groupingUsed);
		return nbFormat;
    }

    public NumberFormat getNumberFormat() {
    	return getNumberFormat(null,null,null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#getHTMLBody(java.lang.String)
     */
    public String getHtmlBody(String text) {
        if (StringUtils.isBlank(text)) return text;
        org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(text);
        document.outputSettings().prettyPrint(false);
        return document.body().html();
    }
    
    public String getDecimalSeparator() {
		return ((DecimalFormat)getNumberFormat()).getDecimalFormatSymbols().getDecimalSeparator()+"";
    }
    
    /**
     * SAK-23567 Gets the shortened version of the title
     *
     * Controlled by "site.title.cut.method", "site.title.cut.maxlength", and "site.title.cut.separator"
     *
     * @param text the full site title (or desc) to shorten
     * @param maxLength maximum length for the string before it is shortened (and after shortening) (null defaults to 25)
     * @param separator the separator string to use (null defaults to '...')
     * @param cutMethod the string key method for cutting (null defaults to '100:0')
     * @return the shortened string
     */
    public String makeShortenedText(String text, Integer maxLength, String separator, String cutMethod) {
        // this method defines the defaults for the 3 configuration options
        if (maxLength == null || maxLength < 1) {
        	// SAK-31985: New property needed to control the site title
            maxLength = serverConfigurationService.getInt("site.title.cut.maxlength", serverConfigurationService.getInt("site.title.maxlength", 25));
        }
        if (separator == null) {
            separator = serverConfigurationService.getString("site.title.cut.separator", " ...");
        }
        if (cutMethod == null) {
            cutMethod = serverConfigurationService.getString("site.title.cut.method", "100:0");
        }
        return makeShortText(text, cutMethod, maxLength, separator);
    }

    /**
     * TESTING ONLY
     * SAK-23567 Gets an array with 2 int values {left,right}
     * 	left: left percentage (before cut separator)
     * 	right: right percentage (after separator)
     */
    protected int[] getCutMethod(String siteTitleCutMethodString) {
        String[] siteTitleCutMethod = siteTitleCutMethodString.split(":");
        int[] cutMethod = new int[]{100,0};
        try {
            if (siteTitleCutMethod.length==2) {
                cutMethod[0] = Integer.parseInt(siteTitleCutMethod[0]);
                cutMethod[1] = Integer.parseInt(siteTitleCutMethod[1]);
                if (cutMethod[0]+cutMethod[1]!=100) {
                    throw new Exception("Invalid cut method values: "+cutMethod[0]+" + "+cutMethod[1]+" != 100");
                }
            }
        } catch (Throwable ex) {
            cutMethod[0] = 100; cutMethod[1] = 0;
        }
        return cutMethod;
    }

    /**
     * TESTING ONLY
     * use {@link #makeShortenedText(String)} instead
     *
     * SAK-23567 Gets the resumed version of the title
     *
     * @param text
     * @param cutMethod
     * @param siteTitleMaxLength
     * @param cutSeparator
     * @return the trimmed string
     */
    protected String makeShortText(String text, String cutMethod, int siteTitleMaxLength, String cutSeparator) {
        String result = text;
        if ( result != null ) {
            result = result.trim();
            if ( result.length() > siteTitleMaxLength && siteTitleMaxLength >= 10 ) {
                int[] siteTitleCutMethod = getCutMethod(cutMethod);
                int begin = Math.round(((siteTitleMaxLength-cutSeparator.length())*siteTitleCutMethod[0])/100);
                int end = Math.round(((siteTitleMaxLength-cutSeparator.length())*siteTitleCutMethod[1])/100);
                // Adjust odd character to the begin
                begin += (siteTitleMaxLength - (begin + cutSeparator.length() + end));
                result = ((begin>0)?result.substring(0,begin):"") + cutSeparator +((end>0)?result.substring(result.length()-end):"");
            } else if ( result.length() > siteTitleMaxLength ) {
                result = result.substring(0,siteTitleMaxLength);
            }
        }
        return result;
    }


    // PRIVATE STUFF

    /**
     * Returns a hex representation of a byte.
     * 
     * @param b
     *        The byte to convert to hex.
     * @return The 2-digit hex value of the supplied byte.
     */
    protected static final String toHex(byte b) {
        char ret[] = new char[2];

        ret[0] = hexDigit((b >>> 4) & (byte) 0x0F);
        ret[1] = hexDigit((b >>> 0) & (byte) 0x0F);

        return new String(ret);
    }

    /**
     * Returns the hex digit corresponding to a number between 0 and 15.
     * 
     * @param i
     *        The number to get the hex digit for.
     * @return The hex digit corresponding to that number.
     * @exception java.lang.IllegalArgumentException
     *            If supplied digit is not between 0 and 15 inclusive.
     */
    protected static final char hexDigit(int i)
    {
        switch (i)
        {
            case 0:
                return '0';
            case 1:
                return '1';
            case 2:
                return '2';
            case 3:
                return '3';
            case 4:
                return '4';
            case 5:
                return '5';
            case 6:
                return '6';
            case 7:
                return '7';
            case 8:
                return '8';
            case 9:
                return '9';
            case 10:
                return 'A';
            case 11:
                return 'B';
            case 12:
                return 'C';
            case 13:
                return 'D';
            case 14:
                return 'E';
            case 15:
                return 'F';
        }

        throw new IllegalArgumentException("Invalid digit:" + i);
    }

    /**
     * HTML character entity references. These abbreviations are used in HTML to escape certain Unicode characters, including characters used in HTML markup. These character entity references were taken directly from the HTML 4.0 specification at:
     * 
     * http://www.w3.org/TR/REC-html40/sgml/entities.html
     */
    private final String[] M_htmlCharacterEntityReferences = { "&nbsp;", "&iexcl;", "&cent;", "&pound;", "&curren;",
            "&yen;", "&brvbar;", "&sect;", "&uml;", "&copy;", "&ordf;", "&laquo;", "&not;", "&shy;", "&reg;", "&macr;", "&deg;",
            "&plusmn;", "&sup2;", "&sup3;", "&acute;", "&micro;", "&para;", "&middot;", "&cedil;", "&sup1;", "&ordm;", "&raquo;",
            "&frac14;", "&frac12;", "&frac34;", "&iquest;", "&Agrave;", "&Aacute;", "&Acirc;", "&Atilde;", "&Auml;", "&Aring;",
            "&AElig;", "&Ccedil;", "&Egrave;", "&Eacute;", "&Ecirc;", "&Euml;", "&Igrave;", "&Iacute;", "&Icirc;", "&Iuml;",
            "&ETH;", "&Ntilde;", "&Ograve;", "&Oacute;", "&Ocirc;", "&Otilde;", "&Ouml;", "&times;", "&Oslash;", "&Ugrave;",
            "&Uacute;", "&Ucirc;", "&Uuml;", "&Yacute;", "&THORN;", "&szlig;", "&agrave;", "&aacute;", "&acirc;", "&atilde;",
            "&auml;", "&aring;", "&aelig;", "&ccedil;", "&egrave;", "&eacute;", "&ecirc;", "&euml;", "&igrave;", "&iacute;",
            "&icirc;", "&iuml;", "&eth;", "&ntilde;", "&ograve;", "&oacute;", "&ocirc;", "&otilde;", "&ouml;", "&divide;",
            "&oslash;", "&ugrave;", "&uacute;", "&ucirc;", "&uuml;", "&yacute;", "&thorn;", "&yuml;", "&fnof;", "&Alpha;",
            "&Beta;", "&Gamma;", "&Delta;", "&Epsilo;", "&Zeta;", "&Eta;", "&Theta;", "&Iota;", "&Kappa;", "&Lambda;", "&Mu;",
            "&Nu;", "&Xi;", "&Omicro;", "&Pi;", "&Rho;", "&Sigma;", "&Tau;", "&Upsilo;", "&Phi;", "&Chi;", "&Psi;", "&Omega;",
            "&alpha;", "&beta;", "&gamma;", "&delta;", "&epsilo;", "&zeta;", "&eta;", "&theta;", "&iota;", "&kappa;", "&lambda;",
            "&mu;", "&nu;", "&xi;", "&omicro;", "&pi;", "&rho;", "&sigmaf;", "&sigma;", "&tau;", "&upsilo;", "&phi;", "&chi;",
            "&psi;", "&omega;", "&thetas;", "&upsih;", "&piv;", "&bull;", "&hellip;", "&prime;", "&Prime;", "&oline;", "&frasl;",
            "&weierp;", "&image;", "&real;", "&trade;", "&alefsy;", "&larr;", "&uarr;", "&rarr;", "&darr;", "&harr;", "&crarr;",
            "&lArr;", "&uArr;", "&rArr;", "&dArr;", "&hArr;", "&forall;", "&part;", "&exist;", "&empty;", "&nabla;", "&isin;",
            "&notin;", "&ni;", "&prod;", "&sum;", "&minus;", "&lowast;", "&radic;", "&prop;", "&infin;", "&ang;", "&and;", "&or;",
            "&cap;", "&cup;", "&int;", "&there4;", "&sim;", "&cong;", "&asymp;", "&ne;", "&equiv;", "&le;", "&ge;", "&sub;",
            "&sup;", "&nsub;", "&sube;", "&supe;", "&oplus;", "&otimes;", "&perp;", "&sdot;", "&lceil;", "&rceil;", "&lfloor;",
            "&rfloor;", "&lang;", "&rang;", "&loz;", "&spades;", "&clubs;", "&hearts;", "&diams;", "&quot;", "&amp;", "&lt;",
            "&gt;", "&OElig;", "&oelig;", "&Scaron;", "&scaron;", "&Yuml;", "&circ;", "&tilde;", "&ensp;", "&emsp;", "&thinsp;",
            "&zwnj;", "&zwj;", "&lrm;", "&rlm;", "&ndash;", "&mdash;", "&lsquo;", "&rsquo;", "&sbquo;", "&ldquo;", "&rdquo;",
            "&bdquo;", "&dagger;", "&Dagger;", "&permil;", "&lsaquo;", "&rsaquo;", "&euro;" };

    /**
     * These character entity references were taken directly from the HTML 4.0 specification at:
     * 
     * http://www.w3.org/TR/REC-html40/sgml/entities.html
     */
    private final char[] M_htmlCharacterEntityReferencesUnicode = { 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170,
            171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194,
            195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218,
            219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242,
            243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255, 402, 913, 914, 915, 916, 917, 918, 919, 920, 921, 922,
            923, 924, 925, 926, 927, 928, 929, 931, 932, 933, 934, 935, 936, 937, 945, 946, 947, 948, 949, 950, 951, 952, 953, 954,
            955, 956, 957, 958, 959, 960, 961, 962, 963, 964, 965, 966, 967, 968, 969, 977, 978, 982, 8226, 8230, 8242, 8243, 8254,
            8260, 8472, 8465, 8476, 8482, 8501, 8592, 8593, 8594, 8595, 8596, 8629, 8656, 8657, 8658, 8659, 8660, 8704, 8706, 8707,
            8709, 8711, 8712, 8713, 8715, 8719, 8721, 8722, 8727, 8730, 8733, 8734, 8736, 8743, 8744, 8745, 8746, 8747, 8756, 8764,
            8773, 8776, 8800, 8801, 8804, 8805, 8834, 8835, 8836, 8838, 8839, 8853, 8855, 8869, 8901, 8968, 8969, 8970, 8971, 9001,
            9002, 9674, 9824, 9827, 9829, 9830, 34, 38, 60, 62, 338, 339, 352, 353, 376, 710, 732, 8194, 8195, 8201, 8204, 8205,
            8206, 8207, 8211, 8212, 8216, 8217, 8218, 8220, 8221, 8222, 8224, 8225, 8240, 8249, 8250, 8364 };

}
