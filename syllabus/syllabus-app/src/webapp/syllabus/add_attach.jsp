<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.attachment}">
	<sakai:view_content>
		<h:form enctype="multipart/form-data">
		<h:outputText value="add_attach.jsp"/>
		  <sakai:tool_bar>
		  	<sakai:tool_bar_message value="#{msgs.title_add_attach_local}" /> 
		  </sakai:tool_bar>
				<sakai:doc_section>
					<h:outputText value="#{msgs.add_attach_prompt}" style="color: #6D7B8D"/>
				  <h:outputText value=""/>
				</sakai:doc_section>					

				<sakai:group_box>
					
					<sakai:panel_edit>

						<sakai:doc_section>
							<h:outputText value="#{msgs.filename}"/>
						  <sakai:inputFileUpload id="syllabus_add_attach"
							       valueChangeListener="#{SyllabusTool.processUpload}"/>
						</sakai:doc_section>		
					</sakai:panel_edit>
				</sakai:group_box>

				<sakai:button_bar>
					<h:commandButton
						action="#{SyllabusTool.processUploadConfirm}"
						value="#{msgs.attach_action}" />
					<h:commandButton
						action="#{SyllabusTool.processUploadCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>
		 </h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view> 
