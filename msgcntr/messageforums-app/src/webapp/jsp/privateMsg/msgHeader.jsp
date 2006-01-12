<%--********************* Message Header*********************--%>
        <sakai:group_box>
          <table width="100%" align="center" class="tablebgcolor" >
            <tr>
              <td align="left" width="25%">
                <h:outputText value="View "/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                	<h:selectOneMenu id="viewlist" onchange="this.form.submit();"  
										valueChangeListener="#{PrivateMessagesTool.processChangeSelectView}" 
										value="#{PrivateMessagesTool.selectView}">
            				<f:selectItem itemLabel="All Messages" itemValue="none"/>
            				<f:selectItem itemLabel="By Conversation" itemValue="threaded"/>
       				</h:selectOneMenu>
          		</td>
          		<td align="right">
          		
          			<h:inputText value="#{PrivateMessagesTool.searchText}" />&nbsp;&nbsp;&nbsp;&nbsp;
          			<h:commandButton value="#{msgs.pvt_search}" action="#{PrivateMessagesTool.processSearch}" onkeypress="document.forms[0].submit;"/>  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

          			<%--<h:outputText value="#{msgs.pvt_advsearch}" />--%>
              </td>
            </tr>                                    
          </table>
          <table width="100%" align="center">
            <tr>
              <td align="left" >
              <%--
                <h:outputText value="#{msgs.pvt_markread}"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              	<h:outputText value="#{msgs.pvt_pntformat}"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              --%>
              </td>
              
              <td align="center" >
	        			<h:commandButton value="#{msgs.pvt_cmpmsg}" action="#{PrivateMessagesTool.processPvtMsgCompose}" onkeypress="document.forms[0].submit;"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              	<h:commandButton action="#{PrivateMessagesTool.processPvtMsgEmptyDelete}" rendered="#{PrivateMessagesTool.msgNavMode == 'Deleted'}" value="#{msgs.pvt_emptydelfol}" onkeypress="document.forms[0].submit;"/>  
              </td>
              
              <td align="right">
              <%--
              <h:commandLink value="#{msgs.pvt_dispop}" action="#{PrivateMessagesTool.processPvtMsgDispOtions}" onkeypress="document.forms[0].submit;"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
             
              <h:commandLink value="#{msgs.pvt_foldersettings}" action="#{PrivateMessagesTool.processPvtMsgFolderSettings}" onkeypress="document.forms[0].submit;">  
					    	<f:param value="#{PrivateMessagesTool.selectedTopicTitle}" name="pvtMsgTopicTitle"/>
    						<f:param value="#{PrivateMessagesTool.selectedTopicId}" name="pvtMsgTopicId"/>
					    </h:commandLink>
					  	--%>
              </td>
            </tr>                                    
          </table>
        </sakai:group_box> 
    		
<hr />  
