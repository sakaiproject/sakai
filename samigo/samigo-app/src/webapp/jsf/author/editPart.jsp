<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
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
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.create_modify_p}" /></title>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
      <samigo:script path="/../library/js/spinner.js" type="text/javascript"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<%-- content... --%>
<%-- some back end stuff stubbed --%>
<%-- TODO need to add validation--%>

<h3><h:outputText value="#{authorMessages.create_modify_p} #{authorMessages.dash} #{sectionBean.assessmentTitle}" escape="false"/></h3>
<h:form id="modifyPartForm"  onsubmit="return editorCheck();">
    <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
    <h:inputHidden id="assessmentId" value="#{sectionBean.assessmentId}"/>
    <h:inputHidden id="sectionId" value="#{sectionBean.sectionId}"/>

    <div class="tier1">
        <div class="titleEditor">
            <h:outputLabel for="title" value="#{authorMessages.title}" />
            <h:inputText id="title" maxlength="250" value="#{sectionBean.sectionTitle}"/>
            <span class="note"><h:outputText value="#{authorMessages.title_note}" /></span>
        </div>
        
        <div class="infoEditor">
            <h:outputLabel value="#{authorMessages.information}" />
            <samigo:wysiwyg rows="140" value="#{sectionBean.sectionDescription}" hasToggle="yes" mode="author">
              <f:validateLength minimum="1" maximum="60000"/>
            </samigo:wysiwyg>
        </div>
        
        <%-- PART ATTACHMENTS --%>
        <%@ include file="/jsf/author/editPart_attachment.jsp" %>
        
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


            <%-- Randomization --%>
            <t:fieldset styleClass="roundedBorder" legend="#{authorMessages.randomization}" rendered="#{sectionBean.type == '2'}">
                <t:div id="drawOption" rendered="#{sectionBean.type == '2'}">
                    <h:outputText rendered="#{sectionBean.type == '2'}" value="#{authorMessages.random_draw_questions_prefix}"/>
                    <h:inputText id="numSelected" rendered="#{sectionBean.type == '2'}"
                                 disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" value="#{sectionBean.numberSelected}" />
                    <h:outputText rendered="#{sectionBean.type == '2'}" value="#{authorMessages.random_draw_questions_suffix}"/>
                    <h:selectOneMenu rendered="#{sectionBean.type == '2'}" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}"
                                     id="assignToPool" value="#{sectionBean.selectedPool}">
                        <%--<f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_for_random_draw}(###)" />--%>
                        <f:selectItems value="#{sectionBean.poolsAvailable}" />
                    </h:selectOneMenu>
                </t:div>
                <t:div id="randomSubmitOption" rendered="#{sectionBean.type == '2'}">
                    <h:selectOneRadio rendered="#{sectionBean.type == '2'}" value="#{sectionBean.randomizationType}" layout="pageDirection" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" id="randomizationType">
                     <f:selectItems value="#{sectionBean.randomizationTypeList}" />
                    </h:selectOneRadio>
                </t:div>
            </t:fieldset>

            <%-- Scoring --%>
            <t:fieldset styleClass="roundedBorder" legend="#{authorMessages.scoring}" rendered="#{sectionBean.type == '2'}">
                <t:div id="pointsOption" rendered="#{sectionBean.type == '2'}">
                    <h:outputText rendered="#{sectionBean.type == '2'}" value="#{authorMessages.random_draw_correct_prefix}"/>
                    <h:inputText rendered="#{sectionBean.type == '2'}" id="numPointsRandom" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" value="#{sectionBean.randomPartScore}" styleClass="ConvertPoint"/>
                    <h:outputText rendered="#{sectionBean.type == '2'}" value="#{authorMessages.random_draw_correct_suffix}"/>
                </t:div>
                <t:div id="deductOption" rendered="#{sectionBean.type == '2'}">
                    <h:outputText rendered="#{sectionBean.type == '2'}" value="#{authorMessages.random_draw_deduct_prefix}"/>
                    <h:inputText rendered="#{sectionBean.type == '2'}" id="numDiscountRandom" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" value="#{sectionBean.randomPartDiscount}" styleClass="ConvertPoint"/>
                    <h:outputText rendered="#{sectionBean.type == '2'}" value="#{authorMessages.random_draw_deduct_suffix}"/>
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
