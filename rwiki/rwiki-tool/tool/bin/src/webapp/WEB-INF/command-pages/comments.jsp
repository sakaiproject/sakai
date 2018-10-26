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
<c:if test="${requestScope.rsacMap.withcomments}" >
<div class="rwiki_comments" >
<a href="#" onclick="ajaxRefPopup(this,'<c:out value="${renderBean.newCommentURL}" />',0); return false;" >Comment</a>
<c:forEach var="comment"
		  items="${renderBean.comments}" >
		 <div class="rwikicommentbody_<c:out value="${comment.commentLevel}" />">
		
	    <div class="rwikicommentheader">
	    <nobr>
	        <c:out value="${rlb.jsp_comment_by}" />: <rwiki:formatDisplayName name="${comment.rwikiObject.user}"/> <c:out value="${rlb.jsp_on}" /> <c:out value="${comment.rwikiObject.version}" /> 
        		<a href="#" onclick="ajaxRefPopup(this,'<c:out value="${comment.newCommentURL}" />',0); return false;" ><c:out value="${rlb.jsp_comment}" /></a>
        		<c:if test="${comment.canEdit}" >
        			<a href="#" onclick="ajaxRefPopup(this,'<c:out value="${comment.editCommentURL}" />',0); return false;" ><c:out value="${rlb.jsp_edit}" /></a>
        		</c:if>
        </nobr>
        	</div>
		 <div class="rwikicomenttext" />
				<c:out value="${comment.renderedPage}" escapeXml="false"/><br/>	    
	      </div>
	    </div>
</c:forEach>
</div>
</c:if>
<!-- To Turn AJAX Comments on, uncomment this and change the commandComponents.xml to use EditSaveAction 
	    <script type="text/javascript" >
var commentsShowing = false;
var lastCommentControl = null;
function toggleComments(target) {
	if ( target == null ) 
		target = lastCommentControl;
	lastCommentControl = target;
	if ( commentsShowing ) {
		target.innerHTML = "<c:out value="${rlb.jsp_show_comments}" />";
	    popupClose(0);
	    commentsShowing = false;
	} else {
		popupClose(0);
		ajaxRefPopup(target,'<c:out value="${renderBean.listCommentsURL}" escapeXml="false" />',0);
		target.innerHTML = "<c:out value="${rlb.jsp_hide_comments}" />";
		commentsShowing = true;
	}
}
var presenceShowing = false;
var lastPresenceControl = null;
function togglePresence(target) {
	if ( target == null ) 
		target = lastPresenceControl;
	lastPresenceControl = target;
	if ( presenceShowing ) {
		target.innerHTML = "<c:out value="${rlb.jsp_show_who}" />";
	    popupClose(0);
	    presenceShowing = false;
	} else {
		popupClose(0);
		ajaxRefPopup(target,'<c:out value="${renderBean.listPresenceURL}" escapeXml="false" />',0);
		target.innerHTML = "<c:out value="${rlb.jsp_hide_who}" />";
		presenceShowing = true;
	}
}
</script>
<div id="rwiki_comments_sidebar" >
<a href="#" onclick="toggleComments(this); return false;" ><c:out value="${rlb.jsp_show_comments}" /></a>
<a href="#" onclick="togglePresence(this); return false;" ><c:out value="${rlb.jsp_show_who}" /></a>
</div>
-->
