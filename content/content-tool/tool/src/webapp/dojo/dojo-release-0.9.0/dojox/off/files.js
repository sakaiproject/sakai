if(!dojo._hasResource["dojox.off.files"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.off.files"] = true;
dojo.provide("dojox.off.files");

// Author: Brad Neuberg, bkn3@columbia.edu, http://codinginparadise.org

// summary:
//	Helps maintain resources that should be
//	available offline, such as CSS files.
// description:
//	dojox.off.files makes it easy to indicate
//	what resources should be available offline,
//	such as CSS files, JavaScript, HTML, etc.
dojox.off.files = {
	// versionURL: String
	//	An optional file, that if present, records the version
	//	of our bundle of files to make available offline. If this
	//	file is present, and we are not currently debugging,
	//	then we only refresh our offline files if the version has
	//	changed. 
	versionURL: "version.js",
	
	// listOfURLs: Array
	//	For advanced usage; most developers can ignore this.
	//	Our list of URLs that will be cached and made available
	//	offline.
	listOfURLs: [],
	
	// refreshing: boolean
	//	For advanced usage; most developers can ignore this.
	//	Whether we are currently in the middle
	//	of refreshing our list of offline files.
	refreshing: false,

	_cancelID: null,
	
	_error: false,
	_errorMessages: [],
	_currentFileIndex: 0,
	_store: null,
	_doSlurp: false,
	
	slurp: function(){
		// summary:
		//	Autoscans the page to find all resources to
		//	cache. This includes scripts, images, CSS, and hyperlinks
		//	to pages that are in the same scheme/port/host as this
		//	page. We also scan the embedded CSS of any stylesheets
		//	to find @import statements and url()'s.
		//  You should call this method from the top-level, outside of
		//	any functions and before the page loads:
		//
		//	<script>
		//		dojo.require("dojox.sql");
		//		dojo.require("dojox.off");
		//		dojo.require("dojox.off.ui");
		//		dojo.require("dojox.off.sync");
		//
		//		// configure how we should work offline
		//
		//		// set our application name
		//		dojox.off.ui.appName = "Moxie";
		//
		//		// automatically "slurp" the page and
		//		// capture the resources we need offline
		//		dojox.off.files.slurp();
		//
		// 		// tell Dojo Offline we are ready for it to initialize itself now
		//		// that we have finished configuring it for our application
		//		dojox.off.initialize();
		//	</script>
		//
		//	Note that inline styles on elements are not handled (i.e.
		//	if you somehow have an inline style that uses a URL);
		//	object and embed tags are not scanned since their format
		//	differs based on type; and elements created by JavaScript
		//	after page load are not found. For these you must manually
		//	add them with a dojox.off.files.cache() method call.
		
		// just schedule the slurp once the page is loaded and
		// Dojo Offline is ready to slurp; dojox.off will call
		// our _slurp() method before indicating it is finished
		// loading
		this._doSlurp = true;
	},
	
	cache: function(urlOrList){ /* void */
		// summary:
		//		Caches a file or list of files to be available offline. This
		//		can either be a full URL, such as http://foobar.com/index.html,
		//		or a relative URL, such as ../index.html. This URL is not
		//		actually cached until dojox.off.sync.synchronize() is called.
		// urlOrList: String or Array[]
		//		A URL of a file to cache or an Array of Strings of files to
		//		cache
		
		//console.debug("dojox.off.files.cache, urlOrList="+urlOrList);
		
		if(dojo.isString(urlOrList)){
			var url = this._trimAnchor(urlOrList+"");
			if(!this.isAvailable(url)){ 
				this.listOfURLs.push(url); 
			}
		}else if(urlOrList instanceof dojo._Url){
			var url = this._trimAnchor(urlOrList.uri);
			if(!this.isAvailable(url)){ 
				this.listOfURLs.push(url); 
			}
		}else{
			dojo.forEach(urlOrList, function(url){
				url = this._trimAnchor(url);
				if(!this.isAvailable(url)){ 
					this.listOfURLs.push(url); 
				}
			}, this);
		}
	},
	
	printURLs: function(){
		// summary:
		//	A helper function that will dump and print out
		//	all of the URLs that are cached for offline
		//	availability. This can help with debugging if you
		//	are trying to make sure that all of your URLs are
		//	available offline
		console.debug("The following URLs are cached for offline use:");
		dojo.forEach(this.listOfURLs, function(i){
			console.debug(i);
		});	
	},
	
	remove: function(url){ /* void */
		// summary:
		//		Removes a URL from the list of files to cache.
		// description:
		//		Removes a URL from the list of URLs to cache. Note that this
		//		does not actually remove the file from the offline cache;
		//		instead, it just prevents us from refreshing this file at a
		//		later time, so that it will naturally time out and be removed
		//		from the offline cache
		// url: String
		//		The URL to remove
		for(var i = 0; i < this.listOfURLs.length; i++){
			if(this.listOfURLs[i] == url){
				this.listOfURLs = this.listOfURLs.splice(i, 1);
				break;
			}
		}
	},
	
	isAvailable: function(url){ /* boolean */
		// summary:
		//		Determines whether the given resource is available offline.
		// url: String
		//	The URL to check
		for(var i = 0; i < this.listOfURLs.length; i++){
			if(this.listOfURLs[i] == url){
				return true;
			}
		}
		
		return false;
	},
	
	refresh: function(callback){ /* void */
		//console.debug("dojox.off.files.refresh");
		// summary:
		//	For advanced usage; most developers can ignore this.
		//	Refreshes our list of offline resources,
		//	making them available offline.
		// callback: Function
		//	A callback that receives two arguments: whether an error
		//	occurred, which is a boolean; and an array of error message strings
		//	with details on errors encountered. If no error occured then message is
		//	empty array with length 0.
		try{
			if(djConfig.isDebug){
				this.printURLs();
			}
			
			this.refreshing = true;
			
			if(this.versionURL){
				this._getVersionInfo(function(oldVersion, newVersion, justDebugged){
					//console.warn("getVersionInfo, oldVersion="+oldVersion+", newVersion="+newVersion
					//				+ ", justDebugged="+justDebugged+", isDebug="+djConfig.isDebug);
					if(djConfig.isDebug || !newVersion || justDebugged 
							|| !oldVersion || oldVersion != newVersion){
						console.warn("Refreshing offline file list");
						this._doRefresh(callback, newVersion);
					}else{
						console.warn("No need to refresh offline file list");
						callback(false, []);
					}
				});
			}else{
				console.warn("Refreshing offline file list");
				this._doRefresh(callback);
			}
		}catch(e){
			this.refreshing = false;
                       
			// can't refresh files -- core operation --
			// fail fast
			dojox.off.coreOpFailed = true;
			dojox.off.enabled = false;
			dojox.off.onFrameworkEvent("coreOperationFailed");
		}
	},
	
	abortRefresh: function(){
		// summary:
		//	For advanced usage; most developers can ignore this.
		//	Aborts and cancels a refresh.
		if(!this.refreshing){
			return;
		}
		
		this._store.abortCapture(this._cancelID);
		this.refreshing = false;
	},
	
	_slurp: function(){
		if(!this._doSlurp){
			return;
		}
		
		var handleUrl = dojo.hitch(this, function(url){
			if(this._sameLocation(url)){
				this.cache(url);
			}
		});
		
		handleUrl(window.location.href);
		
		dojo.query("script").forEach(function(i){
			try{
				handleUrl(i.getAttribute("src"));
			}catch(exp){
				//console.debug("dojox.off.files.slurp 'script' error: " 
				//				+ exp.message||exp);
			}
		});
		
		dojo.query("link").forEach(function(i){
			try{
				if(!i.getAttribute("rel")
					|| i.getAttribute("rel").toLowerCase() != "stylesheet"){
					return;
				}
			
				handleUrl(i.getAttribute("href"));
			}catch(exp){
				//console.debug("dojox.off.files.slurp 'link' error: " 
				//				+ exp.message||exp);
			}
		});
		
		dojo.query("img").forEach(function(i){
			try{
				handleUrl(i.getAttribute("src"));
			}catch(exp){
				//console.debug("dojox.off.files.slurp 'img' error: " 
				//				+ exp.message||exp);
			}
		});
		
		dojo.query("a").forEach(function(i){
			try{
				handleUrl(i.getAttribute("href"));
			}catch(exp){
				//console.debug("dojox.off.files.slurp 'a' error: " 
				//				+ exp.message||exp);
			}
		});
		
		// FIXME: handle 'object' and 'embed' tag
		
		// parse our style sheets for inline URLs and imports
		dojo.forEach(document.styleSheets, function(sheet){
			try{
				if(sheet.cssRules){ // Firefox
					dojo.forEach(sheet.cssRules, function(rule){
						var text = rule.cssText;
						if(text){
							var matches = text.match(/url\(\s*([^\) ]*)\s*\)/i);
							if(!matches){
								return;
							}
							
							for(var i = 1; i < matches.length; i++){
								handleUrl(matches[i])
							}
						}
					});
				}else if(sheet.cssText){ // IE
					var matches;
					var text = sheet.cssText.toString();
					// unfortunately, using RegExp.exec seems to be flakey
					// for looping across multiple lines on IE using the
					// global flag, so we have to simulate it
					var lines = text.split(/\f|\r|\n/);
					for(var i = 0; i < lines.length; i++){
						matches = lines[i].match(/url\(\s*([^\) ]*)\s*\)/i);
						if(matches && matches.length){
							handleUrl(matches[1]);
						}
					}
				}
			}catch(exp){
				//console.debug("dojox.off.files.slurp stylesheet parse error: " 
				//				+ exp.message||exp);
			}
		});
		
		//this.printURLs();
	},
	
	_sameLocation: function(url){
		if(!url){ return false; }
		
		// filter out anchors
		if(url.length && url.charAt(0) == "#"){
			return false;
		}
		
		// FIXME: dojo._Url should be made public;
		// it's functionality is very useful for
		// parsing URLs correctly, which is hard to
		// do right
		url = new dojo._Url(url);
		
		// totally relative -- ../../someFile.html
		if(!url.scheme && !url.port && !url.host){ 
			return true;
		}
		
		// scheme relative with port specified -- brad.com:8080
		if(!url.scheme && url.host && url.port
				&& window.location.hostname == url.host
				&& window.location.port == url.port){
			return true;
		}
		
		// scheme relative with no-port specified -- brad.com
		if(!url.scheme && url.host && !url.port
			&& window.location.hostname == url.host
			&& window.location.port == 80){
			return true;
		}
		
		// else we have everything
		return  window.location.protocol == (url.scheme + ":")
				&& window.location.hostname == url.host
				&& (window.location.port == url.port || !window.location.port && !url.port);
	},
	
	_trimAnchor: function(url){
		return url.replace(/\#.*$/, "");
	},
	
	_doRefresh: function(callback, newVersion){
		// get our local server
		var localServer;
		try{
			localServer = google.gears.factory.create("beta.localserver", "1.0");
		}catch(exp){
			dojo.setObject("google.gears.denied", true);
			dojox.off.onFrameworkEvent("coreOperationFailed");
			throw "Google Gears must be allowed to run";
		}
		
		var storeName = "dot_store_" 
							+ window.location.href.replace(/[^0-9A-Za-z_]/g, "_");
			
		// refresh everything by simply removing
		// any older stores
		localServer.removeStore(storeName);
		
		// open/create the resource store
		localServer.openStore(storeName);
		var store = localServer.createStore(storeName);
		this._store = store;

		// add our list of files to capture
		var self = this;
		this._currentFileIndex = 0;
		this._cancelID = store.capture(this.listOfURLs, function(url, success, captureId){
			//console.debug("store.capture, url="+url+", success="+success);
			if(!success && self.refreshing){
				self._cancelID = null;
				self.refreshing = false;
				var errorMsgs = [];
				errorMsgs.push("Unable to capture: " + url);
				callback(true, errorMsgs);
				return;
			}else if(success){
				self._currentFileIndex++;
			}
			
			if(success && self._currentFileIndex >= self.listOfURLs.length){
				self._cancelID = null;
				self.refreshing = false;
				if(newVersion){
					dojox.storage.put("oldVersion", newVersion, null,
									dojox.off.STORAGE_NAMESPACE);
				}
				dojox.storage.put("justDebugged", djConfig.isDebug, null,
									dojox.off.STORAGE_NAMESPACE);
				callback(false, []);
			}
		});
	},
	
	_getVersionInfo: function(callback){
		var justDebugged = dojox.storage.get("justDebugged", 
									dojox.off.STORAGE_NAMESPACE);
		var oldVersion = dojox.storage.get("oldVersion",
									dojox.off.STORAGE_NAMESPACE);
		var newVersion = null;
		
		callback = dojo.hitch(this, callback);
		
		dojo.xhrGet({
				url: this.versionURL + "?browserbust=" + new Date().getTime(),
				timeout: 5 * 1000,
				handleAs: "javascript",
				error: function(err){
					//console.warn("dojox.off.files._getVersionInfo, err=",err);
					dojox.storage.remove("oldVersion", dojox.off.STORAGE_NAMESPACE);
					dojox.storage.remove("justDebugged", dojox.off.STORAGE_NAMESPACE);
					callback(oldVersion, newVersion, justDebugged);
				},
				load: function(data){
					//console.warn("dojox.off.files._getVersionInfo, load=",data);
					
					// some servers incorrectly return 404's
					// as a real page
					if(data){
						newVersion = data;
					}
					
					callback(oldVersion, newVersion, justDebugged);
				}
		});
	}
}

}
