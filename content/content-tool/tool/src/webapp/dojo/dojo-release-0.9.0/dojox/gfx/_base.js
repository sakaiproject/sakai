if(!dojo._hasResource["dojox.gfx._base"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.gfx._base"] = true;
dojo.provide("dojox.gfx._base");

// candidates for dojox.style (work on VML and SVG nodes)
dojox.gfx._hasClass = function(/*HTMLElement*/node, /*String*/classStr){
	//	summary:
	//		Returns whether or not the specified classes are a portion of the
	//		class list currently applied to the node. 
	// return (new RegExp('(^|\\s+)'+classStr+'(\\s+|$)')).test(node.className)	// Boolean
	return ((" "+node.getAttribute("className")+" ").indexOf(" "+classStr+" ") >= 0);  // Boolean
}

dojox.gfx._addClass = function(/*HTMLElement*/node, /*String*/classStr){
	//	summary:
	//		Adds the specified classes to the end of the class list on the
	//		passed node.
	var cls = node.getAttribute("className");
	if((" "+cls+" ").indexOf(" "+classStr+" ") < 0){
		node.setAttribute("className", cls + (cls ? ' ' : '') + classStr);
	}
}

dojox.gfx._removeClass = function(/*HTMLElement*/node, /*String*/classStr){
	//	summary: Removes classes from node.
	node.setAttribute("className", node.getAttribute("className").replace(new RegExp('(^|\\s+)'+classStr+'(\\s+|$)'), "$1$2"));
}


// candidate for dojox.html.metrics (dynamic font resize handler is not implemented here)

//	derived from Morris John's emResized measurer
dojox.gfx._base._getFontMeasurements = function(){
	//	summary
	//	Returns an object that has pixel equivilents of standard font size values.
	var heights = {
		'1em':0, '1ex':0, '100%':0, '12pt':0, '16px':0, 'xx-small':0, 'x-small':0,
		'small':0, 'medium':0, 'large':0, 'x-large':0, 'xx-large':0
	};

	if(dojo.isIE){
		//	we do a font-size fix if and only if one isn't applied already.
		//	NOTE: If someone set the fontSize on the HTML Element, this will kill it.
		dojo.doc.documentElement.style.fontSize="100%";
	}

	//	set up the measuring node.
	var div=dojo.doc.createElement("div");
	div.style.position="absolute";
	div.style.left="-100px";
	div.style.top="0";
	div.style.width="30px";
	div.style.height="1000em";
	div.style.border="0";
	div.style.margin="0";
	div.style.padding="0";
	div.style.outline="0";
	div.style.lineHeight="1";
	div.style.overflow="hidden";
	dojo.body().appendChild(div);

	//	do the measurements.
	for(var p in heights){
		div.style.fontSize = p;
		heights[p] = Math.round(div.offsetHeight * 12/16) * 16/12 / 1000;
	}
	
	dojo.body().removeChild(div);
	div = null;
	return heights; 	//	object
};

dojox.gfx._base._fontMeasurements = null;

dojox.gfx._base._getCachedFontMeasurements = function(recalculate){
	if(recalculate || !dojox.gfx._base._fontMeasurements){
		dojox.gfx._base._fontMeasurements = dojox.gfx._base._getFontMeasurements();
	}
	return dojox.gfx._base._fontMeasurements;
};

// candidate for dojo.dom

dojox.gfx._base._uniqueId = 0;
dojox.gfx._base._getUniqueId = function(){
	// summary: returns a unique string for use with any DOM element
	var id;
	do{
		id = "dojoUnique" + (++dojox.gfx._base._uniqueId);
	}while(dojo.byId(id));
	return id;
};

dojo.mixin(dojox.gfx, {
	// summary: defines constants, prototypes, and utility functions
	
	// default shapes, which are used to fill in missing parameters
	defaultPath:     {type: "path",     path: ""},
	defaultPolyline: {type: "polyline", points: []},
	defaultRect:     {type: "rect",     x: 0, y: 0, width: 100, height: 100, r: 0},
	defaultEllipse:  {type: "ellipse",  cx: 0, cy: 0, rx: 200, ry: 100},
	defaultCircle:   {type: "circle",   cx: 0, cy: 0, r: 100},
	defaultLine:     {type: "line",     x1: 0, y1: 0, x2: 100, y2: 100},
	defaultImage:    {type: "image",    x: 0, y: 0, width: 0, height: 0, src: ""},
	defaultText:     {type: "text",     x: 0, y: 0, text: "",
		align: "start", decoration: "none", rotated: false, kerning: true },
	defaultTextPath: {type: "textpath", text: "",
		align: "start", decoration: "none", rotated: false, kerning: true },

	// default geometric attributes
	defaultStroke: {type: "stroke", color: "black", style: "solid", width: 1, cap: "butt", join: 4},
	defaultLinearGradient: {type: "linear", x1: 0, y1: 0, x2: 100, y2: 100, 
		colors: [{offset: 0, color: "black"}, {offset: 1, color: "white"}]},
	defaultRadialGradient: {type: "radial", cx: 0, cy: 0, r: 100, 
		colors: [{offset: 0, color: "black"}, {offset: 1, color: "white"}]},
	defaultPattern: {type: "pattern", x: 0, y: 0, width: 0, height: 0, src: ""},
	defaultFont: {type: "font", style: "normal", variant: "normal", weight: "normal", 
		size: "10pt", family: "serif"},

	normalizeColor: function(/*Color*/ color){
		// summary: converts any legal color representation to normalized dojo.Color object
		return (color instanceof dojo.Color) ? color : new dojo.Color(color); // dojo.Color
	},
	normalizeParameters: function(existed, update){
		// summary: updates an existing object with properties from an "update" object
		// existed: Object: the "target" object to be updated
		// update:  Object: the "update" object, whose properties will be used to update the existed object
		if(update){
			var empty = {};
			for(var x in existed){
				if(x in update && !(x in empty)){
					existed[x] = update[x];
				}
			}
		}
		return existed;	// Object
	},
	makeParameters: function(defaults, update){
		// summary: copies the original object, and all copied properties from the "update" object
		// defaults: Object: the object to be cloned before updating
		// update:   Object: the object, which properties are to be cloned during updating
		if(!update) return dojo.clone(defaults);
		var result = {};
		for(var i in defaults){
			if(!(i in result)){
				result[i] = dojo.clone((i in update) ? update[i] : defaults[i]);
			}
		}
		return result; // Object
	},
	formatNumber: function(x, addSpace){
		// summary: converts a number to a string using a fixed notation
		// x:			Number:		number to be converted
		// addSpace:	Boolean?:	if it is true, add a space before a positive number
		var val = x.toString();
		if(val.indexOf("e") >= 0){
			val = x.toFixed(4);
		}else{
			var point = val.indexOf(".");
			if(point >= 0 && val.length - point > 5){
				val = x.toFixed(4);
			}
		}
		if(x < 0){
			return val; // String
		}
		return addSpace ? " " + val : val; // String
	},
	// font operations
	makeFontString: function(font){
		// summary: converts a font object to a CSS font string
		// font:	Object:	font object (see dojox.gfx.defaultFont)
		return font.style + " " + font.variant + " " + font.weight + " " + font.size + " " + font.family; // Object
	},
	splitFontString: function(str){
		// summary: converts a CSS font string to a font object
		// str:		String:	a CSS font string
		var font = dojo.clone(dojox.gfx.defaultFont);
		var t = str.split(/\s+/);
		do{
			if(t.length < 5){ break; }
			font.style  = t[0];
			font.varian = t[1];
			font.weight = t[2];
			var i = t[3].indexOf("/");
			font.size = i < 0 ? t[3] : t[3].substring(0, i);
			var j = 4;
			if(i < 0){
				if(t[4] == "/"){
					j = 6;
					break;
				}
				if(t[4].substr(0, 1) == "/"){
					j = 5;
					break;
				}
			}
			if(j + 3 > t.length){ break; }
			font.size = t[j];
			font.family = t[j + 1];
		}while(false);
		return font;	// Object
	},
	// length operations
	cm_in_pt: 72 / 2.54,	// Number: centimeters per inch
	mm_in_pt: 7.2 / 2.54,	// Number: millimeters per inch
	px_in_pt: function(){
		// summary: returns a number of pixels per point
		return dojox.gfx._base._getCachedFontMeasurements()["12pt"] / 12;	// Number
	},
	pt2px: function(len){
		// summary: converts points to pixels
		// len: Number: a value in points
		return len * dojox.gfx.px_in_pt();	// Number
	},
	px2pt: function(len){
		// summary: converts pixels to points
		// len: Number: a value in pixels
		return len / dojox.gfx.px_in_pt();	// Number
	},
	normalizedLength: function(len) {
		// summary: converts any length value to pixels
		// len: String: a length, e.g., "12pc"
		if(len.length == 0) return 0;
		if(len.length > 2){
			var px_in_pt = dojox.gfx.px_in_pt();
			var val = parseFloat(len);
			switch(len.slice(-2)){
				case "px": return val;
				case "pt": return val * px_in_pt;
				case "in": return val * 72 * px_in_pt;
				case "pc": return val * 12 * px_in_pt;
				case "mm": return val / dojox.gfx.mm_in_pt * px_in_pt;
				case "cm": return val / dojox.gfx.cm_in_pt * px_in_pt;
			}
		}
		return parseFloat(len);	// Number
	},
	
	// a constant used to split a SVG/VML path into primitive components
	pathVmlRegExp: /([A-Za-z]+)|(\d+(\.\d+)?)|(\.\d+)|(-\d+(\.\d+)?)|(-\.\d+)/g,
	pathSvgRegExp: /([A-Za-z])|(\d+(\.\d+)?)|(\.\d+)|(-\d+(\.\d+)?)|(-\.\d+)/g
});

dojox.gfx._createShape = function(shape){
	// summary: creates a shape object based on its type; it is meant to be used
	//	by group-like objects
	// shape: Object: a shape object
	switch(shape.type){
		case dojox.gfx.defaultPath.type:		return this.createPath(shape);
		case dojox.gfx.defaultRect.type:		return this.createRect(shape);
		case dojox.gfx.defaultCircle.type:		return this.createCircle(shape);
		case dojox.gfx.defaultEllipse.type:		return this.createEllipse(shape);
		case dojox.gfx.defaultLine.type:		return this.createLine(shape);
		case dojox.gfx.defaultPolyline.type:	return this.createPolyline(shape);
		case dojox.gfx.defaultImage.type:		return this.createImage(shape);
		case dojox.gfx.defaultText.type:		return this.createText(shape);
		case dojox.gfx.defaultTextPath.type:	return this.createTextPath(shape);
	}
	return null;
};

dojox.gfx._eventsProcessing = {
	connect: function(name, object, method){
		return arguments.length > 2 ? 
			dojo.connect(this.getEventSource(), name, object, method) :
			dojo.connect(this.getEventSource(), name, object);
	},
	disconnect: function(token){
		dojo.disconnect(token);
	}
};

dojo.declare("dojox.gfx.Surface", null, {
	// summary: a surface object to be used for drawings

	constructor: function(){
		// underlying node
		this.rawNode = null;
	},
	getEventSource: function(){
		// summary: returns a node, which can be used to attach event listeners
		
		return this.rawNode; // Node
	}
});
dojo.extend(dojox.gfx.Surface, dojox.gfx._eventsProcessing);

dojo.declare("dojox.gfx.Point", null, {
	// summary: a hypothetical 2D point to be used for drawings - {x, y}
	// description: This object is defined for documentation purposes.
	//	You should use the naked object instead: {x: 1, y: 2}.
});

dojo.declare("dojox.gfx.Rectangle", null, {
	// summary: a hypothetical rectangle - {x, y, width, height}
	// description: This object is defined for documentation purposes.
	//	You should use the naked object instead: {x: 1, y: 2, width: 100, height: 200}.
});

}
