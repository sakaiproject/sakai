/*
 * Copyright (c) 2026, The Apereo Foundation
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
 *
 */
package org.sakaiproject.tool.assessment.util;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;

/**
 * Optional criteria an essay answer must meet before the item's minimum score is awarded:
 * a minimum number of words, and a comma-separated list of keywords that must all appear.
 * Keywords match whole words, ignoring case; a keyword with spaces matches as a phrase.
 */
public final class EssayMinScoreCriteria {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\p{Z}]+");
    private static final String WORD_CHAR = "[\\p{L}\\p{N}_]";

    private EssayMinScoreCriteria() {
    }

    /**
     * @param answerText the student's answer, possibly HTML
     * @param wordCount the minimum number of words; blank or not a positive number means no minimum
     * @param keywords comma-separated keywords; blank means none are required
     * @return true if the answer meets every criterion that is set
     */
    public static boolean isMet(String answerText, String wordCount, String keywords) {
        String text = plainText(answerText);

        int minWords = NumberUtils.toInt(StringUtils.trimToEmpty(wordCount), 0);
        if (minWords > 0 && countWords(text) < minWords) {
            return false;
        }

        return Arrays.stream(StringUtils.split(StringUtils.defaultString(keywords), ','))
                .map(StringUtils::trimToNull)
                .filter(StringUtils::isNotEmpty)
                .allMatch(keyword -> containsWholeWords(text, keyword));
    }

    static int countWords(String text) {
        String trimmed = WHITESPACE.matcher(StringUtils.defaultString(text)).replaceAll(" ").trim();
        return trimmed.isEmpty() ? 0 : trimmed.split(" ").length;
    }

    private static String plainText(String answerText) {
        return StringUtils.isBlank(answerText) ? "" : Jsoup.parse(answerText).text();
    }

    private static boolean containsWholeWords(String text, String keyword) {
        String phrase = Arrays.stream(WHITESPACE.split(keyword))
                .map(Pattern::quote)
                .collect(Collectors.joining("[\\s\\p{Z}]+"));
        Pattern pattern = Pattern.compile("(?<!" + WORD_CHAR + ")" + phrase + "(?!" + WORD_CHAR + ")",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return pattern.matcher(text).find();
    }
}
