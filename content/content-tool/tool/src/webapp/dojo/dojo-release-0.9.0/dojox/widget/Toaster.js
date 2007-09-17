if(!dojo._hasResource["dojox.widget.Toaster"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.widget.Toaster"] = true;
dojo.provide("dojox.widget.Toaster");

dojo.require("dojo.fx");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

// This is mostly taken from Jesse Kuhnert's MessageNotifier.
// Modified by Bryan Forbes to support topics and a variable delay.
// Modified by Karl Tiedt to support 0 duration messages that require user interaction and message stacking

dojo.declare("dojox.widget.Toaster", [dijit._Widget, dijit._Templated], {
		// summary
		//		Message that slides in from the corner of the screen, used for notifications
		//		like "new email".
		templateString: '<div dojoAttachPoint="clipNode"><div dojoAttachPoint="containerNode" dojoAttachEvent="onclick:onSelect"><div dojoAttachPoint="contentNode"></div></div></div>',

		// messageTopic: String
		//		Name of topic; anything published to this topic will be displayed as a message.
		//		Message format is either String or an object like
		//		{message: "hello word", type: "error", duration: 500}
		messageTopic: "",

		_uniqueId: 0,
		
		// messageTypes: Enumeration
		//		Possible message types.
		messageTypes: {
			MESSAGE: "message",
			WARNING: "warning",
			ERROR: "error",
			FATAL: "fatal"
		},

		// defaultType: String
		//		If message type isn't specified (see "messageTopic" parameter),
		//		then display message as this type.
		//		Possible values in messageTypes enumeration ("message", "warning", "error", "fatal")
		defaultType: "message",

		// positionDirection: String
		//		Position from which message slides into screen, one of
		//		["br-up", "br-left", "bl-up", "bl-right", "tr-down", "tr-left", "tl-down", "tl-right"]
		positionDirection: "br-up",
		
		// positionDirectionTypes: Array
		//		Possible values for positionDirection parameter
		positionDirectionTypes: ["br-up", "br-left", "bl-up", "bl-right", "tr-down", "tr-left", "tl-down", "tl-right"],

		// duration: Integer
		//		Number of milliseconds to show message
		duration: "2000",

		//separator: String
		//		String used to separate messages if consecutive calls are made to setContent before previous messages go away
		separator: "<hr></hr>",

		postCreate: function(){
			dojox.widget.Toaster.superclass.postCreate.apply(this);
			this.hide();

			this.clipNode.className = "dijitToasterClip";
			this.containerNode.className += " dijitToasterContainer";
			this.contentNode.className = "dijitToasterContent";
			if(this.messageTopic){
				dojo.subscribe(this.messageTopic, this, "_handleMessage");
			}
		},

		_handleMessage: function(/*String|Object*/message){
			if(dojo.isString(message)){
				this.setContent(message);
			}else{
				this.setContent(message.message, message.type, message.duration);
			}
		},

		setContent: function(/*String*/message, /*String*/messageType, /*int?*/duration){
			// summary
			//		sets and displays the given message and show duration
			// message:
			//		the message
			// messageType:
			//		type of message; possible values in messageTypes enumeration ("message", "warning", "error", "fatal")
			// duration:
			//		duration in milliseconds to display message before removing it. Widget has default value.
			duration = duration||this.duration;
			// sync animations so there are no ghosted fades and such
			if(this.slideAnim){
				if(this.slideAnim.status() != "playing"){
					this.slideAnim.stop();
				}
				if(this.slideAnim.status() == "playing" || (this.fadeAnim && this.fadeAnim.status() == "playing")){
					setTimeout(dojo.hitch(this, function(){
						this.setContent(message, messageType);
					}), 50);
					return;
				}
			}

			var capitalize = function(word){
				return word.substring(0,1).toUpperCase() + word.substring(1);
			};

			// determine type of content and apply appropriately
			for(var type in this.messageTypes){
				dojo.removeClass(this.containerNode, "dijitToaster" + capitalize(this.messageTypes[type]));
			}

			dojo.style(this.containerNode, "opacity", 1);

			if(message && this.isVisible){
				message = this.contentNode.innerHTML + this.separator + message;
			}
			this.contentNode.innerHTML = message;

			dojo.addClass(this.containerNode, "dijitToaster" + capitalize(messageType || this.defaultType));

			// now do funky animation of widget appearing from
			// bottom right of page and up
			this.show();
			var nodeSize = dojo.marginBox(this.containerNode);
			
			if(this.isVisible){
				this._placeClip();
			}else{
				var style = this.containerNode.style;
				var pd = this.positionDirection;
				// sets up initial position of container node and slide-out direction
				if(pd.indexOf("-up") >= 0){
					style.left=0+"px";
					style.top=nodeSize.h + 10 + "px";
				}else if(pd.indexOf("-left") >= 0){
					style.left=nodeSize.w + 10 +"px";
					style.top=0+"px";
				}else if(pd.indexOf("-right") >= 0){
					style.left = 0 - nodeSize.w - 10 + "px";
					style.top = 0+"px";
				}else if(pd.indexOf("-down") >= 0){
					style.left = 0+"px";
					style.top = 0 - nodeSize.h - 10 + "px";
				}else{
					throw new Error(this.id + ".positionDirection is invalid: " + pd);
				}

				this.slideAnim = dojo.fx.slideTo({
					node: this.containerNode,
					top: 0, left: 0,
					duration: 450});
				dojo.connect(this.slideAnim, "onEnd", this, function(nodes, anim){
						//we build the fadeAnim here so we dont have to duplicate it later
						// can't do a fadeHide because we're fading the
						// inner node rather than the clipping node
						this.fadeAnim = dojo.fadeOut({
							node: this.containerNode,
							duration: 1000});
						dojo.connect(this.fadeAnim, "onEnd", this, function(evt){
							this.isVisible = false;
							this.hide();
						});
						//if duration == 0 we keep the message displayed until clicked
						//TODO: fix so that if a duration > 0 is displayed when a duration==0 is appended to it, the fadeOut is canceled
						if(duration>0){
							setTimeout(dojo.hitch(this, function(evt){
								// we must hide the iframe in order to fade
								// TODO: figure out how to fade with a BackgroundIframe
								if(this.bgIframe && this.bgIframe.iframe){
									this.bgIframe.iframe.style.display="none";
								}
								this.fadeAnim.play();
							}), duration);
						}else{
							dojo.connect(this, 'onSelect', this, function(evt){
								this.fadeAnim.play();
							});
						}
						this.isVisible = true;
					});
				this.slideAnim.play();
			}
		},

		_placeClip: function(){
			var view = dijit.getViewport();

			var nodeSize = dojo.marginBox(this.containerNode);

			var style = this.clipNode.style;
			// sets up the size of the clipping node
			style.height = nodeSize.h+"px";
			style.width = nodeSize.w+"px";

			// sets up the position of the clipping node
			var pd = this.positionDirection;
			if(pd.match(/^t/)){
				style.top = view.t+"px";
			}else if(pd.match(/^b/)){
				style.top = (view.h - nodeSize.h - 2 + view.t)+"px";
			}
			if(pd.match(/^[tb]r-/)){
				style.left = (view.w - nodeSize.w - 1 - view.l)+"px";
			}else if(pd.match(/^[tb]l-/)){
				style.left = 0 + "px";
			}

			style.clip = "rect(0px, " + nodeSize.w + "px, " + nodeSize.h + "px, 0px)";
			if(dojo.isIE){
				if(!this.bgIframe){
					this.clipNode.id = "__dojoXToaster_"+this._uniqueId++;
					this.bgIframe = new dijit.BackgroundIframe(this.clipNode);
//TODO					this.bgIframe.setZIndex(this.clipNode);
				}
//TODO				this.bgIframe.onResized();
				var iframe = this.bgIframe.iframe;
				iframe && (iframe.style.display="block");
			}
		},

		onSelect: function(/*Event*/e){
			// summary: callback for when user clicks the message
		},

		show: function(){
			// summary: show the Toaster
			dojo.style(this.containerNode, 'display', '');

			this._placeClip();

			if(!this._scrollConnected){
				this._scrollConnected = dojo.connect(window, "onscroll", this, this._placeClip);
			}
		},

		hide: function(){
			// summary: hide the Toaster

			//Q: ALP: I didn't port all the toggler stuff from d.w.HtmlWidget.  Is it needed? Ditto for show.
			dojo.style(this.containerNode, 'display', 'none');

			if(this._scrollConnected){
				dojo.disconnect(this._scrollConnected);
				this._scrollConnected = false;
			}

			dojo.style(this.containerNode, "opacity", 1);
		}
	}
);

}
