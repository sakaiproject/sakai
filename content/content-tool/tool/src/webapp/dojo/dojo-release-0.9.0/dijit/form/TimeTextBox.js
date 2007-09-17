if(!dojo._hasResource["dijit.form.TimeTextBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.TimeTextBox"] = true;
dojo.provide("dijit.form.TimeTextBox");

dojo.require("dojo.date");
dojo.require("dojo.date.locale");
dojo.require("dojo.date.stamp");
dojo.require("dijit.form._TimePicker");
dojo.require("dijit.form.ValidationTextBox");

dojo.declare(
	"dijit.form.TimeTextBox",
	dijit.form.RangeBoundTextBox,
	{
		// summary:
		//		A validating, serializable, range-bound date text box.

		// constraints object: min, max
		regExpGen: dojo.date.locale.regexp,
		compare: dojo.date.compare,
		format: function(/*Date*/ value, /*Object*/ constraints){
			if(!value || value.toString() == this._invalid){ return null; }
			return dojo.date.locale.format(value, constraints);
		},
		parse: dojo.date.locale.parse,
		serialize: dojo.date.stamp.toISOString,

		value: new Date(""),	// NaN
		_invalid: (new Date("")).toString(),	// NaN

		_popupClass: "dijit.form._TimePicker",

		postMixInProperties: function(){
			dijit.form.RangeBoundTextBox.prototype.postMixInProperties.apply(this, arguments);

			var constraints = this.constraints;
			constraints.selector = 'time';
			if(typeof constraints.min == "string"){ constraints.min = dojo.date.stamp.fromISOString(constraints.min); }
 			if(typeof constraints.max == "string"){ constraints.max = dojo.date.stamp.fromISOString(constraints.max); }
		},

		_onFocus: function(/*Event*/ evt){
			// open the calendar
			this._open();
		},

		setValue: function(/*Date*/ value, /*Boolean, optional*/ priorityChange){
			// summary:
			//	Sets the date on this textbox
			this.inherited('setValue', arguments);
			if(this._picker){
				// #3948: fix blank date on popup only
				if(!value || value.toString() == this._invalid){value=new Date();}
				this._picker.setValue(value);
			}
		},

		_open: function(){
			// summary:
			//	opens the Calendar, and sets the onValueSelected for the Calendar
			var self = this;

			if(!this._picker){
				var popupProto=dojo.getObject(this._popupClass, false);
				this._picker = new popupProto({
					onValueSelected: function(value){

						self.focus(); // focus the textbox before the popup closes to avoid reopening the popup
						setTimeout(dijit.popup.close, 1); // allow focus time to take

						// this will cause InlineEditBox and other handlers to do stuff so make sure it's last
						dijit.form.TimeTextBox.superclass.setValue.call(self, value, true);
					},
					lang: this.lang,
					constraints:this.constraints,
					isDisabledDate: function(/*Date*/ date){
						// summary:
						// 	disables dates outside of the min/max of the TimeTextBox
						return self.constraints && (dojo.date.compare(self.constraints.min,date) > 0 || dojo.date.compare(self.constraints.max,date) < 0);
					}
				});
				this._picker.setValue(this.getValue() || new Date());
			}
			if(!this._opened){
				dijit.popup.open({
					parent: this,
					popup: this._picker,
					around: this.domNode,
					onClose: function(){ self._opened=false; }
				});
				this._opened=true;
			}
		},

		_onBlur: function(){
			// summary: called magically when focus has shifted away from this widget and it's dropdown
			dijit.popup.closeAll();
			this.inherited('_onBlur', arguments);
			// don't focus on <input>.  the user has explicitly focused on something else.
		},

		getDisplayedValue:function(){
			return this.textbox.value;
		},

		setDisplayedValue:function(/*String*/ value){
			this.textbox.value=value;
		}
	}
);

}
