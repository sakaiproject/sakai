<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf"%>
<f:loadBundle	basename="org.sakaiproject.tool.messageforums.bundle.Messages"	var="msgs" />
<link href="/sakai-messageforums-tool/css/msgForums.css"	'	rel='stylesheet' type='text/css' />

<script language="Javascript" type="text/javascript">
	function displayEmail() {

	    document.forms[0].email.disabled = false
	}
</script>

<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_settings}">

		<h:form id="pvtMsgSettings">
			<sakai:tool_bar_message 	value="#{msgs.pvt_msgs_label} #{msgs.pvt_settings}" />
			<h:messages styleClass="alertMessage" id="errorMessages" />

			<sakai:panel_titled title="">
			
				<h:panelGrid styleClass="msgHeadings" rendered="#{PrivateMessagesTool.instructor}">
		      <h:outputText value="#{msgs.pvt_actpvtmsg}"/>
		    </h:panelGrid>
				
				<h:panelGroup rendered="#{PrivateMessagesTool.validEmail}">
				</h:panelGroup>
	
			  <h:panelGrid styleClass="jsfFormTable" columns="2" summary="" rendered="#{PrivateMessagesTool.instructor}">
			    <h:panelGroup styleClass="shorttext">
					  <h:outputLabel for="activate" ><h:outputText value="#{msgs.pvt_actpvtmsg1}"/></h:outputLabel>
					</h:panelGroup>
					<h:panelGroup styleClass="checkbox inlineForm">
					  <h:selectOneRadio id="activate"	value="#{PrivateMessagesTool.activatePvtMsg}"
							                              layout="pageDirection" >
							  <f:selectItem itemValue="yes" itemLabel="#{msgs.pvt_yes}" />
							  <f:selectItem itemValue="no" itemLabel="#{msgs.pvt_no}" />
						</h:selectOneRadio>
				  </h:panelGroup>
			  </h:panelGrid>

			</sakai:panel_titled>

			<sakai:panel_titled title="">
		    <h:panelGrid styleClass="msgHeadings">
		      <h:outputText value="#{msgs.pvt_autofor}" />
		    </h:panelGrid>
	
	      <h:panelGrid styleClass="jsfFormTable" columns="2" summary="" >
			    <h:panelGroup styleClass="shorttext">
					  <h:outputLabel for="fwd_msg"><h:outputText	value="#{msgs.pvt_autofor1}" /></h:outputLabel>
					</h:panelGroup>
					<h:panelGroup styleClass="checkbox inlineForm">
					  <h:selectOneRadio immediate="true" id="fwd_msg"
				    	                  value="#{PrivateMessagesTool.forwardPvtMsg}"
						                  onchange="this.form.submit();"
						                  valueChangeListener="#{PrivateMessagesTool.processPvtMsgSettingsRevise}"
						                  layout="pageDirection">
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
  				 
			</sakai:panel_titled>


			<sakai:button_bar>
				<sakai:button_bar_item	action="#{PrivateMessagesTool.processPvtMsgSettingsSave}"
					                     value="#{msgs.pvt_saveset}" accesskey="s" />
				<sakai:button_bar_item	action="#{PrivateMessagesTool.processPvtMsgCancel}"
					                     value="#{msgs.pvt_cancel}" accesskey="c" />
			</sakai:button_bar>

		</h:form>

	</sakai:view>
</f:view>
