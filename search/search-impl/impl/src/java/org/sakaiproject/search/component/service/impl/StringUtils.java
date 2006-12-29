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
	 */
	public static String escapeHtml(String value, boolean escapeNewlines)
	{
		if (value == null) return "";

		try
		{
			// lazily allocate the StringBuffer
			// only if changes are actually made; otherwise
			// just return the given string without changing it.
			StringBuffer buf = null;
			final int len = value.length();
			for (int i = 0; i < len; i++)
			{
				char c = value.charAt(i);
				switch (c)
				{
					case '<':
					{
						if (buf == null) buf = new StringBuffer(value.substring(0, i));
						buf.append("&lt;");
					}
						break;

					case '>':
					{
						if (buf == null) buf = new StringBuffer(value.substring(0, i));
						buf.append("&gt;");
					}
						break;

					case '&':
					{
						if (buf == null) buf = new StringBuffer(value.substring(0, i));
						buf.append("&amp;");
					}
						break;

					case '"':
					{
						if (buf == null) buf = new StringBuffer(value.substring(0, i));
						buf.append("&quot;");
					}
						break;
					case '\n':
					{
						if (escapeNewlines)
						{
							if (buf == null) buf = new StringBuffer(value.substring(0, i));
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
							if (buf == null) buf = new StringBuffer(value.substring(0, i));
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


}
