<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
       var="msg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="Remove Question"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <h:form id="removePoolForm">
   <h:panelGrid cellpadding="5" cellspacing="3">
     <h:panelGroup>
       <f:verbatim><h3 style="insColor insBak"></f:verbatim>
       <h:outputText  value="#{msg.rm_q_confirm}" />
       <f:verbatim></h3></f:verbatim>
     </h:panelGroup>
     <h:outputText styleClass="validation" value="#{msg.remove_sure_q}" />

<h:dataTable value="#{questionpool.itemsToDelete}" var="question">
      <h:column>
       <h:outputText  value="#{question.text}" />
      </h:column>
</h:dataTable>

     <h:panelGrid columns="2" cellpadding="3" cellspacing="3">
  <h:commandButton type="submit" immediate="true" id="Submit" value="#{msg.remove}"
    action="#{questionpool.removeQuestionsFromPool}" >
  </h:commandButton>

<h:commandButton style="act" value="#{msg.cancel}" action="poolList"/>

     </h:panelGrid>
   </h:panelGrid>

 </h:form>
 <!-- end content -->




</body>
</html>
</f:view>
