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
<%-- "checked in wysiwyg code but disabled, added in lydia's changes between 1.9 and 1.10" --%>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
      </head>
<body onload="<%= request.getAttribute("html.body.onload") %>;resetInsertAnswerSelectMenus();disablePartialCreditField();">

<div class="portletBody container-fluid">
<!-- content... -->
<!-- FORM -->
<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm" onsubmit="return editorCheck();">

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
 
  <!-- NOTE:  Had to call this.form.onsubmit(); when toggling between single  -->
  <!-- and multiple choice, or adding additional answer choices.  -->
  <!-- to invoke the onsubmit() function for htmlarea to save the htmlarea contents to bean -->
  <!-- otherwise, when toggleing or adding more choices, all contents in wywisyg editor are lost -->

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice questions -->
  <!-- 1 POINTS -->
  <div class="form-group row"> 
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.answer_point_value}" />
    <div class="col-md-2">
      <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" required="true" styleClass="form-control ConvertPoint" disabled="#{author.isEditPoolFlow}">
        <f:validateDoubleRange minimum="0.00" />
      </h:inputText>
      <h:message for="answerptr" styleClass="validate" />
    </div>
  </div>

  <div class="form-group row">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.answer_point_value_display}" />    
	<div class="col-md-10">
      <t:selectOneRadio id="itemScoreDisplay" value="#{itemauthor.currentItem.itemScoreDisplayFlag}" layout="spread">
        <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
        <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
      </t:selectOneRadio>
      <ul class="show-item-score">
        <li><t:radio for="itemScoreDisplay" index="0" /></li>
        <li><t:radio for="itemScoreDisplay" index="1" /></li>
      </ul>
    </div>
  </div>

  <f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.answer_min_point_value}" styleClass="col-md-2" />
        <div  class="col-md-2">          
            <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" size="6"  onchange="toggleNegativePointVal(this.value);" styleClass=" form-control  ConvertPoint">
              <f:validateDoubleRange />
            </h:inputText>
            <small>
              <h:outputText value="#{authorMessages.answer_min_point_info}" />
            </small>
            <h:message for="answerminptr" styleClass="validate"/>
        </div>
    </div>
  </f:subview>

  <h2 class="answer-subsection">
    <h:outputText value="#{authorMessages.answer} " />  
    <small>
      <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/mcWhatsThis.faces','MCWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('/samigo-app/jsf/author/mcWhatsThis.faces','MCWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
        <h:outputText  value=" (#{assessmentSettingsMessages.whats_this_link})"/>
      </h:outputLink>
    </small>
  </h2>

  <!-- need to add a listener, for the radio button below,to toggle between single and multiple correct-->
  <div class="form-group">
    <h:selectOneRadio id="chooseAnswerTypeForMC" layout="pageDirection"
	 		        onclick="this.form.onsubmit();this.form.submit();"
                    onkeypress="this.form.onsubmit();this.form.submit();"
                    value="#{itemauthor.currentItem.itemType}"
	                valueChangeListener="#{itemauthor.currentItem.toggleChoiceTypes}" >
      <f:selectItem itemValue="1" itemLabel="#{commonMessages.multiple_choice_sin}" escape="false" />  
      <f:selectItem itemValue="12" itemLabel="#{commonMessages.multipl_mc_ss}" escape="false" /> 
      <f:selectItem itemValue="2"   itemLabel="#{commonMessages.multipl_mc_ms}" escape="false" />
    </h:selectOneRadio>
  </div>

  <!-- partial credit vs negative marking -->
  <h:panelGroup layout="block" id="partialCredit_toggle" styleClass="tier3" rendered="#{itemauthor.currentItem.itemType == 1 && itemauthor.currentItem.partialCreditEnabled==true}">
    <h:panelGroup id="partialCredit_JSF_toggle">
      <h:selectOneRadio id="partialCredit_NegativeMarking"
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
  </h:panelGroup>

<!-- multiple choice, multiple selection: full or partial credit -->
<h:panelGroup layout="block" id="mcms_credit_toggle" styleClass="tier3" rendered="#{itemauthor.currentItem.itemType == 2}">
  <h:panelGroup id="mcms_credit_JSF_toggle">
    <h:selectOneRadio id="mcms_credit_partial_credit"
					  layout="pageDirection"
					  onclick="this.form.onsubmit();this.form.submit();"
					  onkeypress="this.form.onsubmit();this.form.submit();"
					  value="#{itemauthor.currentItem.mcmsPartialCredit}">
      <f:selectItem itemValue="true" itemLabel="#{commonMessages.mutlipl_mc_ms_partial_credit}"  />
      <f:selectItem itemValue="false" itemLabel="#{commonMessages.multipl_mc_ms_full_credit}"  />
    </h:selectOneRadio>
  </h:panelGroup>
</h:panelGroup>
 
<h:panelGroup layout="block" id="discountDiv" styleClass="tier3">
  <h:panelGroup id="discountTable"
        rendered="#{(itemauthor.currentItem.itemType==1 &&(itemauthor.currentItem.partialCreditFlag=='false'||itemauthor.currentItem.partialCreditEnabled==false))
        || itemauthor.currentItem.itemType==12 || (itemauthor.currentItem.itemType==2 && itemauthor.currentItem.mcmsPartialCredit=='false')}">
  <h:outputLabel value="#{authorMessages.negative_point_value}"/>
  <h:inputText id="answerdsc" value="#{itemauthor.currentItem.itemDiscount}" required="true" styleClass="ConvertPoint">
    <f:validateDoubleRange/>
  </h:inputText>
  <f:verbatim> <script type="text/javascript" defer='defer'>
  		var itemType = "${itemauthor.currentItem.itemType}";
  		var discDiv=document.getElementById('itemForm:discountDiv');
		
  		if(itemType == 1) {
		  	var toggleDiv=document.getElementById('itemForm:partialCredit_NegativeMarking');
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
</h:panelGroup>


  <!-- 2 TEXT -->
  
   <div class="form-group row ">
       <h:outputLabel value="#{authorMessages.q_text}" styleClass="col-md-2 form-control-label"/>
       <div class="col-md-8 row">
       <div class="col-md-12">
            <a id="multiple_show_editor" onclick="javascript:show_multiple_text(this);" href="#">
                 <h:outputText id="text" value="#{authorMessages.show_editors}"/> 
            </a>
       </div>
       <div class="col-md-12">
          <!-- WYSIWYG -->
          <h:panelGrid>
              <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="plain" mode="author">
                  <f:validateLength minimum="1" maximum="60000"/>
              </samigo:wysiwyg>
          </h:panelGrid>
       </div>
       </div>
   </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

	<!-- dynamicaly generate rows of answers based on number of answers-->
  <h:dataTable id="mcchoices" styleClass="table" value="#{itemauthor.currentItem.multipleChoiceAnswers}" var="answer" headerClass="navView longtext">
    <h:column>
      <h:panelGrid columns="2" border="0">
        <h:panelGroup styleClass="answer-group">
          <div class="correct-answer">
            <h:outputText value="#{authorMessages.correct_answer}"  />
          </div>

  <!-- if multiple correct, use checkboxes -->
  <h:selectManyCheckbox onclick="updateHiddenMultipleChoice();" onkeypress="updateHiddenMultipleChoice();" value="#{itemauthor.currentItem.corrAnswers}" id="mccheckboxes"
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
    <f:selectItem itemValue="#{answer.label}" itemLabel="&#160; #{answer.label}" escape="false" />
  </h:selectOneRadio>

  <h:commandLink title="#{authorMessages.t_removeC}" id="removelinkSingle" onfocus="document.forms[1].onsubmit();" action="#{itemauthor.currentItem.removeChoicesSingle}" rendered="#{!itemauthor.currentItem.multipleCorrect}">
    <h:outputText id="textSingle" value="#{commonMessages.remove_action}"/>
    <f:param name="answeridSingle" value="#{answer.label}"/>
  </h:commandLink>
 </h:panelGroup>

   <!-- WYSIWYG -->
   <samigo:wysiwyg rows="140" value="#{answer.text}" hasToggle="plain" mode="author">
     <f:validateLength maximum="60000"/>
   </samigo:wysiwyg>
			
         <h:outputText value="#{commonMessages.feedback_optional}" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'))}" />

        <!-- WYSIWYG -->
  <h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'))}">
         <samigo:wysiwyg rows="140" value="#{answer.feedback}" hasToggle="plain" mode="author">
           <f:validateLength maximum="60000"/>
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

<h:inputHidden id="selectedRadioBtn" value="#{itemauthor.currentItem.corrAnswer}" />
<h:inputHidden id="selectedCheckboxesAnswers" value="#{itemauthor.currentItem.corrAnswers}" />

  <div class="form-group row">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.insert_additional_a}" />
    <div class="col-md-10">
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
  </div>

  <!-- 4 RANDOMIZE -->
  <div class="form-group row">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.randomize_answers}" />
    <div class="col-md-10">
      <t:selectOneRadio id="question-randomize" value="#{itemauthor.currentItem.randomized}" layout="spread">
        <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
        <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
      </t:selectOneRadio>
      <ul class="question-randomize">
        <li><t:radio for="question-randomize" index="0" /></li> 
        <li><t:radio for="question-randomize" index="1" /></li> 
      </ul>
    </div>
  </div>

  <!-- 5 RATIONALE -->
  <div class="form-group row">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.require_rationale}" />
    <div class="col-md-10">
      <t:selectOneRadio id="question-rationale" value="#{itemauthor.currentItem.rationale}" layout="spread">
        <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}"/>
        <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
      </t:selectOneRadio>
      <ul class="question-rationale">
        <li><t:radio for="question-rationale" index="0" /></li> 
        <li><t:radio for="question-rationale" index="1" /></li> 
      </ul>
    </div>
  </div>

  <!-- 6 PART -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.assign_to_p} " />
    <div class="col-md-10">
      <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
        <f:selectItems value="#{itemauthor.sectionSelectList}" />
      </h:selectOneMenu>
    </div>
  </h:panelGroup>

  <!-- 7 POOL -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
    <h:outputLabel styleClass="col-md-2" value="#{authorMessages.assign_to_question_p} " />
    <div class="col-md-10">
      <h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}" id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
        <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
        <f:selectItems value="#{itemauthor.poolSelectList}" />
      </h:selectOneMenu>
    </div>
  </h:panelGroup>

  <!-- 8 FEEDBACK -->
  <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">

    <div class="form-group row">
      <h:outputLabel styleClass="col-md-2" value="#{authorMessages.correct_answer_opti}" />
      <div class="col-md-8">
        <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="plain" mode="author">
          <f:validateLength maximum="60000"/>
        </samigo:wysiwyg>
      </div>
    </div>
    <div class="form-group row">
      <h:outputLabel styleClass="col-md-2" value="#{authorMessages.incorrect_answer_op}" />
      <div class="col-md-8">
     <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}"  hasToggle="plain" mode="author">
       <f:validateLength maximum="60000"/>
     </samigo:wysiwyg>
      </div>
    </div>
  </h:panelGroup>

  <!-- METADATA -->
  <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
  <h:outputLabel value="Metadata"/>
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
</h:form>
<!-- end content -->
</div>

<f:verbatim> 
	<script type="text/javascript" defer='defer'>
	$(document).ready(function(){
		var itemType = "${itemauthor.currentItem.itemType}";
		var prefixId='#itemForm\\:';
		var refId=prefixId+'chooseAnswerTypeForMC\\:';
		var optionId=prefixId;
		if(itemType == 1){
			refId+='0';
			var partialCredit=${itemauthor.currentItem.partialCreditEnabled==true};
			if(partialCredit){  				
				optionId+='partialCredit_toggle';
				var showDiscountDiv=${itemauthor.currentItem.partialCreditFlag==false};
				if(showDiscountDiv){
					$(prefixId+'partialCredit_NegativeMarking\\:0').parent().append($(prefixId+'discountDiv'));
				}
			}else{
				optionId+='discountDiv';
			}
		}else if (itemType == 12){
			refId+='1';
			optionId+='discountDiv';
		}else if (itemType == 2) {
 			var showSecondOption= ${itemauthor.currentItem.mcmsPartialCredit==false};
			if(showSecondOption){
				$(prefixId+'mcms_credit_toggle').append($(prefixId+'discountDiv'));
			}
			refId+='2';
			optionId+='mcms_credit_toggle';
		}
		$(refId).parent().append($(optionId));
	});
	</script>
</f:verbatim>
    </body>
  </html>
</f:view>

