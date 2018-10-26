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
    /><jsp:text
    ><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
  <div class="rwiki_help_popup" >
	    <form action="?#" method="post" >
	    <nobr><label for="wiki-textarea-content"><c:out value="${rlb.jsp_new_comment}"/></label></nobr><br/>
		<textarea cols="40" rows="10" name="content" id="wiki-textarea-content" >&#160;</textarea>
		<input type="hidden" name="action" value="commentnewsave"/>
		<input type="hidden" name="panel" value="Main"/>
		<input type="hidden" name="version" value="${currentRWikiObject.version.time}"/>
		<jsp:element name="input">
		 	<jsp:attribute name="type">hidden</jsp:attribute>
			<jsp:attribute name="name">pageName</jsp:attribute>
			<jsp:attribute name="value"><c:out value="${currentRWikiObject.name}" escapeXml="true"/></jsp:attribute>
		</jsp:element>
		<jsp:element name="input">
			<jsp:attribute name="type">hidden</jsp:attribute>
			<jsp:attribute name="name">realm</jsp:attribute>
			<jsp:attribute name="value"><c:out value="${currentRWikiObject.realm}" escapeXml="true"/></jsp:attribute>
		</jsp:element>
		<br/>
		<nobr>
		<span class="act">
			<jsp:element name="input">
				<jsp:attribute name="type">submit</jsp:attribute> 
				<jsp:attribute name="name">save</jsp:attribute>
				<jsp:attribute name="value"><c:out value="${rlb.jsp_button_save}" /></jsp:attribute>
			</jsp:element>
			<jsp:element name="input">
				<jsp:attribute name="type">button</jsp:attribute> 
				<jsp:attribute name="onclick">popupClose(-1);</jsp:attribute>
				<jsp:attribute name="value"><c:out value="${rlb.jsp_button_cancel}" /></jsp:attribute>
			</jsp:element>
		</span>
		</nobr>
	    </form>
	</div>
</jsp:root>
