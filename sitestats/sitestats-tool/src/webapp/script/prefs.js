/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function updateToolSelection(selector) {
	jQuery(selector).each(function(i){
		jQuery(this).children('span').removeClass();
		
		// tool class
		if(jQuery(this).find('ul li :checkbox').length == jQuery(this).find('ul li :checked').length) {
			jQuery(this).children('span').addClass('nodeToolSelected');
			jQuery(this).children(':checkbox').prop('checked','checked');
			  
		}else if(jQuery(this).find('ul li :checked').length === 0) {
			jQuery(this).children('span').addClass('nodeToolUnselected');
			jQuery(this).children(':checkbox').removeProp('checked');
			
		}else{
			jQuery(this).children('span').addClass('nodeToolPartialSelected');
			jQuery(this).children(':checkbox').prop('checked','checked');
		}
		
		// event class
		jQuery(this).find('ul li').each(function(i){
			jQuery(this).find('span').removeClass();
			if(jQuery(this).find(':checkbox').prop('checked')) {
				jQuery(this).find('span').addClass('nodeEventSelected');
			}else{
				jQuery(this).find('span').addClass('nodeEventUnselected');
			}
		});
	});
}

function toggleCheckboxAll() {
	if(jQuery('#useAllTools').prop('checked')) {
		jQuery('.eventTree').hide();
	}else{
		jQuery('.eventTree').show();
		setMainFrameHeightNoScroll( window.name );
	}
}

function selectUnselectEvents(obj) {
	if(obj.checked) {
		jQuery(obj).parent().find('ul li :checkbox').prop('checked','checked');
	}else{
		jQuery(obj).parent().find('ul li :checkbox').removeProp('checked');
	}
}

function updateAllToolsSelection() {
	updateToolSelection('.tool');
}
