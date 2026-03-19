<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<!DOCTYPE html>
<f:view>
    <html>
        <head><%= request.getAttribute("html.head") %>
            <title><h:outputText value="#{generalMessages.permissions}" /></title>
        </head>
        <body onload="<%= request.getAttribute("html.body.onload") %>">
          <div class="portletBody container-fluid">
            <div class="samigo-container">
                <h:form id="permissionsForm">
                    <%@ include file="/jsf/author/permissionsHeadings.jsp" %>
                </h:form>
                <div class="d-flex align-items-center mb-3">
                    <h1 class="fs-3 mb-0">
                        <h:outputText value="#{generalMessages.permissions}" />
                    </h1>
                </div>
                <h:panelGroup rendered="#{template.showAssessmentTypes}">
                    <sakai-permissions id="samigo-permissions" tool="assessment"></sakai-permissions>
                </h:panelGroup>
                <h:panelGroup rendered="#{!template.showAssessmentTypes}">
                    <sakai-permissions id="samigo-permissions" tool="assessment"
                        exclude-permissions="assessment.template.create,assessment.template.edit.own,assessment.template.delete.own"></sakai-permissions>
                </h:panelGroup>
            </div>
          </div>
        </body>
    </html>
</f:view>
