/*******************************************************************
 * Process a click on a link to modify citations in a citation list.
 * Links can trigger a search, a citation editor or an import page.
 * This function will check whether the context is creating a new 
 * citation list or revising an existing citation list. If it's 
 * creating a new citation list, the function checks whether a name
 * has been entered and requires an input before proceeding.  If a
 * name has been entered, the function posts an AJAX request to ensure
 * that a ContentResource and CitationCollection has been created. 
 * If that succeeds, this function makes this function call:
 * 
 * 		createSuccess.invoke(jsObj)
 * 
 * where jsObj is a Javascript object returned by the successful AJAX 
 * call.  The jsObj object has name-value pairs, including jsObj.message,
 * jsObj.contentCollectionId, jsObj.resourceUuid, and jsObj.citationCollectionId. 
 * If the AJAX request fails, this function makes this function call:
 * 
 * 		failureFunction.invoke(jqXHR, textStatus, errorThrown)
 * 
 * where the parameters are the same as those described for the error 
 * function in jQuery's ajax function (http://api.jquery.com/jQuery.ajax/). 
 * If the context is revising an existing citation list rather than creating
 * a new citation list, this function makes this function call:
 * 
 * 		modifySuccess.invoke(jsObj)
 * 
 * where jsObj is a Javascript object with name-value pairs, including 
 * jsObj.contentCollectionId, jsObj.resourceUuid, and jsObj.citationCollectionId. 
 *******************************************************************/

// assume jquery

// create citations_new_resource namespace if it doesn't exist
var citations_new_resource = citations_new_resource || {};

citations_new_resource.secondsBetweenSaveciteRefreshes = 5;

/*
 * used in the json returned by actions that
 * need to be notified to the user 
 */
var reportSuccess = function(msg){
    $('#messageSuccess').html(msg).fadeTo("slow", 1).animate({
        opacity: 1.0
    }, 5000).fadeTo(3000, 0);
};

/*
 * There has been an error
 */
var reportError = function(msg){
    $('#messageError').html(msg).show();
    window.scrollTo(0, 0);
};

/*
 * There has been an error
 */
var reportInvalidity = function(msg){
    $('#messageValidation').html(msg).fadeTo("slow", 1).animate({
        opacity: 1.0
    }, 5000).fadeTo(3000, 0);
};

var resizeFrame = function(updown) {
	if (top.location != self.location) 	 {
		var frame = parent.document.getElementById(window.name);
	}	
	if( frame ) {
		var clientH = document.body.clientHeight;
		if(updown != 'shrink') {
			clientH += 30;
		}
		$( frame ).height( clientH );
	} else {
//		throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
	}
};

var countCitationsSelected = function() {
	return $( ".itemCheckbox input:checked" ).length;
};
var resetSelectableActions = function() {
	if( countCitationsSelected() > 0 ) {
		$( ".selectAction" ).removeAttr("disabled");
	} else {
		$( ".selectAction" ).attr( "disabled", "disabled" );
	}
}
var exportCheckedCitations = function( baseUrl, citationCollectionId, resourceDisplayName ) {
  var exportUrl = baseUrl + "?citationCollectionId=" + citationCollectionId + "&resourceDisplayName=" + resourceDisplayName;
  
  // get each selected checkbox and append it to be exported
  $( ".itemCheckbox input:checked" ).each( function() {
      exportUrl += "&citationId=" + this.value;
    }
  );
  $('#download-frame').attr('src', exportUrl);
  //window.location.assign( exportUrl );
};

var exportAllCitations = function( baseUrl, citationCollectionId, resourceDisplayName ) {
  var exportUrl = baseUrl + "?citationCollectionId=" + citationCollectionId + "&resourceDisplayName=" + resourceDisplayName;
  
  $('#download-frame').attr('src', exportUrl);
  //window.location.assign( exportUrl );
};


var deleteSelectedCitations = function( baseUrl ) {
  // get each selected checkbox and append it to be removed
  $( ".itemCheckbox input:checked" ).each( function() {
      baseUrl += "&citationId=" + this.value;
    }
  );
  // do the action
  window.location.assign( baseUrl );
};

var doCitationAction = function( eventTarget ) {
	// do action
	var citationCollectionId = $('#citationCollectionId').val();
	var resourceDisplayName = $('#displayName').val();
	var action = $(eventTarget).val();
	if( action == "exportSelected" ) {
		if( countCitationsSelected() > 0 ) {
			var url = $(eventTarget).siblings('#exportUrlSel').text();
			exportCheckedCitations( url, citationCollectionId, resourceDisplayName );
		} else {
			var msg = $(eventTarget).siblings('#selectActionWarnLabel').text();
			alert( msg );
		}
	} else if( action == "exportList" ) {
		var url = $(eventTarget).siblings('#exportUrlAll').text();
		exportAllCitations( url, citationCollectionId, resourceDisplayName );
	} else if( action == "removeSelected" ) {
		if( countCitationsSelected() > 0 ) {
			var url = $(eventTarget).siblings('#removeUrlSel').text();
			deleteSelectedCitations( url );
		} else {
			var msg = $(eventTarget).siblings('#selectActionWarnLabel').text();
			alert( msg );
		}
	} else if( action == "removeList" ) {
		$('#ajaxRequest').val('false');
		$('#newCitationListForm').attr('method', 'GET');
		$('#sakai_action').val('doRemoveAllCitations');
		$('#newCitationListForm').submit();
	} else if( action == "reorderList" ) {
		$('#ajaxRequest').val('false');
		$('#newCitationListForm').attr('method', 'GET');
		$('#sakai_action').val('doShowReorderCitations');
		$('#newCitationListForm').submit();		
	}
	  
	// reset select boxes
	$( ".citationActionSelect" ).each( function() {
		this.selectedIndex = 0;
	});
};

citations_new_resource.setupToggleAreas = function(toggler, togglee, openInit, speed){
	// toggler=class of click target
	// togglee=class of container to expand
	// openInit=true - all togglee open on enter
	// speed=speed of expand/collapse animation
	if (openInit == true && openInit != null) {
		$('.expand').hide();
	}
	else {
	    $('.' + togglee).hide();
	    $('.collapse').hide();
	    resizeFrame();
	}
	$('.' + toggler).on('click', function(eventObject){
	    $(this).next('.' + togglee).fadeToggle(speed);
	    $(this).find('.expand').toggle();
	    $(this).find('.collapse').toggle();
	    resizeFrame();
	});
};

citations_new_resource.processClick = function(successAction) {
	
	var requestDisplayName = function() {
		// TODO: use sakai message in DOM
		reportInvalidity('Please supply a name for the citation list.');
		return;
	};
	/*
	 * Convert from an array of params to an object
	 */
	var mapParameters = function(array) {
		var map = {};
		for(var i = 0; i < array.length; i++) {
			var obj = array[i];
			if(obj.name && map[obj.name]) {
				if(typeof(map[obj.name]) == 'string' || typeof(map[obj.name]) == 'number') {
					map[obj.name] = [ map[obj.name], obj.value ];
				} else {
					// assume map[obj.name] is an array
					map[obj.name].push(obj.value);
				}
			} else {
				map[obj.name] = obj.value;
			}
		} 
		return map;
	};
	var postAjaxRequest = function(params, successAction) {
		var actionUrl = $('#newCitationListForm').attr('action');
		$.ajax({
			type		: 'POST',
			url			: actionUrl,
			cache		: false,
			data		: params,
			dataType	: 'json',
			success		: function(jsObj) {
				$.each(jsObj, function(key, value) {
					if(key === 'message' && value && 'null' !== value && '' !== $.trim(value)) {
						reportSuccess(value);
					} else if(key === 'secondsBetweenSaveciteRefreshes') {
						citations_new_resource.secondsBetweenSaveciteRefreshes = value;
					} else if($.isArray(value)) {
						reportError('result for key ' + key + ' is an array: ' + value);
					} else {
						$('input[name=' + key + ']').val(value);
					}
				});
				if(successAction && successAction.invoke) {
					successAction.invoke(jsObj);
				}
			},
			error		: function(jqXHR, textStatus, errorThrown) {
				// TODO: replace with reasonable error handling
				reportError("failed: " + textStatus + " :: " + errorThrown);
			}
		});		
	};
	var paramsChanged = function() {
		var params = $('#newCitationListForm').serializeArray();
		var fossils = $('#fossils').serializeArray();
		var paramsMap = mapParameters(params);
		var fossilMap = mapParameters(fossils);
		
		for(key in fossilMap) {
			if(typeof(paramsMap[key]) == 'undefined') {
				return true
			}
			if(typeof(paramsMap[key]) != typeof(fossilMap[key])) {
				return true;
			}
			if((typeof(fossilMap[key]) == 'string' || typeof(fossilMap[key]) == 'number')) { 
				if(paramsMap[key] != fossilMap[key]) {
					return true;
				}
			} else {
				// check items in list
				var fossilValues = fossilMap[key];
				var paramsValues = paramsMap[key];
				if(fossilValues.length != paramsValues.length) {
					return true;
				}
				fossilValues.sort();
				paramsValues.sort();
				for(var i = 0; i < fossilValues.length; i++) {
					if(fossilValues[i] != paramsValues[i]) {
						return true;
					}
				}
			}	
		}
		return false;
	};
	var handleNewResource = function(successAction) {
    	$('.citation_action').val('create_resource');
    	$('.requested_mimetype').val('application/json');
    	$('.ajaxRequest').val('true');
		var params = $('#newCitationListForm').find('input').serializeArray();
		postAjaxRequest(params, successAction);
	};
	var handleExistingResource = function(successAction) {
    	$('.citation_action').val('update_resource');
    	$('.requested_mimetype').val('application/json');
    	$('.ajaxRequest').val('true');
        if(paramsChanged()) {
            var newValues = $('#newCitationListForm').serializeArray();
        	postAjaxRequest(newValues, successAction);
        } else {
            var jsObj = {};
			if(successAction && successAction.invoke) {
				successAction.invoke(jsObj);
			}
        }
	};
	
	var displayName = $('#displayName').val();
	// TODO: consider chacking for #displayName_fossil and using it?
	// var oldDisplayName = $('#displayName_fossil').val();
	if(displayName && '' !== $.trim(displayName)) {
		var resourceUuid = $('#resourceUuid').val();
		if(resourceUuid) {
			// are there changes?
			handleExistingResource(successAction);
		} else {
			// create a new resource
			handleNewResource(successAction);
		}
	} else {
		// demand a displayName and stay on page
		requestDisplayName();
	}
	
};

citations_new_resource.childWindow = {};

citations_new_resource.checkForClosedWindows = function() {
	for (key in citations_new_resource.childWindow) {
		if (citations_new_resource.childWindow.hasOwnProperty(key)) {
			if(citations_new_resource.childWindow[key].closed) {
				delete citations_new_resource.childWindow[key];
			}
		}
	}
}

citations_new_resource.refreshDemanded = false;

citations_new_resource.watchForUpdates = function(timestamp) {
	
	var size = function(obj) {
        var size = 0, key;
        for (key in obj) {
            if (obj.hasOwnProperty(key)) size++;
        }
        return size;
    };
	
    if(citations_new_resource.childWindow && size(citations_new_resource.childWindow) < 1 && citations_new_resource.refreshDemanded) {
    	// If all child windows are closed and a refresh has been requested, do the refresh
    	$('#sakai_action').val('doFirstListPage');
    	$('#requested_mimetype').val('text/html');
    	$('#ajaxRequest').val('false');
    	$('#newCitationListForm').attr('method', 'GET');
    	$('#newCitationListForm').submit();
    }
    
	var actionUrl = $('#newCitationListForm').attr('action');

	var params = {
		'requested_mimetype' : 'application/json',
		'ajaxRequest' : 'true',
		'citationCollectionId' : $('#citationCollectionId').val(),
		'citation_action' : 'check_for_updates',
		'lastcheck' : timestamp
	};
	
	// check for status change in citationCollection 
	$.ajax({
		type		: 'GET',
		url			: actionUrl,
		cache		: false,
		data		: params,
		dataType	: 'json',

		success: function(jsObj) {
			// in case of status change, update this view
			if(jsObj && jsObj.changed && jsObj.changed == 'true') {
				citations_new_resource.refreshDemanded = true;
				showSpinner( '.firstPageLoad' );
			}
			if(jsObj && jsObj.secondsBetweenSaveciteRefreshes) {
				citations_new_resource.secondsBetweenSaveciteRefreshes = jsObj.secondsBetweenSaveciteRefreshes;
			}
			// if the child window is still open, schedule another check
			if(citations_new_resource.childWindow && size(citations_new_resource.childWindow) > 0) {
				setTimeout(function() { citations_new_resource.watchForUpdates(jsObj.timestamp); }, citations_new_resource.secondsBetweenSaveciteRefreshes * 1000);
			} 
		},
		error		: function(jqXHR, textStatus, errorThrown) {
			// TODO: replace with reasonable error handling
			//alert("savesort error: " + errorThrown);
			reportError("failed: " + textStatus + " :: " + errorThrown);
		},
		complete: function(jqXHR, textStatus){
			citations_new_resource.checkForClosedWindows();
		}
	});
};

citations_new_resource.init = function() {
	var DEFAULT_DIALOG_HEIGHT = 610;
	var DEFAULT_DIALOG_WIDTH = 850;
	var setFrameHeight = function() {
		var body_height = $('body').innerHeight() - 100;
	    if(body_height < DEFAULT_DIALOG_HEIGHT) {
	        var spacer_height = DEFAULT_DIALOG_HEIGHT - body_height;
	        $('body').append('<div style="height:' + spacer_height + 'px; min-height:' + spacer_height + 'px;"></div>');
	        setMainFrameHeight( window.name );
	    }		
	};
	
	var needToSaveAnyChanges = function() {
		var resourceUuid = $('#resourceUuid').val();
		if(resourceUuid && resourceUuid.length && resourceUuid.length > 0) {
			// in this case, resource already exists, so return true to check for changes
			return true;
		} else {
			var displayName = $('#displayName').val();
			if(displayName && displayName.length && displayName.length > 0) {
				// in this case, return true to save any changes in props
				return true;
			}
		}
		return false;
	}
	
	
	
	$('.saveciteClient input').on('click', function(eventObject) {
		$('#saveciteClientId').val($(eventObject.target).attr('id'));
		var successObj = {
			citationCollectionId: $('#citationCollectionId').val(),
			linkId				: $(eventObject.target).attr('id'),
			saveciteClientUrl	: $(eventObject.target).siblings('.saveciteClientUrl').text(),
			popupTitle			: $(eventObject.target).siblings('.popupTitle').text(),
			windowHeight		: $(eventObject.target).siblings('.windowHeight').text(),
			windowWidth			: $(eventObject.target).siblings('.windowWidth').text(),
			invoke				: function(jsObj) {
				if(citations_new_resource.childWindow && citations_new_resource.childWindow[this.linkId] && citations_new_resource.childWindow[this.linkId].close) {
					citations_new_resource.childWindow[this.linkId].close();
				}
				if(jsObj && jsObj.secondsBetweenSaveciteRefreshes) {
					citations_new_resource.secondsBetweenSaveciteRefreshes = jsObj.secondsBetweenSaveciteRefreshes;
				}
				if(jsObj.saveciteUrl) {
					this.saveciteClientUrl = jsObj.saveciteUrl;
				}
				citations_new_resource.childWindow[this.linkId] = openWindow(this.saveciteClientUrl, this.popupTitle, 'scrollbars=yes,toolbar=yes,resizable=yes,height=' + this.windowHeight + ',width=' + this.windowWidth);
				citations_new_resource.childWindow[this.linkId].focus();
				setTimeout(function() { citations_new_resource.watchForUpdates(jsObj.timestamp + 1); }, citations_new_resource.secondsBetweenSaveciteRefreshes * 1000);
			}
		};
		
		citations_new_resource.processClick(successObj);
		return false;
	});
	$('#Search').on('click', function(eventObject) {
		var successObj = {
			citationCollectionId: $('#citationCollectionId').val(),
			linkId				: $(eventObject.target).attr('id'),
			searchUrl			: $(eventObject.target).siblings('.searchUrl').text(),
			popupTitle			: $(eventObject.target).siblings('.popupTitle').text(),
			invoke				: function(jsObj) {
				try {
					if(jsObj && jsObj.resourceUuid) {
						searchUrl += "&resourceId=" + jsObj.resourceUuid;
					}
					if(jsObj && jsObj.citationCollectionId) {
						searchUrl += "&citationCollectionId=" + jsObj.citationCollectionId;
					}
				} catch (e) {
					reportError(e);
				}
				if(jsObj && jsObj.secondsBetweenSaveciteRefreshes) {
					citations_new_resource.secondsBetweenSaveciteRefreshes = jsObj.secondsBetweenSaveciteRefreshes;
				}
				if(citations_new_resource.childWindow && citations_new_resource.childWindow[this.linkId] && citations_new_resource.childWindow[this.linkId].close) {
					citations_new_resource.childWindow[this.linkId].close();
				}
				citations_new_resource.childWindow[this.linkId] = openWindow(this.searchUrl,this.popupTitle,'scrollbars=yes,toolbar=yes,resizable=yes,height=' + DEFAULT_DIALOG_HEIGHT + ',width=' + DEFAULT_DIALOG_WIDTH);
				citations_new_resource.childWindow[this.linkId].focus();
				setTimeout(function() { citations_new_resource.watchForUpdates(jsObj.timestamp + 1); }, citations_new_resource.secondsBetweenSaveciteRefreshes * 1000);
			}
		};
		citations_new_resource.processClick(successObj);
		return false;
	});
	$('#SearchGoogle').on('click', function(eventObject) {
		var successObj = {
			citationCollectionId: $('#citationCollectionId').val(),
			linkId				: $(eventObject.target).attr('id'),
			googleUrl			: $(eventObject.target).siblings('.googleUrl').text(),
			popupTitle			: $(eventObject.target).siblings('.popupTitle').text(),
			invoke				: function(jsObj) {
				if(citations_new_resource.childWindow && citations_new_resource.childWindow[this.linkId] && citations_new_resource.childWindow[this.linkId].close) {
					citations_new_resource.childWindow[this.linkId].close();
				}
				if(jsObj && jsObj.secondsBetweenSaveciteRefreshes) {
					citations_new_resource.secondsBetweenSaveciteRefreshes = jsObj.secondsBetweenSaveciteRefreshes;
				}
				citations_new_resource.childWindow[this.linkId] = openWindow(this.googleUrl,this.popupTitle,'scrollbars=yes,toolbar=yes,resizable=yes,height=' + DEFAULT_DIALOG_HEIGHT + ',width=' + DEFAULT_DIALOG_WIDTH);
				citations_new_resource.childWindow[this.linkId].focus();
				setTimeout(function() { citations_new_resource.watchForUpdates(jsObj.timestamp + 1); }, citations_new_resource.secondsBetweenSaveciteRefreshes * 1000);
			}
		};
		citations_new_resource.processClick(successObj);
		return false;
	});
	$('#PickResource').on('click', function(eventObject) {
		var successObj = {
			invoke				: function(jsObj) {
				$('#sakai_action').val('doPickResource');
				$('#ajaxRequest').val('false');
				$('#newCitationListForm').submit();
			}
		};
		citations_new_resource.processClick(successObj);
		return false;
	});
	$('#CreateCitation').on('click', function(eventObject) {
		var successObj = {
			invoke				: function(jsObj) {
				$('#sakai_action').val('doCreate');
				$('#ajaxRequest').val('false');
				$('#newCitationListForm').submit();
			}
		};
		citations_new_resource.processClick(successObj);
		return false;
	});
	$('#newCitationListForm .citationFields').on('keypress', function(e) {
		if(e.which == 13) {
			e.preventDefault();
			return false;
		}
	});
	$('#ImportCitation').on('click', function(eventObject) {
		var successObj = {
			invoke				: function(jsObj) {
				$('#sakai_action').val('doImportPage');
				$('#ajaxRequest').val('false');
				$('#newCitationListForm').submit();
			}
		};
		citations_new_resource.processClick(successObj);
		return false;
	});
	$('.Cancel').on('click', function(eventObject) {

		SPNR.disableControlsAndSpin( this, null );

		if(needToSaveAnyChanges()) {
			var successObj = {
					invoke				: function(jsObj) {
						$('#sakai_action').val('doCancel');
						$('#ajaxRequest').val('false');
						$('#newCitationListForm').attr('method', 'GET');
						$('#newCitationListForm').submit();
					}
				};
				citations_new_resource.processClick(successObj);
				return false;
		} else {
			$('#sakai_action').val('doCancel');
			$('#ajaxRequest').val('false');
			$('#newCitationListForm').attr('method', 'GET');
			$('#newCitationListForm').submit();
		}
	});
	$('#access_mode_groups').on('change', function(eventObject) {
		$('#groupTable').toggle();
	});
	$('#hideAccess, #showAccess').on('click', function(eventObject){
		$('#accessShown').toggle();
		$('#accessHidden').toggle();
		setFrameHeight();
		return false;
	});
	$('.firstPage').on('click', function(eventObject){
		showSpinner( '.pageLoad' );
		$('#sakai_action').val('doFirstListPage');
		$('#requested_mimetype').val('text/html');
		$('#ajaxRequest').val('false');
		$('#newCitationListForm').attr('method', 'GET');
		$('#newCitationListForm').submit();
	});
	$('.prevPage').on('click', function(eventObject){
		// onclick="javascript: showSpinner( '.pageLoad' ); document.getElementById('sakai_action').value='doPrevListPage'; submitform('$FORM_NAME');"
		showSpinner( '.pageLoad' );
		$('#sakai_action').val('doPrevListPage');
		$('#requested_mimetype').val('text/html');
		$('#ajaxRequest').val('false');
		$('#newCitationListForm').attr('method', 'GET');
		$('#newCitationListForm').submit();
	});
	$('.nextPage').on('click', function(eventObject){
		// onclick="javascript: showSpinner( '.pageLoad' ); document.getElementById('sakai_action').value='doPrevListPage'; submitform('$FORM_NAME');"
		showSpinner( '.pageLoad' );
		$('#sakai_action').val('doNextListPage');
		$('#requested_mimetype').val('text/html');
		$('#ajaxRequest').val('false');
		$('#newCitationListForm').attr('method', 'GET');
		$('#newCitationListForm').submit();
	});
	$('.lastPage').on('click', function(eventObject){
		// onclick="javascript: showSpinner( '.pageLoad' ); document.getElementById('sakai_action').value='doPrevListPage'; submitform('$FORM_NAME');"
		showSpinner( '.pageLoad' );
		$('#sakai_action').val('doLastListPage');
		$('#requested_mimetype').val('text/html');
		$('#ajaxRequest').val('false');
		$('#newCitationListForm').attr('method', 'GET');
		$('#newCitationListForm').submit();
	});
	$('.pageSize').on('focus', function(eventObject){
		//alert('feelin focused');
	});
	$('.pageSize').on('change', function(eventObject){
		// onchange="javascript: changePageSize( 'doChangeListPageSize', 'top', '$FORM_NAME' );"
		showSpinner( '.pageLoad' );
		$('#ajaxRequest').val('false');
		var newPageSize = $(eventObject.target).val();
		var location = $(eventObject.target).siblings('.pageSizeLocation').val();
		$('#requested_mimetype').val('text/html');
		$('#sakai_action').val('doChangeListPageSize');
		$('#pageSelector').val(location);
		$('#newPageSize').val(newPageSize);
		$('#newCitationListForm').attr('method', 'GET');
		$('#newCitationListForm').submit();
	});
	$('.citationActionSelect').on('focus', function(eventObject){
		//onfocus="resetSelectableActions()" 
		resetSelectableActions();
	});
	$('.citationActionSelect').on('change', function(eventObject){
		doCitationAction(eventObject.target);
	});
	$('.citationSortAction').on('change', function(eventObject){
		var newSort = $(eventObject.target).val();
		var oldSort = $('#currentSort').val();
		if(newSort !== oldSort) {
			$('#currentSort').val(newSort);
			$('#ajaxRequest').val('false');
			$('#newCitationListForm').attr('method', 'GET');
			$('#sakai_action').val('doSortCollection');
			//alert("citationSortAction submitting form");
			$('#newCitationListForm').submit();
		}
	});
	$('#savesort').on('click', function(eventObject){
		var actionUrl = $('#newCitationListForm').attr('action');
		//alert("savesort actionUrl:: " + actionUrl);
		$('#ajaxRequest').val('true');
		$('#requested_mimetype').val('application/json');
		$('#citation_action').val('update_saved_sort');
		var params = $('#newCitationListForm').find('input').serializeArray();
		params.push({'new_sort': $('#citationSortAction').val()});
		//alert("savesort params: " + params);
		$.ajax({
			type		: 'POST',
			url			: actionUrl,
			cache		: false,
			data		: params,
			dataType	: 'json',
			success		: function(jsObj) {
				$.each(jsObj, function(key, value) {
					if(key === 'message' && value && 'null' !== value && '' !== $.trim(value)) {
						reportSuccess(value);
					} else if(key === 'secondsBetweenSaveciteRefreshes') {
						citations_new_resource.secondsBetweenSaveciteRefreshes = value;
					} else if($.isArray(value)) {
						reportError('result for key ' + key + ' is an array: ' + value);
					} else {
						$('input[name=' + key + ']').val(value);
					}
				});
			},
			error		: function(jqXHR, textStatus, errorThrown) {
				// TODO: replace with reasonable error handling
				//alert("savesort error: " + errorThrown);
				reportError("failed: " + textStatus + " :: " + errorThrown);
			}
		});	
		//alert("savesort done");
		return false;
	});
	$('.selectAll').on('click', function(eventObject) {
		$( ".itemCheckbox input:checkbox" ).attr("checked","checked");
		highlightCheckedSelections();
	});
	$('.selectNone').on('click', function(eventObject) {
		$( ".itemCheckbox input:checkbox" ).removeAttr("checked");
		highlightCheckedSelections();
	});
	
	
	// If changes are saved, "Done" button should be disabled and "Cancel" button should be enabled
	// If changes are not saved, "Done" button should be enabled and "Cancel" button should be disabled
	$('form').find('input').on('change', function(eventObject){
		
		// if values of input elements in form have changed since save, enable "Cancel" button and disable "Done" button
	});
	$(window).on('unload', function() {
		if(citations_new_resource.childWindow) {
			for (key in citations_new_resource.childWindow) {
				if(citations_new_resource.childWindow[key] && citations_new_resource.childWindow[key].close) {
					citations_new_resource.childWindow[key].close();
				}
			}
		}
	});
	
	setFrameHeight();

};

$(document).ready(function(){
	citations_new_resource.init();
	citations_new_resource.setupToggleAreas('toggleAnchor', 'toggledContent', false, 'fast');
});

