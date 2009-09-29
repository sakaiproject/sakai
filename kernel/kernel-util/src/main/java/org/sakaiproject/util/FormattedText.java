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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * FormattedText provides support for user entry of formatted text; the formatted text is HTML. This includes text formatting in user input such as bold, underline, and fonts.
 */
public class FormattedText
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(FormattedText.class);

	/**
	 * This list of good and evil tags was extracted from:
	 * 
	 * @link http://www.blooberry.com/indexdot/html/tagindex/all.htm
	 */
	private static String[] M_goodTags = {"a", "abbr", "acronym", "address", "b", "big", "blockquote", "br", "center", "cite", "code", 
                        "dd", "del", "dir", "div", "dl", "dt", "em", "font", "hr", "h1", "h2", "h3", "h4", "h5", "h6", "i", "ins",
                        "kbd", "li", "marquee", "menu", "nobr", "noembed", "ol", "p", "pre", "q", "rt", "ruby", "rbc", "rb", "rtc", "rp",
                        "s", "samp", "small", "span", "strike", "strong", "sub", "sup", "tt", "u", "ul", "var", "xmp", "img", "embed",
                        "object", "table", "tr", "td", "th", "tbody", "caption", "thead", "tfoot", "colgroup", "col"};

        private static String[] M_goodAttributes = {"abbr", "accept", "accesskey", "align", "alink", "alt", "axis", "background",
        	"bgcolor", "border", "cellpadding", "cellspacing", "char", "charoff", "charset", "checked", "cite", "class", "classid",
        	"clear", "color", "cols", "colspan", "compact", "content", "coords", "datetime", "dir", "disabled", "enctype", "face",
        	"for", "header", "height", "href", "hreflang", "hspace", "id", "ismap", "label", "lang", "longdesc", "maxlength", "multiple",
        	"name", "noshade", "nowrap", "profile", "readonly", "rel", "rev", "rows", "rowspan", "rules", "scope", "selected", "shape",
        	"size", "span", "src", "start", "style", "summary", "tabindex", "target", "text", "title", "type", "usemap", "valign",
        	"value", "vlink", "vspace", "width"};

        private static String[] M_evilValues = {"javascript:", "behavior:", "vbscript:", "mocha:", "livescript:", "expression"};


	/**
	 * These evil HTML tags are disallowed when the user inputs formatted text; this protects the system from broken pages as well as Cross-Site Scripting (XSS) attacks.
	 */
	private static String[] M_evilTags = { "applet", "base", "body", "bgsound", "button", "col", "colgroup", "comment",  
			"dfn", "fieldset", "form", "frame", "frameset", "head", "html", "iframe", "ilayer", "inlineinput",
			"isindex", "input", "keygen", "label", "layer", "legend", "link", "listing", "map", "meta", "multicol", "nextid",
			"noframes", "nolayer", "noscript", "optgroup", "option", "param", "plaintext", "script", "select",
			"sound", "spacer", "spell", "submit", "textarea", "title", "wbr" };


	/** An array of regular expression pattern-matchers, that will match the tags given in M_evilTags */
	private static Pattern[] M_evilTagsPatterns;

	/** An array of regular expression pattern-matchers, that will match the tags given in M_goodTags */
	private static Pattern[] M_goodTagsPatterns;

	/** An array of regular expression pattern-matchers, that will match the tags given in M_goodCloseTags */
	private static Pattern[] M_goodCloseTagsPatterns;

	/** An array of regular expression pattern-matchers, that will match the attributes given in M_goodAttributeTags */
	private static Pattern[] M_goodAttributePatterns;
	
	/** An array of regular expression pattern-matchers, that will match the attributes given in M_evilValues */
	private static Pattern[] M_evilValuePatterns;
	
	static
	{
		init();
	}

	private static void init()
	{
		M_evilTagsPatterns = new Pattern[M_evilTags.length];
		for (int i = 0; i < M_evilTags.length; i++)
		{
			// matches the start of the particular evil tag "<" followed by whitespace,
			// followed by the tag name, followed by anything, followed by ">", case insensitive,
			// allowed to match over multiple lines.
			M_evilTagsPatterns[i] = Pattern.compile(".*<\\s*" + M_evilTags[i] + ".*>.*", Pattern.CASE_INSENSITIVE
					| Pattern.UNICODE_CASE | Pattern.DOTALL);
		}

		M_goodTagsPatterns = new Pattern[M_goodTags.length];
		M_goodCloseTagsPatterns = new Pattern[M_goodTags.length];
		for (int i = 0; i < M_goodTags.length; i++)
		{
			// matches the start of the particular good tag "<" followed by whitespace,
			// followed by the tag name, followed by anything, followed by ">", case insensitive,
			// allowed to match over multiple lines.
			M_goodTagsPatterns[i] = Pattern.compile(".*<\\s*" + M_goodTags[i] + "(\\s+.*>|>|/>).*", Pattern.CASE_INSENSITIVE
					| Pattern.UNICODE_CASE | Pattern.DOTALL);
			M_goodCloseTagsPatterns[i] = Pattern.compile("<\\s*/\\s*" + M_goodTags[i] + "(\\s.*>|>)", Pattern.CASE_INSENSITIVE
					| Pattern.UNICODE_CASE | Pattern.DOTALL);
		}
		
		M_goodAttributePatterns = new Pattern[M_goodAttributes.length];
		for (int i = 0; i < M_goodAttributes.length; i++) 
		{
			M_goodAttributePatterns[i] = Pattern.compile("\\s+" + M_goodAttributes[i] + 
					"(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))", 
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
		}
		
		M_evilValuePatterns = new Pattern[M_evilValues.length];
		String pads = "(\\s)*?(?:/\\*.*\\*/|<!--.*-->|\0|)*?(\\s)*?";
		for (int i = 0; i < M_evilValues.length; i++)
		{
			String complexPattern = "\\s*";
			String value = M_evilValues[i];
			for (int j = 0; j < value.length(); j++)
			{
				complexPattern += value.charAt(j) + pads;
			}

			M_evilValuePatterns[i] = Pattern.compile(complexPattern, Pattern.CASE_INSENSITIVE | 
					Pattern.UNICODE_CASE | Pattern.DOTALL);
		}
		
	}
	
	/** Matches HTML-style line breaks like &lt;br&gt; */
	private static Pattern M_patternTagBr = Pattern.compile("<\\s*br\\s+?[^<>]*?>", Pattern.CASE_INSENSITIVE);

	/** Matches any HTML-style tag, like &lt;anything&gt; */
	private static Pattern M_patternTag = Pattern.compile("<.*?>", Pattern.DOTALL);

	/** Matches the 3 main pieces of any HTML tag */
	private static Pattern M_patternTagPieces = Pattern.compile("((?:<|</)\\w+)(\\s[^>]*?)?(/>|>)", Pattern.DOTALL);
	
	/** Matches newlines */
	private static Pattern M_patternNewline = Pattern.compile("\\n");

	/**
	 * Matches anchor tags so that a target can be added to them. This ensures that links in formatted text are forced to open up in a new window. This pattern matches as follows: "<a " followed by (string not containing characters "<>") possibly
	 * followed by "target=something" (which is ignored), followed by "href" followed by (string not containing characters "<>") possibly followed by "target=something" (which is ignored), followed by ">". This should match all anchor tags that have an
	 * href attribute. See escapeHtmlFormattedText()
	 */
	private static Pattern M_patternAnchorTag = Pattern.compile("([<]a\\s[^<>]*?)(\\s+href[^<>\\s]*=[^<>\\s]*?)?+([^<>]*?)[>]",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/** Matches all close anchor tags. */
	private static Pattern M_patternCloseAnchorTag = Pattern.compile("[<]\\s[^<>]*?/\\s[^<>]*?a\\s[^<>]*?[>]",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/** Matches all anchor tags that have a target attribute. */
	private static Pattern M_patternAnchorTagWithTarget = Pattern.compile("([<]a\\s[^<>]*?)target=[^<>\\s]*([^<>]*?)[>]",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/** Matches href attribute */
	private static Pattern M_patternHref = Pattern.compile("\\shref\\s*=\\s*(\".*?\"|'.*?')",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		/**
		* @see #processFormattedText(String, StringBuilder)
		* @deprecated since Nov 2007, use {@link #processFormattedText(String, StringBuilder)} instead
		*/
		public static String processFormattedText(final String strFromBrowser,
			StringBuffer errorMessages)	{
			StringBuilder sb = new StringBuilder(errorMessages.toString());
			String fixed = processFormattedText(strFromBrowser, sb);
			errorMessages.setLength(0);
			errorMessages.append(sb.toString());
			return fixed;
		}
		
	/**
	 * Processes and validates user-entered HTML received from the web browser (from the WYSIWYG editor). Validates that the user input follows the Sakai formatted text specification; disallows dangerous stuff such as &lt;SCRIPT&gt; JavaScript tags.
	 * Encodes the text according to the formatted text specification, for the rest of the system to use.
	 * 
	 * @param strFromBrowser
	 *        The formatted text as sent from the web browser (from the WYSIWYG editor)
	 * @param errorMessages
	 *        User-readable error messages will be returned here.
	 * @return The validated processed formatted text, ready for use by the system.
	 */
	public static String processFormattedText(final String strFromBrowser, StringBuilder errorMessages)
	{
		boolean checkForEvilTags = true;
		boolean replaceWhitespaceTags = true;
		return processFormattedText(strFromBrowser, errorMessages, checkForEvilTags, replaceWhitespaceTags);
	}

	/**
	 * Process an HTML document that has been edited using the formatted text widget. The document can contain any valid HTML; it will NOT be checked to eliminate things like image tags, script tags, etc, because it is its own document.
	 * 
	 * @param strFromBrowser
	 * @param errorMessages
	 */
	public static String processHtmlDocument(final String strFromBrowser, StringBuilder errorMessages)
	{
		return strFromBrowser;
	}

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
	 * @return The validated processed HTML formatted text, ready for use by the system.
	 */
	public static String processFormattedText(final String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
			boolean replaceWhitespaceTags)
	{
		String val = strFromBrowser;
		if (val == null || val.length() == 0) return val;

		if (replaceWhitespaceTags)
		{
			// normalize all variants of the "<br>" HTML tag to be "<br />\n"
			val = M_patternTagBr.matcher(val).replaceAll("<br />");

			// replace "<p>" with nothing. Replace "</p>" and "<p />" HTML tags with "<br />"
			// val = val.replaceAll("<p>", "");
			// val = val.replaceAll("</p>", "<br />\n");
			// val = val.replaceAll("<p />", "<br />\n");
		}

		if (checkForEvilTags)
		{
			val = processHtml(strFromBrowser, errorMessages);
		}

		// deal with hardcoded empty space character from Firefox 1.5
		if (val.equals("&nbsp;"))
		{
			val = "";
		}

		// close any open HTML tags (that the user may have accidentally left open)
		StringBuilder buf = new StringBuilder();
		trimFormattedText(val, Integer.MAX_VALUE, buf);
		val = buf.toString();

		// TODO: Fully parse and validate the formatted text against
		// the formatted text specification. Perhaps this could be
		// done by treating the text as an XML document and validating
		// the XML document against a Document-Type-Definition (DTD) for
		// formatted text. This would allow for validating the
		// attributes of allowed tags, for example.

		return val;
	}

	/**
	 * Prepares the given HTML formatted text for output as part of an HTML document.
	 * 
	 * @param value
	 *        The formatted text to output in an HTML document.
	 * @return The string to include in an HTML document.
	 */
	public static String escapeHtmlFormattedText(String value)
	{
		boolean supressNewlines = false;
		return escapeHtmlFormattedText(value, supressNewlines);
	}

	/**
	 * Prepares the given HTML formatted text for output as part of an HTML document, removing newlines ("&lt;br /&gt;").
	 * 
	 * @param value
	 *        The formatted text to output in an HTML document.
	 * @return The string to include in an HTML document.
	 */
	public static String escapeHtmlFormattedTextSupressNewlines(String value)
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
	private static String escapeHtmlFormattedText(String value, boolean supressNewlines)
	{
		if (value == null) return "";
		if (value.length() == 0) return "";

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
		value = M_patternAnchorTagWithTarget.matcher(value).replaceAll("$1$2>");
		value = M_patternAnchorTag.matcher(value).replaceAll("$1$2$3 target=\"_blank\">");

		return value;
	}

	/**
	 * Prepares the given formatted text for editing within the WYSIWYG editor. All HTML meta-characters in the string will be escaped.
	 * 
	 * @param value
	 *        The formatted text to escape
	 * @return The string to use as the value of the formatted textarea widget
	 */
	public static String escapeHtmlFormattedTextarea(String value)
	{
		return escapeHtml(value, false);
	}

	/**
	 * Converts the given plain text into HTML formatted text. Conversion to formatted text involves escaping characters that are used for formatting (such as the '<' character). Also converts plaintext line breaks into HTML line breaks ("<br />
	 * ").
	 * 
	 * @param value
	 *        The plain text to convert to formatted text
	 * @return The converted plain text, now as formatted text
	 */
	public static String convertPlaintextToFormattedText(String value)
	{
		return escapeHtml(value, true);
	}

	private static final boolean LAZY_CONSTRUCTION = true;

	/**
	 * Escape the given value so that it appears as-is in HTML - that is, HTML meta-characters like '<' are escaped to HTML character entity references like '&lt;'. Markup, amper, quote are escaped. Whitespace is not.
	 * 
	 * @param value
	 *        The string to escape.
	 * @param escapeNewlines
	 *        Whether to escape newlines as "&lt;br /&gt;\n" so that they appear as HTML line breaks.
	 * @return value fully escaped for HTML.
	 */
	public static String escapeHtml(String value, boolean escapeNewlines)
	{
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
				switch (c)
				{
					case '<':
					{
						if (buf == null) buf = new StringBuilder(value.substring(0, i));
						buf.append("&lt;");
					}
						break;

					case '>':
					{
						if (buf == null) buf = new StringBuilder(value.substring(0, i));
						buf.append("&gt;");
					}
						break;

					case '&':
					{
						if (buf == null) buf = new StringBuilder(value.substring(0, i));
						buf.append("&amp;");
					}
						break;

					case '"':
					{
						if (buf == null) buf = new StringBuilder(value.substring(0, i));
						buf.append("&quot;");
					}
						break;
					case '\n':
					{
						if (escapeNewlines)
						{
							if (buf == null) buf = new StringBuilder(value.substring(0, i));
							buf.append("<br />\n");
						}
						else
						{
							if (buf != null) buf.append(c);
						}
					}
						break;
					default:
					{
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
					}
						break;
				}
			} // for

			return (buf == null) ? value : buf.toString();
		}
		catch (Exception e)
		{
			M_log.warn("Validator.escapeHtml: ", e);
			return "";
		}

	} // escapeHtml

	/**
	 * Store the given formatted text in the given XML element; stores both a formatted text representation, and a plaintext representation (plaintext means the formatting has been stripped).
	 */
	public static void encodeFormattedTextAttribute(Element element, String baseAttributeName, String value)
	{
		// store the formatted text in an attribute called baseAttributeName-html
		Xml.encodeAttribute(element, baseAttributeName + "-html", value);

		// Store the non-formatted (plaintext) version as well
		Xml.encodeAttribute(element, baseAttributeName, convertFormattedTextToPlaintext(value));
	}

	/**
	 * Returns a String with characters above 128 as entity references.
	 * 
	 * @param value
	 *        The text to encode.
	 * @return The encoded text.
	 */
	public static String encodeUnicode(String value)
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
			M_log.warn("Validator.escapeHtml: ", e);
			return "";
		}
	}

	/**
	 * Returns a String with HTML entity references converted to characters suitable for processing as formatted text.
	 * 
	 * @param value
	 *        The text containing entity references (e.g., a News item description).
	 * @return The HTML, ready for processing.
	 */
	public static String unEscapeHtml(String value)
	{
		if (value == null || value.equals("")) return "";
		value = value.replaceAll("&lt;", "<");
		value = value.replaceAll("&gt;", ">");
		value = value.replaceAll("&amp;", "&");
		value = value.replaceAll("&quot;", "\"");
		return value;
	}

	/**
	 * Returns a String with HTML anchor normalized to include only href and target="_blank" for safe display by a browser.
	 * 
	 * @param anchor
	 *        The anchor tag to be normalized.
	 * @return The anchor tag containing only href and target="_blank".
	 */
	public static String processAnchor(String anchor)
	{
		String newAnchor = "";
		String href = null;

		// get href
		try
		{
			Matcher matcher = M_patternHref.matcher(anchor);
			if (matcher.find()) href = matcher.group();
		}
		catch (Exception e)
		{
			M_log.warn("FormattedText.processAnchor ", e);
		}

		// open in a new window
		if (href != null)
		{
			href = href.replaceAll("\"", "");
			href = href.replaceAll(">", "");
			href = href.replaceFirst("http", "\"http");
			newAnchor = "<a " + href + "\" target=\"_blank\">";
		}
		else
			M_log.warn("FormattedText.processAnchor href == null");
		return newAnchor;
	}

	/**
	 * Processes and validates character data as HTML. Disallows dangerous stuff such as &lt;SCRIPT&gt; JavaScript tags. Encodes the text according to the formatted text specification, for the rest of the system to use.
	 * 
	 * @param source
	 *        The escaped HTML (e.g., from the News service)
	 * @return The validated processed formatted text, ready for use by the system.
	 */
	public static String processEscapedHtml(final String source)
	{
		if (source == null) return "";
		if (source.equals("")) return "";

		String Html = null;
		try
		{
			// TODO call encodeUnicode in other process routine
			Html = encodeUnicode(source);
		}
		catch (Exception e)
		{
			M_log.warn("FormattedText.processEscapedHtml encodeUnicode(source):", e);
		}
		try
		{
			// to use the FormattedText functions
			Html = unEscapeHtml(Html);
		}
		catch (Exception e)
		{
			M_log.warn("FormattedText.processEscapedHtml unEscapeHtml(Html):", e);
		}

		return processHtml(Html, new StringBuilder());
	}

	private static String processHtml(final String source, StringBuilder errorMessages)
	{
		// normalize all variants of the "<br>" HTML tag to be "<br />\n"
		// TODO call a method to do this in each process routine
		String Html = M_patternTagBr.matcher(source).replaceAll("<br />");

		// process text and tags
		StringBuilder buf = new StringBuilder();
		if (Html != null)
		{
			try
			{
				int start = 0;
				Matcher m = M_patternTag.matcher(Html);

				// if there are no tags, return as is
				if (!m.find()) return Html;
				m.reset(Html);

				// if there are tags, make sure they are safe
				while (m.find())
				{
					// append text that isn't part of a tag
					if (m.start() > start) buf.append(Html.substring(start, m.start()));
					start = m.end();

					buf.append(checkTag(m.group(), errorMessages));
				}

				// tail
				if (Html.length() > start) buf.append(Html.substring((start)));
			}
			catch (Exception e)
			{
				M_log.warn("FormattedText.processEscapedHtml M_patternTag.matcher(Html):", e);
			}
		}
		return String.valueOf(buf.toString());
	}

	private static String checkTag (final String tag, StringBuilder errorMessages)
	{
		StringBuilder buf = new StringBuilder();
		boolean escape = true;

		// if it's a good open tag, don't escape the HTML
		for (int i = 0; i < M_goodTags.length; i++)
		{
			if (M_goodTagsPatterns[i].matcher(tag).matches())
			{
				if (M_patternAnchorTag.matcher(tag).matches()
						&& !M_patternCloseAnchorTag.matcher(tag).matches())
				{
					// if it's an anchor tag, sanitize it
					buf.append(checkAttributes(processAnchor(tag), errorMessages));
					escape = false;
				}
				else
				{
					// otherwise just include it
					buf.append(checkAttributes(tag, errorMessages));
					escape = false;
				}
			}
			else if (M_goodCloseTagsPatterns[i].matcher(tag).matches())
			{
				// if it's a good close tag, don't escape the HTML
				buf.append(checkAttributes(tag, errorMessages));
				escape = false;
			}
		}

		// otherwise escape tag
		if (escape)
		{
			buf.append((String) escapeHtml(tag, false));

			Matcher fullTag = M_patternTagPieces.matcher(tag);
			if (fullTag.matches() && fullTag.groupCount() > 2)
			{
				errorMessages.append("The HTML tag '" + fullTag.group(1)
						+ fullTag.group(fullTag.groupCount())
						+ "' is not allowed in formatted text.\n");
			}
		}

		return buf.toString();
	}
	
	private static String checkAttributes(final String tag, StringBuilder errorMessages)
	{
		Matcher fullTag = M_patternTagPieces.matcher(tag);
		String close = "";
		StringBuilder buf = new StringBuilder();
		String leftOvers = "";

		if (fullTag.matches() && fullTag.groupCount() > 2)
		{
			leftOvers = fullTag.group(2);
			buf.append(fullTag.group(1));
			close = fullTag.group(fullTag.groupCount());
		}
		else
		{
			if (M_log.isDebugEnabled()) M_log.debug("Could not parse " + tag);
			return "";
		}

		Matcher matcher;
		for (int i = 0; i < M_goodAttributePatterns.length; i++)
		{
			matcher = M_goodAttributePatterns[i].matcher(tag);
			if (matcher.find())
			{
				for (int j = 0; j < matcher.groupCount(); j++)
				{
					if (checkValue(matcher.group(j) + " ", errorMessages))
					{
						buf.append(matcher.group(j) + " ");
					
						try {
							leftOvers = leftOvers.replace(matcher.group(j), "");
						}
						catch (Exception e)
						{
							M_log.warn(matcher.group(j));
							e.printStackTrace();
						}
					}
				}
			}
		}

		if (leftOvers != null && leftOvers.trim().length() > 1)
		{
			errorMessages.append("The HTML attribute pattern '" + leftOvers + "' is not allowed\n");
		}

		buf.append(close);
		return buf.toString();
	}

	private static boolean checkValue(final String value, StringBuilder errorMessages)
	{
		boolean pass = true;

		Matcher matcher;
		for (int i = 0; i < M_evilValuePatterns.length; i++)
		{
			matcher = M_evilValuePatterns[i].matcher(value);
        	
			if (matcher.find())
			{
				pass = false;
				//errorMessages.append("The attribute value '" + value + "' is not allowed\n");
			}
		}
		return pass;
	}

	/**
	 * Retrieves a formatted text attribute from an XML element; converts from older forms of formatted text or plaintext, if found. For example, if the baseAttributeName "foo" is specified, the attribute "foo-html" will be looked for first, and then
	 * "foo-formatted", and finally just "foo" (plaintext).
	 * 
	 * @param element
	 *        The XML element from which to retrieve the formatted text attribute
	 * @param baseAttributeName
	 *        The base attribute name of the formatted text attribute
	 */
	public static String decodeFormattedTextAttribute(Element element, String baseAttributeName)
	{
		String ret;

		// first check if an HTML-encoded attribute exists, for example "foo-html", and use it if available
		ret = StringUtil.trimToNull(Xml.decodeAttribute(element, baseAttributeName + "-html"));
		if (ret != null) return ret;

		// next try the older kind of formatted text like "foo-formatted", and convert it if found
		ret = StringUtil.trimToNull(Xml.decodeAttribute(element, baseAttributeName + "-formatted"));
		ret = FormattedText.convertOldFormattedText(ret);
		if (ret != null) return ret;

		// next try just a plaintext attribute and convert the plaintext to formatted text if found
		// convert from old plaintext instructions to new formatted text instruction
		ret = Xml.decodeAttribute(element, baseAttributeName);
		ret = FormattedText.convertPlaintextToFormattedText(ret);
		return ret;
	}

	/**
	 * Converts the given HTML formatted text to plain text - loses formatting information. For example, The formatted text <xmp>"Hello <br />
	 * <b>World!</b>"</xmp> becomes plain text "Hello \nWorld!" Strips all formatting information from the formatted text
	 * 
	 * @param value
	 *        The formatted text to convert
	 * @return The plain text (all formatting removed)
	 */
	public static String convertFormattedTextToPlaintext(String value)
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

	/**
	 * Converts old-style formatted text to the new style. Previous to Sakai release 1.5, displayed line breaks were stored as "\n". Now, displayed like breaks are properly stored in the HTML-standard way as "<br />". This method converts from the
	 * previous form.
	 * 
	 * @param value
	 * @return
	 */
	public static String convertOldFormattedText(String value)
	{
		// previously, formatted text used "\n" to indicate a line break.
		// now we use "<br />\n" as a line break. This code converts old
		// formatted text to new formatted text.
		if (value == null) return null;
		if (value.length() == 0) return "";

		value = M_patternNewline.matcher(value).replaceAll("<br />\n");
		return value;
	}

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
	public static boolean trimFormattedText(String formattedText, final int maxNumOfChars, StringBuilder strTrimmed)
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
		Stack tags = new Stack(); // currently open tags (may need to be closed at the end)
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

	/**
	 * decode any HTML Numeric Character References of the style: &#Xhexnumber; or &#decimalnumber; or of our own special style: ^^Xhexnumber^ or ^^decimalnumber^
	 */
	public static String decodeNumericCharacterReferences(String value)
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

	/**
	 * HTML character entity references. These abreviations are used in HTML to escape certain Unicode characters, including characters used in HTML markup. These character entity references were taken directly from the HTML 4.0 specification at:
	 * 
	 * @link http://www.w3.org/TR/REC-html40/sgml/entities.html
	 */
	private static final String[] M_htmlCharacterEntityReferences = { "&nbsp;", "&iexcl;", "&cent;", "&pound;", "&curren;",
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
	 * @link http://www.w3.org/TR/REC-html40/sgml/entities.html
	 */
	private static final char[] M_htmlCharacterEntityReferencesUnicode = { 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170,
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
