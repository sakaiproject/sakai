<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: demo.jsp,v 1.4 2004/09/04 01:48:23 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>Tag Usage demonstration.</title>
			// this style tag is just to ornament the table, see demoStylesheet.jsp
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
  <h2>Tag Usage demonstration.</h2>

  <h:panelGrid columns="2" rowClasses="trEven,trOdd">

  <h:outputLink value="demoScript.faces"><h:outputText value="Script demo." /></h:outputLink>
  <h:outputLink value="demoStylesheet.faces"><h:outputText value="Stylesheet demo." /></h:outputLink>
  <h:outputLink value="demoColorPicker.faces"><h:outputText value="Color Picker demo." /></h:outputLink>
  <h:outputLink value="demoDatePicker.faces"><h:outputText value="DatePicker Demo" /></h:outputLink>
  <h:outputLink value="demoHideDivision.faces"><h:outputText value="Show/HideDivision Demo" /></h:outputLink>
  <h:outputLink value="#"><h:outputText value="Source Code" /></h:outputLink>

  </h:panelGrid>
  <!-- end content -->
      </body>
    </html>
  </f:view>
