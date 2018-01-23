<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.create_modify_a}" /></title>
      <samigo:script path="/js/authoring.js"/>

<script type="text/JavaScript">
<%@ include file="/js/samigotree.js" %>

$(window).load( function() {
  // No need for an insert question box after every single question!
  $('table.parts-table').find('div.part-insert-question:not(:last)').hide();
});
</script>

<samigo:script path="/../library/webjars/jquery/1.12.4/jquery.min.js"/>
<samigo:script path="/js/selection.author.preview.js"/>
<samigo:script path="/../library/js/spinner.js"/>

<samigo:stylesheet path="/css/imageQuestion.author.css"/>

<script type="text/JavaScript">	
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
</script>
</head>
<body onload="document.forms[0].reset(); disableIt(); resetSelectMenus(); ;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody container-fluid">
<!-- content... -->
<!-- some back end stuff stubbed -->
<h:form id="assessmentForm">

  <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="bs-callout-danger">
    <h:panelGrid  columns="1">
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_1}" />
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_21}" rendered="#{assessmentBean.hasGradingData}"/>
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_22}" rendered="#{!assessmentBean.hasGradingData}"/>
    </h:panelGrid>
  </h:panelGroup>

  <h:inputHidden id="assessmentId" value="#{assessmentBean.assessmentId}"/>
  <h:inputHidden id="showCompleteAssessment" value="#{author.showCompleteAssessment}"/>
  <h:inputHidden id="title" value="#{assessmentBean.title}" />
<%-- NOTE!
     add JavaScript to handle events that effect a part or question and
     set the value of these when a particular part or question is affected
     and the "current section" or "current part" needs to be changed
     other alternative maybe value changed listener
--%>
  <h:inputHidden id="SectionIdent" value="#{author.currentSection}"/>
  <h:inputHidden id="ItemIdent" value="#{author.currentItem}"/>

  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>

  <div class="navView">
    <h1>
       <h:outputText value="#{authorMessages.qs}#{authorMessages.column} #{assessmentBean.title}" escape="false" />
       <h:outputText value=" #{authorMessages.dash} #{author.editPoolSectionName}" escape="false" rendered="#{author.isEditPoolFlow}"/>
    </h1>
  </div>

  <div class="navList">
    <h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_qs} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize > 1}" />
    <h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_q} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize == 1}" />
    <h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_qs} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize == 0}" />
    <h:outputText value="#{assessmentBean.totalScore}">
      <f:convertNumber maxFractionDigits="2"/>
    </h:outputText>
    <h:outputText value="&#160;" escape="false" />
    <h:outputText value="#{authorMessages.total_pts}" rendered="#{assessmentBean.totalScore > 1}" />
    <h:outputText value="#{authorMessages.total_pt}" rendered="#{assessmentBean.totalScore == 1}" />
    <h:outputText value="#{authorMessages.total_pts}" rendered="#{assessmentBean.totalScore == 0}" />
   </div>
 
   <p class="navViewAction">
      <h:commandLink title="#{authorMessages.t_addPart}" id="addPart" action="editPart" immediate="true" rendered="#{author.isEditPendingAssessmentFlow}">
         <h:outputText value="#{authorMessages.subnav_add_part}" />
         <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
         <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorPartListener" />
      </h:commandLink>

      <h:outputText value=" #{authorMessages.separator} " rendered="#{assessmentBean.showPrintLink eq 'true'}"/>

      <h:commandLink  title="#{commonMessages.action_preview}" action="beginAssessment" rendered="#{assessmentBean.assessmentId > 0}">
        <h:outputText value="#{commonMessages.action_preview}"/>
        <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
        <f:param name="publishedId" value="#{author.editPublishedAssessmentID}"/>
        <f:param name="actionString" value="previewAssessment" />
        <f:param name="fromEdit" value="true" />
        <f:param name="isFromPrint" value="false" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
      </h:commandLink>

	<h:outputText value=" #{authorMessages.separator} " rendered="#{assessmentBean.showPrintLink eq 'true'}"/>
	
	<h:commandLink action="#{itemauthor.print}" rendered="#{assessmentBean.showPrintLink eq 'true' && assessmentBean.showPrintAssessment ne 'true'}">
		<h:outputText value="#{authorMessages.subnav_print}" escape="false" />
	</h:commandLink>
	<h:commandLink action="#{pdfAssessment.prepPDF}" rendered="#{assessmentBean.showPrintLink eq 'true' && assessmentBean.showPrintAssessment eq 'true'}">
		<f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
		<f:param name="publishedId" value="#{author.editPublishedAssessmentID}"/>
		<f:param name="actionString" value="editAssessment"/>
		<f:param name="isFromPrint" value="true" />
		<h:outputText value="#{printMessages.print}" escape="false" />
	</h:commandLink>

    <h:outputText value=" #{authorMessages.separator} " />
	
	<h:commandLink title="#{authorMessages.t_settings}" id="editAssessmentSettings_editAssessment" action="editAssessmentSettings" immediate="true" rendered="#{author.isEditPendingAssessmentFlow && assessmentBean.assessmentId > 0}">
        <h:outputText value="#{commonMessages.settings_action}" />
	    <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
	    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorSettingsListener" />
	    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener" />
	</h:commandLink>

	<h:commandLink title="#{authorFrontDoorMessages.t_editSettings}" id="editPublishedAssessmentSettings_editAssessment" immediate="true"
          rendered="#{!author.isEditPendingAssessmentFlow}"
          action="#{author.getOutcome}">
        
        <h:outputText  value="#{commonMessages.settings_action}" />
        <f:param name="publishedAssessmentId" value="#{assessmentBean.assessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" />
	    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener" />
    </h:commandLink>
    
    <h:outputText value=" #{authorMessages.separator} " rendered="#{author.isEditPendingAssessmentFlow && assessmentBean.questionSize > 0}"/>

  	<h:commandLink title="#{commonMessages.publish_action}" id="editAssessmentSettings_editAssessment2" action="#{assessmentSettings.getOutcomePublish}" immediate="true" rendered="#{author.isEditPendingAssessmentFlow && assessmentBean.questionSize > 0}">
      <h:outputText value="#{commonMessages.publish_action}" />
      <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmPublishAssessmentListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.PublishAssessmentListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener" />
    </h:commandLink>
    </p>

<h:panelGrid  columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow}">
	<h:outputFormat value="#{authorMessages.edit_published_assessment_warn_edit_pool_questions}">
	    <f:param value="#{author.editPoolName}" />
	</h:outputFormat>
</h:panelGrid>

<h:panelGrid columns="2" width="100%" columnClasses="shortText,navList">

<h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" />

<h:panelGroup>
  <h:commandButton id="republish" value="#{authorMessages.button_republish}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow}"
      action="#{author.getOutcome}" >
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener" />
  </h:commandButton>

  <h:commandButton id="republishRegrade" value="#{authorMessages.button_republish_and_regrade}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}"
      action="#{author.getOutcome}" >
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener" />
  </h:commandButton>
</h:panelGroup>

<h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" />
  
<h:panelGroup>
  <h:commandButton value="#{authorMessages.button_return_to_main}" id="returnToMain" rendered="#{author.isEditPoolFlow}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedQuestionPoolPartListener" />
  </h:commandButton>
</h:panelGroup>
  
</h:panelGrid>

<h:panelGrid columns="1" width="100%" columnClasses="navList" border="0" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}">
  <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublish','width=400,height=400,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublishPopUp','width=400,height=400,scrollbars=yes, resizable=yes');" >
    <h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
  </h:outputLink>
</h:panelGrid>

<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
  <f:param name="itemSequence" value="0"/>
</h:commandLink>

<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<div class="tier1">
  <h:dataTable id="parts" width="100%" value="#{assessmentBean.sections}" var="partBean">
    <%-- note that partBean is ui/delivery/SectionContentsBean not ui/author/SectionBean --%>
    <h:column>
      <h:panelGrid styleClass="table table-striped" columns="2" width="100%" columnClasses="navView,navList" border="0">
       <h:panelGroup rendered="#{!author.isEditPoolFlow}">
		<h:outputText value="#{authorMessages.p}" /> <f:verbatim>&nbsp; </b></f:verbatim>
          <h:selectOneMenu id="number" value="#{partBean.number}" onchange="enableOrderUpdate()" rendered="#{author.isEditPendingAssessmentFlow}" >
          <f:selectItems value="#{assessmentBean.partNumbers}" />          
        </h:selectOneMenu>
        <h:outputText value="#{partBean.number}: " rendered="#{!author.isEditPendingAssessmentFlow}"/>
        <h:outputText value="&#160;" escape="false" />
	  <h:panelGroup rendered="#{!author.isEditPoolFlow}">
		<h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') && partBean.questions > 1}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.questions_lower_case}" escape="false"/>
		<h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') && partBean.questions == 1}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.question_lower_case}" escape="false"/>
		<h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') && partBean.questions == 0}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.questions_lower_case}" escape="false"/>

		<h:outputText rendered="#{(partBean.sectionAuthorType!= null &&partBean.sectionAuthorTypeString == '2') && partBean.numberToBeDrawnString > 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.questions_lower_case}" escape="false"/>
		<h:outputText rendered="#{(partBean.sectionAuthorType!= null &&partBean.sectionAuthorTypeString == '2') && partBean.numberToBeDrawnString == 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.question_lower_case}" escape="false"/>

		<h:commandButton value="#{authorMessages.random_update_questions}" type="submit" id="randomQuestions" action="editAssessment" style="margin-left: 2em"
			rendered="#{(partBean.sectionAuthorType!= null &&partBean.sectionAuthorTypeString == '2' && author.isEditPendingAssessmentFlow)}"
			onclick="SPNR.disableControlsAndSpin( this, null );document.getElementById('assessmentForm:randomQuestionsSectionId').value='#{partBean.sectionId}';" >
		  	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.UpdateRandomPoolQuestionsListener" />
		</h:commandButton>
		
        <h:outputText value="&#160;" escape="false" />
		<h:commandButton value="#{authorMessages.button_edit_questions}" id="editQuestionPoolQuestions" rendered="#{(partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '2'&& !author.isEditPendingAssessmentFlow)}">
		    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedQuestionPoolPartListener"/>
		</h:commandButton>

	  </h:panelGroup>
      </h:panelGroup>

	  <h:panelGroup rendered="#{!author.isEditPoolFlow}">
		<h:commandLink title="#{authorMessages.copy_to_pool}" id="copyToPool" immediate="true" action="#{questionpool.startCopyFromAssessment}" rendered="#{author.isEditPendingAssessmentFlow}">
          <h:outputText value="#{authorMessages.copy_to_pool}" rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '1'}"/>
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
        </h:commandLink>

		<h:outputText value=" #{authorMessages.separator} " rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '1' && author.isEditPendingAssessmentFlow}"/>

        <h:commandLink title="#{authorMessages.t_removeP}" action="confirmRemovePart" immediate="true"
          rendered="#{partBean.number ne 1 && author.isEditPendingAssessmentFlow}">
          <h:outputText value="#{commonMessages.remove_action}" />
          <!-- use this to set the sectionBean.sectionId in ConfirmRemovePartListener -->
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemovePartListener" />
        </h:commandLink>

        <h:outputText value=" #{authorMessages.separator} " rendered="#{partBean.number ne 1 && author.isEditPendingAssessmentFlow}"/>

        <h:commandLink title="#{authorMessages.t_editP}" id="editPart" immediate="true" action="editPart" 
                       onclick="SPNR.insertSpinnerInPreallocated( null, null, 'assessmentForm:parts:#{partBean.number - 1}:editPartSpinner' );">
          <h:outputText value="#{commonMessages.edit_action}" />
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPartListener" />
        </h:commandLink>
        <t:div id="editPartSpinner" styleClass="allocatedSpinPlaceholder"></t:div>

      </h:panelGroup>
	  
    </h:panelGrid>

    <h:outputText escape="false" value="#{partBean.description}" />

    <div class="question-row">
        <!-- PART ATTACHMENTS -->
        <%@ include file="/jsf/author/part_attachment.jsp" %>

		<h:outputText rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '2' && empty partBean.randomQuestionsDrawDate}" value="#{authorMessages.random_draw_msg_no_date}"/>
        <h:outputFormat rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '2' && !empty partBean.randomQuestionsDrawDate && author.isEditPendingAssessmentFlow}" value="#{authorMessages.random_draw_msg}" escape="false">
        	<f:param value="#{partBean.poolNameToBeDrawn}"/>
        	<f:param value="#{partBean.randomQuestionsDrawDate}"/>
        	<f:param value="#{partBean.randomQuestionsDrawTime}"/>
        </h:outputFormat>
        
        <h:outputFormat rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '2' && !author.isEditPoolFlow && !empty partBean.randomQuestionsDrawDate && !author.isEditPendingAssessmentFlow}" value="#{authorMessages.random_draw_msg_published}" escape="false">
        	<f:param value="#{partBean.poolNameToBeDrawn}"/>
        	<f:param value="#{partBean.randomQuestionsDrawDate}"/>
        	<f:param value="#{partBean.randomQuestionsDrawTime}"/>
        </h:outputFormat>
        
<!-- this insert should only show up when there are no questions in this part -->
<h:panelGroup rendered="#{partBean.itemContentsSize eq '0' && author.isEditPendingAssessmentFlow && !author.isEditPoolFlow}">
    <div class="insert-question-row"> 
	  <h:outputLabel for="changeQType" value="#{authorMessages.ins_new_q} "/>
	  <h:outputText value="&#160;" escape="false" />
        <!-- each selectItem stores the itemtype, current sequence -->
         <h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);"  value="#{itemauthor.itemTypeString}">
             <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener" />
             <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
             <f:selectItem itemLabel="#{authorMessages.import_from_q}" itemValue="10,#{partBean.number},0"/>
         </h:selectOneMenu>
    </div>
    <h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
      <f:param name="itemSequence" value="0"/>
    </h:commandLink>
</h:panelGroup>

<t:dataTable id="parts" styleClass="parts-table" width="100%" rowIndexVar="rowIndex"
        value="#{partBean.itemContents}" var="question" rendered="#{(! author.isEditPoolFlow && (partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString ==  '1')) || (author.isEditPoolFlow && author.editPoolSectionId != null && partBean.sectionId == author.editPoolSectionId)}" >

      <h:column>
         <h:panelGrid styleClass="table table-condensed" columns="2" width="100%" columnClasses="navView,navList">
          <h:panelGroup>
          <h:outputText value="#{authorMessages.q} " />
            <h:inputHidden id="currItemId" value="#{question.itemData.itemIdString}"/>
            <h:selectOneMenu id="number" onchange="enableOrderUpdate()" value="#{question.number}" rendered="#{author.isEditPendingAssessmentFlow}">
              <f:selectItems value="#{partBean.questionNumbers}" />
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
     <h:outputText rendered="#{question.itemData.typeId== 15}" value=" #{authorMessages.calculated_question}"/><!-- CALCULATED_QUESTION -->
     <h:outputText rendered="#{question.itemData.typeId== 16}" value=" #{authorMessages.image_map_question}"/><!-- IMAGEMAP_QUESTION -->

     <h:outputText value=" #{authorMessages.dash} " />
     <h:inputText id="answerptr" value="#{question.updatedScore}" required="true" disabled="#{author.isEditPoolFlow || (question.itemData.typeId== 14)}" label="#{authorMessages.pt}" size="6" onkeydown="inIt()" styleClass="ConvertPoint" rendered="#{question.itemData.typeId!= 3}">
	<f:validateDoubleRange minimum="0.00"/></h:inputText>
    <h:outputText rendered="#{question.itemData.typeId== 3}" value="#{question.updatedScore}"/>
		<h:outputText rendered="#{question.itemData.score > 1}" value=" #{authorMessages.points_lower_case}"/>
		<h:outputText rendered="#{question.itemData.score == 1}" value=" #{authorMessages.point_lower_case}"/>
		<h:outputText rendered="#{question.itemData.score == 0}" value=" #{authorMessages.points_lower_case}"/>
	</h:panelGroup>


        </h:panelGroup>
          <h:panelGroup>
            <h:commandLink title="#{authorMessages.t_removeQ}" immediate="true" id="deleteitem" action="#{itemauthor.confirmDeleteItem}" rendered="#{author.isEditPendingAssessmentFlow}">
              <h:outputText value="#{commonMessages.remove_action}" />
              <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
            </h:commandLink>
            <h:outputText value=" #{authorMessages.separator} " rendered="#{author.isEditPendingAssessmentFlow}"/>
            <h:commandLink title="#{authorMessages.t_editQ}" id="modify" action="#{itemauthor.doit}" immediate="true">
              <h:outputText value="#{commonMessages.edit_action}" />
              <f:actionListener
                  type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
              <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
              <f:param name="target" value="assessment"/>
            </h:commandLink>
          </h:panelGroup>
        </h:panelGrid>

       <div class="samigo-question-callout">
		  <h:panelGroup rendered="#{question.itemData.typeId == 11}">
	  			<%@ include file="/jsf/author/preview_item/FillInNumeric.jsp" %>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 9}">
            <%@ include file="/jsf/author/preview_item/Matching.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 8}">
            <%@ include file="/jsf/author/preview_item/FillInTheBlank.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 7}">
            <%@ include file="/jsf/author/preview_item/AudioRecording.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 6}">
            <%@ include file="/jsf/author/preview_item/FileUpload.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 5}">
            <%@ include file="/jsf/author/preview_item/ShortAnswer.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 4}">
            <%@ include file="/jsf/author/preview_item/TrueFalse.jsp" %>
          </h:panelGroup>

          <!-- same as multiple choice single -->
          <h:panelGroup rendered="#{question.itemData.typeId == 3}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceSurvey.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 2}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 1}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceSingleCorrect.jsp" %>
          </h:panelGroup>

		  <h:panelGroup rendered="#{question.itemData.typeId == 12}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
          </h:panelGroup>

		  <h:panelGroup rendered="#{question.itemData.typeId == 14}">
            <%@ include file="/jsf/author/preview_item/ExtendedMatchingItems.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 13}">
            <%@ include file="/jsf/author/preview_item/MatrixChoicesSurvey.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 15}"><!-- CALCULATED_QUESTION -->
                <%@ include file="/jsf/author/preview_item/CalculatedQuestion.jsp" %>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 16}"><!-- IMAGEMAP_QUESTION -->
                <%@ include file="/jsf/author/preview_item/ImageMapQuestion.jsp" %>
          </h:panelGroup>   
      </div>

      <!-- Only want this displayed at the bottom of each part not on every question -->
      <h:panelGroup styleClass="part-insert-question" layout="block" rendered="#{author.isEditPendingAssessmentFlow}">
        <div class="bs-callout-primary">
	      <h:outputLabel for="changeQType" value="#{authorMessages.ins_new_q} "/>
          <h:outputText value="&#160;" escape="false" />
          <!-- each selectItem stores the itemtype, current sequence -->
          <h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);" value="#{itemauthor.itemTypeString}" >
            <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener" />
            <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
            <f:selectItem itemLabel="#{authorMessages.import_from_q}" itemValue="10,#{partBean.number},#{question.itemData.sequence}"/>
          </h:selectOneMenu>
          <h:commandLink id="hiddenlink" styleClass="hiddenLink" action="#{itemauthor.doit}" value="">
            <f:param name="itemSequence" value="#{question.itemData.sequence}"/>
          </h:commandLink>
        </div>
      </h:panelGroup>

    </h:column>
  </t:dataTable>
</div>
  </h:column>
</h:dataTable>
<h:commandButton value="#{authorMessages.button_update_points}" id="pointsUpdate" action="editAssessment" rendered="#{!author.isEditPoolFlow}">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.UpdateAssessmentTotalPointsListener" />
</h:commandButton>
<h:outputText value="&#160;" escape="false" />
<h:commandButton value="#{authorMessages.button_update_order}" id="orderUpdate" action="orderUpdate" rendered="#{!author.isEditPoolFlow}">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.UpdateAssessmentQuestionsOrder" />
</h:commandButton>
</div> <!-- End the main container -->

<h:panelGrid columns="1" width="100%" columnClasses="navList" border="0">
<h:panelGroup>
  <h:commandButton id="republish1" value="#{authorMessages.button_republish}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow}"
      action="#{author.getOutcome}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener" />
  </h:commandButton>

  <h:commandButton id="republishRegrade1" value="#{authorMessages.button_republish_and_regrade}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}"
      action="#{author.getOutcome}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SetFromPageAsEditAssessmentListener" />
  </h:commandButton>

</h:panelGroup>

<h:panelGroup>
  <h:commandButton value="#{authorMessages.button_return_to_main}" id="returnToMain1" rendered="#{author.isEditPoolFlow}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedQuestionPoolPartListener" />
  </h:commandButton>  
</h:panelGroup>
</h:panelGrid>

<h:panelGrid columns="1" width="100%" columnClasses="navList" border="0" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}">
  <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublish','width=400,height=400,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('/samigo-app/jsf/author/regradeRepublishPopUp.faces','RegradeRepublishPopUp','width=400,height=400,scrollbars=yes, resizable=yes');" >
    <h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
  </h:outputLink>
</h:panelGrid>


<h:panelGrid  columns="1" styleClass="validation" rendered="#{author.isEditPoolFlow}">
	<h:outputFormat value="#{authorMessages.edit_published_assessment_warn_edit_pool_questions}">
	    <f:param value="#{author.editPoolName}" />
	</h:outputFormat>
</h:panelGrid>
<h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="bs-callout-danger">
    <h:panelGrid  columns="1">
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_1}" />
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_21}" rendered="#{assessmentBean.hasGradingData}"/>
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_22}" rendered="#{!assessmentBean.hasGradingData}"/>
    </h:panelGrid>
  </h:panelGroup>
  
  <h:inputHidden id="randomQuestionsSectionId" value=""/>
</h:form>
<!-- end content -->
</div>

      </body>
    </html>
  </f:view>

