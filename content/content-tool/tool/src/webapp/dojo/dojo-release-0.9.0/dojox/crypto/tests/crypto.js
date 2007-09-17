if(!dojo._hasResource["dojox.crypto.tests.crypto"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.crypto.tests.crypto"] = true;
dojo.provide("dojox.crypto.tests.crypto");
dojo.require("dojox.crypto");

try{
	dojo.require("dojox.crypto.tests.MD5");
	dojo.require("dojox.crypto.tests.Blowfish");
}catch(e){
	doh.debug(e);
}

}
