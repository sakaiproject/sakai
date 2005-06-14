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
      <title>Test Bed Index</title>
      <samigo:stylesheet path="/css/main.css"/>
      <samigo:script path="/widget/datepicker/datepicker.js"/>
      <samigo:script path="/widget/colorpicker/colorpicker.js"/>

      </head>
    <body>
  <!-- content... -->
  <h2>Test Bed Index</h2>
  <h:form id="navigationForm">
    <h:commandButton type="submit" id="templateIndex" value="Template Index"
      action="templateIndex"/>
    <br />Pick color:
    <samigo:colorPicker value="" size="10" id="pickColor"/>
    <br />Pick date:
    <samigo:datePicker value="" size="10" id="pickDate"/>
  </h:form>
  <!-- end content -->

      </body>
    </html>
  </f:view>
