<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>
    <h:form id="beginAssessmentForm">
        <h:commandButton action="#{beginDeliveryActionBean.startAssessment}" 
                         value="#{deliveryMessages.begin_assessment_button}" />
    </h:form>
</f:view> 