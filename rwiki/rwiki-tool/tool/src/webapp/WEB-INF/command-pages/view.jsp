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
  <jsp:scriptlet>
	  		long start = System.currentTimeMillis();
  </jsp:scriptlet>
  <c:set var="viewBean" value="${requestScope.rsacMap.viewBean}"/>
  <jsp:scriptlet>
	  		long finish = System.currentTimeMillis();
	  	uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("get ViewBean:",start,finish);
			start = System.currentTimeMillis();
  </jsp:scriptlet>
  
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <jsp:scriptlet>
	  		finish = System.currentTimeMillis();
	  	uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("get RenderBean:",start,finish);
			start = System.currentTimeMillis();
  </jsp:scriptlet>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.viewRightRenderBean}"/>
    <jsp:scriptlet>
	  		finish = System.currentTimeMillis();
	  	uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("get RightRenderBean:",start,finish);
			start = System.currentTimeMillis();
  </jsp:scriptlet>
  
  <c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
      <jsp:scriptlet>
	  		finish = System.currentTimeMillis();
	  	uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("get permissionsBean:",start,finish);
			start = System.currentTimeMillis();
  </jsp:scriptlet>
  
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
      <jsp:scriptlet>
	  		finish = System.currentTimeMillis();
	  	uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("get homeBean:",start,finish);
			start = System.currentTimeMillis();
  </jsp:scriptlet>
  <c:set var="recentlyVisitedBean" value="${requestScope.rsacMap.recentlyVisitedBean }"/>
      <jsp:scriptlet>
	  		finish = System.currentTimeMillis();
	  	uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("get recentlyVisitedBean:",start,finish);
			start = System.currentTimeMillis();
  </jsp:scriptlet>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  
  <c:set target="${recentlyVisitedBean}" property="viewPage" value="${viewBean}"/>  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>View: <c:out value="${renderBean.localisedPageName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
	<link rel="alternate" title="Sakai Wiki RSS" 
 			href="${viewBean.rssAccessUrl}" type="application/rss+xml" />
    <c:if test="${requestScope.rsacMap.removeAutoSave}">
	  <script>
		autoSaveClear('<c:out value="${requestScope.rsacMap.currentPageName}" />');
	  </script>
	</c:if>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload">setMainFrameHeightNoScroll('<jsp:expression>request.getAttribute("sakai.tool.placement.id")</jsp:expression>');setFocus(focus_path);parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
	<div class="portletBody">
	<div class="navIntraTool">
	  <form action="?#" method="get" class="rwiki_searchForm">
	  	<rwiki:commandlinks 
							useHomeLink="true"
							usePrinterLink="true"
							useViewLink="true"
							useEditLink="true"
							useInfoLink="true"
							useHistoryLink="true"
							useWatchLink="true"
							withNotification="${requestScope.rsacMap.withnotification}"
							viewLinkName="View"
							homeBean="${homeBean}"
							viewBean="${viewBean}"
						        />
	    <span class="rwiki_searchBox">
	      Search:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />
	      <input type="hidden" name="panel" value="Main" />
	      <input type="text" name="search" />
	    </span>
	  </form>
	</div>
      <c:set var="rwikiContentStyle"  value="rwiki_content" />
      <!--.AJAX COMMENTS.-->
      <!--<jsp:directive.include file="comments.jsp"/>-->
	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <!-- Creates the right hand sidebar -->
	  <jsp:directive.include file="sidebar.jsp"/>
	  <!-- Main page -->
	  <div id="${rwikiContentStyle}" >
	    <div class="rwikiRenderBody">
	      <div class="rwikiRenderedContent"> 
		<c:out value="${renderBean.renderedPage}" escapeXml="false"/><br/>	    
	      </div>
	    </div>
	  </div>
	 <div class="lastmodified" >
	 <c:out value="${renderBean.localisedPageName}"/>
	 last modified by <rwiki:formatDisplayName name="${currentRWikiObject.user }"/> on <fmt:formatDate type="both" value="${currentRWikiObject.version}" /> 
	 </div>
	</div>
      </div>
      <jsp:directive.include file="comments.jsp"/>
      <!--.JS LOGGING.-->
      <!--<a href="#" onclick="document.getElementById('logdiv').innerHTML = ''; logInfo = !logInfo; return false;" >Clear</a>-->
      <!--<div id="logdiv" >-->
      <!--Log Info-->
      <!--</div>-->
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
