<%--********************* Message Header*********************--%>
        <sakai:group_box>
          <table width="100%" align="center" style="background-color:#DDDFE4;">
            <tr>
              <td align="left" width="25%">
                <h:outputText value="View "/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<h:selectOneListbox size="1" id="viewlist">
            				<f:selectItem itemLabel="All Messages" itemValue="none"/>
          				</h:selectOneListbox>
          		</td>
          		<td align="right">
          		
          			<h:inputText value="#{PrivateMessagesTool.searchText}" />&nbsp;&nbsp;&nbsp;&nbsp;
          			<h:commandButton value="Search" action="#{PrivateMessagesTool.processSearch}" onkeypress="document.forms[0].submit;"/>  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

          			<h:outputText value="Advance Search" />          			
              </td>
            </tr>                                    
          </table>
          <table width="100%" align="center">
            <tr>
              <td align="left" >
                <h:outputText value="Mark Checked as Read "/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              	<h:outputText value="  Printer Friendly Format"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              </td>
              
              <td align="center" >
	        			<h:commandButton value="Compose Message" action="#{PrivateMessagesTool.processPvtMsgCompose}" onkeypress="document.forms[0].submit;"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              	<h:commandButton action="#{PrivateMessagesTool.processPvtMsgEmptyDelete}" rendered="#{PrivateMessagesTool.navModeIsDelete}" value="Empty Deleted Folder" onkeypress="document.forms[0].submit;"/>  
              </td>
              
              <td align="right">
              <h:commandLink value="Display Options" action="#{PrivateMessagesTool.processPvtMsgDispOtions}" onkeypress="document.forms[0].submit;"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              <h:commandLink value="Folder Settings" action="#{PrivateMessagesTool.processPvtMsgFolderSettings}" onkeypress="document.forms[0].submit;"/>  
              </td>
            </tr>                                    
          </table>
        </sakai:group_box> 
    		
<hr />  
