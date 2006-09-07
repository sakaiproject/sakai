
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
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      <samigo:script path="/js/authoring.js"/>
      </head>
<body onload="countNum();;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- FORM -->



<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
 <div class="tier2">
<!-- QUESTION PROPERTIES -->
  <!-- 1 POINTS -->

<div class="shorttext">
    <h:outputLabel value="#{msg.answer_point_value}"/>
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true">
<f:validateDoubleRange />
</h:inputText>
 <h:message for="answerptr" styleClass="validate"/>
  </div><br/>

  <!-- 2 TEXT -->
 <div class="longtext">
  <h:outputLabel value="#{msg.q_text}" />
  <!-- STUB FOR WYSIWYG -->

  <!-- WYSIWYG -->
   
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}">
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

  <!-- 3 ANSWER -->
  <div class="longtext">
  <h:outputLabel value="#{msg.answer} " /></div>
   <div class="tier2">
  <h:selectOneRadio layout="lineDirection" id="TF" border="0"
     value="#{itemauthor.currentItem.corrAnswer}" required="true">
     <f:selectItems value="#{itemauthor.trueFalseAnswerSelectList}" />
  </h:selectOneRadio>
<h:message for="TF" styleClass="validate"/>
</div>

    <!-- 4 RATIONALE -->
    <div class="longtext">
    <h:outputLabel value="#{msg.req_rationale}" /></div>
	<div class="tier2">
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" id="rational" required="true">
     <f:selectItem itemValue="true"
       itemLabel="#{msg.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{msg.no}" />
    </h:selectOneRadio>
<br/> <h:message for="rational" styleClass="validate"/>
  </div>

  <!-- 5 PART -->
<div class="longtext">
  <h:panelGrid rendered="#{itemauthor.target == 'assessment'}" columnClasses="shorttext">  
  <h:panelGroup>
     <f:verbatim></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <!-- use this in real  value="#{section.sectionNumberList}" -->
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>
  </div>

  <!-- 6 POOL -->
<div class="longtext">
  <h:panelGrid rendered="#{itemauthor.target == 'assessment'}" columnClasses="shorttext">
  <h:panelGroup>
     <f:verbatim></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>
  </div>

   <!-- FEEDBACK -->
<h:panelGroup rendered="#{assessmentSettings.feedbackAuthoring ne '2'}"><br/>
  <f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{msg.correct_incorrect_an}" />
<f:verbatim></div>
 <div class="tier2"></f:verbatim>
  <h:outputLabel value="#{msg.correct_answer_opti}" />

  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
</h:panelGrid>

 <f:verbatim></div><div class="tier2"></f:verbatim>
 <h:outputLabel  value="#{msg.incorrect_answer_op}" />

  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid><f:verbatim></div></f:verbatim>
</h:panelGroup>
 <!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="tier3"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel value="#{msg.objective}" />
  <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputLabel value="#{msg.keyword}" />
  <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputLabel value="#{msg.rubric_colon}" />
  <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</div>


<%--
<div class="longtext tier1">
  <h:panelGrid columns="3" rendered="#{itemauthor.showMetadata == 'true'}">
  <f:verbatim><span id="num8" class="number"></span></f:verbatim>
  <h:outputLabel for="obj" value="#{msg.objective}" />
  <h:inputText id="obj" value="#{itemauthor.currentItem.objective}" />
  <f:verbatim><span id="num9" class="number"></span></f:verbatim>
  <h:outputLabel for="keyword" value="#{msg.keyword}" />
  <h:inputText id="keyword" value="#{itemauthor.currentItem.keyword}" />
  <f:verbatim><span id="num10" class="number"></span></f:verbatim>
  <h:outputLabel for="rubric" value="#{msg.rubric_colon}" />
  <h:inputText id="rubric" value="#{itemauthor.currentItem.rubric}" />
  </h:panelGrid>

  </div>
--%>

<p class="act">
  <h:commandButton accesskey="#{msg.a_save}" rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton accesskey="#{msg.a_save}" rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>


  <h:commandButton accesskey="#{msg.a_cancel}" rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_cancel}" action="editAssessment" immediate="true"/>
 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_cancel}" action="editPool" immediate="true"/>

</p>
</h:form>


<!-- end content -->
</div>
    </body>
  </html>
</f:view>
