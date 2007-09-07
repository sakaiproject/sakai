/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/*
 * Wicket Ajax Support 
 *
 * @author Igor Vaynberg
 * @author Matej Knopp 
 */
var Class = {
	create: function() {
		return function() {
			this.initialize.apply(this, arguments);
		}
	}
}

if (Function.prototype.bind == null) {
	Function.prototype.bind = function(object) {
		var __method = this;
		return function() {
			return __method.apply(object, arguments);
		}
	}
}

// Wicket Namespace

if (typeof(Wicket) == "undefined")
	Wicket = { };

Wicket.emptyFunction = function() { };

// Browser types

Wicket.Browser = { 
	isKHTML: function() {
		return /Konqueror|KHTML/.test(navigator.userAgent) && !/Apple/.test(navigator.userAgent);
	},
	
	isSafari: function() {
		return /KHTML/.test(navigator.userAgent) && /Apple/.test(navigator.userAgent);
	},
	
	isOpera: function() {
		return typeof(window.opera) != "undefined";
	},

	isIE: function() {
		return typeof(document.all) != "undefined" && typeof(window.opera) == "undefined";
	},
	
	isIEQuirks: function() {
		// is the browser internet explorer in quirks mode (we could use document.compatMode too)		
		return Wicket.Browser.isIE() && document.documentElement.clientHeight == 0;
	},		
	
	isIE7: function() {
		var index = navigator.userAgent.indexOf("MSIE");
		var version = parseFloat(navigator.userAgent.substring(index + 5));
		return Wicket.Browser.isIE() && version >= 7;
	},
	
	isGecko: function() {
		return /Gecko/.test(navigator.userAgent) && !Wicket.Browser.isSafari();
	}
};

/**
 * Add a check for old Safari. It should not be our responsibility to check the 
 * browser's version, but it's a minor version that makes a difference here,
 * so we try to be at least user friendly.  
 */
if (typeof DOMParser == "undefined" && Wicket.Browser.isSafari()) {
   DOMParser = function () {}

   DOMParser.prototype.parseFromString = function (str, contentType) {
   		alert('You are using an old version of Safari.\nTo be able to use this page you need at least version 2.0.1.');
   }
}


// Logging functions

Wicket.Log = { 

	enabled: function() {
		return wicketAjaxDebugEnabled();
	},
	
	info: function(msg) {
	    if (Wicket.Log.enabled())
			WicketAjaxDebug.logInfo(msg);
	},
	
	error: function(msg) {
		if (Wicket.Log.enabled())
			WicketAjaxDebug.logError(msg);
	},  

	log: function(msg) {
		if(Wicket.Log.enabled())
			WicketAjaxDebug.log(msg);
	}
},

// Functions executer

Wicket.FunctionsExecuter = Class.create();

Wicket.FunctionsExecuter.prototype = {
	initialize: function(functions) {
		this.functions = functions;
		this.current = 0;
		this.depth = 0; // we need to limit call stack depth
	},
	
	processNext: function() {
		if (this.current < this.functions.length) {
			var f = this.functions[this.current];
			var run = function() {
				f(this.notify.bind(this));
			}.bind(this);
			this.current++;
						
			if (this.depth > 50 || Wicket.Browser.isKHTML() || Wicket.Browser.isSafari()) {
				// to prevent khtml bug that crashes entire browser
				// or to prevent stack overflow (safari has small call stack)
				this.depth = 0;
				window.setTimeout(run, 1);
			} else {
				this.depth ++;
				run();
			}				
		}
	},	
	
	start: function() {
		this.processNext();
	},
	
	notify: function() {
		this.processNext();
	}
}

/* Replaces the element's outer html with the given text. If it's needed
   (for all browsers except gecko based) it takes the newly created scripts elements 
   and adds them to head (execute them) */
Wicket.replaceOuterHtml = function(element, text) {	
    if (element.outerHTML) { // internet explorer or opera
		var parent = element.parentNode;

       
		// find out the element's index and next element (if any). we need to access
		// newly created elements to execute theirs <script elements
		var i;
		var next = null;
		for (i = 0; i < parent.childNodes.length; ++i) {
			if (parent.childNodes[i] == element) {
				if (i != parent.childNodes.length - 1) {
       				next = parent.childNodes[i+1]
       			}
       			break;       			
       		}
		}
		
		// indicates whether we should manually invoke javascripts in the replaced content
		var forceJavascriptExecution = true;
	   
		var tn = element.tagName;
		if (tn != 'TBODY' && tn != 'TR' && tn != "TD" && tn != "THEAD") {			
			element.outerHTML = text;						
		} else {	  		
			// this is a hack to get around the fact that internet explorer doesn't allow the
			// outerHtml attribute on table elements				
			var tempDiv = document.createElement("div");
			tempDiv.innerHTML = '<table style="display: none">' + text + '</table>';			
			element.parentNode.replaceChild(tempDiv.getElementsByTagName(tn).item(0), element);
						
			// this way opera already executes javascripts, so we don't want to execute javascripts later
			if (Wicket.Browser.isOpera())
				forceJavascriptExecution = false;				
		}
       
	    if (forceJavascriptExecution) {
			for (var j = i; j < parent.childNodes.length && parent.childNodes[j] != next; ++j) {	   		
				Wicket.Head.addJavascripts(parent.childNodes[j]);       
			}
		}

    } else {
    	// create range and fragment
        var range = element.ownerDocument.createRange();
        range.selectNode(element);
		var fragment = range.createContextualFragment(text);
		
		// get the elements to be added
		var elements = new Array();
		for (var i = 0; i < fragment.childNodes.length; ++i)
			elements.push(fragment.childNodes[i]);

        element.parentNode.replaceChild(fragment, element);        

		if (document.all != null) {
			for (var i in elements) {
				Wicket.Head.addJavascripts(elements[i]);
			}
		}
    }		
}	

// Decoding functions

Wicket.decode = function(encoding, text) {
    if (encoding == "wicket1") {
        return Wicket.decode1(text);
    }
}

Wicket.decode1 = function(text) {
    return Wicket.replaceAll(text, "]^", "]");
}

Wicket.replaceAll = function(str, from, to) {
    var idx = str.indexOf(from);
    while (idx > -1) {
        str = str.replace(from, to);
        idx = str.indexOf(from);
    }
    return str;
}

// Form serialization

Wicket.Form = { }

Wicket.Form.encode = function(text) {
    if (encodeURIComponent) {
        return encodeURIComponent(text);
    } else {
        return escape(text);
    }
}

Wicket.Form.serializeSelect = function(select){
    var result = "";
    for (var i = 0; i < select.options.length; ++i) {
        var option = select.options[i];
        if (option.selected) {
            result += Wicket.Form.encode(select.name) + "=" + Wicket.Form.encode(option.value) + "&";
        }
    }
    return result;
}

// this function intentionally ignores image and submit inputs
Wicket.Form.serializeInput = function(input) {
    var type = input.type.toLowerCase();
    if ((type == "checkbox" || type == "radio") && input.checked) {
        return Wicket.Form.encode(input.name) + "=" + Wicket.Form.encode(input.value) + "&";
    } else if (type == "text" || type == "password" || type == "hidden" || type == "textarea") {
		return Wicket.Form.encode(input.name) + "=" + Wicket.Form.encode(input.value) + "&";
	} else {
		return "";
    }
}

// returns url/post-body fragment representing element (e) 
Wicket.Form.serializeElement = function(e) {
    var tag = e.tagName.toLowerCase();
    if (tag == "select") {
        return Wicket.Form.serializeSelect(e);
    } else if (tag == "input" || tag == "textarea") {
        return Wicket.Form.serializeInput(e);
    } else {
    	return "";
    }
}

Wicket.Form.serialize = function(form) {
    var result = "";
    for (var i = 0; i < form.elements.length; ++i) {
        var e = form.elements[i];
        if (e.name && e.name != "" && !e.disabled) {
            result += Wicket.Form.serializeElement(e);
        }
    }
    return result;
}

// DOM (nodes serialization)

Wicket.DOM = { }

// method for serializing DOM nodes to string
// original taken from Tacos (http://tacoscomponents.jot.com)
Wicket.DOM.serializeNodeChildren = function(node) {
	if (node == null) { 
		return "" 
	}
	var result = "";
	
	for (var i = 0; i < node.childNodes.length; i++) {
		var thisNode = node.childNodes[i];
		switch (thisNode.nodeType) {
			case 1: // ELEMENT_NODE
			case 5: // ENTITY_REFERENCE_NODE
				result += Wicket.DOM.serializeNode(thisNode);
				break;
			case 8: // COMMENT
				result += "<!--" + thisNode.nodeValue + "-->";
				break;
			case 4: // CDATA_SECTION_NODE
				result += "<![CDATA[" + thisNode.nodeValue + "]]>";
				break;				
			case 3: // TEXT_NODE
			case 2: // ATTRIBUTE_NODE
				result += thisNode.nodeValue;
				break;
			default:
				break;
		}
	}
	return result;	
}


Wicket.DOM.serializeNode = function(node){
	if (node == null) { 
		return "" 
	}
	var result = "";
	result += '<' + node.nodeName;
	
	if (node.attributes && node.attributes.length > 0) {
				
		for (var i = 0; i < node.attributes.length; i++) {
			result += " " + node.attributes[i].name 
				+ "=\"" + node.attributes[i].value + "\"";	
		}
	}
	
	result += '>';
	result += Wicket.DOM.serializeNodeChildren(node);
	result += '</' + node.nodeName + '>';
	return result;
}

Wicket.DOM.containsElement = function(element) {
	var id = element.getAttribute("id");
	if (id != null)
		return document.getElementById(id) != null;
	else
		return false;
}

// Channel manager

Wicket.Channel = Class.create();
Wicket.Channel.prototype = {
	initialize: function(name) {
		var res = name.match(/^([^|]+)\|(d|s)$/)
		if (res == null)
			this.type ='s'; // default to stack 
		else
			this.type = res[2];
		this.callbacks = new Array();
		this.busy = false;
	},	
	
	schedule: function(callback) {
		if (this.busy == false) {
			this.busy = true;			
			return callback();
		} else {
			Wicket.Log.info("Chanel busy - postponing...");
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
			this.busy = false;
		}
	}
};

Wicket.ChannelManager = Class.create();
Wicket.ChannelManager.prototype = {
	initialize: function() {
		this.channels = new Array();
	},
  
	schedule: function(channel, callback) {
		var c = this.channels[channel];
		if (c == null) {
			c = new Wicket.Channel(channel);
			this.channels[channel] = c;
		}
		return c.schedule(callback);
	},
	
	done: function(channel) {
		var c = this.channels[channel];
		if (c != null)
			c.done();
	}
};

Wicket.channelManager = new Wicket.ChannelManager();

// Ajax

Wicket.Ajax = { 
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
	
	getTransport: function() {
		var t = Wicket.Ajax.transports;
		for (var i = 0; i < t.length; ++i) {
			if (t[i].readyState == 0 || t[i].readyState == 4) {
				return t[i];
			}
		}
		t.push(Wicket.Ajax.createTransport());
		return t[t.length-1];		
	},
	
	preCallHandlers: [],
	postCallHandlers: [],	
	failureHandlers: [],
	
	registerPreCallHandler: function(handler) {
		var h = Wicket.Ajax.preCallHandlers;
		h.push(handler);
	},
	
	registerPostCallHandler: function(handler) {
		var h = Wicket.Ajax.postCallHandlers;
		h.push(handler);
	},
	
	registerFailureHandler: function(handler) {
		var h = Wicket.Ajax.failureHandlers;
		h.push(handler);
	},
	
	invokePreCallHandlers: function() {
		var h = Wicket.Ajax.preCallHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking pre-call handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	},
	
	invokePostCallHandlers: function() {
		var h = Wicket.Ajax.postCallHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking post-call handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	},

	invokeFailureHandlers: function() {
		var h = Wicket.Ajax.failureHandlers;
		if (h.length > 0) {
			Wicket.Log.info("Invoking failure handler(s)...");
		}
		for (var i = 0; i < h.length; ++i) {
			h[i]();
		}
	}
}

Wicket.Ajax.Request = Class.create();

Wicket.Ajax.Request.prototype = {
	initialize: function(url, loadedCallback, parseResponse, randomURL, failureHandler, channel) {
		this.url = url;
		this.loadedCallback = loadedCallback;
		this.parseResponse = parseResponse != null ? parseResponse : true;
		this.randomURL = randomURL != null ? randomURL : true;
		this.failureHandler = failureHandler != null ? failureHandler : function() { };
		this.async = false;
		this.channel = channel;
		this.suppressDone = false;
		this.instance = Math.random();
		this.debugContent = true;
	},
	
	done: function() {
		Wicket.channelManager.done(this.channel);
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
		log("Initiating Ajax "+method+" request on " + url);
	},
	
	failure: function() {
		this.failureHandler();
   		Wicket.Ajax.invokePostCallHandlers();
   		Wicket.Ajax.invokeFailureHandlers();
	},
	
	get: function() {
		if (this.channel != null) {
			var res = Wicket.channelManager.schedule(this.channel, this.doGet.bind(this));
			return res != null ? res : true;
		} else {
			return this.doGet();
		}
	},
	
	doGet: function() {
		this.transport = Wicket.Ajax.getTransport();
	
		var url = this.createUrl();	
		this.log("GET", url);
		
		Wicket.Ajax.invokePreCallHandlers();
		
		var t = this.transport;
		if (t != null) {
			t.open("GET", url, this.async);
			t.onreadystatechange = this.stateChangeCallback.bind(this);
			t.setRequestHeader("Wicket-Ajax", "true");
			t.send(null);
			return true;
		} else {
			this.failure();
       		return false;
		}
	},
	
	post: function(body) {
		if (this.channel != null) {
			var res = Wicket.channelManager.schedule(this.channel, function() { this.doPost(body); }.bind(this));
			return res != null ? res: true;
		} else {
			return doPost(this);
		}
	},
	
	doPost: function(body) {
		this.transport = Wicket.Ajax.getTransport();	
	
		var url = this.createUrl();	
		this.log("POST", url);
		
		Wicket.Ajax.invokePreCallHandlers();
		
		var t = this.transport;
		if (t != null) {
			t.open("POST", url, this.async);
			t.onreadystatechange = this.stateChangeCallback.bind(this);
			t.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			t.setRequestHeader("Wicket-Ajax", "true");
			t.send(body);
			return true;
		} else {
       		this.failure();
       		return false;
		}
	},
	
	stateChangeCallback: function() {	
		var t = this.transport;

		if (t != null && t.readyState == 4) {
			if (t.status == 200) {				
				var responseAsText = t.responseText;
				
				var redirectUrl;
				try {
					redirectUrl = t.getResponseHeader('Ajax-Location');
				} catch (ignore) { // might happen in older mozilla
				}
				
				if (typeof(redirectUrl) != "undefined" && redirectUrl != null && redirectUrl != "") {
					t.onreadystatechange = Wicket.emptyFunction;
					window.location = redirectUrl;
				}
				else {
					var log = Wicket.Log.info;				
					log("Received ajax response (" + responseAsText.length + " characters)");
					if (this.debugContent != false) {
						log("\n" + responseAsText);
					}
	        		
	        		if (this.parseResponse == true) {
						var xmldoc;					
						if (typeof(window.XMLHttpRequest) != "undefined" && typeof(DOMParser) != "undefined") {						
							var parser = new DOMParser();
							xmldoc = parser.parseFromString(responseAsText, "text/xml");						
						} else if (window.ActiveXObject) {
							xmldoc = t.responseXML;
						}
						this.loadedCallback(xmldoc); 
					} else {
						this.loadedCallback(responseAsText);
					}        		
					if (this.suppressDone == false)
						this.done();
				}
        	} else {
        		var log = Wicket.Log.error;
        		log("Received Ajax response with code: " + t.status);
		   		this.done();        		
        		this.failure();
        	}    	
        	t.onreadystatechange = Wicket.emptyFunction;
        	this.transport = null;       
        }        
	}
};

Wicket.Ajax.Call = Class.create();

Wicket.Ajax.Call.prototype = {
	initialize: function(url, successHandler, failureHandler, channel) {
		this.successHandler = successHandler != null ? successHandler : function() { };
		this.failureHandler = failureHandler != null ? failureHandler : function() { };
		var c = channel != null ? channel : "0|s";
		this.request = new Wicket.Ajax.Request(url, this.loadedCallback.bind(this), true, true, failureHandler, c);
		this.request.suppressDone = true;
	},
	
	failure: function(message) {
		if (message != null)
			Wicket.Log.error("Error while parsing response: " + message);
		this.request.done();
		this.failureHandler();
   		Wicket.Ajax.invokePostCallHandlers();
   		Wicket.Ajax.invokeFailureHandlers();
	},	
	
	call: function() {	
		return this.request.get();
	},
	
	post: function(body) {
		return this.request.post(body);
	},

	submitForm: function(form, submitButton) {
	    var body = Wicket.Form.serialize(form);
	    if (submitButton != null) {
	        body += Wicket.Form.encode(submitButton) + "=1";
	    }
	    return this.request.post(body);
	},
	
	submitFormById: function(formId, submitButton) {
		var form = document.getElementById(formId);
		if (form == null || typeof (form) == "undefined")
			Wicket.Log.error("Trying to submit form with id '"+formId+"' that is not in document.");
		return this.submitForm(form, submitButton);
	},
	
	loadedCallback: function(envelope) {
		try {			
			var root = envelope.getElementsByTagName("ajax-response")[0];
						
		    if (root == null || root.tagName != "ajax-response") {
		    	this.failure("Could not find root <ajax-response> element");
		    	return;
		    }
						
		    var steps = new Array();

		    if (Wicket.Browser.isKHTML()) {
		    	// there's a nasty bug in KHTML that makes the browser crash
		    	// when the methods are delayed. Therefore we have to fire it
		    	// ASAP
			    steps.push = function(method) {
			    	method(function() { });
			    }
			}
			
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
	
	success: function(steps) {
		steps.push(function(notify) {
			Wicket.Log.info("Response processed successfully.");			
			Wicket.Ajax.invokePostCallHandlers();
			this.request.done();
			this.successHandler();			
			notify();			
		}.bind(this));
	},
	
	processComponent: function(steps, node) {
		steps.push(function(notify) {
			var compId = node.getAttribute("id");
			var text="";

			if (node.hasChildNodes()) {
				text = node.firstChild.nodeValue;
			}

			var encoding = node.getAttribute("encoding");
			if (encoding != null && encoding!="") {
				text = Wicket.decode(encoding, text);
			}
			
			var element = document.getElementById(compId);

			if (element == null || typeof(element) == "undefined") {			
				Wicket.Log.error("Component with id [["+compId+"]] a was not found while trying to perform markup update. Make sure you called component.setOutputMarkupId(true) on the component whose markup you are trying to update.");
			} else {
				Wicket.replaceOuterHtml(element, text);
			}
			notify();
		});
	},
	
	processEvaluation: function(steps, node) {
		steps.push(function(notify) {
		    var text = node.firstChild.nodeValue;
		    var encoding = node.getAttribute("encoding");
		    if (encoding != null) {
		        text = Wicket.decode(encoding, text);
		    }
		    try {
		    	eval(text);
		    } catch (exception) {
		    	Wicket.Log.error("Exception evaluating javascript: " + exception);
		    }
			notify();
		});
	},
	
	processHeaderContribution: function(steps, node) {
		var c = new Wicket.Head.Contributor();
		c.processContribution(steps, node);
	}
};

// Header contribution related methods

Wicket.Head = { };

Wicket.Head.Contributor = Class.create();

Wicket.Head.Contributor.prototype = {
	initialize: function() {
	},
	
	parse: function(headerNode) {
		var text = headerNode.firstChild.nodeValue;	
	    var encoding = headerNode.getAttribute("encoding");
	    
	    if (encoding != null && encoding != "") {
	        text = Wicket.decode(encoding, text);        
	    }       
	    
		// konqueror crashes if there is a <script element in the xml
		text = text.replace(/<script/g,"<SCRIPT");
		text = text.replace(/<\/script>/g,"</SCRIPT>");	
				
		var xmldoc;
		if (window.ActiveXObject) {
	        xmldoc = new ActiveXObject("Microsoft.XMLDOM");
			xmldoc.loadXML(text);
		} else {
		    var parser = new DOMParser();    
		    xmldoc = parser.parseFromString(text, "text/xml");	
		}	
		
		return xmldoc;	
	},
	
	processContribution: function(steps, headerNode) {
		var xmldoc = this.parse(headerNode);
		var rootNode = xmldoc.documentElement;

		for (var i = 0; i < rootNode.childNodes.length; i++) {
			var node = rootNode.childNodes[i];			
			if (node.tagName != null) {
				var name = node.tagName.toLowerCase();
				
				if (name == "wicket:link") {
					// it is a reference surrounded by wicket:link
					// try to find content node
					
					for (var j = 0; j < node.childNodes.length; ++j) {
						var childNode = node.childNodes[j];
						// try to find a regular node inside wicket:link
						
						if (childNode.nodeType == 1) {
							node = childNode;
							name = node.tagName.toLowerCase();
							break;
						}					
					}					
				}
						
			    if (name == "link") {
					this.processLink(steps, node);
				} else if (name == "script") {
					this.processScript(steps, node);
				} else if (name == "style") {
					this.processStyle(steps, node);
				}
			}
		}	
	},
	
	processLink: function(steps, node) {		
		steps.push(function(notify) {
			if (Wicket.Head.containsElement(node, "href")) {
				notify();
				return;
			}
			var css = Wicket.Head.createElement("link");
			css.id = node.getAttribute("id");
			css.rel = node.getAttribute("rel");
			css.href = node.getAttribute("href");
			css.type = node.getAttribute("type");
			Wicket.Head.addElement(css);
			notify();
		});
	},
	
	processStyle: function(steps, node) {
		steps.push(function(notify) {
			if (Wicket.DOM.containsElement(node)) {
				notify();
				return;
			}	
			var content = Wicket.DOM.serializeNodeChildren(node);
			var style = Wicket.Head.createElement("style");
			style.id = node.getAttribute("id");										
				
			if (Wicket.Browser.isIE()) { 			
				document.createStyleSheet().cssText = content;
			} else {			
				var textNode = document.createTextNode(content);
				style.appendChild(textNode);
			} 		
			Wicket.Head.addElement(style);
			notify();
		});
	},
	
	processScript: function(steps, node) {
		steps.push(function(notify) {		
			if (Wicket.DOM.containsElement(node) ||
				Wicket.Head.containsElement(node, "src")) {
				notify(); 
				return;
			}
			var src = node.getAttribute("src");
			if (src != null && src != "") {
				var onLoad = function(content) {
					Wicket.Head.addJavascript(content, null, src);
					Wicket.Ajax.invokePostCallHandlers();
					notify();
				}
				// we need to schedule the request as timeout
				// calling xml http request from another request call stack doesn't work
				window.setTimeout(function() {
					var req = new Wicket.Ajax.Request(src, onLoad, false, false);
					req.debugContent = false;
					if (Wicket.Browser.isKHTML())
						req.async = false;
					req.get();					
				},1);
			} else {
				var text = Wicket.DOM.serializeNodeChildren(node);
				Wicket.Head.addJavascript(text, node.getAttribute("id"));
				notify();
			}
		});					
	}	
};


Wicket.Head.createElement = function(name) {
	return document.createElement(name);
}

Wicket.Head.addElement = function(element) {
	var head = document.getElementsByTagName("head");

	if (head[0]) {
		head[0].appendChild(element);
	}
}

Wicket.Head.containsElement = function(element, mandatoryAttribute) {
	var attr = element.getAttribute(mandatoryAttribute);
	if (attr == null || attr == "" || typeof(attr) == "undefined")
		return false;

	var head = document.getElementsByTagName("head")[0];
	var nodes = head.getElementsByTagName(element.tagName);
	for (var i = 0; i < nodes.length; ++i) {
		var node = nodes[i];		
		if (node.tagName.toLowerCase() == element.tagName.toLowerCase() &&
			(node.getAttribute(mandatoryAttribute) == attr ||
		     node.getAttribute(mandatoryAttribute+"_") == attr)) {
		    return true;
		}
	}
	return false;
}

Wicket.Head.addJavascript = function(content, id, fakeSrc) {
	var script = Wicket.Head.createElement("script");
	script.id = id;
	script.setAttribute("src_", fakeSrc);
	if (null == script.canHaveChildren || script.canHaveChildren) {
		var textNode = document.createTextNode(content);			
		script.appendChild(textNode);
	} else {
		script.text = content;
	} 		
	Wicket.Head.addElement(script);	
}

/* Goes through all script elements contained by the element and add them to head. */
Wicket.Head.addJavascripts = function(element) {	
	function add(element) {
		var content = Wicket.DOM.serializeNodeChildren(element);
		if (content == null || content == "")
			content = element.text;
		Wicket.Head.addJavascript(content);		
	}
	if (typeof(element) != "undefined" &&
	    typeof(element.tagName) != "undefined" &&
	    element.tagName.toLowerCase() == "script") {
		add(element);
	} else {
		// we need to check if there are any children, becase Safari
		// aborts when the element is a text node			
		if (element.childNodes.length > 0) {			
			var scripts = element.getElementsByTagName("script");
			for (var i = 0; i < scripts.length; ++i) {
				add(scripts[i]);
			}
		}
	}
}

// Throttler

Wicket.ThrottlerEntry = Class.create();
Wicket.ThrottlerEntry.prototype = {
	initialize: function(func) {
		this.func = func;
		this.timestamp = new Date().getTime();
	},
	
	getTimestamp: function() {
		return this.timestamp;
	},
	
	getFunc: function() {
		return this.func;
	},
	
	setFunc: function(func) {
		this.func = func;
	}
};

Wicket.Throttler = Class.create();
Wicket.Throttler.prototype = {
	initialize: function() {
		this.entries = new Array();
	},
	
	throttle: function(id, millis, func) {
		var entry = this.entries[id];
		var me = this;
		if (entry == undefined) {
			entry = new Wicket.ThrottlerEntry(func);
			this.entries[id] = entry;
			window.setTimeout(function() { me.execute(id); }, millis);
		} else {
			entry.setFunc(func);
		}	
	},
	
	execute: function(id) {
		var entry = this.entries[id];
		if (entry != undefined) {
			var func = entry.getFunc();
			var tmp = func();
		}
		
		this.entries[id] = undefined;	
	}
};

Wicket.throttler = new Wicket.Throttler();


/*
 * Compatibility layer
 */

var wicketThrottler = Wicket.throttler;

function wicketAjaxGet(url, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.call();
}

function wicketAjaxPost(url, body, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.post(body);
}

function wicketSubmitForm(form, url, submitButton, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.submitForm(form, submitButton);
}

function wicketSubmitFormById(formId, url, submitButton, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, successHandler, failureHandler, channel);
	return call.submitFormById(formId, submitButton);
}

wicketSerialize = Wicket.Form.serializeElement;

wicketSerializeForm = Wicket.Form.serialize;

wicketEncode = Wicket.Form.encode;

wicketDecode = Wicket.decode;

wicketAjaxGetTransport = Wicket.Ajax.getTransport;

// Global handlers stubs

Wicket.Ajax.registerPreCallHandler(function() {
	if (typeof(window.wicketGlobalPreCallHandler) != "undefined") {
	    var global=wicketGlobalPreCallHandler;
	    if (global!=null) {
	    	global();
	    }
	}    
});

Wicket.Ajax.registerPostCallHandler(function() {
	if (typeof(window.wicketGlobalPostCallHandler) != "undefined") {
	    var global=wicketGlobalPostCallHandler;
	    if (global!=null) {
	    	global();
	    }
	}    
});

Wicket.Ajax.registerFailureHandler(function() {
	if (typeof(window.wicketGlobalFailureHandler) != "undefined") {
	    var global=wicketGlobalFailureHandler;
	    if (global!=null) {
	    	global();
	    }
	}    
});

// DEBUG FUNCTIONS
function wicketAjaxDebugEnabled() {
    if (typeof(wicketAjaxDebugEnable)=="undefined") {
        return false;
    } else {
        return wicketAjaxDebugEnable==true;
    }
}

// MISC FUNCTIONS
function wicketKeyCode(event) {
    if (typeof(event.keyCode)=="undefined") {
        return event.which;
    } else {
        return event.keyCode;
    }
}

function wicketGet(id) {
    return document.getElementById(id);
}

function wicketShow(id) {
    var e=wicketGet(id);
    e.style.display = "";
}

function wicketHide(id) {
    var e=wicketGet(id);
    e.style.display = "none";
}

