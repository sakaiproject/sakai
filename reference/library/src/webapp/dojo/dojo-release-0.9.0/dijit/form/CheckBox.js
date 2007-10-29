if(!dojo._hasResource["dijit.form.CheckBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.CheckBox"] = true;
dojo.provide("dijit.form.CheckBox");

dojo.require("dijit.form.Button");

dojo.declare(
	"dijit.form.CheckBox",
	dijit.form.ToggleButton,
	{
		// summary:
		// 		Same as an HTML checkbox, but with fancy styling.
		//
		// description:
		// User interacts with real html inputs.
		// On onclick (which occurs by mouse click, space-bar, or
		// using the arrow keys to switch the selected radio button),
		// we update the state of the checkbox/radio.
		//
		// There are two modes:
		//   1. High contrast mode
		//   2. Normal mode
		// In case 1, the regular html inputs are shown and used by the user.
		// In case 2, the regular html inputs are invisible but still used by
		// the user. They are turned quasi-invisible and overlay the background-image.

		templateString:"<span class=\"${baseClass}\" baseClass=\"${baseClass}\"\n\t><input\n\t \tid=\"${id}\" tabIndex=\"${tabIndex}\" type=\"${_type}\" name=\"${name}\" value=\"${value}\"\n\t\tclass=\"dijitCheckBoxInput\"\n\t\tdojoAttachPoint=\"inputNode,focusNode\"\n\t \tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onclick:onClick\"\n></span>\n",

		baseClass: "dijitCheckBox",

		//	Value of "type" attribute for <input>
		_type: "checkbox",

		// value: Value
		//	equivalent to value field on normal checkbox (if checked, the value is passed as
		//	the value when form is submitted)
		value: "on",

		postCreate: function(){
			dojo.setSelectable(this.inputNode, false);
			this.setChecked(this.checked);
			dijit.form.ToggleButton.prototype.postCreate.apply(this, arguments);
		},

		setChecked: function(/*Boolean*/ checked){
			this.checked = checked;
			if(dojo.isIE){
				if(checked){ this.inputNode.setAttribute('checked', 'checked'); }
				else{ this.inputNode.removeAttribute('checked'); }
			}else{ this.inputNode.checked = checked; }
			dijit.form.ToggleButton.prototype.setChecked.apply(this, arguments);
		},

		setValue: function(/*String*/ value){
			if(value == null){ value = ""; }
			this.inputNode.value = value;
			dijit.form.CheckBox.superclass.setValue.call(this,value);
		}
	}
);

dojo.declare(
	"dijit.form.RadioButton",
	dijit.form.CheckBox,
	{
		// summary:
		// 		Same as an HTML radio, but with fancy styling.
		//
		// description:
		// Implementation details
		//
		// Specialization:
		// We keep track of dijit radio groups so that we can update the state
		// of all the siblings (the "context") in a group based on input
		// events. We don't rely on browser radio grouping.

		_type: "radio",
		baseClass: "dijitRadio",

		// This shared object keeps track of all widgets, grouped by name
		_groups: {},

		postCreate: function(){
			// add this widget to _groups
			(this._groups[this.name] = this._groups[this.name] || []).push(this);

			dijit.form.CheckBox.prototype.postCreate.apply(this, arguments);
		},

		uninitialize: function(){
			// remove this widget from _groups
			dojo.forEach(this._groups[this.name], function(widget, i, arr){
				if(widget === this){
					arr.splice(i, 1);
					return;
				}
			}, this);
		},

		setChecked: function(/*Boolean*/ checked){
			// If I am being checked then have to deselect currently checked radio button
			if(checked){
				dojo.forEach(this._groups[this.name], function(widget){
					if(widget != this && widget.checked){
						widget.setChecked(false);
					}
				}, this);
			}
			dijit.form.CheckBox.prototype.setChecked.apply(this, arguments);			
		},

		onClick: function(/*Event*/ e){
			if(!this.checked){
				this.setChecked(true);
			}
		}
	}
);

}
