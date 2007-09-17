if(!dojo._hasResource["dijit._base.typematic"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.typematic"] = true;
dojo.provide("dijit._base.typematic");

dijit.typematic = {
	// summary:
	//              These functions are used to repetitively call a user specified callback
	//		method when a specific key or mouse click over a specific DOM node is
	//		held down for a specific amount of time.
	//		Only 1 such event is allowed to occur on the browser page at 1 time.

	_fireEventAndReload: function(){
		this._timer = null;
		this._callback(++this._count, this._node, this._evt);
		this._currentTimeout = (this._currentTimeout < 0) ? this._initialDelay : ((this._subsequentDelay > 1) ? this._subsequentDelay : Math.round(this._currentTimeout * this._subsequentDelay));
		this._timer = setTimeout(dojo.hitch(this, "_fireEventAndReload"), this._currentTimeout);
	},

	trigger: function(/*Event*/ evt, /* Object */ _this, /*DOMNode*/ node, /* Function */ callback, /* Object */ obj, /* Number */ subsequentDelay, /* Number */ initialDelay){
		// summary:
		//      Start a timed, repeating callback sequence.
		//	If already started, the function call is ignored.
		//	This method is not normally called by the user but can be
		//	when the normal listener code is insufficient.
		//	_this: pointer to the user's widget space.
		//	callback: function name to call until the sequence is stopped.
		//	obj: any user space object to pass to the callback.
		//	subsequentDelay: if > 1, the number of milliseconds until the 3->n events occur
		//		or else the fractional time multiplier for the next event.
		//	initialDelay: the number of milliseconds until the 2nd event occurs.
		if (obj != this._obj){
			this.stop();
			this._initialDelay = initialDelay ? initialDelay : 500;
			this._subsequentDelay = subsequentDelay ? subsequentDelay : 0.90;
			this._obj = obj;
			this._evt = evt;
			this._node = node;
			this._currentTimeout = -1;
			this._count = -1;
			this._callback = dojo.hitch(_this, callback);
			this._fireEventAndReload();
		}
	},

	stop: function(){
		// summary:
		//      Stop an ongoing timed, repeating callback sequence.
		if(this._timer){
			clearTimeout(this._timer);
			this._timer = null;
		}
		if(this._obj){
			this._callback(-1, this._node, this._evt);
			this._obj = null;
		}
	},

	addKeyListener: function(/*DOMNode*/ node, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
		// summary: Start listening for a specific typematic key.
		//	node: the DOM node object to listen on for key events.
		//	keyObject: an object defining the key to listen for.
		//		key: (mandatory) the keyCode (number) or character (string) to listen for.
		//		ctrlKey: desired ctrl key state to initiate the calback sequence:
		//			pressed (true)
		//			released (false)
		//			either (unspecified)
		//		altKey: same as ctrlKey but for the alt key
		//		shiftKey: same as ctrlKey but for the shift key
		//	See the trigger method for other parameters.
		var ary = [];
		ary.push(dojo.connect(node, "onkeypress", this, function(evt){
			if(evt.keyCode == keyObject.keyCode && (!keyObject.charCode || keyObject.charCode == evt.charCode)
			&& ((typeof keyObject.ctrlKey == "undefined") || keyObject.ctrlKey == evt.ctrlKey)
			&& ((typeof keyObject.altKey == "undefined") || keyObject.altKey == evt.ctrlKey)
			&& ((typeof keyObject.shiftKey == "undefined") || keyObject.shiftKey == evt.ctrlKey)){
				dojo.stopEvent(evt);
				dijit.typematic.trigger(keyObject, _this, node, callback, keyObject, subsequentDelay, initialDelay);
			}else if (dijit.typematic._obj == keyObject){
				dijit.typematic.stop();
			}
		}));
		ary.push(dojo.connect(node, "onkeyup", this, function(evt){
			if(dijit.typematic._obj == keyObject){
				dijit.typematic.stop();
			}
		}));
		return ary;
	},

	addMouseListener: function(/*DOMNode*/ node, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
		// summary: Start listening for a typematic mouse click.
		//	node: the DOM node object to listen on for mouse events.
		//	See the trigger method for other parameters.
		var ary = [];
		ary.push(dojo.connect(node, "mousedown", this, function(evt){
			dojo.stopEvent(evt);
			dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
		}));
		ary.push(dojo.connect(node, "mouseup", this, function(evt){
			dojo.stopEvent(evt);
			dijit.typematic.stop();
		}));
		ary.push(dojo.connect(node, "mouseout", this, function(evt){
			dojo.stopEvent(evt);
			dijit.typematic.stop();
		}));
		ary.push(dojo.connect(node, "mousemove", this, function(evt){
			dojo.stopEvent(evt);
		}));
		ary.push(dojo.connect(node, "dblclick", this, function(evt){
			dojo.stopEvent(evt);
			if(dojo.isIE){
				dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
				setTimeout("dijit.typematic.stop()",50);
			}
		}));
		return ary;
	},

	addListener: function(/*Node*/ mouseNode, /*Node*/ keyNode, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
		// summary: Start listening for a specific typematic key and mouseclick.
		//	This is a thin wrapper to addKeyListener and addMouseListener.
		//	mouseNode: the DOM node object to listen on for mouse events.
		//	keyNode: the DOM node object to listen on for key events.
		//	The mouseNode is used as the callback obj parameter.
		//	See the trigger method for other parameters.
		return this.addKeyListener(keyNode, keyObject, _this, callback, subsequentDelay, initialDelay).concat(
			this.addMouseListener(mouseNode, _this, callback, subsequentDelay, initialDelay));
	}
};

}
