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
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <f:view>
   
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.DeliveryMessages"
     var="msg"/>
     <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title>
      <h:outputText value="#{delivery.assessmentTitle}"/>

      </title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!--h:outputText value="<body #{delivery.settings.bgcolor} #{delivery.settings.background}>" escape="false" /-->
<!--div class="portletBody"-->
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>
 <!-- content... -->

<h:form id="takeAssessmentForm">

<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton accesskey="#{msg.a_done}" value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h3><h:outputText value="#{msg.begin_assessment_}" /></h3>
<div class="tier1">
 <h4> <h:outputText value="#{delivery.assessmentTitle} #{msg.info} " /></h4>
<div class="tier2">
<h:messages styleClass="validation"/>
  <h:outputText value="#{delivery.instructorMessage}" escape="false"/>
<h:panelGrid columns="2" cellpadding="2">
    <h:outputLabel value="#{msg.course}" rendered="#{delivery.courseName ne ''}" />
    <h:outputText value="#{delivery.courseName} " />

    <h:outputLabel value="#{msg.creator}" rendered="#{delivery.creatorName ne ''}"/>
    <h:outputText value="#{delivery.creatorName}" />


    <h:outputLabel value="#{msg.assessment_title}" rendered="#{delivery.assessmentTitle ne ''}" />
    <h:outputText value="#{delivery.assessmentTitle}" />

    <h:outputLabel value="#{msg.time_limit}"/>
    <h:panelGroup rendered="#{delivery.hasTimeLimit}">
       <h:outputText value="#{delivery.timeLimit_hour} " />
       <h:outputText value="#{msg.time_limit_hour} " />
       <h:outputText value="#{delivery.timeLimit_minute} " />
       <h:outputText value="#{msg.time_limit_minute}" />
    </h:panelGroup>
    <h:panelGroup rendered="#{!delivery.hasTimeLimit}">
       <h:outputText value="No Time Limit" />
    </h:panelGroup>


    <h:outputLabel value="#{msg.num_subs}" />
    <h:outputText value="#{delivery.settings.maxAttempts} (#{delivery.submissionsRemaining} #{msg.remaining})"
       rendered="#{!delivery.settings.unlimitedAttempts}"/>
    <h:outputText value="#{msg.unlimited_}"
       rendered="#{delivery.settings.unlimitedAttempts}"/>

    <h:outputLabel value="#{msg.auto_exp}" />
    <h:outputText value="#{msg.enabled_}"
       rendered="#{delivery.settings.autoSubmit}"/>
    <h:outputText value="#{msg.disabled}"
       rendered="#{!delivery.settings.autoSubmit}"/>

    <h:outputLabel value="#{msg.feedback}" />
    <h:panelGroup>
      <h:outputText value="#{msg.immed}"
         rendered="#{delivery.feedbackComponent.showImmediate}"/>
      <h:outputText value="#{delivery.settings.feedbackDate}"
         rendered="#{delivery.feedbackComponent.showDateFeedback}">
        <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
      </h:outputText>
      <h:outputText value="#{msg.none}"
         rendered="#{delivery.feedbackComponent.showNoFeedback}"/>
    </h:panelGroup>

    <h:panelGroup>
      <h:outputLabel rendered="#{delivery.dueDate!=null}" value="#{msg.due_date}" />
    </h:panelGroup>
    <h:panelGroup>
      <h:outputText value="#{delivery.dueDate}" >
        <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
      </h:outputText>
    </h:panelGroup>

    <h:outputLabel value="#{msg.username}"
      rendered="#{delivery.settings.username ne ''}" />
    <h:inputText value="#{delivery.username}" size="20"
      rendered="#{delivery.settings.username ne ''}" />

    <h:outputLabel value="#{msg.password}"
      rendered="#{delivery.settings.username ne ''}" />
    <h:inputSecret value="#{delivery.password}" size="20"
      rendered="#{delivery.settings.username ne ''}" />
</h:panelGrid>
 </div></div>

<p class="act">

<!-- BEGIN ASSESSMENT BUTTON -->
<!-- When previewing, we don't need to check security. When take the assessment for real, we do -->
 <h:commandButton accesskey="#{msg.a_next}" value="#{msg.begin_assessment_}" 
    action="#{delivery.validate}" type="submit" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl')
			 && delivery.navigation != 1}">
<%--    <f:param name="beginAssessment" value="true"/> --%>
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

 <h:commandButton accesskey="#{msg.a_next}" value="#{msg.begin_assessment_}" 
    action="#{delivery.validate}" type="submit" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl')
			 && delivery.navigation == 1}">
<%--    <f:param name="beginAssessment" value="true"/> --%>
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener" />
  </h:commandButton>

 <h:commandButton accesskey="#{msg.a_next}" value="#{msg.begin_assessment_}" action="#{delivery.pvalidate}" type="submit" styleClass="active" rendered="#{delivery.actionString=='previewAssessment'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>


<!-- CANCEL BUTTON -->
  <h:commandButton value="#{msg.button_cancel}"  action="select" type="submit"
     rendered="#{delivery.actionString=='previewAssessment'
             || delivery.actionString=='takeAssessment'}"
     disabled="#{delivery.actionString=='previewAssessment'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
  </h:commandButton>

  <h:commandButton accesskey="#{msg.a_cancel}" value="#{msg.button_cancel}" type="button"
     style="act" onclick="javascript:window.open('/portal/login','_top')"
onkeypress="javascript:window.open('/portal/login','_top')"
     rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"
     disabled="#{delivery.actionString=='previewAssessment'}"/>
</p>

<!-- DONE BUTTON, FOR PREVIEW ONLY --> 
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton accesskey="#{msg.a_done}" value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>

