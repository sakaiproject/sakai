<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>


<f:view>
	<sakai:view title="#{msgs.pvt_rcvd_msgs}">
<!--jsp/privateMsg/pvtMsgEx.jsp-->

		<script>includeLatestJQuery("msgcntr");</script>
		<script src="/messageforums-tool/js/sak-10625.js"></script>
		<script src="/messageforums-tool/js/forum.js"></script>
		<script src="/messageforums-tool/js/messages.js"></script>
        <script>
            $(document).ready(function() {
                var menuLink = $('#messagesMainMenuLink');
                var menuLinkSpan = menuLink.closest('span');
                menuLinkSpan.addClass('current');
                menuLinkSpan.html(menuLink.text());
            });

            <f:verbatim rendered="#{PrivateMessagesTool.canUseTags}">
                initTagSelector("prefs_pvt_form");
            </f:verbatim>
        </script>

		<h:form id="prefs_pvt_form">
			<%@ include file="/jsp/privateMsg/pvtMenu.jsp" %>

			<%@ include file="topNav.jsp" %>

			<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/>
			<%@ include file="msgHeader.jsp"%>

	  <h:dataTable styleClass="listHier lines nolines" cellpadding="0" cellspacing="0"  id="pvtmsgs" width="100%" value="#{PrivateMessagesTool.searchPvtMsgs}" var="rcvdItems" 
		rendered="#{PrivateMessagesTool.selectView != 'threaded'}" columnClasses="#{PrivateMessagesTool.calculateColumnClass()}">
		  <h:column>
		    <f:facet name="header">
				<h:panelGroup>
					<h:selectBooleanCheckbox id="checkAll" title="#{msgs.cdfm_checkall}"/>
					<h:outputText value=" "/>
					<h:outputLabel value="#{msgs.cdfm_checkall}"/>
				</h:panelGroup>
		    </f:facet>
				<h:selectBooleanCheckbox value="#{rcvdItems.isSelected}" onclick="updateCount(this.checked); toggleBulkOperations(anyChecked(), 'prefs_pvt_form');" />
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		        <h:outputText value="" styleClass="bi bi-paperclip" escape="false" />
				<h:outputText value="#{msgs.sort_attachment}" styleClass="sr-only" />
			</f:facet>
			<h:outputText value="" styleClass="bi bi-paperclip" escape="false" rendered="#{rcvdItems.msg.hasAttachments}" />
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		        <h:outputText value="" styleClass="bi bi-reply-fill" escape="false" />
				<h:outputText value="#{msgs.pvt_msgs_replied}" styleClass="sr-only" />
			</f:facet>
			<h:outputText value="" styleClass="bi bi-reply-fill" escape="false" rendered="#{rcvdItems.replied}" />
		  </h:column>
		  <h:column>
		    <f:facet name="header">
				<h:outputText value="#{msgs.pvt_subject}"/>
		    </f:facet>
			<h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" title="#{rcvdItems.msg.title}" immediate="true">

            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText styleClass="unreadMsg" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
			<h:outputText styleClass="skip" value="#{msgs.pvt_openb}#{msgs.pvt_unread}#{msgs.pvt_closeb}" rendered="#{!rcvdItems.hasRead}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
				<h:outputText value="#{msgs.pvt_date}"/>
		    </f:facet>
			 <%-- This hidden date is for sorting purposes using datetables --%>
		     <h:outputText value="#{((PrivateMessagesTool.userId ne rcvdItems.msg.createdBy) and (! empty rcvdItems.msg.scheduledDate)) ? rcvdItems.msg.scheduledDate : rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}" styleClass="d-none">
			     <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		     <h:outputText value="#{((PrivateMessagesTool.userId ne rcvdItems.msg.createdBy) and (! empty rcvdItems.msg.scheduledDate)) ? rcvdItems.msg.scheduledDate : rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
			 <%-- This hidden date is for sorting purposes using datetables --%>
			 <h:outputText value="#{((PrivateMessagesTool.userId ne rcvdItems.msg.createdBy) and (! empty rcvdItems.msg.scheduledDate)) ? rcvdItems.msg.scheduledDate : rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}" styleClass="d-none">
			     <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		   <h:outputText styleClass="unreadMsg" value="#{((PrivateMessagesTool.userId ne rcvdItems.msg.createdBy) and (! empty rcvdItems.msg.scheduledDate)) ? rcvdItems.msg.scheduledDate : rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}">
			   <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_received'}">
		    <f:facet name="header">
				<h:outputText value="#{msgs.pvt_date_scheduler}"/>
		    </f:facet>
			 <%-- This hidden date is for sorting purposes using datetables --%>
		     <h:outputText value="#{rcvdItems.msg.scheduledDate}" rendered="#{rcvdItems.hasRead}" styleClass="d-none">
			     <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		     <h:outputText value="#{rcvdItems.msg.scheduledDate}" rendered="#{rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
			 <%-- This hidden date is for sorting purposes using datetables --%>
			 <h:outputText value="#{rcvdItems.msg.scheduledDate}" rendered="#{!rcvdItems.hasRead}" styleClass="d-none">
			     <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		   <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.scheduledDate}" rendered="#{!rcvdItems.hasRead}">
			   <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_sent'}">
		    <f:facet name="header">
				<h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.author}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.author}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_received' &&
		  PrivateMessagesTool.selectedTopic.topic.title != 'pvt_drafts' &&
		  PrivateMessagesTool.selectedTopic.topic.title != 'pvt_deleted' &&
		  PrivateMessagesTool.selectedTopic.topic.title != 'pvt_scheduler' }">
		    <f:facet name="header">
				<h:outputText value="#{msgs.pvt_to}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}" />
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
			   <h:outputText value="#{msgs.pvt_label}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.label}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.canUseTags}" headerClass="hidden-xs">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_tags_header}"/>
		    </f:facet>
		    <t:dataList value="#{rcvdItems.tagList}" var="eachTag" >
		      <span class="badge bg-info">
		        <h:outputText value="#{eachTag}"/>
		      </span>
		    </t:dataList>
		  </h:column>
		</h:dataTable>
		
	  <div class="table">
	  <mf:hierPvtMsgDataTable  styleClass="table table-hover table-striped table-bordered" cellpadding="0" cellspacing="0"  id="threaded_pvtmsgs" width="100%" 
	  	value="#{PrivateMessagesTool.searchPvtMsgs}" 
	  	var="rcvdItems" 
	  	rendered="#{PrivateMessagesTool.selectView == 'threaded'}"
	  	expanded="true"
		columnClasses="#{PrivateMessagesTool.calculateColumnClass()}">
		  <h:column>
		    <f:facet name="header">
				<h:panelGroup>
					<h:selectBooleanCheckbox id="checkAll" title="#{msgs.cdfm_checkall}"/>
					<h:outputText value=" "/>
					<h:outputLabel value="#{msgs.cdfm_checkall}"/>
				</h:panelGroup>
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
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_date}"/>
		    </f:facet>
		     <h:outputText value="#{((PrivateMessagesTool.userId ne rcvdItems.msg.createdBy) and (! empty rcvdItems.msg.scheduledDate)) ? rcvdItems.msg.scheduledDate : rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		     <h:outputText styleClass="unreadMsg" value="#{((PrivateMessagesTool.userId ne rcvdItems.msg.createdBy) and (! empty rcvdItems.msg.scheduledDate)) ? rcvdItems.msg.scheduledDate : rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_received'}">
		    <f:facet name="header">
				<h:outputText value="#{msgs.pvt_date_scheduler}"/>
		    </f:facet>
			 <%-- This hidden date is for sorting purposes using datetables --%>
		     <h:outputText value="#{rcvdItems.msg.scheduledDate}" rendered="#{rcvdItems.hasRead}" styleClass="d-none">
			     <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		     <h:outputText value="#{rcvdItems.msg.scheduledDate}" rendered="#{rcvdItems.hasRead}">
			     <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
			 <%-- This hidden date is for sorting purposes using datetables --%>
			 <h:outputText value="#{rcvdItems.msg.scheduledDate}" rendered="#{!rcvdItems.hasRead}" styleClass="d-none">
			     <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		   <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.scheduledDate}" rendered="#{!rcvdItems.hasRead}">
			   <f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{PrivateMessagesTool.userTimeZone}" locale="#{PrivateMessagesTool.userLocale}"/>
			 </h:outputText>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.author}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.msg.author}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectedTopic.topic.title != 'pvt_received' &&
		  PrivateMessagesTool.selectedTopic.topic.title != 'pvt_drafts' &&
		  PrivateMessagesTool.selectedTopic.topic.title != 'pvt_deleted' &&
		  PrivateMessagesTool.selectedTopic.topic.title != 'pvt_scheduler' }">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_to}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText styleClass="unreadMsg" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_label}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.label}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.canUseTags}" headerClass="hidden-xs">
		    <f:facet name="header">
		       <h:outputText styleClass="hidden-xs" value="#{msgs.pvt_tags_header}"/>
		    </f:facet>
		    <t:dataList value="#{rcvdItems.tagList}" var="eachTag" >
		      <span class="badge bg-info">
		        <h:outputText value="#{eachTag}"/>
		      </span>
		    </t:dataList>
		  </h:column>
		</mf:hierPvtMsgDataTable>
		</div>

<%-- Added if user clicks Check All --%>
    <script>
     // setting number checked just in case Check All being processed
     // needed to 'enable' bulk operations
     numberChecked = <h:outputText value="#{PrivateMessagesTool.numberChecked}" />;

     toggleBulkOperations(numberChecked > 0, 'prefs_pvt_form');
     </script>
 
   	</h:form>

	</sakai:view>
</f:view>
