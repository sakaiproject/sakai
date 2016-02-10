<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

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
<!-- currentItem- ItemBean -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <samigo:script path="/js/authoring.js"/>
      <script type="text/JavaScript">
      	function textCounter(field,cntfield,maxlimit) {
          	
      		if (field.value.length > maxlimit) // if too long...trim it!
      		field.value = field.value.substring(0, maxlimit);
     		 // otherwise, update 'characters left' counter
      		else
      			cntfield.value = maxlimit - field.value.length;
      	}
      	</script>
     <style type="text/css">
      .panelGridColumn td {
    	  text-align: right;
      }
      </style>
      </head>
    <body onload="countNum();<%= request.getAttribute("html.body.onload") %>">


<div class="portletBody">
<%-- content... --%>
<%-- FORM --%>

<%-- HEADING --%>
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm" >
<p class="act">
 <h:commandButton  rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton  rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
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

 <!-- 1 POINTS -->
  <div class="tier2">
   <div class="shorttext"> <h:outputLabel value="#{authorMessages.answer_point_value}" />
    	<h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true" size="6" onchange="toPoint(this.id);">
		<f:validateDoubleRange /></h:inputText>
		<h:message for="answerptr" styleClass="validate" />
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
	
 <!-- 2 TEXT -->
  <div class="longtext">
  	<h:outputLabel value="#{authorMessages.q_text}" />
  	<!-- WYSIWYG -->
  	<br/>
  </div>
  <div class="tier2">
  	<h:panelGrid>
   	<samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
     	<f:validateLength minimum="1" maximum="4000"/>
   	</samigo:wysiwyg>
  	</h:panelGrid>
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

<!-- 3 ANSWER -->
<div class="longtext">
    <h:outputLabel value="#{authorMessages.answer} " /> </div>
<br />
<div class="tier2">
	<h:graphicImage alt="#{evaluationMessages.alt_matrixChoicesSurvey}" url="/images/matrixChoicesSurvey.gif" />
	<br/>
		<h:outputLabel value="#{authorMessages.rowchoices}"/> 
		<br/>
		 
		<div class="tier3">
			<h:inputTextarea id="rowData" value="#{itemauthor.currentItem.rowChoices}" rows="6" cols="54" />
    		</div>
    		<f:verbatim><br /></f:verbatim>
 		<h:outputLabel value="#{authorMessages.columnchoices}"/>
 		<div class="tier3">
          <h:panelGrid columns= "1" >
           			<h:inputTextarea id="columnData" 
 							value="#{itemauthor.currentItem.columnChoices}" 
 							rows="6" 
 							cols="54"
 							immediate="true"/>
 			<br/>
  		</h:panelGrid>
 		
		<br/>
		<h:selectBooleanCheckbox id="forceRankingCheckbox" value="#{itemauthor.currentItem.forceRanking}"/>
    	<h:outputText value="#{authorMessages.forceRanking}" />
    	<br/>	
    	<h:selectBooleanCheckbox id="addCommentCheckbox" 
    		immediate = "true"
    		value="#{itemauthor.currentItem.addComment}" 
    		onchange="this.form.submit();"
    		valueChangeListener="#{itemauthor.currentItem.toggleAddComment}" />
    	<h:outputText value="#{authorMessages.addComment}" />
    	<br/>
		<h:panelGrid columns="2" columnClasses="shorttext" rendered="#{itemauthor.currentItem.addComment}" >
			<h:outputText value="#{authorMessages.fieldLabel}"/> 
			<h:panelGrid columns="1" styleClass="panelGridColumn">	
				<h:inputTextarea id="commentField" 
 							value="#{itemauthor.currentItem.commentField}" 
 							rows="3" 
 							cols="48"
 							onkeydown="javascript:textCounter(document.getElementById('itemForm:commentField'),document.getElementById('itemForm:remLen2'), 200);"
 							onkeyup="javascript:textCounter(document.getElementById('itemForm:commentField'),document.getElementById('itemForm:remLen2'), 200);"
 							onclick="javascript:if(this.value=='#{authorMessages.character_limit}') this.value='';"/>
 				<h:panelGroup>
 					<h:outputText  value="#{authorMessages.character_count}" style="font-weight:bold;color:red;"/>
 					<h:inputText readonly="true" id="remLen2" size="2" maxlength="3" value="#{200 - itemauthor.currentItem.commentFieldLenght}"/>
 				</h:panelGroup>
 			</h:panelGrid>
		</h:panelGrid>
					<br/>
	
    	<h:outputText value="#{authorMessages.relativeWidthOfColumns}" /><br/>
    	<h:panelGroup><h:column><h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" escape="false" />
        </h:column><h:column>
        	<h:selectOneMenu id="relativeWidth" value="#{itemauthor.currentItem.selectedRelativeWidth}">
     		<f:selectItems  value="#{itemauthor.selectRelativeWidthList}" />
  			</h:selectOneMenu>
        </h:column>
        </h:panelGroup>

    <br/>
    </div>
</div>
<div class="tier2">
<%-- 3 PART --%>
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
  <h:outputLabel value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <%-- use this in real  value="#{section.sectionNumberList}" --%>
  </h:selectOneMenu>
</h:panelGrid>
<%-- 5 POOL --%>
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">

  <h:outputLabel value="#{authorMessages.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
</h:panelGrid>

<%-- FEEDBACK --%>

 <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
 <f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{commonMessages.feedback_optional}<br />" escape="false"/>
<f:verbatim><div class="tier2"></f:verbatim>
  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}" hasToggle="yes" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
 <f:verbatim> </div></div></f:verbatim>
</h:panelGroup>
</div>

 <%-- METADATA --%>
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
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

 <h:commandButton  rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton  rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton  rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
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
