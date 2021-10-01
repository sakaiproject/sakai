<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>

<%
// hack in attempt to fix navigation quirk
org.sakaiproject.tool.cover.SessionManager.getCurrentToolSession().
	removeAttribute(org.sakaiproject.jsf2.util.JsfTool.LAST_VIEW_VISITED);
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>



<f:view>
  <sakai:view title="#{msgs.cdfm_discussions}" toolCssHref="/messageforums-tool/css/msgcntr.css">
  		<script>includeLatestJQuery("msgcntr");</script>
		<script src="/messageforums-tool/js/sak-10625.js"></script>
		<script src="/messageforums-tool/js/forum.js"></script>
		<script>
			$(document).ready(function () {
				var menuLink = $('#forumsMainMenuLink');
				var menuLinkSpan = menuLink.closest('span');
				menuLinkSpan.addClass('current');
				menuLinkSpan.html(menuLink.text());
			});
		</script>
	<h:form id="msgForum">
	<%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
	<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
        <div class="page-header">
            <h1><h:outputText value="#{msgs.cdfm_discussions}" /></h1>
        </div>
	<%@ include file="/jsp/discussionForum/includes/dfAreaInclude.jsp"%>
	
 	<%
  	String thisId = request.getParameter("panel");
  	if (thisId == null) 
  	{
    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
 		 }
	%>
			<script>
			function resize(){
  				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  			}
			</script> 
 		</h:form>
 	</sakai:view>
 </f:view>
