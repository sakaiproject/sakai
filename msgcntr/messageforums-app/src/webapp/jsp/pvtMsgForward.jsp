<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>


<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view title="#{msgs.pvt_forward}">
    <h:form id="pvtMsgForward">
    
    <h:panelGroup>
          	<f:verbatim><div class="breadCrumb"><h3></f:verbatim>
				  <h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
				  	<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
				  	<f:verbatim><h:outputText value=" / " /></f:verbatim>
				  </h:panelGroup>
	  		      <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}" title=" #{msgs.cdfm_message_forums}"/>
	              <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{PrivateMessagesTool.msgNavMode}" title=" #{PrivateMessagesTool.msgNavMode}"/><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				<h:commandLink action="#{PrivateMessagesTool.processDisplayMessages}" value="#{PrivateMessagesTool.detailMsg.msg.title}" title=" #{PrivateMessagesTool.detailMsg.msg.title}"/><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				<h:outputText value="#{msgs.pvt_forward}" />
			<f:verbatim></h3></div></f:verbatim>
	</h:panelGroup>
    	
	<%-- gsilver:commenting this header out as redundant--%>		
      <%-- gsilver:<sakai:tool_bar_message value="#{msgs.pvt_reply}" />--%> 

			<div class="instruction">
 			  <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline"/>
		  </div>
		  
		  <h:outputLink rendered="#{PrivateMessagesTool.renderPrivacyAlert}" value="#{PrivateMessagesTool.privacyAlertUrl}" target="_blank" >
		  	 <sakai:instruction_message value="#{PrivateMessagesTool.privacyAlert}"/>
		  </h:outputLink>
		  
		  <h:messages styleClass="alertMessage" id="errorMessages" />
		  
		   <h:panelGrid styleClass="jsfFormTable" columns="2" summary="layout">
			  <h:panelGroup styleClass="shorttext required">
				  <h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/>
					<h:outputLabel for="list1"><h:outputText value="#{msgs.pvt_select_forward_recipients}"/></h:outputLabel>
			  </h:panelGroup>
			  <h:panelGroup styleClass="shorttext">
					<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}" size="5" style="width: 20em;">
		         <f:selectItems value="#{PrivateMessagesTool.totalComposeToList}"/>
		       </h:selectManyListbox>
				</h:panelGroup>
		  
		  
		 
				
				<h:panelGroup styleClass="shorttext">
  					<h:outputLabel for="sent_as" ><h:outputText value="#{msgs.pvt_send}" /></h:outputLabel>
  				</h:panelGroup>
  				<h:panelGroup>
					<h:selectOneRadio id="sent_as" value="#{PrivateMessagesTool.composeSendAsPvtMsg}" layout="pageDirection"  style="margin:0" styleClass="checkbox">
		    			  <f:selectItem itemValue="yes" itemLabel="#{msgs.pvt_send_as_private}"/>
		    			  <f:selectItem itemValue="no" itemLabel="#{msgs.pvt_send_as_email}"/>
    			    </h:selectOneRadio>
				</h:panelGroup>
				
				<h:panelGroup  styleClass="shorttext">
					<h:outputLabel for="viewlist" ><h:outputText value="#{msgs.pvt_label}"/></h:outputLabel>
			  </h:panelGroup>
			  <h:panelGroup>
					<h:selectOneListbox size="1" id="viewlist" value="#{PrivateMessagesTool.selectedLabel}">
            	  <f:selectItem itemValue="Normal" itemLabel="#{msgs.pvt_priority_normal}"/>
            	  <f:selectItem itemValue="Low" itemLabel="#{msgs.pvt_priority_low}"/>
            	  <f:selectItem itemValue="High" itemLabel="#{msgs.pvt_priority_high}"/>
          	  </h:selectOneListbox> 
				</h:panelGroup>
				
				<h:panelGroup styleClass="shorttext required">
  					<h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/>
  					<h:outputLabel for="subject" ><h:outputText value="#{msgs.pvt_subject}"  /></h:outputLabel>
  			</h:panelGroup>
  			<h:panelGroup styleClass="shorttext">
					<h:inputText value="#{PrivateMessagesTool.replyToSubject}" id="subject" size="45" />
				</h:panelGroup>		
				
			</h:panelGrid>
			
			
			
			<h4><h:outputText value="#{msgs.pvt_message}" /></h4>
			
			
			<%--SAKAI-10505 --%>
			
		
		   	
		   	
	      <sakai:rich_text_area rows="17" columns="70"  value="#{PrivateMessagesTool.replyToBody}" />	 
		   	
		   	<script language="javascript" type="text/javascript">
		   	
 
	          
	        function FCKeditor_OnComplete(editorInstance )
	        {
	      
	           editorInstance.SetHTML(InsertHTML());
	         
	        }
	        
	        function attachment() {
	        	 var attachment; 
	        	 var hasAttachment = <h:outputText value="#{!empty PrivateMessagesTool.detailMsg.attachList}" />;
//	        	 alert(hasAttachment);
	        	 attachment = (hasAttachment) ? <h:dataTable value="#{PrivateMessagesTool.detailMsg.attachList}" var="eachAttach"  styleClass="attachListJSF" ><h:column><h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-excel'}" alt=""/><h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/html'}" alt=""/><h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/pdf'}" alt=""/><h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/vnd.ms-powerpoint'}" alt=""/><h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachment.attachmentType == 'text/plain'}" alt=""/><h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachment.attachmentType == 'application/msword'}" alt=""/><h:outputLink value="#{eachAttach.url}" target="_blank"><h:outputText value="#{eachAttach.attachment.attachmentName}"/></h:outputLink></h:column></h:dataTable> : '';

 //            alert(attachment);
 //             alert(attachment.toString());
              return attachment;
         	}
         function InsertHTML() 
            { 
            
             var message = <mf:htmlShowArea value="#{PrivateMessagesTool.detailMsg.msg.body}" id="htmlMsgText" hideBorder="true" />;
                                 
              var finalhtml = '<b><i><h:outputText value="#{msgs.pvt_forward}" /> </i></b><br><br>' + 
               '<b><h:outputText value="#{msgs.pvt_authby}" /></b> <h:outputText value="#{PrivateMessagesTool.detailMsg.msg.author}" /> <h:outputText value=" #{msgs.pvt_openb}" /> <h:outputText value="#{PrivateMessagesTool.detailMsg.msg.created}" ><f:convertDateTime pattern="#{msgs.date_format}" /></h:outputText><h:outputText value=" #{msgs.pvt_closeb}" /> <br>' +
               '<b> <h:outputText value="#{msgs.pvt_to}" /></b> <h:outputText value="#{PrivateMessagesTool.detailMsg.msg.recipientsAsText}" /><br>' +
               '<b><h:outputText value="#{msgs.pvt_subject}" /></b> <h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" /> <br>' +
               '<b><h:outputText value="#{msgs.pvt_label}" /></b> <h:outputText value="#{PrivateMessagesTool.detailMsg.msg.label}" /><br>'+
               '<b><h:outputText value="#{msgs.pvt_att}" rendered="#{!empty PrivateMessagesTool.detailMsg.attachList}"/></b>'  + attachment().toString() +
               message.toString();
 
              return finalhtml;
          
            }
                 
            </script>
            
          
        
      	
      <sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgForwardSend}" value="#{msgs.pvt_send}" accesskey="s" styleClass="active" />
        <%--<sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplySaveDraft}" value="Save Draft" />--%>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancelToDetailView}" value="#{msgs.pvt_cancel}" accesskey="x" />
      </sakai:button_bar>
    </h:form>

  </sakai:view>
</f:view> 


	   			
