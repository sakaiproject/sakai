if(!dojo._hasResource["dojox.off._common"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.off._common"] = true;
dojo.provide("dojox.off._common");

dojo.require("dojox.storage");
dojo.require("dojox.sql");
dojo.require("dojox.off.sync");

// Author: Brad Neuberg, bkn3@columbia.edu, http://codinginparadise.org

// summary:
//		dojox.off is the main object for offline applications.
dojo.mixin(dojox.off, {
	// isOnline: boolean
	//	true if we are online, false if not
	isOnline: false,
	
	// NET_CHECK: int
	//		For advanced usage; most developers can ignore this.
	//		Time in seconds on how often we should check the status of the
	//		network with an automatic background timer. The current default
	//		is 5 seconds.
	NET_CHECK: 5,
	
	// STORAGE_NAMESPACE: String
	//		For advanced usage; most developers can ignore this.
	//		The namespace we use to save core data into Dojo Storage.
	STORAGE_NAMESPACE: "_dot",
	
	// enabled: boolean
	//		For advanced usage; most developers can ignore this.
	//		Whether offline ability is enabled or not. Defaults to true.
	enabled: true,
	
	// availabilityURL: String
	//		For advanced usage; most developers can ignore this.
	//		The URL to check for site availability.  We do a GET request on
	//		this URL to check for site availability.  By default we check for a
	//		simple text file in src/off/network_check.txt that has one value
	//		it, the value '1'.
	availabilityURL: dojo.moduleUrl("dojox", "off/network_check.txt"),
	
	// goingOnline: boolean
	//		For advanced usage; most developers can ignore this.
	//		True if we are attempting to go online, false otherwise
	goingOnline: false,
	
	// coreOpFailed: boolean
	//		For advanced usage; most developers can ignore this.
	//		A flag set by the Dojo Offline framework that indicates that the
	//		user denied some operation that required the offline cache or an
	//		operation failed in some critical way that was unrecoverable. For
	//		example, if the offline cache is Google Gears and we try to get a
	//		Gears database, a popup window appears asking the user whether they
	//		will approve or deny this request. If the user denies the request,
	//		and we are doing some operation that is core to Dojo Offline, then
	//		we set this flag to 'true'.  This flag causes a 'fail fast'
	//		condition, turning off offline ability.
	coreOpFailed: false,
	
	// doNetChecking: boolean
	//		For advanced usage; most developers can ignore this.
	//		Whether to have a timing interval in the background doing automatic
	//		network checks at regular intervals; the length of time between
	//		checks is controlled by dojox.off.NET_CHECK. Defaults to true.
	doNetChecking: true,
	
	// hasOfflineCache: boolean
	//		For advanced usage; most developers can ignore this.
	//  	Determines if an offline cache is available or installed; an
	//  	offline cache is a facility that can truely cache offline
	//  	resources, such as JavaScript, HTML, etc. in such a way that they
	//  	won't be removed from the cache inappropriately like a browser
	//  	cache would. If this is false then an offline cache will be
	//  	installed. Only Google Gears is currently supported as an offline
	//  	cache. Future possible offline caches include Firefox 3.
	hasOfflineCache: null,
	
	// browserRestart: boolean
	//		For advanced usage; most developers can ignore this.
	//		If true, the browser must be restarted to register the existence of
	//		a new host added offline (from a call to addHostOffline); if false,
	//		then nothing is needed.
	browserRestart: false,
	
	_STORAGE_APP_NAME: window.location.href.replace(/[^0-9A-Za-z_]/g, "_"),
	
	_initializeCalled: false,
	_storageLoaded: false,
	_pageLoaded: false,
	
	onLoad: function(){
		// summary:
		//	Called when Dojo Offline can be used.
		// description:
		//	Do a dojo.connect to this to know when you can
		//	start using Dojo Offline:
		//		dojo.connect(dojox.off, "onLoad", myFunc);
	},
	
	onNetwork: function(type){
		// summary:
		//	Called when our on- or offline- status changes.
		// description:
		//	If we move online, then this method is called with the
		//	value "online". If we move offline, then this method is
		//	called with the value "offline". You can connect to this
		//	method to do add your own behavior:
		//
		//		dojo.connect(dojox.off, "onNetwork", someFunc)
		//
		//	Note that if you are using the default Dojo Offline UI
		//	widget that most of the on- and off-line notification
		//	and syncing is automatically handled and provided to the
		//	user.
		// type: String
		//	Either "online" or "offline".
	},
	
	initialize: function(){ /* void */
		// summary:
		//		Called when a Dojo Offline-enabled application is finished
		//		configuring Dojo Offline, and is ready for Dojo Offline to
		//		initialize itself.
		// description:
		//		When an application has finished filling out the variables Dojo
		//		Offline needs to work, such as dojox.off.ui.appName, it must
		//		this method to tell Dojo Offline to initialize itself.
		
		//		Note:
		//		This method is needed for a rare edge case. In some conditions,
		//		especially if we are dealing with a compressed Dojo build, the
		//		entire Dojo Offline subsystem might initialize itself and be
		//		running even before the JavaScript for an application has had a
		//		chance to run and configure Dojo Offline, causing Dojo Offline
		//		to have incorrect initialization parameters for a given app,
		//		such as no value for dojox.off.ui.appName. This method is
		//		provided to prevent this scenario, to slightly 'slow down' Dojo
		//		Offline so it can be configured before running off and doing
		//		its thing.	

		//console.debug("dojox.off.initialize");
		this._initializeCalled = true;
		
		if(this._storageLoaded && this._pageLoaded){
			this._onLoad();
		}
	},
	
	goOffline: function(){ /* void */
		// summary:
		//		For advanced usage; most developers can ignore this.
		//		Manually goes offline, away from the network.
		if((dojox.off.sync.isSyncing)||(this.goingOnline)){ return; }
		
		this.goingOnline = false;
		this.isOnline = false;
	},
	
	goOnline: function(callback){ /* void */
		// summary: 
		//		For advanced usage; most developers can ignore this.
		//		Attempts to go online.
		// description:
		//		Attempts to go online, making sure this web application's web
		//		site is available. 'callback' is called asychronously with the
		//		result of whether we were able to go online or not.
		// callback: Function
		//		An optional callback function that will receive one argument:
		//		whether the site is available or not and is boolean. If this
		//		function is not present we call dojo.xoff.onOnline instead if
		//		we are able to go online.
		
		//console.debug("goOnline");
		
		if(dojox.off.sync.isSyncing || dojox.off.goingOnline){
			return;
		}
		
		this.goingOnline = true;
		this.isOnline = false;
		
		// see if can reach our web application's web site
		this._isSiteAvailable(callback);
	},
	
	onFrameworkEvent: function(type /* String */, saveData /* Object? */){
		//	summary:
		//		For advanced usage; most developers can ignore this.
		//		A standard event handler that can be attached to to find out
		//		about low-level framework events. Most developers will not need to
		//		attach to this method; it is meant for low-level information
		//		that can be useful for updating offline user-interfaces in
		//		exceptional circumstances. The default Dojo Offline UI
		//		widget takes care of most of these situations.
		//	type: String
		//		The type of the event:
		//
		//		* "offlineCacheInstalled"
		//			An event that is fired when a user
		//			has installed an offline cache after the page has been loaded.
		//			If a user didn't have an offline cache when the page loaded, a
		//			UI of some kind might have prompted them to download one. This
		//			method is called if they have downloaded and installed an
		//			offline cache so a UI can reinitialize itself to begin using
		//			this offline cache.
		//		* "coreOperationFailed"
		//			Fired when a core operation during interaction with the
		//			offline cache is denied by the user. Some offline caches, such
		//			as Google Gears, prompts the user to approve or deny caching
		//			files, using the database, and more. If the user denies a
		//			request that is core to Dojo Offline's operation, we set
		//			dojox.off.coreOpFailed to true and call this method for
		//			listeners that would like to respond some how to Dojo Offline
		//			'failing fast'.
		//		* "save"
		//			Called whenever the framework saves data into persistent
		//			storage. This could be useful for providing save feedback
		//			or providing appropriate error feedback if saving fails 
		//			due to a user not allowing the save to occur
		//	saveData: Object?
		//		If the type was 'save', then a saveData object is provided with
		//		further save information. This object has the following properties:	
		//
		//		* status - dojox.storage.SUCCESS, dojox.storage.PENDING, dojox.storage.FAILED
		//		Whether the save succeeded, whether it is pending based on a UI
		//		dialog asking the user for permission, or whether it failed. 	
		//
		//		* isCoreSave - boolean
		//		If true, then this save was for a core piece of data necessary
		//		for the functioning of Dojo Offline. If false, then it is a
		//		piece of normal data being saved for offline access. Dojo
		//		Offline will 'fail fast' if some core piece of data could not
		//		be saved, automatically setting dojox.off.coreOpFailed to
		//		'true' and dojox.off.enabled to 'false'.
		//
		// 		* key - String
		//		The key that we are attempting to persist
		//
		// 		* value - Object
		//		The object we are trying to persist
		//
		// 		* namespace - String
		//		The Dojo Storage namespace we are saving this key/value pair
		//		into, such as "default", "Documents", "Contacts", etc.
		//		Optional.
		if(type == "save"){
			if(saveData.isCoreSave && (saveData.status == dojox.storage.FAILED)){
				dojox.off.coreOpFailed = true;
				dojox.off.enabled = false;
			
				// FIXME: Stop the background network thread
				dojox.off.onFrameworkEvent("coreOperationFailed");
			}
		}else if(type == "coreOperationFailed"){
			dojox.off.coreOpFailed = true;
			dojox.off.enabled = false;
			// FIXME: Stop the background network thread
		}
	},
	
	_checkOfflineCacheAvailable: function(callback){
		// is a true, offline cache running on this machine?
		this.hasOfflineCache = dojo.isGears;
		
		callback();
	},
	
	_onLoad: function(){
		//console.debug("dojox.off._onLoad");
		
		// both local storage and the page are finished loading
		
		// cache the Dojo JavaScript -- just use the default dojo.js
		// name for the most common scenario
		// FIXME: TEST: Make sure syncing doesn't break if dojo.js
		// can't be found, or report an error to developer
		dojox.off.files.cache(dojo.moduleUrl("dojo", "dojo.js"));
		
		// pull in the files needed by Dojo
		this._cacheDojoResources();
		
		// FIXME: need to pull in the firebug lite files here!
		// workaround or else we will get an error on page load
		// from Dojo that it can't find 'console.debug' for optimized builds
		// dojox.off.files.cache(djConfig.baseRelativePath + "src/debug.js");
		
		// make sure that resources needed by all of our underlying
		// Dojo Storage storage providers will be available
		// offline
		dojox.off.files.cache(dojox.storage.manager.getResourceList());
		
		// slurp the page if the end-developer wants that
		dojox.off.files._slurp();
		
		// see if we have an offline cache; when done, move
		// on to the rest of our startup tasks
		this._checkOfflineCacheAvailable(dojo.hitch(this, "_onOfflineCacheChecked"));
	},
	
	_onOfflineCacheChecked: function(){
		// this method is part of our _onLoad series of startup tasks
		
		// if we have an offline cache, see if we have been added to the 
		// list of available offline web apps yet
		if(this.hasOfflineCache && this.enabled){
			// load framework data; when we are finished, continue
			// initializing ourselves
			this._load(dojo.hitch(this, "_finishStartingUp"));
		}else if(this.hasOfflineCache && !this.enabled){
			// we have an offline cache, but it is disabled for some reason
			// perhaps due to the user denying a core operation
			this._finishStartingUp();
		}else{
			this._keepCheckingUntilInstalled();
		}
	},
	
	_keepCheckingUntilInstalled: function(){
		// this method is part of our _onLoad series of startup tasks
		
		// kick off a background interval that keeps
		// checking to see if an offline cache has been
		// installed since this page loaded
			
		// FIXME: Gears: See if we are installed somehow after the
		// page has been loaded
		
		// now continue starting up
		this._finishStartingUp();
	},
	
	_finishStartingUp: function(){
		//console.debug("dojox.off._finishStartingUp");
		
		// this method is part of our _onLoad series of startup tasks
		
		if(!this.hasOfflineCache){
			this.onLoad();
		}else if(this.enabled){
			// kick off a thread to check network status on
			// a regular basis
			this._startNetworkThread();

			// try to go online
			this.goOnline(dojo.hitch(this, function(){
				//console.debug("Finished trying to go online");
				// indicate we are ready to be used
				dojox.off.onLoad();
			}));
		}else{ // we are disabled or a core operation failed
			if(this.coreOpFailed){
				this.onFrameworkEvent("coreOperationFailed");
			}else{
				this.onLoad();
			}
		}
	},
	
	_onPageLoad: function(){
		//console.debug("dojox.off._onPageLoad");
		this._pageLoaded = true;
		
		if(this._storageLoaded && this._initializeCalled){
			this._onLoad();
		}
	},
	
	_onStorageLoad: function(){
		//console.debug("dojox.off._onStorageLoad");
		this._storageLoaded = true;
		
		// were we able to initialize storage? if
		// not, then this is a core operation, and
		// let's indicate we will need to fail fast
		if(!dojox.storage.manager.isAvailable()
			&& dojox.storage.manager.isInitialized()){
			this.coreOpFailed = true;
			this.enabled = false;
		}
		
		if(this._pageLoaded && this._initializeCalled){
			this._onLoad();		
		}
	},
	
	_isSiteAvailable: function(callback){
		// summary:
		//		Determines if our web application's website is available.
		// description:
		//		This method will asychronously determine if our web
		//		application's web site is available, which is a good proxy for
		//		network availability. The URL dojox.off.availabilityURL is
		//		used, which defaults to this site's domain name (ex:
		//		foobar.com). We check for dojox.off.AVAILABILITY_TIMEOUT (in
		//		seconds) and abort after that
		// callback: Function
		//		An optional callback function that will receive one argument:
		//		whether the site is available or not and is boolean. If this
		//		function is not present we call dojox.off.onNetwork instead if we
		//		are able to go online.
		dojo.xhrGet({
			url:		this._getAvailabilityURL(),
			handleAs:	"text",
			timeout:	this.NET_CHECK * 1000, 
			error:		dojo.hitch(this, function(err){
				//console.debug("dojox.off._isSiteAvailable.error: " + err);
				this.goingOnline = false;
				this.isOnline = false;
				if(callback){ callback(false); }
			}),
			load:		dojo.hitch(this, function(data){
				//console.debug("dojox.off._isSiteAvailable.load, data="+data);
				this.goingOnline = false;
				this.isOnline = true;
				
				if(callback){ callback(true);
				}else{ this.onNetwork("online"); }
			})
		});
	},
	
	_startNetworkThread: function(){
		//console.debug("startNetworkThread");
		
		// kick off a thread that does periodic
		// checks on the status of the network
		if(!this.doNetChecking){
			return;
		}
		
		window.setInterval(dojo.hitch(this, function(){	
			var d = dojo.xhrGet({
				url:	 	this._getAvailabilityURL(),
				handleAs:	"text",
				timeout: 	this.NET_CHECK * 1000,
				error:		dojo.hitch(this, 
								function(err){
									if(this.isOnline){
										this.isOnline = false;
										
										// FIXME: xhrGet() is not
										// correctly calling abort
										// on the XHR object when
										// it times out; fix inside
										// there instead of externally
										// here
										try{
											if(typeof d.ioArgs.xhr.abort == "function"){
												d.ioArgs.xhr.abort();
											}
										}catch(e){}
					
										// if things fell in the middle of syncing, 
										// stop syncing
										dojox.off.sync.isSyncing = false;
					
										this.onNetwork("offline");
									}
								}
							),
				load:		dojo.hitch(this, 
								function(data){
									if(!this.isOnline){
										this.isOnline = true;
										this.onNetwork("online");
									}
								}
							)
			});

		}), this.NET_CHECK * 1000);
	},
	
	_getAvailabilityURL: function(){
		var url = this.availabilityURL.toString();
		
		// bust the browser's cache to make sure we are really talking to
		// the server
		if(url.indexOf("?") == -1){
			url += "?";
		}else{
			url += "&";
		}
		url += "browserbust=" + new Date().getTime();
		
		return url;
	},
	
	_onOfflineCacheInstalled: function(){
		this.onFrameworkEvent("offlineCacheInstalled");
	},
	
	_cacheDojoResources: function(){
		// if we are a non-optimized build, then the core Dojo bootstrap
		// system was loaded as separate JavaScript files;
		// add these to our offline cache list. these are
		// loaded before the dojo.require() system exists
		
		// FIXME: create a better mechanism in the Dojo core to
		// expose whether you are dealing with an optimized build;
		// right now we just scan the SCRIPT tags attached to this
		// page and see if there is one for _base/_loader/bootstrap.js
		var isOptimizedBuild = true;
		dojo.forEach(dojo.query("script"), function(i){
			var src = i.getAttribute("src");
			if(!src){ return; }
			
			if(src.indexOf("_base/_loader/bootstrap.js") != -1){
				isOptimizedBuild = false;
			}
		});
		
		if(!isOptimizedBuild){
			dojox.off.files.cache(dojo.moduleUrl("dojo", "_base.js").uri);
			dojox.off.files.cache(dojo.moduleUrl("dojo", "_base/_loader/loader.js").uri);
			dojox.off.files.cache(dojo.moduleUrl("dojo", "_base/_loader/bootstrap.js").uri);
			
			// FIXME: pull in the host environment file in a more generic way
			// for other host environments
			dojox.off.files.cache(dojo.moduleUrl("dojo", "_base/_loader/hostenv_browser.js").uri);
		}
		
		// add anything that was brought in with a 
		// dojo.require() that resulted in a JavaScript
		// URL being fetched
		
		// FIXME: modify dojo/_base/_loader/loader.js to
		// expose a public API to get this information
	
		for(var i = 0; i < dojo._loadedUrls.length; i++){
			dojox.off.files.cache(dojo._loadedUrls[i]);
		}
		
		// FIXME: add the standard Dojo CSS file
	},
	
	_save: function(){
		// summary:
		//		Causes the Dojo Offline framework to save its configuration
		//		data into local storage.	
	},
	
	_load: function(callback){
		// summary:
		//		Causes the Dojo Offline framework to load its configuration
		//		data from local storage
		dojox.off.sync._load(callback);
	}
});


// wait until the storage system is finished loading
dojox.storage.manager.addOnLoad(dojo.hitch(dojox.off, "_onStorageLoad"));

// wait until the page is finished loading
dojo.addOnLoad(dojox.off, "_onPageLoad");

}
