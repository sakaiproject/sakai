<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />


<f:view>
	<sakai:view_container title="Move Private Messages">
	<sakai:view_content>
		<h:form id="pvtMsgMove">
		<sakai:tool_bar_message value="Private message- Move Message To" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 

	<h:dataTable width="100%" value="#{PrivateMessagesTool.decoratedForum}" var="forum">
    <h:column >
			<h:dataTable id="privateForums" width="90%" value="#{forum.topics}" var="topic"  >
				<h:column>
			    <f:facet name="header">
					</f:facet>
			    	<h:selectOneRadio value="#{PrivateMessagesTool.moveToTopic}" onchange="this.form.submit();"
                                  valueChangeListener="#{PrivateMessagesTool.processPvtMsgParentFolderMove}">
			      	<f:selectItem itemValue="#{topic.topic.uuid}"  itemLabel="#{topic.topic.title}" />	
			      	<%--<f:param value="#{topic.topic.uuid}" name="pvtMsgMoveTopicId"/>--%>
  			    </h:selectOneRadio>
			  </h:column>
			</h:dataTable>
		</h:column>
  </h:dataTable> 		  
        
 				
		<sakai:button_bar>
	    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMoveMessage}" value="Move Message" />
	    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMoveCancel}" value="Cancel" />
	  </sakai:button_bar>   
          
		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>

