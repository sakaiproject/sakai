if(!dojo._hasResource["dojox.io.proxy.xip"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.io.proxy.xip"] = true;
dojo.provide("dojox.io.proxy.xip");

dojo.require("dojo.io.iframe");
dojo.require("dojox.data.dom");

dojox.io.proxy.xip = {
	//summary: Object that implements the iframe handling for XMLHttpRequest
	//IFrame Proxying.
	//description: Do not use this object directly. See the Dojo Book page
	//on XMLHttpRequest IFrame Proxying:
	//http://dojotoolkit.org/book/dojo-book-0-4/part-5-connecting-pieces/i-o/cross-domain-xmlhttprequest-using-iframe-proxy
	//Usage of XHR IFrame Proxying does not work from local disk in Safari.

	xipClientUrl: djConfig["xipClientUrl"] || dojo.moduleUrl("dojox.io.proxy", "xip_client.html"),

	_state: {},
	_stateIdCounter: 0,

	needFrameRecursion: function(){
		return (dojo.isIE >= 7);
	},

	send: function(facade){		
		var stateId = "XhrIframeProxy" + (this._stateIdCounter++);
		facade._stateId = stateId;


		var frameUrl = this.xipClientUrl + "#0:init:id=" + stateId + "&server=" 
			+ encodeURIComponent(facade._ifpServerUrl) + "&fr=false";
		if(this.needFrameRecursion()){
			//IE7 hack. Need to load server URL, and have that load the xip_client.html.
			//Also, this server URL needs to different from the one eventually loaded by xip_client.html
			//Otherwise, IE7 will not load it. Funky.
			var fullClientUrl = window.location.href.split("#")[0].split("?")[0];
			if((this.xipClientUrl + "").charAt(0) == "/"){
				var endIndex = fullClientUrl.indexOf("://");
				endIndex = fullClientUrl.indexOf("/", endIndex + 3);
				fullClientUrl = fullClientUrl.substring(0, endIndex);
			}else{
				fullClientUrl = fullClientUrl.substring(0, fullClientUrl.lastIndexOf("/") + 1);
			}
			fullClientUrl += this.xipClientUrl;
		
			var serverUrl = facade._ifpServerUrl
				+ (facade._ifpServerUrl.indexOf("?") == -1 ? "?" : "&") + "dojo.fr=1";

			frameUrl = serverUrl + "#0:init:id=" + stateId + "&client=" 
				+ encodeURIComponent(fullClientUrl) + "&fr=" + this.needFrameRecursion(); //fr is for Frame Recursion
		}

		this._state[stateId] = {
			facade: facade,
			stateId: stateId,
			clientFrame: dojo.io.iframe.create(stateId, "", frameUrl)
		};
		
		return stateId;
	},
	
	receive: function(/*String*/stateId, /*String*/urlEncodedData){
		/* urlEncodedData should have the following params:
				- responseHeaders
				- status
				- statusText
				- responseText
		*/
		//Decode response data.
		var response = {};
		var nvPairs = urlEncodedData.split("&");
		for(var i = 0; i < nvPairs.length; i++){
			if(nvPairs[i]){
				var nameValue = nvPairs[i].split("=");
				response[decodeURIComponent(nameValue[0])] = decodeURIComponent(nameValue[1]);
			}
		}

		//Set data on facade object.
		var state = this._state[stateId];
		var facade = state.facade;

		facade._setResponseHeaders(response.responseHeaders);
		if(response.status == 0 || response.status){
			facade.status = parseInt(response.status, 10);
		}
		if(response.statusText){
			facade.statusText = response.statusText;
		}
		if(response.responseText){
			facade.responseText = response.responseText;
			
			//Fix responseXML.
			var contentType = facade.getResponseHeader("Content-Type");
			if(contentType && (contentType == "application/xml" || contentType == "text/xml")){
				facade.responseXML = dojox.data.dom.createDocument(response.responseText, contentType);
			}
		}
		facade.readyState = 4;
		
		this.destroyState(stateId);
	},

	clientFrameLoaded: function(/*String*/stateId){
		var state = this._state[stateId];
		var facade = state.facade;

		if(this.needFrameRecursion()){
			var clientWindow = window.open("", state.stateId + "_clientEndPoint");
		}else{
			var clientWindow = state.clientFrame.contentWindow;
		}

		var reqHeaders = [];
		for(var param in facade._requestHeaders){
			reqHeaders.push(param + ": " + facade._requestHeaders[param]);
		}
		
		var requestData = {
			uri: facade._uri
		};
		if(reqHeaders.length > 0){
			requestData.requestHeaders = reqHeaders.join("\r\n");		
		}
		if(facade._method){
			requestData.method = facade._method;
		}
		if(facade._bodyData){
			requestData.data = facade._bodyData;
		}

		clientWindow.send(dojo.objectToQuery(requestData));
	},
	
	destroyState: function(/*String*/stateId){
		var state = this._state[stateId];
		if(state){
			delete this._state[stateId];
			var parentNode = state.clientFrame.parentNode;
			parentNode.removeChild(state.clientFrame);
			state.clientFrame = null;
			state = null;
		}
	},

	createFacade: function(){
		if(arguments && arguments[0] && arguments[0].iframeProxyUrl){
			return new dojox.io.proxy.xip.XhrIframeFacade(arguments[0].iframeProxyUrl);
		}else{
			return dojox.io.proxy.xip._xhrObjOld.apply(dojo, arguments);
		}
	}
}

//Replace the normal XHR factory with the proxy one.
dojox.io.proxy.xip._xhrObjOld = dojo._xhrObj;
dojo._xhrObj = dojox.io.proxy.xip.createFacade;

/**
	Using this a reference: http://www.w3.org/TR/XMLHttpRequest/

	Does not implement the onreadystate callback since dojo.xhr* does
	not use it.
*/
dojox.io.proxy.xip.XhrIframeFacade = function(ifpServerUrl){
	//summary: XMLHttpRequest facade object used by dojox.io.proxy.xip.
	
	//description: Do not use this object directly. See the Dojo Book page
	//on XMLHttpRequest IFrame Proxying:
	//http://dojotoolkit.org/book/dojo-book-0-4/part-5-connecting-pieces/i-o/cross-domain-xmlhttprequest-using-iframe-proxy
	this._requestHeaders = {};
	this._allResponseHeaders = null;
	this._responseHeaders = {};
	this._method = null;
	this._uri = null;
	this._bodyData = null;
	this.responseText = null;
	this.responseXML = null;
	this.status = null;
	this.statusText = null;
	this.readyState = 0;
	
	this._ifpServerUrl = ifpServerUrl;
	this._stateId = null;
}

dojo.extend(dojox.io.proxy.xip.XhrIframeFacade, {
	//The open method does not properly reset since Dojo does not reuse XHR objects.
	open: function(/*String*/method, /*String*/uri){
		this._method = method;
		this._uri = uri;

		this.readyState = 1;
	},
	
	setRequestHeader: function(/*String*/header, /*String*/value){
		this._requestHeaders[header] = value;
	},
	
	send: function(/*String*/stringData){
		this._bodyData = stringData;
		
		this._stateId = dojox.io.proxy.xip.send(this);
		
		this.readyState = 2;
	},
	abort: function(){
		dojox.io.proxy.xip.destroyState(this._stateId);
	},
	
	getAllResponseHeaders: function(){
		return this._allResponseHeaders; //String
	},
	
	getResponseHeader: function(/*String*/header){
		return this._responseHeaders[header]; //String
	},
	
	_setResponseHeaders: function(/*String*/allHeaders){
		if(allHeaders){
			this._allResponseHeaders = allHeaders;
			
			//Make sure ther are now CR characters in the headers.
			allHeaders = allHeaders.replace(/\r/g, "");
			var nvPairs = allHeaders.split("\n");
			for(var i = 0; i < nvPairs.length; i++){
				if(nvPairs[i]){
					var nameValue = nvPairs[i].split(": ");
					this._responseHeaders[nameValue[0]] = nameValue[1];
				}
			}
		}
	}
});

}
