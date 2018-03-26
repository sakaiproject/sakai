/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			 http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.actions;

import java.io.Serializable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.tool.gradebook.Gradebook;

public class SetGradeAsExcludedAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public SetGradeAsExcludedAction() {}

	private class GradeUpdateResponse implements ActionResponse {

	private String courseGrade;
	private boolean isExcluded;
	private String points;
	private boolean isOverride;
	private String categoryScore;
	private double categoryGrade;
	private String assignmentScore;

	public GradeUpdateResponse(final boolean isExcluded, final String courseGrade, final double categoryGrade, final String points, final boolean isOverride, final String categoryScore, final String assignmentScore) {
		this.courseGrade = courseGrade;
		this.isExcluded = isExcluded;
		this.points = points;
		this.isOverride = isOverride;
		this.categoryGrade = categoryGrade;
		this.categoryScore = categoryScore;
		this.assignmentScore = assignmentScore;
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
		result.put("isExcluded", isExcluded);
		result.put("categoryGrade", categoryGrade);
		result.put("categoryScore", categoryScore);
		result.put("points", points);
		result.put("assignmentScore", assignmentScore);

		return result.toString();
	}
}

private class SaveGradeErrorResponse implements ActionResponse {
	private GradeSaveResponse serverResponse;

	public String getStatus() {
		return "error";
	}

	public String toJson() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		result.put("msg", serverResponse.toString());

		return result.toString();
	}
}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final GradebookPage page = (GradebookPage) target.getPage();

		// clear the feedback message at the top of the page
		target.addChildren(page, FeedbackPanel.class);

		final String assignmentId = params.get("assignmentId").asText();
		final String studentUuid = params.get("studentId").asText();
		final Long categoryId = params.get("categoryId").asLong();

		String assignmentScore;

		GradeDefinition studentGradeDef = businessService.getGradeForStudent(params.get("assignmentId").asLong(), studentUuid);
		if(studentGradeDef != null) {
			assignmentScore = studentGradeDef.getGrade();
			System.out.println(studentGradeDef.isGradeExcluded());
		} else {
			assignmentScore = "-";
		}

		final boolean isExcluded = studentGradeDef.isGradeExcluded();
		businessService.setAssignmentExcludedStatus(params.get("assignmentId").asLong(), studentUuid, isExcluded);

		double categoryGrade;

		if(businessService.getCategoryScoreForStudent(categoryId, studentUuid) == null){
			categoryGrade = -1;
		}else {
			categoryGrade = businessService.getCategoryScoreForStudent(categoryId, studentUuid);
		}
		String categoryScore = "-";

		if (categoryId != null) {
			final Double average = businessService.getCategoryScoreForStudent(Long.valueOf(categoryId), studentUuid);
			if (average != null) {
				categoryScore = FormatHelper.formatDoubleToDecimal(average);
			}
		}

		final CourseGrade studentCourseGrade = businessService.getCourseGrade(studentUuid);
		boolean isOverride = false;
		String grade = "-";
		String points = "0";
		final Gradebook gradebook = businessService.getGradebook();
		if (studentCourseGrade != null) {
			final GradebookUiSettings uiSettings = page.getUiSettings();

			final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
				gradebook,
				page.getCurrentRole(),
				businessService.isCourseGradeVisible(businessService.getCurrentUser().getId()),
				uiSettings.getShowPoints(),
				true
			);

			grade = courseGradeFormatter.format(studentCourseGrade);
			if (studentCourseGrade.getPointsEarned() != null) {
				points = FormatHelper.formatDoubleToDecimal(studentCourseGrade.getPointsEarned());
			}
			if (studentCourseGrade.getEnteredGrade() != null) {
				isOverride = true;
			}
		}

		target.add(page.updateLiveGradingMessage(page.getString("feedback.saved")));

		return new GradeUpdateResponse(
			isExcluded,
			grade,
			categoryGrade,
			points,
			isOverride,
			categoryScore,
			assignmentScore
		);
	}
}
