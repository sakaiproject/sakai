<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: autoSubmit.jsp,v 1.2 2004/11/30 19:22:04 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title><h:outputText value="#{msg.auto_submit}" /></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body>
<!-- content... -->
<h3><h:outputText value="#{msg.auto_submit}" /></h3>
<div class="tier1">
  <h3 style="insColor insBak">
   <h:outputText  value="#{msg.time_exp}" />
  </h3>
  <%-- Clicking OK returns user to the assessments page. --%>
  <h:form id="ok">
  <font color="red"><h:messages/></font>
   <h:commandButton value="#{msg.button_ok}" type="submit"
     style="act" action="select" />
  </h:form>
</div>
<!-- end content -->
      </body>
    </html>
  </f:view>

