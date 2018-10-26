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

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  ><jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
  /><jsp:text
  ><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="viewBean" value="${requestScope.rsacMap.viewBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.viewRightRenderBean}"/>
  <c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <c:set var="recentlyVisitedBean" value="${requestScope.rsacMap.recentlyVisitedBean }"/>
  
  <c:if test="${requestScope.rsacMap.withBreadcrumbs}">
  		<c:set target="${recentlyVisitedBean}" property="viewPage" value="${viewBean }"/>
  </c:if>
  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
    <head>
      <title><c:out value="${rlb.jsp_view}"/>: <c:out value="${renderBean.localisedPageName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>

    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
	<div class="portletBody">
	  <c:if test="${requestScope.rsacMap.withBreadcrumbs}">
	  	<jsp:directive.include file="publicbreadcrumb.jsp"/>
	  </c:if>
	  	<c:choose>
	<c:when test="${rightRenderBean.hasContent}" >
		<c:set var="rwikiContentStyle"  value="withsidebar" />	
	</c:when>
	<c:otherwise>
		<c:set var="rwikiContentStyle"  value="nosidebar" />    
	</c:otherwise>
	</c:choose>
	  <div id="rwiki_head" >				    
		<jsp:directive.include file="sidebar-switcher.jsp"/>		     
	  </div>
	  
	  <!-- Creates the right hand sidebar -->
	  <!-- Main page -->
	  <div id="rwiki_content" class="${rwikiContentStyle}">
	    <div class="rwikiRenderBody">
	      <div class="rwikiRenderedContent"> 
		<c:out value="${renderBean.publicRenderedPage}" escapeXml="false"/><br/>	    
	      </div>
	    </div>
	  </div>
	 <!-- Creates the right hand sidebar -->
	 <jsp:directive.include file="sidebar.jsp"/>
	  
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
