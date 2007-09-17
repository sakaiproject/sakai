if(!dojo._hasResource["dojox.presentation._base"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.presentation._base"] = true;
dojo.provide("dojox.presentation._base");
dojo.experimental("dojox.presentation"); 

dojo.require("dijit._Widget");
dojo.require("dijit._Container"); 
dojo.require("dijit._Templated");
dojo.require("dijit.layout.StackContainer"); 
dojo.require("dijit.layout.ContentPane"); 
dojo.require("dojo.fx"); 

dojo.declare("dojox.presentation.Deck", [ dijit.layout.StackContainer, dijit._Templated ], {
	// summary:
	//	dojox.presentation class
	//	basic powerpoint esque engine for handling transitons and control
	//	in a page-by-page and part-by-part way
	//	
	//	TODO: [off topic] make slideshow be a presentation of images as children
	//	TODO: make ^^ that possible via stripping this code down for mixins
	//	TODO: port 0.4.x toggle into 0.9 for this idea specifically / globally?
	// 	FIXME: parsing part(s)/widget(s) in href="" Slides not working
	//	TODO: make auto actions progress. 
	//	FIXME: Safari keydown/press/up listener not working. 
	//	noClick=true prevents progression of slides in that broweser
	
	// fullScreen: Boolean
	// 	unsupported (that i know of) just yet. Default it to take control
	//	of window. Would be nice to be able to contain presentation in a 
	//	styled container, like StackContainer ... theoretically possible.
	//	[and may not need this variable?]
	fullScreen: true,

	// useNav: Boolean
	//	true to allow navigation popup, false to disallow
	useNav: true,

	// navDuration: Integer
	//	time in MS fadein/out of popup nav [default: 250]
	navDuration: 250,

	// noClick: Boolean
	//	if true, prevents _any_ click events to propagate actions
	//	(limiting control to keyboard and/or action.on="auto" or action.delay=""
	//	actions.
	noClick: false,

	// setHash: Boolean
	//	if true, window location bar will get a #link to slide for direct
	//	access to a particular slide number.
	setHash: true,

	// just to over-ride:
	templateString: null,
	templateString:"<div class=\"dojoShow\" dojoAttachPoint=\"showHolder\">\n\t<div class=\"dojoShowNav\" dojoAttachPoint=\"showNav\" dojoAttachEvent=\"onmouseover: _showNav, onmouseout: _hideNav\">\n\t<div class=\"dojoShowNavToggler\" dojoAttachPoint=\"showToggler\">\n\t\t<img dojoAttachPoint=\"prevNode\" src=\"${prevIcon}\" dojoAttachEvent=\"onclick:previousSlide\">\n\t\t<select dojoAttachEvent=\"onchange:_onEvent\" dojoAttachPoint=\"select\">\n\t\t\t<option dojoAttachPoint=\"_option\">Title</option>\n\t\t</select>\n\t\t<img dojoAttachPoint=\"nextNode\" src=\"${nextIcon}\" dojoAttachEvent=\"onclick:nextSlide\">\n\t</div>\n\t</div>\n\t<div dojoAttachPoint=\"containerNode\"></div>\n</div>\n",

	// nextIcon: String
	//	icon for navigation "next" button
	nextIcon: dojo.moduleUrl('dojox.presentation','resources/icons/next.png'),

	// prevIcon: String
	// 	icon for navigation "previous" button
	prevIcon: dojo.moduleUrl('dojox.presentation','resources/icons/prev.png'),

	_navOpacMin: 0,
	_navOpacMax: 0.85,
	_slideIndex: 0,
	
	// Private:
	_slides: [], 
	_navShowing: true,
	_inNav: false,
	
	startup: function(){
		// summary: connect to the various handlers and controls for this presention
		dojox.presentation.Deck.superclass.startup.call(this);

		if(this.useNav){ 
			this._hideNav(); 
		}else{ 
			this.showNav.style.display = "none"; 
		} 

		this.connect(document,'onclick', '_onEvent');
		this.connect(document,'onkeypress', '_onEvent');
		
		// only if this.fullScreen == true?
		this.connect(window, 'onresize', '_resizeWindow');
		this._resizeWindow();
		
		this._updateSlides(); 
		
		this._readHash();
		this._setHash();
	},

	moveTo: function(/* Integer */ number){
		var slideIndex = number - 1; 
		
		if(slideIndex < 0)
			slideIndex = 0;
		
		if(slideIndex > this._slides.length - 1)
			slideIndex = this._slides.length - 1; 
		
		this._gotoSlide(slideIndex);
	},

	onMove: function (number){
		// summary: stub function? TODOC: ?
	},
	
	nextSlide: function(/*Event*/ evt){
		// summary: transition to the next slide.
		if (!this.selectedChildWidget.isLastChild) {
			this._gotoSlide(this._slideIndex+1);
		}
		if (evt) { evt.stopPropagation(); }
	},

	previousSlide: function(/*Event*/ evt){
		// summary: transition to the previous slide
		if (!this.selectedChildWidget.isFirstChild) {
			
			this._gotoSlide(this._slideIndex-1);
			
		} else { this.selectedChildWidget._reset(); } 
		if (evt) { evt.stopPropagation();}
	},

	getHash: function(id){
		return this.id+"_SlideNo_"+id;
	},
	
	_hideNav: function(evt){
		// summary: hides navigation
		if(this._navAnim){ this._navAnim.stop(); }
		this._navAnim = dojo.animateProperty({
			node:this.showNav, 
			duration:this.navDuration, 
			properties: {
				opacity: { end:this._navOpacMin } 
			}
		}).play();
	},

	_showNav: function(evt){
		// summary: shows navigation
		if(this._navAnim){ this._navAnim.stop(); }
		this._navAnim = dojo.animateProperty({
			node:this.showNav, 
			duration:this.navDuration, 
			properties: { 
				opacity: { end:this._navOpacMax }
			}
		}).play();
	},

	_handleNav: function(evt){
		// summary: does nothing? _that_ seems useful.
		evt.stopPropagation(); 
	},

	_updateSlides: function(){
		// summary: 
		//		populate navigation select list with refs to slides call this
		//		if you add a node to your presentation dynamically.
		this._slides = this.getChildren(); 
		if(this.useNav){
			// populate the select box with top-level slides
			var i=0;
			dojo.forEach(this._slides,dojo.hitch(this,function(slide){
				i++;
				var tmp = this._option.cloneNode(true);
				tmp.text = slide.title+" ("+i+") ";
				this._option.parentNode.insertBefore(tmp,this._option);
			}));
			if(this._option.parentNode){
				this._option.parentNode.removeChild(this._option);
			}
			// dojo._destroyElement(this._option); 
		}
	},

	_onEvent: function(/* Event */ evt){
		// summary: 
		//		main presentation function, determines next 'best action' for a
		//		specified event.
		var _node = evt.target;
		var _type = evt.type;

		if(_type == "click" || _type == "change"){
			if(_node.index && _node.parentNode == this.select){ 
				this._gotoSlide(_node.index);
			}else if(_node == this.select){
				this._gotoSlide(_node.selectedIndex);
			}else{
				if (this.noClick || this.selectedChildWidget.noClick || this._isUnclickable(evt)) return; 
				this.selectedChildWidget._nextAction(evt);
			}
		}else if(_type=="keydown" || _type == "keypress"){
			
			// FIXME: safari doesn't report keydown/keypress?
			
			var key = (evt.charCode == dojo.keys.SPACE ? dojo.keys.SPACE : evt.keyCode);
			switch(key){
				case dojo.keys.DELETE:
				case dojo.keys.BACKSPACE:
				case dojo.keys.LEFT_ARROW:
				case dojo.keys.UP_ARROW:
				case dojo.keys.PAGE_UP:
				case 80:	// key 'p'
					this.previousSlide(evt);
					break;

				case dojo.keys.ENTER:
				case dojo.keys.SPACE:
				case dojo.keys.RIGHT_ARROW:
				case dojo.keys.DOWN_ARROW:
				case dojo.keys.PAGE_DOWN: 
				case 78:	// key 'n'
					this.selectedChildWidget._nextAction(evt); 
					break;

				case dojo.keys.HOME:	this._gotoSlide(0);
			}
		}
		this._resizeWindow();
		evt.stopPropagation(); 
	},
		
	_gotoSlide: function(/* Integer */ slideIndex){

		this.selectChild(this._slides[slideIndex]);
		this.selectedChildWidget._reset();

		this._slideIndex = slideIndex;
		
		if(this.useNav){
			this.select.selectedIndex = slideIndex; 
		}
		
		if(this.setHash){ 
			this._setHash(); 
		}
		this.onMove(this._slideIndex+1);
	},

	_isUnclickable: function(/* Event */ evt){
		// summary: returns true||false base of a nodes click-ability 
		var nodeName = evt.target.nodeName.toLowerCase();
		// TODO: check for noClick='true' in target attrs & return true
		// TODO: check for relayClick='true' in target attrs & return false
		switch(nodeName){
			case 'a' : 
			case 'input' :
			case 'textarea' : return true; break;
		}
		return false; 
	},

	_readHash: function(){
		var th = window.location.hash;
		if (th.length && this.setHash) {
			var parts = (""+window.location).split(this.getHash(''));
			if(parts.length>1){
				this._gotoSlide(parseInt(parts[1])-1);
			}
		}
	},

	_setHash: function(){
		// summary: sets url #mark to direct slide access
		if(this.setHash){
			var slideNo = this._slideIndex+1;
			window.location.href = "#"+this.getHash(slideNo);	
		}
	},

	_resizeWindow: function(/*Event*/ evt){
		// summary: resize this and children to fix this window/container

		// only if this.fullScreen?
		dojo.body().style.height = "auto";
		var wh = dijit.getViewport(); 
		var h = Math.max(
			document.documentElement.scrollHeight || dojo.body().scrollHeight,
			wh.h);
		var w = wh.w; 
		this.selectedChildWidget.domNode.style.height = h +'px';
		this.selectedChildWidget.domNode.style.width = w +'px';
	},

	_transition: function(newWidget,oldWidget){ 
		// summary: over-ride stackcontainers _transition method
		//	but atm, i find it to be ugly with not way to call
		//	_showChild() without over-riding it too. hopefull
		//	basic toggles in superclass._transition will be available
		//	in dijit, and this won't be necessary.
		var anims = [];
		if(oldWidget){
			/*
			anims.push(dojo.fadeOut({ node: oldWidget.domNode, 
				duration:250, 
				onEnd: dojo.hitch(this,function(){
					this._hideChild(oldWidget);
				})
			}));
			*/
			this._hideChild(oldWidget);
		}
		if(newWidget){
			/*
			anims.push(dojo.fadeIn({ 
				node:newWidget.domNode, start:0, end:1, 
				duration:300, 
				onEnd: dojo.hitch(this,function(){
					this._showChild(newWidget);
					newWidget._reset();
					}) 
				})
			);
			*/
			this._showChild(newWidget);
			newWidget._reset();
		}
		//dojo.fx.combine(anims).play();
	}
});

dojo.declare(
	"dojox.presentation.Slide",
	[dijit.layout.ContentPane,dijit._Contained,dijit._Container,dijit._Templated],
	{
	// summary:
	//	a Comonent of a dojox.presentation, and container for each 'Slide'
	//	made up of direct HTML (no part/action relationship), and dojox.presentation.Part(s),
	//	and their attached Actions.

	// templatPath: String
	//	make a ContentPane templated, and style the 'titleNode'
	templateString:"<div dojoAttachPoint=\"showSlide\" class=\"dojoShowPrint dojoShowSlide\">\n\t<h1 class=\"showTitle\" dojoAttachPoint=\"slideTitle\"><span class=\"dojoShowSlideTitle\" dojoAttachPoint=\"slideTitleText\">${title}</span></h1>\n\t<div class=\"dojoShowBody\" dojoAttachPoint=\"containerNode\"></div>\n</div>\n",

	// title: String
	//	string to insert into titleNode, title of Slide
	title: "",

	// inherited from ContentPane FIXME: don't seem to work ATM?
	refreshOnShow: true, 
	preLoad: false,
	doLayout: true,
	parseContent: true,

	// noClick: Boolean
	// 	true on slide tag prevents clicking, false allows
	// 	(can also be set on base presentation for global control)
	noClick: false,

	// private holders:
	_parts: [],
	_actions: [],
	_actionIndex: 0,
	_runningDelay: false,	

	startup: function(){
		// summary: setup this slide with actions and components (Parts)
		this.slideTitleText.innerHTML = this.title; 
		var children = this.getChildren();
		this._actions = [];
		dojo.forEach(children,function(child){
			var tmpClass = child.declaredClass.toLowerCase();
			switch(tmpClass){
				case "dojox.presentation.part" : this._parts.push(child); break;
				case "dojox.presentation.action" : this._actions.push(child); break;
			}
		},this);
	},	


	_nextAction: function(evt){	
		// summary: gotoAndPlay current cached action
		var tmpAction = this._actions[this._actionIndex] || 0;
		if (tmpAction){
			// is this action a delayed action? [auto? thoughts?]
			if(tmpAction.on == "delay"){
				this._runningDelay = setTimeout(
					dojo.hitch(tmpAction,"_runAction"),tmpAction.delay
					);
				console.debug('started delay action',this._runningDelay); 
			}else{
				tmpAction._runAction();
			}

			// FIXME: it gets hairy here. maybe runAction should 
			// call _actionIndex++ onEnd? if a delayed action is running, do
			// we want to prevent action++?
			var tmpNext = this._getNextAction();
			this._actionIndex++;

			if(tmpNext.on == "delay"){
				// FIXME: yeah it looks like _runAction() onend should report
				// _actionIndex++
				console.debug('started delay action',this._runningDelay); 
				setTimeout(dojo.hitch(tmpNext,"_runAction"),tmpNext.delay);
			}
		}else{
			// no more actions in this slide
			this.getParent().nextSlide(evt);
		}	
	},

	_getNextAction: function(){
		// summary: returns the _next action in this sequence
		return this._actions[this._actionIndex+1] || 0;
	},

	_reset: function(){
		// summary: set action chain back to 0 and re-init each Part
		this._actionIndex = [0];
		dojo.forEach(this._parts,function(part){
			part._reset();
		},this);
	}
});

dojo.declare("dojox.presentation.Part", [dijit._Widget,dijit._Contained], {
	// summary: 
	//	dojox.presentation.Part
	//	a node in a presentation.Slide that inherists control from a
	//	dojox.presentation.Action
	//	can be any element type, and requires styling before parsing
	
	// as: String
	//	like an ID, attach to Action via (part) as="" / (action) forSlide="" tags
	//	this should be unique identifier?
	as:null,
	
	// startVisible: boolean
	//	true to leave in page on slide startup/reset
	//	false to hide on slide startup/reset
	startVisible: false,

	// isShowing: Boolean,
	//	private holder for _current_ state of Part
	_isShowing: false,

	postCreate: function(){
		// summary: override and init() this component
		this._reset();
	},

	_reset: function(){
		// summary: set part back to initial calculate state
		// these _seem_ backwards, but quickToggle flips it
		this._isShowing =! this.startVisible; 
		this._quickToggle();
	},

	_quickToggle: function(){
	// summary: ugly [unworking] fix to test setting state of component
	//	before/after an animation. display:none prevents fadeIns?
		if(this._isShowing){
			dojo.style(this.domNode,'display','none');	
			dojo.style(this.domNode,'visibility','hidden');
			dojo.style(this.domNode,'opacity',0);
		}else{
                        dojo.style(this.domNode,'display',''); 
			dojo.style(this.domNode,'visibility','visible'); 
			dojo.style(this.domNode,'opacity',1);
		}
		this._isShowing =! this._isShowing; 
	}
});

dojo.declare("dojox.presentation.Action", [dijit._Widget,dijit._Contained], {
	// summary:	
	//	dojox.presention.Action:
	//	a widget to attach to a dojox.presentation.Part to control
	//	it's properties based on an inherited chain of events ...
	//

	// on: String
	//	FIXME: only 'click' supported ATM. plans include on="delay", 
	//	on="end" of="", and on="auto". those should make semantic sense
	//	to you.	
	on: 'click',

	// forSlide: String
	//	attach this action to a dojox.presentation.Part with a matching 'as' attribute
	forSlide: null,

	// toggle: String
	//	will toggle attached [matching] node(s) via forSlide/as relationship(s)
	toggle: 'fade',
	
	// delay: Integer 
	//	
	delay: 0,

	// duration: Integer
	//	default time in MS to run this action effect on it's 'forSlide' node
	duration: 1000,

	// private holders:
	_attached: [],
	_nullAnim: false,

	_runAction: function(){
		// summary: runs this action on attached node(s)

		var anims = [];
		// executes the action for each attached 'Part' 
		dojo.forEach(this._attached,function(node){
			// FIXME: this is ugly, and where is toggle class? :(
			var dir = (node._isShowing) ? "Out" : "In";
			// node._isShowing =! node._isShowing; 
			node._quickToggle(); // (?) this is annoying
			//var _anim = dojox.fx[ this.toggle ? this.toggle+dir : "fade"+dir]({ 
			var _anim = dojo.fadeIn({
				node:node.domNode, 
				duration: this.duration
				//beforeBegin: dojo.hitch(node,"_quickToggle")
			});
			anims.push(_anim);
		},this);
		var _anim = dojo.fx.combine(anims);
		if(_anim){ _anim.play(); }
	},

	_getSiblingsByType: function(/* String */ declaredClass){
		// summary: quick replacement for getChildrenByType("class"), but in 
		// a child here ... so it's getSiblings. courtesy bill in #dojo 
		// could be moved into parent, and just call this.getChildren(),
		// which makes more sense.
		var siblings = dojo.filter( this.getParent().getChildren(), function(widget){ 
			return widget.declaredClass==declaredClass;
			} 
		);
		return siblings;
	}, 
	
	postCreate: function(){
		// summary: run this once, should this be startup: function()?

		// prevent actions from being visible, _always_
		dojo.style(this.domNode,"display","none"); 
 		var parents = this._getSiblingsByType('dojox.presentation.Part');
		// create a list of "parts" we are attached to via forSlide/as 
		this._attached = [];
		dojo.forEach(parents,function(parentPart){
			if(this.forSlide == parentPart.as){ 
				this._attached.push(parentPart); 
			}
		},this);
	}	

});

}
