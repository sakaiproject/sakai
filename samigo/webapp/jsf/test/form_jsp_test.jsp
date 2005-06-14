<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>Simple JSF Test Using JSP Page and Taglib</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
  <div class="heading"><h:outputText value="Testing..."/></div>
  <h:outputText value="This is a test"/>
  <h:form id="testNavForm">
      <h:commandButton type="submit" id="testNavSubmit" value="Test Nav Button"
        action="bogus"/>
  </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
