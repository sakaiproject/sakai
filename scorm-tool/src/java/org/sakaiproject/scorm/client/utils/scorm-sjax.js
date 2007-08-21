

/**
 * Channel management
 *
 * Wicket Ajaax requests are organized in channels. A channel maintain the order of 
 * requests and determines, what should happen when a request is fired while another 
 * one is being processed. The default behavior (stack) puts the all subsequent requests 
 * in a queue, while the drop behavior limits queue size to one, so only the most
 * recent of subsequent requests is executed.
 * The name of channel determines the policy. E.g. channel with name foochannel|s is 
 * a stack channel, while barchannel|d is a drop channel.
 *
 * The Channel class is supposed to be used through the ChannelManager.
 */
Wicket.SChannel = Wicket.Class.create();
Wicket.SChannel.prototype = {
	initialize: function(name) {
		var res = name.match(/^([^|]+)\|(d|s)$/)
		if (res == null)
			this.type ='s'; // default to stack 
		else
			this.type = res[2];
		this.callbacks = new Array();
		Wicket.Log.info("Marking channel free");
		this.busy = false;
	},	
	
	schedule: function(callback) {
		if (this.busy == false) {
			Wicket.Log.info("Marking channel busy");
			this.busy = true;			
			return callback();
		} else {
			Wicket.Log.info("Channel busy - postponing... type is " + type);
			if (this.type == 's') // stack 
				this.callbacks.push(callback);
			else /* drop */
				this.callbacks[0] = callback;
			return null;				
		}
	},
	
	done: function() {
		var c = null;
		
		if (this.callbacks.length > 0) {
			c = this.callbacks.shift();
		}
			
		if (c != null && typeof(c) != "undefined") {
			Wicket.Log.info("Calling posponed function...");
			// we can't call the callback from this call-stack
			// therefore we set it on timer event
			window.setTimeout(c, 1);			
		} else {
			Wicket.Log.info("Marking channel free");
			this.busy = false;
		}
	}
};

/**
 * Channel manager maintains a map of channels. 
 */
Wicket.SChannelManager = Wicket.Class.create();
Wicket.SChannelManager.prototype = {
	initialize: function() {
		this.channels = new Array();
	},
  
	// Schedules the callback to channel with given name.
	schedule: function(channel, callback) {
		var c = this.channels[channel];
		if (c == null) {
			c = new Wicket.SChannel(channel);
			this.channels[channel] = c;
		}
		return c.schedule(callback);
	},
	
	// Tells the ChannelManager that the current callback in channel with given name 
	// has finished processing and another scheduled callback can be executed (if any).
	done: function(channel) {
		var c = this.channels[channel];
		if (c != null)
			c.done();
	}
};

// Default channel manager instance
Wicket.schannelManager = new Wicket.SChannelManager();


/**
 * The Ajax class handles low level details of creating and pooling XmlHttpRequest objects,
 * as well as registering and execution of pre-call, post-call and failure handlers.
 */
Wicket.Sjax = { 
 	// Creates a new instance of a XmlHttpRequest
	createTransport: function() {
	    var transport = null;
	    if (window.ActiveXObject) {
	        transport = new ActiveXObject("Microsoft.XMLHTTP");
	    } else if (window.XMLHttpRequest) {
	        transport = new XMLHttpRequest();
	    } 
	    
	    if (transport == null) {
	        Wicket.Log.error("Could not locate ajax transport. Your browser does not support the required XMLHttpRequest object or wicket could not gain access to it.");
	    }    
	    return transport;
	},
	
	transports: [],
	
	// Returns a transport from pool if any of them is not being used, or creates new instance
	getTransport: function() {
		var t = Wicket.Sjax.transports;
		for (var i = 0; i < t.length; ++i) {
			if (t[i].readyState == 0) {
				return t[i];
			}
		}
		t.push(Wicket.Sjax.createTransport());
		return t[t.length-1];		
	},
	
	preCallHandlers: [],
	postCallHandlers: [],	
	failureHandlers: [],
	
	registerPreCallHandler: function(handler) {
		var h = Wicket.Sjax.preCallHandlers;
		h.push(handler);
	},
	
	registerPostCallHandler: function(handler) {
		var h = Wicket.Sjax.postCallHandlers;
		h.push(handler);
	},
	
	registerFailureHandler: function(handler) {
		var h = Wicket.Sjax.failureHandlers;
		h.push(handler);
	},
	
	invokePreCallHandlers: function() {
		var h = Wicket.Sjax.preCallHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking pre-call handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	},
	
	invokePostCallHandlers: function() {
		var h = Wicket.Sjax.postCallHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking post-call handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	},

	invokeFailureHandlers: function() {
		var h = Wicket.Sjax.failureHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking failure handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	}
}


/**
 * The Ajax.Request class encapsulates a XmlHttpRequest. 
 */
Wicket.Sjax.Request = Wicket.Class.create();

Wicket.Sjax.Request.prototype = {
    // Creates a new request object.
	initialize: function(url, loadedCallback, parseResponse, randomURL, failureHandler, channel) {
		this.url = url;
		this.loadedCallback = loadedCallback;
		// whether we should give the loadedCallback parsed response (DOM tree) or the raw string
		this.parseResponse = parseResponse != null ? parseResponse : true; 
		this.randomURL = randomURL != null ? randomURL : true;
		this.failureHandler = failureHandler != null ? failureHandler : function() { };
		this.async = true;
		this.channel = channel;
		this.precondition = function() { return true; } // allow a condition to block request 

		// when suppressDone is set, the loadedCallback is responsible for calling
		// Ajax.Request.done() to process possibly pendings requests in the channel.
		this.suppressDone = false;
		this.instance = Math.random();
		this.debugContent = true;
	},
	
	done: function() {
		Wicket.schannelManager.done(this.channel);
	},
	
	createUrl: function() {
		if (this.randomURL == false)
			return this.url;
		else
			return this.url + "&random=" + Math.random();
	},
	
	log: function(method, url) {
		var log = Wicket.Log.info;
		log("");
		log("Initiating Sjax "+method+" request on " + url);
	},
	
	failure: function() {
		this.failureHandler();
   		Wicket.Sjax.invokePostCallHandlers();
   		Wicket.Sjax.invokeFailureHandlers();
	},
	
	// Executes a get request
	get: function() {
		if (this.channel != null) {
			var res = Wicket.schannelManager.schedule(this.channel, this.doGet.bind(this));
			return res != null ? res : true;
		} else {
			return this.doGet();
		}
	},
	
	// The actual get request implementation
	doGet: function() {
		if (this.precondition()) {
	
			this.transport = Wicket.Sjax.getTransport();
		
			var url = this.createUrl();	
			this.log("GET", url);
			
			Wicket.Sjax.invokePreCallHandlers();
			
			var t = this.transport;
			if (t != null) {
				t.open("GET", url, this.async);
				t.onreadystatechange = this.stateChangeCallback.bind(this);
				// set a special flag to allow server distinguish between ajax and non-ajax requests
				t.setRequestHeader("Wicket-Ajax", "true");
				t.send(null);
				return true;
			} else {
				this.failure();
	       		return false;
			}
		} else {
			this.done();
			return false;
		}
	},
	
	// Posts the given string
	post: function(body) {
		if (this.channel != null) {
			var res = Wicket.schannelManager.schedule(this.channel, function() { this.doPost(body); }.bind(this));
			return res != null ? res: true;
		} else {
			return doPost(this);
		}
	},
	
	// The actual post implementation
	doPost: function(body) {
		if (this.precondition()) {
			this.transport = Wicket.Sjax.getTransport();	
		
			var url = this.createUrl();	
			this.log("POST", url);
			
			Wicket.Sjax.invokePreCallHandlers();
			
			var t = this.transport;
			if (t != null) {
				t.open("POST", url, this.async);
				t.onreadystatechange = this.stateChangeCallback.bind(this);
				t.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
				// set a special flag to allow server distinguish between ajax and non-ajax requests
				t.setRequestHeader("Wicket-Ajax", "true");
				t.send(body);
				return true;
			} else {
	       		this.failure();
	       		return false;
			}
		} else {
			this.done();
			return false;
		}
	},
	
	// Method that processes the request states
	stateChangeCallback: function() {	
		var t = this.transport;

		if (t != null && t.readyState == 4) {
			try {
				status = t.status;
			}
			catch (e) {
				Wicket.Log.error("Exception evaluating AJAX status: " + e);
				status = "unavailable";
			}
			if (status == 200 || status == "") { // as stupid as it seems, IE7 sets satus to "" on ok
				// response came without error
				var responseAsText = t.responseText;
				
				// first try to get the redirect header
				var redirectUrl;
				try {
					redirectUrl = t.getResponseHeader('Ajax-Location');
				} catch (ignore) { // might happen in older mozilla
				}
				
				// the redirect header was set, go to new url
				if (typeof(redirectUrl) != "undefined" && redirectUrl != null && redirectUrl != "") {
					t.onreadystatechange = Wicket.emptyFunction;
					window.location = redirectUrl;
				}
				else {
					// no redirect, just regular response
					var log = Wicket.Log.info;				
					log("Received ajax response (" + responseAsText.length + " characters)");
					if (this.debugContent != false) {
						log("\n" + responseAsText);
					}
	        		
	        		// parse the response if the callback needs a DOM tree
	        		if (this.parseResponse == true) {
						var xmldoc;					
						if (typeof(window.XMLHttpRequest) != "undefined" && typeof(DOMParser) != "undefined") {						
							var parser = new DOMParser();
							xmldoc = parser.parseFromString(responseAsText, "text/xml");						
						} else if (window.ActiveXObject) {
							xmldoc = t.responseXML;
						}
						// invoke the loaded callback with an xml document
						this.loadedCallback(xmldoc); 
					} else {
						// invoke the loaded callback with raw string
						this.loadedCallback(responseAsText);
					}        		
					if (this.suppressDone == false)
						this.done();
				}
        	} else {
        		// when an error happened
        		var log = Wicket.Log.error;
        		log("Received Ajax response with code: " + status);
		   		this.done();        		
        		this.failure();
        	}    	
        	t.onreadystatechange = Wicket.emptyFunction;
        	t.abort();
        	this.transport = null;       
        }        
	}
};


Wicket.Sjax.Call = Wicket.Class.create();

Wicket.Sjax.Call.prototype = {
	// Initializes the Call
	initialize: function(url, successHandler, failureHandler, channel) {
		this.successHandler = successHandler != null ? successHandler : function() { };
		this.failureHandler = failureHandler != null ? failureHandler : function() { };

		var c = channel != null ? channel : "0|s"; // set the default channel if not specified
		// initialize the internal Ajax request
		this.request = new Wicket.Sjax.Request(url, this.loadedCallback.bind(this), true, true, failureHandler, c);
		this.request.suppressDone = true;
		this.request.async = false;
		//this.request.suppressDone = false;
		
		Wicket.Log.info("Issuing request, the channel is " + c);
	},
	
	// On ajax request failure
	failure: function(message) {
		Wicket.Log.info("Failure!");
		if (message != null)
			Wicket.Log.error("Error while parsing response: " + message);
		this.request.done();
		this.failureHandler();
   		Wicket.Sjax.invokePostCallHandlers();
   		Wicket.Sjax.invokeFailureHandlers();
	},	
	
	// Fires a get request
	call: function() {	
		Wicket.Log.info("Firing get request!");
		return this.request.get();
	},
	
	// Fires a post request
	post: function(body) {
		Wicket.Log.info("Firing post request!");
		return this.request.post(body);
	},

	// Submits a form using ajax.
	// This method serializes a form and sends it as POST body.
	submitForm: function(form, submitButton) {
	    var body = Wicket.Form.serialize(form);
	    if (submitButton != null) {
	        body += Wicket.Form.encode(submitButton) + "=1";
	    }
	    return this.request.post(body);
	},
	
	// Submits a form using ajax
	submitFormById: function(formId, submitButton) {
		var form = Wicket.$(formId);
		if (form == null || typeof (form) == "undefined")
			Wicket.Log.error("Trying to submit form with id '"+formId+"' that is not in document.");
		return this.submitForm(form, submitButton);
	},
	
	// Processes the response
	loadedCallback: function(envelope) {
		Wicket.Log.info("Inside loaded callback!");
		// To process the response, we go through the xml document and add a function for every action (step).
		// After this is done, a FunctionExecuter object asynchronously executes these functions.
		// The asynchronous execution is necessary, because some steps might involve loading external javascript,
		// which must be asynchronous, so that it doesn't block the browser, but we also have to maintain
		// the order in which scripts are loaded and we have to delay the next steps until the script is
		// loaded.
		try {			
			var root = envelope.getElementsByTagName("ajax-response")[0];
					
			// the root element must be <ajax-response	
		    if (root == null || root.tagName != "ajax-response") {
		    	this.failure("Could not find root <ajax-response> element");
		    	return;
		    }
						
			// iinitialize the array for steps (closures that execute each action)
		    var steps = new Array();

		    if (Wicket.Browser.isKHTML()) {
		    	// there's a nasty bug in KHTML that makes the browser crash
		    	// when the methods are delayed. Therefore we have to fire it
		    	// ASAP. The javascripts that would cause dependency problems are
		    	// loaded synchronously in konqueror.
			    steps.push = function(method) {
			    	method(function() { });
			    }
			}
			
			// go through the ajax response and for every action (component, js evaluation, header contribution)
			// ad the proper closure to steps
		    for (var i = 0; i < root.childNodes.length; ++i) {
		    	var node = root.childNodes[i];				

		        if (node.tagName == "component") {
		           this.processComponent(steps, node);
		        } else if (node.tagName == "evaluate") {
		           this.processEvaluation(steps, node);
		        } else if (node.tagName == "header-contribution") {
		           this.processHeaderContribution(steps, node);
		        }
		        
		    }

Wicket.Log.info("Calling success on steps.");
			// add the last step, which should trigger the success call the done method on request
			this.success(steps);
		    
		    if (Wicket.Browser.isKHTML() == false) {
			    Wicket.Log.info("Response parsed. Now invoking steps...");		    		   		    
			    var executer = new Wicket.FunctionsExecuter(steps);
			    executer.start();		    
		    }		    
		} catch (e) {
			this.failure(e.message);
		}
	},
	
	// Adds a closure to steps that should be invoked after all other steps have been successfully executed
	success: function(steps) {
		steps.push(function(notify) {
			Wicket.Log.info("Response processed successfully.");			
			//--JLR--Wicket.Sjax.invokePostCallHandlers();
			// retach the events to the new components (a bit blunt method...)
			// This should be changed for IE See comments in wicket-event.js add (attachEvent/detachEvent)
			// IE this will cause double events for everything.. (mostly because of the Function.prototype.bind(element))
			//--JLR--Wicket.Focus.attachFocusEvent();
			
			this.request.done();
			this.successHandler();

			// set the focus to the last component
			//--JLR--setTimeout("Wicket.Focus.requestFocus();", 0);
			
			// continue to next step (which should make the processing stop, as success should be the final step)		
			notify();			
		}.bind(this));
	},

	// Adds a closure that replaces a component	
	processComponent: function(steps, node) {
	
Wicket.Log.info("processComponent");	
		steps.push(function(notify) {
			// get the component id
			var compId = node.getAttribute("id");
			var text="";

			// get the new component body
			if (node.hasChildNodes()) {
				text = node.firstChild.nodeValue;
			}

			// if the text was escaped, unascape it
			// (escaping is done when the component body contains a CDATA section)
			var encoding = node.getAttribute("encoding");
			if (encoding != null && encoding!="") {
				text = Wicket.decode(encoding, text);
			}
			
			// get existing component
			var element = Wicket.$(compId);

			if (element == null || typeof(element) == "undefined") {			
				Wicket.Log.error("Component with id [["+compId+"]] a was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.");
			} else {
				// replace the component
				Wicket.replaceOuterHtml(element, text);
			}
			// continue to next step
			notify();
		});
	},
	
	// Adds a closure that evaluates javascript code
	processEvaluation: function(steps, node) {
	
Wicket.Log.info("processEvaluation");	
		steps.push(function(notify) {
			// get the javascript body
		    var text = node.firstChild.nodeValue;
		    
		    // unescape it if necessary
		    var encoding = node.getAttribute("encoding");
		    if (encoding != null) {
		        text = Wicket.decode(encoding, text);
		    }
		    try {
		    	Wicket.Log.info("Trying to evaluate: " + text);
		   		// do the evaluation
		    	eval(text);
		    } catch (exception) {
		    	Wicket.Log.error("Exception evaluating javascript: " + exception);
		    }
		    // continue to next step
			notify();
		});
	},
	
	// Adds a closure that processes a header contribution
	processHeaderContribution: function(steps, node) {
	
Wicket.Log.info("processHeaderContribution");		
		var c = new Wicket.Head.Contributor();
		c.processContribution(steps, node);
	}
};

function pausecomp(call_number, call)
{
	Wicket.Log.info("URL is: " + call.request.url);
	var startDate = new Date();
	var curDate = null;
	
	do { 
		curDate = new Date(); 
	        lookForResponse(call.request);	
		Wicket.Log.info("api result: " + api_result[call_number]);
		var diff = curDate - startDate;
		Wicket.Log.info("diff: " + diff);
		if (diff > 300) {
			Wicket.Log.info("Timed out--unable to get result: " + api_result[call_number] + " for " + call_number);
			break;
		}
		
	}
	while(api_result[call_number] == undefined);
} 


function sjaxCall(scoId, url, arg1, arg2, successHandler, failureHandler, precondition, channel) {
	Wicket.Log.info("Calling sjaxCall with url: " + url + " sco: " + scoId + " arg1: " + arg1 + " arg2: " + arg2);
	var call = new Wicket.Ajax.Call(url + '&scoId=' + scoId + '&arg1=' + arg1 + '&arg2=' + arg2 + '&callNumber=' + call_number, function() {}, function() {}, channel);
	call.request.async = false;
	var resultCode = call.call();
	
	pausecomp(call_number, call);
	
	Wicket.Log.info("sjaxCall resultCode = " + resultCode);
	var resultValue = api_result[call_number];
	Wicket.Log.info("sjaxCall resultValue = " + resultValue);
	call_number++;
	return resultValue;
};




     function lookForResponse(request) {	

Wicket.Log.info("lookForResponse..."); 

	var t = request.transport;
if (t == null) {
	Wicket.Log.info("Transport is null!");
} else {
	Wicket.Log.info("Transport ready state is: " + t.readyState);
}

		if (t != null && t.readyState == 4) {
			try {
				status = t.status;
Wicket.Log.info("Status is: " + status);
			}
			catch (e) {
				Wicket.Log.error("Exception evaluating AJAX status: " + e);
				status = "unavailable";
			}
			if (status == 200 || status == "") { // as stupid as it seems, IE7 sets satus to "" on ok
				// response came without error
				var responseAsText = t.responseText;
Wicket.Log.info("Response: " + responseAsText);
				
				// first try to get the redirect header
				var redirectUrl;
				try {
					redirectUrl = t.getResponseHeader('Ajax-Location');
				} catch (ignore) { // might happen in older mozilla
				}
				
				// the redirect header was set, go to new url
				if (typeof(redirectUrl) != "undefined" && redirectUrl != null && redirectUrl != "") {
					t.onreadystatechange = Wicket.emptyFunction;
					window.location = redirectUrl;
				}
				else {
					// no redirect, just regular response
					var log = Wicket.Log.info;				
					log("Received ajax response (" + responseAsText.length + " characters)");
					if (request.debugContent != false) {
						log("\n" + responseAsText);
					}
	        		
	        		// parse the response if the callback needs a DOM tree
	        		if (request.parseResponse == true) {
						var xmldoc;					
						if (typeof(window.XMLHttpRequest) != "undefined" && typeof(DOMParser) != "undefined") {						
							var parser = new DOMParser();
							xmldoc = parser.parseFromString(responseAsText, "text/xml");						
						} else if (window.ActiveXObject) {
							xmldoc = t.responseXML;
						}
						// invoke the loaded callback with an xml document
						request.loadedCallback(xmldoc); 
					} else {
						// invoke the loaded callback with raw string
						request.loadedCallback(responseAsText);
					}        		
					if (request.suppressDone == false)
						request.done();
				}
        	} else {
        		// when an error happened
        		var log = Wicket.Log.error;
        		log("Received Ajax response with code: " + status);
		   		request.done();        		
        		request.failure();
        	}    	
        	t.onreadystatechange = Wicket.emptyFunction;
        	t.abort();
        	request.transport = null;       
        }       
    }
















