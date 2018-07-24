<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id:  $
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
      <script type="text/javascript" src="/samigo-app/js/authoring.js"></script>
      </head>
      <body onload="countNum();<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody container-fluid">
<%-- content... --%>
<%-- FORM --%>

<%-- HEADING --%>
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
    <p class="act">
        <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
        </h:commandButton>
        
        <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
        </h:commandButton>

        <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
        </h:commandButton>
        
        <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        </h:commandButton>
    </p>

    <%-- QUESTION PROPERTIES --%>
    <%-- 1 POINTS --%>
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

    <!-- 1.2 MIN POINTS -->
    <f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">
        <div class="form-group row">   
            <h:outputLabel for="answerminptr" value="#{authorMessages.answer_min_point_value}" styleClass="col-md-4 form-control-label"/>
            <div class="col-md-2">
                <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" styleClass="form-control ConvertPoint">
                    <f:validateDoubleRange />
                </h:inputText>    
                <h:outputText value="#{authorMessages.answer_min_point_info}"/>
                <h:message for="answerminptr" styleClass="validate"/>
            </div>
        </div>
    </f:subview>

    <!-- DISCOUNT -->
    <div class="form-group row" >
        <h:outputLabel value="#{authorMessages.answer_point_value_display}" styleClass="col-md-4 form-control-label"/>
        <div class="col-md-5 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
        </div>
    </div>

    <%@ include file="/jsf/author/item/rubricAssociation.jsp" %>

    <%-- 2 QUESTION TEXT --%>
    <div class="longtext"> 
        <h:outputLabel value="#{authorMessages.q_text}" /><br/>
    </div>
    <div>
    <div>
            <h:outputText value="#{authorMessages.defining_answers}<br/>#{authorMessages.note_defining_answers}" escape="false"/>
            <h:outputText value=" " escape="false"/>
            <h:panelGrid columns="2" border="0">
                <h:outputText value=" " escape="false"/>
                <h:outputText value="#{authorMessages.range}<br/>#{authorMessages.range_example}" escape="false"/>
                <h:outputText value=" " escape="false"/>
                <h:outputText value="#{authorMessages.scientific_notataion}<br/>#{authorMessages.scientific_notataion_example}" escape="false"/>
                <h:outputText value=" " escape="false"/>
                <h:outputText value="#{authorMessages.complex_numbers}<br/>#{authorMessages.complex_numbers_example}" escape="false"/>
            </h:panelGrid >
            <h:outputText value=" " escape="false"/>
            <h:outputText value="#{authorMessages.accepted_characters}<br/>" escape="false"/>
            <div class="mathjax-warning alert alert-warning" style="display: none;">
                <h:outputText value="#{authorMessages.mathjax_usage_warning}" escape="false"/>
            </div>
            <h:outputText value="#{authorMessages.note_accepted_fin_1}<br/>
                                 #{authorMessages.note_accepted_fin_2}<br/>
                                 #{authorMessages.note_accepted_fin_3}" escape="false"/>
        </div> 
    </div><br/>
  
    <h:panelGrid>
        <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
            <f:validateLength maximum="60000"/>
            <f:validator validatorId="finQuestionValidator"/>
        </samigo:wysiwyg>
    </h:panelGrid>
    <br />

    <!-- 2a ATTACHMENTS -->
    <%@ include file="/jsf/author/item/attachment.jsp" %>

    <%-- 3 PART --%>
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
        <h:outputLabel value="#{authorMessages.assign_to_p}" styleClass="col-md-4 form-control-label"/>
        <div class="col-md-8">
            <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                <f:selectItems  value="#{itemauthor.sectionSelectList}" />
                <%-- use this in real  value="#{section.sectionNumberList}" --%>
            </h:selectOneMenu>
       </div>
    </h:panelGroup>
    
    <%-- 5 POOL --%>
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
    
    <%-- FEEDBACK --%>
    <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
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
            <h:outputLabel value="#{authorMessages.incorrect_answer_op}" styleClass="col-md-4 form-control-label"/>
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

     <%-- METADATA --%>
    <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
        <h:outputLabel value="Metadata"/><br/>
        <div class="form-group row">            
            <h:outputText value="#{authorMessages.objective}" styleClass="col-md-4 form-control-label"/>
            <div class="col-md-5">
                <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputText value="#{authorMessages.keyword}" styleClass="col-md-4 form-control-label"/>
            <div class="col-md-5">
                <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputText value="#{authorMessages.rubric_colon}" styleClass="col-md-4 form-control-label"/>
            <div class="col-md-5">                
                <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
            </div>
        </div> 
    </h:panelGroup>

    <%@ include file="/jsf/author/item/tags.jsp" %>

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
