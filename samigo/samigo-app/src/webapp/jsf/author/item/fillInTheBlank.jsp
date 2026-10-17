<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:t="http://myfaces.apache.org/tomahawk">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
          <h:outputText value="#{authorMessages.item_display_author}"/>
        </title>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/authoring.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script type="text/javascript">
          <jsp:text><![CDATA[
          var defining_answers;
          var mutually_exclusive;
          var last_markers;
          $(function () {
              defining_answers = $("#defining_answers").html();
              mutually_exclusive = $("#mutually_exclusive").html();
              const markers = $("#itemForm\\:newmarkers").val();
              if (markers == "{}") {
                  $("#itemForm\\:customMarker\\:0").prop('checked', true);
                  $("#customMarkerSettings").hide();
              } else {
                  $("#itemForm\\:customMarker\\:1").prop('checked', true);
                  checkMarkers();
                  $("#customMarkerSettings").show();
              }
              $("input[name='itemForm\\:customMarker']").change(function () {
                  markerRadio();
              });
              $("#newmarkers").change(function () {
                  checkMarkers();
              });
          });

          function safe_tags(str) {
              return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
          }

          function markerRadio() {
              const markerState = $("input[name='itemForm\\:customMarker']:checked").val();
              if (markerState == "true") {
                  if (last_markers) {
                      $("#itemForm\\:newmarkers").val(last_markers);
                      checkMarkers();
                  }
                  $("#customMarkerSettings").slideDown();
              } else {
                  $("#customMarkerSettings").slideUp();
                  last_markers = $("#itemForm\\:newmarkers").val();
                  $("#itemForm\\:newmarkers").val("{}");
                  checkMarkers();
              }
          }

          function checkMarkers() {
              var markerPair = $("#itemForm\\:newmarkers").val();
              if (markerPair != '{}') {
                  $("#customMarkerSettings").show();
              }
              const markerEl = document.getElementById('itemForm:newmarkers');
              const pool_badmarkers_error_3 = ]]></jsp:text>
          <h:outputText value="'#{authorMessages.pool_badmarkers_error_3}'" escape="false"/>
          <jsp:text>;
              const pool_badmarkers_error_2 = </jsp:text>
          <h:outputText value="'#{authorMessages.pool_badmarkers_error_2}'" escape="false"/>
          <jsp:text>;
              const pool_badmarkers_error_1 = </jsp:text>
          <h:outputText value="'#{authorMessages.pool_badmarkers_error_1}'" escape="false"/>
          <jsp:text><![CDATA[;

              if (markerPair.match(/[\"\'.,&<>\ |*]/)) {
                  setError(markerEl, pool_badmarkers_error_3);
                  return;
              }
              if (markerPair.charAt(0) == markerPair.charAt(1)) {
                  setError(markerEl, pool_badmarkers_error_2);
                  return;
              }
              if (markerPair.length == 1 || markerPair.length > 2) {
                  setError(markerEl, pool_badmarkers_error_1);
                  return;
              }
              $("#defining_answers").html(defining_answers.replace(/{/g, safe_tags(markerPair.charAt(0))).replace(/}/g, safe_tags(markerPair.charAt(1))));
              $("#mutually_exclusive").html(mutually_exclusive.replace(/{/g, safe_tags(markerPair.charAt(0))).replace(/}/g, safe_tags(markerPair.charAt(1))));

              removeError(markerEl);
          }

          function setError(el, msg) {
              el.parentNode.querySelector('#validationForbiddenCharacters').innerHTML = "Error:";
              el.parentNode.classList.remove('has-success');
              el.parentNode.classList.add('has-error');
              el.parentNode.querySelector('label').innerHTML = msg;
          }

          function removeError (el) {
              el.parentNode.querySelector('#validationForbiddenCharacters').innerHTML = "";
              el.parentNode.classList.remove('has-error');
              el.parentNode.classList.add('has-success');
              el.parentNode.querySelector('label').innerHTML = "";
          }

        ]]></jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <!-- content...  -->
            <!-- FORM  -->
            <!-- HEADING  -->
            <jsp:directive.include file="/jsf/author/item/itemHeadings.jsp"/>
            <!-- warning for editing FIB questions. SAM-2334  -->
            <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="sak-banner-warn">
              <h:panelGrid columns="1">
                <h:outputText value="#{authorMessages.edit_fib_warning}"/>
              </h:panelGrid>
            </h:panelGroup>
            <h:form id="itemForm">
              <f:verbatim>
                <input type="hidden" id="ckeditor-autosave-context" name="ckeditor-autosave-context" value="samigo_edit_fillInTheBlank"/>
              </f:verbatim>
              <h:panelGroup rendered="#{itemauthor.currentItem.itemId!=null}">
                <f:verbatim>
                  <jsp:text><![CDATA[<input type="hidden" id="ckeditor-autosave-entity-id" name="ckeditor-autosave-entity-id" value="]]></jsp:text>
                </f:verbatim>
                <h:outputText value="#{itemauthor.currentItem.itemId}"/>
                <f:verbatim>
                  <jsp:text><![CDATA["/>]]></jsp:text>
                </f:verbatim>
              </h:panelGroup>
              <p class="act">
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                </h:commandButton>
              </p>
              <!-- QUESTION PROPERTIES  -->
              <!-- 1 POINTS  -->
              <div class="form-group row">
                <h:outputLabel for="answerptr" value="#{authorMessages.answer_point_value}" styleClass="col-md-4 col-lg-2 form-label"/>
                <div class="col-md-2">
                  <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" required="true" disabled="#{author.isEditPoolFlow}" styleClass="form-control ConvertPoint">
                    <f:validateDoubleRange minimum="0.00"/>
                  </h:inputText>
                  <h:message for="answerptr" styleClass="validate"/>
                </div>
              </div>
              <div class="form-group row">
                <h:outputLabel for="itemScore" value="#{authorMessages.answer_point_value_display}" styleClass="col-md-4 col-lg-2 form-label"/>
                <div class="col-md-5 samigo-inline-radio">
                  <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" id="itemScore">
                    <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}"/>
                    <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}"/>
                  </h:selectOneRadio>
                </div>
              </div>
              <!-- 1.2 Min POINTS  -->
              <f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">
                <div class="form-group row">
                  <h:outputLabel for="answerminptr" value="#{authorMessages.answer_min_point_value}" styleClass="col-md-4 col-lg-2 form-label"/>
                  <div class="col-md-2">
                    <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" styleClass="form-control ConvertPoint">
                      <f:validateDoubleRange/>
                    </h:inputText>
                    <h:outputText value="#{authorMessages.answer_min_point_info}"/>
                    <h:message for="answerminptr" styleClass="validate"/>
                  </div>
                </div>
              </f:subview>
              <!-- Extra Credit  -->
              <jsp:directive.include file="/jsf/author/inc/extraCreditSetting.jspf"/>
              <div class="form-group row">
                <h:outputLabel value="#{authorMessages.fib_marker}" styleClass="col-md-4 col-lg-2 form-label"/>
                <div class="col-md-2 samigo-inline-radio">
                  <h:selectOneRadio value="customMarker" id="customMarker">
                    <f:selectItem itemValue="false" itemLabel="#{authorMessages.fib_marker_default}" id="default"/>
                    <f:selectItem itemValue="true" itemLabel="#{authorMessages.fib_marker_custom}" id="custom"/>
                  </h:selectOneRadio>
                  <h:outputText value="#{authorMessages.fib_note_5}"/>
                  <h:message for="customMarker" styleClass="validate"/>
                </div>
              </div>
              <div class="form-group row" id="customMarkerSettings" style="display: none;">
                <h:outputLabel value="#{authorMessages.fib_label_custom_markers}" styleClass="col-md-4 col-lg-2 form-label"/>
                <div class="col-md-6">
                  <div class="form-group">
                    <h:inputText id="newmarkers" style="width: 50px;" value="#{itemauthor.currentItem.markersPair}" required="false" styleClass="form-control" maxlength="2" onchange="checkMarkers()">
                      <f:validateLength maximum="2" minimum="2"/>
                      <!--<p:passThroughAttribute name="aria-describedby" value="validationForbiddenCharacters" /> -->
                    </h:inputText>
                    <jsp:element name="label">
                      <jsp:attribute name="for" trim="false">newmarkers</jsp:attribute>
                      <jsp:attribute name="id" trim="false">validationForbiddenCharacters</jsp:attribute>
                      <jsp:attribute name="class" trim="false">help-block</jsp:attribute>
                      <jsp:attribute name="escape" trim="false">false</jsp:attribute>
                      <jsp:body>
                      </jsp:body>
                    </jsp:element>
                  </div>
                  <h:outputText value="#{authorMessages.fib_note_4}&lt;br /&gt;" escape="false"/>
                  <h:message for="newmarkers" styleClass="validate"/>
                </div>
              </div>
              <!-- 2 QUESTION TEXT  -->
              <div id="defining_answers">
                <h:outputText value="#{authorMessages.defining_answers}&lt;br/&gt;" escape="false"/>
                <h:outputText value="#{authorMessages.fib_note_1}&lt;br /&gt;&lt;br /&gt;" escape="false"/>
                <h:outputText value="#{authorMessages.fib_note_2}&lt;br /&gt;&lt;br /&gt;" escape="false"/>
                <h:outputText value="#{authorMessages.fib_note_3}&lt;br /&gt;&lt;br /&gt;" escape="false"/>
              </div>
              <div class="mathjax-warning" style="display: none;">
                <h:outputText value="#{authorMessages.accepted_characters}" escape="false"/>
                <div class="sak-banner-warn">
                  <h:outputText value="#{authorMessages.mathjax_usage_warning}" escape="false"/>
                </div>
              </div>
              <h:outputLabel for="questionItemText_textinput" value="#{authorMessages.q_text}"/>
              <br/>
              <h:panelGrid>
                <samigo:wysiwyg identity="questionItemText" rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
                  <f:validateLength maximum="60000"/>
                </samigo:wysiwyg>
              </h:panelGrid>
              <br/>
              <div>
                <div class="samigo-checkbox">
                  <h:selectBooleanCheckbox id="sensitive" value="#{itemauthor.currentItem.caseSensitiveForFib}">
                  </h:selectBooleanCheckbox>
                  <h:outputLabel for="sensitive" value="#{authorMessages.case_sensitive}" escape="false"/>
                </div>
                <p>
                  <h:outputText value="#{authorMessages.case_sensitive_note}" escape="false"/>
                  <br/>
                  <h:outputText value="#{authorMessages.case_sensitive_example}" escape="false"/>
                </p>
              </div>
              <div id="mutually_exclusive">
                <div class="samigo-checkbox">
                  <h:selectBooleanCheckbox id="exclusive" value="#{itemauthor.currentItem.mutuallyExclusiveForFib}">
                  </h:selectBooleanCheckbox>
                  <h:outputLabel for="exclusive" value="#{authorMessages.mutually_exclusive}" escape="false"/>
                </div>
                <p>
                  <h:outputText value="#{authorMessages.mutually_exclusive_note}" escape="false"/>
                  <br/>
                  <h:outputText value="#{authorMessages.mutually_exclusive_example}" escape="false"/>
                </p>
              </div>
              <div>
                <div class="samigo-checkbox">
                  <h:selectBooleanCheckbox id="spaces" value="#{itemauthor.currentItem.ignoreSpacesForFib}">
                  </h:selectBooleanCheckbox>
                  <h:outputLabel for="spaces" value="#{authorMessages.ignore_spaces}" escape="false"/>
                </div>
                <p>
                  <h:outputText value="#{authorMessages.ignore_spaces_note}" escape="false"/>
                  <br/>
                  <h:outputText value="#{authorMessages.ignore_spaces_example}" escape="false"/>
                </p>
              </div>
              <!-- 2a ATTACHMENTS  -->
              <jsp:directive.include file="/jsf/author/item/attachment.jsp"/>
              <!-- 2b TIMED  -->
              <jsp:directive.include file="/jsf/author/item/timed.jsp"/>
              <!-- 3 PART  -->
              <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment'  &amp;&amp; !author.isEditPoolFlow}">
                <h:outputLabel for="assignToPart" value="#{authorMessages.assign_to_p}" styleClass="col-md-4 col-lg-2 form-label"/>
                <div class="col-md-8">
                  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                    <f:selectItems value="#{itemauthor.sectionSelectList}"/>
                    <!-- use this in real  value="#{section.sectionNumberList}"  -->
                  </h:selectOneMenu>
                </div>
              </h:panelGroup>
              <!-- 5 POOL  -->
              <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' &amp;&amp; author.isEditPendingAssessmentFlow}">
                <h:outputLabel for="assignToPool" value="#{authorMessages.assign_to_question_p}" styleClass="col-md-4 col-lg-2 form-label"/>
                <div class="col-md-8">
                  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
                    <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}"/>
                    <f:selectItems value="#{itemauthor.poolSelectList}"/>
                  </h:selectOneMenu>
                </div>
              </h:panelGroup>
              <!-- FEEDBACK  -->
              <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' &amp;&amp; (author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'))}">
                <div class="form-group row">
                  <h2>
                    <h:outputText value="#{authorMessages.correct_incorrect_an}" styleClass="col-md-12 form-label"/>
                  </h2>
                </div>
                <div class="form-group row">
                  <h:outputLabel for="questionFeedbackCorrect_textinput" value="#{authorMessages.correct_answer_opti}" styleClass="form-label"/>
                  <!-- WYSIWYG  -->
                  <div class="col-md-8">
                    <h:panelGrid>
                      <samigo:wysiwyg identity="questionFeedbackCorrect" rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                      </samigo:wysiwyg>
                    </h:panelGrid>
                  </div>
                </div>
                <div class="form-group row">
                  <h:outputLabel for="questionFeedbackIncorrect_textinput" value="#{authorMessages.incorrect_answer_op}" styleClass="form-label"/>
                  <!-- WYSIWYG  -->
                  <div class="col-md-8">
                    <h:panelGrid>
                      <samigo:wysiwyg identity="questionFeedbackIncorrect" rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                      </samigo:wysiwyg>
                    </h:panelGrid>
                  </div>
                </div>
              </h:panelGroup>
              <!-- METADATA  -->
              <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}">
                <h:outputLabel value="Metadata"/>
                <br/>
                <div class="form-group row">
                  <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-4 col-lg-2 form-label"/>
                  <div class="col-md-5">
                    <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
                  </div>
                </div>
                <div class="form-group row">
                  <h:outputLabel for="keyword" value="#{authorMessages.keyword}" styleClass="col-md-4 col-lg-2 form-label"/>
                  <div class="col-md-5">
                    <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
                  </div>
                </div>
                <div class="form-group row">
                  <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" styleClass="col-md-4 col-lg-2 form-label"/>
                  <div class="col-md-5">
                    <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
                  </div>
                </div>
              </h:panelGroup>
              <jsp:directive.include file="/jsf/author/item/tags.jsp"/>
              <!-- BUTTONS  -->
              <p class="act">
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                </h:commandButton>
              </p>
            </h:form>
            <!-- end content  -->
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
