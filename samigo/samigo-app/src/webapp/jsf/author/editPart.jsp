<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
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
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.create_modify_p}" /></title>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- some back end stuff stubbed -->
<!-- TODO need to add validation-->

<h3>
     <h:outputText value="#{authorMessages.create_modify_p} #{authorMessages.dash} #{sectionBean.assessmentTitle}" escape="false"/></h3>
<h:form id="modifyPartForm"  onsubmit="return editorCheck();">
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
  <h:inputHidden id="assessmentId" value="#{sectionBean.assessmentId}"/>
  <h:inputHidden id="sectionId" value="#{sectionBean.sectionId}"/>


  <div class="tier1">
  <div class="shorttext">
<h:panelGrid columns="2" columnClasses="shorttext">
   <h:outputLabel for="title" value="#{authorMessages.title}" />
   <h:inputText id="title" size="50" maxlength="250" value="#{sectionBean.sectionTitle}"/>
     <h:outputText value="" />
     <h:outputText value="#{authorMessages.title_note}" /></h3>
</h:panelGrid>
  </div>
   <div class="longtext">
   <h:outputLabel value="#{authorMessages.information}" />
<br/>

  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{sectionBean.sectionDescription}" hasToggle="yes">
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>

  <!-- PART ATTACHMENTS -->
  <%@ include file="/jsf/author/editPart_attachment.jsp" %>

</div>

 <!-- Part Type -->
   <div class="longtext">
   <h:outputText value="#{authorMessages.type}" />
   <h:panelGroup >
 <!--  had to separate the radio buttons , 'cuz there is no way to disable only one of them. -->
   <div class="longtext">
     <h:selectOneRadio value="#{sectionBean.type}" layout="pageDirection" onclick="this.form.onsubmit();document.forms[0].submit();" onkeypress="this.form.onsubmit();document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}" disabled="#{!author.isEditPendingAssessmentFlow}">
     <f:selectItems value="#{sectionBean.authorTypeList}" />
     </h:selectOneRadio>
     <h:selectOneRadio accesskey="#{authorMessages.a_options}" rendered="#{sectionBean.hideRandom eq 'true'}" disabled="true" value="" layout="pageDirection" >
       <f:selectItem itemValue="2" itemLabel="#{authorMessages.random_draw_from_que}" />
     </h:selectOneRadio>

<%--
     <h:selectOneRadio rendered="{#sectionBean.hideRandom ne 'true'}" value="#{sectionBean.type}" layout="pageDirection" onclick="this.form.onsubmit();document.forms[0].submit();" onkeypress="this.form.onsubmit();document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}">
       <f:selectItem itemValue="1" itemLabel="#{authorMessages.type_onebyone}" />
       <f:selectItem itemValue="2" itemLabel="#{authorMessages.random_draw_from_que}" />
     </h:selectOneRadio>

     <h:selectOneRadio rendered="#{sectionBean.hideRandom eq 'true'}" value="#{sectionBean.type}" layout="pageDirection" onclick="this.form.onsubmit();document.forms[0].submit();" onkeypress="this.form.onsubmit();document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}">
       <f:selectItem itemValue="1" itemLabel="#{authorMessages.type_onebyone}" />
     </h:selectOneRadio>

     <h:selectOneRadio disabled="#{sectionBean.type =='1'}" value="#{sectionBean.type}" layout="pageDirection" onclick="document.forms[0].submit();"  onkeypress="document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}">
       <f:selectItem itemValue="2" itemLabel="#{authorMessages.random_draw_from_que}" />
     </h:selectOneRadio>
--%>

  <div class="tier2">

<%--
<h:panelGrid rendered="#{sectionBean.hideRandom eq 'false'}" columns="2" columnClasses="longtext" >
--%>

<h:panelGrid columns="2" columnClasses="longtext" >
 
   <h:outputText value="#{authorMessages.pool_name} #{authorMessages.number_questions} " />
   <h:panelGrid>
   <h:selectOneMenu disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" id="assignToPool" value="#{sectionBean.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_for_random_draw}(###)" />
     <f:selectItems value="#{sectionBean.poolsAvailable}" />
   </h:selectOneMenu>
   </h:panelGrid>

<!--h:message for="assignToPool" rendered="#{sectionBean.type != '1'}" styleClass="validate"/-->
   <h:outputText value="#{authorMessages.number_of_qs}" />

<!--h:selectOneMenu disabled="#{sectionBean.type == '1'}" required="true" id="sumSelected" value="#{sectionBean.numberSelected}"-->
    
     <!--f:selectItems value="#{sectionBean.poolsAvailable}" /-->
  <!--/h:selectOneMenu-->

   <h:panelGrid>
   <h:inputText id="numSelected" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" value="#{sectionBean.numberSelected}" />
   </h:panelGrid>

  <h:outputText value="#{authorMessages.point_value_of_questons}"  />
  <h:panelGrid>
    <h:inputText id="numPointsRandom" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" value="#{sectionBean.randomPartScore}" />
    <h:outputText value="#{authorMessages.note_point_value_for_question}" rendered="#{!sectionBean.pointValueHasOverrided}"/>
  </h:panelGrid>
   
  <h:outputText value="#{authorMessages.negative_point_value}"  />
  
  <h:panelGrid>
    <h:inputText id="numDiscountRandom" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" value="#{sectionBean.randomPartDiscount}" />
    <h:outputText value="#{authorMessages.note_negative_point_value_part}" rendered="#{!sectionBean.discountValueHasOverrided}"/>
  </h:panelGrid>

  <h:outputText value="#{authorMessages.type_of_randomization}" />
  <h:selectOneRadio value="#{sectionBean.randomizationType}" layout="pageDirection" disabled="#{sectionBean.type == '1' || !author.isEditPendingAssessmentFlow}" id="randomizationType">
     <f:selectItems value="#{sectionBean.randomizationTypeList}" />
  </h:selectOneRadio>

</h:panelGrid>



</div>
   </h:panelGroup>

</div>

 <!-- Question Ordering -->
   <div class="longtext">
   <h:panelGroup >
   <h:outputText value="#{authorMessages.q_ordering_n}" />

     <h:selectOneRadio disabled="#{sectionBean.type =='2' || !author.isEditPendingAssessmentFlow}" layout="pageDirection" value="#{sectionBean.questionOrdering}">
       <f:selectItem itemLabel="#{authorMessages.as_listed_on_assessm}"
         itemValue="1"/>
       <f:selectItem itemLabel="#{authorMessages.random_within_p}"
         itemValue="2"/>
     </h:selectOneRadio>
   </h:panelGroup>
   </div>

   <div class="longtext">
 <!-- METADATA -->
   <h:outputText value="#{authorMessages.metadata}" />

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel for="obj" value="#{authorMessages.objective}" />
  <h:inputText id="obj" value="#{sectionBean.objective}" disabled="#{!author.isEditPendingAssessmentFlow}"/>
<h:outputLabel for="keyword" value="#{authorMessages.keyword}" />
  <h:inputText id="keyword" value="#{sectionBean.keyword}" disabled="#{!author.isEditPendingAssessmentFlow}"/>
<h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" />
  <h:inputText id="rubric" value="#{sectionBean.rubric}" disabled="#{!author.isEditPendingAssessmentFlow}"/>
</h:panelGrid>

</div>
</div>


  <p class="act">
     <h:commandButton value="#{authorMessages.button_save}" type="submit" accesskey="#{authorMessages.a_save}"
       styleClass="active" action="#{sectionBean.getOutcome}" >
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.SavePartListener" />
     </h:commandButton>
     <h:commandButton accesskey="#{authorMessages.a_cancel}" value="#{authorMessages.button_cancel}" style="act" immediate="true" action="editAssessment" >
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.ResetPartAttachmentListener" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
     </h:commandButton>
  </p>

</h:form>
<!-- end content -->
</div>

      </body>
    </html>
  </f:view>

