<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:t="http://myfaces.apache.org/tomahawk"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text><![CDATA[






<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <!--
$Id$

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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{commonMessages.total_scores}"/>
        </title>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/jsf/widget/hideDivision/hideDivision.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/webjars/jquery/1.12.4/jquery.min.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/jquery.dynamiclist.student.preview.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/selection.student.preview.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/selection.author.preview.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:text><![CDATA[
    <script type="module" src="/webcomponents/bundles/rubric-association-requirements.js]]></jsp:text>
        <h:outputText value="#{studentScores.CDNQuery}"/>
        <jsp:text><![CDATA["></script>

    ]]></jsp:text>
        <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.student.css"/>
        <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.author.css"/>
        <script>
          <jsp:text>includeWebjarLibrary('awesomplete')</jsp:text>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/js/sakai-reminder.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:text>
      var finFormatError = '</jsp:text>
          <h:outputText value="#{deliveryMessages.fin_invalid_characters_error}" escape="false"/>
          <jsp:text>';
    </jsp:text>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/finInputValidator.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:text>
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
    </jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- $Id:   -->
          <!-- content...  -->
          <script>
            <jsp:text>
function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}

  function initRubricDialogWrapper(gradingId) {

    initRubricDialog(gradingId
      , </jsp:text>
            <h:outputText value="'#{evaluationMessages.done}'"/>
            <jsp:text>
      , </jsp:text>
            <h:outputText value="'#{evaluationMessages.cancel}'"/>
            <jsp:text>
      , </jsp:text>
            <h:outputText value="'#{evaluationMessages.saverubricgrading}'"/>
            <jsp:text>
      , </jsp:text>
            <h:outputText value="'#{evaluationMessages.unsavedchangesrubric}'"/>
            <jsp:text>);
  }

  $(document).ready(function(){
    // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
    var currentLink = $('#editStudentResults\\:totalScoresMenuLink');
    currentLink.addClass('current');
    // Remove the link of the current option
    currentLink.html(currentLink.find('a').text());
  });
</jsp:text>
          </script>
          <div class="portletBody container-fluid">
            <h:form id="editStudentResults">
              <h:inputHidden id="publishedIdd" value="#{studentScores.publishedId}"/>
              <h:inputHidden id="publishedId" value="#{studentScores.publishedId}"/>
              <h:inputHidden id="studentid" value="#{studentScores.studentId}"/>
              <h:inputHidden id="studentName" value="#{studentScores.studentName}"/>
              <h:inputHidden id="gradingData" value="#{studentScores.assessmentGradingId}"/>
              <h:inputHidden id="itemId" value="#{totalScores.firstItem}"/>
              <!-- HEADINGS  -->
              <jsp:directive.include file="/jsf/evaluation/evaluationHeadings.jsp"/>
              <h:panelGroup layout="block" styleClass="page-header">
                <div class="b5 d-flex flex-wrap justify-content-between">
                  <h1>
                    <h:outputText value="#{studentScores.studentName} (#{studentScores.displayId})" rendered="#{totalScores.anonymous eq 'false'}"/>
                    <small>
                      <h:outputText value="#{evaluationMessages.submission_id}#{deliveryMessages.column} #{studentScores.assessmentGradingId}" rendered="#{totalScores.anonymous eq 'true'}"/>
                    </small>
                  </h1>
                  <jsp:directive.include file="/jsf/evaluation/submissionNav.jsp"/>
                </div>
              </h:panelGroup>
              <!-- EVALUATION SUBMENU  -->
              <jsp:directive.include file="/jsf/evaluation/evaluationSubmenu.jsp"/>
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <h2>
                <div styleClass="container">
                  <h:outputText value="#{totalScores.assessmentName}" escape="false"/>
                  <h:commandButton styleClass="pull-right print_button" id="print" value="#{evaluationMessages.print_report}" immediate="true">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportAction"/>
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
                <h:outputText value="#{deliveryMessages.table_of_contents}"/>
              </h2>
              <h:panelGroup rendered="#{totalScores.isOneSelectionType}">
                <fieldset class="short-summary-box">
                  <legend class="summary-title">
                    <h:outputText value="#{evaluationMessages.summary_title}" rendered="true"/>
                  </legend>
                  <jsp:text><![CDATA[
    <ul>
      ]]></jsp:text>
                  <li>
                    <h5 style="display: inline;">
                      <h:outputText value="#{evaluationMessages.correct_title}"/>
                      <jsp:text>: </jsp:text>
                      <h:outputText value="#{totalScores.results[studentScores.longAssessmentGradingId][0]}"/>
                    </h5>
                  </li>
                  <li>
                    <h5 style="display: inline;">
                      <h:outputText value="#{evaluationMessages.incorrect_title}"/>
                      <jsp:text>: </jsp:text>
                      <h:outputText value="#{totalScores.results[studentScores.longAssessmentGradingId][1]}"/>
                    </h5>
                  </li>
                  <li>
                    <h5 style="display: inline;">
                      <h:outputText value="#{evaluationMessages.empty_title}"/>
                      <jsp:text>: </jsp:text>
                      <h:outputText value="#{totalScores.results[studentScores.longAssessmentGradingId][2]}"/>
                    </h5>
                  </li>
                  <jsp:text><![CDATA[
    <ul>
  ]]></jsp:text>
                </fieldset>
              </h:panelGroup>
              <div class="toc-holder">
                <t:dataList styleClass="part-wrapper" value="#{delivery.tableOfContents.partsContents}" var="part">
                  <h:panelGroup styleClass="toc-part">
                    <samigo:hideDivision id="part" title=" #{deliveryMessages.p} #{part.number} #{evaluationMessages.dash} #{part.text} #{evaluationMessages.dash}&#10;       #{part.questions-part.unansweredQuestions}#{evaluationMessages.splash}#{part.questions} #{deliveryMessages.ans_q}, #{part.pointsDisplayString} #{evaluationMessages.splash} #{part.roundedMaxPoints} #{deliveryMessages.pt}">
                      <t:dataList layout="unorderedList" itemStyleClass="list-group-item" styleClass="list-group question-wrapper" value="#{part.itemContents}" var="question">
                        <span class="badge rounded-pill text-bg-secondary">
                          <h:outputText escape="false" value="#{commonMessages.cancel_question_cancelled} " rendered="#{question.cancelled}"/>
                          <h:outputText escape="false" value="#{question.roundedMaxPoints}">
                            <f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
                          </h:outputText>
                          <h:outputText escape="false" value=" #{deliveryMessages.pt} "/>
                        </span>
                        <h:outputLink value="##{part.number}#{deliveryMessages.underscore}#{question.number}" styleClass="#{question.cancelled ? 'cancelled-question-link' : ''}">
                          <h:outputText escape="false" value="#{question.sequence}#{deliveryMessages.dot} #{question.strippedText}"/>
                        </h:outputLink>
                        <h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit==true}" value=" #{deliveryMessages.extra_credit_preview}"/>
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
                      <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}"/>
                      <small class="part-text">
                        <h:outputText value="#{part.text}" escape="false" rendered="#{part.numParts ne '1'}"/>
                      </small>
                    </h4>
                  </div>
                  <h:panelGroup layout="block" styleClass="sak-banner-error" rendered="#{part.noQuestions}">
                    <h:outputText value="#{evaluationMessages.no_questions}" escape="false"/>
                  </h:panelGroup>
                  <t:dataList value="#{part.itemContents}" var="question" itemStyleClass="page-header question-box" styleClass="question-wrapper" layout="unorderedList">
                    <h:outputText value="&lt;a name=&quot;#{part.number}_#{question.number}&quot;&gt;&lt;/a&gt;" escape="false"/>
                    <h:panelGroup layout="block" styleClass="row #{delivery.actionString}">
                      <h:panelGroup layout="block" styleClass="col-sm-7">
                        <h:panelGroup layout="block" styleClass="row">
                          <h:panelGroup layout="block" styleClass="col-sm-12 input-group">
                            <p class="input-group-addon m-0">
                              <h:outputText value="#{deliveryMessages.q} #{question.sequence} #{deliveryMessages.of} "/>
                              <h:outputText value="#{part.numbering}#{deliveryMessages.column}  "/>
                            </p>
                            <h:inputText styleClass="form-control #{delivery.trackingQuestions &amp;&amp; question.formattedTimeElapsed ? '' : 'form-control-center'} adjustedScore#{studentScores.assessmentGradingId}.#{question.itemData.itemId}" id="adjustedScore" value="#{question.pointsForEdit}" onchange="toPoint(this.id);" validatorMessage="#{evaluationMessages.number_format_error_adjusted_score}" disabled="#{question.cancelled}">
                              <f:validateDoubleRange/>
                            </h:inputText>
                            <p class="input-group-addon">
                              <h:outputText value=" #{deliveryMessages.splash} #{question.roundedMaxPointsToDisplay} "/>
                              <h:outputText value="#{deliveryMessages.pt}"/>
                              <h:message for="adjustedScore" styleClass="sak-banner-error"/>
                              <h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit == true}" value=" #{deliveryMessages.extra_credit_preview}"/>
                              <h:panelGroup styleClass="d-none d-sm-inline small" rendered="#{question.itemData.minScore &gt; 0 || question.itemData.discount &gt; 0}">
                                <h:outputText value=", ("/>
                                <h:outputText value="#{authorMessages.answer_min_point_short}: #{question.itemData.minScore}" rendered="#{question.itemData.minScore &gt; 0}"/>
                                <h:outputText value=", " rendered="#{question.itemData.minScore &gt; 0 &amp;&amp; question.itemData.discount &gt; 0}"/>
                                <h:outputText value="#{authorMessages.answer_discount_point_short}: #{question.itemData.discount}" rendered="#{question.itemData.discount &gt; 0}"/>
                                <h:outputText value=")"/>
                              </h:panelGroup>
                            </p>
                            <h:panelGroup rendered="#{delivery.trackingQuestions &amp;&amp; question.formattedTimeElapsed != ''}">
                              <p class="input-group-addon input-group-addon-right">
                                <h:outputText value="#{evaluationMessages.time_elapsed}: #{question.formattedTimeElapsed}"/>
                              </p>
                            </h:panelGroup>
                          </h:panelGroup>
                        </h:panelGroup>
                      </h:panelGroup>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.hasAssociatedRubric}">
                      <ul class="nav nav-tabs">
                        <li class="nav-item active">
                          <jsp:text><![CDATA[
            <a class="nav-link active" data-bs-toggle="tab" href="]]></jsp:text>
                          <h:outputText value="#submition#{question.itemData.itemId}"/>
                          <jsp:text><![CDATA[">
              ]]></jsp:text>
                          <h:outputText value="#{commonMessages.student_response}"/>
                          <jsp:text><![CDATA[
            </a>
          ]]></jsp:text>
                        </li>
                        <li class="nav-item">
                          <h:panelGroup rendered="#{question.associatedRubricType == '1'}">
                            <jsp:text><![CDATA[
              <a class="nav-link" data-bs-toggle="tab" href="]]></jsp:text>
                            <h:outputText value="#rubric#{question.itemData.itemId}"/>
                            <jsp:text><![CDATA[">
                ]]></jsp:text>
                            <h:outputText value="#{assessmentSettingsMessages.grading_rubric}"/>
                            <jsp:text><![CDATA[
              </a>
            ]]></jsp:text>
                          </h:panelGroup>
                          <h:panelGroup rendered="#{question.associatedRubricType == '2'}">
                            <h:outputLink title="#{evaluationMessages.saverubricgrading}" styleClass="nav-link" value="#" onclick="initRubricDialogWrapper(#{studentScores.assessmentGradingId}+'.'+#{question.itemData.itemId}); return false;" onkeypress="initRubricDialogWrapper(#{studentScores.assessmentGradingId}+'.'+#{question.itemData.itemId}); return false;">
                              <h:outputText value="#{assessmentSettingsMessages.grading_rubric}"/>
                            </h:outputLink>
                          </h:panelGroup>
                        </li>
                      </ul>
                      <jsp:text><![CDATA[

        <div class="tab-content">
          <div id="]]></jsp:text>
                      <h:outputText value="submition#{question.itemData.itemId}"/>
                      <jsp:text><![CDATA[" class="tab-pane active" role="tabpanel">
      ]]></jsp:text>
                    </h:panelGroup>
                    <h:panelGroup styleClass="samigo-question-callout#{question.cancelled ? ' samigo-question-cancelled' : ''}" layout="block">
                      <h:panelGroup rendered="#{question.itemData.typeId == 7}">
                        <f:subview id="deliverAudioRecording">
                          <jsp:directive.include file="/jsf/evaluation/item/displayAudioRecording.jsp"/>
                        </f:subview>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{question.itemData.typeId == 6}">
                        <f:subview id="deliverFileUpload">
                          <jsp:directive.include file="/jsf/evaluation/item/displayFileUpload.jsp"/>
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
                      <h:panelGroup rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 12 || question.itemData.typeId == 3}">
                        <f:subview id="deliverMultipleChoiceSingleCorrect">
                          <jsp:directive.include file="/jsf/delivery/item/deliverMultipleChoiceSingleCorrect.jsp"/>
                        </f:subview>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{question.itemData.typeId == 2}">
                        <f:subview id="deliverMultipleChoiceMultipleCorrect">
                          <jsp:directive.include file="/jsf/delivery/item/deliverMultipleChoiceMultipleCorrect.jsp"/>
                        </f:subview>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{question.itemData.typeId == 5}">
                        <f:subview id="deliverShortAnswer">
                          <jsp:directive.include file="/jsf/delivery/item/deliverShortAnswerLink.jsp"/>
                        </f:subview>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{question.itemData.typeId == 4}">
                        <f:subview id="deliverTrueFalse">
                          <jsp:directive.include file="/jsf/delivery/item/deliverTrueFalse.jsp"/>
                        </f:subview>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{question.itemData.typeId == 14}">
                        <f:subview id="deliverExtendedMatchingItems">
                          <jsp:directive.include file="/jsf/delivery/item/deliverExtendedMatchingItems.jsp"/>
                        </f:subview>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{question.itemData.typeId == 13}">
                        <f:subview id="deliverMatrixChoicesSurvey">
                          <jsp:directive.include file="/jsf/delivery/item/deliverMatrixChoicesSurvey.jsp"/>
                        </f:subview>
                      </h:panelGroup>
                      <h:panelGroup styleClass="sak-banner-info" rendered="#{question.cancelled}" layout="block">
                        <h:outputText value="#{commonMessages.cancel_question_info_cancelled_question}"/>
                      </h:panelGroup>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.hasAssociatedRubric}">
                      <jsp:text><![CDATA[
          </div>
          <div id="]]></jsp:text>
                      <h:outputText value="rubric#{question.itemData.itemId}"/>
                      <jsp:text><![CDATA[" class="tab-pane" role="tabpanel">
            ]]></jsp:text>
                      <h:panelGroup rendered="#{question.associatedRubricType == '1'}">
                        <jsp:text><![CDATA[
              <sakai-rubric-grading
                id=']]></jsp:text>
                        <h:outputText value="pub.#{totalScores.publishedId}.#{question.itemData.itemId}"/>
                        <jsp:text>'
                tool-id="sakai.samigo"
                enable-pdf-export
                site-id='</jsp:text>
                        <h:outputText value="#{totalScores.siteId}"/>
                        <jsp:text>'
                entity-id='</jsp:text>
                        <h:outputText value="pub.#{totalScores.publishedId}.#{question.itemData.itemId}"/>
                        <jsp:text>'
                evaluated-item-id='</jsp:text>
                        <h:outputText value="#{studentScores.assessmentGradingId}.#{question.itemData.itemId}"/>
                        <jsp:text>'
                evaluated-item-owner-id='</jsp:text>
                        <h:outputText value="#{studentScores.studentId}"/>
                        <jsp:text><![CDATA['
              >
              </sakai-rubric-grading>
            ]]></jsp:text>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{question.associatedRubricType == '2'}">
                        <jsp:text><![CDATA[
              <div id=']]></jsp:text>
                        <h:outputText value="#{studentScores.assessmentGradingId}"/>
                        <jsp:text><![CDATA[-inputs'></div>
              <div id=']]></jsp:text>
                        <h:outputText value="modal#{studentScores.assessmentGradingId}.#{question.itemData.itemId}"/>
                        <jsp:text><![CDATA[' style="display:none;overflow:initial">
                <sakai-dynamic-rubric
                  id=']]></jsp:text>
                        <h:outputText value="#{studentScores.assessmentGradingId}.#{question.itemData.itemId}-pub.#{totalScores.publishedId}.#{question.itemData.itemId}.#{studentScores.assessmentGradingId}"/>
                        <jsp:text>'
                  grading-id='</jsp:text>
                        <h:outputText value="#{studentScores.assessmentGradingId}.#{question.itemData.itemId}"/>
                        <jsp:text>'
                  entity-id='</jsp:text>
                        <h:outputText value="pub.#{totalScores.publishedId}.#{question.itemData.itemId}"/>
                        <jsp:text>'
                  site-id='</jsp:text>
                        <h:outputText value="#{totalScores.siteId}"/>
                        <jsp:text>'
                  evaluated-item-owner-id='</jsp:text>
                        <h:outputText value="#{studentScores.studentId}"/>
                        <jsp:text>'
                  previous-grade='</jsp:text>
                        <h:outputText value="#{question.pointsForEdit}"/>
                        <jsp:text><![CDATA['
                  origin='gradeStudentResult'>
                </sakai-dynamic-rubric>
              </div>
            ]]></jsp:text>
                      </h:panelGroup>
                      <jsp:text><![CDATA[
          </div>
          </div>
        ]]></jsp:text>
                    </h:panelGroup>
                    <div class="tier2">
                      <h:panelGrid columns="2" border="0">
                        <h:outputLabel value="#{evaluationMessages.comment_for_student}#{deliveryMessages.column}"/>
                        <h:outputText value="&amp;#160;" escape="false"/>
                        <h:inputTextarea value="#{question.gradingComment}" rows="4" cols="40" styleClass="awesomplete"/>
                        <jsp:directive.include file="/jsf/evaluation/gradeStudentResultAttachment.jsp"/>
                      </h:panelGrid>
                    </div>
                  </t:dataList>
                </t:dataList>
              </div>
              <h:panelGroup rendered="#{totalScores.anonymous eq 'false' &amp;&amp; studentScores.email != null &amp;&amp; studentScores.email != ''}">
                <h:outputText value="&lt;a href=&quot;mailto:" escape="false"/>
                <h:outputText value="#{studentScores.email}"/>
                <h:outputText value="?subject=" escape="false"/>
                <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}&quot;&gt;" escape="false"/>
                <h:outputText value="  #{evaluationMessages.email} " escape="false"/>
                <h:outputText value="#{studentScores.firstName}"/>
                <h:outputText value="&lt;/a&gt;" escape="false"/>
              </h:panelGroup>
              <p class="act">
                <h:commandButton id="save" styleClass="active" value="#{evaluationMessages.save_cont}" action="gradeStudentResult" type="submit">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreUpdateListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener"/>
                </h:commandButton>
                <h:commandButton id="cancel" value="#{commonMessages.cancel_action}" action="totalScores" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener"/>
                </h:commandButton>
              </p>
            </h:form>
          </div>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
