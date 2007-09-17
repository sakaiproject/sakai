if(!dojo._hasResource["dijit.form._DropDownTextBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form._DropDownTextBox"] = true;
dojo.provide("dijit.form._DropDownTextBox");

dojo.declare(
	"dijit.form._DropDownTextBox",
	null,
	{
		// summary:
		//		Mixin text box with drop down

		templateString:"<table class=\"dijit dijitReset dijitInline dijitLeft\" baseClass=\"${baseClass}\" cellspacing=\"0\" cellpadding=\"0\"\n\tid=\"widget_${id}\" name=\"${name}\" dojoAttachEvent=\"onmouseover:_onMouse,onmouseout:_onMouse\" waiRole=\"presentation\"\n\t><tr\n\t\t><td class='dijitReset dijitStretch dijitComboBoxInput'\n\t\t\t><input class='XdijitInputField' type=\"text\" autocomplete=\"off\" name=\"${name}\"\n\t\t\tdojoAttachEvent=\"onkeypress, onkeyup, onfocus, onblur, compositionend\"\n\t\t\tdojoAttachPoint=\"textbox,focusNode\" id='${id}'\n\t\t\ttabIndex='${tabIndex}' size='${size}' maxlength='${maxlength}'\n\t\t\twaiRole=\"combobox\"\n\t\t></td\n\t\t><td class='dijitReset dijitRight dijitButtonNode dijitDownArrowButton'\n\t\t\tdojoAttachPoint=\"downArrowNode\"\n\t\t\tdojoAttachEvent=\"ondijitclick:_onArrowClick,onmousedown:_onMouse,onmouseup:_onMouse,onmouseover:_onMouse,onmouseout:_onMouse\"\n\t\t><div class=\"dijitDownArrowButtonInner\" waiRole=\"presentation\" tabIndex=\"-1\">\n\t\t\t<div class=\"dijit_a11y dijitDownArrowButtonChar\">&#9660;</div>\n\t\t</div>\n\t</td></tr>\n</table>\n",
		
		baseClass:"dijitComboBox",

		// hasDownArrow: Boolean
		// Set this textbox to have a down arrow button
		// Defaults to true
		hasDownArrow:true,

		// _popupWidget: Widget
		//	link to the popup widget created by makePopop
		_popupWidget:null,

		// _hasMasterPopup: Boolean
		//	Flag that determines if this widget should share one popup per widget prototype,
		//	or create one popup per widget instance.
		//	If true, then makePopup() creates one popup per widget prototype.
		//	If false, then makePopup() creates one popup per widget instance.
		_hasMasterPopup:false,

		// _popupClass: String
		//	Class of master popup (dijit.form._ComboBoxMenu)
		_popupClass:"",

		// _popupArgs: Object
		//	Object to pass to popup widget on initialization
		_popupArgs:{},
		
		// _hasFocus: Boolean
		// Represents focus state of the textbox
		_hasFocus:false,

		_arrowPressed: function(){
			if(!this.disabled&&this.hasDownArrow){
				dojo.addClass(this.downArrowNode, "dijitArrowButtonActive");
			}
		},

		_arrowIdle: function(){
			if(!this.disabled&&this.hasDownArrow){
				dojo.removeClass(this.downArrowNode, "dojoArrowButtonPushed");
			}
		},

		makePopup: function(){
			// summary:
			//	create popup widget on demand
			var _this=this;
			function _createNewPopup(){
				// common code from makePopup
				var node=document.createElement("div");
				document.body.appendChild(node);
				var popupProto=dojo.getObject(_this._popupClass, false);
				return new popupProto(_this._popupArgs, node);
			}
			// this code only runs if there is no popup reference
			if(!this._popupWidget){
				// does this widget have one "master" popup?
				if(this._hasMasterPopup){
					// does the master popup not exist yet?
					var parentClass = dojo.getObject(this.declaredClass, false);
					if(!parentClass.prototype._popupWidget){
						// create the master popup for the first time
						parentClass.prototype._popupWidget=_createNewPopup();
					}
					// assign master popup to local link
					this._popupWidget=parentClass.prototype._popupWidget;
				}else{
					// if master popup is not being used, create one popup per widget instance
					this._popupWidget=_createNewPopup();
				}
			}
		},

		_onArrowClick: function(){
			// summary: callback when arrow is clicked
			if(this.disabled){
				return;
			}
			this.focus();
			this.makePopup();
			if(this._isShowingNow){
				this._hideResultList();
			}else{
				// forces full population of results, if they click
				// on the arrow it means they want to see more options
				this._openResultList();
			}
		},

		_hideResultList: function(){
			if(this._isShowingNow){
				dijit.popup.close();
				this._arrowIdle();
				this._isShowingNow=false;
			}
		},

		_openResultList:function(){
			// summary:
			//	any code that needs to happen before the popup appears.
			//	creating the popupWidget contents etc.
			this._showResultList();
		},

		onfocus:function(){
			this._hasFocus=true;
		},

		onblur:function(){
			this._arrowIdle();
			this._hasFocus=false;
			// removeClass dijitInputFieldFocused
			dojo.removeClass(this.nodeWithBorder, "dijitInputFieldFocused");
			// hide the Tooltip
			this.validate(false);
		},

		onkeypress: function(/*Event*/ evt){
			// summary: generic handler for popup keyboard events
			if(evt.ctrlKey || evt.altKey){
				return;
			}
			switch(evt.keyCode){
				case dojo.keys.PAGE_DOWN:
				case dojo.keys.DOWN_ARROW:
					if(!this._isShowingNow||this._prev_key_esc){
						this.makePopup();
						this._arrowPressed();
						this._openResultList();
					}
					dojo.stopEvent(evt);
					this._prev_key_backspace = false;
					this._prev_key_esc = false;
					break;

				case dojo.keys.PAGE_UP:
				case dojo.keys.UP_ARROW:
				case dojo.keys.ENTER:
					// prevent default actions
					dojo.stopEvent(evt);
					// fall through
				case dojo.keys.ESCAPE:
				case dojo.keys.TAB:
					if(this._isShowingNow){
						this._prev_key_backspace = false;
						this._prev_key_esc = (evt.keyCode==dojo.keys.ESCAPE);
						this._hideResultList();
					}
					break;
			}
		},

		compositionend: function(/*Event*/ evt){
			// summary: When inputting characters using an input method, such as Asian
			// languages, it will generate this event instead of onKeyDown event
			this.onkeypress({charCode:-1});
		},

		_showResultList: function(){
			// Our dear friend IE doesnt take max-height so we need to calculate that on our own every time
			this._hideResultList();
			var childs = this._popupWidget.getListLength ? this._popupWidget.getItems() : [this._popupWidget.domNode];

			if(childs.length){
				var visibleCount = Math.min(childs.length,this.maxListLength);
				with(this._popupWidget.domNode.style){
					// trick to get the dimensions of the popup
					// TODO: doesn't dijit.popup.open() do this automatically?
					display="";
					width="";
					height="";
				}
				this._arrowPressed();
				// hide the tooltip
				this._displayMessage("");
				var best=this.open();
				// #3212: only set auto scroll bars if necessary
				// prevents issues with scroll bars appearing when they shouldn't when node is made wider (fractional pixels cause this)
				var popupbox=dojo.marginBox(this._popupWidget.domNode);
				this._popupWidget.domNode.style.overflow=((best.h==popupbox.h)&&(best.w==popupbox.w))?"hidden":"auto";
				dojo.marginBox(this._popupWidget.domNode, {h:best.h,w:Math.max(best.w,this.domNode.offsetWidth)});

			}
		},

		getDisplayedValue:function(){
			return this.textbox.value;
		},

		setDisplayedValue:function(/*String*/ value){
			this.textbox.value=value;
		},

		uninitialize:function(){
			if(this._popupWidget){
				this._hideResultList();
				this._popupWidget.destroy()
			};
		},

		open:function(){
			this.makePopup();
			var self=this;
			self._isShowingNow=true;
			return dijit.popup.open({
				popup: this._popupWidget,
				around: this.domNode,
				parent: this
			});
		},

		_onBlur: function(){
			// summary: called magically when focus has shifted away from this widget and it's dropdown
			this._hideResultList();
		},

		postMixInProperties:function(){
			this.baseClass=this.hasDownArrow?this.baseClass:this.baseClass+"NoArrow";
		}
	}
);

}
