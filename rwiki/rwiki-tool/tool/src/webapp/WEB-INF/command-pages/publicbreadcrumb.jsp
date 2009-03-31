<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	 xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:fn="http://java.sun.com/jsp/jstl/functions"
 	version="2.0">
    <jsp:directive.page language="java"
        contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
    <c:set var="recentlyVisitedBean" value="${requestScope.rsacMap.recentlyVisitedBean}"/>
<p class="breadcrumb" >
	<c:set var="links" value="${recentlyVisitedBean.publicBreadcrumbLinks}"/>

	<c:choose>
		<c:when test="${fn:length(links) eq 0 }">
		</c:when>
		<c:when test="${fn:length(links) eq 1 }">
			<c:out value="${links[0]}" escapeXml="false"/>
		</c:when>
		<c:otherwise>
			<c:if test="${fn:length(links) - 9 gt 0 }">
				 ... &gt; 
			</c:if>
			<c:out value="${links[0]}" escapeXml="false"/>
			<c:forEach var="link" begin="${fn:length(links) - 8 gt 1 ? fn:length(links) - 8 : 1}" items="${links}">
				&gt; <c:out value="${link}" escapeXml="false"/>
			</c:forEach>	
		</c:otherwise>
	</c:choose>
</p>
</jsp:root>
