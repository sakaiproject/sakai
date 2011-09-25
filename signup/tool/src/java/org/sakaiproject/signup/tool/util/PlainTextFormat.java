/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/signup/branches/2-6-x/impl/src/java/org/sakaiproject/signup/util/PlainTextFormat.java $
 * $Id: PlainTextFormat.java 56827 2009-01-13 21:52:18Z guangzheng.liu@yale.edu $
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool.util;


import org.sakaiproject.util.FormattedText;
/* TODO one duplicate class with signup-impl */

/**
 * <P>
 * This class just provides a static method to extend the funtionality of the
 * metod: convertFormattedTextToPlaintext(htmlText) in the class:FormattedText
 * class.
 * </P>
 */
public class PlainTextFormat {

	/**
	 * customized html from fck editor for calendar
	 * 
	 * @param htmlText
	 *            a html formatted text
	 * @return a plain text
	 */
	public static String convertFormattedHtmlTextToPlaintext(String htmlText) {
		/*
		 * replace "<p>" with nothing. Replace "</p>" and "<p />" HTML
		 * tags with "<br />"
		 */
		if (htmlText == null)
			return "";

		htmlText = htmlText.replaceAll("<p>", "");
		htmlText = htmlText.replaceAll("</p>", "<br />");
		htmlText = htmlText.replaceAll("<p />", "<br />");
		return FormattedText.convertFormattedTextToPlaintext(htmlText);
	}
}
