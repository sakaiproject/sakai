if(!dojo._hasResource["dojox.layout.FloatingPane"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.layout.FloatingPane"] = true;
dojo.provide("dojox.layout.FloatingPane");
dojo.experimental("dojox.layout.FloatingPane"); 

dojo.require("dijit.layout.ContentPane");
dojo.require("dijit._Templated"); 
dojo.require("dijit._Widget"); 
dojo.require("dojo.dnd.move");
dojo.require("dojox.layout.ResizeHandle"); 

dojo.declare("dojox.layout.FloatingPane", [dijit.layout.ContentPane, dijit._Templated], {
	// summary:
	//
	// Makes a dijit.ContentPane float and draggable by it's title [similar to TitlePane]
	// and over-rides onClick to onDblClick for wipeIn/Out of containerNode
	// provides minimize(dock) / show() and hide() methods, and resize [almost] 

	// closable: Boolean
	//	allow closure of this Node
	closable: true,

	// dockable: Boolean
	//	allow minimizing of pane true/false
	dockable: false, 

	// resizable: Boolean
	//	allow resizing of pane true/false
	resizable: false,

	// resizeAxis: String
	//	x | xy | y to limit pane's sizing direction
	resizeAxis: "xy",

	// title: String
	//	title to put in titlebar
	title: "",

	// dockTo: DomNode || null
	//	if null, will create private layout.Dock that scrolls with viewport
	//	on bottom span of viewport.	
	dockTo: null,

	// duration: Integer
	//	time is MS to spend toggling in/out node
	duration: 400,

	// animation holders for toggle
	_showAnim: null,
	_hideAnim: null, 

	// iconSrc: String
	//	[not implemented yet] will be either icon in titlepane to left
	//	of Title, and/or icon show when docked in a fisheye-like dock
	//	or maybe dockIcon would be better?
	iconSrc: null,

	contentClass: "dojoxFloatingPaneContent",
	templateString: null,
	templateString:"<div id=\"${id}\">\n\t<div tabindex=\"0\" waiRole=\"button\" class=\"dijitTitlePaneTitle\" dojoAttachPoint=\"focusNode\">\n\t\t<span dojoAttachPoint=\"closeNode\" dojoAttachEvent=\"onclick: close\" class=\"dojoxFloatingCloseIcon\"></span>\n\t\t<span dojoAttachPoint=\"dockNode\" dojoAttachEvent=\"onclick: minimize\" class=\"dojoxFloatingMiniMizeIcon\"></span>\n\t\t<span dojoAttachPoint=\"titleNode\" class=\"dijitInlineBox dijitTitleNode\"></span>\n\t</div>\n\t<div>\n\t<div dojoAttachPoint=\"containerNode\" dojoType=\"dijit.layout.ContentPane\" waiRole=\"region\" tabindex=\"-1\" class=\"${contentClass}\">\n\t</div>\n\t<span dojoAttachPoint=\"resizeHandle\" class=\"dojoxFloatingResizeHandle\"></span>\n\t</div>\n</div>\n",

	postCreate: function(){
		// summary: 
		this.setTitle(this.title);
		dojox.layout.FloatingPane.superclass.postCreate.apply(this,arguments);
		var move = new dojo.dnd.Moveable(this.domNode,{ handle: this.focusNode });

		if(!this.dockable){ this.dockNode.style.display = "none"; } 
		if(!this.closable){ this.closeNode.style.display = "none"; } 
		if(!this.resizable){
			this.resizeHandle.style.display = "none"; 	
		}else{
			var foo = dojo.marginBox(this.domNode); 
			//this.domNode.style.height = foo.h+"px";
			this.domNode.style.width = foo.w+"px"; 
		}
	},
	
	startup: function(){
	
		dojox.layout.FloatingPane.superclass.startup.call(this); 

		dojo.style(this.domNode,"border","1px solid #dedede"); 
		dojo.style(this.domNode,"overflow","hidden"); 
		//dojo.style(this.dom

		if (this.resizable) {
			this.containerNode.style.overflow = "auto";
			var tmp = new dojox.layout.ResizeHandle({ 
				//targetContainer: this.containerNode, 
				targetId: this.id, 
				resizeAxis: this.resizeAxis 
			},this.resizeHandle);
		}

		if(this.dockable){ 
			// FIXME: argh.
			tmpName = this.dockTo; 

			if(this.dockTo){ this.dockTo = dijit.byId(this.dockTo); }
			else{ this.dockTo = dijit.byId('dojoxGlobalFloatingDock'); }

			if(!this.dockTo){
				// we need to make our dock node, and position it against
				// .dojoxDockDefault .. this is a lot. either dockto="node"
				// and fail if node doesn't exist or make the global one
				// once, and use it on empty OR invalid dockTo="" node?
				if(tmpName){ 
					var tmpId = tmpName;
					var tmpNode = dojo.byId(tmpName); 
				}else{
					var tmpNode = document.createElement('div');
					dojo.body().appendChild(tmpNode);
					dojo.addClass(tmpNode,"dojoxFloatingDockDefault");
					var tmpId = 'dojoxGlobalFloatingDock';
				}
				this.dockTo = new dojox.layout.Dock({ id: tmpId },tmpNode);
				this.dockTo.startup(); 
			}
		} 
	},

	setTitle: function(/* String */ title) {
		this.titleNode.innerHTML = title; 
	},	

	zIndexes: function() {
		// summary: keep track of our own zIndex for bringToTop like behavior [not yet]
		dojo.style(this.domNode,"zIndex","997"); 
	},

	// extend 		
	close: function() {
		if (!this.closable) { return; }
		this.hide(dojo.hitch(this,"destroy")); 
	},

	hide: function(/* Function */ callback) {
		dojo.fadeOut({node:this.domNode, duration:this.duration,
			onEnd: dojo.hitch(this,function() { 
				this.domNode.style.display = "none";
				this.domNode.style.visibility = "hidden"; 
				if (typeof callback == "function") { callback(); }
				})
			}).play();
	},

	show: function(callback) {
		var anim = dojo.fadeIn({node:this.domNode, duration:this.duration,
			beforeBegin: dojo.hitch(this,function() {
				this.domNode.style.display = ""; 
				this.domNode.style.visibility = "visible";
				if (typeof callback == "function") { callback(); }
				this._isDocked = false; 
				})
			}).play();
	},

	minimize: function() {
		if (!this._isDocked) {
		this.hide(dojo.hitch(this,"_dock"));
		} 
	},

	_dock: function() {
		if (!this._isDocked) {
			this.dockTo.addNode(this);
			this._isDocked = true;
		}
	}
	
});

dojo.declare("dojox.layout.Dock", [dijit._Widget,dijit._Templated], {
	// summary:
	//	a widget that attaches to a node and keeps track of incoming / outgoing FloatingPanes
	// 	and handles layout

	templateString: '<div class="dojoxDock"><ul dojoAttachPoint="containerNode" class="dojoxDockList"></ul></div>',

	// private _docked: array of panes currently in our dock
	_docked: [],
	
	addNode: function(refNode) {
		// summary: FIXME: memory leak? 
		var div = document.createElement('li');
		this.containerNode.appendChild(div);
		var node = new dojox.layout._DockNode({ title: refNode.title, paneRef: refNode },div);
		node.startup();
	},

	startup: function() {
		// summary: attaches some event listeners 
		if (this.id == "dojoxGlobalFloatingDock" || this.isFixedDock) {
			// attach window.onScroll, and a position like in presentation/dialog
			dojo.connect(window,'onresize',this,"_positionDock");
			dojo.connect(window,'onscroll',this,"_positionDock");
		}
		dojox.layout.Dock.superclass.startup.call(this); 
	},
	
	_positionDock: function(e) {
		// summary: 
		//	[b0rken atm] keeps the dock [in the event of a globalFloatingDock]
		//	positioned at the bottom of the viewport. (math is off)
		
		var viewport = dijit.getViewport();
		var s = this.domNode.style;
		console.debug(viewport); 
		s.width = viewport.w + "px";
		s.top = (viewport.h + viewport.t) - 50 + "px"
	}


});

dojo.declare("dojox.layout._DockNode", [dijit._Widget,dijit._Templated], {
	// summary:
	//	dojox.layout._DockNode is a private widget used to keep track of
	//	which pane is docked.

	// title: String
	// 	shown in dock icon. should read parent iconSrc?	
	title: "",

	// paneRef: Widget
	//	reference to the FloatingPane we reprasent in any given dock
	paneRef: null,

	templateString: '<li dojoAttachEvent="ondblclick: restore" class="dojoxDockNode">'+
			'<span dojoAttachPoint="restoreNode" class="dojoxDockRestoreButton" dojoAttachEvent="onclick: restore"></span>'+
			'<span class="dojoxDockTitleNode" dojoAttachPoint="titleNode">${title}</span>'+
			'</li>',

	restore: function() {
		// summary: remove this dock item from parent dock, and call show() on reffed floatingpane
		this.paneRef.show();
		this.destroy();
	}

});

}
