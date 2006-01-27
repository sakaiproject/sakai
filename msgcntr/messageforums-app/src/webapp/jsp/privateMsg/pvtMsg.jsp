<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
	<sakai:view_container title="#{msgs.pvtarea_name}">
	<sakai:view_content>
		<h:form id="prefs_pvt_form">
			<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>		
			
			<%--<sakai:tool_bar_message value="#{msgs.pvt_pvtmsg}- #{PrivateMessagesTool.msgNavMode}" /> --%>
			
        <table ><tr>
        <td align="left" width="75%">        	
        	  <h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
  					<h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}"/> /
 						<h:outputText value="#{PrivateMessagesTool.msgNavMode}"/>
        	</td><td nowrap align="right" >
				  <h:outputText   value="Previous Folder"  rendered="#{!PrivateMessagesTool.selectedTopic.hasPreviousTopic}" />
				  &nbsp;&nbsp;
	    		<h:commandLink action="#{PrivateMessagesTool.processDisplayPreviousTopic}" value="Previous Folder"  rendered="#{PrivateMessagesTool.selectedTopic.hasPreviousTopic}">
		  			<f:param value="#{PrivateMessagesTool.selectedTopic.previousTopicTitle}" name="previousTopicTitle"/>
		  		</h:commandLink>
		  		&nbsp;&nbsp;
		  		<h:outputText   value="Next Folder" rendered="#{!PrivateMessagesTool.selectedTopic.hasNextTopic}" />
		  		<h:commandLink action="#{PrivateMessagesTool.processDisplayNextTopic}" value="Next Folder" rendered="#{PrivateMessagesTool.selectedTopic.hasNextTopic}">
		  			<f:param value="#{PrivateMessagesTool.selectedTopic.nextTopicTitle}" name="nextTopicTitle"/>
		  		</h:commandLink>
	  			</td></tr></table>
  	
			<h:messages styleClass="alertMessage" id="errorMessages" /> 
  	
  		<%@include file="msgHeader.jsp"%>
			<br><br><br>

	  <h:dataTable styleClass="listHier" id="pvtmsgs" width="100%" value="#{PrivateMessagesTool.decoratedPvtMsgs}" var="rcvdItems" 
	  	rendered="#{PrivateMessagesTool.selectView != 'threaded'}">   
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded'}">
		    <f:facet name="header">
 					<h:commandLink action="#{PrivateMessagesTool.processCheckAll}" value="#{msgs.cdfm_checkall}" rendered="#{PrivateMessagesTool.msgNavMode == 'Deleted'}" />
		     <%--<h:commandButton alt="SelectAll" image="/sakai-messageforums-tool/images/checkbox.gif" action="#{PrivateMessagesTool.processSelectAllJobs}"/>--%>
		    </f:facet>
		    <h:selectBooleanCheckbox value="#{rcvdItems.isSelected}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView != 'threaded'}">
		    <f:facet name="header">					
			  <h:commandLink>
		        <h:graphicImage value="/images/attachment.gif"
		                        title="#{msgs.sort_attachment}"/>
		        <h:graphicImage value="/images/sortascending.gif" style="border:0" 
    	                        title="#{msgs.sort_attachment_asc}" alt="#{msgs.sort_attachment_asc}"
    	                        rendered="#{PrivateMessagesTool.sortType == 'attachment_asc'}"/>
    	        <h:graphicImage value="/images/sortdescending.gif" style="border:0" 
    	                        title="#{msgs.sort_attachment_desc}" alt="#{msgs.sort_attachment_desc}"
    	                        rendered="#{PrivateMessagesTool.sortType == 'attachment_desc'}"/>    	                       
    	        <f:param name="sortColumn" value="attachment"/>
    	      </h:commandLink>
			</f:facet>
			<h:graphicImage value="/images/attachment.gif" rendered="#{rcvdItems.msg.hasAttachments}"/>			 
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
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" immediate="true">
            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText style="font-weight:bold" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
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
		     <h:outputText value="#{rcvdItems.msg.createdBy}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText style="font-weight:bold" value="#{rcvdItems.msg.createdBy}" rendered="#{!rcvdItems.hasRead}"/>
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
		     <h:outputText style="font-weight:bold" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
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
		     <h:outputText value="#{rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText style="font-weight:bold" value="#{rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}"/>
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
					<h:graphicImage value="/images/attachment.gif"/>								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{rcvdItems.msg.hasAttachments}"/>			 
			</h:column>
			<h:column id="_msg_subject"
				rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_subject}"/>
		    </f:facet>
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" immediate="true">
            <h:outputText value=" #{rcvdItems.msg.title}" rendered="#{rcvdItems.hasRead}"/>
            <h:outputText style="font-weight:bold" value=" #{rcvdItems.msg.title}" rendered="#{!rcvdItems.hasRead}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  </h:column>			
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded' && PrivateMessagesTool.msgNavMode != 'Sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.msg.createdBy}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText style="font-weight:bold" value="#{rcvdItems.msg.createdBy}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded' && PrivateMessagesTool.msgNavMode == 'Sent'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_to}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.sendToStringDecorated}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText style="font-weight:bold" value="#{rcvdItems.sendToStringDecorated}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>		  
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_date}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.created}" rendered="#{rcvdItems.hasRead}"/>
		     <h:outputText style="font-weight:bold" value="#{rcvdItems.msg.created}" rendered="#{!rcvdItems.hasRead}"/>
		  </h:column>
		  <h:column rendered="#{PrivateMessagesTool.selectView == 'threaded'}">
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_label}"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.label}"/>
		  </h:column>
		</mf:hierPvtMsgDataTable>
		
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
