<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: testnav.jsp,v 1.1 2004/10/21 00:07:41 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>TEST</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
<!-- with disable attribs -->
  <samigo:navigationMap map="#{subbean.map}" separator="|" style="myStyle" linkStyle="myLinkStyle"/>
  <!-- end content -->
      </body>
    </html>
  </f:view>
