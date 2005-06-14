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
      <title>Demo Date Picker</title>
      <samigo:stylesheet path="/css/main.css"/>
      <!-- this javascript is required -->
      <samigo:script path="/jsf/widget/datepicker/datepicker.js"/>
      </head>
      <body>
  <!-- $Id: demoDatePicker.jsp,v 1.9 2005/01/07 17:26:25 esmiley.stanford.edu Exp $ -->
  <!-- content... -->
  <h2>Demo Date Picker Form</h2>
  <h:form id="dateForm">
    <br />Pick date:
    <samigo:datePicker value="" size="25" id="pickDate"/>
    <br />
    <h:commandButton type="submit" id="templateIndex" value="Submit"
      action="templateIndex"/>
  </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
