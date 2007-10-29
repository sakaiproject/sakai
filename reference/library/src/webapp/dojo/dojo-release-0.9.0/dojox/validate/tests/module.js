if(!dojo._hasResource["dojox.validate.tests.module"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.validate.tests.module"] = true;
dojo.provide("dojox.validate.tests.module");

try{
	dojo.require("dojox.validate.tests.creditcard");
	dojo.require("dojox.validate.tests.validate"); 

}catch(e){
	doh.debug(e);
	console.debug(e); 
}

}
