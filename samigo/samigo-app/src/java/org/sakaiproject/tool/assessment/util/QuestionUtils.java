package org.sakaiproject.tool.assessment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class QuestionUtils {

	/**
	 * Replace text variables for Calculated Questions
	 * @param text
	 * @param openSeparator
	 * @param closeSeparator
	 * @param replacement
	 * @return
	 */
	public static String replaceTextVariables(String text, String openSeparator, String closeSeparator, String replacement) {
		String result = text;
		if (StringUtils.isNoneBlank(openSeparator, closeSeparator)) {
			String openPattern = openSeparator.replace("[", "\\[").replace("{","\\{").replace("(", "\\(").replace("]", "\\]").replace("}","\\}").replace(")", "\\)");
			String closePattern = closeSeparator.replace("[", "\\[").replace("{","\\{").replace("(", "\\(").replace("]", "\\]").replace("}","\\}").replace(")", "\\)");
			String pattern = openPattern + "(.*?)" + closePattern;
			List<String> replaceList = new ArrayList<String>();
			Matcher m = Pattern.compile(pattern).matcher(result);
			while(m.find()) {
				replaceList.add(m.group(1));
			}
			for (String variable : replaceList) {
				result = result.replace(openSeparator+variable+closeSeparator, replacement);
			}
		}
		return result;
	}
	
}
