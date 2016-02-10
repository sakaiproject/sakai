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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
      </head>
<body onload="<%= request.getAttribute("html.body.onload") %>;resetInsertAnswerSelectMenus();disablePartialCreditField();">

<div class="portletBody">
<!-- content... -->
<!-- FORM -->
<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm" onsubmit="return editorCheck();">
<p class="act">
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}"  styleClass="active">
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

  <!-- NOTE:  Had to call this.form.onsubmit(); when toggling between single  -->
  <!-- and multiple choice, or adding additional answer choices.  -->
  <!-- to invoke the onsubmit() function for htmlarea to save the htmlarea contents to bean -->
  <!-- otherwise, when toggleing or adding more choices, all contents in wywisyg editor are lost -->

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice questions -->
  <!-- 1 POINTS -->
<div class="tier2">

<div class="shorttext"> <h:outputLabel value="#{authorMessages.answer_point_value}" />
<h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true"  disabled="#{author.isEditPoolFlow}" size="6" onchange="toPoint(this.id);">
<f:validateDoubleRange minimum="0.00" />
</h:inputText>

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

<script type="text/javascript">
function toggleNegativePointVal(val){
	var negPointField = document.getElementById('itemForm:answerdsc');
	if(negPointField){
		if(val){
			negPointField.value = 0;
			negPointField.disabled = true;
		}else{
			negPointField.disabled = false;
		}
	}
}
</script>

<f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">
<f:verbatim>
<div class="shorttext">
</f:verbatim>
<h:outputLabel value="#{authorMessages.answer_min_point_value}" />
    <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" size="6"  onchange="toPoint(this.id); toggleNegativePointVal(this.value);">
<f:validateDoubleRange /></h:inputText>
<f:verbatim><div></f:verbatim>
<h:outputText value="#{authorMessages.answer_min_point_info}" style="font-size: x-small" />
<f:verbatim></div></f:verbatim>
<h:message for="answerminptr" styleClass="validate"/>
<f:verbatim>
  </div>
<br/>
</f:verbatim>
</f:subview>

<!-- 1 ANSWER -->
<div class="longtext">
  <h:outputLabel value="#{authorMessages.answer} " />  
    <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/mcWhatsThis.faces','MCWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('/samigo-app/jsf/author/mcWhatsThis.faces','MCWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
    <h:outputText  value=" (#{assessmentSettingsMessages.whats_this_link})"/>
  </h:outputLink>
</div>
<!-- need to add a listener, for the radio button below,to toggle between single and multiple correct-->
<div  id= class="tier2">
  <h:selectOneRadio id="chooseAnswerTypeForMC" layout="pageDirection"
	 		        onclick="this.form.onsubmit();this.form.submit();"
                    onkeypress="this.form.onsubmit();this.form.submit();"
                    value="#{itemauthor.currentItem.itemType}"
	                valueChangeListener="#{itemauthor.currentItem.toggleChoiceTypes}" >
    <f:selectItem itemValue="1" itemLabel="#{commonMessages.multiple_choice_sin}" />  
    <f:selectItem itemValue="12" itemLabel="#{commonMessages.multipl_mc_ss}" /> 
    <f:selectItem itemValue="2"   itemLabel="#{commonMessages.multipl_mc_ms}" />
  </h:selectOneRadio>
</div>
<!-- partial credit vs negative marking -->
<div id="partialCredit_toggle" class="tier3">
  <h:panelGroup id="partialCredit_JSF_toggle" 
   				rendered="#{itemauthor.currentItem.itemType == 1 && itemauthor.currentItem.partialCreditEnabled==true}">
    <h:selectOneRadio id="partialCreadit_NegativeMarking"
					  layout="pageDirection"
					  onclick="this.form.onsubmit();this.form.submit();"
					  onkeypress="this.form.onsubmit();this.form.submit();"
					  value="#{itemauthor.currentItem.partialCreditFlag}"
					  valueChangeListener="#{itemauthor.currentItem.togglePartialCredit}">
      <f:selectItem itemValue="false" itemLabel="#{authorMessages.enable_nagative_marking}"  />
      <f:selectItem itemValue="true" itemLabel="#{authorMessages.enable_partial_credit}"  />
    </h:selectOneRadio>
  <h:panelGroup>
	<h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" escape="false" />
    <h:commandLink  title="#{authorMessages.reset_grading_logic}"
					id="resetlink" 
					rendered="#{itemauthor.currentItem.itemType == 1}"
					onkeypress="this.form.onsubmit();this.form.submit();"
					action="#{itemauthor.currentItem.resetToDefaultGradingLogic}">
      <h:outputText id="resetLinkText" value="#{authorMessages.reset_grading_logic}"  />
    </h:commandLink><!-- TODO  Need to un-check all the radio buttons as well-->
  </h:panelGroup>
  </h:panelGroup>
</div>

<!-- multiple choice, multiple selection: full or partial credit -->
<div id="mcms_credit_toggle" class="tier3">
  <h:panelGroup id="mcms_credit_JSF_toggle" 
   				rendered="#{itemauthor.currentItem.itemType == 2}">
    <h:selectOneRadio id="mcms_credit_partial_credit"
					  layout="pageDirection"
					  onclick="this.form.onsubmit();this.form.submit();"
					  onkeypress="this.form.onsubmit();this.form.submit();"
					  value="#{itemauthor.currentItem.mcmsPartialCredit}">
      <f:selectItem itemValue="true" itemLabel="#{commonMessages.mutlipl_mc_ms_partial_credit}"  />
      <f:selectItem itemValue="false" itemLabel="#{commonMessages.multipl_mc_ms_full_credit}"  />
    </h:selectOneRadio>
  </h:panelGroup>
</div>


    
<div id="discountDiv" class="longtext">
  <h:panelGroup id="discountTable"
        rendered="#{(itemauthor.currentItem.itemType==1 &&(itemauthor.currentItem.partialCreditFlag=='false'||itemauthor.currentItem.partialCreditEnabled==false))
        || itemauthor.currentItem.itemType==12 || (itemauthor.currentItem.itemType==2 && itemauthor.currentItem.mcmsPartialCredit=='false')}">
  <h:outputText value="&nbsp;&nbsp;" escape="false" />
  <h:outputLabel value="#{authorMessages.negative_point_value}"/>
  <h:inputText id="answerdsc" value="#{itemauthor.currentItem.itemDiscount}" required="true" onchange="toPoint(this.id);">
    <f:validateDoubleRange/>
  </h:inputText>
  <f:verbatim> <script type="text/javascript" defer='defer'>
  		var itemType = "${itemauthor.currentItem.itemType}";
  		var discDiv=document.getElementById('discountDiv');
		
  		if(itemType == 1) {
		  	var toggleDiv=document.getElementById('itemForm:partialCreadit_NegativeMarking');
	    	if( typeof(toggleDiv) != 'undefined' && toggleDiv != null){
	    		toggleDiv.rows[0].cells[0].appendChild(discDiv);
	    	}else {
	       	 	var QtypeTable=document.getElementById('itemForm:chooseAnswerTypeForMC');
	       	 	QtypeTable.rows[0].cells[0].appendChild(discDiv);
	        }   
  		} else{
		    	if(itemType == 12) {
		            var QtypeTable=document.getElementById('itemForm:chooseAnswerTypeForMC');
	        	    QtypeTable.rows[1].cells[0].appendChild(discDiv);
			 }
			 if(itemType == 2) {
			     var mcmsPartialCredit = "${itemauthor.currentItem.mcmsPartialCredit}";
   		    	     if(mcmsPartialCredit == 'false') {
		    	          var QtypeTable=document.getElementById('itemForm:mcms_credit_partial_credit');
			     	  QtypeTable.rows[1].cells[0].appendChild(discDiv);
			     }
			 }
		}
    </script>
  </f:verbatim>
</h:panelGroup>
</div>


  <!-- 2 TEXT -->
	<div class="multiple_editor">
		<a id="multiple_show_editor" onclick="javascript:show_multiple_text(this);" href="#">
 	  <h:outputText id="text" value="#{authorMessages.show_editors}"/> 
	</a>
	</div>
   <div class="longtext"><h:outputLabel value="#{authorMessages.q_text}" />
</div>
  <!-- WYSIWYG -->
   
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="plain" mode="author">
     <f:validateLength minimum="1" maximum="60000"/>
   </samigo:wysiwyg>

  </h:panelGrid>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

	<!-- dynamicaly generate rows of answers based on number of answers-->
<div class="tier2">
 <h:dataTable id="mcchoices" value="#{itemauthor.currentItem.multipleChoiceAnswers}" var="answer" headerClass="navView longtext">
<h:column>
<h:panelGrid columns="2" border="0">
<h:panelGroup>
      

	
<h:outputText value="#{authorMessages.correct_answer}"  />
<f:verbatim><br/></f:verbatim>
<!-- if multiple correct, use checkboxes -->
        <h:selectManyCheckbox value="#{itemauthor.currentItem.corrAnswers}" id="mccheckboxes"
	rendered="#{itemauthor.currentItem.itemType == 2 || itemauthor.currentItem.itemType == 12}">
	<f:selectItem itemValue="#{answer.label}" itemLabel="#{answer.label}"/>
        </h:selectManyCheckbox>

<h:commandLink title="#{authorMessages.t_removeC}" id="removelink" onfocus="document.forms[1].onsubmit();" action="#{itemauthor.currentItem.removeChoices}" rendered="#{itemauthor.currentItem.multipleCorrect}">
  <h:outputText id="text" value="#{commonMessages.remove_action}"/>
  <f:param name="answerid" value="#{answer.label}"/>
</h:commandLink>		 

	<!-- if single correct, use radiobuttons -->
<h:selectOneRadio onclick="uncheckOthers(this);" onkeypress="uncheckOthers(this);" id="mcradiobtn"
	layout="pageDirection"
	value="#{itemauthor.currentItem.corrAnswer}"
	rendered="#{itemauthor.currentItem.itemType == 1}">

	<f:selectItem itemValue="#{answer.label}" itemLabel="#{answer.label}"/>
</h:selectOneRadio>

<h:commandLink title="#{authorMessages.t_removeC}" id="removelinkSingle" onfocus="document.forms[1].onsubmit();" action="#{itemauthor.currentItem.removeChoicesSingle}" rendered="#{!itemauthor.currentItem.multipleCorrect}">
  <h:outputText id="textSingle" value="#{commonMessages.remove_action}"/>
  <f:param name="answeridSingle" value="#{answer.label}"/>
</h:commandLink>
 </h:panelGroup>
        <!-- WYSIWYG -->
 <h:panelGrid>

   <samigo:wysiwyg rows="140" value="#{answer.text}" hasToggle="plain" mode="author">
     <f:validateLength maximum="60000"/>
   </samigo:wysiwyg>
 </h:panelGrid>
			
         <h:outputText value="#{commonMessages.feedback_optional}" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'))}" />

        <!-- WYSIWYG -->
  <h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'))}">
         <samigo:wysiwyg rows="140" value="#{answer.feedback}" hasToggle="plain" mode="author">
           <f:validateLength maximum="4000"/>
         </samigo:wysiwyg>
  </h:panelGrid>
        </h:panelGrid>

	</h:column>
	
	<h:column rendered="#{itemauthor.currentItem.itemType==1 && itemauthor.currentItem.partialCreditFlag=='true'}"> 
	<h:panelGrid id="partialCreditInput" >
	<h:commandLink  title="#{authorMessages.reset_score_values}" 
	                rendered="#{answer.sequence==1}"
	                action="#{itemauthor.currentItem.resetPartialCreditValues}">
	  <h:outputText value="#{authorMessages.reset_score_values}"/>
	</h:commandLink> 
	
	
	<f:verbatim ><br/><br/></f:verbatim>
	
	<!-- Begin changes made for Partial Credit --> 
	<h:outputText value="#{authorMessages.percentage_value}" />
	    <h:inputText id="partialCredit" size="2" value="#{answer.partialCredit}" validator="#{answer.validatePartialCredit}"/>
	    <h:outputText id="partialCreditReminder" value="#{authorMessages.enter_new_pc_value}" style="visibility:hidden;" />
	    <h:message for="partialCredit" styleClass="validate"/>
	<!-- end of partial credit -->
	<f:verbatim><br/><br/></f:verbatim>
	
	<h:commandLink  title="#{authorMessages.reset_score_values}" 
	                rendered="#{itemauthor.currentItem.totalMCAnswers==answer.sequence}" 
	                action="#{itemauthor.currentItem.resetPartialCreditValues}">
	  <h:outputText  value="#{authorMessages.reset_score_values}"/>
	</h:commandLink>
	</h:panelGrid>

</h:column>
</h:dataTable>

</div>
<h:inputHidden id="selectedRadioBtn" value="#{itemauthor.currentItem.corrAnswer}" />
<div class="shorttext tier2">
  <h:outputText value="#{authorMessages.insert_additional_a}" />
<h:selectOneMenu  id="insertAdditionalAnswerSelectMenu"  onchange="this.form.onsubmit(); clickAddChoiceLink();" value="#{itemauthor.currentItem.additionalChoices}" >
  <f:selectItem itemLabel="#{authorMessages.select_menu}" itemValue="0"/>
  <f:selectItem itemLabel="1" itemValue="1"/>
  <f:selectItem itemLabel="2" itemValue="2"/>
  <f:selectItem itemLabel="3" itemValue="3"/>
  <f:selectItem itemLabel="4" itemValue="4"/>
  <f:selectItem itemLabel="5" itemValue="5"/>
  <f:selectItem itemLabel="6" itemValue="6"/>
</h:selectOneMenu>
<h:commandLink id="hiddenAddChoicelink" action="#{itemauthor.currentItem.addChoicesAction}" value="">
</h:commandLink>
</div>
<br/>
    <!-- 4 RANDOMIZE -->
  <div class="longtext">
    <h:outputLabel value="#{authorMessages.randomize_answers}" />    </div>
<div class="tier3">
    <h:selectOneRadio value="#{itemauthor.currentItem.randomized}" >
     <f:selectItem itemValue="true"
       itemLabel="#{authorMessages.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
  </div>

    <!-- 5 RATIONALE -->
   <div class="longtext">
 <h:outputLabel value="#{authorMessages.require_rationale}" /></div>
<div class="tier3">
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" >
     <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}"/>
     <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
</div>
    <!-- 6 PART -->
<h:panelGrid columns="3"  columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
<f:verbatim>&nbsp;</f:verbatim>
<h:outputLabel value="#{authorMessages.assign_to_p} " />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
</h:panelGrid>


    <!-- 7 POOL -->

<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
<f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel value="#{authorMessages.assign_to_question_p} " />
  <h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}" id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

</h:panelGrid>


 <!-- 8 FEEDBACK -->
<h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
 <h:outputText value=" " escape="false"/>
 <f:verbatim> <div class="longtext"></f:verbatim>
  <h:outputLabel value="#{authorMessages.correct_incorrect_an}" />
 <f:verbatim></div> </f:verbatim>
 <f:verbatim><div class="tier2"> </f:verbatim>
  <h:outputText value="#{authorMessages.correct_answer_opti}" />
<br/>
  <!-- WYSIWYG --> 
<h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="plain" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
</h:panelGrid>
 <f:verbatim><br/> </f:verbatim>
 <h:outputText value="#{authorMessages.incorrect_answer_op}" />

  <!-- WYSIWYG -->
   <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}"  hasToggle="plain" mode="author">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
</h:panelGrid>
 <f:verbatim></div> </f:verbatim>
</h:panelGroup>

 <!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
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
</div>

<p class="act">
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}"  styleClass="active">
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

<f:subview id="disableNegVal" rendered="#{!empty itemauthor.currentItem.itemMinScore}">
<f:verbatim>
<script type="text/javascript">
	var negPointField = document.getElementById('itemForm:answerdsc');
	if(negPointField){
		negPointField.value = 0;
		negPointField.disabled = true;
	}
</script>
</f:verbatim>
</f:subview>

</h:form>
<!-- end content -->
</div>

    </body>
  </html>
</f:view>

