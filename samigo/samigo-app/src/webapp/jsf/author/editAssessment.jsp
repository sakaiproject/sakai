<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk"
    xmlns:corejsf="http://java.sun.com/upload">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
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
          <h:outputText value="#{authorMessages.create_modify_a}"/>
        </title>
        <jsp:element name="script">
          <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
          <jsp:attribute name="src" trim="false">/samigo-app/js/authoring.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
          <jsp:attribute name="src" trim="false">/samigo-app/js/authoringQuestionCancellation.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script type="text/JavaScript">
          <jsp:directive.include file="/js/samigotree.js"/>
          <jsp:text>

$(window).load( function() {
  // No need for an insert question box after every single question!
  $('div.part-insert-question:not(:last)').hide();
});
</jsp:text>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
          <jsp:attribute name="src" trim="false">/library/webjars/jquery/1.12.4/jquery.min.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
          <jsp:attribute name="src" trim="false">/samigo-app/js/selection.author.preview.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
          <jsp:attribute name="src" trim="false">/library/js/spinner.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.author.css"/>
        <script type="text/JavaScript">
          <jsp:text>	
	jQuery(window).load(function(){
			
		$('input:hidden[id*=hiddenSerializedCoords_]').each(function(){
			var myregexp = /hiddenSerializedCoords_(\d+_\d+)_(\d+)/
			var matches = myregexp.exec(this.id);
			var sequence = matches[1];
			var label = matches[2];
			
			var sel = new selectionAuthor({selectionClass: 'selectiondiv', textClass: 'textContainer'}, 'imageMapContainer_'+sequence);
			try {
				sel.setCoords(jQuery.parseJSON(this.value));
				sel.setText(label);
			}catch(err){}
			
		});	
	});
</jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false">document.forms[0].reset(); disableIt(); resetSelectMenus(); ;<jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <!-- content...  -->
            <!-- some back end stuff stubbed  -->
            <h:form id="assessmentForm">
              <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="sak-banner-warn">
                <h:panelGrid columns="1">
                  <h:outputText value="#{authorMessages.edit_published_assessment_warn_1}"/>
                  <h:outputText value="#{authorMessages.edit_published_assessment_warn_21}" rendered="#{assessmentBean.hasGradingData}"/>
                  <h:outputText value="#{authorMessages.edit_published_assessment_warn_22}" rendered="#{!assessmentBean.hasGradingData}"/>
                </h:panelGrid>
              </h:panelGroup>
              <h:inputHidden id="assessmentId" value="#{assessmentBean.assessmentId}"/>
              <h:inputHidden id="showCompleteAssessment" value="#{author.showCompleteAssessment}"/>
              <h:inputHidden id="title" value="#{assessmentBean.title}"/>
              <!-- NOTE!
     add JavaScript to handle events that effect a part or question and
     set the value of these when a particular part or question is affected
     and the "current section" or "current part" needs to be changed
     other alternative maybe value changed listener
 -->
              <h:inputHidden id="SectionIdent" value="#{author.currentSection}"/>
              <h:inputHidden id="ItemIdent" value="#{author.currentItem}"/>
              <h:inputHidden id="fixedQuestionIds" value="#{sectionBean.fixedQuestionIds}"/>
              <!-- HEADINGS  -->
              <jsp:directive.include file="/jsf/author/editAssessmentHeadings.jsp"/>
              <div class="navView">
                <h1>
                  <h:outputText value="#{authorMessages.qs}#{authorMessages.column} #{assessmentBean.title}" escape="false"/>
                  <h:outputText value=" #{authorMessages.dash} #{author.editPoolSectionName}" escape="false" rendered="#{author.isEditPoolFlow}"/>
                </h1>
              </div>
              <div class="navList">
                <h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_qs} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize &gt; 1}"/>
                <h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_q} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize == 1}"/>
                <h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_qs} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize == 0}"/>
                <h:outputText value="#{assessmentBean.totalScore}">
                  <f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
                </h:outputText>
                <h:outputText value="&amp;#160;" escape="false"/>
                <h:outputText value="#{authorMessages.total_pts}" rendered="#{assessmentBean.totalScore &gt; 1}"/>
                <h:outputText value="#{authorMessages.total_pt}" rendered="#{assessmentBean.totalScore == 1}"/>
                <h:outputText value="#{authorMessages.total_pts}" rendered="#{assessmentBean.totalScore == 0}"/>
              </div>
              <p class="navViewAction">
                <h:commandLink title="#{authorMessages.t_addPart}" id="addPart" action="editPart" immediate="true" rendered="#{author.isEditPendingAssessmentFlow}">
                  <h:outputText value="#{authorMessages.subnav_add_part}"/>
                  <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorPartListener"/>
                </h:commandLink>
                <h:outputText value=" #{authorMessages.separator} " rendered="#{assessmentBean.showPrintLink eq 'true'}"/>
                <h:commandLink title="#{commonMessages.action_preview}" action="beginAssessment" rendered="#{assessmentBean.assessmentId &gt; 0}">
                  <h:outputText value="#{commonMessages.action_preview}"/>
                  <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
                  <f:param name="publishedId" value="#{author.editPublishedAssessmentID}"/>
                  <f:param name="actionString" value="previewAssessment"/>
                  <f:param name="fromEdit" value="true"/>
                  <f:param name="isFromPrint" value="false"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener"/>
                </h:commandLink>
                <h:outputText value=" #{authorMessages.separator} " rendered="#{assessmentBean.showPrintLink eq 'true'}"/>
                <h:commandLink action="#{itemauthor.print}" rendered="#{assessmentBean.showPrintLink eq 'true' &amp;&amp; assessmentBean.showPrintAssessment ne 'true'}">
                  <h:outputText value="#{authorMessages.subnav_print}" escape="false"/>
                </h:commandLink>
                <h:commandLink action="#{pdfAssessment.prepPDF}" rendered="#{assessmentBean.showPrintLink eq 'true' &amp;&amp; assessmentBean.showPrintAssessment eq 'true'}">
                  <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
                  <f:param name="publishedId" value="#{author.editPublishedAssessmentID}"/>
                  <f:param name="actionString" value="editAssessment"/>
                  <f:param name="isFromPrint" value="true"/>
                  <h:outputText value="#{printMessages.print}" escape="false"/>
                </h:commandLink>
                <h:outputText value=" #{authorMessages.separator} "/>
                <h:commandLink title="#{authorMessages.t_settings}" id="editAssessmentSettings_editAssessment" action="editAssessmentSettings" immediate="true" rendered="#{author.isEditPendingAssessmentFlow &amp;&amp; assessmentBean.assessmentId &gt; 0}">
                  <h:outputText value="#{commonMessages.settings_action}"/>
                  <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorSettingsListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener"/>
                </h:commandLink>
                <h:commandLink title="#{authorFrontDoorMessages.t_editSettings}" id="editPublishedAssessmentSettings_editAssessment" immediate="true" rendered="#{!author.isEditPendingAssessmentFlow}" action="#{author.getOutcome}">
                  <h:outputText value="#{commonMessages.settings_action}"/>
                  <f:param name="publishedId" value="#{assessmentBean.assessmentId}"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener"/>
                </h:commandLink>
                <h:outputText value=" #{authorMessages.separator} " rendered="#{author.isEditPendingAssessmentFlow &amp;&amp; assessmentBean.questionSize &gt; 0}"/>
                <h:commandLink title="#{commonMessages.publish_action}" id="editAssessmentSettings_editAssessment2" action="#{assessmentSettings.getOutcomePublish}" immediate="true" rendered="#{author.isEditPendingAssessmentFlow &amp;&amp; assessmentBean.questionSize &gt; 0}">
                  <h:outputText value="#{commonMessages.publish_action}"/>
                  <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmPublishAssessmentListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.PublishAssessmentListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener"/>
                </h:commandLink>
              </p>
              <h:panelGrid columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow &amp;&amp; (assessmentBean.sections[0].sectionAuthorType!= null &amp;&amp; assessmentBean.sections[0].sectionAuthorTypeString == '2')}">
                <h:outputFormat value="#{authorMessages.edit_published_assessment_warn_edit_pool_questions}">
                  <f:param value="#{author.editPoolName}"/>
                </h:outputFormat>
              </h:panelGrid>
              <h:panelGrid columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow &amp;&amp; (assessmentBean.sections[0].sectionAuthorType!= null &amp;&amp; assessmentBean.sections[0].sectionAuthorTypeString == '3')}">
                <h:outputFormat value="#{authorMessages.edit_published_assessment_warn_edit_pool_fixed_questions}">
                  <f:param value="#{author.editPoolNameFixed}"/>
                  <f:param value="#{author.editPoolName}"/>
                </h:outputFormat>
              </h:panelGrid>
              <h:panelGrid columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow &amp;&amp; (assessmentBean.sections[0].sectionAuthorType!= null &amp;&amp; assessmentBean.sections[0].sectionAuthorTypeString == '4')}">
                <h:outputText value="#{authorMessages.edit_published_assessment_warn_edit_multiple_pools}"/>
              </h:panelGrid>
              <h:panelGrid columns="2" width="100%" columnClasses="shortText,navList">
                <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}"/>
                <h:panelGroup>
                  <h:commandButton id="republish" value="#{authorMessages.button_republish}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow}" action="#{author.getOutcome}" onclick="SPNR.disableControlsAndSpin(this, null);">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener"/>
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener"/>
                  </h:commandButton>
                  <h:commandButton id="republishRegrade" value="#{authorMessages.button_republish_and_regrade}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow &amp;&amp; assessmentBean.hasGradingData}" action="#{author.getOutcome}" onclick="SPNR.disableControlsAndSpin(this, null);">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener"/>
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener"/>
                  </h:commandButton>
                </h:panelGroup>
                <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}"/>
                <h:panelGroup>
                  <h:commandButton value="#{authorMessages.button_return_to_main}" id="returnToMain" rendered="#{author.isEditPoolFlow}">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedQuestionPoolPartListener"/>
                  </h:commandButton>
                </h:panelGroup>
              </h:panelGrid>
              <h:panelGrid columns="1" width="100%" columnClasses="navList" border="0" rendered="#{!author.isEditPendingAssessmentFlow &amp;&amp; assessmentBean.hasGradingData}">
                <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublish','width=400,height=400,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublishPopUp','width=400,height=400,scrollbars=yes, resizable=yes');">
                  <h:outputText value=" #{assessmentSettingsMessages.whats_this_link}"/>
                </h:outputLink>
              </h:panelGrid>
              <h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="" styleClass="d-none">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener"/>
              </h:commandLink>
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <div class="tier1">
                <h:dataTable id="parts" width="100%" value="#{assessmentBean.sections}" var="partBean">
                  <!-- note that partBean is ui/delivery/SectionContentsBean not ui/author/SectionBean  -->
                  <h:column>
                    <h:panelGrid styleClass="table table-striped" columns="2" width="100%" columnClasses="navView,navList" border="0">
                      <h:panelGroup rendered="#{!author.isEditPoolFlow}">
                        <h:outputLabel for="number" value="#{authorMessages.p}"/>
                        <h:outputText value="&amp;#160;" escape="false"/>
                        <h:selectOneMenu id="number" value="#{partBean.number}" onchange="enableOrderUpdate()" rendered="#{!author.isEditPoolFlow}">
                          <f:selectItems value="#{assessmentBean.partNumbers}"/>
                        </h:selectOneMenu>
                        <h:outputText value="#{partBean.number}: " rendered="#{!author.isEditPendingAssessmentFlow}"/>
                        <h:outputText value="&amp;#160;" escape="false"/>
                        <h:panelGroup rendered="#{!author.isEditPoolFlow}">
                          <h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') &amp;&amp; partBean.questions &gt; 1}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.questions_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') &amp;&amp; partBean.questions == 1}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.question_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') &amp;&amp; partBean.questions == 0}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.questions_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3') &amp;&amp; partBean.numberToBeFixedString &gt; 1}" value="#{authorMessages.fixed_draw_type} #{partBean.poolNameToBeFixed} - #{partBean.numberToBeFixedString} #{authorMessages.questions_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3') &amp;&amp; partBean.numberToBeFixedString == 1}" value="#{authorMessages.fixed_draw_type} #{partBean.poolNameToBeFixed} - #{partBean.numberToBeFixedString} #{authorMessages.question_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3') &amp;&amp; partBean.numberToBeDrawnString &gt;= 1}" value=" | " escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3') &amp;&amp; partBean.numberToBeDrawnString &gt; 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.questions_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3') &amp;&amp; partBean.numberToBeDrawnString == 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.question_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; (partBean.sectionAuthorTypeString == '2' || partBean.sectionAuthorTypeString == '4')) &amp;&amp; partBean.numberToBeDrawnString &gt; 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.questions_lower_case}" escape="false"/>
                          <h:outputText rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; (partBean.sectionAuthorTypeString == '2' || partBean.sectionAuthorTypeString == '4')) &amp;&amp; partBean.numberToBeDrawnString == 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.question_lower_case}" escape="false"/>
                          <h:outputText escape="false" rendered="#{partBean.timedSection}" value=" &lt;i title='#{authorMessages.timed}' class='fa fa-clock-o'&gt;&lt;/i&gt;"/>
                          <h:commandButton value="#{authorMessages.random_update_questions}" type="submit" id="randomQuestions" action="editAssessment" style="margin-left: 2em" rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; (partBean.sectionAuthorTypeString == '2' || partBean.sectionAuthorTypeString == '3' || partBean.sectionAuthorTypeString == '4')  &amp;&amp; author.isEditPendingAssessmentFlow)}" onclick="SPNR.disableControlsAndSpin( this, null );document.getElementById('assessmentForm:randomQuestionsSectionId').value='#{partBean.sectionId}';">
                            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.UpdateRandomPoolQuestionsListener"/>
                          </h:commandButton>
                          <h:outputText value="&amp;#160;" escape="false"/>
                          <h:commandButton value="#{authorMessages.button_edit_questions}" id="editQuestionPoolQuestions" rendered="#{(partBean.sectionAuthorType!= null &amp;&amp; (partBean.sectionAuthorTypeString == '2' || partBean.sectionAuthorTypeString == '3' || partBean.sectionAuthorTypeString == '4') &amp;&amp; !author.isEditPendingAssessmentFlow)}">
                            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedQuestionPoolPartListener"/>
                          </h:commandButton>
                        </h:panelGroup>
                      </h:panelGroup>
                      <h:panelGroup rendered="#{!author.isEditPoolFlow}">
                        <h:commandLink title="#{authorMessages.copy_to_pool}" id="copyToPool" immediate="true" action="#{questionpool.startCopyFromAssessment}" rendered="#{author.isEditPendingAssessmentFlow}">
                          <h:outputText value="#{authorMessages.copy_to_pool}" rendered="#{partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '1'}"/>
                          <f:param name="sectionId" value="#{partBean.sectionId}"/>
                        </h:commandLink>
                        <h:outputText value=" #{authorMessages.separator} " rendered="#{partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '1' &amp;&amp; author.isEditPendingAssessmentFlow}"/>
                        <h:commandLink title="#{authorMessages.t_removeP}" action="confirmRemovePart" immediate="true" rendered="#{partBean.number ne 1 &amp;&amp; author.isEditPendingAssessmentFlow}">
                          <h:outputText value="#{commonMessages.remove_action}"/>
                          <!-- use this to set the sectionBean.sectionId in ConfirmRemovePartListener  -->
                          <f:param name="sectionId" value="#{partBean.sectionId}"/>
                          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemovePartListener"/>
                        </h:commandLink>
                        <h:outputText value=" #{authorMessages.separator} " rendered="#{partBean.number ne 1 &amp;&amp; author.isEditPendingAssessmentFlow}"/>
                        <h:commandLink title="#{authorMessages.t_editP}" id="editPart" immediate="true" action="editPart" onclick="SPNR.insertSpinnerInPreallocated( null, null, 'assessmentForm:parts:#{partBean.number - 1}:editPartSpinner' );">
                          <h:outputText value="#{commonMessages.edit_action}"/>
                          <f:param name="sectionId" value="#{partBean.sectionId}"/>
                          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPartListener"/>
                        </h:commandLink>
                        <t:div id="editPartSpinner" styleClass="allocatedSpinPlaceholder"/>
                      </h:panelGroup>
                    </h:panelGrid>
                    <h:outputText escape="false" value="#{partBean.description}"/>
                    <div class="question-row">
                      <!-- PART ATTACHMENTS  -->
                      <jsp:directive.include file="/jsf/author/part_attachment.jsp"/>
                      <h:outputText rendered="#{partBean.sectionAuthorType!= null &amp;&amp; (partBean.sectionAuthorTypeString == '2' || partBean.sectionAuthorTypeString == '4') &amp;&amp; empty partBean.randomQuestionsDrawDate}" value="#{authorMessages.random_draw_msg_no_date}"/>
                      <h:outputFormat rendered="#{partBean.sectionAuthorType!= null &amp;&amp; (partBean.sectionAuthorTypeString == '2' || partBean.sectionAuthorTypeString == '4') &amp;&amp; !empty partBean.randomQuestionsDrawDate &amp;&amp; author.isEditPendingAssessmentFlow}" value="#{authorMessages.random_draw_msg}" escape="false">
                        <f:param value="#{partBean.poolNameToBeDrawn}"/>
                        <f:param value="#{partBean.randomQuestionsOwnerDisplayName}"/>
                        <f:param value="#{partBean.randomQuestionsDrawDate}"/>
                        <f:param value="#{partBean.randomQuestionsDrawTime}"/>
                      </h:outputFormat>
                      <h:outputFormat rendered="#{partBean.sectionAuthorType!= null &amp;&amp; (partBean.sectionAuthorTypeString == '2' || partBean.sectionAuthorTypeString == '4') &amp;&amp; !author.isEditPoolFlow &amp;&amp; !empty partBean.randomQuestionsDrawDate &amp;&amp; !author.isEditPendingAssessmentFlow}" value="#{authorMessages.random_draw_msg_published}" escape="false">
                        <f:param value="#{partBean.poolNameToBeDrawn}"/>
                        <f:param value="#{partBean.randomQuestionsOwnerDisplayName}"/>
                        <f:param value="#{partBean.randomQuestionsDrawDate}"/>
                        <f:param value="#{partBean.randomQuestionsDrawTime}"/>
                      </h:outputFormat>
                      <h:outputText rendered="#{partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3' &amp;&amp; empty partBean.randomQuestionsDrawDate &amp;&amp; empty partBean.fixedQuestionsDrawDate}" value="#{authorMessages.fixed_and_random_draw_msg_no_date}"/>
                      <h:outputFormat rendered="#{partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3' &amp;&amp; !empty partBean.randomQuestionsDrawDate &amp;&amp; !empty partBean.fixedQuestionsDrawDate &amp;&amp; author.isEditPendingAssessmentFlow}" value="#{authorMessages.fixed_and_random_draw_msg}" escape="false">
                        <f:param value="#{partBean.poolNameToBeFixed}"/>
                        <f:param value="#{partBean.fixedQuestionsOwnerDisplayName}"/>
                        <f:param value="#{partBean.fixedQuestionsDrawDate}"/>
                        <f:param value="#{partBean.fixedQuestionsDrawTime}"/>
                        <f:param value="#{partBean.poolNameToBeDrawn}"/>
                        <f:param value="#{partBean.randomQuestionsOwnerDisplayName}"/>
                        <f:param value="#{partBean.randomQuestionsDrawDate}"/>
                        <f:param value="#{partBean.randomQuestionsDrawTime}"/>
                      </h:outputFormat>
                      <h:outputFormat rendered="#{partBean.sectionAuthorType!= null &amp;&amp; partBean.sectionAuthorTypeString == '3' &amp;&amp; !author.isEditPoolFlow &amp;&amp; !empty partBean.randomQuestionsDrawDate &amp;&amp; !empty partBean.fixedQuestionsDrawDate &amp;&amp; !author.isEditPendingAssessmentFlow}" value="#{authorMessages.fixed_and_random_draw_msg_published}" escape="false">
                        <f:param value="#{partBean.poolNameToBeFixed}"/>
                        <f:param value="#{partBean.fixedQuestionsOwnerDisplayName}"/>
                        <f:param value="#{partBean.fixedQuestionsDrawDate}"/>
                        <f:param value="#{partBean.fixedQuestionsDrawTime}"/>
                        <f:param value="#{partBean.poolNameToBeDrawn}"/>
                        <f:param value="#{partBean.randomQuestionsOwnerDisplayName}"/>
                        <f:param value="#{partBean.randomQuestionsDrawDate}"/>
                        <f:param value="#{partBean.randomQuestionsDrawTime}"/>
                      </h:outputFormat>
                      <!-- this insert should be at the top of each part  -->
                      <h:panelGroup rendered="#{author.isEditPendingAssessmentFlow &amp;&amp; !author.isEditPoolFlow}">
                        <div class="insert-question-row">
                          <div class="sak-banner-info">
                            <h:outputLabel for="changeQType" value="#{authorMessages.ins_new_q} "/>
                            <h:outputText value="&amp;#160;" escape="false"/>
                            <!-- each selectItem stores the itemtype, current sequence  -->
                            <h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);" value="#{itemauthor.itemTypeString}">
                              <f:selectItem itemLabel="#{authorMessages.select_qtype}" itemValue="1,#{partBean.number},#{partBean.itemContents.size()}"/>
                              <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener"/>
                              <f:selectItems value="#{itemConfig.itemTypeSelectList}"/>
                            </h:selectOneMenu>
                          </div>
                        </div>
                        <h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="" styleClass="d-none">
                        </h:commandLink>
                      </h:panelGroup>
                      <t:dataTable id="parts" styleClass="parts-table" width="100%" rowIndexVar="rowIndex" value="#{partBean.itemContents}" var="question" rendered="#{(! author.isEditPoolFlow &amp;&amp; (partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString ==  '1')) || (author.isEditPoolFlow &amp;&amp; author.editPoolSectionId != null &amp;&amp; partBean.sectionId == author.editPoolSectionId)}">
                        <h:column>
                          <h:panelGrid styleClass="table samigo-question-info" columns="2" width="100%" columnClasses="navView,navList">
                            <h:panelGroup>
                              <h:outputLabel for="number" value="#{authorMessages.q} "/>
                              <h:outputText value="&amp;#160;" escape="false"/>
                              <h:inputHidden id="currItemId" value="#{question.itemData.itemIdString}"/>
                              <h:selectOneMenu id="number" onchange="enableOrderUpdate()" value="#{question.number}" rendered="#{!author.isEditPoolFlow}">
                                <f:selectItems value="#{partBean.questionNumbers}"/>
                              </h:selectOneMenu>
                              <h:outputText value="#{question.number}: " rendered="#{!author.isEditPendingAssessmentFlow}"/>
                              <h:panelGroup>
                                <h:outputText rendered="#{question.itemData.typeId== 1}" value=" #{commonMessages.multiple_choice_sin}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 2}" value=" #{commonMessages.multipl_mc_ms}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 12}" value=" #{commonMessages.multipl_mc_ss}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 13}" value=" #{authorMessages.matrix_choice_surv}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 3}" value=" #{authorMessages.multiple_choice_surv}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 4}" value=" #{authorMessages.true_false}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 5}" value=" #{authorMessages.short_answer_essay}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 8}" value=" #{authorMessages.fill_in_the_blank}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 11}" value=" #{authorMessages.fill_in_numeric}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 9}" value=" #{authorMessages.matching}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 7}" value=" #{authorMessages.audio_recording}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 6}" value=" #{authorMessages.file_upload}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 14}" value=" #{authorMessages.extended_matching_items}"/>
                                <h:outputText rendered="#{question.itemData.typeId== 15}" value=" #{authorMessages.calculated_question}"/>
                                <!-- CALCULATED_QUESTION  -->
                                <h:outputText rendered="#{question.itemData.typeId== 16}" value=" #{authorMessages.image_map_question}"/>
                                <!-- IMAGEMAP_QUESTION  -->
                                <h:outputText value=" #{authorMessages.dash} "/>
                                <h:inputText id="answerptr" styleClass="ConvertPoint" value="#{question.updatedScore}" required="true" label="#{authorMessages.pt}" size="6" onkeydown="inIt()" disabled="#{author.isEditPoolFlow || question.itemData.typeId == 14 || question.cancelled}" rendered="#{question.itemData.typeId!= 3}">
                                  <f:validateDoubleRange minimum="0.00"/>
                                </h:inputText>
                                <h:outputText value="&amp;#160;" escape="false"/>
                                <h:outputLabel styleClass="notbold" for="answerptr" rendered="#{question.itemData.typeId== 3}" value="#{question.updatedScore}"/>
                                <h:outputLabel styleClass="notbold" for="answerptr" rendered="#{question.itemData.score == 1}" value=" #{authorMessages.point_lower_case}"/>
                                <h:outputLabel styleClass="notbold" for="answerptr" rendered="#{question.itemData.score &gt;= 0 &amp;&amp; question.itemData.score != 1}" value=" #{authorMessages.points_lower_case}"/>
                                <h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit == true}" value=" #{authorMessages.extra_credit_preview}"/>
                                <h:panelGroup styleClass="d-none d-sm-inline" rendered="#{question.itemData.minScore &gt; 0 || question.itemData.discount &gt; 0}">
                                  <h:outputText value=" ("/>
                                  <h:outputText value="#{authorMessages.answer_min_point_short}: #{question.itemData.minScore}" rendered="#{question.itemData.minScore &gt; 0}"/>
                                  <h:outputText value=", " rendered="#{question.itemData.minScore &gt; 0 &amp;&amp; question.itemData.discount &gt; 0}"/>
                                  <h:outputText value="#{authorMessages.answer_discount_point_short}: #{question.itemData.discount}" rendered="#{question.itemData.discount &gt; 0}"/>
                                  <h:outputText value=")"/>
                                </h:panelGroup>
                              </h:panelGroup>
                              <h:outputText escape="false" rendered="#{question.timedQuestion}" value=" &lt;i title='#{authorMessages.timed}' class='fa fa-clock-o'&gt;&lt;/i&gt;"/>
                              <!--Rubrics icon -->
                              <h:outputText styleClass="si si-sakai-rubrics" id="rubrics-question-icon" rendered="#{author.isEditPendingAssessmentFlow &amp;&amp; author.questionHasRubric(assessmentBean.assessmentId, question.itemData.itemIdString, false)}" title="#{authorMessages.question_use_rubric}" style="margin-left:0.5em"/>
                              <h:outputText styleClass="si si-sakai-rubrics" id="rubrics-published-question-icon" rendered="#{!author.isEditPendingAssessmentFlow &amp;&amp; author.questionHasRubric(author.editPublishedAssessmentID, question.itemData.itemIdString, true)}" title="#{authorMessages.question_use_rubric}" style="margin-left:0.5em"/>
                            </h:panelGroup>
                            <h:panelGroup>
                              <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow &amp;&amp; question.cancellable &amp;&amp; partBean.cancellationAllowed}">
                                <jsp:text><![CDATA[
              <a href="#" data-item-cancellable="true" data-itemId=']]></jsp:text>
                                <h:outputText value="#{question.itemData.itemIdString}"/>
                                <jsp:text><![CDATA['>
                ]]></jsp:text>
                                <h:outputText value="#{commonMessages.cancel_question}"/>
                                <jsp:text><![CDATA[
              </a>
              ]]></jsp:text>
                                <h:outputText value=" #{authorMessages.separator} "/>
                              </h:panelGroup>
                              <h:commandLink title="#{authorMessages.t_removeQ}" immediate="true" id="deleteitem" action="#{itemauthor.confirmDeleteItem}" rendered="#{author.isEditPendingAssessmentFlow}">
                                <h:outputText value="#{commonMessages.remove_action}"/>
                                <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
                              </h:commandLink>
                              <h:outputText value=" #{authorMessages.separator} " rendered="#{author.isEditPendingAssessmentFlow}"/>
                              <h:commandLink title="#{authorMessages.t_editQ}" id="modify" action="#{itemauthor.doit}" immediate="true">
                                <h:outputText value="#{commonMessages.edit_action}"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener"/>
                                <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
                                <f:param name="target" value="assessment"/>
                              </h:commandLink>
                            </h:panelGroup>
                          </h:panelGrid>
                          <h:panelGroup styleClass="samigo-question-callout#{question.cancelled ? ' samigo-question-cancelled' : ''}" layout="block">
                            <h:panelGroup rendered="#{question.itemData.typeId == 11}">
                              <jsp:directive.include file="/jsf/author/preview_item/FillInNumeric.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 9}">
                              <jsp:directive.include file="/jsf/author/preview_item/Matching.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 8}">
                              <jsp:directive.include file="/jsf/author/preview_item/FillInTheBlank.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 7}">
                              <jsp:directive.include file="/jsf/author/preview_item/AudioRecording.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 6}">
                              <jsp:directive.include file="/jsf/author/preview_item/FileUpload.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 5}">
                              <jsp:directive.include file="/jsf/author/preview_item/ShortAnswer.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 4}">
                              <jsp:directive.include file="/jsf/author/preview_item/TrueFalse.jsp"/>
                            </h:panelGroup>
                            <!-- same as multiple choice single  -->
                            <h:panelGroup rendered="#{question.itemData.typeId == 3}">
                              <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceSurvey.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 2}">
                              <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 1}">
                              <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceSingleCorrect.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 12}">
                              <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 14}">
                              <jsp:directive.include file="/jsf/author/preview_item/ExtendedMatchingItems.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 13}">
                              <jsp:directive.include file="/jsf/author/preview_item/MatrixChoicesSurvey.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 15}">
                              <!-- CALCULATED_QUESTION  -->
                              <jsp:directive.include file="/jsf/author/preview_item/CalculatedQuestion.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{question.itemData.typeId == 16}">
                              <!-- IMAGEMAP_QUESTION  -->
                              <jsp:directive.include file="/jsf/author/preview_item/ImageMapQuestion.jsp"/>
                            </h:panelGroup>
                            <h:panelGroup styleClass="sak-banner-info" rendered="#{question.cancelled}" layout="block">
                              <h:outputText value="#{commonMessages.cancel_question_info_cancelled_question}"/>
                            </h:panelGroup>
                          </h:panelGroup>
                          <!-- Only want this displayed at the bottom of the last part (others hidden via docReady JS)  -->
                          <h:panelGroup styleClass="part-insert-question" layout="block" rendered="#{author.isEditPendingAssessmentFlow}">
                            <div class="sak-banner-info">
                              <h:outputLabel for="changeQType" value="#{authorMessages.ins_new_q} "/>
                              <h:outputText value="&amp;#160;" escape="false"/>
                              <!-- each selectItem stores the itemtype, current sequence  -->
                              <h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);" value="#{itemauthor.itemTypeString}">
                                <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener"/>
                                <f:selectItem itemLabel="#{authorMessages.select_qtype}" itemValue="1,#{partBean.number},#{question.itemData.sequence}"/>
                                <f:selectItems value="#{itemConfig.itemTypeSelectList}"/>
                              </h:selectOneMenu>
                              <h:commandLink id="hiddenlink" styleClass="d-none" action="#{itemauthor.doit}" value="">
                              </h:commandLink>
                            </div>
                          </h:panelGroup>
                        </h:column>
                      </t:dataTable>
                    </div>
                    <!--Question Cancellation Modal -->
                    <div id="cancelQuestionModal" class="modal modal-lg fade question-cancel-modal" tabindex="-1">
                      <h:panelGroup styleClass="modal-dialog" layout="block">
                        <h:panelGroup styleClass="modal-content" layout="block">
                          <h:panelGroup styleClass="modal-header" layout="block">
                            <h2 class="modal-title">
                              <h:outputText value="#{commonMessages.cancel_question}"/>
                            </h2>
                            <jsp:text><![CDATA[
        <button type="button" class="btn btn-close me-1" data-bs-dismiss="modal"
            aria-label=']]></jsp:text>
                            <h:outputText value="#{authorMessages.button_close}"/>
                            <jsp:text><![CDATA['>
        </button>
      ]]></jsp:text>
                          </h:panelGroup>
                          <h:panelGroup styleClass="modal-body" layout="block">
                            <h:panelGroup styleClass="sak-banner-info" layout="block">
                              <h:outputText value="#{commonMessages.cancel_question_info_cancellation}"/>
                              <br/>
                              <br/>
                              <strong>
                                <h:outputText value="#{commonMessages.cancel_question_reduce_total}:"/>
                              </strong>
                              <h:outputText value="#{commonMessages.cancel_question_info_reduce_total}"/>
                              <br/>
                              <br/>
                              <strong>
                                <h:outputText value="#{commonMessages.cancel_question_distribute_points}:"/>
                              </strong>
                              <h:outputText value="#{commonMessages.cancel_question_info_distribute_points}"/>
                            </h:panelGroup>
                            <h:panelGroup styleClass="sak-banner-warn" layout="block">
                              <h:panelGroup rendered="#{partBean.emiItemPresent}">
                                <h:outputText value="#{commonMessages.cancel_question_info_emi} "/>
                                <jsp:text><![CDATA[
            </br>
            </br>
          ]]></jsp:text>
                              </h:panelGroup>
                              <h:outputText value="#{commonMessages.cancel_question_info_no_undo}"/>
                            </h:panelGroup>
                          </h:panelGroup>
                          <h:panelGroup layout="block" styleClass="sak-banner-info" rendered="#{!assessmentBean.totalScoreCancellationAllowed}">
                            <h:outputText value="#{assessmentBean.totalScoreCancellationRestrictionMessage}"/>
                          </h:panelGroup>
                          <h:panelGroup styleClass="modal-footer act" layout="block">
                            <h:commandButton styleClass="active" immediate="true" id="cancelItemTotal" action="editAssessment" disabled="#{!assessmentBean.totalScoreCancellationAllowed}" value="#{commonMessages.cancel_question_reduce_total}">
                              <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemCancellationListener"/>
                              <f:param name="outcome" value="editAssessment"/>
                              <f:param name="itemId" value="ITEM_ID"/>
                              <f:param name="cancellation" value="10"/>
                            </h:commandButton>
                            <h:commandButton styleClass="button me-0 me-lg-1" immediate="true" id="cancelItemDistribute" action="editAssessment" value="#{commonMessages.cancel_question_distribute_points}">
                              <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemCancellationListener"/>
                              <f:param name="outcome" value="editAssessment"/>
                              <f:param name="itemId" value="ITEM_ID"/>
                              <f:param name="cancellation" value="20"/>
                            </h:commandButton>
                            <button type="button" class="button mt-1 mt-lg-0" data-bs-dismiss="modal">
                              <h:outputText value="#{commonMessages.cancel_question_cancel}"/>
                            </button>
                          </h:panelGroup>
                        </h:panelGroup>
                      </h:panelGroup>
                    </div>
                  </h:column>
                </h:dataTable>
                <div class="act">
                  <h:commandButton value="#{authorMessages.button_update_points}" id="pointsUpdate" action="editAssessment" rendered="#{!author.isEditPoolFlow}" onclick="SPNR.disableControlsAndSpin(this, null);">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.UpdateAssessmentTotalPointsListener"/>
                  </h:commandButton>
                  <h:outputText value="&amp;#160;" escape="false"/>
                  <h:commandButton value="#{authorMessages.button_update_order}" id="orderUpdate" action="editAssessment" rendered="#{!author.isEditPoolFlow}" onclick="SPNR.disableControlsAndSpin(this, null);">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.UpdateAssessmentQuestionsOrder"/>
                  </h:commandButton>
                  <h:commandButton value="#{authorMessages.random_update_all_questions}" type="submit" id="randomAllQuestions" action="editAssessment" rendered="#{assessmentBean.allRandomPartsReshuffleEnabled &amp;&amp; (assessmentBean.partsWithRandomQuestionsCount &gt; 1 &amp;&amp; author.isEditPendingAssessmentFlow)}">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.UpdateAllPoolsQuestionsListener"/>
                  </h:commandButton>
                </div>
              </div>
              <jsp:text> </jsp:text>
              <!-- End the main container  -->
              <h:panelGrid columns="1" width="100%" columnClasses="navList" border="0">
                <h:panelGroup>
                  <h:commandButton id="republish1" value="#{authorMessages.button_republish}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow}" action="#{author.getOutcome}" onclick="SPNR.disableControlsAndSpin(this, null);">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener"/>
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener"/>
                  </h:commandButton>
                  <h:commandButton id="republishRegrade1" value="#{authorMessages.button_republish_and_regrade}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow &amp;&amp; assessmentBean.hasGradingData}" action="#{author.getOutcome}" onclick="SPNR.disableControlsAndSpin(this, null);">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener"/>
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener"/>
                  </h:commandButton>
                </h:panelGroup>
                <h:panelGroup>
                  <h:commandButton value="#{authorMessages.button_return_to_main}" id="returnToMain1" rendered="#{author.isEditPoolFlow}">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedQuestionPoolPartListener"/>
                  </h:commandButton>
                </h:panelGroup>
              </h:panelGrid>
              <h:panelGrid columns="1" width="100%" columnClasses="navList" border="0" rendered="#{!author.isEditPendingAssessmentFlow &amp;&amp; assessmentBean.hasGradingData}">
                <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublish','width=400,height=400,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublishPopUp','width=400,height=400,scrollbars=yes, resizable=yes');">
                  <h:outputText value=" #{assessmentSettingsMessages.whats_this_link}"/>
                </h:outputLink>
              </h:panelGrid>
              <h:panelGrid columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow &amp;&amp; (assessmentBean.sections[0].sectionAuthorType!= null &amp;&amp; assessmentBean.sections[0].sectionAuthorTypeString == '2')}">
                <h:outputFormat value="#{authorMessages.edit_published_assessment_warn_edit_pool_questions}">
                  <f:param value="#{author.editPoolName}"/>
                </h:outputFormat>
              </h:panelGrid>
              <h:panelGrid columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow &amp;&amp; (assessmentBean.sections[0].sectionAuthorType!= null &amp;&amp; assessmentBean.sections[0].sectionAuthorTypeString == '3')}">
                <h:outputFormat value="#{authorMessages.edit_published_assessment_warn_edit_pool_fixed_questions}">
                  <f:param value="#{author.editPoolNameFixed}"/>
                  <f:param value="#{author.editPoolName}"/>
                </h:outputFormat>
              </h:panelGrid>
              <h:panelGrid columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow &amp;&amp; (assessmentBean.sections[0].sectionAuthorType!= null &amp;&amp; assessmentBean.sections[0].sectionAuthorTypeString == '4')}">
                <h:outputText value="#{authorMessages.edit_published_assessment_warn_edit_multiple_pools}"/>
              </h:panelGrid>
              <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="sak-banner-warn">
                <h:panelGrid columns="1">
                  <h:outputText value="#{authorMessages.edit_published_assessment_warn_1}"/>
                  <h:outputText value="#{authorMessages.edit_published_assessment_warn_21}" rendered="#{assessmentBean.hasGradingData}"/>
                  <h:outputText value="#{authorMessages.edit_published_assessment_warn_22}" rendered="#{!assessmentBean.hasGradingData}"/>
                </h:panelGrid>
              </h:panelGroup>
              <h:inputHidden id="randomQuestionsSectionId" value=""/>
            </h:form>
            <!-- end content  -->
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
