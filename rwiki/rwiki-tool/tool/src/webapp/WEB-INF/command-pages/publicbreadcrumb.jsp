<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************/
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	 xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:fn="http://java.sun.com/jsp/jstl/functions"
 	version="2.0">
    <jsp:directive.page language="java"
        contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
<!--
    This is a fragment, so it shouldnt have this in it.
    <jsp:text>
        <![CDATA[ <?xml version="1.0" encoding="UTF-8" ?> ]]>
    </jsp:text>
    <jsp:text>
        <![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
    </jsp:text>
-->
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
