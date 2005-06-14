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
      <title>Demo Color Picker</title>
      <samigo:stylesheet path="/css/main.css"/>
      <!-- this javascript is required -->
      <samigo:script path="/jsf/widget/colorpicker/colorpicker.js"/>
      </head>
      <body>
<!-- $Id: demoColorPicker.jsp,v 1.8 2005/01/06 21:21:46 esmiley.stanford.edu Exp $ -->
  <!-- content... -->
  <h2>Demo Color Picker Form</h2>
  <h:form id="colorForm">
    <br />Pick color:
    <samigo:colorPicker size="10" value="aaffdd" id="pickColor"/>
    <br />
    <h:commandButton type="submit" id="sub" value="Submit"
      />
  </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
