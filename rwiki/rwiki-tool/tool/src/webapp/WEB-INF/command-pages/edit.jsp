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
  ><jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    errorPage="/WEB-INF/command-pages/errorpage.jsp" 
    /><jsp:text
    ><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
	<c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
	<c:if test="${!permissionsBean.updateAllowed}">
	<jsp:scriptlet>
		if ( true ) {
			throw new uk.ac.cam.caret.sakai.rwiki.service.exception.UpdatePermissionException("You are not allowed to edit this page");
		}
	</jsp:scriptlet>
	</c:if>
  <c:set var="viewBean" value="${requestScope.rsacMap.viewBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.editRightRenderBean}"/>
  <c:set var="errorBean" value="${requestScope.rsacMap.errorBean}"/>
  <c:set var="editBean" value="${requestScope.rsacMap.editBean}"/>
  <c:set var="nameHelperBean" value="${requestScope.rsacMap.nameHelperBean}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>Edit: <c:out value="${viewBean.localName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload">setMainFrameHeightNoScroll('<jsp:expression>request.getAttribute("sakai.tool.placement.id")</jsp:expression>');setFocus(focus_path);parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
      	<div class="portletBody">
      		
	<div class="navIntraTool">
	  <form action="?#" method="get" class="rwiki_searchForm">
	    <span class="rwiki_pageLinks">
	      <!-- Home Link -->
	      <jsp:element name="a"><jsp:attribute name="href"><c:out value="${homeBean.homeLinkUrl}"/></jsp:attribute><c:out value="${homeBean.homeLinkValue}"/></jsp:element>
	      <!-- View Link -->
	      <jsp:element name="a"><jsp:attribute name="href"><c:out value="${viewBean.viewUrl}"/></jsp:attribute>View</jsp:element>
	      <!-- Edit Link -->
	      <jsp:element name="a"><jsp:attribute name="href"><c:out value="${viewBean.editUrl}"/></jsp:attribute><jsp:attribute name="class">rwiki_currentPage</jsp:attribute>Edit</jsp:element>
	      <!-- Info Link -->
	      <jsp:element name="a"><jsp:attribute name="href"><c:out value="${viewBean.infoUrl}"/></jsp:attribute>Info</jsp:element>
	      <!-- History Link -->
	      <jsp:element name="a"><jsp:attribute name="href"><c:out value="${viewBean.historyUrl}"/></jsp:attribute>History</jsp:element>
	    </span>
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
	  <jsp:directive.include file="sidebar-switcher.jsp"/>
	  <jsp:directive.include file="sidebar.jsp"/>
	  <!-- Main page -->
	  <div id="${rwikiContentStyle}" >

	    <h3>Edit: <span class="pageName" title="${viewBean.pageName}"><c:out value="${viewBean.localName}"/></span></h3>
	    <form action="?#" method="post" id="editForm">
	      <c:if test="${fn:length(errorBean.errors) gt 0}">
		<!-- XXX This is hideous -->
		<p class="validation" style="clear: none;">

		  <c:forEach var="error" items="${errorBean.errors}">
		    <c:out value="${error}"/>
		  </c:forEach>
		</p>
	      </c:if>

	      <c:if test="${editBean.saveType != null and editBean.saveType ne 'preview' and not(fn:startsWith(editBean.saveType, 'attach'))}">

			<c:if test="${editBean.saveType eq 'revert'}">
			    <c:set target="${editBean}" property="previousContent" value="${currentRWikiObject.history[editBean.previousRevision].content}"/>
			</c:if>

		<p class="longtext"><label for="submittedContent">Submitted Content</label>
		  <jsp:element name="input" id="submittedContent" >
		    <jsp:attribute name="type">hidden</jsp:attribute>
		    <jsp:attribute name="name">submittedContent</jsp:attribute>
		    <jsp:attribute name="value"><c:out value="${editBean.previousContent }"/></jsp:attribute>
		  </jsp:element>
		</p>
		<pre class="rwiki_previousContent">
		  <c:out value="${editBean.previousContent}"/>
		</pre>

	      </c:if>
	      <c:if test="${editBean.saveType eq 'preview' and nameHelperBean.submittedContent != null}">
		<p class="longtext"><label for="submittedContent">Submitted Content prior to Preview</label>
		  <jsp:element name="input">
		    <jsp:attribute name="type">hidden</jsp:attribute>
		    <jsp:attribute name="name">submittedContent</jsp:attribute>
		    <jsp:attribute name="value"><c:out value="${nameHelperBean.submittedContent}"/></jsp:attribute>
		  </jsp:element>
		</p>
		<pre class="rwiki_previousContent">
		  <c:out value="${nameHelperBean.submittedContent}"/>
		</pre>
	      </c:if>
	      <c:if test="${fn:startsWith(editBean.saveType, 'attach') and nameHelperBean.submittedContent != null}">
		<p class="longtext"><label for="submittedContent">Submitted Content prior to Attach</label>
		  <jsp:element name="input">
		    <jsp:attribute name="type">hidden</jsp:attribute>
		    <jsp:attribute name="name">submittedContent</jsp:attribute>
		    <jsp:attribute name="value"><c:out value="${nameHelperBean.submittedContent}"/></jsp:attribute>
		  </jsp:element>
		</p>
		<pre class="rwiki_previousContent">
		  <c:out value="${nameHelperBean.submittedContent}"/>
		</pre>
	      </c:if>
	      
	   <div class="longtext">
	      <label for="content" class="block">New Content</label>
		<div id="textarea_outer_sizing_div">
		  <div id="textarea_inner_sizing_div">
		    	<jsp:directive.include file="edittoolbar.jsp"/>
		    <textarea cols="60" rows="25" name="content" id="content" onselect="storeCaret(this)" onclick="storeCaret(this)" onkeyup="storeCaret(this)" >
		      <c:choose>
			<c:when test="${editBean.saveType eq 'preview' or fn:startsWith(editBean.saveType, 'attach')}">
			  <c:out value="${editBean.previousContent}"/>
			</c:when>
			<c:otherwise>
			  <c:out value="${currentRWikiObject.content}"/>
			</c:otherwise>
		      </c:choose>
		    </textarea>
		  </div>
		</div>
		<input type="hidden" name="action" value="save"/>
		<input type="hidden" name="panel" value="Main"/>
		<input type="hidden" name="version" value="${(editBean.saveType eq 'preview' or fn:startsWith(editBean.saveType, 'attach') )? editBean.previousVersion : currentRWikiObject.version.time}"/>
		<jsp:element name="input">
		  <jsp:attribute name="type">hidden</jsp:attribute>
		  <jsp:attribute name="name">pageName</jsp:attribute>
		  <jsp:attribute name="value"><c:out value="${currentRWikiObject.name}"/></jsp:attribute>
		</jsp:element>
		<jsp:element name="input">
		  <jsp:attribute name="type">hidden</jsp:attribute>
		  <jsp:attribute name="name">realm</jsp:attribute>
		  <jsp:attribute name="value"><c:out value="${currentRWikiObject.realm}"/></jsp:attribute>		
		</jsp:element>
	    </div>
	    <div class="rwiki_editControl" id="editControl">
		<p class="act">
		  <c:if test="${requestScope.rsacMap.withnotification}" >
		  	<input type="checkbox" name="smallchange" value="smallchange" /> Minor Change <br />
		  </c:if>
		  <input type="submit" name="save" value="Save" /><c:out value=" "/>
		  <c:if test="${((editBean.saveType eq 'preview' or fn:startsWith(editBean.saveType, 'attach')) and nameHelperBean.submittedContent != null) or (editBean.saveType ne null and editBean.saveType ne 'preview' and not(fn:startsWith(editBean.saveType, 'attach')))}">
		    <input type="submit" name="save" value="Overwrite"/><c:out value=" "/>
		  </c:if>
		  <input type="submit" name="save" value="Preview"/><c:out value=" "/>
		  <input type="submit" name="save" value="Cancel"/>
		</p>
	      </div>
	      <c:if test="${editBean.saveType eq 'preview'}">
		<h3 title="Preview Changes">Preview Changes</h3>
		<div class="rwikiRenderedContent">
		  <c:set var="currentContent" value="${currentRWikiObject.content}"/>
		  <c:set target="${currentRWikiObject}" property="content" value="${editBean.previousContent}"/>	    
		  <c:out value="${renderBean.previewPage}" escapeXml="false"/><br/>
		  <c:set target="${currentRWikiObject}" property="content" value="${currentContent}"/>	    
		</div>
	      </c:if>      
	    </form>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
