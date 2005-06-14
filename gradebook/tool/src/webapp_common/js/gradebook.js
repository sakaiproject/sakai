function clearIfDefaultString(formField, defaultString) {
	if(formField.value == defaultString) {
		formField.value = "";
	}
}

// We sometimes want to have a default submit button that's not
// the first one in the form.
//
// USAGE:
//
//   <h:inputText id="Score" value="#{scoreRow.score}"
//     onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
//   <h:commandButton id="saveButton" ... />
//
// It's important to specify "onkeypress" rather than "onkeydown". Otherwise,
// IE will work fine, but Mozilla and Co. will send the key release event
// on to the first button on the form no matter what.
//
function submitOnEnter(event, defaultButtonId) {
	var characterCode;
	if (event.which) {
		characterCode = event.which;
	} else if (event.keyCode) {
		characterCode = event.keyCode;
	}

	if (characterCode == 13) {
		event.returnValue = false;
		event.cancel = true;
		document.getElementById(defaultButtonId).click();
		return false;
	} else {
		return true;
	}
}
