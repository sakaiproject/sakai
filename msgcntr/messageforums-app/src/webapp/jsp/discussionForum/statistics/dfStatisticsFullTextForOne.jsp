<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
  <sakai:view>
  
  <script language="JavaScript">
	function printFriendly(url) {
		window.open(url,'mywindow','width=960,height=1100'); 		
	}
</script>
 
  	<h:form id="dfStatisticsForm">
  	       	<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
  	
  		<sakai:tool_bar>				
			<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyFullTextForOne}');" title="#{msgs.cdfm_print}">
				<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
			</h:outputLink>
  		</sakai:tool_bar>
  	
          	 <f:verbatim><div class="breadCrumb"><h3></f:verbatim>
			 <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			 <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			 <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			 <h:commandLink action="#{ForumTool.processActionStatistics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}"/>
			 <f:verbatim><h:outputText value="" /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			 <h:commandLink action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.selectedSiteUser}">
			 </h:commandLink>
			 <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			 <h:commandLink action="#{ForumTool.processActionShowFullTextForAll}" value="#{msgs.stat_authored}">
			 </h:commandLink>
			 <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			  <h:outputText value="#{mfStatisticsBean.selectedMsgSubject}" />
			  <f:verbatim></h3></div></f:verbatim>
  
  		<h:dataTable id="subjectBody" value="#{mfStatisticsBean.userSubjectMsgBody}" var="stat" styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   			<h:column rendered="#{!stat.msgDeleted}">
   				<f:verbatim><div class="hierItemBlock"></f:verbatim>
				<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
				<f:verbatim><div class="specialLink" style="width:65%;float:left;text-align:left"></f:verbatim>
				<h:panelGroup>
					<h:outputText value="#{stat.forumTitle}" />
					 <f:verbatim><h:outputText value="/" /></f:verbatim>
					<h:outputText value="#{stat.topicTitle}" />
					 <f:verbatim><h:outputText value="/" /></f:verbatim>
					<h:outputText  value= "#{stat.forumSubject} " />
					<h:outputText value="#{stat.forumDate}">
						<f:convertDateTime pattern="#{msgs.date_format_paren}" />
					</h:outputText>
				<f:verbatim></div ></f:verbatim>
				<f:verbatim><div style="width:30%;float:right;text-align:right"	class="specialLink"></f:verbatim>
				</h:panelGroup>
			
				<h:panelGroup>
					<h:commandLink action="#{ForumTool.processActionDisplayInThread}" title=" #{msgs.stat_display_in_thread}" >
						<f:param value="#{stat.forumId}" name="forumId"/>
  				  		<f:param value="#{stat.topicId}" name="topicId"/>
  				  		<f:param value="#{stat.msgId}" name="msgId"/>
						<h:outputText value="#{msgs.stat_display_in_thread}" />
					</h:commandLink>
				</h:panelGroup>
			
				<f:verbatim></div ></f:verbatim>
				<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
				<f:verbatim></h4></f:verbatim>
				<mf:htmlShowArea value="#{stat.message}" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
  			</h:column>
  			
  			<h:column rendered="#{stat.msgDeleted}">
   				<f:verbatim><div class="hierItemBlock"></f:verbatim>
				<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
				<f:verbatim><div class="specialLink" style="width:65%;float:left;text-align:left"></f:verbatim>
				<h:panelGroup>
					<h:panelGroup styleClass="inactive">
						<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
						<f:verbatim></span></f:verbatim>
 					</h:panelGroup>
				<f:verbatim></div ></f:verbatim>
				<f:verbatim><div style="width:30%;float:right;text-align:right"	class="specialLink"></f:verbatim>
				</h:panelGroup>
				<h:panelGroup>
					<h:commandLink action="#{ForumTool.processActionDisplayInThread}" title=" #{msgs.stat_display_in_thread}" >
							<f:param value="#{stat.forumId}" name="forumId"/>
  				  			<f:param value="#{stat.topicId}" name="topicId"/>
  				  			<f:param value="#{stat.msgId}" name="msgId"/>
						<h:outputText value="#{msgs.stat_display_in_thread}" />
					</h:commandLink>
				</h:panelGroup>
			
				<f:verbatim></div ></f:verbatim>
				<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
				<f:verbatim></h4></f:verbatim>
				<mf:htmlShowArea value="" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
  			</h:column>
  			
  		</h:dataTable>

  		<br /><br />
  		<h:panelGroup>
  			<h:commandButton action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.buttonUserName}"  
			               title="#{mfStatisticsBean.buttonUserName}">			               			
			</h:commandButton>
		</h:panelGroup>
  	</h:form>
  </sakai:view>
 </f:view>