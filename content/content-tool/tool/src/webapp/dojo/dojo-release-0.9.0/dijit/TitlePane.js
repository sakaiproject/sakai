if(!dojo._hasResource["dijit.TitlePane"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.TitlePane"] = true;
dojo.provide("dijit.TitlePane");

dojo.require("dojo.fx");

dojo.require("dijit._Templated");
dojo.require("dijit.layout.ContentPane");

dojo.declare(
	"dijit.TitlePane",
	[dijit.layout.ContentPane, dijit._Templated],
{
	// summary
	//		A pane with a title on top, that can be opened or collapsed.

	// title: String
	//		Title of the pane
	title: "",

	// open: Boolean
	//		Whether pane is opened or closed.
	open: true,

	// duration: Integer
	//		milliseconds to fade in/fade out
	duration: 250,

	templateString:"<div class=\"dijitTitlePane\">\n\t<div dojoAttachEvent=\"onclick:toggle,onkeypress: _onTitleKey\" tabindex=\"0\"\n\t\t\twaiRole=\"button\" class=\"dijitTitlePaneTitle\" dojoAttachPoint=\"focusNode\">\n\t\t<span dojoAttachPoint=\"arrowNode\" class=\"dijitInline dijitArrowNode\"><span dojoAttachPoint=\"arrowNodeInner\" class=\"dijit_a11y dijitArrowNodeInner\"></span></span>\n\t\t<span dojoAttachPoint=\"titleNode\" class=\"dijitInlineBox dijitTitleNode\"></span>\n\t</div>\n\t<div class=\"dijitTitlePaneContentOuter\" dojoAttachPoint=\"hideNode\">\n\t\t<div class=\"dijitReset\" dojoAttachPoint=\"wipeNode\">\n\t\t\t<div class=\"dijitTitlePaneContentInner\" dojoAttachPoint=\"containerNode\" waiRole=\"region\" tabindex=\"-1\">\n\t\t\t\t<!-- nested divs because wipeIn()/wipeOut() doesn't work right on node w/padding etc.  Put padding on inner div. -->\n\t\t\t</div>\n\t\t</div>\n\t</div>\n</div>\n",

	postCreate: function(){
		this.setTitle(this.title);
		if(!this.open){
			this.hideNode.style.display = this.wipeNode.style.display = "none";
		}
		this._setCss();
		dojo.setSelectable(this.titleNode, false);
		dijit.TitlePane.superclass.postCreate.apply(this, arguments);
		dijit.wai.setAttr(this.containerNode, "waiState", "titleledby", this.titleNode.id);
		dijit.wai.setAttr(this.focusNode, "waiState", "haspopup", "true");

		// setup open/close animations
		var hideNode = this.hideNode, wipeNode = this.wipeNode;
		this._wipeIn = dojo.fx.wipeIn({
			node: this.wipeNode,
			duration: this.duration,
			beforeBegin: function(){
				hideNode.style.display="";
			}
		});
		this._wipeOut = dojo.fx.wipeOut({
			node: this.wipeNode,
			duration: this.duration,
			onEnd: function(){
				hideNode.style.display="none";
			}
		});
	},

	setContent: function(content){
		// summary
		// 		Typically called when an href is loaded.  Our job is to make the animation smooth
		if(this._wipeOut.status() == "playing"){
			// we are currently *closing* the pane, so just let that continue
			dijit.layout.ContentPane.prototype.setContent.apply(this, content);
		}else{
			if(this._wipeIn.status() == "playing"){
				this._wipeIn.stop();
			}
			
			// freeze container at current height so that adding new content doesn't make it jump
			dojo.marginBox(this.wipeNode, {h: dojo.marginBox(this.wipeNode).h});

			// add the new content (erasing the old content, if any)
			dijit.layout.ContentPane.prototype.setContent.apply(this, arguments);
			
			// call _wipeIn.play() to animate from current height to new height
			this._wipeIn.play();
		}
	},

	toggle: function(){
		// summary: switches between opened and closed state
		dojo.forEach([this._wipeIn, this._wipeOut], function(animation){
			if(animation.status() == "playing"){
				animation.stop();
			}
		});

		this[this.open ? "_wipeOut" : "_wipeIn"].play();
		this.open =! this.open;

		// load content (if this is the first time we are opening the TitlePane
		// and content is specified as an href, or we have setHref when hidden)
		this._loadCheck();

		this._setCss();
	},

	_setCss: function(){
		var classes = ["dijitClosed", "dijitOpen"];
		var boolIndex = this.open;
		dojo.removeClass(this.focusNode, classes[!boolIndex+0]);
		this.focusNode.className += " " + classes[boolIndex+0];

		// provide a character based indicator for images-off mode
		this.arrowNodeInner.innerHTML = this.open ? "-" : "+"; 
	},

	_onTitleKey: function(/*Event*/ e){
		// summary: callback when user hits a key
		if(e.keyCode == dojo.keys.ENTER || e.charCode == dojo.keys.SPACE){
			this._onTitleClick();
		}
		else if(e.keyCode == dojo.keys.DOWN_ARROW){
			if(this.open){
				this.containerNode.focus();
				e.preventDefault();
			}
	 	}
	},

	setTitle: function(/*String*/ title){
		// summary: sets the text of the title
		this.titleNode.innerHTML=title;
	}
});

}
