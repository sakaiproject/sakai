<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_list}">
		<sakai:view_content>
			<h:form>
		  	<h3>
					<h:outputText  value="#{msgs.delConfNotice}" />
			</h3>	
			<div class="alertMessage">
				<h:outputText  value="#{msgs.delConfAlert}" />
			</div>
				<%-- (gsilver) cannot pass a needed summary atribute to these next items --%>
				<sakai:flat_list value="#{SyllabusTool.selectedEntries}" var="eachEntry" >
					<h:column>
						<f:facet name="header">
							<h:outputText  value="#{msgs.delConfHeaderItem}" />
						</f:facet>
						<h:outputText value="#{eachEntry.entry.title}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderStatus}"/>
						</f:facet>
						<h:outputText value="#{eachEntry.entry.status}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderPublicView}" />
						</f:facet>
						<h:outputText value="#{eachEntry.entry.view}"/>
					</h:column>
				</sakai:flat_list>
				<sakai:button_bar>
				<%-- (gsilver) cannot pass a needed title atribute to these next items --%>
					<sakai:button_bar_item
						action="#{SyllabusTool.processDelete}"
						value="#{msgs.title_delete} " />
					<sakai:button_bar_item
						action="#{SyllabusTool.processDeleteCancel}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
				
