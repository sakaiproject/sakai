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
					<h:outputLink id="print" value="javascript:window.print();" style="vertical-align:bottom;">
						<h:outputText value="#{msgs.print_event}" escape="false"/>
					</h:outputLink>
				</sakai:tool_bar>
			</h:form>
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>      			
				
			<h:form id="meeting">
			 	<sakai:view_title value="#{msgs.event_view_comment_page_title}"/>
				<div class="table-responsive">
				<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn">
				
					<h:outputText value="#{msgs.event_name}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.meetingWrapper.meeting.title}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_location}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.meetingWrapper.meeting.location}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_attendee_name}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeWrapper.displayName}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_attendee_eid}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeEid}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_attendee_role}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeRole}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_appointment_period}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeWrapper.timeslotPeriod}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_appointment_date}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.meetingWrapper.meeting.startTime}" styleClass="longtext">
					 	<f:convertDateTime dateStyle="full" timeZone="#{UserTimeZone.userTimeZone}"/>
					</h:outputText>	
					
					<h:outputText value="&nbsp" escape="false"/>
					<h:outputText value="&nbsp" escape="false"/>
									
					<h:outputText value="#{msgs.event_comment}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.comment}" escape="false" styleClass="longtext" rendered="#{EditCommentSignupMBean.comment !=null}"/>
					<h:outputText value="#{msgs.event_no_comment_available}" escape="false" styleClass="longtext" rendered="#{EditCommentSignupMBean.comment ==null}"/>																	
						
				</h:panelGrid></div>
															
				<sakai:button_bar>
					<h:commandButton id="save" action="#{EditCommentSignupMBean.editAttendeeComment}" value="#{msgs.edit_button}"/> 			
					<h:commandButton id="Back" action="organizerMeeting" value="#{msgs.goback_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>

</f:view> 
