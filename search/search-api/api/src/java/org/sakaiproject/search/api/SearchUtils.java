/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.api;

public class SearchUtils
{
	public static String getCleanStringXX(String text)
	{
		text = text.replaceAll("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f\\ud800-\\udfff\\uffff\\ufffe]", "");
		return text;
	}

	/**
	 * @param string
	 * @param sb
	 * @param minWordLength
	 * @return
	 */
	public static StringBuilder filterWordLengthIgnore(String string, StringBuilder sb, int minWordLength)
	{
		if (sb == null)
		{
			sb = new StringBuilder();
		}
		if (true)
		{
			sb.append(string);
			return sb;
		}
		if (minWordLength == -1)
		{
			sb.append(string);
			return sb;
		}
		char[] content = string.toCharArray();
		int startOfWord = -1;
		boolean symbol = false;
		for (int i = 0; i < content.length; i++)
		{
			// only take words longer than 3 charaters
			// if ( isIdiom(content[i]) ) {
			// symbol = true;
			// }
			if (Character.isWhitespace(content[i]))
			{
				if (startOfWord != -1 && (symbol || (i - startOfWord) > minWordLength))
				{
					if (!symbol || Character.isWhitespace(content[startOfWord]))
					{
						content[startOfWord] = ' ';
					}
					else if ((sb.length() > 0) && sb.charAt(sb.length() - 1) != ' ')
					{
						sb.append(' ');
					}
					String word = new String(content, startOfWord, i - startOfWord);
					sb.append(word);
				}
				symbol = false;
				startOfWord = i;
			}
			else
			{
				if (startOfWord == -1)
				{
					startOfWord = i - 1;
					if (startOfWord == -1)
					{
						startOfWord = 0;
					}
				}
			}
		}
		if (startOfWord != -1 && (content.length - startOfWord - 1) > minWordLength)
		{
			if (Character.isWhitespace(content[startOfWord]))
			{
				content[startOfWord] = ' ';
			}
			String word = new String(content, startOfWord, content.length - startOfWord);
			sb.append(word).append(" ");
		}
		return sb;
	}

	/**
	 * @param string
	 * @param sb
	 */
	public static StringBuilder appendCleanString(String string, StringBuilder sb) {
		if ( string == null ) {
			return sb;
		}
		return appendCleanString(string.toCharArray(),sb);
	}
	public static StringBuilder appendCleanString(char[] content, StringBuilder sb)
	{
		if (sb == null)
		{
			sb = new StringBuilder();
		}
		boolean ignore = true;
		for (int i = 0; i < content.length; i++)
		{
			char c = content[i];
			if (Character.isWhitespace(c) || Character.isISOControl(c) || (c == 160 ) || (c >= 0x00 && c <= 0x08) || (c == 0x0b) || (c == 0x0c) || (c == 0x0e && c <= 0x1f)
					|| (c >= 0xd800 && c <= 0xdfff) || (c == 0xffff) || (c == 0xfffe))
			{
				ignore = true;
			}
			else
			{
				if (ignore)
				{
					sb.append(" ");
					ignore = false;
				}
				sb.append(c);
			}
		}

		return sb;
	}

}
