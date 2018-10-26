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
		
		<sakai:view_content>
			<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>      			
			<h:form id="meeting">
		 		<sakai:view_title value="#{msgs.permission_page_title}"/>
				<sakai:doc_section>
				 <h:panelGrid columns="1" styleClass="instruction" style="background:#fff;">
                        <h:outputText value="#{msgs.permission_note_for_view_attend_group}" escape="false" />                                             
                        <h:outputText value="&nbsp;" escape="false" />
                    </h:panelGrid>					
				</sakai:doc_section>
				<div class="table-responsive">
				<h:panelGrid columns="1">
					
					<h:dataTable 
						id="permissions"
				 		value="#{SignupPermissionsUpdateBean.realmItems}"
				 		binding="#{SignupPermissionsUpdateBean.permissionTable}"				 						 		
				 		var="permission" style="width:80%;" 				 		
				 		rowClasses="oddRow,evenRow"
				 		styleClass="signupTable">
						<h:column>
							<f:facet name="header" >
								<h:outputText value="#{msgs.permission_tab_name}" escape="false"/>
							</f:facet>
							<h:outputText value="#{permission.siteTitle}" escape="false" rendered="#{permission.siteLevel}"/>
							<h:outputText value="&nbsp;&nbsp;-&nbsp;#{permission.groupTitle}" escape="false" rendered="#{!permission.siteLevel}"/>						
						</h:column>
						
						<h:column>
							<f:facet name="header" >
								<h:outputText value="#{msgs.permission_tab_realm_scope}" escape="false"/>
							</f:facet>
							<h:outputText value="#{msgs.permission_site_scope}" escape="false" rendered="#{permission.siteLevel}"/>
							<h:outputText value="#{msgs.permission_group_scope}" escape="false" rendered="#{!permission.siteLevel}"/>						
						</h:column>
						
						<h:column>
							<f:facet name="header" >
								<h:outputText value="#{msgs.permission_tab_action}" escape="false"/>
							</f:facet>
							<h:commandButton value="#{msgs.permission_tab_edit_button}" action="#{SignupPermissionsUpdateBean.updatePermission}" disabled="#{!permission.allowedUpd}"/>					
						</h:column>
					</h:dataTable></div>
								
					<sakai:doc_section>
							<h:panelGrid columns="2" styleClass="instruction" style="width:80%;" columnClasses="note,desc">
								<h:outputText value="#{msgs.organizer_note_name}&nbsp;" escape="false" />
								<h:outputText value="#{msgs.permission_note_for_disabled_button_case}" escape="false"/> 																												
							
							<h:outputText value="&nbsp;" escape="false"/>
							<h:outputText value="#{msgs.permission_note_for_group_realm_case}" escape="false"/>
							
							<h:outputText value="&nbsp;" escape="false" rendered="#{SignupPermissionsUpdateBean.admin}"/>
							<h:outputText value="#{msgs.permission_note_for_admin_advice}" escape="false" rendered="#{SignupPermissionsUpdateBean.admin}"/>
							
							</h:panelGrid>
					</sakai:doc_section>										
						
					<sakai:button_bar>					
							<h:commandButton id="goback" action="listMeetings" value="#{msgs.goback_button}"/>					
		            </sakai:button_bar>
		            
	            	<h:outputText value="&nbsp;" escape="false"/>
	            </h:panelGrid>
				
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
	
</f:view>		