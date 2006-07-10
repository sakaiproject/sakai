<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view title="#{msgs.pvt_delcon}">
    <h:form id="pvtMsgDelete">
    <div class="breadCrumb">
  	    <h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"/> /
  	    <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.cdfm_message_pvtarea}" title=" #{msgs.cdfm_message_pvtarea}"/> /
		  <h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{PrivateMessagesTool.msgNavMode}" title=" #{PrivateMessagesTool.msgNavMode}" /> /
		  <h:outputText value="#{msgs.pvt_delcon}" />
		</div>	
		<sakai:tool_bar_message value="#{msgs.pvt_delcon}" />
		
		<h:messages styleClass="alertMessage" id="errorMessages" /> 

    <sakai:panel_titled title="">
	  <h:dataTable styleClass="listHier" id="pvtmsgdel" width="100%"  value="#{PrivateMessagesTool.selectedDeleteItems}" var="delItems">   
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_subject}"/>
		    </f:facet>
		      <h:outputText value=" #{delItems.msg.title}"/>              
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" />								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{delItems.msg.hasAttachments}" alt="#{msgs.msg_has_attach}" />			 
			</h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{delItems.msg.author}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_date}"/>
		    </f:facet>
		     <h:outputText value="#{delItems.msg.created}">
		         <f:convertDateTime pattern="#{msgs.date_format}" />
		     </h:outputText>
		  </h:column>
		</h:dataTable>
		</sakai:panel_titled>

    <sakai:button_bar>
      <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMultiDelete}" value="#{msgs.pvt_delmsgs}" accesskey="x" />
      <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
    </sakai:button_bar>
    
		 </h:form>

	</sakai:view>
</f:view>
