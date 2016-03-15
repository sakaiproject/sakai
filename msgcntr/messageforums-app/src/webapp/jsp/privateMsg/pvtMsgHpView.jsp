<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view title="#{msgs.cdfm_message_pvtarea}">
		<!--jsp/privateMsg/pvtMsgHpView.jsp-->
		<h:form id="msgForum">
		
<%  
  /** initialize user's private message area per request **/
  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{PrivateMessagesTool}");
  PrivateMessagesTool pmt = (PrivateMessagesTool) binding.getValue(context);
  
  if(pmt.getUserId() != null){
  //show entire page, otherwise, don't allow anon user to use this tool:
  pmt.initializePrivateMessageArea();
%>



		       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>


			<sakai:tool_bar  rendered="#{PrivateMessagesTool.messages}">
  			<sakai:tool_bar_item value="#{msgs.pvt_compose}" action="#{PrivateMessagesTool.processPvtMsgCompose}"  rendered="#{! PrivateMessagesTool.dispError}" />
  			<sakai:tool_bar_item value=" #{msgs.pvt_newfolder}" action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}"  rendered="#{! PrivateMessagesTool.dispError}" />
 			<sakai:tool_bar_item value=" #{msgs.pvt_settings}" action="#{PrivateMessagesTool.processPvtMsgSettings}" rendered="#{PrivateMessagesTool.showSettingsLink}" />
 			<sakai:tool_bar_item value=" #{msgs.pvt_permissions}" action="#{PrivateMessagesTool.processActionPermissions}" rendered="#{PrivateMessagesTool.instructor}" />
 			</sakai:tool_bar>
 			
			<h:panelGroup>
				<f:verbatim>
					<div class="page-header">
					<h1>
				</f:verbatim>
				<h:commandLink action="#{PrivateMessagesTool.processActionHome}"
					value="#{msgs.cdfm_message_forums}"
					title=" #{msgs.cdfm_message_forums}" 
					rendered="#{PrivateMessagesTool.messagesandForums}" />
				<h:outputText value=" /" rendered="#{PrivateMessagesTool.messagesandForums}" />
				<h:outputText value=" #{msgs.pvt_message_nav}" />
				<f:verbatim>
					</h1>
					</div>
				</f:verbatim>
			</h:panelGroup>

			<h:panelGroup styleClass="itemNav" rendered="#{PrivateMessagesTool.messagesandForums}" >
				<h:commandLink action="#{PrivateMessagesTool.processPvtMsgCompose}"
					title=" #{msgs.pvt_compose}">
					<h:outputText value="#{msgs.pvt_compose}" />
				</h:commandLink>

				<h:outputText value=" | " rendered="#{ForumTool.selectedForum.changeSettings}" />

				<h:commandLink
					action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}"
					title=" #{msgs.pvt_newfolder}" styleClass="button">
					<h:outputText value="#{msgs.pvt_newfolder}" />
				</h:commandLink>
			</h:panelGroup>

			<h:outputText value=" " rendered="#{PrivateMessagesTool.messages}" />
			
			<h:panelGroup rendered="#{PrivateMessagesTool.messages && PrivateMessagesTool.dispError}" >
			  <f:verbatim><br /></f:verbatim>
 			  <h:outputText value="#{msgs.pvt_hlprpgerror}" styleClass="bs-callout-danger" />
			</h:panelGroup>

			<div class="table-responsive">
			
			<h:dataTable value="#{PrivateMessagesTool.decoratedForum}"
				var="forum" rendered="#{PrivateMessagesTool.pvtAreaEnabled}"
				cellpadding="0" cellspacing="0" styleClass="hierItemBlockWrapper" style="width: 100%;">
				<h:column>

					<h:dataTable id="privateForums" value="#{forum.topics}" var="topic" styleClass="table table-hover table-striped table-bordered" >
						<h:column>

							<!-- h:panelGrid columns="2" styleClass="table table-hover table-striped table-bordered" -->
								           
				
								
								<h:panelGroup rendered="#{!topic.topic.mutable}">
									<h:graphicImage style="margin-right:0.2em; vertical-align:top;" url="/images/dir_closed.gif" alt="" />
									<h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}"
										             immediate="true" title=" #{msgs[topic.topic.title]}">
										<h:outputText value="  " />
										<h:outputText value="#{msgs[topic.topic.title]}" />
										<f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId" />
									</h:commandLink>
									<h:outputText	value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msg}"
										            rendered="#{topic.totalNoMessages < 2}"
										            styleClass="textPanelFooter" />
									<h:outputText	value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msgs}"
										            rendered="#{topic.totalNoMessages > 1}"
										            styleClass="textPanelFooter" />
									<h:outputText	value=" - #{topic.unreadNoMessages} #{msgs.pvt_unread}"
										            rendered="#{topic.topic.title != 'pvt_sent'}"
										            styleClass="textPanelFooter" />
									<h:outputText value=" #{msgs.cdfm_closeb}" styleClass="textPanelFooter" />
								</h:panelGroup>
								
				
							
								
								<h:panelGroup rendered="#{!topic.topic.mutable}">
									<h:outputText value=" " />
								</h:panelGroup>
								<h:panelGroup rendered="#{topic.topic.mutable}">
									<h:graphicImage style="margin-right:0.2em; vertical-align:top;" url="/images/dir_closed.gif" alt="" />
									<h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}"
										             immediate="true" title=" #{topic.topic.title}">

										<h:outputText value="  " />
										<h:outputText value="#{topic.topic.title}" />
										<f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId" />
									</h:commandLink>
								
									<h:outputText	value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msg}"
										            rendered="#{topic.totalNoMessages < 2}"
										            styleClass="textPanelFooter" />
									<h:outputText	value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs}"
										            rendered="#{topic.totalNoMessages > 1}"
										            styleClass="textPanelFooter" />
									<h:outputText value=" - #{topic.unreadNoMessages} #{msgs.pvt_unread}"
										            rendered="#{topic.topic.title != 'Sent'}"
										            styleClass="textPanelFooter" />
									<h:outputText value=" #{msgs.cdfm_closeb}"
										            styleClass="textPanelFooter" />
								</h:panelGroup>
								<h:panelGroup styleClass="itemAction msgNav"
									            rendered="#{topic.topic.mutable}">
									<h:commandLink action="#{PrivateMessagesTool.processPvtMsgFolderSettings}"
										             title=" #{msgs.pvt_foldersettings}">
										<h:outputText value="#{msgs.pvt_foldersettings}" />
										<f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId" />
									</h:commandLink>
								</h:panelGroup>
					
							<!-- /h:panelGrid -->

						</h:column>
					</h:dataTable>
				</h:column>
			</h:dataTable>

			</div>

			<h:inputHidden id="mainOrHp" value="pvtMsgHpView" />


<%
}else{
//user is an anon user, just show a message saying they can't use this tool:
%>

<h:outputText value="#{msgs.pvt_anon_warning}" styleClass="information"/>

<%
}
%>
		</h:form>
	</sakai:view>
</f:view>