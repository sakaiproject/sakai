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
  <c:set var="nameHelperBean" value="${requestScope.rsacMap.nameHelperBean}"/>
  <c:set var="viewBean" value="${requestScope.rsacMap.viewBean}"/>
  <c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.previewRightRenderBean}"/>
  <c:set var="errorBean" value="${requestScope.rsacMap.errorBean}"/>
  <c:set var="editBean" value="${requestScope.rsacMap.editBean}"/>
  <c:set var="realmBean" value="${requestScope.rsacMap.realmBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
    <head>
      <title><c:out value="${rlb.jsp_preview_changes_to}"/>: <c:out value="${viewBean.localName}"/></title>
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
							useViewLink="true"
							useEditLink="false"
							useInfoLink="true"
							useHistoryLink="false"
							useWatchLink="false"
							withNotification="${requestScope.rsacMap.withnotification}"
							viewLinkName="View"
							homeBean="${homeBean}"
							viewBean="${realmBean}" 
							resourceLoaderBean="${rlb}" 
						        />	  
		<span class="rwiki_searchBox">
	    		<c:out value="${rlb.jsp_search}"/>:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />
	    		<input type="hidden" name="panel" value="Main" />
	    		<input type="text" name="search" />
	    </span>
	  </form>
	</div>
	<c:choose>
	
	  <c:when test="${rightRenderBean.hasContent}" >
		 <c:set var="rwikiContentStyle"  value="withsidebar" />	
	  </c:when>
	  <c:otherwise>
		 <c:set var="rwikiContentStyle"  value="nosidebar" />    
	  </c:otherwise>
	</c:choose>
		
	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <div id="rwiki_head" >				    
		<jsp:directive.include file="sidebar-switcher.jsp"/>		     
	  </div>
	  
	  <!-- Main page -->
	  <div id="rwiki_content" class="${rwikiContentStyle}" >
			<h3 title="Preview Changes: ${viewBean.pageName}"><c:out value="${rlb.jsp_preview_changes}"/>: <c:out value="${viewBean.localName}"/></h3>
	    <div class="rwikiRenderBody">
	      <div class="rwikiRenderedContent"> 
		<c:set var="currentContent" value="${currentRWikiObject.content}"/>
		<c:set target="${currentRWikiObject}" property="content" value="${nameHelperBean.content}"/>	    
		<c:out value="${renderBean.previewPage}" escapeXml="false"/><br/>
		<c:set target="${currentRWikiObject}" property="content" value="${currentContent}"/>	    
	      </div>
	    </div>
	    <form action="?#" method="post" >
	      <jsp:element name="input">
		<jsp:attribute name="type">hidden</jsp:attribute>
		<jsp:attribute name="name">pageName</jsp:attribute>
		<jsp:attribute name="value"><c:out value="${viewBean.pageName}"/></jsp:attribute>
	      </jsp:element>
	      <input type="hidden" name="panel" value="Main"/>
	      <jsp:element name="input">
		<jsp:attribute name="type">hidden</jsp:attribute>
		<jsp:attribute name="name">content</jsp:attribute>
		<jsp:attribute name="value"><c:out value="${nameHelperBean.content}"/></jsp:attribute>
	      </jsp:element>
	      <input type="hidden" name="command_preview" value="preview"/>

	      <p class="act">
		<input type="hidden" name="action" value="edit" />
		<input type="submit" name="preview" value="${rlb.jsp_button_back_to_edit}"/>
	      </p>

	      <div class="rwiki_docdetails">
		<h3 onClick="expandcontent(this, 'pagedetails')" class="expandable" id="pagedetailsh3">
		  <span class="showstate">&#160;</span><c:out value="${rlb.jsp_document_properties}"/>
		</h3>
		<div id="pagedetails" class="expandablecontent">
		  <table cellspacing="0">
		    <tbody>
		      <tr>
			<th><c:out value="${rlb.jsp_global_page_name}"/></th>
			<td><c:out value="${viewBean.pageName}"/></td>
		      </tr>
		      <tr>
			<th><c:out value="${rlb.jsp_page_realm}"/></th>
			<td><c:out value="${currentRWikiObject.realm}"/></td>
		      </tr>
		      <tr>
			<th><c:out value="${rlb.jsp_id}"/></th>
			<td><c:out value="${currentRWikiObject.id}"/></td>
		      </tr>
		      <tr>
			<th><c:out value="${rlb.jsp_last_edited}"/></th>
			<td><c:out value="${currentRWikiObject.version}"/></td>
		      </tr>
		    </tbody>
		  </table>
		</div>
		<script type="text/javascript">
		  <![CDATA[
		  <!--
		  hidecontent('pagedetailsh3','pagedetails');
		  -->
		  ]]>
		</script>
	      </div>
	    </form>
	  </div>
	  
	  <jsp:directive.include file="sidebar.jsp"/>
	  
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
