<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view title="#{msgs.pvt_detmsgreply}">
    <h:form id="pvtMsgDetail">
           		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
<!--jsp/privateMsg/pvtMsgDetail.jsp-->
<%--			<sakai:tool_bar_message value="#{msgs.pvt_detmsgreply}" /> --%> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 

<h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel specialLink">
	<h:panelGroup>
          	<f:verbatim><div class="breadCrumb"><h3></f:verbatim>
<%--   			  <h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"/><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
  			  <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.cdfm_message_pvtarea}" title=" #{msgs.cdfm_message_pvtarea}"/><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
--%>
				  <h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
				  	<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
				  	<f:verbatim><h:outputText value=" / " /></f:verbatim>
				  </h:panelGroup>
	  		      <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}" title=" #{msgs.cdfm_message_forums}"/>
	              <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}" title=" #{(PrivateMessagesTool.msgNavMode == 'pvt_received' || PrivateMessagesTool.msgNavMode == 'pvt_sent' || PrivateMessagesTool.msgNavMode == 'pvt_deleted')? msgs[PrivateMessagesTool.msgNavMode]: PrivateMessagesTool.msgNavMode}"/><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
<%--			
				<h:outputText value="#{msgs[PrivateMessagesTool.selectedTopic.topic.title]}" rendered="#{PrivateMessagesTool.searchPvtMsgsEmpty && (PrivateMessagesTool.selectedTopic.topic.title == 'pvt_received' || PrivateMessagesTool.selectedTopic.topic.title == 'pvt_sent' || PrivateMessagesTool.selectedTopic.topic.title == 'pvt_deleted') }" />
			<h:outputText value="#{PrivateMessagesTool.selectedTopic.topic.title}" rendered="#{PrivateMessagesTool.searchPvtMsgsEmpty  && PrivateMessagesTool.selectedTopic.topic.title != 'pvt_received' && PrivateMessagesTool.selectedTopic.topic.title != 'pvt_sent' && PrivateMessagesTool.selectedTopic.topic.title != 'pvt_deleted'}" />
		
				<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				--%>
				<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />
			<f:verbatim></h3></div></f:verbatim>
	</h:panelGroup>
	<h:panelGroup styleClass="itemNav ">			
       			
								<h:outputText value="#{msgs.pvt_prev_msg}"  rendered="#{!PrivateMessagesTool.detailMsg.hasPre}" />
								<h:commandLink action="#{PrivateMessagesTool.processDisplayPreviousMsg}" value="#{msgs.pvt_prev_msg}"  
								               rendered="#{PrivateMessagesTool.detailMsg.hasPre}" title=" #{msgs.pvt_prev_msg}">
								</h:commandLink>
								<h:outputText value=" | " /><h:outputText value=" " />
								<h:outputText   value="#{msgs.pvt_next_msg}" rendered="#{!PrivateMessagesTool.detailMsg.hasNext}" />
								<h:commandLink action="#{PrivateMessagesTool.processDisplayNextMsg}" value="#{msgs.pvt_next_msg}" 
								               rendered="#{PrivateMessagesTool.detailMsg.hasNext}" title=" #{msgs.pvt_next_msg}">
								</h:commandLink>
        </h:panelGroup>
      </h:panelGrid>
      <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >

                   <table style="width: 100%;">
				
				<tr>
				<td text-align="left">

  <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReply}" value="#{msgs.pvt_repmsg}" accesskey="r"/>
          
          <%--SAK-10505 add forward --%>
            <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplyAll}" value="#{msgs.pvt_repmsg_ALL}" accesskey="r" /><sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgForward}" value="#{msgs.pvt_forwardmsg}" accesskey="r"/>
          
				</td>


				<td text-align="right">
				 
                 
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMove}" value="#{msgs.pvt_move}" accesskey="m" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirm}" value="#{msgs.pvt_delete}"  />
				</td>				
				</tr>	  
     
				</table>

          
        </sakai:button_bar>        
        <sakai:button_bar rendered="#{PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirmYes}" value="#{msgs.pvt_delete}" accesskey="s" styleClass="active"/>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancelToDetailView}" value="#{msgs.pvt_cancel}" accesskey="x" />
        </sakai:button_bar>
          
        <table class="itemSummary">
          <tr>
            <th>
            	 <h:outputText value="#{msgs.pvt_to}" />
            </th>
            <td>
            	 <h:outputText value="#{PrivateMessagesTool.detailMsg.visibleRecipientsAsText}" rendered="#{! (PrivateMessagesTool.detailMsg.author == PrivateMessagesTool.authorString) }"  />
            	 <h:outputText value="#{PrivateMessagesTool.detailMsg.recipientsAsText}" rendered="#{ (PrivateMessagesTool.detailMsg.author == PrivateMessagesTool.authorString) }" />
            </td>
          </tr>
          <tr>
            <th>
              <h:outputText value="#{msgs.pvt_subject}"/>
            </th>
            <td>
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />  
            </td>  
          </tr>          
          <tr>
            <th>
              <h:outputText value="#{msgs.pvt_authby}"/>
            </th>
            <td>
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.author}" />  
            	<h:outputText value="#{msgs.pvt_openb}" />  
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.created}" >
                	<f:convertDateTime pattern="#{msgs.date_format}" />  
                </h:outputText>
            	<h:outputText value=" #{msgs.pvt_closeb}" /> 
            </td>
          </tr>
          
          <tr>
            <th>
              <h:outputText value="#{msgs.pvt_label} "/>
            </th>
            <td>
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.label}" />  
            </td>
          </tr> 
          <tr>
            <th>
              <h:outputText value="#{msgs.pvt_att}" rendered="#{!empty PrivateMessagesTool.detailMsg.attachList}" />
            </th>
            <td>
              <%-- Attachments --%>
			  <%-- gsilver:copying the rendered attribute from the column to the dataTable - do not want a table rendered without children (is not xhtml)--%>
              <h:dataTable value="#{PrivateMessagesTool.detailMsg.attachList}" var="eachAttach"  styleClass="attachListJSF"  rendered="#{!empty PrivateMessagesTool.detailMsg.attachList}">
					  		<h:column rendered="#{!empty PrivateMessagesTool.detailMsg.attachList}">
									<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
									<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />								  
<%--								  <h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
								  	<h:outputText value="#{eachAttach.attachmentName}"/>
									</h:outputLink>--%>
								  <h:outputLink value="#{eachAttach.url}" target="_blank">
								  	<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
									</h:outputLink>
									
								</h:column>
							</h:dataTable>   
              <%-- Attachments --%>
            </td>
          </tr>
                                        
        </table>    
		<hr class="itemSeparator" />
		
        	  <mf:htmlShowArea value="#{PrivateMessagesTool.detailMsg.msg.body}" id="htmlMsgText" hideBorder="true" />
        
        <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >
        
                   <table style="width: 100%;">
				
				<tr>
				<td text-align="left">
  <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReply}" value="#{msgs.pvt_repmsg}" accesskey="r"/>
          <%--SAKAI-10505 add forward--%>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplyAll}" value="#{msgs.pvt_repmsg_ALL}" accesskey="r" /><sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgForward}" value="#{msgs.pvt_forwardmsg}" accesskey="r"/>
          

				</td>


				<td text-align="right">
				 
               
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMove}" value="#{msgs.pvt_move}" accesskey="m" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirm}" value="#{msgs.pvt_delete}"  />
				</td>				
				</tr>	  
     
				</table>
        
        
          
        </sakai:button_bar>        
        <sakai:button_bar rendered="#{PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirmYes}" value="#{msgs.pvt_delete}" accesskey="s" styleClass="active"/>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancelToDetailView}" value="#{msgs.pvt_cancel}" accesskey="x" />
        </sakai:button_bar>

        
      </h:form>

  </sakai:view>
</f:view> 
                    
