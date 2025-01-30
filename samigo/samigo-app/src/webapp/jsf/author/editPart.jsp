<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

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
<%
	String thisId = request.getParameter("panel");
	if (thisId == null) {
		thisId = "Main"	+ org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
	}
    String selectId = "modifyPartForm:assignToPool";
    String selectIdFixed = "modifyPartForm:assignToPoolFixed";
%>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.create_modify_p}" /></title>
      <!-- AUTHORING -->
      <script src="/samigo-app/js/authoring.js"></script>
      <script src="/library/js/spinner.js"></script>
      <script>includeWebjarLibrary('select2');</script>
      <script src="/samigo-app/js/select2.js"></script>
      <script>includeWebjarLibrary('bootstrap-multiselect');</script>
      <script>
        window.addEventListener("load", () => {
          // Initialize bootstrap multiselect
          const multiplePoolsSelect = document.getElementById("modifyPartForm:multiplePools");

          const filterPlaceholder = `<h:outputText value="#{assessmentSettingsMessages.multiselect_filterPlaceholder}" />`;
          const selectAllText = `<h:outputText value="#{authorMessages.multiselect_select_all_pools}" />`;
          const nonSelectedText = `<h:outputText value="#{authorMessages.multiselect_no_pools_selected}" />`;
          const allSelectedText = `<h:outputText value="#{authorMessages.multiselect_select_all_pools}" />`;
          const nSelectedText = `<h:outputText value="#{authorMessages.multiselect_n_pools_selected}" />`;

          $(multiplePoolsSelect).multiselect({
              enableFiltering: true,
              enableCaseInsensitiveFiltering: true,
              includeSelectAllOption: true,
              filterPlaceholder: filterPlaceholder,
              selectAllText: selectAllText,
              nonSelectedText: nonSelectedText,
              allSelectedText: allSelectedText,
              nSelectedText: nSelectedText,
              templates: {
                button: '<button type="button" class="multiselect dropdown-toggle btn-primary" data-bs-toggle="dropdown"><span class="multiselect-selected-text"></span><i class="si si-caret-down-fill ps-2"></i></button>',
                filter: '<div class="multiselect-filter d-flex align-items-center"><i class="fa fa-sm fa-search text-muted"></i><input type="search" class="multiselect-search form-control" /></div>',
              },
          });
      });
      </script>
    </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<%-- content... --%>
<%-- some back end stuff stubbed --%>
<%-- TODO need to add validation--%>
<input id="toolId" type="hidden" value="<%= thisId %>">
<input id="selectorId" type="hidden" value="<%= selectId %>">
<input id="selectorIdFixed" type="hidden" value="<%= selectIdFixed %>">

<h3><h:outputText value="#{authorMessages.create_modify_p} #{authorMessages.dash} #{sectionBean.assessmentTitle}" escape="false"/></h3>
<h:form id="modifyPartForm"  onsubmit="return editorCheck();">
    <f:verbatim><input type="hidden" id="ckeditor-autosave-context" name="ckeditor-autosave-context" value="samigo_editPart" /></f:verbatim>
    <h:panelGroup rendered="#{sectionBean.sectionId!=null}"><f:verbatim><input type="hidden" id="ckeditor-autosave-entity-id" name="ckeditor-autosave-entity-id" value="</f:verbatim><h:outputText value="#{sectionBean.sectionId}"/><f:verbatim>"/></f:verbatim></h:panelGroup>

    <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
    <h:inputHidden id="assessmentId" value="#{sectionBean.assessmentId}"/>
    <h:inputHidden id="sectionId" value="#{sectionBean.sectionId}"/>
    <h:inputHidden id="fixedQuestionIds" value="#{sectionBean.fixedQuestionIds}" />

    <div class="tier1">
        <div class="titleEditor">
            <h:outputLabel for="title" value="#{authorMessages.title}" />
            <h:inputText id="title" maxlength="250" value="#{sectionBean.sectionTitle}"/>
            <span class="note"><h:outputText value="#{authorMessages.title_note}" /></span>
        </div>
        
        <div class="infoEditor">
            <h:outputLabel value="#{authorMessages.information}" />
            <samigo:wysiwyg rows="140" value="#{sectionBean.sectionDescription}" hasToggle="yes" mode="author">
              <f:validateLength maximum="60000"/>
            </samigo:wysiwyg>
        </div>
        
        <%-- PART ATTACHMENTS --%>
        <%@ include file="/jsf/author/editPart_attachment.jsp" %>

        <%-- TIMED --%>
        <fieldset>
            <legend>
                <h:outputText value="#{authorMessages.timed}" />
                <div id="typeSpinner" class="allocatedSpinPlaceholder"></div>
            </legend>
            <div class="row">
                <h:outputLabel styleClass="col-lg-2" value="#{assessmentSettingsMessages.assessment_timed}" />
                <div class="col-lg-9">
                    <t:selectOneRadio id="selTimed" value="#{sectionBean.timedSection}" layout="spread" onclick="toggleSection('modifyPartForm:timeSection', this.value)">
                        <f:selectItem itemValue="false" itemLabel="#{assessmentSettingsMessages.assessment_not_timed}"/>
                        <f:selectItem itemValue="true" itemLabel="#{assessmentSettingsMessages.assessment_is_timed}"/>
                    </t:selectOneRadio>
                    <ul class="ulTimed">
                        <li>
                            <t:radio renderLogicalId="true" for="selTimed" index="0" />
                        </li>
                        <li>
                            <t:radio renderLogicalId="true" for="selTimed" index="1" />

                            <h:panelGroup id="timeSection" styleClass="#{!sectionBean.timedSection ? 'hidden' : ''}">
                                <span>.&#160;</span>
                                <h:outputLabel id="isTimedTimeLimitLabel" value="#{assessmentSettingsMessages.assessment_is_timed_limit} " />
                                
                                <input type="number" min="0" max="11" onchange="this.reportValidity()? document.getElementById('modifyPartForm:hiddenTimedHours').value=this.value : false" value="<h:outputText value="#{sectionBean.timedHours}" />"/>
                                <h:inputHidden id="hiddenTimedHours" value="#{sectionBean.timedHours}" />
                                <h:outputLabel id="timedHoursLabel"  value="#{assessmentSettingsMessages.timed_hours} " />
                                
                                <input type="number" min="0" max="59" onchange="this.reportValidity()? document.getElementById('modifyPartForm:hiddenTimedMinutes').value=this.value : false" value="<h:outputText value="#{sectionBean.timedMinutes}" />"/>
                                <h:inputHidden id="hiddenTimedMinutes" value="#{sectionBean.timedMinutes}" />
                                <h:outputLabel id="timedMinutesLabel" value="#{assessmentSettingsMessages.timed_minutes} " />
                            </h:panelGroup>
                        </li>
                    </ul>
                    <h:outputLabel styleClass="help-block info-text small" value="#{assessmentSettingsMessages.part_timed_info}" />
                </div>
            </div>
        </fieldset>
        
        <%-- Type --%>
        <fieldset>
            <legend>
                <h:outputText value="#{authorMessages.type}" />
                <div id="typeSpinner" class="allocatedSpinPlaceholder"></div>
            </legend>
            <h:selectOneRadio id="typeTable" value="#{sectionBean.type}" layout="pageDirection" valueChangeListener="#{sectionBean.toggleAuthorType}"
                       onclick="SPNR.insertSpinnerInPreallocated( this, null, 'typeSpinner' );this.form.onsubmit();document.forms[0].submit();"
                       onkeypress="this.form.onsubmit();document.forms[0].submit();" 
                       disabled="#{!author.isEditPendingAssessmentFlow}" disabledClass="inactive">
                <f:selectItems value="#{sectionBean.authorTypeList}" />
            </h:selectOneRadio>
        </fieldset>
        
        <%-- Options --%>
        <fieldset class="addEditPartOptions">
            <legend><h:outputText value="#{authorMessages.options}"/></legend>

            <%-- Question Ordering --%>    
            <t:fieldset styleClass="roundedBorder" legend="#{authorMessages.q_ordering_n}" rendered="#{sectionBean.type == '1'}">
                <h:selectOneRadio rendered="#{sectionBean.type =='1'}" layout="pageDirection" value="#{sectionBean.questionOrdering}">
                    <f:selectItem itemLabel="#{authorMessages.as_listed_on_assessm}" itemValue="1"/>
                    <f:selectItem itemLabel="#{authorMessages.random_within_p}" itemValue="2"/>
                </h:selectOneRadio>
            </t:fieldset>

            <%-- Fixed --%>
            <t:fieldset styleClass="roundedBorder" legend="#{authorMessages.fixed}" rendered="#{sectionBean.type == '3'}">
                <h:outputText rendered="#{sectionBean.type == '3'}" value="#{authorMessages.random_fixed_and_draw_questions_prefix}"/>
                <h:outputText rendered="#{sectionBean.type == '3'}" value="#{authorMessages.random_draw_questions_suffix}"/>
                <h:selectOneMenu rendered="#{sectionBean.type == '3'}" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}"
                                 id="assignToPoolFixed" value="#{sectionBean.selectedPoolFixed}" valueChangeListener="#{sectionBean.fixedRandomizationTypeList}" onchange="SPNR.insertSpinnerInPreallocated( this, null, 'typeSpinner' );this.form.onsubmit();document.forms[0].submit();">
                    <f:selectItems value="#{sectionBean.poolsAvailable}" />
                </h:selectOneMenu>
                <t:dataTable value="#{sectionBean.allItems}" var="question" styleClass="table table-striped table-hover table-bordered" id="questionpool-questions" rowIndexVar="row">
                    <h:column id="colremove" rendered="#{questionpool.importToAuthoring == 'false'}" headerClass="columnCheckDelete">
                        <f:facet name="header">
                        </f:facet>
                        <h:selectManyCheckbox immediate="true" id="randomizationTypesFixed"  value="#{sectionBean.fixedQuestionIds}">
                            <f:selectItem itemValue="#{question.itemIdString}" itemLabel=""/>
                         </h:selectManyCheckbox>
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <h:panelGroup>
                                <h:outputText value="#{questionPoolMessages.q_text}" />
                             </h:panelGroup>
                         </f:facet>
                         <h:outputText escape="false" value="#{questionPoolMessages.q_question} #{row + 1} : #{question.themeText}" rendered="#{question.typeId == 14}"/>
                         <h:outputText escape="false" value="#{questionPoolMessages.q_question} #{row + 1} : #{itemContents.htmlStripped[question.text]}" rendered="#{question.typeId ne 14}"/>
                     </h:column>
                     <h:column>
                          <f:facet name="header">
                            <h:panelGroup>
                              <h:outputText value="#{questionPoolMessages.q_type}" />
                            </h:panelGroup>
                          </f:facet>
                         <h:outputText rendered="#{question.typeId== 1}" value="#{authorMessages.multiple_choice_type}"/>
                         <h:outputText rendered="#{question.typeId== 2}" value="#{authorMessages.multiple_choice_type}"/>
                         <h:outputText rendered="#{question.typeId== 3}" value="#{authorMessages.multiple_choice_surv}"/>
                         <h:outputText rendered="#{question.typeId== 4}" value="#{authorMessages.true_false}"/>
                         <h:outputText rendered="#{question.typeId== 5}" value="#{authorMessages.short_answer_essay}"/>
                         <h:outputText rendered="#{question.typeId== 6}" value="#{authorMessages.file_upload}"/>
                         <h:outputText rendered="#{question.typeId== 7}" value="#{authorMessages.audio_recording}"/>
                         <h:outputText rendered="#{question.typeId== 8}" value="#{authorMessages.fill_in_the_blank}"/>
                         <h:outputText rendered="#{question.typeId== 9}" value="#{authorMessages.matching}"/>
                         <h:outputText rendered="#{question.typeId== 11}" value="#{authorMessages.fill_in_numeric}"/>
                         <h:outputText rendered="#{question.typeId== 12}" value="#{authorMessages.multiple_choice_type}"/>
                         <h:outputText rendered="#{question.typeId== 14}" value="#{authorMessages.extended_matching_items}"/>
                         <h:outputText rendered="#{question.typeId== 13}" value="#{authorMessages.matrix_choice_surv}"/>
                         <h:outputText rendered="#{question.typeId== 15}" value="#{authorMessages.calculated_question}"/><!-- // CALCULATED_QUESTION -->
                         <h:outputText rendered="#{question.typeId== 16}" value="#{authorMessages.image_map_question}"/><!-- // IMAGEMAP_QUESTION -->
                    </h:column>
                 </t:dataTable>
            </t:fieldset>

            <%-- Randomization --%>
            <t:fieldset styleClass="roundedBorder" legend="#{authorMessages.randomization}" rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}">
                <t:div id="drawOption" rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}">
                    <h:outputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" value="#{authorMessages.random_draw_questions_prefix}"/>
                    <h:inputText id="numSelected" rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}"
                                 disabled="#{!author.isEditPendingAssessmentFlow}" value="#{sectionBean.numberSelected}" />
                    <h:outputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" value="#{authorMessages.random_draw_questions_suffix}"/>
                    <h:selectOneMenu rendered="#{sectionBean.type == '2' || sectionBean.type == '3'}" disabled="#{!author.isEditPendingAssessmentFlow}"
                                     id="assignToPool" value="#{sectionBean.selectedPool}">
                        <f:selectItems value="#{sectionBean.poolsAvailable}" />
                    </h:selectOneMenu>
                    <h:selectManyListbox rendered="#{sectionBean.type == '4'}" id="multiplePools" value="#{sectionBean.selectedPoolsMultiple}">
                      <f:selectItems value="#{sectionBean.poolsAvailable}" />
                    </h:selectManyListbox>
  </div>
                </t:div>
                <t:div id="randomSubmitOption" rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}">
                    <h:selectOneRadio rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" value="#{sectionBean.randomizationType}" layout="pageDirection" disabled="#{!author.isEditPendingAssessmentFlow}" id="randomizationType">
                     <f:selectItems value="#{sectionBean.randomizationTypeList}" />
                    </h:selectOneRadio>
                </t:div>
            </t:fieldset>

            <%-- Scoring --%>
            <t:fieldset styleClass="roundedBorder" legend="#{authorMessages.scoring}" rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}">
                <t:div id="pointsOption" rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}">
                    <h:outputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" value="#{authorMessages.random_draw_correct_prefix}"/>
                    <h:inputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" id="numPointsRandom" disabled="#{!author.isEditPendingAssessmentFlow}" value="#{sectionBean.randomPartScore}" styleClass="ConvertPoint"/>
                    <h:outputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" value="#{authorMessages.random_draw_correct_suffix}"/>
                </t:div>
                <t:div id="deductOption" rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}">
                    <h:outputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" value="#{authorMessages.random_draw_deduct_prefix}"/>
                    <h:inputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" id="numDiscountRandom" disabled="#{!author.isEditPendingAssessmentFlow}" value="#{sectionBean.randomPartDiscount}" styleClass="ConvertPoint"/>
                    <h:outputText rendered="#{sectionBean.type == '2' || sectionBean.type == '3' || sectionBean.type == '4'}" value="#{authorMessages.random_draw_deduct_suffix}"/>
                </t:div>
            </t:fieldset>
        </fieldset>
        <%-- End Options --%>
        
        <%-- METADATA --%>
        <fieldset>
            <legend><h:outputText value="#{authorMessages.metadata}"/></legend>
            <h:panelGrid columns="2" columnClasses="shorttext">
                <h:outputLabel for="obj" value="#{authorMessages.objective}" />
                <h:inputText id="obj" value="#{sectionBean.objective}" disabled="#{!author.isEditPendingAssessmentFlow}"/>
                <h:outputLabel for="keyword" value="#{authorMessages.keyword}" />
                <h:inputText id="keyword" value="#{sectionBean.keyword}" disabled="#{!author.isEditPendingAssessmentFlow}"/>
                <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" />
                <h:inputText id="rubric" value="#{sectionBean.rubric}" disabled="#{!author.isEditPendingAssessmentFlow}"/>
            </h:panelGrid>
        </fieldset>

  <p class="act">
     <h:commandButton value="#{commonMessages.action_save}" type="submit" onclick="SPNR.disableControlsAndSpin( this, null );"
       styleClass="active" action="#{sectionBean.getOutcome}" >
        <f:param name="fixedQuestionIds" value="#{sectionBean.fixedQuestionIds}"/>
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.SavePartListener" />
     </h:commandButton>
     <h:commandButton value="#{commonMessages.cancel_action}" style="act" immediate="true" action="editAssessment" onclick="SPNR.disableControlsAndSpin( this, null );" >
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.ResetPartAttachmentListener" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
     </h:commandButton>
  </p>
  
<h:outputText value="#{authorMessages.required}" />

</h:form>
<!-- end content -->
</div>

</body>
</html>
</f:view>
