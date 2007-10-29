if(!dojo._hasResource["dojox.wire.tests.wire"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.wire.tests.wire"] = true;
dojo.provide("dojox.wire.tests.wire");

try{
	dojo.require("dojox.wire.tests.programmatic._base");
	dojo.require("dojox.wire.tests.programmatic.Wire");
	dojo.requireIf(dojo.isBrowser, "dojox.wire.tests.programmatic.DataWire");
	dojo.requireIf(dojo.isBrowser, "dojox.wire.tests.programmatic.XmlWire");
	dojo.require("dojox.wire.tests.programmatic.CompositeWire");
	dojo.require("dojox.wire.tests.programmatic.TableAdapter");
	dojo.require("dojox.wire.tests.programmatic.TreeAdapter");
	dojo.require("dojox.wire.tests.programmatic.TextAdapter");
}catch(e){
	doh.debug(e);
}

}
