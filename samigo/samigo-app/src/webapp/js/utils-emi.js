/**
 * This does front-end validation for the emi options entered.
 * It checks that the use only fill in valid options.
 * The key entered must be a alphabet letter or 'good' whitespace.
 * Good Codes: 0
 * 
 * @param element The element where the option are entered.
 * @param validEMIOptions The valid options as a string.
 * @param event The key press event.
 * @returns {Boolean} true if it is valid, otherwise false.
 */
function checkEMIOptions(element, validEMIOptions, event) {
	var charCode = event.charCode;
	if(charCode === undefined) {
		//for older IE versions
		charCode = event.keyCode;
	}
    //whitespace
    if (isCharGoodWhitespace(charCode)){
        return true;
    }
    //A-Z or a-z
	if (isCharAlphaUpper(charCode) || isCharAlphaLower(charCode)){
		return isValidOption(element, validEMIOptions, charCode);
	}
	return false;
}
/*
 * good whitespace (tab, shift, backspace...)
 */
function isCharGoodWhitespace(charCode){
    return (charCode === 0 ||
            charCode === 8 ||
            charCode === 9 ||
            charCode === 14 ||
            charCode === 15 ||
            charCode === 127);
}

function isCharNumber(charCode){
	return (charCode >= 48 && charCode <= 57);
}

function isCharAlphaUpper(charCode){
	return (charCode >= 65 && charCode <= 90);
}

function isCharAlphaLower(charCode){
	return (charCode >= 97 && charCode <= 122);
}

function isCharAlpha(charCode){
	return (isCharAlphaUpper(charCode) || isCharAlphaLower(charCode));
}

/**
 * Check if the key selected is valid.
 * @param element
 * @param validEMIOptions
 * @param charCode
 * @returns
 */
function isValidOption(element, validEMIOptions, charCode){
	// don't use if it is not in the options
	var keychar = String.fromCharCode(charCode).toUpperCase();
	if (validEMIOptions.indexOf(keychar) === -1) {
		return false;
	}
	// now check that it is not a duplicate
	if (typeof element.value === undefined) {
		element.value = element.val();
	}
	var index = element.value.toUpperCase().indexOf(keychar);
	if (index === -1) {
		return true;
	} else {
		// check that the duplicate is not selected, then we can replace
		return (element.selectionStart <= index && element.selectionEnd > index);
	}
}