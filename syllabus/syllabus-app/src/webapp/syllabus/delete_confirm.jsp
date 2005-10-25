<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>


<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_list}">
		<sakai:view_content>
			<h:form>
		  	<sakai:tool_bar_message value="Deleting Syllabus Items..." /> 
				<table width="100%" align="center">
					<tr>
						<table width="100%" align="center">
							<tr>
								<td align="left" style="font-size: 12pt; color: #8B0000; background-color:#fff; background-position: .3em;background-repeat:no-repeat;border:1px solid #c00;display:block;clear:both;color:#c00;font-size:x-small;margin:5px 0px;padding-left:5px; padding-right:0px; padding-top:5px; padding-bottom:5px" width="25%">
								Are you sure you want to delete the specified item(s)?
								</td>
							</tr>
						</table>
					</tr>
					<tr>
						<sakai:flat_list value="#{SyllabusTool.selectedEntries}" var="eachEntry">
							<h:column>
								<f:facet name="header">
									<h:outputText style="align:left" value="Syllabus Item" />
								</f:facet>
								<h:outputText value="#{eachEntry.entry.title}"/>
							</h:column>
							<h:column>
								<f:facet name="header">
									<h:outputText value="Status"/>
								</f:facet>
								<h:outputText value="#{eachEntry.entry.status}"/>
							</h:column>
							<h:column>
								<f:facet name="header">
									<h:outputText value="Syllabus View" />
								</f:facet>
								<h:outputText value="#{eachEntry.entry.view}"/>
							</h:column>
						</sakai:flat_list>
					</tr>
					<br/>
				</table>
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processDelete}"
						value="   #{msgs.title_delete}   " />
					<sakai:button_bar_item
						action="#{SyllabusTool.processDeleteCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
				