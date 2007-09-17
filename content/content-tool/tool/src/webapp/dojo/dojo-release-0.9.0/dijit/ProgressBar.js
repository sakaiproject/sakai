if(!dojo._hasResource["dijit.ProgressBar"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.ProgressBar"] = true;
dojo.provide("dijit.ProgressBar");

dojo.require("dojo.fx");
dojo.require("dojo.number");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.declare("dijit.ProgressBar", [dijit._Widget, dijit._Templated], {
	// summary:
	// a progress widget
	//
	// usage:
	// <div dojoType="ProgressBar"
	//   places="0"
	//   progress="..." maximum="..."></div>

	// progress: String (Percentage or Number)
	// initial progress value.
	// with "%": percentage value, 0% <= progress <= 100%
	// or without "%": absolute value, 0 <= progress <= maximum
	progress: "0",

	// maximum: Float
	// max sample number
	maximum: 100,

	// places: Number
	// number of places to show in values; 0 by default
	places: 0,

	// indeterminate: Boolean
	// false: show progress
	// true: show that a process is underway but that the progress is unknown
	indeterminate: false,

	templateString:"<div class=\"dijitProgressBar dijitProgressBarEmpty\"\n\t><div waiRole=\"progressbar\" tabindex=\"0\" dojoAttachPoint=\"internalProgress\" class=\"dijitProgressBarFull\"\n\t\t><div class=\"dijitProgressBarTile\"></div\n\t\t><span style=\"visibility:hidden\">&nbsp;</span\n\t></div\n\t><div dojoAttachPoint=\"label\" class=\"dijitProgressBarLabel\">&nbsp;</div\n\t><img dojoAttachPoint=\"inteterminateHighContrastImage\" class=\"dijitProgressBarIndeterminateHighContrastImage\"\n\t></img\n></div>\n",

	_indeterminateHighContrastImagePath:
		dojo.moduleUrl("dijit", "themes/a11y/indeterminate_progress.gif"),

	// public functions
	postCreate: function(){
		dijit.ProgressBar.superclass.postCreate.apply(this, arguments);

		this.inteterminateHighContrastImage.setAttribute("src",
			this._indeterminateHighContrastImagePath);

		this.update();
	},

	update: function(/*Object?*/attributes){
		// summary: update progress information
		//
		// attributes: may provide progress and/or maximum properties on this parameter,
		//	see attribute specs for details.
		dojo.mixin(this, attributes||{});
		var percent = 1, classFunc;
		if(this.indeterminate){
			classFunc = "addClass";
			dijit.wai.removeAttr(this.internalProgress, "waiState", "valuenow");
		}else{
			classFunc = "removeClass";
			if(String(this.progress).indexOf("%") != -1){
				percent = Math.min(parseFloat(this.progress)/100, 1);
				this.progress = percent * this.maximum;
			}else{
				this.progress = Math.min(this.progress, this.maximum);
				percent = this.progress / this.maximum;
			}
			var text = this.report(percent);
			this.label.firstChild.nodeValue = text;
			dijit.wai.setAttr(this.internalProgress, "waiState", "valuenow", text);
		}
		dojo[classFunc](this.domNode, "dijitProgressBarIndeterminate");
		this.internalProgress.style.width = (percent * 100) + "%";
		this.onChange();
	},

	report: function(/*float*/percent){
		// Generates message to show; may be overridden by user
		return dojo.number.format(percent, {type: "percent", places: this.places, locale: this.lang});
	},

	onChange: function(){}
});

}
