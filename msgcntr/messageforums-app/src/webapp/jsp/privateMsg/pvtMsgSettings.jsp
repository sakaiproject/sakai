<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<script language="Javascript">
	function displayEmail() {

	    document.forms[0].email.disabled = false
	}
</script>
  
<f:view>
	<sakai:view_container title="#{msgs.pvtarea_name}">
	<sakai:view_content>
		<h:form id="pvtMsgSettings">
		  <sakai:tool_bar_message value="#{msgs.pvt_pvtmsg}- #{msgs.pvt_settings}" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 
    
  <h4><h:outputText value="#{msgs.pvt_actpvtmsg}" rendered="#{PrivateMessagesTool.instructor}"/></h4>
 				<h:panelGroup rendered="#{PrivateMessagesTool.validEmail}">

				</h:panelGroup>  
 				<sakai:group_box >
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText rendered="#{PrivateMessagesTool.instructor}" value="#{msgs.pvt_actpvtmsg1}"/>		
              </td>
              <td>
                <h:selectOneRadio id="activate"
                                  rendered="#{PrivateMessagesTool.instructor}" 
                                  value="#{PrivateMessagesTool.activatePvtMsg}"                                   
                                  layout="pageDirection">
    							<f:selectItem itemValue="yes" itemLabel="Yes"/>
    							<f:selectItem itemValue="no" itemLabel="No"/>
  							</h:selectOneRadio>
              </td>
            </tr>
          </table>
        </sakai:group_box>
      
  <h4><h:outputText value="#{msgs.pvt_autofor}" /></h4>
 
 				<sakai:group_box >
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="#{msgs.pvt_autofor1}"/>		
              </td>
              <td>
                <h:selectOneRadio immediate="true"
                                  value="#{PrivateMessagesTool.forwardPvtMsg}"
                                  onchange="this.form.submit();"
                                  valueChangeListener="#{PrivateMessagesTool.processPvtMsgSettingsRevise}"
                                  layout="pageDirection">
    							<f:selectItem itemValue="yes" itemLabel="Yes"/>
    							<f:selectItem itemValue="no" itemLabel="No"/>
  							</h:selectOneRadio>  								
  							<%--
  							<h:selectOneRadio value="#{PrivateMessagesTool.forwardPvtMsg}" layout="pageDirection" >
    							<f:selectItem itemValue='yes' itemLabel='Yes'/>
    							<f:selectItem itemValue='no' itemLabel='No'/>
  							</h:selectOneRadio>
  				      --%>
              </td>
            </tr>
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="#{msgs.pvt_emailfor}"/>		
              </td>
              <td>                             
               <h:inputText value="#{PrivateMessagesTool.forwardPvtMsgEmail}"                            
                            disabled="#{PrivateMessagesTool.forwardPvtMsg == 'no'}"/>                                          		
              </td>
            </tr>            
          </table>
        </sakai:group_box>
                
 <br>
 
        <sakai:button_bar>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSettingsSave}" value="#{msgs.pvt_saveset}" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="#{msgs.pvt_cancel}" />
        </sakai:button_bar>   
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
