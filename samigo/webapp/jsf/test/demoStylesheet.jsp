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
      <title>Stub</title
			<!-- this tag calculates relative to the application context -->
      <samigo:stylesheet path="/jsf/test/demoCSS.css"/>
      </head>
      <body>
  <!-- $Id: demoStylesheet.jsp,v 1.4 2004/09/07 20:49:08 esmiley.stanford.edu Exp $ -->
  <!-- content... -->
  <h1>style demonstration: h1</h1>
  <h2>style demonstration: h2</h2>

	<h:outputText value="trEven and trOdd styles: from external stylesheet." />
	<h:panelGrid columns="2" rowClasses="trEven,trOdd">
	  <h:outputText value="trEven style from external stylesheet." />
	  <h:outputText value="trEven style from external stylesheet." />

	  <h:outputText value="trOdd style from external stylesheet." />
	  <h:outputText value="trOdd style from external stylesheet." />

	  <h:outputText value="trEven style from external stylesheet." />
	  <h:outputText value="trEven style from external stylesheet." />

	  <h:outputText value="trOdd style from external stylesheet." />
	  <h:outputText value="trOdd style from external stylesheet." />

	  <h:outputText value="trEven style from external stylesheet." />
	  <h:outputText value="trEven style from external stylesheet." />

	  <h:outputText value="trOdd style from external stylesheet." />
	  <h:outputText value="trOdd style from external stylesheet." />

	  <h:outputText value="trEven style from external stylesheet." />
	  <h:outputText value="trEven style from external stylesheet." />

	  <h:outputText value="trOdd style from external stylesheet." />
	  <h:outputText value="trOdd style from external stylesheet." />

	  <h:outputText value="trEven style from external stylesheet." />
	  <h:outputText value="trEven style from external stylesheet." />

	  <h:outputText value="trOdd style from external stylesheet." />
	  <h:outputText value="trOdd style from external stylesheet." />

	  <h:outputText value="trEven style from external stylesheet." />
	  <h:outputText value="trEven style from external stylesheet." />

	  <h:outputText value="trOdd style from external stylesheet." />
	  <h:outputText value="trOdd style from external stylesheet." />

  </h:panelGrid>

  <!-- end content -->
      </body>
    </html>
  </f:view>
