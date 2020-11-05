<%@ page contentType="application/json; charset=UTF-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:view>
<h:outputText value="[#{delivery.timeLimit},#{delivery.currentTimeElapse},#{delivery.assessmentId}]"/>
</f:view>
