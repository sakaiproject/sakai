<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: testImporting.jsp,v 1.1 2005/02/04 01:51:34 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>Test Importing</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
  <div class="heading"><h:outputText value="Quick testing hack."/></div>
  <h:form id="testImport">
    <h:commandButton value="<< IMPORT :O >>" type="submit" immediate="true" action="editAssessment" >
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.author.ImportAssessmentListener" />
    </h:commandButton>
  </h:form>


  <!-- end content -->
      </body>
    </html>
  </f:view>
