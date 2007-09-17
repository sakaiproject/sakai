if(!dojo._hasResource["dojox.wire.tests.wireml"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.wire.tests.wireml"] = true;
dojo.provide("dojox.wire.tests.wireml");

try{
	if(dojo.isBrowser){
		doh.registerUrl("dojox.wire.tests.ml.Action", dojo.moduleUrl("dojox", "wire/tests/markup/Action.html"));
		doh.registerUrl("dojox.wire.tests.ml.Transfer", dojo.moduleUrl("dojox", "wire/tests/markup/Transfer.html"));
		doh.registerUrl("dojox.wire.tests.ml.Invocation", dojo.moduleUrl("dojox", "wire/tests/markup/Invocation.html"));
		doh.registerUrl("dojox.wire.tests.ml.Data", dojo.moduleUrl("dojox", "wire/tests/markup/Data.html"));
		doh.registerUrl("dojox.wire.tests.ml.DataStore", dojo.moduleUrl("dojox", "wire/tests/markup/DataStore.html"));
		doh.registerUrl("dojox.wire.tests.ml.Service", dojo.moduleUrl("dojox", "wire/tests/markup/Service.html"));
	}
}catch(e){
	doh.debug(e);
}

}
