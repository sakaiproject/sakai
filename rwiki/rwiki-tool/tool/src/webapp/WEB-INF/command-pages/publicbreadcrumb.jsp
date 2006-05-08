<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->
<jspii:root xmlns:jspii="http://java.sun.com/JSP/Page"
	 xmlns:cii="http://java.sun.com/jsp/jstl/core"
	xmlns:fnii="http://java.sun.com/jsp/jstl/functions"
 	version="2.0">
    <jspii:directive.page language="java"
        contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
<!--
    This is a fragment, so it shouldnt have this in it.
    <jspii:text>
        <![CDATA[ <?xml version="1.0" encoding="UTF-8" ?> ]]>
    </jspii:text>
    <jspii:text>
        <![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
    </jspii:text>
-->
    <cii:set var="recentlyVisitedBean" value="${requestScope.rsacMap.recentlyVisitedBean}"/>
<p class="breadcrumb" >
	<cii:set var="links" value="${recentlyVisitedBean.publicBreadcrumbLinks}"/>

	<cii:choose>
		<cii:when test="${fnii:length(links) eq 0 }">
		</cii:when>
		<cii:when test="${fnii:length(links) eq 1 }">
			<cii:out value="${links[0]}" escapeXml="false"/>
		</cii:when>
		<cii:otherwise>
			<cii:if test="${fnii:length(links) - 9 gt 0 }">
				 ... &gt; 
			</cii:if>
			<cii:out value="${links[0]}" escapeXml="false"/>
			<cii:forEach var="link" begin="${fnii:length(links) - 8 gt 1 ? fnii:length(links) - 8 : 1}" items="${links}">
				&gt; <cii:out value="${link}" escapeXml="false"/>
			</cii:forEach>	
		</cii:otherwise>
	</cii:choose>
</p>
</jspii:root>
