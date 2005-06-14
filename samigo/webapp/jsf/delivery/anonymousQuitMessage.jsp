<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
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
      <title><h:outputText value="#{msg.invalid_assessment}"/></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<h3> <h:outputText value="#{msg.anonymous_quit_warning}"/></h3>
<h:form id="redirectLoginForm">
 <div class="validation">
<h:outputText  value="#{msg.anonymous_quit_warning_message}" />
 </div>
 
<p class="act">
  <h:commandButton value="#{msg.button_continue}" type="button"
     styleClass="active" onclick="javascript:history.go(-1);" />

  <h:commandButton value="#{msg.button_quit}" type="button"
     onclick="javascript:window.open('/portal/','_top')" />
</p>
</h:form>

      </body>
    </html>
  </f:view>

