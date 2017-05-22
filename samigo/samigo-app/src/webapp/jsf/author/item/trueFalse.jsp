
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
    <!-- 1 POINTS -->
    <div class="form-group row">
        <h:outputLabel for="answerptr" value="#{authorMessages.answer_point_value}" styleClass="col-md-4 form-control-label"/>
        <div class="col-md-2">
            <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" 
                         required="true" disabled="#{author.isEditPoolFlow}" styleClass="form-control ConvertPoint">
                <f:validateDoubleRange minimum="0.00"/>
            </h:inputText>
            <h:message for="answerptr" styleClass="validate"/>
        </div>
    </div>
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.answer_point_value_display}" styleClass="col-md-4 form-control-label"/>
        <div class="col-md-5 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
        </div>
    </div>

    <!-- 1.2 MIN POINTS -->
    <f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">
        <div class="form-group row">   
            <h:outputLabel for="answerminptr" value="#{authorMessages.answer_min_point_value}" 
                           styleClass="col-md-4 form-control-label"/>
            <div class="col-md-2">
                <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" styleClass="form-control ConvertPoint">
                    <f:validateDoubleRange />
                </h:inputText>    
                <h:outputText value="#{authorMessages.answer_min_point_info}"/>
                <h:message for="answerminptr" styleClass="validate"/>
            </div>
        </div>
    </f:subview>

    <!-- NEGATIVE POINT -->
    <div class="form-group row">
        <h:outputLabel for="answerdsc" value="#{authorMessages.negative_point_value}" styleClass="col-md-4 form-control-label"/>
        <div class="col-md-2">
            <h:inputText id="answerdsc" value="#{itemauthor.currentItem.itemDiscount}" 
                         required="true" styleClass="form-control ConvertPoint">
                        <f:validateDoubleRange/>
            </h:inputText>
            <h:message for="answerdsc" styleClass="validate"/>
            <h:outputText value="#{authorMessages.note_negative_point_value_question}" />
        </div>
    </div>

    <!-- 2 TEXT -->
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.q_text}" styleClass="col-md-4 form-control-label"/>
        <!-- STUB FOR WYSIWYG -->
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
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.answer} " styleClass="col-md-4 form-control-label"/>
        <div class="col-md-8 samigo-inline-radio">
            <h:selectOneRadio layout="lineDirection" id="TF" border="0"
                              value="#{itemauthor.currentItem.corrAnswer}">
                <f:selectItems value="#{itemauthor.trueFalseAnswerSelectList}" />
            </h:selectOneRadio>
        </div>
    </div>
    
    <!-- 4 RATIONALE -->
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.req_rationale}" styleClass="col-md-4 form-control-label"/>
        <div class="col-md-2 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" id="rational" required="true">
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
            <h:message for="rational" styleClass="validate"/>
        </div>
    </div>

    <!-- 5 PART -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment'}">
        <h:panelGroup rendered="#{!author.isEditPoolFlow}">
            <h:outputLabel value="#{authorMessages.assign_to_p}" styleClass="col-md-4 form-control-label"/>
            <div class="col-md-8">
                <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                    <f:selectItems  value="#{itemauthor.sectionSelectList}" />
                </h:selectOneMenu>
            </div>
        </h:panelGroup>
    </h:panelGroup>

    <!-- 6 POOL -->
    <h:panelGroup styleClass="form-group row" layout="block" 
                  rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">  
        <h:outputLabel value="#{authorMessages.assign_to_question_p}" styleClass="col-md-4 form-control-label"/>
        <div class="col-md-8">
            <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
                <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
                <f:selectItems value="#{itemauthor.poolSelectList}" />
            </h:selectOneMenu>
        </div>
    </h:panelGroup>

    <!-- FEEDBACK -->
    <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}"><br/>
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.correct_incorrect_an}" styleClass="col-md-12 form-control-label"/>
        </div>
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.correct_answer_opti}" styleClass="col-md-4 form-control-label"/>
            <!-- WYSIWYG -->
            <div class="col-md-8">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
                </h:panelGrid>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel  value="#{authorMessages.incorrect_answer_op}" styleClass="col-md-4 form-control-label"/>
            <!-- WYSIWYG -->
            <div class="col-md-8">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
                </h:panelGrid>
            </div>
        </div>
    </h:panelGroup>

    <!-- METADATA -->
    <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
        <h:outputLabel value="Metadata"/><br/>
        <div class="form-group row">
            <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-4 form-control-label"/>
            <div class="col-md-5">
                <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
            </div>
       </div>
       <div class="form-group row">
           <h:outputLabel for="keyword" value="#{authorMessages.keyword}" styleClass="col-md-4 form-control-label"/>
           <div class="col-md-5">
               <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
           </div>
       </div>
       <div class="form-group row">
           <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" styleClass="col-md-4 form-control-label"/>
           <div class="col-md-5">
               <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
           </div>
       </div>
    </h:panelGroup>

    <%@ include file="/jsf/author/item/tags.jsp" %>


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
