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

<style>
	.modified { 
	  color: red;
	}
</style>

			<h:form>
		  	<h3>
					<h:outputText  value="#{msgs.delConfNotice}" />
			</h3>	
			<div class="alertMessage">
				<h:outputText  value="#{msgs.delConfAlert}" />
			</div>
				<h:dataTable value="#{SyllabusTool.selectedEntries}" var="eachEntry"  summary="#{msgs.del_conf_listsummary}" styleClass="listHier lines nolines">
					<h:column>
						<f:facet name="header">                                   
							<h:outputText  value="#{msgs.delConfHeaderItem}" />
						</f:facet>
						<h:outputText value="#{eachEntry.entry.title}" rendered="#{!eachEntry.titleChanged}"/>
						<h:outputText value="#{eachEntry.entry.title}" rendered="#{eachEntry.titleChanged}" styleClass="modified"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderStartTime}"/>
						</f:facet>
						<h:outputText value="#{eachEntry.entry.startDate}" rendered="#{!eachEntry.startTimeChanged}">
							<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
						</h:outputText>
						<h:outputText value="#{eachEntry.entry.startDate}" rendered="#{eachEntry.startTimeChanged}" styleClass="modified">
							<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
						</h:outputText>
						<h:outputText value="#{msgs.delConfRemoved}" rendered="#{eachEntry.startTimeChanged && eachEntry.entry.startDate == null}" styleClass="modified"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderEndTime}"/>
						</f:facet>
						<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{!eachEntry.endTimeChanged}">
							<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
						</h:outputText>
						<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{eachEntry.endTimeChanged}" styleClass="modified">
							<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
						</h:outputText>
						<h:outputText value="#{msgs.delConfRemoved}" rendered="#{eachEntry.endTimeChanged && eachEntry.entry.endDate == null}" styleClass="modified"/>
					</h:column>
					<h:column rendered="#{SyllabusTool.calendarExistsForSite}">
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderCalendar}"/>
						</f:facet>
						<h:outputText value="#{eachEntry.entry.linkCalendar ? msgs.yes : msgs.no}" rendered="#{!eachEntry.postToCalendarChanged}"/>
						<h:outputText value="#{eachEntry.entry.linkCalendar ? msgs.yes : msgs.no}" rendered="#{eachEntry.postToCalendarChanged}" styleClass="modified"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderStatus}"/>
						</f:facet>
						<h:outputText value="#{eachEntry.status}" rendered="#{!eachEntry.statusChanged}"/>
						<h:outputText value="#{eachEntry.status}" rendered="#{eachEntry.statusChanged}" styleClass="modified"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.delConfHeaderPublicView}" />
						</f:facet>
						<h:outputText value="#{msgs[eachEntry.entry.view]}"/>
					</h:column>
					<h:column>
						<f:facet name="header">                                   
							<h:outputText  value="#{msgs.delConfHeaderRemove}" />
						</f:facet>
						<f:subview id="deleteImg" rendered="#{eachEntry.selected}">
							<f:verbatim><span class="fa fa-times" onclick="$('.datInputStart').focus();"></span></f:verbatim>
						</f:subview>
					</h:column>
				</h:dataTable>
				<sakai:button_bar>
					<h:commandButton
						action="#{SyllabusTool.processDelete}"
						styleClass="active"
						value="#{msgs.update} "
						accesskey="s" />
					<h:commandButton
						action="#{SyllabusTool.processDeleteCancel}"
						value="#{msgs.cancel}"
						accesskey="x" />
				</sakai:button_bar>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
				
