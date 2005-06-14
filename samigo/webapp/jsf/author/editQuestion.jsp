<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: editQuestion.jsp,v 1.4 2005/05/24 16:54:50 janderse.umich.edu Exp $ -->
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
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- FORM -->
<h:form id="itemForm">

<h:inputHidden id="assessmentID" value="#{item.assessmentID}"/>
<h:inputHidden id="assessTitle" value="#{item.assessTitle}" />
<h:inputHidden id="ItemIdent" value="#{item.ItemIdent}"/>
<h:inputHidden id="ItemIdent" value="#{item.itemNo}"/>
<h:inputHidden id="currentSection" value="#{item.currentSection}"/>
<h:inputHidden id="insertPosition" value="#{item.insertPosition}"/>


<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
  <!-- QUESTION PROPERTIES -->

<!-- TODO:
This will have all the other files in ./item included here.
We need to find a strategy for doing this.

something like if type true false render include true false etc.
-->
<h:panelGrid columns="2" cellpadding="3" cellspacing="3">
  <h:commandButton type="submit" value="#{msg.button_save}" action="editAssessment">
    <f:actionListener
      type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorQuestionListener" />
  </h:commandButton>
  <h:commandButton type="submit" value="#{msg.button_cancel}" action="editAssessment"/>
</h:panelGrid>

</h:form>
<!-- end content -->
    </body>
  </html>
</f:view>