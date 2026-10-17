<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <f:view>
    <f:verbatim>
      <jsp:text><![CDATA[<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    ]]></jsp:text>
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta http-equiv="Refresh" content="0; URL=/portal"/>
      </head>
      <jsp:element name="body">
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
