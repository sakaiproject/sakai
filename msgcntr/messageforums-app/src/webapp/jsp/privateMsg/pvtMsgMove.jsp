<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
	<sakai:view title="#{msgs.pvt_move} #{msgs.pvt_rcvd_msgs}">
	
		<h:form id="pvtMsgMove">
		  <sakai:tool_bar_message value="#{msgs.pvt_msgs_label} #{msgs.pvt_move_msg_to}" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 

			<h:dataTable width="100%" value="#{PrivateMessagesTool.decoratedForum}" var="forum">
		    <h:column>
					<h:dataTable id="privateForums" width="90%" value="#{forum.topics}" var="topic"  >
						<h:column>
					    <f:facet name="header">
							</f:facet>
					    	<h:selectOneRadio value="#{PrivateMessagesTool.moveToTopic}" onclick="this.form.submit();"
		                             valueChangeListener="#{PrivateMessagesTool.processPvtMsgParentFolderMove}">
					      	<f:selectItem itemValue="#{topic.topic.uuid}"  itemLabel="#{topic.topic.title}" />	
					      	<%--<f:param value="#{topic.topic.uuid}" name="pvtMsgMoveTopicId"/>--%>
		  			    </h:selectOneRadio>
					  </h:column>
					</h:dataTable>
				</h:column>
		  </h:dataTable> 		  
        
 				
			<sakai:button_bar>
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMoveMessage}" value="#{msgs.pvt_move_msg}" accesskey="m" />
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMoveCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
		  </sakai:button_bar>   
	          
		</h:form>

	</sakai:view>
</f:view>

