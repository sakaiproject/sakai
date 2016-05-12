<%--********************* Message Header*********************--%>
<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
<sakai:script contextBase="/messageforums-tool" path="/js/bulkops.js"/>		
<sakai:script contextBase="/jsf-resource" path="/inputDate/inputDate.js"/>		
<sakai:script contextBase="/jsf-resource" path="/inputDate/calendar1.js"/>		
<sakai:script contextBase="/jsf-resource" path="/inputDate/calendar2.js"/>			

  <h:panelGroup>
  <f:verbatim><div class="searchNav specialLink space_mobile"></f:verbatim>
    <h:outputLabel for="search_text"><h:outputText value="#{msgs.pvt_search_text}" /></h:outputLabel>
    <f:verbatim><h:outputText value=" " /></f:verbatim>
	  <h:inputText value="#{PrivateMessagesTool.searchText}" id="search_text" />
		<h:commandButton styleClass="button_search" value="#{msgs.pvt_search}" action="#{PrivateMessagesTool.processSearch}" onkeypress="document.forms[0].submit;"/>
		
		<f:verbatim><span id='adv_button'></f:verbatim>
		  <h:commandButton styleClass="button_search" value="#{msgs.pvt_advsearch}" onmousedown="javascript:toggleDisplay('adv_input','adv_button');setMainFrameHeight('#{PrivateMessagesTool.placementId}');" title="#{msgs.pvt_advsearch}"/>
		<f:verbatim></span></f:verbatim>
		<f:verbatim></div></f:verbatim>
  </h:panelGroup>
  		
	<h:outputText value="  " />
	<h:panelGroup>
		<f:verbatim><div id='adv_input' style="display: none;" ></f:verbatim>
		
		  <h:outputText value=" " />
			<h:outputText value=" " />
			<h:outputText value=" " />
			<h:outputText value=" " />
			<h:panelGroup styleClass="itemNav specialLink">
	      	<h:commandButton styleClass="button_search" value="#{msgs.pvt_clear_search}" action="#{PrivateMessagesTool.processClearSearch}" onkeypress="document.forms[0].submit;"
	                     title="#{msgs.pvt_clear_search}"/>
				<h:commandButton  styleClass="button_search" value="#{msgs.pvt_normal_search}" onmousedown="javascript:toggleDisplay('adv_button','adv_input');" 
				               title="#{msgs.pvt_normal_search}" />
			</h:panelGroup>
			<%-- gsilver:jsf problem - all these h:selectBooleanCheckbox produce input[type="checkbox"] missing name and/or value attributes--%>
			<%-- gsilver:jsf problem - and they also produce unary/shorthand  attributes - that do not validate (ie "checked" instead of "checked="checked"--%>
			<%-- gsilver:jsf problem - and the input[type="text"] produced by sakai:input_date is unclosed--%>
			<h:outputText value="#{msgs.pvt_search_in}"  styleClass="header" />
			<h:panelGroup styleClass="checkbox">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnSubject}" id="subject" />
			  <h:outputLabel for="subject"><h:outputText value="#{msgs.pvt_subject}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnAuthor}" id="author" />
			  <h:outputLabel for="author"><h:outputText value="#{msgs.pvt_authby}" /></h:outputLabel>
			</h:panelGroup>
			
			<h:panelGroup styleClass="checkbox"	>
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnBody}" id="body" />
			  <h:outputLabel for="body" ><h:outputText value="#{msgs.pvt_body}" /></h:outputLabel>
			</h:panelGroup>
			<h:panelGroup styleClass="checkbox" >
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnLabel}" id="label" />
			  <h:outputLabel for="label" ><h:outputText value="#{msgs.pvt_label}" /></h:outputLabel>
			</h:panelGroup>  
			
			<h:panelGroup styleClass="checkbox">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnDate}" id="search_by_date" />
			  <h:outputLabel for="search_by_date"><h:outputText value="#{msgs.pvt_date_range}" /></h:outputLabel>
			</h:panelGroup>
			
			<h:panelGroup styleClass="shorttext" id="pvt_beg_date">
			  <f:verbatim><span class="labeled"></f:verbatim>
			  <h:outputText value="#{msgs.pvt_beg_date}"/>
			  <f:verbatim></span></f:verbatim>
			  <sakai:input_date  value="#{PrivateMessagesTool.searchFromDate}" showDate="true" id="beg_date" />
			</h:panelGroup>
			
			<h:panelGroup styleClass="shorttext" id="pvt_end_date">
			  <f:verbatim><span class="labeled"></f:verbatim>
			  <h:outputText value="#{msgs.pvt_end_date}"/>
			  <f:verbatim></span></f:verbatim>
			  <sakai:input_date  value="#{PrivateMessagesTool.searchToDate}" showDate="true" id="end_date" />
			</h:panelGroup>	
		

		<f:verbatim></div><h:outputText value=" " /></f:verbatim>
	</h:panelGroup>

	  <h:panelGroup> <%-- This is the show items --%>
	  	<f:verbatim><div class="viewNav messages_viewnav"></f:verbatim>
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

<div class="navPanel nav_table">
  <div style="float:left; display:inline; width: auto; padding-top: 0.5em;">
    <%-- Mark All As Read --%>
  	<h:commandLink action="#{PrivateMessagesTool.processActionMarkCheckedAsRead}" id="markAsread" styleClass="ToggleBulk" title="#{msgs.cdfm_mark_check_as_read}" >
 		<h:graphicImage value="/../../library/image/silk/email_open.png" />
 		<h:outputText value=" #{msgs.cdfm_mark_check_as_read}" />
	</h:commandLink> 	
	<span class="ToggleBulkDisabled" style="color:grey">
		<h:graphicImage value="/../../library/image/silk/email_open.png" />
 		<h:outputText value=" #{msgs.cdfm_mark_check_as_read}" />
	</span>
    <%-- Mark Checked As Unread --%>
	<h:outputText value="  | " /><h:outputText value=" " />
  	
  	<h:commandLink action="#{PrivateMessagesTool.processActionMarkCheckedAsUnread}" id="markAsUnread" styleClass="ToggleBulk" title="#{msgs.cdfm_mark_check_as_unread}" >
  		<h:graphicImage value="/../../library/image/silk/email.png" />
		<h:outputText value=" #{msgs.cdfm_mark_check_as_unread}" />		
 	</h:commandLink>
 	<span class="ToggleBulkDisabled" style="color:grey">
		<h:graphicImage value="/../../library/image/silk/email.png" />
		<h:outputText value=" #{msgs.cdfm_mark_check_as_unread}" />	
	</span>
	<%-- Delete Checked 
			first link renders on non-Deleted folders and moves to Deleted folder
			second link renders on Deleted folder page and does the 'actual' delete --%>
	<h:outputText value="  | " /><h:outputText value=" " />
	<h:commandLink action="#{PrivateMessagesTool.processActionDeleteChecked}" id="deleteMarked" styleClass="ToggleBulk"
				title="#{msgs.cdfm_mark_check_as_delete}" rendered="#{PrivateMessagesTool.msgNavMode != 'pvt_deleted'}" >
		<h:graphicImage value="/../../library/image/silk/email_delete.png" />
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
	</h:commandLink>
 	<h:commandLink id="deleteChecked" styleClass="ToggleBulk" action="#{PrivateMessagesTool.processPvtMsgEmptyDelete}" rendered="#{PrivateMessagesTool.msgNavMode == 'pvt_deleted'}" 
 				 onkeypress="document.forms[0].submit;" accesskey="x" >
 		<h:graphicImage value="/../../library/image/silk/email_delete.png" />
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
  	</h:commandLink>
  	<span class="ToggleBulkDisabled" style="color:grey">
		<h:graphicImage value="/../../library/image/silk/email_delete.png" />
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
	</span>
 	  
	<%-- Move Checked To Folder --%>
	<h:outputText value="  | " /><h:outputText value=" " />
	<h:commandLink action="#{PrivateMessagesTool.processActionMoveCheckedToFolder}" id="moveCheckedToFolder" styleClass="ToggleBulk" title="#{msgs.cdfm_mark_check_move_to_folder}" >
		<h:graphicImage value="/images/page_move.png" alt="#{msgs.msg_is_unread}"  />
		<h:outputText value=" #{msgs.cdfm_mark_check_move_to_folder}" />
	</h:commandLink>
	<span class="ToggleBulkDisabled" style="color:grey">
		<h:graphicImage value="/images/page_move.png" alt="#{msgs.msg_is_unread}"  />
		<h:outputText value=" #{msgs.cdfm_mark_check_move_to_folder}" />
	</span>
	</div>

  <div style="float:right; display:inline; width:33%;"></div>
</div>
