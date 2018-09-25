<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<%
    response.setContentType("text/html; charset=UTF-8");
    response.addHeader("Cache-Control", "no-store, no-cache");
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.postem.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.title_list}">
		<script src="/library/js/spinner.js" type="text/javascript"></script>
		<h:form>
			<h:outputText styleClass="alertMessage" value="#{msgs.delete_confirm}" />
			<br />
			<table styleClass="itemSummary">
				<tr>
					<th scope="row"><h:outputText value="#{msgs.title_label}" /></th>
					<td><h:outputText value="#{PostemTool.currentGradebook.title}"/></td>
				</tr>
			</table>

			<sakai:button_bar>
				<sakai:button_bar_item action="#{PostemTool.processDelete}"
									   onclick="SPNR.disableControlsAndSpin(this, null);"
									   value="#{msgs.bar_delete}" />
				<sakai:button_bar_item action="#{PostemTool.processCancelView}"
									   onclick="SPNR.disableControlsAndSpin(this, null);"
									   value="#{msgs.cancel}" />
			</sakai:button_bar>
		</h:form>
	</sakai:view>
</f:view>
