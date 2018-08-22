<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
	  <h:outputText  value="#{delivery.secureDeliveryHTMLFragment}" escape="false" />

 <h:outputText value="<div class='confirmationBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>
 
<h:outputText styleClass="messageSamigo3" value="#{deliveryMessages.timeOutSubmission}" rendered="#{delivery.timeOutSubmission=='true'}"/>
<div>

<h:form id="submittedForm">
<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

	<h:outputText value="#{deliveryMessages.submission_confirmation_message_1}" rendered="#{!delivery.actionString=='takeAssessmentViaUrl'}"/>
    <h:outputText value="#{deliveryMessages.submission_confirmation_message_4}" rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"/>

  <f:verbatim><p/></f:verbatim>
  <h:panelGrid columns="2" rowClasses="timerSubmission" columnClasses="timerSubmissionInfoCol1, timerSubmissionInfoCol2">

    <h:outputLabel value="#{deliveryMessages.assessment_title}"/>
    <h:outputText value="#{delivery.assessmentTitle}" escape="false"/>

    <h:outputLabel value="#{deliveryMessages.submission_dttm}" />
    <h:outputText value="#{delivery.submissionDate}">
        <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
     </h:outputText>

    <h:outputLabel value="#{deliveryMessages.course_name}"/>
    <h:outputText value="#{delivery.courseName}" />

    <h:outputLabel  value="#{deliveryMessages.creator}" />
    <h:outputText value="#{delivery.creatorName}"/>

    <h:outputLabel value="#{deliveryMessages.number_of_submission_short}" />
    <h:panelGroup>
	<h:outputText value="#{delivery.submissionsRemaining} #{deliveryMessages.text_out_of} #{delivery.settings.maxAttempts}"
          rendered="#{!delivery.settings.unlimitedAttempts}"/>
      <h:outputText value="#{deliveryMessages.unlimited_}"
          rendered="#{delivery.settings.unlimitedAttempts}"/>
    </h:panelGroup>

    <h:outputLabel value="#{deliveryMessages.conf_num}" />
    <h:outputText value="#{delivery.confirmation}" />


    <h:outputLabel value="#{deliveryMessages.final_page}" rendered="#{delivery.url!=null && delivery.url!=''}"/>
    
    <h:outputLabel value="<b>#{deliveryMessages.anonymousScore}</b>" rendered="#{delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin && (delivery.feedbackComponent.showImmediate || delivery.feedbackComponent.showOnSubmission || delivery.feedbackOnDate) && delivery.feedbackComponentOption=='1'}"/>
    <h:outputText value="<b>#{delivery.roundedRawScoreViaURL}</b>" rendered="#{delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin && (delivery.feedbackComponent.showImmediate || delivery.feedbackComponent.showOnSubmission || delivery.feedbackOnDate) && delivery.feedbackComponentOption=='1'}" escape="false"/>
  </h:panelGrid>

  <p><h:outputText value="#{delivery.receiptEmailSetting}" escape="false" /></p>
<div class="tier1">
</div>
</h:form>
</div>
  </f:view>
