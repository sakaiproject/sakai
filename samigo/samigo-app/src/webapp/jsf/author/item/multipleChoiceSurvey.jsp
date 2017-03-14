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

<div class="portletBody container-fluid">
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

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice SURVEY questions only -->
  <!-- text for answers are predetermined(in properties file), do not allow users to change -->

    <!-- 1 POINTS -->
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.answer_point_value}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-2">
            <h:outputText value="#{authorMessages.zeropoints}"/>
        </div>
    </div>
    <br/>
    
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.answer_point_value_display}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-5 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
        </div>
    </div>    
    
  <!-- 2 TEXT -->
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.q_text}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <!-- WYSIWYG -->
        <div class="col-md-8">
            <h:panelGrid>
            <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
                <f:validateLength minimum="1" maximum="60000"/>
            </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </div>

    <!-- 2a ATTACHMENTS -->
    <%@ include file="/jsf/author/item/attachment.jsp" %>

    <!-- 3 ANSWER -->
    <!-- qti survey type  
       PREDEFINED_SCALE: YES, AGREE, UNDECIDED, AVERAGE, STRONGLY_AGREE, EXCELLENT, 5, 10 
    -->

    <div class="longtext">
        <h:outputLabel value="#{authorMessages.answer} " /> </div>
        <div class="samigo-radio">
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
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">    
        <h:outputLabel rendered="#{itemauthor.target == 'assessment'}" value="#{authorMessages.assign_to_p}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-8">
            <h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}" id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                <f:selectItems  value="#{itemauthor.sectionSelectList}" />
            </h:selectOneMenu>
        </div>
    </h:panelGroup>

    <!-- 5 POOL -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
        <h:outputLabel rendered="#{itemauthor.target == 'assessment'}" value="#{authorMessages.assign_to_question_p}" 
                    styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-8">                    
            <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
                <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
                <f:selectItems value="#{itemauthor.poolSelectList}" />
            </h:selectOneMenu>
        </div>
    </h:panelGroup>

    <!-- FEEDBACK -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
        <h:outputLabel value="#{commonMessages.feedback_optional}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <!-- WYSIWYG -->
        <div class="col-md-8">
            <h:panelGrid>
                <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}" hasToggle="yes" mode="author">
                    <f:validateLength maximum="60000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div> 
    </h:panelGroup>

    <!-- METADATA -->
    <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
        <h:outputLabel value="Metadata"/>
        <div class="form-group row">
            <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-4 col-lg-2 form-control-label" />
            <div class="col-md-5 col-lg-3">
                <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="keyword" value="#{authorMessages.keyword}" styleClass="col-md-4 col-lg-2 form-control-label" />
            <div class="col-md-5 col-lg-3">
                <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" styleClass="col-md-4 col-lg-2 form-control-label" />
            <div class="col-md-5 col-lg-3">
                <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
            </div>
        </div> 
    </h:panelGroup>


    <%@ include file="/jsf/author/item/tags.jsp" %>

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

