if(!dojo._hasResource["dijit.Tree"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.Tree"] = true;
dojo.provide("dijit.Tree");

dojo.require("dojo.fx");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit._Container");
dojo.require("dijit._tree.Controller");

dojo.declare(
	"dijit._TreeBase",
	[dijit._Widget, dijit._Templated, dijit._Container, dijit._Contained],
{
	// summary:
	//	Base class for Tree and _TreeNode

	// state: String
	//		dynamic loading-related stuff.
	//		When an empty folder node appears, it is "UNCHECKED" first,
	//		then after dojo.data query it becomes "LOADING" and, finally "LOADED"	
	state: "UNCHECKED",
	locked: false,

	lock: function(){
		// summary: lock this node (and it's descendants) while a delete is taking place?
		this.locked = true;
	},
	unlock: function(){
		if(!this.locked){
			//dojo.debug((new Error()).stack);
			throw new Error(this.declaredClass+" unlock: not locked");
		}
		this.locked = false;
	},

	isLocked: function(){
		// summary: can this node be modified?
		// returns: false if this node or any of it's ancestors are locked
		var node = this;
		while(true){
			if(node.lockLevel){
				return true;
			}
			if(!node.getParent() || node.isTree){
				break;
			}	
			node = node.getParent();	
		}
		return false;
	},

	setChildren: function(/* Object[] */ childrenArray){
		// summary:
		//		Sets the children of this node.
		//		Sets this.isFolder based on whether or not there are children
		// 		Takes array of objects like: {label: ...} (_TreeNode options basically)
		//		See parameters of _TreeNode for details.

		this.destroyDescendants();

		this.state = "LOADED";
		var nodeMap= {};
		if(childrenArray && childrenArray.length > 0){
			this.isFolder = true;
			if(!this.containerNode){ // maybe this node was unfolderized and still has container
				this.containerNode = this.tree.containerNodeTemplate.cloneNode(true);
				this.domNode.appendChild(this.containerNode);
			}

			// Create _TreeNode widget for each specified tree node
			dojo.forEach(childrenArray, function(childParams){
				var child = new dijit._TreeNode(dojo.mixin({
					tree: this.tree,
					label: this.tree.store.getLabel(childParams.item)
				}, childParams));
				this.addChild(child);
				nodeMap[this.tree.store.getIdentity(childParams.item)] = child;
			}, this);

			// note that updateLayout() needs to be called on each child after
			// _all_ the children exist
			dojo.forEach(this.getChildren(), function(child, idx){
				child._updateLayout();
			});

		}else{
			this.isFolder=false;
		}
		
		if(this.isTree){
			// put first child in tab index if one exists.
			var fc = this.getChildren()[0];
			var tabnode = fc ? fc.labelNode : this.domNode; 
			tabnode.setAttribute("tabIndex", "0");
		}

		return nodeMap;
	},

	addChildren: function(/* object[] */ childrenArray){
		// summary:
		//		adds the children to this node.
		// 		Takes array of objects like: {label: ...}  (_TreeNode options basically)

		//		See parameters of _TreeNode for details.
		var nodeMap = {};
		if (childrenArray && childrenArray.length > 0){
			dojo.forEach(childrenArray, function(childParams){
				var child = new dijit._TreeNode(
					dojo.mixin({
						tree: this.tree,
						label: this.tree.store.getLabel(childParams.item)
					}, childParams)
				);
				this.addChild(child);
				nodeMap[this.tree.store.getIdentity(childParams.item)] = child;
			}, this);
	
			dojo.forEach(this.getChildren(), function(child, idx){
				child._updateLayout();
			});
		}
	
		return nodeMap;
	},

	deleteNode: function(/* treeNode */ node) {
		node.destroy();
	
		dojo.forEach(this.getChildren(), function(child, idx){
			child._updateLayout();
		});
	},

	makeFolder: function() {
		//summary: if this node wasn't already a folder, turn it into one and call _setExpando()
		this.isFolder=true;
		this._setExpando(false);
	}
});

dojo.declare(
	"dijit.Tree",
	dijit._TreeBase,
{
	// summary
	//	Tree view does all the drawing, visual node management etc.
	//	Throws events about clicks on it, so someone may catch them and process
	//	Events:
	//		afterTreeCreate,
	//		beforeTreeDestroy,
	//		execute				: for clicking the label, or hitting the enter key when focused on the label,
	//		toggleOpen			: for clicking the expando key (toggles hide/collapse),
	//		previous			: go to previous visible node,
	//		next				: go to next visible node,
	//		zoomIn				: go to child nodes,
	//		zoomOut				: go to parent node

	// store: String||dojo.data.Store
	//	The store to get data to display in the tree
	store: null,

	// query: String
	//	query to get top level node(s) of tree (ex: {type:'continent'})
	query: null,

	// childrenAttr: String
	//		name of attribute that holds children of a tree node
	childrenAttr: "children",

	templateString:"<div class=\"dijitTreeContainer\" style=\"\" waiRole=\"tree\"\n\tdojoAttachEvent=\"onclick:_onClick,onkeypress:_onKeyPress\"\n></div>\n",		

	isExpanded: true, // consider this "root node" to be always expanded

	isTree: true,

	_publish: function(/*String*/ topicName, /*Object*/ message){
		// summary:
		//		Publish a message for this widget/topic
		dojo.publish(this.id, [dojo.mixin({tree: this, event: topicName}, message||{})]);
	},

	postMixInProperties: function(){
		this.tree = this;

		// setup table mapping keys to events
		var keyTopicMap = {};
		keyTopicMap[dojo.keys.ENTER]="execute";
		keyTopicMap[dojo.keys.LEFT_ARROW]="zoomOut";
		keyTopicMap[dojo.keys.RIGHT_ARROW]="zoomIn";
		keyTopicMap[dojo.keys.UP_ARROW]="previous";
		keyTopicMap[dojo.keys.DOWN_ARROW]="next";
		keyTopicMap[dojo.keys.HOME]="first";
		keyTopicMap[dojo.keys.END]="last";
		this._keyTopicMap = keyTopicMap;
	},

	postCreate: function(){
		this.containerNode = this.domNode;

		// make template for container node (we will clone this and insert it into
		// any nodes that have children)
		var div = document.createElement('div');
		div.style.display = 'none';
		div.className = "dijitTreeContainer";	
		dijit.wai.setAttr(div, "waiRole", "role", "presentation");
		this.containerNodeTemplate = div;


		// start the controller, passing in the store
		this._controller = new dijit._tree.DataController(
			{	
				store: this.store,
				treeId: this.id,
				query: this.query,
				childrenAttr: this.childrenAttr
			}
		);

		this._publish("afterTreeCreate");
	},

	destroy: function(){
		// publish destruction event so that any listeners should stop listening
		this._publish("beforeTreeDestroy");
		return dijit._Widget.prototype.destroy.apply(this, arguments);
	},

	toString: function(){
		return "["+this.declaredClass+" ID:"+this.id+"]";
	},

	getIconClass: function(/*dojo.data.Item*/ item){
		// summary: user overridable class to return CSS class name to display icon
	},

	_domElement2TreeNode: function(/*DomNode*/ domElement){
		var ret;
		do{
			ret=dijit.byNode(domElement);
		}while(!ret && (domElement = domElement.parentNode));
		return ret;
	},

	_onClick: function(/*Event*/ e){
		// summary: translates click events into commands for the controller to process
		var domElement = e.target;

		// find node
		var nodeWidget = this._domElement2TreeNode(domElement);	
		if(!nodeWidget || !nodeWidget.isTreeNode){
			return;
		}

		if(domElement == nodeWidget.expandoNode ||
			 domElement == nodeWidget.expandoNodeText){
			// expando node was clicked
			if(nodeWidget.isFolder){
				this._publish("toggleOpen", {node:nodeWidget});
			}
		}else{
			this._publish("execute", { item: nodeWidget.item, node: nodeWidget} );
			this.onClick(nodeWidget.item, nodeWidget);
		}
		dojo.stopEvent(e);
	},

	onClick: function(/* dojo.data */ item){
		// summary: user overridable function
		console.log("default onclick handler", item);
	},

	_onKeyPress: function(/*Event*/ e){
		// summary: translates keypress events into commands for the controller
		if(e.altKey){ return; }
		var treeNode = this._domElement2TreeNode(e.target);
		if(!treeNode){ return; }

		// Note: On IE e.keyCode is not 0 for printables so check e.charCode.
		// In dojo charCode is universally 0 for non-printables.
		if(e.charCode){  // handle printables (letter navigation)
			// Check for key navigation.
			var navKey = e.charCode;
			if(!e.altKey && !e.ctrlKey && !e.shiftKey && !e.metaKey){
				navKey = (String.fromCharCode(navKey)).toLowerCase();
				this._publish("letterKeyNav", { node: treeNode, key: navKey } );
				dojo.stopEvent(e);
			}
		}else{  // handle non-printables (arrow keys)
			if(this._keyTopicMap[e.keyCode]){
				this._publish(this._keyTopicMap[e.keyCode], { node: treeNode, item: treeNode.item } );	
				dojo.stopEvent(e);
			}
		}
	},

	blurNode: function(){
		// summary
		//	Removes focus from the currently focused node (which must be visible).
		//	Usually not called directly (just call focusNode() on another node instead)
		var node = this.lastFocused;
		if(!node){ return; }
		var labelNode = node.labelNode;
		dojo.removeClass(labelNode, "dijitTreeLabelFocused");
		labelNode.setAttribute("tabIndex", "-1");
		this.lastFocused = null;
	},

	focusNode: function(/* _tree.Node */ node){
		// summary
		//	Focus on the specified node (which must be visible)

		this.blurNode();

		// set tabIndex so that the tab key can find this node
		var labelNode = node.labelNode;
		labelNode.setAttribute("tabIndex", "0");

		this.lastFocused = node;
		dojo.addClass(labelNode, "dijitTreeLabelFocused");

		// set focus so that the label wil be voiced using screen readers
		labelNode.focus();
	},
	
	_onBlur: function(){
		// summary:
		// 		We've moved away from the whole tree.  The currently "focused" node
		//		(see focusNode above) should remain as the lastFocused node so we can
		//		tab back into the tree.  Just change CSS to get rid of the dotted border
		//		until that time
		if(this.lastFocused){
			var labelNode = this.lastFocused.labelNode;
			dojo.removeClass(labelNode, "dijitTreeLabelFocused");	
		}
	},
	
	_onFocus: function(){
		// summary:
		//		If we were previously on the tree, there's a currently "focused" node
		//		already.  Just need to set the CSS back so it looks focused.
		if(this.lastFocused){
			var labelNode = this.lastFocused.labelNode;
			dojo.addClass(labelNode, "dijitTreeLabelFocused");			
		}
	}
});

dojo.declare(
	"dijit._TreeNode",
	dijit._TreeBase,
{
	// summary
	//		Single node within a tree

	templateString:"<div class=\"dijitTreeNode dijitTreeExpandLeaf dijitTreeChildrenNo\" waiRole=\"presentation\"\n\t><span dojoAttachPoint=\"expandoNode\" class=\"dijitTreeExpando\" waiRole=\"presentation\"\n\t></span\n\t><span dojoAttachPoint=\"expandoNodeText\" class=\"dijitExpandoText\" waiRole=\"presentation\"\n\t></span\n\t>\n\t<div dojoAttachPoint=\"contentNode\" class=\"dijitTreeContent\" waiRole=\"presentation\">\n\t\t<div dojoAttachPoint=\"iconNode\" class=\"dijitInline dijitTreeIcon\" waiRole=\"presentation\"></div>\n\t\t<span dojoAttachPoint=labelNode class=\"dijitTreeLabel\" wairole=\"treeitem\" expanded=\"true\" tabindex=\"-1\"></span>\n\t</div>\n</div>\n",		

	// item: dojo.data.Item
	//		the dojo.data entry this tree represents
	item: null,	

	isTreeNode: true,

	// label: String
	//		Text of this tree node
	label: "",

	isFolder: null, // set by widget depending on children/args

	isExpanded: false,

	postCreate: function(){
		// set label, escaping special characters
		this.labelNode.innerHTML = "";
		this.labelNode.appendChild(document.createTextNode(this.label));
		
		// set expand icon for leaf 	
		this._setExpando();
		
		// set icon based on item
		dojo.addClass(this.iconNode, this.tree.getIconClass(this.item));
	},

	markProcessing: function(){
		// summary: visually denote that tree is loading data, etc.
		this.state = "LOADING";
		this._setExpando(true);	
	},

	unmarkProcessing: function(){
		// summary: clear markup from markProcessing() call
		this._setExpando(false);	
	},
	
	_updateLayout: function(){
		// summary: set appropriate CSS classes for this.domNode

		dojo.removeClass(this.domNode, "dijitTreeIsRoot");
		if(this.getParent()["isTree"]){
			dojo.addClass(this.domNode, "dijitTreeIsRoot");
		}

		dojo.removeClass(this.domNode, "dijitTreeIsLast");
		if(!this.getNextSibling()){
			dojo.addClass(this.domNode, "dijitTreeIsLast");	
		}
	},

	_setExpando: function(/*Boolean*/ processing){
		// summary: set the right image for the expando node

		// apply the appropriate class to the expando node
		var styles = ["dijitTreeExpandoLoading", "dijitTreeExpandoOpened",
			"dijitTreeExpandoClosed", "dijitTreeExpandoLeaf"];
		var idx = processing ? 0 : (this.isFolder ?	(this.isExpanded ? 1 : 2) : 3);
		dojo.forEach(styles,
			function(s){
				dojo.removeClass(this.expandoNode, s);
			}, this
		);
		dojo.addClass(this.expandoNode, styles[idx]);

		// provide a non-image based indicator for images-off mode
		this.expandoNodeText.innerHTML =
			processing ? "*" :
				(this.isFolder ?
					(this.isExpanded ? "-" : "+") : "*");
	},	

	setChildren: function(items){
		var ret = dijit.Tree.superclass.setChildren.apply(this, arguments);

		// create animations for showing/hiding the children
		this._wipeIn = dojo.fx.wipeIn({node: this.containerNode, duration: 250});
		dojo.connect(this.wipeIn, "onEnd", dojo.hitch(this, "_afterExpand"));
		this._wipeOut = dojo.fx.wipeOut({node: this.containerNode, duration: 250});
		dojo.connect(this.wipeOut, "onEnd", dojo.hitch(this, "_afterCollapse"));

		return ret;
	},

	expand: function(){
        // summary: show my children
		if(this.isExpanded){ return; }

		// cancel in progress collapse operation
		if(this._wipeOut.status() == "playing"){
			this._wipeOut.stop();
		}

		this.isExpanded = true;
		dijit.wai.setAttr(this.labelNode, "waiState", "expanded", "true");
		dijit.wai.setAttr(this.containerNode, "waiRole", "role", "group");

		this._setExpando();

		// TODO: use animation that's constant speed of movement, not constant time regardless of height
		this._wipeIn.play();
	},

	_afterExpand: function(){
        this.onShow();
 		this._publish("afterExpand", {node: this});		
	},

	collapse: function(){					
		if(!this.isExpanded){ return; }

		// cancel in progress expand operation
		if(this._wipeIn.status() == "playing"){
			this._wipeIn.stop();
		}

		this.isExpanded = false;
		dijit.wai.setAttr(this.labelNode, "waiState", "expanded", "false");
		this._setExpando();

		this._wipeOut.play();
	},

	_afterCollapse: function(){
		this.onHide();
		this._publish("afterCollapse", {node: this});
	},


	setLabelNode: function(label) {
		this.labelNode.innerHTML="";
		this.labelNode.appendChild(document.createTextNode(label));
	},


	toString: function(){
		return '['+this.declaredClass+', '+this.label+']';
	}
});

}
