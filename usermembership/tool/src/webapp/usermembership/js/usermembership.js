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
