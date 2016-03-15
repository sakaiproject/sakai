<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf"%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_settings}">
<!--jsp/privateMsg/pvtMsgSettings.jsp-->
	<%-- gsilver:moved this function here from the top  to avoid validation errors (content before the DOCTYPE)--%>
	<script language="Javascript" type="text/javascript">
		function displayEmail() {
	
			document.forms[0].email.disabled = false
		}
	</script>
<h:form id="pvtMsgSettings">
       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
			<h1><h:outputText value="#{msgs.pvt_msgs_label} #{msgs.pvt_settings}" /></h1>
			<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/>
			
			    <h:panelGroup styleClass="shorttext">
					  <h:outputLabel for="" ><h:outputText value="#{msgs.pvt_actpvtmsg1}"/></h:outputLabel>
					</h:panelGroup>
					<h:panelGroup >
					<h:selectOneRadio id="activate"	value="#{PrivateMessagesTool.activatePvtMsg}"
							                              layout="pageDirection"  styleClass="checkbox inlineForm table-inline">
						<f:selectItem itemValue="yes" itemLabel="#{msgs.pvt_yes}" />
						<f:selectItem itemValue="no" itemLabel="#{msgs.pvt_no}" />
					</h:selectOneRadio>
				</h:panelGroup>
			  
			  <h2><h:outputText value="#{msgs.pvt_personal_settings}"  rendered="#{!PrivateMessagesTool.emailForwardDisabled}"/></h2>

			    <h:panelGroup styleClass="shorttext">
			      <h:outputLabel for=""><h:outputText	value="#{msgs.pvt_autofor1}" /></h:outputLabel>
			    </h:panelGroup>
			    <h:panelGroup>
			      <h:selectOneRadio immediate="true" id="fwd_msg"
				value="#{PrivateMessagesTool.forwardPvtMsg}"
				onchange="this.form.submit();"
				valueChangeListener="#{PrivateMessagesTool.processPvtMsgSettingsRevise}"
				layout="pageDirection"
				styleClass="checkbox inlineForm table-inline">
			          <f:selectItem itemValue="yes" itemLabel="#{msgs.pvt_yes}" />
				  <f:selectItem itemValue="no" itemLabel="#{msgs.pvt_no}" />
			      </h:selectOneRadio> 
			    </h:panelGroup>
		           <h:panelGroup styleClass="shorttext">
		             <h:outputLabel for="fwd_email"><h:outputText value="#{msgs.pvt_emailfor}" /></h:outputLabel>
		           </h:panelGroup>
		           <h:panelGroup styleClass="shorttext">
		             <h:inputText value="#{PrivateMessagesTool.forwardPvtMsgEmail}" id="fwd_email"
		               disabled="#{PrivateMessagesTool.forwardPvtMsg == 'no'}" />
		           </h:panelGroup>
		         
		         
		       <h:panelGroup rendered="#{PrivateMessagesTool.instructor}">	  
		         <f:verbatim><h2></f:verbatim><h:outputText value="#{msgs.pvt_site_settings}" /><f:verbatim></h2></f:verbatim>
				  
                         <h:panelGroup styleClass="shorttext" rendered="#{!PrivateMessagesTool.emailCopyDisabled}">
                           <h:outputLabel for="" ><h:outputText value="#{msgs.pvt_sendemailout}"/></h:outputLabel>
                         </h:panelGroup>
                         <h:panelGroup rendered="#{!PrivateMessagesTool.emailCopyDisabled}">
                           <h:selectOneRadio id="email_sendout" value="#{PrivateMessagesTool.sendToEmail}"
                               layout="pageDirection"  styleClass="checkbox inlineForm">
                             <f:selectItem itemValue="0" itemLabel="#{msgs.pvt_sendemail_0}" />
                             <f:selectItem itemValue="1" itemLabel="#{msgs.pvt_sendemail_1}" />
                             <f:selectItem itemValue="2" itemLabel="#{msgs.pvt_sendemail_2}" />
                           </h:selectOneRadio>
                         </h:panelGroup>
  
			    <h:panelGroup styleClass="shorttext" rendered="#{PrivateMessagesTool.currentSiteHasGroups}">
			    	<h:outputText value="#{msgs.hiddenGroups_hiddenGroups}"/>
			    </h:panelGroup>
			    <h:panelGroup styleClass="shorttext" rendered="#{PrivateMessagesTool.currentSiteHasGroups}">
			    	<h:outputText value="#{msgs.hiddenGroups_addGroup}: "/>
			    	<h:selectOneListbox size="1" id="nonHiddenGroup" value ="#{PrivateMessagesTool.selectedNonHiddenGroup}" onchange="this.form.submit();"
						                  valueChangeListener="#{PrivateMessagesTool.processActionAddHiddenGroup}">
				      <f:selectItems value="#{PrivateMessagesTool.nonHiddenGroups}"/>
				    </h:selectOneListbox>
				    
				    <h:dataTable styleClass="listHier lines nolines" id="hiddenGroups" value="#{PrivateMessagesTool.hiddenGroups}" var="hiddenGroup" rendered="#{!empty PrivateMessagesTool.hiddenGroups}"
				    	cellpadding="0" cellspacing="0">
			  			<h:column>			  				
	  						<h:outputText value="#{hiddenGroup.groupId}"/>
	  						<h:commandLink action="#{PrivateMessagesTool.processActionRemoveHiddenGroup}">
				      			<f:param value="#{hiddenGroup.groupId}" name="groupId"/>
				      			<h:graphicImage url="/images/silk/cross.png" title="#{msgs.hiddenGroups_remove}" alt="#{msgs.hiddenGroups_remove}" style="margin-left:.5em"/>
				      		</h:commandLink>
	  					</h:column>
	  				</h:dataTable>
			    </h:panelGroup>
			
		      </h:panelGroup>
						

			<sakai:button_bar>
				<sakai:button_bar_item	action="#{PrivateMessagesTool.processPvtMsgSettingsSave}"
					                     value="#{msgs.pvt_saveset}" accesskey="s" styleClass="active" />
				<sakai:button_bar_item	action="#{PrivateMessagesTool.processPvtMsgCancel}"
					                     value="#{msgs.pvt_cancel}" accesskey="x" />
			</sakai:button_bar>

		</h:form>

	</sakai:view>
</f:view>
