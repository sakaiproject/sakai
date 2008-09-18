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
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
    <html xmlns="http://www.w3.org/1999/xhtml">
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
  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='assessment'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='questionpool'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton accesskey="#{authorMessages.a_cancel}" rendered="#{itemauthor.target=='assessment'}" value="#{authorMessages.button_cancel}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{authorMessages.button_cancel}" action="editPool" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
 </h:commandButton>
</p>

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice SURVEY questions only -->
  <!-- text for answers are predetermined(in properties file), do not allow users to change -->

  <!-- 1 POINTS -->
  <div class="tier2">
<div class="shorttext">

    <h:outputLabel value="#{authorMessages.answer_point_value}"/>
    <h:outputText value="#{authorMessages.zeropoints}"/>
 </div>
<br/>
  <!-- 2 TEXT -->
  <div class="longtext">
  <h:outputLabel value="#{authorMessages.q_text}" />
  <!-- WYSIWYG -->
  <br/>
   
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes">
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

  <!-- 3 ANSWER -->
  <!-- qti survey type  
       PREDEFINED_SCALE: YES, AGREE, UNDECIDED, AVERAGE, STRONGLY_AGREE, EXCELLENT, 5, 10 
  -->

<div class="longtext">
    <h:outputLabel value="#{authorMessages.answer} " /> </div>
   <div class="tier2">
    <h:selectOneRadio layout="pageDirection" value="#{itemauthor.currentItem.scaleName}" id="selectscale">
     <f:selectItem itemValue="YES" itemLabel="#{authorMessages.yes_no}" />
     <f:selectItem itemValue="AGREE" itemLabel="#{authorMessages.disagree_agree}" />
     <f:selectItem itemValue="UNDECIDED" itemLabel="#{authorMessages.disagree_undecided}" />
     <f:selectItem itemValue="AVERAGE"
	itemLabel="#{authorMessages.below_average} -> #{authorMessages.above_average}" />
     <f:selectItem itemValue="STRONGLY_AGREE"
       itemLabel="#{authorMessages.strongly_disagree} -> #{authorMessages.strongly_agree}" />
     <f:selectItem itemValue="EXCELLENT"
        itemLabel="#{authorMessages.unacceptable} -> #{authorMessages.excellent}" />
     <f:selectItem itemValue="5" itemLabel="#{authorMessages.scale5}" />
     <f:selectItem itemValue="10" itemLabel="#{authorMessages.scale10}" />
    </h:selectOneRadio>
  <br />

  </div>
    <!-- 4 PART -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel rendered="#{itemauthor.target == 'assessment'}" value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}" id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
 </h:panelGrid>

    <!-- 5 POOL -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
<f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel rendered="#{itemauthor.target == 'assessment'}" value="#{authorMessages.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
  </h:panelGrid>

 <!-- FEEDBACK -->
 <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
 <f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{authorMessages.feedback_optional}<br />" />
<f:verbatim><div class="tier2"></f:verbatim>
  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}" hasToggle="yes">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
 <f:verbatim> </div></div></f:verbatim>
</h:panelGroup>

<!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="tier2"></f:verbatim>

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
</div>

<p class="act">

  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='assessment'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='questionpool'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton accesskey="#{authorMessages.a_cancel}" rendered="#{itemauthor.target=='assessment'}" value="#{authorMessages.button_cancel}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{authorMessages.button_cancel}" action="editPool" immediate="true">
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

