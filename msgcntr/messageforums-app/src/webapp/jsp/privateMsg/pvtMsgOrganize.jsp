<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_organize}">
<!--jsp/privateMsg/pvtMsgOrganize.jsp-->
			<h:form id="pvtMsgOrganize">
			       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
				<hr />
				<sakai:tool_bar_message 	value="#{msgs.pvt_msgs_label} #{msgs.pvt_organize}" />
				<hr />

				<sakai:button_bar>
					<sakai:button_bar_item 	action="#{PrivateMessagesTool.processPvtMsgCancel}"	value="#{msgs.pvt_cancel}" />
				</sakai:button_bar>

			</h:form>

	</sakai:view_container>
</f:view>
