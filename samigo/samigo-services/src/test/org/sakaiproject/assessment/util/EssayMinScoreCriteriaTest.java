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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public class EssayMinScoreCriteriaTest {

    @Test
    public void noCriteriaIsAlwaysMet() {
        assertTrue(EssayMinScoreCriteria.isMet(essay(null, null), "Hi"));
        assertTrue(EssayMinScoreCriteria.isMet(essay("", " , "), "Hi"));
        assertTrue(EssayMinScoreCriteria.isMet(essay("not a number", ""), "Hi"));
        assertTrue(EssayMinScoreCriteria.isMet(essay("0", null), "Hi"));
    }

    @Test
    public void onlyEssaysHaveCriteria() {
        ItemData shortAnswer = essay("5", "cell");
        shortAnswer.setTypeId(TypeIfc.FILL_IN_BLANK);
        assertTrue(EssayMinScoreCriteria.isMet(shortAnswer, "one"));
        assertTrue(EssayMinScoreCriteria.toMetaData(TypeIfc.FILL_IN_BLANK, 5, "cell").isEmpty());
    }

    @Test
    public void toMetaDataNormalizesEntries() {
        Map<String, String> metaData = EssayMinScoreCriteria.toMetaData(TypeIfc.ESSAY_QUESTION, 5, "  cell, wall ");
        assertEquals("5", metaData.get(ItemMetaDataIfc.MIN_SCORE_WORD_COUNT));
        assertEquals("cell, wall", metaData.get(ItemMetaDataIfc.MIN_SCORE_KEYWORDS));

        Map<String, String> cleared = EssayMinScoreCriteria.toMetaData(TypeIfc.ESSAY_QUESTION, 0, null);
        assertEquals("", cleared.get(ItemMetaDataIfc.MIN_SCORE_WORD_COUNT));
        assertEquals("", cleared.get(ItemMetaDataIfc.MIN_SCORE_KEYWORDS));
    }

    @Test
    public void wordCountIsAMinimum() {
        assertFalse(EssayMinScoreCriteria.isMet(essay("3", null), "one two"));
        assertTrue(EssayMinScoreCriteria.isMet(essay("3", null), "one two three"));
        assertTrue(EssayMinScoreCriteria.isMet(essay(" 3 ", null), "one two three four"));
    }

    @Test
    public void punctuationIsNotAWord() {
        assertFalse(EssayMinScoreCriteria.isMet(essay("5", null), ". . . . . -- !!"));
        assertTrue(EssayMinScoreCriteria.isMet(essay("3", null), "cell-wall , is ... strong"));
    }

    @Test
    public void wordCountIgnoresMarkupAndEntities() {
        // Block elements and &nbsp; separate words; tags are not words.
        assertTrue(EssayMinScoreCriteria.isMet(essay("4", null), "<p>one&nbsp;two</p><p><strong>three</strong>  four</p>"));
        assertFalse(EssayMinScoreCriteria.isMet(essay("5", null), "<p>one&nbsp;two</p><p><strong>three</strong>  four</p>"));
        assertFalse(EssayMinScoreCriteria.isMet(essay("1", null), "<p></p><br/>"));
    }

    @Test
    public void chineseAndJapaneseCharactersCountAsWords() {
        // 10 Han characters, then 2 kana and 1 Latin word
        assertTrue(EssayMinScoreCriteria.isMet(essay("10", null), "植物细胞有细胞壁保护"));
        assertFalse(EssayMinScoreCriteria.isMet(essay("11", null), "植物细胞有细胞壁保护"));
        assertTrue(EssayMinScoreCriteria.isMet(essay("3", null), "です DNA"));
    }

    @Test
    public void allKeywordsAreRequired() {
        String answer = "<p>The cell wall protects the Plant cell.</p>";
        assertTrue(EssayMinScoreCriteria.isMet(essay(null, "cell wall, plant"), answer));
        assertFalse(EssayMinScoreCriteria.isMet(essay(null, "cell wall, plant, nucleus"), answer));
    }

    @Test
    public void keywordsMatchWholeWordsIgnoringCase() {
        assertTrue(EssayMinScoreCriteria.isMet(essay(null, "PHOTOSYNTHESIS"), "Photosynthesis happens."));
        assertFalse(EssayMinScoreCriteria.isMet(essay(null, "cell"), "Cellular respiration"));
        assertTrue(EssayMinScoreCriteria.isMet(essay(null, "cell"), "A cell, then another."));
        assertTrue(EssayMinScoreCriteria.isMet(essay(null, "C++"), "I use C++ daily"));
    }

    @Test
    public void keywordsMatchInsideChineseText() {
        assertTrue(EssayMinScoreCriteria.isMet(essay(null, "细胞壁"), "植物细胞有细胞壁保护"));
        assertFalse(EssayMinScoreCriteria.isMet(essay(null, "细胞核"), "植物细胞有细胞壁保护"));
    }

    @Test
    public void phraseKeywordsAllowAnyWhitespaceBetweenWords() {
        assertTrue(EssayMinScoreCriteria.isMet(essay(null, "cell  wall"), "<p>cell</p><p>wall</p>"));
        assertFalse(EssayMinScoreCriteria.isMet(essay(null, "cell wall"), "cell and wall"));
    }

    @Test
    public void bothCriteriaMustBeMet() {
        assertFalse(EssayMinScoreCriteria.isMet(essay("3", "cell"), "cell wall"));
        assertFalse(EssayMinScoreCriteria.isMet(essay("3", "cell"), "the plant grows tall"));
        assertTrue(EssayMinScoreCriteria.isMet(essay("3", "cell"), "the plant cell grows"));
    }

    private ItemData essay(String wordCount, String keywords) {
        ItemData item = new ItemData();
        item.setTypeId(TypeIfc.ESSAY_QUESTION);
        item.setItemMetaDataSet(new HashSet<>());
        if (wordCount != null) {
            item.addItemMetaData(ItemMetaDataIfc.MIN_SCORE_WORD_COUNT, wordCount);
        }
        if (keywords != null) {
            item.addItemMetaData(ItemMetaDataIfc.MIN_SCORE_KEYWORDS, keywords);
        }
        return item;
    }
}
