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
  <jsp:text>&#13;
&#13;
&#13;
&#13;
&#13;
&#13;<![CDATA[
<!DOCTYPE html]]>&#13;
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"&#13;<![CDATA[
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">]]>&#13;
</jsp:text>
  <!--
* $Id:  $

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
  <!-- currentItem- ItemBean  -->
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
        <script>
          <jsp:text>&#13;
      	function textCounter(field,cntfield,maxlimit) {&#13;
          	&#13;<![CDATA[
      		if (field.value.length > maxlimit) // if too long...trim it!]]>&#13;
      		field.value = field.value.substring(0, maxlimit);&#13;
     		 // otherwise, update 'characters left' counter&#13;
      		else&#13;
      			cntfield.value = maxlimit - field.value.length;&#13;
      	}&#13;
      	</jsp:text>
        </script>
        <style type="text/css">
          <jsp:text>&#13;
      .panelGridColumn td {&#13;
    	  text-align: right;&#13;
      }&#13;
      </jsp:text>
        </style>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false">countNum();<jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <!-- content...  -->
            <!-- FORM  -->
            <!-- HEADING  -->
            <jsp:directive.include file="/jsf/author/item/itemHeadings.jsp"/>
            <h:form id="itemForm">
              <f:verbatim>
                <input type="hidden" id="ckeditor-autosave-context" name="ckeditor-autosave-context" value="samigo_edit_matrixChoicesSurvey"/>
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
              <div>
                <!-- 1 POINTS  -->
                <div class="mb-3 row">
                  <h:outputLabel for="answerptr" value="#{authorMessages.answer_point_value}" styleClass="col-md-4 col-lg-2 form-label"/>
                  <div class="col-md-2">
                    <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" required="true" disabled="#{author.isEditPoolFlow}" size="6" styleClass="form-control ConvertPoint">
                      <f:validateDoubleRange/>
                    </h:inputText>
                    <h:message for="answerptr" styleClass="validate"/>
                  </div>
                </div>
                <div class="mb-3 row">
                  <h:outputLabel for="itemScore" value="#{authorMessages.answer_point_value_display}" styleClass="col-md-4 col-lg-2 form-label"/>
                  <div class="col-md-2 samigo-inline-radio">
                    <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" id="itemScore">
                      <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}"/>
                      <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}"/>
                    </h:selectOneRadio>
                  </div>
                </div>
                <!-- Extra Credit  -->
                <jsp:directive.include file="/jsf/author/inc/extraCreditSetting.jspf"/>
                <!-- 2 TEXT  -->
                <h:outputLabel for="questionItemText_textinput" value="#{authorMessages.q_text}" styleClass="form-label"/>
                <div class="mb-3 row">
                  <!-- WYSIWYG  -->
                  <div class="col-md-8">
                    <h:panelGrid>
                      <samigo:wysiwyg identity="questionItemText" rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="4000"/>
                      </samigo:wysiwyg>
                    </h:panelGrid>
                  </div>
                </div>
                <!-- 2a ATTACHMENTS  -->
                <jsp:directive.include file="/jsf/author/item/attachment.jsp"/>
                <!-- 3 ANSWER  -->
                <div class="tier2">
                  <div class="mb-3 row">
                    <h:outputLabel value="#{authorMessages.answer} " styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-6">
                      <h:graphicImage alt="#{evaluationMessages.alt_matrixChoicesSurvey}" url="/images/matrixChoicesSurvey.gif"/>
                    </div>
                  </div>
                  <div class="mb-3 row">
                    <h:outputLabel for="rowData" value="#{authorMessages.rowchoices}" styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-8">
                      <h:inputTextarea id="rowData" value="#{itemauthor.currentItem.rowChoices}" rows="6" cols="54"/>
                    </div>
                  </div>
                  <div class="mb-3 row">
                    <h:outputLabel for="columnData" value="#{authorMessages.columnchoices}" styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-8">
                      <h:inputTextarea id="columnData" value="#{itemauthor.currentItem.columnChoices}" rows="6" cols="54" immediate="true"/>
                    </div>
                  </div>
                  <div class="samigo-checkbox">
                    <h:selectBooleanCheckbox id="forceRankingCheckbox" value="#{itemauthor.currentItem.forceRanking}"/>
                    <h:outputLabel for="forceRankingCheckbox" value="#{authorMessages.forceRanking}"/>
                  </div>
                  <div class="samigo-checkbox">
                    <h:selectBooleanCheckbox id="addCommentCheckbox" immediate="true" value="#{itemauthor.currentItem.addComment}" onchange="this.form.submit();" valueChangeListener="#{itemauthor.currentItem.toggleAddComment}"/>
                    <h:outputLabel for="addCommentCheckbox" value="#{authorMessages.addComment}"/>
                  </div>
                  <h:panelGroup styleClass="mb-3 row" layout="block" rendered="#{itemauthor.currentItem.addComment}">
                    <h:outputLabel for="commentField" value="#{authorMessages.commentField}" styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-8">
                      <h:panelGrid columns="1">
                        <h:inputTextarea id="commentField" value="#{itemauthor.currentItem.commentField}" rows="6" cols="48" onkeydown="javascript:textCounter(document.getElementById('itemForm:commentField'),document.getElementById('itemForm:remLen2'), 200);" onkeyup="javascript:textCounter(document.getElementById('itemForm:commentField'),document.getElementById('itemForm:remLen2'), 200);" onclick="javascript:if(this.value=='#{authorMessages.character_limit}') this.value='';"/>
                        <h:panelGroup>
                          <h:outputLabel value="#{authorMessages.character_count}" styleClass="alert-info"/>
                          <h:outputText value=" "/>
                          <h:inputText readonly="true" id="remLen2" size="2" maxlength="3" value="#{200 - itemauthor.currentItem.commentFieldLenght}"/>
                        </h:panelGroup>
                      </h:panelGrid>
                    </div>
                  </h:panelGroup>
                  <br/>
                  <div class="mb-3 row">
                    <h:outputLabel for="relativeWidth" value="#{authorMessages.relativeWidthOfColumns}" styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-8">
                      <h:selectOneMenu id="relativeWidth" value="#{itemauthor.currentItem.selectedRelativeWidth}">
                        <f:selectItems value="#{itemauthor.selectRelativeWidthList}"/>
                      </h:selectOneMenu>
                    </div>
                  </div>
                </div>
                <!-- 3a TIMED  -->
                <jsp:directive.include file="/jsf/author/item/timed.jsp"/>
                <!-- 4 PART  -->
                <h:panelGroup styleClass="mb-3 row" layout="block" rendered="#{itemauthor.target == 'assessment' &amp;&amp; !author.isEditPoolFlow}">
                  <h:outputLabel for="assignToPart" value="#{authorMessages.assign_to_p}" styleClass="col-md-4 col-lg-2 form-label"/>
                  <div class="col-md-8">
                    <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                      <f:selectItems value="#{itemauthor.sectionSelectList}"/>
                      <!-- use this in real  value="#{section.sectionNumberList}"  -->
                    </h:selectOneMenu>
                  </div>
                </h:panelGroup>
                <!-- 5 POOL  -->
                <h:panelGroup styleClass="mb-3 row" layout="block" rendered="#{itemauthor.target == 'assessment' &amp;&amp; author.isEditPendingAssessmentFlow}">
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
                  <div class="mb-3 row">
                    <h:outputLabel for="questionFeedbackGeneral_textinput" value="#{commonMessages.feedback_optional}" styleClass="form-label"/>
                    <div class="col-md-8">
                      <!-- WYSIWYG  -->
                      <h:panelGrid>
                        <samigo:wysiwyg identity="questionFeedbackGeneral" rows="140" value="#{itemauthor.currentItem.generalFeedback}" hasToggle="yes" mode="author">
                          <f:validateLength maximum="60000"/>
                        </samigo:wysiwyg>
                      </h:panelGrid>
                    </div>
                  </div>
                </h:panelGroup>
                <!-- METADATA  -->
                <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
                  <h:outputLabel value="Metadata"/>
                  <br/>
                  <div class="mb-3 row">
                    <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-5 col-lg-3 ">
                      <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
                    </div>
                  </div>
                  <div class="mb-3 row">
                    <h:outputLabel for="keyword" value="#{authorMessages.keyword}" styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-5 col-lg-3">
                      <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
                    </div>
                  </div>
                  <div class="mb-3 row">
                    <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" styleClass="col-md-4 col-lg-2 form-label"/>
                    <div class="col-md-5 col-lg-3">
                      <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
                    </div>
                  </div>
                </h:panelGroup>
                <jsp:directive.include file="/jsf/author/item/tags.jsp"/>
              </div>
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
