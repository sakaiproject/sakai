<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:t="http://myfaces.apache.org/tomahawk">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
]]></jsp:text>
  <!--
* $Id: matching.jsp 59563 2009-04-02 15:18:05Z arwhyte@umich.edu $

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

 -->
  <!-- "checked in wysiwyg code but disabled, added in lydia's changes between 1.9 and 1.10"  -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{authorMessages.item_display_author}"/>
        </title>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/info.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <!-- AUTHORING  -->
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/authoring.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/focusElement.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:text><![CDATA[
		const addGlobalVariable = () => {
			const globalVariable = prompt("]]></jsp:text>
          <h:outputText value="#{authorMessages.calc_question_enter_global_variable}" escape="false"/>
          <jsp:text><![CDATA[");
			if (globalVariable) {
				document.getElementById('globalvariablename').value = globalVariable;
			}
		}

		document.addEventListener('DOMContentLoaded', () => {
			const deleteLinks = document.querySelectorAll('a.sam-deleteglobalvariable');
			[].forEach.call(deleteLinks, (link) => {
				link.existingOnclick = link.onclick;
				link.onclick = null;
				link.addEventListener('click', (evt) => {
					evt.preventDefault();
					const shouldEvt = confirm("]]></jsp:text>
          <h:outputText value="#{authorMessages.calc_question_delete_global_variable_confirm}" escape="false"/>
          <jsp:text><![CDATA[");
					if ( shouldEvt && evt.currentTarget.existingOnclick ) {
						evt.currentTarget.existingOnclick();
						evt.stopPropagation();
					}
				 })
			});
		}, false);
	]]></jsp:text>
        </script>
      </head>
      <!-- unfortunately have to use a scriptlet here  -->
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <!-- content...  -->
            <!-- FORM  -->
            <!-- HEADING  -->
            <jsp:directive.include file="/jsf/author/item/itemHeadings.jsp"/>
            <h:form id="itemForm">
              <f:verbatim>
                <input type="hidden" id="ckeditor-autosave-context" name="ckeditor-autosave-context" value="samigo_edit_calculatedQuestion"/>
              </f:verbatim>
              <h:panelGroup rendered="#{itemauthor.currentItem.itemId!=null}">
                <f:verbatim>
                  <jsp:text><![CDATA[<input type="hidden" id="ckeditor-autosave-entity-id" name="ckeditor-autosave-entity-id" value="]]></jsp:text>
                </f:verbatim>
                <h:outputText value="#{itemauthor.currentItem.itemId}"/>
                <f:verbatim>
                  <jsp:text><![CDATA["/>]]></jsp:text>
                </f:verbatim>
              </h:panelGroup>
              <p class="act">
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                </h:commandButton>
              </p>
              <!-- QUESTION PROPERTIES  -->
              <!-- this is for creating multiple choice questions  -->
              <!-- kludge: we add in 1 useless textarea, the 1st does not seem to work  -->
              <div style="display:none">
                <h:inputTextarea id="ed0" cols="10" rows="10" value="            "/>
              </div>
              <!-- 1 POINTS  -->
              <div class="mb-3 row">
                <h:outputLabel for="answerptr" value="#{authorMessages.answer_point_value}" styleClass="col-md-2 form-label"/>
                <div class="col-md-2">
                  <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" required="true" disabled="#{author.isEditPoolFlow}" styleClass="form-control">
                    <f:validateDoubleRange/>
                  </h:inputText>
                  <h:message for="answerptr" styleClass="validate"/>
                </div>
              </div>
              <div class="mb-3 row">
                <h:outputLabel for="itemScore" value="#{authorMessages.answer_point_value_display}" styleClass="col-md-2 form-label"/>
                <div class="col-md-5 samigo-inline-radio">
                  <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" id="itemScore">
                    <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}"/>
                    <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}"/>
                  </h:selectOneRadio>
                </div>
              </div>
              <!-- Extra Credit  -->
              <jsp:directive.include file="/jsf/author/inc/extraCreditSetting.jspf"/>
              <!-- 2 QUESTION TEXT  -->
              <div>
                <h3>
                  <h:outputText value="#{authorMessages.calc_question_general_instructsion_label}"/>
                </h3>
                <p>
                  <h:outputText value="#{authorMessages.calc_question_general_instructions1}"/>
                </p>
                <p>
                  <h:outputText value="#{authorMessages.calc_question_general_instructions2}"/>
                </p>
                <div class="card mb-3 px-3">
                  <h6>
                    <h:outputText value="#{authorMessages.calc_question_example_label1}"/>
                  </h6>
                  <p class="tier2">
                    <h:outputText value="#{authorMessages.calc_question_example1}"/>
                  </p>
                  <p>
                    <i>
                      <h:outputText value="#{authorMessages.calc_question_example1_formula}"/>
                    </i>
                  </p>
                  <p>
                    <h:outputText value="#{authorMessages.calc_question_example1_student}"/>
                  </p>
                </div>
                <div class="card mb-3 px-3">
                  <h6>
                    <h:outputText value="#{authorMessages.calc_question_example_label2}"/>
                  </h6>
                  <p class="tier2">
                    <h:outputText value="#{authorMessages.calc_question_example2}"/>
                  </p>
                  <p>
                    <i>
                      <h:outputText value="#{authorMessages.calc_question_example2_formula}"/>
                    </i>
                  </p>
                </div>
                <hr/>
                <h3>
                  <h:outputText value="#{authorMessages.calc_question_instructions_label}"/>
                </h3>
                <p>
                  <h:outputText value="#{authorMessages.calc_question_walkthrough_label}"/>
                </p>
                <ol>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_walkthrough1}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_walkthrough2}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_walkthrough3}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_walkthrough4}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_walkthrough5}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_walkthrough6}"/>
                  </li>
                </ol>
                <hr/>
                <h3>
                  <h:outputText value="#{authorMessages.calc_question_syntax_label}"/>
                </h3>
                <table class="table">
                  <thead>
                    <tr>
                      <th>
                        <h:outputText value="#{authorMessages.calc_question_syntax_function}"/>
                      </th>
                      <th>
                        <h:outputText value="#{authorMessages.calc_question_syntax_syntax}"/>
                      </th>
                      <th>
                        <h:outputText value="#{authorMessages.calc_question_syntax_description}"/>
                      </th>
                      <th>
                        <h:outputText value="#{authorMessages.calc_question_syntax_exameple}"/>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_variable1}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_variable2}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_variable3}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_variable4}"/>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_calculation1}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_calculation2}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_calculation3}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_calculation4}"/>
                        <br/>
                        <i>
                          <h:outputText value="#{authorMessages.calc_question_syntax_calculation5}"/>
                        </i>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_formula1}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_formula2}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_formula3}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_example1}"/>
                        <br/>
                        <i>
                          <h:outputText value="#{authorMessages.calc_question_example1_formula}"/>
                        </i>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_global1}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_global2}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_global3}"/>
                      </td>
                      <td>
                        <h:outputText value="#{authorMessages.calc_question_syntax_global4}"/>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <h3>
                  <h:outputText value="#{authorMessages.calc_question_definition_label}"/>
                </h3>
                <p>
                  <h:outputText value="#{authorMessages.calc_question_definition1}"/>
                </p>
                <p>
                  <h:outputText value="#{authorMessages.calc_question_definition2}"/>
                </p>
                <p>
                  <h:outputText value="#{authorMessages.calc_question_definition3}"/>
                </p>
                <hr/>
                <h3>
                  <h:outputText value="#{authorMessages.calc_question_additional_label}"/>
                </h3>
                <ol>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_answer_variance}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_answer_decimal}"/>
                  </li>
                  <li>
                    <h:outputFormat value="#{authorMessages.calc_question_simple_instructions_step_5}" escape="false"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_operators}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_functions}"/>
                  </li>
                </ol>
                <div id="calcQShowHide" class="collapsed">
                  <h:outputLink onclick="$('#calcQInstructions').toggle();toggleCollapse(document.getElementById('calcQShowHide'));" value="#">
                    <h:outputText value="#{authorMessages.calc_question_hideshow}"/>
                  </h:outputLink>
                </div>
                <div id="calcQInstructions" class="pt-3" style="display:none;">
                  <ol start="6">
                    <li>
                      <h:outputText value="#{authorMessages.calc_question_simple_instructions_step_4}"/>
                    </li>
                    <li>
                      <h:outputText value="#{authorMessages.calc_question_scientific_notation}"/>
                    </li>
                    <li>
                      <h:outputText value="#{authorMessages.calc_question_constants}"/>
                    </li>
                    <li>
                      <h:outputText value="#{authorMessages.calc_question_var_names}"/>
                    </li>
                    <li>
                      <h:outputText value="#{authorMessages.calc_question_unique_names}"/>
                    </li>
                  </ol>
                </div>
                <br/>
                <!-- WYSIWYG  -->
                <h:panelGrid>
                  <div class="longtext">
                    <h:outputLabel for="questionItemText_textinput" value="#{authorMessages.q_text}"/>
                  </div>
                  <samigo:wysiwyg identity="questionItemText" rows="140" value="#{itemauthor.currentItem.instruction}" hasToggle="yes" mode="author">
                    <f:validateLength maximum="60000"/>
                  </samigo:wysiwyg>
                </h:panelGrid>
              </div>
              <!-- 2a ATTACHMENTS  -->
              <jsp:directive.include file="/jsf/author/item/attachment.jsp"/>
              <!-- 3 ANSWER  -->
              <h:commandButton rendered="#{itemauthor.target=='assessment' || itemauthor.target=='questionpool'}" id="extractButton" value="#{authorMessages.calc_question_extract_button}" action="calculatedQuestion" styleClass="active">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CalculatedQuestionExtractListener"/>
              </h:commandButton>
              <!-- display variables  -->
              <div class="longtext">
                <jsp:text> </jsp:text>
                <h:outputLabel value="#{authorMessages.calc_question_var_label} "/>
              </div>
              <div class="tier2">
                <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="pairs" value="#{itemauthor.currentItem.calculatedQuestion.variablesList}" var="variable">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_varname_col}"/>
                    </f:facet>
                    <h:outputText escape="false" value="#{variable.name}" rendered="#{variable.active }"/>
                    <h:outputText escape="false" value="#{variable.name}" rendered="#{!variable.active }" styleClass="disabledField"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_min}"/>
                    </f:facet>
                    <h:inputText value="#{variable.min}" disabled="#{!variable.active }" styleClass="#{(!variable.validMin ? 'validationError' : '') }"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_max}"/>
                    </f:facet>
                    <h:inputText value="#{variable.max}" disabled="#{!variable.active }" styleClass="#{(!variable.validMax ? 'validationError' : '') }"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_dec}"/>
                    </f:facet>
                    <h:selectOneMenu value="#{variable.decimalPlaces}" disabled="#{!variable.active }">
                      <f:selectItems value="#{itemauthor.decimalPlaceList}"/>
                    </h:selectOneMenu>
                  </h:column>
                </h:dataTable>
                <h:outputLabel value="&lt;p&gt;#{authorMessages.no_variables_defined}&lt;/p&gt;" escape="false" rendered="#{itemauthor.currentItem.calculatedQuestion.variablesList eq '[]'}"/>
              </div>
              <!-- display global variables  -->
              <div class="longtext">
                <h:outputLabel value="#{authorMessages.calc_question_globalvar_label} "/>
              </div>
              <div class="tier2 globalvariable">
                <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" columnClasses="first,name,formula" id="globalvariables" value="#{itemauthor.currentItem.calculatedQuestion.globalVariablesList}" var="globalvariable" rendered="#{not empty itemauthor.currentItem.calculatedQuestion.globalVariablesList}">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_globalvarname_col}"/>
                    </f:facet>
                    <h:outputText escape="false" value="#{globalvariable.name}" rendered="#{globalvariable.active }"/>
                    <h:outputText escape="false" value="#{globalvariable.name}" rendered="#{!globalvariable.active }" styleClass="disabledField"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_formula_col}"/>
                    </f:facet>
                    <h:inputTextarea value="#{globalvariable.text}" cols="40" rows="3" disabled="#{!globalvariable.active}" styleClass="#{(!globalvariable.validFormula ? 'validationError' : '')}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_globalvardelete_col}"/>
                    </f:facet>
                    <h:commandLink styleClass="sam-deleteglobalvariable" title="#{authorMessages.calc_question_delete_global_variable_link}" action="calculatedQuestion" immediate="true" rendered="true">
                      <h:panelGroup rendered="#{globalvariable.addedButNotExtracted}">
                        <jsp:element name="span">
                          <jsp:attribute name="class" trim="false">fa fa-trash</jsp:attribute>
                          <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
                          <jsp:body>
                          </jsp:body>
                        </jsp:element>
                        <span class="sr-only">
                          <h:outputText value="#{authorMessages.calc_question_delete_global_variable_link}"/>
                        </span>
                      </h:panelGroup>
                      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CalculatedQuestionDeleteGlobalVariableListener"/>
                      <f:param name="globalvariabledelete" value="#{globalvariable.name}"/>
                    </h:commandLink>
                  </h:column>
                  <h:outputLabel value="&lt;p&gt;#{authorMessages.no_global_variables_defined}&lt;/p&gt;" escape="false" rendered="#{itemauthor.currentItem.calculatedQuestion.globalVariablesList eq '[]'}"/>
                </h:dataTable>
              </div>
              <h:commandButton rendered="#{itemauthor.target=='assessment' || itemauthor.target=='questionpool'}" id="addGlobalVariableButton" value="#{authorMessages.calc_question_add_global_variable_button}" onclick="addGlobalVariable();" styleClass="active">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CalculatedQuestionAddGlobalVariableListener"/>
              </h:commandButton>
              <input id="globalvariablename" type="hidden" name="globalvariablename"/>
              <br/>
              <br/>
              <!-- display formulas  -->
              <div class="longtext">
                <h:outputLabel value="#{authorMessages.calc_question_formula_label} "/>
                <ul>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_simple_instructions_step_3b}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_operators}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_functions}"/>
                  </li>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_constants}"/>
                  </li>
                </ul>
              </div>
              <div class="tier2">
                <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="formulas" value="#{itemauthor.currentItem.calculatedQuestion.formulasList}" var="formula">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_formulaname_col}"/>
                    </f:facet>
                    <h:outputText escape="false" value="#{formula.name}" rendered="#{formula.active }"/>
                    <h:outputText escape="false" value="#{formula.name}" rendered="#{!formula.active }" styleClass="disabledField"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_formula_col}"/>
                    </f:facet>
                    <h:inputTextarea value="#{formula.text }" cols="40" rows="3" disabled="#{!formula.active }" styleClass="#{(!formula.validFormula ? 'validationError' : '')}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_tolerance}"/>
                    </f:facet>
                    <h:inputText value="#{formula.tolerance}" disabled="#{!formula.active }" styleClass="#{(!formula.validTolerance ? 'validationError' : '')}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_dec}"/>
                    </f:facet>
                    <h:selectOneMenu id="assignToPart" value="#{formula.decimalPlaces}" disabled="#{!formula.active }">
                      <f:selectItems value="#{itemauthor.decimalPlaceList}"/>
                    </h:selectOneMenu>
                  </h:column>
                </h:dataTable>
                <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="formulasCalculation" value="#{itemauthor.currentItem.calculatedQuestion.formulasList}" var="formulaCalculation" rendered="#{itemauthor.currentItem.calculatedQuestion.showFormulasCalculation}">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_formulaname_col}"/>
                    </f:facet>
                    <h:outputText escape="false" value="#{formulaCalculation.name}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_calculation_sample_col}"/>
                    </f:facet>
                    <h:outputText escape="false" value="#{formulaCalculation.value}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_calculation_status_col}"/>
                    </f:facet>
                    <h:outputText value="#{formulaCalculation.status}"/>
                  </h:column>
                </h:dataTable>
                <h:commandButton id="checkFormulasButton" value="#{itemauthor.currentItem.calculatedQuestion.showFormulasCalculation ? commonMessages.another_solution : commonMessages.check_formulas}" styleClass="mb-3">
                  <f:setPropertyActionListener target="#{itemauthor.currentItem.calculatedQuestion.showFormulasCalculation}" value="true"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CalculatedQuestionFormulaValidatorListener"/>
                </h:commandButton>
                <h:outputLabel value="&lt;p&gt;#{authorMessages.no_formulas_defined}&lt;/p&gt;" escape="false" rendered="#{itemauthor.currentItem.calculatedQuestion.formulasList eq '[]'}"/>
              </div>
              <!-- display calculations  -->
              <div class="longtext">
                <h:outputLabel value="#{authorMessages.calc_question_calculation_label} "/>
                <ul>
                  <li>
                    <h:outputText value="#{authorMessages.calc_question_define_calculations}"/>
                  </li>
                </ul>
              </div>
              <div class="tier2">
                <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="calculations" value="#{itemauthor.currentItem.calculatedQuestion.calculationsList}" var="calculation" rendered="#{itemauthor.currentItem.calculatedQuestion.hasCalculations}">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_calculation_col}"/>
                    </f:facet>
                    <h:outputText escape="false" value="#{calculation.text}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_calculation_sample_col}"/>
                    </f:facet>
                    <h:outputText escape="false" value="#{calculation.formula} = #{calculation.value}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.calc_question_calculation_status_col}"/>
                    </f:facet>
                    <h:outputText value="#{calculation.status}"/>
                  </h:column>
                </h:dataTable>
                <h:outputLabel value="&lt;p&gt;#{authorMessages.calc_question_no_calculations}&lt;/p&gt;" escape="false" rendered="#{! itemauthor.currentItem.calculatedQuestion.hasCalculations}"/>
              </div>
              <br/>
              <br/>
              <!-- 5a TIMED  -->
              <jsp:directive.include file="/jsf/author/item/timed.jsp"/>
              <!-- 6 PART  -->
              <h:panelGroup styleClass="mb-3 row" layout="block" rendered="#{itemauthor.target == 'assessment' &amp;&amp; !author.isEditPoolFlow}">
                <h:outputLabel for="assignToPart" value="#{authorMessages.assign_to_p}" styleClass="col-md-2 form-label"/>
                <div class="col-md-10">
                  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                    <f:selectItems value="#{itemauthor.sectionSelectList}"/>
                  </h:selectOneMenu>
                </div>
              </h:panelGroup>
              <!-- 7 POOL  -->
              <h:panelGroup styleClass="mb-3 row" layout="block" rendered="#{itemauthor.target == 'assessment' &amp;&amp; author.isEditPendingAssessmentFlow}">
                <h:outputLabel for="assignToPool" value="#{authorMessages.assign_to_question_p}" styleClass="col-md-2 form-label"/>
                <div class="col-md-10">
                  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
                    <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}"/>
                    <f:selectItems value="#{itemauthor.poolSelectList}"/>
                  </h:selectOneMenu>
                </div>
              </h:panelGroup>
              <!-- 8 FEEDBACK  -->
              <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' &amp;&amp; (author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'))}">
                <div class="mb-3 row">
                  <h2>
                    <h:outputText value="#{authorMessages.correct_incorrect_an}" styleClass="col-md-12 form-label"/>
                  </h2>
                </div>
                <div class="mb-3 row">
                  <h:outputLabel for="questionFeedbackCorrect_textinput" value="#{authorMessages.correct_answer_opti}" styleClass="form-label"/>
                  <!-- WYSIWYG  -->
                  <div class="col-md-10">
                    <h:panelGrid>
                      <samigo:wysiwyg identity="questionFeedbackCorrect" rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                      </samigo:wysiwyg>
                    </h:panelGrid>
                  </div>
                </div>
                <div class="mb-3 row">
                  <h:outputLabel for="questionFeedbackIncorrect_textinput" value="#{authorMessages.incorrect_answer_op}" styleClass="form-label"/>
                  <!-- WYSIWYG  -->
                  <div class="col-md-10">
                    <h:panelGrid>
                      <samigo:wysiwyg identity="questionFeedbackIncorrect" rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                      </samigo:wysiwyg>
                    </h:panelGrid>
                  </div>
                </div>
              </h:panelGroup>
              <!-- METADATA  -->
              <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
                <div class="mb-3 row">
                  <h:outputLabel value="Metadata" styleClass="col-md-12 form-label"/>
                </div>
                <div class="mb-3 row">
                  <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-2 form-label"/>
                  <div class="col-md-5">
                    <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
                  </div>
                </div>
                <div class="mb-3 row">
                  <h:outputLabel for="keyword" value="#{authorMessages.keyword}" styleClass="col-md-2 form-label"/>
                  <div class="col-md-5">
                    <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
                  </div>
                </div>
                <div class="mb-3 row">
                  <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" styleClass="col-md-2 form-label"/>
                  <div class="col-md-5">
                    <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
                  </div>
                </div>
              </h:panelGroup>
              <jsp:directive.include file="/jsf/author/item/tags.jsp"/>
              <p class="act">
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener"/>
                </h:commandButton>
                <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener"/>
                </h:commandButton>
              </p>
            </h:form>
            <!-- end content  -->
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
