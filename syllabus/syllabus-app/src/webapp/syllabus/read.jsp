<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>

<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<h:form>

		  	<sakai:tool_bar_message value="#{msgs.editNotice}" /> 
		  	        
				<sakai:doc_section>
					<h:outputText value="#{msgs.newSyllabusForm1}"/>
					<h:outputText value="*" style="color: red"/>
					<h:outputText value=" means" />
				</sakai:doc_section>					
				<sakai:doc_section>
					<h:outputText value="#{msgs.newSyllabusForm2}"/>
				</sakai:doc_section>

				<sakai:group_box>
					
					<sakai:panel_edit>
						<sakai:doc_section>
							<h:outputText value="*" style="color: red"/>
							<h:outputText value="#{msgs.syllabus_title}"/>
						</sakai:doc_section>
						<sakai:doc_section>
							<h:inputText value="#{SyllabusTool.entry.entry.title}" id="title"/>
							<h:outputText value="#{msgs.empty_title_validate}" style="color: red" 
								rendered="#{SyllabusTool.displayTitleErroMsg}"/>
						</sakai:doc_section>

						<h:outputText value="#{msgs.syllabus_content}"/>
						<sakai:rich_text_area value="#{SyllabusTool.entry.entry.asset}"   rows="17" columns="65" javascriptLibrary="/library/htmlarea"/>
						
						<h:outputText value="#{msgs.attachment}"/>
						<h:outputText value=" "/>

						<h:outputText value=" "/>
						<sakai:doc_section>
							<h:commandLink action="#{SyllabusTool.processAddAttWithOldItem}" onfocus="document.forms[0].onsubmit();">
									<h:outputText value="#{msgs.attachment_local} "/>
							</h:commandLink>
							<h:outputLink value="sakai.filepicker.helper/tool" onfocus="document.forms[0].onsubmit(); document.forms[0].submit();return false;">
							  <h:outputText id="file_picker" value=" #{msgs.file_picker}"/>
							</h:outputLink>
						</sakai:doc_section>
						
						<h:outputText value="" style="color: red"  rendered="#{SyllabusTool.displayEvilTagMsg}"/>
				    <h:outputText value="#{msgs.empty_content_validate} #{SyllabusTool.evilTagMsg}" style="color: red"  rendered="#{SyllabusTool.displayEvilTagMsg}"/>
					</sakai:panel_edit>
				</sakai:group_box>

				<sakai:group_box>
					<syllabus:syllabus_table value="#{SyllabusTool.allAttachments}" var="eachAttach">
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
								<h:outputText value="Title" />
							</f:facet>
							<h:outputLink value="#{eachAttach.url}" target="_new_window">
								<h:outputText value="#{eachAttach.name}"/>
							</h:outputLink>
							<h:commandLink action="#{SyllabusTool.processDeleteAttach}" 
								onfocus="document.forms[0].onsubmit();">
								<h:outputText value="     Remove"/>
								<f:param value="#{eachAttach.syllabusAttachId}" name="syllabus_current_attach"/>
							</h:commandLink>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
								<h:outputText value="Size" />
							</f:facet>
							<h:outputText value="#{eachAttach.size}"/>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
		  			    <h:outputText value="Type" />
							</f:facet>
							<h:outputText value="#{eachAttach.type}"/>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
								<h:outputText value="Created by" />
							</f:facet>
							<h:outputText value="#{eachAttach.createdBy}"/>
						</h:column>
					  <h:column rendered="#{!empty SyllabusTool.allAttachments}">
							<f:facet name="header">
								<h:outputText value="Last modified by" />
							</f:facet>
							<h:outputText value="#{eachAttach.lastModifiedBy}"/>
						</h:column>
					</syllabus:syllabus_table>

					<h:panelGroup>
						<h:outputText value="#{msgs.email_notify}"/>
						<h:outputText value="      "/>
						<h:selectOneListbox size = "1"  id = "list1" value="#{SyllabusTool.entry.entry.emailNotification}">
							<f:selectItem itemLabel="#{msgs.notifyNone}" itemValue="none"/>
							<f:selectItem itemLabel="#{msgs.notifyHigh}" itemValue="high"/>
							<f:selectItem itemLabel="#{msgs.notifyLow}" itemValue="low"/>
						</h:selectOneListbox>
					</h:panelGroup>
				</sakai:group_box>

				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processReadPost}"
						value="#{msgs.bar_post}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processReadPreview}"
						value="#{msgs.bar_preview}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processReadSave}"
						value="#{msgs.bar_save_draft}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processReadCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>