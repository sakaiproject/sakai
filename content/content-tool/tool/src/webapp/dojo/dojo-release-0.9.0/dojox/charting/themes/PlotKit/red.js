if(!dojo._hasResource["dojox.charting.themes.PlotKit.red"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.charting.themes.PlotKit.red"] = true;
dojo.provide("dojox.charting.themes.PlotKit.red");
dojo.require("dojox.charting.Theme");

(function(){
	var dxc=dojox.charting;
	var colors=dxc.Theme.defineColors({ hue:0, saturation:60, low:40, high:88 });
	dxc.themes.PlotKit.red=new dxc.Theme({
		chart:{ backgroundColor:"#f5e6e6", backgroundImage:null, border:0 },
		axis:{
			stroke:{ color:"#fff",width:2 },
			line:{ color:"#fff",width:1 },
			majorTick:{ color:"#fff", width:2, length:12 },
			minorTick:{ color:"#fff", width:1, length:8 },
			font:"normal normal normal 8pt Tahoma",
			fontColor:"#999"
		},
		series:{
			stroke:{ width:2, color:"#666" },
			fill:"#666",
			font:"normal normal normal 7pt Tahoma",	//	label
			fontColor:"#000"
		},
		marker:{	//	any markers on a series.
			stroke:{ width:2 },
			fill:"#333",
			font:"normal normal normal 7pt Tahoma",	//	label
			fontColor:"#000"
		},
		colors:colors
	});
})();

}
