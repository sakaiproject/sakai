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
                <div class="page-header">
                    <h1>
                        <h:outputText value="#{generalMessages.permissions}" />
                    </h1>
                </div>
                <sakai-permissions id="samigo-permissions" tool="assessment" bundle-key="org.sakaiproject.tool.assessment.bundle.GeneralMessages"></sakai-permissions>
            </div>
          </div>
        </body>
    </html>
</f:view>
