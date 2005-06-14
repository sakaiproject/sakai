<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: stub.jsp,v 1.2 2004/11/24 23:34:48 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>Stub</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
  <div class="heading"><h:outputText value="This is a stub for a forward."/></div>
<samigo:dataLine value="#{mytest.links}" var="link">
  <h:column>
   <h:commandLink action="#{link.action}">
     <h:outputText value="#{link.text}" />
   </h:commandLink>
   <h:outputText value=" | " />
  </h:column>
</samigo:dataLine>

  <!-- end content -->
      </body>
    </html>
  </f:view>
