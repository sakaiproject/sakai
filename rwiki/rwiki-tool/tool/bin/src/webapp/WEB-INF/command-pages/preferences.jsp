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
 FIXME: i18n
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
   xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    /><jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="viewBean" value="${requestScope.rsacMap.viewBean}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <c:set var="preferencesBean" value="${requestScope.rsacMap.preferencesBean}"/>
  <c:set var="realmBean" value="${requestScope.rsacMap.realmBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
 
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}">
    <head>
      <title>Info: <c:out value="${realmBean.localName}" /></title>
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
							useEditLink="true"
							useInfoLink="true"
							useHistoryLink="true"
							useWatchLink="true"
							withNotification="${requestScope.rsacMap.withnotification}"
							viewLinkName="View"
							homeBean="${homeBean}"
							viewBean="${viewBean}"
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
	  
	  
	  <div id="rwiki_content" class="${rwikiContentStyle}">
	    <!-- CONTENT HERE -->
	    <h3><c:out value="${rlb.jsp_notifications_for}"/>: <c:out value="${viewBean.localSpace}"/></h3>
	    <form action="?#" method="post">
	      <p class="radio">
		<c:choose>
		  <c:when test="${preferencesBean.notificationLevel eq 'separate'}"><input type="radio" name="notificationLevel" id="notificationSeparate" value="separate" checked="checked"/></c:when>
		  <c:otherwise><input type="radio" name="notificationLevel" id="notificationSeparate" value="separate"/></c:otherwise>
		</c:choose><label for="notificationSeparate"><c:out value="${rlb.jsp_notify_seperate}"/></label>
	      </p>
	      <p class="radio">
		<c:choose>
		  <c:when test="${preferencesBean.notificationLevel eq 'digest'}"><input type="radio" name="notificationLevel" value="digest" id="notificationDigest" checked="checked"/></c:when>
		  <c:otherwise><input type="radio" name="notificationLevel" value="digest" id="notificationDigest"/></c:otherwise>
		</c:choose><label for="notificationDigest"><c:out value="${rlb.jsp_notify_summary}"/></label>
	      </p>
	      <p class="radio">
		<c:choose>
		  <c:when test="${preferencesBean.notificationLevel eq 'none'}"><input type="radio" name="notificationLevel" value="none" id="notificationNone" checked="checked"/></c:when>
		  <c:otherwise><input type="radio" name="notificationLevel" value="none" id="notificationNone"/></c:otherwise>
		</c:choose><label for="notificationNone"><c:out value="${rlb.jsp_notify_none}"/></label>
	      </p>
	      <p class="radio">
		<c:choose>
		  <c:when test="${preferencesBean.notificationLevel eq 'nopreference'}"><input type="radio" name="notificationLevel" value="nopreference" id="notificationNoPreference" checked="checked"/></c:when>
		  <c:otherwise><input type="radio" name="notificationLevel" value="nopreference" id="notificationNoPreference"/></c:otherwise>
		</c:choose><label for="notificationNoPreference"><c:out value="${rlb.jsp_notify_no_preference}"/></label>
	      </p>
	      <input type="submit" name="command_save" value="${rlb.jsp_button_save}"/>
	      <input type="submit" name="command_cancel" value="${rlb.jsp_button_cancel}"/>
	      <input type="hidden" name="action" value="updatePreferences"/>
	      <input type="hidden" name="pageName" value="${viewBean.pageName}"/>
	    </form>
	    	  
	    <jsp:directive.include file="sidebar.jsp"/>
	    
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
