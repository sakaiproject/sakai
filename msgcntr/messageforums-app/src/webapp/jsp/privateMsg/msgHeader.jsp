<%--********************* Message Header*********************--%>
<script>includeLatestJQuery("msgcntr");</script>
<script src="/messageforums-tool/js/sak-10625.js"></script>
<script src="/messageforums-tool/js/bulkops.js"></script>
<script src="/library/js/lang-datepicker/lang-datepicker.js"></script>

  <script>
    $(document).ready(function() {
     localDatePicker({
      input: '#prefs_pvt_form\\:searchFromDate',
      useTime: 0,
      parseFormat: 'YYYY-MM-DD',
      allowEmptyDate: true,
      val: '<h:outputText value="#{PrivateMessagesTool.searchFromDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
      ashidden: { iso8601: 'searchFromDateISO8601' }
    });
     localDatePicker({
      input: '#prefs_pvt_form\\:searchToDate',
      useTime: 0,
      parseFormat: 'YYYY-MM-DD',
      allowEmptyDate: true,
      val: '<h:outputText value="#{PrivateMessagesTool.searchToDate}"><f:convertDateTime pattern="yyyy-MM-dd"/></h:outputText>',
      ashidden: { iso8601: 'searchToDateISO8601' }
    });
    });
  </script>

  <h:panelGroup>
  <f:verbatim><div class="searchNav specialLink space_mobile"></f:verbatim>
    <h:outputLabel for="search_text"><h:outputText value="#{msgs.pvt_search_text}" /></h:outputLabel>
    <h:outputText value=" " />
	  <h:inputText value="#{PrivateMessagesTool.searchText}" id="search_text" />
		<h:commandButton styleClass="button_search" value="#{msgs.pvt_search}" action="#{PrivateMessagesTool.processSearch}" onkeypress="document.forms[0].submit;"/>
		
		<f:verbatim><span id='adv_button'></f:verbatim>
		  <h:commandButton id="advanced_search_button" styleClass="button_search" value="#{msgs.pvt_advsearch}" onmousedown="javascript:toggleDisplay('adv_input','adv_button');setMainFrameHeight('#{PrivateMessagesTool.placementId}');" title="#{msgs.pvt_advsearch}"/>
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

			<h:panelGroup rendered="#{PrivateMessagesTool.canUseTags}" styleClass="checkbox">
			  <h:selectBooleanCheckbox value="#{PrivateMessagesTool.searchOnTags}" id="search_by_tags" />
			  <h:outputLabel for="search_by_tags"><h:outputText value="#{msgs.pvt_tags_header}" /></h:outputLabel>
			</h:panelGroup>
			
			<h:panelGroup styleClass="shorttext" id="pvt_selected_label">
              <f:verbatim><span class="labeled"></f:verbatim>
              <h:outputText value="#{msgs.pvt_label}"/>
              <f:verbatim></span></f:verbatim>
              <h:selectOneListbox size="1" id="selectedSearchLabel" value="#{PrivateMessagesTool.selectedSearchLabel}">
                <f:selectItem itemValue="pvt_priority_normal" itemLabel="#{msgs.pvt_priority_normal}"/>
                <f:selectItem itemValue="pvt_priority_low" itemLabel="#{msgs.pvt_priority_low}"/>
                <f:selectItem itemValue="pvt_priority_high" itemLabel="#{msgs.pvt_priority_high}"/>
              </h:selectOneListbox>
            </h:panelGroup>

			<h:panelGroup styleClass="shorttext" id="pvt_beg_date">
			  <f:verbatim><span class="labeled"></f:verbatim>
			  <h:outputText value="#{msgs.pvt_beg_date}"/>
			  <f:verbatim></span></f:verbatim>
			  <h:inputText value="#{PrivateMessagesTool.searchFromDateString}" size="20" id="searchFromDate"/>
			</h:panelGroup>
			
			<h:panelGroup styleClass="shorttext" id="pvt_end_date">
			  <f:verbatim><span class="labeled"></f:verbatim>
			  <h:outputText value="#{msgs.pvt_end_date}"/>
			  <f:verbatim></span></f:verbatim>
			  <h:inputText value="#{PrivateMessagesTool.searchToDateString}" size="20" id="searchToDate"/>
			</h:panelGroup>

			<h:panelGroup styleClass="shorttext" id="pvt_selected_tags" rendered="#{PrivateMessagesTool.canUseTags}">
			  <f:verbatim><span class="labeled"></f:verbatim>
			  <h:outputText value="#{msgs.pvt_tags_header}"/>
			  <f:verbatim></span></f:verbatim>
			  <div class="row">
				<div class="col-xs-12 col-sm-6">
				  <h:inputHidden value="#{PrivateMessagesTool.selectedTags}" id="tag_selector" />
				  <sakai-tag-selector
				      id="tag-selector"
				      selected-temp='<h:outputText value="#{PrivateMessagesTool.selectedTags}"/>'
				      collection-id='<h:outputText value="#{PrivateMessagesTool.getUserId()}"/>'
				      site-id='<h:outputText value="#{PrivateMessagesTool.getSiteId()}"/>'
				      tool='<h:outputText value="#{PrivateMessagesTool.getTagTool()}"/>'
				      add-new="false"
				  ></sakai-tag-selector>
				</div>
			  </div>
			</h:panelGroup>

		<f:verbatim></div></f:verbatim><h:outputText value=" " />
	</h:panelGroup>

	  <h:panelGroup> <%-- This is the show items --%>
	  	<f:verbatim><div class="viewNav messages_viewnav"></f:verbatim>
	    <h:outputLabel for="viewlist"><h:outputText value="#{msgs.msg_view}" /></h:outputLabel>
	    <h:outputText value=" " />
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
 		<span class="bi bi-envelope-open" aria-hidden="true"></span>
 		<h:outputText value=" #{msgs.cdfm_mark_check_as_read}" />
	</h:commandLink> 	
	<span class="ToggleBulkDisabled">
		<span class="bi bi-envelope-open" aria-hidden="true"></span>
 		<h:outputText value=" #{msgs.cdfm_mark_check_as_read}" />
	</span>
    <%-- Mark Checked As Unread --%>
	<h:outputText value="  | " /><h:outputText value=" " />
  	
  	<h:commandLink action="#{PrivateMessagesTool.processActionMarkCheckedAsUnread}" id="markAsUnread" styleClass="ToggleBulk" title="#{msgs.cdfm_mark_check_as_unread}" >
  		<span class="bi bi-envelope" aria-hidden="true"></span>
		<h:outputText value=" #{msgs.cdfm_mark_check_as_unread}" />		
 	</h:commandLink>
 	<span class="ToggleBulkDisabled">
		<span class="bi bi-envelope" aria-hidden="true"></span>
		<h:outputText value=" #{msgs.cdfm_mark_check_as_unread}" />	
	</span>
	<%-- Delete Checked 
			first link renders on non-Deleted folders and moves to Deleted folder
			second link renders on Deleted folder page and does the 'actual' delete --%>
	<h:outputText value="  | " /><h:outputText value=" " />
	<h:commandLink action="#{PrivateMessagesTool.processActionDeleteChecked}" id="deleteMarked" styleClass="ToggleBulk"
				title="#{msgs.cdfm_mark_check_as_delete}" rendered="#{PrivateMessagesTool.msgNavMode != 'pvt_deleted'}" >
		<span class="bi bi-envelope-x" aria-hidden="true"></span>
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
	</h:commandLink>
 	<h:commandLink id="deleteChecked" styleClass="ToggleBulk" action="#{PrivateMessagesTool.processPvtMsgEmptyDelete}" rendered="#{PrivateMessagesTool.msgNavMode == 'pvt_deleted'}" 
 				 onkeypress="document.forms[0].submit;" accesskey="x" >
 		<span class="bi bi-envelope-x" aria-hidden="true"></span>
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
  	</h:commandLink>
  	<span class="ToggleBulkDisabled">
		<span class="bi bi-envelope-x" aria-hidden="true"></span>
		<h:outputText value=" #{msgs.cdfm_mark_check_as_delete}" />
	</span>
 	  
	<%-- Move Checked To Folder --%>
	<h:outputText value="  | " /><h:outputText value=" " />
	<h:commandLink action="#{PrivateMessagesTool.processActionMoveCheckedToFolder}" id="moveCheckedToFolder" styleClass="ToggleBulk" title="#{msgs.cdfm_mark_check_move_to_folder}" >
		<span class="bi bi-folder-symlink" aria-hidden="true"></span>
		<h:outputText value=" #{msgs.cdfm_mark_check_move_to_folder}" />
	</h:commandLink>
	<span class="ToggleBulkDisabled">
		<span class="bi bi-folder-symlink" aria-hidden="true"></span>
		<h:outputText value=" #{msgs.cdfm_mark_check_move_to_folder}" />
	</span>
	</div>

  <div style="float:right; display:inline; width:33%;"></div>
</div>
