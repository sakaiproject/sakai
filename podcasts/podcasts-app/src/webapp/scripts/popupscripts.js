//Initialize Popover
// el is element to where popover should be placed and where it gets activeted by click
// message is the content, the popup will display
function initializePopover(elId, message) {
	$('#'+elId).popover({
		placement:'bottom',
		content: message
	});
}

//Copy to Clipboard
function copyToClipboard(elId) {
  /* Get the text field */
  var copyText = document.getElementById(elId);

  copyText.select();
  copyText.setSelectionRange(0, 99999); /*For mobile devices*/

  document.execCommand("copy");
}
