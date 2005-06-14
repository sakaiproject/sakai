<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.TemplateMessages"
     var="msg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Wysiwyg Test</title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
     </head>
<body>
<h:form id="wysiwygEditorForm">
  <h:panelGrid columns="2" border="1">
  <samigo:wysiwyg rows="140" value="This is a JSF test." />
  <samigo:wysiwyg rows="140" value="This is a JSF test." />
  <samigo:wysiwyg rows="140" value="This is a JSF test." />
  <samigo:wysiwyg rows="140" value="This is a JSF test." />
  <samigo:wysiwyg rows="140" value="This is a JSF test." />
  <samigo:wysiwyg rows="140" value="This is a JSF test." />
  </h:panelGrid>
</h:form>
<!-- end content -->


      </body>
    </html>
  </f:view>
