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
  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice SURVEY questions only -->
  <!-- text for answers are predetermined(in properties file), do not allow users to change -->

  <!-- 1 POINTS -->
  <div class="tier2">
<div class="shorttext">

    <h:outputLabel value="#{msg.answer_point_value}"/>
    <h:outputText value="#{msg.zeropoints}"/>
 </div>
<br/>
  <!-- 2 TEXT -->
  <div class="longtext">
  <h:outputLabel value="#{msg.q_text}" />
  <!-- WYSIWYG -->
  <br/>
   
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}">
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

  <!-- 3 ANSWER -->
  <!-- qti survey type  
       PREDEFINED_SCALE: YES, AGREE, UNDECIDED, AVERAGE, STRONGLY_AGREE, EXCELLENT, 5, 10 
  -->

<div class="longtext">
    <h:outputLabel value="#{msg.answer} " /> </div>
   <div class="tier2">
     <h:message for="selectscale" styleClass="validate"/>
    <h:selectOneRadio layout="pageDirection" value="#{itemauthor.currentItem.scaleName}" id="selectscale" required="true">
     <f:selectItem itemValue="YES" itemLabel="#{msg.yes_no}" />
     <f:selectItem itemValue="AGREE" itemLabel="#{msg.disagree_agree}" />
     <f:selectItem itemValue="UNDECIDED" itemLabel="#{msg.disagree_undecided}" />
     <f:selectItem itemValue="AVERAGE"
	itemLabel="#{msg.below_average} -> #{msg.above_average}" />
     <f:selectItem itemValue="STRONGLY_AGREE"
       itemLabel="#{msg.strongly_disagree} -> #{msg.strongly_agree}" />
     <f:selectItem itemValue="EXCELLENT"
        itemLabel="#{msg.unacceptable} -> #{msg.excellent}" />
     <f:selectItem itemValue="5" itemLabel="#{msg.scale5}" />
     <f:selectItem itemValue="10" itemLabel="#{msg.scale10}" />
    </h:selectOneRadio>
  <br />

  </div>
    <!-- 4 PART -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel rendered="#{itemauthor.target == 'assessment'}" value="#{msg.assign_to_p}" />
  <h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}" id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
 </h:panelGrid>

    <!-- 5 POOL -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim>&nbsp;</f:verbatim>
  <h:outputLabel rendered="#{itemauthor.target == 'assessment'}" value="#{msg.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
  </h:panelGrid>

 <!-- FEEDBACK -->
 <h:panelGroup rendered="#{assessmentSettings.feedbackAuthoring ne '2'}">
 <f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{msg.feedback_optional}<br />" />
<f:verbatim><div class="tier2"></f:verbatim>
  <!-- WYSIWYG -->

  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
 <f:verbatim> </div></div></f:verbatim>
</h:panelGroup>
<!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="tier2"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
  <h:outputText value="#{msg.objective}" />
  <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" />

  <h:outputText value="#{msg.keyword}" />
  <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" />

  <h:outputText value="#{msg.rubric_colon}" />
  <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" />
  </h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
  </div>
</div>

<p class="act">

  <h:commandButton accesskey="#{msg.a_save}" rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton accesskey="#{msg.a_save}" rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>


  <h:commandButton accesskey="#{msg.a_cancel}" rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_cancel}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_cancel}" action="editPool" immediate="true"/>
</p>
</h:form>
<!-- end content -->
</div>

    </body>
  </html>
</f:view>

