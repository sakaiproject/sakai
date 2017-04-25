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
		jQuery(this).removeProp('disabled');
	    if(what == 'what-resources'){
	    	if(i == TOTALSBY_IX_TOOL || i == TOTALSBY_IX_EVENT) {
	    		// disable Tool and Event selection
	    		jQuery(this).removeProp('selected').prop('disabled', true);
	    	}
	    }else if(what == 'what-visits') {
	    	if(i == TOTALSBY_IX_TOOL || i == TOTALSBY_IX_RESOURCE || i == TOTALSBY_IX_RESOURCEACTION) {
	    		// disable Tool, Resource and Resource Action selection
	    		jQuery(this).removeProp('selected').prop('disabled', true);
	    	}
	    }else if(what == 'what-events') {
	    	if(i == TOTALSBY_IX_RESOURCE || i == TOTALSBY_IX_RESOURCEACTION) {
	    		// disable Resource and Resource Action selection
	    		jQuery(this).removeProp('selected').prop('disabled', true);
	    	}
	    }else if(what == 'what-presences') {
	    	if(i == TOTALSBY_IX_TOOL || i == TOTALSBY_IX_EVENT || i == TOTALSBY_IX_RESOURCE || i == TOTALSBY_IX_RESOURCEACTION) {
	    		// disable Resource and Resource Action selection
	    		jQuery(this).removeProp('selected').prop('disabled', true);
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
	    var howTotalsByUserSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_USER]).prop('selected');
	    var howTotalsByToolSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_TOOL]).prop('selected');
	    var howTotalsByEventSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_EVENT]).prop('selected');
	    var howTotalsByResourceSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_RESOURCE]).prop('selected');
	    var howTotalsByResourceActionSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_RESOURCEACTION]).prop('selected');
	    var howTotalsByDateSelected = jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_DATE]).prop('selected');
	    
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
				jQuery(this).removeProp('disabled');
				var value = jQuery(this).val();
				if(value == 'user') {
					if(!howTotalsByUserSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'tool') {
					if(!howTotalsByToolSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'event') {
					if(!howTotalsByEventSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'resource') {
					if(!howTotalsByResourceSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'resource-action') {
					if(!howTotalsByResourceActionSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'date') {
					if(!howTotalsByDateSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}				
			});
	    	
	    }else if(chartType == 'timeseries' || chartType == 'timeseriesbar') {
	    	jQuery('#chartDataSourceTr').hide();
			jQuery('#chartSeriesSourceTr').show();
	    	jQuery('#howChartSource option').each(function(i){
	    		if(jQuery(this).val() == 'date') {
	    			jQuery(this).prop('selected','selected');
	    		}
	    	});
	    	// select Date if chart is TimeSeries based
    	    jQuery(jQuery('#howTotalsBy').children()[TOTALSBY_IX_DATE]).prop('selected','selected');
    		// disable invalid options in chart series source options
		    jQuery('#howChartSeriesSource option').each(function(i){
				jQuery(this).removeProp('disabled');
				var value = jQuery(this).val();
				if(value == 'user') {
					if(!howTotalsByUserSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'tool') {
					if(!howTotalsByToolSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'event') {
					if(!howTotalsByEventSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'resource') {
					if(!howTotalsByResourceSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
					}
				}
				if(value == 'resource-action') {
					if(!howTotalsByResourceActionSelected) {
						jQuery(this).prop('disabled', true);
						if(jQuery(this).prop('selected')) {
							jQuery(this).removeProp('selected');
						}
					}else{
						jQuery(this).removeProp('disabled');
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
	if(what == 'what-visits' || what == 'what-presences') {
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
		jQuery('#what-selection-label').show();
		jQuery('#whatEventSelType').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		if(jQuery('#whatLimitedAction').prop('checked')) {
			jQuery('#whatResourceAction').removeProp('disabled');
		}else{
			jQuery('#whatResourceAction').prop('disabled', true);
		}
		if(jQuery('#whatLimitedResourceIds').prop('checked')) {
			jQuery('#whatResourceIds').removeProp('disabled');
			jQuery('.containerHover').hide();
		}else{
			jQuery('#whatResourceIds').prop('disabled', true);
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
	if(jQuery('#howSortCheck').prop('checked')) {
		jQuery('#howSortBy').removeProp('disabled');
		jQuery('#howSortAscending').removeProp('disabled');
	}else{
		jQuery('#howSortBy').prop('disabled', true);
		jQuery('#howSortAscending').prop('disabled', true);
	}
	// max results
	if(jQuery('#howMaxResultsCheck').prop('checked')){
		jQuery('#howMaxResults').removeProp('disabled');
	}else{
		jQuery('#howMaxResults').prop('disabled', true);
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

function loadJQueryDatePicker(inputField, value){
	localDatePicker({
	  input: '#'+inputField,
	  useTime: 1,
	  parseFormat: 'YYYY-MM-DD HH:mm:ss',
	  allowEmptyDate: false,
	  val: value,
	  ashidden: { iso8601: inputField+'ISO8601' }
  });
}
