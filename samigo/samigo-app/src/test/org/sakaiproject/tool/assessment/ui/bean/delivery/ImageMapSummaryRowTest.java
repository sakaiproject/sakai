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
package org.sakaiproject.tool.assessment.ui.bean.delivery;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.util.ResourceLoader;

public class ImageMapSummaryRowTest {

    private static final String REGION_1 = "{\"x1\":10,\"y1\":10,\"x2\":50,\"y2\":50}";
    private static final String REGION_2 = "{\"x1\":30,\"y1\":30,\"x2\":70,\"y2\":70}";

    @Test
    public void outsideClickIsDistinguishedFromMissingResponse() {
        ItemTextIfc itemText = itemText(1L, REGION_1);
        ItemContentsBean item = imageMapItem(List.of(itemText), List.of(row(itemText, "{\"click\":true,\"x\":100,\"y\":100}")));

        ImageMapSummaryRow summary = ImageMapSummaryRow.of(item).get(0);

        assertTrue(summary.isResponsePresent());
        assertNull(summary.getStudentArea());
    }

    @Test
    public void missingResponseIsNotReportedAsPresent() {
        ItemTextIfc itemText = itemText(1L, REGION_1);
        ItemContentsBean item = imageMapItem(List.of(itemText), List.of(row(itemText, null)));

        ImageMapSummaryRow summary = ImageMapSummaryRow.of(item).get(0);

        assertFalse(summary.isResponsePresent());
        assertNull(summary.getStudentArea());
    }

    @Test
    public void overlappingRegionPrefersTheItemsOwnArea() {
        ItemTextIfc first = itemText(1L, REGION_1);
        ItemTextIfc second = itemText(2L, REGION_2);
        ItemContentsBean item = imageMapItem(List.of(first, second), List.of(
                row(first, null),
                row(second, "{\"click\":true,\"x\":40,\"y\":40}")));

        List<ImageMapSummaryRow> rows = ImageMapSummaryRow.of(item);

        assertEquals(Integer.valueOf(2), rows.get(1).getCorrectArea());
        assertEquals(Integer.valueOf(2), rows.get(1).getStudentArea());
        assertTrue(rows.get(1).isResponsePresent());
    }

    private static ItemContentsBean imageMapItem(List<ItemTextIfc> itemTexts, List<ImageMapQuestionBean> rows) {
        ItemDataIfc itemData = mock(ItemDataIfc.class);
        when(itemData.getTypeId()).thenReturn(TypeIfc.IMAGEMAP_QUESTION);
        when(itemData.getItemTextArraySorted()).thenReturn(itemTexts);

        ItemContentsBean item = new ItemContentsBean(mock(ResourceLoader.class));
        item.setItemData(itemData);
        item.setMatchingArray(new ArrayList<>(rows));
        return item;
    }

    private static ItemTextIfc itemText(Long id, String regionJson) {
        AnswerIfc answer = mock(AnswerIfc.class);
        when(answer.getText()).thenReturn(regionJson);

        ItemTextIfc itemText = mock(ItemTextIfc.class);
        when(itemText.getId()).thenReturn(id);
        when(itemText.getAnswerArraySorted()).thenReturn(List.of(answer));
        return itemText;
    }

    private static ImageMapQuestionBean row(ItemTextIfc itemText, String response) {
        ImageMapQuestionBean row = new ImageMapQuestionBean();
        row.setItemText(itemText);
        row.setText("Item " + itemText.getId());
        row.setItemGradingData(new ItemGradingData());
        if (response != null) {
            row.setResponse(response);
        }
        return row;
    }
}
