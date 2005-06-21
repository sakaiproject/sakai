<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: removeQuestion.jsp,v 1.13 2005/05/24 16:54:50 janderse.umich.edu Exp $ -->
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
      <title><h:outputText value="#{msg.remove_q_conf}" /></title>
      <%-- later, we'll use the new sakai 2.0 stylesheet tags --%>
      <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
      <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
  <!-- content... -->

 <h:form>
  <h3><h:outputText  value="#{msg.remove_q_conf}" /></h3>
  <div class="validation indnt1">
      <h:outputText value="#{msg.sure_rem_q}" />
  </div>

<p class="act">
       <h:commandButton immediate="true" value="#{msg.button_remove}" type="submit" action="#{itemauthor.deleteItem}" styleClass="active">
       </h:commandButton>
       <h:commandButton value="#{msg.button_cancel}" type="submit"
         action="author" />
   </p>

 </h:form>
 <!-- end content -->

      </body>
    </html>
  </f:view>

