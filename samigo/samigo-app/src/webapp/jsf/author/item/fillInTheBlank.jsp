<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
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
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <samigo:script path="/js/authoring.js"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<%-- content... --%>
<%-- FORM --%>

<%-- HEADING --%>
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>

<%-- warning for editing FIB questions. SAM-2334 --%>
<h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="messageSamigo2">
	<h:panelGrid  columns="1">
		<h:outputText value="#{authorMessages.edit_fib_warning}" />
	</h:panelGrid>
</h:panelGroup>

<h:form id="itemForm">
<p class="act">
 <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
 </h:commandButton>
</p>

<%-- QUESTION PROPERTIES --%>
<%-- 1 POINTS --%>
<div class="tier2">
  <div class="shorttext"><h:outputLabel value="#{authorMessages.answer_point_value}" />
  <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true" disabled="#{author.isEditPoolFlow}" onchange="toPoint(this.id);">
    <f:validateDoubleRange minimum="0.00"/>
  </h:inputText>
 <br/>  <h:message for="answerptr" styleClass="validate"/>
 </div>
 <div class="longtext">
    <h:outputLabel value="#{authorMessages.answer_point_value_display}" />    </div>
	<div class="tier3">
    <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
     <f:selectItem itemValue="true"
       itemLabel="#{authorMessages.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
  </div>
<br/>
  <!-- 1.2 Min POINTS -->
<f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">
<f:verbatim>
<div class="shorttext">
</f:verbatim>
    <h:outputLabel value="#{authorMessages.answer_min_point_value}" />
    <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" onchange="toPoint(this.id);">
<f:validateDoubleRange/>
</h:inputText>
<f:verbatim><div></f:verbatim>
<h:outputText value="#{authorMessages.answer_min_point_info}" style="font-size: x-small" />
<f:verbatim></div></f:verbatim>
    <h:message for="answerminptr" styleClass="validate"/><br/>
<f:verbatim>
  </div>
<br/>
</f:verbatim>
</f:subview>

<%-- 2 QUESTION TEXT --%>
  <div class="longtext"> <h:outputLabel value="#{authorMessages.q_text}" />
  <br/></div>
  <f:verbatim><div class="tier2"></f:verbatim>
  <h:outputText value="#{authorMessages.defining_answers}" escape="false"/>
  <f:verbatim><div class="tier3"></f:verbatim>
  <h:outputText value="#{authorMessages.fib_note_1}<br /><br />" escape="false"/>
  <h:outputText value="#{authorMessages.fib_note_2}<br /><br />" escape="false"/>
  <h:outputText value="#{authorMessages.fib_note_3}<br /><br />" escape="false"/>
  <f:verbatim></div></f:verbatim>
  
   <h:panelGrid>
   <samigo:wysiwyg
     rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
    <f:validateLength maximum="60000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
 <br />

<h:panelGrid columns="1" border="0">
<h:panelGroup>
<h:selectBooleanCheckbox value="#{itemauthor.currentItem.caseSensitiveForFib}">
</h:selectBooleanCheckbox>
<h:outputText value="#{authorMessages.case_sensitive}" escape="false"/>
</h:panelGroup>
<h:panelGroup>
<h:outputText value="&nbsp;&nbsp;" escape="false"/>
<h:outputText value="#{authorMessages.case_sensitive_note}" escape="false"/>
</h:panelGroup>
<h:panelGroup>
<h:outputText value="&nbsp;&nbsp;" escape="false"/>
<h:outputText value="#{authorMessages.case_sensitive_example}" escape="false"/>
</h:panelGroup>
</h:panelGrid>
<br/>

<h:panelGrid columns="1">
<h:panelGroup>
<h:selectBooleanCheckbox value="#{itemauthor.currentItem.mutuallyExclusiveForFib}">
</h:selectBooleanCheckbox>
<h:outputText value="#{authorMessages.mutually_exclusive}" escape="false"/>
</h:panelGroup>
<h:panelGroup>
<h:outputText value="&nbsp;&nbsp;" escape="false"/>
<h:outputText value="#{authorMessages.mutually_exclusive_note}" escape="false"/>
</h:panelGroup>
<h:panelGroup>
<h:outputText value="&nbsp;&nbsp;" escape="false"/>
<h:outputText value="#{authorMessages.mutually_exclusive_example}" escape="false"/>
</h:panelGroup>
</h:panelGrid>
</div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

<%-- 3 PART --%>
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'  && !author.isEditPoolFlow}">
  <f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <%-- use this in real  value="#{section.sectionNumberList}" --%>
  </h:selectOneMenu>
</h:panelGrid>
<%-- 5 POOL --%>
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
 <f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel value="#{authorMessages.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
</h:panelGrid>
<%-- FEEDBACK --%>
<f:verbatim><div class="longtext"></f:verbatim>
 <h:outputLabel value="#{authorMessages.correct_incorrect_an}" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}"/>
<f:verbatim> <div class="tier2"></f:verbatim>
  
  <h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
   <h:outputText value="#{authorMessages.correct_answer_opti}" />
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>

  <h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
   <h:outputText value="#{authorMessages.incorrect_answer_op}" />
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
  
 <f:verbatim> </div></div></f:verbatim>
 <%-- METADATA --%>
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="tier3"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputText value="#{authorMessages.objective}" />
  <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputText value="#{authorMessages.keyword}" />
  <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputText value="#{authorMessages.rubric_colon}" />
  <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
</div>
<%-- BUTTONS --%>
<p class="act">

 <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
 </h:commandButton>

</p>
</h:form>
<%-- end content --%>
</div>
    </body>
  </html>
</f:view>
