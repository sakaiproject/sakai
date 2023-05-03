<div class="b5 d-flex gap-1">
    <h:commandLink title="#{evaluationMessages.submission_nav_previous_submission}"
            styleClass="button b5 me-0#{submissionNav.previousGradingId == null ? ' disabled' : ''}"
            disabled="#{submissionNav.previousGradingId == null}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
        <f:param name="gradingId" value="#{submissionNav.previousGradingId}" />
        <span aria-hidden="true" class="fa fa-chevron-circle-left"></span>
    </h:commandLink>
    <h:selectOneMenu id="otherStudents" value="#{submissionNav.currentGradingId}" onchange="submit()">
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionNavListener" />
        <f:selectItems value="#{submissionNav.submissionsSelection}" />
    </h:selectOneMenu>
    <h:commandLink title="#{evaluationMessages.submission_nav_next_submission}"
            styleClass="button#{submissionNav.nextGradingId == null ? ' disabled' : ''}"
            disabled="#{submissionNav.nextGradingId == null}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
        <f:param name="gradingId" value="#{submissionNav.nextGradingId}" />
        <span aria-hidden="true" class="fa fa-chevron-circle-right"></span>
    </h:commandLink>
</div>
