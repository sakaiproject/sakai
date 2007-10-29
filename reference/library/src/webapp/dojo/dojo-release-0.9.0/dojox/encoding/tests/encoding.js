if(!dojo._hasResource["dojox.encoding.tests.encoding"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.encoding.tests.encoding"] = true;
dojo.provide("dojox.encoding.tests.encoding");

try{
	dojo.require("dojox.encoding.tests.ascii85");
	dojo.require("dojox.encoding.tests.easy64");
	dojo.require("dojox.encoding.tests.bits");
	dojo.require("dojox.encoding.tests.splay");
	dojo.require("dojox.encoding.tests.lzw");
}catch(e){
	doh.debug(e);
}

}
