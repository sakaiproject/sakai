
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: audioRecording.jsp,v 1.32 2005/05/24 16:54:49 janderse.umich.edu Exp $ -->
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
      <%-- later, we'll use the new sakai 2.0 stylesheet tags --%>
      <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
      <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
      <samigo:script path="/js/authoring.js"/>

      </head>
<body onload="countNum();<%= request.getAttribute("html.body.onload") %>">
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
    <h:outputLabel value="#{msg.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" >
<f:validateDoubleRange/>
</h:inputText>
    <h:message for="answerptr" styleClass="validate"/><br/>
  <h:outputText value="  #{msg.zero_survey}" />
  </div>

  <!-- 2 TEXT -->

<span id="num2" class="number"></span>
  <div class="longtext"> <h:outputLabel for="qtextarea" value="#{msg.q_text}" />

  <!-- WYSIWYG -->

  <h:panelGrid width="50%">
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}"  >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
  </div>

  <!-- 3 TIME allowed -->
  <span id="num3" class="number"></span>
   <div class="longtext"><h:outputLabel value="#{msg.time_allowed_seconds}" />
  <h:outputText value="#{msg.time_allowed_seconds_indic}" />
  <h:inputText id="timeallowed" value="#{itemauthor.currentItem.timeAllowed}" required="true">
<f:validateDoubleRange/>
</h:inputText>
 <h:message for="timeallowed" styleClass="validate"/><br/>
  </div>
  <!-- 4 attempts -->
   <span id="num4" class="number"></span>
  <div class="longtext">

  <h:outputLabel value="#{msg.number_of_attempts}" />
  <h:outputText value="#{msg.number_of_attempts_indic}" />
  <h:selectOneMenu id="noattempts" value="#{itemauthor.currentItem.numAttempts}" required="true">
  <f:selectItem itemLabel="#{msg.select}" itemValue=""/>
  <f:selectItem itemLabel="1" itemValue="1"/>
  <f:selectItem itemLabel="2" itemValue="2"/>
  <f:selectItem itemLabel="3" itemValue="3"/>
  <f:selectItem itemLabel="4" itemValue="4"/>
  <f:selectItem itemLabel="5" itemValue="5"/>
  <f:selectItem itemLabel="6" itemValue="6"/>

  </h:selectOneMenu>
</div><br/>
 <h:message for="noattempts" styleClass="validate"/><br/>

  <!-- 5 PART -->

  <h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
   <f:verbatim><span id="num5" class="number"></span></f:verbatim>
   <h:outputLabel value="#{msg.assign_to_p} " />
   <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
  </h:panelGrid>


  <!-- 6 POOL -->
  <h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim>  <span id="num6" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_question_p} " />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

</h:panelGrid>


 <!-- FEEDBACK -->

   <span id="num6" class="number"></span>
   <div class="longtext">
   <h:outputLabel value="#{msg.feedback_optional}<br />" />


  <h:panelGrid width="50%">
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}"  >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
  </h:panelGrid>
  </div>

 <!-- METADATA -->
<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim><span id="num7" class="number"></span></f:verbatim>
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
