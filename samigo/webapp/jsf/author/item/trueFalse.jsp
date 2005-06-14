
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: trueFalse.jsp,v 1.40 2005/06/02 22:58:48 lydial.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      <samigo:script path="/js/authoring.js"/>
      </head>
<body onload="countNum();;<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- FORM -->



<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
 <div class="indnt2">
<!-- QUESTION PROPERTIES -->
  <!-- 1 POINTS -->
 
   <span id="num1" class="number"></span>
<div class="shorttext">
    <h:outputLabel value="#{msg.answer_point_value}"/>
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true">
<f:validateDoubleRange />
</h:inputText>
 <h:message for="answerptr" styleClass="validate"/>
  <h:outputText value="  #{msg.zero_survey}" />
  </div>

  <!-- 2 TEXT -->
     <span id="num2" class="number"></span>
 <div class="longtext">
  <h:outputLabel value="#{msg.q_text}" />
  <!-- STUB FOR WYSIWYG -->
 
  <!-- WYSIWYG -->
    <%--
  <h:panelGrid columns="2">
  <h:inputTextarea id="qtextarea" value="#{itemauthor.currentItem.itemText}" cols="48" rows="8"/>
  <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
  </h:panelGrid>
    --%>
  <h:panelGrid width="50%">
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}">
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>

  <!-- 3 ANSWER -->
 <span id="num3" class="number"></span>
  <div class="longtext">
  <h:outputLabel value="#{msg.answer} " /></div>
   <div class="indnt2">

  <h:selectOneRadio layout="lineDirection" id="TF" border="0"
     value="#{itemauthor.currentItem.corrAnswer}" required="true">
     <f:selectItems value="#{itemauthor.trueFalseAnswerSelectList}" />
  </h:selectOneRadio>
<h:message for="TF" styleClass="validate"/>
</div>

    <!-- 4 RATIONALE -->
  
     <span id="num4" class="number"></span>
    <div class="longtext">
    <h:outputLabel value="#{msg.req_rationale}" />
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" id="rational" required="true">
     <f:selectItem itemValue="true"
       itemLabel="#{msg.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{msg.no}" />
    </h:selectOneRadio>
<br/> <h:message for="rational" styleClass="validate"/><br/>
  </div>

  <!-- 5 PART -->
 
  <h:panelGrid rendered="#{itemauthor.target == 'assessment'}" columnClasses="shorttext">  <h:panelGroup>
   <f:verbatim><span id="num5" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <!-- use this in real  value="#{section.sectionNumberList}" -->
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>


  <!-- 6 POOL -->
 
  <h:panelGrid rendered="#{itemauthor.target == 'assessment'}" columnClasses="shorttext">
  <h:panelGroup>
   <f:verbatim><span id="num6" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>
 
 <!-- FEEDBACK -->

  <span id="num7" class="number"></span><div class="longtext">
  <h:outputLabel value="#{msg.correct_incorrect_an}" />

  <h:panelGrid columns="2">
  <h:outputLabel value="#{msg.correct_answer_opti}" />
  <h:outputLabel  value="#{msg.incorrect_answer_op}" />

  <%--<h:panelGroup>--%>

  <!-- WYSIWYG -->
   
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

 
  <!-- WYSIWYG -->
   
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>

 <!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim><span id="num9" class="number"></span></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="indnt3"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel value="#{msg.objective}" />
  <h:inputText id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputLabel value="#{msg.keyword}" />
  <h:inputText id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputLabel value="#{msg.rubric_colon}" />
  <h:inputText id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</div>


<%--
<div class="longtext indnt1">
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
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_save}" action="editAssessment" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_save}" action="editPool" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>


  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_cancel}" action="editAssessment" immediate="true"/>
 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_cancel}" action="editPool" immediate="true"/>

</p>
</h:form>


<!-- end content -->
    </body>
  </html>
</f:view>
