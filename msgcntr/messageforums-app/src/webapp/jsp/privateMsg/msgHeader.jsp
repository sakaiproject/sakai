<%--********************* Message Header*********************--%>
<script language="Javascript" type="text/javascript">
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
	
	// global counter to determine if any checkboxes are checked
	var numberChecked = 0;
	
	function updateCount(checked)
	{ 
		if (checked)
		{
			numberChecked++;
		}
		else
		{
			numberChecked--;
		}
	}
	
	function anyChecked()
	{
		return numberChecked > 0;
	}

	// storage for onclick properties of bulk operations
	// for enabling/disabling
	var readOnClick, deleteOnClick, moveOnClick;
	
	// needed for disabling in non-IE browsers
	var readTextColor, deleteTextColor, moveTextColor;
	
	// storage for style information of bulk operations
	// so if 'disabled', remove underline
	var readStyle, deleteStyle, moveStyle;
	
	function toggleBulkOperations(anyChecked, formRef)
	{ 
		// stores link object since multiple access methods
		var readEl, deleteEl, moveEl;
		var IEbrowser;
		var markAsread = formRef + ':markAsread';
		var deleteMarked = formRef + ':deleteMarked';
		var deleteChecked = formRef + ':deleteChecked';
		var moveChecked = formRef + ':moveCheckedToFolder';
		
		if (document.all)
		{
			readEl = document.all[markAsread];
			if (document.all[deleteMarked])
				deleteEl = document.all[deleteMarked];
			else
				deleteEl = document.all[deleteChecked];
			moveEl = document.all[moveChecked];
			IEbrowser=true;
		}
		else
		{
			readEl = document.getElementById(markAsread);
			if (document.getElementById(deleteMarked))
				deleteEl = document.getElementById(deleteMarked);
			else
				deleteEl = document.getElementById(deleteChecked);
			moveEl = document.getElementById(moveChecked);
			IEbrowser=false;
		}

		if (anyChecked)
		{ 
			// toggle onclick functionality
			readEl.onclick = readOnClick;
			deleteEl.onclick = deleteOnClick;
			moveEl.onclick = moveOnClick;

			if (IEbrowser) 
			{	// IE - just set disabled property to false	
				readEl.disabled =  false;
				deleteEl.disabled = false;
				moveEl.disabled = false;
			}
			else
			{ // non-IE - reset text color
				readEl.style.color = readTextColor;
				deleteEl.style.color = readTextColor;
				moveEl.style.color = readTextColor;
			}
		}
		else
		{
			// toggle onclick functionality
			readOnClick = readEl.onclick;
			deleteOnClick = deleteEl.onclick;
			moveOnClick = moveEl.onclick;

			// 'disable'
			readEl.onclick = 'return false;';
			deleteEl.onclick = 'return false;';
			moveEl.onclick = 'return false;';

			if (IEbrowser) 
			{ // IE - set disabled property to true
				readEl.disabled = true;
				deleteEl.disabled = true;
				moveEl.disabled = true;
			}
			else
			{ // non-IE - set color to grey and onclick to return false
				readTextColor = readEl.style.color;
				readEl.style.color = 'grey';

				deleteTextColor = readEl.style.color;
				deleteEl.style.color = 'grey';

				moveTextColor = readEl.style.color;
				moveEl.style.color = 'grey';
			}
		}
	}		
</script>
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/inputDate.js"/>		
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar1.js"/>		
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar2.js"/>			

<!--Y:\msgcntr\messageforums-app\src\webapp\jsp\privateMsg\msgHeader.jsp-->
<h:panelGrid columns="2"  style="width: 100%" summary="layout" styleClass="navPanel ">
  <h:panelGroup>
  	<f:verbatim><div class="viewNav"></f:verbatim>
    <h:outputLabel for="viewlist"><h:outputText value="#{msgs.msg_view}" /></h:outputLabel>
    <f:verbatim><h:outputText value=" " /></f:verbatim>
		<h:selectOneMenu id="viewlist" onchange="this.form.submit();"  
									      valueChangeListener="#{PrivateMessagesTool.processChangeSelectView}" 
									      value="#{PrivateMessagesTool.selectView}">
      <f:selectItem itemLabel="#{msgs.pvt_view_all_msgs}" itemValue="none"/>
		  <f:selectItem itemLabel="#{msgs.pvt_view_conversation}" itemValue="threaded"/>
	  </h:selectOneMenu>
	  <f:verbatim></div></f:verbatim>
	 </h:panelGroup>
  
  <h:panelGroup>
  <f:verbatim><div class="searchNav specialLink"></f:verbatim>
    <h:outputLabel for="search_text"><h:outputText value="#{msgs.pvt_search_text}" /></h:outputLabel>
    <f:verbatim><h:outputText value=" " /></f:verbatim>
	  <h:inputText value="#{PrivateMessagesTool.searchText}" id="search_text" />
		<h:commandButton value="#{msgs.pvt_search}" action="#{PrivateMessagesTool.processSearch}" onkeypress="document.forms[0].submit;"/>
		
		<f:verbatim><div id='adv_button'></f:verbatim>
		  <h:commandLink value="#{msgs.pvt_advsearch}" onmousedown="javascript:toggleDisplay('adv_input','adv_button');setMainFrameHeight('#{PrivateMessagesTool.placementId}');" title="#{msgs.pvt_advsearch}"/>
		<f:verbatim></div></f:verbatim>
		<f:verbatim></div></f:verbatim>
  </h:panelGroup>
  		
	<h:outputText value="  " />
	<h:panelGroup>
		<f:verbatim><div id='adv_input' style="display: none;" ></f:verbatim>
		
		<h:panelGrid styleClass="msgAdvSearch" columns="5" summary="layout" style="padding-right: 0.8em;">
		  <h:outputText value=" " />
			<h:outputText value=" " />
			<h:outputText value=" " />
			<h:outputText value=" " />
			<h:panelGroup styleClass="itemNav specialLink">
	      <h:commandLink value="#{msgs.pvt_clear_search}" action="#{PrivateMessagesTool.processClearSearch}" onkeypress="document.forms[0].submit;"
	                     title="#{msgs.pvt_clear_search}"/>
	      <f:verbatim><h:outputText value=" |  " /></f:verbatim>
				<h:commandLink value="#{msgs.pvt_normal_search}" onmousedown="javascript:toggleDisplay('adv_button','adv_input');" 
				               title="#{msgs.pvt_normal_search}" />
			</h:panelGroup>
			<%-- gsilver:jsf problem - all these h:selectBooleanCheckbox produce input[type="checkbox"] missing name and/or value attributes--%>
			<%-- gsilver:jsf problem - and they also produce unary/shorthand  attributes - that do not validate (ie "checked" instead of "checked="checked"--%>
			<%-- gsilver:jsf problem - and the input[type="text"] produced by sakai:input_date is unclosed--%>
			<h:outputText value="#{msgs.pvt_search_in}"  style="white-space: nowrap;" />
			<h:panelGroup styleClass="checkbox" style="white-space: nowrap;">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnSubject}" id="subject" />
			  <h:outputLabel for="subject"><h:outputText value="#{msgs.pvt_subject}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox" style="white-space: nowrap;">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnAuthor}" id="author" />
			  <h:outputLabel for="author"><h:outputText value="#{msgs.pvt_authby}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox" style="white-space: nowrap;">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnDate}" id="search_by_date" />
			  <h:outputLabel for="search_by_date"><h:outputText value="#{msgs.pvt_date_range}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="shorttext" style="text-align:right;white-space: nowrap;display:block;padding-right: 0.5em;">
			  <h:outputText value="#{msgs.pvt_beg_date}"/>
			  <sakai:input_date  value="#{PrivateMessagesTool.searchFromDate}" showDate="true" id="beg_date" />
			</h:panelGroup>
			
			<h:outputText value=" " />
			<h:panelGroup styleClass="checkbox"	>
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnBody}" id="body" />
			  <h:outputLabel for="body" ><h:outputText value="#{msgs.pvt_body}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox"  style="white-space: nowrap;">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnLabel}" id="label" />
			  <h:outputLabel for="label" ><h:outputText value="#{msgs.pvt_label}" /></h:outputLabel>
			</h:panelGroup>  
			<h:outputText value=" " />    
			<h:panelGroup styleClass="shorttext" style="text-align:right;white-space: nowrap;display:block;padding-right: 0.5em;">
			  <h:outputText value="#{msgs.pvt_end_date}"/>
			<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
			  <sakai:input_date  value="#{PrivateMessagesTool.searchToDate}" showDate="true" id="end_date" />
			</h:panelGroup>	
		
		</h:panelGrid>
		<f:verbatim></div><h:outputText value=" " /></f:verbatim>
	</h:panelGroup>
</h:panelGrid>

<div class="navPanel">
  <div style="float:left; display:inline; width: 33%; padding-top: 0.5em;">
    <%-- Mark All As Read --%>
  	<h:commandLink action="#{PrivateMessagesTool.processActionMarkCheckedAsRead}" id="markAsread" title="#{msgs.cdfm_mark_check_as_read}" >
 		<h:graphicImage value="#{PrivateMessagesTool.serverUrl}/library/image/silk/email.png" />
 		<h:outputText value=" #{msgs.cdfm_mark_check_as_read}" />
 	</h:commandLink>
 	
	<%-- Delete Checked 
			first link renders on non-Deleted folders and moves to Deleted folder
			second link renders on Deleted folder page and does the 'actual' delete --%>
	<h:outputText value="  | " /><h:outputText value=" " />
	<h:commandLink action="#{PrivateMessagesTool.processActionDeleteChecked}" id="deleteMarked"
				title="#{msgs.cdfm_mark_check_as_delete}" rendered="#{PrivateMessagesTool.msgNavMode != 'Deleted'}" >
		<h:graphicImage value="#{PrivateMessagesTool.serverUrl}/library/image/silk/email_delete.png" />
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
	</h:commandLink>
 	<h:commandLink id="deleteChecked" action="#{PrivateMessagesTool.processPvtMsgEmptyDelete}" rendered="#{PrivateMessagesTool.msgNavMode == 'Deleted'}" 
 				 onkeypress="document.forms[0].submit;" accesskey="x" >
 		<h:graphicImage value="#{PrivateMessagesTool.serverUrl}/library/image/silk/email_delete.png" />
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
  	</h:commandLink>
 	  
	<%-- Move Checked To Folder --%>
	<h:outputText value="  | " /><h:outputText value=" " />
	<h:commandLink action="#{PrivateMessagesTool.processActionMoveCheckedToFolder}" id="moveCheckedToFolder" title="#{msgs.cdfm_mark_check_move_to_folder}" >
		<h:graphicImage value="/images/page_move.png" alt="#{msgs.msg_is_unread}"  />
		<h:outputText value=" #{msgs.cdfm_mark_check_move_to_folder}" />
	</h:commandLink>
	</div>

  <div style="float:right; display:inline; width:33%;"></div>
</div>
