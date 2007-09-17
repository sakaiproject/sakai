dojo.require("dijit._editor.RichText");
dojo.require("dojo.parser");

// configure how we should work offline

// set our application name
dojox.off.ui.appName = "Moxie";

// automatically "slurp" the page and
// capture the resources we need offline
dojox.off.files.slurp();

var moxie = {
	_availableKeys: null,
	_documents: null,

	initialize: function(){
		//console.debug("moxie.initialize");
		
		// make sure the rich text control is finished
		// loading; workaround for bug 3395
		var richTextControl = dijit.byId("storageValue");
		if(!richTextControl || !richTextControl.isLoaded){
			dojo.connect(richTextControl, "onLoad", this, "initialize");
			return;
		}
		
		// clear out old values
		dojo.byId("storageKey").value = "";
		richTextControl.setValue("Click Here to Begin Editing");
		
		// initialize our event handlers
		var directory = dojo.byId("directory");
		dojo.connect(directory, "onchange", this, this.directoryChange);
		dojo.connect(dojo.byId("saveButton"), "onclick", this, this.save);
		
		// create our database
		this._createDb();
		
		// load and write out our available keys
		this._loadKeys();
		
		// setup our offline handlers
		this._initOfflineHandlers();
		
		this._initialized = true;
	},
	
	directoryChange: function(evt){
		var key = evt.target.value;
		
		// add this value into the form
		var keyNameField = dojo.byId("storageKey");
		keyNameField.value = key;
		
		// if blank key ignore
		if(key == ""){ return; }
		
		this._load(key);		
	},
	
	save: function(evt){
		// cancel the button's default behavior
		evt.preventDefault();
		evt.stopPropagation();
		
		// get the new values
		var key = dojo.byId("storageKey").value;
		var richTextControl = dijit.byId("storageValue");
		var value = richTextControl.getValue();
		
		if(!key){
			alert("Please enter a file name");
			return;
		}
		
		if(!value){
			alert("Please enter file contents");
			return;
		}
		
		// do the save
		this._save(key, value)
	},
	
	_save: function(key, value){
		this._printStatus("Saving '" + key + "'...");
		
		if(dojox.off.isOnline){
			this._saveOnline(key, value);
		}else{
			this._saveOffline(key, value);
		}
	},
	
	_saveOnline: function(key, value){
		// dispatch the request
		dojo.xhrPost({
			url:	 "/moxie/" + encodeURIComponent(key),
			content:	{ "content": value },
			error:		function(err){
				//console.debug("error, err="+err);
				var msg = "Unable to save file " + key + ": " + err;
				if(!dojox.off.sync.actions.isReplaying){
					alert(msg);
				}else{
					dojox.off.sync.actions.haltReplay(msg);
				}
			},
			load:		dojo.hitch(this, function(data){
				//console.debug("load, data="+data);	
				this._printStatus("Saved '" + key + "'");
			
				// add to our list of available keys
				this._addKey(key);
			
				if(!dojox.off.sync.actions.isReplaying){
					// update the list of available keys
					this._printAvailableKeys();
				}else{
					dojox.off.sync.actions.continueReplay();	
				}
			})
		});	
	},
	
	_saveOffline: function(key, value){
		// create an action object to capture this action
		var action = {name: "save", key: key, value: value};
		
		// save it in our action log for replaying when we 
		// go back online
		dojox.off.sync.actions.add(action);
		
		// also add it to our offline, downloaded data
		
		// do an update if this fileName is already in use
		if(dojox.sql("SELECT * FROM DOCUMENTS WHERE fileName = ?", key).length){
			dojox.sql("UPDATE DOCUMENTS SET content = ? WHERE fileName = ?",
						value, key);
			for(var i = 0; i < this._documents.length; i++){
				if(this._documents[i].fileName == key){
					this._documents[i].content = value;
					break;
				}
			}
		}else{
			dojox.sql("INSERT INTO DOCUMENTS (fileName, content) VALUES (?, ?)",
							key, value);
			this._documents.push({fileName: key, content: value});
		}
						
		// update our UI
		this._printStatus("Saved '" + key + "'");
		this._addKey(key);
		this._printAvailableKeys();
	},
	
	_loadKeys: function(){
		if(dojox.off.isOnline){
			this._loadKeysOnline();
		}else{
			this._loadKeysOffline();
		}
	},
	
	_loadKeysOnline: function(){
		var self = this;
		var url = "/moxie/*"
					+ "?browserbust=" + new Date().getTime()
					+ "&proxybust=" + new Date().getTime();
		var bindArgs = {
			url:	 url,
			handleAs:	"javascript",
			headers:	{ "Accept" : "text/javascript" },
			error:		function(err){
				//console.debug("error, err="+err);
				err = err.message||err;
				alert("Unable to load our list of available keys from "
						+ "the server: " + err);
			},
			load:		function(data){
				//console.debug("load, data="+data);	
				// 'data' is a JSON array, where each entry is a String filename
				// of the available keys
				self._availableKeys = data;
				self._printAvailableKeys();
			}
		};
		
		// dispatch the request
		dojo.xhrGet(bindArgs);	
	},
	
	_loadKeysOffline: function(){
		this._loadDownloadedData();
		this._printAvailableKeys();
	},
	
	_printAvailableKeys: function(){
		var directory = dojo.byId("directory");
		
		// clear out any old keys
		directory.innerHTML = "";
		
		// add a blank selection
		var optionNode = document.createElement("option");
		optionNode.appendChild(document.createTextNode(""));
		optionNode.value = "";
		directory.appendChild(optionNode);
		
		// sort our available keys alphabetically
		var keys = this._availableKeys.slice().sort();
		
		// add new ones
		for (var i = 0; i < keys.length; i++) {
			var optionNode = document.createElement("option");
			optionNode.appendChild(document.createTextNode(keys[i]));
			optionNode.value = keys[i];
			directory.appendChild(optionNode);
		}
	},
	
	_addKey: function(key){
		var alreadyPresent = false;
		for(var i = 0; i < this._availableKeys.length; i++){
			if(this._availableKeys[i] == key){
				alreadyPresent = true;
				break;
			}	
		}	
		
		if(!alreadyPresent){
			this._availableKeys.push(key);
		}
	},
	
	_load: function(key){
		this._printStatus("Loading '" + key + "'...");
		
		if(dojox.off.isOnline){
			this._loadOnline(key);
		}else{
			this._loadOffline(key);
		}
	},
	
	_loadOnline: function(key){
		// get the value from the server
		var self = this;
		// add 'cachebust' to the URL to make sure we get a fresh
		// copy that is not returned from either the browser's cache
		// or the local offline proxy's cache
		var url = "/moxie/" + encodeURIComponent(key) 
					+ "?cachebust=" + new Date().getTime(); 
		var bindArgs = {
			url:	 url,
			handleAs:	"text",
			error:		function(err){
				//console.debug("error, err="+err);
				err = err.message||err;
				alert("The file " + key + " is not available: "
						+ err);
			},
			load:		function(data){	
				self._updateEditorContents(data);
			
				// print out that we are done
				self._printStatus("Loaded '" + key + "'");
			}
		};
		
		// dispatch the request
		dojo.xhrGet(bindArgs);	
	},
	
	_loadOffline: function(key){
		var doc = null;
		for(var i = 0; i < this._documents.length; i++){
			var currentDoc = this._documents[i];
			if(currentDoc.fileName == key){
				doc = currentDoc;
				break;
			}
		}
		this._updateEditorContents(doc.content);
	},
	
	_updateEditorContents: function(contents){
		// set the new Editor widget value
		var richTextControl = dijit.byId("storageValue");
		richTextControl.setValue(contents);
	},
	
	_printStatus: function(message){
		// remove the old status
		var top = dojo.byId("top");
		for (var i = 0; i < top.childNodes.length; i++) {
			var currentNode = top.childNodes[i];
			if (currentNode.nodeType == 1 &&
					currentNode.className == "status") {
				top.removeChild(currentNode);
			}		
		}
		
		var status = document.createElement("span");
		status.className = "status";
		status.innerHTML = message;
		
		top.appendChild(status);
		dojo.fadeOut({ node: status, duration: 2000 }).play();
	},
	
	_initOfflineHandlers: function(){
		// setup what we do when we are replaying our action
		// log when the network reappears
		dojo.connect(dojox.off.sync.actions, "onReplay", this, function(action, actionLog){
			if(action.name == "save"){
				this._save(action.key, action.value);
			}
		});
		
		// handle syncing
		dojo.connect(dojox.off.sync, "onSync", this, function(type){
			// setup how we download our data from the server
			if(type == "download"){
				this._downloadData();
			}else if(type == "finished"){
				// refresh our UI when we are finished syncing
				this._printAvailableKeys();
			}
		});
	},
	
	_downloadData: function(){
		var self = this;
		
		// add 'cachebust' to the URL to make sure we get a fresh
		// copy that is not returned from either the browser's cache
		// or the local offline proxy's cache
		var bindArgs = {
			url:	 "/moxie/download?cachebust=" + new Date().getTime(),
			handleAs:	"javascript",
			headers:	{ "Accept" : "text/javascript" },
			error:		function(err){
				//console.debug("moxie._downloadData.error, err="+err);
				err = err.message||err;
				var message = "Unable to download our documents from server: "
								+ err;
				dojox.off.sync.finishedDownloading(false, message);
			},
			load:		function(data){
				//console.debug("moxie._downloadData.load");
				self._saveDownloadedData(data);
			}
		};
		
		// dispatch the request
		dojo.xhrGet(bindArgs);	
	},
	
	_saveDownloadedData: function(data){
		// 'data' is a JSON structure passed to us by the server
		// that is an array of object literals, where each literal
		// has a 'fileName' entry and a 'content' entry.
		dojox.sql("DROP TABLE IF EXISTS DOCUMENTS");
		
		this._createDb();
					
		dojo.forEach(data, function(record){
			dojox.sql("INSERT INTO DOCUMENTS (fileName, content) VALUES (?, ?)",
						record.fileName, record.content);
		});
		
		this._documents = data;
		
		dojox.off.sync.finishedDownloading(true, null);
	},
	
	_loadDownloadedData: function(){
		this._availableKeys = [];
		this._documents = dojox.sql("SELECT * FROM DOCUMENTS");
		if(!this._documents){ this._documents = []; }
		
		for(var i = 0; i < this._documents.length; i++){
			var fileName = this._documents[i].fileName;
			this._availableKeys.push(fileName);
		}
	},
	
	_createDb: function(){
		dojox.sql("CREATE TABLE IF NOT EXISTS DOCUMENTS ("
					+ "fileName		TEXT NOT NULL PRIMARY KEY UNIQUE, "
					+ "content		TEXT NOT NULL) ");
	}
};

// wait until Dojo Offline and the default Offline Widget are ready
// before we initialize ourselves. When this gets called the page
// is also finished loading.
dojo.connect(dojox.off.ui, "onLoad", moxie, moxie.initialize);

// tell Dojo Offline we are ready for it to initialize itself now
// that we have finished configuring it for our application
dojox.off.initialize();
