<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.EvaluationMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.grading}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- $Id:  -->
<!-- content...  note: there may be no way to do this inside Sakai...-->
<frameset ROWS="100%,*">
   <frame frameName="mainFrame" href="totalScores" frameborder="0" noresize="true" />
   <frame frameName="dummyFrame"  forward="NAVIGATION" frameborder="0" />
</frameset>
<!-- end content... -->
      </html>
  </f:view>
