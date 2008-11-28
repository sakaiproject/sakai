function checkWhatSelection() {
	var visitsRadio = jQuery('.what-visits');
    var eventsRadio = jQuery('.what-events');
    var resourcesRadio = jQuery('.what-resources');
	if(visitsRadio.attr('checked')) {
		jQuery('#what-events-by-selectionRadio').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		jQuery('#what-resources-options').hide();
		jQuery('#what-resources-select').hide();
	}else if(eventsRadio.attr('checked')) {
		jQuery('#what-events-by-selectionRadio').show();
		if(jQuery('.what-events-bytool').attr('checked')) {
			jQuery('#what-tools-select').show();
			jQuery('#what-events-select').hide();
		}else{
			jQuery('#what-tools-select').hide();
			jQuery('#what-events-select').show();
		}
		jQuery('#what-resources-options').hide();
		jQuery('#what-resources-select').hide();
	}else{
		jQuery('#what-events-by-selectionRadio').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		if(jQuery('.whatLimitedAction').attr('checked')) {
			jQuery('.whatResourceAction').removeAttr('disabled');
		}else{
			jQuery('.whatResourceAction').attr('disabled','disabled');
		}
		if(jQuery('.whatLimitedResourceIds').attr('checked')) {
			jQuery('.whatResourceIds').removeAttr('disabled');
			jQuery('.whatResourceIds').width('auto');
			jQuery('.containerHover').hide();
		}else{
			jQuery('.whatResourceIds').attr('disabled','disabled');
			jQuery('.whatResourceIds').width('304px');
			jQuery('.containerHover').show();
		}
		jQuery('#what-resources-options').show();
		jQuery('#what-resources-select').show();
	}
	setMainFrameHeightNoScroll(window.name);
}

function checkWhenSelection() {
	if(jQuery('.when-custom').attr('checked')){
		jQuery('#when-customPanel').show();
		setMainFrameHeightNoScroll(window.name);
	}else{
		jQuery('#when-customPanel').hide();
	}	
}

function checkWhoSelection() {
	if(jQuery('.who-all').attr('checked')){
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').hide();
	}else if(jQuery('.who-role').attr('checked')){
		jQuery('.who-role-select').show();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').hide();
	}else if(jQuery('.who-group').attr('checked')){
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').show();
		jQuery('.who-custom-select').hide();
	}else if(jQuery('.who-custom').attr('checked')){
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').show();
	}else{
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').hide();
	}
	setMainFrameHeightNoScroll(window.name);
}

function checkHowSelection() {
	var resourcesRadio = jQuery('.what-resources');
	if(resourcesRadio.attr('checked')) {
		// resources selected
		jQuery('.howTotalsByEvent').removeAttr('checked');
		//jQuery('.howTotalsByResource').attr('checked','checked');
		//jQuery('.howTotalsByResourceAction').attr('checked','checked');
		if(jQuery('.howSortBy').val == 'event') {
			jQuery('.howSortBy').val('resource');
		}
		
		jQuery('.howTotalsByEvent').attr('disabled','disabled');
		jQuery('.howTotalsByResource').removeAttr('disabled');
		jQuery('.howTotalsByResourceAction').removeAttr('disabled');
	}else{
		// visits/tools/events selected
		jQuery('.howTotalsByResource').removeAttr('checked');
		jQuery('.howTotalsByResourceAction').removeAttr('checked');
		//jQuery('.howTotalsByEvent').attr('checked','checked');
		if(jQuery('.howSortBy').val == 'resource' || jQuery('.howSortBy').val == 'resource-action') {
			jQuery('.howSortBy').val('event');
		}

		jQuery('.howTotalsByEvent').removeAttr('disabled');
		jQuery('.howTotalsByResource').attr('disabled','disabled');
		jQuery('.howTotalsByResourceAction').attr('disabled','disabled');
	}
	// sorting
	if(jQuery('.howSortCheck').attr('checked')){
		jQuery('.howSortBy').show();
		jQuery('.howSortAscending').show();
		jQuery('.howSortAscendingTxt').show();
		
		if(jQuery('.howSortBy').val() == 'user' && !jQuery('.howTotalsByUser').attr('checked')) {
			jQuery('.howSortBy').val('total');
		}
		if(jQuery('.howSortBy').val() == 'event' && !jQuery('.howTotalsByEvent').attr('checked')) {
			jQuery('.howSortBy').val('total');
		}
		if(jQuery('.howSortBy').val() == 'resource' && !jQuery('.howTotalsByResource').attr('checked')) {
			jQuery('.howSortBy').val('total');
		}
		if(jQuery('.howSortBy').val() == 'resource-action' && !jQuery('.howTotalsByResourceAction').attr('checked')) {
			jQuery('.howSortBy').val('total');
		}
		if(jQuery('.howSortBy').val() == 'date' && 
				(!jQuery('.howTotalsByDate').attr('checked') && !jQuery('.howTotalsByLastDate').attr('checked'))
				) {
			jQuery('.howSortBy').val('total');
		}
		
	}else{
		jQuery('.howSortBy').hide();
		jQuery('.howSortAscending').hide();
		jQuery('.howSortAscendingTxt').hide();
	}
	// max results
	if(jQuery('.howMaxResultsCheck').attr('checked')){
		jQuery('.howMaxResults').show();
	}else{
		jQuery('.howMaxResults').hide();
		jQuery('.howMaxResults').val('0');
	}
}

function checkReportDetails() {
	if(jQuery('.reportSite').val() == 'all') {
		jQuery('.who-all').attr('checked','checked');
		jQuery('.who').hide();
	}else{
		jQuery('.who').show();
	}
	setMainFrameHeightNoScroll(window.name);
}