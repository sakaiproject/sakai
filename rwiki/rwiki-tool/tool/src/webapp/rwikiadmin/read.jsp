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
<sakai:view_container title="#{msgs.title_read}">
<h:form>

	<sakai:tool_bar>
		<sakai:tool_bar_item
			action="#{RWikiTool.processActionReadNew}"
			value="#{msgs.bar_new}" />
		<sakai:tool_bar_item
			action="#{RWikiTool.processActionReadDelete}"
			value="#{msgs.bar_delete}" />
		<sakai:tool_bar_item
			action="#{RWikiTool.processActionReadEdit}"
			value="#{msgs.bar_edit}" />
	</sakai:tool_bar>

	<sakai:view_content>

		<sakai:messages />

		<%-- the document display --%>
		<%-- the document display --%>
		<sakai:doc_section_title>
			<h:outputText value="#{msgs.prop_hdr_name}"/>
			<h:outputText value="#{RWikiTool.entry.entry.name}"/>
		
			<h:outputText value="#{msgs.prop_hdr_realm}"/>
			<h:outputText value="#{RWikiTool.entry.entry.realm}"/>
		</sakai:doc_section_title>
		
		<sakai:doc_section>
			<h:outputText value="#{msgs.prop_hdr_pageContent}"/>
			<h:outputText value="#{RWikiTool.entry.renderedContent}" escape="false" />
		</sakai:doc_section>
		<sakai:doc_properties>
 
			<h:outputText value="#{msgs.prop_hdr_id}"/>
			<h:outputText value="#{RWikiTool.entry.entry.id}"/>

			<h:outputText value="#{msgs.prop_hdr_version}"/>
			<h:outputText value="#{RWikiTool.entry.entry.version}"/>

		</sakai:doc_properties>


		<sakai:button_bar>
			<sakai:button_bar_item
					action="#{RWikiTool.processActionReadBack}"
					value="#{msgs.bar_back}" />
		</sakai:button_bar>

	</sakai:view_content>

</h:form>
</sakai:view_container>
</f:view>
