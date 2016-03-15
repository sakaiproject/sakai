<%--

    Copyright 2011-2013 The Australian National University

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

--%>
<%@ page contentType="text/html" isELIgnored="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashMap" %>

<%@ page import="au.edu.anu.portal.portlets.rss.utils.Constants" %>

<portlet:defineObjects /> 

<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.request.locale}" scope="request" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="au.edu.anu.portal.portlets.rss.utils.messages" />

<link type="text/css" rel="stylesheet"  href="<%=request.getContextPath()%>/css/simple-rss-portlet.css" />

<div class="simple-rss-portlet">
				
	<c:if test="${not empty errorMessage}">
		<p class="alertMessage">${errorMessage}</p>
	</c:if>
		
	
	<form method="POST" action="<portlet:actionURL/>" id="<portlet:namespace/>_config">
	
		<c:if test="${portletTitleIsLocked || feedUrlIsLocked}">
			<p class="information"><fmt:message key="edit.preconfigured" /></p>
		</c:if>
		
		<c:if test="${!portletTitleIsLocked}">
			<p><fmt:message key="config.portlet.title" /></p>
			<input type="text" name="portletTitle" value="${portletTitle}" size="30"/>
		</c:if>
		
		<p><fmt:message key="config.portlet.maxitems" /></p>
		<input type="text" name="maxItems" value="${maxItems}" size="5"/>
		
		<c:if test="${!feedUrlIsLocked}">
			<p><fmt:message key="config.portlet.url" /></p>
			<input type="text" name="feedUrl" value="${feedUrl}" size="50" />
		</c:if>
		
		<p>
 			<input type="submit" value="<fmt:message key='config.button.submit' />">
 			<a href="${cancelUrl}"><fmt:message key='config.button.cancel' /></a>
		</p>
	</form>
	
</div>
