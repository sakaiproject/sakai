<%--********************* Message Header*********************--%>
<script language="Javascript">
	function toggleDisplay(show,hide) 
	{
		if (document.getElementById) 
		{
			target = document.getElementById(show);
			targethide= document.getElementById(hide);
			
			if(target != null)
			{
			 	if (target.style.display == "none")
				{
	      	target.style.display = "";
	        targethide.style.display="none" ;
	       }
	       else 
	       {
	         target.style.display = "none";
	         targethide.style.display="" ;
	       }
			}
		}
	}
</script>
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/inputDate.js"/>		
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar1.js"/>		
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar2.js"/>			
			
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
								
								<div id='adv_button' class="rightAlign">
								<h:commandLink value="Advance Search" onmousedown="javascript:toggleDisplay('adv_input','adv_button');" />
          			</div>
          			<div style="display: none;" id='adv_input'>
          				<h:commandLink value="Clear Search" action="#{PrivateMessagesTool.processClearSearch}" onkeypress="document.forms[0].submit;"/>
          				<h:commandLink value="Normal Search" onmousedown="javascript:toggleDisplay('adv_button','adv_input');" />
          				<br>
          				<table align="center" cellspacing="2" cellpadding="3" >
          					<tr>
          						<td><h:outputText value="SEARCH IN" /></td>
          						<td><h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnSubject}" /><h:outputText value="Subject" /></td>
          						<td><h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnAuthor}"/><h:outputText value="Authored By" /></td>
          						<td><h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnDate}" /><h:outputText value="Date Between" /></td>
          						<td><sakai:input_date  value="#{PrivateMessagesTool.searchFromDate}" showDate="true" /></td>
          						<td><h:outputText value="AND" /></td>    
          					</tr>
          					<tr>
          						<td></td>
          						<td><h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnBody}" /><h:outputText value="Body" /></td>
          						<td><h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnLabel}"/><h:outputText value="Label" /></td>  
          						<td></td>    
          						<td><sakai:input_date  value="#{PrivateMessagesTool.searchToDate}" showDate="true" /></td>  
          						<td></td>      						
										</tr>
									</table>
          			</div>
          			
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
