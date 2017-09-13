/**
 * Copyright (c) 2003-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.util;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Validator;

public class SiteTextEditUtil {
	/**
	 * @param formattedText 
	          The formatted text to convert to plain text and then to trim
	 * @param maxNumOfChars
	          The maximum number of characters for the trimmed text.
	 * @return Ellipse 
	           A String to represent the ending pattern of the trimmed text
	 */
	public String doPlainTextAndLimit(String formattedText, int maxNumOfChars, String ellipse)
	{
		formattedText = StringUtils.trimToNull(formattedText);
		if(formattedText == null || formattedText.equalsIgnoreCase("<br/>") || formattedText.equalsIgnoreCase("<br>")||
				formattedText.equals("&nbsp;") || FormattedText.escapeHtml(formattedText,false).equals("&lt;br type=&quot;_moz&quot; /&gt;")){
			
			return "";
		}

		StringBuilder sb = new StringBuilder();
		String text = FormattedText.convertFormattedTextToPlaintext(formattedText);				
		if(maxNumOfChars>text.length()){
			maxNumOfChars=text.length();
		}
		String trimmedText=text.substring(0, maxNumOfChars);
		sb.setLength(0);
		sb.append(trimmedText).append(ellipse);
		return Validator.escapeHtml(sb.toString());				
	}
}