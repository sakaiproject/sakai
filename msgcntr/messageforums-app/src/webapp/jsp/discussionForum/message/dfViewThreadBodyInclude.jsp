

				<h:outputText escape="false" value="<a id=\"#{message.message.id}\" name=\"#{message.message.id}\"></a>" />
				<f:verbatim><div class="hierItemBlock"></f:verbatim>
					<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
                        <f:verbatim><div class="specialLink" style="width:45%;float:left;text-align:left"></f:verbatim>
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
                           
   				   	      <h:commandLink action="#{ForumTool.processDfMsgMarkMsgAsReadFromThread}" rendered="#{!message.read}"> 
   	                        <f:param value="#{message.message.id}" name="messageId"/>
           	    					<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
           	    					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
      						   	<h:graphicImage value="/images/silk/email.png" alt="#{msgs.msg_is_unread}" rendered="#{!message.read}" 
				   	        		onmouseover="this.src=this.src.replace(/email\.png/, 'email_open.png');"
				   	        		onmouseout="this.src=this.src.replace(/email_open\.png/, 'email.png');" />
                           </h:commandLink>
	                     <f:verbatim></div></f:verbatim>

                         <f:verbatim><div style="width:45%;float:right;text-align:right" class="specialLink"></f:verbatim>
				   	     	<h:commandLink action="#{ForumTool.processDfMsgReplyMsgFromEntire}"
	                         	rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}">
	                            <f:param value="#{message.message.id}" name="messageId"/>
              	    			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
              	    			<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
        	    				<h:graphicImage value="/images/silk/email_edit.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" 
        	    					rendered="#{ForumTool.selectedTopic.isNewResponseToResponse}"/>
        	    				<h:outputText value="Reply" />
   	                        </h:commandLink>
   	                        	
		                    <h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
		                      
	  					    <h:outputLink value="#" onclick="toggleDisplay('#{message.message.id}_advanced_box'); resize(); toggleHide(this); return false;" >
								<h:graphicImage value="/images/silk/email_go.png" alt="#{msgs.cdfm_button_bar_reply_to_msg}" />
	   							<h:outputText value="#{msgs.cdfm_other_actions}" />
	   						</h:outputLink>
	   						<h:outputText escape="false" value="<div id=\"#{message.message.id}_advanced_box\" style=\"display:none\">" />
	   							<h:commandLink action="#{ForumTool.processDfMsgGrd}" value="#{msgs.cdfm_button_bar_grade}" 
                             		rendered="#{ForumTool.selectedTopic.isPostToGradebook && ForumTool.gradebookExist}" />
                             	<h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
                             	
                             	<h:commandLink action="#{ForumTool.processDfMsgRvs}" value="#{msgs.cdfm_button_bar_revise}"
									rendered="#{ForumTool.selectedTopic.isReviseAny}" />
								<h:outputText value=" #{msgs.cdfm_toolbar_separator} " />
                             	
	   					    	<h:outputText value="Delete #{msgs.cdfm_toolbar_separator} Approve #{msgs.cdfm_toolbar_separator} Deny" />

	   						<h:outputText escape="false" value="</div>" />     	
						  <f:verbatim></div></f:verbatim>

						<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
                     <f:verbatim></h4></f:verbatim>
					 <mf:htmlShowArea value="#{message.message.body}" hideBorder="true"/>
				<f:verbatim></div></f:verbatim>