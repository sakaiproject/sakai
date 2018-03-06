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

package org.sakaiproject.signup.tool.downloadEvents;

import org.sakaiproject.util.FormattedText;

/**
 * <P>
 * This class just provides a static method to extend the funtionality of the
 * metod: convertFormattedTextToPlaintext(htmlText) in the class:FormattedText
 * class.
 * </P>
 * 
 * @author Peter Liu
 */
public class ExcelPlainTextFormat {
	
	private static final String BreakMark="~@~";
	
	private static final String LINEBREAK_EXCEL="\n";
	
	/**
	 * customized html from fck editor for calendar
	 * 
	 * @param htmlText
	 *            a html formatted text
	 * @return a plain text
	 */
	public static String convertFormattedHtmlTextToExcelPlaintext(String htmlText) {
		/*
		 * replace "<p>" with nothing. Replace "</p>" and "<p />" HTML
		 * tags with "<br />"
		 */
		if (htmlText == null)
			return "";

		htmlText = htmlText.replaceAll("<p>", "");
		htmlText = htmlText.replaceAll("\r", "");
		htmlText = htmlText.replaceAll("</p>", BreakMark);
		htmlText = htmlText.replaceAll("<p />", BreakMark);
		htmlText = htmlText.replaceAll("<br />", BreakMark);
		htmlText = FormattedText.convertFormattedTextToPlaintext(htmlText);
		StringBuilder sb = new StringBuilder();
		int begin_pos = 0;
		int find_pos= 0;
		while(find_pos > -1 && find_pos < htmlText.length()){
			find_pos = htmlText.indexOf(BreakMark,begin_pos);
			if(find_pos > -1){
				sb.append(htmlText.subSequence(begin_pos, find_pos));
				sb.append(LINEBREAK_EXCEL);
				find_pos +=BreakMark.length();
				begin_pos = find_pos;
			}
			
		}
		
		if (begin_pos < htmlText.length()){
			sb.append(htmlText.subSequence(begin_pos, htmlText.length()-1));
		}
		htmlText = htmlText.replaceAll(BreakMark, LINEBREAK_EXCEL);
		
		return htmlText;
	}
}
