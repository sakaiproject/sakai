if(!dojo._hasResource["dojox.data.XmlStore"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.data.XmlStore"] = true;
dojo.provide("dojox.data.XmlStore");
dojo.provide("dojox.data.XmlItem");

dojo.require("dojo.data.util.simpleFetch");
dojo.require("dojo.data.util.filter");
dojo.require("dojox.data.dom");

dojo.declare("dojox.data.XmlStore", null, {
	//	summary:
	//		A data store for XML based services or documents
	//	description:
	//		A data store for XML based services or documents
	
	constructor: function(/* object */ args) {
		//	summary:
		//		Constructor for the XML store.  
		//	args:
		//		An anonymous object to initialize properties.  It expects the following values:
		//		url:		The url to a service or an XML document that represents the store
		//		rootItem:	A tag name for root items
		//		keyAttribute:	An attribute name for a key or an indentify
		// 		attributeMap:   An anonymous object contains properties for attribute mapping,
		//						{"tag_name.item_attribute_name": "@xml_attribute_name", ...}
		//		sendQuery:		A boolean indicate to add a query string to the service URL 
		console.log("XmlStore()");
		if(args){
			this._url = args.url;
			this._rootItem = (args.rootItem || args.rootitem);
			this._keyAttribute = (args.keyAttribute || args.keyattribute);
			this._attributeMap = (args.attributeMap || args.attributemap);
			this._labelAttr = args.label;
			this._sendQuery = (args.sendQuery || args.sendquery);
		}
		this._newItems = [];
		this._deletedItems = [];
		this._modifiedItems = [];
	},

/* dojo.data.api.Read */

	getValue: function(/* item */ item, /* attribute || attribute-name-string */ attribute, /* value? */ defaultValue){
		//	summary:
		//		Return an attribute value
		//	description:
		//		'item' must be an XML element.
		//		If 'attribute' specifies "tagName", the tag name of the element is
		//		returned.
		//		If 'attribute' specifies "childNodes", the first element child is
		//		returned.
		//		If 'attribute' specifies "text()", the value of the first text
		//		child is returned.
		//		For generic attributes, if '_attributeMap' is specified,
		//		an actual attribute name is looked up with the tag name of
		//		the element and 'attribute' (concatenated with '.').
		//		Then, if 'attribute' starts with "@", the value of the XML
		//		attribute is returned.
		//		Otherwise, the first child element of the tag name specified with
		//		'attribute' is returned.
		//	item:
		//		An XML element that holds the attribute
		//	attribute:
		//		A tag name of a child element, An XML attribute name or one of
		// 		special names
		//	defaultValue:
		//		A default value
		//	returns:
		//		An attribute value found, otherwise 'defaultValue'
		var element = item.element;
		if(attribute === "tagName"){
			return element.nodeName;
		}else if (attribute === "childNodes"){
			for (var i = 0; i < element.childNodes.length; i++) {
				var node = element.childNodes[i];
				if (node.nodeType === 1 /*ELEMENT_NODE*/) {
					return this._getItem(node); //object
				}
			}
			return defaultValue;
		}else if(attribute === "text()"){
			for(var i = 0; i < element.childNodes.length; i++){
				var node = element.childNodes[i];
				if(node.nodeType === 3 /*TEXT_NODE*/ ||
					node.nodeType === 4 /*CDATA_SECTION_NODE*/){
					return node.nodeValue; //string
				}
			}
			return defaultValue;
		}else{
			attribute = this._getAttribute(element.nodeName, attribute);
			if(attribute.charAt(0) === '@'){
				var name = attribute.substring(1);
				var value = element.getAttribute(name);
				return (value !== undefined) ? value : defaultValue; //object
			}else{
				for(var i = 0; i < element.childNodes.length; i++){
					var node = element.childNodes[i];
					if(	node.nodeType === 1 /*ELEMENT_NODE*/ &&
						node.nodeName === attribute){
						return this._getItem(node); //object
					}
				}
				return defaultValue; //object
			}
		}
	},

	getValues: function(/* item */ item, /* attribute || attribute-name-string */ attribute){
		//	summary:
		//		Return an array of attribute values
		//	description:
		//		'item' must be an XML element.
		//		If 'attribute' specifies "tagName", the tag name of the element is
		//		returned.
		//		If 'attribute' specifies "childNodes", child elements are returned.
		//		If 'attribute' specifies "text()", the values of child text nodes
		//		are returned.
		//		For generic attributes, if '_attributeMap' is specified,
		//		an actual attribute name is looked up with the tag name of
		//		the element and 'attribute' (concatenated with '.').
		//		Then, if 'attribute' starts with "@", the value of the XML
		//		attribute is returned.
		//		Otherwise, child elements of the tag name specified with
		//		'attribute' are returned.
		//	item:
		//		An XML element that holds the attribute
		//	attribute:
		//		A tag name of child elements, An XML attribute name or one of
		//		special names
		//	returns:
		//		An array of attribute values found, otherwise an empty array
		var element = item.element;
		if(attribute === "tagName"){
			return [element.nodeName];
		}else if(attribute === "childNodes"){
			var values = [];
			for(var i = 0; i < element.childNodes.length; i++){
				var node = element.childNodes[i];
				if(node.nodeType === 1 /*ELEMENT_NODE*/){
					values.push(this._getItem(node));
				}
			}
			return values; //array
		}else if(attribute === "text()"){
			var values = [];
			for(var i = 0; i < element.childNodes.length; i++){
				var node = childNodes[i];
				if(node.nodeType === 3){
					values.push(node.nodeValue);
				}
			}
			return values; //array
		}else{
			attribute = this._getAttribute(element.nodeName, attribute);
			if(attribute.charAt(0) === '@'){
				var name = attribute.substring(1);
				var value = element.getAttribute(name);
				return (value !== undefined) ? [value] : []; //array
			}else{
				var values = [];
				for(var i = 0; i < element.childNodes.length; i++){
					var node = element.childNodes[i];
					if(	node.nodeType === 1 /*ELEMENT_NODE*/ &&
						node.nodeName === attribute){
						values.push(this._getItem(node));
					}
				}
				return values; //array
			}
		}
	},

	getAttributes: function(/* item */ item) {
		//	summary:
		//		Return an array of attribute names
		// 	description:
		//		'item' must be an XML element.
		//		tag names of child elements and XML attribute names of attributes
		//		specified to the element are returned along with special attribute
		//		names applicable to the element including "tagName", "childNodes"
		//		if the element has child elements, "text()" if the element has
		//		child text nodes, and attribute names in '_attributeMap' that match
		//		the tag name of the element.
		//	item:
		//		An XML element
		//	returns:
		//		An array of attributes found
		var element = item.element;
		var attributes = [];
		attributes.push("tagName");
		if(element.childNodes.length > 0){
			var names = {};
			var childNodes = true;
			var text = false;
			for(var i = 0; i < element.childNodes.length; i++){
				var node = element.childNodes[i];
				if (node.nodeType === 1 /*ELEMENT_NODE*/) {
					var name = node.nodeName;
					if(!names[name]){
						attributes.push(name);
						names[name] = name;
					}
					childNodes = true;
				}else if(node.nodeType === 3){
					text = true;
				}
			}
			if(childNodes){
				attributes.push("childNodes");
			}
			if(text){
				attributes.push("text()");
			}
		}
		for(var i = 0; i < element.attributes.length; i++){
			attributes.push("@" + element.attributes[i].nodeName);
		}
		if(this._attributeMap){
			for (var key in this._attributeMap){
				var i = key.indexOf('.');
				if(i > 0){
					var tagName = key.substring(0, i);
					if (tagName === element.nodeName){
						attributes.push(key.substring(i + 1));
					}
				}else{ // global attribute
					attributes.push(key);
				}
			}
		}
		return attributes; //array
	},

	hasAttribute: function(/* item */ item, /* attribute || attribute-name-string */ attribute){
		//	summary:
		//		Check whether an element has the attribute
		//	item:
		//		An XML element
		//	attribute:
		//		A tag name of a child element, An XML attribute name or one of
		//		special names
		//	returns:
		//		True if the element has the attribute, otherwise false
		return (this.getValue(item, attribute) !== undefined); //boolean
	},

	containsValue: function(/* item */ item, /* attribute || attribute-name-string */ attribute, /* anything */ value){
		//	summary:
		//		Check whether the attribute values contain the value
		//	item:
		//		An XML element that holds the attribute
		//	attribute:
		//		A tag name of a child element, An XML attribute name or one of
		//		special names
		//	returns:
		//		True if the attribute values contain the value, otherwise false
		var values = this.getValues(item, attribute);
		for(var i = 0; i < values.length; i++){
			if((typeof value === "string")){
				if(values[i].toString && values[i].toString() === value){
					return true;
				}
			}else if (values[i] === value){
				return true; //boolean
			}
		}
		return false;//boolean
	},

	isItem: function(/* anything */ something){
		//	summary:
		//		Check whether the object is an item (XML element)
		//	item:
		//		An object to check
		// 	returns:
		//		True if the object is an XML element, otherwise false
		if(something && something.element && something.store && something.store === this){
			return true; //boolean
		}
		return false; //boolran
	},

	isItemLoaded: function(/* anything */ something){
		//	summary:
		//		Check whether the object is an item (XML element) and loaded
		//	item:
		//		An object to check
		//	returns:
		//		True if the object is an XML element, otherwise false
		return this.isItem(something); //boolean
	},

	loadItem: function(/* object */ keywordArgs){
		//	summary:
		//		Load an item (XML element)
		//	keywordArgs:
		//		object containing the args for loadItem.  See dojo.data.api.Read.loadItem()
	},

	getFeatures: function() {
		//	summary:
		//		Return supported data APIs
		//	returns:
		//		"dojo.data.api.Read" and "dojo.data.api.Write"
		var features = {
			"dojo.data.api.Read": true,
			"dojo.data.api.Write": true
		};
		return features; //array
	},

	getLabel: function(/* item */ item){
		//	summary: 
		//		See dojo.data.api.Read.getLabel()
		if(this._labelAttr && this.isItem(item)){
			var label = this.getValue(item,this._labelAttr);
			if(label){
				return label.toString();
			}
		}
		return undefined; //undefined
	},

	getLabelAttributes: function(/* item */ item){
		//	summary: 
		//		See dojo.data.api.Read.getLabelAttributes()
		if(this._labelAttr){
			return [this._labelAttr]; //array
		}
		return null; //null
	},

	_fetchItems: function(request, fetchHandler, errorHandler) {
		//	summary:
		//		Fetch items (XML elements) that match to a query
		//	description:
		//		If '_sendQuery' is true, an XML document is loaded from
		//		'_url' with a query string.
		//		Otherwise, an XML document is loaded and list XML elements that
		//		match to a query (set of element names and their text attribute
		//		values that the items to contain).
		//		A wildcard, "*" can be used to query values to match all
		//		occurrences.
		//		If '_rootItem' is specified, it is used to fetch items.
		//	request:
		//		A request object
		//	fetchHandler:
		//		A function to call for fetched items
		//	errorHandler:
		//		A function to call on error
		var url = this._getFetchUrl(request);
		console.log("XmlStore._fetchItems(): url=" + url);
		if(!url){
			errorHandler(new Error("No URL specified."));
			return;
		}
		var localRequest = (!this._sendQuery ? request : null); // use request for _getItems()

		var self = this;
		var getArgs = {
				url: url,
				handleAs: "xml",
				preventCache: true
			};
		var getHandler = dojo.xhrGet(getArgs);
		getHandler.addCallback(function(data){
			var items = self._getItems(data, localRequest);
			console.log("XmlStore._fetchItems(): length=" + (items ? items.length : 0));
			if (items && items.length > 0) {
				fetchHandler(items, request);
			}
			else {
				fetchHandler([], request);
			}
		});
		getHandler.addErrback(function(data){
			errorHandler(data, request);
		});
	},

	_getFetchUrl: function(request){
		//	summary:
		//		Generate a URL for fetch
		//	description:
		//		This default implementation generates a query string in the form of
		//		"?name1=value1&name2=value2..." off properties of 'query' object
		//		specified in 'request' and appends it to '_url', if '_sendQuery'
		//		is set to false.
		//		Otherwise, '_url' is returned as is.
		//		Sub-classes may override this method for the custom URL generation.
		//	request:
		//		A request object
		//	returns:
		//		A fetch URL
		if(!this._sendQuery){
			return this._url;
		}
		var query = request.query;
		if(!query){
			return this._url;
		}
		if(dojo.isString(query)){
			return this._url + query;
		}
		var queryString = "";
		for(var name in query){
			var value = query[name];
			if(value){
				if(queryString){
					queryString += "&";
				}
				queryString += (name + "=" + value);
			}
		}
		if(!queryString){
			return this._url;
		}
		return this._url + "?" + queryString;
	},

	_getItems: function(document, request) {
		//	summary:
		//		Fetch items (XML elements) in an XML document based on a request
		//	description:
		//		This default implementation walks through child elements of
		//		the document element to see if all properties of 'query' object
		//		match corresponding attributes of the element (item).
		//		If 'request' is not specified, all child elements are returned.
		//		Sub-classes may override this method for the custom search in
		//		an XML document.
		//	document:
		//		An XML document
		//	request:
		//		A request object
		//	returns:
		//		An array of items
		var query = null;
		if(request){
			query = request.query;
		}
		var items = [];
		var nodes = null;
		if(this._rootItem){
			nodes = document.getElementsByTagName(this._rootItem);
		}
		else{
			nodes = document.documentElement.childNodes;
		}
		for(var i = 0; i < nodes.length; i++){
			var node = nodes[i];
			if(node.nodeType != 1 /*ELEMENT_NODE*/){
				continue;
			}
            var item = this._getItem(node);
			if(query){
				var found = true;
				var ignoreCase = request.queryOptions ? request.queryOptions.ignoreCase : false; 

				//See if there are any string values that can be regexp parsed first to avoid multiple regexp gens on the
				//same value for each item examined.  Much more efficient.
				var regexpList = {};
				for(var key in query){
					var value = query[key];
					if(typeof value === "string"){
						regexpList[key] = dojo.data.util.filter.patternToRegExp(value, ignoreCase);
					}
				}

				for(var attribute in query){
					var value = this.getValue(item, attribute);
					if(value){
						var queryValue = query[attribute];
						if ((typeof value) === "string" && 
							(regexpList[attribute])){
							if((value.match(regexpList[attribute])) !== null){
								continue;
							}
						}else if((typeof value) === "object"){
							if(	value.toString && 
								(regexpList[attribute])){
								var stringValue = value.toString();
								if((stringValue.match(regexpList[attribute])) !== null){
									continue;
								}
							}else{
								if(queryValue === "*" || queryValue === value){
									continue;
								}
							}
						}
					}
					found = false;
					break;
				}
				if(!found){
					continue;
				}
			}
			items.push(item);
		}
		for(var i in items){
			var element = items[i].element;
			element.parentNode.removeChild(element); // make it root
		}
		return items;
	},

	close: function(/*dojo.data.api.Request || keywordArgs || null */ request){
		 //	summary: 
		 //		See dojo.data.api.Read.close()
	},

/* dojo.data.api.Write */

	newItem: function(/* object? */ keywordArgs){
		//	summary:
		//		Return a new XML element
		//	description:
		//		At least, 'keywordArgs' must contain "tagName" to be used for
		//		the new	element.
		//		Other attributes in 'keywordArgs' are set to the new element,
		//		including "text()", but excluding "childNodes".
		// 	keywordArgs:
		//		An object containing initial attributes
		//	returns:
		//		An XML element
		console.log("XmlStore.newItem()");
		keywordArgs = (keywordArgs || {});
		var tagName = keywordArgs.tagName;
		if(!tagName){
			tagName = this._rootItem;
			if(!tagName){
				return null;
			}
		}

		var document = this._getDocument();
		var element = document.createElement(tagName);
		for(var attribute in keywordArgs){
			if(attribute === "tagName"){
				continue;
			}else if(attribute === "text()"){
				var text = document.createTextNode(keywordArgs[attribute]);
				element.appendChild(text);
			}else{
				attribute = this._getAttribute(tagName, attribute);
				if(attribute.charAt(0) === '@'){
					var name = attribute.substring(1);
					element.setAttribute(name, keywordArgs[attribute]);
				}else{
					var child = document.createElement(attribute);
					var text = document.createTextNode(keywordArgs[attribute]);
					child.appendChild(text);
					element.appendChild(child);
				}
			}
		}

		var item = this._getItem(element);
		this._newItems.push(item);
		return item; //object
	},
	
	deleteItem: function(/* item */ item){
		//	summary:
		//		Delete an XML element
		//	item:
		//		An XML element to delete
		//	returns:
		//		True
		console.log("XmlStore.deleteItem()");
		var element = item.element;
		if(element.parentNode){
			this._backupItem(item);
			element.parentNode.removeChild(element);
			return true;
		}
		this._forgetItem(item);
		this._deletedItems.push(item);
		return true; //boolean
	},
	
	setValue: function(/* item */ item, /* attribute || string */ attribute, /* almost anything */ value){
		//	summary:
		//		Set an attribute value
		//	description:
		//		'item' must be an XML element.
		//		If 'attribute' specifies "tagName", nothing is set and false is
		//		returned.
		//		If 'attribute' specifies "childNodes", the value (XML element) is
		//		added to the element.
		//		If 'attribute' specifies "text()", a text node is created with
		//		the value and set it to the element as a child.
		//		For generic attributes, if '_attributeMap' is specified,
		//		an actual attribute name is looked up with the tag name of
		//		the element and 'attribute' (concatenated with '.').
		//		Then, if 'attribute' starts with "@", the value is set to the XML
		//		attribute.
		//		Otherwise, a text node is created with the value and set it to
		//		the first child element of the tag name specified with 'attribute'.
		//		If the child element does not exist, it is created.
		//	item:
		//		An XML element that holds the attribute
		//	attribute:
		//		A tag name of a child element, An XML attribute name or one of
		//		special names
		//	value:
		//		A attribute value to set
		//	returns:
		//		False for "tagName", otherwise true
		if(attribute === "tagName"){
			return false; //boolean
		}

		this._backupItem(item);

		var element = item.element;
		if(attribute === "childNodes"){
			var child = value.element;
			element.appendChild(child);
		}else if(attribute === "text()"){
			while (element.firstChild){
				element.removeChild(element.firstChild);
			}
			var text = this._getDocument(element).createTextNode(value);
			element.appendChild(text);
		}else{
			attribute = this._getAttribute(element.nodeName, attribute);
			if(attribute.charAt(0) === '@'){
				var name = attribute.substring(1);
				element.setAttribute(name, value);
			}else{
				var child = null;
				for(var i = 0; i < element.childNodes.length; i++){
					var node = element.childNodes[i];
					if(	node.nodeType === 1 /*ELEMENT_NODE*/&&
						node.nodeName === attribute){
						child = node;
						break;
					}
				}
				var document = this._getDocument(element);
				if(child){
					while(child.firstChild){
						child.removeChild(child.firstChild);
					}
				}else{
					child = document.createElement(attribute);
					element.appendChild(child);
				}
				var text = document.createTextNode(value);
				child.appendChild(text);
			}
		}
		return true; //boolean
	},
	
	setValues: function(/* item */ item, /* attribute || string */ attribute, /* array */ values){
		//	summary:
		//		Set attribute values
		//	description:
		//		'item' must be an XML element.
		//		If 'attribute' specifies "tagName", nothing is set and false is
		//		returned.
		//		If 'attribute' specifies "childNodes", the value (array of XML
		//		elements) is set to the element's childNodes.
		//		If 'attribute' specifies "text()", a text node is created with
		//		the values and set it to the element as a child.
		//		For generic attributes, if '_attributeMap' is specified,
		//		an actual attribute name is looked up with the tag name of
		//		the element and 'attribute' (concatenated with '.').
		//		Then, if 'attribute' starts with "@", the first value is set to
		//		the XML attribute.
		//		Otherwise, child elements of the tag name specified with
		//		'attribute' are replaced with new child elements and their
		//		child text nodes of values.
		//	item:
		//		An XML element that holds the attribute
		//	attribute:
		//		A tag name of child elements, an XML attribute name or one of
		//		special names
		//	value:
		//		A attribute value to set
		//	returns:
		//		False for "tagName", otherwise true
		if(attribute === "tagName"){
			return false; //boolean
		}

		this._backupItem(item);

		var element = item.element;
		if(attribute === "childNodes"){
			while(element.firstChild){
				element.removeChild(element.firstChild);
			}
			for(var i = 0; i < values.length; i++){
				var child = values[i].element;
				element.appendChild(child);
			}
		}else if(attribute === "text()"){
			while (element.firstChild){
				element.removeChild(element.firstChild);
			}
			var value = "";
			for(var i = 0; i < values.length; i++){
				value += values[i];
			}
			var text = this._getDocument(element).createTextNode(value);
			element.appendChild(text);
		}else{
			attribute = this._getAttribute(element.nodeName, attribute);
			if(attribute.charAt(0) === '@'){
				var name = attribute.substring(1);
				element.setAttribute(name, values[0]);
			}else{
				for(var i = element.childNodes.length - 1; i >= 0; i--){
					var node = element.childNodes[i];
					if(	node.nodeType === 1 /*ELEMENT_NODE*/ &&
						node.nodeName === attribute){
						element.removeChild(node);
					}
				}
				var document = this._getDocument(element);
				for(var i = 0; i < values.length; i++){
					var child = document.createElement(attribute);
					var text = document.createTextNode(values[i]);
					child.appendChild(text);
					element.appendChild(child);
				}
			}
		}
		return true; //boolean
	},
	
	unsetAttribute: function(/* item */ item, /* attribute || string */ attribute){
		//	summary:
		//		Remove an attribute
		//	description:
		//		'item' must be an XML element.
		//		'attribute' can be an XML attribute name of the element or one of
		//		special names described below.
		//		If 'attribute' specifies "tagName", nothing is removed and false is
		//		returned.
		//		If 'attribute' specifies "childNodes" or "text()", all child nodes
		//		are removed.
		//		For generic attributes, if '_attributeMap' is specified,
		//		an actual attribute name is looked up with the tag name of
		//		the element and 'attribute' (concatenated with '.').
		//		Then, if 'attribute' starts with "@", the XML attribute is removed.
		//		Otherwise, child elements of the tag name specified with
		//		'attribute' are removed.
		//	item:
		//		An XML element that holds the attribute
		//	attribute:
		//		A tag name of child elements, an XML attribute name or one of
		//		special names
		//	returns:
		//		False for "tagName", otherwise true
		if(attribute === "tagName"){
			return false; //boolean
		}

		this._backupItem(item);

		var element = item.element;
		if(attribute === "childNodes" || attribute === "text()"){
			while(element.firstChild){
				element.removeChild(element.firstChild);
			}
		}else{
			attribute = this._getAttribute(element.nodeName, attribute);
			if(attribute.charAt(0) === '@'){
				var name = attribute.substring(1);
				element.removeAttribute(name);
			}else{
				for(var i = element.childNodes.length - 1; i >= 0; i--){
					var node = element.childNodes[i];
					if(	node.nodeType === 1 /*ELEMENT_NODE*/ &&
						node.nodeName === attribute){
						element.removeChild(node);
					}
				}
			}
		}
		return true; //boolean
	},
	
	save: function(/* object */ keywordArgs){
		//	summary:
		//		Save new and/or modified items (XML elements)
		// 	description:
		//		'_url' is used to save XML documents for new, modified and/or
		//		deleted XML elements.
		// 	keywordArgs:
		//		An object for callbacks
		if(!keywordArgs){
			keywordArgs = {};
		}
		for(var i in this._modifiedItems){
			this._saveItem(this._modifiedItems[i], keywordArgs, "PUT");
		}
		for(var i = 0; i < this._newItems.length; i++){
			var item = this._newItems[i];
			if(item.element.parentNode){ // reparented
				this._newItems.splice(i, 1);
				i--;
				continue;
			}
			this._saveItem(this._newItems[i], keywordArgs, "POST");
		}
		for(var i in this._deletedItems){
			this._saveItem(this._deletedItems[i], keywordArgs, "DELETE");
		}
	},

	revert: function(){
		// summary:
		//	Invalidate changes (new and/or modified elements)
		// returns:
		//	True
		console.log("XmlStore.revert() _newItems=" + this._newItems.length);
		console.log("XmlStore.revert() _deletedItems=" + this._deletedItems.length);
		console.log("XmlStore.revert() _modifiedItems=" + this._modifiedItems.length);
		this._newItems = [];
		this._restoreItems(this._deletedItems);
		this._deletedItems = [];
		this._restoreItems(this._modifiedItems);
		this._modifiedItems = [];
		return true; //boolean
	},
	
	isDirty: function(/* item? */ item){
		//	summary:
		//		Check whether an item is new, modified or deleted
		//	description:
		//		If 'item' is specified, true is returned if the item is new,
		//		modified or deleted.
		//		Otherwise, true is returned if there are any new, modified
		//		or deleted items.
		//	item:
		//		An item (XML element) to check
		//	returns:
		//		True if an item or items are new, modified or deleted, otherwise
		//		false
		if (item) {
			var element = this._getRootElement(item.element);
			return (this._getItemIndex(this._newItems, element) >= 0 ||
				this._getItemIndex(this._deletedItems, element) >= 0 ||
				this._getItemIndex(this._modifiedItems, element) >= 0); //boolean
		}
		else {
			return (this._newItems.length > 0 ||
				this._deletedItems.length > 0 ||
				this._modifiedItems.length > 0); //boolean
		}
	},

	_saveItem: function(item, keywordArgs, method){
		if(method === "PUT"){
			url = this._getPutUrl(item);
		}else if(method === "DELETE"){
			url = this._getDeleteUrl(item);
		}else{ // POST
			url = this._getPostUrl(item);
		}
		if(!url){
			if(keywordArgs.onError){
				keywordArgs.onError.call(scope, new Error("No URL for saving content: " + postContent));
			}
			return;
		}

		var saveArgs = {
			url: url,
			method: (method || "POST"),
			contentType: "text/xml",
			handleAs: "xml"
		};
		var saveHander;
		if(method === "PUT"){
			saveArgs.putData = this._getPutContent(item);
			saveHandler = dojo.rawXhrPut(saveArgs);
		}else if(method === "DELETE"){
			saveHandler = dojo.xhrDelete(saveArgs);
		}else{ // POST
			saveArgs.postData = this._getPostContent(item);
			saveHandler = dojo.rawXhrPost(saveArgs);
		}
		var scope = (keywordArgs.scope || dojo.global);
		var self = this;
		saveHandler.addCallback(function(data){
			self._forgetItem(item);
			if(keywordArgs.onComplete){
				keywordArgs.onComplete.call(scope);
			}
		});
		saveHandler.addErrback(function(error){
			if(keywordArgs.onError){
				keywordArgs.onError.call(scope, error);
			}
		});
	},

	_getPostUrl: function(item){
		//	summary:
		//		Generate a URL for post
		//	description:
		//		This default implementation just returns '_url'.
		//		Sub-classes may override this method for the custom URL.
		//	item:
		//		An item to save
		//	returns:
		//		A post URL
		return this._url; //string
	},

	_getPutUrl: function(item){
		//	summary:
		//		Generate a URL for put
		//	description:
		//		This default implementation just returns '_url'.
		//		Sub-classes may override this method for the custom URL.
		//	item:
		//		An item to save
		//	returns:
		//		A put URL
		return this._url; //string
	},

	_getDeleteUrl: function(item){
		//	summary:
		//		Generate a URL for delete
		// 	description:
		//		This default implementation returns '_url' with '_keyAttribute'
		//		as a query string.
		//		Sub-classes may override this method for the custom URL based on
		//		changes (new, deleted, or modified).
		// 	item:
		//		An item to delete
		// 	returns:
		//		A delete URL
		if (!this._url) {
			return this._url; //string
		}
		var url = this._url;
		if (item && this._keyAttribute) {
			var value = this.getValue(item, this._keyAttribute);
			if (value) {
				url = url + '?' + this._keyAttribute + '=' + value;
			}
		}
		return url;	//string
	},

	_getPostContent: function(item){
		//	summary:
		//		Generate a content to post
		// 	description:
		//		This default implementation generates an XML document for one
		//		(the first only) new or modified element.
		//		Sub-classes may override this method for the custom post content
		//		generation.
		//	item:
		//		An item to save
		//	returns:
		//		A post content
		var element = item.element;
		var declaration = "<?xml version=\"1.0\"?>"; // FIXME: encoding?
		return declaration + dojox.data.dom.innerXML(element); //XML string
	},

	_getPutContent: function(item){
		//	summary:
		//		Generate a content to put
		// 	description:
		//		This default implementation generates an XML document for one
		//		(the first only) new or modified element.
		//		Sub-classes may override this method for the custom put content
		//		generation.
		//	item:
		//		An item to save
		//	returns:
		//		A post content
		var element = item.element;
		var declaration = "<?xml version=\"1.0\"?>"; // FIXME: encoding?
		return declaration + dojox.data.dom.innerXML(element); //XML string
	},

/* internal API */

	_getAttribute: function(tagName, attribute){
		if(this._attributeMap){
			var key = tagName + "." + attribute;
			var value = this._attributeMap[key];
			if(value){
				attribute = value;
			}else{ // look for global attribute
				value = this._attributeMap[attribute];
				if(value){
					attribute = value;
				}
			}
		}
		return attribute; //object
	},

	_getItem: function(element){
		return new dojox.data.XmlItem(element, this); //object
	},

	_getItemIndex: function(items, element){
		for(var i in items){
			if(items[i].element === element){
				return i; //int
			}
		}
		return -1; //int
	},

	_backupItem: function(item){
		var element = this._getRootElement(item.element);
		if(	this._getItemIndex(this._newItems, element) >= 0 ||
			this._getItemIndex(this._modifiedItems, element) >= 0){
			return; // new or already modified
		}
		if(element != item.element){
			item = this._getItem(element);
		}
		item._backup = element.cloneNode(true);
		this._modifiedItems.push(item);
	},

	_restoreItems: function(items){
		for(var i in items){
			var item = items[i];
			if(item._backup){
				item.element = item._backup;
				item._backup = null;
			}
		}
	},

	_forgetItem: function(item){
		var element = item.element;
		var index = this._getItemIndex(this._newItems, element);
		if(index >= 0){
			this._newItems.splice(index, 1);
		}
		index = this._getItemIndex(this._deletedItems, element);
		if(index >= 0){
			this._deletedItems.splice(index, 1);
		}
		index = this._getItemIndex(this._modifiedItems, element);
		if(index >= 0){
			this._modifiedItems.splice(index, 1);
		}
	},

	_getDocument: function(element){
		if(element){
			return element.ownerDocument;  //DOMDocument
		}else if(!this._document){
			return dojox.data.dom.createDocument(); // DOMDocument
		}
	},

	_getRootElement: function(element){
		while(element.parentNode){
			element = element.parentNode;
		}
		return element; //DOMElement
	}

});

//FIXME: Is a full class here really needed for containment of the item or would
//an anon object work fine?
dojo.declare("dojox.data.XmlItem", null, {
	constructor: function(element, store) {
		//	summary:
		//		Initialize with an XML element
		//	element:
		//		An XML element
		//	store:
		//		The containing store, if any.
		this.element = element;
		this.store = store;
	}, 
	//	summary:
	//		A data item of 'XmlStore'
	//	description:
	//		This class represents an item of 'XmlStore' holding an XML element.
	//		'element'
	//	element:
	//		An XML element

	toString: function() {
		//	summary:
		//		Return a value of the first text child of the element
		// 	returns:
		//		a value of the first text child of the element
		var str = "";
		if (this.element) {
			for (var i = 0; i < this.element.childNodes.length; i++) {
				var node = this.element.childNodes[i];
				if (node.nodeType === 3) {
					str = node.nodeValue;
					break;
				}
			}
		}
		return str;	//String
	}

});
dojo.extend(dojox.data.XmlStore,dojo.data.util.simpleFetch);

}
