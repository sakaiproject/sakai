<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:loadBundle
      basename="org.sakaiproject.tool.assessment.bundle.MainIndexMessages"
      var="msg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.tool_title}"/></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

  <!-- content... -->
  <h:form id="navigationForm">
  <h:panelGrid columnClasses="h1text,tdDisplay" width="90%"
      cellpadding="3" cellspacing="3" columns="1">
    <h:panelGroup>
       <f:verbatim><h3 style="insColor insBak"></f:verbatim>
       <h:outputText  value="#{msg.tool_welcome}, #{defaultlogin.username}" />
       <f:verbatim></h3></f:verbatim>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton type="submit" id="navigationSubmitSelect"
        value="#{msg.button_select}" action="select">
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
      </h:commandButton>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton type="submit" id="navigationSubmitView"
        value="#{msg.button_view}" action="view">
        <f:actionListener
          type="test.org.sakaiproject.tool.assessment.ui.listener.TestActionListener" />
      </h:commandButton>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton type="submit" id="navigationSubmitAuthor"
         value="#{msg.button_author}" action="author">
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
      </h:commandButton>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton type="submit" id="navigationSubmitTemplate"
        value="#{msg.button_template}" action="template">
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
      </h:commandButton>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton type="submit" id="navigationSubmitPDF"
        value="#{msg.button_pdf}" action="pdf">
        <f:actionListener
          type="test.org.sakaiproject.tool.assessment.ui.listener.TestActionListener" />
      </h:commandButton>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton type="submit" id="navigationSubmitDebug"
        value="#{msg.button_debug}" action="debug">
        <f:actionListener
          type="test.org.sakaiproject.tool.assessment.ui.listener.TestActionListener" />
      </h:commandButton>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton type="submit" id="navigationSubmitExit"
        value="#{msg.button_exit}" action="exit"/>
    </h:panelGroup>
  </h:panelGrid>
  </h:form>
  <h:panelGrid columnClasses="tdDisplay" width="50%" border="1"
        columns="3">
      <h:panelGroup>
        <h:outputText value="#{msg.version}"/>
        <h:outputText value=" #{buildinfo.buildVersion}"/>
      </h:panelGroup>
      <h:panelGroup>
        <h:outputText value="#{msg.built}"/>
        <h:outputText value=" #{buildinfo.buildTime}"/>
      </h:panelGroup>
      <h:panelGroup>
        <h:outputText value="#{msg.tag}"/>
        <h:outputText value=" #{buildinfo.buildTag}"/>
      </h:panelGroup>
  </h:panelGrid>
  <!-- end content -->
      </body>
    </html>
  </f:view>

