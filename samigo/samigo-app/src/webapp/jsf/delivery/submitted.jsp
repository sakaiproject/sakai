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
      <title><h:outputText value="#{deliveryMessages.submission}" /></title>
      </head>

<script language="javascript" style="text/JavaScript">
function reviewAssessment(field){
var insertlinkid= field.id.replace("reviewAssessment", "hiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}
</script>

      <body onload="<%= request.getAttribute("html.body.onload") %>">

 <!--h:outputText value="<body #{delivery.settings.bgcolor} #{delivery.settings.background}>" escape="false" /-->
<!--div class="portletBody"-->
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>
 
<!--h:outputText value="<div class='portletBody' style='background:#{delivery.settings.divBgcolor}; background-image: url(http://www.w3.org/WAI/UA/TS/html401/images/test-background.gif)>;'" escape="false"/-->
 <!-- content... -->
<h3><h:outputText value="#{deliveryMessages.submission}" /></h3>
<div class="tier1">
<h4>
  <h:outputText value="#{delivery.assessmentTitle} " escape="false"/>
  <h:outputText value="#{deliveryMessages.submission_info}" />
</h4>

<h:form id="submittedForm">
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

	<h:outputText value="#{deliveryMessages.submission_confirmation_message_1}" rendered="#{!delivery.actionString=='takeAssessmentViaUrl' || !delivery.anonymousLogin}"/>
    <h:outputText value="#{deliveryMessages.submission_confirmation_message_4}" rendered="#{delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin}"/>
    <h:outputText escape="false" value="<br /> #{delivery.submissionMessage}" />

  <f:verbatim><p/></f:verbatim>
  <h:panelGrid columns="2">

    <h:outputLabel value="#{deliveryMessages.course_name}"/>
    <h:outputText value="#{delivery.courseName}" />

    <h:outputLabel  value="#{deliveryMessages.creator}" />
    <h:outputText value="#{delivery.creatorName}"/>

    <h:outputLabel value="#{deliveryMessages.assessment_title}"/>
    <h:outputText value="#{delivery.assessmentTitle}" escape="false"/>

    <h:outputLabel value="#{deliveryMessages.number_of_sub_remain}" />
    <h:panelGroup>
      <h:outputText value="#{delivery.submissionsRemaining} out of #{delivery.settings.maxAttempts}"
          rendered="#{!delivery.settings.unlimitedAttempts}"/>
      <h:outputText value="#{deliveryMessages.unlimited_}"
          rendered="#{delivery.settings.unlimitedAttempts}"/>
    </h:panelGroup>

    <h:outputLabel value="#{deliveryMessages.conf_num}" />
    <h:outputText value="#{delivery.confirmation}" />

    <h:outputLabel value="#{deliveryMessages.submission_dttm}" />
    <h:outputText value="#{delivery.submissionDate}">
        <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
     </h:outputText>

    <h:outputLabel value="#{deliveryMessages.final_page}" rendered="#{delivery.url!=null && delivery.url!=''}"/>
    <h:outputLink title="#{deliveryMessages.t_url}" value="#" rendered="#{delivery.url!=null && delivery.url!=''}"
       onclick="window.open('#{delivery.url}','new_window');" onkeypress="window.open('#{delivery.url}','new_window');">
        <h:outputText value="#{delivery.url}" escape="false"/>
    </h:outputLink>

  </h:panelGrid>
</div>

<br /><br />
<div class="tier1">
  <h:panelGrid columns="2" cellpadding="3" cellspacing="3">
    <h:commandButton type="submit" value="#{deliveryMessages.button_continue}" action="select"
       rendered="#{delivery.actionString=='takeAssessment'}" />

    <h:commandButton value="#{deliveryMessages.button_continue}" type="button" 
       rendered="#{delivery.actionString=='takeAssessmentViaUrl' && !delivery.anonymousLogin}"
       style="act" onclick="javascript:window.open('#{delivery.selectURL}','_top')" onkeypress="javascript:window.open('#{delivery.selectURL}','_top')" />

    <h:commandButton value="#{deliveryMessages.review_results}" type="button" id="reviewAssessment"
       rendered="#{delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin}" 
       style="act" onclick="reviewAssessment(this);" onkeypress="reviewAssessment(this);" />

    <h:commandLink id="hiddenlink" action="takeAssessment" rendered="#{delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin}">
      <f:param name="publishedId" value="#{delivery.assessmentId}" />
      <f:param name="nofeedback" value="false"/>
      <f:param name="actionString" value="reviewAssessment"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener"/>
    </h:commandLink>
  </h:panelGrid>
</div>

</h:form>
  <!-- end content -->
</div>

      </body>
    </html>
  </f:view>

