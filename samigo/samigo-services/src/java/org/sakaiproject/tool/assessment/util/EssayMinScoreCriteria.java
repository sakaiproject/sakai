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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

/**
 * Optional criteria an essay answer must meet before the item's minimum score is awarded:
 * a minimum number of words, and a comma-separated list of keywords that must all appear.
 * Keywords match whole words, ignoring case; a keyword with spaces matches as a phrase.
 * Chinese and Japanese characters count as one word each, as word processors count them.
 */
public final class EssayMinScoreCriteria {

    private static final String WHITESPACE = "[\\s\\p{Z}]";
    private static final String CJK = "\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}";
    private static final Pattern WORDS = Pattern.compile("[" + CJK + "]|[^" + CJK + "\\s\\p{Z}]+");
    // CJK text has no spaces, so its characters don't bound a keyword
    private static final String WORD_CHAR = "[\\p{L}\\p{N}_&&[^" + CJK + "]]";

    private EssayMinScoreCriteria() {
    }

    /**
     * The criteria as item metadata, keyed by label. Empty entries mean the criterion isn't set.
     * Only essays have criteria; other question types get an empty map.
     */
    public static Map<String, String> toMetaData(Long typeId, Integer wordCount, String keywords) {
        Map<String, String> metaData = new LinkedHashMap<>();
        if (TypeIfc.ESSAY_QUESTION.equals(typeId)) {
            metaData.put(ItemMetaDataIfc.MIN_SCORE_WORD_COUNT, wordCount != null && wordCount > 0 ? wordCount.toString() : "");
            metaData.put(ItemMetaDataIfc.MIN_SCORE_KEYWORDS, StringUtils.trimToEmpty(keywords));
        }
        return metaData;
    }

    /**
     * @param item the question; criteria only apply to essays
     * @param answerText the student's answer, possibly HTML
     * @return true if the answer meets every criterion set on the item
     */
    public static boolean isMet(ItemDataIfc item, String answerText) {
        if (!TypeIfc.ESSAY_QUESTION.equals(item.getTypeId())) {
            return true;
        }
        String text = StringUtils.isBlank(answerText) ? "" : Jsoup.parse(answerText).text();

        int minWords = NumberUtils.toInt(StringUtils.trimToEmpty(item.getItemMetaDataByLabel(ItemMetaDataIfc.MIN_SCORE_WORD_COUNT)));
        if (minWords > 0 && countWords(text) < minWords) {
            return false;
        }

        return Arrays.stream(StringUtils.split(StringUtils.defaultString(item.getItemMetaDataByLabel(ItemMetaDataIfc.MIN_SCORE_KEYWORDS)), ','))
                .map(StringUtils::trimToNull)
                .filter(StringUtils::isNotEmpty)
                .allMatch(keyword -> containsWholeWords(text, keyword));
    }

    private static int countWords(String text) {
        Matcher matcher = WORDS.matcher(text);
        int count = 0;
        while (matcher.find()) {
            // Punctuation on its own isn't a word
            if (matcher.group().codePoints().anyMatch(Character::isLetterOrDigit)) {
                count++;
            }
        }
        return count;
    }

    private static boolean containsWholeWords(String text, String keyword) {
        String phrase = Arrays.stream(keyword.split(WHITESPACE + "+"))
                .map(Pattern::quote)
                .collect(Collectors.joining(WHITESPACE + "+"));
        Pattern pattern = Pattern.compile("(?<!" + WORD_CHAR + ")" + phrase + "(?!" + WORD_CHAR + ")",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return pattern.matcher(text).find();
    }
}
