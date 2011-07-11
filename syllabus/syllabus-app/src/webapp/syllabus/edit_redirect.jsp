<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<h:form id="redirectForm">
				<h3>
					<h:outputText value="#{msgs.redirect_sylla}" />
				</h3>
				<h:messages styleClass="alertMessage" rendered="#{!empty facesContext.maximumSeverity}" />
				<h:panelGrid styleClass="jsfFormTable" columns="1">
					<h:panelGroup styleClass="shorttext required">
						<h:outputText value="*" styleClass="reqStar"/>
						<h:outputLabel for="urlValue"><h:outputText value="#{msgs.syllabus_url}"/></h:outputLabel>
						<h:inputText id="urlValue" value="#{SyllabusTool.currentRediredUrl}" size="65"/>
					</h:panelGroup>
				</h:panelGrid>
				<sakai:button_bar>
					<sakai:button_bar_item
						styleClass="active"
						action="#{SyllabusTool.processEditSaveRedirect}"
						value="#{msgs.save}" 
						accesskey="s" />
						
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditCancelRedirect}"
						value="#{msgs.cancel}" 
						accesskey="x" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
