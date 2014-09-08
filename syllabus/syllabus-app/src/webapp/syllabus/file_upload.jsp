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
			<h:form enctype="multipart/form-data">
				<h:outputText value="file_upload.jsp" />				
		  	<sakai:tool_bar_message value="file upload" />
		  	<sakai:tool_bar_message value="#{SyllabusTool.fileName}" />
		  	
<%--		  	<sakai:fileInput binding="#{SyllabusTool.fileUploadUI}" />--%>
<%--		  	<sakai:fileInput value="#{SyllabusTool.fileName}"/>--%>
        <sakai:fileInput value="#{SyllabusTool.fileName}" target="#{SyllabusTool.targetFileName}"/>
<%--		  	<sakai:fileInput value="#{SyllabusTool.fileName}" />--%>
		  	
				<sakai:button_bar>					
					<h:commandButton
						action="#{SyllabusTool.processFileupload}"
						value="#{msgs.upload}" />
					<h:commandButton
						action="#{SyllabusTool.processUploadCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>		  	

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
