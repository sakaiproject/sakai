<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<script language="Javascript">
	function displayEmail() {

	    document.forms[0].email.disabled = false
	}
</script>
  
<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="pvtMsgSettings">
		

<h2>Private message- Settings</h2>
	<h:panelGroup rendered="#{PrivateMessagesTool.forwardPvtMsg == 'yes' && not PrivateMessagesTool.validEmail}">
					<h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
					 value="! Please provide a valid email address" />
	</h:panelGroup>     
  <h4><h:outputText value="Activate Private Messages" rendered="#{PrivateMessagesTool.instructor}"/></h4>
 				<h:panelGroup rendered="#{PrivateMessagesTool.validEmail}">

				</h:panelGroup>  
 				<sakai:group_box >
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText rendered="#{PrivateMessagesTool.instructor}" value="Activate Private Messages: "/>		
              </td>
              <td>
                <h:selectOneRadio id="activate"
                                  rendered="#{PrivateMessagesTool.superUser}" 
                                  value="#{PrivateMessagesTool.activatePvtMsg}"                                   
                                  layout="pageDirection">
    							<f:selectItem itemValue="yes" itemLabel="Yes"/>
    							<f:selectItem itemValue="no" itemLabel="No"/>
  							</h:selectOneRadio>
              </td>
            </tr>
          </table>
        </sakai:group_box>
      
  <h4><h:outputText value="Auto Forward Private Messages" /></h4>
 
 				<sakai:group_box >
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="Auto Forward Private Messages:"/>		
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
          			<h:outputText value="Email address for forwarding:"/>		
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
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSettingsSave}" value="Save Settings" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>   
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
