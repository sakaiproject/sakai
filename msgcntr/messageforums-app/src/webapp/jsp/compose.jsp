<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="compose">
  			<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
  			<h:commandLink action="#{PrivateMessagesTool.processHpView}" value="#{msgs.cdfm_message_pvtarea}" /> /
				<h:outputText value="#{msgs.pvt_compose1}" />
				<sakai:tool_bar_message value="#{msgs.pvt_pvtcompose}" />
 			  <div class="instruction">
  			    <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" style="color: red"/>
			  </div>
			  
			  <h:outputLink rendered="#{PrivateMessagesTool.renderPrivacyAlert}" value="#{PrivateMessagesTool.privacyAlertUrl}" target="_blank" >
			  	 <sakai:instruction_message value="#{PrivateMessagesTool.privacyAlert}"/>
			  </h:outputLink>
			  <h:messages styleClass="alertMessage" id="errorMessages" /> 

				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="#{msgs.pvt_star}" style="color: red"/>
          			<h:outputText value="#{msgs.pvt_to}"/>		
              </td>
              <td align="left">
      
          			<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}" size="5" style="width:200px;">
            			<f:selectItems value="#{PrivateMessagesTool.totalComposeToList}"/>
          			</h:selectManyListbox>      
          			
              </td>                           
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="#{msgs.pvt_send}" />
              </td>
              <td align="left">
              	<h:selectOneRadio value="#{PrivateMessagesTool.composeSendAsPvtMsg}" layout="pageDirection">
  			    			<f:selectItem itemValue="yes" itemLabel="#{msgs.pvt_send_as_private}"/>
  			    			<f:selectItem itemValue="no" itemLabel="#{msgs.pvt_send_as_email}"/>
			    			</h:selectOneRadio>
              </td>
            </tr>
            <tr>
              <td align="left">
              	<h:outputText value="#{msgs.pvt_star}" style="color: red"/>
                <h:outputText value="#{msgs.pvt_subject}" />
              </td>
              <td align="left">
              	<h:inputText value="#{PrivateMessagesTool.composeSubject}" style="width:300px;"/>
              </td>
            </tr>                                   
          </table>
        </sakai:group_box>

	      <sakai:group_box>
	        <sakai:panel_edit>
	          <sakai:doc_section> 
	          	<h:outputText value="#{msgs.pvt_message}" />  
	            <sakai:rich_text_area value="#{PrivateMessagesTool.composeBody}" rows="17" columns="70"/>
	          </sakai:doc_section>    
	        </sakai:panel_edit>
	      </sakai:group_box>
<%--********************* Attachment *********************--%>	
	      <sakai:group_box>
					<h4><h:outputText value="#{msgs.pvt_att}"/></h4>
	        
	        <sakai:doc_section>	        
	        	<h:outputText value="#{msgs.pvt_noatt}" rendered="#{empty PrivateMessagesTool.attachments}"/>
	        </sakai:doc_section>
	        
	        <sakai:doc_section>
	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{PrivateMessagesTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" />
	          </sakai:button_bar>
	        </sakai:doc_section>
	        
					<h:dataTable styleClass="listHier" id="attmsg" width="100%" value="#{PrivateMessagesTool.attachments}" var="eachAttach" >
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Title"/>
							</f:facet>
							<sakai:doc_section>
								<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
								<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
								<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
								<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
								<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
							
								<h:outputText value="#{eachAttach.attachmentName}"/>
							</sakai:doc_section>
							
							
							<sakai:doc_section>
								<h:commandLink action="#{PrivateMessagesTool.processDeleteAttach}" 
									immediate="true"
									onfocus="document.forms[0].onsubmit();">
									<h:outputText value="     Remove"/>
<%--									<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
									<f:param value="#{eachAttach.attachmentId}" name="pvmsg_current_attach"/>
								</h:commandLink>
							</sakai:doc_section>
							
						
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.pvt_attsize}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentSize}"/>
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
		  			    <h:outputText value="#{msgs.pvt_atttype}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentType}"/>
						</h:column>
						<%--
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.pvt_noatt}Created by" />
							</f:facet>
							<h:outputText value="#{eachAttach.createdBy}"/>
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Last modified by" />
							</f:facet>
							<h:outputText value="#{eachAttach.lastModifiedBy}"/>
						</h:column>
						--%>
						</h:dataTable>   
					</sakai:group_box>   
        		
<%--********************* Label *********************
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="#{msgs.pvt_label}"/>
              </td>
              <td align="left">
 							  <h:selectOneListbox size="1" id="viewlist">
            		  <f:selectItem itemLabel="Normal" itemValue="none"/>
          			</h:selectOneListbox>  
              </td>                           
            </tr>                                
          </table>
        </sakai:group_box>
--%>		        
      <sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSend}" value="#{msgs.pvt_send}" />
        <%--<sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSaveDraft}" value="#{msgs.pvt_savedraft}" />--%>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgComposeCancel}" value="#{msgs.pvt_cancel}" />
      </sakai:button_bar>
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

