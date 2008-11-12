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
