/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.delivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener.answeredItemIds;
import static org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener.dropRowsForFrozenItems;
import static org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener.hasAnswerContent;
import static org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener.shouldPreserveExistingAnswer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;

/**
 * SAK-44349: persistence backstop rules. Posts that were not verified by the
 * stale-tab guard (forced time-expiry submit, token-less paths) may add or
 * change answers but must never blank or delete existing ones.
 */
public class SubmitToGradingBackstopTest {

    private ItemGradingData item(Long gradingId, Long publishedAnswerId, String answerText) {
        return item(gradingId, 100L, publishedAnswerId, answerText);
    }

    private ItemGradingData item(Long gradingId, Long publishedItemId, Long publishedAnswerId, String answerText) {
        ItemGradingData item = new ItemGradingData();
        item.setItemGradingId(gradingId);
        item.setPublishedItemId(publishedItemId);
        item.setPublishedAnswerId(publishedAnswerId);
        item.setAnswerText(answerText);
        return item;
    }

    @Test
    public void answerContentRecognizesSelectionsAndText() {
        assertFalse(hasAnswerContent(null));
        assertFalse(hasAnswerContent(item(1L, null, null)));
        assertFalse(hasAnswerContent(item(1L, null, "   ")));
        assertTrue(hasAnswerContent(item(1L, 42L, null)));
        assertTrue(hasAnswerContent(item(1L, null, "an essay")));
    }

    @Test
    public void verifiedPostsKeepFullAuthorityIncludingIntentionalClears() {
        ItemGradingData saved = item(1L, 42L, "saved answer");
        ItemGradingData blank = item(1L, null, "");
        assertFalse(shouldPreserveExistingAnswer(true, saved, blank));
    }

    @Test
    public void unverifiedPostsCannotBlankASavedAnswer() {
        ItemGradingData saved = item(1L, 42L, null);
        ItemGradingData blank = item(1L, null, " ");
        assertTrue(shouldPreserveExistingAnswer(false, saved, blank));

        ItemGradingData savedText = item(2L, null, "essay text");
        assertTrue(shouldPreserveExistingAnswer(false, savedText, item(2L, null, null)));
    }

    @Test
    public void unverifiedPostsMayStillAddOrImproveAnswers() {
        // nothing saved yet: writing a first answer is fine
        assertFalse(shouldPreserveExistingAnswer(false, item(1L, null, null), item(1L, 42L, null)));
        // At THIS row-level layer a non-blank replacement passes; in practice
        // the item-level freeze (dropRowsForFrozenItems, applied first) drops
        // replacements for answered items, so this layer only sees them when
        // the freeze was skipped. It must then still block blanking only.
        assertFalse(shouldPreserveExistingAnswer(false, item(1L, 42L, null), item(1L, 43L, null)));
        assertFalse(shouldPreserveExistingAnswer(false, item(1L, null, "old"), item(1L, null, "new")));
    }

    @Test
    public void answeredItemsAreDetectedFromPersistedRows() {
        Set<Long> answered = answeredItemIds(Arrays.asList(
            item(1L, 100L, 42L, null),      // MC answered
            item(2L, 200L, null, "essay"),  // essay answered
            item(3L, 300L, null, "  "),     // blank row: not answered
            item(4L, null, 42L, null)));    // no item id: ignored
        assertEquals(new HashSet<>(Arrays.asList(100L, 200L)), answered);
        assertTrue(answeredItemIds(null).isEmpty());
    }

    @Test
    public void frozenItemsRejectBothRemovalsAndReplacementRows() {
        // The changed-MC-answer-then-stale-timeout case: old row for item 100
        // in removes, replacement row (different answer) in adds. Both must be
        // dropped or a single-select item ends up with two rows (or a
        // uniqueStudentResponse constraint violation).
        Set<Long> frozen = new HashSet<>(Arrays.asList(100L));
        Set<ItemGradingData> removes = new HashSet<>(Arrays.asList(item(1L, 100L, 42L, null)));
        Set<ItemGradingData> adds = new HashSet<>(Arrays.asList(
            item(null, 100L, 43L, null),   // replacement for frozen item: drop
            item(null, 200L, null, "new essay text")));  // first-time answer: keep

        assertEquals(1, dropRowsForFrozenItems(removes, frozen));
        assertEquals(1, dropRowsForFrozenItems(adds, frozen));
        assertTrue(removes.isEmpty());
        assertEquals(1, adds.size());
        assertEquals(Long.valueOf(200L), adds.iterator().next().getPublishedItemId());
    }

    @Test
    public void nothingIsFrozenWhenNothingWasAnswered() {
        Set<ItemGradingData> adds = new HashSet<>(Arrays.asList(item(null, 100L, 43L, null)));
        assertEquals(0, dropRowsForFrozenItems(adds, answeredItemIds(Arrays.asList(item(1L, 100L, null, " ")))));
        assertEquals(1, adds.size());
    }
}
