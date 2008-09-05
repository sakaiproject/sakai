<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>

  <sakai:view title="#{msgs.cdfm_reply_to_topic}">
     <h:form id="dfCompose">
            		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
<!--jsp/dfTopicReply.jsp-->
                
			<h3><h:outputText value="#{msgs.cdfm_reply_to_topic}" /></h3>
			<h4> 
			    <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		      <h:outputText value=" - "/>
	        <h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>        
        </h4>

       <p class="textPanel"> 
         <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
		   </p>
         <p class="textPanelFooter"> 
        <h:commandLink immediate="true" 
		                   action="#{ForumTool.processDfComposeToggle}" 
 				               onmousedown="document.forms[0].onsubmit();"
	  			               rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
		  		               value="#{msgs.cdfm_read_full_description}"
		  		               title="#{msgs.cdfm_read_full_description}">
			  	  <f:param value="dfTopicReply" name="redirectToProcessAction"/>
				  <f:param value="true" name="composeExpand"/>
  			 </h:commandLink>
	  		<h:commandLink immediate="true" 
		  		             action="#{ForumTool.processDfComposeToggle}" 
			               onmousedown="document.forms[0].onsubmit();"
 				 	           value="#{msgs.cdfm_hide_full_description}" 
 				             rendered="#{ForumTool.selectedTopic.readFullDesciption}"
 				             title="#{msgs.cdfm_read_full_description}">
  				<f:param value="dfTopicReply" name="redirectToProcessAction"/>
	  		</h:commandLink>
			</p>
			
			<mf:htmlShowArea value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
		                   rendered="#{ForumTool.selectedTopic.readFullDesciption}"
		                   id="topic_extended_description" 
		                   hideBorder="false" />

					<br />
					
					<p class="instruction">
							<h:outputText value="#{msgs.cdfm_required}"/>
              <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
          </p>
          <h:panelGrid styleClass="jsfFormTable" columns="2">
            <h:panelGroup styleClass="shorttext">
				     <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
					   <h:outputLabel for="df_compose_title"><h:outputText value="#{msgs.cdfm_title}" /></h:outputLabel>
			  </h:panelGroup>
            <h:panelGroup styleClass="shorttext">
					   <f:verbatim><h:outputText value=" " /></f:verbatim>
					   <h:inputText value="#{ForumTool.composeTitle}"  required="true" id="df_compose_title" size="40" />
					   
				   </h:panelGroup>
          </h:panelGrid>
		  <f:verbatim><h4></f:verbatim>
	            <h:outputText value="#{msgs.cdfm_message}" />
			<f:verbatim></h4></f:verbatim>	
            <sakai:rich_text_area value="#{ForumTool.composeBody}" rows="17" columns="70"/>
	      
<%--********************* Attachment *********************--%>	

	        <h4>
	          <h:outputText value="#{msgs.cdfm_att}"/>
	        </h4>
		        
			<h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}" styleClass="instruction"/>
	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" immediate="true"
	          	                       accesskey="a" />
	          </sakai:button_bar>

					<h:dataTable styleClass="listHier lines nolines" cellpadding="0" cellspacing="0" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach" columnClasses="bogus,itemAction,bogus,bogus" rendered="#{!empty ForumTool.attachments}">
					  <h:column rendered="#{!empty ForumTool.attachments}">
								<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
								<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
								<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
								</h:column>
								<h:column>
								<h:commandLink action="#{ForumTool.processDeleteAttach}" 
									immediate="true"
									onfocus="document.forms[0].onsubmit();"
									title="#{msgs.cdfm_remove}">
									<h:outputText value="#{msgs.cdfm_remove}"/>
<%--									<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>--%>
									<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>
								</h:commandLink>
						
						</h:column>
					  <h:column rendered="#{!empty ForumTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.cdfm_attsize}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
						</h:column>
					  <h:column rendered="#{!empty ForumTool.attachments}">
							<f:facet name="header">
		  			    <h:outputText value="#{msgs.cdfm_atttype}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
						</h:column>
						</h:dataTable>   
        		
<%--********************* Label *********************
				<sakai:panel_titled>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="Label"/>
              </td>
              <td align="left">
 							  <h:selectOneListbox size="1" id="viewlist">
            		  <f:selectItem itemLabel="Normal" itemValue="none"/>
          			</h:selectOneListbox>  
              </td>                           
            </tr>                                
          </table>
        </sakai:panel_titled>
--%>
      <sakai:button_bar>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyTopicPost}" value="#{msgs.cdfm_button_bar_post_message}" accesskey="s" styleClass="active"/>
   <%--     <sakai:button_bar_item action="#{ForumTool.processDfReplyTopicSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" /> --%>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyTopicCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="x" />
      </sakai:button_bar>
    </h:form>

  </sakai:view>
</f:view> 

