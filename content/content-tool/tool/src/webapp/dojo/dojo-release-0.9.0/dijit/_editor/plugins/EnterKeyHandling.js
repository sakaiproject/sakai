if(!dojo._hasResource["dijit._editor.plugins.EnterKeyHandling"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor.plugins.EnterKeyHandling"] = true;
dojo.provide("dijit._editor.plugins.EnterKeyHandling");


dojo.declare("dijit._editor.plugins.EnterKeyHandling",null,{
	// blockNodeForEnter: String
	//		this property decides the behavior of Enter key. It can be either P,
	//		DIV, BR, or empty (which means disable this feature). Anything else
	//		will trigger errors.
	blockNodeForEnter: 'P',
	constructor: function(args){
		if(args){
			dojo.mixin(this,args);
		}
	},
	setEditor: function(editor){
		this.editor=editor;
		if(this.blockNodeForEnter=='BR'){
			if(dojo.isIE){
				editor.contentDomPreFilters.push(dojo.hitch(this, "regularPsToSingleLinePs"));
				editor.contentDomPostFilters.push(dojo.hitch(this, "singleLinePsToRegularPs"));
				editor.onLoadDeferred.addCallback(dojo.hitch(this, "_fixNewLineBehaviorForIE"));
			}else{
				editor.onLoadDeferred.addCallback(dojo.hitch(this,function(d){
					try{
						this.editor.document.execCommand("insertBrOnReturn", false, true);
					}catch(e){};
					return d;
				}));
			}
		}else if(this.blockNodeForEnter){
			//add enter key handler
			// FIXME: need to port to the new event code!!
			dojo['require']('dijit._editor.range');
			var h=dojo.hitch(this,this.handleEnterKey);
			editor.addKeyHandler(13, 0, h); //enter
			editor.addKeyHandler(13, 2, h); //shift+enter
			this.connect(this.editor,'onKeyPressed','onKeyPressed');
		}
	},
	connect: function(o,f,tf){
		if(!this._connects){
			this._connects=[];
		}
		this._connects.push(dojo.connect(o,f,this,tf));
	},
	destroy: function(){
		dojo.forEach(this._connects,dojo.disconnect);
		this._connects=[];
	},
	onKeyPressed: function(e){
		if(this._checkListLater){
			if(dojo.withGlobal(this.editor.window, 'isCollapsed', dijit._editor.selection)){
				if(!dojo.withGlobal(this.editor.window, 'hasAncestorElement', dijit._editor.selection, ['LI'])){
					//circulate the undo detection code by calling RichText::execCommand directly
					dijit._editor.RichText.prototype.execCommand.apply(this.editor, ['formatblock',this.blockNodeForEnter]);
					//set the innerHTML of the new block node
					var block = dojo.withGlobal(this.editor.window, 'getAncestorElement', dijit._editor.selection, [this.blockNodeForEnter])
					if(block){
						block.innerHTML=this.bogusHtmlContent;
						if(dojo.isIE){
							//the following won't work, it will move the caret to the last list item in the previous list
//							var newrange = dijit.range.create();
//							newrange.setStart(block.firstChild,0);
//							var selection = dijit.range.getSelection(this.editor.window)
//							selection.removeAllRanges();
//							selection.addRange(newrange);
							//move to the start by move backward one char
							var r = this.editor.document.selection.createRange();
							r.move('character',-1);
							r.select();
						}
					}else{
						alert('onKeyPressed: Can not find the new block node');
					}
				}
			}
			this._checkListLater = false;
		}else if(this._pressedEnterInBlock){
			//the new created is the original current P, so we have previousSibling below
			this.removeTrailingBr(this._pressedEnterInBlock.previousSibling);
			delete this._pressedEnterInBlock;
		}
	},
	bogusHtmlContent: '&nbsp;',
	blockNodes: /^(?:H1|H2|H3|H4|H5|H6|LI)$/,
	handleEnterKey: function(e){
		// summary: manually handle enter key event to make the behavior consistant across
		//	all supported browsers. See property blockNodeForEnter for available options
		if(!this.blockNodeForEnter){ return true; } //let browser handle this
		if(e.shiftKey  //shift+enter always generates <br>
		    || this.blockNodeForEnter=='BR'){
			var parent = dojo.withGlobal(this.editor.window, "getParentElement", dijit._editor.selection);
			var header = dijit.range.getAncestor(parent,this.editor.blockNodes);
			if(header){
				if(header.tagName=='LI'){
					return true; //let brower handle
				}
				var selection = dijit.range.getSelection(this.editor.window);
				var range = selection.getRangeAt(0);
				if(!range.collapsed){
					range.deleteContents();
				}
				if(dijit.range.atBeginningOfContainer(header, range.startContainer, range.startOffset)){
					dojo.place(this.editor.document.createElement('br'), header, "before");
				}else if(dijit.range.atEndOfContainer(header, range.startContainer, range.startOffset)){
					dojo.place(this.editor.document.createElement('br'), header, "after");
					var newrange = dijit.range.create();
					newrange.setStartAfter(header);

					selection.removeAllRanges();
					selection.addRange(newrange);
				}else{
					return true; //let brower handle
				}
			}else{
				//don't change this: do not call this.execCommand, as that may have other logic in subclass
				// FIXME
				dijit._editor.RichText.prototype.execCommand.call(this.editor, 'inserthtml', '<br>');
			}
			return false;
		}
		var _letBrowserHandle = true;
		//blockNodeForEnter is either P or DIV
		//first remove selection
		var selection = dijit.range.getSelection(this.editor.window);
		var range = selection.getRangeAt(0);
		if(!range.collapsed){
			range.deleteContents();
		}

		var block = dijit.range.getBlockAncestor(range.endContainer, null, this.editor.editNode);

		if(block.blockNode && block.blockNode.tagName == 'LI'){
			this._checkListLater = true;
			return true;
		}else{
			this._checkListLater = false;
		}

		//text node directly under body, let's wrap them in a node
		if(!block.blockNode){
			this.editor.document.execCommand('formatblock',false, this.blockNodeForEnter);
			//get the newly created block node
			// FIXME
			block = {blockNode:dojo.withGlobal(this.editor.window, "getAncestorElement", dijit._editor.selection, [this.blockNodeForEnter]),
					blockContainer: this.editor.editNode};
			if(block.blockNode){
				if((block.blockNode.textContent||block.blockNode.innerHTML).replace(/^\s+|\s+$/g, "").length==0){
					this.removeTrailingBr(block.blockNode);
					return false;
				}
			}else{
				block.blockNode = this.editor.editNode;
			}
			selection = dijit.range.getSelection(this.editor.window);
			range = selection.getRangeAt(0);
		}
		var newblock = this.editor.document.createElement(this.blockNodeForEnter);
		newblock.innerHTML=this.bogusHtmlContent;
		this.removeTrailingBr(block.blockNode);
		if(dijit.range.atEndOfContainer(block.blockNode, range.endContainer, range.endOffset)){
			if(block.blockNode === block.blockContainer){
				block.blockNode.appendChild(newblock);
			}else{
				dojo.place(newblock, block.blockNode, "after");
			}
			_letBrowserHandle = false;
			//lets move caret to the newly created block
			var newrange = dijit.range.create();
			newrange.setStart(newblock,0);
			selection.removeAllRanges();
			selection.addRange(newrange);
			if(this.editor.height){
				newblock.scrollIntoView(false);
			}
		}else if(dijit.range.atBeginningOfContainer(block.blockNode,
				range.startContainer, range.startOffset)){
			if(block.blockNode === block.blockContainer){
				dojo.place(newblock, block.blockNode, "first");
			}else{
				dojo.place(newblock, block.blockNode, "before");
			}
			if(this.editor.height){
				//browser does not scroll the caret position into view, do it manually
				newblock.scrollIntoView(false);
			}
			_letBrowserHandle = false;
		}else{ //press enter in the middle of P
			if(dojo.isMoz){
				//press enter in middle of P may leave a trailing <br/>, let's remove it later
				this._pressedEnterInBlock = block.blockNode;
			}
		}
		return _letBrowserHandle;
	},
	removeTrailingBr: function(container){
		if(/P|DIV|LI/i .test(container.tagName)){
			var para = container;
		}else{
			var para = dijit._editor.selection.getParentOfType(container,['P','DIV','LI']);
		}

		if(!para){ return; }
		if(para.lastChild){
			if(para.childNodes.length>1 && para.lastChild.nodeType==3 && /^[\s\xAD]*$/ .test(para.lastChild.nodeValue)){
				dojo._destroyElement(para.lastChild);
			}
			if(para.lastChild && para.lastChild.tagName=='BR'){
				dojo._destroyElement(para.lastChild);
			}
		}
		if(para.childNodes.length==0){
			para.innerHTML=this.bogusHtmlContent;
		}
	},
	_fixNewLineBehaviorForIE: function(d){
		if(typeof this.editor.document.__INSERTED_EDITIOR_NEWLINE_CSS == "undefined"){
			var lineFixingStyles = "p{margin:0 !important;}";
			var insertCssText = function(
				/*String*/ cssStr, 
				/*Document*/ doc, 
				/*String*/ URI)
			{
				//	summary:
				//		Attempt to insert CSS rules into the document through inserting a
				//		style element
			
				// DomNode Style  = insertCssText(String ".dojoMenu {color: green;}"[, DomDoc document, dojo.uri.Uri Url ])
				if(!cssStr){ 
					return; //	HTMLStyleElement
				}
				if(!doc){ doc = document; }
//					if(URI){// fix paths in cssStr
//						cssStr = dojo.html.fixPathsInCssText(cssStr, URI);
//					}
				var style = doc.createElement("style");
				style.setAttribute("type", "text/css");
				// IE is b0rken enough to require that we add the element to the doc
				// before changing it's properties
				var head = doc.getElementsByTagName("head")[0];
				if(!head){ // must have a head tag 
					console.debug("No head tag in document, aborting styles");
					return;	//	HTMLStyleElement
				}else{
					head.appendChild(style);
				}
				if(style.styleSheet){// IE
					var setFunc = function(){ 
						try{
							style.styleSheet.cssText = cssStr;
						}catch(e){ dojo.debug(e); }
					};
					if(style.styleSheet.disabled){
						setTimeout(setFunc, 10);
					}else{
						setFunc();
					}
				}else{ // w3c
					var cssText = doc.createTextNode(cssStr);
					style.appendChild(cssText);
				}
				return style;	//	HTMLStyleElement
			}
			insertCssText(lineFixingStyles, this.editor.document);
			this.editor.document.__INSERTED_EDITIOR_NEWLINE_CSS = true;
			// this.regularPsToSingleLinePs(this.editNode);
			return d;
		}
	},
	regularPsToSingleLinePs: function(element, noWhiteSpaceInEmptyP){
		function wrapLinesInPs(el){
		  // move "lines" of top-level text nodes into ps
			function wrapNodes(nodes){
				// nodes are assumed to all be siblings
				var newP = nodes[0].ownerDocument.createElement('p'); // FIXME: not very idiomatic
				nodes[0].parentNode.insertBefore(newP, nodes[0]);
				for(var i=0; i<nodes.length; i++){
				    newP.appendChild(nodes[i]);
				}
			}

			var currentNodeIndex = 0;
			var nodesInLine = [];
			var currentNode;
			while(currentNodeIndex < el.childNodes.length){
				currentNode = el.childNodes[currentNodeIndex];
				if( (currentNode.nodeName!='BR') &&
					(currentNode.nodeType==1) &&
					(dojo.style(currentNode, "display")!="block")
				){
					nodesInLine.push(currentNode);
				}else{
					// hit line delimiter; process nodesInLine if there are any
					var nextCurrentNode = currentNode.nextSibling;
					if(nodesInLine.length){
						wrapNodes(nodesInLine);
						currentNodeIndex = (currentNodeIndex+1)-nodesInLine.length;
						if(currentNode.nodeName=="BR"){
							dojo._destroyElement(currentNode);
						}
					}
					nodesInLine = [];
				}
				currentNodeIndex++;
			}
			if(nodesInLine.length){ wrapNodes(nodesInLine); }
		}

		function splitP(el){
		    // split a paragraph into seperate paragraphs at BRs
		    var currentNode = null;
		    var trailingNodes = [];
		    var lastNodeIndex = el.childNodes.length-1;
		    for(var i=lastNodeIndex; i>=0; i--){
				currentNode = el.childNodes[i];
				if(currentNode.nodeName=="BR"){
					var newP = currentNode.ownerDocument.createElement('p');
					dojo.place(newP, el, "after");
					if (trailingNodes.length==0 && i != lastNodeIndex) {
						newP.innerHTML = "&nbsp;"
					}
					dojo.forEach(trailingNodes, function(node){
						newP.appendChild(node);
					});
					dojo._destroyElement(currentNode);
					trailingNodes = [];
				}else{
					trailingNodes.unshift(currentNode);
				}
		    }
		}

		var pList = [];
		var ps = element.getElementsByTagName('p');
		dojo.forEach(ps, function(p){ pList.push(p); });
		dojo.forEach(pList, function(p){
			if(	(p.previousSibling) &&
				(p.previousSibling.nodeName == 'P' || dojo.style(p.previousSibling, 'display') != 'block')
			){
				var newP = p.parentNode.insertBefore(this.document.createElement('p'), p);
				// this is essential to prevent IE from losing the P.
				// if it's going to be innerHTML'd later we need
				// to add the &nbsp; to _really_ force the issue
				newP.innerHTML = noWhiteSpaceInEmptyP ? "" : "&nbsp;";
			}
			splitP(p);
	  },this.editor);
		wrapLinesInPs(element);
		return element;
	},

	singleLinePsToRegularPs: function(element){
		function getParagraphParents(node){
			var ps = node.getElementsByTagName('p');
			var parents = [];
			for(var i=0; i<ps.length; i++){
				var p = ps[i];
				var knownParent = false;
				for(var k=0; k < parents.length; k++){
					if(parents[k] === p.parentNode){
						knownParent = true;
						break;
					}
				}
				if(!knownParent){
					parents.push(p.parentNode);
				}
			}
			return parents;
		}

		function isParagraphDelimiter(node){
			if(node.nodeType != 1 || node.tagName != 'P'){
				return (dojo.style(node, 'display') == 'block');
			}else{
				if(!node.childNodes.length || node.innerHTML=="&nbsp;"){ return true }
				//return node.innerHTML.match(/^(<br\ ?\/?>| |\&nbsp\;)$/i);
			}
		}

		var paragraphContainers = getParagraphParents(element);
		for(var i=0; i<paragraphContainers.length; i++){
			var container = paragraphContainers[i];
			var firstPInBlock = null;
			var node = container.firstChild;
			var deleteNode = null;
			while(node){
				if(node.nodeType != "1" || node.tagName != 'P'){
					firstPInBlock = null;
				}else if (isParagraphDelimiter(node)){
					deleteNode = node;
					firstPInBlock = null;
				}else{
					if(firstPInBlock == null){
						firstPInBlock = node;
					}else{
						if( (!firstPInBlock.lastChild || firstPInBlock.lastChild.nodeName != 'BR') &&
							(node.firstChild) &&
							(node.firstChild.nodeName != 'BR')
						){
							firstPInBlock.appendChild(this.editor.document.createElement('br'));
						}
						while(node.firstChild){
							firstPInBlock.appendChild(node.firstChild);
						}
						deleteNode = node;
					}
				}
				node = node.nextSibling;
				if(deleteNode){
					dojo._destroyElement(deleteNode);
					deleteNode = null;
				}
			}
		}
		return element;
	}
});

}
