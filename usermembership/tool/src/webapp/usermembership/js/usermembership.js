var USR_MEMBRSHP = {};

USR_MEMBRSHP.applyStateToCheckboxes = function( state )
{
    $( ".chkStatus" ).prop( "checked", state );
    $( ".chkStatus" ).attr( "value", state );
    USR_MEMBRSHP.checkEnableButtons();
};

USR_MEMBRSHP.checkEnableButtons = function()
{
    // Enable the buttons if any of the checkboxes are checked
    var disabled = $( ".chkStatus:checked" ).length > 0 ? false : true;
    $( "#sitelistform\\:setToInactive1" ).prop( "disabled", disabled );
    $( "#sitelistform\\:setToActive1" ).prop( "disabled", disabled );
    $( "#sitelistform\\:exportCsv1" ).prop( "disabled", disabled );
    $( "#sitelistform\\:exportXls1" ).prop( "disabled", disabled );
    $( "#sitelistform\\:setToInactive2" ).prop( "disabled", disabled );
    $( "#sitelistform\\:setToActive2" ).prop( "disabled", disabled );
    $( "#sitelistform\\:exportCsv2" ).prop( "disabled", disabled );
    $( "#sitelistform\\:exportXls2" ).prop( "disabled", disabled );
};

USR_MEMBRSHP.invertSelection = function()
{
    // For each checkbox...
    $( ".chkStatus" ).each( function()
    {
        // Invert it's selected state and value
        var checked = $( this ).prop( "checked" );
        $( this ).prop( "checked", !checked );
        $( this ).attr( "value", !checked );
    });

    USR_MEMBRSHP.checkEnableButtons();
};

USR_MEMBRSHP.toggleActions = function( clickedElement )
{
    var specifier = clickedElement.parentElement.id === "sitelistform:headerContainer1" ? "1" : "2";
    var span = $( "#sitelistform\\:actionHeader" + specifier );
    if( span.attr( "class" ) === "collapsed" )
    {
        span.attr( "class", "expanded" );
        $( "#sitelistform\\:actionContainer" + specifier ).show();
        $( "#sitelistform\\:actionContainer" + specifier ).css( "display", "inline-flex" );
    }
    else
    {
        span.attr( "class", "collapsed" );
        $( "#sitelistform\\:actionContainer" + specifier ).hide();
        $( "#sitelistform\\:actionContainer" + specifier ).css( "display", "none" );
    }

    setMainFrameHeight( USR_MEMBRSHP.frameID );
};

USR_MEMBRSHP.disableControls = function( escape )
{
    // Clone and disable all drop downs (disable the clone, hide the original)
    var dropDowns = USR_MEMBRSHP.nodeListToArray( document.getElementsByTagName( "select" ) );
    for( i = 0; i < dropDowns.length; i++ )
    {
        // Hide the original drop down
        var select = dropDowns[i];
        select.style.display = "none";

        // Create the cloned element and disable it
        var newSelect = document.createElement( "select" );
        newSelect.setAttribute( "id", select.getAttribute( "id" ) + "Disabled" );
        newSelect.setAttribute( "name", select.getAttribute( "name" ) + "Disabled" );
        newSelect.setAttribute( "disabled", "true" );
        newSelect.className = select.className;
        newSelect.innerHTML = select.innerHTML;

        // Add the clone to the DOM where the original was
        var parent = select.parentNode;
        parent.insertBefore( newSelect, select );
    }

    // Get all the input elements, separate into lists by type
    var allInputs = USR_MEMBRSHP.nodeListToArray( document.getElementsByTagName( "input" ) );
    var elementsToCloneAndDisable = [];
    var elementsToDisable = [];
    for( i = 0; i < allInputs.length; i++ )
    {
        if( (allInputs[i].type === "submit" || allInputs[i].type === "button" || allInputs[i].type === "checkbox") 
                && allInputs[i].id !== escape )
        {
            elementsToCloneAndDisable.push( allInputs[i] );
        }
        else if( allInputs[i].type === "text" && allInputs[i].id !== escape )
        {
            elementsToDisable.push( allInputs[i] );
        }
    }

    // Disable all elements
    USR_MEMBRSHP.toggleElements( elementsToDisable, true );
    for( i = 0; i < elementsToCloneAndDisable.length; i++ )
    {
        USR_MEMBRSHP.cloneAndHideElement( "", elementsToCloneAndDisable[i] );
    }
};

USR_MEMBRSHP.nodeListToArray = function( nodeList )
{
    var array = [];
    for( var i = nodeList.length >>> 0; i--; )
    {
        array[i] = nodeList[i];
    }

    return array;
};

USR_MEMBRSHP.cloneAndHideElement = function( divId, element )
{
    // First, set the element to be invisible
    element.style.display = "none";

    // Now create a new disabled element with the same attributes as the existing element
    var newElement = document.createElement( "input" );

    newElement.setAttribute( 'type', element.type );
    newElement.setAttribute( 'id', element.getAttribute( 'id' ) + 'Disabled' );
    newElement.setAttribute( 'name', element.getAttribute( 'name' ) + 'Disabled' );
    newElement.setAttribute( 'value', element.getAttribute( 'value' ) );
    newElement.className = element.className + " noPointers";
    newElement.setAttribute( 'disabled', 'true' );
    if( element.type === "checkbox" )
    {
        newElement.checked = element.checked;
    }

    if( "" !== divId )
    {
        var div = document.getElementById( divId );
        div.insertBefore( newElement, element );
    }
    else
    {
        var parent = element.parentNode;
        parent.insertBefore( newElement, element );
    }
};

USR_MEMBRSHP.toggleElements = function( elements, disabled )
{
    for( i = 0; i < elements.length; i ++ )
    {
        elements[i].disabled = disabled;
    }
};

USR_MEMBRSHP.showSpinner = function( spinnerID )
{
    if( typeof spinnerID === "string" )
    {
        document.getElementById( spinnerID ).style.visibility = "visible";
    }
    else
    {
        var button = document.getElementById( spinnerID.id + "Disabled" );
        button.className = "spinnerOverlay";
    }
};
