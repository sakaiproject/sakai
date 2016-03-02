<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.tool.messageforums.ui.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>


<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view title="#{msgs.pvtarea_name}">
<!--jsp/privateMsg/pvtMsg.jsp-->
		<h:form id="prefs_pvt_form">
<%
// FOR WHEN COMING FROM SYNOPTIC TOOL 
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext exContext = context.getExternalContext();
    Map paramMap = exContext.getRequestParameterMap();
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{PrivateMessagesTool}");
    PrivateMessagesTool pmt = (PrivateMessagesTool) binding.getValue(context);
    
     if ( "pvt_received".equals((String) paramMap.get("selectedTopic")) ) {
	  pmt.initializeFromSynoptic();
    }
    
    if(pmt.getUserId() != null){
  	//show entire page, otherwise, don't allow anon user to use this tool:
%>

		       		<script type="text/javascript" src="/library/webjars/jquery/1.11.3/jquery.min.js?version="></script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
			<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>		
			
			<sakai:tool_bar>
       			<sakai:tool_bar_item value="#{msgs.pvt_compose}" action="#{PrivateMessagesTool.processPvtMsgCompose}" />
 			</sakai:tool_bar>

			<%--<sakai:tool_bar_message value="#{msgs.pvt_pvtmsg}- #{PrivateMessagesTool.msgNavMode}" /> --%>
			<%@ include file="topNav.jsp" %>
 
 			<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/> 
 			<!-- Display successfully moving checked messsages to Deleted folder -->
  			<h:outputText value="#{PrivateMessagesTool.multiDeleteSuccessMsg}" styleClass="success" rendered="#{PrivateMessagesTool.multiDeleteSuccess}" />
  			
  		<%@ include file="msgHeader.jsp"%>
		<%-- gsilver:this table needs a render atrtibute that will make it not display if there are no messages - and a companion text block classed as "instruction" that will render instead--%>	
	  <div class="table-responsive">
	  <h:dataTable styleClass="table table-hover table-striped table-bordered" id="pvtmsgs" width="100%" value="#{PrivateMessagesTool.decoratedPvtMsgs}" var="rcvdItems" 
	  	             rendered="#{PrivateMessagesTool.selectView != 'threaded'}"
	  	             summary="#{msgs.pvtMsgListSummary}"
					 columnClasses="attach,attach,attach,specialLink,bogus,bogus,bogus">
	  	                
		  <h:column>
		    <f:facet name="header">
 					<h:commandLink action="#{PrivateMessagesTool.processCheckAll}" value="#{msgs.cdfm_checkall}" 
 					               title="#{msgs.cdfm_checkall}" />
		     <%--<h:commandButton alt="SelectAll" image="/messageforums-tool/images/checkbox.gif" action="#{PrivateMessagesTool.processSelectAllJobs}"/>--%>
		    </f:facet>
				<h:selectBooleanCheckbox value="#{rcvdItems.isSelected}" onclick="updateCount(this.checked); toggleBulkOperations(anyChecked(), 'prefs_pvt_form');" />
		  </h:column>
		  <h:column>
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
		  <h:column>
		    <f:facet name="header">
	        	<h:graphicImage value="/images/replied_menu.png"
		                        title="#{msgs.pvt_msgs_replied}" 
		                        alt="#{msgs.pvt_msgs_replied}" />
			</f:facet>
			<h:graphicImage value="/images/replied_flag.png" rendered="#{rcvdItems.replied}"
								title="#{msgs.pvt_replied}"
								alt="#{msgs.pvt_replied}" />			 
		  </h:column>
		  <h:column>
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
			  <f:verbatim><h4></f:verbatim>
			<h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" title="#{rcvdItems.msg.title}" immediate="true">

            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText styleClass="unreadMsg" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
			<h:outputText styleClass="skip" value="#{msgs.pvt_openb}#{msgs.pvt_unread}#{msgs.pvt_closeb}" rendered="#{!rcvdItems.hasRead}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  			<f:verbatim></h4></f:verbatim>
		  </h:column>			
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_sent'}">
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
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title == 'pvt_sent'}">
		    <f:facet name="header">
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
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}" />
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>	
		  	  
		  <h:column>
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
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		   <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}">
			   <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		  </h:column>
		  <h:column>
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
		     <h:outputText value="#{rcvdItems.label}"/>
		  </h:column>
		</h:dataTable>
	</div>
		
	  <mf:hierPvtMsgDataTable styleClass="table table-hover table-striped table-bordered" id="threaded_pvtmsgs" width="100%" 
	                          value="#{PrivateMessagesTool.decoratedPvtMsgs}" 
	  	                        var="rcvdItems" 
	  	                        rendered="#{PrivateMessagesTool.selectView == 'threaded'}"
	                        	 expanded="true"
								 columnClasses="attach,attach,attack,specialLink,bogus,bogus,bogus">
		 	<h:column>
		    <f:facet name="header">
 					<h:commandLink action="#{PrivateMessagesTool.processCheckAll}" value="#{msgs.cdfm_checkall}" 
 					               title="#{msgs.cdfm_checkall}"/>
		     <%--<h:commandButton alt="SelectAll" image="/messageforums-tool/images/checkbox.gif" action="#{PrivateMessagesTool.processSelectAllJobs}"/>--%>
		    </f:facet>
				<h:selectBooleanCheckbox value="#{rcvdItems.isSelected}" onclick="updateCount(this.checked); toggleBulkOperations(anyChecked(), 'prefs_pvt_form');" />
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" />								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{rcvdItems.msg.hasAttachments}" alt="#{msgs.msg_has_attach}" />			 
			</h:column>
			<h:column>
				<f:facet name="header">					
		        	<h:graphicImage value="/images/replied_menu.png"
			                        title="#{msgs.pvt_msgs_replied}" 
			                        alt="#{msgs.pvt_msgs_replied}" />
				</f:facet>
				<h:graphicImage value="/images/replied_flag.png" rendered="#{rcvdItems.replied}"
									title="#{msgs.pvt_replied}"
									alt="#{msgs.pvt_replied}" />			 
		  	</h:column>
			<h:column id="_msg_subject">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_subject}"/>
		    </f:facet>
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" immediate="true" title=" #{rcvdItems.msg.title}">
            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText styleClass="unreadMsg" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  </h:column>			
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.msg.author}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.author}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title == 'pvt_sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_to}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>		  
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_date}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_label}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.label}"/>
		  </h:column>
		</mf:hierPvtMsgDataTable>
		
<%-- Added if user clicks Check All --%>
    <script language="Javascript" type="text/javascript">
     // setting number checked just in case Check All being processed
     // needed to 'enable' bulk operations
     numberChecked = <h:outputText value="#{PrivateMessagesTool.numberChecked}" />;

     toggleBulkOperations(numberChecked > 0, 'prefs_pvt_form');
     </script>
     
<%
}else{
//user is an anon user, just show a message saying they can't use this tool:
%>

<h:outputText value="#{msgs.pvt_anon_warning}" styleClass="information"/>

<%
}
%>

		 </h:form>
	</sakai:view>
</f:view>
