/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.utils;

import java.nio.CharBuffer;

// FIXME: Component
// FORCED TO BIN DUE TO SVN Insisting on Commit
public class NameHelper
{

	public static final char SPACE_SEPARATOR = '/';

	public static final String DEFAULT_PAGE = "home";

	public static boolean isGlobalised(final String name)
	{

		// This should be a RE

		if (name == null)
		{
			return false;
		}

		char[] chars = name.toCharArray();
		boolean allowedSpaceOrSeparator = false;

		if (chars[0] != SPACE_SEPARATOR)
		{
			return false;
		}

		for (int i = 1; i < chars.length; i++)
		{
			if (chars[i] == ' ' || chars[i] == SPACE_SEPARATOR)
			{
				if (!allowedSpaceOrSeparator)
				{
					return false;
				}
				else
				{
					allowedSpaceOrSeparator = false;
				}
			}
			else if (Character.isUpperCase(chars[i])
					|| Character.isWhitespace(chars[i]))
			{
				return false;
			}
			else
			{
				allowedSpaceOrSeparator = true;
			}
		}

		return allowedSpaceOrSeparator;
	}

	public static String globaliseName(final String name,
			final String defaultSpace)
	{

		if (name == null || name.length() == 0)
		{
			return normalize(defaultSpace + SPACE_SEPARATOR + DEFAULT_PAGE,
					false);
		}

		if (name.charAt(0) != SPACE_SEPARATOR)
		{
			return normalize(defaultSpace + SPACE_SEPARATOR + name, true);
		}
		else
		{
			return normalize(name, true);
		}
	}

	/**
	 * Takes a globalised page name and a space name and localises that page
	 * name within that space.
	 * 
	 * @param pageName
	 *        Globalised page name
	 * @param space
	 *        Globalised space name to localise within
	 * @return localised page name
	 */
	public static String localizeName(final String pageName, final String space)
	{
		// Space may not be normalized SAK-2697, and may aswell globalise the
		// pageName whilst we are at it.
		String normalSpace = normalize(space, false);
		String name = globaliseName(pageName, normalSpace);

		int nameLength = name.length();
		int spaceLength = normalSpace.length();

		char[] chars = name.toCharArray();
		int lastSeparator = findLastSeparator(chars, nameLength);

		boolean capitalise = true;
		for (int i = lastSeparator; i < nameLength; i++)
		{
			if (chars[i] == SPACE_SEPARATOR || chars[i] == ' ')
			{
				capitalise = true;
			}
			else if (capitalise)
			{
				chars[i] = Character.toUpperCase(chars[i]);
				capitalise = false;
			}
		}

		if (nameLength <= spaceLength + 1)
		{
			return new String(chars);
		}

		char[] spaceChars = normalSpace.toCharArray();
		for (int i = 0; i < spaceLength; i++)
		{
			if (chars[i] != spaceChars[i])
			{
				return new String(chars);
			}
		}

		if (chars[spaceLength] != SPACE_SEPARATOR)
		{
			return new String(chars);
		}
		else
		{
			return new String(chars, spaceLength + 1, nameLength - spaceLength
					- 1);
		}
	}

	private static int findLastSeparator(char[] chars, int nameLength)
	{
		for (int i = nameLength - 1; i >= 0; i--)
		{
			if (chars[i] == SPACE_SEPARATOR)
			{
				return i;
			}
		}
		return 0;
	}

	private static String normalize(final String nameToNormalize,
			final boolean isPageName)
	{
		char[] chars = nameToNormalize.toCharArray();
		int charBufferLength = chars.length + 1
				+ (isPageName ? DEFAULT_PAGE.length() : 0);
		CharBuffer name = CharBuffer.allocate(charBufferLength);

		int wordStart = 0;

		boolean addSeparator = true;
		boolean addWhiteSpaceOrSeparator = true;

		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];

			if (c == SPACE_SEPARATOR)
			{
				if (!addWhiteSpaceOrSeparator)
				{
					name.put(chars, wordStart, i - wordStart);
				}
				addSeparator = true;
				addWhiteSpaceOrSeparator = true;
			}
			else if (Character.isWhitespace(c))
			{
				if (!addWhiteSpaceOrSeparator)
				{
					name.put(chars, wordStart, i - wordStart);

				}
				addWhiteSpaceOrSeparator = true;
			}
			else if (addSeparator)
			{
				name.put(SPACE_SEPARATOR);
				chars[i] = Character.toLowerCase(c);
				wordStart = i;
				addSeparator = false;
				addWhiteSpaceOrSeparator = false;
			}
			else if (addWhiteSpaceOrSeparator)
			{
				addWhiteSpaceOrSeparator = false;
				wordStart = i;
				name.put(' ');
				chars[i] = Character.toLowerCase(c);
			}
			else
			{
				chars[i] = Character.toLowerCase(c);
			}

		}

		if (addSeparator && isPageName)
		{
			name.put(SPACE_SEPARATOR);
			name.put(DEFAULT_PAGE);
		}
		else if (!addWhiteSpaceOrSeparator)
		{
			name.put(chars, wordStart, chars.length - wordStart);
		}

		int position = name.position();
		name.position(0);
		name.limit(position);

		return name.toString();
	}

	/**
	 * @param pageName
	 * @param defaultSpace
	 * @return
	 */
	public static String localizeSpace(final String pageName,
			final String defaultSpace)
	{
		String globalName = globaliseName(pageName, defaultSpace);

		int index = globalName.lastIndexOf(SPACE_SEPARATOR);
		return globalName.substring(0, index);
	}

}
