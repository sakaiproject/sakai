<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_list}">
		<sakai:view_content>
			<h:form>
		  	<h3>
					<h:outputText  value="#{msgs.delConfNotice}" />
			</h3>	
			<div class="alertMessage">
				<h:outputText  value="#{msgs.delConfAlert}" />
			</div>
				<sakai:flat_list value="#{SyllabusTool.selectedEntries}" var="eachEntry"  summary="#{msgs.del_conf_listsummary}" styleClass="listHier lines nolines">
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
						<h:outputText value="#{eachEntry.status}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderPublicView}" />
						</f:facet>
						<h:outputText value="#{msgs[eachEntry.entry.view]}"/>
					</h:column>
				</sakai:flat_list>
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processDelete}"
						styleClass="active"
						value="#{msgs.title_delete} "
						accesskey="s" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processDeleteCancel}"
						value="#{msgs.cancel}"
						accesskey="x" />
				</sakai:button_bar>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
				
