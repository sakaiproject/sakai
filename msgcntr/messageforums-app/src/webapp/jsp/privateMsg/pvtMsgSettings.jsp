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
       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
			<sakai:tool_bar_message value="#{msgs.pvt_msgs_label} #{msgs.pvt_settings}" />
			<h:messages styleClass="alertMessage" id="errorMessages" />

			  <h:panelGrid styleClass="jsfFormTable" columns="2" summary="layout" 
			  				rendered="#{PrivateMessagesTool.instructor && PrivateMessagesTool.messagesandForums}">
			    <h:panelGroup styleClass="shorttext">
					  <h:outputLabel for="" ><h:outputText value="#{msgs.pvt_actpvtmsg1}"/></h:outputLabel>
					</h:panelGroup>
					<h:panelGroup >
					  <h:selectOneRadio id="activate"	value="#{PrivateMessagesTool.activatePvtMsg}"
							                              layout="pageDirection"  styleClass="checkbox inlineForm">
							  <f:selectItem itemValue="yes" itemLabel="#{msgs.pvt_yes}" />
							  <f:selectItem itemValue="no" itemLabel="#{msgs.pvt_no}" />
						</h:selectOneRadio>
				  </h:panelGroup>
			  </h:panelGrid>

	      <h:panelGrid styleClass="jsfFormTable" columns="2" summary="layout" >
			    <h:panelGroup styleClass="shorttext">
					  <h:outputLabel for=""><h:outputText	value="#{msgs.pvt_autofor1}" /></h:outputLabel>
					</h:panelGroup>
					<h:panelGroup>
					  <h:selectOneRadio immediate="true" id="fwd_msg"
				    	                  value="#{PrivateMessagesTool.forwardPvtMsg}"
						                  onchange="this.form.submit();"
						                  valueChangeListener="#{PrivateMessagesTool.processPvtMsgSettingsRevise}"
						                  layout="pageDirection"
										   styleClass="checkbox inlineForm"
										   >
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
				  
			  </h:panelGrid>
			  
			 <%--
  							<h:selectOneRadio value="#{PrivateMessagesTool.forwardPvtMsg}" layout="pageDirection" >
    							<f:selectItem itemValue='yes' itemLabel='Yes'/>
    							<f:selectItem itemValue='no' itemLabel='No'/>
  							</h:selectOneRadio>
  				      --%>
  				 



			<sakai:button_bar>
				<sakai:button_bar_item	action="#{PrivateMessagesTool.processPvtMsgSettingsSave}"
					                     value="#{msgs.pvt_saveset}" accesskey="s" styleClass="active" />
				<sakai:button_bar_item	action="#{PrivateMessagesTool.processPvtMsgCancel}"
					                     value="#{msgs.pvt_cancel}" accesskey="x" />
			</sakai:button_bar>

		</h:form>

	</sakai:view>
</f:view>
