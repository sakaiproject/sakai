<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>


<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<h:form enctype="multipart/form-data">
				
		  	<sakai:tool_bar_message value="file upload" />
		  	<sakai:tool_bar_message value="#{SyllabusTool.fileName}" />
		  	
<%--		  	<sakai:fileInput binding="#{SyllabusTool.fileUploadUI}" />--%>
<%--		  	<sakai:fileInput value="#{SyllabusTool.fileName}"/>--%>
        <sakai:fileInput value="#{SyllabusTool.fileName}" target="#{SyllabusTool.targetFileName}"/>
<%--		  	<sakai:fileInput value="#{SyllabusTool.fileName}" />--%>
		  	
				<sakai:button_bar>					
					<sakai:button_bar_item
						action="#{SyllabusTool.processFileupload}"
						value="#{msgs.upload}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processUploadCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>		  	

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>