<html>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: timeout.jsp,v 1.5 2005/05/24 16:54:49 janderse.umich.edu Exp $ -->
  <f:view>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.DeliveryMessages"
     var="msg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.timeout}"/></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css" />
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
  <!-- content... -->
  <h3><h:outputText value="#{msg.timeout}"/></h3>
 <h:form id="removeTemplateForm">
  <div class="validation">
       <h:outputText value="#{msg.timeout_save}" escape="false"
         rendered="#{!delivery.settings.autoSubmit}" />
       <h:outputText value="#{msg.timeout_submit}" escape="false"
         rendered="#{delivery.settings.autoSubmit}" />
  </div>
 <p class="act">
       <h:commandButton value="#{msg.button_return}" type="submit"
         styleClass="active" action="select" >
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
       </h:commandButton>
 </p>
 </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
</html>
