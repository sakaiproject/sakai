<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.attachment}">
		<sakai:view_content>
			<h:form>

				<table width="100%" align="center">
					<tr>
					  <td>
					  	<br/>
					 	</td>
					</tr>
					<tr>
						<table width="100%" align="center">
							<tr>
								<td align="center" style="font-size: 12pt; color: #8B0000; background-color:#fff; background-position: .3em;background-repeat:no-repeat;border:1px solid #c00;display:block;clear:both;color:#c00;font-size:x-small;margin:5px 0px;padding-left:25px; padding-right:5px; padding-top:5px; padding-bottom:5px" width="60%">
									Are you sure you want to remove the following attachment from the syllabus?
								</td>
							</tr>
						</table>
					</tr>
					<tr>
						<sakai:group_box>
							<syllabus:syllabus_table value="#{SyllabusTool.prepareRemoveAttach}" var="eachAttach">
						  	<h:column>
									<f:facet name="header">
										<h:outputText value="Title" />
									</f:facet>
									<h:outputText value="#{eachAttach.name}"/>
								</h:column>
							  <h:column>
									<f:facet name="header">
										<h:outputText value="Size" />
									</f:facet>
									<h:outputText value="#{eachAttach.size}"/>
								</h:column>
					  		<h:column>
									<f:facet name="header">
			  				    <h:outputText value="Type" />
									</f:facet>
									<h:outputText value="#{eachAttach.type}"/>
								</h:column>
							  <h:column>
									<f:facet name="header">
										<h:outputText value="Created by" />
									</f:facet>
									<h:outputText value="#{eachAttach.createdBy}"/>
								</h:column>
							  <h:column>
									<f:facet name="header">
										<h:outputText value="Last modified by" />
									</f:facet>
									<h:outputText value="#{eachAttach.lastModifiedBy}"/>
								</h:column>
							</syllabus:syllabus_table>
						</sakai:group_box>
					</tr>
					<tr>
						<table width="100%" align="center">
							<tr>
							  <td width="50%" align="right">
									<h:commandButton 
									  value="    #{msgs.bar_ok}   " 
									  action="#{SyllabusTool.processRemoveAttach}"/>
								</td>
								<td width="50%" align="left">
									<h:commandButton 
									  value="#{msgs.bar_cancel}" 
									  action="#{SyllabusTool.processRemoveAttachCancel}"/>
								</td>
							</tr>
						</table>
					</tr>
				</table>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
				