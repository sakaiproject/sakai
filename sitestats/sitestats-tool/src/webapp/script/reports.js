function checkWhatSelection() {
	var what = jQuery('.what').val();
	if(what == 'what-visits') {
		jQuery('.what').css('width','250px');
		jQuery('#what-selection-label').hide();
		jQuery('.what-events-selection-type').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		jQuery('#what-resources-options').hide();
		jQuery('#what-resources-select').hide();
	}else if(what == 'what-events') {
		jQuery('.what').css('width','122px');
		var eventsSelectionType = jQuery('.what-events-selection-type').val();
		jQuery('#what-selection-label').show();
		jQuery('.what-events-selection-type').show();
		if(eventsSelectionType == 'what-events-bytool') {
			jQuery('#what-tools-select').show();
			jQuery('#what-events-select').hide();
		}else{
			jQuery('#what-tools-select').hide();
			jQuery('#what-events-select').show();
		}
		jQuery('#what-resources-options').hide();
		jQuery('#what-resources-select').hide();
	}else if(what == 'what-resources') {
		jQuery('.what').css('width','250px');
		jQuery('#what-selection-label').show();
		jQuery('.what-events-selection-type').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		if(jQuery('.whatLimitedAction').attr('checked')) {
			jQuery('.whatResourceAction').removeAttr('disabled');
		}else{
			jQuery('.whatResourceAction').attr('disabled','disabled');
		}
		if(jQuery('.whatLimitedResourceIds').attr('checked')) {
			jQuery('.whatResourceIds').removeAttr('disabled');
			jQuery('.containerHover').hide();
		}else{
			jQuery('.whatResourceIds').attr('disabled','disabled');
			jQuery('.containerHover').show();
		}
		jQuery('#what-resources-options').show();
		jQuery('#what-resources-select').show();
	}
	checkHowTotalsBySelection();
	setMainFrameHeightNoScroll(window.name);
}

function checkWhenSelection() {
	if(jQuery('.when').val() == 'when-custom'){
		jQuery('#when-custom-from').removeAttr('style');
		jQuery('#when-custom-to').removeAttr('style');
		setMainFrameHeightNoScroll(window.name);
	}else{
		jQuery('#when-custom-from').attr('style','display:none');
		jQuery('#when-custom-to').attr('style','display:none');
	}	
}

function checkWhoSelection() {
	var who = jQuery('.who').val();
	jQuery('.wicket-ajax-indicator').hide();
	if(who == 'who-all'){
		jQuery('.whoRole').attr('style','display:none');
		jQuery('.whoGroup').attr('style','display:none');
		jQuery('.whoCustom').attr('style','display:none');
	}else if(who == 'who-role'){
		jQuery('.whoRole').removeAttr('style');
		jQuery('.whoGroup').attr('style','display:none');
		jQuery('.whoCustom').attr('style','display:none');
	}else if(who == 'who-groups'){
		jQuery('.whoRole').attr('style','display:none');
		jQuery('.whoGroup').removeAttr('style');
		jQuery('.whoCustom').attr('style','display:none');
	}else if(who == 'who-custom'){
		jQuery('.whoRole').attr('style','display:none');
		jQuery('.whoGroup').attr('style','display:none');
		jQuery('.whoCustom').removeAttr('style');
	}else{
		jQuery('.whoRole').attr('style','display:none');
		jQuery('.whoGroup').attr('style','display:none');
		jQuery('.whoCustom').attr('style','display:none');
	}
	setMainFrameHeightNoScroll(window.name);
}

function checkHowSelection() {	
	// sorting
	if(jQuery('.howSortCheck').attr('checked')) {
		jQuery('.howSortBy').removeAttr('disabled');
		jQuery('.howSortAscending').removeAttr('disabled');
	}else{
		jQuery('.howSortBy').attr('disabled','disabled');
		jQuery('.howSortAscending').attr('disabled','disabled');
	}
	// max results
	if(jQuery('.howMaxResultsCheck').attr('checked')){
		jQuery('.howMaxResults').removeAttr('disabled');
	}else{
		jQuery('.howMaxResults').attr('disabled','disabled');
		jQuery('.howMaxResults').val('0');
	}
}

function checkHowTotalsBySelection() {
	var what = jQuery('.what').val();
	jQuery('.howTotalsBy').children().each(function(i){
		jQuery(this).removeAttr('disabled');
	    if(what == 'what-resources'){
	    	if(i == 1) {
	    		// disable Event selection
	    		jQuery(this).removeAttr('selected').attr('disabled','disabled');
	    	}
	    }else if(i == 2 || i == 3) {
    		// disable Resource and Resource Action selection
	        jQuery(this).removeAttr('selected').attr('disabled','disabled');
	    }
	    if( jQuery(jQuery('.howTotalsBy').children()[4]).attr('selected') && jQuery(jQuery('.howTotalsBy').children()[4]).attr('selected') ) {
	    	// both date fields are selected, select first only
	    	jQuery(jQuery('.howTotalsBy').children()[5]).removeAttr('selected');
	    }
	});
}

function checkReportDetails() {
	if(jQuery('.reportSite').val() == 'all') {
		jQuery('.who').val('who-all');
		jQuery('.whoSection').hide();
	}else{
		jQuery('.whoSection').show();
	}
	setMainFrameHeightNoScroll(window.name);
}