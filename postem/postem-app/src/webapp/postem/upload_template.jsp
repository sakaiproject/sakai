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

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session"> 
<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.postem.bundle.Messages"/> 
</jsp:useBean>
<f:view>
	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<h:form enctype="multipart/form-data">
				
		  	<sakai:tool_bar_message value="#{msgs.file_upload}" />
		  	<sakai:tool_bar_message value="#{msgs.template}" />
		  	
        <sakai:fileInput value="#{PostemTool.fileName}" target="#{PostemTool.targetFileName}"/>
		  	
				<sakai:button_bar>					
					<sakai:button_bar_item
						action="#{PostemTool.processTemplateUpload}"
						value="#{msgs.upload}" />
					<sakai:button_bar_item
						action="#{PostemTool.processUploadCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>		  	

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>