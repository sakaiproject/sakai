<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="pvtMsgSettings">
		

<h2>Private message- Settings</h2>

  <h4><h:outputText value="Activate Private Messages" rendered="#{PrivateMessagesTool.superUser}"/></h4>
 
 				<sakai:group_box >
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText rendered="#{PrivateMessagesTool.superUser}" value="Activate Private Messages: "/>		
              </td>
              <td>
                <h:selectOneRadio rendered="#{PrivateMessagesTool.superUser}" value="#{PrivateMessagesTool.activatePvtMsg}">
    							<f:selectItem itemValue="yes" itemLabel="Yes"/>
    							<f:selectItem itemValue="no" itemLabel="No"/>
  							</h:selectOneRadio>
              </td>
            </tr>
          </table>
        </sakai:group_box>
        <%--
 				<sakai:group_box >
 	  			<h:dataTable width="80%" rendered="#{PrivateMessagesTool.superUser}">
						<h:column >
							<h:outputText value="Activate Private Messages: "/>						
						</h:column>
						<h:column >
							<h:selectOneRadio value="#{PrivateMessagesTool.activatePvtMsg}">
    						<f:selectItem itemValue="yes" itemLabel="Yes"/>
    						<f:selectItem itemValue="no" itemLabel="No"/>
  						</h:selectOneRadio>						
						</h:column>
	   			</h:dataTable> 	 
        </sakai:group_box>
        --%>        
  <h4><h:outputText value="Auto Forward Private Messages" /></h4>
 
 				<sakai:group_box >
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="Auto Forward Private Messages:"/>		
              </td>
              <td>
                <h:selectOneRadio value="#{PrivateMessagesTool.forwardPvtMsg}">
    							<f:selectItem itemValue="yes" itemLabel="Yes"/>
    							<f:selectItem itemValue="no" itemLabel="No"/>
  							</h:selectOneRadio>
              </td>
            </tr>
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="Email address for forwarding:"/>		
              </td>
              <td>
                <h:inputText value="#{PrivateMessagesTool.forwardPvtMsgEmail}"/>
              </td>
            </tr>            
          </table>
        </sakai:group_box>
                
<%--        
    <h:outputText value="Activate Private Messages:"/>
    		<h:selectOneRadio value="#{PrivateMessagesTool.forwardPvtMsg}">
    			<f:selectItem itemValue="yes" itemLabel="Yes"/><br />
    			<f:selectItem itemValue="no" itemLabel="No"/><br />
  			</h:selectOneRadio>
			
 <h4><h:outputText value="Auto Forward Private Messages"/></h4>
 <h:outputText value="Auto Forward Private Messages:"/>
 <br>
 <h:outputText value="Email address for forwarding:"/>
 <h:inputText value="#{PrivateMessagesTool.forwardPvtMsgEmail}"/>
 --%>
 <br>
 
        <sakai:button_bar>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSettingRevise}" value="Revise" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>   
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
