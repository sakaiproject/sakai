if(!dojo._hasResource["dojox.gfx.shape"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.gfx.shape"] = true;
dojo.provide("dojox.gfx.shape");

dojo.require("dojox.gfx._base");

dojo.declare("dojox.gfx.Shape", null, {
	// summary: a Shape object, which knows how to apply 
	// graphical attributes and transformations
	
	constructor: function(){
		// rawNode: Node: underlying node
		this.rawNode = null;
		
		// shape: Object: an abstract shape object
		//	(see dojox.gfx.defaultPath,
		//	dojox.gfx.defaultPolyline,
		//	dojox.gfx.defaultRect,
		//	dojox.gfx.defaultEllipse,
		//	dojox.gfx.defaultCircle,
		//	dojox.gfx.defaultLine,
		//	or dojox.gfx.defaultImage)
		this.shape = null;
		
		// matrix: dojox.gfx.matrix.Matrix: a transformation matrix
		this.matrix = null;
		
		// fillStyle: Object: a fill object 
		//	(see dojox.gfx.defaultLinearGradient, 
		//	dojox.gfx.defaultRadialGradient, 
		//	dojox.gfx.defaultPattern, 
		//	or dojo.Color)
		this.fillStyle = null;
		
		// strokeStyle: Object: a stroke object 
		//	(see dojox.gfx.defaultStroke) 
		this.strokeStyle = null;
		
		// bbox: dojox.gfx.Rectangle: a bounding box of this shape
		//	(see dojox.gfx.defaultRect)
		this.bbox = null;
		
		// virtual group structure
		
		// parent: Object: a parent or null
		//	(see dojox.gfx.Surface,
		//	dojox.gfx.shape.VirtualGroup,
		//	or dojox.gfx.Group)
		this.parent = null;
		
		// parentMatrix: dojox.gfx.matrix.Matrix
		//	a transformation matrix inherited from the parent
		this.parentMatrix = null;
	},
	
	// trivial getters
	
	getNode: function(){
		// summary: returns the current DOM Node or null
		return this.rawNode; // Node
	},
	getShape: function(){
		// summary: returns the current shape object or null
		//	(see dojox.gfx.defaultPath,
		//	dojox.gfx.defaultPolyline,
		//	dojox.gfx.defaultRect,
		//	dojox.gfx.defaultEllipse,
		//	dojox.gfx.defaultCircle,
		//	dojox.gfx.defaultLine,
		//	or dojox.gfx.defaultImage)
		return this.shape; // Object
	},
	getTransform: function(){
		// summary: returns the current transformation matrix or null
		return this.matrix;	// dojox.gfx.matrix.Matrix
	},
	getFill: function(){
		// summary: returns the current fill object or null
		//	(see dojox.gfx.defaultLinearGradient, 
		//	dojox.gfx.defaultRadialGradient, 
		//	dojox.gfx.defaultPattern, 
		//	or dojo.Color)
		return this.fillStyle;	// Object
	},
	getStroke: function(){
		// summary: returns the current stroke object or null
		//	(see dojox.gfx.defaultStroke) 
		return this.strokeStyle;	// Object
	},
	getParent: function(){
		// summary: returns the parent or null
		//	(see dojox.gfx.Surface,
		//	dojox.gfx.shape.VirtualGroup,
		//	or dojox.gfx.Group)
		return this.parent;	// Object
	},
	getBoundingBox: function(){
		// summary: returns the bounding box or null
		//	(see dojox.gfx.defaultRect)
		return this.bbox;	// dojox.gfx.Rectangle
	},
	getTransformedBoundingBox: function(){
		// summary: returns an array of four points or null
		//	four points represent four corners of the untransformed bounding box
		var b = this.getBoundingBox();
		if(!b){
			return null;	// null
		}
		var m = this._getRealMatrix();
		var r = [];
		var g = dojox.gfx.matrix;
		r.push(g.multiplyPoint(m, b.x, b.y));
		r.push(g.multiplyPoint(m, b.x + b.width, b.y));
		r.push(g.multiplyPoint(m, b.x + b.width, b.y + b.height));
		r.push(g.multiplyPoint(m, b.x, b.y + b.height));
		return r;	// Array
	},
	getEventSource: function(){
		// summary: returns a Node, which is used as 
		//	a source of events for this shape
		return this.rawNode;	// Node
	},
	
	// empty settings
	
	setShape: function(shape){
		// summary: sets a shape object
		//	(the default implementation simply ignores it)
		// shape: Object: a shape object
		//	(see dojox.gfx.defaultPath,
		//	dojox.gfx.defaultPolyline,
		//	dojox.gfx.defaultRect,
		//	dojox.gfx.defaultEllipse,
		//	dojox.gfx.defaultCircle,
		//	dojox.gfx.defaultLine,
		//	or dojox.gfx.defaultImage)
		return this;	// self
	},
	setFill: function(fill){
		// summary: sets a fill object
		//	(the default implementation simply ignores it)
		// fill: Object: a fill object
		//	(see dojox.gfx.defaultLinearGradient, 
		//	dojox.gfx.defaultRadialGradient, 
		//	dojox.gfx.defaultPattern, 
		//	or dojo.Color)
		return this;	// self
	},
	setStroke: function(stroke){
		// summary: sets a stroke object
		//	(the default implementation simply ignores it)
		// stroke: Object: a stroke object
		//	(see dojox.gfx.defaultStroke) 
		return this;	// self
	},
	
	// z-index
	
	moveToFront: function(){
		// summary: moves a shape to front of its parent's list of shapes
		//	(the default implementation does nothing)
		return this;	// self
	},
	moveToBack: function(){
		// summary: moves a shape to back of its parent's list of shapes
		//	(the default implementation does nothing)
		return this;
	},

	setTransform: function(matrix){
		// summary: sets a transformation matrix
		// matrix: dojox.gfx.matrix.Matrix: a matrix or a matrix-like object
		//	(see an argument of dojox.gfx.matrix.Matrix 
		//	constructor for a list of acceptable arguments)
		this.matrix = dojox.gfx.matrix.clone(matrix ? dojox.gfx.matrix.normalize(matrix) : dojox.gfx.identity, true);
		return this._applyTransform();	// self
	},
	
	// apply left & right transformation
	
	applyRightTransform: function(matrix){
		// summary: multiplies the existing matrix with an argument on right side
		//	(this.matrix * matrix)
		// matrix: dojox.gfx.matrix.Matrix: a matrix or a matrix-like object
		//	(see an argument of dojox.gfx.matrix.Matrix 
		//	constructor for a list of acceptable arguments)
		return matrix ? this.setTransform([this.matrix, matrix]) : this;	// self
	},
	applyLeftTransform: function(matrix){
		// summary: multiplies the existing matrix with an argument on left side
		//	(matrix * this.matrix)
		// matrix: dojox.gfx.matrix.Matrix: a matrix or a matrix-like object
		//	(see an argument of dojox.gfx.matrix.Matrix 
		//	constructor for a list of acceptable arguments)
		return matrix ? this.setTransform([matrix, this.matrix]) : this;	// self
	},

	applyTransform: function(matrix){
		// summary: a shortcut for dojox.gfx.Shape.applyRightTransform
		// matrix: dojox.gfx.matrix.Matrix: a matrix or a matrix-like object
		//	(see an argument of dojox.gfx.matrix.Matrix 
		//	constructor for a list of acceptable arguments)
		return matrix ? this.setTransform([this.matrix, matrix]) : this;	// self
	},
	
	// virtual group methods
	
	removeShape: function(silently){
		// summary: removes the shape from its parent's list of shapes
		// silently: Boolean?: if true, do not redraw a picture yet
		if(this.parent){
			this.parent.remove(this, silently);
		}
		return this;	// self
	},
	_setParent: function(parent, matrix){
		// summary: sets a parent
		// parent: Object: a parent or null
		//	(see dojox.gfx.Surface,
		//	dojox.gfx.shape.VirtualGroup,
		//	or dojox.gfx.Group)
		// matrix: dojox.gfx.matrix.Matrix:
		//	a 2D matrix or a matrix-like object
		this.parent = parent;
		return this._updateParentMatrix(matrix);	// self
	},
	_updateParentMatrix: function(matrix){
		// summary: updates the parent matrix with new matrix
		// matrix: dojox.gfx.matrix.Matrix:
		//	a 2D matrix or a matrix-like object
		this.parentMatrix = matrix ? dojox.gfx.matrix.clone(matrix) : null;
		return this._applyTransform();	// self
	},
	_getRealMatrix: function(){
		// summary: returns the cumulative ("real") transformation matrix
		//	by combining the shape's matrix with its parent's matrix
		return this.parentMatrix ? new dojox.gfx.matrix.Matrix2D([this.parentMatrix, this.matrix]) : this.matrix;	// dojox.gfx.Matrix2D
	}
});
dojo.extend(dojox.gfx.Shape, dojox.gfx._eventsProcessing);

dojo.declare("dojox.gfx.shape.VirtualGroup", dojox.gfx.Shape, {
	// summary: a virtual group of shapes, which can be used 
	//	as a foundation for renderer-specific groups, or as a way 
	//	to logically group shapes (e.g, to propagate matricies)
	
	constructor: function() {
		// children: Array: a list of children
		this.children = [];
	},
	
	// group management
	
	add: function(shape){
		// summary: adds a shape to the list
		// shape: dojox.gfx.Shape: a shape
		var oldParent = shape.getParent();
		if(oldParent){
			oldParent.remove(shape, true);
		}
		this.children.push(shape);
		return shape._setParent(this, this._getRealMatrix());	// self
	},
	remove: function(shape, silently){
		// summary: removes a shape from the list
		// silently: Boolean?: if true, do not redraw a picture yet
		for(var i = 0; i < this.children.length; ++i){
			if(this.children[i] == shape){
				if(silently){
					// skip for now
				}else{
					shape._setParent(null, null);
				}
				this.children.splice(i, 1);
				break;
			}
		}
		return this;	// self
	},
	clear: function(){
		// summary: removes all shapes from a group/surface
		this.children = [];
		return this;
	},
	
	// apply transformation
	
	_applyTransform: function(){
		// summary: applies a transformation matrix to a group
		var matrix = this._getRealMatrix();
		for(var i = 0; i < this.children.length; ++i){
			this.children[i]._updateParentMatrix(matrix);
		}
		return this;	// self
	}
});

dojo.declare("dojox.gfx.shape.Rect", dojox.gfx.Shape, {
	// summary: a generic rectangle
	constructor: function(rawNode) {
		// rawNode: Node: a DOM Node
		this.shape = dojo.clone(dojox.gfx.defaultRect);
		this.attach(rawNode);
	},
	getBoundingBox: function(){
		// summary: returns the bounding box (its shape in this case)
		return this.shape;	// dojox.gfx.Rectangle
	}
});

dojo.declare("dojox.gfx.shape.Ellipse", dojox.gfx.Shape, {
	// summary: a generic ellipse
	constructor: function(rawNode) {
		// rawNode: Node: a DOM Node
		this.shape = dojo.clone(dojox.gfx.defaultEllipse);
		this.attach(rawNode);
	},
	getBoundingBox: function(){
		// summary: returns the bounding box
		if(!this.bbox){
			var shape = this.shape;
			this.bbox = {x: shape.cx - shape.rx, y: shape.cy - shape.ry, 
				width: 2 * shape.rx, height: 2 * shape.ry};
		}
		return this.bbox;	// dojox.gfx.Rectangle
	}
});

dojo.declare("dojox.gfx.shape.Circle", dojox.gfx.Shape, {
	// summary: a generic circle
	//	(this is a helper object, which is defined for convenience)
	constructor: function(rawNode) {
		// rawNode: Node: a DOM Node
		this.shape = dojo.clone(dojox.gfx.defaultCircle);
		this.attach(rawNode);
	},
	getBoundingBox: function(){
		// summary: returns the bounding box
		if(!this.bbox){
			var shape = this.shape;
			this.bbox = {x: shape.cx - shape.r, y: shape.cy - shape.r, 
				width: 2 * shape.r, height: 2 * shape.r};
		}
		return this.bbox;	// dojox.gfx.Rectangle
	}
});

dojo.declare("dojox.gfx.shape.Line", dojox.gfx.Shape, {
	// summary: a generic line
	//	(this is a helper object, which is defined for convenience)
	constructor: function(rawNode) {
		// rawNode: Node: a DOM Node
		this.shape = dojo.clone(dojox.gfx.defaultLine);
		this.attach(rawNode);
	},
	getBoundingBox: function(){
		// summary: returns the bounding box
		if(!this.bbox){
			var shape = this.shape;
			this.bbox = {
				x:		Math.min(shape.x1, shape.x2),
				y:		Math.min(shape.y1, shape.y2),
				width:	Math.abs(shape.x2 - shape.x1),
				height:	Math.abs(shape.y2 - shape.y1)
			};
		}
		return this.bbox;	// dojox.gfx.Rectangle
	}
});

dojo.declare("dojox.gfx.shape.Polyline", dojox.gfx.Shape, {
	// summary: a generic polyline/polygon
	//	(this is a helper object, which is defined for convenience)
	constructor: function(rawNode) {
		// rawNode: Node: a DOM Node
		this.shape = dojo.clone(dojox.gfx.defaultPolyline);
		this.attach(rawNode);
	},
	getBoundingBox: function(){
		// summary: returns the bounding box
		if(!this.bbox && this.shape.points.length){
			var p = this.shape.points;
			var l = p.length;
			var t = p[0];
			var bbox = {l: t.x, t: t.y, r: t.x, b: t.y};
			for(var i = 1; i < l; ++i){
				t = p[i];
				if(bbox.l > t.x) bbox.l = t.x;
				if(bbox.r < t.x) bbox.r = t.x;
				if(bbox.t > t.y) bbox.t = t.y;
				if(bbox.b < t.y) bbox.b = t.y;
			}
			this.bbox = {
				x:		bbox.l, 
				y:		bbox.t, 
				width:	bbox.r - bbox.l, 
				height:	bbox.b - bbox.t
			};
		}
		return this.bbox;	// dojox.gfx.Rectangle
	}
});

dojo.declare("dojox.gfx.shape.Image", dojox.gfx.Shape, {
	// summary: a generic image
	//	(this is a helper object, which is defined for convenience)
	constructor: function(rawNode) {
		// rawNode: Node: a DOM Node
		this.shape = dojo.clone(dojox.gfx.defaultImage);
		this.attach(rawNode);
	},
	getBoundingBox: function(){
		// summary: returns the bounding box (its shape in this case)
		return this.shape;	// dojox.gfx.Rectangle
	}
});

dojo.declare("dojox.gfx.shape.Text", dojox.gfx.Shape, {
	// summary: a generic text
	constructor: function(rawNode) {
		// rawNode: Node: a DOM Node
		this.fontStyle = null;
		this.shape = dojo.clone(dojox.gfx.defaultText);
		this.attach(rawNode);
	},
	setFont: function(newFont){
		// summary: sets a font for text
		// newFont: Object: a font object (see dojox.gfx.defaultFont) or a font string
		this.fontStyle = typeof newFont == "string" ? dojox.gfx.splitFontString(newFont) :
			dojox.gfx.makeParameters(dojox.gfx.defaultFont, newFont);
		this._setFont();
		return this;	// self
	}
});

}
