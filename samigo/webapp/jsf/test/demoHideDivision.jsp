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
      <title>Demo Hide Division Tag</title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      <samigo:stylesheet path="/css/main.css"/>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      </head>
 <!--
TESTING!
     <body onload="javascript:hideUnhideAllDivs('none')">
-->
     <body onload="hideUnhideAllDivs('none');">
  <!-- $Id: demoHideDivision.jsp,v 1.7 2004/12/01 04:34:03 esmiley.stanford.edu Exp $ -->
  <!-- content... -->
<h:form id="oops">

  <h2>Click to hide/unhide</h2>
  <samigo:hideDivision title="Hide Me" id="div1">
  <h:panelGrid columns="3" rowClasses="trEven,trOdd" styleClass="indnt2">
    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

  </h:panelGrid>

  </samigo:hideDivision >
  <samigo:hideDivision title="Hide Me Too" id="div2">
  <h:panelGrid columns="3" rowClasses="trEven,trOdd">
    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

    <h:outputText value="Column 1."/>
    <h:outputText value="Column 2."/>
    <h:outputText value="Column 3."/>

  </h:panelGrid>

  </samigo:hideDivision >
</h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
