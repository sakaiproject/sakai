if(!dojo._hasResource["dojox.data.dom"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.data.dom"] = true;
dojo.provide("dojox.data.dom");

//DOM type to int value for reference.
//Ints make for more compact code than full constant names.
//ELEMENT_NODE                  = 1;
//ATTRIBUTE_NODE                = 2;
//TEXT_NODE                     = 3;
//CDATA_SECTION_NODE            = 4;
//ENTITY_REFERENCE_NODE         = 5;
//ENTITY_NODE                   = 6;
//PROCESSING_INSTRUCTION_NODE   = 7;
//COMMENT_NODE                  = 8;
//DOCUMENT_NODE                 = 9;
//DOCUMENT_TYPE_NODE            = 10;
//DOCUMENT_FRAGMENT_NODE        = 11;
//NOTATION_NODE                 = 12;

//FIXME:  Remove this file when possible.
//This file contains internal/helper APIs as holders until the true DOM apis of Dojo 0.9 are finalized.
//Therefore, these should not be generally used, they are present only for the use by XmlStore and the
//wires project until proper dojo replacements are available.  When such exist, XmlStore and the like
//will be ported off these and this file will be deleted.
dojo.experimental("dojox.data.dom");

dojox.data.dom.createDocument = function(/*string?*/ str, /*string?*/ mimetype){
	//	summary:
	//		cross-browser implementation of creating an XML document object.
	//
	//	str:
	//		Optional text to create the document from.  If not provided, an empty XML document will be created.
	//	mimetype:
	//		Optional mimetype of the text.  Typically, this is text/xml.  Will be defaulted to text/xml if not provided.
	var _document = dojo.doc;

	if(!mimetype){ mimetype = "text/xml"; }
	if(str && (typeof dojo.global["DOMParser"]) !== "undefined"){
		var parser = new DOMParser();
		return parser.parseFromString(str, mimetype);	//	DOMDocument
	}else if((typeof dojo.global["ActiveXObject"]) !== "undefined"){
		var prefixes = [ "MSXML2", "Microsoft", "MSXML", "MSXML3" ];
		for(var i = 0; i<prefixes.length; i++){
			try{
				var doc = new ActiveXObject(prefixes[i]+".XMLDOM");
				if(str){
					if(doc){
						doc.async = false;
						doc.loadXML(str);
						return doc;	//	DOMDocument
					}else{
						console.log("loadXML didn't work?");
					}
				}else{
					if(doc){ 
						return doc; //DOMDocument
					}
				}
			}catch(e){ /* squelch */ };
		}
	}else if((_document.implementation)&&
		(_document.implementation.createDocument)){
		if(str){
			if(_document.createElement){
				// FIXME: this may change all tags to uppercase!
				var tmp = _document.createElement("xml");
				tmp.innerHTML = str;
				var xmlDoc = _document.implementation.createDocument("foo", "", null);
				for(var i = 0; i < tmp.childNodes.length; i++) {
					xmlDoc.importNode(tmp.childNodes.item(i), true);
				}
				return xmlDoc;	//	DOMDocument
			}
		}else{
			return _document.implementation.createDocument("", "", null); // DOMDocument
		}
	}
	return null;	//	DOMDocument
}

dojox.data.dom.textContent = function(/*Node*/node, /*string?*/text){
	//	summary:
	//		Implementation of the DOM Level 3 attribute; scan node for text
	//	description:
	//		Implementation of the DOM Level 3 attribute; scan node for text
	//		This function can also update the text of a node by replacing all child 
	//		content of the node.
	//	node:
	//		The node to get the text off of or set the text on.
	//	text:
	//		Optional argument of the text to apply to the node.
	if(arguments.length>1){
		var _document = node.ownerDocument || dojo.doc;  //Preference is to get the node owning doc first or it may fail
		dojox.data.dom.replaceChildren(node, _document.createTextNode(text));
		return text;	//	string
	} else {
		if(node.textContent !== undefined){ //FF 1.5
			return node.textContent;	//	string
		}
		var _result = "";
		if(node == null){
			return _result; //empty string.
		}
		for(var i = 0; i < node.childNodes.length; i++){
			switch(node.childNodes[i].nodeType){
				case 1: // ELEMENT_NODE
				case 5: // ENTITY_REFERENCE_NODE
					_result += dojox.data.dom.textContent(node.childNodes[i]);
					break;
				case 3: // TEXT_NODE
				case 2: // ATTRIBUTE_NODE
				case 4: // CDATA_SECTION_NODE
					_result += node.childNodes[i].nodeValue;
					break;
				default:
					break;
			}
		}
		return _result;	//	string
	}
}

dojox.data.dom.replaceChildren = function(/*Element*/node, /*Node || array*/ newChildren){
	//	summary:
	//		Removes all children of node and appends newChild. All the existing
	//		children will be destroyed.
	//	description:
	//		Removes all children of node and appends newChild. All the existing
	//		children will be destroyed.
	// 	node:
	//		The node to modify the children on
	//	newChildren:
	//		The children to add to the node.  It can either be a single Node or an
	//		array of Nodes.
	var nodes = [];
	
	if(dojo.isIE){
		for(var i=0;i<node.childNodes.length;i++){
			nodes.push(node.childNodes[i]);
		}
	}

	dojox.data.dom.removeChildren(node);
	for(var i=0;i<nodes.length;i++){
		dojo._destroyElement(nodes[i]);
	}

	if(!dojo.isArray(newChildren)){
		node.appendChild(newChildren);
	}else{
		for(var i=0;i<newChildren.length;i++){
			node.appendChild(newChildren[i]);
		}
	}
}

dojox.data.dom.removeChildren = function(/*Element*/node){
	//	summary:
	//		removes all children from node and returns the count of children removed.
	//		The children nodes are not destroyed. Be sure to call dojo._destroyElement on them
	//		after they are not used anymore.
	//	node:
	//		The node to remove all the children from.
	var count = node.childNodes.length;
	while(node.hasChildNodes()){
		node.removeChild(node.firstChild);
	}
	return count; // int
}


dojox.data.dom.innerXML = function(/*Node*/node){
	//	summary:
	//		Implementation of MS's innerXML function.
	//	node:
	//		The node from which to generate the XML text representation.
	if(node.innerXML){
		return node.innerXML;	//	string
	}else if (node.xml){
		return node.xml;		//	string
	}else if(typeof XMLSerializer != "undefined"){
		return (new XMLSerializer()).serializeToString(node);	//	string
	}
}


}
