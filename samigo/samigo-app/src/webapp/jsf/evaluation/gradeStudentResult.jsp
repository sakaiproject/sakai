<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

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
      <title><h:outputText value="#{commonMessages.total_scores}" /></title>

    <script src="/samigo-app/jsf/widget/hideDivision/hideDivision.js"></script>
    <script src="/library/webjars/jquery/1.12.4/jquery.min.js"></script>
    <script src="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
    <script src="/samigo-app/js/jquery.dynamiclist.student.preview.js"></script>
    <script src="/samigo-app/js/selection.student.preview.js"></script>
    <script src="/samigo-app/js/selection.author.preview.js"></script>
    <script type="module" src="/webcomponents/bundles/rubric-association-requirements.js<h:outputText value="#{studentScores.CDNQuery}" />"></script>

    <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.student.css">
    <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.author.css">
    <script>includeWebjarLibrary('awesomplete')</script>
    <script src="/library/js/sakai-reminder.js"></script>
    <script src="/samigo-app/js/finInputValidator.js"></script>
    
    <script>
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

        var sakaiReminder = new SakaiReminder();
        $('textarea.awesomplete').each(function() {
          new Awesomplete(this, {
            list: sakaiReminder.getAll()
          });
        });
        $('#editStudentResults').submit(function(e) {
          $('textarea.awesomplete').each(function() {
            sakaiReminder.new($(this).val());
          });
        });

      });
    </script>

      </head>
  <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- $Id:  -->
<!-- content... -->
<script>
function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}

  function initRubricDialogWrapper(gradingId) {

    initRubricDialog(gradingId
      , <h:outputText value="'#{evaluationMessages.done}'"/>
      , <h:outputText value="'#{evaluationMessages.cancel}'"/>
      , <h:outputText value="'#{evaluationMessages.saverubricgrading}'"/>
      , <h:outputText value="'#{evaluationMessages.unsavedchangesrubric}'"/>);
  }

  $(document).ready(function(){
    // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
    var currentLink = $('#editStudentResults\\:totalScoresMenuLink');
    currentLink.addClass('current');
    // Remove the link of the current option
    currentLink.html(currentLink.find('a').text());
  });
</script>

<div class="portletBody container-fluid">
<h:form id="editStudentResults">
  <h:inputHidden id="publishedIdd" value="#{studentScores.publishedId}" />
  <h:inputHidden id="publishedId" value="#{studentScores.publishedId}" />
  <h:inputHidden id="studentid" value="#{studentScores.studentId}" />
  <h:inputHidden id="studentName" value="#{studentScores.studentName}" />
  <h:inputHidden id="gradingData" value="#{studentScores.assessmentGradingId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h:panelGroup layout="block" styleClass="page-header">
    <div class="b5 d-flex flex-wrap justify-content-between">
      <h1>
        <h:outputText value="#{studentScores.studentName} (#{studentScores.displayId})" rendered="#{totalScores.anonymous eq 'false'}"/>
        <small><h:outputText value="#{evaluationMessages.submission_id}#{deliveryMessages.column} #{studentScores.assessmentGradingId}" rendered="#{totalScores.anonymous eq 'true'}"/></small>
      </h1>
      <%@ include file="/jsf/evaluation/submissionNav.jsp" %>
    </div>
  </h:panelGroup>

  <!-- EVALUATION SUBMENU -->
  <%@ include file="/jsf/evaluation/evaluationSubmenu.jsp" %>

  <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<h2>
  <div styleClass="container">
    <h:outputText value="#{totalScores.assessmentName}" escape="false"/>
    <h:commandButton styleClass="pull-right print_button" id="print" value="#{evaluationMessages.print_report}" immediate="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportAction" />
    </h:commandButton>
  </div>
</h2>

<div class="form-group row">
   <h:outputLabel styleClass="col-md-2" value="#{evaluationMessages.comment_for_student}#{deliveryMessages.column}"/>
   <div class="col-md-6">
     <h:inputTextarea value="#{studentScores.comments}" rows="3" cols="30" styleClass="awesomplete"/>
   </div>
</div>


<h2>
  <h:outputText value="#{deliveryMessages.table_of_contents}" />
</h2>

<h:panelGroup rendered="#{totalScores.isOneSelectionType}">
  <fieldset class="short-summary-box">
    <legend class="summary-title"><h:outputText value="#{evaluationMessages.summary_title}" rendered="true" /></legend>
    <ul>
      <li><h5 style="display: inline;"><h:outputText value="#{evaluationMessages.correct_title}" />: <h:outputText value="#{totalScores.results[studentScores.longAssessmentGradingId][0]}" /></h5></li>
      <li><h5 style="display: inline;"><h:outputText value="#{evaluationMessages.incorrect_title}" />: <h:outputText value="#{totalScores.results[studentScores.longAssessmentGradingId][1]}" /></h5></li>
      <li><h5 style="display: inline;"><h:outputText value="#{evaluationMessages.empty_title}" />: <h:outputText value="#{totalScores.results[studentScores.longAssessmentGradingId][2]}" /></h5></li>
    <ul>
  </fieldset>
</h:panelGroup>

<div class="toc-holder">
  <t:dataList styleClass="part-wrapper" value="#{delivery.tableOfContents.partsContents}" var="part">
    <h:panelGroup styleClass="toc-part">
      <samigo:hideDivision id="part" title=" #{deliveryMessages.p} #{part.number} #{evaluationMessages.dash} #{part.text} #{evaluationMessages.dash}
       #{part.questions-part.unansweredQuestions}#{evaluationMessages.splash}#{part.questions} #{deliveryMessages.ans_q}, #{part.pointsDisplayString} #{evaluationMessages.splash} #{part.roundedMaxPoints} #{deliveryMessages.pt}" > 
        <t:dataList layout="unorderedList" itemStyleClass="list-group-item" styleClass="list-group question-wrapper" value="#{part.itemContents}" var="question">
                <span class="badge rounded-pill text-bg-secondary">
                  <h:outputText escape="false" value="#{commonMessages.cancel_question_cancelled} " rendered="#{question.cancelled}" />
                  <h:outputText escape="false" value="#{question.roundedMaxPoints}">
                    <f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
                  </h:outputText>
                  <h:outputText escape="false" value=" #{deliveryMessages.pt} "/>
                </span>
                <h:outputLink value="##{part.number}#{deliveryMessages.underscore}#{question.number}" styleClass="#{question.cancelled ? 'cancelled-question-link' : ''}">
                  <h:outputText escape="false" value="#{question.sequence}#{deliveryMessages.dot} #{question.strippedText}"/>
                </h:outputLink>
                <h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit==true}" value=" #{deliveryMessages.extra_credit_preview}" />
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

      <h:panelGroup layout="block" styleClass="sak-banner-error" rendered="#{part.noQuestions}">
        <h:outputText value="#{evaluationMessages.no_questions}" escape="false"/>
      </h:panelGroup>

      <t:dataList value="#{part.itemContents}" var="question" itemStyleClass="page-header question-box" styleClass="question-wrapper" layout="unorderedList">
        <h:outputText value="<a name=\"#{part.number}_#{question.number}\"></a>" escape="false" />
          <h:panelGroup layout="block" styleClass="row #{delivery.actionString}">
            <h:panelGroup layout="block" styleClass="col-sm-7">
              <h:panelGroup layout="block" styleClass="row">
                <h:panelGroup layout="block" styleClass="col-sm-12 input-group">
                  <p class="input-group-addon m-0">
                    <h:outputText value="#{deliveryMessages.q} #{question.sequence} #{deliveryMessages.of} " />
                    <h:outputText value="#{part.numbering}#{deliveryMessages.column}  " />
                  </p>
                  <h:inputText styleClass="form-control adjustedScore#{studentScores.assessmentGradingId}.#{question.itemData.itemId}" id="adjustedScore" value="#{question.pointsForEdit}" onchange="toPoint(this.id);" validatorMessage="#{evaluationMessages.number_format_error_adjusted_score}" disabled="#{question.cancelled}">
                    <f:validateDoubleRange/>
                  </h:inputText>
                  <h:panelGroup rendered="#{delivery.trackingQuestions && question.formattedTimeElapsed != ''}">
                    <p class="input-group-addon">
                      <h:outputText value="#{evaluationMessages.time_elapsed}: #{question.formattedTimeElapsed}" />
                    </p>
                  </h:panelGroup>
                </h:panelGroup>
                <h:panelGroup layout="block" styleClass="col-sm-12 input-group">
                  <p class="samigo-input-group-addon">
                    <h:outputText value=" #{deliveryMessages.splash} #{question.roundedMaxPointsToDisplay} " />
                    <h:outputText value="#{deliveryMessages.pt}" />
                    <h:message for="adjustedScore" styleClass="sak-banner-error" />
                    <h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit == true}" value=" #{deliveryMessages.extra_credit_preview}" />
                  </p>
                </h:panelGroup>
              </h:panelGroup>
            </h:panelGroup>
          </h:panelGroup>

      <h:panelGroup rendered="#{question.hasAssociatedRubric}">
        <ul class="nav nav-tabs">
          <li class="nav-item active">
            <a class="nav-link" data-bs-toggle="tab" href="<h:outputText value="#submition#{question.itemData.itemId}" />">
              <h:outputText value="#{commonMessages.student_response}" />
            </a>
          </li>
          <li class="nav-item">
            <h:panelGroup rendered="#{question.associatedRubricType == '1'}" >
              <a class="nav-link" data-bs-toggle="tab" href="<h:outputText value="#rubric#{question.itemData.itemId}" />">
                <h:outputText value="#{assessmentSettingsMessages.grading_rubric}" />
              </a>
            </h:panelGroup>
            <h:panelGroup rendered="#{question.associatedRubricType == '2'}" >
              <h:outputLink title="#{evaluationMessages.saverubricgrading}"
                    styleClass="nav-link"
                    value="#"
                    onclick="initRubricDialogWrapper(#{studentScores.assessmentGradingId}+'.'+#{question.itemData.itemId}); return false;"
                    onkeypress="initRubricDialogWrapper(#{studentScores.assessmentGradingId}+'.'+#{question.itemData.itemId}); return false;" >
                <h:outputText value="#{assessmentSettingsMessages.grading_rubric}" />
              </h:outputLink>
            </h:panelGroup>
          </li>
        </ul>

        <div class="tab-content">
          <div id="<h:outputText value="submition#{question.itemData.itemId}" />" class="tab-pane active" role="tabpanel">
      </h:panelGroup>
          <h:panelGroup styleClass="samigo-question-callout#{question.cancelled ? ' samigo-question-cancelled' : ''}" layout="block">
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
            <h:panelGroup styleClass="sak-banner-info" rendered="#{question.cancelled}" layout="block">
              <h:outputText value="#{commonMessages.cancel_question_info_cancelled_question}"/>
            </h:panelGroup>
          </h:panelGroup>

        <h:panelGroup rendered="#{question.hasAssociatedRubric}">
          </div>
          <div id="<h:outputText value="rubric#{question.itemData.itemId}" />" class="tab-pane" role="tabpanel">
            <h:panelGroup rendered="#{question.associatedRubricType == '1'}" >
              <sakai-rubric-grading
                id='<h:outputText value="pub.#{totalScores.publishedId}.#{question.itemData.itemId}"/>'
                tool-id="sakai.samigo"
                enable-pdf-export="true"
                site-id='<h:outputText value="#{totalScores.siteId}"/>'
                entity-id='<h:outputText value="pub.#{totalScores.publishedId}.#{question.itemData.itemId}"/>'
                evaluated-item-id='<h:outputText value="#{studentScores.assessmentGradingId}.#{question.itemData.itemId}" />'
                evaluated-item-owner-id='<h:outputText value="#{studentScores.studentId}"/>'
              >
              </sakai-rubric-grading>
            </h:panelGroup>
            <h:panelGroup rendered="#{question.associatedRubricType == '2'}" >
              <div id='<h:outputText value="#{studentScores.assessmentGradingId}"/>-inputs'></div>
              <div id='<h:outputText value="modal#{studentScores.assessmentGradingId}.#{question.itemData.itemId}" />' style="display:none;overflow:initial">
                <sakai-dynamic-rubric
                  id='<h:outputText value="#{studentScores.assessmentGradingId}.#{question.itemData.itemId}-pub.#{totalScores.publishedId}.#{question.itemData.itemId}.#{studentScores.assessmentGradingId}"/>'
                  grading-id='<h:outputText value="#{studentScores.assessmentGradingId}.#{question.itemData.itemId}"/>'
                  entity-id='<h:outputText value="pub.#{totalScores.publishedId}.#{question.itemData.itemId}"/>'
                  site-id='<h:outputText value="#{totalScores.siteId}"/>'
                  evaluated-item-owner-id='<h:outputText value="#{studentScores.studentId}" />'
                  previous-grade='<h:outputText value="#{question.pointsForEdit}"/>'
                  origin='gradeStudentResult'>
                </sakai-dynamic-rubric>
              </div>
            </h:panelGroup>
          </div>
          </div>
        </h:panelGroup>

          <div class="tier2">
          <h:panelGrid columns="2" border="0" >
            <h:outputLabel value="#{evaluationMessages.comment_for_student}#{deliveryMessages.column}"/>
            <h:outputText value="&#160;" escape="false" />
            <h:inputTextarea value="#{question.gradingComment}" rows="4" cols="40" styleClass="awesomplete"/>
            <%@ include file="/jsf/evaluation/gradeStudentResultAttachment.jsp" %>
          </h:panelGrid>
          </div>
      </t:dataList>
  </t:dataList>
</div>

<h:panelGroup rendered="#{totalScores.anonymous eq 'false' && studentScores.email != null && studentScores.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}">
  <h:outputText value="<a href=\"mailto:" escape="false" />
  <h:outputText value="#{studentScores.email}" />
  <h:outputText value="?subject=" escape="false" />
  <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
  <h:outputText value="  #{evaluationMessages.email} " escape="false"/>
  <h:outputText value="#{studentScores.firstName}" />
  <h:outputText value="</a>" escape="false" />
</h:panelGroup>

<p class="act">
   <h:commandButton id="save" styleClass="active" value="#{evaluationMessages.save_cont}" action="gradeStudentResult" type="submit">
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
   </h:commandButton>
   <h:commandButton id="cancel" value="#{commonMessages.cancel_action}" action="totalScores" immediate="true">
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
