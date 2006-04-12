<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/>
  
<!-- this page delivers an DIV suitable for use by AJAX in the browser -->  
  
  <c:set var="presenceBean" value="${requestScope.rsacMap.presenceBean}"/>
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
<div class="rwiki_comments" >
	
<div class="rwikicommentbody_0" >	
	    <div class="rwikicommentheader">
Users visits:
<jsp:element name="a">
	<jsp:attribute name="href">#</jsp:attribute>
	<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${renderBean.openPageChatURL}" />',1); startChat('<c:out value="${renderBean.listPageChatURL}" />'); return false;</jsp:attribute>
	Chat</jsp:element>

		</div>
	<c:forEach var="presence"
		  items="${presenceBean.pagePresence}" >
		<div class="rwikicomenttext" >
			<c:out value="${presence.user}" />&#160;   
			<c:out value="${presence.age}" /> ago
	     </div>
	</c:forEach>
</div>
<div class="rwikicommentbody_1" >
	    <div class="rwikicommentheader">
Space visits:	
<jsp:element name="a">
	<jsp:attribute name="href">#</jsp:attribute>
	<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${renderBean.openSpaceChatURL}" />',1); startChat('<c:out value="${renderBean.listSpaceChatURL}" />'); return false;</jsp:attribute>
	Chat</jsp:element>
		</div>
<c:forEach var="presence"
		  items="${presenceBean.spacePresence}" >
		<div class="rwikicomenttext" >
			<c:out value="${presence.user}" />&#160;  
			<c:out value="${presence.age}" /> ago
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
