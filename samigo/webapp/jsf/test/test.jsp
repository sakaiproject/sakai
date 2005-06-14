<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: test.jsp,v 1.5 2004/10/20 19:37:48 esmiley.stanford.edu Exp $ -->
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
<!-- with disable attribs -->
  <samigo:pagerButtons  formId="myForm" dataTableId="myData" firstItem="31" lastItem="60"
    totalItems="3000" prevText="prev" nextText="next" numItems="30"
    prevDisabled="false" nextDisabled="true"/>
<!-- without disable attribs -->
  <samigo:pagerButtons  formId="myForm" dataTableId="myData" firstItem="31" lastItem="60"
		prevText="prev" nextText="next" numItems="30" totalItems="3000"/>
<!-- test auto disable attribs -->
  <samigo:pagerButtons  formId="myForm" dataTableId="myData" firstItem="2971" lastItem="3000"
		prevText="prev" nextText="next" numItems="30" totalItems="3000"/>
<form id="myForm" action="javascript:alert('It\'s alive!');">
   <input type="hidden" value="whee" />
</form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
