<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- REVIEW TABLE  -->
  <div class="table table-sent-assessments">
    <t:dataTable styleClass="table table-hover table-striped table-bordered table-assessments" id="reviewTable" value="#{select.reviewableAssessments}" var="reviewable" summary="#{selectIndexMessages.sum_submittedAssessment}">
      <!-- TITLE  -->
      <t:column rendered="#{select.displayAllAssessments != 3}">
        <f:facet name="header">
          <h:panelGroup>
            <h:outputText value="#{selectIndexMessages.title} " styleClass="currentSort"/>
            <h:panelGroup rendered="#{select.displayAllAssessments != '1'}">
              <h:outputText value="" styleClass="displayAllAssessments hidden"/>
            </h:panelGroup>
          </h:panelGroup>
        </f:facet>
        <h:outputText value="#{reviewable.assessmentTitle}" rendered="#{!reviewable.isRecordedAssessment}" styleClass="d-none"/>
        <h:outputText styleClass="highlight fa fa-fw fa-exclamation-circle" rendered="#{reviewable.isRecordedAssessment &amp;&amp; !reviewable.isAssessmentRetractForEdit &amp;&amp; reviewable.hasAssessmentBeenModified &amp;&amp; select.warnUserOfModification}" title="#{selectIndexMessages.has_been_modified}"/>
        <h:outputText styleClass="highlight fa fa-fw fa-exclamation" rendered="#{reviewable.isRecordedAssessment &amp;&amp; reviewable.isAssessmentRetractForEdit}" title="#{selectIndexMessages.currently_being_edited}"/>
        <h:outputText value="#{reviewable.assessmentTitle}" styleClass="currentSort" rendered="#{reviewable.isRecordedAssessment}" escape="false"/>
      </t:column>
      <!-- STATS creating separate column for stats  -->
      <t:column>
        <f:facet name="header">
          <h:panelGroup>
            <h:outputText value="#{selectIndexMessages.stats}" styleClass="currentSort"/>
          </h:panelGroup>
        </f:facet>
        <h:panelGroup>
          <h:commandLink title="#{selectIndexMessages.t_histogram}" id="histogram" action="#{delivery.getOutcome}" immediate="true" rendered="#{reviewable.feedback eq 'show' &amp;&amp; reviewable.feedbackComponentOption == '2' &amp;&amp; reviewable.statistics &amp;&amp; !reviewable.isAssessmentRetractForEdit &amp;&amp; reviewable.isRecordedAssessment}">
            <f:param name="publishedAssessmentId" value="#{reviewable.assessmentId}"/>
            <f:param name="hasNav" value="false"/>
            <f:param name="allSubmissions" value="true"/>
            <f:param name="actionString" value="reviewAssessment"/>
            <f:param name="isFromStudent" value="true"/>
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
            <h:outputText value="#{selectIndexMessages.stats} "/>
          </h:commandLink>
        </h:panelGroup>
        <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.feedback eq 'na' ||  reviewable.feedbackComponentOption == '1' || reviewable.isAssessmentRetractForEdit || !reviewable.statistics) &amp;&amp; reviewable.isRecordedAssessment}"/>
      </t:column>
      <!-- created separate column for statistics   -->
      <!-- Recorded SCORE  -->
      <t:column>
        <f:facet name="header">
          <h:panelGroup>
            <h:outputText value="#{selectIndexMessages.recorded_score}" styleClass="currentSort"/>
          </h:panelGroup>
        </f:facet>
        <h:outputText value="#{reviewable.roundedRawScoreToDisplay} " styleClass="currentSort" rendered="#{reviewable.showScore eq 'show' &amp;&amp; reviewable.isRecordedAssessment &amp;&amp; !reviewable.isAssessmentRetractForEdit}"/>
        <h:outputText value="" rendered="#{!reviewable.isRecordedAssessment &amp;&amp; reviewable.showScore eq 'show' &amp;&amp; !reviewable.isAssessmentRetractForEdit}"/>
        <h:outputText value="#{selectIndexMessages.highest_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' &amp;&amp; reviewable.isRecordedAssessment &amp;&amp; reviewable.scoringOption eq '1' &amp;&amp; (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) &amp;&amp; !reviewable.isAssessmentRetractForEdit}"/>
        <h:outputText value="#{selectIndexMessages.last_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' &amp;&amp; reviewable.isRecordedAssessment &amp;&amp; reviewable.scoringOption eq '2' &amp;&amp; (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) &amp;&amp; !reviewable.isAssessmentRetractForEdit}"/>
        <h:outputText value="#{selectIndexMessages.average_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' &amp;&amp; reviewable.isRecordedAssessment &amp;&amp; reviewable.scoringOption eq '4' &amp;&amp; (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) &amp;&amp; !reviewable.isAssessmentRetractForEdit}"/>
        <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.showScore eq 'na' || reviewable.isAssessmentRetractForEdit) &amp;&amp; reviewable.isRecordedAssessment}"/>
      </t:column>
      <!-- FEEDBACK DATE  -->
      <t:column>
        <f:facet name="header">
          <h:panelGroup>
            <h:outputText value="#{selectIndexMessages.feedback_date}" styleClass="currentSort"/>
          </h:panelGroup>
        </f:facet>
        <h:outputText value="#{reviewable.feedbackDate}" styleClass="currentSort" rendered="#{reviewable.feedbackComponentOption == '2'  &amp;&amp; reviewable.feedbackDelivery eq '2' &amp;&amp; !reviewable.isAssessmentRetractForEdit &amp;&amp; reviewable.isRecordedAssessment}">
          <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
        </h:outputText>
        <h:outputText value=" - " rendered="#{reviewable.feedbackComponentOption == '2'  &amp;&amp; reviewable.feedbackDelivery eq '2' &amp;&amp; !reviewable.isAssessmentRetractForEdit &amp;&amp; reviewable.isRecordedAssessment &amp;&amp; not empty reviewable.feedbackEndDate}"/>
        <h:outputText value="#{reviewable.feedbackEndDate}" styleClass="currentSort" rendered="#{reviewable.feedbackComponentOption == '2'  &amp;&amp; reviewable.feedbackDelivery eq '2' &amp;&amp; !reviewable.isAssessmentRetractForEdit &amp;&amp; reviewable.isRecordedAssessment &amp;&amp; not empty reviewable.feedbackEndDate}">
          <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
        </h:outputText>
        <h:outputText value="#{selectIndexMessages.immediate}" styleClass="currentSort" rendered="#{reviewable.feedbackComponentOption == '2'  &amp;&amp; (reviewable.feedbackDelivery eq '1' || reviewable.feedbackDelivery eq '4') &amp;&amp; !reviewable.isAssessmentRetractForEdit &amp;&amp; reviewable.isRecordedAssessment}"/>
        <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.feedbackComponentOption == '1' || reviewable.feedbackDelivery==null  || reviewable.feedbackDelivery eq '3' || reviewable.isAssessmentRetractForEdit) &amp;&amp; reviewable.isRecordedAssessment}"/>
        <!-- mustansar  -->
        <h:commandLink title="#{selectIndexMessages.t_reviewAssessment}" action="#{delivery.getOutcome}" immediate="true" rendered="#{reviewable.feedback == 'show' &amp;&amp; reviewable.feedbackComponentOption == '2' &amp;&amp; !reviewable.isAssessmentRetractForEdit &amp;&amp; select.displayAllAssessments != '1' &amp;&amp; !reviewable.isRecordedAssessment }">
          <f:param name="publishedId" value="#{reviewable.assessmentId}"/>
          <f:param name="assessmentGradingId" value="#{reviewable.assessmentGradingId}"/>
          <f:param name="nofeedback" value="false"/>
          <f:param name="actionString" value="reviewAssessment"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener"/>
          <h:outputText styleClass="currentSort" value="#{commonMessages.feedback}" rendered="#{reviewable.isRecordedAssessment &amp;&amp; select.displayAllAssessments != '1' }" escape="false"/>
          <h:outputText value="#{commonMessages.feedback}" rendered="#{!reviewable.isRecordedAssessment }" escape="false"/>
        </h:commandLink>
        <!-- mustansar  -->
      </t:column>
      <!-- SCORE  -->
      <t:column rendered="#{select.displayAllAssessments != '1'}" headerstyleClass="d-none d-sm-table-cell" styleClass="d-none d-sm-table-cell">
        <f:facet name="header">
          <h:panelGroup>
            <h:outputText value="#{selectIndexMessages.individual_score}" styleClass="currentSort"/>
          </h:panelGroup>
        </f:facet>
        <h:outputText value="#{reviewable.roundedRawScoreToDisplay} " rendered="#{(reviewable.showScore eq 'show' &amp;&amp; !reviewable.isAssessmentRetractForEdit) &amp;&amp; !reviewable.isRecordedAssessment}"/>
        <h:outputText value="#{selectIndexMessages.not_applicable}" rendered="#{(reviewable.showScore eq 'na' || reviewable.isAssessmentRetractForEdit) &amp;&amp; !reviewable.isRecordedAssessment}"/>
      </t:column>
      <!-- TIME  -->
      <t:column rendered="#{select.displayAllAssessments != '1'}" headerstyleClass="d-none d-sm-table-cell" styleClass="d-none d-sm-table-cell">
        <f:facet name="header">
          <h:panelGroup>
            <h:outputText value="#{selectIndexMessages.time} " styleClass="currentSort"/>
          </h:panelGroup>
        </f:facet>
        <h:panelGroup>
          <h:outputText id="timeElapse" value="#{reviewable.timeElapse}" styleClass="currentSort" rendered="#{reviewable.isRecordedAssessment}"/>
          <h:outputText value="#{reviewable.timeElapse}" rendered="#{!reviewable.isRecordedAssessment}"/>
        </h:panelGroup>
      </t:column>
      <!-- SUBMITTED  -->
      <t:column rendered="#{select.displayAllAssessments != '1'}" headerstyleClass="d-none d-sm-table-cell" styleClass="d-none d-sm-table-cell">
        <f:facet name="header">
          <h:panelGroup>
            <h:outputText value="#{selectIndexMessages.submitted} " styleClass="currentSort"/>
          </h:panelGroup>
        </f:facet>
        <h:outputText value="#{reviewable.submissionDate}" styleClass="currentSort" rendered="#{reviewable.isRecordedAssessment}">
          <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
        </h:outputText>
        <h:outputText value="#{reviewable.submissionDate}" rendered="#{!reviewable.isRecordedAssessment}">
          <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
        </h:outputText>
      </t:column>
    </t:dataTable>
    <t:div styleClass="sam-asterisks-row" rendered="#{(select.hasAnyAssessmentBeenModified &amp;&amp; select.warnUserOfModification) || select.hasAnyAssessmentRetractForEdit}">
      <h:panelGroup rendered="#{select.hasAnyAssessmentBeenModified &amp;&amp; select.warnUserOfModification}">
        <f:verbatim>
          <jsp:text><![CDATA[<p>]]></jsp:text>
        </f:verbatim>
        <h:outputText styleClass="highlight fa fa-fw fa-exclamation-circle"/>
        <h:outputText value="#{selectIndexMessages.has_been_modified}" styleClass="highlight"/>
        <f:verbatim>
          <jsp:text><![CDATA[</p>]]></jsp:text>
        </f:verbatim>
      </h:panelGroup>
      <h:panelGroup rendered="#{select.hasAnyAssessmentRetractForEdit}">
        <f:verbatim>
          <jsp:text><![CDATA[<p>]]></jsp:text>
        </f:verbatim>
        <h:outputText styleClass="highlight fa fa-fw fa-exclamation" title="#{selectIndexMessages.currently_being_edited}"/>
        <h:outputText value="#{selectIndexMessages.currently_being_edited}" styleClass="highlight"/>
        <f:verbatim>
          <jsp:text><![CDATA[</p>]]></jsp:text>
        </f:verbatim>
      </h:panelGroup>
    </t:div>
  </div>
</jsp:root>
