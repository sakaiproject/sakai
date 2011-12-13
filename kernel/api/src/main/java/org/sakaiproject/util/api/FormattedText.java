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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util.api;

import org.w3c.dom.Element;

/**
 * These Utils provide support for user entry of formatted text (typically HTML). This
 * includes text formatting in user input such as bold, underline, and fonts.
 * There are also utils which support other kinds of text processing (e.g. javascript)
 * and escaping (e.g. SQL). Generally anything related to text which is not simply
 * plaintext and has some kind of formatting.
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public interface FormattedText {

    /**
     * This is maintained for backwards compatibility
     * @see #processFormattedText(String, StringBuilder)
     * @deprecated since Nov 2007, use {@link #processFormattedText(String, StringBuilder)} instead
     */
    public String processFormattedText(final String strFromBrowser, StringBuffer errorMessages);

    /**
     * Processes and validates user-entered HTML received from the web browser (from the WYSIWYG editor). Validates that the user input follows the Sakai formatted text specification; disallows dangerous stuff such as &lt;SCRIPT&gt; JavaScript tags.
     * Encodes the text according to the formatted text specification, for the rest of the system to use.
     * <br/>
     * Use {@link #processFormattedText(String, StringBuilder, boolean)} if you need the behavior of the old sakai html cleaner processor
     * 
     * @param strFromBrowser
     *        The formatted text as sent from the web browser (from the WYSIWYG editor)
     * @param errorMessages
     *        User-readable error messages will be returned here.
     * @return The validated processed formatted text, ready for use by the system.
     */
    public String processFormattedText(final String strFromBrowser, StringBuilder errorMessages);

    /**
     * Processes and validates user-entered HTML received from the web browser (from the WYSIWYG editor). Validates that the user input follows the Sakai formatted text specification; disallows dangerous stuff such as &lt;SCRIPT&gt; JavaScript tags.
     * Encodes the text according to the formatted text specification, for the rest of the system to use.
     * 
     * @param strFromBrowser
     *        The formatted text as sent from the web browser (from the WYSIWYG editor)
     * @param errorMessages
     *        User-readable error messages will be returned here.
     * @param useLegacySakaiCleaner if true the old html cleaner is used, if false the new OWASP antisamy cleaner is used
     * @return The validated processed formatted text, ready for use by the system.
     */
    public String processFormattedText(final String strFromBrowser, StringBuilder errorMessages, boolean useLegacySakaiCleaner);

    /**
     * Process an HTML document that has been edited using the formatted text widget. The document can contain any valid HTML; it will NOT be checked to eliminate things like image tags, script tags, etc, because it is its own document.
     * 
     * @param strFromBrowser
     * @param errorMessages
     */
    public String processHtmlDocument(final String strFromBrowser, StringBuilder errorMessages);

    /**
     * Processes and validates HTML formatted text received from the web browser (from the WYSIWYG editor). Validates that the user input follows the Sakai formatted text specification; can disallow dangerous stuff such as &lt;SCRIPT&gt; JavaScript tags.
     * Encodes the text according to the formatted text specification, for the rest of the system to use.
     * <br/>
     * Use {@link #processFormattedText(String, StringBuilder, boolean, boolean, boolean)} if you need the behavior of the old sakai html cleaner processor
     * 
     * @param strFromBrowser
     *        The formatted text as sent from the web browser (from the WYSIWYG editor)
     * @param errorMessages
     *        User-readable error messages will be returned here.
     * @param checkForEvilTags
     *        If true, check for tags and attributes that shouldn't be in formatted text
     * @param replaceWhitespaceTags
     *        If true, clean up line breaks to be like "&lt;br /&gt;".
     * @return The validated processed HTML formatted text, ready for use by the system.
     */
    public String processFormattedText(final String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
            boolean replaceWhitespaceTags);

    /**
     * Processes and validates HTML formatted text received from the web browser (from the WYSIWYG editor). Validates that the user input follows the Sakai formatted text specification; can disallow dangerous stuff such as &lt;SCRIPT&gt; JavaScript tags.
     * Encodes the text according to the formatted text specification, for the rest of the system to use.
     * 
     * @param strFromBrowser
     *        The formatted text as sent from the web browser (from the WYSIWYG editor)
     * @param errorMessages
     *        User-readable error messages will be returned here.
     * @param checkForEvilTags
     *        If true, check for tags and attributes that shouldn't be in formatted text
     * @param replaceWhitespaceTags
     *        If true, clean up line breaks to be like "&lt;br /&gt;".
     * @param useLegacySakaiCleaner if true the old html cleaner is used, if false the new OWASP antisamy cleaner is used
     * @return The validated processed HTML formatted text, ready for use by the system.
     */
    public String processFormattedText(final String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
            boolean replaceWhitespaceTags, boolean useLegacySakaiCleaner);

    /**
     * Prepares the given HTML formatted text for output as part of an HTML document.
     * 
     * @param value
     *        The formatted text to output in an HTML document.
     * @return The string to include in an HTML document.
     * @see FormattedText#escapeHtml(String, boolean)
     */
    public String escapeHtmlFormattedText(String value);

    /**
     * Prepares the given HTML formatted text for output as part of an HTML document, removing newlines ("&lt;br /&gt;").
     * 
     * @param value
     *        The formatted text to output in an HTML document.
     * @return The string to include in an HTML document.
     * @see FormattedText#escapeHtml(String, boolean)
     */
    public String escapeHtmlFormattedTextSupressNewlines(String value);

    /**
     * Prepares the given formatted text for editing within the WYSIWYG editor. All HTML meta-characters in the string will be escaped.
     * 
     * @param value
     *        The formatted text to escape
     * @return The string to use as the value of the formatted textarea widget
     * @see FormattedText#escapeHtml(String, boolean)
     */
    public String escapeHtmlFormattedTextarea(String value);

    /**
     * Converts the given plain text into HTML formatted text. Conversion to formatted text involves escaping characters that are used for formatting (such as the '<' character). Also converts plaintext line breaks into HTML line breaks ("<br />
     * ").
     * 
     * @param value
     *        The plain text to convert to formatted text
     * @return The converted plain text, now as formatted text
     */
    public String convertPlaintextToFormattedText(String value);

    /**
     * Escape a plaintext string so that it can be output as part of an HTML document. 
     * Amperstand, greater-than, less-than, newlines, etc, will be escaped so that they display (instead of being interpreted as formatting).
     * Automatically converts newlines.
     * 
     * @param value
     *        The string to escape.
     * @return value fully escaped for HTML.
     * @see #escapeHtml(String, boolean)
     */
    public String escapeHtml(String value);

    /**
     * Escape the given value so that it appears as-is in HTML - 
     * that is, HTML meta-characters like '<' are escaped to HTML character entity references like '&lt;'. 
     * Markup, amper, quote are escaped. Whitespace is not.
     * 
     * @param value The string containing html to escape (can be null or "")
     * @param escapeNewlines
     *        Whether to convert newlines (\n) to "&lt;br /&gt;\n" so that they appear as HTML line breaks.
     * @return value fully escaped for HTML (this will never return a null but will instead return empty string - "")
     */
    public String escapeHtml(String value, boolean escapeNewlines);

    /**
     * Store the given formatted text in the given XML element; stores both a formatted text representation, and a plaintext representation (plaintext means the formatting has been stripped).
     */
    public void encodeFormattedTextAttribute(Element element, String baseAttributeName, String value);

    /**
     * Returns a String with characters above 128 as entity references.
     * 
     * @param value
     *        The text to encode.
     * @return The encoded text.
     */
    public String encodeUnicode(String value);

    /**
     * For converting plain-text URLs in a String to HTML &lt;a&gt; tags
     * Any URLs in the source text that happen to be already in a &lt;a&gt; tag will be unaffected.
     * 
     * @param text the plain text to convert
     * @return the full source text with URLs converted to HTML.
     */
    public String encodeUrlsAsHtml(String text);

    /**
     * Returns a String with HTML entity references converted to characters suitable for processing as formatted text.
     * 
     * @param value
     *        The text containing entity references (e.g., a News item description).
     * @return The HTML, ready for processing.
     */
    public String unEscapeHtml(String value);

    /**
     * Returns a String with HTML anchor normalized to include only href and target="_blank" for safe display by a browser.
     * 
     * @param anchor
     *        The anchor tag to be normalized.
     * @return The anchor tag containing only href and target="_blank".
     */
    public String processAnchor(String anchor);

    /**
     * Processes and validates character data as HTML. Disallows dangerous stuff such as &lt;SCRIPT&gt; JavaScript tags. Encodes the text according to the formatted text specification, for the rest of the system to use.
     * 
     * @param source
     *        The escaped HTML (e.g., from the News service)
     * @return The validated processed formatted text, ready for use by the system.
     */
    public String processEscapedHtml(final String source);

    /**
     * Retrieves a formatted text attribute from an XML element; converts from older forms of formatted text or plaintext, if found. For example, if the baseAttributeName "foo" is specified, the attribute "foo-html" will be looked for first, and then
     * "foo-formatted", and finally just "foo" (plaintext).
     * 
     * @param element
     *        The XML element from which to retrieve the formatted text attribute
     * @param baseAttributeName
     *        The base attribute name of the formatted text attribute
     */
    public String decodeFormattedTextAttribute(Element element, String baseAttributeName);

    /**
     * Converts the given HTML formatted text to plain text - loses formatting information. For example, The formatted text <xmp>"Hello <br />
     * <b>World!</b>"</xmp> becomes plain text "Hello \nWorld!" Strips all formatting information from the formatted text
     * 
     * @param value
     *        The formatted text to convert
     * @return The plain text (all formatting removed)
     */
    public String convertFormattedTextToPlaintext(String value);

    /**
     * Converts old-style formatted text to the new style. Previous to Sakai release 1.5, displayed line breaks were stored as "\n". Now, displayed like breaks are properly stored in the HTML-standard way as "<br />". This method converts from the
     * previous form.
     * 
     * @param value
     * @return
     */
    public String convertOldFormattedText(String value);

    /**
     * Trims a formatted text string to the given maximum number of displayed characters, preserving formatting. For example, trim("Hello &amp; <b>World</b>!", 9) returns "Hello &amp; <b>W</b>" Ignores HTML comments like "<!-- comment -->"
     * 
     * @param formattedText
     *        The formatted text to trim
     * @param maxNumOfChars
     *        The maximum number of displayed characters in the returned trimmed formatted text.
     * @param strTrimmed
     *        A StringBuilder to hold the trimmed formatted text
     * @return true If the formatted text was trimmed
     */
    public boolean trimFormattedText(String formattedText, final int maxNumOfChars, StringBuilder strTrimmed); // trimFormattedText()

    /**
     * decode any HTML Numeric Character References of the style: &#Xhexnumber; or &#decimalnumber; or of our own special style: ^^Xhexnumber^ or ^^decimalnumber^
     */
    public String decodeNumericCharacterReferences(String value);

    /**
     * WEB Utility -
     * Return a string based on value that is safe to place into a javascript / html identifier: 
     * anything not alphanumeric change to the char 'x'. 
     * If the first character is not alphabetic, a letter 'i' is prepended.
     * Used for generating javascript variable and field names.
     * 
     * @param value
     *        The string to escape.
     * @return value fully escaped using javascript / html identifier rules.
     */
    public String escapeJavascript(String value);

    /**
     * WEB Utility -
     * Return a string based on value that is safe to place into a javascript value that is in single quotes.
     * Useful to use with JSON or Javascript variables which are being set dynamically.
     * Can also be accomplished with: Use http://commons.apache.org/lang/api/org/apache/commons/lang/StringEscapeUtils.html
     * 
     * @param value
     *        The string to escape.
     * @return value String escaped for JSON or JS.
     */
    public String escapeJsQuoted(String value);

    /**
     * WEB Utility -
     * Return a string based on id that is fully escaped using URL rules, using a UTF-8 underlying encoding.
     * 
     * Note: java.net.URLEncode.encode() provides a more standard option
     *       FormattedText.decodeNumericCharacterReferences() undoes this operation
     * 
     * @param value
     *        The string to escape.
     * @return value fully escaped using URL rules.
     */
    public String escapeUrl(String value);

}