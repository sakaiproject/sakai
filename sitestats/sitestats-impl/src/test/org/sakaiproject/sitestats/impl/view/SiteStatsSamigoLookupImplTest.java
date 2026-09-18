/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

public class SiteStatsSamigoLookupImplTest {

	private static final Long ESSAY_ONE = 11L;
	private static final Long ESSAY_TWO = 22L;
	private static final Long MULTIPLE_CHOICE = 33L;

	@Test
	public void questionScoringPathCompletesWhenEveryManualItemHasGradedDate() {
		Set<Long> manualItems = manualItems(ESSAY_ONE, ESSAY_TWO);
		Set<ItemGradingData> items = new HashSet<ItemGradingData>();
		items.add(questionScoredItem(ESSAY_ONE));
		items.add(questionScoredItem(ESSAY_TWO));
		items.add(unscoredItem(MULTIPLE_CHOICE));

		assertTrue(SiteStatsSamigoLookupImpl.attemptGraded(null, items, manualItems));
	}

	@Test
	public void questionScoringPathStaysIncompleteUntilEveryManualItemIsGraded() {
		Set<Long> manualItems = manualItems(ESSAY_ONE, ESSAY_TWO);
		Set<ItemGradingData> items = new HashSet<ItemGradingData>();
		items.add(questionScoredItem(ESSAY_ONE));
		items.add(unscoredItem(ESSAY_TWO));

		assertFalse(SiteStatsSamigoLookupImpl.attemptGraded(null, items, manualItems));
	}

	@Test
	public void questionScoringPathDoesNotRequireManualItemsAbsentFromTheAttempt() {
		Set<Long> publishedManualItems = manualItems(ESSAY_ONE, ESSAY_TWO);
		Set<ItemGradingData> items = new HashSet<ItemGradingData>();
		items.add(questionScoredItem(ESSAY_ONE));

		assertTrue(SiteStatsSamigoLookupImpl.attemptGraded(null, items, publishedManualItems));
	}

	@Test
	public void submittedAttemptWithNoManualItemRowsStillNeedsGrading() {
		Set<Long> manualItems = manualItems(ESSAY_ONE, ESSAY_TWO);

		assertFalse(SiteStatsSamigoLookupImpl.attemptGraded(null, Collections.emptySet(), manualItems));
	}

	@Test
	public void totalScorePathCompletesFromAssessmentGradedDate() {
		Set<Long> manualItems = manualItems(ESSAY_ONE);
		Set<ItemGradingData> items = new HashSet<ItemGradingData>();
		items.add(unscoredItem(ESSAY_ONE));

		assertTrue(SiteStatsSamigoLookupImpl.attemptGraded(new Date(), items, manualItems));
	}

	@Test
	public void questionScoringPathStaysIncompleteIfAnyPartOfAManualItemIsUngraded() {
		Set<Long> manualItems = manualItems(ESSAY_ONE);
		Set<ItemGradingData> items = new HashSet<ItemGradingData>();
		items.add(questionScoredItem(ESSAY_ONE));
		items.add(unscoredItem(ESSAY_ONE));

		assertFalse(SiteStatsSamigoLookupImpl.attemptGraded(null, items, manualItems));
	}

	@Test
	public void ungradedEssayWithNullAssessmentGradedDateNeedsGrading() {
		Set<Long> manualItems = manualItems(ESSAY_ONE);
		Set<ItemGradingData> items = new HashSet<ItemGradingData>();
		items.add(unscoredItem(ESSAY_ONE));

		assertFalse(SiteStatsSamigoLookupImpl.attemptGraded(null, items, manualItems));
	}

	@Test
	public void essayAudioAndFileUploadRequireManualGrading() {
		assertTrue(SiteStatsSamigoLookupImpl.isManualGradingType(TypeIfc.ESSAY_QUESTION));
		assertTrue(SiteStatsSamigoLookupImpl.isManualGradingType(TypeIfc.AUDIO_RECORDING));
		assertTrue(SiteStatsSamigoLookupImpl.isManualGradingType(TypeIfc.FILE_UPLOAD));
		assertFalse(SiteStatsSamigoLookupImpl.isManualGradingType(TypeIfc.MULTIPLE_CHOICE));
	}

	private static Set<Long> manualItems(Long... itemIds) {
		Set<Long> ids = new HashSet<Long>();
		for (Long itemId : itemIds) {
			ids.add(itemId);
		}
		return ids;
	}

	/**
	 * Matches GradingService.updateItemScore() / QuestionScoreUpdateListener: item gradedDate is
	 * set, assessment gradedDate is left null.
	 */
	private static ItemGradingData questionScoredItem(Long publishedItemId) {
		ItemGradingData item = unscoredItem(publishedItemId);
		item.setGradedDate(new Date());
		return item;
	}

	private static ItemGradingData unscoredItem(Long publishedItemId) {
		ItemGradingData item = new ItemGradingData();
		item.setPublishedItemId(publishedItemId);
		item.setAutoScore(Double.valueOf(0));
		return item;
	}
}
