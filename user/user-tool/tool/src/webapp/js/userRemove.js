function disableButtonsActivateSpinner()
{
    // Clone and disable thebuttons (disable the clone, hide the original)
    var btnRemove = document.getElementById( "remove" );
    var btnCancel = document.getElementById( "cancel" );
    var list = [btnRemove, btnCancel];
    for( i = 0; i < list.length; i++ )
    {
        var original = list[i];
        var clone = document.createElement( "input" );
        var parent = original.parentNode;
        clone.setAttribute( "type", "button" );
        clone.setAttribute( "id", original.getAttribute( "id" ) + "Disabled" );
        clone.setAttribute( "name", original.getAttribute( "name" ) + "Disabled" );
        clone.setAttribute( "className", original.getAttribute( "className" ) );
        clone.value = original.value;
        clone.setAttribute( "disabled", "true" );
        original.style.display = "none";
        parent.insertBefore( clone, original );
    }
    
    // Show the spinner
    document.getElementById( "deleteUserSpinner" ).style.visibility = "visible";
}
