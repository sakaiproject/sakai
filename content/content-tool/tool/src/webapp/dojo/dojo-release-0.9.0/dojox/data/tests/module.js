if(!dojo._hasResource["dojox.tests.module"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.tests.module"] = true;
dojo.provide("dojox.tests.module");

try{
	dojo.require("dojox.data.tests.stores.CsvStore");
	dojo.requireIf(dojo.isBrowser, "dojox.data.tests.stores.HtmlTableStore");
	dojo.requireIf(dojo.isBrowser, "dojox.data.tests.stores.OpmlStore");
	dojo.requireIf(dojo.isBrowser, "dojox.data.tests.stores.XmlStore");
	dojo.requireIf(dojo.isBrowser, "dojox.data.tests.stores.FlickrStore");
	dojo.requireIf(dojo.isBrowser, "dojox.data.tests.dom");
}catch(e){
	doh.debug(e);
}


}
