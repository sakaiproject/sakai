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
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
<div class="rwiki_comments" >
	
<div class="rwikicommentbody_0" >	
	    <div class="rwikicommentheader">
<c:out value="${rlb.jsp_presence_user_visits}"/>:
<jsp:element name="a">
	<jsp:attribute name="href">#</jsp:attribute>
	<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${renderBean.openPageChatURL}" />',1); startChat('<c:out value="${renderBean.listPageChatURL}" />'); return false;</jsp:attribute>
	<c:out value="${rlb.jsp_presence_chat}"/></jsp:element>

		</div>
	<c:forEach var="presence"
		  items="${presenceBean.pagePresence}" >
		<div class="rwikicomenttext" >
			<c:out value="${presence.user}" />&#160;   
			<c:out value="${presence.age}" />&#160;<c:out value="${rlb.jsp_ago}"/>
	     </div>
	</c:forEach>
</div>
<div class="rwikicommentbody_1" >
	    <div class="rwikicommentheader">
<c:out value="${rlb.jsp_presence_space_visits}"/>:	
<jsp:element name="a">
	<jsp:attribute name="href">#</jsp:attribute>
	<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${renderBean.openSpaceChatURL}" />',1); startChat('<c:out value="${renderBean.listSpaceChatURL}" />'); return false;</jsp:attribute>
	<c:out value="${rlb.jsp_presence_chat}"/></jsp:element>
		</div>
<c:forEach var="presence"
		  items="${presenceBean.spacePresence}" >
		<div class="rwikicomenttext" >
			<c:out value="${presence.user}" />&#160;  
			<c:out value="${presence.age}" />&#160;<c:out value="${rlb.jsp_ago}"/>
	     </div>
</c:forEach>
 	</div>
</div>
<script type="text/javascript" >
var runChat = false;
var ajaxChatLoader = null;
var chatListURL = "";
var chatDivID = "chatmessages";
function startChat(url) {
	chatListURL = url;
	runChat = true;
}
function stopChat() {
	runChat = false;
}
function reloadChatCallback(responsestring) {
	var chatDiv = document.getElementByID(chatDivID);
	if ( chatDivID != null ) {
		chatDiv.innerHTML = responsestring;
	}
}

function reloadChat() {
	var chatDiv = document.getElementByID(chatDicID);
	if ( chatDiv != null ) {
		if ( ajaxChatLoader == null ) {
    			ajaxChatLoader = new AsyncDIVLoader();
 			ajaxChatLoader.loaderName = "chatReloader";
		}
		ajaxChatLoader.loadXMLDoc(url,"reloadChatCallback"); 
	}
	if ( runChat ) 
     	window.setTimeout("reloadChat()",250);
}
</script>


</jsp:root>
