<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core">
<jsp:directive.page pageEncoding="UTF-8" contentType="application/json; charset=UTF-8" trimDirectiveWhitespaces="true" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page trimDirectiveWhitespaces="true"/>
  <f:view>
    <h:outputText value="[#{delivery.timeLimit},#{delivery.currentTimeElapse},#{delivery.assessmentId},#{delivery.backupTimeLimit},#{delivery.backupTimeElapse}]"/>
  </f:view>
</jsp:root>
