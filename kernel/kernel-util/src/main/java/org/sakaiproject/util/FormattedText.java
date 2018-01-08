/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.api.FormattedText.Level;
import org.sakaiproject.util.api.MockFormattedText;
import org.w3c.dom.Element;
import java.text.NumberFormat;

/**
 * COVER
 * FormattedText provides support for user entry of formatted text; the formatted text is HTML. This
 * includes text formatting in user input such as bold, underline, and fonts.
 * 
 * @deprecated use the {@link FormattedText} service instead of this cover
 */
@Deprecated
public class FormattedText {

    private static final Logger log = LoggerFactory.getLogger(FormattedText.class);

    private static Object LOCK = new Object();
    private static org.sakaiproject.util.api.FormattedText formattedText;
    protected static org.sakaiproject.util.api.FormattedText getFormattedText() {
        if (formattedText == null) {
            synchronized (LOCK) {
                org.sakaiproject.util.api.FormattedText component = (org.sakaiproject.util.api.FormattedText) ComponentManager.get(org.sakaiproject.util.api.FormattedText.class);
                if (component == null) {
                    log.warn("Unable to find the FormattedText using the ComponentManager (this is OK if this is a unit test)");
                    // we will just make a new mock one each time but we will also keep trying to find one in the CM
                    return new MockFormattedText();
                } else {
                    formattedText = component;
                }
            }
        }
        return formattedText;
    }

    public static String processFormattedText(String strFromBrowser, StringBuffer errorMessages) {
        return getFormattedText().processFormattedText(strFromBrowser, errorMessages);
    }

    public static String processFormattedText(String strFromBrowser, StringBuilder errorMessages) {
        return getFormattedText().processFormattedText(strFromBrowser, errorMessages);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#processFormattedText(String, StringBuilder, Level)
     */
    public static String processFormattedText(String strFromBrowser, StringBuilder errorMessages, Level level) {
        return getFormattedText().processFormattedText(strFromBrowser, errorMessages, level);
    }

    public static String processFormattedText(String strFromBrowser, StringBuilder errorMessages, boolean useLegacySakaiCleaner) {
        return getFormattedText().processFormattedText(strFromBrowser, errorMessages, useLegacySakaiCleaner);
    }

    public static String processHtmlDocument(String strFromBrowser, StringBuilder errorMessages) {
        return getFormattedText().processHtmlDocument(strFromBrowser, errorMessages);
    }

    public static String processFormattedText(String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
            boolean replaceWhitespaceTags) {
        return getFormattedText().processFormattedText(strFromBrowser, errorMessages, checkForEvilTags, replaceWhitespaceTags);
    }

    public static String processFormattedText(String strFromBrowser, StringBuilder errorMessages, boolean checkForEvilTags,
            boolean replaceWhitespaceTags, boolean useLegacySakaiCleaner) {
        return getFormattedText().processFormattedText(strFromBrowser, errorMessages, null, 
                checkForEvilTags, replaceWhitespaceTags, useLegacySakaiCleaner);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#processFormattedText(String, StringBuilder, Level, boolean, boolean, boolean)
     */
    public static String processFormattedText(String strFromBrowser, StringBuilder errorMessages, Level level, 
            boolean checkForEvilTags, boolean replaceWhitespaceTags, boolean useLegacySakaiCleaner) {
        return getFormattedText().processFormattedText(strFromBrowser, errorMessages, level, 
                checkForEvilTags, replaceWhitespaceTags, useLegacySakaiCleaner);
    }

    public static String escapeHtmlFormattedText(String value) {
        return getFormattedText().escapeHtmlFormattedText(value);
    }

    public static String escapeHtmlFormattedTextSupressNewlines(String value) {
        return getFormattedText().escapeHtmlFormattedTextSupressNewlines(value);
    }

    public static String escapeHtmlFormattedTextarea(String value) {
        return getFormattedText().escapeHtmlFormattedTextarea(value);
    }

    public static String convertPlaintextToFormattedText(String value) {
        return getFormattedText().convertPlaintextToFormattedText(value);
    }

    public static String escapeHtml(String value, boolean escapeNewlines) {
        return getFormattedText().escapeHtml(value, escapeNewlines);
    }

    public static void encodeFormattedTextAttribute(Element element, String baseAttributeName, String value) {
        getFormattedText().encodeFormattedTextAttribute(element, baseAttributeName, value);
    }

    public static String encodeUnicode(String value) {
        return getFormattedText().encodeUnicode(value);
    }

    public static String unEscapeHtml(String value) {
        return getFormattedText().unEscapeHtml(value);
    }

    public static String processAnchor(String anchor) {
        return getFormattedText().processAnchor(anchor);
    }

    public static String processEscapedHtml(String source) {
        return getFormattedText().processEscapedHtml(source);
    }

    public static String decodeFormattedTextAttribute(Element element, String baseAttributeName) {
        return getFormattedText().decodeFormattedTextAttribute(element, baseAttributeName);
    }

    public static String convertFormattedTextToPlaintext(String value) {
        return getFormattedText().convertFormattedTextToPlaintext(value);
    }

    public static String convertOldFormattedText(String value) {
        return getFormattedText().convertOldFormattedText(value);
    }

    public static boolean trimFormattedText(String formattedText, int maxNumOfChars, StringBuilder strTrimmed) {
        return getFormattedText().trimFormattedText(formattedText, maxNumOfChars, strTrimmed);
    }

    public static String decodeNumericCharacterReferences(String value) {
        return getFormattedText().decodeNumericCharacterReferences(value);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#validateURL(String)
     */
    public static boolean validateURL(String urlToValidate) {
        return getFormattedText().validateURL(urlToValidate);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#sanitizeHrefURL(String)
     */
    public static String sanitizeHrefURL(String urlToSanitize) {
        return getFormattedText().sanitizeHrefURL(urlToSanitize);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#stripHtmlFromText(String,boolean)
     */
    public static String stripHtmlFromText(String text, boolean smartSpacing) {
        return getFormattedText().stripHtmlFromText(text, smartSpacing);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#stripHtmlFromText(String,boolean,boolean)
     */
    public static String stripHtmlFromText(String text, boolean smartSpacing, boolean stripEscapeSequences) {
        return getFormattedText().stripHtmlFromText(text, smartSpacing, stripEscapeSequences);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#makeShortenedText(String)
     */
    public static String makeShortenedText(String text, Integer maxLength, String separator, String cutMethod) {
        return getFormattedText().makeShortenedText(text, maxLength, separator, cutMethod);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#escapeJsQuoted(String)
     */
    public static String escapeJsQuoted(String text) {
        return getFormattedText().escapeJsQuoted(text);
    }

    
    /**
     * @see org.sakaiproject.util.api.FormattedText#getDecimalSeparator()
     */
    public static String getDecimalSeparator() {
        return getFormattedText().getDecimalSeparator();
}
    
    /**
     * @see org.sakaiproject.util.api.FormattedText#getNumberFormat(Integer,Integer,Boolean)
     */
    public static NumberFormat getNumberFormat(Integer maxFractionDigits, Integer minFractionDigits, Boolean groupingUsed) {
    	return getFormattedText().getNumberFormat(maxFractionDigits, minFractionDigits, groupingUsed);
    }

    /**
     * @see org.sakaiproject.util.api.FormattedText#getNumberFormat()
     */
    public static NumberFormat getNumberFormat() {
    	return getFormattedText().getNumberFormat();
    }
    
    /**
     * @see org.sakaiproject.util.api.FormattedText#getHtmlBody(String)
     */
    public static String getHtmlBody(String text) {
    	return getFormattedText().getHtmlBody(text);
    }

}
