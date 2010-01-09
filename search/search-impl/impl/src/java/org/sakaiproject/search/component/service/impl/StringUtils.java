/**
 * 
 */
package org.sakaiproject.search.component.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 *
 */
public class StringUtils
{
	
	private static final Log log = LogFactory.getLog(StringUtils.class);

	/**
	 * This is a copy of the method in FormattedText... its here because FormattedText 
	 * depends on the component manager, and I need to run simple unit tests
	 * @param value
	 * @param escapeNewlines
	 * @return
	 * 
	 * deprecated use commons-lang StringEscapeUtils.escapeHTML
	 */
	public static String escapeHtml(String value, boolean escapeNewlines)
	{
		if (value == null) return "";

		try
		{
			// lazily allocate the StringBuilder
			// only if changes are actually made; otherwise
			// just return the given string without changing it.
			StringBuilder buf = null;
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
			log.warn("Validator.escapeHtml: ", e);
			return value;
		}

	} // escapeHtml

	public static final char HIGHEST_CHARACTER = '>';

	public static final char[][] specialChars = new char[HIGHEST_CHARACTER + 1][];
	static
	{
		specialChars['>'] = "&gt;".toCharArray();
		specialChars['<'] = "&lt;".toCharArray();
		specialChars['&'] = "&amp;".toCharArray();
		specialChars['"'] = "&#34;".toCharArray();
		specialChars['\''] = "&#39;".toCharArray();
	}
	
	/**
	 * 
	 * @param toEscape
	 * @return
	 * 
	 * @deprecated Use Commons-lang StringEscapeUtils.escapeXml as this code might result in NPE's
	 */
	public static String xmlEscape(String toEscape)
	{
		char[] chars = toEscape.toCharArray();
		int lastEscapedBefore = 0;
		StringBuilder escapedString = null;
		for (int i = 0; i < chars.length; i++)
		{
			if (chars[i] <= HIGHEST_CHARACTER)
			{
				char[] escapedPortion = specialChars[chars[i]];
				if (escapedPortion != null)
				{
					if (lastEscapedBefore == 0)
					{
						escapedString = new StringBuilder(chars.length + 5);
					}
					if (lastEscapedBefore < i)
					{
						escapedString.append(chars, lastEscapedBefore, i
								- lastEscapedBefore);
					}
					lastEscapedBefore = i + 1;
					escapedString.append(escapedPortion);
				}
			}
		}

		if (lastEscapedBefore == 0)
		{
			return toEscape;
		}

		if (lastEscapedBefore < chars.length)
		{
			escapedString.append(chars, lastEscapedBefore, chars.length
					- lastEscapedBefore);
		}

		return escapedString.toString();
	}

}
