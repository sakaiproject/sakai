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
    <h:outputLabel value="#{authorMessages.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true" disabled="#{author.isEditPoolFlow}" onchange="toPoint(this.id);">
<f:validateDoubleRange minimum="0.00"/>
</h:inputText><br/>
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
<!-- 1.2 MIN POINTS 
  Ths is commented out since it doesn't make sense to have a min value for a question that doesn't automatically calculate a score
   <div class="shorttext">
    <h:outputLabel value="#{authorMessages.answer_min_point_value}" />
    <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" onchange="toPoint(this.id);">
<f:validateDoubleRange/>
</h:inputText><br/>
    <h:message for="answerminptr" styleClass="validate"/>
  </div>
<br/>
-->
  <!-- 2 TEXT -->
  <div class="longtext"> <h:outputLabel value="#{authorMessages.q_text}" />

  <!-- WYSIWYG -->

  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
     <f:validateLength maximum="60000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
  </div>

  <!-- 2a ATTACHMENTS -->  
  <%@ include file="/jsf/author/item/attachment.jsp" %>

  <!-- 3 TIME allowed -->
   <div class="longtext">
  <h:outputLabel for="timeallowed" value="#{authorMessages.time_allowed_seconds}:  #{authorMessages.time_allowed_seconds_indic} " />
  <h:inputText id="timeallowed" value="#{itemauthor.currentItem.timeAllowed}" required="true">
<f:validateDoubleRange/>
</h:inputText>
 <h:message for="timeallowed" styleClass="validate"/>
  </div>
<br/>
  <!-- 4 attempts -->
  <div class="longtext">

  <h:outputLabel for="noattempts" value="#{authorMessages.number_of_attempts} : #{authorMessages.number_of_attempts_indic}" />
  <h:selectOneMenu id="noattempts" value="#{itemauthor.currentItem.numAttempts}" required="true">
  <f:selectItem itemLabel="#{authorMessages.unlimited}" itemValue="9999"/> <%-- 9999 indicates unlimited --%>
  <f:selectItem itemLabel="1" itemValue="1"/>
  <f:selectItem itemLabel="2" itemValue="2"/>
  <f:selectItem itemLabel="3" itemValue="3"/>
  <f:selectItem itemLabel="4" itemValue="4"/>
  <f:selectItem itemLabel="5" itemValue="5"/>
  <f:selectItem itemLabel="6" itemValue="6"/>
  <f:selectItem itemLabel="7" itemValue="7"/>
  <f:selectItem itemLabel="8" itemValue="8"/>
  <f:selectItem itemLabel="9" itemValue="9"/>
  <f:selectItem itemLabel="10" itemValue="10"/>
  </h:selectOneMenu>
</div>
 <h:message for="noattempts" styleClass="validate"/><br/>

  <!-- 5 PART -->

  <h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
   <f:verbatim>&nbsp;</f:verbatim>
   <h:outputLabel value="#{authorMessages.assign_to_p} " />
   <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
  </h:panelGrid>


  <!-- 6 POOL -->
  <h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
  <f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel value="#{authorMessages.assign_to_question_p} " />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

</h:panelGrid>


 <!-- FEEDBACK -->
 <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
  <f:verbatim><div class="longtext"></f:verbatim>
   <h:outputLabel value="#{commonMessages.feedback_optional}<br />" escape="false"/>

  <h:panelGrid >
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}" hasToggle="yes" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
 <f:verbatim> </div></f:verbatim>
</h:panelGroup>

 <!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="tier3"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel value="#{authorMessages.objective}" />
  <h:inputText id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputLabel value="#{authorMessages.keyword}" />
  <h:inputText id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputLabel value="#{authorMessages.rubric_colon}" />
  <h:inputText id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
</div>

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
