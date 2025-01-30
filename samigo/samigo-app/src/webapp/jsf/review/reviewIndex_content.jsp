<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head><%= request.getAttribute("html.head") %>
<title><h:outputText value="#{selectIndexMessages.page_title}" /></title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">
    <!-- IF A SECURE DELIVERY MODULES ARE AVAILABLE, INJECT THEIR INITIAL HTML FRAGMENTS HERE -->
    <h:outputText  value="#{select.secureDeliveryHTMLFragments}" escape="false" />

    <!--JAVASCRIPT -->
    <div class="portletBody container-fluid">
        <h:form id="selectIndexForm">
            <!-- SUBMISSIONS -->
            <h2>
                <h:outputText value="#{selectIndexMessages.submissions_for_assessment} #{select.reviewAssessmentTitle}" />
            </h2>
            <t:div rendered="#{select.isThereAssessmentToReview eq 'true'}" styleClass="panel panel-default sam-submittedPanel">

                <%-- Include REVIEW TABLE --%>
                <%@ include file="../select/selectIndex_review_table.jsp"%>

            </t:div>
        </h:form>
    </div>
    <!-- end content -->
</body>
</html>
