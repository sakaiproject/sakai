if(!dojo._hasResource["dijit.tests.i18n.module"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.tests.i18n.module"] = true;
dojo.provide("dijit.tests.i18n.module");

try{
	if(dojo.isBrowser){
		doh.registerUrl("dijit.tests.i18n.currency", dojo.moduleUrl("dijit", "tests/i18n/currency.html"));
		doh.registerUrl("dijit.tests.i18n.date", dojo.moduleUrl("dijit", "tests/i18n/date.html"));
		doh.registerUrl("dijit.tests.i18n.number", dojo.moduleUrl("dijit", "tests/i18n/number.html"));
		doh.registerUrl("dijit.tests.i18n.textbox", dojo.moduleUrl("dijit", "tests/i18n/textbox.html"));
		doh.registerUrl("dijit.tests.i18n.time", dojo.moduleUrl("dijit", "tests/i18n/time.html"));
	}
}catch(e){
	doh.debug(e);
}

}
