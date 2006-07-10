<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
	<sakai:view title="#{msgs.pvtarea_name}">

		<h:form id="prefs_pvt_form">
			<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>		
			
			<%--<sakai:tool_bar_message value="#{msgs.pvt_pvtmsg}- #{PrivateMessagesTool.msgNavMode}" /> --%>
			<h:panelGrid columns="2" summary="" width="100%">
        <h:panelGroup>
          	<f:verbatim><div class="breadCrumb"></f:verbatim>
			      <h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"/>
            <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
	  		      <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}" title=" #{msgs.cdfm_message_forums}"/>
            <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
	 		      <h:outputText value="#{PrivateMessagesTool.msgNavMode}"/>
			    <f:verbatim></div></f:verbatim>
        </h:panelGroup>
        <h:panelGroup styleClass="msgNav">
          <h:outputText   value="#{msgs.pvt_prev_folder}"  rendered="#{!PrivateMessagesTool.selectedTopic.hasPreviousTopic}" />
				 <h:commandLink action="#{PrivateMessagesTool.processDisplayPreviousTopic}" value="#{msgs.pvt_prev_folder}"  
				                rendered="#{PrivateMessagesTool.selectedTopic.hasPreviousTopic}" title=" #{msgs.pvt_prev_folder}">
	  			   <f:param value="#{PrivateMessagesTool.selectedTopic.previousTopicTitle}" name="previousTopicTitle"/>
	  		   </h:commandLink>
				  <f:verbatim><h:outputText value=" " /></f:verbatim>
				 <h:outputText   value="#{msgs.pvt_next_folder}" rendered="#{!PrivateMessagesTool.selectedTopic.hasNextTopic}" />
	  		   <h:commandLink action="#{PrivateMessagesTool.processDisplayNextTopic}" value="#{msgs.pvt_next_folder}" 
	  		                  rendered="#{PrivateMessagesTool.selectedTopic.hasNextTopic}" title=" #{msgs.pvt_next_folder}">
	  			  <f:param value="#{PrivateMessagesTool.selectedTopic.nextTopicTitle}" name="nextTopicTitle"/>
	  		   </h:commandLink>
        </h:panelGroup>
      </h:panelGrid>
 
 			<h:messages styleClass="alertMessage" id="errorMessages" /> 
  	
  		<%@include file="msgHeader.jsp"%>
			
	  <h:dataTable styleClass="listHier" id="pvtmsgs" width="100%" value="#{PrivateMessagesTool.decoratedPvtMsgs}" var="rcvdItems" 
	  	             rendered="#{PrivateMessagesTool.selectView != 'threaded'}"
	  	             summary="#{msgs.pvtMsgListSummary}">
	  	                
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded'} && #{PrivateMessagesTool.msgNavMode == 'Deleted'}">
		    <f:facet name="header">
 					<h:commandLink action="#{PrivateMessagesTool.processCheckAll}" value="#{msgs.cdfm_checkall}" 
 					               rendered="#{PrivateMessagesTool.msgNavMode == 'Deleted'}" title="#{msgs.cdfm_checkall}"/>
		     <%--<h:commandButton alt="SelectAll" image="/sakai-messageforums-tool/images/checkbox.gif" action="#{PrivateMessagesTool.processSelectAllJobs}"/>--%>
		    </f:facet>
				<h:selectBooleanCheckbox value="#{rcvdItems.isSelected}" rendered="#{PrivateMessagesTool.msgNavMode == 'Deleted'}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded'}">
		    <f:facet name="header">					
			  <h:commandLink>
		        <h:graphicImage value="/images/attachment.gif"
		                        title="#{msgs.sort_attachment}" 
		                        alt="#{msgs.sort_attachment}" />
		        <h:graphicImage value="/images/sortascending.gif" style="border:0" 
    	                        title="#{msgs.sort_attachment_asc}" alt="#{msgs.sort_attachment_asc}"
    	                        rendered="#{PrivateMessagesTool.sortType == 'attachment_asc'}"/>
    	        <h:graphicImage value="/images/sortdescending.gif" style="border:0" 
    	                        title="#{msgs.sort_attachment_desc}" alt=" #{msgs.sort_attachment_desc}"
    	                        rendered="#{PrivateMessagesTool.sortType == 'attachment_desc'}"/>    	                       
    	        <f:param name="sortColumn" value="attachment"/>
    	      </h:commandLink>
			</f:facet>
			<h:graphicImage value="/images/attachment.gif" rendered="#{rcvdItems.msg.hasAttachments}" alt="#{msgs.msg_has_attach}" />			 
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded'}">
		    <f:facet name="header">
		       <h:commandLink value="#{msgs.pvt_subject}"
		                      title="#{msgs.sort_subject}">
		         <h:graphicImage value="/images/sortascending.gif" style="border:0" 
    	                         title="#{msgs.sort_subject_asc}" alt="#{msgs.sort_subject_asc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'subject_asc'}"/>
    	         <h:graphicImage value="/images/sortdescending.gif" style="border:0" 
    	                         title="#{msgs.sort_subject_desc}" alt="#{msgs.sort_subject_desc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'subject_desc'}"/>
    	         <f:param name="sortColumn" value="subject"/>
    	       </h:commandLink>
		    </f:facet>
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" title="#{rcvdItems.msg.title}" immediate="true">
            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText styleClass="unreadMsg" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  </h:column>			
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded' && PrivateMessagesTool.msgNavMode != 'Sent'}">
		    <f:facet name="header">
		       <h:commandLink value="#{msgs.pvt_authby}"
		                      title="#{msgs.sort_author}">
		         <h:graphicImage value="/images/sortascending.gif" style="border:0" 
    	                         title="#{msgs.sort_author_asc}" alt="#{msgs.sort_author_asc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'author_asc'}"/>
    	         <h:graphicImage value="/images/sortdescending.gif" style="border:0" 
    	                         title="#{msgs.sort_author_desc}" alt="#{msgs.sort_author_desc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'author_desc'}"/>
    	         <f:param name="sortColumn" value="author"/>
    	       </h:commandLink>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.msg.author}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.author}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded' && PrivateMessagesTool.msgNavMode == 'Sent'}">
		    <f:facet name="header">
   		       <h:outputText value="#{msgs.pvt_to}"/>
		       <%--
		       <h:commandLink value="#{msgs.pvt_to}"
		                      title="#{msgs.sort_to}">
		         <h:graphicImage value="/images/sortascending.gif" style="border:0" 
    	                         title="#{msgs.sort_to_asc}" alt="#{msgs.sort_to_asc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'to_asc'}"/>
    	         <h:graphicImage value="/images/sortdescending.gif" style="border:0" 
    	                         title="#{msgs.sort_to_desc}" alt="#{msgs.sort_to_desc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'to_desc'}"/>    	                       
    	         <f:param name="sortColumn" value="to"/>
    	       </h:commandLink>
    	       --%>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}" />
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>	
		  	  
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded'}">
		    <f:facet name="header">
		       <h:commandLink value="#{msgs.pvt_date}"
		                      title="#{msgs.sort_date}">
		         <h:graphicImage value="/images/sortascending.gif" style="border:0" 
    	                         title="#{msgs.sort_date_asc}" alt="#{msgs.sort_date_asc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'date_asc'}"/>
    	         <h:graphicImage value="/images/sortdescending.gif" style="border:0" 
    	                         title="#{msgs.sort_date_desc}" alt="#{msgs.sort_date_desc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'date_desc'}"/>    	                       
    	         <f:param name="sortColumn" value="date"/>
    	       </h:commandLink>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" />
			 </h:outputText>
		   <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}">
			   <f:convertDateTime pattern="#{msgs.date_format}" />
			 </h:outputText>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded'}">
		    <f:facet name="header">
		       <h:commandLink value="#{msgs.pvt_label}"
		                      title="#{msgs.sort_label}">
		         <h:graphicImage value="/images/sortascending.gif" style="border:0" 
    	                         title="#{msgs.sort_label_asc}" alt="#{msgs.sort_label_asc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'label_asc'}"/>
    	         <h:graphicImage value="/images/sortdescending.gif" style="border:0" 
    	                         title="#{msgs.sort_label_desc}" alt="#{msgs.sort_label_desc}"
    	                         rendered="#{PrivateMessagesTool.sortType == 'label_desc'}"/>    	                       
    	         <f:param name="sortColumn" value="label"/>
    	       </h:commandLink>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.label}"/>
		  </h:column>
		</h:dataTable>
		
	  <mf:hierPvtMsgDataTable styleClass="listHier" id="threaded_pvtmsgs" width="100%" 
	                          value="#{PrivateMessagesTool.decoratedPvtMsgs}" 
	  	                        var="rcvdItems" 
	  	                        rendered="#{PrivateMessagesTool.selectView == 'threaded'}"
	                        	 expanded="true">
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
		    <f:facet name="header">
		    </f:facet>
		    <h:selectBooleanCheckbox value="#{rcvdItems.isSelected}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" />								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{rcvdItems.msg.hasAttachments}" alt="#{msgs.msg_has_attach}" />			 
			</h:column>
			<h:column id="_msg_subject"
				rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_subject}"/>
		    </f:facet>
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" immediate="true" title=" #{rcvdItems.msg.title}">
            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText styleClass="unreadMsg" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  </h:column>			
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded' && PrivateMessagesTool.msgNavMode != 'Sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.msg.author}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.author}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded' && PrivateMessagesTool.msgNavMode == 'Sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_to}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>		  
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_date}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" />
			 </h:outputText>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" />
			 </h:outputText>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_label}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.label}"/>
		  </h:column>
		</mf:hierPvtMsgDataTable>
		
		 </h:form>

	</sakai:view>
</f:view>
