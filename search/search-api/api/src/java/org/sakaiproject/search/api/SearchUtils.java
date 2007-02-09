/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.search.api;

public class SearchUtils
{
	public static String getCleanString(String text, int minWordLength ) {
		text = text.replaceAll("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f\\ud800-\\udfff\\uffff\\ufffe]", "");
		if ( minWordLength == -1 ) {
			return text;
		}
		return filterWordLength(text, null, minWordLength).toString();
	}

	/**
	 * @param string
	 * @param sb 
	 * @param minWordLength
	 * @return
	 */
	public static StringBuffer filterWordLength(String string, StringBuffer sb, int minWordLength)
	{
		
		char[] content = string.toCharArray();
		if ( sb == null ) {
			sb = new StringBuffer();
		}
		if ( minWordLength == -1 ) {
			sb.append(string);
			return sb;
		}
		int startOfWord = -1;
		for (int i = 0; i < content.length; i++)
		{
			// only take words longer than 3 charaters
			
			if (!Character.isLetterOrDigit(content[i]))
			{
				if ( startOfWord != -1 && (i-startOfWord) > minWordLength) {
					if ( !Character.isLetterOrDigit(content[startOfWord]) ) {
						content[startOfWord] = ' ';
					} else if ( (sb.length() > 0) &&  sb.charAt(sb.length()-1) != ' ' ) {
						sb.append(' ');
					}
					String word = new String(content,startOfWord,i-startOfWord);
					sb.append(word);
				}
				startOfWord = i;
			} else {
				if ( startOfWord == -1 ) {
					startOfWord = i-1;
					if ( startOfWord == -1 ) {
						startOfWord = 0;
					}
				}
			}
		}
		if ( startOfWord != -1 && (content.length-startOfWord-1) > minWordLength) {
			if ( !Character.isLetterOrDigit(content[startOfWord]) ) {
				content[startOfWord] = ' ';
			}
			String word = new String(content,startOfWord,content.length-startOfWord);
			sb.append(word).append(" ");
		}
		return sb;
	}
	
	
}
