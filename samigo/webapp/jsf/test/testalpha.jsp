<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: testalpha.jsp,v 1.1 2004/09/21 01:35:49 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>test</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
  <p><samigo:alphaIndex initials="ABC"/></p>
  <p><samigo:alphaIndex /></p>
  <p><samigo:alphaIndex initials="#{bean.prop1}"/></p>
  <!-- end content -->
      </body>
    </html>
  </f:view>
