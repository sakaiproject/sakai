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
    AUTHZ.toggleElements( elementsToDisable, true );
    for( i = 0; i < elementsToCloneAndDisable.length; i++ )
    {
        AUTHZ.cloneAndHideElement( "", elementsToCloneAndDisable[i] );
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

AUTHZ.cloneAndHideElement = function( divId, element )
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

AUTHZ.toggleElements = function( elements, disabled )
{
    for( i = 0; i < elements.length; i ++ )
    {
        elements[i].disabled = disabled;
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
