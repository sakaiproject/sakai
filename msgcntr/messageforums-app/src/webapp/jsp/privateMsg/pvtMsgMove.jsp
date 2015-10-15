<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.pvt_move} #{msgs.pvt_rcvd_msgs}">
<!--jsp/privateMsg/pvtMsgMove.jsp-->	
		<h:form id="pvtMsgMove">
		       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
		<f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
			<h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
				<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
				<h:outputText value=" / " />
			</h:panelGroup>
	  	
			<h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}" title=" #{msgs.cdfm_message_forums}"/>
			<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
		
			<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" 
				value="#{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}" 
				title=" #{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted' || PrivateMessagesTool.msgNavMode == 'pvt_drafts')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}" />
			
			<h:outputText value=" " />
			<h:outputText value=" / " />
			<h:outputText value=" " />
			
			<h:outputText value="#{msgs.pvt_move_msg_to}" />	
		<f:verbatim></h3></div></f:verbatim>
		
<%--		  <sakai:tool_bar_message value="#{msgs.pvt_msgs_label} #{msgs.pvt_move_msg_to}" /> --%>
			<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/> 

			<h:dataTable value="#{PrivateMessagesTool.decoratedForum}" var="forum">
		    <h:column>
					<h:dataTable id="privateForums" value="#{forum.topics}" var="topic"  >
						<h:column>
					    <f:facet name="header">
						</f:facet>
					    <h:selectOneRadio value="#{PrivateMessagesTool.moveToTopic}" onclick="this.form.submit();"
					    			valueChangeListener="#{PrivateMessagesTool.processPvtMsgParentFolderMove}">
					      	<f:selectItem itemValue="#{topic.topic.uuid}"  
							      	itemDisabled="#{PrivateMessagesTool.selectedTopic.topic == topic.topic}"
					      			itemLabel="#{(topic.topic.title == 'pvt_received' || topic.topic.title == 'pvt_sent' || topic.topic.title == 'pvt_deleted' || topic.topic.title == 'pvt_drafts')? msgs[topic.topic.title] : topic.topic.title}" />
					      	<%--<f:param value="#{topic.topic.uuid}" name="pvtMsgMoveTopicId"/>--%>
		  			    </h:selectOneRadio>
					  </h:column>
					  <h:column>
					  	<h:outputText value="#{msgs.pvt_move_current_folder}" styleClass="unreadMsg"
					      			rendered="#{PrivateMessagesTool.selectedTopic.topic == topic.topic}"/>
					  </h:column>
					</h:dataTable>
				</h:column>
		  </h:dataTable> 		  
        
 				
			<sakai:button_bar>
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMoveMessage}" value="#{msgs.pvt_move_msg}" accesskey="s" styleClass="active"/>
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancelToDetailView}" value="#{msgs.pvt_cancel}" accesskey="x" />
		  </sakai:button_bar>   
	          
		</h:form>

	</sakai:view>
</f:view>

