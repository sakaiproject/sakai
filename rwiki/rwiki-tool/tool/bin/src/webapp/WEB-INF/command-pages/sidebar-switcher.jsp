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
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" 
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	version="2.0">
  <jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
   <c:if test="${rightRenderBean.hasContent}" > 
      <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>    
<div id="rwiki_sidebar_switcher">
    <jsp:element name="a">
    	<jsp:attribute name="id">sidebar_switch_on</jsp:attribute>      
    	<jsp:attribute name="href">#</jsp:attribute>
    	<jsp:attribute name="onclick">showSidebar('<jsp:expression>request.getAttribute("sakai.tool.placement.id")</jsp:expression>')</jsp:attribute>
    	<jsp:body>(+)</jsp:body>
    </jsp:element>
    <jsp:element name="a">
    	<jsp:attribute name="id">sidebar_switch_off</jsp:attribute>      
    	<jsp:attribute name="href">#</jsp:attribute>
    	<jsp:attribute name="onclick">hideSidebar('<jsp:expression>request.getAttribute("sakai.tool.placement.id")</jsp:expression>')</jsp:attribute>
    	<jsp:body>(-)</jsp:body>
    </jsp:element>
</div>
    </c:if>
</jsp:root>
