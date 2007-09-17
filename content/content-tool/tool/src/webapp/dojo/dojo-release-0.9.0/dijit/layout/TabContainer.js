if(!dojo._hasResource["dijit.layout.TabContainer"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout.TabContainer"] = true;
dojo.provide("dijit.layout.TabContainer");

dojo.require("dijit.layout.StackContainer");
dojo.require("dijit._Templated");

dojo.declare(
	"dijit.layout.TabContainer",
	[dijit.layout.StackContainer, dijit._Templated],
{
	// summary
	//	A TabContainer is a container that has multiple panes, but shows only
	//	one pane at a time.  There are a set of tabs corresponding to each pane,
	//	where each tab has the title (aka title) of the pane, and optionally a close button.
	//
	//	Publishes topics <widgetId>-addChild, <widgetId>-removeChild, and <widgetId>-selectChild
	//	(where <widgetId> is the id of the TabContainer itself.

	// tabPosition: String
	//   Defines where tabs go relative to tab content.
	//   "top", "bottom", "left-h", "right-h"
	tabPosition: "top",

	templateString: null,	// override setting in StackContainer
	templateString:"<div class=\"dijitTabContainer\">\n\t<div dojoAttachPoint=\"tablistNode\"></div>\n\t<div class=\"dijitTabPaneWrapper\" dojoAttachPoint=\"containerNode\" dojoAttachEvent=\"onkeypress:_onKeyPress\" waiRole=\"tabpanel\"></div>\n</div>\n",

	postCreate: function(){	
		dijit.layout.TabContainer.superclass.postCreate.apply(this, arguments);
		// create the tab list that will have a tab (a.k.a. tab button) for each tab panel
		this.tablist = new dijit.layout.TabController(
			{
				id: this.id + "_tablist",
				tabPosition: this.tabPosition,
				doLayout: this.doLayout,
				containerId: this.id
			}, this.tablistNode);		
	},

	_setupChild: function(tab){
		dojo.addClass(tab.domNode, "dijitTabPane");
		dijit.layout.TabContainer.superclass._setupChild.apply(this, arguments);
		return tab;
	},

	startup: function(){
		if(this._started){ return; }

		// wire up the tablist and its tabs
		this.tablist.startup();
		dijit.layout.TabContainer.superclass.startup.apply(this, arguments);
		
		if(dojo.isSafari){
			// sometimes safari 3.0.3 miscalculates the height of the tab labels, see #4058
			setTimeout(dojo.hitch(this, "layout"), 0);
		}
	},

	layout: function(){
		// Summary: Configure the content pane to take up all the space except for where the tabs are
		if(!this.doLayout){ return; }

		// position and size the titles and the container node
		var titleAlign=this.tabPosition.replace(/-h/,"");
		var children = [
			{domNode: this.tablist.domNode, layoutAlign: titleAlign},
			{domNode: this.containerNode, layoutAlign: "client"}
		];
		dijit.layout.layoutChildren(this.domNode, this._contentBox, children);

		// Compute size to make each of my children.
		// children[1] is the margin-box size of this.containerNode, set by layoutChildren() call above
		this._containerContentBox = dijit.layout.marginBox2contentBox(this.containerNode, children[1]);

		if(this.selectedChildWidget){
			this._showChild(this.selectedChildWidget);
			if(this.doLayout && this.selectedChildWidget.resize){
				this.selectedChildWidget.resize(this._containerContentBox);
			}
		}
	},

	destroy: function(){
		this.tablist.destroy();
		dijit.layout.TabContainer.superclass.destroy.apply(this, arguments);
	}
});

//TODO: make private?
dojo.declare(
	"dijit.layout.TabController",
	dijit.layout.StackController,
	{
		// summary
		// 	Set of tabs (the things with titles and a close button, that you click to show a tab panel).
		//	Lets the user select the currently shown pane in a TabContainer or StackContainer.
		//	TabController also monitors the TabContainer, and whenever a pane is
		//	added or deleted updates itself accordingly.

		templateString: "<div wairole='tablist' dojoAttachEvent='onkeypress:onkeypress'></div>",

		// tabPosition: String
		//   Defines where tabs go relative to the content.
		//   "top", "bottom", "left-h", "right-h"
		tabPosition: "top",

		doLayout: true,

		// buttonWidget: String
		//	the name of the tab widget to create to correspond to each page
		buttonWidget: "dijit.layout._TabButton",

		postMixInProperties: function(){
			this["class"] = "dijitTabLabels-" + this.tabPosition + (this.doLayout ? "" : " dijitTabNoLayout");
			dijit.layout.TabController.superclass.postMixInProperties.apply(this, arguments);
		}
	}
);

dojo.declare(
	"dijit.layout._TabButton", dijit.layout._StackButton,
{
	// summary
	//	A tab (the thing you click to select a pane).
	//	Contains the title of the pane, and optionally a close-button to destroy the pane.
	//	This is an internal widget and should not be instantiated directly.

	baseClass: "dijitTab",

	templateString: "<div baseClass='dijitTab' dojoAttachEvent='onclick:onClick,onmouseover:_onMouse,onmouseout:_onMouse'>"
						+"<div class='dijitTabInnerDiv' dojoAttachPoint='innerDiv'>"
							+"<span dojoAttachPoint='containerNode,focusNode' tabIndex='-1' waiRole='tab'>${!label}</span>"
							+"<span dojoAttachPoint='closeButtonNode' class='closeImage'"
							+" dojoAttachEvent='onmouseover:_onMouse, onmouseout:_onMouse, onclick:onClickCloseButton'"
							+" baseClass='dijitTabCloseButton'>"
								+"<span dojoAttachPoint='closeText' class='closeText'>x</span>"
							+"</span>"
						+"</div>"
					+"</div>",

	postCreate: function(){
		if(this.closeButton){
			dojo.addClass(this.innerDiv, "dijitClosable");
		} else {
			this.closeButtonNode.style.display="none";
		}
		dijit.layout._TabButton.superclass.postCreate.apply(this, arguments);
		dojo.setSelectable(this.containerNode, false);
	}
});

}
