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
<sakai:view title="#{msgs.title_status}" toolCssHref="/sakai-roster-tool/css/roster.css">		
	<%="<script src=js/roster.js></script>"%>

			<h:form id="roster_form">

				<t:aliasBean alias="#{viewBean}" value="#{status}">
					<%@include file="inc/nav.jspf" %>
				</t:aliasBean>


				<%-- Initialize the filter --%>
				<h:outputText value="#{enrollmentStatusFilter.init}"/>

				<h:panelGrid columns="2" columnClasses="filterLabel,filterElement">
						<h:outputLabel for="sectionFilter" value="#{msgs.enrollment_status_filter}"/>
	        		    <h:selectOneMenu
							id="sectionFilter"
							value="#{enrollmentStatusFilter.sectionFilter}"
							rendered="#{enrollmentStatusFilter.multipleEnrollmentSetsDisplayed}"
							onchange="this.form.submit()"
							immediate="true">
	        		    	<f:selectItems value="#{enrollmentStatusFilter.enrollmentSetSelectItems}"/>
	        		   	</h:selectOneMenu>
	        		   	<h:outputText value="#{enrollmentStatusFilter.firstEnrollmentSetTitle}" rendered="#{ ! enrollmentStatusFilter.multipleEnrollmentSetsDisplayed}" />

						<h:outputLabel for="statusFilter" value="#{msgs.enrollment_status_filter_label}"/>
	        		    <h:selectOneMenu id="statusFilter" value="#{enrollmentStatusFilter.statusFilter}" onchange="this.form.submit()" immediate="true">
	        		    	<f:selectItem itemLabel="#{msgs.roster_enrollment_status_all}" itemValue="#{enrollmentStatusFilter.allStatus}"/>
	        		    	<f:selectItems value="#{enrollmentStatusFilter.statusSelectItems}"/>
	        		   	</h:selectOneMenu>
		        </h:panelGrid>

				<h:panelGrid columns="2">
					<h:panelGroup>
	    		        <h:inputText id="search" value="#{enrollmentStatusFilter.searchFilterString}"
	        		        onfocus="clearIfDefaultString(this, '#{msgs.roster_search_text}')"/>
	        		    <h:commandButton value="#{msgs.roster_search_button}" actionListener="#{enrollmentStatusFilter.search}"/>
	        		    <h:commandButton value="#{msgs.roster_clear_button}" actionListener="#{enrollmentStatusFilter.clearSearch}"/>
                    </h:panelGroup>
	        		    
                    <h:outputText value="#{enrollmentStatusFilter.currentlyDisplayingMessage}" styleClass="instruction" />
                </h:panelGrid>

                <t:dataTable cellpadding="0" cellspacing="0"
                             id="rosterTable"
                             value="#{status.participants}"
                             var="participant"
                             sortColumn="#{enrollmentStatusPrefs.sortColumn}"
                             sortAscending="#{enrollmentStatusPrefs.sortAscending}"
                             styleClass="listHier rosterTable">
                    <h:column>
                        <f:facet name="header">
                            <t:commandSortHeader columnName="sortName" immediate="true" arrow="true">
                                <h:outputText value="#{msgs.facet_name}" />
                            </t:commandSortHeader>
                        </f:facet>
                        <h:commandLink action="#{profileBean.displayProfile}" value="#{participant.user.sortName}" title="#{msgs.show_profile}" rendered="#{status.renderProfileLinks}">
                            <f:param name="participantId" value="#{participant.user.id}" />
                            <f:param name="returnPage" value="status" />
                        </h:commandLink>
                        <h:outputText value="#{participant.user.sortName}" rendered="#{ ! status.renderProfileLinks}" />
                    </h:column>
                    <h:column>
                        <f:facet name="header">
                            <t:commandSortHeader columnName="displayId" immediate="true" arrow="true">
                                <h:outputText value="#{msgs.facet_userId}" />
                            </t:commandSortHeader>
                        </f:facet>
                        <h:outputText value="#{participant.user.displayId}"/>
                    </h:column>
                    <h:column rendered="#{status.emailColumnRendered}">
                        <f:facet name="header">
                            <t:commandSortHeader columnName="email" immediate="true" arrow="true">
                                <h:outputText value="#{msgs.facet_email}" />
                            </t:commandSortHeader>
                        </f:facet>
                        <h:outputLink value="mailto:#{participant.user.email}"><h:outputText value="#{participant.user.email}"/></h:outputLink>
                    </h:column>
			        <h:column>
			            <f:facet name="header">
			                <t:commandSortHeader columnName="status" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_status}" />
			                </t:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.enrollmentStatus}"/>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <t:commandSortHeader columnName="credits" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_credits}" />
			                </t:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.enrollmentCredits}"/>
			        </h:column>
			    
			    </t:dataTable>


                 <%-- Messages to display when there are no participants in the table above --%>
            <t:div styleClass="instruction">      		      		
                <h:outputFormat value="#{msgs.no_participants_msg}" rendered="#{empty filter.participants}" >
                    <f:param value="#{filter.searchFilterString}"/>
                </h:outputFormat>
            </t:div>

            </h:form>
</sakai:view>
</f:view>
