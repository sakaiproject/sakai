<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
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
    <!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <samigo:script path="/js/authoring.js"/>
      </head>
<body onload="countNum();<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody container-fluid">
<!-- content... -->
<!-- FORM -->
<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">

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

<!-- QUESTION PROPERTIES -->
  <!-- 1 POINTS -->
  <div class="form-group row"> 
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.answer_point_value}" />
    <div class="col-md-2">
      <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" required="true" disabled="#{author.isEditPoolFlow}" styleClass="form-control ConvertPoint">
	    <f:validateDoubleRange minimum="0.00"/>
	  </h:inputText>
	<h:message for="answerptr" styleClass="validate"/>
    </div>
  </div>

  <div class="form-group row">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.answer_point_value_display}" />
	<div class="col-md-10">
      <t:selectOneRadio id="itemScore" value="#{itemauthor.currentItem.itemScoreDisplayFlag}" layout="spread">
        <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
        <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
      </t:selectOneRadio>
      <ul class="item-score">
        <li><t:radio for="itemScore" index="0" /></li> 
        <li><t:radio for="itemScore" index="1" /></li> 
      </ul>
    </div>
  </div>

<!-- 1.2 MIN POINTS -->
<f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}" >
  <div class="form-group row">
    <h:outputLabel value="#{authorMessages.answer_min_point_value}" styleClass="col-md-2"/>
    <div class="col-md-2">
        <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" styleClass="form-control ConvertPoint">
	      <f:validateDoubleRange/>
	    </h:inputText>
        <h:outputText value="#{authorMessages.answer_min_point_info}" style="font-size: x-small" />
	    <h:message for="answerminptr" styleClass="validate"/>
    </div>
  </div>
</f:subview>

  <!-- 2 TEXT -->
  <div class="form-group row">  
  <h:outputLabel value="#{authorMessages.q_text}" styleClass="col-md-2 form-control-label"/>
      <div class="col-md-8">
          <!-- WYSIWYG -->
          <h:panelGrid>
              <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
                  <f:validateLength minimum="1" maximum="60000"/>
              </samigo:wysiwyg>
          </h:panelGrid>
      </div>
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

   <!-- 3 PART -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.assign_to_p} " />
    <div class="col-md-10">
      <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
        <f:selectItems value="#{itemauthor.sectionSelectList}" />
      </h:selectOneMenu>
    </div>
  </h:panelGroup>


  <!-- 4 POOL -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.assign_to_question_p} " />
    <div class="col-md-10">
      <h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}" id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
        <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
        <f:selectItems value="#{itemauthor.poolSelectList}" />
      </h:selectOneMenu>
    </div>
  </h:panelGroup>


  <!-- 5 ANSWER and ANSWERFEEDBACK -->
  <h2>
    <h:outputText value="#{authorMessages.answer_provide_a_mo}" />  
  </h2>

  <div class="form-group row">
    <h:outputLabel value="#{authorMessages.model_short_answer}" styleClass="col-md-2 form-control-label"/>
    <div class="col-md-8">
        <h:panelGrid>
            <!-- WYSIWYG -->
            <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrAnswer}" hasToggle="yes" mode="author">
                <f:validateLength maximum="60000"/>
            </samigo:wysiwyg>
        </h:panelGrid>
     </div>
 </div>

 <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
  <h:outputLabel value="#{commonMessages.feedback_optional}"  styleClass="col-md-2 form-control-label"/>
    <div class="col-md-8">
        <!-- WYSIWYG  -->
        <h:panelGrid>
            <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}" hasToggle="yes" mode="author">
                <f:validateLength maximum="60000"/>
            </samigo:wysiwyg>
        </h:panelGrid>
    </div>
</h:panelGroup>




<!-- METADATA -->


<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<h:outputLabel value="Metadata"/><br/>


<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputText value="#{authorMessages.objective}" />
  <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputText value="#{authorMessages.keyword}" />
  <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputText value="#{authorMessages.rubric_colon}" />
  <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>

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
