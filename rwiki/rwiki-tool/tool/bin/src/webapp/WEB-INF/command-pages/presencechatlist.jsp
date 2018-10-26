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
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	/>
  
<!-- this page delivers an DIV suitable for use by AJAX in the browser -->  
  
<c:set var="presenceBean" value="${requestScope.rsacMap.presenceBean}"/>
<c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>

<div class="rwikicommentbody_0" >	
	<c:forEach var="messages"
		  items="${presenceBean.spaceMessages}" >
		<div class="rwikicomenttext" >
			<c:out value="${presence.message}" />   		  
			<c:out value="${presence.user}" />&#160;   
			<c:out value="${presence.age}" />&#160;<c:out value="${rlb.jsp_ago}"/>
	     </div>
	</c:forEach>
</div>
</jsp:root>
