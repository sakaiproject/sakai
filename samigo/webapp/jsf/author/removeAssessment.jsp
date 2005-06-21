<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: removeAssessment.jsp,v 1.10 2005/05/24 16:54:50 janderse.umich.edu Exp $ -->
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
      <title><h:outputText value="#{msg.remove_assessment_co}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->
 <h:form id="removeAssessmentForm">
   <h:outputText value="#{assessment.assessmentId}"/>
   <h:inputHidden id="assessmentId" value="#{assessmentBean.assessmentId}"/>
   <h3><h:outputText  value="#{msg.remove_assessment_co}" /></h3>
   <div class="validation indnt1">
          <h:outputText value="#{msg.cert_rem_assmt} \"#{assessmentBean.title}\" ?" />
  </div>
<p class="act">
       <h:commandButton value="#{msg.button_remove}" type="submit"
         styleClass="active" action="removeAssessment" >
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.author.RemoveAssessmentListener" />
       </h:commandButton>
       <h:commandButton value="#{msg.button_cancel}" type="submit"
         action="author" />
    </p>
 </h:form>
 <!-- end content -->
      </body>
    </html>
  </f:view>
