<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!-- $Id: previewQuestion.jsp,v 1.5 2005/05/24 16:54:50 janderse.umich.edu Exp $ -->

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
      <title><h:outputText value="#{msg.create_modify_a}" /></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
<body onload="document.forms[0].reset();;<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- some back end stuff stubbed -->
<h:form id="assesssmentForm">

<h:messages/>
 <h:panelGrid columns="2" border="1" width="100%">
    <h:panelGroup>
       <h:outputText styleClass="tier1" value="#{itemContents.itemData.type.keyword}" />
       <h:outputText styleClass="tier1" value="#{itemContents.itemData.score}" />
       <h:outputText styleClass="tier1" value="#{msg.points_lower_case}" />
    </h:panelGroup>

 </h:panelGrid>
  
        <h:panelGrid>
          <h:panelGroup rendered="#{itemContents.itemData.typeId == 9}">
            <%@ include file="/jsf/author/questionpreview/Matching.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 8}">
            <%@ include file="/jsf/author/questionpreview/FillInTheBlank.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 7}">
            <%@ include file="/jsf/author/questionpreview/AudioRecording.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 6}">
            <%@ include file="/jsf/author/questionpreview/FileUpload.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 5}">
            <%@ include file="/jsf/author/questionpreview/ShortAnswer.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 4}">
            <%@ include file="/jsf/author/questionpreview/TrueFalse.jsp" %>
          </h:panelGroup>

          <!-- same as multiple choice single -->
          <h:panelGroup rendered="#{itemContents.itemData.typeId == 1}">
            <%@ include file="/jsf/author/questionpreview/MultipleChoiceSingleCorrect.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 2}">
            <%@ include file="/jsf/author/questionpreview/MultipleChoiceMultipleCorrect.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 3}">
            <%@ include file="/jsf/author/questionpreview/MultipleChoiceSurvey.jsp" %>
          </h:panelGroup>
   
        </h:panelGrid>
<p class="act">
  <h:commandButton value="#{msg.button_back}"  action="editPool" type="submit" styleClass="active"/>
 
</p>

</h:form>
<!-- end content -->
      </body>
    </html>
  </f:view>

