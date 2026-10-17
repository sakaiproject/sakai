<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <div class="d-flex gap-1">
    <h:commandLink title="#{evaluationMessages.submission_nav_previous_submission}" styleClass="button_color#{submissionNav.previousGradingId == null ? ' disabled' : ''}" disabled="#{submissionNav.previousGradingId == null}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener"/>
      <f:param name="gradingId" value="#{submissionNav.previousGradingId}"/>
      <jsp:element name="span">
        <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
        <jsp:attribute name="class" trim="false">fa fa-chevron-circle-left</jsp:attribute>
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </h:commandLink>
    <h:selectOneMenu id="otherStudents" value="#{submissionNav.currentGradingId}" onchange="submit()">
      <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener"/>
      <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionNavListener"/>
      <f:selectItems value="#{submissionNav.submissionsSelection}"/>
    </h:selectOneMenu>
    <h:commandLink title="#{evaluationMessages.submission_nav_next_submission}" styleClass="button_color#{submissionNav.nextGradingId == null ? ' disabled' : ''}" disabled="#{submissionNav.nextGradingId == null}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener"/>
      <f:param name="gradingId" value="#{submissionNav.nextGradingId}"/>
      <jsp:element name="span">
        <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
        <jsp:attribute name="class" trim="false">fa fa-chevron-circle-right</jsp:attribute>
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </h:commandLink>
  </div>
</jsp:root>
