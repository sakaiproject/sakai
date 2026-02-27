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
	/><jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="historyBean" value="${requestScope.rsacMap.historyBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.diffRightRenderBean}"/>
  <c:set var="diffBean" value="${requestScope.rsacMap.diffBean}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
    <head>
      <title><c:out value="${rlb.jsp_title_diff}"/>: <c:out value="${historyBean.localName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression> callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
      	<div class="portletBody">
      		<div class="navIntraTool actionToolBar">
	  <div class="rwiki_searchForm">
	  				<rwiki:commandlinks 
							useHomeLink="true"
							useViewLink="true"
							useEditLink="false"
							useInfoLink="false"
							useHistoryLink="true"
							useWatchLink="false"
							viewLinkName="View Current"
							homeBean="${homeBean}"
							viewBean="${historyBean}"
							resourceLoaderBean="${rlb}"  
						        />
	  
	    <span class="rwiki_searchBox">
	      <button type="button" class="btn btn-secondary" onclick="openWikiSearch()"><c:out value="${rlb.jsp_search}"/></button>
	    </span>
	  </div>
	</div>
	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <div id="rwiki_head" >				    
		<jsp:directive.include file="sidebar-switcher.jsp"/>		     
	  </div>
	  <!-- Main page -->
	  
	  <div id="rwiki_content" class="nosidebar">
	    <h3>
	      <c:out value="${rlb.jsp_page_differences}"/>: <c:out value="${historyBean.localName}"/>
	      (<c:out value="${rlb.jsp_page_version}"/> <c:out value="${diffBean.left.revision}"/>
	      vs <c:out value="${diffBean.right.revision}"/>)
	    </h3>
	    <div class="differences">
	      <table class="colordiff">
		<tr>
		  <td class="pageLeft">
		    <jsp:setProperty name="historyBean" property="interestedRevision" value="${diffBean.left.revision}"/>
		    <jsp:element name="a">
		      <jsp:attribute name="href"><c:out value="${historyBean.viewRevisionUrl}"/></jsp:attribute>
		      <c:out value="${rlb.jsp_page_version}"/> <c:out value="${diffBean.left.revision}"/>
		    </jsp:element>
		    <br/>
		    (<c:out value="${rlb.jsp_page_modified}"/>: <fmt:formatDate type="both" value="${diffBean.left.version}"/> <c:out value="${rlb.jsp_by}"/> <rwiki:formatDisplayName name="${(diffBean.left.user)}"/>)
		  </td>
		  <td class="pageRight">
		    <jsp:setProperty name="historyBean" property="interestedRevision" value="${diffBean.right.revision}"/>
		    <jsp:element name="a">
		      <jsp:attribute name="href"><c:out value="${historyBean.viewRevisionUrl}"/></jsp:attribute>
		      <c:out value="${rlb.jsp_page_version}"/> <c:out value="${diffBean.right.revision}"/>
		    </jsp:element>
		    <br/>
		    (<c:out value="${rlb.jsp_page_modified}"/>: <fmt:formatDate type="both" value="${diffBean.right.version}"/> <c:out value="${rlb.jsp_by}"/> <rwiki:formatDisplayName name="${(diffBean.right.user)}"/>)
		  </td>
		</tr>
		<c:out value="${diffBean.genericDiffBean.colorDiffTable}" escapeXml="false"/>
	      </table>
	    </div>
	    <table border="0" cellpadding="0" cellspacing="0" class="keytable">
	      <tr>
		<td colspan="2" class="keytablehead">Key</td>
	      </tr>
	      <tr>
		<td width="50%" class="deletedLeft"><c:out value="${rlb.jsp_page_diff_deleted}"/></td>
		<td width="50%" class="deletedRight">&#160;</td>
	      </tr>
	      <tr>
		<td colspan="2" class="changedLeft"><div align="center"><c:out value="${rlb.jsp_page_diff_changed}"/></div></td>
	      </tr>
	      <tr>
		<td width="50%" class="addedLeft">&#160;</td>
		<td width="50%" class="addedRight">Added</td>
	      </tr>
	    </table>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
