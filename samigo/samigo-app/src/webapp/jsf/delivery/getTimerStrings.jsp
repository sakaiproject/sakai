<%@ page pageEncoding="utf-8" contentType="application/json; charset=UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:view>
<h:outputText value="{&quot;savedMessage&quot;:&quot;#{deliveryMessages.savedMessage}&quot;,&quot;subMessage&quot;:&quot;#{deliveryMessages.subMessage}&quot;,&quot;alertMessage&quot;:&quot;#{deliveryMessages.alertMessage}&quot;,&quot;closeAlertMessage&quot;:&quot;#{deliveryMessages.closeAlertMessage}&quot;,&quot;alertTitle&quot;:&quot;#{deliveryMessages.alertTitle}&quot;,&quot;hideMessage&quot;:&quot;#{deliveryMessages.hideMessage}&quot;,&quot;showMessage&quot;:&quot;#{deliveryMessages.showMessage}&quot;,&quot;timeRemaining&quot;:&quot;#{deliveryMessages.timeRemaining}&quot;,&quot;timeLimitHour&quot;:&quot;#{deliveryMessages.time_limit_hour}&quot;,&quot;timeLimitMinute&quot;:&quot;#{deliveryMessages.time_limit_minute}&quot;,&quot;timeLimitSecond&quot;:&quot;#{deliveryMessages.time_limit_second}&quot;,&quot;minReqScale&quot;:&quot;#{delivery.minReqScale}&quot;}" escape="false"/>
</f:view>
