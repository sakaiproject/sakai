<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>

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

				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processReadPreviewBack}"
						value="Back" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>