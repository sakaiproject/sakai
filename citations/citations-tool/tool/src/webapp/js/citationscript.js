/*
 * define the CitationsHelperFrame object
 */
function CitationsHelperFrame( baseUrl, mode ) {
  this.baseUrl = baseUrl;
  this.mode    = mode;
  this.refreshCitationsHelper = refreshCitationsHelper;
}

/*
 * CitationsHelperFrame method to refresh the screen.  Should only work on
 * LIST and ADD_CITATIONS modes (citationsHelperMode variable needs to be set)
 */
function refreshCitationsHelper() {
  if( this.mode == "LIST" ) {
    // need to reload page instead of just count
    window.location.assign( this.baseUrl + "&sakai_action=doList" );
  } else {
    // need to reload just the count
    if( document.getElementById( "messageDiv" ) ) {
      // need to have ?panel=null in the URL
      if( ! this.baseUrl.match( "panel\=null" ) ) {
        this.baseUrl += "?panel=null";
      }
    
      $( "#messageDiv" ).load( this.baseUrl + "&sakai_action=doMessageFrame&operation=refreshCount",
        function() {
          // update the citation list count using the value from the AJAX response
          $( "#citationCountDisplay" ).html( document.getElementById( "citationCount" ).value );
        }
      );
    }
  }
}

/*
 * Shows advanced form (and hides basic form) on search and search results
 * screens.  Advanced and basic search form elements need to have the correct
 * ids for this function to work properly.
 */
function showAdvancedForm( advancedType ) {
  // hide the basic form, header & button
  $( "#basicSearchForm" ).hide();
  $( "#basicSearchHeader" ).hide();
  $( "#basicSearchButton" ).hide();
  
  // show the advanced form & header
  $( "#advancedSearchForm" ).show();
  $( "#advancedSearchHeader" ).show();
  $( "#advancedSearchButton" ).show();
  
  // carry over any keywords entered if advanced keyword field is blank
  var advKeywordCriteria = document.getElementById( "advCriteria1" );
  if( advKeywordCriteria.value == "" ) {
    advKeywordCriteria.value = document.getElementById( "keywords" ).value;
  }
  
  // set hidden form parameter value
  $( "#searchType" ).val( advancedType );
  
  resizeFrame();
}

/*
 * Shows basic form (and hides advanced form) on search and search results
 * screens.  Advanced and basic search form elements need to have the correct
 * ids for this function to work properly.
 */
function showBasicForm( basicType ) {
  // hide the advanced form, header & button
  $( "#advancedSearchForm" ).hide();
  $( "#advancedSearchHeader" ).hide();
  $( "#advancedSearchButton" ).hide();
  
  // show the basic form, header & button
  $( "#basicSearchForm" ).show();
  $( "#basicSearchHeader" ).show();
  $( "#basicSearchButton" ).show();
  
  // carry over advanced fields entered
  var keywordBuffer = "";
  $( ".advField" ).each( function() {
    if( this.value && this.value != "" ) {
      keywordBuffer = keywordBuffer + this.value + " ";
    }
  } );
  
  if( keywordBuffer.length > 0 ) {
    var keywords = keywordBuffer.substr( 0, keywordBuffer.length-1 );
    document.getElementById( "keywords" ).value = keywords;
  }
  
  // set hidden form parameter value
  $( "#searchType" ).val( basicType );
  
  resizeFrame();
}

function clearAdvancedForm() {
  $( ".advField" ).each( function() {
    this.value = "";
  } );
}

/*
 * Shows/hides details associated with a given search result.
 *
 * Params:
 *   citationId  id of the citation with details to show/hide
 *   altShow     text used to set the alt of the show toggle icon
 *   altHide     text used to set the alt of the hide toggle icon
 */
function toggleDetails( citationId, altShow, altHide ) {
  $( "#details_" + citationId ).slideToggle( 300,
    function() {
      resizeFrame();
    }
  );
  
  // toggle expand/hide image
  var image = document.getElementById( "toggle_" + citationId );
  
  if( image.alt == altShow ) {
    image.src = "/library/image/sakai/collapse.gif?panel=Main";
    image.alt = altHide;
  } else {
    image.src = "/library/image/sakai/expand.gif?panel=Main";
    image.alt = altShow;
  }
}

/*
 * Shows the details of all search results on the screen.
 *
 * Params:
 *   altHide  text used to set the alt of the hide toggle icon
 */
function showAllDetails( altHide ) {
  // show descriptions
  $( ".citationDetails" ).show( function() {
      resizeFrame();
    }
  );

  // show proper toggle icon
  $( ".toggleIcon" ).each( function() {
    this.src = "/library/image/sakai/collapse.gif?panel=Main";
    this.alt = altHide;
  } );
}

/*
 * Hides the details of all search results on the screen.
 *
 * Params:
 *   altShow  text used to set the alt of the show toggle icon
 */
function hideAllDetails( altShow ) {
  // hide descriptions
  $( ".citationDetails" ).hide( function() {
      resizeFrame();
    }
  );
  
  // show proper toggle icon
  $( ".toggleIcon" ).each( function() {
    this.src = "/library/image/sakai/expand.gif?panel=Main";
    this.alt = altShow;
  } );
}

/*
 * Checks all checkboxes in the given form
 *
 * Params:
 *   formId  id of form element for which all checkboxes should be checked
 */
function selectAll( formId ) {
  $( "input:checkbox", "#" + formId ).attr( "checked", "checked" );
  highlightCheckedSelections();
}

/*
 * Unchecks all checkboxes in the given form
 *
 * Params:
 *   formId  id of form element for which all checkboxes should be unchecked
 */
function selectNone( formId ) {
  $( "input:checkbox", "#" + formId ).attr( "checked", "" );
  highlightCheckedSelections();
}

/*
 * Resizes the frame to avoid double scroll bars when making dynamic changes
 * to the page.  This method has not been tested with IE 5.5.
 */
function resizeFrame() {

// check needed as getElementById in IE doesn't like an empty parameter so the var
// frame line returns an error. Firefox appears to be okay with it
  if (window.name != "")
  {
  	var frame = parent.document.getElementById( window.name );
      
  	if( frame ) 
  	{
    	var clientH = document.body.clientHeight + 10;
    	$( frame ).height( clientH );
  	}
  }
} // end function

/*
 * Submits the form with the given id
 */
function submitform( id )
{
  var theForm = document.getElementById(id);
  if(theForm && theForm.onsubmit)
  {
    theForm.onsubmit();
  }
  if(theForm && theForm.submit)
  {
     theForm.submit();
  }
}

/*
 * Adds/removes a citation to/from a citation collection.
 *
 * This function makes an AJAX call to the server to add/remove the citation
 * without having to refresh the page.
 *
 * Params:
 *   baseUrl         base URL for this tool
 *   citationButton  button used to control add/remove of a citation
 *   collectionId    id of the citation collection to edit
 *   spinnerId       id of the html element used to show/hide a spinner while
 *                   the AJAX process completes
 *   addLabel        value used for the button to indicate add
 *   removeLabel     value used for the button to indicate remove
 */
function toggleCitation( baseUrl, citationButton, collectionId, spinnerId, addLabel, removeLabel )
{
  if(citationButton && citationButton.value)
  {
    // switch to spinner
    $( "#" + citationButton.id ).hide();
    $( "#" + spinnerId ).show();

    var notSet = "NOTSET";
          
    var firstPage = notSet;
    var prevPage = notSet;
    var nextPage = notSet;
          
          
    if (document.getElementById("firstPage1").getAttribute("disabled") != null);
       firstPage = document.getElementById("firstPage1").getAttribute("disabled");
            
    if (document.getElementById("prevPage1").getAttribute("disabled") != null);
        prevPage = document.getElementById("prevPage1").getAttribute("disabled");

    if (document.getElementById("nextPage1").getAttribute("disabled") != null);
        nextPage = document.getElementById("nextPage1").getAttribute("disabled");
    
    // disable inputs
    $( "input" ).attr( "disabled", "disabled" );


    if( addLabel == citationButton.value )
    {
      // do AJAX load
      $( "#messageDiv" ).load( baseUrl + "&sakai_action=doMessageFrame&collectionId=" + collectionId + "&citationId=" + citationButton.id + "&operation=add",
        function() {
        
          // update the button's id using the value from the AJAX response
          document.getElementById( citationButton.id ).id = document.getElementById( "addedCitationId" ).value;
          
          // update the citation list count using the value from the AJAX response
          if( document.getElementById( "citationCountDisplay" ) && document.getElementById( "citationCount" ) ) {
            $( "#citationCountDisplay" ).html( document.getElementById( "citationCount" ).value );
          }
          
          // hide the spinner
          $( "#" + spinnerId ).hide();
          $( "#" + citationButton.id ).show();
          
          // change label to remove
          citationButton.value = removeLabel;
          
          // change highlighting
          highlightButtonSelections( removeLabel );
          
          // enable inputs
          $( "input" ).attr( "disabled", "" );
        
          if (firstPage != null && firstPage != notSet && firstPage)
          {
             document.getElementById("firstPage1").setAttribute("disabled", firstPage);
             document.getElementById("firstPage2").setAttribute("disabled", firstPage);
          }
          
          if (prevPage != null && prevPage != notSet && prevPage)
          {
             document.getElementById("prevPage1").setAttribute("disabled", prevPage);
             document.getElementById("prevPage2").setAttribute("disabled", prevPage);
          }

          if (nextPage != null && nextPage != notSet && nextPage)
          {
             document.getElementById("nextPage1").setAttribute("disabled", nextPage);
             document.getElementById("nextPage2").setAttribute("disabled", nextPage);
          }

        } );
    }
    else
    {
      // do AJAX load
      $( "#messageDiv" ).load( baseUrl + "&sakai_action=doMessageFrame&collectionId=" + collectionId + "&citationId=" + citationButton.id + "&operation=remove",
        function() {
        
          // update the citation list count using the value from the AJAX response
          if( document.getElementById( "citationCountDisplay" ) && document.getElementById( "citationCount" ) ) {
            $( "#citationCountDisplay" ).html( document.getElementById( "citationCount" ).value );
          }
          
          // hide the spinner
          $( "#" + spinnerId ).hide();
          $( "#" + citationButton.id ).show();
          
          // change label to add
          citationButton.value = addLabel;
          
          // change highlighting
          highlightButtonSelections( removeLabel );
          
          // enable inputs
          $( "input" ).attr( "disabled", "" );
        
          if (firstPage != null && firstPage != notSet && firstPage)
          {
             document.getElementById("firstPage1").setAttribute("disabled", firstPage);
             document.getElementById("firstPage2").setAttribute("disabled", firstPage);
          }
          
          if (prevPage != null && prevPage != notSet && prevPage)
          {
             document.getElementById("prevPage1").setAttribute("disabled", prevPage);
             document.getElementById("prevPage2").setAttribute("disabled", prevPage);
          }

          if (nextPage != null && nextPage != notSet && nextPage)
          {
             document.getElementById("nextPage1").setAttribute("disabled", nextPage);
             document.getElementById("nextPage2").setAttribute("disabled", nextPage);
          }
 
        } );
    }    
  }
}

/*
 * Highlights rows selected in a table using a checkbox element
 */
function highlightCheckedSelections() {
  $( "tr[input]" ).removeClass( "highLightAdded" );
  $( "tr[input[@checked]]" ).addClass( "highLightAdded" );
}

/*
 * Show all database descriptions
 */
function showDbDescriptions( altHide ) {
  // show descriptions
  $( ".dbDescription" ).show( function() {
      resizeFrame();
    }
  );

  // show proper toggle icon
  $( ".dbToggleIcon" ).each( function() {
    this.src = "/library/image/sakai/collapse.gif?panel=Main";
    this.alt = altHide;
  } );
}

/*
 * Hide all database descriptions
 */
function hideDbDescriptions( altShow ) {
  // hide descriptions
  $( ".dbDescription" ).hide( function() {
      resizeFrame();
    }
  );
  
  // show proper toggle icon
  $( ".dbToggleIcon" ).each( function() {
    this.src = "/library/image/sakai/expand.gif?panel=Main";
    this.alt = altShow;
  } );
}

function toggleDbDescription( database_id, altShow, altHide ) {
  // toggle description
  $( "#description_" + database_id ).slideToggle( 300,
    function() {
      resizeFrame();
    }
  );
  
  // toggle expand/hide image
  var image = document.getElementById( "toggle_" + database_id );
  
  if( image.alt == altShow ) {
    image.src = "/library/image/sakai/collapse.gif?panel=Main";
    image.alt = altHide;
  } else {
    image.src = "/library/image/sakai/expand.gif?panel=Main";
    image.alt = altShow;
  }
}

/*
 * Submits search form if everything checks out
 */
function submitSearchForm( basicType, advancedType, formname ) {
  if( legalSearch( document.getElementById( "searchType" ).value ) ) {
    checkSearchSpinner( basicType, advancedType );
    submitform( formname );
    return true;
  } else {
    return false;
  }
}

function checkSearchSpinner( basicType, advancedType ) {
  // get the searchType value from the form
  var searchType = $( "#searchType" ).val();
  
  if( searchType == basicType ) {
    // show basic spinner
    showSpinner( '.basicSearchLoad' );

// In case artifacts from advanced button are still showing
    $( ".advSearch" ).hide();

    // show basic cancel button
    $( ".basicSearch" ).hide();
    $( ".basicCancel" ).show();
  } else if( searchType == advancedType ) {
    // show advanced spinner
    showSpinner( '.advancedSearchLoad' );

// In case artifacts from basic button are still showing
    $( ".basicSearch" ).hide();
    
    // show advanced cancel button
    $( ".advSearch" ).hide();
    $( ".advCancel" ).show();
  }
}

function showSpinner( spinnerSelector ) {
  // show the spinner, replacing anything marked to be replaced
  $( spinnerSelector + "_replace" ).hide();
  $( spinnerSelector ).show();
  
  // disable buttons in results table only
  if( $( "input[@type=button]:visible", "#resultsTable" ) ) {
    $( "input[@type=button]:visible", "#resultsTable" ).attr( "disabled", "disabled" );
  }
}

/*
 * Returns an array of checked databases
 */
function checkedDatabases () {
  var selected = new Array;
  $('input[@type="checkbox"][@checked]', '#databaseArea').each(
     function (i) {
        selected.push($(this).attr('id'));
      });
  return selected;
}

/*
 * Returns the number of databases checked
 */
function numCheckedDatabases() {
  return checkedDatabases().length;
  //return $( "#dbSelectedCount" ).html();
}

/*
 * Only allow a certain number of DBs to be checked.  If number of checked DBs
 * is larger than maxDbNum, alert the user.
 */
function countDatabases( checkbox, maxDbNum ) {
  if( numCheckedDatabases() > maxDbNum ) {
    alert( "Sorry - No more than " + maxDbNum + " databases can be searched at one time." );
    checkbox.checked = false;
  }
}

/*
 * Check to see if the user has entered a query into the given search form.
 *
 * Returns true if a query is present, false otherwise
 */
function gotAQuery( searchFormType, basicType, advancedType ) {
  var gotone = false;
  var value;
  
  if( searchFormType == basicType ) {
    value = $( ".basicField" ).val();
    if( value && value.match(/\S/) ) {
      gotone = true;
    }
  } else if( searchFormType == advancedType ) {
    $( ".advField" ).each( function() {
      value = $( this ).val();
      if( value && value.match(/\S/) ) {
        gotone = true;
      }
    } );
  }
  
  return( gotone );
}

function details( record ) {
  $( "#details_" + record.id ).slideToggle( 300,
    function() {
      resizeFrame();
    }
  );
}

function numCitationsSelected() {
  var count;
  count = $( "input[@type=checkbox][@checked]" ).size();
  return count;
}

function updateSelectableActions() {
  if( $( "input[@type=checkbox][@checked]" ).size() > 0 ) {
    $( ".selectAction" ).attr( "disabled", "" );
  } else {
    $( ".selectAction" ).attr( "disabled", "disabled" );
  }
}

function exportSelectedCitations( baseUrl, citationCollectionId, resourceId ) {
  var exportUrl = baseUrl + "?citationCollectionId=" + citationCollectionId + "&resourceId=" + encodeURIComponent(resourceId);
  
  // get each selected checkbox and append it to be exported
  $( "input[@type=checkbox][@checked]" ).each( function() {
      exportUrl += "&citationId=" + this.value;
    }
  );
  
  window.location.assign( exportUrl );
}

function exportAllCitations( baseUrl, citationCollectionId, resourceId ) {
  var exportUrl = baseUrl + "?citationCollectionId=" + citationCollectionId + "&resourceId=" + encodeURIComponent(resourceId);
  window.location.assign( exportUrl );
}

function removeSelectedCitations( baseUrl ) {
  // get each selected checkbox and append it to be removed
  $( "input[@type=checkbox][@checked]" ).each( function() {
      baseUrl += "&citationId=" + this.value;
    }
  );
  
  // do the action
  window.location.assign( baseUrl );
}

function reorderCitations( baseUrl, ids ) {
	baseUrl += "&orderedCitationIds=" + ids;
  
  // do the action
  window.location.assign( baseUrl );
}

function removeAllCitations( formname ) {
  document.getElementById('sakai_action').value='doRemoveAllCitations';
  submitform( formname );
}

/*
 * Sort citations
 */
function sortAllCitations( formname, sortby ) 
{
  document.getElementById('sakai_action').value='doSortCollection';

  document.getElementById('sort').value = sortby;
  submitform( formname );
}

/*
 * Import citations
 */
 
function importCitations(formname, alertString)
{
  if (document.getElementById('risupload').value.length == 0 && 
      document.getElementById('ristext').value.length == 0)
      alert(alertString);
  else
  {
  	$( '#risFileUpload' ).hide();
  	$( '#risTextUpload' ).hide();
  	$( '#import1' ).attr("disabled","disabled");
  	$( '#import2' ).attr("disabled","disabled");
  	$( '#AddCitations1' ).attr("disabled","disabled");
  	$( '#AddCitations2' ).attr("disabled","disabled");
   	
  	$( '#importingMessage' ).show();
  	document.getElementById('sakai_action').value='doImport';
  	submitform( formname);
  }
}

function changePageSize( action, location, formname ) {
  showSpinner( '.pageLoad' );
  document.getElementById( 'sakai_action' ).value = action;
  document.getElementById( 'pageSelector' ).value = location;
  submitform( formname );
}

/**
 * Check required fields on create and edit pages
 *
 */
function checkRequiredFields( alertMsg ) {
  // check the inputs of all single-value requiredFields
  var returnVal = true;
  $( "input:visible", ".requiredField" ).each( function() {
    if( !this.value || this.value == "" ) {
      alert( alertMsg );
      returnVal = false;
    }
  } );
  
  if( returnVal == false ) {
    return false;
  }
  
  // check the inputs of all multi-value requiredFields
  var multiAlert = true;
  var showMultiAlert = false;
  $( "input:visible", ".requiredField_multi" ).each( function() {
    showMultiAlert = true;
    if( this.value && this.value != "" ) {
      multiAlert = false;
    }
  } );
  
  if( multiAlert && showMultiAlert ) {
    alert( alertMsg );
    return false;
  }
  //If electronic citation check if at least one url is supplied
  if($('#type_selector').val() == 'electronic'){
    var showUrlAlert = $("input:text[name^='url_']" ).filter(function(){
                        return this.value.trim().length;
                        }).length > 0;
    if(!showUrlAlert){
      alert( "Must supply at least one url for electronic citation." );
      return false;
    }
  }
  return true;
}

function checkReqStarForElectronicCitation(){
  //For electronic citation display reqstar for Link field.
  if($('#type_selector').val() == 'electronic'){
      var addReqStar = '<span class="reqStar">*</span>';
      $(addReqStar).insertBefore('#url_div label:first');
  }
  //else remove reqStar from the Link field
  else{
      $('#url_div .reqStar').remove();
  }
}
$(document).ready( function() {
	if (typeof $.fn.googleBooksCover != "undefined"){
        $.fn.googleBooksCover();
    }
    //when editing an existing electronic citation check for required link field
    checkReqStarForElectronicCitation();
});
