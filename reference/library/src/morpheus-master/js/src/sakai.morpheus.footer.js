/**
 * For Footer toggles in Morpheus
 */

/* Show server time on footer if container exists */
$PBJQ(document).ready(function(){
	
	updateFooterTime = (function(){
		if( $PBJQ('#preferredTime').length == 1 ){
			var preferredTzDisplay= $PBJQ('#preferredTime').data('preferredtzdisplay');
			var preferredServerDateAndGMTOffset = new Date( parseInt( $PBJQ('#preferredTime').data('preferredserverdateandgmtoffset') ) );
			var preferredLocalOffset = preferredServerDateAndGMTOffset.getTime() - (new Date()).getTime(); 	
		}
		var serverTzDisplay= $PBJQ('#serverTime').data('servertzdisplay');
		var serverDateAndGMTOffset = new Date( parseInt( $PBJQ('#serverTime').data('serverdateandgmtoffset') ) );
		var serverLocalOffset = serverDateAndGMTOffset.getTime() - (new Date()).getTime();

		return function() {
			var offsetDate = new Date((new Date()).getTime() + serverLocalOffset);
			var dateString = offsetDate.toUTCString()
					.replace(/GMT/, serverTzDisplay)
					.replace(/UTC/, serverTzDisplay);

			$PBJQ('#serverTime').text(dateString);
	
			if( $PBJQ('#preferredTime').length == 1 ){
				var offsetDate = new Date((new Date()).getTime() + preferredLocalOffset);
				var dateString = offsetDate.toUTCString()
						.replace(/GMT/, preferredTzDisplay)
						.replace(/UTC/, preferredTzDisplay);
	
				$PBJQ('#preferredTime').text(dateString);
			}
			
			setTimeout('updateFooterTime()', 1000);
		};

	})();

	if( $PBJQ('#serverTime').length == 1 ){
		updateFooterTime();
	}

});