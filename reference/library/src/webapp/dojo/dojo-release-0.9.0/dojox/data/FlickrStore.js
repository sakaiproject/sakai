if(!dojo._hasResource["dojox.data.FlickrStore"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.data.FlickrStore"] = true;
dojo.provide("dojox.data.FlickrStore");

dojo.require("dojo.data.util.simpleFetch");
dojo.require("dojo.io.script");
dojo.require("dojo.date.stamp");

dojo.declare("dojox.data.FlickrStore", null, {
	constructor: function(/*Object*/args){
		//	summary:
		//		Initializer for the FlickrStore store.  
		//	description:
		//		The FlickrStore is a Datastore interface to one of the basic services
		//		of the Flickr service, the public photo feed.  This does not provide
		//		access to all the services of Flickr.
		//		This store cannot do * and ? filtering as the flickr service 
		//		provides no interface for wildcards.
		if(args && args.label){
			this.label = args.label;
		}
	},

	_flickrUrl: "http://api.flickr.com/services/feeds/photos_public.gne",

	_storeRef: "_S",

	label: "title",

	_assertIsItem: function(/* item */ item){
		//	summary:
		//      This function tests whether the item passed in is indeed an item in the store.
		//	item: 
		//		The item to test for being contained by the store.
		if(!this.isItem(item)){ 
			throw new Error("dojox.data.FlickrStore: a function was passed an item argument that was not an item");
		}
	},

	_assertIsAttribute: function(/* attribute-name-string */ attribute){
		//	summary:
		//		This function tests whether the item passed in is indeed a valid 'attribute' like type for the store.
		//	attribute: 
		//		The attribute to test for being contained by the store.
		if(typeof attribute !== "string"){ 
			throw new Error("dojox.data.FlickrStore: a function was passed an attribute argument that was not an attribute name string");
		}
	},

	getFeatures: function(){
		//	summary: 
		//      See dojo.data.api.Read.getFeatures()
		return {
			'dojo.data.api.Read': true
		};
	},

	getValue: function(item, attribute){
		//	summary: 
		//      See dojo.data.api.Read.getValue()
		var values = this.getValues(item, attribute);
		if(values){
			return values[0];
		}
		return undefined;
	},

	getAttributes: function(item){
		//	summary: 
		//      See dojo.data.api.Read.getAttributes()
		return ["title", "description", "author", "datePublished", "dateTaken", "imageUrl", "imageUrlSmall", "imageUrlMedium", "tags", "link"]; 
	},

	hasAttribute: function(item, attribute){
		//	summary: 
		//      See dojo.data.api.Read.hasAttributes()
		if(this.getValue(item,attribute)){
			return true;
		}
		return false;
	},

	isItemLoaded: function(item){
		 //	summary: 
		 //      See dojo.data.api.Read.isItemLoaded()
		 return this.isItem(item);
	},

	loadItem: function(keywordArgs){
		//	summary: 
		//      See dojo.data.api.Read.loadItem()
	},

	getLabel: function(item){
		//	summary: 
		//      See dojo.data.api.Read.getLabel()
		return this.getValue(item,this.label);
	},
	
	getLabelAttributes: function(item){
		//	summary: 
		//      See dojo.data.api.Read.getLabelAttributes()
		return [this.label];
	},

	containsValue: function(item, attribute, value){
		//	summary: 
		//      See dojo.data.api.Read.containsValue()
		var values = this.getValues(item,attribute);
		for(var i = 0; i < values.length; i++){
			if(values[i] === value){
				return true;
			}
		}
		return false;
	},

	getValues: function(item, attribute){
		//	summary: 
		//      See dojo.data.api.Read.getValue()

		this._assertIsItem(item);
		this._assertIsAttribute(attribute);
		if(attribute === "title"){
			return [this._unescapeHtml(item.title)];
		}else if(attribute === "author"){
			return [this._unescapeHtml(item.author)];
		}else if(attribute === "datePublished"){
			return [dojo.date.stamp.fromISOString(item.published)];
		}else if(attribute === "dateTaken"){
			return [dojo.date.stamp.fromISOString(item.date_taken)];
		}else if(attribute === "imageUrlSmall"){
			return [item.media.m.replace(/_m\./, "_s.")];
		}else if(attribute === "imageUrl"){
			return [item.media.m.replace(/_m\./, ".")];
		}else if(attribute === "imageUrlMedium"){
			return [item.media.m];
		}else if(attribute === "link"){
			return [item.link];
		}else if(attribute === "tags"){
			return item.tags.split(" ");
		}else if(attribute === "description"){
			return [this._unescapeHtml(item.description)];
		}
		return undefined;
	},

	isItem: function(item){
		//	summary: 
		//      See dojo.data.api.Read.isItem()
		if(item && item[this._storeRef] === this){
			return true;
		}
		return false;
	},
	
	close: function(request){
		//	summary: 
		//      See dojo.data.api.Read.close()
	},

	_fetchItems: function(request, fetchHandler, errorHandler){
		//	summary:
		//		Fetch flickr items that match to a query
		//	request:
		//		A request object
		//	fetchHandler:
		//		A function to call for fetched items
		//	errorHandler:
		//		A function to call on error

		if(!request.query){
			request.query={};
		}

		//Build up the content to send the request for.
		var content = {format: "json", tagmode:"any"};
		if (request.query.tags) {
			content.tags = request.query.tags;
		}
		if (request.query.tagmode) {
			content.tagmode = request.query.tagmode;
		}
		if (request.query.userid) {
			content.id = request.query.userid;
		}
		if (request.query.userids) {
			content.ids = request.query.userids;
		}
		if (request.query.lang) {
			content.lang = request.query.lang;
		}

		//Linking this up to Flickr is a PAIN!
		var self = this;
		var handle = null;
		var getArgs = {
			url: this._flickrUrl,
			preventCache: true,
			content: content
		};
		var myHandler = function(data){
			if(handle !== null){
				dojo.disconnect(handle);
			}

			//Process the items...
			fetchHandler(self._processFlickrData(data), request);
		};
        handle = dojo.connect("jsonFlickrFeed", myHandler);
		var deferred = dojo.io.script.get(getArgs);
		
		//We only set up the errback, because the callback isn't ever really used because we have
		//to link to the jsonFlickrFeed function....
		deferred.addErrback(function(error){
			dojo.disconnect(handle);
			errorHandler(error, request);
		});
	},

	_processFlickrData: function(data){
		 var items = [];
		 if(data.items){
			 items = data.items;
			 //Add on the store ref so that isItem can work.
			 for(var i = 0; i < data.items.length; i++){
				 var item = data.items[i];
				 item[this._storeRef] = this;
			 }
		 }
		 return items;
	},

	_unescapeHtml: function(str){
		// summary: Utility function to un-escape XML special characters in an HTML string.
		// description: Utility function to un-escape XML special characters in an HTML string.
		//
		// str: String.
		//   The string to un-escape
		// returns: HTML String converted back to the normal text (unescaped) characters (<,>,&, ", etc,).
		//
		//TODO: Check to see if theres already compatible escape() in dojo.string or dojo.html
		str = str.replace(/&amp;/gm, "&").replace(/&lt;/gm, "<").replace(/&gt;/gm, ">").replace(/&quot;/gm, "\"");
		str = str.replace(/&#39;/gm, "'"); 
		return str;
	}
});
dojo.extend(dojox.data.FlickrStore,dojo.data.util.simpleFetch);										  
										  
//We have to define this because of how the Flickr API works.  
//This somewhat stinks, but what can you do?
if (!jsonFlickrFeed) {
	var jsonFlickrFeed = function(data){};
}


}
