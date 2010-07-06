<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%
response.setContentType("text/html; charset=UTF-8");
%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.roster.bundle.Messages"/>
</jsp:useBean>
<f:view>

<sakai:view title="#{msgs.facet_roster_list}" toolCssHref="/sakai-roster-tool/css/roster.css">
	<%="<script src=\"js/roster.js\"></script>"%>
		<h:form id="roster_form">
			<t:aliasBean alias="#{viewBean}" value="#{groupMembership}">
				<%@include file="inc/nav.jspf" %>
			</t:aliasBean>

			<h:outputText value="#{msgs.title_msg_groups}"
				rendered="#{groupMembership.renderModifyMembersInstructions}" styleClass="instruction"
				style="display: block;" />

            <%@include file="inc/groupsFilter.jspf" %>
			
			<t:dataTable cellpadding="0" cellspacing="0"
                         id="rosterTable"
                         value="#{groupMembership.participants}"
                         var="participant"
                         sortColumn="#{prefs.sortColumn}"
                         sortAscending="#{prefs.sortAscending}"
                         styleClass="listHier rosterTable"
                         rendered="#{!groupMembership.groupedBy}">
                <h:column>
                    <f:facet name="header">
                        <t:commandSortHeader columnName="sortName" immediate="true" arrow="true">
                            <h:outputText value="#{msgs.facet_name}" />
                        </t:commandSortHeader>
                    </f:facet>
                    <h:commandLink action="#{profileBean.displayProfile}" value="#{participant.user.sortName}" title="#{msgs.show_profile}" rendered="#{groupMembership.renderProfileLinks}">
                        <f:param name="participantId" value="#{participant.user.id}" />
                        <f:param name="returnPage" value="groupMembership" />
                    </h:commandLink>
                    <h:outputText value="#{participant.user.sortName}" rendered="#{ ! groupMembership.renderProfileLinks}" />
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <t:commandSortHeader columnName="displayId" immediate="true" arrow="true">
                            <h:outputText value="#{msgs.facet_userId}" />
                        </t:commandSortHeader>
                    </f:facet>
                    <h:outputText value="#{participant.user.displayId}"/>
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <t:commandSortHeader columnName="role" immediate="true" arrow="true">
                            <h:outputText value="#{msgs.facet_role}" />
                        </t:commandSortHeader>
                    </f:facet>
                    <h:outputText value="#{participant.roleTitle}"/>
                </h:column>
		        <h:column>
                    <f:facet name="header">
	                    <h:outputText value="#{msgs.facet_groups}" />
                    </f:facet>
                    <h:outputFormat value="#{participant.groupsString}" title="#{participant.groupsString}">
                    	<f:converter converterId="groupTextTruncateConverter"/>
                    </h:outputFormat>
                </h:column>
            </t:dataTable>
            
            <t:dataTable cellpadding="0" cellspacing="0"
                         id="groupTable"
                         value="#{groupMembership.groupedParticipants}"
                         var="groupTable"
                         width="100%"
                         rendered="#{groupMembership.groupedBy}">
                <t:column rendered="#{groupTable.groupedParticipantCount >= 1}">
                	<t:panelGrid columns="2" width="100%">
	                	<sakai:view_title value="#{groupTable.groupTitle}" />
	                	
	                	<t:panelGroup styleClass="instruction" style="text-align: right" rendered="#{groupMembership.groupedBy}">    
					        <t:div style="padding-left:10px;" rendered="#{filter.participantCount > 0}">
					             <h:outputFormat value="#{msgs.currently_displaying_participants}" rendered="#{groupTable.groupedParticipantCount >= 1}">
					                <f:param value="#{groupTable.groupedParticipantCount}" />
					            </h:outputFormat>
					        </t:div>
					
					        <t:div style="padding-left:10px;" rendered="#{groupTable.groupedParticipantCount > 1 }">
					            <h:outputText value="#{groupTable.roleCountMessage}" />
					        </t:div>
					    </t:panelGroup>
					</t:panelGrid>
                	<t:dataTable cellpadding="0" cellspacing="0"
                		id="rosterTable2"
                		value="#{groupTable.groupedParticipants}"
                		var="participant"
                		sortColumn="#{prefs.sortColumn}"
                        sortAscending="#{prefs.sortAscending}"
                        styleClass="listHier rosterTable">
		                <t:column width="25%">
		                    <f:facet name="header">
		                        <t:commandSortHeader columnName="sortName" immediate="true" arrow="true">
		                            <h:outputText value="#{msgs.facet_name}" />
		                        </t:commandSortHeader>
		                    </f:facet>
		                    <h:commandLink action="#{profileBean.displayProfile}" value="#{participant.user.sortName}" title="#{msgs.show_profile}" rendered="#{groupMembership.renderProfileLinks}">
		                        <f:param name="participantId" value="#{participant.user.id}" />
		                        <f:param name="returnPage" value="groupMembership" />
		                    </h:commandLink>
		                    <h:outputText value="#{participant.user.sortName}" rendered="#{ ! groupMembership.renderProfileLinks}" />
		                </t:column>
		                <t:column width="10%">
		                    <f:facet name="header">
		                        <t:commandSortHeader columnName="displayId" immediate="true" arrow="true">
		                            <h:outputText value="#{msgs.facet_userId}" />
		                        </t:commandSortHeader>
		                    </f:facet>
		                    <h:outputText value="#{participant.user.displayId}"/>
		                </t:column>
		                <t:column width="10%">
		                    <f:facet name="header">
		                        <t:commandSortHeader columnName="role" immediate="true" arrow="true">
		                            <h:outputText value="#{msgs.facet_role}" />
		                        </t:commandSortHeader>
		                    </f:facet>
		                    <h:outputText value="#{participant.roleTitle}"/>
		                </t:column>
				        <t:column width="55%">
		                    <f:facet name="header">
			                    <h:outputText value="#{msgs.facet_groups}" />
		                    </f:facet>
		                    <h:outputFormat value="#{participant.groupsString}" title="#{participant.groupsString}">
		                    	<f:converter converterId="groupTextTruncateConverter"/>
		                    </h:outputFormat>
		                </t:column>
	                </t:dataTable>
	            </t:column>
            </t:dataTable>

      <%-- Messages to display when there are no participants in the table above --%>
      <t:div styleClass="instruction">

      			<%-- No filtering --%>
                <h:outputText value="#{msgs.no_participants}" rendered="#{empty filter.participants && filter.searchFilterString eq filter.defaultSearchText && empty filter.sectionFilterTitle}" />

      			<%-- Filtering on section, but not user --%>
                <h:outputFormat value="#{msgs.no_participants_msg}" rendered="#{empty filter.participants && filter.searchFilterString eq filter.defaultSearchText && not empty filter.sectionFilterTitle}" >
                     <f:param value="#{filter.sectionFilterTitle}"/>
                </h:outputFormat>

      			<%-- Filtering on user, but not section --%>
                <h:outputFormat value="#{msgs.no_participants_msg}" rendered="#{empty filter.participants &&  filter.searchFilterString != filter.defaultSearchText && empty filter.sectionFilterTitle}" >
                    <f:param value="#{filter.searchFilterString}"/>                    
                </h:outputFormat>

      			<%-- Filtering on section and user --%>
                <h:outputFormat value="#{msgs.no_participants_in_section}" rendered="#{empty filter.participants &&  filter.searchFilterString != filter.defaultSearchText && not empty filter.sectionFilterTitle}" >
                    <f:param value="#{filter.searchFilterString}"/>
                    <f:param value="#{filter.sectionFilterTitle}"/>
                </h:outputFormat>

            </t:div>
            
        </h:form>
</sakai:view>

</f:view>
