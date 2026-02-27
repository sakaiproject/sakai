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
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jsp/jstl/core" version="2.0"
 xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		/><jsp:text
		><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
	</jsp:text>
	<c:set var="viewBean" value="${requestScope.rsacMap.viewBean}" />
	<c:set var="homeBean" value="${requestScope.rsacMap.homeBean}" />
    <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  	<html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title><c:out value="${rlb.jsp_info}"/>Permission Denied</title>
	<jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
	</head>
	<jsp:element name="body">
		<jsp:attribute name="onload">
			<jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression> callAllLoaders();</jsp:attribute>
		<div id="rwiki_container">
			<div class="portletBody">
					<div class="navIntraTool actionToolBar">
						<div class="rwiki_searchForm">
							    	  	<rwiki:commandlinks 
							useHomeLink="true"
							useViewLink="false"
							useEditLink="false"
							useInfoLink="false"
							useHistoryLink="false"
							useWatchLink="false"
							withNotification="${requestScope.rsacMap.withnotification}"
							viewLinkName="View"
							homeBean="${homeBean}"
							viewBean="${viewBean}"
							resourceLoaderBean="${rlb}"
						        />
						
						</div>
					</div>
	<jsp:directive.include file="breadcrumb.jsp"/>
	<h3><c:out value="${rlb.jsp_permission_denied}"/></h3>
	<p><c:out value="${rlb.jsp_permission_denied_message}"/></p>
		
</div>
</div>
	</jsp:element>
	</html>
</jsp:root>
