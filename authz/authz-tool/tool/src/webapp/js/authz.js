$(document).ready(function(){
	// When the checkboxes change update the cell.
	$('input:checkbox').change(function(){
		$(this).parents('td').toggleClass('active', this.checked);
	}).change();
    $("table.checkGrid tr:even").addClass("evenrow");
    // Save the default selected
    $(':checked').parents('td').addClass('defaultSelected');
    
    $('.permissionDescription').hover(function(e){
        $(this).parents('tr').children('td').toggleClass('rowHover', e.type == "mouseenter");
    });
    
    $('th').hover(function(event){
    	var col = ($(this).prevAll().size());
        $('.' + col).add(this).toggleClass('rowHover', event.type == "mouseenter");
    });
    
    $('th#permission').hover(function(event){
        $('.checkGrid td.checkboxCell').toggleClass('rowHover', event.type == "mouseenter");
    });
    
    $('th#permission a').click(function(e){
        $('.checkGrid input').prop('checked', ($('.checkGrid :checked').length == 0)).change();
        e.preventDefault();
    });
    $('.permissionDescription a').click(function(e){
        var anyChecked = $(this).parents('tr').find('input:checked').not('[disabled]').length > 0;
        $(this).parents('tr').find('input:checkbox').not('[disabled]').prop('checked', !anyChecked).change();
        e.preventDefault();
    });
    $('th.role a').click(function(e){
        var col = ($(this).parent('th').prevAll().size());
        var anyChecked = $('.' + col + ' input:checked').not('[disabled]').length > 0;
        $('.' + col + ' input').not('[disabled]').prop('checked', !anyChecked).change();
        e.preventDefault();
    });
    
    $('#clearall').click(function(e){
        $("input").not('[disabled]').prop("checked", false).change();
        e.preventDefault();
    });
    $('#restdef').click(function(e){
        $("input").prop("checked", false);
        $(".defaultSelected input").prop("checked", true).change();
        e.preventDefault();
    });
    
});

var AUTHZ = {};

AUTHZ.disableControls = function( escape )
{
    // Clone and disable all drop downs (disable the clone, hide the original)
    var dropDowns = AUTHZ.nodeListToArray( document.getElementsByTagName( "select" ) );
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
    var allInputs = AUTHZ.nodeListToArray( document.getElementsByTagName( "input" ) );
    var buttons = [];
    var textFields = [];
    for( i = 0; i < allInputs.length; i++ )
    {
        if( (allInputs[i].type === "submit" || allInputs[i].type === "button") && allInputs[i].id !== escape )
        {
            buttons.push( allInputs[i] );
        }
        else if( allInputs[i].type === "text" && allInputs[i].id !== escape )
        {
            textFields.push( allInputs[i] );
        }
    }

    // Disable all buttons
    AUTHZ.toggleElements( textFields, true );
    for( i = 0; i < buttons.length; i++ )
    {
        AUTHZ.disableButton( "", buttons[i] );
    }

    // Get the download/upload links
    var undoAll = document.getElementById( "restdef" );
    var links = [undoAll];
    for( i = 0; i < links.length; i++ )
    {
        if( links[i] !== null )
        {
            AUTHZ.disableLink( links[i] );
        }
    }
};

AUTHZ.nodeListToArray = function( nodeList )
{
    var array = [];
    for( var i = nodeList.length >>> 0; i--; )
    {
        array[i] = nodeList[i];
    }

    return array;
};

AUTHZ.disableButton = function( divId, button )
{
    // first set the button to be invisible
    button.style.display = "none";

    // now create a new disabled button with the same attributes as the existing button
    var newButton = document.createElement( "input" );

    newButton.setAttribute( 'type', 'button' );
    newButton.setAttribute( 'id', button.getAttribute( 'id' ) + 'Disabled' );
    newButton.setAttribute( 'name', button.getAttribute( 'name' ) + 'Disabled' );
    newButton.setAttribute( 'value', button.getAttribute( 'value' ) );
    newButton.className = button.className + " noPointers";
    newButton.setAttribute( 'disabled', 'true' );

    if( "" !== divId )
    {
        var div = document.getElementById( divId );
        div.insertBefore( newButton, button );
    }
    else
    {
        var parent = button.parentNode;
        parent.insertBefore( newButton, button );
    }
};

AUTHZ.toggleElements = function( buttons, disabled )
{
    for( i = 0; i < buttons.length; i ++ )
    {
        buttons[i].disabled = disabled;
    }
};

AUTHZ.disableLink = function( link )
{
    link.className = "noPointers";
    link.disabled = true;
};

AUTHZ.showSpinner = function( spinnerID )
{
    document.getElementById( spinnerID ).style.visibility = "visible";
};