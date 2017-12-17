/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtils {

	/**
	 * Minimum length supported by <code>truncateAtWhitespace()</code>
	 */
	private static final int MINIMUM_SUPPORTED_LENGTH	= 4;

	private StringUtils() {
	}

  /**
   * Replace all occurances of the target text with the provided replacement
   * text.  Both target and replacement may be regular expressions - see
   * <code>java.util.regex.Matcher</code>.
   * @param text Text to modify
   * @param targetText Text to find and replace
   * @param newText New text
   * @return Updated text
   */
	public static String replace(String text, String targetText, String newText) {
  	  	Pattern pattern = Pattern.compile(targetText, Pattern.CASE_INSENSITIVE);
  	  	Matcher matcher = pattern.matcher(text);
  	  	return matcher.replaceAll(newText);
	}

	/**
	 * Null (or zero-length) String?
	 */
	public static boolean isNull(String string) {
	 	return (string == null) || (string.length() == 0);
	}

	/**
	 * Truncate text on a whitespace boundary (near a specified length).
	 * The length of the resultant string will be in the range:<br>
	 * <code>   (requested-length * .25) ~ (requested-length * 1.5) </code>
	 * @param text Text to truncate
	 * @param length Target length
	 * @return Truncated text
	 */
	public static String truncateAtWhitespace(String text, int length) {
		int	desired, lowerBound, upperBound;
		/*
		 * Make sure we have a reasonable length to work with
		 */
		if (length < MINIMUM_SUPPORTED_LENGTH) {
			 throw new IllegalArgumentException("Requested length too short (must be "
			 																	+ MINIMUM_SUPPORTED_LENGTH
			 																	+ " or greated)");
		}
		/*
		 * No need to truncate - the original string "fits"
		 */
		if (text.length() <= length) {
			return text;
		}
		/*
		 * Try to find whitespace befor the requested maximum
		 */
		lowerBound 	= length / 4;
		upperBound	= length + (length / 2);

		for (int i = length - 1; i > lowerBound; i--) {
			if (Character.isWhitespace(text.charAt(i))) {
				return text.substring(0, i);
			}
		}
		/*
		 * No whitespace - look beyond the desired maximum
		 */
		for (int i = (length); i < upperBound; i++) {
			if (Character.isWhitespace(text.charAt(i))) {
				return text.substring(0, i);
			}
		}
		/*
		 * No whitespace, just truncate the text at the requested length
		 */
		return text.substring(0, length);
  }

	/**
	 * Trim specified charcater from front of string
	 * @param text Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimFront(String text, char character) {
		String 	normalizedText;
		int			index;

		if (StringUtils.isNull(text)) {
			return text;
		}

		normalizedText = text.trim();
		index	= 0;

		while (normalizedText.charAt(index) == character) {
			if (++index >= normalizedText.length()) break;
		}
		return normalizedText.substring(index).trim();
	}

	/**
	 * Trim specified charcater from end of string
	 * @param text Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimEnd(String text, char character) {
		String 	normalizedText;
		int			index;

		if (StringUtils.isNull(text)) {
			return text;
		}

		normalizedText = text.trim();
		index	= normalizedText.length() - 1;

		while (normalizedText.charAt(index) == character) {
			if (--index < 0) {
				return "";
			}
		}
		return normalizedText.substring(0, index + 1).trim();
	}

	/**
	 * Trim specified charcater from both ends of a String
	 * @param text Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimAll(String text, char character) {
		String normalizedText = trimFront(text, character);

		return trimEnd(normalizedText, character);
	}

	/**
	 * Capitlize each word in a string (journal titles, etc)
	 * @param text Text to inspect
	 * @return Capitalized text
	 */
	public static String capitalize(String text) {
		StringBuilder	resultText;
		char 					previousC;

    resultText	= new StringBuilder();
    previousC 	= '.';

    for (int i = 0;  i < text.length();  i++ ) {
       char c = text.charAt(i);

       if (Character.isLetter(c) && !Character.isLetter(previousC)) {
          resultText.append(Character.toUpperCase(c));
       } else {
          resultText.append(c);
       }
       previousC = c;
    }
    return resultText.toString();
  }

	/**
	 * Remove a character (or range of characters) from a string.  If the
	 * character is in a word, remove the entire word.
	 * @param source String to edit
	 * @param pattern Character (or range) to remove.  Range is a regular
	 *				expression: <code>[\u002c-\u002f]</code> or <code>[,-/]</code>
	 * @return Modified text
	 */
	public static String removeCharacterOrRangeAsWord(String source, String pattern) {
		return removeCharacterOrRange(source, pattern, true);
	}

	/**
	 * Remove a character (or range of characters) from a string.  If the
	 * character is at the start or end of a word, remove the character only
	 * (leave the word in place).
	 * @param source String to edit
	 * @param pattern Character (or range) to remove.  Range is a regular
	 *				expression: <code>[\u002c-\u002f]</code> or <code>[,-/]</code>
	 * @return Modified text
	 */
	public static String removeCharacterOrRangeAsCharacter(String source, String pattern) {
		return removeCharacterOrRange(source, pattern, false);
	}

	/*
	 * Helpers
	 */

	/**
	 * Remove a character (or range of characters) from a string.
	 * [optional] If a character is embedded in a word, remove the entire word.
	 * @param source String to edit
	 * @param pattern Character (or range) to remove.  Range is a regular
	 *				expression: <code>[\u002c-\u002f]</code> or <code>[,-/]</code>
	 * @param removeAsWord Always remove entire word?
	 * @return Modified text
	 */
	private static String removeCharacterOrRange(String source, String pattern,
								 														  boolean removeAsWord) {
		StringBuilder 	patternBuffer	= new StringBuilder();
		String 				rangePattern, result;

		patternBuffer.append('[');
		patternBuffer.append(pattern);
		patternBuffer.append(']');

		rangePattern = patternBuffer.toString();

		result = StringUtils.replace(source, "\\S+" + rangePattern + "\\S+", "");

		if (removeAsWord) {
			result = StringUtils.replace(result, rangePattern + "\\S+", "");
			result = StringUtils.replace(result, "\\S+" + rangePattern, "");
		}
		return StringUtils.replace(result, rangePattern, " ");
	}


	/*
	 * Test
	 */
  public static void main(String[] args)
                         throws Exception {
		log.debug(StringUtils.replace(args[0], args[1], args[2]));
	}
}