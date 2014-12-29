/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.util;

import java.util.HashMap;
import java.util.Map;

import org.radeox.regex.MatchResult;
import org.radeox.regex.Matcher;
import org.radeox.regex.Pattern;
import org.radeox.regex.Substitution;


/*
 * Escapes and encodes Strings for web usage @author stephan
 * 
 * @version $Id$
 */

public class Encoder
{
	

	public static final char HIGHEST_CHARACTER = '~';

	public static final char[][] specialChars = new char[HIGHEST_CHARACTER + 1][];
	static
	{
		specialChars['>'] = "&gt;".toCharArray();
		specialChars['<'] = "&lt;".toCharArray();
		specialChars['&'] = "&amp;".toCharArray();
		specialChars['"'] = "&#34;".toCharArray();
		specialChars['\''] = "&#39;".toCharArray();
		specialChars['['] = "&#91;".toCharArray();
		specialChars[']'] = "&#93;".toCharArray();
		specialChars['_'] = "&#95;".toCharArray();
		specialChars['-'] = "&#45;".toCharArray();
		specialChars['~'] = "&#126;".toCharArray();
	}
	private final static Map<String,String> ESCAPED_CHARS = new HashMap<String,String>();
	static {
		ESCAPED_CHARS.put("&gt;", ">");
		ESCAPED_CHARS.put("&lt;", "<");
		ESCAPED_CHARS.put("&amp;", "&");
		ESCAPED_CHARS.put("&quot;", "\"");
		ESCAPED_CHARS.put("&#39;", "'");
		ESCAPED_CHARS.put("&#91;", "[");
		ESCAPED_CHARS.put("&#93;", "]");
		ESCAPED_CHARS.put("&#95;", "_");
		ESCAPED_CHARS.put("&#45;", "-");
		ESCAPED_CHARS.put("&#126;", "~");
	}

	public static String escape(String toEscape)
	{
		char[] chars = toEscape.toCharArray();
		int lastEscapedBefore = 0;
		StringBuffer escapedString = null;
		for (int i = 0; i < chars.length; i++)
		{
			if (chars[i] <= HIGHEST_CHARACTER)
			{
				char[] escapedPortion = specialChars[chars[i]];
				if (escapedPortion != null)
				{
					if (lastEscapedBefore == 0)
					{
						escapedString = new StringBuffer(chars.length + 5);
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


	// private final static Pattern entityPattern =
	// Pattern.compile("&(#?[0-9a-fA-F]+);");



	public static String unescape(String str)
	{
		StringBuffer result = new StringBuffer();

		org.radeox.regex.Compiler compiler = org.radeox.regex.Compiler.create();
		Pattern entityPattern = compiler.compile("&(#?[0-9a-fA-F]+|gt|lt|amp);");

		Matcher matcher = Matcher.create(str, entityPattern);
		result.append(matcher.substitute(new Substitution()
		{
			public void handleMatch(StringBuffer buffer, MatchResult result)
			{
				
				String m = result.group(1);
				if ( "amp".equals(m)) {					
					buffer.append("&");
				} else if ( "gt".equals(m)) {					
					buffer.append(">");
				} else if ( "lt".equals(m)) {					
					buffer.append("<");
				} else {
					buffer.append(toChar(result.group(1)));
				}

			}
		}));
		return result.toString();
	}

	public static String toEntity(int c)
	{
		if (c <= HIGHEST_CHARACTER)
		{
			char[] escapedPortion = specialChars[c];
			if ( escapedPortion != null ) {
				return new String(escapedPortion);
			}
		}
		return "&#" + c + ";";
	}

	public static char toChar(String number)
	{
		return (char) Integer.decode(number.substring(1)).intValue();
	}
}
