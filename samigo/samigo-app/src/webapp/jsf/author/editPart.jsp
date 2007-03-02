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
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
 <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.create_modify_p}" /></title>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- some back end stuff stubbed -->
<!-- TODO need to add validation-->

<h3>
     <h:outputText value="#{msg.create_modify_p} #{msg.dash} #{sectionBean.assessmentTitle}" /></h3>
<h:form id="modifyPartForm"  onsubmit="return editorCheck();">
<h:messages styleClass="validation"/>
  <h:inputHidden id="assessmentId" value="#{sectionBean.assessmentId}"/>
  <h:inputHidden id="sectionId" value="#{sectionBean.sectionId}"/>


  <div class="tier1">
  <div class="shorttext">
<h:panelGrid columns="2" columnClasses="shorttext">
   <h:outputLabel for="title" value="#{msg.title}" />
   <h:inputText id="title" size="50" maxlength="250" value="#{sectionBean.sectionTitle}"/>
     <h:outputText value="" />
     <h:outputText value="#{msg.title_note}" /></h3>
</h:panelGrid>
  </div>
   <div class="longtext">
   <h:outputLabel value="#{msg.information}" />
<br/>
  <h:panelGrid width="50%">
   <samigo:wysiwyg rows="140" value="#{sectionBean.sectionDescription}">
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>

  <!-- PART ATTACHMENTS -->
  <%@ include file="/jsf/author/editPart_attachment.jsp" %>

</div>

 <!-- Part Type -->
   <div class="longtext">
   <h:outputText value="#{msg.type}" />
   <h:panelGroup >
 <!--  had to separate the radio buttons , 'cuz there is no way to disable only one of them. -->
   <div class="longtext">
     <h:selectOneRadio value="#{sectionBean.type}" layout="pageDirection" onclick="this.form.onsubmit();document.forms[0].submit();" onkeypress="this.form.onsubmit();document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}">
     <f:selectItems value="#{sectionBean.authorTypeList}" />
     </h:selectOneRadio>
     <h:selectOneRadio accesskey="#{msg.a_options}" rendered="#{sectionBean.hideRandom eq 'true'}" disabled="true" value="" layout="pageDirection" >
       <f:selectItem itemValue="2" itemLabel="#{msg.random_draw_from_que}" />
     </h:selectOneRadio>

<%--
     <h:selectOneRadio rendered="{#sectionBean.hideRandom ne 'true'}" value="#{sectionBean.type}" layout="pageDirection" onclick="this.form.onsubmit();document.forms[0].submit();" onkeypress="this.form.onsubmit();document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}">
       <f:selectItem itemValue="1" itemLabel="#{msg.type_onebyone}" />
       <f:selectItem itemValue="2" itemLabel="#{msg.random_draw_from_que}" />
     </h:selectOneRadio>

     <h:selectOneRadio rendered="#{sectionBean.hideRandom eq 'true'}" value="#{sectionBean.type}" layout="pageDirection" onclick="this.form.onsubmit();document.forms[0].submit();" onkeypress="this.form.onsubmit();document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}">
       <f:selectItem itemValue="1" itemLabel="#{msg.type_onebyone}" />
     </h:selectOneRadio>

     <h:selectOneRadio disabled="#{sectionBean.type =='1'}" value="#{sectionBean.type}" layout="pageDirection" onclick="document.forms[0].submit();"  onkeypress="document.forms[0].submit();" valueChangeListener="#{sectionBean.toggleAuthorType}">
       <f:selectItem itemValue="2" itemLabel="#{msg.random_draw_from_que}" />
     </h:selectOneRadio>
--%>

  <div class="tier2">

<%--
<h:panelGrid rendered="#{sectionBean.hideRandom eq 'false'}" columns="2" columnClasses="longtext" >
--%>

<h:panelGrid columns="2" columnClasses="longtext" >
 
   <h:outputText value="#{msg.pool_name} #{msg.number_questions} " />
   <h:selectOneMenu disabled="#{sectionBean.type == '1'}" id="assignToPool" value="#{sectionBean.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_for_random_draw}(###)" />
     <f:selectItems value="#{sectionBean.poolsAvailable}" />
  </h:selectOneMenu>
<!--h:message for="assignToPool" rendered="#{sectionBean.type != '1'}" styleClass="validate"/-->
 <h:outputText value="#{msg.number_of_qs}" />

<!--h:selectOneMenu disabled="#{sectionBean.type == '1'}" required="true" id="sumSelected" value="#{sectionBean.numberSelected}"-->
    
     <!--f:selectItems value="#{sectionBean.poolsAvailable}" /-->
  <!--/h:selectOneMenu-->
   <h:inputText id="numSelected" disabled="#{sectionBean.type == '1'}" value="#{sectionBean.numberSelected}" />

  <h:outputText value="#{msg.type_of_randomization}" />
  <h:selectOneRadio value="#{sectionBean.randomizationType}" layout="pageDirection" disabled="#{sectionBean.type == '1'}" id="randomizationType">
     <f:selectItems value="#{sectionBean.randomizationTypeList}" />
  </h:selectOneRadio>

</h:panelGrid>



</div>
   </h:panelGroup>

</div>

 <!-- Question Ordering -->
   <div class="longtext">
   <h:panelGroup >
   <h:outputText value="#{msg.q_ordering_n}" />

     <h:selectOneRadio disabled="#{sectionBean.type =='2'}"layout="pageDirection" value="#{sectionBean.questionOrdering}">
       <f:selectItem itemLabel="#{msg.as_listed_on_assessm}"
         itemValue="1"/>
       <f:selectItem itemLabel="#{msg.random_within_p}"
         itemValue="2"/>
     </h:selectOneRadio>
   </h:panelGroup>
   </div>

   <div class="longtext">
 <!-- METADATA -->
   <h:outputText value="#{msg.metadata}" />

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel for="obj" value="#{msg.objective}" />
  <h:inputText id="obj" value="#{sectionBean.objective}" />
<h:outputLabel for="keyword" value="#{msg.keyword}" />
  <h:inputText id="keyword" value="#{sectionBean.keyword}" />
<h:outputLabel for="rubric" value="#{msg.rubric_colon}" />
  <h:inputText id="rubric" value="#{sectionBean.rubric}" />
</h:panelGrid>

</div>
</div>


  <p class="act">
     <h:commandButton value="#{msg.button_save}" type="submit" accesskey="#{msg.a_save}"
       styleClass="active" action="#{sectionBean.getOutcome}" >
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.SavePartListener" />
     </h:commandButton>
     <h:commandButton accesskey="#{msg.a_cancel}" value="#{msg.button_cancel}" style="act" immediate="true" action="editAssessment" >
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

