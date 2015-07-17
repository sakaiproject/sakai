<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<sakai:view_container title="Attachments">
	<sakai:view_content>
		<h:form enctype="multipart/form-data">
		       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
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
					<sakai:button_bar_item
						action="#{PrivateMessagesTool.processUploadConfirm}"
						value="Attach" />
					<sakai:button_bar_item
						action="#{PrivateMessagesTool.processUploadCancel}"
						value="Cancel" />
				</sakai:button_bar>
		 </h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view> 