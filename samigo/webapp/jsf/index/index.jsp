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
  <h:form id="indexPage"> 
      <h:commandLink type="submit" 
        value="Student entry point: admin" action="#{backingbean.chooseAgentAdmin}">
      </h:commandLink>
      <f:verbatim><br /><br /></f:verbatim>

      <h:commandLink type="submit" 
        value="Student entry point: rachel" action="#{backingbean.chooseAgentRachel}">
      </h:commandLink>
      <f:verbatim><br /><br /></f:verbatim>

      <h:commandLink type="submit" 
        value="Student entry point: marith" action="#{backingbean.chooseAgentMarith}">
      </h:commandLink>
      <f:verbatim><br /><br /></f:verbatim>

      <h:commandLink type="submit"
        value="Instructor entry point" action="#{backingbean.chooseAgentAdminInstructor}">
      </h:commandLink>

<%-- remove this
      <h:commandLink type="submit"
         value="Instructor entry point" action="author">
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
      </h:commandLink>
--%>
  </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>

