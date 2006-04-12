<!-- To Turn Comments on, uncomment this 
<div class="rwiki_comments" >
<a href="#" onclick="ajaxRefPopup(this,'<c:out value="${renderBean.newCommentURL}" />',0); return false;" >Comment</a>
<c:forEach var="comment"
		  items="${renderBean.comments}" >
		 <div class="rwikicommentbody_<c:out value="${comment.commentLevel}" />">
		
	    <div class="rwikicommentheader">
	    
	        Comment by: <rwiki:formatDisplayName name="${comment.rwikiObject.user}"/> on <c:out value="${comment.rwikiObject.version}" /> 
        		<a href="#" onclick="ajaxRefPopup(this,'<c:out value="${comment.newCommentURL}" />',0); return false;" >Comment</a>
        		<c:if test="${comment.canEdit}" >
        			<a href="#" onclick="ajaxRefPopup(this,'<c:out value="${comment.editCommentURL}" />',0); return false;" >Edit</a>
        		</c:if>
        	</div>
		 <div class="rwikicomenttext" />
				<c:out value="${comment.renderedPage}" escapeXml="false"/><br/>	    
	      </div>
	    </div>
</c:forEach>
</div>
-->
<!-- To Turn AJAX Comments on, uncomment this and change the commandComponents.xml to use EditSaveAction 
	    <script type="text/javascript" >
var commentsShowing = false;
var lastCommentControl = null;
function toggleComments(target) {
	if ( target == null ) 
		target = lastCommentControl;
	lastCommentControl = target;
	if ( commentsShowing ) {
		target.innerHTML = "Show Comments";
	    popupClose(0);
	    commentsShowing = false;
	} else {
		popupClose(0);
		ajaxRefPopup(target,'<c:out value="${renderBean.listCommentsURL}" escapeXml="false" />',0);
		target.innerHTML = "Hide Comments";
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
		target.innerHTML = "Show Who";
	    popupClose(0);
	    presenceShowing = false;
	} else {
		popupClose(0);
		ajaxRefPopup(target,'<c:out value="${renderBean.listPresenceURL}" escapeXml="false" />',0);
		target.innerHTML = "Hide Who";
		presenceShowing = true;
	}
}
</script>
<div id="rwiki_comments_sidebar" >
<a href="#" onclick="toggleComments(this); return false;" >Show Comments</a>
<a href="#" onclick="togglePresence(this); return false;" >Show Who</a>
</div>
-->
