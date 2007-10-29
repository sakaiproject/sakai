if(!dojo._hasResource["dojox.tests.module"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.tests.module"] = true;
dojo.provide("dojox.tests.module");

try{
	dojo.require("dojox.wire.tests.wire");
	dojo.require("dojox.wire.tests.wireml");
}catch(e){
	doh.debug(e);
}


}
