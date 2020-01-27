<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
	<sakai:view_container title="Attachment">
<!--jsp/privateMsg/removeAttachConfirm.jsp-->
		<sakai:view_content>
			<h:form id="removeAttachConfirm">

       		<script>includeLatestJQuery("msgcntr");</script>
			<script src="/messageforums-tool/js/sak-10625.js"></script>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
				
