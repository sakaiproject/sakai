/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.CategoryScoreData;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExcuseGradeAction extends InjectableAction implements Serializable {

    private static final long serialVersionUID = 1L;
    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    private GradebookNgBusinessService businessService;

    private class ExcuseGradeResponse implements ActionResponse {
        private String courseGrade;
        private String points;
        private String categoryScore;
        private boolean isOverride;
        private List<Long> droppedItems;

        public ExcuseGradeResponse(final String courseGrade, final String points, final boolean isOverride, final String categoryScore,
                                   List<Long> droppedItems) {
            this.courseGrade = courseGrade;
            this.categoryScore = categoryScore;
            this.points = points;
            this.isOverride = isOverride;
            this.droppedItems = droppedItems;
        }

        public String getStatus() {
            return "OK";
        }

        public String toJson() {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();

            ArrayNode courseGradeArray = mapper.createArrayNode();
            courseGradeArray.add(courseGrade);
            courseGradeArray.add(points);
            courseGradeArray.add(isOverride ? 1 : 0);

            result.put("courseGrade", courseGradeArray);
            result.put("categoryScore", categoryScore);

            ArrayNode catDroppedItemsArray = mapper.createArrayNode();
            droppedItems.stream().forEach(i -> catDroppedItemsArray.add(i));
            result.put("categoryDroppedItems", catDroppedItemsArray);

            return result.toString();
        }
    }

    @Override
    public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
        final GradebookPage page = (GradebookPage) target.getPage();

        target.addChildren(page, FeedbackPanel.class);

        final String assignmentId = params.get("assignmentId").asText();
        final String studentUuid = params.get("studentId").asText();
        String excuse = params.get("excuseBit").asText();
        final String categoryId = params.has("categoryId") ? params.get("categoryId").asText() : null;

        boolean hasExcuse = false;
        if (StringUtils.equals(excuse, "1")) {
            excuse = "0";
        } else if (StringUtils.equals(excuse, "0")) {
            excuse = "1";
            hasExcuse = true;
        }

        final GradeSaveResponse result = businessService.saveExcuse(Long.valueOf(assignmentId),
                studentUuid, hasExcuse);

        if (result.equals(GradeSaveResponse.NO_CHANGE)) {
            target.add(page.updateLiveGradingMessage(page.getString("feedback.saved")));
        }

        target.appendJavaScript(
                String.format("GbGradeTable.updateExcuse('%s', '%s', '%s');", assignmentId, studentUuid, excuse));


        final CourseGrade studentCourseGrade = businessService.getCourseGrade(studentUuid);

        boolean isOverride = false;
        String grade = getGrade(studentCourseGrade, page);
        String points = "0";

        if (studentCourseGrade != null) {
            if (studentCourseGrade.getPointsEarned() != null) {
                points = FormatHelper.formatDoubleToDecimal(studentCourseGrade.getPointsEarned());
            }
            if (studentCourseGrade.getEnteredGrade() != null) {
                isOverride = true;
            }
        }

        String categoryScore = getCategoryScore(categoryId, studentUuid);
        List<Long> droppedItems = getDroppedItems(categoryId, studentUuid);
        target.add(page.updateLiveGradingMessage(page.getString("feedback.saved")));

        return new ExcuseGradeAction.ExcuseGradeResponse(
                grade,
                points,
                isOverride,
                categoryScore,
                droppedItems);
    }

    private String getGrade(CourseGrade studentCourseGrade, GradebookPage page) {

        final GradebookUiSettings uiSettings = page.getUiSettings();
        final Gradebook gradebook = businessService.getGradebook();
        final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
                gradebook,
                page.getCurrentRole(),
                businessService.isCourseGradeVisible(businessService.getCurrentUser().getId()),
                uiSettings.getShowPoints(),
                true);
        if (studentCourseGrade != null)
            return courseGradeFormatter.format(studentCourseGrade);
        else
            return "-";
    }

    private String getCategoryScore(String categoryId, String studentId) {
        if (categoryId != null) {
            final Optional<CategoryScoreData> averageData = businessService.getCategoryScoreForStudent(Long.valueOf(categoryId), studentId, true);
            if (averageData.isPresent()) {
                double average = averageData.get().score;
                return FormatHelper.formatDoubleToDecimal(average);
            }
        }
        return "-";
    }

    private List<Long> getDroppedItems(String categoryId, String studentId){
        Optional<CategoryScoreData> catData = categoryId == null ?
                Optional.empty() : businessService.getCategoryScoreForStudent(Long.valueOf(categoryId), studentId, true);
        return catData.map(c -> c.droppedItems).orElse(Collections.emptyList());
    }
}
