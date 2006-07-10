<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<f:loadBundle
	basename="org.sakaiproject.tool.messageforums.bundle.Messages"
	var="msgs" />
<link href='/sakai-messageforums-tool/css/msgForums.css'
	rel='stylesheet' type='text/css' />

<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_organize}">

			<h:form id="pvtMsgOrganize">

				<hr />
				<sakai:tool_bar_message 	value="#{msgs.pvt_msgs_label} #{msgs.pvt_organize}" />
				<hr />

				<sakai:button_bar>
					<sakai:button_bar_item 	action="#{PrivateMessagesTool.processPvtMsgCancel}"	value="#{msgs.pvt_cancel}" />
				</sakai:button_bar>

			</h:form>

	</sakai:view_container>
</f:view>
