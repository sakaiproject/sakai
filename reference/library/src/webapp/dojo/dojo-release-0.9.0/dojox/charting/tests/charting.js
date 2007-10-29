if(!dojo._hasResource["dojox.charting.tests.charting"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.charting.tests.charting"] = true;
dojo.provide("dojox.charting.tests.charting");

try{
	dojo.require("dojox.charting.tests._color");
	dojo.require("dojox.charting.tests.Theme");
	dojo.require("dojox.charting.themes.PlotKit.blue");
	console.log(dojox.charting.themes.PlotKit.blue);
}catch(e){
	doh.debug(e);
}

}
