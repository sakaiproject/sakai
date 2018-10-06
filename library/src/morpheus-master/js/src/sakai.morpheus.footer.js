/**
 * For the Sakai Footer
 */

/* if the Esc key is hit, the panel will close */
function footerDetailsPanelEscHandler(e){
  if (e.keyCode === 27) { // esc keycode
    toggleFooterDetailsPanel(e);
  }
}

/* displays or hides the server details panel in the footer */
function toggleFooterDetailsPanel(e) {
	e.preventDefault();			// override # in href from popping to the top of the page
	
	$PBJQ("#Mrphs-footer--details__panel").toggleClass("Mrphs-footer--details__panel-show");
	
	if ($PBJQ('#Mrphs-footer--details__panel').hasClass('Mrphs-footer--details__panel-show')) {
		// ESC key can close the panel:
		$PBJQ(document).on('keyup',footerDetailsPanelEscHandler);
		// then focus on the close button
		$PBJQ('#Mrphs-footer--details__close').focus();
	} else {
		$PBJQ(document).off('keyup',footerDetailsPanelEscHandler);
		// refocus on the launching button
		$PBJQ('#Mrphs-footer--details__info').focus();
	}
}

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

	$PBJQ("#Mrphs-footer--details__info").click(function (e) {
		toggleFooterDetailsPanel(e);
	});
	
	$PBJQ("#Mrphs-footer--details__close").click(function (e) {
		toggleFooterDetailsPanel(e);
	});
});