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
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
   xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	/><jsp:text
	><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
      <c:set var="searchBean" value="${requestScope.rsacMap.fullSearchBean}"/>
      <c:set var="currentLocalSpace" value="${requestScope.rsacMap.currentLocalSpace}"/>
      <c:set var="rightRenderBean" value="${requestScope.rsacMap.searchRightRenderBean}"/>
        <c:set var="recentlyVisitedBean" value="${requestScope.rsacMap.recentlyVisitedBean}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
	<c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
    <head>
      <title><c:out value="${rlb.jsp_search}"/>: <c:out value="${searchBean.search}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
    	<div id="rwiki_container">
    		
    	<div class="portletBody">    		
      <div class="navIntraTool actionToolBar">
	<form action="?#" method="get" class="rwiki_searchForm">
		<rwiki:commandlinks 
							useHomeLink="true"
							useViewLink="false"
							useEditLink="false"
							useInfoLink="false"
							useHistoryLink="false"
							useWatchLink="false"
							homeBean="${homeBean}" 
							resourceLoaderBean="${rlb}"
						        />
	  <span class="rwiki_searchBox">
	    <c:out value="${rlb.jsp_search}"/>: 
	    <input type="hidden" name="action" value="full_search" />
	    <input type="hidden" name="panel" value="Main" />
	    <input type="text" name="search" />
	  </span>
	</form>
      </div>

      	
	<jsp:directive.include file="breadcrumb.jsp"/>
	<!-- Creates the right hand sidebar -->
	<jsp:directive.include file="sidebar.jsp"/>
	
	<jsp:setProperty name="searchBean" value="10" property="pagesize"/>
	<c:set var="searchResults" value="${searchBean.searchResults}"/>
	<c:set var="searchPages" value="${searchBean.searchPages}"/>
	<c:set var="requestSearchPages" value="${searchBean.requestPage}"/>
	<h3><c:out value="${rlb.jsp_search}"/>: <c:out value="${searchBean.search}"/></h3>
	<p>
     <c:out value="${rlb.jsp_search_took}"/><c:out value=" "/>  <c:out value="${searchBean.timeTaken}" /> <c:out value="${rlb.jsp_ms_found}"/><c:out value=" "/> <c:out value="${searchBean.nresults}" />
    </p>

	<jsp:useBean id="searchViewBean" class="uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean"/>
	<jsp:setProperty name="searchViewBean" value="${currentLocalSpace}" property="localSpace"/>
	
      	<div id="rwiki_content" class="nosidebar" >
		    	
 			  		<c:forEach var="foundItem" items="${searchResults}" >
     		<p>
     		<c:out value="${foundItem.index}" />:&#160; 
                       <jsp:element name="a">
                       <jsp:attribute name="href"><c:out value="${foundItem.url}"  escapeXml="false" /></jsp:attribute>
                   <c:out value="${foundItem.title}"  escapeXml="false" /> 
                       </jsp:element><br />
     		    <c:out value="${rlb.jsp_search_content}"/>: <br />
     		    			<c:out value="${foundItem.searchResult}"  escapeXml="false" /> 
     		    		<br />
     		    	<span style="font-size: smaller;" >
     		    <c:out value="${rlb.jsp_search_realms}"/>: <br />
     		    <c:forEach var="realm" items="${foundItem.valueMap.realm}" >
     		    			<c:out value="${realm}" /> <br />
		    			</c:forEach>
     		    		<br />
     		    <c:out value="${rlb.jsp_search_score}"/>: <c:out value="${foundItem.score}" />
				</span>	  		  
	  			</p>
		    		</c:forEach>
		    		<p>
		    		<c:out value="${rlb.jsp_search_result_page}"/>:
		    		<c:forEach var="pages" items="${searchPages}" >
		    		<jsp:element name="a">
		    		   <jsp:attribute name="href"><c:out value="${pages.fullSearchLinkUrl}" escapeXml="false" /></jsp:attribute>
		    		   <c:out value="${pages.page}" /> &#160;
		    		</jsp:element>
		    		</c:forEach>
		    		</p>
	    
	</div>

      </div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
