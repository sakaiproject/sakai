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

import org.junit.Test;

public class EssayMinScoreCriteriaTest {

    @Test
    public void noCriteriaIsAlwaysMet() {
        assertTrue(EssayMinScoreCriteria.isMet("Hi", null, null));
        assertTrue(EssayMinScoreCriteria.isMet("Hi", "", " , "));
        assertTrue(EssayMinScoreCriteria.isMet("Hi", "not a number", ""));
        assertTrue(EssayMinScoreCriteria.isMet("Hi", "0", null));
    }

    @Test
    public void wordCountIsAMinimum() {
        assertFalse(EssayMinScoreCriteria.isMet("one two", "3", null));
        assertTrue(EssayMinScoreCriteria.isMet("one two three", "3", null));
        assertTrue(EssayMinScoreCriteria.isMet("one two three four", " 3 ", null));
    }

    @Test
    public void wordCountIgnoresMarkupAndEntities() {
        // Block elements and &nbsp; separate words; tags are not words.
        assertEquals(4, EssayMinScoreCriteria.countWords(
                org.jsoup.Jsoup.parse("<p>one&nbsp;two</p><p><strong>three</strong>  four</p>").text()));
        assertTrue(EssayMinScoreCriteria.isMet("<p>one&nbsp;two</p><p><strong>three</strong></p>", "3", null));
        assertFalse(EssayMinScoreCriteria.isMet("<p></p><br/>", "1", null));
    }

    @Test
    public void allKeywordsAreRequired() {
        String answer = "<p>The cell wall protects the Plant cell.</p>";
        assertTrue(EssayMinScoreCriteria.isMet(answer, null, "cell wall, plant"));
        assertFalse(EssayMinScoreCriteria.isMet(answer, null, "cell wall, plant, nucleus"));
    }

    @Test
    public void keywordsMatchWholeWordsIgnoringCase() {
        assertTrue(EssayMinScoreCriteria.isMet("Photosynthesis happens.", null, "PHOTOSYNTHESIS"));
        assertFalse(EssayMinScoreCriteria.isMet("Cellular respiration", null, "cell"));
        assertTrue(EssayMinScoreCriteria.isMet("A cell, then another.", null, "cell"));
        assertTrue(EssayMinScoreCriteria.isMet("I use C++ daily", null, "C++"));
    }

    @Test
    public void phraseKeywordsAllowAnyWhitespaceBetweenWords() {
        assertTrue(EssayMinScoreCriteria.isMet("<p>cell</p><p>wall</p>", null, "cell  wall"));
        assertFalse(EssayMinScoreCriteria.isMet("cell and wall", null, "cell wall"));
    }

    @Test
    public void bothCriteriaMustBeMet() {
        assertFalse(EssayMinScoreCriteria.isMet("cell wall", "3", "cell"));
        assertFalse(EssayMinScoreCriteria.isMet("the plant grows tall", "3", "cell"));
        assertTrue(EssayMinScoreCriteria.isMet("the plant cell grows", "3", "cell"));
    }
}
