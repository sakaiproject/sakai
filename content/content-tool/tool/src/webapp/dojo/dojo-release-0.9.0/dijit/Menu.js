if(!dojo._hasResource["dijit.Menu"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.Menu"] = true;
dojo.provide("dijit.Menu");

dojo.require("dijit._Widget");
dojo.require("dijit._Container");
dojo.require("dijit._Templated");

dojo.declare(
	"dijit.Menu",
	[dijit._Widget, dijit._Templated, dijit._Container],
{
	preamble: function() {
		this._bindings = [];
	},
	
	templateString:
			'<table class="dijit dijitMenu dijitReset dijitMenuTable" waiRole="menu" dojoAttachEvent="onkeypress:_onKeyPress">' +
				'<tbody class="dijitReset" dojoAttachPoint="containerNode"></tbody>'+
			'</table>',

	// targetNodeIds: String[]
	//	Array of dom node ids of nodes to attach to.
	//	Fill this with nodeIds upon widget creation and it becomes context menu for those nodes.
	targetNodeIds: [],

	// contextMenuForWindow: Boolean
	//	if true, right clicking anywhere on the window will cause this context menu to open;
	//	if false, must specify targetNodeIds
	contextMenuForWindow: false,

	// parentMenu: Widget
	// pointer to menu that displayed me
	parentMenu: null,

	// popupDelay: Integer
	//	number of milliseconds before hovering (without clicking) causes the popup to automatically open
	popupDelay: 500,

	// _contextMenuWithMouse: Boolean
	//	used to record mouse and keyboard events to determine if a context
	//	menu is being opened with the keyboard or the mouse
	_contextMenuWithMouse: false,

	postCreate: function(){
		if(this.contextMenuForWindow){
			this.bindDomNode(dojo.body());
		}else{
			dojo.forEach(this.targetNodeIds, this.bindDomNode, this);
		}
	},

	startup: function(){
		dojo.forEach(this.getChildren(), function(child){ child.startup(); });
	},

	onExecute: function(){
		// summary: attach point for notification about when a menu item has been executed
	},

	onCancel: function(/*Boolean*/ closeAll){
		// summary: attach point for notification about when the user cancels the current menu
	},

	focus: function(){
		this._focusFirstItem();
	},

	_moveToPopup: function(/*Event*/ evt){
		if(this._focusedItem && this._focusedItem.popup && !this._focusedItem.disabled){
			return this._activateCurrentItem(evt);
		}
		return false;
	},

	_activateCurrentItem: function(/*Event*/ evt){
		if(this._focusedItem){
			this._focusedItem._onClick(evt);
			return true; //do not pass to parent menu
		}
		return false;
	},

	_onKeyPress: function(/*Event*/ evt){
		// summary
		//	Handle keyboard based menu navigation.
		if(evt.ctrlKey || evt.altKey){ return; }

		var key = (evt.charCode == dojo.keys.SPACE ? dojo.keys.SPACE : evt.keyCode);
		switch(key){
 			case dojo.keys.DOWN_ARROW:
				this._focusNeighborItem(1);
				dojo.stopEvent(evt);
				break;
			case dojo.keys.UP_ARROW:
				this._focusNeighborItem(-1);
				dojo.stopEvent(evt);
				break;
			case dojo.keys.RIGHT_ARROW:
				this._moveToPopup(evt);
				dojo.stopEvent(evt);
				break;
			case dojo.keys.LEFT_ARROW:
				if(this.parentMenu){
					this.onCancel(false);
				}else{
					dojo.stopEvent(evt);
				}
				break;
			case dojo.keys.TAB:
				dojo.stopEvent(evt);
				// Hmm, there's no good infrastructure to support cancel closing the whole tree
				// of menus, but it's close to an execute event, in the sense that focus is returned
				// to the previously focused node (for a context menu) or to the DropDownButton
				this.onExecute();
				break;
		}
	},

	_findValidItem: function(dir){
		// summary: find the next/previous item to focus on (depending on dir setting).

		var curItem = this._focusedItem;
		if(curItem){
			curItem = dir>0 ? curItem.getNextSibling() : curItem.getPreviousSibling();
		}

		var children = this.getChildren();
		for(var i=0; i < children.length; ++i){
			if(!curItem){
				curItem = children[(dir>0) ? 0 : (children.length-1)];
			}
			//find next/previous visible menu item, not including separators
			if(curItem._onHover && dojo.style(curItem.domNode, "display") != "none"){
				return curItem;
			}
			curItem = dir>0 ? curItem.getNextSibling() : curItem.getPreviousSibling();
		}
	},

	_focusNeighborItem: function(dir){
		// summary: focus on the next / previous item (depending on dir setting)
		var item = this._findValidItem(dir);
		this._focusItem(item);
	},

	_focusFirstItem: function(){
		// blur focused item to make findValidItem() find the first item in the menu
		if(this._focusedItem){
			this._blurFocusedItem();
		}
		var item = this._findValidItem(1);
		this._focusItem(item);
	},

	_focusItem: function(/*MenuItem*/ item){
		// summary: internal function to focus a given menu item

		if(!item || item==this._focusedItem){
			return;
		}

		if(this._focusedItem){
			this._blurFocusedItem();
		}
		item._focus();
		this._focusedItem = item;
	},

	onItemHover: function(/*MenuItem*/ item){
		this._focusItem(item);

		if(this._focusedItem.popup && !this._focusedItem.disabled && !this.hover_timer){
			this.hover_timer = setTimeout(dojo.hitch(this, "_openPopup"), this.popupDelay);
		}
	},

	_blurFocusedItem: function(){
		// summary: internal function to remove focus from the currently focused item
		if(this._focusedItem){
			// Close all popups that are open and descendants of this menu
			dijit.popup.closeTo(this);
			this._focusedItem._blur();
			this._stopPopupTimer();
			this._focusedItem = null;
		}
	},

	onItemUnhover: function(/*MenuItem*/ item){
		//this._blurFocusedItem();
	},

	_stopPopupTimer: function(){
		if(this.hover_timer){
			clearTimeout(this.hover_timer);
			this.hover_timer = null;
		}
	},

	_getTopMenu: function(){
		for(var top=this; top.parentMenu; top=top.parentMenu);
		return top;
	},

	onItemClick: function(/*Widget*/ item){
		// summary: user defined function to handle clicks on an item
		// summary: internal function for clicks
		if(item.disabled){ return false; }

		if(item.popup){
			if(!this.is_open){
				this._openPopup();
			}
		}else{
			// before calling user defined handler, close hierarchy of menus
			// and restore focus to place it was when menu was opened
			this.onExecute();

			// user defined handler for click
			item.onClick();
		}
	},

	// thanks burstlib!
	_iframeContentWindow: function(/* HTMLIFrameElement */iframe_el) {
		//	summary
		//	returns the window reference of the passed iframe
		var win = dijit.getDocumentWindow(dijit.Menu._iframeContentDocument(iframe_el)) ||
			// Moz. TODO: is this available when defaultView isn't?
			dijit.Menu._iframeContentDocument(iframe_el)['__parent__'] ||
			(iframe_el.name && document.frames[iframe_el.name]) || null;
		return win;	//	Window
	},

	_iframeContentDocument: function(/* HTMLIFrameElement */iframe_el){
		//	summary
		//	returns a reference to the document object inside iframe_el
		var doc = iframe_el.contentDocument // W3
			|| (iframe_el.contentWindow && iframe_el.contentWindow.document) // IE
			|| (iframe_el.name && document.frames[iframe_el.name] && document.frames[iframe_el.name].document)
			|| null;
		return doc;	//	HTMLDocument
	},

	bindDomNode: function(/*String|DomNode*/ node){
		// summary: attach menu to given node
		node = dojo.byId(node);

		//TODO: this is to support context popups in Editor.  Maybe this shouldn't be in dijit.Menu
		var win = dijit.getDocumentWindow(node.ownerDocument);
		if(node.tagName.toLowerCase()=="iframe"){
			win = this._iframeContentWindow(node);
			node = dojo.withGlobal(win, dojo.body);
		}

		// to capture these events at the top level,
		// attach to document, not body
		var cn = (node == dojo.body() ? dojo.doc : node);
		
		node[this.id] = this._bindings.push([
			dojo.connect(cn, "oncontextmenu", this, "_openMyself"),
			dojo.connect(cn, "onkeydown", this, "_contextKey"),
			dojo.connect(cn, "onmousedown", this, "_contextMouse")
		]);
	},

	unBindDomNode: function(/*String|DomNode*/ nodeName){
		// summary: detach menu from given node
		var node = dojo.byId(nodeName);
		var bid = node[this.id]-1, b = this._bindings[bid];
		dojo.forEach(b, dojo.disconnect);
		delete this._bindings[bid];
	},

	_contextKey: function(e){
		this._contextMenuWithMouse = false;
		if (e.keyCode == dojo.keys.F10) {
			dojo.stopEvent(e);
			if (e.shiftKey && e.type=="keydown") {
				// FF: copying the wrong property from e will cause the system
				// context menu to appear in spite of stopEvent. Don't know
				// exactly which properties cause this effect.
				var _e = { target: e.target, pageX: e.pageX, pageY: e.pageY };
				_e.preventDefault = _e.stopPropagation = function(){};
				// IE: without the delay, focus work in "open" causes the system
				// context menu to appear in spite of stopEvent.
				window.setTimeout(dojo.hitch(this, function(){ this._openMyself(_e); }), 1);
			}
		}
	},

	_contextMouse: function(e){
		this._contextMenuWithMouse = true;
	},

	_openMyself: function(/*Event*/ e){
		// summary:
		//		Internal function for opening myself when the user
		//		does a right-click or something similar

		dojo.stopEvent(e);
		
		// Get coordinates.
		// if we are opening the menu with the mouse or on safari open
		// the menu at the mouse cursor
		// (Safari does not have a keyboard command to open the context menu
		// and we don't currently have a reliable way to determine
		// _contextMenuWithMouse on Safari)
		var x,y;
		if(dojo.isSafari || this._contextMenuWithMouse){
			x=e.pageX;
			y=e.pageY;
		}else{
			// otherwise open near e.target
			var coords = dojo.coords(e.target, true);
			x = coords.x + 10;
			y = coords.y + 10;
		}

		var self=this;
		var savedFocus = dijit.getFocus(this);
		function closeAndRestoreFocus(){
			// user has clicked on a menu or popup
			dijit.focus(savedFocus);
			dijit.popup.closeAll();
		}
		dijit.popup.open({
			popup: this,
			x: x,
			y: y,
			onExecute: closeAndRestoreFocus,
			onCancel: closeAndRestoreFocus,
			orient: this.isLeftToRight() ? 'L' : 'R'
		});
		this.focus();
		
		this._onBlur = function(){
			// Usually the parent closes the child widget but if this is a context
			// menu then there is no parent
			dijit.popup.closeAll();
			// don't try to restor focus; user has clicked another part of the screen
			// and set focus there
		}
	},

	onOpen: function(/*Event*/ e){
		// summary
		//		Open menu relative to the mouse
		this.isShowingNow = true;
	},

	onClose: function(){
		// summary: callback when this menu is closed
		this._stopPopupTimer();
		this.parentMenu = null;
		this.isShowingNow = false;
		this.currentPopup = null;
		if(this._focusedItem){
			this._blurFocusedItem();
		}
	},

	_openPopup: function(){
		// summary: open the popup to the side of the current menu item
		this._stopPopupTimer();
		var from_item = this._focusedItem;
		var popup = from_item.popup;

		if(popup.isShowingNow){ return; }
		popup.parentMenu = this;
		var self = this;
		dijit.popup.open({
			parent: this,
			popup: popup,
			around: from_item.arrowCell,
			orient: this.isLeftToRight() ? {'TR': 'TL', 'TL': 'TR'} : {'TL': 'TR', 'TR': 'TL'},
			submenu: true,
			onCancel: function(){
				// called when the child menu is canceled
				dijit.popup.close();
				self._focusedItem._focus();	// put focus back on my node
				self.currentPopup = null;
			}
		});
			

		this.currentPopup = popup;
		
		if(popup.focus){
			popup.focus();
		}
	}
}
);

dojo.declare(
	"dijit.MenuItem",
	[dijit._Widget, dijit._Templated, dijit._Contained],
{
	// summary
	//	A line item in a Menu2

	// Make 3 columns
	//   icon, label, and expand arrow (BiDi-dependent) indicating sub-menu
	templateString:
		 '<tr class="dijitReset dijitMenuItem"'
		+'dojoAttachEvent="onmouseover:_onHover,onmouseout:_onUnhover,ondijitclick:_onClick">'
		+'<td class="dijitReset"><div class="dijitMenuItemIcon ${iconClass}"></div></td>'
		+'<td tabIndex="-1" class="dijitReset dijitMenuItemLabel" dojoAttachPoint="containerNode" waiRole="menuitem"></td>'
		+'<td class="dijitReset" dojoAttachPoint="arrowCell">'
			+'<div class="dijitMenuExpand" dojoAttachPoint="expand" style="display:none">'
			+'<span class="dijit_a11y dijitInline dijitArrowNode dijitMenuExpandInner">+</span>'
			+'</div>'
		+'</td>'
		+'</tr>',

	// iconSrc: String
	//	path to icon to display to the left of the menu text
	iconSrc: '',

	// label: String
	//	menu text
	label: '',

	// iconClass: String
	//	class to apply to div in button to make it display an icon
	iconClass: "",

	// disabled: Boolean
	//  if true, the menu item is disabled
	//  if false, the menu item is enabled
	disabled: false,

	postCreate: function(){
		dojo.setSelectable(this.domNode, false);
		this.setDisabled(this.disabled);
		if(this.label){
			this.containerNode.innerHTML=this.label;
		}
	},

	_onHover: function(){
		// summary: callback when mouse is moved onto menu item
		this.getParent().onItemHover(this);
	},

	_onUnhover: function(){
		// summary: callback when mouse is moved off of menu item
		// if we are unhovering the currently selected item
		// then unselect it
		this.getParent().onItemUnhover(this);
	},

	_onClick: function(evt){
		this.getParent().onItemClick(this);
		dojo.stopEvent(evt);
	},

	onClick: function() {
		// summary
		//	User defined function to handle clicks
	},

	_focus: function(){
		dojo.addClass(this.domNode, 'dijitMenuItemHover');
		try{
			dijit.focus(this.containerNode);
		}catch(e){
			// this throws on IE (at least) in some scenarios
		}
	},

	_blur: function(){
		dojo.removeClass(this.domNode, 'dijitMenuItemHover');
	},

	setDisabled: function(/*Boolean*/ value){
		// summary: enable or disable this menu item
		this.disabled = value;
		dojo[value ? "addClass" : "removeClass"](this.domNode, 'dijitMenuItemDisabled');
		dijit.wai.setAttr(this.containerNode, 'waiState', 'disabled', value ? 'true' : 'false');
	}
});

dojo.declare(
	"dijit.PopupMenuItem",
	dijit.MenuItem,
{
	_fillContent: function(){
		// my inner HTML contains both the menu item text and a popup widget, like
		// <div dojoType="dijit.PopupMenuItem">
		//		<span>pick me</span>
		//		<popup> ... </popup>
		// </div>
		// the first part holds the menu item text and the second part is the popup
		if(this.srcNodeRef){
			var nodes = dojo.query("*", this.srcNodeRef);
			dijit.PopupMenuItem.superclass._fillContent.call(this, nodes[0]);
			
			// save pointer to srcNode so we can grab the drop down widget after it's instantiated
			this.dropDownContainer = this.srcNodeRef;
		}
	},

	startup: function(){
		// we didn't copy the dropdown widget from the this.srcNodeRef, so it's in no-man's
		// land now.  move it to document.body.
		if(!this.popup){
			var node = dojo.query("[widgetId]", this.dropDownContainer)[0];
			this.popup = dijit.byNode(node);
		}
		dojo.body().appendChild(this.popup.domNode);

		this.popup.domNode.style.display="none";
		dojo.addClass(this.expand, "dijitMenuExpandEnabled");
		dojo.style(this.expand, "display", "");
		dijit.wai.setAttr(this.containerNode, "waiState", "haspopup", "true");
	}
});

dojo.declare(
	"dijit.MenuSeparator",
	[dijit._Widget, dijit._Templated, dijit._Contained],
{
	// summary
	//	A line between two menu items

	templateString: '<tr class="dijitMenuSeparator"><td colspan=3>'
			+'<div class="dijitMenuSeparatorTop"></div>'
			+'<div class="dijitMenuSeparatorBottom"></div>'
			+'</td></tr>',

	postCreate: function(){
		dojo.setSelectable(this.domNode, false);
	}
});

}
