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
			<h:outputText value="main_student.jsp" />
				<br/>
	
				<h:dataTable value="#{SyllabusTool.entries}" var="eachEntry">
					<h:column>
						<f:facet name="header">
							<h:outputText style="height: 16px; width=72px" value="#{msgs.mainEditHeaderItem}" />
						</f:facet>
						<h:outputText value="#{eachEntry.entry.title}"/>
						<f:subview id="date" rendered="#{eachEntry.entry.startDate != null || eachEntry.entry.endDate != null}">
							<f:verbatim><br/><span style="font-weight: normal"></f:verbatim>
							<h:outputText value="("/>
							<h:outputText value="#{eachEntry.entry.startDate}">
								<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
							</h:outputText>
							<h:outputText value=" - " rendered="#{eachEntry.entry.startDate != null && eachEntry.entry.endDate != null}"/>
							<h:outputText value="#{eachEntry.entry.endDate}">
						  		<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
							</h:outputText>
							<h:outputText value=")"/>
							<f:verbatim></span></f:verbatim>
						</f:subview>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.syllabus_content}"/>
						</f:facet>
						<h:outputText value="#{eachEntry.entry.content}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.syllabus_url}"/>
						</f:facet>
						<h:outputLink value="#{eachEntry.entry.redirectUrl}" target="newWin">
						  <h:outputText value="#{eachEntry.entry.redirectUrl}" />
						</h:outputLink>
					</h:column>
				</h:dataTable>
				
				<sakai:button_bar>
					<h:commandButton
						action="#{SyllabusTool.processEditCancel}"
						value="#{msgs.back}" 
						accesskey="x" />
				</sakai:button_bar>


		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
