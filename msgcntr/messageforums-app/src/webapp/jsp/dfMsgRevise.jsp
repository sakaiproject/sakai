<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view_container title="#{msgs.cdfm_revise_forum_msg}">
    <sakai:view_content>
<!--jsp/dfMsgRevise.jsp-->
	<h:form id="dfCompose">
	       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>

     <h3><h:outputText value="#{msgs.cdfm_revise_forum_msg}" /></h3>
     <h4><h:outputText value="#{ForumTool.selectedForum.forum.title}-#{ForumTool.selectedTopic.topic.title}" /></h4> 

     <div class="textPanel">
	 <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
        </div>
	<p class="instruction">		
              <h:outputText value="#{msgs.cdfm_required}"/>
              <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
	  </p>
		
		<h:messages styleClass="alertMessage" id="errorMessages" />

          <h:panelGrid styleClass="jsfFormTable" columns="2" width="100%">
            <h:panelGroup styleClass="required">
							     <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
					   <h:outputLabel for="df_compose_title"><h:outputText value="#{msgs.cdfm_reply_title}" /></h:outputLabel>

            </h:panelGroup>
            <h:panelGroup>
					   <h:inputText value="#{ForumTool.composeTitle}" size="40" required="true" id="df_compose_title" />
				   </h:panelGroup>

          </h:panelGrid>
		  <h4>
	            <h:outputText value="#{msgs.cdfm_message}" />
			</h4>	
	            <sakai:rich_text_area value="#{ForumTool.composeBody}" rows="17" columns="70"/>
	      
	      
<%--********************* Attachment *********************--%>	
	        <h4>
	          <h:outputText value="#{msgs.cdfm_att}"/>
	        </h4>
			<div class="instruction">
	        <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}"/>
			</div>
	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" immediate="true" accesskey="a" />
	          </sakai:button_bar>
			  	<%-- gsilver:moved rendered attribute from h:colun to dataTable - we do not want empty tables--%>
					<h:dataTable styleClass="listHier lines nolines" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach"
					columnClasses="att,bogus,itemAction specialLink,bogus,bogus" rendered="#{!empty ForumTool.attachments}">
					  <h:column>
							<f:facet name="header">
							</f:facet>
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />								
							</h:column>	
							<h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.cdfm_title}"/>
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
							</h:column>
							<h:column>
								<h:commandLink action="#{ForumTool.processDeleteAttachRevise}" 
									immediate="true"
									onfocus="document.forms[0].onsubmit();"
									title="#{msgs.cdfm_remove}">
									<h:outputText value="#{msgs.cdfm_remove}"/>
<%--									<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
									<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>
								</h:commandLink>
			
						</h:column>
					  <h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.cdfm_attsize}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
						</h:column>
					  <h:column>
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
        <sakai:button_bar_item action="#{ForumTool.processDfMsgRevisedPost}" value="#{msgs.cdfm_button_bar_post_revised_msg}" accesskey="s" styleClass="active" />
       <%-- <sakai:button_bar_item action="#{ForumTool.processDfMsgSaveRevisedDraft}" value="#{msgs.cdfm_button_bar_save_draft}" /> --%>
        <sakai:button_bar_item action="#{ForumTool.processDfMsgRevisedCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="x" />
      </sakai:button_bar>
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

