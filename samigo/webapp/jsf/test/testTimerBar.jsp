<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: testTimerBar.jsp,v 1.2 2004/12/15 20:55:21 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>Test</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
  <h:outputText value="Timer bar test."/>
  <samigo:timerBar height="15" width="300" wait="60" elapsed="30"
    expireMessage="this is the expire message"
    expireScript="alert('this is the expire script');" />

  <!-- end content -->
      </body>
    </html>
  </f:view>
