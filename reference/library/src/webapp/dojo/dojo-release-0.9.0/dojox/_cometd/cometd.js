if(!dojo._hasResource["dojox._cometd.cometd"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox._cometd.cometd"] = true;
dojo.provide("dojox._cometd.cometd");
dojo.require("dojo.AdapterRegistry");
// FIXME: determine if we can use XMLHTTP to make x-domain posts despite not
//        being able to hear back about the result
dojo.require("dojo.io.script");
// dojo.require("dojo.cookie"); // for peering

/*
 * this file defines Comet protocol client. Actual message transport is
 * deferred to one of several connection type implementations. The default is a
 * forever-frame implementation. A single global object named "cometd" is
 * used to mediate for these connection types in order to provide a stable
 * interface.
 */

dojox.cometd = new function(){

	this.initialized = false;
	this.connected = false;

	this.connectionTypes = new dojo.AdapterRegistry(true);

	this.version = 0.1;
	this.minimumVersion = 0.1;
	this.clientId = null;

	this._isXD = false;
	this.handshakeReturn = null;
	this.currentTransport = null;
	this.url = null;
	this.lastMessage = null;
	this.globalTopicChannels = {};
	this.backlog = [];
	this.handleAs="json-comment-optional";
	this.advice;

	this._subscriptions = [];

	this.tunnelInit = function(childLocation, childDomain){
		// placeholder
	}

	this.tunnelCollapse = function(){
		console.debug("tunnel collapsed!");
		// placeholder
	}

	this.init = function(props, root, bargs){
		if(dojo.isString(props)){
			var oldRoot = root;
			root = props;
			props = oldRoot;
		}
		// FIXME: if the root isn't from the same host, we should automatically
		// try to select an XD-capable transport
		props = props||{};
		// go ask the short bus server what we can support
		props.version = this.version;
		props.minimumVersion = this.minimumVersion;
		props.channel = "/meta/handshake";

        props.ext = { "json-comment-filtered": true };

		// FIXME: what about ScriptSrcIO for x-domain comet?
		this.url = root||djConfig["cometdRoot"];
		if(!this.url){
			console.debug("no cometd root specified in djConfig and no root passed");
			return;
		}
		
		// FIXME: we need to select a way to handle JSONP-style stuff
		// generically here. We already know if the server is gonna be on
		// another domain (or can know it), so we should select appropriate
		// negotiation methods here as well as in final transport type
		// selection.
		var bindArgs = {
			url: this.url,
			handleAs: this.handleAs,
			content: { "message": dojo.toJson([props]) },
			callbackParamName: "jsonp", // usually ignored
			load: dojo.hitch(this, "finishInit"),
			error: function(e){ console.debug("handshake error!:", e); }
		};

		// borrowed from dojo.uri.Uri in lieu of fixed host and port properties
        var regexp = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
		var r = (""+window.location).match(new RegExp(regexp));
		if(r[4]){
			var tmp = r[4].split(":");
			var thisHost = tmp[0];
			var thisPort = tmp[1]||"80"; // FIXME: match 443

			r = this.url.match(new RegExp(regexp));
			if(r[4]){
				tmp = r[4].split(":");
				var urlHost = tmp[0];
				var urlPort = tmp[1]||"80";
				this._isXD = ((urlHost != thisHost)||(urlPort != thisPort));
			}
		}
		if(bargs){
			dojo.mixin(bindArgs, bargs);
		}
		if(this._isXD){
			return dojo.io.script.get(bindArgs);
		}else{
			return dojo.xhrPost(bindArgs);
		}
	}

	this.finishInit = function(data){
		data = data[0];
		this.handshakeReturn = data;
		// pick a transport
		if (data["advice"]){
			this.advice = data.advice;
		}
		
		// TODO remove authSuccessful when all clients updated to the spec
		if((!data.successful)&&(!data.authSuccessful)){  
			console.debug("cometd init failed");
			// TODO follow advice
			return;
		}
		if(data.version < this.minimumVersion){
			console.debug("cometd protocol version mismatch. We wanted", this.minimumVersion, "but got", data.version);
			return;
		}
		this.currentTransport = this.connectionTypes.match(
			data.supportedConnectionTypes,
			data.version,
			this._isXD
		);
		this.currentTransport.version = data.version;
		this.clientId = data.clientId;
		this.tunnelInit = dojo.hitch(this.currentTransport, "tunnelInit");
		this.tunnelCollapse = dojo.hitch(this.currentTransport, "tunnelCollapse");

		this.currentTransport.startup(data);
		while(this.backlog.length != 0){
			var cur = this.backlog.shift();
			var fn = cur.shift();
			this[fn].apply(this, cur);
		}

		dojo.addOnUnload(dojox.cometd,"disconnect");
	}

	this._getRandStr = function(){
		return Math.random().toString().substring(2, 10);
	}

	// public API functions called by cometd or by the transport classes
	this.deliver = function(messages){
		// console.debug(messages);
		dojo.forEach(messages, this._deliver, this);
		return messages;
	}

	this._deliver = function(message){
		// dipatch events along the specified path
		if(!this.currentTransport){
			this.backlog.push(["deliver", message]);
			return;
		}

		if(!message["channel"]){
			if(message["success"] !== true){
				console.debug("cometd error: no channel for message!", message);
				return;
			}
		}
		this.lastMessage = message;

		if(message.advice){
			this.advice = message.advice; // TODO maybe merge?
		}

		// check to see if we got a /meta channel message that we care about
		if(	(message["channel"]) &&
			(message.channel.length > 5)&&
			(message.channel.substr(0, 5) == "/meta")){
			// check for various meta topic actions that we need to respond to
			switch(message.channel){
				case "/meta/subscribe":
					if(!message.successful){
						console.debug("cometd subscription error for channel", message.channel, ":", message.error);
						return;
					}
					dojox.cometd.subscribed(message.subscription, message);
					break;
				case "/meta/unsubscribe":
					if(!message.successful){
						console.debug("cometd unsubscription error for channel", message.channel, ":", message.error);
						return;
					}
					this.unsubscribed(message.subscription, message);
					break;
			}
		}
		// send the message down for processing by the transport
		this.currentTransport.deliver(message);

		if(message.data){
			// dispatch the message to any locally subscribed listeners
			var tname = (this.globalTopicChannels[message.channel]) ? message.channel : "/cometd"+message.channel;
			dojo.publish(tname, [ message ]);
		}
	}

	this.disconnect = function(){
		dojo.forEach(this._subscriptions, dojo.unsubscribe);
		this._subscriptions = [];
		this.backlog = [];
		if(!this.currentTransport){
			console.debug("no current transport to disconnect from");
			return;
		}
		this.currentTransport.disconnect();
	}

	// public API functions called by end users
	this.publish = function(/*string*/channel, /*object*/data, /*object*/properties){
		// summary: 
		//		publishes the passed message to the cometd server for delivery
		//		on the specified topic
		// channel:
		//		the destination channel for the message
		// data:
		//		a JSON object containing the message "payload"
		// properties:
		//		Optional. Other meta-data to be mixed into the top-level of the
		//		message
		if(!this.currentTransport){
			this.backlog.push(["publish", channel, data, properties]);
			// console.debug(this.backlog);
			return;
		}
		var message = {
			data: data,
			channel: channel
		};
		if(properties){
			dojo.mixin(message, properties);
		}
		return this.currentTransport.sendMessage(message);
	}

	this.subscribe = function(	/*string*/				channel, 
								/*boolean, optional*/	useLocalTopics, 
								/*object, optional*/	objOrFunc, 
								/*string, optional*/	funcName){ // return: boolean
		// summary:
		//		inform the server of this client's interest in channel
		// channel:
		//		name of the cometd channel to subscribe to
		// useLocalTopics:
		//		Determines if up a local event topic subscription to the passed
		//		function using the channel name that was passed is constructed,
		//		or if the topic name will be prefixed with some other
		//		identifier for local message distribution. Setting this to
		//		"true" is a good way to hook up server-sent message delivery to
		//		pre-existing local topics.
		// objOrFunc:
		//		an object scope for funcName or the name or reference to a
		//		function to be called when messages are delivered to the
		//		channel
		// funcName:
		//		the second half of the objOrFunc/funcName pair for identifying
		//		a callback function to notifiy upon channel message delivery

		if(!this.currentTransport){
			this.backlog.push(["subscribe", channel, useLocalTopics, objOrFunc, funcName]);
			return;
		}

		if((useLocalTopics !== true)||(useLocalTopcis !== false)){
			// similar to: function(channel, objOrFunc, funcName, useLocalTopics);
			var ofn = funcName;
			funcName = objOrFunc;
			objOrFunc = useLocalTopics;
			useLocalTopics = ofn;
		}
		// console.debug(objOrFunc, funcName);

		if(objOrFunc){
			var tname = (useLocalTopics) ? channel : "/cometd"+channel;
			if(useLocalTopics){
				this.globalTopicChannels[channel] = true;
			}

			this._subscriptions.push(
				dojo.subscribe(tname, objOrFunc, funcName)
			);
		}
		// FIXME: would we handle queuing of the subscription if not connected?
		// Or should the transport object?
		return this.currentTransport.sendMessage({
			channel: "/meta/subscribe",
			subscription: channel
		});
	}

	this.subscribed = function(	/*string*/				channel, 
								/*obj*/					message){
		// console.debug(channel, message);
	}

	this.unsubscribe = function(/*string*/				channel, 
								/*boolean, optional*/	useLocalTopics, 
								/*object, optional*/	objOrFunc, 
								/*string, optional*/	funcName){ // return: boolean
		// summary:
		//		inform the server of this client's disinterest in channel
		// channel:
		//		name of the cometd channel to subscribe to
		// useLocalTopics:
		//		Determines if up a local event topic subscription to the passed
		//		function using the channel name that was passed is destroyed,
		//		or if the topic name will be prefixed with some other
		//		identifier for stopping message distribution.
		// objOrFunc:
		//		an object scope for funcName or the name or reference to a
		//		function to be called when messages are delivered to the
		//		channel
		// funcName:
		//		the second half of the objOrFunc/funcName pair for identifying
		if(!this.currentTransport){
			this.backlog.push(["unsubscribe", channel, useLocalTopics, objOrFunc, funcName]);
			return;
		}
		//		a callback function to notifiy upon channel message delivery
		if(objOrFunc){
			// FIXME: should actual local topic unsubscription be delayed for
			// successful unsubcribe notices from the other end? (guessing "no")
			// FIXME: if useLocalTopics is false, should we go ahead and
			// destroy the local topic?
			var tname = (useLocalTopics) ? channel : "/cometd"+channel;
			// FIXME: we're not passing a valid handle!!
			dojo.unsubscribe(tname, objOrFunc, funcName);
		}
		return this.currentTransport.sendMessage({
			channel: "/meta/unsubscribe",
			subscription: channel
		});
	}

	this.unsubscribed = function(/*string*/				channel, 
								/*obj*/					message){
		// console.debug(channel, message);
	}

	// FIXME: add an "addPublisher" function
}

/*
transport objects MUST expose the following methods:
	- check
	- startup
	- sendMessage
	- deliver
	- disconnect
optional, standard but transport dependent methods are:
	- tunnelCollapse
	- tunnelInit

Transports SHOULD be namespaced under the cometd object and transports MUST
register themselves with cometd.connectionTypes

here's a stub transport defintion:

cometd.blahTransport = new function(){
	this.lastTimestamp = null;
	this.lastId = null;

	this.check = function(types, version, xdomain){
		// summary:
		//		determines whether or not this transport is suitable given a
		//		list of transport types that the server supports
		return dojo.lang.inArray(types, "blah");
	}

	this.startup = function(){
		if(dojox.cometd.connected){ return; }
		// FIXME: fill in startup routine here
		dojox.cometd.connected = true;
	}

	this.sendMessage = function(message){
		// FIXME: fill in message sending logic
	}

	this.deliver = function(message){
		if(message["timestamp"]){
			this.lastTimestamp = message.timestamp;
		}
		if(message["id"]){
			this.lastId = message.id;
		}
		if(	(message.channel.length > 5)&&
			(message.channel.substr(0, 5) == "/meta")){
			// check for various meta topic actions that we need to respond to
			// switch(message.channel){
			// 	case "/meta/connect":
			//		// FIXME: fill in logic here
			//		break;
			//	// case ...: ...
			//	}
		}
	}

	this.disconnect = function(){
		if(!dojox.cometd.connected){ return; }
		// FIXME: fill in shutdown routine here
		dojox.cometd.connected = false;
	}
}
cometd.connectionTypes.register("blah", cometd.blahTransport.check, cometd.blahTransport);
*/

dojox.cometd.longPollTransport = new function(){
	this.lastTimestamp = null;
	this.lastId = null;
	this.backlog = [];

	this.check = function(types, version, xdomain){
		return ((!xdomain)&&(dojo.indexOf(types, "long-polling") >= 0));
	}

	this.tunnelInit = function(){
		if(dojox.cometd.connected){ return; }
		// FIXME: open up the connection here
		this.openTunnelWith({
			message: dojo.toJson([
				{
					channel:	"/meta/connect",
					clientId:	dojox.cometd.clientId,
					connectionType: "long-polling"
				}
			])
		});
	}

	this.tunnelCollapse = function(){
		if(!dojox.cometd.connected){
			// try to restart the tunnel
			dojox.cometd.connected = false;
			
			// TODO handle transport specific advice
			
			if(dojox.cometd["advice"]){
				if(dojox.cometd.advice["reconnect"]=="none"){
					return;
				}
			
	            if(	(dojox.cometd.advice["interval"])&&
					(dojox.cometd.advice.interval>0) ){
					setTimeout(function(){
						dojox.cometd.currentTransport._reconnect();
					}, dojox.cometd.advice.interval);
				}else{
					this._reconnect();
				}
			}else{
				this._reconnect();
			}
	    }
	}	
			
	this._reconnect = function(){
		if(	(dojox.cometd["advice"])&&
			(dojox.cometd.advice["reconnect"]=="handshake")
		){
			dojox.cometd.init(null,dojox.cometd.url);
 		}else if (dojox.cometd.initialized){
			this.openTunnelWith({
				message: dojo.toJson([
					{
						channel:	"/meta/reconnect",
						connectionType: "long-polling",
						clientId:	dojox.cometd.clientId,
						timestamp:	this.lastTimestamp,
						id:			this.lastId
					}
				])
			});
		}
	}

	this.deliver = function(message){
		// console.debug(message);
		// handle delivery details that this transport particularly cares
		// about. Most functions of should be handled by the main cometd object
		// with only transport-specific details and state being tracked here.
		if(message["timestamp"]){
			this.lastTimestamp = message.timestamp;
		}
		if(message["id"]){
			this.lastId = message.id;
		}

		// check to see if we got a /meta channel message that we care about
		if(	(message.channel.length > 5)&&
			(message.channel.substr(0, 5) == "/meta")){
			// check for various meta topic actions that we need to respond to
			switch(message.channel){
				case "/meta/connect":
					if(!message.successful){
						console.debug("cometd connection error:", message.error);
						return;
					}
					dojox.cometd.initialized = true;
					this.processBacklog();
					break;
				case "/meta/reconnect":
					if(!message.successful){
						console.debug("cometd reconnection error:", message.error);
						return;
					}
					break;
				case "/meta/subscribe":
					if(!message.successful){
						console.debug("cometd subscription error for channel", message.channel, ":", message.error);
						return;
					}
					dojox.cometd.subscribed(message.channel);
					// console.debug(message.channel);
					break;
			}
		}
	}

	this.openTunnelWith = function(content, url){
		// console.debug("openTunnelWith:", content, (url||cometd.url));
		var d = dojo.xhrPost({
			url: (url||dojox.cometd.url),
			content: content,
			handleAs: dojox.cometd.handleAs,
			load: dojo.hitch(this, function(data){
				// console.debug(evt.responseText);
				// console.debug(data);
		                dojox.cometd.connected = false;
				dojox.cometd.deliver(data);
				this.tunnelCollapse();
			}),
			error: function(err){ 
				console.debug("tunnel opening failed:", err);
				dojo.cometd.connected = false;

				// TODO - follow advice to reconnect or rehandshake?
			}
		});
		dojox.cometd.connected = true;
	}

	this.processBacklog = function(){
		while(this.backlog.length > 0){
			this.sendMessage(this.backlog.shift(), true);
		}
	}

	this.sendMessage = function(message, bypassBacklog){
		if((bypassBacklog)||(dojox.cometd.initialized)){
			message.clientId = dojox.cometd.clientId;

			return dojo.xhrPost({
				url: dojox.cometd.url||djConfig["cometdRoot"],
				handleAs: dojox.cometd.handleAs,
				load: dojo.hitch(dojox.cometd, "deliver"),
				content: { 
					message: dojo.toJson([ message ]) 
				}
			});
		}else{
			this.backlog.push(message);
		}
	}

	this.startup = function(handshakeData){
		if(dojox.cometd.initialized){ return; }
		this.tunnelInit();
	}

	this.disconnect = function(){
		if(!dojox.cometd.initialized){ return; }
        
		dojo.xhrPost({
			url: dojox.cometd.url||djConfig["cometdRoot"],
			handleAs: dojox.cometd.handleAs,
			content: { 
				message: dojo.toJson([{  
				    channel:	"/meta/disconnect",
				    clientId:	dojox.cometd.clientId
				}])
			}
		});
		
		dojox.cometd.initialized=false;
	}
}

dojox.cometd.callbackPollTransport = new function(){
	this.lastTimestamp = null;
	this.lastId = null;
	this.backlog = [];

	this.check = function(types, version, xdomain){
		// we handle x-domain!
		return (dojo.indexOf(types, "callback-polling") >= 0);
	}

	this.tunnelInit = function(){
		if(dojox.cometd.connected){ return; }
		// FIXME: open up the connection here
		this.openTunnelWith({
			message: dojo.toJson([
				{
					channel:	"/meta/connect",
					clientId:	dojox.cometd.clientId,
					connectionType: "callback-polling"
				}
			])
		});
	}

	this.tunnelCollapse = function(){
	        // TODO implement advice handling
		if(!dojox.cometd.connected){
			// try to restart the tunnel
			this.openTunnelWith({
				message: dojo.toJson([
					{
						channel:	"/meta/reconnect",
						connectionType: "long-polling",
						clientId:	dojox.cometd.clientId,
						timestamp:	this.lastTimestamp,
						id:		this.lastId
					}
				])
			});
		}
	}

	// the logic appears to be the same
	this.deliver = dojox.cometd.longPollTransport.deliver;

	this.openTunnelWith = function(content, url){
		// create a <script> element to generate the request
		dojo.io.script.get({
			load: dojo.hitch(this, function(data){
				dojox.cometd.connected = false;
				dojox.cometd.deliver(data);
				this.tunnelCollapse();
			}),
			error: function(){ 
				dojox.cometd.connected = false;
				console.debug("tunnel opening failed"); 
			},
			url: (url||dojox.cometd.url),
			content: content,
			handleAs: dojox.cometd.handleAs,
			callbackParamName: "jsonp"
		});
		dojox.cometd.connected = true;
	}

	this.processBacklog = function(){
		while(this.backlog.length > 0){
			this.sendMessage(this.backlog.shift(), true);
		}
	}

	this.sendMessage = function(message, bypassBacklog){
		if((bypassBacklog)||(dojox.cometd.initialized)){
			message.clientId = dojox.cometd.clientId;
			var bindArgs = {
				url: dojox.cometd.url||djConfig["cometdRoot"],
				handleAs: dojox.cometd.handleAs,
				load: dojo.hitch(dojox.cometd, "deliver"),
				callbackParamName: "jsonp",
				content: { message: dojo.toJson([ message ]) }
			};
			return dojo.io.script.get(bindArgs);
		}else{
			this.backlog.push(message);
		}
	}

	this.startup = function(handshakeData){
		if(dojox.cometd.initialized){ return; }
		this.tunnelInit();
	}

	this.disconnect = dojox.cometd.longPollTransport.disconnect;
}
dojox.cometd.connectionTypes.register("long-polling", dojox.cometd.longPollTransport.check, dojox.cometd.longPollTransport);
dojox.cometd.connectionTypes.register("callback-polling", dojox.cometd.callbackPollTransport.check, dojox.cometd.callbackPollTransport);

}
