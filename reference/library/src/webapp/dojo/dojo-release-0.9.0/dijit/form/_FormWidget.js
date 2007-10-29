if(!dojo._hasResource["dijit.form._FormWidget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form._FormWidget"] = true;
dojo.provide("dijit.form._FormWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.declare("dijit.form._FormWidget", [dijit._Widget, dijit._Templated],
{
	/*
	Summary:
		FormElement widgets correspond to native HTML elements such as <input> or <button> or <select>.
		Each FormElement represents a single input value, and has a (possibly hidden) <input> element,
		to which it serializes its input value, so that form submission (either normal submission or via FormBind?)
		works as expected.

		All these widgets should have these attributes just like native HTML input elements.
		You can set them during widget construction, but after that they are read only.

		They also share some common methods.
	*/

	// baseClass: String
	//		Used to add CSS classes like FormElementDisabled
	// TODO: remove this in favor of this.domNode.baseClass?
	baseClass: "",

	// value: String
	//		Corresponds to the native HTML <input> element's attribute.
	value: "",

	// name: String
	//		Name used when submitting form; same as "name" attribute or plain HTML elements
	name: "",

	// id: String
	//		Corresponds to the native HTML <input> element's attribute.
	//		Also becomes the id for the widget.
	id: "",

	// alt: String
	//		Corresponds to the native HTML <input> element's attribute.
	alt: "",

	// type: String
	//		Corresponds to the native HTML <input> element's attribute.
	type: "text",

	// tabIndex: Integer
	//		Order fields are traversed when user hits the tab key
	tabIndex: "0",

	// disabled: Boolean
	//		Should this widget respond to user input?
	//		In markup, this is specified as "disabled='disabled'", or just "disabled".
	disabled: false,

	// intermediateChanges: Boolean
	//              Fires onChange for each value change or only on demand
	intermediateChanges: false,

	setDisabled: function(/*Boolean*/ disabled){
		// summary:
		//		Set disabled state of widget.

		this.domNode.disabled = this.disabled = disabled;
		if(this.focusNode){
			this.focusNode.disabled = disabled;
		}
		if(disabled){
			//reset those, because after the domNode is disabled, we can no longer receive
			//mouse related events, see #4200
			this._hovering = false;
			this._active = false;
		}
		dijit.wai.setAttr(this.focusNode || this.domNode, "waiState", "disabled", disabled);
		this._setStateClass();
	},


	_onMouse : function(/*Event*/ event){
		// summary:
		//	Sets _hovering, _active, and baseClass attributes depending on mouse state,
		//	then calls setStateClass() to set appropriate CSS class for this.domNode.
		//
		//	To get a different CSS class for hover, send onmouseover and onmouseout events to this method.
		//	To get a different CSS class while mouse button is depressed, send onmousedown to this method.

		var mouseNode = event.target;

		if(!this.disabled){
			switch(event.type){
				case "mouseover" :
					this._hovering = true;
					var baseClass, node=mouseNode;
					while( node.nodeType===1 && !(baseClass=node.getAttribute("baseClass")) && node != this.domNode ){
						node=node.parentNode;
					}
					this.baseClass= baseClass || "dijit"+this.declaredClass.replace(/.*\./g,"");
					break;

				case "mouseout" :	
					this._hovering = false;	
					this.baseClass=null;
					break;

				case "mousedown" :
					this._active = true;
					// set a global event to handle mouseup, so it fires properly
					//	even if the cursor leaves the button
					var self = this;
					// #2685: use this.connect and disconnect so destroy works properly
					var mouseUpConnector = this.connect(dojo.body(), "onmouseup", function(){
						self._active = false;
						self._setStateClass();
						self.disconnect(mouseUpConnector);
					});
					break;
			}
			this._setStateClass();
		}
	},

	focus: function(){
		dijit.focus(this.focusNode);
	},

	_setStateClass: function(/*String*/ base){
		// summary:
		//	Update the visual state of the widget by changing the css class on the domnode
		//	according to widget state.
		//
		//	State will be one of:
		//		<baseClass>
		//		<baseClass> + "Disabled"	- if the widget is disabled
		//		<baseClass> + "Active"		- if the mouse (or space/enter key?) is being pressed down
		//		<baseClass> + "Hover"		- if the mouse is over the widget
		//		<baseClass> + "Focused"		- if the widget has focus
		//
		//	Note: if you don't want to change the way the widget looks on hover, then don't call
		//	this routine on hover.  Similarly for mousedown --> active
		//
		//	For widgets which can be in a checked state (like checkbox or radio),
		//	in addition to the above classes...
		//		<baseClass> + "Checked"
		//		<baseClass> + "CheckedDisabled"	- if the widget is disabled
		//		<baseClass> + "CheckedActive"		- if the mouse is being pressed down
		//		<baseClass> + "CheckedHover"		- if the mouse is over the widget
		//		<baseClass> + "CheckedFocused"		- if the widget has focus

		// get original class (non state related) specified in template
		var origClass = (this.styleNode||this.domNode).className;

		// compute list of classname representing the states of the widget
		var base = this.baseClass || this.domNode.getAttribute("baseClass") || "dijitFormWidget";
		origClass = origClass.replace(new RegExp("\\b"+base+"(Checked)?(Selected)?(Disabled|Active|Focused|Hover)?\\b\\s*", "g"), "");
		var classes = [ base ];
		
		function multiply(modifier){
			classes=classes.concat(dojo.map(classes, function(c){ return c+modifier; }));
		}

		if(this.checked){
			multiply("Checked");
		}
		if(this.selected){
			multiply("Selected");
		}
		
		// Only one of these four can be applied.
		// Active trumps Focused, Focused trumps Hover, and Disabled trumps all.
		if(this.disabled){
			multiply("Disabled");
		}else if(this._active){
			multiply("Active");
		}else if(this._focused){
			multiply("Focused");
		}else if(this._hovering){
			multiply("Hover");
		}

		(this.styleNode || this.domNode).className = origClass + " " + classes.join(" ");
	},

	onChange: function(newValue){
		// summary: callback when value is changed
	},

	postCreate: function(){
		this.setValue(this.value, true);
		this.setDisabled(this.disabled);
		this._setStateClass();
	},

	setValue: function(/*anything*/ newValue, /*Boolean, optional*/ priorityChange){
		// summary: set the value of the widget.
		this._lastValue = newValue;
		dijit.wai.setAttr(this.focusNode || this.domNode, "waiState", "valuenow", this.forWaiValuenow());
		if((this.intermediateChanges || priorityChange) && newValue != this._lastValueReported){
			this._lastValueReported = newValue;
			this.onChange(newValue);
		}
	},

	getValue: function(){
		// summary: get the value of the widget.
		return this._lastValue;
	},

	undo: function(){
		// summary: restore the value to the last value passed to onChange
		this.setValue(this._lastValueReported, false);
	},

	_onKeyPress: function(e){
		if(e.keyCode == dojo.keys.ESCAPE && !e.shiftKey && !e.ctrlKey && !e.altKey){
			var v = this.getValue();
			var lv = this._lastValueReported;
			// Equality comparison of objects such as dates are done by reference so 
			// two distinct objects are != even if they have the same data. So use 
			// toStrings in case the values are objects.
			if(lv != undefined && v.toString() != lv.toString()){	
				this.undo();
				dojo.stopEvent(e);
				return false;
			}
		}
		return true;
	},

	forWaiValuenow: function(){
		// summary: returns a value, reflecting the current state of the widget,
		//		to be used for the ARIA valuenow.
		// 		This method may be overridden by subclasses that want
		// 		to use something other than this.getValue() for valuenow
		return this.getValue();
	}
});

}
