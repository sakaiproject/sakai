<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:t="http://myfaces.apache.org/tomahawk">
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
* $Id$

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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <jsp:text> </jsp:text>
          <h:outputText value="#{delivery.assessmentTitle}"/>
          <jsp:text> </jsp:text>
        </title>
        <script>
          <jsp:text>var portal = portal || { siteId: '</jsp:text>
          <h:outputText value="#{delivery.siteId}"/>
          <jsp:text>' };</jsp:text>
        </script>
        <jsp:directive.include file="/jsf/delivery/deliveryjQuery.jsp"/>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/sakai-editor/editor-bootstrap.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/sakai-editor/editor.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/sakai-editor/editor-launch.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/saveForm.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/deliveryQuestionCancellation.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:text><![CDATA[
      <script type="module" src="/webcomponents/bundles/rubric-association-requirements.js]]></jsp:text>
        <h:outputText value="#{questionScores.CDNQuery}"/>
        <jsp:text><![CDATA["></script>

    ]]></jsp:text>
        <h:outputText value="#{delivery.mathJaxHeader}" escape="false" rendered="#{(delivery.actionString=='takeAssessmentViaUrl' ||  delivery.actionString=='previewAssessment') and delivery.isMathJaxEnabled}"/>
      </head>
      <body>
        <h:panelGroup rendered="#{delivery.assessmentSubmitted}">
          <jsp:directive.include file="/jsf/delivery/assessmentHasBeenSubmittedContent.jsp"/>
        </h:panelGroup>
        <h:panelGroup rendered="#{!delivery.assessmentSubmitted}">
          <h:outputText value="&lt;a name='top'&gt;&lt;/a&gt;" escape="false"/>
          <div class="portletBody Mrphs-sakai-samigo">
            <jsp:text><![CDATA[
<div>

]]></jsp:text>
            <!-- content...  -->
            <h:form id="takeAssessmentForm" enctype="multipart/form-data" onsubmit="saveTime(); serializeImagePoints();">
              <!-- JAVASCRIPT  -->
              <jsp:directive.include file="/js/delivery.js"/>
              <script>
                <jsp:text><![CDATA[
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
var formatByQuestion = ']]></jsp:text>
                <h:outputText value="#{delivery.settings.formatByQuestion}"/>
                <jsp:text><![CDATA[';
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
var redrawAnchorName = ']]></jsp:text>
                <h:outputText value="#{delivery.redrawAnchorName}"/>
                <jsp:text><![CDATA[';
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
  document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=]]></jsp:text>
                <jsp:text>${delivery.timeElapse}</jsp:text>
                <jsp:text><![CDATA[;
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
]]></jsp:text>
              </script>
              <h:panelGroup rendered="#{delivery.actionString =='gradeAssessment' || delivery.actionString =='reviewAssessment' }">
                <f:verbatim>
                  <jsp:element name="script">
                    <jsp:attribute name="language" trim="false">javascript</jsp:attribute>
                    <jsp:attribute name="src" trim="false">/samigo-app/js/jquery.dynamiclist.student.preview.js</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                  <jsp:element name="script">
                    <jsp:attribute name="language" trim="false">javascript</jsp:attribute>
                    <jsp:attribute name="src" trim="false">/samigo-app/js/selection.student.preview.js</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                  <jsp:element name="script">
                    <jsp:attribute name="language" trim="false">javascript</jsp:attribute>
                    <jsp:attribute name="src" trim="false">/samigo-app/js/selection.author.preview.js</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                </f:verbatim>
              </h:panelGroup>
              <h:panelGroup rendered="#{delivery.actionString !='gradeAssessment' &amp;&amp; delivery.actionString !='reviewAssessment' }">
                <f:verbatim>
                  <jsp:element name="script">
                    <jsp:attribute name="language" trim="false">javascript</jsp:attribute>
                    <jsp:attribute name="src" trim="false">/samigo-app/js/jquery.dynamiclist.student.js</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                  <jsp:element name="script">
                    <jsp:attribute name="language" trim="false">javascript</jsp:attribute>
                    <jsp:attribute name="src" trim="false">/samigo-app/js/selection.student.js</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                  <jsp:element name="script">
                    <jsp:attribute name="language" trim="false">javascript</jsp:attribute>
                    <jsp:attribute name="src" trim="false">/samigo-app/js/selection.author.preview.js</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                </f:verbatim>
              </h:panelGroup>
              <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.student.css"/>
              <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.author.css"/>
              <script>
                <jsp:text>
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

</jsp:text>
              </script>
              <h:inputHidden id="partIndex" value="#{delivery.partIndex}"/>
              <h:inputHidden id="questionIndex" value="#{delivery.questionIndex}"/>
              <h:inputHidden id="formatByPart" value="#{delivery.settings.formatByPart}"/>
              <h:inputHidden id="formatByAssessment" value="#{delivery.settings.formatByAssessment}"/>
              <h:inputHidden id="lastSubmittedDate1" value="#{delivery.assessmentGrading.submittedDate.time}" rendered="#{delivery.assessmentGrading.submittedDate!=null}"/>
              <h:inputHidden id="lastSubmittedDateStr" value="#{delivery.submittedDateString}" rendered="#{delivery.assessmentGrading.submittedDate!=null}"/>
              <h:inputHidden id="lastSubmittedDate2" value="0" rendered="#{delivery.assessmentGrading.submittedDate==null}"/>
              <h:inputHidden id="hasTimeLimit" value="#{delivery.hasTimeLimit}"/>
              <h:inputHidden id="attemptDate" value="#{delivery.assessmentGrading.attemptDate.time}" rendered="#{delivery.assessmentGrading.attemptDate!=null}"/>
              <h:inputHidden id="showTimeWarning" value="#{delivery.showTimeWarning}"/>
              <h:inputHidden id="showTimer" value="#{delivery.showTimer}"/>
              <h:inputHidden id="dueDate" value="#{delivery.dueDate.time}" rendered="#{delivery.dueDate != null}"/>
              <h:inputHidden id="retractDate" value="#{delivery.retractDate.time}" rendered="#{delivery.retractDate != null}"/>
              <!-- DONE BUTTON FOR PREVIEW  -->
              <h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
                <div class="sak-banner-info mb-5">
                  <h:outputText value="#{deliveryMessages.ass_preview}" escape="false"/>
                  <br/>
                  <h:commandButton value="#{deliveryMessages.exit_preview}" styleClass="exit-preview-button" action="#{person.cleanResourceIdListInPreview}" type="submit" onclick="return returnToHostUrl(&quot;#{delivery.selectURL}&quot;);"/>
                </div>
              </h:panelGroup>
              <jsp:text><![CDATA[

<div id="delivPageWrapper">
]]></jsp:text>
              <h:outputText value="&lt;div id='delivAssessmentWrapper' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'&gt;" escape="false"/>
              <!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE  -->
              <h:outputText value="#{delivery.secureDeliveryHTMLFragment}" escape="false"/>
              <!-- HEADING  -->
              <f:subview id="assessmentDeliveryHeading">
                <jsp:directive.include file="/jsf/delivery/assessmentDeliveryHeading.jsp"/>
              </f:subview>
              <!-- FORM ... note, move these hiddens to whereever they are needed as fparams -->
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
              <h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}"/>
              <!-- PART/ITEM DATA TABLES  -->
              <h:panelGroup layout="block" rendered="#{delivery.pageContents.isNoParts &amp;&amp; delivery.navigation eq '1'}">
                <h:outputText value="#{deliveryMessages.linear_no_contents_warning_1}"/>
                <h:outputText value="#{deliveryMessages.linear_no_contents_warning_2}" escape="false"/>
                <h:outputText value="#{deliveryMessages.linear_no_contents_warning_3}" escape="false"/>
              </h:panelGroup>
              <h:panelGroup rendered="#{!delivery.pageContents.isNoParts || delivery.navigation ne '1'}">
                <div class="tier1">
                  <h:dataTable width="100%" value="#{delivery.pageContents.partsContents}" var="part">
                    <h:column>
                      <h4 class="part-header">
                        <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}"/>
                        <h:outputText value=" #{deliveryMessages.dash} #{part.nonDefaultText}" escape="false" rendered="#{! empty part.nonDefaultText}"/>
                        <span class="badge rounded-pill text-bg-secondary">
                          <h:outputText value="#{part.pointsDisplayString} #{deliveryMessages.splash} #{part.roundedMaxPoints} #{deliveryMessages.pt}" rendered="#{delivery.actionString=='reviewAssessment'}"/>
                        </span>
                      </h4>
                      <h4 class="tier1">
                        <small class="part-text">
                          <h:outputText value="#{part.description}" escape="false">
                            <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.SecureContentWrapper"/>
                          </h:outputText>
                        </small>
                      </h4>
                      <h:panelGroup rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl') &amp;&amp; part.enabled == -1}" styleClass="time-bar-button-container" layout="block">
                        <div class="sak-banner-info">
                          <h:outputFormat value="#{deliveryMessages.partTimer_info_start_deadline}" escape="false" rendered="#{delivery.deadline != null &amp;&amp; delivery.deadline != ''}">
                            <f:param value="#{part.timeLimitString}"/>
                            <f:param value="#{delivery.deadlineString}"/>
                          </h:outputFormat>
                          <h:outputFormat value="#{deliveryMessages.partTimer_info_start}" escape="false" rendered="#{delivery.deadline == null}">
                            <f:param value="#{part.timeLimitString}"/>
                          </h:outputFormat>
                        </div>
                        <h:commandButton type="submit" value="#{deliveryMessages.timer_start}" styleClass="active" action="#{part.startTimedSection}"/>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl') &amp;&amp; part.timedSection &amp;&amp; part.enabled == 1}" styleClass="time-bar-container" layout="block">
                        <jsp:text><![CDATA[
        <sakai-timer-bar
            id="]]></jsp:text>
                        <h:outputText value="#{part.sectionId}"/>
                        <jsp:text>"
            time-limit="</jsp:text>
                        <h:outputText value="#{part.realTimeLimit}"/>
                        <jsp:text>"
            time-elapsed="</jsp:text>
                        <h:outputText value="#{part.timeElapsed}"/>
                        <jsp:text>"
            text="</jsp:text>
                        <h:outputText value="#{deliveryMessages.partTimer_title}"/>
                        <jsp:text>"
            sync-call="/api/assessmentgrading/</jsp:text>
                        <h:outputText value="#{delivery.assessmentGradingId}"/>
                        <jsp:text>/timerinfo/part/</jsp:text>
                        <h:outputText value="#{part.sectionId}"/>
                        <jsp:text><![CDATA["
        ></sakai-timer-bar>
    ]]></jsp:text>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl') &amp;&amp; part.enabled == 0 &amp;&amp; !delivery.settings.formatByQuestion}">
                        <div class="sak-banner-warn">
                          <h:outputText value="#{deliveryMessages.partTimer_info_end}"/>
                        </div>
                      </h:panelGroup>
                      <!-- PART can be hidden if is timed and not enabled (this does not apply to assessments by question)  -->
                      <h:panelGroup rendered="#{delivery.actionString=='previewAssessment' || delivery.actionString=='reviewAssessment' || part.enabled == 1 || delivery.settings.formatByQuestion}">
                        <!-- PART ATTACHMENTS  -->
                        <jsp:directive.include file="/jsf/delivery/part_attachment.jsp"/>
                        <f:verbatim>
                          <jsp:text><![CDATA[<div class="tier2">]]></jsp:text>
                        </f:verbatim>
                        <h:outputText value="#{deliveryMessages.no_question}" escape="false" rendered="#{part.noQuestions}"/>
                        <h:dataTable width="100%" value="#{part.itemContents}" var="question">
                          <h:column>
                            <h:panelGroup layout="block" styleClass="row #{delivery.actionString}">
                              <h:panelGroup layout="block" styleClass="col-12 col-md-6">
                                <h:panelGroup layout="block" styleClass="row">
                                  <h:panelGroup layout="block" styleClass="col-12">
                                    <div class="input-group my-3">
                                      <p class="samigo-input-group-text m-0 pe-4 border-end-0">
                                        <h:outputText value="&lt;a name='p#{part.number}q#{question.number}'&gt;&lt;/a&gt;" escape="false"/>
                                        <h:outputText value="#{deliveryMessages.q} #{question.sequence} #{deliveryMessages.of} #{part.numbering}"/>
                                      </p>
                                      <!-- REVIEW ASSESSMENT  -->
                                      <h:inputText styleClass="form-control adjustedScore" value="#{question.pointsDisplayString}" disabled="true" rendered="#{delivery.actionString=='reviewAssessment'}"/>
                                      <h:panelGroup layout="block" rendered="#{delivery.actionString != 'gradeAssessment' &amp;&amp; delivery.actionString != 'reviewAssessment'}">
                                        <jsp:directive.include file="/jsf/delivery/item/deliverAssessmentQuestionPoints.jsp"/>
                                      </h:panelGroup>
                                    </div>
                                  </h:panelGroup>
                                  <h:panelGroup layout="block" styleClass="col-12" rendered="#{delivery.actionString != 'takeAssessment' &amp;&amp; delivery.actionString != 'takeAssessmentViaUrl' &amp;&amp; delivery.actionString != 'previewAssessment'}">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverAssessmentQuestionPoints.jsp"/>
                                  </h:panelGroup>
                                </h:panelGroup>
                              </h:panelGroup>
                            </h:panelGroup>
                            <!-- PART can be hidden if is timed and not enabled (apply to assessments by question)  -->
                            <h:panelGroup rendered="#{delivery.actionString=='previewAssessment' || delivery.actionString=='reviewAssessment' || part.enabled == 1}">
                              <h:panelGroup rendered="#{delivery.actionString == 'reviewAssessment' and delivery.feedbackComponent.showItemLevel}">
                                <jsp:text><![CDATA[
         <sakai-rubric-student
           site-id=']]></jsp:text>
                                <h:outputText value="#{delivery.siteId}"/>
                                <jsp:text>'
           tool-id="sakai.samigo"
           entity-id='</jsp:text>
                                <h:outputText value="#{delivery.rubricAssociation}.#{question.effectiveItemId}"/>
                                <jsp:text>'
           evaluated-item-id='</jsp:text>
                                <h:outputText value="#{delivery.assessmentGradingId}.#{question.itemData.itemId}"/>
                                <jsp:text>'
           evaluated-item-owner-id='</jsp:text>
                                <h:outputText value="#{person.id}"/>
                                <jsp:text><![CDATA['>
         </sakai-rubric-student>
       ]]></jsp:text>
                              </h:panelGroup>
                              <h:panelGroup rendered="#{(delivery.actionString == 'takeAssessment' || delivery.actionString == 'takeAssessmentViaUrl') &amp;&amp; question.enabled == 1 || delivery.actionString == 'previewAssessment'}">
                                <jsp:text><![CDATA[
           <sakai-rubric-student-preview-button
                site-id=']]></jsp:text>
                                <h:outputText value="#{delivery.siteId}"/>
                                <jsp:text>'
                tool-id="sakai.samigo"
                entity-id="</jsp:text>
                                <h:outputText value="#{delivery.rubricAssociation}.#{question.effectiveItemId}"/>
                                <jsp:text><![CDATA["></sakai-rubric-student-preview-button>
       ]]></jsp:text>
                              </h:panelGroup>
                              <h:panelGroup rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl') &amp;&amp; question.enabled == -1}" styleClass="time-bar-button-container" layout="block">
                                <h:panelGroup rendered="#{question.timedQuestion}" styleClass="sak-banner-info" layout="block">
                                  <h:outputFormat value="#{deliveryMessages.questionTimer_info_start_deadline}" escape="false" rendered="#{delivery.deadline != null &amp;&amp; delivery.deadline != ''}">
                                    <f:param value="#{question.timeLimitString}"/>
                                    <f:param value="#{delivery.deadlineString}"/>
                                  </h:outputFormat>
                                  <h:outputFormat value="#{deliveryMessages.questionTimer_info_start}" escape="false" rendered="#{delivery.deadline == null}">
                                    <f:param value="#{question.timeLimitString}"/>
                                  </h:outputFormat>
                                </h:panelGroup>
                                <h:commandButton type="submit" value="#{deliveryMessages.timer_start}" styleClass="active" action="#{question.startTimedQuestion}"/>
                              </h:panelGroup>
                              <h:panelGroup rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl') &amp;&amp; question.enabled == 0}">
                                <div class="sak-banner-warn">
                                  <h:outputText value="#{deliveryMessages.questionTimer_info_end}"/>
                                </div>
                              </h:panelGroup>
                              <h:panelGroup rendered="#{(delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl') &amp;&amp; question.timedQuestion &amp;&amp; question.enabled == 1}" styleClass="time-bar-container" layout="block">
                                <jsp:text><![CDATA[
           <sakai-timer-bar
               id="]]></jsp:text>
                                <h:outputText value="#{question.itemData.itemId}"/>
                                <jsp:text>"
               time-limit="</jsp:text>
                                <h:outputText value="#{question.realTimeLimit}"/>
                                <jsp:text>"
               time-elapsed="</jsp:text>
                                <h:outputText value="#{question.timeElapsed}"/>
                                <jsp:text>"
               text="</jsp:text>
                                <h:outputText value="#{deliveryMessages.questionTimer_title}"/>
                                <jsp:text>"
               sync-call="/api/assessmentgrading/</jsp:text>
                                <h:outputText value="#{delivery.assessmentGradingId}"/>
                                <jsp:text>/timerinfo/question/</jsp:text>
                                <h:outputText value="#{question.itemData.itemId}"/>
                                <jsp:text><![CDATA["
           ></sakai-timer-bar>
       ]]></jsp:text>
                              </h:panelGroup>
                              <h:panelGroup id="questionContent" rendered="#{delivery.actionString=='previewAssessment' || delivery.actionString=='reviewAssessment' || question.enabled == 1}" styleClass="samigo-question-callout#{question.cancelled ? ' samigo-question-cancelled' : ''}" layout="block">
                                <h:panelGroup rendered="#{question.itemData.typeId == 7}">
                                  <f:subview id="deliverAudioRecording">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverAudioRecording.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 6}">
                                  <f:subview id="deliverFileUpload">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverFileUpload.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 11}">
                                  <f:subview id="deliverFillInNumeric">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverFillInNumeric.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 8}">
                                  <f:subview id="deliverFillInTheBlank">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverFillInTheBlank.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 9}">
                                  <f:subview id="deliverMatching">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverMatching.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 15}">
                                  <!-- // CALCULATED_QUESTION  -->
                                  <f:subview id="deliverCalculatedQuestion">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverCalculatedQuestion.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 16}">
                                  <!-- // IMAGEMAP_QUESTION  -->
                                  <f:subview id="deliverImageMapQuestion">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverImageMapQuestion.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 3 || question.itemData.typeId == 12}">
                                  <f:subview id="deliverMultipleChoiceSingleCorrect">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverMultipleChoiceSingleCorrect.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 2}">
                                  <f:subview id="deliverMultipleChoiceMultipleCorrect">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverMultipleChoiceMultipleCorrect.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 14}">
                                  <f:subview id="deliverExtendedMatchingItems">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverExtendedMatchingItems.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 5}">
                                  <f:subview id="deliverShortAnswer">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverShortAnswer.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 4}">
                                  <f:subview id="deliverTrueFalse">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverTrueFalse.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <h:panelGroup rendered="#{question.itemData.typeId == 13}">
                                  <f:subview id="deliverMatrixChoicesSurvey">
                                    <jsp:directive.include file="/jsf/delivery/item/deliverMatrixChoicesSurvey.jsp"/>
                                  </f:subview>
                                </h:panelGroup>
                                <div role="alert" class="sak-banner-error" style="display: none" id="autosave-timeexpired-warning">
                                  <h:outputText value="#{deliveryMessages.time_expired2} "/>
                                </div>
                                <div role="alert" class="sak-banner-error" style="display: none" id="autosave-timeleft-warning">
                                  <h:panelGroup rendered="#{(delivery.deadlineString != null &amp;&amp; delivery.deadlineString ne '')}">
                                    <h:outputFormat value="#{deliveryMessages.time_left}" escape="false">
                                      <f:param value="#{delivery.deadlineString}"/>
                                    </h:outputFormat>
                                  </h:panelGroup>
                                </div>
                                <div role="alert" class="sak-banner-error" style="display: none" id="autosave-failed-warning">
                                  <h5>
                                    <h:outputText value="#{deliveryMessages.autosaveFailed_heading}" escape="false"/>
                                  </h5>
                                  <p>
                                    <h:outputText value="#{deliveryMessages.autosaveFailed_reason}" escape="false"/>
                                    <br/>
                                    <h:outputText value="#{deliveryMessages.autosaveFailedDetail}" escape="false"/>
                                  </p>
                                </div>
                                <div role="alert" class="sak-banner-error" style="display: none" id="multiple-tabs-warning">
                                  <h5>
                                    <h:outputText value="#{deliveryMessages.multipleTabsWarning_heading}" escape="false"/>
                                  </h5>
                                  <p>
                                    <h:outputText value="#{deliveryMessages.multipleTabsWarning_1}" escape="false"/>
                                    <br/>
                                    <h:outputText value="#{deliveryMessages.multipleTabsWarning_2}" escape="false"/>
                                    <br/>
                                    <h:outputText value="#{deliveryMessages.multipleTabsWarning_3}" escape="false"/>
                                  </p>
                                  <h5>
                                    <h:outputText value="#{deliveryMessages.multipleTabsWarningFix_heading}" escape="false"/>
                                  </h5>
                                  <ol>
                                    <li>
                                      <h:outputText value="#{deliveryMessages.multipleTabsWarningFix_1}" escape="false"/>
                                    </li>
                                    <li>
                                      <h:outputText value="#{deliveryMessages.multipleTabsWarningFix_2}" escape="false"/>
                                    </li>
                                    <li>
                                      <h:outputText value="#{deliveryMessages.multipleTabsWarningFix_3}" escape="false"/>
                                    </li>
                                    <li>
                                      <h:outputText value="#{deliveryMessages.multipleTabsWarningFix_4}" escape="false"/>
                                    </li>
                                  </ol>
                                </div>
                                <h:panelGroup styleClass="sak-banner-info" rendered="#{question.cancelled}" layout="block">
                                  <h:outputText value="#{commonMessages.cancel_question_info_cancelled_question}"/>
                                  <h:outputText value=" "/>
                                  <h:outputText value="#{commonMessages.cancel_question_info_skip_question}"/>
                                </h:panelGroup>
                              </h:panelGroup>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{part.enabled == 0 &amp;&amp; delivery.settings.formatByQuestion}">
                              <div class="sak-banner-warn">
                                <h:outputText value="#{deliveryMessages.partTimer_info_end_byquestion}"/>
                              </div>
                            </h:panelGroup>
                          </h:column>
                        </h:dataTable>
                      </h:panelGroup>
                      <jsp:text><![CDATA[
     </div>
     ]]></jsp:text>
                      <!-- /f:subview  -->
                    </h:column>
                  </h:dataTable>
                </div>
              </h:panelGroup>
              <f:verbatim>
                <br/>
              </f:verbatim>
              <div role="alert" aria-live="polite" aria-atomic="true">
                <span id="autosave-msg">
                  <h:outputText value="#{deliveryMessages.autosaving}"/>
                </span>
                <span id="autosave-lasttime-msg">
                  <h:outputText value="#{deliveryMessages.autosaveTime}"/>
                  <jsp:text> </jsp:text>
                  <jsp:element name="span">
                    <jsp:attribute name="id" trim="false">autosave-lasttime</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                </span>
              </div>
              <!-- 1. special case: linear + no question to answer  -->
              <h:panelGrid columns="2" border="0" rendered="#{delivery.pageContents.isNoParts &amp;&amp; delivery.navigation eq '1'}">
                <h:panelGrid columns="1" width="100%" border="0" columnClasses="act">
                  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.confirmSubmit}" id="submitForm3" styleClass="active" rendered="#{(delivery.actionString=='takeAssessment'&#10;                   || delivery.actionString=='takeAssessmentViaUrl'&#10;&#9;&#9;&#9;&#9;   || delivery.actionString=='previewAssessment')&#10;&#9;&#9;&#9;&#9;   &amp;&amp; delivery.navigation eq '1' &amp;&amp; !delivery.doContinue}"/>
                </h:panelGrid>
                <h:panelGrid columns="1" width="100%" border="0">
                  <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="select" rendered="#{delivery.pageContents.isNoParts &amp;&amp; delivery.navigation eq '1'}"/>
                </h:panelGrid>
              </h:panelGrid>
              <!-- 2. normal flow  -->
              <h:panelGrid columns="6" border="0" rendered="#{!(delivery.pageContents.isNoParts &amp;&amp; delivery.navigation eq '1')}">
                <!-- PREVIOUS  -->
                <h:panelGrid columns="1" border="0">
                  <h:commandButton id="previous" type="submit" value="#{deliveryMessages.previous}" styleClass="active" action="#{delivery.previous}" disabled="#{!delivery.previous}" rendered="#{(delivery.actionString=='previewAssessment'&#10;                 || delivery.actionString=='takeAssessment'&#10;                 || delivery.actionString=='takeAssessmentViaUrl')&#10;              &amp;&amp; delivery.navigation ne '1' &amp;&amp; ((delivery.previous &amp;&amp; delivery.doContinue) || (!delivery.previous &amp;&amp; delivery.doContinue) || (delivery.previous &amp;&amp; !delivery.doContinue))}"/>
                </h:panelGrid>
                <!-- NEXT  -->
                <h:panelGrid columns="1" border="0" columnClasses="act">
                  <h:commandButton id="next1" type="submit" value="#{commonMessages.action_next}" styleClass="active" action="#{delivery.nextPage}" disabled="#{!delivery.doContinue}" rendered="#{(delivery.actionString=='previewAssessment'&#10;                 || delivery.actionString=='takeAssessment'&#10;                 || delivery.actionString=='takeAssessmentViaUrl')&#10;              &amp;&amp; (delivery.previous &amp;&amp; !delivery.doContinue)}"/>
                  <h:commandButton id="next" type="submit" value="#{commonMessages.action_next}" action="#{delivery.nextPage}" styleClass="active" disabled="#{!delivery.nextEnabled}" rendered="#{(delivery.actionString=='previewAssessment'&#10;                 || delivery.actionString=='takeAssessment'&#10;                 || delivery.actionString=='takeAssessmentViaUrl')&#10;              &amp;&amp; delivery.doContinue}"/>
                </h:panelGrid>
                <h:panelGrid columns="1" border="0">
                  <h:outputText value="&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;" escape="false"/>
                </h:panelGrid>
                <!-- SAVE  -->
                <h:panelGrid columns="1" border="0">
                  <h:commandButton id="save" type="submit" value="#{commonMessages.action_save}" styleClass="active" action="#{delivery.saveWork}" rendered="#{delivery.actionString=='previewAssessment'&#10;                 || delivery.actionString=='takeAssessment'&#10;                 || delivery.actionString=='takeAssessmentViaUrl'}"/>
                </h:panelGrid>
                <h:panelGrid columns="1" border="0">
                  <!-- EXIT  -->
                  <h:commandButton type="submit" value="#{deliveryMessages.button_exit}" styleClass="active" action="#{delivery.saveAndExit}" id="saveAndExit" rendered="#{(delivery.actionString=='previewAssessment'  &#10;                 || delivery.actionString=='takeAssessment'&#10;                 || (delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; !delivery.anonymousLogin))&#10;              &amp;&amp; delivery.navigation ne '1' &amp;&amp; !delivery.hasTimeLimit}"/>
                  <!-- SAVE AND EXIT DURING PAU WITH ANONYMOUS LOGIN -->
                  <h:commandButton type="submit" value="#{deliveryMessages.button_exit}" action="#{delivery.saveAndExit}" id="quit" rendered="#{(delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; delivery.anonymousLogin) &amp;&amp; !delivery.hasTimeLimit}"/>
                  <!-- SAVE AND EXIT FOR LINEAR ACCESS  -->
                  <h:commandButton type="submit" value="#{deliveryMessages.button_exit}" action="#{delivery.saveAndExit}" id="saveAndExit2" rendered="#{(delivery.actionString=='previewAssessment'  &#10;                 ||delivery.actionString=='takeAssessment'&#10;                 || (delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; !delivery.anonymousLogin))&#10;            &amp;&amp; delivery.navigation eq '1' &amp;&amp; delivery.doContinue &amp;&amp; !delivery.hasTimeLimit}"/>
                </h:panelGrid>
                <h:panelGrid columns="2" width="100%" border="0" columnClasses="act">
                  <!-- SUBMIT FOR GRADE  -->
                  <h:commandButton id="submitForGrade" type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.confirmSubmit}" styleClass="active" rendered="#{(delivery.actionString=='takeAssessment' ||delivery.actionString=='takeAssessmentViaUrl' || delivery.actionString=='previewAssessment') &#10;             &amp;&amp; delivery.navigation ne '1' &#10;             &amp;&amp; !delivery.doContinue}"/>
                  <!-- SUBMIT FOR GRADE DURING PAU  -->
                  <h:commandButton type="submit" value="#{deliveryMessages.button_submit}" action="#{delivery.confirmSubmit}" id="submitForm1" styleClass="active" rendered="#{delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; delivery.doContinue &amp;&amp; delivery.anonymousLogin}"/>
                  <!-- SUBMIT FOR GRADE FOR LINEAR ACCESS  -->
                  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.confirmSubmit}" id="submitForm" styleClass="active" rendered="#{(delivery.actionString=='takeAssessment'&#10;                   || delivery.actionString=='takeAssessmentViaUrl'&#10;&#9;&#9;&#9;&#9;   || delivery.actionString=='previewAssessment')&#10;&#9;&#9;&#9;&#9;   &amp;&amp; delivery.navigation eq '1' &amp;&amp; !delivery.doContinue}"/>
                  <!-- SUBMIT FOR DUE OR RETRACT DATE  -->
                  <h:commandButton id="submitNoCheck" type="submit" styleClass="d-none active" action="#{delivery.submitFromTimeoutPopup}" value=""/>
                </h:panelGrid>
              </h:panelGrid>
              <h:commandButton id="autoSave" type="submit" value="" style="display: none" action="#{delivery.autoSave}" rendered="#{delivery.actionString=='takeAssessment'&#10;                  || delivery.actionString=='takeAssessmentViaUrl'}"/>
              <h:commandLink id="hiddenReloadLink" action="#{delivery.samePage}" value="">
              </h:commandLink>
              <f:verbatim>
                <jsp:text><![CDATA[</p>]]></jsp:text>
                <br/>
                <br/>
              </f:verbatim>
              <!-- CLOSING THE WRAPPER DIVS  -->
              <f:verbatim>
                <jsp:text><![CDATA[</div>]]></jsp:text>
              </f:verbatim>
              <f:verbatim>
                <jsp:text><![CDATA[</div>
]]></jsp:text>
              </f:verbatim>
              <jsp:directive.include file="/jsf/delivery/questionProgress.jspf"/>
            </h:form>
            <!-- end content  -->
          </div>
          <f:verbatim>
            <jsp:text><![CDATA[</div>]]></jsp:text>
          </f:verbatim>
          <jsp:element name="script">
            <jsp:attribute name="src" trim="false">/samigo-app/js/questionProgress.js</jsp:attribute>
            <jsp:body>
            </jsp:body>
          </jsp:element>
          <script>
            <jsp:expression> request.getAttribute("html.body.onload") </jsp:expression>
            <jsp:text> 
	setLocation(); 
	checkRadio();
	fixImplicitLabeling();
	SaveFormContentAsync('deliverAssessment.faces', 'takeAssessmentForm', 'takeAssessmentForm:autoSave', 'takeAssessmentForm:lastSubmittedDate1', 'takeAssessmentForm:lastSubmittedDate2',  </jsp:text>
            <h:outputText value="#{delivery.autoSaveRepeatMilliseconds}"/>
            <jsp:text>, </jsp:text>
            <h:outputText value="#{delivery.actionString=='takeAssessment' or delivery.actionString=='takeAssessmentViaUrl'}"/>
            <jsp:text>); 
	setTimeout('setLocation2()',2);
	questionProgress.transposeTOCTables();
	questionProgress.access(</jsp:text>
            <h:outputText value="#{delivery.navigation}"/>
            <jsp:text>, </jsp:text>
            <h:outputText value="#{delivery.questionLayout}"/>
            <jsp:text><![CDATA[);
    questionProgress.setUp();

	//Save button can be disabled due to auto-save. If so, just wait a bit and try again.
	var tryClickSave = function(){
		let btn = document.getElementById("takeAssessmentForm:save");
		if(!btn.disabled){
			btn.click();
		} else {
			setTimeout(tryClickSave, 500);
		}
	}
	
	//Messages received from Timer (vuecomponent).
	window.addEventListener(
		"message",
		(event) => {
			if(event.origin == window.location.origin){
				switch(event.data.msg) {
					case 'SAVE':
						SaveFormContentAsync('deliverAssessment.faces', 'takeAssessmentForm', 'takeAssessmentForm:autoSave', 'takeAssessmentForm:lastSubmittedDate1', 'takeAssessmentForm:lastSubmittedDate2', 0, ]]></jsp:text>
            <h:outputText value="#{delivery.actionString=='takeAssessment' or delivery.actionString=='takeAssessmentViaUrl'}"/>
            <jsp:text>); 
						break;

					case 'END':
						tryClickSave();
						break;

					default:
						console.log("Unrecognized msg");
				}
			}
		}
	);
</jsp:text>
          </script>
        </h:panelGroup>
      </body>
    </html>
  </f:view>
</jsp:root>
