<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title>
      <h:outputText value="#{delivery.assessmentTitle}"/>

      </title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>; isMacintosh_Netscape();">
 <!--h:outputText value="<body #{delivery.settings.bgcolor} #{delivery.settings.background}>" escape="false" /-->
<!--div class="portletBody"-->
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

<!--JAVASCRIPT -->
<script language="javascript" type="text/JavaScript">
<%@ include file="/js/browser.js" %>
</script>

 <!-- content... -->
<h:form id="takeAssessmentForm">
<h:inputHidden id="isMacNetscapeBrowser" value="#{person.isMacNetscapeBrowser}" />
<h:inputHidden id="timerId" value="#{delivery.timerId}" rendered="#{delivery.timerId!=null}" />

<!-- DONE BUTTON FOR PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton accesskey="#{deliveryMessages.a_done}" value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h3><h:outputText value="#{deliveryMessages.begin_assessment_}" /></h3>
<div class="tier1">
 <h4> <h:outputText value="#{delivery.assessmentTitle} #{deliveryMessages.info} " escape="false"/></h4>
<div class="tier2">
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
  <h:outputText value="#{delivery.instructorMessage}" escape="false"/>

  <p></p> 
  <!-- ASSESSMENT ATTACHMENTS -->
  <%@ include file="/jsf/delivery/assessment_attachment.jsp" %>

<h:panelGrid columns="2" cellpadding="2">
    <h:outputLabel value="#{deliveryMessages.course}" rendered="#{delivery.courseName ne ''}" />
    <h:outputText value="#{delivery.courseName} " />

    <h:outputLabel value="#{deliveryMessages.creator}" rendered="#{delivery.creatorName ne ''}"/>
    <h:outputText value="#{delivery.creatorName}" />


    <h:outputLabel value="#{deliveryMessages.assessment_title}" rendered="#{delivery.assessmentTitle ne ''}"/>
    <h:outputText value="#{delivery.assessmentTitle}" escape="false"/>

    <h:outputLabel value="#{deliveryMessages.time_limit}"/>
    <h:panelGroup rendered="#{delivery.hasTimeLimit}">
       <h:outputText value="#{delivery.timeLimit_hour} " />
       <h:outputText value="#{deliveryMessages.time_limit_hour} " />
       <h:outputText value="#{delivery.timeLimit_minute} " />
       <h:outputText value="#{deliveryMessages.time_limit_minute}" />
    </h:panelGroup>
    <h:panelGroup rendered="#{!delivery.hasTimeLimit}">
       <h:outputText value="#{deliveryMessages.no_time_limit}" />
    </h:panelGroup>

    <h:outputLabel value="#{deliveryMessages.num_subs}" rendered="#{!delivery.anonymousLogin}"/>
    <h:outputText value="#{delivery.settings.maxAttempts} (#{delivery.submissionsRemaining} #{deliveryMessages.remaining})"
       rendered="#{!delivery.settings.unlimitedAttempts && !delivery.anonymousLogin}"/>
    <h:outputText value="#{deliveryMessages.unlimited_}"
       rendered="#{delivery.settings.unlimitedAttempts && !delivery.anonymousLogin}"/>

    <h:outputLabel value="#{deliveryMessages.feedback}" />
    <h:panelGroup>
      <h:outputText value="#{deliveryMessages.immed}"
         rendered="#{delivery.feedbackComponent.showImmediate}"/>
      <h:outputText value="#{delivery.settings.feedbackDate}"
         rendered="#{delivery.feedbackComponent.showDateFeedback}">
        <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
      </h:outputText>
      <h:outputText value="#{deliveryMessages.none}"
         rendered="#{delivery.feedbackComponent.showNoFeedback}"/>
    </h:panelGroup>

    <h:panelGroup>
      <h:outputLabel rendered="#{delivery.dueDate!=null}" value="#{deliveryMessages.due_date}" />
    </h:panelGroup>
    <h:panelGroup>
      <h:outputText value="#{delivery.dueDateString}" >
      </h:outputText>
    </h:panelGroup>

    <h:outputLabel value="#{deliveryMessages.username}"
      rendered="#{delivery.settings.username ne ''}" />
    <h:inputText value="#{delivery.username}" size="20"
      rendered="#{delivery.settings.username ne ''}" />

    <h:outputLabel value="#{deliveryMessages.password}"
      rendered="#{delivery.settings.username ne ''}" />
    <h:inputSecret value="#{delivery.password}" size="20"
      rendered="#{delivery.settings.username ne ''}" />
</h:panelGrid>
 </div></div>

<p class="act">

<!-- BEGIN ASSESSMENT BUTTON -->
<!-- When previewing, we don't need to check security. When take the assessment for real, we do -->
 <h:commandButton id="beginAssessment1" accesskey="#{deliveryMessages.a_next}" value="#{deliveryMessages.begin_assessment_}" 
    action="#{delivery.validate}" type="submit" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl')
			 && delivery.navigation != 1}">
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

 <h:commandButton id="beginAssessment2" accesskey="#{deliveryMessages.a_next}" value="#{deliveryMessages.begin_assessment_}" 
    action="#{delivery.validate}" type="submit" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl')
			 && delivery.navigation == 1}">
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener" />
  </h:commandButton>

 <h:commandButton id="beginAssessment3" accesskey="#{deliveryMessages.a_next}" value="#{deliveryMessages.begin_assessment_}" action="#{delivery.pvalidate}" type="submit" styleClass="active" rendered="#{delivery.actionString=='previewAssessment'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>


<!-- CANCEL BUTTON -->
  <h:commandButton value="#{deliveryMessages.button_cancel}"  action="select" type="submit"
     rendered="#{delivery.actionString=='previewAssessment'
             || delivery.actionString=='takeAssessment'}"
     disabled="#{delivery.actionString=='previewAssessment'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
  </h:commandButton>

  <h:commandButton accesskey="#{deliveryMessages.a_cancel}" value="#{deliveryMessages.button_cancel}" type="button"
     style="act" onclick="javascript:window.open('#{delivery.portal}/login','_top')"
onkeypress="javascript:window.open('#{delivery.portal}/login','_top')"
     rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"
     disabled="#{delivery.actionString=='previewAssessment'}"/>
</p>

<!-- DONE BUTTON, FOR PREVIEW ONLY --> 
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton accesskey="#{deliveryMessages.a_done}" value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>

