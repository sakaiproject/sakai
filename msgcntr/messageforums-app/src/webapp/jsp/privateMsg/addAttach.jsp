<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<sakai:view_container title="Attachments">
	<sakai:view_content>
		<h:form enctype="multipart/form-data">
		       		<script>includeLatestJQuery("msgcntr");</script>
       		<script src="/messageforums-tool/js/sak-10625.js"></script>
       		<script src="/messageforums-tool/js/messages.js"></script>
		  <sakai:tool_bar>
		  	<sakai:tool_bar_message value="Attachment from a Local File" /> 
		  </sakai:tool_bar>
		  	        
				<sakai:doc_section>
					<h:outputText value="Browse to locate a file, then choose 'Attach'. Please be patient - uploads may take some time." style="color: #6D7B8D"/>
				  <h:outputText value=""/>
				</sakai:doc_section>					

				<sakai:group_box>
					
					<sakai:panel_edit>

						<sakai:doc_section>
							<h:outputText value="Filename: "/>
						  <sakai:inputFileUpload id="syllabus_add_attach"
							       valueChangeListener="#{PrivateMessagesTool.processUpload}"/>
						</sakai:doc_section>		
					</sakai:panel_edit>
				</sakai:group_box>

				<sakai:button_bar>
					<h:commandButton
						action="#{PrivateMessagesTool.processUploadConfirm}"
						value="Attach" />
					<h:commandButton
						action="#{PrivateMessagesTool.processUploadCancel}"
						value="Cancel" />
				</sakai:button_bar>
		 </h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view> 