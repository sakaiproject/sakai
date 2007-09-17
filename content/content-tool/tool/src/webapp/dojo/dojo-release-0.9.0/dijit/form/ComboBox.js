if(!dojo._hasResource["dijit.form.ComboBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.ComboBox"] = true;
dojo.provide("dijit.form.ComboBox");

dojo.require("dojo.data.ItemFileReadStore");
dojo.require("dijit.form._DropDownTextBox");
dojo.require("dijit.form.ValidationTextBox");
dojo.requireLocalization("dijit.form", "ComboBox", null, "ROOT");

dojo.declare(
	"dijit.form.ComboBoxMixin",
	dijit.form._DropDownTextBox,
	{
		// summary:
		//		Auto-completing text box, and base class for FilteringSelect widget.
		//
		//		The drop down box's values are populated from an class called
		//		a data provider, which returns a list of values based on the characters
		//		that the user has typed into the input box.
		//
		//		Some of the options to the ComboBox are actually arguments to the data
		//		provider.

		// pageSize: Integer
		//		Argument to data provider.
		//		Specifies number of search results per page (before hitting "next" button)
		pageSize: 30,

		// store: Object
		//		Reference to data provider object used by this ComboBox
		store: null,

		// query: Object 
		//		A query that can be passed to 'store' to initially filter the items, 
		//		before doing further filtering based on searchAttr and the key. 
		query: {},

		// autoComplete: Boolean
		//		If you type in a partial string, and then tab out of the <input> box,
		//		automatically copy the first entry displayed in the drop down list to
		//		the <input> field
		autoComplete: true,

		// searchDelay: Integer
		//		Delay in milliseconds between when user types something and we start
		//		searching based on that value
		searchDelay: 100,

		// searchAttr: String
		//		Searches pattern match against this field
		searchAttr: "name",

		// ignoreCase: Boolean
		//		Does the ComboBox menu ignore case?
		ignoreCase: true,

		_hasMasterPopup:true,

		_popupClass:"dijit.form._ComboBoxMenu",

		getValue:function(){
			// don't get the textbox value but rather the previously set hidden value
			return dijit.form.TextBox.superclass.getValue.apply(this, arguments);
		},

		setDisplayedValue:function(/*String*/ value){
			this.setValue(value, true);
		},

		_getCaretPos: function(/*DomNode*/ element){
			// khtml 3.5.2 has selection* methods as does webkit nightlies from 2005-06-22
			if(typeof(element.selectionStart)=="number"){
				// FIXME: this is totally borked on Moz < 1.3. Any recourse?
				return element.selectionStart;
			}else if(dojo.isIE){
				// in the case of a mouse click in a popup being handled,
				// then the document.selection is not the textarea, but the popup
				// var r = document.selection.createRange();
				// hack to get IE 6 to play nice. What a POS browser.
				var tr = document.selection.createRange().duplicate();
				var ntr = element.createTextRange();
				tr.move("character",0);
				ntr.move("character",0);
				try{
					// If control doesnt have focus, you get an exception.
					// Seems to happen on reverse-tab, but can also happen on tab (seems to be a race condition - only happens sometimes).
					// There appears to be no workaround for this - googled for quite a while.
					ntr.setEndPoint("EndToEnd", tr);
					return String(ntr.text).replace(/\r/g,"").length;
				}
				catch(e){
					return 0; // If focus has shifted, 0 is fine for caret pos.
				}
			}
		},

		_setCaretPos: function(/*DomNode*/ element, /*Number*/ location){
			location = parseInt(location);
			this._setSelectedRange(element, location, location);
		},

		_setSelectedRange: function(/*DomNode*/ element, /*Number*/ start, /*Number*/ end){
			if(!end){
				end = element.value.length;
			}  // NOTE: Strange - should be able to put caret at start of text?
			// Mozilla
			// parts borrowed from http://www.faqts.com/knowledge_base/view.phtml/aid/13562/fid/130
			if(element.setSelectionRange){
				dijit.focus(element);
				element.setSelectionRange(start, end);
			}else if(element.createTextRange){ // IE
				var range = element.createTextRange();
				with(range){
					collapse(true);
					moveEnd('character', end);
					moveStart('character', start);
					select();
				}
			}else{ //otherwise try the event-creation hack (our own invention)
				// do we need these?
				element.value = element.value;
				element.blur();
				dijit.focus(element);
				// figure out how far back to go
				var dist = parseInt(element.value.length)-end;
				var tchar = String.fromCharCode(37);
				var tcc = tchar.charCodeAt(0);
				for(var x = 0; x < dist; x++){
					var te = document.createEvent("KeyEvents");
					te.initKeyEvent("keypress", true, true, null, false, false, false, false, tcc, tcc);
					element.dispatchEvent(te);
				}
			}
		},

		onkeypress: function(/*Event*/ evt){
			// summary: handles keyboard events
			if(evt.ctrlKey || evt.altKey){
				return;
			}
			var doSearch = false;
			if(this._isShowingNow){this._popupWidget.handleKey(evt);}
			switch(evt.keyCode){
				case dojo.keys.PAGE_DOWN:
				case dojo.keys.DOWN_ARROW:
					if(!this._isShowingNow||this._prev_key_esc){
						this._arrowPressed();
						doSearch=true;
					}else{
						this._announceOption(this._popupWidget.getHighlightedOption());
					}
					dojo.stopEvent(evt);
					this._prev_key_backspace = false;
					this._prev_key_esc = false;
					break;

				case dojo.keys.PAGE_UP:
				case dojo.keys.UP_ARROW:
					if(this._isShowingNow){
						this._announceOption(this._popupWidget.getHighlightedOption());
					}
					dojo.stopEvent(evt);
					this._prev_key_backspace = false;
					this._prev_key_esc = false;
					break;

				case dojo.keys.ENTER:
					// prevent submitting form if user presses enter
					// also prevent accepting the value if either Next or Previous are selected
					if(this._isShowingNow){
						// only stop event on prev/next
						var highlighted=this._popupWidget.getHighlightedOption();
						if(highlighted==this._popupWidget.nextButton){
							this._nextSearch(1);
							dojo.stopEvent(evt);
							break;
						}
						else if(highlighted==this._popupWidget.previousButton){
							this._nextSearch(-1);
							dojo.stopEvent(evt);
							break;
						}
					}
					// default case:
					// prevent submit, but allow event to bubble
					evt.preventDefault();
					// fall through

				case dojo.keys.TAB:
					if(this._isShowingNow){
						this._prev_key_backspace = false;
						this._prev_key_esc = false;
						if(this._isShowingNow&&this._popupWidget.getHighlightedOption()){
							this._popupWidget.setValue({target:this._popupWidget.getHighlightedOption()}, true);
						}else{
							this.setDisplayedValue(this.getDisplayedValue());
						}
						this._hideResultList();
					}else{
						// also allow arbitrary user input
						this.setDisplayedValue(this.getDisplayedValue());
					}
					break;

				case dojo.keys.SPACE:
					this._prev_key_backspace = false;
					this._prev_key_esc = false;
					if(this._isShowingNow && this._popupWidget.getHighlightedOption()){
						dojo.stopEvent(evt);
						this._selectOption();
						this._hideResultList();
					}
					else{doSearch=true;}
					break;

				case dojo.keys.ESCAPE:
					this._prev_key_backspace = false;
					this._prev_key_esc = true;
					this._hideResultList();
					this.setValue(this.getValue());
					break;

				case dojo.keys.DELETE:
				case dojo.keys.BACKSPACE:
					this._prev_key_esc = false;
					this._prev_key_backspace = true;
					doSearch=true;
					break;

				case dojo.keys.RIGHT_ARROW: // fall through

				case dojo.keys.LEFT_ARROW: // fall through
					this._prev_key_backspace = false;
					this._prev_key_esc = false;
					break;

				default:// non char keys (F1-F12 etc..)  shouldn't open list
					this._prev_key_backspace = false;
					this._prev_key_esc = false;
					if(evt.charCode!=0){
						doSearch=true;
					}
			}
			if(this.searchTimer){
				clearTimeout(this.searchTimer);
			}
			if(doSearch){
				// need to wait a tad before start search so that the event bubbles through DOM and we have value visible
				this.searchTimer = setTimeout(dojo.hitch(this, this._startSearchFromInput), this.searchDelay);
			}
		},

		_autoCompleteText: function(/*String*/ text){
			// summary:
			// Fill in the textbox with the first item from the drop down list, and
			// highlight the characters that were auto-completed.   For example, if user
			// typed "CA" and the drop down list appeared, the textbox would be changed to
			// "California" and "ifornia" would be highlighted.

			// IE7: clear selection so next highlight works all the time
			this._setSelectedRange(this.focusNode, this.focusNode.value.length, this.focusNode.value.length);
			// does text autoComplete the value in the textbox?
			// #3744: escape regexp so the user's input isn't treated as a regular expression.
			// Example: If the user typed "(" then the regexp would throw "unterminated parenthetical."
			// Also see #2558 for the autocompletion bug this regular expression fixes.
			if(new RegExp("^"+escape(this.focusNode.value), this.ignoreCase ? "i" : "").test(escape(text))){
				var cpos = this._getCaretPos(this.focusNode);
				// only try to extend if we added the last character at the end of the input
				if((cpos+1) > this.focusNode.value.length){
					// only add to input node as we would overwrite Capitalisation of chars
					// actually, that is ok
					this.focusNode.value = text;//.substr(cpos);
					// visually highlight the autocompleted characters
					this._setSelectedRange(this.focusNode, cpos, this.focusNode.value.length);
				}
			}else{
				// text does not autoComplete; replace the whole value and highlight
				this.focusNode.value = text;
				this._setSelectedRange(this.focusNode, 0, this.focusNode.value.length);
			}
		},

		_openResultList: function(/*Object*/ results, /*Object*/ dataObject){
			if(this.disabled||dataObject.query[this.searchAttr]!=this._lastQuery){
				return;
			}
			this._popupWidget.clearResultList();
			if(!results.length){
				this._hideResultList();
				return;
			}

			// Fill in the textbox with the first item from the drop down list, and
			// highlight the characters that were auto-completed.   For example, if user
			// typed "CA" and the drop down list appeared, the textbox would be changed to
			// "California" and "ifornia" would be highlighted.

			var zerothvalue=new String(this.store.getValue(results[0], this.searchAttr));
			if(zerothvalue&&(this.autoComplete)&&
			(!this._prev_key_backspace)&&
			// when the user clicks the arrow button to show the full list,
			// startSearch looks for "*".
			// it does not make sense to autocomplete
			// if they are just previewing the options available.
			(dataObject.query[this.searchAttr]!="*")){
				this._autoCompleteText(zerothvalue);
				// announce the autocompleted value
				dijit.wai.setAttr(this.focusNode || this.domNode, "waiState", "valuenow", zerothvalue);
			}
			this._popupWidget.createOptions(results, dataObject, dojo.hitch(this, this._getMenuLabelFromItem));

			// show our list (only if we have content, else nothing)
			this._showResultList();
		},

		onfocus:function(){
			dijit.form._DropDownTextBox.prototype.onfocus.apply(this, arguments);
			this.inherited('onfocus', arguments);
		},

		onblur:function(){ /* not _onBlur! */
			// call onblur first to avoid race conditions with _hasFocus
			dijit.form._DropDownTextBox.prototype.onblur.apply(this, arguments);
			if(!this._isShowingNow){
				// if the user clicks away from the textbox, set the value to the textbox value
				this.setDisplayedValue(this.getDisplayedValue());
			}
			// don't call this since the TextBox setValue is asynchronous
			// if you uncomment this line, when you click away from the textbox,
			// the value in the textbox reverts to match the hidden value
			//this.parentClass.onblur.apply(this, arguments);
		},

		_announceOption: function(/*Node*/ node){
			// summary:
			//	a11y code that puts the highlighted option in the textbox
			//	This way screen readers will know what is happening in the menu

			if(node==null){return;}
			// pull the text value from the item attached to the DOM node
			var newValue;
			if(node==this._popupWidget.nextButton||node==this._popupWidget.previousButton){
				newValue=node.innerHTML;
			}else{
				newValue=this.store.getValue(node.item, this.searchAttr);
			}
			// get the text that the user manually entered (cut off autocompleted text)
			this.focusNode.value=this.focusNode.value.substring(0, this._getCaretPos(this.focusNode));
			// autocomplete the rest of the option to announce change
			this._autoCompleteText(newValue);
		},

		_selectOption: function(/*Event*/ evt){
			var tgt = null;
			if(!evt){
				evt ={ target: this._popupWidget.getHighlightedOption()};
			}
				// what if nothing is highlighted yet?
			if(!evt.target){
				// handle autocompletion where the the user has hit ENTER or TAB
				this.setDisplayedValue(this.getDisplayedValue());
				return;
			// otherwise the user has accepted the autocompleted value
			}else{
				tgt = evt.target;
			}
			if(!evt.noHide){
				this._hideResultList();
				this._setCaretPos(this.focusNode, this.store.getValue(tgt.item, this.searchAttr).length);
			}
			this._doSelect(tgt);
		},

		_doSelect: function(tgt){
			this.setValue(this.store.getValue(tgt.item, this.searchAttr), true);
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
				this._startSearch("");
			}
		},

		_startSearchFromInput: function(){
			this._startSearch(this.focusNode.value);
		},

		_startSearch: function(/*String*/ key){
			this.makePopup();
			// create a new query to prevent accidentally querying for a hidden value from FilteringSelect's keyField
			var query=this.query;
			this._lastQuery=query[this.searchAttr]=key+"*";
			var dataObject=this.store.fetch({queryOptions:{ignoreCase:this.ignoreCase, deep:true}, query: query, onComplete:dojo.hitch(this, "_openResultList"), start:0, count:this.pageSize});
			function nextSearch(dataObject, direction){
				dataObject.start+=dataObject.count*direction;
				dataObject.store.fetch(dataObject);
			}
			this._nextSearch=this._popupWidget.onPage=dojo.hitch(this, nextSearch, dataObject);
		},

		_getValueField:function(){
			return this.searchAttr;
		},

		postMixInProperties: function(){
			dijit.form._DropDownTextBox.prototype.postMixInProperties.apply(this, arguments);
			if(!this.store){
				// if user didn't specify store, then assume there are option tags
				var items = dojo.query("> option", this.srcNodeRef).map(function(node){
					node.style.display="none";
					return { value: node.getAttribute("value"), name: String(node.innerHTML) };
				});
				this.store = new dojo.data.ItemFileReadStore({data: {identifier:this._getValueField(), items:items}});

				// if there is no value set and there is an option list,
				// set the value to the first value to be consistent with native Select
				if(items&&items.length&&!this.value){
					// For <select>, IE does not let you set the value attribute of the srcNodeRef (and thus dojo.mixin does not copy it).
					// IE does understand selectedIndex though, which is automatically set by the selected attribute of an option tag
					this.value=items[this.srcNodeRef.selectedIndex!=-1?this.srcNodeRef.selectedIndex:0][this._getValueField()];
				}
			}
			// instantiate query so comboboxes with different data stores and default query work together
			if(this.query==dijit.form.ComboBoxMixin.prototype.query){this.query={};}
		},

		postCreate: function(){
			// call the associated TextBox postCreate
			// ValidationTextBox for ComboBox; MappedTextBox for FilteringSelect
			this.inherited('postCreate', arguments);
		},

		_getMenuLabelFromItem:function(/*Item*/ item){
			return {html:false, label:this.store.getValue(item, this.searchAttr)};
		},

		open:function(){
			this._popupWidget.onChange=dojo.hitch(this, this._selectOption);
			// connect onkeypress to ComboBox
			this._popupWidget._onkeypresshandle=this._popupWidget.connect(this._popupWidget.domNode, "onkeypress", dojo.hitch(this, this.onkeypress));
			return dijit.form._DropDownTextBox.prototype.open.apply(this, arguments);
		}
	}
);

dojo.declare(
	"dijit.form._ComboBoxMenu",
	[dijit._Widget, dijit._Templated],

	{
		// summary:
		//	Focus-less div based menu for internal use in ComboBox

		templateString:"<div class='dijitMenu' dojoAttachEvent='onclick,onmouseover,onmouseout' tabIndex='-1' style='display:none; position:absolute; overflow:\"auto\";'>"
				+"<div class='dijitMenuItem' dojoAttachPoint='previousButton'></div>"
				+"<div class='dijitMenuItem' dojoAttachPoint='nextButton'></div>"
			+"</div>",
		_onkeypresshandle:null,
		_messages:null,
		_comboBox:null,

		postMixInProperties:function(){
			this._messages = dojo.i18n.getLocalization("dijit.form", "ComboBox", this.lang);
			this.inherited("postMixInProperties", arguments);
		},

		setValue:function(/*Object*/ value){
			this.value=value;
			this.onChange(value);
		},

		onChange:function(/*Object*/ value){},
		onPage:function(/*Number*/ direction){},

		postCreate:function(){
			// fill in template with i18n messages
			this.previousButton.innerHTML=this._messages["previousMessage"];
			this.nextButton.innerHTML=this._messages["nextMessage"];
			this.inherited("postCreate", arguments);
		},

		onClose:function(){
			this.disconnect(this._onkeypresshandle);
			this._blurOptionNode();
		},

		_createOption:function(/*Object*/ item, labelFunc){
			// summary: creates an option to appear on the popup menu
			// subclassed by FilteringSelect

			var labelObject=labelFunc(item);
			var menuitem = document.createElement("div");
			if(labelObject.html){menuitem.innerHTML=labelObject.label;}
			else{menuitem.appendChild(document.createTextNode(labelObject.label));}
			// #3250: in blank options, assign a normal height
			if(menuitem.innerHTML==""){
				menuitem.innerHTML="&nbsp;"
			}
			menuitem.item=item;
			return menuitem;
		},

		createOptions:function(results, dataObject, labelFunc){
			//this._dataObject=dataObject;
			//this._dataObject.onComplete=dojo.hitch(comboBox, comboBox._openResultList);
			// display "Previous . . ." button
			this.previousButton.style.display=dataObject.start==0?"none":"";
			// create options using _createOption function defined by parent ComboBox (or FilteringSelect) class
			// #2309: iterate over cache nondestructively
			var _this=this;
			dojo.forEach(results, function(item){
				var menuitem=_this._createOption(item, labelFunc);
				menuitem.className = "dijitMenuItem";
				_this.domNode.insertBefore(menuitem, _this.nextButton);
			});
			// display "Next . . ." button
			this.nextButton.style.display=dataObject.count==results.length?"":"none";
		},

		clearResultList:function(){
			// keep the previous and next buttons of course
			while(this.domNode.childNodes.length>2){
				this.domNode.removeChild(this.domNode.childNodes[this.domNode.childNodes.length-2]);
			}
		},

		// these functions are called in showResultList
		getItems:function(){
			return this.domNode.childNodes;
		},

		getListLength:function(){
			return this.domNode.childNodes.length-2;
		},

		onclick:function(/*Event*/ evt){
			if(evt.target === this.domNode){
				return;
			}else if(evt.target==this.previousButton){
				this.onPage(-1);
			}else if(evt.target==this.nextButton){
				this.onPage(1);
			}else{
				var tgt=evt.target;
				// while the clicked node is inside the div
				while(!tgt.item){
					// recurse to the top
					tgt=tgt.parentNode;
				}
				this.setValue({target:tgt}, true);
			}
		},

		onmouseover:function(/*Event*/ evt){
			if(evt.target === this.domNode){ return; }
			this._focusOptionNode(evt.target);
		},

		onmouseout:function(/*Event*/ evt){
			if(evt.target === this.domNode){ return; }
			this._blurOptionNode();
		},

		_focusOptionNode:function(/*DomNode*/ node){
			// summary:
			//	does the actual highlight
			if(this._highlighted_option != node){
				this._blurOptionNode();
				this._highlighted_option = node;
				dojo.addClass(this._highlighted_option, "dijitMenuItemHover");
			}
		},

		_blurOptionNode:function(){
			// summary:
			//	removes highlight on highlighted option
			if(this._highlighted_option){
				dojo.removeClass(this._highlighted_option, "dijitMenuItemHover");
				this._highlighted_option = null;
			}
		},

		_highlightNextOption:function(){
			// because each press of a button clears the menu,
			// the highlighted option sometimes becomes detached from the menu!
			// test to see if the option has a parent to see if this is the case.
			if(!this.getHighlightedOption()){
				this._focusOptionNode(this.domNode.firstChild.style.display=="none"?this.domNode.firstChild.nextSibling:this.domNode.firstChild);
			}else if(this._highlighted_option.nextSibling&&this._highlighted_option.nextSibling.style.display!="none"){
				this._focusOptionNode(this._highlighted_option.nextSibling);
			}
			dijit.scrollIntoView(this._highlighted_option);
		},


		_highlightPrevOption:function(){
			// if nothing selected, highlight last option
			// makes sense if you select Previous and try to keep scrolling up the list
			if(!this.getHighlightedOption()){
				this._focusOptionNode(this.domNode.lastChild.style.display=="none"?this.domNode.lastChild.previousSibling:this.domNode.lastChild);
			}else if(this._highlighted_option.previousSibling&&this._highlighted_option.previousSibling.style.display!="none"){
				this._focusOptionNode(this._highlighted_option.previousSibling);
			}
			dijit.scrollIntoView(this._highlighted_option);
		},

		_page:function(/*Boolean*/ up){
			var scrollamount=0;
			var oldscroll=this.domNode.scrollTop;
			var height=parseInt(dojo.getComputedStyle(this.domNode).height);
			// if no item is highlighted, highlight the first option
			if(!this.getHighlightedOption()){this._highlightNextOption();}
			while(scrollamount<height){
				if(up){
					// stop at option 1
					if(!this.getHighlightedOption().previousSibling||this._highlighted_option.previousSibling.style.display=="none"){break;}
					this._highlightPrevOption();
				}else{
					// stop at last option
					if(!this.getHighlightedOption().nextSibling||this._highlighted_option.nextSibling.style.display=="none"){break;}
					this._highlightNextOption();
				}
				// going backwards
				var newscroll=this.domNode.scrollTop;
				scrollamount+=(newscroll-oldscroll)*(up ? -1:1);
				oldscroll=newscroll;
			}
		},

		pageUp:function(){
			this._page(true);
		},

		pageDown:function(){
			this._page(false);
		},

		getHighlightedOption:function(){
			// summary:
			//	Returns the highlighted option.
			return this._highlighted_option&&this._highlighted_option.parentNode ? this._highlighted_option : null;
		},

		handleKey:function(evt){
			switch(evt.keyCode){
				case dojo.keys.DOWN_ARROW:
					this._highlightNextOption();
					break;
				case dojo.keys.PAGE_DOWN:
					this.pageDown();
					break;	
				case dojo.keys.UP_ARROW:
					this._highlightPrevOption();
					break;
				case dojo.keys.PAGE_UP:
					this.pageUp();
					break;	
			}
		}
	}
);

dojo.declare(
	"dijit.form.ComboBox",
	[dijit.form.ValidationTextBox, dijit.form.ComboBoxMixin],
	{}
);

}
