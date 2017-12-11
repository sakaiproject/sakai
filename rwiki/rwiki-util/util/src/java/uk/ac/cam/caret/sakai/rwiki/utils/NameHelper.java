/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.utils;

import java.nio.CharBuffer;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.cover.SiteService;

@Slf4j
public class NameHelper
{

	public static final char SPACE_SEPARATOR = '/';

	public static final String DEFAULT_PAGE = "home";

	public static boolean isGlobalised(final String name)
	{

		// This should be a RE but RE is too slow

		if (name == null)
		{
			return false;
		}

		char[] chars = name.toCharArray();
		boolean allowedSpaceOrSeparator = false;
		int numberOfSeparators = 1;

		if (chars[0] != SPACE_SEPARATOR)
		{
			return false;
		}

		for (int i = 1; i < chars.length; i++)
		{
			if (chars[i] == ' ' || chars[i] == SPACE_SEPARATOR)
			{
			  	if (chars[i] == SPACE_SEPARATOR) 
				{
					numberOfSeparators++;
				}
				if (!allowedSpaceOrSeparator)
				{
					return false;
				}
				else
				{
					allowedSpaceOrSeparator = false;
				}
			}
			else if (Character.isWhitespace(chars[i]))
			{
				return false;
			}
			else if (Character.isUpperCase(chars[i]) && numberOfSeparators < 3)
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
		if (lastSeparator >= findThirdSeparator(chars, nameLength)) 
		{
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

	private static int findThirdSeparator(char[] chars, int nameLength) {
	  	int numberOfSeparators = 0;
	  	for (int i = 0; i < nameLength; i++) 
		{
		  	if (chars[i] == SPACE_SEPARATOR)
			{
			  	numberOfSeparators++;
				if (numberOfSeparators == 3) 
				{
				  	return i;
				}

			}
		}
		return nameLength;
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
		int numberOfSeparators = 0;

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
				if (++numberOfSeparators > 2) 
				{ 
					chars[i] = Character.toLowerCase(c);
				}
				wordStart = i;
				addSeparator = false;
				addWhiteSpaceOrSeparator = false;
			}
			else if (addWhiteSpaceOrSeparator)
			{
				addWhiteSpaceOrSeparator = false;
				wordStart = i;
				name.put(' ');
				if (numberOfSeparators > 2) 
				{ 
					chars[i] = Character.toLowerCase(c);
				}
			}
			else
			{
				if (numberOfSeparators > 2) 
				{ 
				  chars[i] = Character.toLowerCase(c);
				}
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
	
	public static String localizeSpace(final String globalPageName)
	{
		int index = globalPageName.lastIndexOf(SPACE_SEPARATOR);
		return globalPageName.substring(0, index);
	}

	/**
	 * @param localSpace with format "/site/siteId"
	 * @return aliasSpace if available with format "/siteAliasName". Otherwise, return "/site/siteId"
	 */
	public static String aliasSpace (final String localSpace)
	{
		String localAliasSpace = localSpace;
		AliasService aliasService = ComponentManager.get(AliasService.class);
		
		String parts[] = StringUtils.split (localSpace, Entity.SEPARATOR);
		String siteId = null;
		if (parts.length > 1) {
	        siteId = parts[1];
		}
		// recognize alias for site id - but if a site id exists that matches the requested site id, that's what we will use
		if ((siteId != null) && (siteId.length() > 0))
		{
			// get site alias first
			List target = aliasService.getAliases(
					"/site/" + siteId);

			// get mail archive alias, if site alias is not available
			if (target.isEmpty()) {
				target = aliasService.getAliases(
						"/mailarchive/channel/" + siteId + "/main");
			}
			
			if (!target.isEmpty()) {
				// take the first alias only
				AliasEdit alias = (AliasEdit) target.get(0);
				siteId = alias.getId();

				// if there is no a site id exists that matches the alias name
				if (!SiteService.siteExists(siteId))
				{
					localAliasSpace = "/" + siteId;
				}
			}
		}			
		
		return localAliasSpace;
	}
}
