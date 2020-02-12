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
		<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
		<script src="/sakai-signup-tool/js/signupScript.js"></script>
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/> 
			<h:form id="meeting">
				<div class="page-header">
					<sakai:view_title value="#{msgs.event_edit_comment_page_title}"/>
				</div>
				<div class="table-responsive">
				<h:panelGrid columns="2" columnClasses="titleColumn,valueColumn" style="margin-top:20px;">
					<h:outputText value="#{msgs.event_name}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.meetingWrapper.meeting.title}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_location}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.meetingWrapper.meeting.location}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_attendee_name}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeWrapper.displayName}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_attendee_eid}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeEid}" styleClass="longtext"/>
					
					<h:outputText value="#{msgs.event_attendee_role}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeRole}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_appointment_period}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.attendeeWrapper.timeslotPeriod}" styleClass="longtext" escape="false"/>
					
					<h:outputText value="#{msgs.event_appointment_date}" styleClass="titleText" escape="false"/>
					<h:outputText value="#{EditCommentSignupMBean.meetingWrapper.meeting.startTime}" styleClass="longtext">
					 	<f:convertDateTime dateStyle="full" timeZone="#{UserTimeZone.userTimeZone}"/>
					</h:outputText>				
					
					<h:outputText value="&nbsp;" escape="false"/>
					<h:outputText value="&nbsp;" escape="false"/>
					
					<h:outputText value="#{msgs.event_comment}" styleClass="titleText" escape="false"/>
					<sakai:rich_text_area value="#{EditCommentSignupMBean.comment}" width="720" height="200" rows="5"  columns="80"/>
				   	
					<h:outputText value="#{msgs.event_email_notification}" styleClass="titleText" escape="false"/>
					<h:panelGrid columns="1" style="width:100%;margin:-3px 0 0 -3px;" rendered="#{EditMeetingSignupMBean.publishedSite}">
						<h:panelGroup styleClass="editText" >
							<h:selectBooleanCheckbox id="emailChoice" value="#{EditCommentSignupMBean.sendEmail}" onclick="isShowEmailChoice()"/>
							<h:outputText value="#{EditCommentSignupMBean.userType}" escape="false"/>
						</h:panelGroup>
					</h:panelGrid>
				</h:panelGrid>
				</div>
				<sakai:button_bar>
					<h:commandButton id="save" action="#{EditCommentSignupMBean.attendeeSaveComment}" value="#{msgs.save_button}"/> 
					<h:commandButton id="Cancel" action="#{EditCommentSignupMBean.checkReturnUrl}" value="#{msgs.cancel_button}"  immediate="true"/>  
                </sakai:button_bar>

			 </h:form>
  		</sakai:view_content>	
	</sakai:view_container>
</f:view> 
