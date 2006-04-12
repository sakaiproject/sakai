<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<%
		response.setContentType("text/html; charset=UTF-8");
		response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		response.addDateHeader("Last-Modified", System.currentTimeMillis());
		response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		response.addHeader("Pragma", "no-cache");
%>

<f:loadBundle basename="uk.ac.cam.caret.sakai.rwiki.tool.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view_container title="#{msgs.title_delete}">
<h:form>

	<sakai:tool_bar_message value="#{msgs.toolbar_delete}" />

	<sakai:view_content>

		<sakai:messages />
	
		<sakai:instruction_message value="#{msgs.instruction_delete}"/>
	
		<%-- the list of entries selected for delete --%>
		<sakai:flat_list value="#{RWikiTool.selectedEntries}" var="co">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.col_hdr_name}" />
				</f:facet>
				<h:outputText value="#{co.entry.name}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.col_hdr_realm}"/>
				</f:facet>
				<h:outputText value="#{co.entry.realm}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.col_hdr_pageContent}"/>
				</f:facet>
				<h:outputText value="#{co.entry.content}"/>
			</h:column>
		</sakai:flat_list>
	
		<sakai:button_bar>
			<sakai:button_bar_item
					action="#{RWikiTool.processActionDeleteDelete}"
					value="#{msgs.bar_delete}" />
			<sakai:button_bar_item
					action="#{RWikiTool.processActionDeleteCancel}"
					value="#{msgs.bar_cancel}" />
		</sakai:button_bar>

	</sakai:view_content>

</h:form>
</sakai:view_container>
</f:view>
