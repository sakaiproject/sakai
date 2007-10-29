if(!dojo._hasResource["dojox.collections.tests.collections"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.collections.tests.collections"] = true;
dojo.provide("dojox.collections.tests.collections");
dojo.require("dojox.collections");

try{
	dojo.require("dojox.collections.tests._base");
	dojo.require("dojox.collections.tests.ArrayList");
	dojo.require("dojox.collections.tests.BinaryTree");
	dojo.require("dojox.collections.tests.Dictionary");
	dojo.require("dojox.collections.tests.Queue");
	dojo.require("dojox.collections.tests.Set");
	dojo.require("dojox.collections.tests.SortedList");
	dojo.require("dojox.collections.tests.Stack");
}catch(e){
	doh.debug(e);
}

}
