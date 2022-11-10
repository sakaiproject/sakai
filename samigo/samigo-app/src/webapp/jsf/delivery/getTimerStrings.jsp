<%@ page pageEncoding="utf-8" contentType="application/json; charset=UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:view>
<h:outputText value="{&quot;pleaseWait&quot;:&quot;#{deliveryMessages.please_wait}&quot;,&quot;timeWarning&quot;:&quot;#{deliveryMessages.timeWarning}&quot;,&quot;timeWarningClose&quot;:&quot;#{deliveryMessages.timeWarningClose}&quot;,&quot;savedMessage&quot;:&quot;#{deliveryMessages.savedMessage}&quot;,&quot;subMessage&quot;:&quot;#{deliveryMessages.subMessage}&quot;,&quot;hideMessage&quot;:&quot;#{deliveryMessages.hideMessage}&quot;,&quot;showMessage&quot;:&quot;#{deliveryMessages.showMessage}&quot;,&quot;srRemaining&quot;:&quot;#{deliveryMessages.srRemaining}&quot;,&quot;srTimerInfo&quot;:&quot;#{deliveryMessages.srTimerInfo}&quot;,&quot;minReqScale&quot;:&quot;#{delivery.minReqScale}&quot;}" escape="false"/>
</f:view>
