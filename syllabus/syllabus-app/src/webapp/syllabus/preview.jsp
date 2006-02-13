<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container>
		<sakai:view_content>
			<h:form>
			<h:outputText value="preview.jsp" />
		  	<sakai:tool_bar_message value="#{msgs.previewNotice}" /> 
				<h4><h:outputText value="#{SyllabusTool.entry.entry.title}"/></h4>
				<div class="indnt1">
					<syllabus:syllabus_htmlShowArea value="#{SyllabusTool.entry.entry.asset}" />
				</div>	
					<h:dataTable value="#{SyllabusTool.attachments}" var="eachAttach">
					  <h:column>
							<f:facet name="header">
								<h:outputText value="" />
							</f:facet>
							<h:graphicImage url="/syllabus/excel.gif" rendered="#{eachAttach.type == 'application/vnd.ms-excel'}"/>
							<h:graphicImage url="/syllabus/html.gif" rendered="#{eachAttach.type == 'text/html'}"/>
							<h:graphicImage url="/syllabus/pdf.gif" rendered="#{eachAttach.type == 'application/pdf'}"/>
							<h:graphicImage url="/syllabus/ppt.gif" rendered="#{eachAttach.type == 'application/vnd.ms-powerpoint'}"/>
							<h:graphicImage url="/syllabus/text.gif" rendered="#{eachAttach.type == 'text/plain'}"/>
							<h:graphicImage url="/syllabus/word.gif" rendered="#{eachAttach.type == 'application/msword'}"/>
							
							<h:outputLink value="#{eachAttach.url}" target="_new_window">
								<h:outputText value="#{eachAttach.name}" />
							</h:outputLink>
						</h:column>
					</h:dataTable>
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditPreviewBack}"
						value="#{msgs.back}" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
