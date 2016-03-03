
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
<body onload="countNum();;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- FORM -->



<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
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

 <div class="tier2">
<!-- QUESTION PROPERTIES -->
  <!-- 1 POINTS -->

<div class="shorttext">
    <h:outputLabel value="#{authorMessages.answer_point_value}"/>
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true" disabled="#{author.isEditPoolFlow}" onchange="toPoint(this.id);">
<f:validateDoubleRange minimum="0.00"/>
</h:inputText>
 <h:message for="answerptr" styleClass="validate"/>
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

  <!-- 1.2 MIN POINTS -->
<f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">
<f:verbatim>
<div class="shorttext">
</f:verbatim>
    <h:outputLabel value="#{authorMessages.answer_min_point_value}"/>
    <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" onchange="toPoint(this.id);">
<f:validateDoubleRange />
</h:inputText>
<f:verbatim><div></f:verbatim>
<h:outputText value="#{authorMessages.answer_min_point_info}" style="font-size: x-small" />
<f:verbatim></div></f:verbatim>
 <h:message for="answerminptr" styleClass="validate"/>
<f:verbatim>
  </div>
<br/>
</f:verbatim>
</f:subview>
  
<!-- DISCOUNT -->
<div class="longtext">
<h:panelGrid columns="2" border="0">
  <h:panelGrid border="0">
    <h:outputLabel value="#{authorMessages.negative_point_value}"/>
    <h:outputText value="&nbsp;" escape="false"/>
  </h:panelGrid>
  <h:panelGrid border="0">
    <h:panelGroup>
    <h:inputText id="answerdsc" value="#{itemauthor.currentItem.itemDiscount}" required="true" onchange="toPoint(this.id);" >
  	  <f:validateDoubleRange/>
    </h:inputText>
    <h:message for="answerdsc" styleClass="validate"/>
    </h:panelGroup>
    <h:outputText value="#{authorMessages.note_negative_point_value_question}" />
  </h:panelGrid>
</h:panelGrid>
</div><br/>

  <!-- 2 TEXT -->
 <div class="longtext">
  <h:outputLabel value="#{authorMessages.q_text}" />
  <!-- STUB FOR WYSIWYG -->

  <!-- WYSIWYG -->
   
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
     <f:validateLength minimum="1" maximum="60000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

  <!-- 3 ANSWER -->
  <div class="longtext">
  <h:outputLabel value="#{authorMessages.answer} " /></div>
   <div class="tier2">
  <h:selectOneRadio layout="lineDirection" id="TF" border="0"
     value="#{itemauthor.currentItem.corrAnswer}">
     <f:selectItems value="#{itemauthor.trueFalseAnswerSelectList}" />
  </h:selectOneRadio>
</div>

    <!-- 4 RATIONALE -->
    <div class="longtext">
    <h:outputLabel value="#{authorMessages.req_rationale}" /></div>
	<div class="tier2">
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" id="rational" required="true">
     <f:selectItem itemValue="true"
       itemLabel="#{authorMessages.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
<br/> <h:message for="rational" styleClass="validate"/>
  </div>

  <!-- 5 PART -->
<div class="longtext">
  <h:panelGrid rendered="#{itemauthor.target == 'assessment'}" columnClasses="shorttext">  
  <h:panelGroup rendered="#{!author.isEditPoolFlow}">
     <f:verbatim></f:verbatim>
  <h:outputLabel value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>
  </div>

  <!-- 6 POOL -->
<div class="longtext">
  <h:panelGrid rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}" columnClasses="shorttext">
  <h:panelGroup>
     <f:verbatim></f:verbatim>
  <h:outputLabel value="#{authorMessages.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>
  </div>

   <!-- FEEDBACK -->
<h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}"><br/>
  <f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{authorMessages.correct_incorrect_an}" />
<f:verbatim></div>
 <div class="tier2"></f:verbatim>
  <h:outputLabel value="#{authorMessages.correct_answer_opti}" />

  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
</h:panelGrid>

 <f:verbatim></div><div class="tier2"></f:verbatim>
 <h:outputLabel  value="#{authorMessages.incorrect_answer_op}" />

  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid><f:verbatim></div></f:verbatim>
</h:panelGroup>

 <!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="tier3"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel value="#{authorMessages.objective}" />
  <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputLabel value="#{authorMessages.keyword}" />
  <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputLabel value="#{authorMessages.rubric_colon}" />
  <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</div>


<%--
<div class="longtext tier1">
  <h:panelGrid columns="3" rendered="#{itemauthor.showMetadata == 'true'}">
  <f:verbatim><span id="num8" class="number"></span></f:verbatim>
  <h:outputLabel for="obj" value="#{authorMessages.objective}" />
  <h:inputText id="obj" value="#{itemauthor.currentItem.objective}" />
  <f:verbatim><span id="num9" class="number"></span></f:verbatim>
  <h:outputLabel for="keyword" value="#{authorMessages.keyword}" />
  <h:inputText id="keyword" value="#{itemauthor.currentItem.keyword}" />
  <f:verbatim><span id="num10" class="number"></span></f:verbatim>
  <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" />
  <h:inputText id="rubric" value="#{itemauthor.currentItem.rubric}" />
  </h:panelGrid>

  </div>
--%>

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


<!-- end content -->
</div>
    </body>
  </html>
</f:view>
