if(!dojo._hasResource["dijit.form.Button"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.Button"] = true;
dojo.provide("dijit.form.Button");

dojo.require("dijit.form._FormWidget");
dojo.require("dijit._Container");

dojo.declare("dijit.form.Button", dijit.form._FormWidget, {
/*
 * usage
 *	<button dojoType="button" onClick="...">Hello world</button>
 *
 *  var button1 = new dijit.form.Button({label: "hello world", onClick: foo});
 *	dojo.body().appendChild(button1.domNode);
 */
	// summary
	//	Basically the same thing as a normal HTML button, but with special styling.

	// label: String
	//	text to display in button
	label: "",

	// showLabel: Boolean
	// whether or not to display the text label in button 
	showLabel: true,

	// iconClass: String
	//	class to apply to div in button to make it display an icon
	iconClass: "",

	type: "button",
	baseClass: "dijitButton",
	templateString:"<div class=\"dijit dijitLeft dijitInline dijitButton\" baseClass=\"${baseClass}\"\n\tdojoAttachEvent=\"onclick:_onButtonClick,onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse\"\n\t><div class='dijitRight'\n\t><button class=\"dijitStretch dijitButtonNode dijitButtonContents\" dojoAttachPoint=\"focusNode,titleNode\"\n\t\ttabIndex=\"${tabIndex}\" type=\"${type}\" id=\"${id}\" name=\"${name}\" waiRole=\"button\" waiState=\"labelledby-${id}_label\"\n\t\t><div class=\"dijitInline ${iconClass}\"></div\n\t\t><span class=\"dijitButtonText\" id=\"${id}_label\" dojoAttachPoint=\"containerNode\">${label}</span\n\t></button\n></div></div>\n",

	// TODO: set button's title to this.containerNode.innerText

	_onButtonClick: function(/*Event*/ e){
		// summary: callback when the user mouse clicks the button portion
		dojo.stopEvent(e);
		if(this.disabled){ return; }
		return this.onClick(e);
	},

	postCreate: function(){
		// summary:
		//	get label and set as title on button icon if necessary
		if (this.showLabel == false){
			var labelText = "";
			this.label = this.containerNode.innerHTML;
			labelText = dojo.trim(this.containerNode.innerText || this.containerNode.textContent);
			// set title attrib on iconNode
			this.titleNode.title=labelText;
			dojo.addClass(this.containerNode,"dijitDisplayNone");
		}
		dijit.form._FormWidget.prototype.postCreate.apply(this, arguments);
	},

	onClick: function(/*Event*/ e){
		// summary: callback for when button is clicked; user can override this function

		// for some reason type=submit buttons don't automatically submit the form; do it manually
		if(this.type=="submit"){
			for(var node=this.domNode; node; node=node.parentNode){
				var widget=dijit.byNode(node);
				if(widget && widget._onSubmit){
					widget._onSubmit(e);
					break;
				}
				if(node.tagName.toLowerCase() == "form"){
					node.submit();
					break;
				}
			}
		}
	},

	setLabel: function(/*String*/ content){
		// summary: reset the label (text) of the button; takes an HTML string
		this.containerNode.innerHTML = this.label = content;
		if(dojo.isMozilla){ // Firefox has re-render issues with tables
			var oldDisplay = dojo.getComputedStyle(this.domNode).display;
			this.domNode.style.display="none";
			var _this = this;
			setTimeout(function(){_this.domNode.style.display=oldDisplay;},1);
		}
		if (this.showLabel == false){
				this.titleNode.title=dojo.trim(this.containerNode.innerText || this.containerNode.textContent);
		}
	}		
});

/*
 * usage
 *	<button dojoType="DropDownButton" label="Hello world"><div dojotype=dijit.Menu>...</div></button>
 *
 *  var button1 = new dijit.form.DropDownButton({ label: "hi", dropDown: new dijit.Menu(...) });
 *	dojo.body().appendChild(button1);
 */
dojo.declare("dijit.form.DropDownButton", [dijit.form.Button, dijit._Container], {
	// summary
	//		push the button and a menu shows up

	baseClass : "dijitDropDownButton",

	templateString:"<div class=\"dijit dijitLeft dijitInline dijitDropDownButton\" baseClass=\"dijitDropDownButton\"\n\tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse,onclick:_onArrowClick,onkeypress:_onKey\"\n\t><div class='dijitRight'>\n\t<button tabIndex=\"${tabIndex}\" class=\"dijitStretch dijitButtonNode dijitButtonContents\" type=\"${type}\" id=\"${id}\" name=\"${name}\"\n\t\tdojoAttachPoint=\"focusNode,titleNode\" waiRole=\"button\" waiState=\"haspopup-true,labelledby-${id}_label\"\n\t\t><div class=\"dijitInline ${iconClass}\"></div\n\t\t><span class=\"dijitButtonText\" \tdojoAttachPoint=\"containerNode,popupStateNode\"\n\t\tid=\"${id}_label\">${label}</span\n\t\t><span class='dijitA11yDownArrow'>&#9660;</span>\n\t</button>\n</div></div>\n",

	_fillContent: function(){
		// my inner HTML contains both the button contents and a drop down widget, like
		// <DropDownButton>  <span>push me</span>  <Menu> ... </Menu> </DropDownButton>
		// The first node is assumed to be the button content. The widget is the popup.
		if(this.srcNodeRef){ // programatically created buttons might not define srcNodeRef
			//FIXME: figure out how to filter out the widget and use all remaining nodes as button
			//	content, not just nodes[0]
			var nodes = dojo.query("*", this.srcNodeRef);
			dijit.form.DropDownButton.superclass._fillContent.call(this, nodes[0]);

			// save pointer to srcNode so we can grab the drop down widget after it's instantiated
			this.dropDownContainer = this.srcNodeRef;
		}
	},

	startup: function(){
		// the child widget from srcNodeRef is the dropdown widget.  Insert it in the page DOM,
		// make it invisible, and store a reference to pass to the popup code.
		if(!this.dropDown){
			var dropDownNode = dojo.query("[widgetId]", this.dropDownContainer)[0];
			this.dropDown = dijit.byNode(dropDownNode);
			delete this.dropDownContainer;
		}
		dojo.body().appendChild(this.dropDown.domNode);
		this.dropDown.domNode.style.display="none";
	},

	_onArrowClick: function(/*Event*/ e){
		// summary: callback when the user mouse clicks on menu popup node
		if(this.disabled){ return; }
		this._toggleDropDown();
	},

	_onKey: function(/*Event*/ e){
		// summary: callback when the user presses a key on menu popup node
		if(this.disabled){ return; }
		if(e.keyCode == dojo.keys.DOWN_ARROW){
			if(!this.dropDown || this.dropDown.domNode.style.display=="none"){
				dojo.stopEvent(e);
				return this._toggleDropDown();
			}
		}
	},

	_onBlur: function(){
		// summary: called magically when focus has shifted away from this widget and it's dropdown
		dijit.popup.closeAll();
		// don't focus on button.  the user has explicitly focused on something else.
	},

	_toggleDropDown: function(){
		// summary: toggle the drop-down widget; if it is up, close it, if not, open it
		if(this.disabled){ return; }
		dijit.focus(this.popupStateNode);
		var dropDown = this.dropDown;
		if(!dropDown){ return false; }
		if(!dropDown.isShowingNow){
			// If there's an href, then load that first, so we don't get a flicker
			if(dropDown.href && !dropDown.isLoaded){
				var self = this;
				var handler = dojo.connect(dropDown, "onLoad", function(){
					dojo.disconnect(handler);
					self._openDropDown();
				});
				dropDown._loadCheck(true);
				return;
			}else{
				this._openDropDown();
			}
		}else{
			dijit.popup.closeAll();
			this._opened = false;
		}
	},
	
	_openDropDown: function(){
		var dropDown = this.dropDown;
		var oldWidth=dropDown.domNode.style.width;
		var self = this;

		dijit.popup.open({
			parent: this,
			popup: dropDown,
			around: this.domNode,
			orient: this.isLeftToRight() ? {'BL':'TL', 'BR':'TR', 'TL':'BL', 'TR':'BR'}
				: {'BR':'TR', 'BL':'TL', 'TR':'BR', 'TL':'BL'},
			onExecute: function(){
				dijit.popup.closeAll();
				self.focus();
			},
			onCancel: function(){
				dijit.popup.closeAll();
				self.focus();
			},
			onClose: function(){
				dropDown.domNode.style.width = oldWidth;
				self.popupStateNode.removeAttribute("popupActive");
			}
		});
		if(this.domNode.offsetWidth > dropDown.domNode.offsetWidth){
			var adjustNode = null;
			if(!this.isLeftToRight()){
				adjustNode = dropDown.domNode.parentNode; 
				var oldRight = adjustNode.offsetLeft + adjustNode.offsetWidth;
			}
			// make menu at least as wide as the button
			dojo.marginBox(dropDown.domNode, {w: this.domNode.offsetWidth});
			if(adjustNode){
				adjustNode.style.left = oldRight - this.domNode.offsetWidth + "px";
			}
		}
		this.popupStateNode.setAttribute("popupActive", "true");
		this._opened=true;
		if(dropDown.focus){
			dropDown.focus();
		}
		// TODO: set this.checked and call setStateClass(), to affect button look while drop down is shown
	}
});

/*
 * usage
 *	<button dojoType="ComboButton" onClick="..."><span>Hello world</span><div dojoType=dijit.Menu>...</div></button>
 *
 *  var button1 = new dijit.form.ComboButton({label: "hello world", onClick: foo, dropDown: "myMenu"});
 *	dojo.body().appendChild(button1.domNode);
 */
dojo.declare("dijit.form.ComboButton", dijit.form.DropDownButton, {
	// summary
	//		left side is normal button, right side displays menu
	templateString:"<table class='dijit dijitReset dijitInline dijitLeft dijitComboButton'  baseClass='dijitComboButton'\n\tid=\"${id}\" name=\"${name}\" cellspacing='0' cellpadding='0'\n\tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse\">\n\t<tr>\n\t\t<td\tclass=\"dijitStretch dijitButtonContents dijitButtonNode\"\n\t\t\ttabIndex=\"${tabIndex}\"\n\t\t\tdojoAttachEvent=\"ondijitclick:_onButtonClick\"  dojoAttachPoint=\"titleNode\"\n\t\t\twaiRole=\"button\" waiState=\"labelledby-${id}_label\">\n\t\t\t<div class=\"dijitInline ${iconClass}\"></div>\n\t\t\t<span class=\"dijitButtonText\" id=\"${id}_label\" dojoAttachPoint=\"containerNode\">${label}</span>\n\t\t</td>\n\t\t<td class='dijitReset dijitRight dijitButtonNode dijitDownArrowButton'\n\t\t\tdojoAttachPoint=\"popupStateNode,focusNode\"\n\t\t\tdojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse,onmousedown:_onMouse,ondijitclick:_onArrowClick, onkeypress:_onKey\"\n\t\t\tbaseClass=\"dijitComboButtonDownArrow\"\n\t\t\ttitle=\"${optionsTitle}\"\n\t\t\ttabIndex=\"${tabIndex}\"\n\t\t\twaiRole=\"button\" waiState=\"haspopup-true\"\n\t\t><div waiRole=\"presentation\">&#9660;</div>\n\t</td></tr>\n</table>\n",

	// optionsTitle: String
	//  text that describes the options menu (accessibility)
	optionsTitle: "",

	baseClass: "dijitComboButton"
});

dojo.declare("dijit.form.ToggleButton", dijit.form.Button, {
	// summary
	//	A button that can be in two states (checked or not).
	//	Can be base class for things like tabs or checkbox or radio buttons

	baseClass: "dijitToggleButton",

	// checked: Boolean
	//		Corresponds to the native HTML <input> element's attribute.
	//		In markup, specified as "checked='checked'" or just "checked".
	//		True if the button is depressed, or the checkbox is checked,
	//		or the radio button is selected, etc.
	checked: false,

	onClick: function(/*Event*/ evt){
		this.setChecked(!this.checked);
	},

	setChecked: function(/*Boolean*/ checked){
		// summary
		//	Programatically deselect the button
		this.checked = checked;
		this._setStateClass();
		this.onChange(checked);
	}
});

}
