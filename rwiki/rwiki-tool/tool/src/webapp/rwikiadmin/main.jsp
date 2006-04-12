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
<sakai:view_container title="#{msgs.title_list}">
<h:form>

	<sakai:tool_bar>
		<sakai:tool_bar_item
			action="#{RWikiTool.processActionListNew}"
			value="#{msgs.bar_new}" />
		<sakai:tool_bar_item
			action="#{RWikiTool.processActionListDelete}"
			value="#{msgs.bar_delete}" />
		<sakai:tool_bar_item
			action="#{RWikiTool.processActionListEdit}"
			value="#{msgs.bar_edit}" />
	</sakai:tool_bar>

	<sakai:view_content>

		<sakai:messages />
	
		<%-- the list of announcements --%>
		<sakai:flat_list value="#{RWikiTool.entries}" var="co">
			<h:column>
				<f:facet name="header">
					<h:outputText value=""/>
				</f:facet>
				<h:selectBooleanCheckbox value="#{co.selected}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.col_hdr_name}" />
				</f:facet>
				<h:commandLink action="#{co.processActionListRead}">
					<h:outputText value="#{co.entry.name}"/>
				</h:commandLink>
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
				<h:outputLink value="?page=#{co.entry.name}" >
					<f:verbatim>view</f:verbatim>
				</h:outputLink>
			</h:column>
		</sakai:flat_list>

	</sakai:view_content>

</h:form>
</sakai:view_container>
</f:view>
