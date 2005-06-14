<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: testSelectIndex.jsp,v 1.1 2004/12/05 09:40:35 zqingru.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>TEST SELECT INDEX </title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body>
  <!-- content... -->
<h:form>
  <h:commandLink immediate="true" action="select">
    <h:outputText value="Select Assessments" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
    <f:param name="publishedId" value="1" />
  </h:commandLink>
</h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
