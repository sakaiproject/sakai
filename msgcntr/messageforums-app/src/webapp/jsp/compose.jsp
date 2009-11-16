<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<!-- <h4>compose.jsp</h4> -->
<f:view>
  <sakai:view title="#{msgs.pvt_pvtcompose}">
<!--Y:\msgcntr\messageforums-app\src\webapp\jsp\compose.jsp-->
    <h:form id="compose">
           		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
		<!-- compose.jsp -->
  			<div class="breadCrumb specialLink">
				<h3>	
				  <h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
				  	<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
				  	<f:verbatim>&nbsp; / </f:verbatim>
				  </h:panelGroup>
				  <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.cdfm_message_pvtarea}" title=" #{msgs.cdfm_message_pvtarea}"/> /
				  <h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{PrivateMessagesTool.msgNavMode}" title=" #{msgs.cdfm_message_forums}" 
									rendered="#{! PrivateMessagesTool.fromMain}" />
				  <h:outputText escape="false" value=" / " rendered="#{! PrivateMessagesTool.fromMain}" />
				  <h:outputText value="#{msgs.pvt_compose1}" />
				</h3>
			</div>
			
			<sakai:tool_bar_message value="#{msgs.pvt_pvtcompose}" />
 			
 			<div class="instruction">
  			  <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" />
			</div>
			  
		  <h:outputLink rendered="#{PrivateMessagesTool.renderPrivacyAlert}" value="#{PrivateMessagesTool.privacyAlertUrl}" target="_blank" >
		  	  <sakai:instruction_message value="#{PrivateMessagesTool.privacyAlert}"/>
		  </h:outputLink>
		  
		  <h:messages styleClass="alertMessage" id="errorMessages" /> 
		  
		  <h:panelGrid styleClass="jsfFormTable" columns="2" summary="layout">
			  <h:panelGroup styleClass="shorttext required">
				  <h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/>
					<h:outputLabel for="list1"><h:outputText value="#{msgs.pvt_to}"/></h:outputLabel>
			  </h:panelGroup>
			  <h:panelGroup styleClass="shorttext">
					<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}" size="5" style="width: 20em;">
		         <f:selectItems value="#{PrivateMessagesTool.totalComposeToList}"/>
		       </h:selectManyListbox>
				</h:panelGroup>
		
	
			 <h:panelGroup styleClass="shorttext" rendered= "#{PrivateMessagesTool.dispSendEmailOut}">
					<h:outputLabel><h:outputText value="#{msgs.pvt_send_cc}"/></h:outputLabel>
			  </h:panelGroup>
			   
			  
			  <h:panelGroup styleClass="checkbox" style="white-space: nowrap;" rendered= "#{PrivateMessagesTool.dispSendEmailOut}">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.booleanEmailOut}" id="send_email_out" >
			  </h:selectBooleanCheckbox>
		   <h:outputLabel for="send_email_out"><h:outputText value="#{msgs.pvt_send_as_email}"/></h:outputLabel>
			</h:panelGroup>			  
	
				<h:outputLabel for="viewlist"><h:outputText value="#{msgs.pvt_label}" /></h:outputLabel>
				<h:selectOneListbox size="1" id="viewlist" value="#{PrivateMessagesTool.selectedLabel}">
            <f:selectItem itemValue="Normal" itemLabel="#{msgs.pvt_priority_normal}"/>
            <f:selectItem itemValue="Low" itemLabel="#{msgs.pvt_priority_low}"/>
            <f:selectItem itemValue="High" itemLabel="#{msgs.pvt_priority_high}"/>
        </h:selectOneListbox>
				
				<h:panelGroup styleClass="shorttext required">
				  <h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/>
					<h:outputLabel for="subject"><h:outputText value="#{msgs.pvt_subject}" /></h:outputLabel>
				</h:panelGroup>
				<h:panelGroup styleClass="shorttext">
					<h:inputText value="#{PrivateMessagesTool.composeSubject}" id="subject" size="45">
					  <f:validateLength minimum="1" maximum="255"/>
					</h:inputText>
				</h:panelGroup>
			</h:panelGrid>       
		  
		  <h4><h:outputText value="#{msgs.pvt_message}" /></h4>
			<sakai:inputRichText value="#{PrivateMessagesTool.composeBody}" id="pvt_message_body" rows="22" cols="120"> 
				<f:validateLength maximum="65000"/>
			</sakai:inputRichText>
	    
<%--********************* Attachment *********************--%>	
			  <h4>
	        <h:outputText value="#{msgs.pvt_att}"/>
	      </h4>
	        
	      <sakai:doc_section>	        
	        	<h:outputText value="#{msgs.pvt_noatt}" rendered="#{empty PrivateMessagesTool.attachments}"/>
	      </sakai:doc_section>
	        
	      <sakai:doc_section>
	        <sakai:button_bar>
	          <sakai:button_bar_item action="#{PrivateMessagesTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}"
	                                 accesskey="a" />
	        </sakai:button_bar>
	      </sakai:doc_section>
	        <%--designNote: copying the redenred attribute used in the first h:column to the dataTable - is there are no attachmetns - do not render the table at all.--%>
			<h:dataTable styleClass="listHier lines nolines" cellpadding="0" cellspacing="0"
			columnClasses="attach,bogus,itemAction specialLink,bogus,bogus"  id="attmsg" width="100%" value="#{PrivateMessagesTool.attachments}" var="eachAttach"   rendered="#{!empty PrivateMessagesTool.attachments}">
		      <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
				    	<f:facet name="header">

						</f:facet>
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />						 
						</h:column>	
						<h:column>
				    	<f:facet name="header">
						  <h:outputText value="#{msgs.pvt_title}"/>
						</f:facet>

						<h:outputText value="#{eachAttach.attachment.attachmentName}"/>

						</h:column>
						<h:column>
							
						  <h:commandLink action="#{PrivateMessagesTool.processDeleteAttach}" 
							           		immediate="true"
									           onfocus="document.forms[0].onsubmit();"
									           title="#{msgs.pvt_attrem}">
							  <h:outputText value="#{msgs.pvt_attrem}"/>
<%--							<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
								<f:param value="#{eachAttach.attachment.attachmentId}" name="pvmsg_current_attach"/>
							</h:commandLink>
									
					</h:column>
					<h:column rendered="#{!empty PrivateMessagesTool.attachments}">
					  <f:facet name="header">
						  <h:outputText value="#{msgs.pvt_attsize}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
					</h:column>
					<h:column rendered="#{!empty PrivateMessagesTool.attachments}">
					  <f:facet name="header">
		  			    <h:outputText value="#{msgs.pvt_atttype}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
					</h:column>
						<%--
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.pvt_noatt}Created by" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.createdBy}"/>
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Last modified by" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.lastModifiedBy}"/>
						</h:column>
						--%>
				</h:dataTable>   
		   			
      <sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSend}" value="#{msgs.pvt_send}" accesskey="s"  styleClass="active" />
        <%--<sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSaveDraft}" value="#{msgs.pvt_savedraft}" />--%>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgComposeCancel}" value="#{msgs.pvt_cancel}" accesskey="x" />
      </sakai:button_bar>
    </h:form>
	     
   </sakai:view>
</f:view> 

