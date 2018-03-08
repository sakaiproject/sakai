<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
$Id$
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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <style type="text/css">
        .TableColumn {
          text-align: center
        }
        .TableClass {
          border-style: dotted;
          border-width: 0.5px;
          border-color: light grey;
        }
      </style>
      <title><h:outputText value="#{commonMessages.total_scores}" /></title>
    <samigo:script path="/jsf/widget/hideDivision/hideDivision.js" />
    
		<samigo:script path="/../library/webjars/jquery/1.12.4/jquery.min.js"/>
		<samigo:script path="/js/jquery.dynamiclist.student.preview.js"/>
		<samigo:script path="/js/selection.student.preview.js"/>
		<samigo:script path="/js/selection.author.preview.js"/>

		<samigo:stylesheet path="/css/imageQuestion.student.css"/>
		<samigo:stylesheet path="/css/imageQuestion.author.css"/>
		
		<script type="text/JavaScript">		
			jQuery(window).load(function(){
				
				$('div[id^=sectionImageMap_]').each(function(){
					var myregexp = /sectionImageMap_(\d+_\d+)/
					var matches = myregexp.exec(this.id);
					var sequence = matches[1];
					var serializedImageMapId = $(this).find('input:hidden[id$=serializedImageMap]').attr('id').replace(/:/g, '\\:');
					
					var dynamicList = new DynamicList(serializedImageMapId, 'imageMapTemplate_'+sequence, 'pointerClass', 'imageMapContainer_'+sequence);
					dynamicList.fillElements();
					
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
		</script>
		
      </head>
  <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- $Id:  -->
<!-- content... -->
<script type="text/javascript">
function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}
</script>

<div class="portletBody container-fluid">
<h:form id="editStudentResults">
  <h:inputHidden id="publishedIdd" value="#{studentScores.publishedId}" />
  <h:inputHidden id="publishedId" value="#{studentScores.publishedId}" />
  <h:inputHidden id="studentid" value="#{studentScores.studentId}" />
  <h:inputHidden id="studentName" value="#{studentScores.studentName}" />
  <h:inputHidden id="gradingData" value="#{studentScores.assessmentGradingId}" />
  <h:inputHidden id="itemId" value="#{studentScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <div class="page-header">
    <h1>
      <h:outputText value="#{studentScores.studentName}" rendered="#{totalScores.anonymous eq 'false'}"/>
      <h:outputText value="#{evaluationMessages.submission_id}#{deliveryMessages.column} #{studentScores.assessmentGradingId}" rendered="#{totalScores.anonymous eq 'true'}"/>
    </h1>
  </div>

  <h:outputText value="<ul class='navIntraTool actionToolbar' role='menu'>" escape="false"/>
   <h:outputText value="<li role='menuitem'><span>" escape="false"/>
    <h:commandLink title="#{evaluationMessages.t_submissionStatus}" action="submissionStatus" immediate="true">
      <h:outputText value="#{evaluationMessages.sub_status}" />
      <f:param name="allSubmissions" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
    </h:commandLink>
    <h:outputText value="</span><li role='menuitem'><span>" escape="false"/>
    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{commonMessages.total_scores}" />
    </h:commandLink>
    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.firstItem ne ''}" />
    <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.q_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
    </h:commandLink>
    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.firstItem ne ''}" /> 
    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>
   <h:outputText value="</span></li></ul>" escape="false"/>

  <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<h2>
  <h:outputText value="#{totalScores.assessmentName}" escape="false"/>
</h2>

<div class="form-group row">
   <h:outputLabel styleClass="col-md-2" value="#{evaluationMessages.comment_for_student}#{deliveryMessages.column}"/>
   <div class="col-md-6">
     <h:inputTextarea value="#{studentScores.comments}" rows="3" cols="30"/>
   </div>
</div>

<h2>
  <h:outputText value="#{deliveryMessages.table_of_contents}" />
</h2>

<div class="toc-holder">
  <t:dataList styleClass="part-wrapper" value="#{delivery.tableOfContents.partsContents}" var="part">
    <h:panelGroup styleClass="toc-part">
      <samigo:hideDivision id="part" title=" #{deliveryMessages.p} #{part.number} #{evaluationMessages.dash} #{part.text} #{evaluationMessages.dash}
       #{part.questions-part.unansweredQuestions}#{evaluationMessages.splash}#{part.questions} #{deliveryMessages.ans_q}, #{part.pointsDisplayString} #{part.roundedMaxPoints} #{deliveryMessages.pt}" > 
        <t:dataList layout="unorderedList" itemStyleClass="list-group-item" styleClass="list-group question-wrapper" value="#{part.itemContents}" var="question">
                <span class="badge">
                  <h:outputText escape="false" value="#{question.roundedMaxPoints}">
                    <f:convertNumber maxFractionDigits="2"/>
                  </h:outputText>
                  <h:outputText escape="false" value=" #{deliveryMessages.pt} "/>
                </span>
                <h:outputLink value="##{part.number}#{deliveryMessages.underscore}#{question.number}"> 
                  <h:outputText escape="false" value="#{question.number}#{deliveryMessages.dot} #{question.strippedText}"/>
                </h:outputLink>
        </t:dataList>
      </samigo:hideDivision>
     </h:panelGroup>
  </t:dataList>
</div>

<br/>
<div class="tier2">
  <t:dataList value="#{delivery.pageContents.partsContents}" var="part" styleClass="table">
      <div class="page-header">
        <h4 class="part-header">
          <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}" />
          <small class="part-text">
            <h:outputText value="#{part.text}" escape="false" rendered="#{part.numParts ne '1'}" />
          </small>
        </h4>
      </div>

      <h:panelGroup layout="block" styleClass="bs-callout-error" rendered="#{part.noQuestions}">
        <h:outputText value="#{evaluationMessages.no_questions}" escape="false"/>
      </h:panelGroup>

      <t:dataList value="#{part.itemContents}" var="question" itemStyleClass="page-header question-box" styleClass="question-wrapper" layout="unorderedList">
        <h:outputText value="<a name=\"#{part.number}_#{question.number}\"></a>" escape="false" />
        <h:panelGroup layout="block" styleClass="input-group col-sm-6">
            <span class="input-group-addon">
              <h:outputText value="#{deliveryMessages.q} #{question.number} #{deliveryMessages.of} " />
              <h:outputText value="#{part.questions}#{deliveryMessages.column}  " />
            </span>
            <h:inputText styleClass="form-control" id="adjustedScore" value="#{question.pointsForEdit}" onchange="toPoint(this.id);" >
              <f:validateDoubleRange/>
            </h:inputText>
            <span class="input-group-addon">
            <h:outputText value=" #{deliveryMessages.splash} #{question.roundedMaxPointsToDisplay} " />
            <h:outputText value="#{deliveryMessages.pt}"/>
            </span>
            <h:message for="adjustedScore" style="color:red"/>
        </h:panelGroup>

          <div class="samigo-question-callout">
            <h:panelGroup rendered="#{question.itemData.typeId == 7}">
              <f:subview id="deliverAudioRecording">
               <%@ include file="/jsf/evaluation/item/displayAudioRecording.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 6}">
              <f:subview id="deliverFileUpload">
                <%@ include file="/jsf/evaluation/item/displayFileUpload.jsp" %>
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
              rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 12 || question.itemData.typeId == 3}">
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
                <%@ include file="/jsf/delivery/item/deliverShortAnswerLink.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 4}">
              <f:subview id="deliverTrueFalse">
                <%@ include file="/jsf/delivery/item/deliverTrueFalse.jsp" %>
              </f:subview>
            </h:panelGroup>
            
            <h:panelGroup rendered="#{question.itemData.typeId == 14}">
              <f:subview id="deliverExtendedMatchingItems">
                <%@ include file="/jsf/delivery/item/deliverExtendedMatchingItems.jsp" %>
              </f:subview>
            </h:panelGroup>
            
            <h:panelGroup rendered="#{question.itemData.typeId == 13}">
              <f:subview id="deliverMatrixChoicesSurvey">
                <%@ include file="/jsf/delivery/item/deliverMatrixChoicesSurvey.jsp" %>
              </f:subview>
            </h:panelGroup>
          </div>

          <div class="tier2">
          <h:panelGrid columns="2" border="0" >
            <h:outputLabel value="#{evaluationMessages.comment_for_student}#{deliveryMessages.column}"/>
            <h:outputText value="&#160;" escape="false" />
            <h:inputTextarea value="#{question.gradingComment}" rows="4" cols="40"/>
    	    <%@ include file="/jsf/evaluation/gradeStudentResultAttachment.jsp" %>
          </h:panelGrid>
          </div>
      </t:dataList>
  </t:dataList>
</div>

<h:panelGroup rendered="#{totalScores.anonymous eq 'false' && studentScores.email != null && studentScores.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}">
  <h:outputText value="<a href=\"mailto:" escape="false" />
  <h:outputText value="#{studentScores.email}" escape="false" />
  <h:outputText value="?subject=" escape="false" />
  <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
  <h:outputText value="  #{evaluationMessages.email} #{studentScores.firstName}" escape="false"/>
  <h:outputText value="</a>" escape="false" />
</h:panelGroup>

<p class="act">
   <h:commandButton styleClass="active" value="#{evaluationMessages.save_cont}" action="totalScores" type="submit">
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
   </h:commandButton>
   <h:commandButton value="#{commonMessages.cancel_action}" action="totalScores" immediate="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
   </h:commandButton>
</p>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
