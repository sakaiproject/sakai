if(!dojo._hasResource["dijit.form.CurrencyTextBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.CurrencyTextBox"] = true;
dojo.provide("dijit.form.CurrencyTextBox");

//FIXME: dojo.experimental throws an unreadable exception?
//dojo.experimental("dijit.form.CurrencyTextBox");

dojo.require("dojo.currency");
dojo.declare(
	"dijit.form.CurrencyTextBox",
	dijit.form.NumberTextBox,
	{
		// code: String
		//		the ISO4217 currency code, a three letter sequence like "USD"
		//		See http://en.wikipedia.org/wiki/ISO_4217
		currency: "",

		regExpGen: dojo.currency.regexp,
		format: dojo.currency.format,
		parse: dojo.currency.parse,

		postMixInProperties: function(){
			if(this.constraints === dijit.form.ValidationTextBox.prototype.constraints){
				// declare a constraints property on 'this' so we don't overwrite the shared default object in 'prototype'
				this.constraints = {};
			}
			this.constraints.currency = this.currency;
			dijit.form.CurrencyTextBox.superclass.postMixInProperties.apply(this, arguments);
		}
	}
);

}
