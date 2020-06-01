/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/FormattedText.java $
 * $Id: FormattedText.java 97738 2011-08-31 17:30:03Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util.api;

import java.text.NumberFormat;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Element;

/**
 * This is a special Mock which will allow unit testing using FormattedText to generally still work,
 * This is necessary because of all the places where it is used
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
@Slf4j
public class MockFormattedText implements FormattedText {
    private static final String WARNING = "Using MOCK FormattedText: all values just pass through and are not processed: FOR TESTING ONLY (if this is live there is a big problem)";

    public String processFormattedText(String strFromBrowser, StringBuffer errorMessages) {
        log.warn(WARNING);
        return strFromBrowser;
    }

    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages) {
        log.warn(WARNING);
        return strFromBrowser;
    }

    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages, Level level) {
        log.warn(WARNING);
        return strFromBrowser;
    }

    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages, boolean useLegacySakaiCleaner) {
        log.warn(WARNING);
        return strFromBrowser;
    }

    public String processHtmlDocument(String strFromBrowser, StringBuilder errorMessages) {
        log.warn(WARNING);
        return strFromBrowser;
    }

    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
            boolean replaceWhitespaceTags) {
        log.warn(WARNING);
        return strFromBrowser;
    }

    public String processFormattedText(String strFromBrowser, StringBuilder errorMessages, Level level, boolean checkForEvilTags,
            boolean replaceWhitespaceTags, boolean useLegacySakaiCleaner) {
        log.warn(WARNING);
        return strFromBrowser;
    }

    public String escapeHtmlFormattedText(String value) {
        log.warn(WARNING);
        return value;
    }

    public String escapeHtmlFormattedTextSupressNewlines(String value) {
        log.warn(WARNING);
        return value;
    }

    public String escapeHtmlFormattedTextarea(String value) {
        log.warn(WARNING);
        return value;
    }

    public String convertPlaintextToFormattedText(String value) {
        log.warn(WARNING);
        return value;
    }

    public String escapeHtml(String value) {
        log.warn(WARNING);
        return value;
    }

    public String escapeHtml(String value, boolean escapeNewlines) {
        log.warn(WARNING);
        return value;
    }

    public void encodeFormattedTextAttribute(Element element, String baseAttributeName, String value) {
        log.warn(WARNING);
    }

    public String encodeUnicode(String value) {
        log.warn(WARNING);
        return value;
    }

    public String encodeUrlsAsHtml(String text) {
        log.warn(WARNING);
        return text;
    }

    public String unEscapeHtml(String value) {
        log.warn(WARNING);
        return value;
    }

    public String processAnchor(String anchor) {
        log.warn(WARNING);
        return anchor;
    }

    public String processEscapedHtml(String source) {
        log.warn(WARNING);
        return source;
    }

    public String decodeFormattedTextAttribute(Element element, String baseAttributeName) {
        log.warn(WARNING);
        return baseAttributeName;
    }

    public String convertFormattedTextToPlaintext(String value) {
        log.warn(WARNING);
        return value;
    }

    public String convertOldFormattedText(String value) {
        log.warn(WARNING);
        return value;
    }

    public boolean trimFormattedText(String formattedText, int maxNumOfChars, StringBuilder strTrimmed) {
        log.warn(WARNING);
        return false;
    }

    public String decodeNumericCharacterReferences(String value) {
        log.warn(WARNING);
        return value;
    }

    public String escapeJavascript(String value) {
        log.warn(WARNING);
        return value;
    }

    public String escapeJsQuoted(String value) {
        log.warn(WARNING);
        return value;
    }

    public String escapeUrl(String value) {
        log.warn(WARNING);
        return value;
    }

    public boolean validateURL(String urlToValidate) {
        log.warn(WARNING);
        return false;
    }

    public String sanitizeHrefURL(String urlToSanitize) {
        log.warn(WARNING);
        return urlToSanitize;
    }

    @Override
    public String stripHtmlFromText(String text, boolean smartSpacing) {
        log.warn(WARNING);
        return text;
    }

    @Override
    public String stripHtmlFromText(String text, boolean smartSpacing, boolean stripEscapeSequences)
    {
        log.warn(WARNING);
        return text;
    }

    @Override
    public String makeShortenedText(String text, Integer maxLength, String separator, String cutMethod) {
        log.warn(WARNING);
        return text;
    }

    @Override
    public String getDecimalSeparator() {
        log.warn(WARNING);
        return null;
}

    @Override
    public NumberFormat getNumberFormat(Integer maxFractionDigits, Integer minFractionDigits, Boolean groupingUsed) {
    	return null;
    }

    @Override
    public NumberFormat getNumberFormat() {
    	return getNumberFormat(null,null,null);
    }

	@Override
	public String getHtmlBody(String text) {
		// TODO Auto-generated method stub
		return getHtmlBody(null);
	}
    
}
