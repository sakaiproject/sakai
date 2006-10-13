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
  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
   xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/><jsp:text
	><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="historyBean" value="${requestScope.rsacMap.historyBean}"/>
  <c:set var="renderBean" value="${requestScope.rsacMap.reviewRenderBean}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.reviewRightRenderBean}"/>
  <c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
  <c:set var="interestedRevision" value="${requestScope.rsacMap.reviewHelperBean.interestedRevision}"/>
  <c:set var="reviewRenderBean" value="${requestScope.rsacMap.reviewRenderBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title>Review: <c:out value="${historyBean.localName}"/> Version: <c:out value="${interestedRevision}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
      	<div class="portletBody">
      		<div class="navIntraTool">
	  <form action="?#" method="get" class="rwiki_searchForm">
	  	<rwiki:commandlinks 
							useHomeLink="true"
							useViewLink="true"
							useEditLink="false"
							useInfoLink="false"
							useHistoryLink="true"
							useWatchLink="false"
							withNotification="false"
							viewLinkName="View Current"
							homeBean="${homeBean}"
							viewBean="${historyBean}"
						        />
	    <span class="rwiki_searchBox">
	    Search:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />
	    <input type="hidden" name="panel" value="Main" />
	    <input type="text" name="search" />
	    </span>
	  </form>
	</div>
		<c:set var="rwikiContentStyle"  value="rwiki_content" />
		
	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <!-- Creates the right hand sidebar -->
	  <jsp:directive.include file="sidebar.jsp"/>
	  <!-- Main page -->
		<div id="${rwikiContentStyle}" >
			<h3>
	      <c:out value="${historyBean.localName}"/> Version: <c:out value="${interestedRevision}"/>
	    </h3>

	    <p class="alert">
	      You are viewing version <c:out value="${interestedRevision}"/>
	      of this page, updated <fmt:formatDate type="both" value="${reviewRenderBean.rwikiObject.version}"/>. The current 
	      version is 
	      <jsp:element name="a">
		<jsp:attribute name="href"><c:out value="${historyBean.viewUrl}"/></jsp:attribute>
		version <c:out value="${currentRWikiObject.revision}"/> 
	      </jsp:element>, updated 
	      <fmt:formatDate type="both" value="${currentRWikiObject.version}"/>
	    </p>
	    <div class="rwikiRenderBody">
	      <div class="rwikiRenderedContent">
		<c:out value="${reviewRenderBean.renderedPage}" escapeXml="false"/>
	      </div>
	    </div>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
