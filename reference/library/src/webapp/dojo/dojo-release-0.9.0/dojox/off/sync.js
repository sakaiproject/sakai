if(!dojo._hasResource["dojox.off.sync"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.off.sync"] = true;
dojo.provide("dojox.off.sync");

dojo.require("dojox.storage.GearsStorageProvider");
dojo.require("dojox.off._common");
dojo.require("dojox.off.files");

// Author: Brad Neuberg, bkn3@columbia.edu, http://codinginparadise.org

// summary:
//		Exposes syncing functionality to offline applications
dojo.mixin(dojox.off.sync, {
	// isSyncing: boolean
	//		Whether we are in the middle of a syncing session.
	isSyncing: false,
	
	// cancelled: boolean
	//		Whether we were cancelled during our last sync request or not. If
	//		we are cancelled, then successful will be false.
	cancelled: false,
	
	// successful: boolean
	//		Whether the last sync was successful or not.  If false, an error
	//		occurred.
	successful: true,
	
	// details: String[]
	//		Details on the sync. If the sync was successful, this will carry
	//		any conflict or merging messages that might be available; if the
	//		sync was unsuccessful, this will have an error message.  For both
	//		of these, this should be an array of Strings, where each string
	//		carries details on the sync. 
	//	Example: 
	//		dojox.off.sync.details = ["The document 'foobar' had conflicts - yours one",
	//						"The document 'hello world' was automatically merged"];
	details: [],
	
	// error: boolean
	//		Whether an error occurred during the syncing process.
	error: false,
	
	// actions: dojox.off.sync.ActionLog
	//		Our ActionLog that we store offline actions into for later
	//		replaying when we go online
	actions: null,
	
	// autoSync: boolean
	//		For advanced usage; most developers can ignore this.
	//		Whether we do automatically sync on page load or when we go online.
	//		If true we do, if false syncing must be manually initiated.
	//		Defaults to true.
	autoSync: true,
	
	// summary:
	//	An event handler that is called during the syncing process with
	//	the state of syncing. It is important that you connect to this
	//	method and respond to certain sync events, especially the 
	//	"download" event.
	// description:
	//	This event handler is called during the syncing process. You can
	//	do a dojo.connect to receive sync feedback:
	//
	//		dojo.connect(dojox.off.sync, "onSync", someFunc);
	//
	//	You will receive one argument, which is the type of the event
	//	and which can have the following values.
	//
	//	The most common two types that you need to care about are "download"
	//	and "finished", especially if you are using the default
	//	Dojo Offline UI widget that does the hard work of informing
	//	the user through the UI about what is occuring during syncing.
	//
	//	If you receive the "download" event, you should make a network call
	//	to retrieve and store your data somehow for offline access. The
	//	"finished" event indicates that syncing is done. An example:
	//	
	//		dojo.connect(dojox.off.sync, "onSync", function(type){
	//			if(type == "download"){
	//				// make a network call to download some data
	//				// for use offline
	//				dojo.xhrGet({
	//					url: 		"downloadData.php",
	//					handleAs:	"javascript",
	//					error:		function(err){
	//						dojox.off.sync.finishedDownloading(false, "Can't download data");
	//					},
	//					load:		function(data){
	//						// store our data
	//						dojox.storage.put("myData", data);
	//
	//						// indicate we are finished downloading
	//						dojox.off.sync.finishedDownloading(true);
	//					}
	//				});
	//			}else if(type == "finished"){
	//				// update UI somehow to indicate we are finished,
	//				// such as using the download data to change the 
	//				// available data
	//			}
	//		})
	//
	//	Here is the full list of event types if you want to do deep
	//	customization, such as updating your UI to display the progress
	//	of syncing (note that the default Dojo Offline UI widget does
	//	this for you if you choose to pull that in). Most of these
	//	are only appropriate for advanced usage and can be safely
	//	ignored:
	//
	//		* "start"
	//				syncing has started
	//		* "refreshFiles"
	//				syncing will begin refreshing
	//				our offline file cache
	//		* "upload"
	//				syncing will begin uploading
	//				any local data changes we have on the client.
	//				This event is fired before we fire
	//				the dojox.off.sync.actions.onReplay event for
	//				each action to replay; use it to completely
	//				over-ride the replaying behavior and prevent
	//				it entirely, perhaps rolling your own sync
	//				protocol if needed.
	//		* "download"
	//				syncing will begin downloading any new data that is
	//				needed into persistent storage. Applications are required to
	//				implement this themselves, storing the required data into
	//				persistent local storage using Dojo Storage.
	//		* "finished"
	//				syncing is finished; this
	//				will be called whether an error ocurred or not; check
	//				dojox.off.sync.successful and dojox.off.sync.error for sync details
	//		* "cancel"
	//				Fired when canceling has been initiated; canceling will be
	//				attempted, followed by the sync event "finished".
	onSync: function(/* String */ type){},
	
	synchronize: function(){ /* void */
		// summary: Starts synchronizing

		//dojo.debug("synchronize");
		if(this.isSyncing || dojox.off.goingOnline || (!dojox.off.isOnline)){
			return;
		}
	
		this.isSyncing = true;
		this.successful = false;
		this.details = [];
		this.cancelled = false;
		
		this.start();
	},
	
	cancel: function(){ /* void */
		// summary:
		//	Attempts to cancel this sync session
		
		if(!this.isSyncing){ return; }
		
		this.cancelled = true;
		if(dojox.off.files.refreshing){
			dojox.off.files.abortRefresh();
		}
		
		this.onSync("cancel");
	},
	
	finishedDownloading: function(successful /* boolean? */, 
									errorMessage /* String? */){
		// summary:
		//		Applications call this method from their
		//		after getting a "download" event in
		//		dojox.off.sync.onSync to signal that
		//		they are finished downloading any data 
		//		that should be available offline
		// successful: boolean?
		//		Whether our downloading was successful or not.
		//		If not present, defaults to true.
		// errorMessage: String?
		//		If unsuccessful, a message explaining why
		if(typeof successful == "undefined"){
			successful = true;
		}
		
		if(!successful){
			this.successful = false;
			this.details.push(errorMessage);
			this.error = true;
		}
		
		this.finished();
	},
	
	start: function(){ /* void */
		// summary:
		//	For advanced usage; most developers can ignore this.
		//	Called at the start of the syncing process. Advanced
		//	developers can over-ride this method to use their
		//	own sync mechanism to start syncing.
		
		if(this.cancelled){
			this.finished();
			return;
		}
		this.onSync("start");
		this.refreshFiles();
	},
	
	refreshFiles: function(){ /* void */
		// summary:
		//	For advanced usage; most developers can ignore this.
		//	Called when we are going to refresh our list
		//	of offline files during syncing. Advanced developers 
		//	can over-ride this method to do some advanced magic related to
		//	refreshing files.
		
		//dojo.debug("refreshFiles");
		if(this.cancelled){
			this.finished();
			return;
		}
		
		this.onSync("refreshFiles");
		
		dojox.off.files.refresh(dojo.hitch(this, function(error, errorMessages){
			if(error){
				this.error = true;
				this.successful = false;
				for(var i = 0; i < errorMessages.length; i++){
					this.details.push(errorMessages[i]);
				}
				
				// even if we get an error while syncing files,
				// keep syncing so we can upload and download
				// data
			}
			
			this.upload();
		}));
	},
	
	upload: function(){ /* void */
		// summary:
		//	For advanced usage; most developers can ignore this.
		//	Called when syncing wants to upload data. Advanced
		//	developers can over-ride this method to completely
		//	throw away the Action Log and replaying system
		//	and roll their own advanced sync mechanism if needed.
		
		if(this.cancelled){
			this.finished();
			return;
		}
		
		this.onSync("upload");
		
		// when we are done uploading start downloading
		dojo.connect(this.actions, "onReplayFinished", this, this.download);
		
		// replay the actions log
		this.actions.replay();
	},
	
	download: function(){ /* void */
		// summary:
		//	For advanced usage; most developers can ignore this.
		//	Called when syncing wants to download data. Advanced
		//	developers can over-ride this method to use their
		//	own sync mechanism.
		
		if(this.cancelled){
			this.finished();
			return;
		}
		
		// apps should respond to the "download"
		// event to download their data; when done
		// they must call dojox.off.sync.finishedDownloading()
		this.onSync("download");
	},
	
	finished: function(){ /* void */
		// summary:
		//	For advanced usage; most developers can ignore this.
		//	Called when syncing is finished. Advanced
		//	developers can over-ride this method to clean
		//	up after finishing their own sync
		//	mechanism they might have rolled.
		this.isSyncing = false;
		
		this.successful = (!this.cancelled && !this.error);
		
		this.onSync("finished");
	},
	
	_save: function(callback){
		this.actions._save(function(){
			callback();
		});
	},
	
	_load: function(callback){
		this.actions._load(function(){
			callback();
		});
	}
});


// summary:
//		A class that records actions taken by a user when they are offline,
//		suitable for replaying when the network reappears. 
// description:
//		The basic idea behind this method is to record user actions that would
//		normally have to contact a server into an action log when we are
//		offline, so that later when we are online we can simply replay this log
//		in the order user actions happened so that they can be executed against
//		the server, causing synchronization to happen. 
//		
//		When we replay, for each of the actions that were added, we call a 
//		method named onReplay that applications should connect to and 
//		which will be called over and over for each of our actions -- 
//		applications should take the offline action
//		information and use it to talk to a server to have this action
//		actually happen online, 'syncing' themselves with the server. 
//
//		For example, if the action was "update" with the item that was updated, we
//		might call some RESTian server API that exists for updating an item in
//		our application.  The server could either then do sophisticated merging
//		and conflict resolution on the server side, for example, allowing you
//		to pop up a custom merge UI, or could do automatic merging or nothing
//		of the sort. When you are finished with this particular action, your
//		application is then required to call continueReplay() on the actionLog object
//		passed to onReplay() to continue replaying the action log, or haltReplay()
//		with the reason for halting to completely stop the syncing/replaying
//		process.
//
//		For example, imagine that we have a web application that allows us to add
//		contacts. If we are offline, and we update a contact, we would add an action;
//		imagine that the user has to click an Update button after changing the values
//		for a given contact:
//	
//		dojox.off.whenOffline(dojo.byId("updateButton"), "onclick", function(evt){
//			// get the updated customer values
//			var customer = getCustomerValues();
//			
//			// we are offline -- just record this action
//			var action = {name: "update", customer: customer};
//			dojox.off.sync.actions.add(action)
//			
//			// persist this customer data into local storage as well
//			dojox.storage.put(customer.name, customer);
//		})
//
//		Then, when we go back online, the dojox.off.sync.actions.onReplay event
//		will fire over and over, once for each action that was recorded while offline:
//
//		dojo.connect(dojox.off.sync.actions, "onReplay", function(action, actionLog){
//			// called once for each action we added while offline, in the order
//			// they were added
//			if(action.name == "update"){
//				var customer = action.customer;
//				
//				// call some network service to update this customer
//				dojo.xhrPost({
//					url: "updateCustomer.php",
//					content: {customer: dojo.toJson(customer)},
//					error: function(err){
//						actionLog.haltReplay(err);
//					},
//					load: function(data){
//						actionLog.continueReplay();
//					}
//				})
//			}
//		})
//
//		Note that the actions log is always automatically persisted locally while using it, so
//		that if the user closes the browser or it crashes the actions will safely be stored
//		for later replaying.
dojo.declare("dojox.off.sync.ActionLog", null, {
		// entries: Array
		//		An array of our action entries, where each one is simply a custom
		//		object literal that were passed to add() when this action entry
		//		was added.
		entries: [],
		
		// reasonHalted: String
		//		If we halted, the reason why
		reasonHalted: null,
		
		// isReplaying: boolean
		//		If true, we are in the middle of replaying a command log; if false,
		//		then we are not
		isReplaying: false,
		
		// autoSave: boolean
		//		Whether we automatically save the action log after each call to
		//		add(); defaults to true. For applications that are rapidly adding
		//		many action log entries in a short period of time, it can be
		//		useful to set this to false and simply call save() yourself when
		//		you are ready to persist your command log -- otherwise performance
		//		could be slow as the default action is to attempt to persist the
		//		actions log constantly with calls to add().
		autoSave: true,
		
		add: function(action /* Object */){ /* void */
			// summary:
			//	Adds an action to our action log
			// description:
			//	This method will add an action to our
			//	action log, later to be replayed when we
			//	go from offline to online. 'action'
			//	will be available when this action is
			//	replayed and will be passed to onReplay.
			//
			//	Example usage:
			//	
			//	dojox.off.sync.log.add({actionName: "create", itemType: "document",
			//					  {title: "Message", content: "Hello World"}});
			// 
			//	The object literal is simply a custom object appropriate
			//	for our application -- it can be anything that preserves the state
			//	of a user action that will be executed when we go back online
			//	and replay this log. In the above example,
			//	"create" is the name of this action; "documents" is the 
			//	type of item this command is operating on, such as documents, contacts,
			//	tasks, etc.; and the final argument is the document that was created. 
			
			if(this.isReplaying){
				throw "Programming error: you can not call "
						+ "dojox.off.sync.actions.add() while "
						+ "we are replaying an action log";
			}
			
			this.entries.push(action);
			
			// save our updated state into persistent
			// storage
			if(this.autoSave){
				this._save();
			}
		},
		
		onReplay: function(action /* Object */, 
							actionLog /* dojox.off.sync.ActionLog */){ /* void */
			// summary:
			//	Called when we replay our log, for each of our action
			//	entries.
			// action: Object
			//	A custom object literal representing an action for this
			//	application, such as 
			//	{actionName: "create", item: {title: "message", content: "hello world"}}
			// actionLog: dojox.off.sync.ActionLog
			//	A reference to the dojox.off.sync.actions log so that developers
			//	can easily call actionLog.continueReplay() or actionLog.haltReplay().
			// description:
			//	This callback should be connected to by applications so that
			//	they can sync themselves when we go back online:
			//
			//		dojo.connect(dojox.off.sync.actions, "onReplay", function(action, actionLog){
			//				// do something
			//		})
			//
			//	When we replay our action log, this callback is called for each
			//	of our action entries in the order they were added. The 
			//	'action' entry that was passed to add() for this action will 
			//	also be passed in to onReplay, so that applications can use this information
			//	to do their syncing, such as contacting a server web-service
			//	to create a new item, for example. 
			// 
			//	Inside the method you connected to onReplay, you should either call
			//	actionLog.haltReplay(reason) if an error occurred and you would like to halt
			//	action replaying or actionLog.continueReplay() to have the action log
			//	continue replaying its log and proceed to the next action; 
			//	the reason you must call these is the action you execute inside of 
			//	onAction will probably be asynchronous, since it will be talking on 
			//	the network, and you should call one of these two methods based on 
			//	the result of your network call.
		},
		
		length: function(){ /* Number */
			// summary:
			//	Returns the length of this 
			//	action log
			return this.entries.length;
		},
		
		haltReplay: function(reason /* String */){ /* void */
			// summary: Halts replaying this command log.
			// reason: String
			//		The reason we halted.
			// description:
			//		This method is called as we are replaying an action log; it
			//		can be called from dojox.off.sync.actions.onReplay, for
			//		example, for an application to indicate an error occurred
			//		while replaying this action, halting further processing of
			//		the action log. Note that any action log entries that
			//		were processed before have their effects retained (i.e.
			//		they are not rolled back), while the action entry that was
			//		halted stays in our list of actions to later be replayed.	
			if(!this.isReplaying){
				return;
			}
			
			if(reason){
				this.reasonHalted = reason.toString();		
			}
			
			// save the state of our action log, then
			// tell anyone who is interested that we are
			// done when we are finished saving
			if(this.autoSave){
				var self = this;
				this._save(function(){
					self.isReplaying = false;
					self.onReplayFinished();
				});
			}else{
				this.isReplaying = false;
				this.onReplayFinished();
			}
		},
		
		continueReplay: function(){ /* void */
			// summary:
			//		Indicates that we should continue processing out list of
			//		actions.
			// description:
			//		This method is called by applications that have overridden
			//		dojox.off.sync.actions.onReplay() to continue replaying our 
			//		action log after the application has finished handling the 
			//		current action.
			if(!this.isReplaying){
				return;
			}
			
			// shift off the old action we just ran
			this.entries.shift();
			
			// are we done?
			if(!this.entries.length){
				// save the state of our action log, then
				// tell anyone who is interested that we are
				// done when we are finished saving
				if(this.autoSave){
					var self = this;
					this._save(function(){
						self.isReplaying = false;
						self.onReplayFinished();
					});
					return;
				}else{
					this.isReplaying = false;
					this.onReplayFinished();
					return;
				}
			}
			
			// get the next action
			var nextAction = this.entries[0];
			this.onReplay(nextAction, this);
		},
		
		clear: function(){ /* void */
			// summary:
			//	Completely clears this action log of its entries
			
			if(this.isReplaying){
				return;
			}
			
			this.entries = [];
			
			// save our updated state into persistent
			// storage
			if(this.autoSave){
				this._save();
			}
		},
		
		replay: function(){ /* void */
			// summary:
			//	For advanced usage; most developers can ignore this.
			//	Replays all of the commands that have been
			//	cached in this command log when we go back online;
			//	onCommand will be called for each command we have
			
			if(this.isReplaying){
				return;
			}
			
			this.reasonHalted = null;
			
			if(!this.entries.length){
				this.onReplayFinished();
				return;
			}
			
			this.isReplaying = true;
			
			var nextAction = this.entries[0];
			this.onReplay(nextAction, this);
		},
		
		// onReplayFinished: Function
		//	For advanced usage; most developers can ignore this.
		//	Called when we are finished replaying our commands;
		//	called if we have successfully exhausted all of our
		//	commands, or if an error occurred during replaying.
		//	The default implementation simply continues the
		//	synchronization process. Connect to this to register
		//	for the event:
		//
		//		dojo.connect(dojox.off.sync.actions, "onReplayFinished", 
		//					someFunc)
		onReplayFinished: function(){
		},

		toString: function(){
			var results = "";
			results += "[";
			
			for(var i = 0; i < this.entries.length; i++){
				results += "{";
				for(var j in this.entries[i]){
					results += j + ": \"" + this.entries[i][j] + "\"";
					results += ", ";
				}
				results += "}, ";
			}
			
			results += "]";
			
			return results;
		},
		
		_save: function(callback){
			if(!callback){
				callback = function(){};
			}
			
			try{
				var self = this;
				var resultsHandler = function(status, key, message){
					//console.debug("resultsHandler, status="+status+", key="+key+", message="+message);
					if(status == dojox.storage.FAILED){
						dojox.off.onFrameworkEvent("save", 
											{status: dojox.storage.FAILED,
											isCoreSave: true,
											key: key,
											value: message,
											namespace: dojox.off.STORAGE_NAMESPACE});
						callback();
					}else if(status == dojox.storage.SUCCESS){
						callback();
					}
				};
				
				dojox.storage.put("actionlog", this.entries, resultsHandler,
									dojox.off.STORAGE_NAMESPACE);
			}catch(exp){
				console.debug("dojox.off.sync._save: " + exp.message||exp);
				dojox.off.onFrameworkEvent("save",
							{status: dojox.storage.FAILED,
							isCoreSave: true,
							key: "actionlog",
							value: this.entries,
							namespace: dojox.off.STORAGE_NAMESPACE});
				callback();
			}
		},
		
		_load: function(callback){
			var entries = dojox.storage.get("actionlog", dojox.off.STORAGE_NAMESPACE);
			
			if(!entries){
				entries = [];
			}
			
			this.entries = entries;
			
			callback();
		}
	}
);

dojox.off.sync.actions = new dojox.off.sync.ActionLog();

}
