<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
  this form is made from deliverAssessment. Take it from the top down 
  stopping right before

div class="tier1"
  h:dataTable width="100%" value="#{delivery.pageContents.partsContents}" var="part"

then take all the submit for grading buttons, with the <p class="act"> before them, and all the way to the end

Changes to deliverAssessment: For all the submitforgrading buttons,
change the action to "confirmsubmit", 
remove the javascript onclick stuff.

-->

<!--
* $Id: deliverAssessment.jsp 626 2008-01-11 18:18:08Z jayshao $
<%--
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
--%>
-->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title> <h:outputText value="#{delivery.assessmentTitle}"/>
      </title>
      </head>
       <body onload="<%= request.getAttribute("html.body.onload") %>; ">

      <h:outputText value="<a name='top'></a>" escape="false" />
 
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

<!-- content... -->
<h:form id="takeAssessmentForm" enctype="multipart/form-data"
   onsubmit="saveTime()">

<!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<script language="javascript" type="text/JavaScript">

function saveTime()
{
  if((typeof (document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=undefined) && ((document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=null) ){
  pauseTiming = 'false';
  document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded/10;
 }
}

</script>
<h:inputHidden id="partIndex" value="#{delivery.partIndex}"/>
<h:inputHidden id="questionIndex" value="#{delivery.questionIndex}"/>
<h:inputHidden id="formatByPart" value="#{delivery.settings.formatByPart}"/>
<h:inputHidden id="formatByAssessment" value="#{delivery.settings.formatByAssessment}"/>
<h:inputHidden id="lastSubmittedDate1" value="#{delivery.assessmentGrading.submittedDate.time}" 
   rendered ="#{delivery.assessmentGrading.submittedDate!=null}"/>
<h:inputHidden id="lastSubmittedDate2" value="0"
   rendered ="#{delivery.assessmentGrading.submittedDate==null}"/>

<!-- HEADING -->
<h3 style="insColor insBak">
   <h:outputText value="#{deliveryMessages.submission_warning}" escape="false"/>
</h3>


<!-- HEADING -->
<f:subview id="assessmentDeliveryHeading">
<%@ include file="/jsf/delivery/assessmentDeliveryTimer.jsp" %>
</f:subview>


<!-- FORM ... note, move these hiddens to whereever they are needed as fparams-->
<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />
<%-- PART/ITEM DATA TABLES --%>

  <h:panelGroup styleClass="validation">
    <h:panelGrid border="0">
	  <h:outputText value="#{deliveryMessages.submit_warning_1}" />
	  <h:panelGroup>
	    <h:outputText value="#{deliveryMessages.submit_warning_2}" />
        <h:outputText value=" <b>#{deliveryMessages.button_submit_grading}</b> "  escape="false"/>
	    <h:outputText value="#{deliveryMessages.submit_warning_3}" />
	  </h:panelGroup>
	  <h:panelGroup rendered="#{delivery.navigation ne '1'}">
	    <h:outputText value="#{deliveryMessages.submit_warning_4}" />
	    <h:outputText value=" <b>#{deliveryMessages.previous}</b> " escape="false"/>
	    <h:outputText value="#{deliveryMessages.submit_warning_5}" />
	  </h:panelGroup>

         <h:panelGroup rendered="#{delivery.navigation eq '1'}">
            <h:outputText value="#{deliveryMessages.submit_warning_4}" />
            <h:outputText value=" <b>#{deliveryMessages.button_cancel}</b> " escape="false"/>
            <h:outputText value="#{deliveryMessages.submit_warning_6}" />
          </h:panelGroup>
	</h:panelGrid>
  </h:panelGroup>

  <h:panelGrid columns="2">

    <h:outputLabel value="#{deliveryMessages.course_name}"/>
    <h:outputText value="#{delivery.courseName}" />

    <h:outputLabel  value="#{deliveryMessages.creator}" />
    <h:outputText value="#{delivery.creatorName}"/>

    <h:outputLabel value="#{deliveryMessages.assessment_title}"/>
    <h:outputText value="#{delivery.assessmentTitle}" escape="false"/>
  </h:panelGrid>

<p class="act">

  <%-- SUBMIT FOR GRADE --%>
  <h:commandButton id="submitforGrade" accesskey="#{deliveryMessages.a_submit}" type="submit" value="#{deliveryMessages.button_submit_grading}"
    action="#{delivery.submitForGrade}" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='previewAssessment') 
             && delivery.navigation ne '1' 
             && !delivery.continue}"
	disabled="#{delivery.actionString=='previewAssessment'}" 
    onclick="disableSubmitForGrade()" onkeypress="" />

  <%-- SUBMIT FOR GRADE FOR LINEAR ACCESS --%>
  <h:commandButton accesskey="#{deliveryMessages.a_submit}" type="submit" value="#{deliveryMessages.button_submit_grading}"
      action="#{delivery.submitForGrade}"  id="submitForm" styleClass="active"
      rendered="#{(delivery.actionString=='takeAssessment'
				   || delivery.actionString=='previewAssessment')
				   && delivery.navigation eq '1' && !delivery.continue}" 
      disabled="#{delivery.actionString=='previewAssessment'}"
      onclick="pauseTiming='false'; disableSubmit()" onkeypress="pauseTiming='false'"/>

  <%-- SUBMIT FOR GRADE DURING PAU --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}"
    action="#{delivery.submitForGrade}"  id="submitForm1" styleClass="active"
    rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"
    onclick="pauseTiming='false'; disableSubmit1();" onkeypress="pauseTiming='false'"/>

  <!-- Previous button for non-linear assessments -->
  <h:commandButton id="previous" type="submit" value="#{deliveryMessages.previous}"
    action="#{delivery.confirmSubmitPrevious}"
    rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.navigation ne '1'}" 
    onclick="disablePrevious()" onkeypress="" />

  <!-- Previous button for linear assessments -->
  <h:commandButton type="submit" value="#{deliveryMessages.button_cancel}"
    action="select" id="cancel"
    rendered="#{(delivery.actionString=='previewAssessment'  
                 || delivery.actionString=='takeAssessment'
				 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.navigation eq '1'}"  
    onclick="pauseTiming='false'; disableSave();" onkeypress="pauseTiming='false'" 
    disabled="#{delivery.actionString=='previewAssessment'}" />

</h:form>
<!-- end content -->
</div>
    </body>
  </html>
</f:view>
