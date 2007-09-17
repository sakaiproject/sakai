if(!dojo._hasResource["dojox.off.ui"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.off.ui"] = true;
dojo.provide("dojox.off.ui");

dojo.require("dojox.storage.Provider");
dojo.require("dojox.storage.manager");
dojo.require("dojox.storage.GearsStorageProvider");

// Author: Brad Neuberg, bkn3@columbia.edu, http://codinginparadise.org

// summary:
//	dojox.off.ui provides a standard,
//	default user-interface for a 
//	Dojo Offline Widget that can easily
//	be dropped into applications that would
//	like to work offline.
dojo.mixin(dojox.off.ui, {
	// appName: String
	//	This application's name, such as "Foobar". Note that
	//	this is a string, not HTML, so embedded markup will
	//	not work, including entities. Only the following
	//	characters are allowed: numbers, letters, and spaces.
	//	You must set this property.
	appName: "setme",
	
	// autoEmbed: boolean
	//	For advanced usage; most developers can ignore this.
	//	Whether to automatically auto-embed the default Dojo Offline
	//	widget into this page; default is true. 
	autoEmbed: true,
	
	// autoEmbedID: String
	//	For advanced usage; most developers can ignore this.
	//	The ID of the DOM element that will contain our
	//	Dojo Offline widget; defaults to the ID 'dot-widget'.
	autoEmbedID: "dot-widget",
	
	// runLink: String
	//	For advanced usage; most developers can ignore this.
	//	The URL that should be navigated to to run this 
	//	application offline; this will be placed inside of a
	//	link that the user can drag to their desktop and double
	//	click. Note that this URL must exactly match the URL
	//	of the main page of our resource that is offline for
	//	it to be retrieved from the offline cache correctly.
	//	For example, if you have cached your main page as
	//	http://foobar.com/index.html, and you set this to
	//	http://www.foobar.com/index.html, the run link will
	//	not work. By default this value is automatically set to 
	//	the URL of this page, so it does not need to be set
	//	manually unless you have unusual needs.
	runLink: window.location.href,
	
	// runLinkTitle: String
	//	For advanced usage; most developers can ignore this.
	//	The text that will be inside of the link that a user
	//	can drag to their desktop to run this application offline.
	//	By default this is automatically set to "Run " plus your
	//	application's name.
	runLinkTitle: "Run Application",
	
	// learnHowPath: String
	//	For advanced usage; most developers can ignore this.
	//	The path to a web page that has information on 
	//	how to use this web app offline; defaults to
	//	src/off/ui-template/learnhow.html, relative to
	//	your Dojo installation. Make sure to set
	//	dojo.to.ui.customLearnHowPath to true if you want
	//	a custom Learn How page.
	learnHowPath: dojo.moduleUrl("dojox", "off/resources/learnhow.html"),
	
	// customLearnHowPath: boolean
	//	For advanced usage; most developers can ignore this.
	//	Whether the developer is using their own custom page
	//	for the Learn How instructional page; defaults to false.
	//	Use in conjunction with dojox.off.ui.learnHowPath.
	customLearnHowPath: false,
	
	htmlTemplatePath: dojo.moduleUrl("dojox", "off/resources/offline-widget.html").uri,
	cssTemplatePath: dojo.moduleUrl("dojox", "off/resources/offline-widget.css").uri,
	onlineImagePath: dojo.moduleUrl("dojox", "off/resources/greenball.png").uri,
	offlineImagePath: dojo.moduleUrl("dojox", "off/resources/redball.png").uri,
	rollerImagePath: dojo.moduleUrl("dojox", "off/resources/roller.gif").uri,
	checkmarkImagePath: dojo.moduleUrl("dojox", "off/resources/checkmark.png").uri,
	learnHowJSPath: dojo.moduleUrl("dojox", "off/resources/learnhow.js").uri,
	
	_initialized: false,
	
	onLoad: function(){
		// summary:
		//	A function that should be connected to allow your
		//	application to know when Dojo Offline, the page, and
		//	the Offline Widget are all initialized and ready to be
		//	used:
		//
		//		dojo.connect(dojox.off.ui, "onLoad", someFunc)
	},

	_initialize: function(){
		//console.debug("dojox.off.ui._initialize");
		
		// make sure our app name is correct
		if(this._validateAppName(this.appName) == false){
			alert("You must set dojox.off.ui.appName; it can only contain "
					+ "letters, numbers, and spaces; right now it "
					+ "is incorrectly set to '" + dojox.off.ui.appName + "'");
			dojox.off.enabled = false;
			return;
		}
		
		// set our run link text to its default
		this.runLinkText = "Run " + this.appName;
		
		// setup our event listeners for Dojo Offline events
		// to update our UI
		dojo.connect(dojox.off, "onNetwork", this, "_onNetwork");
		dojo.connect(dojox.off.sync, "onSync", this, "_onSync");
		
		// cache our default UI resources
		dojox.off.files.cache([
							this.htmlTemplatePath,
							this.cssTemplatePath,
							this.onlineImagePath,
							this.offlineImagePath,
							this.rollerImagePath,
							this.checkmarkImagePath
							]);
		
		// embed the offline widget UI
		if(this.autoEmbed){
			this._doAutoEmbed();
		}
	},
	
	_doAutoEmbed: function(){
		// fetch our HTML for the offline widget

		// dispatch the request
		dojo.xhrGet({
			url:	 this.htmlTemplatePath,
			handleAs:	"text",
			error:		function(err){
				dojox.off.enabled = false;
				err = err.message||err;
				alert("Error loading the Dojo Offline Widget from "
						+ this.htmlTemplatePath + ": " + err);
			},
			load:		dojo.hitch(this, this._templateLoaded)	 
		});
	},
	
	_templateLoaded: function(data){
		//console.debug("dojox.off.ui._templateLoaded");
		// inline our HTML
		var container = dojo.byId(this.autoEmbedID);
		if(container){ container.innerHTML = data; }
		
		// fill out our image paths
		this._initImages();
		
		// update our network indicator status ball
		this._updateNetIndicator();
		
		// update our 'Learn How' text
		this._initLearnHow();
		
		this._initialized = true;
		
		// check offline cache settings
		if(!dojox.off.hasOfflineCache){
			this._showNeedsOfflineCache();
			return;
		}
		
		// check to see if we need a browser restart
		// to be able to use this web app offline
		if(dojox.off.hasOfflineCache && dojox.off.browserRestart){
			this._needsBrowserRestart();
			return;
		}else{
			var browserRestart = dojo.byId("dot-widget-browser-restart");
			if(browserRestart){ browserRestart.style.display = "none"; }
		}
		
		// update our sync UI
		this._updateSyncUI();
		
		// register our event listeners for our main buttons
		this._initMainEvtHandlers();
		
		// if offline functionality is disabled, disable everything
		this._setOfflineEnabled(dojox.off.enabled);
		
		// update our UI based on the state of the network
		this._onNetwork(dojox.off.isOnline ? "online" : "offline");
		
		// try to go online
		this._testNet();
	},
	
	_testNet: function(){
		dojox.off.goOnline(dojo.hitch(this, function(isOnline){
			//console.debug("testNet callback, isOnline="+isOnline);
			
			// display our online/offline results
			this._onNetwork(isOnline ? "online" : "offline");
			
			// indicate that our default UI 
			// and Dojo Offline are now ready to
			// be used
			this.onLoad();
		}));
	},
	
	_updateNetIndicator: function(){
		var onlineImg = dojo.byId("dot-widget-network-indicator-online");
		var offlineImg = dojo.byId("dot-widget-network-indicator-offline");
		var titleText = dojo.byId("dot-widget-title-text");
		
		if(onlineImg && offlineImg){
			if(dojox.off.isOnline == true){
				onlineImg.style.display = "inline";
				offlineImg.style.display = "none";
			}else{
				onlineImg.style.display = "none";
				offlineImg.style.display = "inline";
			}
		}
		
		if(titleText){
			if(dojox.off.isOnline){
				titleText.innerHTML = "Online";
			}else{
				titleText.innerHTML = "Offline";
			}
		}
	},
	
	_initLearnHow: function(){
		var learnHow = dojo.byId("dot-widget-learn-how-link");
		
		if(!learnHow){ return; }
		
		if(!this.customLearnHowPath){
			// add parameters to URL so the Learn How page
			// can customize itself and display itself
			// correctly based on framework settings
			var dojoPath = djConfig.baseRelativePath;
			this.learnHowPath += "?appName=" + encodeURIComponent(this.appName)
									+ "&hasOfflineCache=" + dojox.off.hasOfflineCache
									+ "&runLink=" + encodeURIComponent(this.runLink)
									+ "&runLinkText=" + encodeURIComponent(this.runLinkText)
									+ "&baseRelativePath=" + encodeURIComponent(dojoPath);
			
			// cache our Learn How JavaScript page and
			// the HTML version with full query parameters
			// so it is available offline without a cache miss					
			dojox.off.files.cache(this.learnHowJSPath);
			dojox.off.files.cache(this.learnHowPath);
		}
		
		learnHow.setAttribute("href", this.learnHowPath);
		
		var appName = dojo.byId("dot-widget-learn-how-app-name");
		
		if(!appName){ return; }
		
		appName.innerHTML = "";
		appName.appendChild(document.createTextNode(this.appName));
	},
	
	_validateAppName: function(appName){
		if(!appName){ return false; }
		
		return (/^[a-z0-9 ]*$/i.test(appName));
	},
	
	_updateSyncUI: function(){
		var roller = dojo.byId("dot-roller");
		var checkmark = dojo.byId("dot-success-checkmark");
		var syncMessages = dojo.byId("dot-sync-messages");
		var details = dojo.byId("dot-sync-details");
		var cancel = dojo.byId("dot-sync-cancel");
		
		if(dojox.off.sync.isSyncing){
			this._clearSyncMessage();
			
			if(roller){ roller.style.display = "inline"; }
			
			if(checkmark){ checkmark.style.display = "none"; }
			
			if(syncMessages){
				dojo.removeClass(syncMessages, "dot-sync-error");
			}
			
			if(details){ details.style.display = "none"; }
			
			if(cancel){ cancel.style.display = "inline"; }
		}else{	
			if(roller){ roller.style.display = "none"; }
			
			if(cancel){ cancel.style.display = "none"; }
			
			if(syncMessages){
				dojo.removeClass(syncMessages, "dot-sync-error");
			}
		}
	},
	
	_setSyncMessage: function(message){
		var syncMessage = dojo.byId("dot-sync-messages");
		if(syncMessage){
			// when used with Google Gears pre-release in Firefox/Mac OS X,
			// the browser would crash when testing in Moxie
			// if we set the message this way for some reason.
			// Brad Neuberg, bkn3@columbia.edu
			//syncMessage.innerHTML = message;
			
			while(syncMessage.firstChild){
				syncMessage.removeChild(syncMessage.firstChild);
			}
			syncMessage.appendChild(document.createTextNode(message));
		}
	},
	
	_clearSyncMessage: function(){
		this._setSyncMessage("");
	},
	
	_initImages: function(){	
		var onlineImg = dojo.byId("dot-widget-network-indicator-online");
		if(onlineImg){
			onlineImg.setAttribute("src", this.onlineImagePath);
		}
		
		var offlineImg = dojo.byId("dot-widget-network-indicator-offline");
		if(offlineImg){
			offlineImg.setAttribute("src", this.offlineImagePath);
		}
		
		var roller = dojo.byId("dot-roller");
		if(roller){
			roller.setAttribute("src", this.rollerImagePath);
		}
		
		var checkmark = dojo.byId("dot-success-checkmark");
		if(checkmark){
			checkmark.setAttribute("src", this.checkmarkImagePath);
		}
	},
	
	_showDetails: function(evt){
		// cancel the button's default behavior
		evt.preventDefault();
		evt.stopPropagation();
		
		if(!dojox.off.sync.details.length){
			return;
		}
		
		// determine our HTML message to display
		var html = "";
		html += "<html><head><title>Sync Details</title><head><body>";
		html += "<h1>Sync Details</h1>\n";
		html += "<ul>\n";
		for(var i = 0; i < dojox.off.sync.details.length; i++){
			html += "<li>";
			html += dojox.off.sync.details[i];
			html += "</li>";	
		}
		html += "</ul>\n";
		html += "<a href='javascript:window.close()' "
				 + "style='text-align: right; padding-right: 2em;'>"
				 + "Close Window"
				 + "</a>\n";
		html += "</body></html>";
		
		// open a popup window with this message
		var windowParams = "height=400,width=600,resizable=true,"
							+ "scrollbars=true,toolbar=no,menubar=no,"
							+ "location=no,directories=no,dependent=yes";

		var popup = window.open("", "SyncDetails", windowParams);
		
		if(!popup){ // aggressive popup blocker
			alert("Please allow popup windows for this domain; can't display sync details window");
			return;
		}
		
		popup.document.open();
		popup.document.write(html);
		popup.document.close();
		
		// put the focus on the popup window
		if(popup.focus){
			popup.focus();
		}
	},
	
	_cancel: function(evt){
		// cancel the button's default behavior
		evt.preventDefault();
		evt.stopPropagation();
		
		dojox.off.sync.cancel();
	},
	
	_needsBrowserRestart: function(){
		var browserRestart = dojo.byId("dot-widget-browser-restart");
		if(browserRestart){
			dojo.addClass(browserRestart, "dot-needs-browser-restart");
		}
		
		var appName = dojo.byId("dot-widget-browser-restart-app-name");
		if(appName){
			appName.innerHTML = "";
			appName.appendChild(document.createTextNode(this.appName));
		}
		
		var status = dojo.byId("dot-sync-status");
		if(status){
			status.style.display = "none";
		}
	},
	
	_showNeedsOfflineCache: function(){
		var widgetContainer = dojo.byId("dot-widget-container");
		if(widgetContainer){
			dojo.addClass(widgetContainer, "dot-needs-offline-cache");
		}
	},
	
	_hideNeedsOfflineCache: function(){
		var widgetContainer = dojo.byId("dot-widget-container");
		if(widgetContainer){
			dojo.removeClass(widgetContainer, "dot-needs-offline-cache");
		}
	},
	
	_initMainEvtHandlers: function(){
		var detailsButton = dojo.byId("dot-sync-details-button");
		if(detailsButton){
			dojo.connect(detailsButton, "onclick", this, this._showDetails);
		}
		var cancelButton = dojo.byId("dot-sync-cancel-button");
		if(cancelButton){
			dojo.connect(cancelButton, "onclick", this, this._cancel);
		}
	},
	
	_setOfflineEnabled: function(enabled){
		var elems = [];
		elems.push(dojo.byId("dot-sync-status"));
		
		for(var i = 0; i < elems.length; i++){
			if(elems[i]){
				elems[i].style.visibility = 
							(enabled ? "visible" : "hidden");
			}
		}
	},
	
	_syncFinished: function(){
		this._updateSyncUI();
		
		var checkmark = dojo.byId("dot-success-checkmark");
		var details = dojo.byId("dot-sync-details");
		
		if(dojox.off.sync.successful == true){
			this._setSyncMessage("Sync Successful");
			if(checkmark){ checkmark.style.display = "inline"; }
		}else if(dojox.off.sync.cancelled == true){
			this._setSyncMessage("Sync Cancelled");
			
			if(checkmark){ checkmark.style.display = "none"; }
		}else{
			this._setSyncMessage("Sync Error");
			
			var messages = dojo.byId("dot-sync-messages");
			if(messages){
				dojo.addClass(messages, "dot-sync-error");
			}
			
			if(checkmark){ checkmark.style.display = "none"; }
		}
		
		if(dojox.off.sync.details.length && details){
			details.style.display = "inline";
		}
	},
	
	_onFrameworkEvent: function(type, saveData){
		if(type == "save"){
			if(saveData.status == dojox.storage.FAILED && !saveData.isCoreSave){
				alert("Please increase the amount of local storage available "
						+ "to this application");
				if(dojox.storage.hasSettingsUI()){
					dojox.storage.showSettingsUI();
				}		
			
				// FIXME: Be able to know if storage size has changed
				// due to user configuration
			}
		}else if(type == "coreOperationFailed"){
			console.log("Application does not have permission to use Dojo Offline");
		
			if(!this._userInformed){
				alert("This application will not work if Google Gears is not allowed to run");
				this._userInformed = true;
			}
		}else if(type == "offlineCacheInstalled"){
			// clear out the 'needs offline cache' info
			this._hideNeedsOfflineCache();
		
			// check to see if we need a browser restart
			// to be able to use this web app offline
			if(dojox.off.hasOfflineCache == true
				&& dojox.off.browserRestart == true){
				this._needsBrowserRestart();
				return;
			}else{
				var browserRestart = dojo.byId("dot-widget-browser-restart");
				if(browserRestart){
					browserRestart.style.display = "none";
				}
			}
		
			// update our sync UI
			this._updateSyncUI();
		
			// register our event listeners for our main buttons
			this._initMainEvtHandlers();
		
			// if offline is disabled, disable everything
			this._setOfflineEnabled(dojox.off.enabled);
		
			// try to go online
			this._testNet();
		}
	},
	
	_onSync: function(type){
		//console.debug("ui, onSync="+type);
		switch(type){
			case "start": 
				this._updateSyncUI();
				break;
				
			case "refreshFiles":
				this._setSyncMessage("Downloading UI...");
				break;
				
			case "upload":
				this._setSyncMessage("Uploading new data...");
				break;
				
			case "download":
				this._setSyncMessage("Downloading new data...");
				break;
				
			case "finished":
				this._syncFinished();
				break;
				
			case "cancel":
				this._setSyncMessage("Canceling Sync...");
				break;
				
			default:
				dojo.warn("Programming error: "
							+ "Unknown sync type in dojox.off.ui: " + type);
				break;
		}
	},
	
	_onNetwork: function(type){
		// summary:
		//	Called when we go on- or off-line
		// description:
		//	When we go online or offline, this method is called to update
		//	our UI. Default behavior is to update the Offline
		//	Widget UI and to attempt a synchronization.
		// type: String
		//	"online" if we just moved online, and "offline" if we just
		//	moved offline.
		
		if(!this._initialized){ return; }
		
		// update UI
		this._updateNetIndicator();
		
		if(type == "offline"){
			this._setSyncMessage("You are working offline");
		
			// clear old details
			var details = dojo.byId("dot-sync-details");
			if(details){ details.style.display = "none"; }
			
			// if we fell offline during a sync, hide
			// the sync info
			this._updateSyncUI();
		}else{ // online
			// synchronize, but pause for a few seconds
			// so that the user can orient themselves
			if(dojox.off.sync.autoSync){
				window.setTimeout("dojox.off.sync.synchronize()", 1000);
			}
		}
	}
});

// register ourselves for low-level framework events
dojo.connect(dojox.off, "onFrameworkEvent", dojox.off.ui, "_onFrameworkEvent");

// start our magic when the Dojo Offline framework is ready to go
dojo.connect(dojox.off, "onLoad", dojox.off.ui, dojox.off.ui._initialize);

}
