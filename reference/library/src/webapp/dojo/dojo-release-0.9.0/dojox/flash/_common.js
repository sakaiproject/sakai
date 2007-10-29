if(!dojo._hasResource["dojox.flash._common"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.flash._common"] = true;
dojo.provide("dojox.flash._common");

dojox.flash = function(){
	// summary:
	//	The goal of dojox.flash is to make it easy to extend Flash's capabilities
	//	into an AJAX/DHTML environment.
	// description:  
	//	The goal of dojox.flash is to make it easy to extend Flash's capabilities
	//	into an AJAX/DHTML environment. Robust, performant, reliable 
	//	JavaScript/Flash communication is harder than most realize when they
	//	delve into the topic, especially if you want it
	//	to work on Internet Explorer, Firefox, and Safari, and to be able to
	//	push around hundreds of K of information quickly. dojox.flash makes it
	//	possible to support these platforms; you have to jump through a few
	//	hoops to get its capabilites, but if you are a library writer 
	//	who wants to bring Flash's storage or streaming sockets ability into
	//	DHTML, for example, then dojox.flash is perfect for you.
	//  
	//	dojox.flash provides an easy object for interacting with the Flash plugin. 
	//	This object provides methods to determine the current version of the Flash
	//	plugin (dojox.flash.info); execute Flash instance methods 
	//	independent of the Flash version
	//	being used (dojox.flash.comm); write out the necessary markup to 
	//	dynamically insert a Flash object into the page (dojox.flash.Embed; and 
	//	do dynamic installation and upgrading of the current Flash plugin in 
	//	use (dojox.flash.Install).
	//		
	//	To use dojox.flash, you must first wait until Flash is finished loading 
	//	and initializing before you attempt communication or interaction. 
	//	To know when Flash is finished use dojo.connect:
	//		
	//	dojo.connect(dojox.flash, "loaded", myInstance, "myCallback");
	//		
	//	Then, while the page is still loading provide the file name
	//	and the major version of Flash that will be used for Flash/JavaScript
	//	communication (see "Flash Communication" below for information on the 
	//	different kinds of Flash/JavaScript communication supported and how they 
	//	depend on the version of Flash installed):
	//		
	//	dojox.flash.setSwf({flash6: dojo.moduleUrl("dojox", "_storage/storage_flash6.swf"),
	//						flash8: dojo.moduleUrl("dojox", "_storage/storage_flash8.swf")});
	//		
	//	This will cause dojox.flash to pick the best way of communicating
	//	between Flash and JavaScript based on the platform.
	//		
	//	If no SWF files are specified, then Flash is not initialized.
	//		
	//	Your Flash must use DojoExternalInterface to expose Flash methods and
	//	to call JavaScript; see "Flash Communication" below for details.
	//		
	//	setSwf can take an optional 'visible' attribute to control whether
	//	the Flash object is visible or not on the page; the default is visible:
	//		
	//	dojox.flash.setSwf({flash6: dojo.moduleUrl("dojox", "_storage/storage_flash6.swf"),
	//						flash8: dojo.moduleUrl("dojox", "_storage/storage_flash8.swf"),
	//						visible: false });
	//		
	//	Once finished, you can query Flash version information:
	//		
	//	dojox.flash.info.version
	//		
	//	Or can communicate with Flash methods that were exposed:
	//		
	//	var results = dojox.flash.comm.sayHello("Some Message");
	//		
	//	Only string values are currently supported for both arguments and
	//	for return results. Everything will be cast to a string on both
	//	the JavaScript and Flash sides.
	//		
	//	-------------------
	//	Flash Communication
	//	-------------------
	//		
	//	dojox.flash allows Flash/JavaScript communication in 
	//	a way that can pass large amounts of data back and forth reliably and
	//	very fast. The dojox.flash
	//	framework encapsulates the specific way in which this communication occurs,
	//	presenting a common interface to JavaScript irrespective of the underlying
	//	Flash version.
	//		
	//	There are currently three major ways to do Flash/JavaScript communication
	//	in the Flash community:
	//		
	//	1) Flash 6+ - Uses Flash methods, such as SetVariable and TCallLabel,
	//	and the fscommand handler to do communication. Strengths: Very fast,
	//	mature, and can send extremely large amounts of data; can do
	//	synchronous method calls. Problems: Does not work on Safari; works on 
	//	Firefox/Mac OS X only if Flash 8 plugin is installed; cryptic to work with.
	//		
	//	2) Flash 8+ - Uses ExternalInterface, which provides a way for Flash
	//	methods to register themselves for callbacks from JavaScript, and a way
	//	for Flash to call JavaScript. Strengths: Works on Safari; elegant to
	//	work with; can do synchronous method calls. Problems: Extremely buggy 
	//	(fails if there are new lines in the data, for example); performance
	//	degrades drastically in O(n^2) time as data grows; locks up the browser while
	//	it is communicating; does not work in Internet Explorer if Flash
	//	object is dynamically added to page with document.writeln, DOM methods,
	//	or innerHTML.
	//		
	//	3) Flash 6+ - Uses two seperate Flash applets, one that we 
	//	create over and over, passing input data into it using the PARAM tag, 
	//	which then uses a Flash LocalConnection to pass the data to the main Flash
	//	applet; communication back to Flash is accomplished using a getURL
	//	call with a javascript protocol handler, such as "javascript:myMethod()".
	//	Strengths: the most cross browser, cross platform pre-Flash 8 method
	//	of Flash communication known; works on Safari. Problems: Timing issues;
	//	clunky and complicated; slow; can only send very small amounts of
	//	data (several K); all method calls are asynchronous.
	//		
	//	dojox.flash.comm uses only the first two methods. This framework
	//	was created primarily for dojox.storage, which needs to pass very large
	//	amounts of data synchronously and reliably across the Flash/JavaScript
	//	boundary. We use the first method, the Flash 6 method, on all platforms
	//	that support it, while using the Flash 8 ExternalInterface method
	//	only on Safari with some special code to help correct ExternalInterface's
	//	bugs.
	//		
	//	Since dojox.flash needs to have two versions of the Flash
	//	file it wants to generate, a Flash 6 and a Flash 8 version to gain
	//	true cross-browser compatibility, several tools are provided to ease
	//	development on the Flash side.
	//		
	//	In your Flash file, if you want to expose Flash methods that can be
	//	called, use the DojoExternalInterface class to register methods. This
	//	class is an exact API clone of the standard ExternalInterface class, but
	//	can work in Flash 6+ browsers. Under the covers it uses the best
	//	mechanism to do communication:
	//		
	//	class HelloWorld{
	//		function HelloWorld(){
	//			// Initialize the DojoExternalInterface class
	//			DojoExternalInterface.initialize();
	//			
	//			// Expose your methods
	//			DojoExternalInterface.addCallback("sayHello", this, this.sayHello);
	//				
	//			// Tell JavaScript that you are ready to have method calls
	//			DojoExternalInterface.loaded();
	//				
	//			// Call some JavaScript
	//			var resultsReady = function(results){
	//				trace("Received the following results from JavaScript: " + results);
	//			}
	//			DojoExternalInterface.call("someJavaScriptMethod", resultsReady, 
	//																	 someParameter);
	//		}
	//			
	//		function sayHello(){ ... }
	//			
	//		static main(){ ... }
	//	}
	//		
	//	DojoExternalInterface adds two new functions to the ExternalInterface
	//	API: initialize() and loaded(). initialize() must be called before
	//	any addCallback() or call() methods are run, and loaded() must be
	//	called after you are finished adding your callbacks. Calling loaded()
	//	will fire the dojox.flash.loaded() event, so that JavaScript can know that
	//	Flash has finished loading and adding its callbacks, and can begin to
	//	interact with the Flash file.
	//		
	//	To generate your SWF files, use the ant task
	//	"buildFlash". You must have the open source Motion Twin ActionScript 
	//	compiler (mtasc) installed and in your path to use the "buildFlash"
	//	ant task; download and install mtasc from http://www.mtasc.org/.
	//		
	//		
	//		
	//	buildFlash usage:
	//		
	//	// FIXME: this is not correct in the 0.9 world!
	//	ant buildFlash -Ddojox.flash.file=../tests/flash/HelloWorld.as
	//		
	//	where "dojox.flash.file" is the relative path to your Flash 
	//	ActionScript file.
	//		
	//	This will generate two SWF files, one ending in _flash6.swf and the other
	//	ending in _flash8.swf in the same directory as your ActionScript method:
	//		
	//	HelloWorld_flash6.swf
	//	HelloWorld_flash8.swf
	//		
	//	Initialize dojox.flash with the filename and Flash communication version to
	//	use during page load; see the documentation for dojox.flash for details:
	//		
	//	dojox.flash.setSwf({flash6: dojo.moduleUrl("dojox", "flash/tests/flash/HelloWorld_flash6.swf"),
	//					 	flash8: dojo.moduleUrl("dojox", "flash/tests/flash/HelloWorld_flash8.swf")});
	//		
	//	Now, your Flash methods can be called from JavaScript as if they are native
	//	Flash methods, mirrored exactly on the JavaScript side:
	//		
	//	dojox.flash.comm.sayHello();
	//		
	//	Only Strings are supported being passed back and forth currently.
	//		
	//	JavaScript to Flash communication is synchronous; i.e., results are returned
	//	directly from the method call:
	//		
	//	var results = dojox.flash.comm.sayHello();
	//		
	//	Flash to JavaScript communication is asynchronous due to limitations in
	//	the underlying technologies; you must use a results callback to handle
	//	results returned by JavaScript in your Flash AS files:
	//		
	//	var resultsReady = function(results){
	//		trace("Received the following results from JavaScript: " + results);
	//	}
	//	DojoExternalInterface.call("someJavaScriptMethod", resultsReady);
	//		
	//		
	//		
	//	-------------------
	//	Notes
	//	-------------------
	//		
	//	If you have both Flash 6 and Flash 8 versions of your file:
	//		
	//	dojox.flash.setSwf({flash6: dojo.moduleUrl("dojox", "flash/tests/flash/HelloWorld_flash6.swf"),
	//					 	flash8: dojo.moduleUrl("dojox", "flash/tests/flash/HelloWorld_flash8.swf")});
	//											 
	//	but want to force the browser to use a certain version of Flash for
	//	all platforms (for testing, for example), use the djConfig
	//	variable 'forceFlashComm' with the version number to force:
	//		
	//	var djConfig = { forceFlashComm: 6 };
	//		
	//	Two values are currently supported, 6 and 8, for the two styles of
	//	communication described above. Just because you force dojox.flash
	//	to use a particular communication style is no guarantee that it will
	//	work; for example, Flash 8 communication doesn't work in Internet
	//	Explorer due to bugs in Flash, and Flash 6 communication does not work
	//	in Safari. It is best to let dojox.flash determine the best communication
	//	mechanism, and to use the value above only for debugging the dojox.flash
	//	framework itself.
	//		
	//	Also note that dojox.flash can currently only work with one Flash object
	//	on the page; it and the API do not yet support multiple Flash objects on
	//	the same page.
	//		
	//	We use some special tricks to get decent, linear performance
	//	out of Flash 8's ExternalInterface on Safari; see the blog
	//	post 
	//	http://codinginparadise.org/weblog/2006/02/how-to-speed-up-flash-8s.html
	//	for details.
	//		
	//	Your code can detect whether the Flash player is installing or having
	//	its version revved in two ways. First, if dojox.flash detects that
	//	Flash installation needs to occur, it sets dojox.flash.info.installing
	//	to true. Second, you can detect if installation is necessary with the
	//	following callback:
	//		
	//	dojo.connect(dojox.flash, "installing", myInstance, "myCallback");
	//		
	//	You can use this callback to delay further actions that might need Flash;
	//	when installation is finished the full page will be refreshed and the
	//	user will be placed back on your page with Flash installed.
	//		
	//	-------------------
	//	Todo/Known Issues
	//	-------------------
	//
	//	There are several tasks I was not able to do, or did not need to fix
	//	to get dojo.storage out:		
	//		
	//	* When using Flash 8 communication, Flash method calls to JavaScript
	//	are not working properly; serialization might also be broken for certain
	//	invalid characters when it is Flash invoking JavaScript methods.
	//	The Flash side needs to have more sophisticated serialization/
	//	deserialization mechanisms like JavaScript currently has. The
	//	test_flash2.html unit tests should also be updated to have much more
	//	sophisticated Flash to JavaScript unit tests, including large
	//	amounts of data.
	//		
	//	* On Internet Explorer, after doing a basic install, the page is
	//	not refreshed or does not detect that Flash is now available. The way
	//	to fix this is to create a custom small Flash file that is pointed to
	//	during installation; when it is finished loading, it does a callback
	//	that says that Flash installation is complete on IE, and we can proceed
	//	to initialize the dojox.flash subsystem.
	//		
	//	Author- Brad Neuberg, bkn3@columbia.edu
}

dojox.flash = {
	flash6_version: null,
	flash8_version: null,
	ready: false,
	_visible: true,
	_loadedListeners: new Array(),
	_installingListeners: new Array(),
	
	setSwf: function(/* Object */ fileInfo){
		// summary: Sets the SWF files and versions we are using.
		// fileInfo: Object
		//		An object that contains two attributes, 'flash6' and 'flash8',
		//		each of which contains the path to our Flash 6 and Flash 8
		//		versions of the file we want to script.
		//	example:
		//		var swfloc6 = dojo.moduleUrl("dojox.storage", "Storage_version6.swf").toString();
		//		var swfloc8 = dojo.moduleUrl("dojox.storage", "Storage_version8.swf").toString();
		//		dojox.flash.setSwf({flash6: swfloc6, flash8: swfloc8, visible: false}); 	
		
		if(!fileInfo){
			return;
		}
		
		if(fileInfo["flash6"]){
			this.flash6_version = fileInfo.flash6;
		}
		
		if(fileInfo["flash8"]){
			this.flash8_version = fileInfo.flash8;
		}
		
		if(fileInfo["visible"]){
			this._visible = fileInfo.visible;
		}
		
		// initialize ourselves		
		this._initialize();
	},
	
	useFlash6: function(){ /* Boolean */
		// summary: Returns whether we are using Flash 6 for communication on this platform.
		
		if(this.flash6_version == null){
			return false;
		}else if (this.flash6_version != null && dojox.flash.info.commVersion == 6){
			// if we have a flash 6 version of this SWF, and this browser supports 
			// communicating using Flash 6 features...
			return true;
		}else{
			return false;
		}
	},
	
	useFlash8: function(){ /* Boolean */
		// summary: Returns whether we are using Flash 8 for communication on this platform.
		
		if(this.flash8_version == null){
			return false;
		}else if (this.flash8_version != null && dojox.flash.info.commVersion == 8){
			// if we have a flash 8 version of this SWF, and this browser supports
			// communicating using Flash 8 features...
			return true;
		}else{
			return false;
		}
	},
	
	addLoadedListener: function(/* Function */ listener){
		// summary:
		//	Adds a listener to know when Flash is finished loading. 
		//	Useful if you don't want a dependency on dojo.event.
		// listener: Function
		//	A function that will be called when Flash is done loading.
		
		this._loadedListeners.push(listener);
	},

	addInstallingListener: function(/* Function */ listener){
		// summary:
		//	Adds a listener to know if Flash is being installed. 
		//	Useful if you don't want a dependency on dojo.event.
		// listener: Function
		//	A function that will be called if Flash is being
		//	installed
		
		this._installingListeners.push(listener);
	},	
	
	loaded: function(){
		// summary: Called back when the Flash subsystem is finished loading.
		// description:
		//	A callback when the Flash subsystem is finished loading and can be
		//	worked with. To be notified when Flash is finished loading, connect
		//	your callback to this method using the following:
		//	
		//	dojo.event.connect(dojox.flash, "loaded", myInstance, "myCallback");
		
		//dojo.debug("dojox.flash.loaded");
		dojox.flash.ready = true;
		if(dojox.flash._loadedListeners.length > 0){
			for(var i = 0;i < dojox.flash._loadedListeners.length; i++){
				dojox.flash._loadedListeners[i].call(null);
			}
		}
	},
	
	installing: function(){
		// summary: Called if Flash is being installed.
		// description:
		//	A callback to know if Flash is currently being installed or
		//	having its version revved. To be notified if Flash is installing, connect
		//	your callback to this method using the following:
		//	
		//	dojo.event.connect(dojox.flash, "installing", myInstance, "myCallback");
		 
		//dojo.debug("installing");
		if(dojox.flash._installingListeners.length > 0){
			for(var i = 0; i < dojox.flash._installingListeners.length; i++){
				dojox.flash._installingListeners[i].call(null);
			}
		}
	},
	
	// Initializes dojox.flash.
	_initialize: function(){
		//dojo.debug("dojox.flash._initialize");
		// see if we need to rev or install Flash on this platform
		var installer = new dojox.flash.Install();
		dojox.flash.installer = installer;

		if(installer.needed() == true){		
			installer.install();
		}else{
			//dojo.debug("Writing object out");
			// write the flash object into the page
			dojox.flash.obj = new dojox.flash.Embed(this._visible);
			dojox.flash.obj.write(dojox.flash.info.commVersion);
			
			// initialize the way we do Flash/JavaScript communication
			dojox.flash.comm = new dojox.flash.Communicator();
		}
	}
};


dojox.flash.Info = function(){
	// summary: A class that helps us determine whether Flash is available.
	// description:
	//	A class that helps us determine whether Flash is available,
	//	it's major and minor versions, and what Flash version features should
	//	be used for Flash/JavaScript communication. Parts of this code
	//	are adapted from the automatic Flash plugin detection code autogenerated 
	//	by the Macromedia Flash 8 authoring environment. 
	//	
	//	An instance of this class can be accessed on dojox.flash.info after
	//	the page is finished loading.
	//	
	//	This constructor must be called before the page is finished loading.	
	
	// Visual basic helper required to detect Flash Player ActiveX control 
	// version information on Internet Explorer
	if(dojo.isIE){
		document.write([
			'<script language="VBScript" type="text/vbscript"\>',
			'Function VBGetSwfVer(i)',
			'  on error resume next',
			'  Dim swControl, swVersion',
			'  swVersion = 0',
			'  set swControl = CreateObject("ShockwaveFlash.ShockwaveFlash." + CStr(i))',
			'  if (IsObject(swControl)) then',
			'    swVersion = swControl.GetVariable("$version")',
			'  end if',
			'  VBGetSwfVer = swVersion',
			'End Function',
			'</script\>'].join("\r\n"));
	}
	
	this._detectVersion();
	this._detectCommunicationVersion();
}

dojox.flash.Info.prototype = {
	// version: String
	//		The full version string, such as "8r22".
	version: -1,
	
	// versionMajor, versionMinor, versionRevision: String
	//		The major, minor, and revisions of the plugin. For example, if the
	//		plugin is 8r22, then the major version is 8, the minor version is 0,
	//		and the revision is 22. 
	versionMajor: -1,
	versionMinor: -1,
	versionRevision: -1,
	
	// capable: Boolean
	//		Whether this platform has Flash already installed.
	capable: false,
	
	// commVersion: int
	//		The major version number for how our Flash and JavaScript communicate.
	//		This can currently be the following values:
	//		6 - We use a combination of the Flash plugin methods, such as SetVariable
	//		and TCallLabel, along with fscommands, to do communication.
	//		8 - We use the ExternalInterface API. 
	//		-1 - For some reason neither method is supported, and no communication
	//		is possible. 
	commVersion: 6,
	
	// installing: Boolean
	//	Set if we are in the middle of a Flash installation session.
	installing: false,
	
	isVersionOrAbove: function(
							/* int */ reqMajorVer, 
							/* int */ reqMinorVer, 
							/* int */ reqVer){ /* Boolean */
		// summary: 
		//	Asserts that this environment has the given major, minor, and revision
		//	numbers for the Flash player.
		// description:
		//	Asserts that this environment has the given major, minor, and revision
		//	numbers for the Flash player. 
		//	
		//	Example- To test for Flash Player 7r14:
		//	
		//	dojox.flash.info.isVersionOrAbove(7, 0, 14)
		// returns:
		//	Returns true if the player is equal
		//	or above the given version, false otherwise.
		
		// make the revision a decimal (i.e. transform revision 14 into
		// 0.14
		reqVer = parseFloat("." + reqVer);
		
		if(this.versionMajor >= reqMajorVer && this.versionMinor >= reqMinorVer
			 && this.versionRevision >= reqVer){
			return true;
		}else{
			return false;
		}
	},
	
	getResourceList: function(/*string*/ swfloc6, /*String*/ swfloc8){ /*String[]*/
		// summary:
		//		Returns all resources required for embedding.
		// description:
		//		This is a convenience method for Dojo Offline, meant to
		//		encapsulate us from the specific resources necessary for
		//		embedding. Dojo Offline requires that we sync our offline
		//		resources for offline availability; this method will return all
		//		offline resources, including any possible query parameters that
		//		might be used since caches treat resources with query
		//		parameters as different than ones that have query parameters.
		//		If offline and we request a resource with a query parameter
		//		that was not cached with a query parameter, then we will have a
		//		cache miss and not be able to work offline
		var results = [];
		
		// flash 6
		var swfloc = swfloc6;
		results.push(swfloc);
		swfloc = swfloc + "?baseRelativePath=" + escape(dojo.baseUrl); // FIXME: should this be encodeURIComponent?
		results.push(swfloc);
		// Safari has a strange bug where it appends '%20'%20quality=
		// to the end of Flash movies taken through XHR while offline;
		// append this so we don't get a cache miss
		swfloc += "'%20'%20quality=";
		results.push(swfloc);
		
		// flash 8
		swfloc = swfloc8;
		results.push(swfloc);
		swfloc +=  "?baseRelativePath="+escape(dojo.baseUrl); // FIXME: should this be encodeURIComponent?
		results.push(swfloc);
		// Safari has a strange bug where it appends '%20'%20quality=
		// to the end of Flash movies taken through XHR while offline;
		// append this so we don't get a cache miss
		swfloc += "'%20'%20quality=";
		results.push(swfloc);
		
		// flash 6 gateway
		results.push(dojo.moduleUrl("dojox", "flash/flash6/flash6_gateway.swf")+"");
		
		return results;
	},
	
	_detectVersion: function(){
		var versionStr;
		
		// loop backwards through the versions until we find the newest version	
		for(var testVersion = 25; testVersion > 0; testVersion--){
			if(dojo.isIE){
				versionStr = VBGetSwfVer(testVersion);
			}else{
				versionStr = this._JSFlashInfo(testVersion);		
			}
				
			if(versionStr == -1 ){
				this.capable = false; 
				return;
			}else if(versionStr != 0){
				var versionArray;
				if(dojo.isIE){
					var tempArray = versionStr.split(" ");
					var tempString = tempArray[1];
					versionArray = tempString.split(",");
				}else{
					versionArray = versionStr.split(".");
				}
					
				this.versionMajor = versionArray[0];
				this.versionMinor = versionArray[1];
				this.versionRevision = versionArray[2];
				
				// 7.0r24 == 7.24
				var versionString = this.versionMajor + "." + this.versionRevision;
				this.version = parseFloat(versionString);
				
				this.capable = true;
				
				break;
			}
		}
	},
	 
	// JavaScript helper required to detect Flash Player PlugIn version 
	// information. Internet Explorer uses a corresponding Visual Basic
	// version to interact with the Flash ActiveX control. 
	_JSFlashInfo: function(testVersion){
		// NS/Opera version >= 3 check for Flash plugin in plugin array
		if(navigator.plugins != null && navigator.plugins.length > 0){
			if(navigator.plugins["Shockwave Flash 2.0"] || 
				 navigator.plugins["Shockwave Flash"]){
				var swVer2 = navigator.plugins["Shockwave Flash 2.0"] ? " 2.0" : "";
				var flashDescription = navigator.plugins["Shockwave Flash" + swVer2].description;
				var descArray = flashDescription.split(" ");
				var tempArrayMajor = descArray[2].split(".");
				var versionMajor = tempArrayMajor[0];
				var versionMinor = tempArrayMajor[1];
				if(descArray[3] != ""){
					var tempArrayMinor = descArray[3].split("r");
				}else{
					var tempArrayMinor = descArray[4].split("r");
				}
				var versionRevision = tempArrayMinor[1] > 0 ? tempArrayMinor[1] : 0;
				var version = versionMajor + "." + versionMinor + "." 
											+ versionRevision;
											
				return version;
			}
		}
		
		return -1;
	},
	
	// Detects the mechanisms that should be used for Flash/JavaScript 
	// communication, setting 'commVersion' to either 6 or 8. If the value is
	// 6, we use Flash Plugin 6+ features, such as GetVariable, TCallLabel,
	// and fscommand, to do Flash/JavaScript communication; if the value is
	// 8, we use the ExternalInterface API for communication. 
	_detectCommunicationVersion: function(){
		if(this.capable == false){
			this.commVersion = null;
			return;
		}
		
		// detect if the user has over-ridden the default flash version
		if (typeof djConfig["forceFlashComm"] != "undefined" &&
				typeof djConfig["forceFlashComm"] != null){
			this.commVersion = djConfig["forceFlashComm"];
			return;
		}
		
		// we prefer Flash 6 features over Flash 8, because they are much faster
		// and much less buggy
		
		// at this point, we don't have a flash file to detect features on,
		// so we need to instead look at the browser environment we are in
		if(dojo.isSafari||dojo.isOpera){
			this.commVersion = 8;
		}else{
			this.commVersion = 6;
		}
	}
};

dojox.flash.Embed = function(visible){
	// summary: A class that is used to write out the Flash object into the page.
	
	this._visible = visible;
}

dojox.flash.Embed.prototype = {
	// width: int
	//	The width of this Flash applet. The default is the minimal width
	//	necessary to show the Flash settings dialog. Current value is 
	//  215 pixels.
	width: 215,
	
	// height: int 
	//	The height of this Flash applet. The default is the minimal height
	//	necessary to show the Flash settings dialog. Current value is
	// 138 pixels.
	height: 138,
	
	// id: String
	// 	The id of the Flash object. Current value is 'flashObject'.
	id: "flashObject",
	
	// Controls whether this is a visible Flash applet or not.
	_visible: true,

	protocol: function(){
		switch(window.location.protocol){
			case "https:":
				return "https";
				break;
			default:
				return "http";
				break;
		}
	},
	
	write: function(/* String */ flashVer, /* Boolean? */ doExpressInstall){
		// summary: Writes the Flash into the page.
		// description:
		//	This must be called before the page
		//	is finished loading. 
		// flashVer: String
		//	The Flash version to write.
		// doExpressInstall: Boolean
		//	Whether to write out Express Install
		//	information. Optional value; defaults to false.
		
		//dojo.debug("write");
		doExpressInstall = !!doExpressInstall;
		
		// determine our container div's styling
		var containerStyle = "";
		containerStyle += ("width: " + this.width + "px; ");
		containerStyle += ("height: " + this.height + "px; ");
		if(this._visible == false){
			containerStyle += "position: absolute; z-index: 10000; top: -1000px; left: -1000px; ";
		}

		// figure out the SWF file to get and how to write out the correct HTML
		// for this Flash version
		var objectHTML;
		var swfloc;
		// Flash 6
		if(flashVer == 6){
			swfloc = dojox.flash.flash6_version;
			var dojoPath = djConfig.baseRelativePath;
			swfloc = swfloc + "?baseRelativePath=" + escape(dojoPath);
			objectHTML = 
						  '<embed id="' + this.id + '" src="' + swfloc + '" '
						+ '    quality="high" bgcolor="#ffffff" '
						+ '    width="' + this.width + '" height="' + this.height + '" '
						+ '    name="' + this.id + '" '
						+ '    align="middle" allowScriptAccess="sameDomain" '
						+ '    type="application/x-shockwave-flash" swLiveConnect="true" '
						+ '    pluginspage="'
						+ this.protocol()
						+ '://www.macromedia.com/go/getflashplayer">';
		}else{ // Flash 8
			swfloc = dojox.flash.flash8_version;
			var swflocObject = swfloc;
			var swflocEmbed = swfloc;
			var dojoPath = djConfig.baseRelativePath;
			if(doExpressInstall){
				// the location to redirect to after installing
				var redirectURL = escape(window.location);
				document.title = document.title.slice(0, 47) + " - Flash Player Installation";
				var docTitle = escape(document.title);
				swflocObject += "?MMredirectURL=" + redirectURL
				                + "&MMplayerType=ActiveX"
				                + "&MMdoctitle=" + docTitle
								+ "&baseRelativePath=" + escape(dojoPath);
				swflocEmbed += "?MMredirectURL=" + redirectURL 
								+ "&MMplayerType=PlugIn"
								+ "&baseRelativePath=" + escape(dojoPath);
			}

			if(swflocEmbed.indexOf("?") == -1){
				swflocEmbed +=  "?baseRelativePath="+escape(dojoPath)+"' ";
			}
			
			objectHTML =
				'<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" '
				  + 'codebase="'
					+ this.protocol()
					+ '://fpdownload.macromedia.com/pub/shockwave/cabs/flash/'
					+ 'swflash.cab#version=8,0,0,0" '
				  + 'width="' + this.width + '" '
				  + 'height="' + this.height + '" '
				  + 'id="' + this.id + '" '
				  + 'align="middle"> '
				  + '<param name="allowScriptAccess" value="sameDomain" /> '
				  + '<param name="movie" value="' + swflocObject + '" /> '
				  + '<param name="quality" value="high" /> '
				  + '<param name="bgcolor" value="#ffffff" /> '
				  + '<embed src="' + swflocEmbed + "' "
				  + 'quality="high" '
				  + 'bgcolor="#ffffff" '
				  + 'width="' + this.width + '" '
				  + 'height="' + this.height + '" '
				  + 'id="' + this.id + '" '
				  + 'name="' + this.id + '" '
				  + 'swLiveConnect="true" '
				  + 'align="middle" '
				  + 'allowScriptAccess="sameDomain" '
				  + 'type="application/x-shockwave-flash" '
				  + 'pluginspage="'
					+ this.protocol()
					+'://www.macromedia.com/go/getflashplayer" />'
				+ '</object>';
		}

		// now write everything out
		objectHTML = '<div id="' + this.id + 'Container" style="' + containerStyle + '"> '
						+ objectHTML
					 + '</div>';
		document.writeln(objectHTML);
	},  
	
	get: function(){ /* Object */
		// summary: Gets the Flash object DOM node.
		
		//return (dojo.render.html.ie) ? window[this.id] : document[this.id];
		
		// more robust way to get Flash object; version above can break
		// communication on IE sometimes
		return document.getElementById(this.id);
	},
	
	setVisible: function(/* Boolean */ visible){
		// summary: Sets the visibility of this Flash object.
		
		var container = dojo.byId(this.id + "Container");
		if(visible == true){
			container.style.visibility = "visible";
		}else{
			container.style.position = "absolute";
			container.style.x = "-1000px";
			container.style.y = "-1000px";
			container.style.visibility = "hidden";
		}
	},
	
	center: function(){
		// summary: Centers the flash applet on the page.
		
		/*
		var elementWidth = this.width;
		var elementHeight = this.height;

		var scroll_offset = dojo._docScroll();
		var viewport_size = dojo.html.getViewport();

		// compute the centered position    
		var x = scroll_offset.x + (viewport_size.width - elementWidth) / 2;
		var y = scroll_offset.y + (viewport_size.height - elementHeight) / 2; 
		*/
		var x = 100;
		var y = 100;

		// set the centered position
		var container = dojo.byId(this.id + "Container");
		container.style.top = y + "px";
		container.style.left = x + "px";
	}
};


dojox.flash.Communicator = function(){
	// summary:
	//	A class that is used to communicate between Flash and JavaScript in 
	//	a way that can pass large amounts of data back and forth reliably,
	//	very fast, and with synchronous method calls.
	// description: 
	//	A class that is used to communicate between Flash and JavaScript in 
	//	a way that can pass large amounts of data back and forth reliably,
	//	very fast, and with synchronous method calls. This class encapsulates the 
	//	specific way in which this communication occurs,
	//	presenting a common interface to JavaScript irrespective of the underlying
	//	Flash version.

	if(dojox.flash.useFlash6()){
		this._writeFlash6();
	}else if(dojox.flash.useFlash8()){
		this._writeFlash8();
	}
}

dojox.flash.Communicator.prototype = {
	_writeFlash6: function(){
		var id = dojox.flash.obj.id;
		
		// global function needed for Flash 6 callback;
		// we write it out as a script tag because the VBScript hook for IE
		// callbacks does not work properly if this function is evalled() from
		// within the Dojo system
		document.writeln('<script language="JavaScript">');
		document.writeln('  function ' + id + '_DoFSCommand(command, args){ ');
		document.writeln('    dojox.flash.comm._handleFSCommand(command, args); ');
		document.writeln('}');
		document.writeln('</script>');
		
		// hook for Internet Explorer to receive FSCommands from Flash
		if(dojo.isIE){
			document.writeln('<SCRIPT LANGUAGE=VBScript\> ');
			document.writeln('on error resume next ');
			document.writeln('Sub ' + id + '_FSCommand(ByVal command, ByVal args)');
			document.writeln(' call ' + id + '_DoFSCommand(command, args)');
			document.writeln('end sub');
			document.writeln('</SCRIPT\> ');
		}
	},
	
	_writeFlash8: function(){
		// nothing needs to be written out for Flash 8 communication; 
		// happens automatically
	},
	
	//Flash 6 communication.
	
	// Handles fscommand's from Flash to JavaScript. Flash 6 communication.
	_handleFSCommand: function(command, args){
		//console.debug("fscommand, command="+command+", args="+args);
		// Flash 8 on Mac/Firefox precedes all commands with the string "FSCommand:";
		// strip it off if it is present
		if((command) && dojo.isString(command) && (/^FSCommand:(.*)/.test(command) == true)){
			command = command.match(/^FSCommand:(.*)/)[1];
		}
		 
		if(command == "addCallback"){ // add Flash method for JavaScript callback
			this._fscommandAddCallback(command, args);
		}else if(command == "call"){ // Flash to JavaScript method call
			this._fscommandCall(command, args);
		}else if(command == "fscommandReady"){ // see if fscommands are ready
			this._fscommandReady();
		}
	},
	
	// Handles registering a callable Flash function. Flash 6 communication.
	_fscommandAddCallback: function(command, args){
		var functionName = args;
			
		// do a trick, where we link this function name to our wrapper
		// function, _call, that does the actual JavaScript to Flash call
		var callFunc = function(){
			return dojox.flash.comm._call(functionName, arguments);
		};			
		dojox.flash.comm[functionName] = callFunc;
		
		// indicate that the call was successful
		dojox.flash.obj.get().SetVariable("_succeeded", true);
	},
	
	// Handles Flash calling a JavaScript function. Flash 6 communication.
	_fscommandCall: function(command, args){
		var plugin = dojox.flash.obj.get();
		var functionName = args;
		
		// get the number of arguments to this method call and build them up
		var numArgs = parseInt(plugin.GetVariable("_numArgs"));
		var flashArgs = new Array();
		for(var i = 0; i < numArgs; i++){
			var currentArg = plugin.GetVariable("_" + i);
			flashArgs.push(currentArg);
		}
		
		// get the function instance; we technically support more capabilities
		// than ExternalInterface, which can only call global functions; if
		// the method name has a dot in it, such as "dojox.flash.loaded", we
		// eval it so that the method gets run against an instance
		var runMe;
		if(functionName.indexOf(".") == -1){ // global function
			runMe = window[functionName];
		}else{
			// instance function
			runMe = eval(functionName);
		}
		
		// make the call and get the results
		var results = null;
		if(dojo.isFunction(runMe)){
			results = runMe.apply(null, flashArgs);
		}
		
		// return the results to flash
		plugin.SetVariable("_returnResult", results);
	},
	
	// Reports that fscommands are ready to run if executed from Flash.
	_fscommandReady: function(){
		var plugin = dojox.flash.obj.get();
		plugin.SetVariable("fscommandReady", "true");
	},
	
	// The actual function that will execute a JavaScript to Flash call; used
	// by the Flash 6 communication method. 
	_call: function(functionName, args){
		// we do JavaScript to Flash method calls by setting a Flash variable
		// "_functionName" with the function name; "_numArgs" with the number
		// of arguments; and "_0", "_1", etc for each numbered argument. Flash
		// reads these, executes the function call, and returns the result
		// in "_returnResult"
		var plugin = dojox.flash.obj.get();
		plugin.SetVariable("_functionName", functionName);
		plugin.SetVariable("_numArgs", args.length);
		for(var i = 0; i < args.length; i++){
			// unlike Flash 8's ExternalInterface, Flash 6 has no problem with
			// any special characters _except_ for the null character \0; double
			// encode this so the Flash side never sees it, but we can get it 
			// back if the value comes back to JavaScript
			var value = args[i];
			value = value.replace(/\0/g, "\\0");
			
			plugin.SetVariable("_" + i, value);
		}
		
		// now tell Flash to execute this method using the Flash Runner
		plugin.TCallLabel("/_flashRunner", "execute");
		
		// get the results
		var results = plugin.GetVariable("_returnResult");
		
		// we double encoded all null characters as //0 because Flash breaks
		// if they are present; turn the //0 back into /0
		results = results.replace(/\\0/g, "\0");
		
		return results;
	},
	
	// Flash 8 communication.
	
	// Registers the existence of a Flash method that we can call with
	// JavaScript, using Flash 8's ExternalInterface. 
	_addExternalInterfaceCallback: function(methodName){
		var wrapperCall = function(){
			// some browsers don't like us changing values in the 'arguments' array, so
			// make a fresh copy of it
			var methodArgs = new Array(arguments.length);
			for(var i = 0; i < arguments.length; i++){
				methodArgs[i] = arguments[i];
			}
			return dojox.flash.comm._execFlash(methodName, methodArgs);
		};
		
		dojox.flash.comm[methodName] = wrapperCall;
	},
	
	// Encodes our data to get around ExternalInterface bugs.
	// Flash 8 communication.
	_encodeData: function(data){
		// double encode all entity values, or they will be mis-decoded
		// by Flash when returned
		var entityRE = /\&([^;]*)\;/g;
		data = data.replace(entityRE, "&amp;$1;");
		
		// entity encode XML-ish characters, or Flash's broken XML serializer
		// breaks
		data = data.replace(/</g, "&lt;");
		data = data.replace(/>/g, "&gt;");
		
		// transforming \ into \\ doesn't work; just use a custom encoding
		data = data.replace("\\", "&custom_backslash;&custom_backslash;");
		
		data = data.replace(/\n/g, "\\n");
		data = data.replace(/\r/g, "\\r");
		data = data.replace(/\f/g, "\\f");
		data = data.replace(/\0/g, "\\0"); // null character
		data = data.replace(/\'/g, "\\\'");
		data = data.replace(/\"/g, '\\\"');
		
		return data;
	},
	
	// Decodes our data to get around ExternalInterface bugs.
	// Flash 8 communication.
	_decodeData: function(data){
		if(data == null || typeof data == "undefined"){
			return data;
		}
		
		// certain XMLish characters break Flash's wire serialization for
		// ExternalInterface; these are encoded on the 
		// DojoExternalInterface side into a custom encoding, rather than
		// the standard entity encoding, because otherwise we won't be able to
		// differentiate between our own encoding and any entity characters
		// that are being used in the string itself
		data = data.replace(/\&custom_lt\;/g, "<");
		data = data.replace(/\&custom_gt\;/g, ">");
		
		// Unfortunately, Flash returns us our String with special characters
		// like newlines broken into seperate characters. So if \n represents
		// a new line, Flash returns it as "\" and "n". This means the character
		// is _not_ a newline. This forces us to eval() the string to cause
		// escaped characters to turn into their real special character values.
		data = eval('"' + data + '"');
		
		return data;
	},
	
	// Sends our method arguments over to Flash in chunks in order to
	// have ExternalInterface's performance not be O(n^2).
	// Flash 8 communication.
	_chunkArgumentData: function(value, argIndex){
		var plugin = dojox.flash.obj.get();
		
		// cut up the string into pieces, and push over each piece one
		// at a time
		var numSegments = Math.ceil(value.length / 1024);
		for(var i = 0; i < numSegments; i++){
			var startCut = i * 1024;
			var endCut = i * 1024 + 1024;
			if(i == (numSegments - 1)){
				endCut = i * 1024 + value.length;
			}
			
			var piece = value.substring(startCut, endCut);
			
			// encode each piece seperately, rather than the entire
			// argument data, because ocassionally a special 
			// character, such as an entity like &foobar;, will fall between
			// piece boundaries, and we _don't_ want to encode that value if
			// it falls between boundaries, or else we will end up with incorrect
			// data when we patch the pieces back together on the other side
			piece = this._encodeData(piece);
			
			// directly use the underlying CallFunction method used by
			// ExternalInterface, which is vastly faster for large strings
			// and lets us bypass some Flash serialization bugs
			plugin.CallFunction('<invoke name="chunkArgumentData" '
														+ 'returntype="javascript">'
														+ '<arguments>'
														+ '<string>' + piece + '</string>'
														+ '<number>' + argIndex + '</number>'
														+ '</arguments>'
														+ '</invoke>');
		}
	},
	
	// Gets our method return data in chunks for better performance.
	// Flash 8 communication.
	_chunkReturnData: function(){
		var plugin = dojox.flash.obj.get();
		
		var numSegments = plugin.getReturnLength();
		var resultsArray = new Array();
		for(var i = 0; i < numSegments; i++){
			// directly use the underlying CallFunction method used by
			// ExternalInterface, which is vastly faster for large strings
			var piece = 
					plugin.CallFunction('<invoke name="chunkReturnData" '
															+ 'returntype="javascript">'
															+ '<arguments>'
															+ '<number>' + i + '</number>'
															+ '</arguments>'
															+ '</invoke>');
															
			// remove any leading or trailing JavaScript delimiters, which surround
			// our String when it comes back from Flash since we bypass Flash's
			// deserialization routines by directly calling CallFunction on the
			// plugin
			if(piece == '""' || piece == "''"){
				piece = "";
			}else{
				piece = piece.substring(1, piece.length-1);
			}
		
			resultsArray.push(piece);
		}
		var results = resultsArray.join("");
		
		return results;
	},
	
	// Executes a Flash method; called from the JavaScript wrapper proxy we
	// create on dojox.flash.comm.
	// Flash 8 communication.
	_execFlash: function(methodName, methodArgs){
		var plugin = dojox.flash.obj.get();
				
		// begin Flash method execution
		plugin.startExec();
		
		// set the number of arguments
		plugin.setNumberArguments(methodArgs.length);
		
		// chunk and send over each argument
		for(var i = 0; i < methodArgs.length; i++){
			this._chunkArgumentData(methodArgs[i], i);
		}
		
		// execute the method
		plugin.exec(methodName);
														
		// get the return result
		var results = this._chunkReturnData();
		
		// decode the results
		results = this._decodeData(results);
		
		// reset everything
		plugin.endExec();
		
		return results;
	}
}

// FIXME: dojo.declare()-ify this

dojox.flash.Install = function(){
	// summary: Helps install Flash plugin if needed.
	// description:
	//		Figures out the best way to automatically install the Flash plugin
	//		for this browser and platform. Also determines if installation or
	//		revving of the current plugin is needed on this platform.
}

dojox.flash.Install.prototype = {
	needed: function(){ /* Boolean */
		// summary:
		//		Determines if installation or revving of the current plugin is
		//		needed. 
	
		// do we even have flash?
		if(dojox.flash.info.capable == false){
			return true;
		}

		var isMac = (navigator.appVersion.indexOf("Macintosh") >= 0);

		// are we on the Mac? Safari needs Flash version 8 to do Flash 8
		// communication, while Firefox/Mac needs Flash 8 to fix bugs it has
		// with Flash 6 communication
		if(isMac && (!dojox.flash.info.isVersionOrAbove(8, 0, 0))){
			return true;
		}

		// other platforms need at least Flash 6 or above
		if(!dojox.flash.info.isVersionOrAbove(6, 0, 0)){
			return true;
		}

		// otherwise we don't need installation
		return false;
	},

	install: function(){
		// summary: Performs installation or revving of the Flash plugin.
		
		//dojo.debug("install");
		// indicate that we are installing
		dojox.flash.info.installing = true;
		dojox.flash.installing();
		
		if(dojox.flash.info.capable == false){ // we have no Flash at all
			//dojo.debug("Completely new install");
			// write out a simple Flash object to force the browser to prompt
			// the user to install things
			var installObj = new dojox.flash.Embed(false);
			installObj.write(8); // write out HTML for Flash 8 version+
		}else if(dojox.flash.info.isVersionOrAbove(6, 0, 65)){ // Express Install
			//dojo.debug("Express install");
			var installObj = new dojox.flash.Embed(false);
			installObj.write(8, true); // write out HTML for Flash 8 version+
			installObj.setVisible(true);
			installObj.center();
		}else{ // older Flash install than version 6r65
			alert("This content requires a more recent version of the Macromedia "
						+" Flash Player.");
			window.location.href = + dojox.flash.Embed.protocol() +
						"://www.macromedia.com/go/getflashplayer";
		}
	},
	
	// Called when the Express Install is either finished, failed, or was
	// rejected by the user.
	_onInstallStatus: function(msg){
		if (msg == "Download.Complete"){
			// Installation is complete.
			dojox.flash._initialize();
		}else if(msg == "Download.Cancelled"){
			alert("This content requires a more recent version of the Macromedia "
						+" Flash Player.");
			window.location.href = dojox.flash.Embed.protocol() +
						"://www.macromedia.com/go/getflashplayer";
		}else if (msg == "Download.Failed"){
			// The end user failed to download the installer due to a network failure
			alert("There was an error downloading the Flash Player update. "
						+ "Please try again later, or visit macromedia.com to download "
						+ "the latest version of the Flash plugin.");
		}	
	}
}

// find out if Flash is installed
dojox.flash.info = new dojox.flash.Info();

// vim:ts=4:noet:tw=0:

}
