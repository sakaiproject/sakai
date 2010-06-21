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
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Option indexes for "How:Totals by" select box
var TOTALSBY_IX_USER = 0;
var TOTALSBY_IX_TOOL = 1;
var TOTALSBY_IX_EVENT = 2;
var TOTALSBY_IX_RESOURCE = 3;
var TOTALSBY_IX_RESOURCEACTION = 4;
var TOTALSBY_IX_DATE = 5;
var TOTALSBY_IX_SITE = 6;

function checkHowTotalsBySelection() {
	var what = jQuery('#what').val();
	// disable invalid options in howTotalsBy
	jQuery('#howTotalsBy option').each(function(i){
		jQuery(this).removeAttr('disabled');
	    if(what == 'what-resources'){
	    	if(i == TOTALSBY_IX_TOOL || i == TOTALSBY_IX_EVENT) {
	    		// disable Tool and Event selection
	    		jQuery(this).removeAttr('selected').attr('disabled','disabled');
	    	}
	    }else if(what == 'what-visits') {
	    	if(i == TOTALSBY_IX_TOOL || i == TOTALSBY_IX_RESOURCE || i == TOTALSBY_IX_RESOURCEACTION) {
	    		// disable Tool, Resource and Resource Action selection
	    		jQuery(this).removeAttr('selected').attr('disabled','disabled');
	    	}
	    }else if(what == 'what-events') {
	    	if(i == TOTALSBY_IX_RESOURCE || i == TOTALSBY_IX_RESOURCEACTION) {
	    		// disable Resource and Resource Action selection
	    		jQuery(this).removeAttr('selected').attr('disabled','disabled');
	    	}
	    }else if(what == 'what-presences') {
	    	if(i == TOTALSBY_IX_TOOL || i == TOTALSBY_IX_EVENT || i == TOTALSBY_IX_RESOURCE || i == TOTALSBY_IX_RESOURCEACTION) {
	    		// disable Resource and Resource Action selection
	    		jQuery(this).removeAttr('selected').attr('disabled','disabled');
	    	}
	    }
	});
}

function checkHowChartSelection() {
	var presentation = jQuery('#howPresentation').val();
	if(presentation == 'how-presentation-table') {
		jQuery('#chartTypeTr').hide();
		jQuery('#chartDataSourceTr').hide();
		jQuery('#chartSeriesSourceTr').hide();
	}else{
		jQuery('#chartTypeTr').show();
	    var chartType = jQuery('#howChartType').val();
	    var howTotalsByUserSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_USER]).attr('selected');
	    var howTotalsByToolSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_TOOL]).attr('selected');
	    var howTotalsByEventSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_EVENT]).attr('selected');
	    var howTotalsByResourceSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_RESOURCE]).attr('selected');
	    var howTotalsByResourceActionSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_RESOURCEACTION]).attr('selected');
	    var howTotalsByDateSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_DATE]).attr('selected');
	    
	    if(chartType == 'bar' || chartType == 'pie') {
			jQuery('#chartDataSourceTr').show();
			jQuery('#chartSeriesSourceTr').hide();
	    	if(chartType == 'bar') {
				jQuery('#howChartCategorySourceContainer').show();
		    }else if(chartType == 'pie') {
		    	jQuery('#howChartCategorySourceContainer').hide();
		    }
	    	// disable invalid options in chart data source options
		    jQuery('#howChartSource option, #howChartCategorySource option').each(function(i){
				jQuery(this).removeAttr('disabled');
				var value = jQuery(this).val();
				if(value == 'user') {
					if(!howTotalsByUserSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'tool') {
					if(!howTotalsByToolSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'event') {
					if(!howTotalsByEventSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'resource') {
					if(!howTotalsByResourceSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'resource-action') {
					if(!howTotalsByResourceActionSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'date') {
					if(!howTotalsByDateSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}				
			});
	    	
	    }else if(chartType == 'timeseries' || chartType == 'timeseriesbar') {
	    	jQuery('#chartDataSourceTr').hide();
			jQuery('#chartSeriesSourceTr').show();
	    	jQuery('#howChartSource option').each(function(i){
	    		if(jQuery(this).val() == 'date') {
	    			jQuery(this).attr('selected','selected');
	    		}
	    	});
	    	// select Date if chart is TimeSeries based
    	    jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_DATE]).attr('selected','selected');
    		// disable invalid options in chart series source options
		    jQuery('#howChartSeriesSource option').each(function(i){
				jQuery(this).removeAttr('disabled');
				var value = jQuery(this).val();
				if(value == 'user') {
					if(!howTotalsByUserSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'tool') {
					if(!howTotalsByToolSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'event') {
					if(!howTotalsByEventSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'resource') {
					if(!howTotalsByResourceSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
				if(value == 'resource-action') {
					if(!howTotalsByResourceActionSelected) {
						jQuery(this).attr('disabled','disabled');
						if(jQuery(this).attr('selected')) {
							jQuery(this).removeAttr('selected');
						}
					}else{
						jQuery(this).removeAttr('disabled');
					}
				}
			});
	    }
	    // just in case, remove possible invalid option (reportParams.howChartCategorySource.null)
	    jQuery('#howChartCategorySource option[value=""]').remove();
		setMainFrameHeightNoScroll(window.name);
	}
}

function checkWhatSelection() {
	var what = jQuery('#what').val();
	if(what == 'what-visits') {
		jQuery('#what').css('width','300px');
		jQuery('#what-selection-label').hide();
		jQuery('#whatEventSelType').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		jQuery('#what-resources-options').hide();
		jQuery('#what-resources-select').hide();
	}else if(what == 'what-events') {
		jQuery('#what').css('width','122px');
		var eventsSelectionType = jQuery('#whatEventSelType').val();
		jQuery('#what-selection-label').show();
		jQuery('#whatEventSelType').show();
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
		jQuery('#what').css('width','300px');
		jQuery('#what-selection-label').show();
		jQuery('#whatEventSelType').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		if(jQuery('#whatLimitedAction').attr('checked')) {
			jQuery('#whatResourceAction').removeAttr('disabled');
		}else{
			jQuery('#whatResourceAction').attr('disabled','disabled');
		}
		if(jQuery('#whatLimitedResourceIds').attr('checked')) {
			jQuery('#whatResourceIds').removeAttr('disabled');
			jQuery('.containerHover').hide();
		}else{
			jQuery('#whatResourceIds').attr('disabled','disabled');
			jQuery('.containerHover').show();
		}
		jQuery('#what-resources-options').show();
		jQuery('#what-resources-select').show();
	}
	checkHowTotalsBySelection();
	checkHowChartSelection();
	setMainFrameHeightNoScroll(window.name);
}

function checkWhenSelection() {
	if(jQuery('#when').val() == 'when-custom'){
		jQuery('#when-custom-from').show();
		jQuery('#when-custom-to').show();
		setMainFrameHeightNoScroll(window.name);
	}else{
		jQuery('#when-custom-from').hide();
		jQuery('#when-custom-to').hide();
	}	
}

function checkWhoSelection() {
	var who = jQuery('#who').val();
	jQuery('.wicket-ajax-indicator').hide();
	if(who == 'who-all'){
		jQuery('#whoRole').hide();
		jQuery('#whoGroup').hide();
		jQuery('#whoCustom').hide();
	}else if(who == 'who-role'){
		jQuery('#whoRole').show();
		jQuery('#whoGroup').hide();
		jQuery('#whoCustom').hide();
	}else if(who == 'who-groups'){
		jQuery('#whoRole').hide();
		jQuery('#whoGroup').show();
		jQuery('#whoCustom').hide();
	}else if(who == 'who-custom'){
		jQuery('#whoRole').hide();
		jQuery('#whoGroup').hide();
		jQuery('#whoCustom').show();
	}else{
		jQuery('#whoRole').hide();
		jQuery('#whoGroup').hide();
		jQuery('#whoCustom').hide();
	}
	setMainFrameHeightNoScroll(window.name);
}

function checkHowSelection() {	
	// sorting
	if(jQuery('#howSortCheck').attr('checked')) {
		jQuery('#howSortBy').removeAttr('disabled');
		jQuery('#howSortAscending').removeAttr('disabled');
	}else{
		jQuery('#howSortBy').attr('disabled','disabled');
		jQuery('#howSortAscending').attr('disabled','disabled');
	}
	// max results
	if(jQuery('#howMaxResultsCheck').attr('checked')){
		jQuery('#howMaxResults').removeAttr('disabled');
	}else{
		jQuery('#howMaxResults').attr('disabled','disabled');
		jQuery('#howMaxResults').val('0');
	}
}

function checkReportDetails() {
	if(jQuery('#reportSite').val() == 'all') {
		jQuery('#who').val('who-all');
		jQuery('#whoSection').hide();
	}else{
		jQuery('#whoSection').show();
	}
	setMainFrameHeightNoScroll(window.name);
}