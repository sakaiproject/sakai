<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<f:view locale="#{UserLocale.locale}">
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	   <jsp:setProperty name="msgs" property="baseName" value="messages"/>
	</jsp:useBean>
	<sakai:view_container title="Signup Tool">
			<style type="text/css">
				@import url("/sakai-signup-tool/css/signupStyle.css");
			</style>
			<style type="text/css" media="print">
				@import url("/sakai-signup-tool/css/print.css");
			</style>
		
			<h:form id="viewComment">
				<sakai:tool_bar>
					<h:outputLink id="print" value="javascript:window.print();">
						<h:graphicImage url="/images/printer.png"
							alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					</h:outputLink>
				</sakai:tool_bar>
			</h:form>
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{errorMessageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{errorMessageUIBean.error}"/>      			
				
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.event_view_comment_page_title}"/>
											
				<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn">
				
					<h:outputText value="#{msgs.event_name}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{ViewCommentSignupMBean.meetingWrapper.meeting.title}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_location}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{ViewCommentSignupMBean.meetingWrapper.meeting.location}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_attendee_name}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{ViewCommentSignupMBean.attendeeWraper.displayName}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_attendee_role}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{ViewCommentSignupMBean.attendeeRole}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_appointment_period}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{ViewCommentSignupMBean.attendeeWraper.timeslotPeriod}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_appointment_date}" styleClass="titleText" escape="false"/>
					<h:panelGroup>
						<h:outputText value="#{ViewCommentSignupMBean.meetingWrapper.meeting.startTime}" styleClass="longtext">
						 	<f:convertDateTime pattern="EEEEEEEE, " />
						</h:outputText>
						<h:outputText value="#{ViewCommentSignupMBean.meetingWrapper.meeting.startTime}" styleClass="longtext">
						 	<f:convertDateTime dateStyle="long" />
						</h:outputText>
					</h:panelGroup>	
					
					<h:outputText value="&nbsp" escape="false"/>
					<h:outputText value="&nbsp" escape="false"/>
									
					<h:outputText value="#{msgs.event_comment}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{ViewCommentSignupMBean.attendeeWraper.signupAttendee.comments}" escape="false" styleClass="longtext" rendered="#{ViewCommentSignupMBean.attendeeWraper.signupAttendee.comments !=null}"/>
					<h:outputText value="#{msgs.event_no_comment_available}" escape="false" styleClass="longtext" rendered="#{ViewCommentSignupMBean.attendeeWraper.signupAttendee.comments ==null}"/>																	
						
				</h:panelGrid>
															
				<sakai:button_bar>			
					<sakai:button_bar_item id="Back" action="organizerMeeting" value="#{msgs.goback_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>

</f:view> 
