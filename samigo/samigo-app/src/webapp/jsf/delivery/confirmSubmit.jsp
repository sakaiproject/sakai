<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:corejsf="http://java.sun.com/upload">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <!--
  this form is made from deliverAssessment. Take it from the top down 
  stopping right before

div class="tier1"
  

then take all the submit for grading buttons, with the <p class="act"> before them, and all the way to the end

Changes to deliverAssessment: For all the submitforgrading buttons,
change the action to "confirmsubmit", 
remove the javascript onclick stuff.

 -->
  <!--
* $Id: deliverAssessment.jsp 626 2008-01-11 18:18:08Z jayshao $

***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <jsp:text> </jsp:text>
          <h:outputText value="#{delivery.assessmentTitle}"/>
        </title>
        <jsp:directive.include file="/jsf/delivery/deliveryjQuery.jsp"/>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression>; </jsp:attribute>
        <jsp:body>
          <h:outputText value="&lt;a name='top'&gt;&lt;/a&gt;" escape="false"/>
          <div class="portletBody">
            <h:outputText value="&lt;div style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'&gt;" escape="false"/>
            <!-- content...  -->
            <h:form id="takeAssessmentForm" enctype="multipart/form-data" onsubmit="saveTime()">
              <!-- DONE BUTTON FOR PREVIEW  -->
              <h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
                <div class="sak-banner-info mb-5">
                  <h:outputText value="#{deliveryMessages.ass_preview}" escape="false"/>
                  <br/>
                  <h:commandButton value="#{deliveryMessages.exit_preview}" styleClass="exit-preview-button" action="#{person.cleanResourceIdListInPreview}" type="submit" onclick="return returnToHostUrl(&quot;#{delivery.selectURL}&quot;);"/>
                </div>
              </h:panelGroup>
              <!-- JAVASCRIPT  -->
              <jsp:directive.include file="/js/delivery.js"/>
              <script>
                <jsp:text><![CDATA[

function saveTime()
{
  if((typeof (document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=undefined) && ((document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=null) ){
  pauseTiming = 'false';
  document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=]]></jsp:text>
                <jsp:text>${delivery.timeElapse}</jsp:text>
                <jsp:text>;
 }
}

</jsp:text>
              </script>
              <h:inputHidden id="partIndex" value="#{delivery.partIndex}"/>
              <h:inputHidden id="questionIndex" value="#{delivery.questionIndex}"/>
              <h:inputHidden id="formatByPart" value="#{delivery.settings.formatByPart}"/>
              <h:inputHidden id="formatByAssessment" value="#{delivery.settings.formatByAssessment}"/>
              <h:inputHidden id="lastSubmittedDate1" value="#{delivery.assessmentGrading.submittedDate.time}" rendered="#{delivery.assessmentGrading.submittedDate!=null}"/>
              <h:inputHidden id="lastSubmittedDate2" value="0" rendered="#{delivery.assessmentGrading.submittedDate==null}"/>
              <h:inputHidden id="hasTimeLimit" value="#{delivery.hasTimeLimit}"/>
              <h:inputHidden id="showTimeWarning" value="#{delivery.showTimeWarning}"/>
              <!-- HEADING  -->
              <h3 style="insColor insBak">
                <h:outputText value="#{deliveryMessages.submission_warning}" escape="false"/>
              </h3>
              <!-- HEADING  -->
              <f:subview id="assessmentDeliveryHeading">
                <jsp:directive.include file="/jsf/delivery/assessmentDeliveryTimer.jsp"/>
              </f:subview>
              <!-- FORM ... note, move these hiddens to whereever they are needed as fparams -->
              <h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
              <h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}"/>
              <!-- PART/ITEM DATA TABLES  -->
              <h:panelGroup styleClass="sak-banner-warn">
                <h:outputText value="#{deliveryMessages.submit_warning_1} " escape="false"/>
                <h:outputText value="#{deliveryMessages.submit_warning_2} " escape="false"/>
                <h:outputText value="#{deliveryMessages.submit_warning_3_non_linear} " rendered="#{delivery.navigation ne '1'}" escape="false"/>
                <h:outputText value="#{deliveryMessages.submit_warning_3_linear} " rendered="#{delivery.navigation eq '1'}" escape="false"/>
              </h:panelGroup>
              <h:panelGroup layout="block">
                <h:panelGroup layout="block" styleClass="row">
                  <h:panelGroup layout="block" styleClass="col-sm-2">
                    <h:outputLabel value="#{deliveryMessages.course_name}"/>
                  </h:panelGroup>
                  <h:panelGroup layout="block" styleClass="col-sm-10">
                    <h:outputText value="#{delivery.courseName}"/>
                  </h:panelGroup>
                </h:panelGroup>
                <h:panelGroup layout="block" styleClass="row">
                  <h:panelGroup layout="block" styleClass="col-sm-2">
                    <h:outputLabel value="#{deliveryMessages.creator}"/>
                  </h:panelGroup>
                  <h:panelGroup layout="block" styleClass="col-sm-10">
                    <h:outputText value="#{delivery.creatorName}"/>
                  </h:panelGroup>
                </h:panelGroup>
                <h:panelGroup layout="block" styleClass="row">
                  <h:panelGroup layout="block" styleClass="col-sm-2">
                    <h:outputLabel value="#{deliveryMessages.assessment_title}"/>
                  </h:panelGroup>
                  <h:panelGroup layout="block" styleClass="col-sm-10">
                    <h:outputText value="#{delivery.assessmentTitle}" escape="false"/>
                  </h:panelGroup>
                </h:panelGroup>
              </h:panelGroup>
              <jsp:text><![CDATA[

<p class="act">

  ]]></jsp:text>
              <!-- SUBMIT FOR GRADE  -->
              <h:commandButton id="submitForGrade" type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.submitForGrade}" styleClass="active" rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='previewAssessment') &#10;             &amp;&amp; delivery.navigation ne '1' &#10;             &amp;&amp; !delivery.doContinue}" disabled="#{delivery.actionString=='previewAssessment'}"/>
              <!-- SUBMIT FOR GRADE FOR LINEAR ACCESS  -->
              <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.submitForGrade}" id="submitForm" styleClass="active" rendered="#{(delivery.actionString=='takeAssessment'&#10;&#9;&#9;&#9;&#9;   || delivery.actionString=='previewAssessment')&#10;&#9;&#9;&#9;&#9;   &amp;&amp; delivery.navigation eq '1' &amp;&amp; !delivery.doContinue}" disabled="#{delivery.actionString=='previewAssessment'}" onclick="pauseTiming='false'" onkeypress="pauseTiming='false'"/>
              <!-- SUBMIT FOR GRADE DURING PAU  -->
              <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.submitForGrade}" id="submitForm1" styleClass="active" rendered="#{delivery.actionString=='takeAssessmentViaUrl'}" onclick="pauseTiming='false'" onkeypress="pauseTiming='false'"/>
              <!-- Previous button for non-linear assessments  -->
              <h:commandButton id="previous" type="submit" value="#{deliveryMessages.previous}" action="#{delivery.confirmSubmitPrevious}" rendered="#{(delivery.actionString=='previewAssessment'&#10;                 || delivery.actionString=='takeAssessment'&#10;                 || delivery.actionString=='takeAssessmentViaUrl')&#10;              &amp;&amp; delivery.navigation ne '1'}"/>
              <h:commandButton id="save" type="submit" value="#{commonMessages.action_save}" action="#{delivery.saveWork}" style="display:none" rendered="#{delivery.actionString=='previewAssessment'&#10;                  || delivery.actionString=='takeAssessment'&#10;                  || delivery.actionString=='takeAssessmentViaUrl'}"/>
              <!-- Previous button for linear assessments  -->
              <h:commandButton type="submit" value="#{commonMessages.cancel_action}" action="select" id="cancel" rendered="#{(delivery.actionString=='previewAssessment'  &#10;                 || delivery.actionString=='takeAssessment'&#10;&#9;&#9;&#9;&#9; || delivery.actionString=='takeAssessmentViaUrl')&#10;              &amp;&amp; delivery.navigation eq '1'}" onclick="pauseTiming='false'" onkeypress="pauseTiming='false'" disabled="#{delivery.actionString=='previewAssessment'}"/>
            </h:form>
            <!-- end content  -->
          </div>
          <jsp:text><![CDATA[
</div>
    ]]></jsp:text>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
