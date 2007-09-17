if(!dojo._hasResource["dijit.ColorPalette"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.ColorPalette"] = true;
dojo.provide("dijit.ColorPalette");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dojo.colors");
dojo.require("dojo.i18n");
dojo.requireLocalization("dojo", "colors", null, "ROOT");

dojo.declare(
		"dijit.ColorPalette",
		[dijit._Widget, dijit._Templated],
{
	// summary
	//		Grid showing various colors, so the user can pick a certain color

	// defaultTimeout: Number
	//      number of milliseconds before a held key or button becomes typematic
	defaultTimeout: 500,

	// timeoutChangeRate: Number
	//      fraction of time used to change the typematic timer between events
	//      1.0 means that each typematic event fires at defaultTimeout intervals
	//      < 1.0 means that each typematic event fires at an increasing faster rate
	timeoutChangeRate: 0.90,

	// palette: String
	//		Size of grid, either "7x10" or "3x4".
	palette: "7x10",

	//_value: String
	//		The value of the selected color.
	value: null,

	//_currentFocus: Integer
	//		Index of the currently focused color.
	_currentFocus: 0,

	// _xDim: Integer
	//		This is the number of colors horizontally across.
	_xDim: null,

	// _yDim: Integer
	///		This is the number of colors vertically down.
	_yDim: null,

	// _palettes: Map
	// 		This represents the value of the colors.
	//		The first level is a hashmap of the different arrays available
	//		The next two dimensions represent the columns and rows of colors.
	_palettes: {
			
		"7x10":	[["white", "seashell", "cornsilk", "lemonchiffon","lightyellow", "palegreen", "paleturquoise", "lightcyan",	"lavender", "plum"],
				["lightgray", "pink", "bisque", "moccasin", "khaki", "lightgreen", "lightseagreen", "lightskyblue", "cornflowerblue", "violet"],
				["silver", "lightcoral", "sandybrown", "orange", "palegoldenrod", "chartreuse", "mediumturquoise", 	"skyblue", "mediumslateblue","orchid"],
				["gray", "red", "orangered", "darkorange", "yellow", "limegreen", 	"darkseagreen", "royalblue", "slateblue", "mediumorchid"],
				["dimgray", "crimson", 	"chocolate", "coral", "gold", "forestgreen", "seagreen", "blue", "blueviolet", "darkorchid"],
				["darkslategray","firebrick","saddlebrown", "sienna", "olive", "green", "darkcyan", "mediumblue","darkslateblue", "darkmagenta" ],
				["black", "darkred", "maroon", "brown", "darkolivegreen", "darkgreen", "midnightblue", "navy", "indigo", 	"purple"]],
				
		"3x4": [["white", "lime", "green", "blue"],
			["silver", "yellow", "fuchsia", "navy"],
			["gray", "red", "purple", "black"]]	
			
	},

	// _imagePaths: Map
	//		This is stores the path to the palette images
	_imagePaths: {
		"7x10": dojo.moduleUrl("dijit", "templates/colors7x10.png"),
		"3x4": dojo.moduleUrl("dijit", "templates/colors3x4.png")
	},

	// _paletteCoords: Map
	//		This is a map that is used to calculate the coordinates of the
	//		images that make up the palette.
	_paletteCoords: {
		"leftOffset": 3, "topOffset": 3,
		"cWidth": 18, "cHeight": 16
	},

	// templatePath: String
	//		Path to the template of this widget.
	templateString:"<fieldset class=\"dijitInlineBox\">\n\t<div style=\"overflow: hidden\" dojoAttachPoint=\"divNode\" waiRole=\"grid\" tabIndex=\"-1\">\n\t\t<img style=\"border-style: none;\" dojoAttachPoint=\"imageNode\" tabIndex=\"-1\" />\n\t</div>\t\n</fieldset>\n",


	_paletteDims: {
		"7x10": {"width": "185px", "height": "117px"},
		"3x4": {"width": "77px", "height": "53px"}
	},


	postCreate: function(){
		// A name has to be given to the colorMap, this needs to be unique per Palette.
		dojo.mixin(this.divNode.style, this._paletteDims[this.palette]);
		this.imageNode.setAttribute("src", this._imagePaths[this.palette]);
		var choices = this._palettes[this.palette];	
		this.domNode.style.position = "relative";
		this._highlightNodes = [];	
		this.colorNames = dojo.i18n.getLocalization("dojo", "colors", this.lang);

		for(var row=0; row < choices.length; row++){
			for(var col=0; col < choices[row].length; col++){
				var highlightNode = document.createElement("img");
				highlightNode.src = dojo.moduleUrl("dijit", "templates/blank.gif");
				dojo.addClass(highlightNode, "dijitPaletteImg");
				var color = choices[row][col];
				var colorValue = new dojo.Color(dojo.Color.named[color]);
				highlightNode.alt = this.colorNames[color];
				highlightNode.color = colorValue.toHex();
				var highlightStyle = highlightNode.style;
				highlightStyle.color = highlightStyle.backgroundColor = highlightNode.color;
				dojo.forEach(["Dijitclick", "MouseOut", "MouseOver", "Blur", "Focus"], function(handler){
					this.connect(highlightNode, "on"+handler.toLowerCase(), "_onColor"+handler);
				}, this);
				this.divNode.appendChild(highlightNode);
				var coords = this._paletteCoords;
				highlightStyle.top = coords.topOffset + (row * coords.cHeight) + "px";
				highlightStyle.left = coords.leftOffset + (col * coords.cWidth) + "px";
				highlightNode.setAttribute("tabIndex","-1");
				highlightNode.title = this.colorNames[color];
				dijit.wai.setAttr(highlightNode, "waiRole", "role", "gridcell");
				highlightNode.index = this._highlightNodes.length;
				this._highlightNodes.push(highlightNode);
			}
		}
		this._highlightNodes[this._currentFocus].tabIndex = 0;
		this._xDim = choices[0].length;
		this._yDim = choices.length;

		// Now set all events
		// The palette itself is navigated to with the tab key on the keyboard
		// Keyboard navigation within the Palette is with the arrow keys
		// Spacebar selects the color.
		// For the up key the index is changed by negative the x dimension.		

		var keyIncrementMap = {
			UP_ARROW: -this._xDim,
			// The down key the index is increase by the x dimension.	
			DOWN_ARROW: this._xDim,
			// Right and left move the index by 1.
			RIGHT_ARROW: 1,
			LEFT_ARROW: -1
		};
		for(var key in keyIncrementMap){
			dijit.typematic.addKeyListener(this.domNode,
				{keyCode:dojo.keys[key], ctrlKey:false, altKey:false, shiftKey:false},
				this,
				function(){
					var increment = keyIncrementMap[key];
					return function(count){ this._navigateByKey(increment, count); };
				}(),
				this.timeoutChangeRate, this.defaultTimeout);
		}
	},

	focus: function(){
		// summary:
		//		Focus this ColorPalette.
		dijit.focus(this._highlightNodes[this._currentFocus]);
	},

	onChange: function(color){
		// summary:
		//		Callback when a color is selected.
		// color: String
		//		Hex value corresponding to color.
		console.debug("Color selected is: "+color);
	},

	_onColorDijitclick: function(/*Event*/ evt){
		// summary:
		//		Handler for click, enter key & space key. Selects the color.
		// evt:
		//		The event.
		var target = evt.currentTarget;
		if (this._currentFocus != target.index){
			this._currentFocus = target.index;
			dijit.focus(target);
		}
		this._selectColor(target);
		dojo.stopEvent(evt);
	},

	_onColorMouseOut: function(/*Event*/ evt){
		// summary:
		//		Handler for onMouseOut. Removes highlight.
		// evt:
		//		The mouse event.
		dojo.removeClass(evt.currentTarget, "dijitPaletteImgHighlight");
	},

	_onColorMouseOver: function(/*Event*/ evt){
		// summary:
		//		Handler for onMouseOver. Highlights the color.
		// evt:
		//		The mouse event.
		var target = evt.currentTarget;
		target.tabIndex = 0;
		target.focus();
	},

	_onColorBlur: function(/*Event*/ evt){
		// summary:
		//		Handler for onBlur. Removes highlight and sets
		//		the first color as the palette's tab point.
		// evt:
		//		The blur event.
		dojo.removeClass(evt.currentTarget, "dijitPaletteImgHighlight");
		evt.currentTarget.tabIndex = -1;
		this._currentFocus = 0;
		this._highlightNodes[0].tabIndex = 0;
	},

	_onColorFocus: function(/*Event*/ evt){
		// summary:
		//		Handler for onFocus. Highlights the color.
		// evt:
		//		The focus event.
		if(this._currentFocus != evt.currentTarget.index){
			this._highlightNodes[this._currentFocus].tabIndex = -1;
		}
		this._currentFocus = evt.currentTarget.index;
		dojo.addClass(evt.currentTarget, "dijitPaletteImgHighlight");

	},

	_selectColor: function(selectNode){	
		// summary:
		// 		This selects a color. It triggers the onChange event
		// area:
		//		The area node that covers the color being selected.
		this.value = selectNode.color;
		this.onChange(selectNode.color);
	},

	_navigateByKey: function(increment, typeCount){
		// summary:we
		// 	  	This is the callback for typematic.
		// 		It changes the focus and the highlighed color.
		// increment:
		// 		How much the key is navigated.
		//	typeCount:
		//		How many times typematic has fired.

		// typecount == -1 means the key is released.
		if(typeCount == -1){ return; }

		var newFocusIndex = this._currentFocus + increment;
		if(newFocusIndex < this._highlightNodes.length && newFocusIndex > -1)
		{
			var focusNode = this._highlightNodes[newFocusIndex];
			focusNode.tabIndex = 0;
			focusNode.focus();
		}
	}
});

}
