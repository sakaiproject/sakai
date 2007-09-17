if(!dojo._hasResource["dojox.validate.ca"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.validate.ca"] = true;
dojo.provide("dojox.validate.ca");

dojo.require("dojox.validate._base");

dojox.validate.ca.isPhoneNumber = function(/* String */value) {
	// summary: Validates 10 Canadian digit phone number for several common formats
	// returns: Boolean
        return dojox.validate.us.isPhoneNumber(value);  // same as US
};

dojox.validate.ca.isProvince = function(/* String[2] */value) {
	// summary: Validates Canadian province abbreviations (2 chars)
	// returns: Boolean
	var re = new RegExp("^" + dojox.regexp.ca.province() + "$", "i");
	return re.test(value);
}; 
 
dojox.validate.ca.isSocialInsuranceNumber = function(/* String */value) {
	// summary: Validates Canadian 9 digit social insurance number for several common formats
	// This routine only pattern matches and does not use the Luhn Algorithm to validate number.
	// returns: Boolean
        var flags = {
                format: [
                        "###-###-###",
                        "### ### ###",
                        "#########" 
                ]
        };
        return dojox.validate.isNumberFormat(value, flags);
};

dojox.validate.ca.isPostalCode = function(value) {
	// summary: Validates Canadian 6 digit postal code:
	//	Canadian postal codes are in the format ANA NAN,
	//	where A is a letter and N is a digit, with a space
	//	separating the third and fourth characters.
	// returns: Boolean
        var re = new RegExp("^" + dojox.regexp.ca.postalCode() + "$", "i");
        return re.test(value);
};

}
