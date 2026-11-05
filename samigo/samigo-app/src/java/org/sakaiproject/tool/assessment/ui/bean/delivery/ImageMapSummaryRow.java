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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.util.ImageMapCoordinates;

/**
 * One row of the Hot Spot summary: which area is correct and which area the student marked.
 * Used by the student report PDF and the grading JSP.
 */
public final class ImageMapSummaryRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String label;
    private final Integer correctArea;
    private final Integer studentArea;
    private final Boolean correct;
    private final boolean responsePresent;

    public ImageMapSummaryRow(String label, Integer correctArea, Integer studentArea, Boolean correct,
            boolean responsePresent) {
        this.label = label;
        this.correctArea = correctArea;
        this.studentArea = studentArea;
        this.correct = correct;
        this.responsePresent = responsePresent;
    }

    public String getLabel() {
        return label;
    }

    public Integer getCorrectArea() {
        return correctArea;
    }

    public Integer getStudentArea() {
        return studentArea;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public boolean isResponsePresent() {
        return responsePresent;
    }

    /**
     * Builds the summary for an image-map item. Clicks are matched to the authored regions so the
     * result does not depend on drawing or label placement.
     */
    public static List<ImageMapSummaryRow> of(ItemContentsBean item) {
        List<ImageMapSummaryRow> rows = new ArrayList<>();
        if (item == null || item.getItemData() == null
                || !TypeIfc.IMAGEMAP_QUESTION.equals(item.getItemData().getTypeId())
                || item.getMatchingArray() == null) {
            return rows;
        }

        List<ItemTextIfc> itemTexts = item.getItemData().getItemTextArraySorted();
        Map<Long, Integer> areaByItemTextId = new HashMap<>();
        List<String> regionJsons = new ArrayList<>();
        if (itemTexts != null) {
            int area = 1;
            for (ItemTextIfc itemText : itemTexts) {
                if (itemText == null) {
                    continue;
                }
                if (itemText.getId() != null) {
                    areaByItemTextId.put(itemText.getId(), area);
                }
                regionJsons.add(firstRegionJson(itemText));
                area++;
            }
        }

        for (MatchingEntry entry : item.getMatchingArray()) {
            if (!(entry instanceof ImageMapQuestionBean)) {
                continue;
            }
            ImageMapQuestionBean imageMapBean = (ImageMapQuestionBean) entry;
            Long itemTextId = imageMapBean.getItemText() == null ? null : imageMapBean.getItemText().getId();
            Integer correctArea = itemTextId == null ? null : areaByItemTextId.get(itemTextId);
            boolean responsePresent = ImageMapCoordinates.parseStudentPoint(imageMapBean.getResponse()).isPresent();
            Integer studentArea = responsePresent
                    ? ImageMapCoordinates.findRegionIndex(imageMapBean.getResponse(), regionJsons, correctArea).orElse(null)
                    : null;
            rows.add(new ImageMapSummaryRow(imageMapBean.getText(), correctArea, studentArea,
                    imageMapBean.getIsCorrect(), responsePresent));
        }
        return rows;
    }

    private static String firstRegionJson(ItemTextIfc itemText) {
        List<AnswerIfc> answers = itemText.getAnswerArraySorted();
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        AnswerIfc answer = answers.get(0);
        return answer == null ? null : answer.getText();
    }
}
