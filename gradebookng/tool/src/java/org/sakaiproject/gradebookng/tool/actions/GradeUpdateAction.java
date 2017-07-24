package org.sakaiproject.gradebookng.tool.actions;


import java.io.Serializable;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.util.CourseGradeFormatter;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
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
        private boolean extraCredit;

        public GradeUpdateResponse(boolean extraCredit, String courseGrade, String points, String categoryScore) {
            this.courseGrade = courseGrade;
            this.categoryScore = categoryScore;
            this.points = points;
            this.extraCredit = extraCredit;
        }

        public String getStatus() {
            return "OK";
        }

        public String toJson() {
            return "{" +
                        "\"courseGrade\": [\"" + courseGrade + "\"," + "\"" + points + "\"]," +
                        "\"categoryScore\": \"" + categoryScore + "\"," +
                        "\"extraCredit\": " + extraCredit +
                    "}";
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
            // TODO map to a reasonable message based on server response
            return String.format("{\"msg\": \"%s\"}", serverResponse.toString());
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

        if (StringUtils.isNotBlank(rawNewGrade) && (!FormatHelper.isValidDouble(rawNewGrade) || FormatHelper.validateDouble(rawNewGrade) < 0)) {
            target.add(page.updateLiveGradingMessage(page.getString("feedback.error")));

            return new ArgumentErrorResponse("Grade not valid");
        }

        final String oldGrade = FormatHelper.formatGrade(rawOldGrade);
        final String newGrade = FormatHelper.formatGrade(rawNewGrade);

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

        String grade = "-";
        String points = "0";

        if (studentCourseGrade != null) {
            final GradebookUiSettings uiSettings = page.getUiSettings();
            final Gradebook gradebook = businessService.getGradebook();
            final CourseGradeFormatter courseGradeFormatter = new CourseGradeFormatter(
                gradebook,
                GbRole.INSTRUCTOR,
                true,
                uiSettings.getShowPoints(),
                true);

            grade = courseGradeFormatter.format(studentCourseGrade);
            if (studentCourseGrade.getPointsEarned() != null) {
                points = FormatHelper.formatDoubleToDecimal(studentCourseGrade.getPointsEarned());
            }
        }

        String categoryScore = "-";

        if (categoryId != null) {
            final Double average = businessService.getCategoryScoreForStudent(Long.valueOf(categoryId), studentUuid);
            if (average != null) {
                categoryScore = FormatHelper.formatDoubleToDecimal(average);
            }
        }

        target.add(page.updateLiveGradingMessage(page.getString("feedback.saved")));

        return new GradeUpdateResponse(
            result.equals(GradeSaveResponse.OVER_LIMIT),
            grade,
            points,
            categoryScore);
    }
}
