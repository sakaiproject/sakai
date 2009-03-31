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
<%-- "checked in wysiwyg code but disabled, added in lydia's changes between 1.9 and 1.10" --%>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <!-- HTMLAREA -->
      <samigo:stylesheet path="/htmlarea/htmlarea.css"/>
      <samigo:script path="/htmlarea/htmlarea.js"/>
      <samigo:script path="/htmlarea/lang/en.js"/>
      <samigo:script path="/htmlarea/dialog.js"/>
      <samigo:script path="/htmlarea/popupwin.js"/>
      <samigo:script path="/htmlarea/popups/popup.js"/>
      <samigo:script path="/htmlarea/navigo_js/navigo_editor.js"/>
      <samigo:script path="/jsf/widget/wysiwyg/samigo/wysiwyg.js"/>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
<%--
<script language="javascript" type="text/JavaScript">
<!--
<%@ include file="/js/authoring.js" %>
//-->
</script>
--%>
      </head>
<%-- unfortunately have to use a scriptlet here --%>
<body onload="<%= request.getAttribute("html.body.onload") %>">
<%--
      <body onload="javascript:initEditors('<%=request.getContextPath()%>');;<%= request.getAttribute("html.body.onload") %>">
--%>

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
  <!-- this is for creating multiple choice questions -->
  <%-- kludge: we add in 1 useless textarea, the 1st does not seem to work --%>
  <div style="display:none">
  <h:inputTextarea id="ed0" cols="10" rows="10" value="            " />
  </div>

  <!-- 1 POINTS -->
  <div class="tier2">
   <div class="shorttext"> <h:outputLabel value="#{authorMessages.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true">
<f:validateDoubleRange/>
</h:inputText>
<br/><h:message for="answerptr" styleClass="validate"/>
  </div>
<br/>
  <!-- 2 TEXT -->
  <div class="longtext"> <h:outputLabel value="#{authorMessages.q_text}" />
  <br/>
  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.instruction}" hasToggle="yes">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

  <!-- 3 ANSWER -->
  <div class="longtext"> <h:outputLabel value="#{authorMessages.create_pairing} " /></div>
<div class="tier2">
  <!-- display existing pairs -->

<h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="pairs" value="#{itemauthor.currentItem.matchItemBeanList}" var="pair">
      
      <h:column>
        <f:facet name="header">
          
          <h:outputText value=""  />
        </f:facet>

          <h:outputText value="#{pair.sequence}"  />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.matching_choice_col}"  />
        </f:facet>
          <h:outputText escape="false" value="#{pair.choice}"  />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.matching_match_col}"  />
        </f:facet>
          <h:outputText escape="false" value="#{pair.match}"  />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value=""/>
        </f:facet>

     <h:panelGrid>
     <h:panelGroup>
<h:commandLink rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}" id="modifylink" immediate="true" action="#{itemauthor.currentItem.editMatchPair}">
  <h:outputText id="modifytext" value="#{authorMessages.button_edit}"/>
  <f:param name="sequence" value="#{pair.sequence}"/>
</h:commandLink>

          <h:outputText value="#{authorMessages.matching_currently_editing}" rendered="#{itemauthor.currentItem.currentMatchPair.sequence== pair.sequence}"/>
          <h:outputText value=" #{authorMessages.separator} " rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}"/>

<h:commandLink id="removelink" immediate="true" action="#{itemauthor.currentItem.removeMatchPair}" rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}">
  <h:outputText id="removetext" value="#{authorMessages.button_remove}"/>
  <f:param name="sequence" value="#{pair.sequence}"/>
</h:commandLink>
     </h:panelGroup>
     </h:panelGrid>
      </h:column>

     </h:dataTable>
<h:outputLabel value="<p>#{authorMessages.no_matching_pair}</p>" rendered="#{itemauthor.currentItem.matchItemBeanList eq '[]'}"/>

</div>
        <!-- WYSIWYG -->
<div class="tier2">
   
          <h:outputText value=" #{authorMessages.matching_choice_col}"/>
<h:panelGrid>
  <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.choice}" hasToggle="yes">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
</h:panelGrid>
          <h:outputText value=" #{authorMessages.matching_match_col}"/>

 <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.match}" hasToggle="yes">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

   </h:panelGrid>
</div>

 <!-- Match FEEDBACK -->

<div class="tier2">
  
<!-- WYSIWYG -->
<h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'))}">
  <h:outputText value="#{authorMessages.correct_match_feedback_opt}"/>
  <f:verbatim><br/></f:verbatim>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.corrMatchFeedback}" hasToggle="yes">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
   </h:panelGrid>

  <f:verbatim><br/></f:verbatim>

  <!-- WYSIWYG -->
  <h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'))}">
   <h:outputText value="#{authorMessages.incorrect_match_feedback_opt}"/>
   <f:verbatim><br/></f:verbatim>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.incorrMatchFeedback}" hasToggle="yes">
     <f:validateLength maximum="4000"/>
  </samigo:wysiwyg>
  </h:panelGrid>

<f:verbatim><br/></f:verbatim>
  </div>


<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<div class="tier2">
  <h:commandButton accesskey="#{authorMessages.a_create}" value="#{authorMessages.button_save_pair}" action="#{itemauthor.currentItem.addMatchPair}">
  </h:commandButton>
</div>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>

<%--
    <!-- 4 RANDOMIZE -->
   <div class="longtext">  <h:outputText value="#{authorMessages.randomize_answers}" />
    <h:selectOneRadio value="#{itemauthor.currentItem.randomized}" >
     <f:selectItem itemValue="true"
       itemLabel="#{authorMessages.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
  </div>


    <!-- 5 RATIONALE -->
   <div class="longtext"> <h:outputText value="#{authorMessages.req_rationale}" />
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" >
     <f:selectItem itemValue="true"
       itemLabel="#{authorMessages.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
  </div>

--%>
    <!-- 6 PART -->

<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim>&nbsp;</f:verbatim>
<h:outputLabel value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>

  </h:panelGrid>

    <!-- 7 POOL -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
<f:verbatim>&nbsp;</f:verbatim>  <h:outputLabel value="#{authorMessages.assign_to_question_p}" />
<%-- stub debug --%>
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

  </h:panelGrid><br/>


 <!-- 8 FEEDBACK -->
  <f:verbatim></f:verbatim>
<f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{authorMessages.correct_incorrect_an}" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}"/>
<f:verbatim><br/></br/></div><div class="tier2"></f:verbatim>

<h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
  <h:outputText value="#{authorMessages.correct_answer_opti}" />
  <f:verbatim><br/></f:verbatim>
  <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
 </h:panelGrid>

<f:verbatim><br/></f:verbatim>

 <h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
  <h:outputText value="#{authorMessages.incorrect_answer_op}"/>
  <f:verbatim><br/></f:verbatim>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
 </h:panelGrid>

<f:verbatim><br/></div></f:verbatim>

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

