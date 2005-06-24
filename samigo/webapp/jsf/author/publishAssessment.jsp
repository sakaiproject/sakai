<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
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
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages"
     var="msg"/>
      <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.publish_assessment_confirmation}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->
 <h:form id="publishAssessmentForm">
   <h:inputHidden id="assessmentId" value="#{assessmentSettings.assessmentId}"/>
   <h3><h:outputText  value="#{msg.publish_assessment_confirmation}" /></h3>
<div class="indnt1">

  <!-- Error publishing assessment -->
  <h:messages globalOnly="true" styleClass="validation" />

  <div class="validation">
       <h:outputText value="#{msg.publish_confirm_message}" />
   </div>
   <div class="shorttext">
     <h:outputLabel value="#{msg.assessment_title}" rendered="#{assessmentSettings.title ne null}" />
     <h:outputText value="#{assessmentSettings.title}" />
</div>
   <div class="shorttext">
     <h:outputLabel value="#{msg.assessment_available_date}" />
       <h:outputText rendered="#{assessmentSettings.startDate ne null}" value="#{assessmentSettings.startDate}" >
          <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>

       </h:outputText>
       <h:outputText rendered="#{assessmentSettings.startDate eq null}" value="Immediate" />
    </div>
   <div class="shorttext">
     <h:outputLabel rendered="#{assessmentSettings.dueDate ne null}" value="#{msg.assessment_due_date}" />
     <h:outputText value="#{assessmentSettings.dueDate}" >
       <f:convertDateTime pattern="#{genMsg.output_date_picker}" />
     </h:outputText>
</div>
   <div class="shorttext">
     <h:outputLabel rendered="#{assessmentSettings.retractDate ne null}" value="#{msg.assessment_retract_date}" />
     <h:outputText value="#{assessmentSettings.retractDate}" >
       <f:convertDateTime pattern="#{genMsg.output_date_picker}" />
     </h:outputText>
</div>
   <div class="shorttext">
     <h:outputLabel value="#{msg.time_limit}" />
     <h:outputText rendered="#{assessmentSettings.valueMap.hasTimeAssessment eq 'true'}"
        value="#{assessmentSettings.timedHours} hour,
        #{assessmentSettings.timedMinutes} minutes, #{assessmentSettings.timedSeconds} seconds" />
     <h:outputText rendered="#{assessmentSettings.valueMap.hasTimeAssessment ne 'true'}"
        value="No Time Limit" />
</div>
   <div class="shorttext">
     <h:outputLabel value="#{msg.auto_submit}" />

       <h:outputText value="On" rendered="#{assessmentSettings.autoSubmit}" />
       <h:outputText value="Off" rendered="#{!assessmentSettings.autoSubmit}" />
    </div>
   <div class="shorttext">

     <h:outputLabel value="#{msg.submissions}" />

       <h:outputText value="Unlimited" rendered="#{assessmentSettings.unlimitedSubmissions eq '1'}" />
       <h:outputText value="#{assessmentSettings.submissionsAllowed}"
         rendered="#{assessmentSettings.unlimitedSubmissions eq '0'}" />
     </div>
   <div class="shorttext">

     <h:outputLabel value="#{msg.feedback_type}" />

       <h:outputText value="Immediate" rendered="#{assessmentSettings.feedbackDelivery eq '1'}" />
       <h:outputText value="No Feedback" rendered="#{assessmentSettings.feedbackDelivery eq '3'}" />
       <h:outputText value="Available on #{assessmentSettings.feedbackDate}"
          rendered="#{assessmentSettings.feedbackDelivery eq '2'}" >
         <f:convertDateTime pattern="#{genMsg.output_date_picker}" />
       </h:outputText>
    </div>
   <div class="shorttext">

     <h:outputLabel rendered="#{assessmentSettings.publishedUrl ne null}" value="#{msg.published_assessment_url}" />
     <h:outputText value="#{assessmentSettings.publishedUrl}" />

</div>
   <div class="shorttext">
     <h:outputLabel value="#{msg.released_to_2}" />
     <h:outputText value="#{assessmentSettings.releaseTo}" />
 </div>
</div>
     <p class="act">
       <h:commandButton value="#{msg.button_save_and_publish}" type="submit"
         styleClass="active" action="publishAssessment" >
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.author.PublishAssessmentListener" />
       </h:commandButton>
       <h:commandButton value="#{msg.button_cancel}" type="submit"
         action="author" />
</p>

 </h:form>
 <!-- end content -->
      </body>
    </html>
  </f:view>
