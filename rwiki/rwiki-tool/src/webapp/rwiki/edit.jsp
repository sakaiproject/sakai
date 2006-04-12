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
<sakai:view_container title="#{msgs.title_edit}">
<h:form>

	<sakai:tool_bar_message value="#{RWikiTool.editToolbarMsg}" />

	<sakai:view_content>

		<sakai:messages />
	
		<h:outputText value="#{RWikiTool.breadcrumbLinks}" escape="false" />
	
		<sakai:instruction_message value="#{RWikiTool.editInstructionMsg}" />
	
		<sakai:group_box title="#{msgs.title_entry}">
			<sakai:panel_edit>
	 
				<%-- the selected entry --%>
				<h:outputText value="#{msgs.prop_hdr_name}"/>
				<h:inputText value="#{RWikiTool.entry.entry.name}" required="true" />
		
				<h:outputText value="#{msgs.prop_hdr_realm}"/>
				<h:inputText value="#{RWikiTool.entry.entry.realm}" required="true" />
	
				<h:outputText value="#{msgs.prop_hdr_pageContent}"/>
				<h:inputTextarea value="#{RWikiTool.entry.entry.content}" required="true" rows="40" cols="80" />

			</sakai:panel_edit>
		</sakai:group_box>

		<sakai:doc_properties>
 
			<h:outputText value="#{msgs.prop_hdr_id}"/>
			<h:outputText value="#{RWikiTool.entry.entry.id}"/>

			<h:outputText value="#{msgs.prop_hdr_version}"/>
			<h:outputText value="#{RWikiTool.entry.entry.version}"/>

		</sakai:doc_properties>

		<sakai:button_bar>
			<sakai:button_bar_item
					action="#{RWikiTool.processActionEditSave}"
					value="#{msgs.bar_save}" />
			<sakai:button_bar_item
					immediate="true"
					action="#{RWikiTool.processActionEditCancel}"
					value="#{msgs.bar_cancel}" />
		</sakai:button_bar>

	</sakai:view_content>

</h:form>
</sakai:view_container>
</f:view>
