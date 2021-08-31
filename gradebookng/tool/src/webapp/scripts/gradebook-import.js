/**************************************************************************************
 *                    Gradebook Import Javascript
 *************************************************************************************/
var GBI = GBI || {};

GBI.selectAllNone = function( element )
{
	const selectAll = $(element).prop( "checked" );

	// Toggling the gradebook item checkbox will also toggle it's corresponding comment checkbox if it has one, but not vice versa; see below comment
	$(".import_selection_item").each( function() { GBI.toggleCheckBox( selectAll, this ); });

	// This is needed in the case where comment is selected but associated gradebook item is not
	$(".import_selection_comment").each( function() { GBI.toggleCheckBox( selectAll, this ); });
};

GBI.toggleCheckBox = function( selectAll, element )
{
	const checkbox = $(element);
	if( (selectAll && !checkbox.prop( "checked" )) || (!selectAll && checkbox.prop( "checked" )) )
	{
		// Fire click handler so we don't have to duplicate the handler's logic here
		checkbox.click();
	}
};
