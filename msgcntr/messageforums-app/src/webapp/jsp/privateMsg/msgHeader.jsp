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

<h:panelGrid columns="2" styleClass="jsfFormTable" style="width: 100%" summary="">
  <h:panelGroup>
    <h:outputLabel for="viewlist"><h:outputText value="#{msgs.msg_view}" /></h:outputLabel>
    <f:verbatim><h:outputText value=" " /></f:verbatim>
		<h:selectOneMenu id="viewlist" onchange="this.form.submit();"  
									      valueChangeListener="#{PrivateMessagesTool.processChangeSelectView}" 
									      value="#{PrivateMessagesTool.selectView}">
      <f:selectItem itemLabel="#{msgs.pvt_view_all_msgs}" itemValue="none"/>
		  <f:selectItem itemLabel="#{msgs.pvt_view_conversation}" itemValue="threaded"/>
	  </h:selectOneMenu>
	 </h:panelGroup>
  
  <h:panelGroup styleClass="msgNav">
    <h:outputLabel for="search_text"><h:outputText value="#{msgs.pvt_search_text}" /></h:outputLabel>
    <f:verbatim><h:outputText value=" " /></f:verbatim>
	  <h:inputText value="#{PrivateMessagesTool.searchText}" id="search_text" />
		<h:commandButton value="#{msgs.pvt_search}" action="#{PrivateMessagesTool.processSearch}" onkeypress="document.forms[0].submit;"/>
		
		<f:verbatim><div id='adv_button'></f:verbatim>
		  <h:commandLink value="#{msgs.pvt_advsearch}" onmousedown="javascript:toggleDisplay('adv_input','adv_button');" title="#{msgs.pvt_advsearch}"/>
		<f:verbatim></div></f:verbatim>
  </h:panelGroup>
  		
	<h:outputText value=" " />
	<h:panelGroup>
		<f:verbatim><div id='adv_input' style="display: none;" ></f:verbatim>
		
		<h:panelGrid styleClass="msgAdvSearch" columns="5" summary="">
		  <h:outputText value=" " />
			<h:outputText value=" " />
			<h:outputText value=" " />
			<h:outputText value=" " />
			<h:panelGroup styleClass="msgNav">
	      <h:commandLink value="#{msgs.pvt_clear_search}" action="#{PrivateMessagesTool.processClearSearch}" onkeypress="document.forms[0].submit;"
	                     title="#{msgs.pvt_clear_search}"/>
	      <f:verbatim><h:outputText value=" " /></f:verbatim>
				<h:commandLink value="#{msgs.pvt_normal_search}" onmousedown="javascript:toggleDisplay('adv_button','adv_input');" 
				               title="#{msgs.pvt_normal_search}" />
			</h:panelGroup>
			
			<h:outputText value="#{msgs.pvt_search_in}" />
			<h:panelGroup styleClass="checkbox inlineForm">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnSubject}" id="subject" />
			  <h:outputLabel for="subject"><h:outputText value="#{msgs.pvt_subject}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox inlineForm">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnAuthor}" id="author" />
			  <h:outputLabel for="author"><h:outputText value="#{msgs.pvt_authby}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox inlineForm">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnDate}" id="search_by_date" />
			  <h:outputLabel for="search_by_date"><h:outputText value="#{msgs.pvt_date_range}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox inlineForm">
			  <h:outputLabel for="beg_date"><h:outputText value="#{msgs.pvt_beg_date}" /></h:outputLabel>
			  <sakai:input_date  value="#{PrivateMessagesTool.searchFromDate}" showDate="true" id="beg_date" />
			</h:panelGroup>
			
			<h:outputText value=" " />
			<h:panelGroup styleClass="checkbox inlineForm">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnBody}" id="body" />
			  <h:outputLabel for="body" ><h:outputText value="#{msgs.pvt_body}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox inlineForm">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnLabel}" id="label" />
			  <h:outputLabel for="label" ><h:outputText value="#{msgs.pvt_label}" /></h:outputLabel>
			</h:panelGroup>  
			<h:outputText value=" " />    
			<h:panelGroup styleClass="checkbox inlineForm">
			  <h:outputLabel for="end_date" ><h:outputText value="#{msgs.pvt_end_date}" /></h:outputLabel>
			  <f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
			  <sakai:input_date  value="#{PrivateMessagesTool.searchToDate}" showDate="true" id="end_date" />
			</h:panelGroup>
		
		</h:panelGrid>
		<f:verbatim></div><h:outputText value=" " /></f:verbatim>
	</h:panelGroup>
</h:panelGrid>

<div class="composeMsg">
  <h:commandButton value="#{msgs.pvt_cmpmsg}" action="#{PrivateMessagesTool.processPvtMsgCompose}" onkeypress="document.forms[0].submit;" accesskey="n" />
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<h:commandButton action="#{PrivateMessagesTool.processPvtMsgEmptyDelete}" rendered="#{PrivateMessagesTool.msgNavMode == 'Deleted'}" value="#{msgs.pvt_emptydelfol}" onkeypress="document.forms[0].submit;" accesskey="x" />
</div>
			
	    			
	    			<%--<h:outputText value="#{msgs.pvt_advsearch}" />--%>
	 
	    <!-- <table class="pvtMsgs">
	      <tr>
	        <td>
	        <%--
	          <h:outputText value="#{msgs.pvt_markread}"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	        	<h:outputText value="#{msgs.pvt_pntformat}"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	        --%>
	        </td>
	        
	        <td id="pvtMsgComposeButton">
	   			<h:commandButton value="#{msgs.pvt_cmpmsg}" action="#{PrivateMessagesTool.processPvtMsgCompose}" onkeypress="document.forms[0].submit;"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	        	<h:commandButton action="#{PrivateMessagesTool.processPvtMsgEmptyDelete}" rendered="#{PrivateMessagesTool.msgNavMode == 'Deleted'}" value="#{msgs.pvt_emptydelfol}" onkeypress="document.forms[0].submit;"/>  
	        </td>
	        
	        <td>
	        <%--
	        <h:commandLink value="#{msgs.pvt_dispop}" action="#{PrivateMessagesTool.processPvtMsgDispOtions}" onkeypress="document.forms[0].submit;"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	       
	        <h:commandLink value="#{msgs.pvt_foldersettings}" action="#{PrivateMessagesTool.processPvtMsgFolderSettings}" onkeypress="document.forms[0].submit;">  
	   	<f:param value="#{PrivateMessagesTool.selectedTopicTitle}" name="pvtMsgTopicTitle"/>
					<f:param value="#{PrivateMessagesTool.selectedTopicId}" name="pvtMsgTopicId"/>
	   </h:commandLink>
	 	--%>
	        </td>
	      </tr>                                    
	    </table>-->
	    

    		
<hr />  
