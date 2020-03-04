/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.util;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.api.FormattedText;

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
		return ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(htmlText);
	}
	
	/**
	 * Additional formatting for ICS files. 
	 * Google and Yahoo calendar don't take \r and \r for description so strip them. 
	 * Convert HTML br's
	 * @param htmlText
	 * @return
	 */
	public static String convertFormattedHtmlTextToICalText(String htmlText) {
		htmlText = convertFormattedHtmlTextToPlaintext(htmlText);
		htmlText = htmlText.replaceAll("\r", "");
		htmlText = htmlText.replaceAll("\t", "");
		htmlText = htmlText.replaceAll("<br />", "\n");
		return htmlText;
	}
}
