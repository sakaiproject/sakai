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
       	<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
  	
  		<sakai:tool_bar>
			<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyDisplayInThread}');" title="#{msgs.cdfm_print}">
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
		<h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
		<h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
		<f:verbatim></h3></div></f:verbatim>
          	  
  		<mf:hierDataTable id="allMessagesForOneTopic" value="#{ForumTool.messages}" var="msgDecorateBean" noarrows="true" styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   			<h:column id="_msg_subject" >
   			<h:panelGroup rendered="#{ForumTool.selectedMsgId!=msgDecorateBean.message.id}">
   				<f:verbatim><div class="hierItemBlock"></f:verbatim>
				<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
				<f:verbatim><div class="specialLink" style="width:65%;float:left;text-align:left"></f:verbatim>
				<h:panelGroup rendered="#{!msgDecorateBean.message.deleted}">
					<h:outputText value="#{msgDecorateBean.message.title}" />
					<h:outputText  value= " - #{msgDecorateBean.message.author} " />		
					<h:outputText value="#{msgDecorateBean.message.created}">
						<f:convertDateTime pattern="#{msgs.date_format_paren}" />
					</h:outputText>
					<h:panelGroup rendered="#{!empty msgDecorateBean.attachList}">
						<h:dataTable value="#{msgDecorateBean.attachList}" var="eachAttach" styleClass="attachListJSF" rendered="#{!empty msgDecorateBean.attachList}">
						<h:column rendered="#{!empty msgDecorateBean.attachList}">
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>		
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
							<h:outputLink value="#{eachAttach.url}" target="_blank">
								<h:outputText value="#{eachAttach.attachment.attachmentName}" />
							</h:outputLink>								
						</h:column>
						</h:dataTable>
				</h:panelGroup>
				<f:verbatim></div ></f:verbatim>			
				</h:panelGroup>
				
				<h:panelGroup styleClass="inactive" rendered="#{msgDecorateBean.message.deleted}">
				 	<f:verbatim><span></f:verbatim>
					<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
					<f:verbatim></span></f:verbatim>
				<f:verbatim></div ></f:verbatim>				
				</h:panelGroup>
						
				<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
				<f:verbatim></h4></f:verbatim>
				<mf:htmlShowArea value="#{msgDecorateBean.message.body}" hideBorder="true" rendered="#{!msgDecorateBean.message.deleted}"/>
				<mf:htmlShowArea value="" hideBorder="true" rendered="#{msgDecorateBean.message.deleted}"/>	
				<f:verbatim></div></f:verbatim>
				</h:panelGroup>
				
				<h:panelGroup rendered="#{ForumTool.selectedMsgId==msgDecorateBean.message.id}">
				<f:verbatim><a name="boldMsg" /></f:verbatim>
   				<f:verbatim><div class="hierItemBlockBold"></f:verbatim>
				<f:verbatim><h4 class="textPanelHeaderBold"></f:verbatim>
				<f:verbatim><div class="specialLink" style="width:100%;float:left;text-align:left"></f:verbatim>
				<h:panelGroup rendered="#{!msgDecorateBean.message.deleted}">
					<h:outputText value="#{msgDecorateBean.message.title}" />
					<h:outputText  value= " - #{msgDecorateBean.message.author} " />			
					<h:outputText value="#{msgDecorateBean.message.created}">
						<f:convertDateTime pattern="#{msgs.date_format_paren}" />
					</h:outputText>
					<h:panelGroup rendered="#{!empty msgDecorateBean.attachList}">
						<h:dataTable value="#{msgDecorateBean.attachList}" var="eachAttach" styleClass="attachListJSF" rendered="#{!empty msgDecorateBean.attachList}">
						<h:column rendered="#{!empty msgDecorateBean.attachList}">
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>		
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
							<h:outputLink value="#{eachAttach.url}" target="_blank">
								<h:outputText value="#{eachAttach.attachment.attachmentName}" />
							</h:outputLink>								
						</h:column>
						</h:dataTable>
				</h:panelGroup>
				<f:verbatim></div ></f:verbatim>			
				</h:panelGroup>
				
				<h:panelGroup styleClass="inactive" rendered="#{msgDecorateBean.message.deleted}">
					<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
					<f:verbatim></span></f:verbatim>
					<f:verbatim></div ></f:verbatim>				
				</h:panelGroup>
			
				
				<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
				<f:verbatim></h4></f:verbatim>
				<mf:htmlShowArea value="#{msgDecorateBean.message.body}" hideBorder="true" rendered="#{!msgDecorateBean.message.deleted}"/>
				<mf:htmlShowArea value="" hideBorder="true" rendered="#{msgDecorateBean.message.deleted}"/>
				
				<f:verbatim></div></f:verbatim>
				</h:panelGroup>				
  			
 		</h:column>
	</mf:hierDataTable>

  		<br /><br />
  		<h:panelGroup>
  			<h:commandButton action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.buttonUserName}"  
			               title="#{mfStatisticsBean.buttonUserName}">			               			
			</h:commandButton>
		</h:panelGroup>
		
			<%
				String thisId = request.getParameter("panel");
				if (thisId == null) {
					thisId = "Main"	+ org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
				}
			%>
			<script type="text/javascript">
				function resize(){
  					mySetMainFrameHeight('<%=org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  				}
			</script>

			<script type="text/javascript">
				resize();				
			//find the anchor	
				document.location.href=document.location.href + "#boldMsg";	
			//Set attribute onload here to skip calling portal's setMainFrameHeight, otherwise the scroll bar will reset to go to the top. Put setFocus method here is because portal's onload has two methods. one is setMainFrameHeight, another is setFocus. 			
				document.body.setAttribute("onload", "setFocus(focus_path)");
			</script>
  	</h:form>
  </sakai:view>
 </f:view>