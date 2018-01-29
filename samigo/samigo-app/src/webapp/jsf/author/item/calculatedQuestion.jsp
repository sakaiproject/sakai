<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id: matching.jsp 59563 2009-04-02 15:18:05Z arwhyte@umich.edu $
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
<%-- 
saveButton style is used only on save buttons to attach a click handler, so that javascript
can throw a confirm dialog box if nothing has been changed.  saveButton style doesn't do any
styling.

changeWatch style is used only on fields that should be watched for changes, with the saveButton 
above.  If a changeWatch field is changed, the saveButton buttons will not trigger a 
confirmation dialog
--%>
<f:view>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head><%= request.getAttribute("html.head") %>
	<title><h:outputText value="#{authorMessages.item_display_author}"/></title>
	<samigo:script path="/js/info.js"/>
	<!-- AUTHORING -->
	<samigo:script path="/js/authoring.js"/>
	<script type="text/javascript">
	$(document).ready(function() {
		initCalcQuestion();
	});
	</script>
</head>
<%-- unfortunately have to use a scriptlet here --%>
<body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody container-fluid">
<!-- content... -->
<!-- FORM -->

<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
	<p class="act">
		<h:commandButton 
				rendered="#{itemauthor.target=='assessment'}" 
				value="#{commonMessages.action_save}" 
				action="#{itemauthor.currentItem.getOutcome}" 
				styleClass="active saveButton">
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
	  	</h:commandButton>
	  	<h:commandButton 
	  			rendered="#{itemauthor.target=='questionpool'}" 
	  			value="#{commonMessages.action_save}" 
	  			action="#{itemauthor.currentItem.getPoolOutcome}" 
	  			styleClass="active saveButton">
	    	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
	  	</h:commandButton>
	
	  	<h:commandButton 
	  			rendered="#{itemauthor.target=='assessment'}" 
	  			value="#{commonMessages.cancel_action}" 
	  			action="editAssessment" 
	  			immediate="true">
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
	  	</h:commandButton>
	 	<h:commandButton 
                rendered="#{itemauthor.target=='questionpool'}" 
	 			value="#{commonMessages.cancel_action}" 
	 			action="editPool" 
	 			immediate="true">
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
	 	</h:commandButton>
	</p>

  	<!-- QUESTION PROPERTIES -->
  	<!-- this is for creating multiple choice questions -->
	<%-- kludge: we add in 1 useless textarea, the 1st does not seem to work --%>
	<div style="display:none">
		<h:inputTextarea id="ed0" cols="10" rows="10" value="            " />
	</div>

	<!-- 1 POINTS -->
	<div class="form-group row">
		<h:outputLabel for="answerptr" value="#{authorMessages.answer_point_value}" styleClass="col-md-4 form-control-label"/>
		<div class="col-md-2">
			<h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" 
							required="true" styleClass="form-control">
				<f:validateDoubleRange/>
			</h:inputText>			
			<h:message for="answerptr" styleClass="validate"/>
	  	</div>
	</div>

	<div class="form-group row">
		<h:outputLabel value="#{authorMessages.answer_point_value_display}" styleClass="col-md-4 form-control-label"/>
		<div class="col-md-5 samigo-inline-radio">
			<h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
				<f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
				<f:selectItem itemValue="false"	itemLabel="#{authorMessages.no}" />
			</h:selectOneRadio>
		</div>
	</div>

    <%-- 2 QUESTION TEXT --%>
    <div class="longtext"> <h:outputLabel value="#{authorMessages.q_text}" />
    <br/></div>
	<div class="tier2">
	  	<p><h:outputText value="#{authorMessages.calc_question_general_instructions1 }" /></p>
	  	<label><h:outputText value="#{authorMessages.calc_question_instructions_label}"/></label>
		<ol>
			<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_1}" /></li>
			<ul>
				<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_1a}" /></li>
			</ul>
			<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_2}" /></li>
			<ul>
				<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_2a}" /></li>
				<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_2b}" /></li>
			</ul>
			<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_3}" /></li>
			<ol type="a">
				<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_3a}" /></li>
				<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_3b}" /></li>
			</ol>
		</ol>
		<div class="mathjax-warning" style="display: none;">
			<h:outputText value="#{authorMessages.accepted_characters}" escape="false"/>
			<div class="alert alert-warning">
				<h:outputText value="#{authorMessages.mathjax_usage_warning}" escape="false"/>
			</div>
		</div>
		<label><h:outputText value="#{authorMessages.calc_question_example_label}"/></label>
		<p class="tier2"><h:outputText value="#{authorMessages.calc_question_example1}"/></p>
		<p class="tier2"><h:outputText value="#{authorMessages.calc_question_example1_formula}"/></p>

		<div id="calcQShowHide" class="collapsed">
		<h:outputLink onclick="$('#calcQInstructions').toggle();toggleCollapse(document.getElementById('calcQShowHide'));" value="#">
			<h:outputText value="#{authorMessages.calc_question_hideshow}"/> 
		</h:outputLink>
		</div>
		<div id="calcQInstructions" style='display:none;'>
			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_general_instructsion_label}" /></div>
			<div class="tier2">
				<h:outputText value="#{authorMessages.calc_question_general_instructions1 }" /><br/>
				<h:outputText value="#{authorMessages.calc_question_general_instructions2 }" /><br/>
				<h:outputText value="#{authorMessages.calc_question_general_instructions3 }" />
			</div>
			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_walkthrough_label}" /></div>
			<div class="tier2">
				<ol>
					<li><h:outputText value="#{authorMessages.calc_question_walkthrough1 }" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_walkthrough2 }" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_walkthrough3 }" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_walkthrough4 }" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_walkthrough5 }" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_walkthrough6 }" /></li>
				</ol>
			</div>
			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_var_label}" /></div>
			<div class="tier2">
				<h:outputText value="#{authorMessages.calc_question_define_vars}" />
			</div>
			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_formula_label}" /></div>
			<div class="tier2">
				<h:outputText value="#{authorMessages.calc_question_answer_expression}" />
			</div>
			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_example_label}" /></div>
			<p class="tier2"><h:outputText value="#{authorMessages.calc_question_example2}" /></p>
			<p class="tier2"><h:outputText value="#{authorMessages.calc_question_example2_formula}" /></p>

			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_calculation_label}" /></div>
			<div class="tier2">
				<h:outputText value="#{authorMessages.calc_question_define_calculations}" />
			</div>			
			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_example_label}" /></div>
			<div class="tier2">
				<h:outputText value="#{authorMessages.calc_question_example3}" />
			</div>

			<div class="longtext"><h:outputLabel value="#{authorMessages.calc_question_additional_label}" /></div>
			<div class="tier2">
				<ul>
					<li><h:outputText value="#{authorMessages.calc_question_answer_variance}" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_answer_decimal}" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_scientific_notation}" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_operators}" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_functions}" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_constants}" /></li>
					<li><h:outputText value="#{authorMessages.calc_question_var_names}"/></li>
					<li><h:outputText value="#{authorMessages.calc_question_unique_names}"/></li>
				</ul>
			</div>
		</div>
	  
	  	<br/>
	  
	  <!-- WYSIWYG -->
	  	<h:panelGrid>
	   		<samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.instruction}" hasToggle="yes" mode="author">
	     		<f:validateLength maximum="60000"/>
	   		</samigo:wysiwyg>
	
	  	</h:panelGrid>
	  
	</div>

  	<!-- 2a ATTACHMENTS -->
  	<%@ include file="/jsf/author/item/attachment.jsp" %>

  	<!-- 3 ANSWER -->

	  	<h:commandButton rendered="#{itemauthor.target=='assessment' || itemauthor.target=='questionpool'}" 
	  			value="#{authorMessages.calc_question_extract_button}" 
	  			action="calculatedQuestion" 
	  			styleClass="active">
	  		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CalculatedQuestionExtractListener" />
		</h:commandButton>
	<!-- display variables -->
	<div class="longtext"> <h:outputLabel value="#{authorMessages.calc_question_var_label} " /></div>
	<div class="tier2">
		<h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="pairs" 
				value="#{itemauthor.currentItem.calculatedQuestion.variablesList}" var="variable">
	      
	    	<h:column>
	        	<f:facet name="header">          
	          		<h:outputText value="" />
	        	</f:facet>
	
	          	<h:outputText value="" />
	      	</h:column>
	
	      	<h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_varname_col}"  />
	        	</f:facet>
	          	<h:outputText escape="false" value="#{variable.name}" rendered="#{variable.active }" />
	          	<h:outputText escape="false" value="#{variable.name}" rendered="#{!variable.active }" 
	          			styleClass="disabledField" />
	      	</h:column>
	
	      	<h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_min}"  />
	        	</f:facet>
	          	<h:inputText value="#{variable.min}" disabled="#{!variable.active }" 
	          			styleClass="#{(!variable.validMin ? 'validationError' : '') } changeWatch"/>
	      	</h:column>
	
	      	<h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_max}"  />
	        	</f:facet>
	          	<h:inputText value="#{variable.max}" disabled="#{!variable.active }" 
	          			styleClass="#{(!variable.validMax ? 'validationError' : '') } changeWatch"/>
	      	</h:column>
	
	      	<h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_dec}"  />
	        	</f:facet>
			  	<h:selectOneMenu value="#{variable.decimalPlaces}" disabled="#{!variable.active }" styleClass="changeWatch">
		     		<f:selectItems value="#{itemauthor.decimalPlaceList}" />
	  			</h:selectOneMenu>
	      	</h:column>
	
		</h:dataTable>
		<h:outputLabel value="<p>#{authorMessages.no_variables_defined}</p>" escape="false"
				rendered="#{itemauthor.currentItem.calculatedQuestion.variablesList eq '[]'}"/>
	</div>

	<!-- display formulas -->
	<div class="longtext">
		<h:outputLabel value="#{authorMessages.calc_question_formula_label} " />
		<ul>
			<li><h:outputText value="#{authorMessages.calc_question_simple_instructions_step_3b}" /></li>
			<li><h:outputText value="#{authorMessages.calc_question_operators}" /></li>
			<li><h:outputText value="#{authorMessages.calc_question_functions}" /></li>
			<li><h:outputText value="#{authorMessages.calc_question_constants}" /></li>
		</ul>
	</div>
	<div class="tier2">
		<h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="formulas" 
				value="#{itemauthor.currentItem.calculatedQuestion.formulasList}" var="formula">
	    	<h:column>
	        	<f:facet name="header">          
		    		<h:outputText value=""  />
	    	    </f:facet>
	          <h:outputText value="" />
	      </h:column>
	
	      <h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_formulaname_col}"  />
	        	</f:facet>
	          	<h:outputText escape="false" value="#{formula.name}" rendered="#{formula.active }" />
	          	<h:outputText escape="false" value="#{formula.name}" rendered="#{!formula.active }" styleClass="disabledField" />
	      </h:column>
	
	      <h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_formula_col}"  />
	        	</f:facet>
	        	<h:inputTextarea value="#{formula.text }"
	        			cols="40" rows="3" 
	        			disabled="#{!formula.active }" 
	        			styleClass="#{(!formula.validFormula ? 'validationError' : '')} changeWatch"/>        	
	      </h:column>
	      
	      <h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_tolerance}"  />
	        	</f:facet>
	          	<h:inputText value="#{formula.tolerance}"  
	          			disabled="#{!formula.active }" 
	          			styleClass="#{(!formula.validTolerance ? 'validationError' : '')} changeWatch"/>
	      </h:column>
	      
	      <h:column>
	        	<f:facet name="header">
	          		<h:outputText value="#{authorMessages.calc_question_dec}" />
	        	</f:facet>
			  	<h:selectOneMenu id="assignToPart" value="#{formula.decimalPlaces}" disabled="#{!formula.active }" styleClass="changeWatch">
	     			<f:selectItems  value="#{itemauthor.decimalPlaceList}" />
	  			</h:selectOneMenu>          
	      </h:column>
		</h:dataTable>
		<h:outputLabel value="<p>#{authorMessages.no_formulas_defined}</p>" escape="false"
				rendered="#{itemauthor.currentItem.calculatedQuestion.formulasList eq '[]'}"/>
	</div>

    <!-- display calculations -->
    <div class="longtext"> 
        <h:outputLabel value="#{authorMessages.calc_question_calculation_label} " />
        <ul>
            <li><h:outputText value="#{authorMessages.calc_question_define_calculations}" /></li>
        </ul>
     </div>
    <div class="tier2">
        <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="calculations" 
                value="#{itemauthor.currentItem.calculatedQuestion.calculationsList}" var="calculation"
                rendered="#{itemauthor.currentItem.calculatedQuestion.hasCalculations}">
          <h:column>
            <f:facet name="header">
              <h:outputText value="" />
            </f:facet>
            <h:outputText value="" />
          </h:column>
          <h:column>
            <f:facet name="header">
              <h:outputText value="#{authorMessages.calc_question_calculation_col}" />
            </f:facet>
            <h:outputText escape="false" value="#{calculation.text}" />
          </h:column>
          <h:column>
            <f:facet name="header">
              <h:outputText value="#{authorMessages.calc_question_calculation_sample_col}" />
            </f:facet>
            <h:outputText escape="false" value="#{calculation.formula}" />
          </h:column>
          <h:column>
            <f:facet name="header">
              <h:outputText value="" />
            </f:facet>
            <h:outputText value="#{calculation.value}" />
          </h:column>
          <h:column>
            <f:facet name="header">
              <h:outputText value="#{authorMessages.calc_question_calculation_status_col}" />
            </f:facet>
            <h:outputText value="#{calculation.status}" />
          </h:column>
        </h:dataTable>
        <h:outputLabel value="<p>#{authorMessages.calc_question_no_calculations}</p>" escape="false"
                rendered="#{! itemauthor.currentItem.calculatedQuestion.hasCalculations}"/>
    </div>

	<br/>
	<br/>
    <!-- 6 PART -->
	<h:panelGroup styleClass="form-group row" layout="block"
					rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">		
		<h:outputLabel value="#{authorMessages.assign_to_p}" styleClass="col-md-4 form-control-label"/>
		<div class="col-md-8">
	  		<h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
	    		<f:selectItems  value="#{itemauthor.sectionSelectList}" />
	  		</h:selectOneMenu>
	  	</div>
	</h:panelGroup>

    <!-- 7 POOL -->
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

	<!-- 8 FEEDBACK -->
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

	<!-- METADATA -->
	<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
		<div class="form-group row">
			<h:outputLabel value="Metadata" styleClass="col-md-12 form-control-label"/>
		</div>
		<div class="form-group row">
			<h:outputLabel value="#{authorMessages.objective}" styleClass="col-md-4 form-control-label"/>
			<div class="col-md-5">
				<h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
			</div>
		</div>
		<div class="form-group row">
			<h:outputLabel value="#{authorMessages.keyword}" styleClass="col-md-4 form-control-label"/>
			<div class="col-md-5">
				<h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
			</div>
		</div>
		<div  class="form-group row">
			<h:outputLabel value="#{authorMessages.rubric_colon}" styleClass="col-md-4 form-control-label"/>
			<div class="col-md-5">
				<h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control" />
			</div>
		</div>
	</h:panelGroup>

	<%@ include file="/jsf/author/item/tags.jsp" %>

	<p class="act">
		<h:commandButton 
				rendered="#{itemauthor.target=='assessment'}" 
				value="#{commonMessages.action_save}" 
				action="#{itemauthor.currentItem.getOutcome}" 
				styleClass="active saveButton">
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
	  	</h:commandButton>
	  	<h:commandButton 
	  			rendered="#{itemauthor.target=='questionpool'}" 
	  			value="#{commonMessages.action_save}" 
	  			action="#{itemauthor.currentItem.getPoolOutcome}" 
	  			styleClass="active saveButton">
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
	  	</h:commandButton>
	  	<h:commandButton  
	  			rendered="#{itemauthor.target=='assessment'}" 
	  			value="#{commonMessages.cancel_action}" 
	  			action="editAssessment" 
	  			immediate="true">
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
	  	</h:commandButton>
	 	<h:commandButton 
                rendered="#{itemauthor.target=='questionpool'}" 
	 			value="#{commonMessages.cancel_action}" 
	 			action="editPool" 
	 			immediate="true">
	        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
	 	</h:commandButton>
	</p>

</h:form>
<!-- end content -->
</div>
</body>
</html>
</f:view>

