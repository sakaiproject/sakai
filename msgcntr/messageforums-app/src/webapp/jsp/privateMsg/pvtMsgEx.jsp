<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>


<f:view>
	<sakai:view title="#{msgs.pvt_rcvd_msgs}">
<!--jsp/privateMsg/pvtMsgEx.jsp-->
		<h:form id="prefs_form_search">
		       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
			<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>		
			
			<%@ include file="topNav.jsp" %>

			<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/>
			<%@ include file="msgHeader.jsp"%>


	  <h:dataTable styleClass="listHier lines nolines" cellpadding="0" cellspacing="0"  id="pvtmsgs" width="100%" value="#{PrivateMessagesTool.searchPvtMsgs}" var="rcvdItems" 
	  	rendered="#{PrivateMessagesTool.selectView != 'threaded'}" columnClasses="attach,attach,specialLink,bogus,bogus,bogus">   
		  <h:column>
		    <f:facet name="header">
 					<h:commandLink action="#{PrivateMessagesTool.processCheckAll}" value="#{msgs.cdfm_checkall}" 
 					               title="#{msgs.cdfm_checkall}" />
		     <%--<h:commandButton alt="SelectAll" image="/messageforums-tool/images/checkbox.gif" action="#{PrivateMessagesTool.processSelectAllJobs}"/>--%>
		    </f:facet>
				<h:selectBooleanCheckbox value="#{rcvdItems.isSelected}" onclick="updateCount(this.checked); toggleBulkOperations(anyChecked(), 'prefs_form_search');" />
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}"/>								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{rcvdItems.msg.hasAttachments}" alt="#{msgs.msg_has_attach}" />			 
			</h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_subject}"/>
		    </f:facet>
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" immediate="true" title=" #{rcvdItems.msg.title}">
            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText styleClass="unreadMsg" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  </h:column>			
		  <h:column rendered="#{PrivateMessagesTool.msgNavMode != 'Sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.msg.author}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.author}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.msgNavMode == 'Sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_to}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}" />
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
		</h:dataTable>
		
	  <div class="table-responsive">
	  <mf:hierPvtMsgDataTable  styleClass="table table-hover table-striped table-bordered" cellpadding="0" cellspacing="0"  id="threaded_pvtmsgs" width="100%" 
	  	value="#{PrivateMessagesTool.searchPvtMsgs}" 
	  	var="rcvdItems" 
	  	rendered="#{PrivateMessagesTool.selectView == 'threaded'}"
	  	expanded="true"
	  	columnClasses="attach,attach,specialLink,bogus,bogus,bogus">
		  <h:column>
		    <f:facet name="header">
		    	<h:commandLink action="#{PrivateMessagesTool.processCheckAll}" value="#{msgs.cdfm_checkall}" 
 					               title="#{msgs.cdfm_checkall}" />
		    </f:facet>
		    <h:selectBooleanCheckbox value="#{rcvdItems.isSelected}" onclick="updateCount(this.checked); toggleBulkOperations(anyChecked(), 'prefs_form_search');" />
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" />								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{rcvdItems.msg.hasAttachments}" alt="#{msgs.msg_has_attach}" />			 
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
		  <h:column rendered="#{PrivateMessagesTool.msgNavMode != 'pvt_sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.msg.author}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.author}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.msgNavMode == 'pvt_sent'}">
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
		</div>
		
<%-- Added if user clicks Check All --%>
    <script language="Javascript" type="text/javascript">
     // setting number checked just in case Check All being processed
     // needed to 'enable' bulk operations
     numberChecked = <h:outputText value="#{PrivateMessagesTool.numberChecked}" />;

     toggleBulkOperations(numberChecked > 0, 'prefs_form_search');
     </script>
 
   	</h:form>

	</sakai:view>
</f:view>
