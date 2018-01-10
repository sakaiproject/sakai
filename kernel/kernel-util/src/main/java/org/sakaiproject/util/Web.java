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

package org.sakaiproject.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * Web is a web (html, http, etc) technlogies collection of helper methods.
 * </p>
 * @deprecated use apache commons utils for {@link org.sakaiproject.util.api.FormattedText}, this will be removed after 2.9 - Dec 2011
 */
@Deprecated
@Slf4j
public class Web
{
	// used to remove javascript from html
	private static final String START_JAVASCRIPT = "<script";
	private static final String END_JAVASCRIPT = "</script>";
	
	/**
	 * Escape a plaintext string so that it can be output as part of an HTML document. Amperstand, greater-than, less-than, newlines, etc, will be escaped so that they display (instead of being interpreted as formatting).
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value fully escaped for HTML.
     * @deprecated this is a passthrough for {@link FormattedText#escapeHtml(String, boolean)} so use that instead
	 */
	public static String escapeHtml(String value)
	{
		return FormattedText.escapeHtml(value, true);
	}

	/**
	 * Escape HTML-formatted text in preparation to include it in an HTML document.
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value escaped for HTML.
	 * @deprecated this is a passthrough for {@link FormattedText#escapeHtmlFormattedText(String)} so use that instead
	 */
	public static String escapeHtmlFormattedText(String value)
	{
		return FormattedText.escapeHtmlFormattedText(value);
	}
	   
    /**
     * Escape the given value so that it appears as-is in HTML - that is, HTML meta-characters like '<' are escaped to HTML character entity references like '&lt;'. Markup, amper, quote are escaped. Whitespace is not.
     * 
     * @param value
     *        The string to escape.
     * @param escapeNewlines
     *        Whether to escape newlines as "&lt;br /&gt;\n" so that they appear as HTML line breaks.
     * @return value fully escaped for HTML.
     * @deprecated this is a passthrough for {@link FormattedText#escapeHtml(String, boolean)} so use that instead
     */
    public static String escapeHtml(String value, boolean escapeNewlines) {
        return FormattedText.escapeHtml(value, escapeNewlines);
    }

    /**
     * Return a string based on value that is safe to place into a javascript value that is in single quiotes.
     * 
     * @param value
     *        The string to escape.
     * @return value escaped.
     * @deprecated just a passthrough for {@link FormattedText#escapeJsQuoted(String)} so use that instead
     */
    public static String escapeJsQuoted(String value)
    {
        return FormattedText.escapeJsQuoted(value);
    }

    /**
     * Return a string based on id that is fully escaped using URL rules, using a UTF-8 underlying encoding.
     * 
     * Note: java.net.URLEncode.encode() provides a more standard option
     *       FormattedText.decodeNumericCharacterReferences() undoes this op
     * 
     * @param id
     *        The string to escape.
     * @return id fully escaped using URL rules.
     * @deprecated just a passthrough for {@link Validator#escapeUrl(String)} so use that instead
     */
    public static String escapeUrl(String id)
    {
        return Validator.escapeUrl( id );

    } // escapeUrl

    /**
     * Returns a String with HTML entity references converted to characters suitable for processing as formatted text.
     * 
     * @param value
     *        The text containing entity references (e.g., a News item description).
     * @return The HTML, ready for processing.
     * @deprecated just a copy of {@link org.sakaiproject.util.api.FormattedText#unEscapeHtml(String)} so use that instead
     */
    public static String unEscapeHtml(String value)
    {
        // FIXME delete this method
        if (value == null) return "";
        if (value.equals("")) return "";
        value = value.replaceAll("&lt;", "<");
        value = value.replaceAll("&gt;", ">");
        value = value.replaceAll("&amp;", "&");
        value = value.replaceAll("&quot;", "\"");
        return value;
    }

    /**
     * For converting plain-text URLs in a String to HTML &lt;a&gt; tags
     * Any URLs in the source text that happen to be already in a &lt;a&gt; tag will be unaffected.
     * @param text the plain text to convert
     * @return the full source text with URLs converted to HTML.
     * @deprecated just a copy of {@link org.sakaiproject.util.api.FormattedText#encodeUrlsAsHtml(String)} so use that instead
     */
    public static String encodeUrlsAsHtml(String text)
    {
        Pattern p = Pattern.compile("(?<!href=['\"]{1})(((https?|s?ftp|ftps|file|smb|afp|nfs|(x-)?man|gopher|txmt)://|mailto:)[-:;@a-zA-Z0-9_.,~%+/?=&#]+(?<![.,?:]))");
        Matcher m = p.matcher(text);
        StringBuffer buf = new StringBuffer();
        while(m.find()) {
            String matchedUrl = m.group();
            m.appendReplacement(buf, "<a href=\"" + Web.unEscapeHtml(matchedUrl) + "\">$1</a>");
        }
        m.appendTail(buf);
        return buf.toString();
    }


	/**
	 * Return a string based on value that is safe to place into a javascript / html identifier: anything not alphanumeric change to 'x'. If the first character is not alphabetic, a letter 'i' is prepended.
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value fully escaped using javascript / html identifier rules.
	 */
	public static String escapeJavascript(String value)
	{
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
			log.warn("escapeJavascript: ", e);
			return value;
		}
	}

	/**
	 * Returns the hex digit cooresponding to a number between 0 and 15.
	 * 
	 * @param i
	 *        The number to get the hex digit for.
	 * @return The hex digit cooresponding to that number.
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
	 * Form a path string from the parts of the array starting at index start to the end, each with a '/' in front.
	 * 
	 * @param parts
	 *        The parts strings
	 * @param start
	 *        The index of the first part to use
	 * @param end
	 *        The index past the last part to use
	 * @return a path string from the parts of the array starting at index start to the end, each with a '/' in front.
	 */
	public static String makePath(String[] parts, int start, int end)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = start; i < end; i++)
		{
			buf.append('/');
			buf.append(parts[i]);
		}

		if (buf.length() > 0) return buf.toString();

		return null;
	}

	protected static void print(PrintWriter out, String name, int value)
	{
		out.print(" " + name + ": ");
		if (value == -1)
		{
			out.println("none");
		}
		else
		{
			out.println(value);
		}
	}

	protected static void print(PrintWriter out, String name, String value)
	{
		out.print(" " + name + ": ");
		out.println(value == null ? "none" : value);
	}

	/**
	 * Compute the URL that would return to this servlet based on the current request, with the optional path and parameters
	 * 
	 * @param req
	 *        The request.
	 * @return The URL back to this servlet based on the current request.
	 */
	public static String returnUrl(HttpServletRequest req, String path)
	{
		StringBuilder url = new StringBuilder();
		url.append(serverUrl(req));
		url.append(req.getContextPath());
		url.append(req.getServletPath());

		if (path != null) url.append(path);

		// TODO: params

		return url.toString();
	}

	/**
	 * Send the HTML / Javascript to invoke an automatic update
	 * 
	 * @param out
	 * @param req
	 *        The request.
	 * @param placementId
	 *        The tool's placement id / presence location / part of the delivery address
	 * @param updateTime
	 *        The time (seconds) between courier checks
	 * @deprecated 
	 *        To avoid inappropriate kernel dependencies, construct this URL in the tool pending relocation of this to courier (see SAK-18481).
	 */
	public static void sendAutoUpdate(PrintWriter out, HttpServletRequest req, String placementId, int updateTime)
	{
		String userId = ComponentManager.get(SessionManager.class).getCurrentSessionUserId();
		StringBuilder url = new StringBuilder(serverUrl(req));
		url.append("/courier/");
		url.append(placementId);
		url.append("?userId=");
		url.append(userId);
		
		out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
		out.println("updateTime = " + updateTime + "000;");
		out.println("updateUrl = \"" + url.toString() + "\";");
		out.println("scheduleUpdate();");
		out.println("</script>");
	}

	/**
	 * Compute the URL that would return to this server based on the current request. 
	 * 
	 * Note: this method is duplicated in the /sakai-kernel-api/src/main/java/org/sakaiproject/util/RequestFilter.java
	 * 
	 * @param req
	 *        The request.
	 * @return The URL back to this server based on the current request.
	 * @deprecated use RequestFilter.serverUrl
	 */
	public static String serverUrl(HttpServletRequest req)
	{
	    return RequestFilter.serverUrl(req);
	}

	public static String snoop(PrintWriter out, boolean html, ServletConfig config, HttpServletRequest req)
	{
		// if no out, send to system out
		ByteArrayOutputStream ostream = null;
		if (out == null)
		{
			ostream = new ByteArrayOutputStream();
			out = new PrintWriter(ostream);
			html = false;
		}

		String h1 = "";
		String h1x = "";
		String pre = "";
		String prex = "";
		String b = "";
		String bx = "";
		String p = "";
		if (html)
		{
			h1 = "<h1>";
			h1x = "</h1>";
			pre = "<pre>";
			prex = "</pre>";
			b = "<b>";
			bx = "</b>";
			p = "<p>";
		}

		Enumeration<?> e = null;

		out.println(h1 + "Snoop for request" + h1x);
		out.println(req.toString());

		if (config != null)
		{
			e = config.getInitParameterNames();
			if (e != null)
			{
				boolean first = true;
				while (e.hasMoreElements())
				{
					if (first)
					{
						out.println(h1 + "Init Parameters" + h1x);
						out.println(pre);
						first = false;
					}
					String param = (String) e.nextElement();
					out.println(" " + param + ": " + config.getInitParameter(param));
				}
				out.println(prex);
			}
		}

		out.println(h1 + "Request information:" + h1x);
		out.println(pre);

		print(out, "Request method", req.getMethod());
		String requestUri = req.getRequestURI();
		print(out, "Request URI", requestUri);
		displayStringChars(out, requestUri);
		print(out, "Request protocol", req.getProtocol());
		String servletPath = req.getServletPath();
		print(out, "Servlet path", servletPath);
		displayStringChars(out, servletPath);
		String contextPath = req.getContextPath();
		print(out, "Context path", contextPath);
		displayStringChars(out, contextPath);
		String pathInfo = req.getPathInfo();
		print(out, "Path info", pathInfo);
		displayStringChars(out, pathInfo);
		print(out, "Path translated", req.getPathTranslated());
		print(out, "Query string", req.getQueryString());
		print(out, "Content length", req.getContentLength());
		print(out, "Content type", req.getContentType());
		print(out, "Server name", req.getServerName());
		print(out, "Server port", req.getServerPort());
		print(out, "Remote user", req.getRemoteUser());
		print(out, "Remote address", req.getRemoteAddr());
		// print(out, "Remote host", req.getRemoteHost());
		print(out, "Authorization scheme", req.getAuthType());

		out.println(prex);

		e = req.getHeaderNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Request headers:" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				out.println(" " + name + ": " + req.getHeader(name));
			}
			out.println(prex);
		}

		e = req.getParameterNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Servlet parameters (Single Value style):" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				out.println(" " + name + " = " + req.getParameter(name));
			}
			out.println(prex);
		}

		e = req.getParameterNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Servlet parameters (Multiple Value style):" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				String vals[] = (String[]) req.getParameterValues(name);
				if (vals != null)
				{
					out.print(b + " " + name + " = " + bx);
					out.println(vals[0]);
					for (int i = 1; i < vals.length; i++)
						out.println("           " + vals[i]);
				}
				out.println(p);
			}
			out.println(prex);
		}

		e = req.getAttributeNames();
		if (e.hasMoreElements())
		{
			out.println(h1 + "Request attributes:" + h1x);
			out.println(pre);
			while (e.hasMoreElements())
			{
				String name = (String) e.nextElement();
				out.println(" " + name + ": " + req.getAttribute(name));
			}
			out.println(prex);
		}

		if (ostream != null)
		{
			out.flush();
			return ostream.toString();
		}

		return "";
	}

	/**
	 * Returns a hex representation of a byte.
	 * 
	 * @param b
	 *        The byte to convert to hex.
	 * @return The 2-digit hex value of the supplied byte.
	 */
	protected static final String toHex(byte b)
	{

		char ret[] = new char[2];

		ret[0] = hexDigit((b >>> 4) & (byte) 0x0F);
		ret[1] = hexDigit((b >>> 0) & (byte) 0x0F);

		return new String(ret);
	}

	/**
	 ** Encode filename (accomodating UTF-8 characters) for specific browser download/access
	 ** Sadly, Mozilla uses a different encoding scheme than everyone else
	 ** Sadly, Safari has a known bug where doesn't correctly translate encoding for user
	 **
	 ** This method require inclusion of the javamail mail package. 
	 ** @deprecated  It is now possible to specify encoded filenames for the browser
	 **              see @link{Web#buildContentDisposition}
	 **/
	public static String encodeFileName(HttpServletRequest req, String fileName )
	{
		String agent = req.getHeader("USER-AGENT");
		try
		{
			if ( agent != null && agent.indexOf("MSIE")>=0 )
				fileName = java.net.URLEncoder.encode(fileName, "UTF8");
			else if ( agent != null && agent.indexOf("Mozilla")>=0 && agent.indexOf("Safari") == -1 )
				fileName = javax.mail.internet.MimeUtility.encodeText(fileName, "UTF8", "B");
			else
				fileName = java.net.URLEncoder.encode(fileName, "UTF8");
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			log.error(e.getMessage(), e);
		}
		
		return fileName;		
	}

	/**
	 * This attempts to build the value of the content disposition header. It provides a ISO-8859-1 representation
	 * and a full UTF-8 version. This allows browser that understand the full version to use that and
	 * for mainly IE 8 the old limited one.
	 * @param filename The filename to encode
	 * @param isDownload Whether the file is a download, will use "attachment" if true and "inline" if false.
	 * @return The value of the content disposition header specifying it's inline content.
	 */
	public static String buildContentDisposition(String filename, boolean isDownload) {
		try {
			// This will replace all non US-ASCII characters with '?'
			// Although this behaviour is unspecified doing it manually is overkill (too much work).
			// Make sure we escape double quotes.
			String iso8859Filename = new String(filename.getBytes("ISO-8859-1"), "ISO-8859-1")
					.replace("\\", "\\\\")
					.replace("\"", "\\\"");
			String utf8Filename = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
			return new StringBuilder()
					.append(isDownload ? "attachment; " : "inline; ")
					.append("filename=\"").append(iso8859Filename).append("\"; ")
							// For sensible browser give them a full UTF-8 encoded string.
					.append("filename*=UTF-8''").append(utf8Filename)
					.toString();
		} catch (UnsupportedEncodingException shouldNeverHappen) {
			throw new RuntimeException(shouldNeverHappen);
		}
	}

	private static String internalEscapeHtml(String value, boolean escapeNewlines) {
	    // FIXME this method needs to be removed entirely and is only here as a reference of how this used to work


	    if (value == null) return "";

		try {
			StringBuilder buf = new StringBuilder();
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
			return value;
		}
	}

	/**
	 ** Make sure any HTML is 'clean' (no javascript, invalid image tags)
	 **/
	public static String cleanHtml( String htmlStr )
	{
		//KNL-610 if a null String is passed return to avoid NPE -DH
		if (htmlStr == null)
		{
			return null;
		}
		
		// handle embedded images			
		htmlStr = htmlStr.replaceAll("<img ", "<img alt='' ");
			
		// remove all javascript (risk of exploit)
		// note that String.replaceAll() does not reliably handle line terminators, 
		// so javascript is removed string by string
		while ( htmlStr.indexOf(START_JAVASCRIPT) != -1 )
		{
			int badStart = htmlStr.indexOf(START_JAVASCRIPT);
			int badEnd = htmlStr.indexOf(END_JAVASCRIPT);
			String badHtml;
		
			if ( badStart > -1 && badEnd == -1)
				badHtml = htmlStr.substring( badStart );
			else
				badHtml = htmlStr.substring( badStart, badEnd+END_JAVASCRIPT.length() );
				
			// use replace( CharSequence, CharSequence) -- no regexp
			htmlStr = htmlStr.replace( new StringBuilder(badHtml), new StringBuilder() );
		}

		return htmlStr;
	}


    protected static void displayStringChars(PrintWriter out, String str)
    {
        if (str == null)
        {
            out.print("null");
        }
        else
            for (int i = 0; i < str.length(); i++)
            {
                int c = (int) str.charAt(i);
                out.print(Integer.toHexString(c) + " ");
            }
        out.println();
    }

}
