<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
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
      <title> <h:outputText value="#{delivery.assessmentTitle}"/>
      </title>
      </head>
       <body onload="<%= request.getAttribute("html.body.onload") %>; checkRadio(); setLocation();">

   <!--div class="portletBody" style='background:#c57717'-->
      <!--h:outputText value="<body bgcolor='#c57717' #{delivery.settings.bgcolor} #{delivery.settings.background} onLoad='checkRadio();'>" escape="false" /-->

      <h:outputText value="<a name='top'></a>" escape="false" />
 
 <!--h:outputText value="<div class='portletBody' style='background:#{delivery.settings.divBgcolor};background-image:url(http://www.w3.org/WAI/UA/TS/html401/images/test-background.gif)'>" escape="false"/-->
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

<!-- content... -->
<h:form id="takeAssessmentForm" enctype="multipart/form-data"
   onsubmit="saveTime()">

<!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<script language="javascript" type="text/JavaScript">

function checkRadio()
{
  for (i=0; i<document.forms[0].elements.length; i++)
  {
    if (document.forms[0].elements[i].type == "radio")
    {
      if (document.forms[0].elements[i].defaultChecked == true)
      {
        document.forms[0].elements[i].click();
      }
    }
  }
}

function setLocation()
{
	partIndex = document.forms[0].elements['takeAssessmentForm:partIndex'].value;
	questionIndex = document.forms[0].elements['takeAssessmentForm:questionIndex'].value;
	formatByPart = document.forms[0].elements['takeAssessmentForm:formatByPart'].value;
	formatByAssessment = document.forms[0].elements['takeAssessmentForm:formatByAssessment'].value;
    //alert("partIndex = " + partIndex);
    //alert("questionIndex = " + questionIndex);
	//alert("formatByPart = " + formatByPart);
	//alert("formatByAssessment = " + formatByAssessment);
	// We don't want to set the location when the index points to fist question on the page
	// We only set the location in following cases:
	// 1. If it is formatByPart, we set the location when it is not the first question of each part
	// 2. If it is formatByAssessment, we set the location when:
	//    a. it is not the first question of the first part
	//    b. it is a question in any parts other than the first one
	if ((formatByPart == 'true' && questionIndex != 0) || (formatByAssessment == 'true' && ((partIndex == 0 && questionIndex !=0) || partIndex != 0))) {
		window.location = '#p' + ++partIndex + 'q' + ++questionIndex;
	}
}

function noenter(){
return!(window.event && window.event.keyCode == 13);
}

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

<!-- DONE BUTTON FOR PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton id="done" accesskey="#{deliveryMessages.a_done}" value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>


<!-- HEADING -->
<f:subview id="assessmentDeliveryHeading">
<%@ include file="/jsf/delivery/assessmentDeliveryHeading.jsp" %>
</f:subview>

<!-- FORM ... note, move these hiddens to whereever they are needed as fparams-->
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />
<!-- h:inputHidden id="ItemIdent" value="#{item.ItemIdent}"/ -->
<!-- h:inputHidden id="ItemIdent2" value="#{item.itemNo}"/ -->
<!-- h:inputHidden id="currentSection" value="#{item.currentSection}"/ -->
<!-- h:inputHidden id="insertPosition" value="#{item.insertPosition}"/ -->
<%-- PART/ITEM DATA TABLES --%>
<div class="tier1">
  <h:dataTable width="100%" value="#{delivery.pageContents.partsContents}" var="part">
    <h:column>
     <!-- f:subview id="parts" -->
      <f:verbatim><h4></f:verbatim>
      <h:panelGrid columns="2" width="100%" columnClasses="navView,navList">
       <h:panelGroup>
      <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}" />
      <h:outputText value=" #{deliveryMessages.dash} #{part.nonDefaultText}" escape="false"/>
         </h:panelGroup>
      <!-- h:outputText value="#{part.unansweredQuestions}/#{part.questions} " / -->
      <!-- h:outputText value="#{deliveryMessages.ans_q}, " / -->
      <h:outputText value="#{part.pointsDisplayString} #{part.maxPoints} #{deliveryMessages.pt}" 
         rendered="#{delivery.actionString=='reviewAssessment'}"/>
</h:panelGrid>
      <f:verbatim></h4></f:verbatim>
      <h:outputText value="#{part.description}" escape="false"/>
   <f:verbatim><div class="tier2"></f:verbatim>

  <!-- PART ATTACHMENTS -->
  <%@ include file="/jsf/delivery/part_attachment.jsp" %>
   <f:verbatim><div class="tier2"></f:verbatim>

   <h:outputText value="#{deliveryMessages.no_question}" escape="false" rendered="#{part.noQuestions}"/>

      <h:dataTable width="100%" value="#{part.itemContents}" var="question">
        <h:column>
<f:verbatim><h5></f:verbatim>
<h:panelGrid columns="2" width="100%" columnClasses="navView,navList">
         <h:panelGroup>
           <h:outputText value="<a name='p#{part.number}q#{question.number}'></a>" escape="false" />

        <h:outputText value="#{deliveryMessages.q} #{question.sequence} #{deliveryMessages.of} #{part.numbering}"/>
</h:panelGroup>
<h:panelGroup>
<h:outputText value=" #{question.pointsDisplayString} #{question.maxPoints} #{deliveryMessages.pt}" rendered="#{delivery.actionString=='reviewAssessment'}"/>

        <h:outputText value="#{question.maxPoints} #{deliveryMessages.pt}" rendered="#{delivery.actionString!='reviewAssessment'}" />
</h:panelGroup>
</h:panelGrid>
        
          <f:verbatim><div class="tier3"></f:verbatim>
          <h:panelGroup rendered="#{question.itemData.typeId == 7}">
           <f:subview id="deliverAudioRecording">
           <%@ include file="/jsf/delivery/item/deliverAudioRecording.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 6}">
           <f:subview id="deliverFileUpload">
           <%@ include file="/jsf/delivery/item/deliverFileUpload.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 11}">
	       <f:subview id="deliverFillInNumeric">
	       <%@ include file="/jsf/delivery/item/deliverFillInNumeric.jsp" %>
	       </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 8}">
           <f:subview id="deliverFillInTheBlank">
           <%@ include file="/jsf/delivery/item/deliverFillInTheBlank.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 9}">
           <f:subview id="deliverMatching">
            <%@ include file="/jsf/delivery/item/deliverMatching.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup
            rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 3 || question.itemData.typeId == 12}">
           <f:subview id="deliverMultipleChoiceSingleCorrect">
           <%@ include file="/jsf/delivery/item/deliverMultipleChoiceSingleCorrect.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 2}">
           <f:subview id="deliverMultipleChoiceMultipleCorrect">
           <%@ include file="/jsf/delivery/item/deliverMultipleChoiceMultipleCorrect.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 5}">
           <f:subview id="deliverShortAnswer">
           <%@ include file="/jsf/delivery/item/deliverShortAnswer.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 4}">
           <f:subview id="deliverTrueFalse">
           <%@ include file="/jsf/delivery/item/deliverTrueFalse.jsp" %>
           </f:subview>

           <f:verbatim></div></f:verbatim>

          </h:panelGroup>

        </h:column>
      </h:dataTable>
<f:verbatim></div></f:verbatim>
     <!-- /f:subview -->

    </h:column>
  </h:dataTable>
</div>
<p class="act">
  <%-- NEXT --%>
  <h:commandButton id="next" type="submit" value="#{deliveryMessages.save_and_continue}"
    action="#{delivery.next_page}" styleClass="active"
    rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment' 
                 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.continue}"
    onclick="disableNext()" onkeypress="" />

	<h:commandLink id="hiddenReloadLink" action="#{delivery.same_page}" value="">
	</h:commandLink>

  <%-- SUBMIT FOR GRADE --%>
  <h:commandButton id="submitForGrade" type="submit" value="#{deliveryMessages.button_submit_grading}"
    action="#{delivery.confirmSubmit}" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='previewAssessment') 
             && delivery.navigation ne '1' 
             && !delivery.continue}"
	disabled="#{delivery.actionString=='previewAssessment'}" 
    onclick="disableSubmitForGrade()" onkeypress="" />

  <%-- SUBMIT FOR GRADE DURING PAU --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit}"
    action="#{delivery.confirmSubmit}"  id="submitForm1" styleClass="active"
    rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"
    onclick="pauseTiming='false'; disableSubmit1();" onkeypress="pauseTiming='false'"/>

  <%-- PREVIOUS --%>
  <h:commandButton id="previous" type="submit" value="#{deliveryMessages.previous}"
    action="#{delivery.previous}"
    rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.navigation ne '1' && delivery.previous}" 
    onclick="disablePrevious()" onkeypress="" />

  <%-- SUBMIT FOR GRADE FOR LINEAR ACCESS --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}"
      action="#{delivery.confirmSubmit}"  id="submitForm" styleClass="active"
      rendered="#{(delivery.actionString=='takeAssessment'
				   || delivery.actionString=='previewAssessment')
				   && delivery.navigation eq '1' && !delivery.continue}" 
      disabled="#{delivery.actionString=='previewAssessment'}"
      onclick="pauseTiming='false'; disableSubmit()" onkeypress="pauseTiming='false'"/>

  <%-- SAVE AND EXIT --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_save_for_later}"
    action="#{delivery.saveAndExit}" id="saveAndExit"
    rendered="#{(delivery.actionString=='previewAssessment'  
                 || delivery.actionString=='takeAssessment')
              && delivery.navigation ne '1' && !delivery.hasTimeLimit}"  
    onclick="pauseTiming='false'; disableSave();" onkeypress="pauseTiming='false'" 
    disabled="#{delivery.actionString=='previewAssessment'}" />

  <%-- SAVE AND EXIT DURING PAU WITH ANONYMOUS LOGIN--%>
  <h:commandButton  type="submit" value="#{deliveryMessages.button_quit}"
    action="#{delivery.saveAndExit}" id="quit"
    rendered="#{(delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin) && !delivery.hasTimeLimit}"
    onclick="pauseTiming='false'; disableQuit()" onkeypress="pauseTiming='false'"  /> 

  <%-- SAVE AND EXIT FOR LINEAR ACCESS --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_save_for_later}"
    action="#{delivery.saveAndExit}" id="saveAndExit2"
    rendered="#{delivery.actionString=='takeAssessment'
            && delivery.navigation eq '1' && delivery.continue && !delivery.hasTimeLimit}"
    onclick="disableSave2();" onkeypress=""
    disabled="#{delivery.actionString=='previewAssessment'}"/>

</p>

<!-- DONE BUTTON IN PREVIEW -->
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
