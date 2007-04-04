

				<h:outputText escape="false" value="<a id=\"#{message.message.id}\" name=\"#{message.message.id}\"></a>" />
				<f:verbatim><div class="hierItemBlock"></f:verbatim>
					<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
              <f:verbatim><div class="specialLink" style="width:65%;float:left;text-align:left"></f:verbatim>
              
              <h:outputText value="#{msgs.cdfm_msg_pending_label} " styleClass="highlight" rendered="#{message.msgPending}" />
							<h:outputText value="#{msgs.cdfm_msg_denied_label} " rendered="#{message.msgDenied}" />
              
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
                           
      					<h:graphicImage value="/images/silk/email.png" alt="#{msgs.msg_is_unread}" rendered="#{!message.read}" style="cursor:pointer"
      								onclick="doAjax(#{message.message.id}, #{ForumTool.selectedTopic.topic.id}, this);"
				   	        		onmouseover="this.src=this.src.replace(/email\.png/, 'email_open.png');"
				   	        		onmouseout="this.src=this.src.replace(/email_open\.png/, 'email.png');" />
						<h:outputText value="<br />" escape="false" rendered="#{!empty message.attachList}" />
						<h:panelGroup rendered="#{!empty message.attachList}">
					      	<h:dataTable value="#{message.attachList}" var="eachAttach"  styleClass="attachListJSF"  rendered="#{!empty message.attachList}">
								  		<h:column rendered="#{!empty message.message.attachments}">
									      <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-excel'}" alt="" />
											  <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/html'}" alt="" />
												<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/pdf'}" alt="" />
												<h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
												<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/plain'}" alt="" />
												<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/msword'}" alt="" />
												<h:outputLink value="#{eachAttach.url}" target="_blank">
												 	<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
											  </h:outputLink>											  
										  </h:column>
						    </h:dataTable>
					      </h:panelGroup>
	                  <f:verbatim></div></f:verbatim>

                         <f:verbatim><div style="width:30%;float:right;text-align:right" class="specialLink"></f:verbatim>
				   	     	<h:panelGroup rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && message.msgApproved}">
				   	     		<h:commandLink action="#{ForumTool.processDfMsgReplyMsgFromEntire}" title="#{msgs.cdfm_reply}">
	                    <f:param value="#{message.message.id}" name="messageId"/>
              	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
              	    	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
        	    				<h:graphicImage value="/images/silk/email_go.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" />
        	    				<h:outputText value="#{msgs.cdfm_reply}" />
   	                </h:commandLink>
   	                     
		              </h:panelGroup>
	  					    
	  					    <h:panelGroup rendered="#{(ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist) || ForumTool.selectedTopic.isModeratedAndHasPerm || message.revise}">
	  					      <h:outputText value=" #{msgs.cdfm_toolbar_separator} " rendered="#{ForumTool.selectedTopic.isNewResponseToResponse && message.msgApproved}" />
	  					      <h:outputLink value="#" onclick="toggleDisplay('#{message.message.id}_advanced_box'); toggleHide(this); return false;" >
								      <h:graphicImage value="/images/silk/cog.png" alt="#{msgs.cdfm_other_actions}" />
	   							    <h:outputText value="#{msgs.cdfm_other_actions}" />
	   						    </h:outputLink>
	   						  </h:panelGroup>
	   						  <h:outputText escape="false" value="<div id=\"#{message.message.id}_advanced_box\" style=\"display:none\">" />
	   							
	   							<h:panelGroup rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}">
	   							<h:commandLink action="#{ForumTool.processDfMsgGrdFromThread}" value="#{msgs.cdfm_button_bar_grade}">
	   								<f:param value="#{message.message.id}" name="messageId"/>
	              	    			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	              	    			<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	   							</h:commandLink>
                      <h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
                  </h:panelGroup>
                  
                  <h:panelGroup rendered="#{message.revise}">
                    <h:commandLink action="#{ForumTool.processDfMsgRvsFromThread}" value="#{msgs.cdfm_button_bar_revise}" >
                    	<f:param value="#{message.message.id}" name="messageId"/>
		        	    <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
		        	    <f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
                    </h:commandLink> 
								    <h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
								  </h:panelGroup>
								  
								  <h:panelGroup rendered="#{ForumTool.selectedTopic.isModeratedAndHasPerm}">
								    <h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{msgs.cdfm_moderate}">
									    <h:outputText value="#{msgs.cdfm_moderate}" />
		        	        <f:param value="#{message.message.id}" name="messageId"/>
		        	        <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
		        	        <f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
			             </h:commandLink>
			          </h:panelGroup>

	   						<h:outputText escape="false" value="</div>" />     	
						  <f:verbatim></div></f:verbatim>

						<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
                     <f:verbatim></h4></f:verbatim>
					 <mf:htmlShowArea value="#{message.message.body}" hideBorder="true"/>
				<f:verbatim></div></f:verbatim>