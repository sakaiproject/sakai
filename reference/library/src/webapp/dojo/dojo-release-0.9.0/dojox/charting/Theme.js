if(!dojo._hasResource["dojox.charting.Theme"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.charting.Theme"] = true;
dojo.provide("dojox.charting.Theme");

dojo.require("dojox.gfx");
dojo.require("dojox.charting._color");

(function(){
	var dxc=dojox.charting;
	dxc.Theme=function(/*object?*/kwArgs){
		kwArgs=kwArgs||{};
		this.chart=dojo.mixin(dojo.clone(dxc.Theme._def.chart), kwArgs.chart||{});
		this.axis=dojo.mixin(dojo.clone(dxc.Theme._def.axis), kwArgs.axis||{});
		this.series=dojo.mixin(dojo.clone(dxc.Theme._def.series), kwArgs.series||{});
		this.marker=dojo.mixin(dojo.clone(dxc.Theme._def.marker), kwArgs.marker||{});
		this.markers=dojo.mixin(dojo.clone(dxc.Theme.Markers), kwArgs.markers||{});
		this.colors=[];

		//	push the colors, use _def colors if none passed.
		kwArgs.colors=kwArgs.colors||dxc.Theme._def.colors;
		dojo.forEach(kwArgs.colors, function(item){ 
			this.colors.push(item); 
		}, this);

		//	for color assignment, if needed.
		var current=0;
		this.nextColor=function(){
			return this.colors[current++%this.colors.length];
		};
	};

	//	"static" fields
	//	default markers.
	//	A marker is defined by an SVG path segment; it should be defined as
	//		relative motion, and with the assumption that the path segment
	//		will be moved to the value point (i.e prepend Mx,y)
	dxc.Theme.Markers={
		NONE:"", 
		CIRCLE:"m-3,0 c0,-4 6,-4 6,0 m-6,0 c0,4 6,4 6,0", 
		SQUARE:"m-3,-3 l0,6 6,0 0,-6 z", 
		DIAMOND:"m0,-3 l3,3 -3,3 -3,-3 z", 
		CROSS:"m0,-3 l0,6 m-3,-3 l6,0", 
		X:"m-3,-3 l6,6 m0,-6 l-6,6", 
		TRIANGLE:"m-3,3 l3,-6 3,6 z", 
		TRIANGLE_INVERTED:"m-3,-3 l3,6 3,-6 z"
	};
	dxc.Theme._def={
		chart:{ backgroundColor:"#fff", backgroundImage:null, border:"1px solid #999" },
		axis:{
			stroke:{ color:"#000",width:2 },
			line:{ color:"#999",width:1,style:"Dot",cap:"round" },
			majorTick:{ color:"#999", width:2, length:12 },
			minorTick:{ color:"#999", width:1, length:8 },
			font:"normal normal normal 8pt Tahoma",
			fontColor:"#000"
		},
		series:{
			stroke:{ width:2, color:"#333" },
			fill:"#ccc",
			font:"normal normal normal 7pt Tahoma",	//	label
			fontColor:"#000"
		},
		marker:{	//	any markers on a series.
			stroke:{ width:2 },
			fill:"#333",
			font:"normal normal normal 7pt Tahoma",	//	label
			fontColor:"#000"
		},
		colors:[
			"#000","#111","#222","#333",
			"#444","#555","#666","#777",
			"#888","#999","#aaa","#bbb",
			"#ccc"
		]
	};
	//	everything will be defined as Hex strings.
	dxc.Theme.defineColors=function(kwArgs){
		//	we can generate a set of colors based on keyword arguments
		var n=kwArgs.num||32;	//	the number of colors to generate
		var c=[];
		if(kwArgs.hue){
			//	single hue, generate a set based on brightness
			var s=kwArgs.saturation||100;	//	saturation
			var st=kwArgs.low||30;
			var end=kwArgs.high||90;
			var step=(end-st)/n;			//	brightness steps
			for(var i=0; i<n; i++){
				c.push(dxc._color.fromHsb(kwArgs.hue, s, st+(step*i)).toHex());
			}
			return c;
		}
		if(kwArgs.stops){
			//	TODO: fill out colors based on n stops.
		}
	};
	
	//	prototype methods
	dojo.extend(dxc.Theme, {
		//	intended for private use by the charting engine.
		_apply:function(shape, type, prop, color){
			var o=dojo.clone(this[type][prop]);
			if(color&&o.color){ o.color=color }
			switch(prop){
				case "stroke":
				case "line":
				case "majorTick":
				case "minorTick":{
					shape.setStroke(o);
					break;
				}
				case "fill":{
					shape.setFill(o);
					break;
				}
				case "font":{
					shape.setFont(o).setFill(this[type].fontColor);
					break;
				}
			}
		},
		_applyToChart:function(node){
			var ns=node.style;
			for(var p in this.chart){ ns[p]=chart[p]; }
		}
	});
})();

}
