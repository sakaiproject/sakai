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
    <h:outputText value="{&quot;pleaseWait&quot;:&quot;#{deliveryMessages.please_wait}&quot;,&quot;timeWarning&quot;:&quot;#{deliveryMessages.timeWarning}&quot;,&quot;timeWarningClose&quot;:&quot;#{deliveryMessages.timeWarningClose}&quot;,&quot;savedMessage&quot;:&quot;#{deliveryMessages.savedMessage}&quot;,&quot;subMessage&quot;:&quot;#{deliveryMessages.subMessage}&quot;,&quot;hideMessage&quot;:&quot;#{deliveryMessages.hideMessage}&quot;,&quot;showMessage&quot;:&quot;#{deliveryMessages.showMessage}&quot;,&quot;srRemaining&quot;:&quot;#{deliveryMessages.srRemaining}&quot;,&quot;srTimerInfo&quot;:&quot;#{deliveryMessages.srTimerInfo}&quot;,&quot;minReqScale&quot;:&quot;#{delivery.minReqScale}&quot;,&quot;titleMessage&quot;:&quot;#{deliveryMessages.titleMessage}&quot;}" escape="false"/>
  </f:view>
</jsp:root>
