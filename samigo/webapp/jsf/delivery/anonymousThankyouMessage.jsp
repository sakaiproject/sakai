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
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<h:form id="redirectLoginForm">
  <h:outputText value="#{msg.anonymous_thank_you}"/>
  <h3 style="insColor insBak"><h:outputText  value="#{msg.anonymous_thank_you_message}" /></h3>
  <h:commandButton value="#{msg.button_continue}" type="button"
     style="act" onclick="javascript:window.open('/portal/','_top')" />
</h:form>

      </body>
    </html>
  </f:view>

