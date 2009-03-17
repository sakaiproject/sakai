package org.sakaiproject.site.util;

import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;

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
		formattedText = StringUtil.trimToNull(formattedText);
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
		return sb.toString();				
	}
}