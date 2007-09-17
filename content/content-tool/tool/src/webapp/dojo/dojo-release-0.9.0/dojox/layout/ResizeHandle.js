if(!dojo._hasResource["dojox.layout.ResizeHandle"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.layout.ResizeHandle"] = true;
dojo.provide("dojox.layout.ResizeHandle");
dojo.experimental("dojox.layout.ResizeHandle"); 

dojo.require("dijit._Widget");
dojo.require("dijit._Templated"); 
dojo.require("dojo.fx"); 

dojo.declare("dojox.layout.ResizeHandle", [dijit._Widget, dijit._Templated], {
	// summary
	//	The handle on the bottom-right corner of FloatingPane or other widgets that allows
	//	the widget to be resized.
	//	Typically not used directly.

	// targetId: String
	//	id of the Widget OR DomNode that I will size
	targetId: '',

	// targetContainer: DomNode
	//	over-ride targetId and attch this handle directly to a reference of a DomNode
	targetContainer: null, 

	// resizeAxis: String
	//	one of: x|y|xy limit resizing to a single axis, default to xy ... 
	resizeAxis: "xy",

	// activeResize: Boolean
	// 	if true, node will size realtime with mouse movement, 
	//	if false, node will create virtual node, and only resize target on mouseUp
	activeResize: false,
	
	// activeResizeClass: String
	//	css class applied to virtual resize node. 
	activeResizeClass: 'dojoxResizeHandleClone',

	// animateSizing: Boolean
	//	only applicable if activeResize = false. onMouseup, animate the node to the
	//	new size
	animateSizing: true,
	
	// animateMethod: String
	// 	one of "chain" or "combine" ... visual effect only. combine will "scale" 
	// 	node to size, "chain" will alter width, then height
	animateMethod: 'chain',

	// animateDuration: Integer
	//	time in MS to run sizing animation. if animateMethod="chain", total animation 
	//	playtime is 2*animateDuration
	animateDuration: 225,

	// minHeight: Integer
	//	smallest height in px resized node can be
	minHeight: 100,

	// minWidth: Integer
	//	smallest width in px resize node can be
	minWidth: 100,

	// resize handle template, fairly easy to override: 
	templateString: '<div dojoAttachPoint="resizeHandle" class="dojoxResizeHandle"><div></div></div>',

	// private propteries and holders
	_isSizing: false,
	_connects: [],
	_activeResizeNode: null,	
	_activeResizeLastEvent: null,
	// defaults to match default resizeAxis. set resizeAxis variable to modify. 
	_resizeX: true,
	_resizeY: true,


	postCreate: function(){
		// summary: setup our one major listener upon creation
		dojo.connect(this.resizeHandle, "onmousedown", this, "_beginSizing");
		if(!this.activeResize){ 
			this._activeResizeNode = document.createElement('div');
			dojo.addClass(this._activeResizeNode,this.activeResizeClass); 
		}else{ this.animateSizing = false; } 	

		if (!this.minSize) { 
			this.minSize = { w: this.minWidth, h: this.minHeight };
		}
		// should we modify the css for the cursor hover to n-resize nw-resize and w-resize?
		this._resizeX = this._resizeY = false; 
		switch (this.resizeAxis.toLowerCase()) {
		case "xy" : 
			this._resizeX = this._resizeY = true; 
			// FIXME: need logic to determine NW or NE class to see
			// based on which [todo] corner is clicked
			dojo.addClass(this.resizeHandle,"dojoxResizeNW"); 
			break; 
		case "x" : 
			this._resizeX = true; 
			dojo.addClass(this.resizeHandle,"dojoxResizeW");
			break;
		case "y" : 
			this._resizeY = true; 
			dojo.addClass(this.resizeHandle,"dojoxResizeN");
			break;
		}
	},

	_beginSizing: function(/*Event*/ e){
		// summary: setup movement listeners and calculate initial size
		
		if (this._isSizing){ return false; }

		this.targetWidget = dijit.byId(this.targetId);

		// FIXME: resizing widgets does weird things, disable virtual resizing for now:
		if (this.targetWidget) { this.activeResize = true; } 

		this.targetDomNode = this.targetWidget ? this.targetWidget.domNode : dojo.byId(this.targetId);
		if (this.targetContainer) { this.targetDomNode = this.targetContainer; } 
		if (!this.targetDomNode){ return; }

		if (!this.activeResize) {
			this.targetDomNode.appendChild(this._activeResizeNode); 
			dojo.fadeIn({ node: this._activeResizeNode, duration:120, 
				beforeBegin: dojo.hitch(this,function(){
					this._activeResizeNode.style.display=''; 
				})
			}).play(); 
		}

		this._isSizing = true;
		this.startPoint  = {'x':e.clientX, 'y':e.clientY};

		// FIXME: this is funky: marginBox adds height, contentBox ignores padding (expected, but foo!)
		var mb = (this.targetWidget) ? dojo.marginBox(this.targetDomNode) : dojo.contentBox(this.targetDomNode);  
		this.startSize  = { 'w':mb.w, 'h':mb.h };

		this._connects = []; 
		this._connects.push(dojo.connect(document,"onmousemove",this,"_updateSizing")); 
		this._connects.push(dojo.connect(document,"onmouseup", this, "_endSizing"));

		e.preventDefault();
	},

	_updateSizing: function(/*Event*/ e){
		// summary: called when moving the ResizeHandle ... determines 
		//	new size based on settings/position and sets styles.

		if(this.activeResize){
			this._changeSizing(e);
		}else{
			var tmp = this._getNewCoords(e);	
			if(tmp === false){ return; }
			dojo.style(this._activeResizeNode,"width",tmp.width+"px");
			dojo.style(this._activeResizeNode,"height",tmp.height+"px"); 
			this._activeResizeNode.style.display=''; 
		}
	},

	_getNewCoords: function(/* Event */ e){
		
		// On IE, if you move the mouse above/to the left of the object being resized,
		// sometimes clientX/Y aren't set, apparently.  Just ignore the event.
		try{
			if(!e.clientX  || !e.clientY){ return false; }
		}catch(e){
			// sometimes you get an exception accessing above fields...
			return false;
		}
		this._activeResizeLastEvent = e; 

		var dx = this.startPoint.x - e.clientX;
		var dy = this.startPoint.y - e.clientY;
		
		var newW = (this._resizeX) ? this.startSize.w - dx : this.startSize.w;
		var newH = (this._resizeY) ? this.startSize.h - dy : this.startSize.h;

		// minimum size check
		if(this.minSize){
			//var mb = dojo.marginBox(this.targetDomNode);
			if(newW < this.minSize.w){
				newW = this.minSize.w;
			}
			if(newH < this.minSize.h){
				newH = this.minSize.h;
			}
		}
		return {width:newW, height:newH};  // Object
	},
	
	_changeSizing: function(/*Event*/ e){
		// summary: apply sizing information based on information in (e) to attached node
		var tmp = this._getNewCoords(e);
		if(tmp===false){ return; }

		if(this.targetWidget && typeof this.targetWidget.resize == "function"){ 
			this.targetWidget.resize({ w: tmp.width, h: tmp.height });
		}else{
			if(this.animateSizing){
				var anim = dojo.fx[this.animateMethod]([
					dojo.animateProperty({
						node: this.targetDomNode,
						properties: { 
							width: { start: this.startSize.w, end: tmp.width, unit:'px' } 
						},	
						duration: this.animateDuration
					}),
					dojo.animateProperty({
						node: this.targetDomNode,
						properties: { 
							height: { start: this.startSize.h, end: tmp.height, unit:'px' }
						},
						duration: this.animateDuration
					})
				]);
				anim.play();
			}else{
				dojo.style(this.targetDomNode,"width",tmp.width+"px"); 
				dojo.style(this.targetDomNode,"height",tmp.height+"px");
			}
		}	
		e.preventDefault();
	},

	_endSizing: function(/*Event*/ e){
		// summary: disconnect listenrs and cleanup sizing
		dojo.forEach(this._connects,function(c){
			dojo.disconnect(c); 
		});
		if(!this.activeResize){
			dojo.fadeOut({ node:this._activeResizeNode, duration:250,
				onEnd: dojo.hitch(this,function(){
					this._activeResizeNode.style.display="none";
				})
			}).play();
			this._changeSizing(e);
		}
		this._isSizing = false;
	}

});

}
