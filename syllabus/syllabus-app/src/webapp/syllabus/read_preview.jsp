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

		  	<sakai:tool_bar_message value="#{msgs.previewNotice}" /> 
		  	        
 				<sakai:group_box>
					<table width="100%">
						<tr>
							<td width="100%" align="center" style="FONT-WEIGHT: bold; FONT-SIZE: 12">
								<h:outputText value="#{SyllabusTool.entry.entry.title}"/>
							</td>
						</tr>
						<tr>
							<td width="100%" align="left">
								<syllabus:syllabus_htmlShowArea value="#{SyllabusTool.entry.entry.asset}" />
							</td>
						</tr>
					</table>
				</sakai:group_box>

				<sakai:group_box>
					<h:dataTable value="#{SyllabusTool.allAttachments}" var="eachAttach">
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
								<h:outputText value="#{eachAttach.name}" style="text-decoration:underline;"/>
							</h:outputLink>
						</h:column>
					</h:dataTable>
				</sakai:group_box>
				
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processReadPreviewBack}"
						value="Back" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>