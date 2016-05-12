<%-- For breadcrumb, displays either Messages & Forums / Messages / or just Messages /
	 also displays the Previous/Next folder links --%>
 <h:panelGrid columns="2" width="100%" styleClass="navPanel mobile_blocked">	 		
	<h:panelGroup>
		<f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
			<h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
				<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
				<h:outputText value=" / " />
			</h:panelGroup>
	  	
			<h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}" title=" #{msgs.cdfm_message_forums}"/>
			<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
			
			<h:outputText value="#{msgs[PrivateMessagesTool.selectedTopic.topic.title]}" rendered="#{PrivateMessagesTool.searchPvtMsgsEmpty && (PrivateMessagesTool.selectedTopic.topic.title == 'pvt_received' || PrivateMessagesTool.selectedTopic.topic.title == 'pvt_sent' || PrivateMessagesTool.selectedTopic.topic.title == 'pvt_deleted' || PrivateMessagesTool.selectedTopic.topic.title == 'pvt_drafts') }" />
			<h:outputText value="#{PrivateMessagesTool.selectedTopic.topic.title}" rendered="#{PrivateMessagesTool.searchPvtMsgsEmpty  && PrivateMessagesTool.selectedTopic.topic.title != 'pvt_received' && PrivateMessagesTool.selectedTopic.topic.title != 'pvt_sent' && PrivateMessagesTool.selectedTopic.topic.title != 'pvt_deleted' && PrivateMessagesTool.selectedTopic.topic.title != 'pvt_drafts'}" />
		
			<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}" title=" #{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}" rendered="#{! PrivateMessagesTool.searchPvtMsgsEmpty}"/>
			
			<h:outputText value=" / " rendered="#{! PrivateMessagesTool.searchPvtMsgsEmpty}" />
			<h:outputText value=" " />
			
			<h:outputText value="#{msgs.pvt_search}" rendered="#{! PrivateMessagesTool.searchPvtMsgsEmpty}" />
		
		<f:verbatim></h3></div></f:verbatim>
	</h:panelGroup>
	<h:panelGroup styleClass="itemNav specialLink">
		<%-- gsilver:huh? renders anyway - because it is looking at topics instead of at folders?--%>
		<h:commandLink styleClass="btn-primary" action="#{PrivateMessagesTool.processDisplayPreviousTopic}" value="#{msgs.pvt_prev_folder}"  
			                rendered="#{PrivateMessagesTool.selectedTopic.hasPreviousTopic}" title=" #{msgs.pvt_prev_folder}">
			<f:param value="#{PrivateMessagesTool.selectedTopic.previousTopicTitle}" name="previousTopicTitle"/>
		</h:commandLink>
		<h:panelGroup rendered="#{!PrivateMessagesTool.selectedTopic.hasPreviousTopic}" styleClass="button formButtonDisabled">
			<h:outputText value="#{msgs.pvt_prev_folder}"/>
		</h:panelGroup>
		
		<h:commandLink styleClass="btn-primary" action="#{PrivateMessagesTool.processDisplayNextTopic}" value="#{msgs.pvt_next_folder}" 
				  		                  rendered="#{PrivateMessagesTool.selectedTopic.hasNextTopic}" title=" #{msgs.pvt_next_folder}">
			<f:param value="#{PrivateMessagesTool.selectedTopic.nextTopicTitle}" name="nextTopicTitle"/>
		</h:commandLink>
		<h:panelGroup  rendered="#{!PrivateMessagesTool.selectedTopic.hasNextTopic}" styleClass="button formButtonDisabled">
			<h:outputText value="#{msgs.pvt_next_folder}" />
		</h:panelGroup>
	</h:panelGroup>
</h:panelGrid>
