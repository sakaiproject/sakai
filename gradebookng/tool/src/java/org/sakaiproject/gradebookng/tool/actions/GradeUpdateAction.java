/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.service.gradebook.shared.CategoryScoreData;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;

public class GradeUpdateAction extends InjectableAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public GradeUpdateAction() {
	}

	private class GradeUpdateResponse implements ActionResponse {
		private String courseGrade;
		private String points;
		private String categoryScore;
		private List<Long> droppedItems;
		private boolean isOverride;
		private boolean extraCredit;

		public GradeUpdateResponse(final boolean extraCredit, final String courseGrade, final String points, final boolean isOverride,
				final String categoryScore, List<Long> droppedItems) {
			this.courseGrade = courseGrade;
			this.categoryScore = categoryScore;
			this.droppedItems = droppedItems;
			this.points = points;
			this.isOverride = isOverride;
			this.extraCredit = extraCredit;
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
			result.put("extraCredit", extraCredit);

			ArrayNode catDroppedItemsArray = mapper.createArrayNode();
			droppedItems.stream().forEach(i -> catDroppedItemsArray.add(i));
			result.put("categoryDroppedItems", catDroppedItemsArray);

			return result.toString();
		}
	}

	private class SaveGradeErrorResponse implements ActionResponse {
		private GradeSaveResponse serverResponse;

		public SaveGradeErrorResponse(GradeSaveResponse serverResponse) {
			this.serverResponse = serverResponse;
		}

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

	private class SaveGradeNoChangeResponse extends EmptyOkResponse {
		public SaveGradeNoChangeResponse() {
		}

		@Override
		public String getStatus() {
			return "nochange";
		}
	}

	@Override
	public ActionResponse handleEvent(final JsonNode params, final AjaxRequestTarget target) {
		final GradebookPage page = (GradebookPage) target.getPage();

		// clear the feedback message at the top of the page
		target.addChildren(page, FeedbackPanel.class);

		final String rawOldGrade = params.get("oldScore").textValue();
		final String rawNewGrade = params.get("newScore").textValue();

		if (StringUtils.isNotBlank(rawNewGrade)
				&& (!FormatHelper.isValidDouble(rawNewGrade) || FormatHelper.validateDouble(rawNewGrade) < 0)) {
			target.add(page.updateLiveGradingMessage(page.getString("feedback.error")));

			return new ArgumentErrorResponse("Grade not valid");
		}

		final String oldGrade = FormatHelper.formatGradeFromUserLocale(rawOldGrade);
		final String newGrade = FormatHelper.formatGradeFromUserLocale(rawNewGrade);

		final String assignmentId = params.get("assignmentId").asText();
		final String studentUuid = params.get("studentId").asText();
		final String categoryId = params.has("categoryId") ? params.get("categoryId").asText() : null;

		// We don't pass the comment from the use interface,
		// but the service needs it otherwise it will assume 'null'
		// so pull it back from the service and poke it in there!
		final String comment = businessService.getAssignmentGradeComment(Long.valueOf(assignmentId), studentUuid);

		// for concurrency, get the original grade we have in the UI and pass it into the service as a check
		final GradeSaveResponse result = businessService.saveGrade(Long.valueOf(assignmentId),
				studentUuid,
				oldGrade,
				newGrade,
				comment);

		if (result.equals(GradeSaveResponse.NO_CHANGE)) {
			target.add(page.updateLiveGradingMessage(page.getString("feedback.saved")));

			return new SaveGradeNoChangeResponse();
		}

		if (!result.equals(GradeSaveResponse.OK) && !result.equals(GradeSaveResponse.OVER_LIMIT)) {
			target.add(page.updateLiveGradingMessage(page.getString("feedback.error")));

			return new SaveGradeErrorResponse(result);
		}

		final CourseGrade studentCourseGrade = businessService.getCourseGrade(studentUuid);

		boolean isOverride = false;
		String grade = "-";
		String points = "0";

		if (studentCourseGrade != null) {
			final GradebookUiSettings uiSettings = page.getUiSettings();
			final Gradebook gradebook = businessService.getGradebook();
			final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
					gradebook,
					page.getCurrentRole(),
					businessService.isCourseGradeVisible(businessService.getCurrentUser().getId()),
					uiSettings.getShowPoints(),
					true);

			grade = courseGradeFormatter.format(studentCourseGrade);
			if (studentCourseGrade.getPointsEarned() != null) {
				points = FormatHelper.formatDoubleToDecimal(studentCourseGrade.getPointsEarned());
			}
			if (studentCourseGrade.getEnteredGrade() != null) {
				isOverride = true;
			}
		}

		Optional<CategoryScoreData> catData = categoryId == null ?
				Optional.empty() : businessService.getCategoryScoreForStudent(Long.valueOf(categoryId), studentUuid);
		String categoryScore = catData.map(c -> String.valueOf(c.score)).orElse("-");
		List<Long> droppedItems = catData.map(c -> c.droppedItems).orElse(Collections.emptyList());

		target.add(page.updateLiveGradingMessage(page.getString("feedback.saved")));

		return new GradeUpdateResponse(
				result.equals(GradeSaveResponse.OVER_LIMIT),
				grade,
				points,
				isOverride,
				categoryScore,
				droppedItems);
	}
}
