<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: fake_select.jsp,v 1.3 2004/11/24 23:34:48 esmiley.stanford.edu Exp $ -->
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
<h:form>
  <h:commandLink id="taketitle" action="beginAssessment">
    <f:actionListener
       type="test.org.sakaiproject.tool.assessment.ui.listener.FakeBeginDeliveryActionListener" />
    <h:outputText value="Push me!" />
  </h:commandLink>
</h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
