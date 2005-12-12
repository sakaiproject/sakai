<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
   <sakai:view>
      <h:form id="topic_settings">
      <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
        <sakai:tool_bar_message value="#{msgs.cdfm_discussion_topic_settings}" />
 		<p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel" for="topic_title"  value="#{msgs.cdfm_topic_title}"/>	</h:panelGroup>
				<h:panelGroup><h:outputText  id="topic_title"  value="#{ForumTool.selectedTopic.topic.title}"/></h:panelGroup>

				<h:panelGroup><h:outputLabel id="outputLabel1" for="topic_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:outputText id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/></h:panelGroup>

				<h:panelGroup><h:outputLabel id="outputLabel2" for="topic_fullDescription"  value="#{msgs.cdfm_fullDescription}"/>	</h:panelGroup>
				<h:panelGroup><h:outputText id="topic_fullDescription"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/></h:panelGroup>
				
				<h:panelGroup><h:outputLabel id="outputLabel3" for="topic_attachments"  value="#{msgs.cdfm_attachments}"/>	</h:panelGroup>
				<%--<h:panelGroup>
					 <h:dataTable id="topic_attachments"  value="#{ForumTool.selectedTopic.topic.attachments}" var="attachment" >
					  		<h:column rendered="#{!empty ForumTool.selectedTopic.topic.attachments}">
								<h:graphicImage url="/images/excel.gif" rendered="#{attachment.attachmentType == 'application/vnd.ms-excel'}"/>
								<h:graphicImage url="/images/html.gif" rendered="#{attachment.attachmentType == 'text/html'}"/>
								<h:graphicImage url="/images/pdf.gif" rendered="#{attachment.attachmentType == 'application/pdf'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{attachment.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								<h:graphicImage url="/images/text.gif" rendered="#{attachment.attachmentType == 'text/plain'}"/>
								<h:graphicImage url="/images/word.gif" rendered="#{attachment.attachmentType == 'application/msword'}"/>
								<h:outputText value="#{attachment.attachmentName}"/>
						</h:column>
					</h:dataTable> 			 
				</h:panelGroup>--%>
      		</h:panelGrid>
		</p>
        <h4><h:outputText  value="#{msgs.cdfm_topic_posting}"/></h4>
        <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel4" for="topic_posting"  value="#{msgs.cdfm_lock_topic}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="pageDirection" disabled="true" id="topic_posting"  value="#{ForumTool.selectedTopic.locked}">
    					<f:selectItem itemValue="true" itemLabel="Yes"/>
    					<f:selectItem itemValue="false" itemLabel="No"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		</p>
      <mf:forumHideDivision title="#{msgs.cdfm_control_permissions}" id="cntrl_perm">
      </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_message_permissions}" id="msg_perm">
      </mf:forumHideDivision>
      
       <p class="act">
          <h:commandButton action="#{ForumTool.processActionReviseTopicSettings}" value="#{msgs.cdfm_button_bar_revise}"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" value="#{msgs.cdfm_button_bar_delete}">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>
	 </h:form>
    </sakai:view>
</f:view>
