if(!dojo._hasResource["dojox.gfx.vml"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.gfx.vml"] = true;
dojo.provide("dojox.gfx.vml");

dojo.require("dojox.gfx._base");
dojo.require("dojox.gfx.shape");
dojo.require("dojox.gfx.path");

dojo.experimental("dojox.gfx.vml");

// dojox.gfx.vml.xmlns: String: a VML's namespace
dojox.gfx.vml.xmlns = "urn:schemas-microsoft-com:vml";

// dojox.gfx.vml.text_alignment: Object: mapping from SVG alignment to VML alignment
dojox.gfx.vml.text_alignment = {start: "left", middle: "center", end: "right"};

// dojox.gfx.vml.pi4: Number: Pi / 4
dojox.gfx.vml.pi4 = Math.PI / 4;

// dojox.gfx.vml.two_pi: Number: 2 * Pi
dojox.gfx.vml.two_pi = Math.PI * 2;

dojox.gfx.vml._parseFloat = function(str) {
	// summary: a helper function to parse VML-specific floating-point values
	// str: String: a representation of a floating-point number
	return str.match(/^\d+f$/i) ? parseInt(str) / 65536 : parseFloat(str);	// Number
};

dojox.gfx.vml._bool = {"t": 1, "true": 1};

dojo.extend(dojox.gfx.Shape, {
	// summary: VML-specific implementation of dojox.gfx.Shape methods

	setFill: function(fill){
		// summary: sets a fill object (VML)
		// fill: Object: a fill object
		//	(see dojox.gfx.defaultLinearGradient, 
		//	dojox.gfx.defaultRadialGradient, 
		//	dojox.gfx.defaultPattern, 
		//	or dojo.Color)

		if(!fill){
			// don't fill
			this.fillStyle = null;
			this.rawNode.filled = false;
			return this;
		}
		if(typeof(fill) == "object" && "type" in fill){
			// gradient
			switch(fill.type){
				case "linear":
					var f = dojox.gfx.makeParameters(dojox.gfx.defaultLinearGradient, fill),
						s = [], a = f.colors;
					this.fillStyle = f;
					dojo.forEach(a, function(v, i, a){
						a[i].color = dojox.gfx.normalizeColor(v.color);
					});
					if(a[0].offset > 0){
						s.push("0 " + a[0].color.toHex());
					}
					for(var i = 0; i < a.length; ++i){
						s.push(a[i].offset.toFixed(8) + " " + a[i].color.toHex());
					}
					var i = a.length - 1;
					if(a[i].offset < 1){
						s.push("1 " + a[i].color.toHex());
					}
					var fo = this.rawNode.fill;
					fo.colors.value = s.join(";");
					fo.method = "sigma";
					fo.type = "gradient";
					fo.angle = (dojox.gfx.matrix._radToDeg(Math.atan2(f.x2 - f.x1, f.y2 - f.y1)) + 180) % 360;
					fo.on = true;
					break;
				case "radial":
					var f = dojox.gfx.makeParameters(dojox.gfx.defaultRadialGradient, fill);
					this.fillStyle = f;
					var l = parseFloat(this.rawNode.style.left),
						t = parseFloat(this.rawNode.style.top),
						w = parseFloat(this.rawNode.style.width),
						h = parseFloat(this.rawNode.style.height),
						c = isNaN(w) ? 1 : 2 * f.r / w,
						a = new Array(f.colors.length);
					// massage colors
					dojo.forEach(f.colors, function(v, i){
						a[i] = {offset: 1 - v.offset * c, color: dojox.gfx.normalizeColor(v.color)};
					});
					var i = a.length - 1;
					while(i >= 0 && a[i].offset < 0){ --i; }
					if(i < a.length - 1){
						// correct excessive colors
						var q = a[i], p = a[i + 1];
						p.color = dojo.blendColors(q.color, p.color, q.offset / (q.offset - p.offset));
						p.offset = 0;
						while(a.length - i > 2) a.pop();
					}
					// set colors
					var i = a.length - 1;
					var s = [];
					if(a[i].offset > 0){
						s.push("0 " + a[i].color.toHex());
					}
					for(; i >= 0; --i){
						s.push(a[i].offset.toFixed(8) + " " + a[i].color.toHex());
					}
					if(a[0].offset < 1){
						s.push("1 " + a[0].color.toHex());
					}
					var fo = this.rawNode.fill;
					fo.colors.value = s.join(";");
					fo.method = "sigma";
					fo.type = "gradientradial";
					if(isNaN(w) || isNaN(h) || isNaN(l) || isNaN(t)){
						fo.focusposition = "0.5 0.5";
					}else{
						fo.focusposition = ((f.cx - l) / w).toFixed(8) + " " + ((f.cy - t) / h).toFixed(8);
					}
					fo.focussize = "0 0";
					fo.on = true;
					break;
				case "pattern":
					var f = dojox.gfx.makeParameters(dojox.gfx.defaultPattern, fill);
					this.fillStyle = f;
					var fo = this.rawNode.fill;
					fo.type = "tile";
					fo.src = f.src;
					if(f.width && f.height){
						// in points
						fo.size.x = dojox.gfx.px2pt(f.width);
						fo.size.y = dojox.gfx.px2pt(f.height);
					}
					fo.alignShape = false;
					fo.position.x = 0;
					fo.position.y = 0;
					fo.origin.x = f.width  ? f.x / f.width  : 0;
					fo.origin.y = f.height ? f.y / f.height : 0;
					fo.on = true;
					break;
			}
			this.rawNode.fill.opacity = 1;
			return this;
		}
		// color object
		this.fillStyle = dojox.gfx.normalizeColor(fill);
		this.rawNode.fillcolor = this.fillStyle.toHex();
		this.rawNode.fill.opacity = this.fillStyle.a;
		this.rawNode.filled = true;
		return this;	// self
	},

	setStroke: function(stroke){
		// summary: sets a stroke object (VML)
		// stroke: Object: a stroke object
		//	(see dojox.gfx.defaultStroke) 
	
		if(!stroke){
			// don't stroke
			this.strokeStyle = null;
			this.rawNode.stroked = false;
			return this;
		}
		// normalize the stroke
		if(typeof stroke == "string"){
			stroke = {color: stroke};
		}
		var s = this.strokeStyle = dojox.gfx.makeParameters(dojox.gfx.defaultStroke, stroke);
		s.color = dojox.gfx.normalizeColor(s.color);
		// generate attributes
		var rn = this.rawNode;
		rn.stroked = true;
		rn.strokecolor = s.color.toCss();
		rn.strokeweight = s.width + "px";	// TODO: should we assume that the width is always in pixels?
		if(rn.stroke) {
			rn.stroke.opacity = s.color.a;
			rn.stroke.endcap = this._translate(this._capMap, s.cap);
			if(typeof(s.join) == "number") {
				rn.stroke.joinstyle = "miter";
				rn.stroke.miterlimit = s.join;
			}else{
				rn.stroke.joinstyle = s.join;
				// rn.stroke.miterlimit = s.width;
			}
			rn.stroke.dashstyle = s.style == "none" ? "Solid" : s.style;
		}
		return this;	// self
	},
	
	_capMap: { butt: 'flat' },
	_capMapReversed: { flat: 'butt' },
	
	_translate: function(dict, value) {
		return (value in dict) ? dict[value] : value;
	},
	
	_applyTransform: function() {
		var matrix = this._getRealMatrix();
		if(!matrix) return this;
		var skew = this.rawNode.skew;
		if(typeof(skew) == "undefined"){
			for(var i = 0; i < this.rawNode.childNodes.length; ++i){
				if(this.rawNode.childNodes[i].tagName == "skew"){
					skew = this.rawNode.childNodes[i];
					break;
				}
			}
		}
		if(skew){
			skew.on = false;
			var mt = matrix.xx.toFixed(8) + " " + matrix.xy.toFixed(8) + " " + 
				matrix.yx.toFixed(8) + " " + matrix.yy.toFixed(8) + " 0 0";
			var offset = Math.floor(matrix.dx).toFixed() + "px " + Math.floor(matrix.dy).toFixed() + "px";
			var l = parseFloat(this.rawNode.style.left);
			var t = parseFloat(this.rawNode.style.top);
			var w = parseFloat(this.rawNode.style.width);
			var h = parseFloat(this.rawNode.style.height);
			if(isNaN(l)) l = 0;
			if(isNaN(t)) t = 0;
			if(isNaN(w)) w = 1;
			if(isNaN(h)) h = 1;
			var origin = (-l / w - 0.5).toFixed(8) + " " + (-t / h - 0.5).toFixed(8);
			skew.matrix =  mt;
			skew.origin = origin;
			skew.offset = offset;
			skew.on = true;
		}
		return this;
	},

	setRawNode: function(rawNode){
		// summary:
		//	assigns and clears the underlying node that will represent this
		//	shape. Once set, transforms, gradients, etc, can be applied.
		//	(no fill & stroke by default)
		rawNode.stroked = false;
		rawNode.filled  = false;
		this.rawNode = rawNode;
	},

	// Attach family
	
	attachFill: function(rawNode){
		// summary: deduces a fill style from a Node.
		// rawNode: Node: an VML node
		var fillStyle = null;
		var fo = rawNode.fill;
		if(rawNode) {
			if(fo.on && fo.type == "gradient"){
				var fillStyle = dojo.clone(dojox.gfx.defaultLinearGradient);
				var rad = dojox.gfx.matrix._degToRad(fo.angle);
				fillStyle.x2 = Math.cos(rad);
				fillStyle.y2 = Math.sin(rad);
				fillStyle.colors = [];
				var stops = fo.colors.value.split(";");
				for(var i = 0; i < stops.length; ++i){
					var t = stops[i].match(/\S+/g);
					if(!t || t.length != 2) continue;
					fillStyle.colors.push({offset: dojox.gfx.vml._parseFloat(t[0]), color: new dojo.Color(t[1])});
				}
			}else if(fo.on && fo.type == "gradientradial"){
				var fillStyle = dojo.clone(dojox.gfx.defaultRadialGradient);
				var w = parseFloat(rawNode.style.width);
				var h = parseFloat(rawNode.style.height);
				fillStyle.cx = isNaN(w) ? 0 : fo.focusposition.x * w;
				fillStyle.cy = isNaN(h) ? 0 : fo.focusposition.y * h;
				fillStyle.r  = isNaN(w) ? 1 : w / 2;
				fillStyle.colors = [];
				var stops = fo.colors.value.split(";");
				for(var i = stops.length - 1; i >= 0; --i){
					var t = stops[i].match(/\S+/g);
					if(!t || t.length != 2) continue;
					fillStyle.colors.push({offset: dojox.gfx.vml._parseFloat(t[0]), color: new dojo.Color(t[1])});
				}
			}else if(fo.on && fo.type == "tile"){
				var fillStyle = dojo.clone(dojox.gfx.defaultPattern);
				fillStyle.width  = dojox.gfx.pt2px(fo.size.x); // from pt
				fillStyle.height = dojox.gfx.pt2px(fo.size.y); // from pt
				fillStyle.x = fo.origin.x * fillStyle.width;
				fillStyle.y = fo.origin.y * fillStyle.height;
				fillStyle.src = fo.src;
			}else if(fo.on && rawNode.fillcolor){
				// a color object !
				fillStyle = new dojo.Color(rawNode.fillcolor+"");
				fillStyle.a = fo.opacity;
			}
		}
		return fillStyle;	// Object
	},

	attachStroke: function(rawNode) {
		// summary: deduces a stroke style from a Node.
		// rawNode: Node: an VML node
		var strokeStyle = dojo.clone(dojox.gfx.defaultStroke);
		if(rawNode && rawNode.stroked){
			strokeStyle.color = new dojo.Color(rawNode.strokecolor.value);
			//console.debug("We are expecting an .75pt here, instead of strokeweight = " + rawNode.strokeweight );
			strokeStyle.width = dojox.gfx.normalizedLength(rawNode.strokeweight+"");
			strokeStyle.color.a = rawNode.stroke.opacity;
			strokeStyle.cap = this._translate(this._capMapReversed, rawNode.stroke.endcap);
			strokeStyle.join = rawNode.stroke.joinstyle == "miter" ? rawNode.stroke.miterlimit : rawNode.stroke.joinstyle;
			strokeStyle.style = rawNode.stroke.dashstyle;
		}else{
			return null;
		}
		return strokeStyle;	// Object
	},

	attachTransform: function(rawNode) {
		// summary: deduces a transformation matrix from a Node.
		// rawNode: Node: an VML node
		var matrix = {};
		if(rawNode){
			var s = rawNode.skew;
			matrix.xx = s.matrix.xtox;
			matrix.xy = s.matrix.ytox;
			matrix.yx = s.matrix.xtoy;
			matrix.yy = s.matrix.ytoy;
			matrix.dx = dojox.gfx.pt2px(s.offset.x);
			matrix.dy = dojox.gfx.pt2px(s.offset.y);
		}
		return dojox.gfx.matrix.normalize(matrix);	// dojox.gfx.matrix.Matrix
	},

	attach: function(rawNode){
		// summary: reconstructs all shape parameters from a Node.
		// rawNode: Node: an VML node
		if(rawNode){
			this.rawNode = rawNode;
			this.shape = this.attachShape(rawNode);
			if("attachFont" in this){
				this.fontStyle = this.attachFont(rawNode);
			}
			if("attachText" in this){
				this.text = this.attachText(rawNode);
			}
			this.fillStyle = this.attachFill(rawNode);
			this.strokeStyle = this.attachStroke(rawNode);
			this.matrix = this.attachTransform(rawNode);
		}
	}
});

dojox.gfx.vml._clear = function(){
	// summary: removes all shapes from a group/surface
	var r = this.rawNode;
	while(r.firstChild != r.lastChild){
		if(r.firstChild != this.bgNode){
			r.removeChild(r.firstChild);
		}
		if(r.lastChild != this.bgNode){
			r.removeChild(r.lastChild);
		}
	}
	return this;	// self
};

dojo.declare("dojox.gfx.Group", dojox.gfx.shape.VirtualGroup, {
	// summary: a group shape (VML), which can be used 
	//	to logically group shapes (e.g, to propagate matricies)
	add: function(shape){
		// summary: adds a shape to a group/surface
		// shape: dojox.gfx.Shape: an VML shape object
		if(this != shape.getParent()){
			this.rawNode.appendChild(shape.rawNode);
			dojox.gfx.Group.superclass.add.apply(this, arguments);
		}
		return this;	// self
	},
	remove: function(shape, silently){
		// summary: remove a shape from a group/surface
		// shape: dojox.gfx.Shape: an VML shape object
		// silently: Boolean?: if true, regenerate a picture
		if(this == shape.getParent()){
			if(this.rawNode == shape.rawNode.parentNode){
				this.rawNode.removeChild(shape.rawNode);
			}
			dojox.gfx.Group.superclass.remove.apply(this, arguments);
		}
		return this;	// self
	},
	clear: dojox.gfx.vml._clear,
	attach: function(rawNode){
		// summary: reconstructs all group shape parameters from a Node (VML).
		// rawNode: Node: a node
		if(rawNode){
			this.rawNode = rawNode;
			this.shape = null;
			this.fillStyle = null;
			this.strokeStyle = null;
			this.matrix = null;
			// attach the background
			this.bgNode = rawNode.firstChild;	// TODO: check it first
		}
	}
});
dojox.gfx.Group.nodeType = "group";

var zIndex = {
	moveToFront: function(){
		// summary: moves a shape to front of its parent's list of shapes (VML)
		this.rawNode.parentNode.appendChild(this.rawNode);
		return this;
	},
	moveToBack: function(){
		// summary: moves a shape to back of its parent's list of shapes (VML)
		var r = this.rawNode;
		var p = r.parentNode;
		var n = p.firstChild;
		p.insertBefore(r, n);
		if(n.tagName == "rect"){
			// surface has a background rectangle, which position should be preserved
			n.swapNode(r);
		}
		return this;
	}
};
dojo.extend(dojox.gfx.Shape, zIndex);
dojo.extend(dojox.gfx.Group, zIndex);
delete zIndex;

dojo.declare("dojox.gfx.Rect", dojox.gfx.shape.Rect, {
	// summary: a rectangle shape (VML)

	attachShape: function(rawNode){
		// summary: builds a rectangle shape from a Node.
		// rawNode: Node: a VML node

		// a workaround for the VML's arcsize bug: cannot read arcsize of an instantiated node
		var arcsize = rawNode.outerHTML.match(/arcsize = \"(\d*\.?\d+[%f]?)\"/)[1];
		arcsize = (arcsize.indexOf("%") >= 0) ? parseFloat(arcsize) / 100 : dojox.gfx.vml._parseFloat(arcsize);
		var style = rawNode.style;
		var width  = parseFloat(style.width);
		var height = parseFloat(style.height);
		// make an object
		var o = dojox.gfx.makeParameters(dojox.gfx.defaultRect, {
			x: parseInt(style.left),
			y: parseInt(style.top),
			width:  width,
			height: height,
			r: Math.min(width, height) * arcsize
		});
		return o;	// dojox.gfx.shape.Rect
	},
	setShape: function(newShape){
		// summary: sets a rectangle shape object (VML)
		// newShape: Object: a rectangle shape object
		var shape = this.shape = dojox.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		var style = this.rawNode.style;
		style.left   = shape.x.toFixed();
		style.top    = shape.y.toFixed();
		style.width  = (typeof(shape.width) == "string" && shape.width.indexOf("%") >= 0)  ? shape.width  : shape.width.toFixed();
		style.height = (typeof(shape.width) == "string" && shape.height.indexOf("%") >= 0) ? shape.height : shape.height.toFixed();
		var r = Math.min(1, (shape.r / Math.min(parseFloat(shape.width), parseFloat(shape.height)))).toFixed(8);
		// a workaround for the VML's arcsize bug: cannot read arcsize of an instantiated node
		var parent = this.rawNode.parentNode;
		var before = null;
		if(parent){
			if(parent.lastChild != this.rawNode){
				for(var i = 0; i < parent.childNodes.length; ++i){
					if(parent.childNodes[i] == this.rawNode){
						before = parent.childNodes[i+1];
						break;
					}
				}
			}
			parent.removeChild(this.rawNode);
		}
		this.rawNode.arcsize = r;
		if(parent){
			if(before){
				parent.insertBefore(this.rawNode, before);
			}else{
				parent.appendChild(this.rawNode);
			}
		}
		// set all necessary styles, which are lost by VML (yes, it's a VML's bug)
		return this.setTransform(this.matrix).setFill(this.fillStyle).setStroke(this.strokeStyle);	// self
	}
});
dojox.gfx.Rect.nodeType = "roundrect"; // use a roundrect so the stroke join type is respected

dojo.declare("dojox.gfx.Ellipse", dojox.gfx.shape.Ellipse, {
	// summary: an ellipse shape (VML)

	attachShape: function(rawNode){
		// summary: builds an ellipse shape from a Node.
		// rawNode: Node: an VML node
		var style = this.rawNode.style;
		var rx = parseInt(style.width ) / 2;
		var ry = parseInt(style.height) / 2;
		var o = dojox.gfx.makeParameters(dojox.gfx.defaultEllipse, {
			cx: parseInt(style.left) + rx,
			cy: parseInt(style.top ) + ry,
			rx: rx,
			ry: ry
		});
		return o;	// dojox.gfx.shape.Ellipse
	},
	setShape: function(newShape){
		// summary: sets an ellipse shape object (VML)
		// newShape: Object: an ellipse shape object
		var shape = this.shape = dojox.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		var style = this.rawNode.style;
		style.left   = (shape.cx - shape.rx).toFixed();
		style.top    = (shape.cy - shape.ry).toFixed();
		style.width  = (shape.rx * 2).toFixed();
		style.height = (shape.ry * 2).toFixed();
		return this.setTransform(this.matrix);	// self
	}
});
dojox.gfx.Ellipse.nodeType = "oval";

dojo.declare("dojox.gfx.Circle", dojox.gfx.shape.Circle, {
	// summary: a circle shape (VML)

	attachShape: function(rawNode){
		// summary: builds a circle shape from a Node.
		// rawNode: Node: an VML node
		var style = this.rawNode.style;
		var r = parseInt(style.width) / 2;
		var o = dojox.gfx.makeParameters(dojox.gfx.defaultCircle, {
			cx: parseInt(style.left) + r,
			cy: parseInt(style.top)  + r,
			r:  r
		});
		return o;	// dojox.gfx.shape.Circle
	},
	setShape: function(newShape){
		// summary: sets a circle shape object (VML)
		// newShape: Object: a circle shape object
		var shape = this.shape = dojox.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		var style = this.rawNode.style;
		style.left   = (shape.cx - shape.r).toFixed();
		style.top    = (shape.cy - shape.r).toFixed();
		style.width  = (shape.r * 2).toFixed();
		style.height = (shape.r * 2).toFixed();
		return this;	// self
	}
});
dojox.gfx.Circle.nodeType = "oval";

dojo.declare("dojox.gfx.Line", dojox.gfx.shape.Line, {
	// summary: a line shape (VML)
	
	constructor: function(rawNode){
		if(rawNode) rawNode.setAttribute("dojoGfxType", "line");
	},
	attachShape: function(rawNode){
		// summary: builds a line shape from a Node.
		// rawNode: Node: an VML node
		var p = rawNode.path.v.match(dojox.gfx.pathVmlRegExp);
		var shape = {};
		do{
			if(p.length < 7 || p[0] != "m" || p[3] != "l" || p[6] != "e") break;
			shape.x1 = parseInt(p[1]);
			shape.y1 = parseInt(p[2]);
			shape.x2 = parseInt(p[4]);
			shape.y2 = parseInt(p[5]);
		}while(false);
		return dojox.gfx.makeParameters(dojox.gfx.defaultLine, shape);	// dojox.gfx.shape.Line
	},
	setShape: function(newShape){
		// summary: sets a line shape object (VML)
		// newShape: Object: a line shape object
		var shape = this.shape = dojox.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		this.rawNode.path.v = "m" + shape.x1.toFixed() + " " + shape.y1.toFixed() +
			"l" + shape.x2.toFixed() + " " + shape.y2.toFixed() + "e";
		return this.setTransform(this.matrix);	// self
	}
});
dojox.gfx.Line.nodeType = "shape";

dojo.declare("dojox.gfx.Polyline", dojox.gfx.shape.Polyline, {
	// summary: a polyline/polygon shape (VML)
	
	constructor: function(rawNode){
		if(rawNode) rawNode.setAttribute("dojoGfxType", "polyline");
	},
	attachShape: function(rawNode){
		// summary: builds a polyline/polygon shape from a Node.
		// rawNode: Node: an VML node
		var shape = dojo.clone(dojox.gfx.defaultPolyline);
		var p = rawNode.path.v.match(dojox.gfx.pathVmlRegExp);
		do{
			if(p.length < 3 || p[0] != "m") break;
			var x = parseInt(p[0]);
			var y = parseInt(p[1]);
			if(isNaN(x) || isNaN(y)) break;
			shape.points.push({x: x, y: y});
			if(p.length < 6 || p[3] != "l") break;
			for(var i = 4; i < p.length; i += 2){
				x = parseInt(p[i]);
				y = parseInt(p[i + 1]);
				if(isNaN(x) || isNaN(y)) break;
				shape.points.push({x: x, y: y});
			}
		}while(false);
		return shape;	// dojox.gfx.shape.Polyline
	},
	setShape: function(points, closed){
		// summary: sets a polyline/polygon shape object (VML)
		// points: Object: a polyline/polygon shape object
		// closed: Boolean?: if true, close the polyline explicitely
		if(points && points instanceof Array){
			// branch
			// points: Array: an array of points
			this.shape = dojox.gfx.makeParameters(this.shape, { points: points });
			if(closed && this.shape.points.length) this.shape.points.push(this.shape.points[0]);
		}else{
			this.shape = dojox.gfx.makeParameters(this.shape, points);
		}
		this.bbox = null;
		var attr = [];
		var p = this.shape.points;
		if(p.length > 0){
			attr.push("m");
			var k = 1;
			if(typeof p[0] == "number"){
				attr.push(p[0].toFixed());
				attr.push(p[1].toFixed());
				k = 2;
			}else{
				attr.push(p[0].x.toFixed());
				attr.push(p[0].y.toFixed());
			}
			if(p.length > k){
				attr.push("l");
				for(var i = k; i < p.length; ++i){
					if(typeof p[i] == "number"){
						attr.push(p[i].toFixed());
					}else{
						attr.push(p[i].x.toFixed());
						attr.push(p[i].y.toFixed());
					}
				}
			}
		}
		attr.push("e");
		this.rawNode.path.v = attr.join(" ");
		return this.setTransform(this.matrix);	// self
	}
});
dojox.gfx.Polyline.nodeType = "shape";

dojo.declare("dojox.gfx.Image", dojox.gfx.shape.Image, {
	// summary: an image (VML)
	
	constructor: function(rawNode){
		if(rawNode) rawNode.setAttribute("dojoGfxType", "image");
	},
	getEventSource: function() {
		// summary: returns a Node, which is used as 
		//	a source of events for this shape
		return this.rawNode ? this.rawNode.firstChild : null;	// Node
	},
	attachShape: function(rawNode){
		// summary: builds an image shape from a Node.
		// rawNode: Node: an VML node
		var shape = dojo.clone(dojox.gfx.defaultImage);
		shape.src = rawNode.firstChild.src;
		return shape;	// dojox.gfx.shape.Image
	},
	setShape: function(newShape){
		// summary: sets an image shape object (VML)
		// newShape: Object: an image shape object
		var shape = this.shape = dojox.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		var firstChild = this.rawNode.firstChild;
        firstChild.src = shape.src;
        if(shape.width || shape.height){
			firstChild.style.width  = shape.width;
			firstChild.style.height = shape.height;
        }
		return this.setTransform(this.matrix);	// self
	},
	setStroke: function(){
		// summary: ignore setting a stroke style
		return this;	// self
	},
	setFill: function(){
		// summary: ignore setting a fill style
		return this;	// self
	},
	attachStroke: function(rawNode){
		// summary: ignore attaching a stroke style
		return null;
	},
	attachFill: function(rawNode){
		// summary: ignore attaching a fill style
		return null;
	},
	attachTransform: function(rawNode) {
		// summary: deduces a transformation matrix from a Node.
		// rawNode: Node: an VML node
		var matrix = {};
		if(rawNode){
			var m = rawNode.filters["DXImageTransform.Microsoft.Matrix"];
			matrix.xx = m.M11;
			matrix.xy = m.M12;
			matrix.yx = m.M21;
			matrix.yy = m.M22;
			matrix.dx = m.Dx;
			matrix.dy = m.Dy;
		}
		return dojox.gfx.matrix.normalize(matrix);	// dojox.gfx.matrix.Matrix
	},
	_applyTransform: function() {
		var matrix = this._getRealMatrix();
		if(!matrix) return this;
		matrix = dojox.gfx.matrix.multiply(matrix, {dx: this.shape.x, dy: this.shape.y});
		var f = this.rawNode.filters["DXImageTransform.Microsoft.Matrix"];
		f.M11 = matrix.xx;
		f.M12 = matrix.xy;
		f.M21 = matrix.yx;
		f.M22 = matrix.yy;
		f.Dx  = matrix.dx;
		f.Dy  = matrix.dy;
		return this;
	}
});
dojox.gfx.Image.nodeType = "div";

dojo.declare("dojox.gfx.Text", dojox.gfx.shape.Text, {
	// summary: an anchored text (VML)
	
	constructor: function(rawNode){
		if(rawNode){rawNode.setAttribute("dojoGfxType", "text");}
		this.fontStyle = null;
	},
	attachShape: function(rawNode){
		// summary: builds a text shape from a Node.
		// rawNode: Node: an VML node
		var shape = null;
		if(rawNode){
			shape = dojo.clone(dojox.gfx.defaultText);
			var p = rawNode.path.v.match(dojox.gfx.pathVmlRegExp);
			if(!p || p.length != 7){ return null; }
			var c = rawNode.childNodes;
			for(var i = 0; i < c.length; ++i){
				if(c[i].tagName == "textpath"){
					var s = c[i].style;
					shape.text = c[i].string;
					switch(s["v-text-align"]){
						case "left":
							shape.x = parseInt(p[1]);
							shape.align = "start";
							break;
						case "center":
							shape.x = (parseInt(p[1]) + parseInt(p[4])) / 2;
							shape.align = "middle";
							break;
						case "right":
							shape.x = parseInt(p[4]);
							shape.align = "end";
							break;
					}
					shape.y = parseInt(p[2]);
					shape.decoration = s["text-decoration"];
					shape.rotated = s["v-rotate-letters"].toLowerCase() in dojox.gfx.vml._bool;
					shape.kerning = s["v-text-kern"].toLowerCase() in dojox.gfx.vml._bool;
					break;
				}
			}
		}
		return shape;	// dojox.gfx.shape.Text
	},
	_alignment: {start: "left", middle: "center", end: "right"},
	setShape: function(newShape){
		// summary: sets a text shape object (VML)
		// newShape: Object: a text shape object
		this.shape = dojox.gfx.makeParameters(this.shape, newShape);
		this.bbox = null;
		var r = this.rawNode, s = this.shape, x = s.x, y = s.y.toFixed();
		switch(s.align){
			case "middle":
				x -= 5;
				break;
			case "end":
				x -= 10;
				break;
		}
		this.rawNode.path.v = "m" + x.toFixed() + "," + y + 
			"l" + (x + 10).toFixed() + "," + y + "e";
		// find path and text path
		var p = null, t = null, c = r.childNodes;
		for(var i = 0; i < c.length; ++i){
			var tag = c[i].tagName;
			if(tag == "path"){
				p = c[i];
				if(t) break;
			}else if(tag == "textpath"){
				t = c[i];
				if(p) break;
			}
		}
		if(!p){
			p = this.rawNode.ownerDocument.createElement("v:path");
			r.appendChild(p);
		}
		if(!t){
			t = this.rawNode.ownerDocument.createElement("v:textpath");
			r.appendChild(t);
		}
		p.textPathOk = true;
		t.on = true;
		var a = dojox.gfx.vml.text_alignment[s.align];
		t.style["v-text-align"] = a ? a : "left";
		t.style["text-decoration"] = s.decoration;
		t.style["v-rotate-letters"] = s.rotated;
		t.style["v-text-kern"] = s.kerning;
		t.string = s.text;
		return this.setTransform(this.matrix);	// self
	},
	_setFont: function(){
		// summary: sets a font object (VML)
		var f = this.fontStyle, c = this.rawNode.childNodes;
		for(var i = 0; i < c.length; ++i){
			if(c[i].tagName == "textpath"){
				c[i].style.font = dojox.gfx.makeFontString(f);
				break;
			}
		}
		this.setTransform(this.matrix);
	},
	attachFont: function(rawNode){
		// summary: deduces a font style from a Node.
		// rawNode: Node: an VML node
		if(!rawNode){ return null; }
		var fontStyle = dojo.clone(dojox.gfx.defaultFont);
		var c = this.rawNode.childNodes;
		for(var i = 0; i < c.length; ++i){
			if(c[i].tagName == "textpath"){
				var s = c[i].style;
				fontStyle.style = s.fontstyle;
				fontStyle.variant = s.fontvariant;
				fontStyle.weight = s.fontweight;
				fontStyle.size = s.fontsize;
				fontStyle.family = s.fontfamily;
				break;
			}
		}
		return fontStyle;	// Object
	},
	attachTransform: function(rawNode) {
		// summary: deduces a transformation matrix from a Node.
		// rawNode: Node: an VML node
		var matrix = dojox.gfx.Shape.prototype.attachTransform.call(this);
		// see comments in _getRealMatrix()
		if(matrix){
			matrix = dojox.gfx.matrix.multiply(matrix, {dy: dojox.gfx.normalizedLength(this.fontStyle.size) * 0.35});
		}
		return matrix;	// dojox.gfx.Matrix2D
	},
	_getRealMatrix: function(){
		// summary: returns the cumulative ("real") transformation matrix
		//	by combining the shape's matrix with its parent's matrix;
		//	it makes a correction for a font size
		var matrix = dojox.gfx.Shape.prototype._getRealMatrix.call(this);
		// It appears that text is always aligned vertically at a middle of x-height (???).
		// It is impossible to obtain these metrics from VML => I try to approximate it with 
		// more-or-less util value of 0.7 * FontSize, which is typical for European fonts.
		if(matrix){
			matrix = dojox.gfx.matrix.multiply(matrix, 
				{dy: -dojox.gfx.normalizedLength(this.fontStyle ? this.fontStyle.size : "10pt") * 0.35});
		}
		return matrix;	// dojox.gfx.Matrix2D
	},
	getTextWidth: function(){ 
		// summary: get the text width, in px 
		var rawNode = this.rawNode; 
		var _display = rawNode.style.display; 
		rawNode.style.display = "inline"; 
		var _width = dojox.gfx.pt2px(parseFloat(rawNode.currentStyle.width)); 
		rawNode.style.display = _display; 
		return _width; 
	} 
});
dojox.gfx.Text.nodeType = "shape";

dojox.gfx.path._calcArc = function(alpha){
	var cosa  = Math.cos(alpha);
	var sina  = Math.sin(alpha);
	// return a start point, 1st and 2nd control points, and an end point
	var p2 = {x: cosa + (4 / 3) * (1 - cosa), y: sina - (4 / 3) * cosa * (1 - cosa) / sina};
	return {
		e:  {x: cosa, y: sina},
		c2: p2,
		c1: {x: p2.x, y: -p2.y},
		s:  {x: cosa, y: -sina}
	};
};

dojo.declare("dojox.gfx.Path", dojox.gfx.path.Path, {
	// summary: a path shape (VML)

	constructor: function(rawNode){
		if(rawNode && !rawNode.getAttribute("dojoGfxType")){
			rawNode.setAttribute("dojoGfxType", "path");
		}
		this.vmlPath = "";
		this.lastControl = {};
	},
	_updateWithSegment: function(segment){
		// summary: updates the bounding box of path with new segment
		// segment: Object: a segment
		var last = dojo.clone(this.last);
		dojox.gfx.Path.superclass._updateWithSegment.apply(this, arguments);
		// add a VML path segment
		var path = this[this.renderers[segment.action]](segment, last);
		if(typeof(this.vmlPath) == "string"){
			this.vmlPath += path.join("");
		}else{
			this.vmlPath = this.vmlPath.concat(path);
		}
		if(typeof(this.vmlPath) == "string"){
			this.rawNode.path.v = this.vmlPath + " r0,0 e";
		}
	},
	attachShape: function(rawNode){
		// summary: builds a path shape from a Node.
		// rawNode: Node: an VML node
		var shape = dojo.clone(dojox.gfx.defaultPath);
		var p = rawNode.path.v.match(dojox.gfx.pathVmlRegExp);
		var t = [], skip = false;
		for(var i = 0; i < p.length; ++p){
			var s = p[i];
			if(s in this._pathVmlToSvgMap) {
				skip = false;
				t.push(this._pathVmlToSvgMap[s]);
			} else if(!skip){
				var n = parseInt(s);
				if(isNaN(n)){
					skip = true;
				}else{
					t.push(n);
				}
			}
		}
		var l = t.length;
		if(l >= 4 && t[l - 1] == "" && t[l - 2] == 0 && t[l - 3] == 0 && t[l - 4] == "l"){
			t.pop(); t.pop(); t.pop(); t.pop();
		}
		if(l) shape.path = t.join(" ");
		return shape;	// dojox.gfx.path.Path
	},
	setShape: function(newShape){
		// summary: forms a path using a shape (VML)
		// newShape: Object: an VML path string or a path object (see dojox.gfx.defaultPath)
		this.vmlPath = [];
		this.lastControl = {};
		dojox.gfx.Path.superclass.setShape.apply(this, arguments);
		this.vmlPath = this.vmlPath.join("");
		this.rawNode.path.v = this.vmlPath + " r0,0 e";
		return this;
	},
	_pathVmlToSvgMap: {m: "M", l: "L", t: "m", r: "l", c: "C", v: "c", qb: "Q", x: "z", e: ""},
	// VML-specific segment renderers
	renderers: {
		M: "_moveToA", m: "_moveToR", 
		L: "_lineToA", l: "_lineToR", 
		H: "_hLineToA", h: "_hLineToR", 
		V: "_vLineToA", v: "_vLineToR", 
		C: "_curveToA", c: "_curveToR", 
		S: "_smoothCurveToA", s: "_smoothCurveToR", 
		Q: "_qCurveToA", q: "_qCurveToR", 
		T: "_qSmoothCurveToA", t: "_qSmoothCurveToR", 
		A: "_arcTo", a: "_arcTo", 
		Z: "_closePath", z: "_closePath"
	},
	_addArgs: function(path, args, from, upto){
		if(typeof(upto) == "undefined"){
			upto = args.length;
		}
		if(typeof(from) == "undefined"){
			from = 0;
		}
		for(var i = from; i < upto; ++i){
			path.push(" ");
			path.push(args[i].toFixed());
		}
	},
	_addArgsAdjusted: function(path, last, args, from, upto){
		if(typeof(upto) == "undefined"){
			upto = args.length;
		}
		if(typeof(from) == "undefined"){
			from = 0;
		}
		for(var i = from; i < upto; i += 2){
			path.push(" ");
			path.push((last.x + args[i]).toFixed());
			path.push(" ");
			path.push((last.y + args[i + 1]).toFixed());
		}
	},
	_moveToA: function(segment){
		var p = [" m"];
		var n = segment.args;
		var l = n.length;
		if(l == 2){
			this._addArgs(p, n);
		}else{
			this._addArgs(p, n, 0, 2);
			p.push(" l");
			this._addArgs(p, n, 2);
		}
		this.lastControl = {};
		return p;
	},
	_moveToR: function(segment, last){
		var p = ["x" in last ? " t" : " m"];
		var n = segment.args;
		var l = n.length;
		if(l == 2){
			this._addArgs(p, n);
		}else{
			this._addArgs(p, n, 0, 2);
			p.push(" r");
			this._addArgs(p, n, 2);
		}
		this.lastControl = {};
		return p;
	},
	_lineToA: function(segment){
		var p = [" l"];
		this._addArgs(p, segment.args);
		this.lastControl = {};
		return p;
	},
	_lineToR: function(segment){
		var p = [" r"];
		this._addArgs(p, segment.args);
		this.lastControl = {};
		return p;
	},
	_hLineToA: function(segment, last){
		var p = [" l"];
		var n = segment.args;
		var l = n.length;
		var y = " " + last.y.toFixed();
		for(var i = 0; i < l; ++i){
			p.push(" ");
			p.push(n[i].toFixed());
			p.push(y);
		}
		this.lastControl = {};
		return p;
	},
	_hLineToR: function(segment){
		var p = [" r"];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; ++i){
			p.push(" ");
			p.push(n[i].toFixed());
			p.push(" 0");
		}
		this.lastControl = {};
		return p;
	},
	_vLineToA: function(segment, last){
		var p = [" l"];
		var n = segment.args;
		var l = n.length;
		var x = " " + last.x.toFixed();
		for(var i = 0; i < l; ++i){
			p.push(x);
			p.push(" ");
			p.push(n[i].toFixed());
		}
		this.lastControl = {};
		return p;
	},
	_vLineToR: function(segment){
		var p = [" r"];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; ++i){
			p.push(" 0 ");
			p.push(n[i].toFixed());
		}
		this.lastControl = {};
		return p;
	},
	_curveToA: function(segment){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 6){
			p.push(" c");
			this._addArgs(p, n, i, i + 6);
		}
		this.lastControl = {x: n[l - 4], y: n[l - 3], type: "C"};
		return p;
	},
	_curveToR: function(segment, last){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 6){
			p.push(" v");
			this._addArgs(p, n, i, i + 6);
			this.lastControl = {x: last.x + n[i + 2], y: last.y + n[i + 3]};
			last.x += n[i + 4];
			last.y += n[i + 5];
		}
		this.lastControl.type = "C";
		return p;
	},
	_smoothCurveToA: function(segment, last){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 4){
			p.push(" c");
			if(this.lastControl.type == "C"){
				this._addArgs(p, [
					2 * last.x - this.lastControl.x, 
					2 * last.y - this.lastControl.y
				]);
			}else{
				this._addArgs(p, [last.x, last.y]);
			}
			this._addArgs(p, n, i, i + 4);
		}
		this.lastControl = {x: n[l - 4], y: n[l - 3], type: "C"};
		return p;
	},
	_smoothCurveToR: function(segment, last){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 4){
			p.push(" v");
			if(this.lastControl.type == "C"){
				this._addArgs(p, [
					last.x - this.lastControl.x, 
					last.y - this.lastControl.y
				]);
			}else{
				this._addArgs(p, [0, 0]);
			}
			this._addArgs(p, n, i, i + 4);
			this.lastControl = {x: last.x + n[i], y: last.y + n[i + 1]};
			last.x += n[i + 2];
			last.y += n[i + 3];
		}
		this.lastControl.type = "C";
		return p;
	},
	_qCurveToA: function(segment){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 4){
			p.push(" qb");
			this._addArgs(p, n, i, i + 4);
		}
		this.lastControl = {x: n[l - 4], y: n[l - 3], type: "Q"};
		return p;
	},
	_qCurveToR: function(segment, last){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 4){
			p.push(" qb");
			this._addArgsAdjusted(p, last, n, i, i + 4);
			this.lastControl = {x: last.x + n[i], y: last.y + n[i + 1]};
			last.x += n[i + 2];
			last.y += n[i + 3];
		}
		this.lastControl.type = "Q";
		return p;
	},
	_qSmoothCurveToA: function(segment, last){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 2){
			p.push(" qb");
			if(this.lastControl.type == "Q"){
				this._addArgs(p, [
					this.lastControl.x = 2 * last.x - this.lastControl.x, 
					this.lastControl.y = 2 * last.y - this.lastControl.y
				]);
			}else{
				this._addArgs(p, [
					this.lastControl.x = last.x, 
					this.lastControl.y = last.y
				]);
			}
			this._addArgs(p, n, i, i + 2);
		}
		this.lastControl.type = "Q";
		return p;
	},
	_qSmoothCurveToR: function(segment, last){
		var p = [];
		var n = segment.args;
		var l = n.length;
		for(var i = 0; i < l; i += 2){
			p.push(" qb");
			if(this.lastControl.type == "Q"){
				this._addArgs(p, [
					this.lastControl.x = 2 * last.x - this.lastControl.x, 
					this.lastControl.y = 2 * last.y - this.lastControl.y
				]);
			}else{
				this._addArgs(p, [
					this.lastControl.x = last.x, 
					this.lastControl.y = last.y
				]);
			}
			this._addArgsAdjusted(p, last, n, i, i + 2);
		}
		this.lastControl.type = "Q";
		return p;
	},
	_curvePI4: dojox.gfx.path._calcArc(Math.PI / 8),
	_calcArcTo: function(path, last, rx, ry, xRotg, large, sweep, x, y){
		var m = dojox.gfx.matrix;
		// calculate parameters
		var xRot = dojox.gfx.matrix._degToRad(xRotg);
		var rx2 = rx * rx;
		var ry2 = ry * ry;
		var pa = m.multiplyPoint(
			m.rotate(-xRot), 
			{x: (last.x - x) / 2, y: (last.y - y) / 2}
		);
		var pax2 = pa.x * pa.x;
		var pay2 = pa.y * pa.y;
		var c1 = Math.sqrt((rx2 * ry2 - rx2 * pay2 - ry2 * pax2) / (rx2 * pay2 + ry2 * pax2));
		var ca = {
			x:  c1 * rx * pa.y / ry,
			y: -c1 * ry * pa.x / rx
		};
		if(large == sweep){
			ca = {x: -ca.x, y: -ca.y};
		}
		// our center
		var c = m.multiplyPoint(
			[
				m.translate(
					(last.x + x) / 2,
					(last.y + y) / 2
				),
				m.rotate(xRot)
			], 
			ca
		);
		// start of our arc
		var startAngle = Math.atan2(last.y - c.y, last.x - c.x) - xRot;
		var endAngle   = Math.atan2(y - c.y, x - c.x) - xRot;
		// size of our arc in radians
		var theta = sweep ? endAngle - startAngle : startAngle - endAngle;
		if(theta < 0){
			theta += dojox.gfx.vml.two_pi;
		}else if(theta > dojox.gfx.vml.two_pi){
			theta = dojox.gfx.vml.two_pi;
		}
		// calculate our elliptic transformation
		var elliptic_transform = m.normalize([
			m.translate(c.x, c.y),
			m.rotate(xRot),
			m.scale(rx, ry)
		]);
		// draw curve chunks
		var alpha = dojox.gfx.vml.pi4 / 2;
		var curve = this._curvePI4;
		var step  = sweep ? alpha : -alpha;
		for(var angle = theta; angle > 0; angle -= dojox.gfx.vml.pi4){
			if(angle < dojox.gfx.vml.pi4){
				alpha = angle / 2;
				curve = dojox.gfx.path._calcArc(alpha);
				step  = sweep ? alpha : -alpha;
			}
			var c1, c2, e;
			var M = m.normalize([elliptic_transform, m.rotate(startAngle + step)]);
			if(sweep){
				c1 = m.multiplyPoint(M, curve.c1);
				c2 = m.multiplyPoint(M, curve.c2);
				e  = m.multiplyPoint(M, curve.e );
			}else{
				c1 = m.multiplyPoint(M, curve.c2);
				c2 = m.multiplyPoint(M, curve.c1);
				e  = m.multiplyPoint(M, curve.s );
			}
			// draw the curve
			path.push(" c");
			this._addArgs(path, [c1.x, c1.y, c2.x, c2.y, e.x, e.y]);
			startAngle += 2 * step;
		}
	},
	_arcTo: function(segment, last){
		var p = [];
		var n = segment.args;
		var l = n.length;
		var relative = segment.action == "a";
		for(var i = 0; i < l; i += 7){
			var x1 = n[i + 5];
			var y1 = n[i + 6];
			if(relative){
				x1 += last.x;
				y1 += last.y;
			}
			this._calcArcTo(
				p, last, n[i], n[i + 1], n[i + 2], 
				n[i + 3] ? 1 : 0, n[i + 4] ? 1 : 0,
				x1, y1
			);
			last = {x: x1, y: y1};
		}
		this.lastControl = {};
		return p;
	},
	_closePath: function(){
		this.lastControl = {};
		return ["x"];
	}
});
dojox.gfx.Path.nodeType = "shape";

dojo.declare("dojox.gfx.TextPath", dojox.gfx.Path, {
	// summary: a textpath shape (VML)

	constructor: function(rawNode){
		if(rawNode){rawNode.setAttribute("dojoGfxType", "textpath");}
		this.fontStyle = null;
		if(!("text" in this)){
			this.text = dojo.clone(dojox.gfx.defaultTextPath);
		}
		if(!("fontStyle" in this)){
			this.fontStyle = dojo.clone(dojox.gfx.defaultFont);
		}
	},
	setText: function(newText){
		// summary: sets a text to be drawn along the path
		this.text = dojox.gfx.makeParameters(this.text, 
			typeof(newText) == "string" ? {text: newText} : newText);
		this._setText();
		return this;	// self
	},
	setFont: function(newFont){
		// summary: sets a font for text
		this.fontStyle = typeof newFont == "string" ? 
			dojox.gfx.splitFontString(newFont) :
			dojox.gfx.makeParameters(dojox.gfx.defaultFont, newFont);
		this._setFont();
		return this;	// self
	},

	_setText: function(){
		// summary: sets a text shape object (VML)
		this.bbox = null;
		var r = this.rawNode;
		var s = this.text;
		// find path and text path
		var p = null, t = null;
		var c = r.childNodes;
		for(var i = 0; i < c.length; ++i){
			var tag = c[i].tagName;
			if(tag == "path"){
				p = c[i];
				if(t) break;
			}else if(tag == "textpath"){
				t = c[i];
				if(p) break;
			}
		}
		if(!p){
			p = this.rawNode.ownerDocument.createElement("v:path");
			r.appendChild(p);
		}
		if(!t){
			t = this.rawNode.ownerDocument.createElement("v:textpath");
			r.appendChild(t);
		}
		p.textPathOk = true;
		t.on = true;
		var a = dojox.gfx.vml.text_alignment[s.align];
		t.style["v-text-align"] = a ? a : "left";
		t.style["text-decoration"] = s.decoration;
		t.style["v-rotate-letters"] = s.rotated;
		t.style["v-text-kern"] = s.kerning;
		t.string = s.text;
	},
	_setFont: function(){
		// summary: sets a font object (VML)
		var f = this.fontStyle;
		var c = this.rawNode.childNodes;
		for(var i = 0; i < c.length; ++i){
			if(c[i].tagName == "textpath"){
				c[i].style.font = dojox.gfx.makeFontString(f);
				break;
			}
		}
	},
	attachText: function(rawNode){
		// summary: builds a textpath shape from a Node.
		// rawNode: Node: an VML node
		return dojox.gfx.Text.prototype.attachText.call(this, rawNode);
	},
	attachFont: function(rawNode){
		// summary: deduces a font style from a Node.
		// rawNode: Node: an VML node
		return dojox.gfx.Text.prototype.attachFont.call(this, rawNode);
	}
});
dojox.gfx.TextPath.nodeType = "shape";


dojox.gfx.vml._creators = {
	// summary: VML shape creators
	createPath: function(path){
		// summary: creates a VML path shape
		// path: Object: a path object (see dojox.gfx.defaultPath)
		return this.createObject(dojox.gfx.Path, path, true);	// dojox.gfx.Path
	},
	createRect: function(rect){
		// summary: creates a VML rectangle shape
		// rect: Object: a path object (see dojox.gfx.defaultRect)
		return this.createObject(dojox.gfx.Rect, rect);	// dojox.gfx.Rect
	},
	createCircle: function(circle){
		// summary: creates a VML circle shape
		// circle: Object: a circle object (see dojox.gfx.defaultCircle)
		return this.createObject(dojox.gfx.Circle, circle);	// dojox.gfx.Circle
	},
	createEllipse: function(ellipse){
		// summary: creates a VML ellipse shape
		// ellipse: Object: an ellipse object (see dojox.gfx.defaultEllipse)
		return this.createObject(dojox.gfx.Ellipse, ellipse);	// dojox.gfx.Ellipse
	},
	createLine: function(line){
		// summary: creates a VML line shape
		// line: Object: a line object (see dojox.gfx.defaultLine)
		return this.createObject(dojox.gfx.Line, line, true);	// dojox.gfx.Line
	},
	createPolyline: function(points){
		// summary: creates a VML polyline/polygon shape
		// points: Object: a points object (see dojox.gfx.defaultPolyline)
		//	or an Array of points
		return this.createObject(dojox.gfx.Polyline, points, true);	// dojox.gfx.Polyline
	},
	createImage: function(image){
		// summary: creates a VML image shape
		// image: Object: an image object (see dojox.gfx.defaultImage)
		if(!this.rawNode) return null;
		var shape = new dojox.gfx.Image();
		var node = this.rawNode.ownerDocument.createElement('div');
		node.style.position = "absolute";
		node.style.width  = this.rawNode.style.width;
		node.style.height = this.rawNode.style.height;
		node.style.filter = "progid:DXImageTransform.Microsoft.Matrix(M11=1, M12=0, M21=0, M22=1, Dx=0, Dy=0)";
		var img  = this.rawNode.ownerDocument.createElement('img');
		node.appendChild(img);
		shape.setRawNode(node);
		this.rawNode.appendChild(node);
		shape.setShape(image);
		this.add(shape);
		return shape;	// dojox.gfx.Image
	},
	createText: function(text){
		// summary: creates a VML text shape
		// text: Object: a text object (see dojox.gfx.defaultText)
		return this.createObject(dojox.gfx.Text, text, true);	// dojox.gfx.Text
	},
	createTextPath: function(text){
		// summary: creates an VML text shape
		// text: Object: a textpath object (see dojox.gfx.defaultTextPath)
		return this.createObject(dojox.gfx.TextPath, {}, true).setText(text);	// dojox.gfx.TextPath
	},
	createGroup: function(){
		// summary: creates a VML group shape
		var g = this.createObject(dojox.gfx.Group, null, true);	// dojox.gfx.Group
		// create a background rectangle, which is required to show all other shapes
		var r = g.rawNode.ownerDocument.createElement("v:rect");
		r.style.left = r.style.top = 0;
		r.style.width  = g.rawNode.style.width;
		r.style.height = g.rawNode.style.height;
		r.filled = r.stroked = false;
		g.rawNode.appendChild(r);
		g.bgNode = r;
		return g;	// dojox.gfx.Group
	},
	createObject: function(shapeType, rawShape, overrideSize) {
		// summary: creates an instance of the passed shapeType class
		// shapeType: Function: a class constructor to create an instance of
		// rawShape: Object: properties to be passed in to the classes "setShape" method
		if(!this.rawNode) return null;
		var shape = new shapeType();
		var node = this.rawNode.ownerDocument.createElement('v:' + shapeType.nodeType);
		shape.setRawNode(node);
		this.rawNode.appendChild(node);
		if(overrideSize) this._overrideSize(node);
		shape.setShape(rawShape);
		this.add(shape);
		return shape;	// dojox.gfx.Shape
	},
	createShape: dojox.gfx._createShape,
	_overrideSize: function(node){
		node.style.width  = this.rawNode.style.width;
		node.style.height = this.rawNode.style.height;
		node.coordsize = parseFloat(node.style.width) + " " + parseFloat(node.style.height);
	}
};

dojo.extend(dojox.gfx.Group, dojox.gfx.vml._creators);
dojo.extend(dojox.gfx.Surface, dojox.gfx.vml._creators);

delete dojox.gfx.vml._creators;

dojox.gfx.attachNode = function(node){
	// summary: creates a shape from a Node
	// node: Node: an VML node
	if(!node) return null;
	var s = null;
	switch(node.tagName.toLowerCase()){
		case dojox.gfx.Rect.nodeType:
			s = new dojox.gfx.Rect();
			break;
		case dojox.gfx.Ellipse.nodeType:
			s = (node.style.width == node.style.height)
				? new dojox.gfx.Circle()
				: new dojox.gfx.Ellipse();
			break;
		case dojox.gfx.Path.nodeType:
			switch(node.getAttribute("dojoGfxType")){
				case "line":
					s = new dojox.gfx.Line();
					break;
				case "polyline":
					s = new dojox.gfx.Polyline();
					break;
				case "path":
					s = new dojox.gfx.Path();
					break;
				case "text":
					s = new dojox.gfx.Text();
					break;
				case "textpath":
					s = new dojox.gfx.TextPath();
					break;
			}
			break;
		case dojox.gfx.Image.nodeType:
			switch(node.getAttribute("dojoGfxType")){
				case "image":
					s = new dojox.gfx.Image();
					break;
			}
			break;
		default:
			//console.debug("FATAL ERROR! tagName = " + node.tagName);
			return null;	// dojox.gfx.Shape
	}
	s.attach(node);
	return s;	// dojox.gfx.Shape
};

dojo.extend(dojox.gfx.Surface, {
	// summary: a surface object to be used for drawings (VML)

	setDimensions: function(width, height){
		// summary: sets the width and height of the rawNode
		// width: String: width of surface, e.g., "100px"
		// height: String: height of surface, e.g., "100px"
		if(!this.rawNode) return this;
		this.rawNode.style.width = width;
		this.rawNode.style.height = height;
		this.rawNode.coordsize = width + " " + height;
		this.bgNode.style.width = width;
		this.bgNode.style.height = height;
		return this;	// self
	},
	getDimensions: function(){
		// summary: returns an object with properties "width" and "height"
		return this.rawNode ? { width: this.rawNode.style.width, height: this.rawNode.style.height } : null; // Object
	},
	// group control
	add: function(shape){
		// summary: adds a shape to a group/surface
		// shape: dojox.gfx.Shape: an VMLshape object
		var oldParent = shape.getParent();
		if(this != oldParent){
			this.rawNode.appendChild(shape.rawNode);
			if(oldParent){
				oldParent.remove(shape, true);
			}
			shape._setParent(this, null);
		}
		return this;	// self
	},
	remove: function(shape, silently){
		// summary: remove a shape from a group/surface
		// shape: dojox.gfx.Shape: an VML shape object
		// silently: Boolean?: if true, regenerate a picture
		if(this == shape.getParent()){
			if(this.rawNode == shape.rawNode.parentNode){
				this.rawNode.removeChild(shape.rawNode);
			}
			shape._setParent(null, null);
		}
		return this;	// self
	},
	clear: dojox.gfx.vml._clear
});

dojox.gfx.createSurface = function(parentNode, width, height){
	// summary: creates a surface (VML)
	// parentNode: Node: a parent node
	// width: String: width of surface, e.g., "100px"
	// height: String: height of surface, e.g., "100px"

	var s = new dojox.gfx.Surface(), p = dojo.byId(parentNode);
	s.rawNode = p.ownerDocument.createElement("v:group");
	s.rawNode.style.width  = width  ? width  : "100%";
	s.rawNode.style.height = height ? height : "100%";
	//p.style.position = "absolute";
	//p.style.clip = "rect(0 " + s.rawNode.style.width + " " + s.rawNode.style.height + " 0)";
	s.rawNode.style.position = "relative";
	s.rawNode.coordsize = (width && height)
		? (parseFloat(width) + " " + parseFloat(height))
		: "100% 100%";
	s.rawNode.coordorigin = "0 0";
	p.appendChild(s.rawNode);
	// create a background rectangle, which is required to show all other shapes
	var r = s.rawNode.ownerDocument.createElement("v:rect");
	r.style.left = r.style.top = 0;
	r.style.width  = s.rawNode.style.width;
	r.style.height = s.rawNode.style.height;
	r.filled = r.stroked = false;
	s.rawNode.appendChild(r);
	s.bgNode = r;
	return s;	// dojox.gfx.Surface
};

dojox.gfx.attachSurface = function(node){
	// summary: creates a surface from a Node
	// node: Node: an VML node
	var s = new dojox.gfx.Surface();
	s.rawNode = node;
	var r = node.firstChild;
	if(!r || r.tagName != "rect"){
		return null;	// dojox.gfx.Surface
	}
	s.bgNode = r;
	return s;	// dojox.gfx.Surface
};

}
