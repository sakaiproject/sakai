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
*      http://www.opensource.org/licenses/ECL-2.0
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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title> <h:outputText value="#{delivery.assessmentTitle}"/> </title>
      <%@ include file="/jsf/delivery/deliveryjQuery.jsp" %>
      <script src="/sakai-editor/editor-bootstrap.js"></script>
      <script src="/sakai-editor/editor.js"></script>
      <script src="/sakai-editor/editor-launch.js"></script>
      <script src="/samigo-app/js/saveForm.js"></script>
      <script src="/samigo-app/js/finInputValidator.js"></script>
      <script src="/webcomponents/rubrics/sakai-rubrics-utils.js<h:outputText value="#{studentScores.CDNQuery}" />"></script>
      <script type="module" src="/webcomponents/rubrics/rubric-association-requirements.js<h:outputText value="#{questionScores.CDNQuery}" />"></script>

    <h:panelGroup rendered="#{delivery.actionString == 'reviewAssessment'}">
      <script>
        var rbcstoken = '<h:outputText value="#{delivery.rbcsToken}" />';
      </script>
    </h:panelGroup>

    <h:outputText value="#{delivery.mathJaxHeader}" escape="false" rendered="#{(delivery.actionString=='takeAssessmentViaUrl' ||  delivery.actionString=='previewAssessment') and delivery.isMathJaxEnabled}"/>
      </head>
	<body>

  <h:panelGroup rendered="#{delivery.assessmentSubmitted}">
    <%@ include file="/jsf/delivery/assessmentHasBeenSubmittedContent.jsp" %>
  </h:panelGroup>

  <h:panelGroup rendered="#{!delivery.assessmentSubmitted}">
 
      <h:outputText value="<a name='top'></a>" escape="false" />
      
<div class="portletBody Mrphs-sakai-samigo">
<div>

<!-- content... -->
<h:form id="takeAssessmentForm" enctype="multipart/form-data"
   onsubmit="saveTime(); serializeImagePoints();">

<!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<script>
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
var formatByQuestion = '<h:outputText value="#{delivery.settings.formatByQuestion}" />';
function setLocation()
{
    // reset questionindex to avoid a Safari bug
	partIndex = document.forms[0].elements['takeAssessmentForm:partIndex'].value;
	questionIndex = document.forms[0].elements['takeAssessmentForm:questionIndex'].value;
 	if (!formatByQuestion)
           document.forms[0].elements['takeAssessmentForm:questionIndex'].value = "0";
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
		//alert("from TOC:" + window.location);
	}
}
var redrawAnchorName = '<h:outputText value="#{delivery.redrawAnchorName}" />';
function setLocation2()
{
	//alert("redrawAnchorName=" + redrawAnchorName);	
	if (redrawAnchorName != null && redrawAnchorName != "") {
		window.location = '#' + redrawAnchorName;
		//alert("from redraw: window.location..." + window.location);
	}
}
function noenter(){
return!(window.event && window.event.keyCode == 13);
}
function saveTime()
{
  if((typeof (document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=undefined) && ((document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=null) ){
  pauseTiming = 'false';
  document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=${delivery.timeElapse};
 }
}
function disableRationale(){
	var textAreas = document.getElementsByTagName("textarea");
	//alert(textAreas[0].id);
	//alert(textAreas[0].id.endsWith('rationale'));
	if (textAreas.length == 1 && textAreas[0].id.endsWith('rationale')) {
		textAreas[0].disabled = true;
	}
}
function enableRationale(){
	var textAreas = document.getElementsByTagName("textarea");
	//alert(textAreas[0].id);
	//alert(textAreas[0].id.endsWith('rationale'));
	if (textAreas.length == 1 && textAreas[0].id.endsWith('rationale')) {
		textAreas[0].disabled = false;
	}
	/* Somehow the following for-loop becomes an infinite look of enableRationale(). No time to look into this now. Use above work around. 
	   Should come back later to figure out the reason.
	for(i=0; i < textAreas.length; i++){
		alert(i);
		if (textAreas[i].id.endsWith('rationale')) {
        textAreas[i].disabled = false;
		return;
		}
    }
	*/
}
// modified from tompuleo.com
String.prototype.endsWith = function(txt)
{
  var rgx;
  rgx = new RegExp(txt+"$");
  return this.match(rgx) != null; 
}
function clickSaCharCountLink(field){
var insertlinkid= field.id.replace("getAaCharCount", "hiddenlink");
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


<h:panelGroup rendered="#{delivery.actionString =='gradeAssessment' || delivery.actionString =='reviewAssessment' }" >
	<f:verbatim>
		<script language='javascript' src='/samigo-app/js/jquery.dynamiclist.student.preview.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.student.preview.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.author.preview.js'></script>
	</f:verbatim>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.actionString !='gradeAssessment' && delivery.actionString !='reviewAssessment' }" >
	<f:verbatim>
		<script language='javascript' src='/samigo-app/js/jquery.dynamiclist.student.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.student.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.author.preview.js'></script>
	</f:verbatim>
</h:panelGroup>

<link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.student.css">
<link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.author.css">

<script>
	var dynamicListMap = [];		
	jQuery(window).load(function(){
		
		$('div[id^=sectionImageMap_]').each(function(){
			var myregexp = /sectionImageMap_(\d+_\d+)/
			var matches = myregexp.exec(this.id);
			var sequence = matches[1];
			var serializedImageMapId = $(this).find('input:hidden[id$=serializedImageMap]').attr('id').replace(/:/g, '\\:');
			
			var dynamicList = new DynamicList(serializedImageMapId, 'imageMapTemplate_'+sequence, 'pointerClass', 'imageMapContainer_'+sequence);
			dynamicList.fillElements();
			
			dynamicListMap[sequence] = dynamicList;
			
		});	
		
		$('input:hidden[id^=hiddenSerializedCoords_]').each(function(){
			var myregexp = /hiddenSerializedCoords_(\d+_\d+)_(\d+)/
			var matches = myregexp.exec(this.id);
			var sequence = matches[1];
			var label = parseInt(matches[2])+1;
			
			var sel = new selectionAuthor({selectionClass: 'selectiondiv', textClass: 'textContainer'}, 'answerImageMapContainer_'+sequence);
			try {
				sel.setCoords(jQuery.parseJSON(this.value));
				sel.setText(label);
			}catch(err){}
			
		});	
	});
	
	function resetImageMap(key) {
		if(dynamicListMap[key] !== undefined)
			dynamicListMap[key].resetElements();
	}
	
	function serializeImagePoints(){
		for(var key in dynamicListMap) {
			if (typeof dynamicListMap[key].serializeElements === 'function') {
				dynamicListMap[key].serializeElements();
			}
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
<h:inputHidden id="hasTimeLimit" value="#{delivery.hasTimeLimit}"/>   
<h:inputHidden id="showTimeWarning" value="#{delivery.showTimeWarning}"/>
<h:inputHidden id="showTimer" value="#{delivery.showTimer}"/>
<h:inputHidden id="dueDate" value="#{delivery.dueDate.time}" rendered="#{delivery.dueDate != null}" />
<h:inputHidden id="retractDate" value="#{delivery.retractDate.time}" rendered="#{delivery.retractDate != null}" />

<!-- DONE BUTTON FOR PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
  <div class="sak-banner-info">
     <h:outputText value="#{deliveryMessages.ass_preview}" escape="false" />
     <h:commandButton value="#{deliveryMessages.exit_preview}"
        styleClass="exitPreviewButton"
        action="#{person.cleanResourceIdListInPreview}"
        type="submit"
        onclick="return returnToHostUrl(\"#{delivery.selectURL}\");" />
  </div>
</h:panelGroup>

<div id="delivPageWrapper">
<h:outputText value="<div id='delivAssessmentWrapper' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

    <!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE -->
    <h:outputText  value="#{delivery.secureDeliveryHTMLFragment}" escape="false"  />

    <!-- HEADING -->
    <f:subview id="assessmentDeliveryHeading">
      <%@ include file="/jsf/delivery/assessmentDeliveryHeading.jsp" %>
    </f:subview>

    <!-- FORM ... note, move these hiddens to whereever they are needed as fparams-->
    <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
    <h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
    <h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />

    <%-- PART/ITEM DATA TABLES --%>
    <h:panelGroup layout="block" rendered="#{delivery.pageContents.isNoParts && delivery.navigation eq '1'}">
      <h:outputText value="#{deliveryMessages.linear_no_contents_warning_1}"/>
      <h:outputText value="#{deliveryMessages.linear_no_contents_warning_2}" escape="false"/>
      <h:outputText value="#{deliveryMessages.linear_no_contents_warning_3}" escape="false"/>
    </h:panelGroup>

<h:panelGroup rendered="#{!delivery.pageContents.isNoParts || delivery.navigation ne '1'}">
  <div class="tier1">
  <h:dataTable width="100%" value="#{delivery.pageContents.partsContents}" var="part">
    <h:column>
    <h4 class="part-header">
        <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}" />
        <small class="part-text">
            <h:outputText value=" #{deliveryMessages.dash} #{part.nonDefaultText}" escape="false"/>
        </small>
        <span class="badge"><h:outputText value="#{part.pointsDisplayString} #{deliveryMessages.splash} #{part.roundedMaxPoints} #{deliveryMessages.pt}" rendered="#{delivery.actionString=='reviewAssessment'}"/></span>
    </h4>
    <h4 class="tier1">
        <small class="part-text">
            <h:outputText value="#{part.description}" escape="false"/>
        </small>
    </h4>

  <!-- PART ATTACHMENTS -->
  <%@ include file="/jsf/delivery/part_attachment.jsp" %>
   <f:verbatim><div class="tier2"></f:verbatim>

   <h:outputText value="#{deliveryMessages.no_question}" escape="false" rendered="#{part.noQuestions}"/>

   <h:dataTable width="100%" value="#{part.itemContents}" var="question">
     <h:column>
		<h:panelGroup layout="block" styleClass="input-group col-sm-6">
			<span class="input-group-addon">
				<h:outputText value="<a name='p#{part.number}q#{question.number}'></a>" escape="false" />
				<h:outputText value="#{deliveryMessages.q} #{question.sequence} #{deliveryMessages.of} #{part.numbering}"/>
			</span>
			<%-- REVIEW ASSESSMENT --%>
			<h:inputText styleClass="form-control adjustedScore" value="#{question.pointsDisplayString}" disabled="true" rendered="#{delivery.actionString=='reviewAssessment'}"/>
			<span class="input-group-addon">
				<%-- REVIEW ASSESSMENT --%>
				<h:outputText value="#{question.roundedMaxPointsToDisplay} #{deliveryMessages.pt}" rendered="#{delivery.actionString=='reviewAssessment'}"/>
				<%-- DELIVER ASSESSMENT --%>
				<h:outputText value="#{question.roundedMaxPoints}" rendered="#{delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag && delivery.actionString!='reviewAssessment'}"  >
					<f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
				</h:outputText>
				<h:outputText value=" #{deliveryMessages.pt}" rendered="#{delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag && delivery.actionString!='reviewAssessment'}"  />
				<h:outputText value="#{deliveryMessages.discount} #{question.itemData.discount} "  rendered="#{question.itemData.discount!='0.0' && delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag}"  >
					<f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
				</h:outputText>
			</span>
			<h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit == true}" value="#{deliveryMessages.extra_credit_preview}" />
		</h:panelGroup>

       <h:panelGroup rendered="#{delivery.actionString == 'reviewAssessment' and delivery.feedbackComponent.showItemLevel}">
         <sakai-rubric-student
           token='<h:outputText value="#{delivery.rbcsToken}" />'
           tool-id="sakai.samigo"
           entity-id='<h:outputText value="pub.#{delivery.assessmentId}.#{question.itemData.itemId}"/>'
           evaluated-item-id='<h:outputText value="#{delivery.assessmentGradingId}.#{question.itemData.itemId}" />'>
         </sakai-rubric-student>
       </h:panelGroup>

       <h:panelGroup rendered="#{delivery.actionString == 'takeAssessment' || delivery.actionString == 'takeAssessmentViaUrl'}">
           <sakai-rubric-student-preview-button
                token="<h:outputText value="#{delivery.rbcsToken}" />"
                tool-id="sakai.samigo"
                entity-id="<h:outputText value="pub.#{delivery.assessmentId}.#{question.itemData.itemId}" />"></sakai-rubric-student-preview-button>
       </h:panelGroup>

       <div class="samigo-question-callout">
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
          <h:panelGroup rendered="#{question.itemData.typeId == 15}"><!-- // CALCULATED_QUESTION -->
           <f:subview id="deliverCalculatedQuestion">
            <%@ include file="/jsf/delivery/item/deliverCalculatedQuestion.jsp" %>
           </f:subview>
          </h:panelGroup>
           <h:panelGroup rendered="#{question.itemData.typeId == 16}"><!-- // IMAGEMAP_QUESTION -->
           <f:subview id="deliverImageMapQuestion">
            <%@ include file="/jsf/delivery/item/deliverImageMapQuestion.jsp" %>
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
          
          <h:panelGroup rendered="#{question.itemData.typeId == 14}">
           <f:subview id="deliverExtendedMatchingItems">
           <%@ include file="/jsf/delivery/item/deliverExtendedMatchingItems.jsp" %>
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
           </h:panelGroup>
           
           <h:panelGroup rendered="#{question.itemData.typeId == 13}">
           <f:subview id="deliverMatrixChoicesSurvey">
           <%@ include file="/jsf/delivery/item/deliverMatrixChoicesSurvey.jsp" %>
           </f:subview>
           </h:panelGroup>

           <div role="alert" class="sak-banner-error" style="display: none" id="autosave-timeexpired-warning">
             <h:outputText value="#{deliveryMessages.time_expired2} " />
           </div>
           <div role="alert" class="sak-banner-error" style="display: none" id="autosave-timeleft-warning">
             <h:panelGroup rendered="#{(delivery.deadlineString != null && delivery.deadlineString ne '')}">
               <h:outputFormat value="#{deliveryMessages.time_left}" escape="false">
                 <f:param value="#{delivery.deadlineString}"/>
               </h:outputFormat>
             </h:panelGroup>
           </div>
           <div role="alert" class="sak-banner-error" style="display: none" id="autosave-failed-warning">
             <p><h:outputText value="#{deliveryMessages.autosaveFailed}" escape="false" /></p>
             <p><h:outputText value="#{deliveryMessages.autosaveFailedDetail}" escape="false" /></p>
           </div>
           <div role="alert" class="sak-banner-error" style="display: none" id="multiple-tabs-warning">
             <p><h:outputText value="#{deliveryMessages.multipleTabsWarning}" escape="false" /></p>
             <p><h:outputText value="#{deliveryMessages.multipleTabsWarningDetail}" escape="false" /></p>
           </div>
          
         </div>
        </h:column>
      </h:dataTable>
     </div>
     <!-- /f:subview -->

    </h:column>
  </h:dataTable>
  </div>
</h:panelGroup>

  <f:verbatim><br/></f:verbatim>

<!-- 1. special case: linear + no question to answer -->
<h:panelGrid columns="2" border="0" rendered="#{delivery.pageContents.isNoParts && delivery.navigation eq '1'}">
  <h:panelGrid columns="1" width="100%" border="0" columnClasses="act">
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}"
      action="#{delivery.confirmSubmit}"  id="submitForm3" styleClass="active"
      rendered="#{(delivery.actionString=='takeAssessment'
                   || delivery.actionString=='takeAssessmentViaUrl'
				   || delivery.actionString=='previewAssessment')
				   && delivery.navigation eq '1' && !delivery.doContinue}" 
      />
  </h:panelGrid>

  <h:panelGrid columns="1" width="100%" border="0">
  <h:commandButton value="#{commonMessages.cancel_action}" type="submit"
     action="select" rendered="#{delivery.pageContents.isNoParts && delivery.navigation eq '1'}" />
  </h:panelGrid>
</h:panelGrid>

<!-- 2. normal flow -->
<h:panelGrid columns="6" border="0" rendered="#{!(delivery.pageContents.isNoParts && delivery.navigation eq '1')}">
  <%-- PREVIOUS --%>
  <h:panelGrid columns="1" border="0">
	<h:commandButton id="previous" type="submit" value="#{deliveryMessages.previous}" styleClass="active"
    action="#{delivery.previous}"
    disabled="#{!delivery.previous}" 
	rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.navigation ne '1' && ((delivery.previous && delivery.doContinue) || (!delivery.previous && delivery.doContinue) || (delivery.previous && !delivery.doContinue))}" />
  </h:panelGrid>

  <%-- NEXT --%>
  <h:panelGrid columns="1" border="0" columnClasses="act">
    <h:commandButton id="next1" type="submit" value="#{commonMessages.action_next}" styleClass="active"
    action="#{delivery.nextPage}" disabled="#{!delivery.doContinue}"
	rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && (delivery.previous && !delivery.doContinue)}" />

    <h:commandButton id="next" type="submit" value="#{commonMessages.action_next}"
    action="#{delivery.nextPage}" styleClass="active"
	rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.doContinue}" />

  </h:panelGrid>


  <h:panelGrid columns="1" border="0">
           <h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" escape="false" />
  </h:panelGrid>

  <%-- SAVE --%>
  <h:panelGrid columns="1" border="0" >
  <h:commandButton id="save" type="submit" value="#{commonMessages.action_save}" styleClass="active"
    action="#{delivery.saveWork}" rendered="#{delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl'}" />
  </h:panelGrid>

  <h:panelGrid columns="1"  border="0">
  <%-- EXIT --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_exit}" styleClass="active"
    action="#{delivery.saveAndExit}" id="saveAndExit"
    rendered="#{(delivery.actionString=='previewAssessment'  
                 || delivery.actionString=='takeAssessment'
                 || (delivery.actionString=='takeAssessmentViaUrl' && !delivery.anonymousLogin))
              && delivery.navigation ne '1' && !delivery.hasTimeLimit}"  
    />

  <%-- SAVE AND EXIT DURING PAU WITH ANONYMOUS LOGIN--%>
  <h:commandButton  type="submit" value="#{deliveryMessages.button_exit}"
    action="#{delivery.saveAndExit}" id="quit"
    rendered="#{(delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin) && !delivery.hasTimeLimit}"
    /> 

  <%-- SAVE AND EXIT FOR LINEAR ACCESS --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_exit}"
    action="#{delivery.saveAndExit}" id="saveAndExit2"
    rendered="#{(delivery.actionString=='previewAssessment'  
                 ||delivery.actionString=='takeAssessment'
                 || (delivery.actionString=='takeAssessmentViaUrl' && !delivery.anonymousLogin))
            && delivery.navigation eq '1' && delivery.doContinue && !delivery.hasTimeLimit}"
    />
  </h:panelGrid>

  <h:panelGrid columns="2" width="100%" border="0" columnClasses="act">
  <%-- SUBMIT FOR GRADE --%>
  <h:commandButton id="submitForGrade" type="submit" value="#{deliveryMessages.button_submit_grading}"
    action="#{delivery.confirmSubmit}" styleClass="active"
    rendered="#{(delivery.actionString=='takeAssessment' ||delivery.actionString=='takeAssessmentViaUrl' || delivery.actionString=='previewAssessment') 
             && delivery.navigation ne '1' 
             && !delivery.doContinue}"
    />

  <%-- SUBMIT FOR GRADE DURING PAU --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit}"
    action="#{delivery.confirmSubmit}"  id="submitForm1" styleClass="active"
    rendered="#{delivery.actionString=='takeAssessmentViaUrl' && delivery.doContinue && delivery.anonymousLogin}"
    />

  <%-- SUBMIT FOR GRADE FOR LINEAR ACCESS --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}"
      action="#{delivery.confirmSubmit}"  id="submitForm" styleClass="active"
      rendered="#{(delivery.actionString=='takeAssessment'
                   || delivery.actionString=='takeAssessmentViaUrl'
				   || delivery.actionString=='previewAssessment')
				   && delivery.navigation eq '1' && !delivery.doContinue}" 
      />

  <%-- SUBMIT FOR DUE OR RETRACT DATE --%>
  <h:commandButton id="submitNoCheck" type="submit" styleClass="hidden active" action="#{delivery.submitFromTimeoutPopup}" value="" />

  </h:panelGrid>
</h:panelGrid>

   <h:commandButton id="autoSave" type="submit" value="" style="display: none"
   action="#{delivery.autoSave}" rendered="#{delivery.actionString=='takeAssessment'
                  || delivery.actionString=='takeAssessmentViaUrl'}" />

	<h:commandLink id="hiddenReloadLink" action="#{delivery.samePage}" value="">
	</h:commandLink>

<f:verbatim></p><br /><br /></f:verbatim>

<!-- CLOSING THE WRAPPER DIVS -->
<f:verbatim></div></f:verbatim>
<f:verbatim></div>
</f:verbatim>
<%@ include file="/jsf/delivery/questionProgress.jspf" %>

<!-- DONE BUTTON IN PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
  <h:commandButton value="#{deliveryMessages.exit_preview}"
     action="#{person.cleanResourceIdListInPreview}"
     type="submit"
     onclick="return returnToHostUrl(\"#{delivery.selectURL}\");" />
</h:panelGroup>

</h:form>
<!-- end content -->
</div>
<f:verbatim></div></f:verbatim>
<script src="/samigo-app/js/questionProgress.js"></script>
<script>
	<%= request.getAttribute("html.body.onload") %> 
	setLocation(); 
	checkRadio();
	fixImplicitLabeling();
	SaveFormContentAsync('deliverAssessment.faces', 'takeAssessmentForm', 'takeAssessmentForm:autoSave', 'takeAssessmentForm:lastSubmittedDate1', 'takeAssessmentForm:lastSubmittedDate2',  <h:outputText value="#{delivery.autoSaveRepeatMilliseconds}"/>, <h:outputText value="#{delivery.actionString=='takeAssessment' or delivery.actionString=='takeAssessmentViaUrl'}"/>); 
	setTimeout('setLocation2()',2);
	questionProgress.transposeTOCTables();
	questionProgress.access(<h:outputText value="#{delivery.navigation}"/>, <h:outputText value="#{delivery.questionLayout}"/>);
    questionProgress.setUp();
</script>
</h:panelGroup>
    </body>
  </html>
</f:view>
