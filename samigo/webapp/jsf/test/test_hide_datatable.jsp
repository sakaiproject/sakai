<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: test_hide_datatable.jsp,v 1.1 2004/12/16 20:13:44 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>TEST</title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      </head>
      <body onload="hideUnhideAllDivs('none');">
<!-- content... -->
<h:outputText value=" Test of hide in datable tag " />
<h:form id="fred">
<h:dataTable value="#{testlinks.links}" var="link"
  first="0" rows="100">
  <h:column>
    <samigo:hideDivision id="myhide" title="test">
      <h:panelGrid columns="2">
        <h:outputText value="heading one"/>
        <h:outputText value="heading two"/>
        <h:outputText value="#{link.text}" />
        <h:outputText value="#{link.text}" />
      </h:panelGrid>
    </samigo:hideDivision>
  </h:column>
</h:dataTable>
<hr />

</h:form>

<!-- end content -->
      </body>
    </html>
  </f:view>
