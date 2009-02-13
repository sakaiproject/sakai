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
		jQuery(obj).parent().find('ul li :checkbox').attr('checked','checked');
	}else{
		jQuery(obj).parent().find('ul li :checkbox').removeAttr('checked');
	}
}

function updateAllToolsSelection() {
	updateToolSelection('.tool');
}

function updateToolSelection(selector) {
	jQuery(selector).each(function(i){
		jQuery(this).children('span').removeClass();
		
		// tool class
		if(jQuery(this).find('ul li :checkbox').length == jQuery(this).find('ul li :checked').length) {
			jQuery(this).children('span').addClass('nodeToolSelected');
			jQuery(this).children(':checkbox').attr('checked','checked');
			  
		}else if(jQuery(this).find('ul li :checked').length == 0) {
			jQuery(this).children('span').addClass('nodeToolUnselected');
			jQuery(this).children(':checkbox').removeAttr('checked');
			
		}else{
			jQuery(this).children('span').addClass('nodeToolPartialSelected');
			jQuery(this).children(':checkbox').attr('checked','checked');
		}
		
		// event class
		jQuery(this).find('ul li').each(function(i){
			jQuery(this).find('span').removeClass();
			if(jQuery(this).find(':checkbox').attr('checked')) {
				jQuery(this).find('span').addClass('nodeEventSelected');
			}else{
				jQuery(this).find('span').addClass('nodeEventUnselected');
			}
		});
	});
}