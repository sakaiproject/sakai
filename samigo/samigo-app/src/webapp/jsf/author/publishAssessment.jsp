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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{assessmentSettingsMessages.publish_assessment_confirmation}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
 <!-- content... -->
 <h:form id="publishAssessmentForm">
   <h:inputHidden id="assessmentId" value="#{assessmentSettings.assessmentId}"/>
   <h3><h:outputText  value="#{assessmentSettingsMessages.publish_assessment_confirmation}" /></h3>
<div class="tier1">

  <!-- Error publishing assessment -->
  <h:messages globalOnly="true" styleClass="validation" />
  <div class="validation">
       <h:outputText value="#{assessmentSettingsMessages.publish_confirm_message}" />
   </div>

<h:panelGrid columns="2" rowClasses="shorttext">

     <h:outputLabel value="#{assessmentSettingsMessages.assessment_title}" rendered="#{assessmentSettings.title ne null}" />
     <h:outputText value="#{assessmentSettings.title}" rendered="#{assessmentSettings.title ne null}" />

     <h:outputLabel value="#{assessmentSettingsMessages.assessment_available_date}" />
     <h:panelGroup>
       <h:outputText rendered="#{assessmentSettings.startDate ne null}" value="#{assessmentSettings.startDate}" >
          <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
       </h:outputText>
       <h:outputText rendered="#{assessmentSettings.startDate eq null}" value="Immediate" />
     </h:panelGroup>

     <h:outputLabel rendered="#{assessmentSettings.dueDate ne null}" 
        value="#{assessmentSettingsMessages.assessment_due_date}" />
     <h:outputText value="#{assessmentSettings.dueDate}" 
        rendered="#{assessmentSettings.dueDate ne null}" >
       <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
     </h:outputText>

     <h:outputLabel rendered="#{assessmentSettings.retractDate ne null}" value="#{assessmentSettingsMessages.assessment_retract_date}" />
     <h:outputText value="#{assessmentSettings.retractDate}" rendered="#{assessmentSettings.retractDate ne null}">
       <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
     </h:outputText>

     <h:outputLabel value="#{assessmentSettingsMessages.time_limit}" />
     <h:panelGroup>
       <h:outputText rendered="#{assessmentSettings.valueMap.hasTimeAssessment eq 'true'}"
          value="#{assessmentSettings.timedHours} hour,
          #{assessmentSettings.timedMinutes} minutes, #{assessmentSettings.timedSeconds} seconds. " />
       <h:outputText rendered="#{assessmentSettings.valueMap.hasTimeAssessment eq 'true'}"
          value="#{assessmentSettingsMessages.auto_submit_description}" />
       <h:outputText rendered="#{assessmentSettings.valueMap.hasTimeAssessment ne 'true'}"
          value="#{assessmentSettingsMessages.no_time_limit}" />
     </h:panelGroup>

<%-- SAK-3578: auto submit will always be ON for timed assessment,
     so no need to have this option
     <h:outputLabel value="#{assessmentSettingsMessages.auto_submit}" />
     <h:panelGroup>
       <h:outputText value="On" rendered="#{assessmentSettings.autoSubmit}" />
       <h:outputText value="Off" rendered="#{!assessmentSettings.autoSubmit}" />
     </h:panelGroup>
--%>

     <h:outputLabel value="#{assessmentSettingsMessages.submissions}" />
     <h:panelGroup>
       <h:outputText value="#{assessmentSettingsMessages.unlimited_submission}" rendered="#{assessmentSettings.unlimitedSubmissions eq '1'}" />
       <h:outputText value="#{assessmentSettings.submissionsAllowed}"
         rendered="#{assessmentSettings.unlimitedSubmissions eq '0'}" />
     </h:panelGroup>


     <h:outputLabel value="#{assessmentSettingsMessages.feedback_type}" />
     <h:panelGroup>
       <h:outputText value="#{assessmentSettingsMessages.immediate}" rendered="#{assessmentSettings.feedbackDelivery eq '1'}" />
       <h:outputText value="#{assessmentSettingsMessages.no_feedback_short}" rendered="#{assessmentSettings.feedbackDelivery eq '3'}" />
       <h:outputText value="#{assessmentSettingsMessages.available_on} #{assessmentSettings.feedbackDate}"
          rendered="#{assessmentSettings.feedbackDelivery eq '2'}" >
         <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
       </h:outputText>
     </h:panelGroup>

     <h:outputLabel value="#{assessmentSettingsMessages.released_to_2}" />
     <h:outputText value="#{assessmentSettings.releaseTo}" />


     <h:outputLabel rendered="#{assessmentSettings.publishedUrl ne null}" value="#{assessmentSettingsMessages.published_assessment_url}" />
     <h:outputText value="#{assessmentSettings.publishedUrl}" />
</h:panelGrid>

<script language="javascript" type="text/JavaScript">
<!--
var clicked = 'false';
function toggle(){
  if (clicked == 'false'){
    clicked = 'true'
  }
  else{ // any subsequent click disable button & action
    document.forms[0].elements['publishAssessmentForm:publish'].disabled=true;
  }
}
//-->
</script>

<f:verbatim><p></p></f:verbatim>
     <h:outputText value="#{assessmentSettingsMessages.open_new_browser_for_publishedUrl}" />

     <p class="act">
       <h:commandButton id="publish" value="#{assessmentSettingsMessages.button_save_and_publish}" type="submit"
         styleClass="active" action="publishAssessment" onclick="toggle()" onkeypress="toggle()">
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.author.PublishAssessmentListener" />
       </h:commandButton>
       <h:commandButton value="#{assessmentSettingsMessages.button_cancel}" type="submit"
         action="author" />
</p>

 </h:form>
 <!-- end content -->
</div>

      </body>
    </html>

  </f:view>
