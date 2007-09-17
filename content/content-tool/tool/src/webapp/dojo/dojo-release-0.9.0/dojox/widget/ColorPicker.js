if(!dojo._hasResource["dojox.widget.ColorPicker"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.widget.ColorPicker"] = true;
dojo.provide("dojox.widget.ColorPicker");
dojo.experimental("dojox.widget.ColorPicker"); // level: prototype

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dojo.dnd.move"); 
dojo.require("dojo.fx"); 

dojo.declare("dojox.widget.ColorPicker",
	[dijit._Widget, dijit._Templated],
	{
	// summary: a HSV color picker - like PhotoShop
	//
	// description: 
	//	provides an interactive HSV ColorPicker similar to
	//	PhotoShop's color selction tool. Will eventually
	//	mixin FormWidget and be used as a suplement or a
	//	'more interactive' replacement for ColorPalette
	//	
	// example:
	//
	//	code: 
	//	var picker = new dojox.widget.ColorPicker({
	//		// a couple of example toggles:
	//		animatePoint:false,
	//		showHsv: false,
	//		webSafe: false,
	//		showRgb: false 	
	//	});
	//	
	//	markup:
	//	<div dojoType="dojox.widget.ColorPicker"></div>
	//

	// showRgb: Boolean
	//	show/update RGB input nodes
	showRgb: true,
	
	// showHsv: Boolean
	//	show/update HSV input nodes
	showHsv: true,
	
	// showHex: Boolean
	//	show/update Hex value field
	showHex: true,

	// webSafe: Boolean
	//	deprecated? or just use a toggle to show/hide that node, too?
	webSafe: true,

	// animatePoint: Boolean
	//	toggle to use slideTo (true) or just place the cursor (false) on click
	animatePoint: true,

	// slideDuration: Integer
	//	time in ms picker node will slide to next location (non-dragging) when animatePoint=true
	slideDuration: 250, 

	_underlay: dojo.moduleUrl("dojox.widget","ColorPicker/images/underlay.png"),
	templateString:"<div class=\"dojoxColorPicker\">\n\t<div class=\"dojoxColorPickerBox\">\n\t\t<div dojoAttachPoint=\"cursorNode\" class=\"dojoxColorPickerPoint\"></div>\n\t\t<img dojoAttachPoint=\"colorUnderlay\" dojoAttachEvent=\"onclick: _setPoint\" class=\"dojoxColorPickerUnderlay\" src=\"${_underlay}\">\n\t</div>\n\t<div class=\"dojoxHuePicker\">\n\t\t<div dojoAttachPoint=\"hueCursorNode\" class=\"dojoxHuePickerPoint\"></div>\n\t\t<div dojoAttachPoint=\"hueNode\" class=\"dojoxHuePickerUnderlay\" dojoAttachEvent=\"onclick: _setHuePoint\"></div>\n\t</div>\n\t<div dojoAttachPoint=\"previewNode\" class=\"dojoxColorPickerPreview\"></div>\n\t<div dojoAttachPoint=\"safePreviewNode\" class=\"dojoxColorPickerWebSafePreview\"></div>\n\t<div class=\"dojoxColorPickerOptional\">\n\t\t<div class=\"dijitInline dojoxColorPickerRgb\" dojoAttachPoint=\"rgbNode\">\n\t\t\t<table>\n\t\t\t<tr><td>r</td><td><input dojoAttachPoint=\"Rval\" size=\"1\"></td></tr>\n\t\t\t<tr><td>g</td><td><input dojoAttachPoint=\"Gval\" size=\"1\"></td></tr>\n\t\t\t<tr><td>b</td><td><input dojoAttachPoint=\"Bval\" size=\"1\"></td></tr>\n\t\t\t</table>\n\t\t</div>\n\t\t<div class=\"dijitInline dojoxColorPickerHsv\" dojoAttachPoint=\"hsvNode\">\n\t\t\t<table>\n\t\t\t<tr><td>h</td><td><input dojoAttachPoint=\"Hval\"size=\"1\"> &deg;</td></tr>\n\t\t\t<tr><td>s</td><td><input dojoAttachPoint=\"Sval\" size=\"1\"> %</td></tr>\n\t\t\t<tr><td>v</td><td><input dojoAttachPoint=\"Vval\" size=\"1\"> %</td></tr>\n\t\t\t</table>\n\t\t</div>\n\t\t<div class=\"dojoxColorPickerHex\" dojoAttachPoint=\"hexNode\">\t\n\t\t\thex: <input dojoAttachPoint=\"hexCode\" size=\"6\" class=\"dojoxColorPickerHexCode\">\n\t\t</div>\n\t</div>\n</div>\n",

	postCreate: function(){
		// summary: As quickly as we can, set up ie6 alpha-filter support for our
		// 	underlay.  we don't do image handles (done in css), just the 'core' 
		//	of this widget: the underlay. 
		if(dojo.isIE){ 
			this.colorUnderlay.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+this._underlay+"', sizingMethod='scale')";
			this.colorUnderlay.src = dojo.moduleUrl("dojox.widget","FisheyeList/blank.gif").toString();
		}
		// hide toggle-able nodes:
		if (!this.showRgb){ this.rgbNode.style.display = "none"; }
		if (!this.showHsv){ this.hsvNode.style.display = "none"; }
		if (!this.showHex){ this.hexNode.style.display = "none"; } 
		if (!this.webSafe){ this.safePreviewNode.style.display = "none"; } 
	},

	startup: function(){
		// summary: defer all additional calls until we're started, and our
		// embeded sliders are loaded? (not implemented yet)

		// this._offset = ((dojo.marginBox(this.cursorNode).w)/2); 
		this._offset = 0; 

		this._mover = new dojo.dnd.Moveable(this.cursorNode, {
			mover: dojo.dnd.boxConstrainedMover({ t:0, l:0, w:150, h:150 })
		}); 
		this._hueMover = new dojo.dnd.Moveable(this.hueCursorNode, {
			mover: dojo.dnd.boxConstrainedMover({ t:0, l:0, w:0, h:150 })
		});

		// no dnd/move/move published ... use a timer:
		dojo.subscribe("/dnd/move/stop",dojo.hitch(this,"_clearTimer"));
		dojo.subscribe("/dnd/move/start",dojo.hitch(this,"_setTimer"));

		// ugly scaling calculator.  need a XYslider badly
		this._sc = (1/dojo.coords(this.colorUnderlay).w);  
		this._hueSc = (255/(dojo.coords(this.hueNode).h+this._offset)); 
		
		// initial color
		this._updateColor(); 
		
	},

	_setTimer: function(/* DomNode */node){
		this._timer = setInterval(dojo.hitch(this,"_updateColor"),45);	
	},
	_clearTimer: function(/* DomNode */node){
		clearInterval(this._timer);
	},

	_setHue: function(/* Decimal */h){
		// summary: sets a natural color background for the 
		// 	underlay image against closest hue value (full saturation) 
		// h: 0..255 

		// this is not a pretty conversion:
		var hue = dojo.colorFromArray(this._hsv2rgb(h,1,1,{ inputRange: 1 })).toHex();
		dojo.style(this.colorUnderlay,"backgroundColor",hue);
	},

	_updateColor: function(){
		// summary: update the previewNode color, and input values [optional]
		var h = Math.round((255+(this._offset))-((dojo.style(this.hueCursorNode,"top")+this._offset)*this._hueSc));
		var s = Math.round((dojo.style(this.cursorNode,"left")*this._sc)*100); 
		var v = Math.round(100-(dojo.style(this.cursorNode,"top")*this._sc)*100);

		// limit hue calculations to only when it changes
		if (h != this._hue){ this._setHue(h); }

		var rgb = this._hsv2rgb(h,s/100,v/100,{ inputRange: 1 }); 
		var hex = (dojo.colorFromArray(rgb).toHex());

		this.previewNode.style.backgroundColor = hex;	
		if(this.webSafe){ this.safePreviewNode.style.backgroundColor = hex; }
		if(this.showHex){ this.hexCode.value = hex; }
		if(this.showRgb){
			this.Rval.value = rgb[0];
			this.Gval.value = rgb[1];	
			this.Bval.value = rgb[2];
		}
		if(this.showHsv){
			this.Hval.value = Math.round((h*360)/255); // convert to 0..360
			this.Sval.value = s;
			this.Vval.value = v;
		}
	},

	_setHuePoint: function(/* Event */evt){ 
		// summary: set the hue picker handle on relative y coordinates
		if (this.animatePoint){
			dojo.fx.slideTo({ 
				node: this.hueCursorNode, 
				duration:this.slideDuration,
				top: evt.layerY,
				left: 0,
				onEnd: dojo.hitch(this,"_updateColor")
			}).play();
		}else{
			dojo.style(this.hueCursorNode,"top",(evt.layerY)+"px");
			this._updateColor(); 
		}
	},

	_setPoint: function(/* Event */evt){
		// summary: set our picker point based on relative x/y coordinates
		if (this.animatePoint){
			dojo.fx.slideTo({ 
				node: this.cursorNode, 
				duration:this.slideDuration,
				top: evt.layerY-this._offset, 
				left: evt.layerX-this._offset,
				onEnd: dojo.hitch(this,"_updateColor")
			}).play();
		}else{
			dojo.style(this.cursorNode,"left",(evt.layerX-this._offset)+"px");
			dojo.style(this.cursorNode,"top",(evt.layerY-this._offset)+"px");
			this._updateColor(); 
		}
	},

	// this ported directly from 0.4 dojo.gfx.colors.hsv, with bugs :)
	_hsv2rgb: function(/* int || Array */h, /* int */s, /* int */v, /* Object? */options){
		//	summary
		//	converts an HSV value set to RGB, ranges depending on optional options object.
		//	patch for options by Matthew Eernisse 	
		if (dojo.isArray(h)) {
			if(s){
				options = s;
			}
			v = h[2] || 0;
			s = h[1] || 0;
			h = h[0] || 0;
		}
	
		var opt = {
			inputRange:  (options && options.inputRange)  ? options.inputRange : [255, 255, 255],
			outputRange: (options && options.outputRange) ? options.outputRange : 255
		};
	
	    switch(opt.inputRange[0]) { 
			// 0.0-1.0 
			case 1: h = h * 360; break; 
			// 0-100 
			case 100: h = (h / 100) * 360; break; 
			// 0-360 
			case 360: h = h; break; 
			// 0-255 
			default: h = (h / 255) * 360; 
		} 
		if (h == 360){ h = 0;}
	
		//	no need to alter if inputRange[1] = 1
		switch(opt.inputRange[1]){
			case 100: s /= 100; break;
			case 255: s /= 255;
		}
	
		//	no need to alter if inputRange[1] = 1
		switch(opt.inputRange[2]){
			case 100: v /= 100; break;
			case 255: v /= 255;
		}
	
		var r = null;
		var g = null;
		var b = null;
	
		if (s == 0){
			// color is on black-and-white center line
			// achromatic: shades of gray
			r = v;
			g = v;
			b = v;
		}else{
			// chromatic color
			var hTemp = h / 60;		// h is now IN [0,6]
			var i = Math.floor(hTemp);	// largest integer <= h
			var f = hTemp - i;		// fractional part of h
	
			var p = v * (1 - s);
			var q = v * (1 - (s * f));
			var t = v * (1 - (s * (1 - f)));
	
			switch(i){
				case 0: r = v; g = t; b = p; break;
				case 1: r = q; g = v; b = p; break;
				case 2: r = p; g = v; b = t; break;
				case 3: r = p; g = q; b = v; break;
				case 4: r = t; g = p; b = v; break;
				case 5: r = v; g = p; b = q; break;
			}
		}
	
		switch(opt.outputRange){
			case 1:
				r = dojo.math.round(r, 2);
				g = dojo.math.round(g, 2);
				b = dojo.math.round(b, 2);
				break;
			case 100:
				r = Math.round(r * 100);
				g = Math.round(g * 100);
				b = Math.round(b * 100);
				break;
			default:
				r = Math.round(r * 255);
				g = Math.round(g * 255);
				b = Math.round(b * 255);
		}
		return [r, g, b];
	}
});

}
