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
	// open print preview in another browser window so can size approx what actual
	// print out will look like
	function printFriendly(url) {
		window.open(url,'mywindow','width=960,height=1100'); 		
	}
</script>
	<h:form id="msgForum">
<!--jsp/discussionForum/message/dfAllMessages.jsp-->
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
		

		<sakai:tool_bar>
				<sakai:tool_bar_item value="#{msgs.cdfm_container_title_thread}" action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages"
		  			rendered="#{ForumTool.selectedTopic.isNewResponse}" />
      			
      			<sakai:tool_bar_item value="#{msgs.cdfm_thread_view}" action="#{ForumTool.processActionDisplayThreadedView}" />
      			
        		<sakai:tool_bar_item action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" 
					rendered="#{ForumTool.instructor}" />
					
				<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyUrl}');">
					<h:graphicImage url="/images/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
				</h:outputLink>
 		</sakai:tool_bar>

			    <h:panelGroup>
					<f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" title=" #{msgs.cdfm_topic_settings}">
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
				  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  	  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
					  <f:verbatim></h3></div></f:verbatim>
				 </h:panelGroup>
		
	
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
		<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.messages}" var="message" 
   	 		noarrows="true" styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="attach,bogus">
			<h:column id="_msg_subject">
					<f:verbatim><div class="hierItemBlock"></f:verbatim>

					<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
                        <f:verbatim><div class="specialLink" style="width:50%;float:left;text-align:left"></f:verbatim>
                           <h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{message.message.title}">
                        
                            <h:outputText value="#{message.message.title}" rendered="#{message.read}" />
     						<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}" />

		        	    	<f:param value="#{message.message.id}" name="messageId"/>
		        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
		        	    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
			          	</h:commandLink>
			          	
			          	<h:outputText value=" - #{message.message.author}" rendered="#{message.read}" />
   			   	    	<h:outputText styleClass="unreadMsg" value=" - #{message.message.author}" rendered="#{!message.read }" />
   
                           <h:outputText value="#{message.message.created}" rendered="#{message.read}">
   				   	         <f:convertDateTime pattern="#{msgs.date_format_paren}" />
   				   	      </h:outputText>
   				   	      <h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read}">
   				   	      	<f:convertDateTime pattern="#{msgs.date_format_paren}" />
   				   	      </h:outputText>
                           
   				   	      <h:commandLink action="#{ForumTool.processDfMsgMarkMsgAsRead}" rendered="#{!message.read}"> 
   	                        <f:param value="#{message.message.id}" name="messageId"/>
           	    					<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
           	    					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
      						   	<h:graphicImage value="/images/silk/email.png" alt="#{msgs.msg_is_unread}" rendered="#{!message.read}" />
                           </h:commandLink>
	                     <f:verbatim></div></f:verbatim>

                         <f:verbatim><div style="width:50%;float:right;text-align:right" class="specialLink"></f:verbatim>
				   	     	<h:commandLink action="#{ForumTool.processDfMsgReplyMsgFromEntire}"
	                         	rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}">
	                            <f:param value="#{message.message.id}" name="messageId"/>
              	    			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
              	    			<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
        	    				<h:graphicImage value="/images/silk/email_edit.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" 
        	    					rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}"/>
        	    				<h:outputText value="Reply" />
   	                        </h:commandLink>
   	                        	
		                    <h:outputText value=" | " />
		                      
	  					    <h:outputLink value="#" onclick="toggleDisplay('#{message.message.id}_advanced_box'); resize(); return false;" >
								<h:graphicImage value="/images/silk/email_go.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" />
	   							<h:outputText value="Advanced Options" />
	   						</h:outputLink>
	   						<h:outputText escape="false" value="<div id=\"#{message.message.id}_advanced_box\" style=\"display:none\">" />
	   					    	<h:outputText value="Moderate | Delete | Revise | Etc" />
	   						<h:outputText escape="false" value="</div>" />     	
						  <f:verbatim></div></f:verbatim>

						<f:verbatim><div style="clear:both;height:.1em"></div></f:verbatim>
                     <f:verbatim></h4></f:verbatim>

		  			<mf:htmlShowArea value="#{message.message.body}" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
			</h:column>
		</mf:hierDataTable>
				
		<h:inputHidden id="mainOrForumOrTopic" value="dfAllMessages" />
		
	</h:form>
	
<%
  String thisId = request.getParameter("panel");
  if (thisId == null) 
  {
    thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  }
%>
			<script type="text/javascript">
			function resize(){
  				setMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  			}
			</script> 
</sakai:view>
</f:view>