<html>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: passwordAccessError.jsp,v 1.1 2005/06/10 04:20:27 daisyf.stanford.edu Exp $ -->
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
      <title><h:outputText value="#{msg.access_denied}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
  <!-- content... -->
  <h3><h:outputText value="#{msg.access_denied}"/></h3>
 <h:form id="removeTemplateForm">
   <h:panelGroup>
       <f:verbatim><div class="validation"></f:verbatim>
       <h:outputText value="#{msg.password_denied}" escape="false" />
       <f:verbatim></div></f:verbatim>
   </h:panelGroup>

   <f:verbatim><p class="act"></f:verbatim>
       <h:commandButton value="#{msg.button_return}" type="submit"
         style="act" action="select" rendered="#{!delivery.accessViaUrl}">
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
       </h:commandButton>
   <f:verbatim></p></f:verbatim>
  <h:commandButton value="#{msg.button_return}" type="button" rendered="#{delivery.accessViaUrl}"
     style="act" onclick="javascript:window.open('login.faces','_top')" />

 </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
</html>
