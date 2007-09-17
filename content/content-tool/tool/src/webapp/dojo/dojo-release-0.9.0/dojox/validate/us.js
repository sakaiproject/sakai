if(!dojo._hasResource["dojox.validate.us"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.validate.us"] = true;
dojo.provide("dojox.validate.us");
dojo.require("dojox.validate._base");

dojox.validate.us.isState = function(/*String*/value, /*Object?*/flags){
	// summary: Validates US state and territory abbreviations.
	//
	// value: A two character string
	// flags: An object
	//    flags.allowTerritories  Allow Guam, Puerto Rico, etc.  Default is true.
	//    flags.allowMilitary  Allow military 'states', e.g. Armed Forces Europe (AE).  Default is true.

	var re = new RegExp("^" + dojox.regexp.us.state(flags) + "$", "i");
	return re.test(value); // Boolean
}

dojox.validate.us.isPhoneNumber = function(/*String*/value){
	// summary: Validates 10 US digit phone number for several common formats
	// value: The telephone number string

	var flags = {
		format: [
			"###-###-####",
			"(###) ###-####",
			"(###) ### ####",
			"###.###.####",
			"###/###-####",
			"### ### ####",
			"###-###-#### x#???",
			"(###) ###-#### x#???",
			"(###) ### #### x#???",
			"###.###.#### x#???",
			"###/###-#### x#???",
			"### ### #### x#???",
			"##########"
		]
	};
	return dojox.validate.isNumberFormat(value, flags); // Boolean
}

dojox.validate.us.isSocialSecurityNumber = function(/*String*/value){
	// summary: Validates social security number
	var flags = {
		format: [
			"###-##-####",
			"### ## ####",
			"#########"
		]
	};
	return dojox.validate.isNumberFormat(value, flags); // Boolean
}

dojox.validate.us.isZipCode = function(/*String*/value){
	// summary: Validates U.S. zip-code
	var flags = {
		format: [
			"#####-####",
			"##### ####",
			"#########",
			"#####"
		]
	};
	return dojox.validate.isNumberFormat(value, flags); // Boolean
}

}
