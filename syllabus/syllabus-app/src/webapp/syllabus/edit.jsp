<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>


<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<h:form>

		  	<sakai:tool_bar_message value="#{msgs.add_sylla}" /> 
		  	        
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
						<sakai:rich_text_area value="#{SyllabusTool.entry.entry.asset}" rows="17" columns="80" javascriptLibrary="/library/htmlarea"/>
						
 						
<%--						<h:outputText value="#{msgs.syllabus_view}"/>
						<h:selectOneRadio id="chooseCreatSyllaType" value="#{SyllabusTool.entry.entry.view}" layout="pageDirection">
			 				<f:selectItem itemLabel="#{msgs.yes}" itemValue="yes"/>
					 		<f:selectItem itemLabel= "#{msgs.no}" itemValue="no"/>
						</h:selectOneRadio>--%>
						
					</sakai:panel_edit>

				</sakai:group_box>

				<h:panelGroup>
					<h:outputText value="#{msgs.email_notify}"/>
					<h:outputText value="      "/>
					<h:selectOneListbox size = "1"  id = "list1" value="#{SyllabusTool.entry.entry.emailNotification}">
						<f:selectItem itemLabel="#{msgs.notifyNone}" itemValue="none"/>
						<f:selectItem itemLabel="#{msgs.notifyHigh}" itemValue="high"/>
						<f:selectItem itemLabel="#{msgs.notifyLow}" itemValue="low"/>
					</h:selectOneListbox>
				</h:panelGroup>

				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditPost}"
						value="#{msgs.bar_post}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditPreview}"
						value="#{msgs.bar_preview}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditSave}"
						value="#{msgs.bar_save_draft}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>
		 </h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view> 