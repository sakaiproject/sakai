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
package org.sakaiproject.tool.assessment.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.util.ImageMapCoordinates;

public class GradingServiceImageMapTest {

    private static final String SMALL_REGION = "{\"x1\":100,\"y1\":100,\"x2\":110,\"y2\":110}";
    private static final String CLICK_IN_REGION = "{\"click\":true,\"x\":105,\"y\":105}";
    private static final String CLICK_OUTSIDE_REGION = "{\"click\":true,\"x\":50,\"y\":50}";
    private static final String LEGACY_CORNER_OUTSIDE_CLICK_INSIDE = "{\"x\":96,\"y\":97}";

    private GradingService gradingService;

    @Before
    public void setUp() {
        gradingService = new GradingService();
    }

    @Test
    public void clickInsideSmallRegionIsCorrect() {
        HotSpotFixture fixture = hotspot(SMALL_REGION);
        fixture.grading.setAnswerText(CLICK_IN_REGION);

        double score = gradingService.getImageMapScore(fixture.grading, fixture.item, fixture.itemTexts, fixture.answers);

        assertEquals(1.0, score, 0.001);
        assertEquals(Boolean.TRUE, fixture.grading.getIsCorrect());
    }

    @Test
    public void clickOutsideSmallRegionIsIncorrect() {
        HotSpotFixture fixture = hotspot(SMALL_REGION);
        fixture.grading.setAnswerText(CLICK_OUTSIDE_REGION);

        double score = gradingService.getImageMapScore(fixture.grading, fixture.item, fixture.itemTexts, fixture.answers);

        assertEquals(0.0, score, 0.001);
        assertEquals(Boolean.FALSE, fixture.grading.getIsCorrect());
    }

    @Test
    public void legacyMarkerCornerOutsideSmallRegionStillScoresWhenClickWouldBeInside() {
        HotSpotFixture fixture = hotspot(SMALL_REGION);
        fixture.grading.setAnswerText(LEGACY_CORNER_OUTSIDE_CLICK_INSIDE);

        double score = gradingService.getImageMapScore(fixture.grading, fixture.item, fixture.itemTexts, fixture.answers);

        assertEquals(1.0, score, 0.001);
        assertEquals(Boolean.TRUE, fixture.grading.getIsCorrect());
        ImageMapCoordinates.Point point = ImageMapCoordinates.parseStudentPoint(LEGACY_CORNER_OUTSIDE_CLICK_INSIDE).get();
        assertFalse(point.isClickStored());
        assertEquals(105.0, point.getClickX(), 0.001);
        assertEquals(105.0, point.getClickY(), 0.001);
    }

    @Test
    public void unparseableAnswerScoresZero() {
        HotSpotFixture fixture = hotspot(SMALL_REGION);
        fixture.grading.setAnswerText("undefined");

        double score = gradingService.getImageMapScore(fixture.grading, fixture.item, fixture.itemTexts, fixture.answers);

        assertEquals(0.0, score, 0.001);
        assertEquals(Boolean.FALSE, fixture.grading.getIsCorrect());
    }

    private static HotSpotFixture hotspot(String regionJson) {
        PublishedItemData item = new PublishedItemData();
        item.setItemId(16L);
        item.setScore(1.0);

        PublishedItemText itemText = new PublishedItemText();
        itemText.setId(9001L);
        itemText.setItem(item);
        itemText.setSequence(1L);
        itemText.setText("Right eye");

        PublishedAnswer answer = new PublishedAnswer();
        answer.setId(1L);
        answer.setItem(item);
        answer.setItemText(itemText);
        answer.setText(regionJson);
        answer.setIsCorrect(Boolean.TRUE);

        Set<AnswerIfc> answers = new HashSet<>();
        answers.add(answer);
        itemText.setAnswerSet(answers);

        ItemGradingData grading = new ItemGradingData();
        grading.setPublishedItemId(16L);
        grading.setPublishedItemTextId(9001L);

        Map<Long, ItemTextIfc> itemTexts = new HashMap<>();
        itemTexts.put(9001L, itemText);
        Map<Long, AnswerIfc> publishedAnswers = new HashMap<>();
        publishedAnswers.put(1L, answer);

        return new HotSpotFixture(item, grading, itemTexts, publishedAnswers);
    }

    private static final class HotSpotFixture {
        private final PublishedItemData item;
        private final ItemGradingData grading;
        private final Map<Long, ItemTextIfc> itemTexts;
        private final Map<Long, AnswerIfc> answers;

        private HotSpotFixture(PublishedItemData item, ItemGradingData grading, Map<Long, ItemTextIfc> itemTexts,
                Map<Long, AnswerIfc> answers) {
            this.item = item;
            this.grading = grading;
            this.itemTexts = itemTexts;
            this.answers = answers;
        }
    }
}
