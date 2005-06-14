
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: shortAnswer.jsp,v 1.35 2005/05/24 16:54:49 janderse.umich.edu Exp $ -->
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
<body onload="countNum();<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- FORM -->



<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
<!-- QUESTION PROPERTIES -->
  <!-- 1 POINTS -->
  <div class="indnt2">
   <span id="num1" class="number"></span>
    <div class="shorttext">  <h:outputLabel for="answerptr" value="#{msg.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" >
<f:validateDoubleRange/>
</h:inputText>
  <h:outputText value="  #{msg.zero_survey}" />
<br/> <h:message for="answerptr" styleClass="validate"/>
  </div>

  <!-- 2 TEXT -->
  
  <span id="num2" class="number"></span>
   <div class="longtext"><h:outputLabel for="qtextarea" value="#{msg.q_text}" />
  <br/>
  <!-- WYSIWYG -->
 
  <h:panelGrid width="50%">
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}">
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>

</div>
  <!-- 3 PART -->
 
  <h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
  <f:verbatim><span id="num3" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <!-- use this in real  value="#{section.sectionNumberList}" -->
  </h:selectOneMenu>

  </h:panelGrid>

  <!-- 4 POOL -->

  <h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
  <f:verbatim><span id="num4" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_question_p}" />
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>
  
  </h:panelGrid>

 <!-- 5 ANSWER and ANSWERFEEDBACK -->
  
  <span id="num5" class="number"></span>
   <div class="longtext">
  <h:outputLabel value="#{msg.answer_provide_a_mo}" />
  <h:panelGrid columns="2">
  <h:outputLabel value="#{msg.model_short_answer}" />
  <h:outputLabel value="#{msg.feedback_optional}" />

 <!-- not for autograding, but using corrAnswer and corrFeedback to pass value to bean -->
<%--
  <h:inputTextarea id="corranswer" value="#{itemauthor.currentItem.corrAnswer}" cols="30" rows="3"/>
  <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
--%>
  <!-- WYSIWYG -->
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrAnswer}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

<%--
  <h:inputTextarea id="genfdbk" value="#{itemauthor.currentItem.generalFeedback}" cols="30" rows="3"/>
  <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
--%>
   <!-- WYSIWYG  -->
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.generalFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>


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
