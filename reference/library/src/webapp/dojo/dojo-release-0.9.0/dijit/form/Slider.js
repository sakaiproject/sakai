if(!dojo._hasResource["dijit.form.Slider"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.Slider"] = true;
dojo.provide("dijit.form.Slider");

dojo.require("dijit.form._FormWidget");
dojo.require("dijit._Container");
dojo.require("dojo.dnd.move");
dojo.require("dijit.form.Button");

dojo.declare(
	"dijit.form.HorizontalSlider",
	[dijit.form._FormWidget, dijit._Container],
{
	// summary
	//	A form widget that allows one to select a value with a horizontally draggable image

	templateString:"<table class=\"dijit dijitReset dijitSlider\" cellspacing=0 cellpadding=0 border=0 rules=none id=\"${id}\"\n\t><tr class=\"dijitReset\"\n\t\t><td class=\"dijitReset\" colspan=2></td\n\t\t><td dojoAttachPoint=\"containerNode,topDecoration\" class=\"dijitReset\" style=\"text-align:center;width:100%;\"></td\n\t\t><td class=\"dijitReset\" colspan=2></td\n\t></tr\n\t><tr class=\"dijitReset\"\n\t\t><td class=\"dijitReset dijitSliderButtonContainer dijitHorizontalSliderButtonContainer\"\n\t\t\t><button dojoType=\"dijit.form.Button\" tabIndex=\"-1\" alt=\"-\" style=\"display:none\" dojoAttachPoint=\"decrementButton\" dojoAttachEvent=\"onClick: decrement\">-</button\n\t\t></td\n\t\t><td class=\"dijitReset\"\n\t\t\t><div class=\"dijitSliderBar dijitSliderBumper dijitHorizontalSliderBumper dijitSliderLeftBumper dijitHorizontalSliderLeftBumper\"></div\n\t\t></td\n\t\t><td class=\"dijitReset\"\n\t\t\t><input dojoAttachPoint=\"valueNode\" name=\"${name}\" type=\"hidden\"\n\t\t\t><div style=\"position:relative;\" dojoAttachPoint=\"sliderBarContainer\"\n\t\t\t\t><div dojoAttachPoint=\"progressBar\" class=\"dijitSliderBar dijitHorizontalSliderBar dijitSliderProgressBar dijitHorizontalSliderProgressBar\" dojoAttachEvent=\"onclick:_onBarClick\"\n\t\t\t\t\t><div tabIndex=\"${tabIndex}\" dojoAttachPoint=\"sliderHandle,focusNode\" class=\"dijitSliderMoveable dijitHorizontalSliderMoveable\" dojoAttachEvent=\"onkeypress:_onKeyPress,onclick:_onHandleClick\" waiRole=\"slider\" valuemin=\"${minimum}\" valuemax=\"${maximum}\"\n\t\t\t\t\t\t><div class=\"dijitSliderImageHandle dijitHorizontalSliderImageHandle\"></div\n\t\t\t\t\t></div\n\t\t\t\t></div\n\t\t\t\t><div dojoAttachPoint=\"remainingBar\" class=\"dijitSliderBar dijitHorizontalSliderBar dijitSliderRemainingBar dijitHorizontalSliderRemainingBar\" dojoAttachEvent=\"onclick:_onBarClick\"></div\n\t\t\t></div\n\t\t></td\n\t\t><td class=\"dijitReset\"\n\t\t\t><div class=\"dijitSliderBar dijitSliderBumper dijitHorizontalSliderBumper dijitSliderRightBumper dijitHorizontalSliderRightBumper\"></div\n\t\t></td\n\t\t><td class=\"dijitReset dijitSliderButtonContainer dijitHorizontalSliderButtonContainer\" style=\"right:0px;\"\n\t\t\t><button dojoType=\"dijit.form.Button\" tabIndex=\"-1\" alt=\"+\" style=\"display:none\" dojoAttachPoint=\"incrementButton\" dojoAttachEvent=\"onClick: increment\">+</button\n\t\t></td\n\t></tr\n\t><tr class=\"dijitReset\"\n\t\t><td class=\"dijitReset\" colspan=2></td\n\t\t><td dojoAttachPoint=\"containerNode,bottomDecoration\" class=\"dijitReset\" style=\"text-align:center;\"></td\n\t\t><td class=\"dijitReset\" colspan=2></td\n\t></tr\n></table>\n",
	value: 0,

	// showButtons: boolean
	//	Show increment/decrement buttons at the ends of the slider?
	showButtons: true,

	// minimum:: integer
	//	The minimum value allowed.
	minimum: 0,
	
	// maximum: integer
	//	The maximum allowed value.
	maximum: 100,
	
	// discreteValues: integer
	//	The maximum allowed values dispersed evenly between minimum and maximum (inclusive).
	discreteValues: Infinity,
	
	// pageIncrement: integer
	//	The amount of change with shift+arrow
	pageIncrement: 2,
	
	// clickSelect: boolean
	//	If clicking the progress bar changes the value or not
	clickSelect: true,

	widgetsInTemplate: true,

	_mousePixelCoord: "pageX",
	_pixelCount: "w",
	_startingPixelCoord: "x",
	_startingPixelCount: "l",
	_handleOffsetCoord: "left",
	_progressPixelSize: "width",
	_upsideDown: false,

	 setDisabled: function(/*Boolean*/ disabled){
		if(this.showButtons){
			this.incrementButton.disabled = disabled;
			this.decrementButton.disabled = disabled;
		}
		dijit.form.HorizontalSlider.superclass.setDisabled.apply(this, arguments); 
	 },

	_onKeyPress: function(/*Event*/ e){
		if(this.disabled || e.altKey || e.ctrlKey){ return; }
		switch(e.keyCode){
			case dojo.keys.HOME:
				this.setValue(this.minimum);
				break;
			case dojo.keys.END:
				this.setValue(this.maximum);
				break;
			case dojo.keys.UP_ARROW:
			case dojo.keys.RIGHT_ARROW:
			case dojo.keys.PAGE_UP:
				this.increment(e);
				break;
			case dojo.keys.DOWN_ARROW:
			case dojo.keys.LEFT_ARROW:
			case dojo.keys.PAGE_DOWN:
				this.decrement(e);
				break;
			default:
				this.inherited("_onKeyPress", arguments);
				return;
		}
		dojo.stopEvent(e);
	},

	_onHandleClick: function(e){
		if(this.disabled){ return; }
		dijit.focus(this.sliderHandle);
		dojo.stopEvent(e);
	},

	_onBarClick: function(e){
		if(this.disabled || !this.clickSelect){ return; }
		dojo.stopEvent(e);
		var abspos = dojo.coords(this.sliderBarContainer, true);
		var pixelValue = e[this._mousePixelCoord] - abspos[this._startingPixelCoord];
		this._setPixelValue(this._upsideDown ? (abspos[this._pixelCount] - pixelValue) : pixelValue, abspos[this._pixelCount], true);
	},

	_setPixelValue: function(/*Number*/ pixelValue, /*Number*/ maxPixels, /*Boolean, optional*/ priorityChange){
		pixelValue = pixelValue < 0 ? 0 : maxPixels < pixelValue ? maxPixels : pixelValue;
		var count = this.discreteValues;
		if(count > maxPixels || count <= 1){ count = maxPixels; }
		count--;
		var pixelsPerValue = maxPixels / count;
		var wholeIncrements = Math.round(pixelValue / pixelsPerValue);
		this.setValue((this.maximum-this.minimum)*wholeIncrements/count + this.minimum, priorityChange);
	},

	setValue: function(/*Number*/ value, /*Boolean, optional*/ priorityChange){
		this.valueNode.value = this.value = value;
		this.inherited('setValue', arguments);
		var percent = (value - this.minimum) / (this.maximum - this.minimum);
		this.progressBar.style[this._progressPixelSize] = (percent*100) + "%";
		this.remainingBar.style[this._progressPixelSize] = ((1-percent)*100) + "%";
	},

	_bumpValue: function(signedChange){
		var s = dojo.getComputedStyle(this.sliderBarContainer);
		var c = dojo._getContentBox(this.sliderBarContainer, s);
		var count = this.discreteValues;
		if(count > c[this._pixelCount] || count <= 1){ count = c[this._pixelCount]; }
		count--;
		var value = (this.value - this.minimum) * count / (this.maximum - this.minimum) + signedChange;
		if(value < 0){ value = 0; }
		if(value > count){ value = count; }
		value = value * (this.maximum - this.minimum) / count + this.minimum;
		this.setValue(value);
	},

	decrement: function(e){
		// summary
		//	decrement slider by 1 unit
		this._bumpValue(e.keyCode == dojo.keys.PAGE_DOWN?-this.pageIncrement:-1);
	},

	increment: function(e){
		// summary
		//	increment slider by 1 unit
		this._bumpValue(e.keyCode == dojo.keys.PAGE_UP?this.pageIncrement:1);
	},

	_mouseWheeled: function(/*Event*/ evt){
		dojo.stopEvent(evt);
		var scrollAmount = 0;
		if(typeof evt.wheelDelta == 'number'){ // IE
			scrollAmount = evt.wheelDelta;
		}else if(typeof evt.detail == 'number'){ // Mozilla+Firefox
			scrollAmount = -evt.detail;
		}
		if(scrollAmount > 0){
			this.increment(evt);
		}else if(scrollAmount < 0){
			this.decrement(evt);
		}
	},

	startup: function(){
		dojo.forEach(this.getChildren(), function(child){
			if(this[child.container] != this.containerNode){
				this[child.container].appendChild(child.domNode);
			}
		}, this);
	},

	_onBlur: function(){
		dijit.form.HorizontalSlider.superclass.setValue.call(this, this.value, true);
	},

	postCreate: function(){
		if(this.showButtons){
			this.incrementButton.domNode.style.display="";
			this.decrementButton.domNode.style.display="";
		}
		this.connect(this.domNode, dojo.isIE ? "onmousewheel" : 'DOMMouseScroll', "_mouseWheeled");

		// define a custom constructor for a SliderMover that points back to me
		var _self = this;
		var mover = function(node, e){
			dijit.form._SliderMover.call(this, node, e);
			this.widget = _self;
		};
		dojo.extend(mover, dijit.form._SliderMover.prototype);
		

		this._movable = new dojo.dnd.Moveable(this.sliderHandle, {mover: mover});
		this.inherited('postCreate', arguments);
	},
	
	destroy: function(){
		this._movable.destroy();
		dijit.form.HorizontalSlider.superclass.destroy.apply(this, arguments);	
	}
});

dojo.declare(
	"dijit.form.VerticalSlider",
	dijit.form.HorizontalSlider,
{
	// summary
	//	A form widget that allows one to select a value with a vertically draggable image

	templateString:"<table class=\"dijitReset dijitSlider\" cellspacing=0 cellpadding=0 border=0 rules=none id=\"${id}\"\n><tbody class=\"dijitReset\"\n\t><tr class=\"dijitReset\"\n\t\t><td class=\"dijitReset\"></td\n\t\t><td class=\"dijitReset dijitSliderButtonContainer dijitVerticalSliderButtonContainer\"\n\t\t\t><button dojoType=\"dijit.form.Button\" tabIndex=-1 alt=\"+\"  style=\"display:none\" dojoAttachPoint=\"incrementButton\" dojoAttachEvent=\"onClick: increment\">+</button\n\t\t></td\n\t\t><td class=\"dijitReset\"></td\n\t></tr\n\t><tr class=\"dijitReset\"\n\t\t><td class=\"dijitReset\"></td\n\t\t><td class=\"dijitReset\"\n\t\t\t><center><div class=\"dijitSliderBar dijitSliderBumper dijitVerticalSliderBumper dijitSliderTopBumper dijitVerticalSliderTopBumper\"></div></center\n\t\t></td\n\t\t><td class=\"dijitReset\"></td\n\t></tr\n\t><tr class=\"dijitReset\"\n\t\t><td dojoAttachPoint=\"leftDecoration\" class=\"dijitReset\" style=\"text-align:center;height:100%;\"></td\n\t\t><td class=\"dijitReset\" style=\"height:100%;\"\n\t\t\t><input dojoAttachPoint=\"valueNode\" type=\"hidden\" name=\"${name}\"\n\t\t\t><center style=\"position:relative;height:100%;\" dojoAttachPoint=\"sliderBarContainer\"\n\t\t\t\t><div dojoAttachPoint=\"remainingBar\" class=\"dijitSliderBar dijitVerticalSliderBar dijitSliderRemainingBar dijitVerticalSliderRemainingBar\" dojoAttachEvent=\"onclick:_onBarClick\"></div\n\t\t\t\t><div dojoAttachPoint=\"progressBar\" class=\"dijitSliderBar dijitVerticalSliderBar dijitSliderProgressBar dijitVerticalSliderProgressBar\" dojoAttachEvent=\"onclick:_onBarClick\"\n\t\t\t\t\t><div tabIndex=\"${tabIndex}\" dojoAttachPoint=\"sliderHandle,focusNode\" class=\"dijitSliderMoveable\" dojoAttachEvent=\"onkeypress:_onKeyPress,onclick:_onHandleClick\" style=\"vertical-align:top;\" waiRole=\"slider\" valuemin=\"${minimum}\" valuemax=\"${maximum}\"\n\t\t\t\t\t\t><div class=\"dijitSliderImageHandle dijitVerticalSliderImageHandle\"></div\n\t\t\t\t\t></div\n\t\t\t\t></div\n\t\t\t></center\n\t\t></td\n\t\t><td dojoAttachPoint=\"containerNode,rightDecoration\" class=\"dijitReset\" style=\"text-align:center;height:100%;\"></td\n\t></tr\n\t><tr class=\"dijitReset\"\n\t\t><td class=\"dijitReset\"></td\n\t\t><td class=\"dijitReset\"\n\t\t\t><center><div class=\"dijitSliderBar dijitSliderBumper dijitVerticalSliderBumper dijitSliderBottomBumper dijitVerticalSliderBottomBumper\"></div></center\n\t\t></td\n\t\t><td class=\"dijitReset\"></td\n\t></tr\n\t><tr class=\"dijitReset\"\n\t\t><td class=\"dijitReset\"></td\n\t\t><td class=\"dijitReset dijitSliderButtonContainer dijitVerticalSliderButtonContainer\"\n\t\t\t><button dojoType=\"dijit.form.Button\" tabIndex=-1 alt=\"-\" style=\"display:none\" dojoAttachPoint=\"decrementButton\" dojoAttachEvent=\"onClick: decrement\">-</button\n\t\t></td\n\t\t><td class=\"dijitReset\"></td\n\t></tr\n></tbody></table>\n",
	_mousePixelCoord: "pageY",
	_pixelCount: "h",
	_startingPixelCoord: "y",
	_startingPixelCount: "t",
	_handleOffsetCoord: "top",
	_progressPixelSize: "height",
	_upsideDown: true
});

dojo.declare("dijit.form._SliderMover",
	dojo.dnd.Mover,
{
	onMouseMove: function(e){
		var widget = this.widget;
		var c = this.constraintBox;
		if(!c){
			var container = widget.sliderBarContainer;
			var s = dojo.getComputedStyle(container);
			var c = dojo._getContentBox(container, s);
			c[widget._startingPixelCount] = 0;
			this.constraintBox = c;
		}
		var m = this.marginBox;
		var pixelValue = m[widget._startingPixelCount] + e[widget._mousePixelCoord];
		dojo.hitch(widget, "_setPixelValue")(widget._upsideDown? (c[widget._pixelCount]-pixelValue) : pixelValue, c[widget._pixelCount]);
	},

	destroy: function(e){
		var widget = this.widget;
		widget.setValue(widget.value, true);
		dojo.dnd.Mover.prototype.destroy.call(this);
	}
});


dojo.declare("dijit.form.HorizontalRule", [dijit._Widget, dijit._Templated],
{
	//	Summary:
	//		Create hash marks for the Horizontal slider
	templateString: '<div class="RuleContainer HorizontalRuleContainer"></div>',

	// count: Integer
	//      Number of hash marks to generate
	count: 3,

	// container: Node
	//      If this is a child widget, connect it to this parent node 
	container: "containerNode",

	// ruleStyle: String
	//      CSS style to apply to individual hash marks
	ruleStyle: "",

	_positionPrefix: '<div class="RuleMark HorizontalRuleMark" style="left:',
	_positionSuffix: '%;',
	_suffix: '"></div>',

	_genHTML: function(pos, ndx){
		return this._positionPrefix + pos + this._positionSuffix + this.ruleStyle + this._suffix;
	},

	postCreate: function(){
		if(this.count==1){
			var innerHTML = this._genHTML(50, 0);
		}else{
			var innerHTML = this._genHTML(0, 0);
			var interval = 100 / (this.count-1);
			for(var i=1; i < this.count-1; i++){
				innerHTML += this._genHTML(interval*i, i);
			}
			innerHTML += this._genHTML(100, this.count-1);
		}
		this.domNode.innerHTML = innerHTML;
	}
});

dojo.declare("dijit.form.VerticalRule", dijit.form.HorizontalRule,
{
	//	Summary:
	//		Create hash marks for the Vertical slider
	templateString: '<div class="RuleContainer VerticalRuleContainer"></div>',
	_positionPrefix: '<div class="RuleMark VerticalRuleMark" style="top:'
});

dojo.declare("dijit.form.HorizontalRuleLabels", dijit.form.HorizontalRule,
{
	//	Summary:
	//		Create labels for the Horizontal slider
	templateString: '<div class="RuleContainer HorizontalRuleContainer"></div>',

	// labelStyle: String
	//      CSS style to apply to individual text labels
	labelStyle: "",

	// labels: Array
	//	Array of text labels to render - evenly spaced from left-to-right or bottom-to-top
	labels: [],

	_positionPrefix: '<div class="RuleLabelContainer HorizontalRuleLabelContainer" style="left:',
	_labelPrefix: '"><span class="RuleLabel HorizontalRuleLabel">',
	_suffix: '</span></div>',

	_calcPosition: function(pos){
		return pos;
	},

	_genHTML: function(pos, ndx){
		return this._positionPrefix + this._calcPosition(pos) + this._positionSuffix + this.labelStyle + this._labelPrefix + this.labels[ndx] + this._suffix;
	},

	postMixInProperties: function(){
		dijit.form.HorizontalRuleLabels.superclass.postMixInProperties.apply(this);
		if(!this.labels.length){
			// for markup creation, labels are specified as child elements
			this.labels = dojo.query("> li", this.srcNodeRef).map(function(node){
				return String(node.innerHTML);
			});
		}
		this.srcNodeRef.innerHTML="";
	},

	postCreate: function(){
		this.count = this.labels.length;
		dijit.form.HorizontalRuleLabels.superclass.postCreate.apply(this);
	}
});

dojo.declare("dijit.form.VerticalRuleLabels", dijit.form.HorizontalRuleLabels,
{
	//	Summary:
	//		Create labels for the Vertical slider
	templateString: '<div class="RuleContainer VerticalRuleContainer"></div>',

	_positionPrefix: '<div class="RuleLabelContainer VerticalRuleLabelContainer" style="top:',
	_labelPrefix: '"><span class="RuleLabel VerticalRuleLabel">',

	_calcPosition: function(pos){
		return 100-pos;
	}
});

}
