function toggleCheckboxAll() {
	if(jQuery('#useAllTools').attr('checked')) {
		jQuery('.eventTree').hide();
	}else{
		jQuery('.eventTree').show();
		setMainFrameHeightNoScroll( window.name );
	}
}

function selectUnselectEvents(obj) {
	if(obj.checked) {
		jQuery(obj).parent().parent().find('span :checkbox').attr('checked','checked');
	}else{
		jQuery(obj).parent().parent().find('span :checkbox').removeAttr('checked');
	}
}

function updateAllToolsSelection() {
	updateToolSelection('.tool');
}

function updateToolSelection(toolClass) {
	jQuery(toolClass).each(function(i){
		jQuery(this).children('span').removeClass();
		
		// tool class
		if(jQuery(this).parent().find('span :checkbox').length == jQuery(this).parent().find('span :checked').length) {
			jQuery(this).children('span').addClass('nodeToolSelected');
			jQuery(this).children(':checkbox').attr('checked','checked');
			  
		}else if(jQuery(this).parent().find('span :checked').length == 0) {
			jQuery(this).children('span').addClass('nodeToolUnselected');
			jQuery(this).children(':checkbox').removeAttr('checked');
			
		}else{
			jQuery(this).children('span').addClass('nodeToolPartialSelected');
			jQuery(this).children(':checkbox').attr('checked','checked');
		}
		
		// event class
		jQuery(this).parent().find('span').each(function(i){
			jQuery(this).find('span').removeClass();
			if(jQuery(this).find(':checkbox').attr('checked')) {
				jQuery(this).find('li span').addClass('nodeEventSelected');
			}else{
				jQuery(this).find('li span').addClass('nodeEventUnselected');
			}
		});
	});
}