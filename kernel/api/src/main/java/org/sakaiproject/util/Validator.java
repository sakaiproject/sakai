/*********************************************************************************a
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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdInvalidException;

/**
 * <p>
 * Validator is utility class that helps to validate stuff.
 * </p>
 * @deprecated use apache commons utils or {@link org.sakaiproject.util.api.FormattedText}, this will be removed after 2.9 - Dec 2011
 */
@Deprecated 
@Slf4j
public class Validator
{
	/** These characters are not allowed in a resource id */
	public static final String INVALID_CHARS_IN_RESOURCE_ID = "^/\\{}[]()%*?#&=\n\r\t\b\f";

	/** These characters are not allowed in a user id */
	protected static final String INVALID_CHARS_IN_USER_ID = "^/\\%*?\n\r\t\b\f";

	/** These characters are not allowed in a site type */
	protected static final String INVALID_CHARS_IN_SITE_TYPE = " $&':<>[]{}#%@/;=?\\^|~\"";

	/** These characters are not allowed in a site skin */
	protected static final String INVALID_CHARS_IN_SITE_SKIN = " $&':<>[]{}#%@/;=?\\^|~\"";
	
	protected static final String MAP_TO_A = "��������";

	protected static final String MAP_TO_B = "��";

	protected static final String MAP_TO_C = "����";

	protected static final String MAP_TO_E = "��������";

	protected static final String MAP_TO_I = "�����";

	protected static final String MAP_TO_L = "��";

	protected static final String MAP_TO_N = "���";

	protected static final String MAP_TO_O = "������";

	protected static final String MAP_TO_U = "������";

	protected static final String MAP_TO_Y = "ش??";

	protected static final String MAP_TO_X = "???�����?����?";

	/**
	 * These characters are allowed; but if escapeResourceName() is called, they are escaped (actually, removed) Certain characters cause problems with filenames in certain OSes - so get rid of these characters in filenames
	 */
	protected static final String ESCAPE_CHARS_IN_RESOURCE_ID = ";'\"";

	protected static final String INVALID_CHARS_IN_ZIP_ENTRY = "/\\%:*?'\"[]";

	/** These characters are escaped when making a URL */
	// protected static final String ESCAPE_URL = "#%?&='\"+ ";
	// not '/' as that is assumed to be part of the path
	protected static final String ESCAPE_URL = "$&+,:;=?@ '\"<>#%{}|\\^~[]`";

	/**
	 * These can't be encoded in URLs safely even using %nn notation, so encode them using our own custom URL encoding
	 */
	protected static final String ESCAPE_URL_SPECIAL = "^?;";

	/** Valid special email local id characters (- those that are invalid resource ids) */
	protected static final String VALID_EMAIL = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ!#$&'*+-=?^_`{|}~.";

	protected static final String INVALID_CHARS_IN_FILENAME = "[\\/:\"*?<>|]+";

	/**
	 * Escape a plaintext string so that it can be output as part of an HTML document. Amperstand, greater-than, less-than, newlines, etc, will be escaped so that they display (instead of being interpreted as formatting).
	 * 
	 * @param value
	 *        The string to escape.
	 * @return value fully escaped for HTML.
     * @deprecated this is a passthrough for {@link org.sakaiproject.util.api.FormattedText#escapeHtml(String, boolean)} so use that instead
	 */
	@Deprecated
	public static String escapeHtml(String value)
	{
		return FormattedText.escapeHtml(value, true);
	}

	/**
	 * Escape plaintext for display inside a plain textarea.
     * @deprecated this is a passthrough for {@link org.sakaiproject.util.api.FormattedText#escapeHtml(String, boolean)} so use that instead
	 */
	@Deprecated
	public static String escapeHtmlTextarea(String value)
	{
		return FormattedText.escapeHtml(value, false);
	}

	/**
	 * Escape HTML-formatted text in preparation to include it in an HTML document.
     * @deprecated this is a passthrough for {@link org.sakaiproject.util.api.FormattedText#escapeHtmlFormattedText(String)} so use that instead
	 */
	@Deprecated
	public static String escapeHtmlFormattedText(String value)
	{
		return FormattedText.escapeHtmlFormattedText(value);
	}

	/**
	 * Escapes the given HTML-formatted text for editing within the WYSIWYG editor. All HTML meta-characters in the string (such as amperstand, less-than, etc), will be escaped.
	 * 
	 * @param value
	 *        The formatted text to escape
	 * @return The string to use as the value of the formatted textarea widget
     * @deprecated this is a passthrough for {@link org.sakaiproject.util.api.FormattedText#escapeHtmlFormattedTextarea(String)} so use that instead
	 */
	@Deprecated
	public static String escapeHtmlFormattedTextarea(String value)
	{
		return FormattedText.escapeHtmlFormattedTextarea(value);
	}

	/**
	 * Return a string based on id that is fully escaped using URL rules, using a UTF-8 underlying encoding.
	 * One reason for this existing is that the standard URLEncoder in Java will encode slashes ('/') but this method doesn't.
	 * Also watch out as it trims trailing spaces, and other character get lost here too.
	 * 
	 * Note: {@link java.net.URLEncode#encode(String, String)} provides a more standard option
	 *       {@link FormattedText#decodeNumericCharacterReferences(String)} undoes this op
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped using URL rules.
	 * @deprecated use {@link java.net.URLEncoder#encode(String, String)}
	 */
	@Deprecated
	public static String escapeUrl(String id)
	{
		if (id == null) return "";
		id = id.trim();
        // convert the string to bytes in UTF-8
        byte[] bytes = id.getBytes(StandardCharsets.UTF_8);

        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            // escape ascii control characters, ascii high bits, specials
            if (ESCAPE_URL_SPECIAL.indexOf((char) b) != -1) {
                buf.append("^^x"); // special funky way to encode bad URL characters
                buf.append(toHex(b));
                buf.append('^');
            }
            // 0x1F is the last control character
            // 0x7F is DEL chatecter
            // 0x80 is the start of the top of the 256bit set.
            else if ((ESCAPE_URL.indexOf((char) b) != -1) || (b <= 0x1F) || (b == 0x7F) || (b >= 0x80)) {
                buf.append("%");
                buf.append(toHex(b));
            } else {
                buf.append((char) b);
            }
        }

        return buf.toString();

    } // escapeUrl
    
    /**
     * Is this a valid local part of an email id?
     * @deprecated use commons-validator {@link org.apache.commons.validator.routines.EmailValidator}
     */
	@Deprecated
    public static boolean checkEmailLocal(String id)
    {
        // rules based on rfc2882, but a bit more conservative

        for (int i = 0; i < id.length(); i++)
        {
            if (VALID_EMAIL.indexOf(id.charAt(i)) == -1) return false;
        }

        return true;

    } // checkEmailLocal

	/**
	 * Return a string based on id that is valid according to Resource name validity rules.
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped using Resource name validity rules.
	 */
	public static String escapeResourceName(String id)
	{
		if (id == null) return "";
		id = id.trim();
		try
		{
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < id.length(); i++)
			{
				char c = id.charAt(i);
				if (MAP_TO_A.indexOf(c) >= 0)
				{
					buf.append('a');
				}
				else if (MAP_TO_E.indexOf(c) >= 0)
				{
					buf.append('e');
				}
				else if (MAP_TO_I.indexOf(c) >= 0)
				{
					buf.append('i');
				}
				else if (MAP_TO_O.indexOf(c) >= 0)
				{
					buf.append('o');
				}
				else if (MAP_TO_U.indexOf(c) >= 0)
				{
					buf.append('u');
				}
				else if (MAP_TO_Y.indexOf(c) >= 0)
				{
					buf.append('y');
				}
				else if (MAP_TO_N.indexOf(c) >= 0)
				{
					buf.append('n');
				}
				else if (MAP_TO_B.indexOf(c) >= 0)
				{
					buf.append('b');
				}
				else if (MAP_TO_C.indexOf(c) >= 0)
				{
					buf.append('c');
				}
				else if (MAP_TO_L.indexOf(c) >= 0)
				{
					buf.append('l');
				}
				else if (MAP_TO_X.indexOf(c) >= 0)
				{
					buf.append('x');
				}
				else if (c < '\040')	// Remove any ascii control characters
				{
					buf.append('_');
				}
				else if (INVALID_CHARS_IN_RESOURCE_ID.indexOf(c) >= 0 || ESCAPE_CHARS_IN_RESOURCE_ID.indexOf(c) >= 0)
				{
					buf.append('_');
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
			log.warn("Validator.escapeResourceName: ", e);
			return "";
		}

	} // escapeResourceName

	/**
	 * Return a string based on id that is fully escaped the question mark.
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped question mark.
	 */
	public static String escapeQuestionMark(String id)
	{
		if (id == null) return "";
		try
		{
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < id.length(); i++)
			{
				char c = id.charAt(i);
				if (c == '?')
				{
					buf.append('_');
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
			log.warn("Validator.escapeQuestionMark: ", e);
			return "";
		}

	} // escapeQuestionMark

	/**
	 * Return a string based on id that is fully escaped to create a zip entry
	 * 
	 * @param id
	 *        The string to escape.
	 * @return id fully escaped to create a zip entry
	 */
	public static String escapeZipEntry(String id)
	{
		if (id == null) return "";
		try
		{
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < id.length(); i++)
			{
				char c = id.charAt(i);
				if (INVALID_CHARS_IN_ZIP_ENTRY.indexOf(c) != -1)
				{
					buf.append('_');
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
			log.warn("Validator.escapeZipEntry: ", e);
			return "";
		}

	} // escapeZipEntry

	/**
	 * Check for a valid user id.
	 * 
	 * @exception IdInvalidException
	 *            if the id is invalid.
	 */
	public static boolean checkUserId(String id)
	{
		// the rules:
		// Null is rejected
		// all blank is rejected
		// INVALID_CHARS_IN_USER_ID characters are rejected

		if (id == null) return false;
		if (id.trim().length() == 0) return false;

		// we must reject certain characters that we cannot even escape and get into Tomcat via a URL
		for (int i = 0; i < id.length(); i++)
		{
			if (INVALID_CHARS_IN_USER_ID.indexOf(id.charAt(i)) != -1) return false;
		}

		return true;

	} // checkUserId

	/**
	 * Check for a valid resource id.
	 * 
	 * @return true if valid, false if not
	 */
	public static boolean checkResourceId(String id)
	{
		// the rules:
		// Null is rejected
		// all blank is rejected
		// INVALID_CHARS_IN_RESOURCE_ID characters are rejected

		if (id == null) return false;
		if (id.trim().length() == 0) return false;

		// we must reject certain characters that we cannot even escape and get into Tomcat via a URL
		for (int i = 0; i < id.length(); i++)
		{
			if (INVALID_CHARS_IN_RESOURCE_ID.indexOf(id.charAt(i)) != -1) return false;
		}

		return true;

	} // checkResourceId


	/**
	 * Check for a syntactically valid site type.
	 * 
	 * @return true if valid, false if not
	 */
	public static boolean checkSiteType(String id)
	{
		// the rules:
		// Null is accepted
		// all blank is accepted
		// INVALID_CHARS_IN_SITE_TYPE characters are rejected

		if (id == null) return true;
		if (id.trim().length() == 0) return false;

		// reject certain characters
		for (int i = 0; i < id.length(); i++)
		{
			if (INVALID_CHARS_IN_SITE_TYPE.indexOf(id.charAt(i)) != -1) return false;
		}

		return true;

	} // checkSiteType

	/**
	 * Check for a syntactically valid skin name
	 * 
	 * @return true if valid, false if not
	 */
	public static boolean checkSiteSkin(String id)
	{
		// the rules:
		// Null is allowed
		// all blank is rejected
		// INVALID_CHARS_IN_SITE_TYPE characters are rejected

		if (id == null) return true;
		if (id.trim().length() == 0) return false;

		// reject certain characters
		for (int i = 0; i < id.length(); i++)
		{
			if (INVALID_CHARS_IN_SITE_SKIN.indexOf(id.charAt(i)) != -1) return false;
		}

		return true;

	} // checkSiteSkin

	/**
	 * Isolate and return just the file name part of a full drive and path file name.
	 * 
	 * @param fullName
	 *        The full file name from a local os file system (mac, unix, windoze)
	 * @return Just the name (and extension) of the file, without the drive or path.
	 * @deprecated use commons-io: {@link org.apache.commons.io.FilenameUtils#getName(String)} instead
	 */
	@Deprecated
	public static String getFileName(String fullName)
	{
		// examples: windows: c:\this\that\me.doc
		// unix: /usr/local/dev/test.txt
		// mac:? one:two:three:four
		// so... just take the last characters back till we see a \ or / or :
		StringBuilder buf = new StringBuilder();
		int index = fullName.length() - 1;
		while (index >= 0)
		{
			char c = fullName.charAt(index--);
			if ((c == '\\') || (c == '/') || (c == ':')) break;
			buf.insert(0, c);
		}

		return buf.toString();

	} // getFileName

	/**
	 * Isolate and return just the file extension part of a full drive and path file name.
	 * 
	 * @param fullName
	 *        The full file name from a local os file system (mac, unix, windoze)
	 * @return Just the extension of the file, to the right of the dot, not including the dot, or blank if none.
	 */
	public static String getFileExtension(String fullName)
	{
		// just take from the last dot to the end, or return "" if there's no dot.
		int index = fullName.lastIndexOf('.');
		if (index == -1) return "";

		return fullName.substring(index + 1);

	} // getFileExtension

	/**
	 * Determine whether a file resource should be opened in the current window or a new window.
	 * 
	 * @param contentType
	 *        The content type to check
	 * @return A string identifying the window in which to open the resource: "_self" to open the resource in the current window, "_blank" for a new window, or an empty string if the resource is not a file.
	 */
	public static String getResourceTarget(String contentType)
	{
		// we will open a new window unless...
		String rv = "_blank";

		// get the resource's type
		if (contentType != null)
		{
			// if the browser will not inline, but mark as attachments, let's not open a new window
			if (!letBrowserInline(contentType))
			{
				rv = "_self";
			}
		}

		return rv;

	} // getResourceTarget

	/**
	 * Is this a mime type that the browser can handle inline, in a browser window? If so, links to this type should be to a _blank, and content-disposition should be inline. If not, links to this type should be to _self, and content-disposition should be
	 * attachment.
	 * 
	 * @param type
	 *        The mime type to check.
	 * @return true if this type of resource the browser can handle in line, false if not.
	 */
	public static boolean letBrowserInline(String type)
	{
		if (type == null) return false;

		String lType = type.toLowerCase();

		// text (plain/html) mime types
		if (lType.startsWith("text/")) return true;
		// XHTML mime type
		if (lType.equals("application/xhtml+xml")) return true;
		
		// image mime types
		if (lType.startsWith("image/")) return true;
		
		// PDF mime types
		if (lType.equals("application/pdf")) return true;
		if (lType.equals("application/x-pdf")) return true;
		
		// internal OSP/Forms
		if (lType.equals("application/x-osp")) return true;
		
		// Shockwave Flash mime types
		if (lType.equals("application/x-shockwave-flash")) return true;
		if (lType.equals("application/futuresplash")) return true;
		
		// checks for VRML file MIME types:x-world/x-vrml, model/vrml, application/x-blaxxunCC3D, application/x-blaxxunCC3Dpro, application/x-CC3D
		// need to check for any other MIME types which can be opened by browser plug-ins? %%%zqian
		if (lType.indexOf("vrml") != -1 || lType.indexOf("cc3d") != -1) return true;

		// check additional inline types for this instance specified in sakai.properties
		String moreInlineTypes[] = ServerConfigurationService.getStrings("content.mime.inline");  
		
		if (moreInlineTypes != null) {
            for (String moreInlineType : moreInlineTypes) {
                if (lType.equals(moreInlineType))
                    return true;
            }
		}
		
		return false;

	} // letBrowserInline

	/**
	 * Limit the string to a certain number of characters, adding "..." if it was truncated.
	 * 
	 * @param value
	 *        The string to limit.
	 * @param length the
	 *        length to limit to (as an Integer).
	 * @return The limited string.
	 */
	public static String limit(String value, Integer length)
	{
		return limit(value, length.intValue());

	} // limit

	/**
	 * Limit the string to a certain number of characters, adding "..." if it was truncated
	 * 
	 * @param value
	 *        The string to limit.
	 * @param length the
	 *        length to limit to (as an int).
	 * @return The limited string.
	 */
	public static String limit(String value, int length)
	{
		StringBuilder buf = new StringBuilder(value);
		if (buf.length() > length)
		{
			buf.setLength(length);
			buf.append("...");
		}

		return buf.toString();

	} // limit

	/**
	 * Returns a hex representation of a byte.
	 * 
	 * @param b
	 *        The byte to convert to hex.
	 * @return The 2-digit hex value of the supplied byte.
	 */
	private static String toHex(byte b)
	{

		char ret[] = new char[2];

		ret[0] = hexDigit((b >>> 4) & (byte) 0x0F);
		ret[1] = hexDigit((b >>> 0) & (byte) 0x0F);

		return new String(ret);
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
	private static char hexDigit(int i)
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
	 * Return a safe filename by replacing all whitespace and invalid characters
	 *
	 * @param filename
	 *        The string to clean
	 * @return safe filename string
	 */
	public static String cleanFilename(String filename) {
		// replace all whitespace
		String cleanFilename = filename.replaceAll("\\s", "_");

		// replace all invalid characters
		final int len = cleanFilename.length();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < len; i++) {
			char c = cleanFilename.charAt(i);
			if (INVALID_CHARS_IN_FILENAME.indexOf(c) != -1) {
				buf.append("_");
			} else {
				buf.append(c);
			}
		}

		return buf.toString();
	}
}
