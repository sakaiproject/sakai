if(!dojo._hasResource["dijit._editor.range"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor.range"] = true;
dojo.provide("dijit._editor.range");

dijit.range={};

dijit.range.getIndex=function(/*DomNode*/node, /*DomNode*/parent){
//	dojo.profile.start("dijit.range.getIndex");
	var ret=[], retR=[];
	var stop = parent;
	var onode = node;

	while(node != stop){
		var i = 0;
		var pnode = node.parentNode, n;
		while(n=pnode.childNodes[i++]){
			if(n===node){
				--i;
				break;
			}
		}
		if(i>=pnode.childNodes.length){
			dojo.debug("Error finding index of a node in dijit.range.getIndex");
		}
		ret.unshift(i);
		retR.unshift(i-pnode.childNodes.length);
		node = pnode;
	}

	//normalized() can not be called so often to prevent
	//invalidating selection/range, so we have to detect
	//here that any text nodes in a row
	if(ret.length>0 && onode.nodeType==3){
		var n = onode.previousSibling;
		while(n && n.nodeType==3){
			ret[ret.length-1]--;
			n = n.previousSibling;
		}
		n = onode.nextSibling;
		while(n && n.nodeType==3){
			retR[retR.length-1]++;
			n = n.nextSibling;
		}
	}
//	dojo.profile.end("dijit.range.getIndex");
	return {o: ret, r:retR};
}

dijit.range.getNode = function(/*Array*/index, /*DomNode*/parent){
	if(!dojo.isArray(index) || index.length==0){
		return parent;
	}
	var node = parent;
//	if(!node)debugger
	dojo.every(index, function(i){
		if(i>=0&&i< node.childNodes.length){
			node = node.childNodes[i];
		}else{
			node = null;
			console.debug('Error: can not find node with index',index,'under parent node',parent );
			return false; //terminate dojo.every
		}
		return true; //carry on the every loop
	});

	return node;
}

dijit.range.getCommonAncestor = function(n1,n2,root){
	var getAncestors = function(n,root){
		var as=[];
		while(n){
			as.unshift(n);
			if(n!=root && n.tagName!='BODY'){
				n = n.parentNode;
			}else{
				break;
			}
		}
		return as;
	};
	var n1as = getAncestors(n1,root);
	var n2as = getAncestors(n2,root);

	var m = Math.min(n1as.length,n2as.length);
	var com = n1as[0]; //at least, one element should be in the array: the root (BODY by default)
	for(var i=1;i<m;i++){
		if(n1as[i]===n2as[i]){
			com = n1as[i]
		}else{
			break;
		}
	}
	return com;
}

dijit.range.getAncestor = function(/*DomNode*/node, /*RegEx?*/regex, /*DomNode?*/root){
	root = root || node.ownerDocument.body;
	while(node && node !== root){
		var name = node.nodeName.toUpperCase() ;
		if(regex.test(name)){
			return node;
		}
		
		node = node.parentNode;
	}
	return null;
}

dijit.range.BlockTagNames = /^(?:P|DIV|H1|H2|H3|H4|H5|H6|ADDRESS|PRE|OL|UL|LI|DT|DE)$/;
dijit.range.getBlockAncestor = function(/*DomNode*/node, /*RegEx?*/regex, /*DomNode?*/root){
	root = root || node.ownerDocument.body;
	regex = regex || dijit.range.BlockTagNames;
	var block=null, blockContainer;
	while(node && node !== root){
		var name = node.nodeName.toUpperCase() ;
		if(!block && regex.test(name)){
			block = node;
		}
		if(!blockContainer && (/^(?:BODY|TD|TH|CAPTION)$/).test(name)){
			blockContainer = node;
		}
		
		node = node.parentNode;
	}
	return {blockNode:block, blockContainer:blockContainer || node.ownerDocument.body};
}

dijit.range.atBeginningOfContainer = function(/*DomNode*/container, /*DomNode*/node, /*Int*/offset){
	var atBeginning = false;
	var offsetAtBeginning = (offset == 0);
	if(!offsetAtBeginning && node.nodeType==3){ //if this is a text node, check whether the left part is all space
		if(dojo.trim(node.nodeValue.substr(0,offset))==0){
			offsetAtBeginning = true;
		}
	}
	if(offsetAtBeginning){
		var cnode = node;
		atBeginning = true;
		while(cnode && cnode !== container){
			if(cnode.previousSibling){
				atBeginning = false;
				break;
			}
			cnode = cnode.parentNode;
		}
	}
	return atBeginning;
}

dijit.range.atEndOfContainer = function(/*DomNode*/container, /*DomNode*/node, /*Int*/offset){
	var atEnd = false;
	var offsetAtEnd = (offset == (node.length || node.childNodes.length));
	if(!offsetAtEnd && node.nodeType==3){ //if this is a text node, check whether the right part is all space
		if(dojo.trim(node.nodeValue.substr(offset))==0){
			offsetAtEnd = true;
		}
	}
	if(offsetAtEnd){
		var cnode = node;
		atEnd = true;
		while(cnode && cnode !== container){
			if(cnode.nextSibling){
				atEnd = false;
				break;
			}
			cnode = cnode.parentNode;
		}
	}
	return atEnd;
}

dijit.range.adjacentNoneTextNode=function(startnode, next){
	var node = startnode;
	var len = (0-startnode.length) || 0;
	var prop = next?'nextSibling':'previousSibling';
	while(node){
		if(node.nodeType!=3){
			break;
		}
		len += node.length
		node = node[prop];
	}
	return [node,len];
}

dijit.range._w3c = Boolean(window['getSelection']);
dijit.range.create = function(){
	if(dijit.range._w3c){
		return document.createRange();
	}else{//IE
		return new dijit.range.W3CRange;
	}
}

dijit.range.getSelection = function(win, /*Boolean?*/ignoreUpdate){
	if(dijit.range._w3c){
		return win.getSelection();
	}else{//IE
		var id=win.__W3CRange;
		if(!id || !dijit.range.ie.cachedSelection[id]){
			var s = new dijit.range.ie.selection(win);
			//use win as the key in an object is not reliable, which
			//can leads to quite odd behaviors. thus we generate a
			//string and use it as a key in the cache
			id=(new Date).getTime();
			while(id in dijit.range.ie.cachedSelection){
				id=id+1;
			}
			id=String(id);
			dijit.range.ie.cachedSelection[id] = s;
		}else{
			var s = dijit.range.ie.cachedSelection[id];
		}
		if(!ignoreUpdate){
			s._getCurrentSelection();
		}
		return s;
	}
}
		
if(!dijit.range._w3c){
	dijit.range.ie={
		cachedSelection: {},
		selection: function(win){
			this._ranges = [];
			this.addRange = function(r, /*boolean*/internal){
				this._ranges.push(r);
				if(!internal){
					r._select();
				}
				this.rangeCount = this._ranges.length;
			};
			this.removeAllRanges = function(){
				//don't detach, the range may be used later
//				for(var i=0;i<this._ranges.length;i++){
//					this._ranges[i].detach();
//				}
				this._ranges = [];
				this.rangeCount = 0;
			};
			var _initCurrentRange = function(){
				var r = win.document.selection.createRange();
				var type=win.document.selection.type.toUpperCase();
				if(type == "CONTROL"){
					//TODO: multiple range selection(?)
					return new dijit.range.W3CRange(dijit.range.ie.decomposeControlRange(r));
				}else{
					return new dijit.range.W3CRange(dijit.range.ie.decomposeTextRange(r));
				}
			};
			this.getRangeAt = function(i){
				return this._ranges[i];
			};
			this._getCurrentSelection = function(){
				this.removeAllRanges();
				var r=_initCurrentRange();
				if(r){
					this.addRange(r, true);
				}
			};
		},
		decomposeControlRange: function(range){
			var firstnode = range.item(0), lastnode = range.item(range.length-1)
			var startContainer = firstnode.parentNode, endContainer = lastnode.parentNode;
			var startOffset = dijit.range.getIndex(firstnode, startContainer).o;
			var endOffset = dijit.range.getIndex(lastnode, endContainer).o+1;
			return [[startContainer, startOffset],[endContainer, endOffset]];
		},
		getEndPoint: function(range, end){
			var atmrange = range.duplicate();
			atmrange.collapse(!end);
			var cmpstr = 'EndTo' + (end?'End':'Start');
			var parentNode = atmrange.parentElement();

			var startnode, startOffset, lastNode;
			if(parentNode.childNodes.length>0){
				dojo.every(parentNode.childNodes, function(node,i){
					var calOffset;
					if(node.nodeType != 3){
						atmrange.moveToElementText(node);

						if(atmrange.compareEndPoints(cmpstr,range) > 0){
							startnode = node.previousSibling;
							if(lastNode && lastNode.nodeType == 3){
								//where share we put the start? in the text node or after?
								startnode = lastNode;
								calOffset = true;
							}else{
								startnode = parentNode;
								startOffset = i;
								return false;
							}
						}else{
							if(i==parentNode.childNodes.length-1){
								startnode = parentNode;
								startOffset = parentNode.childNodes.length;
								return false;
							}
						}
					}else{
						if(i==parentNode.childNodes.length-1){//at the end of this node
							startnode = node;
							calOffset = true;
						}
					}
		//			try{
						if(calOffset && startnode){
							var prevnode = dijit.range.adjacentNoneTextNode(startnode)[0];
							if(prevnode){
								startnode = prevnode.nextSibling;
							}else{
								startnode = parentNode.firstChild; //firstChild must be a text node
							}
							var prevnodeobj = dijit.range.adjacentNoneTextNode(startnode);
							prevnode = prevnodeobj[0];
							var lenoffset = prevnodeobj[1];
							if(prevnode){
								atmrange.moveToElementText(prevnode);
								atmrange.collapse(false);
							}else{
								atmrange.moveToElementText(parentNode);
							}
							atmrange.setEndPoint(cmpstr, range);
							startOffset = atmrange.text.length-lenoffset;

							return false;
						}
		//			}catch(e){ debugger }
					lastNode = node;
					return true;
				});
			}else{
				startnode = parentNode;
				startOffset = 0;
			}

			//if at the end of startnode and we are dealing with start container, then
			//move the startnode to nextSibling if it is a text node
			//TODO: do this for end container?
			if(!end && startnode.nodeType!=3 && startOffset == startnode.childNodes.length){
				if(startnode.nextSibling && startnode.nextSibling.nodeType==3){
					startnode = startnode.nextSibling;
					startOffset = 0;
				}
			}
			return [startnode, startOffset];
		},
		setEndPoint: function(range, container, offset){
			//text node
			var atmrange = range.duplicate();
			if(container.nodeType!=3){ //normal node
				atmrange.moveToElementText(container);
				atmrange.collapse(true);
				if(offset == container.childNodes.length){
					if(offset > 0){
						//a simple atmrange.collapse(false); won't work here:
						//although moveToElementText(node) is supposed to encompass the content of the node,
						//but when collapse to end, it is in fact after the ending tag of node (collapse to start
						//is after the begining tag of node as expected)
						var node = container.lastChild;
						var len = 0;
						while(node && node.nodeType == 3){
							len += node.length;
							container = node; //pass through
							node = node.previousSibling;
						}
						if(node){
							atmrange.moveToElementText(node);
						}
						atmrange.collapse(false);
						offset = len; //pass through
					}else{ //no childNodes
						atmrange.moveToElementText(container);
						atmrange.collapse(true);
					}
				}else{
					if(offset > 0){
						var node = container.childNodes[offset-1];
						if(node.nodeType==3){
							container = node; 
							offset = node.length;
							//pass through
						}else{
							atmrange.moveToElementText(node);
							atmrange.collapse(false);
						}
					}
				}
			}
			if(container.nodeType==3){
				var prevnodeobj = dijit.range.adjacentNoneTextNode(container);
				var prevnode = prevnodeobj[0], len = prevnodeobj[1];
				if(prevnode){
					atmrange.moveToElementText(prevnode);
					atmrange.collapse(false);
					//if contentEditable is not inherit, the above collapse won't make the end point
					//in the correctly position: it always has a -1 offset, so compensate it
					if(prevnode.contentEditable!='inherit'){
						len++;
					}
				}else{
					atmrange.moveToElementText(container.parentNode);
					atmrange.collapse(true);
				}

				offset += len;
				if(offset>0){
					if(atmrange.moveEnd('character',offset) != offset){
						alert('Error when moving!');
					}
					atmrange.collapse(false);
				}
			}
			
			return atmrange;
		},
		decomposeTextRange: function(range){
			var tmpary = dijit.range.ie.getEndPoint(range);
			var startContainter = tmpary[0], startOffset = tmpary[1];
			var endContainter = tmpary[0], endOffset = tmpary[1];

			if(range.htmlText.length){
				if(range.htmlText == range.text){ //in the same text node
					endOffset = startOffset+range.text.length;
				}else{
					tmpary = dijit.range.ie.getEndPoint(range,true);
					endContainter = tmpary[0], endOffset = tmpary[1];
				}
			}
			return [[startContainter, startOffset],[endContainter, endOffset], range.parentElement()];
		},
		setRange: function(range, startContainter, 
			startOffset, endContainter, endOffset, check){
			var startrange = dijit.range.ie.setEndPoint(range, startContainter, startOffset);
			range.setEndPoint('StartToStart', startrange);
			if(!this.collapsed){
				var endrange = dijit.range.ie.setEndPoint(range, endContainter, endOffset);
				range.setEndPoint('EndToEnd', endrange);
			}

			return range;
		}
	}

dojo.declare("dijit.range.W3CRange",null, {
	constructor: function(){
		if(arguments.length>0){
			this.setStart(arguments[0][0][0],arguments[0][0][1]);
			this.setEnd(arguments[0][1][0],arguments[0][1][1],arguments[0][2]);
		}else{
			this.commonAncestorContainer = null;
			this.startContainer = null;
			this.startOffset = 0;
			this.endContainer = null;
			this.endOffset = 0;
			this.collapsed = true;
		}
	},
	_simpleSetEndPoint: function(node, range, end){
		var r = (this._body||node.ownerDocument.body).createTextRange();
		if(node.nodeType!=1){
			r.moveToElementText(node.parentNode);
		}else{
			r.moveToElementText(node);
		}
		r.collapse(true);
		range.setEndPoint(end?'EndToEnd':'StartToStart',r);
	},
	_updateInternal: function(__internal_common){
		if(this.startContainer !== this.endContainer){
			if(!__internal_common){
				var r = (this._body||this.startContainer.ownerDocument.body).createTextRange();
				this._simpleSetEndPoint(this.startContainer,r);
				this._simpleSetEndPoint(this.endContainer,r,true);
				__internal_common = r.parentElement();
			}
			this.commonAncestorContainer = dijit.range.getCommonAncestor(this.startContainer, this.endContainer, __internal_common);
		}else{
			this.commonAncestorContainer = this.startContainer;
		}
		this.collapsed = (this.startContainer === this.endContainer) && (this.startOffset == this.endOffset);
	},
	setStart: function(node, offset, __internal_common){
		if(this.startContainer === node && this.startOffset == offset){
			return;
		}
		delete this._cachedBookmark;

		this.startContainer = node;
		this.startOffset = offset;
		if(!this.endContainer){
			this.setEnd(node, offset, __internal_common);
		}else{
			this._updateInternal(__internal_common);
		}
	},
	setEnd: function(node, offset, __internal_common){
		if(this.endContainer === node && this.endOffset == offset){
			return;
		}
		delete this._cachedBookmark;

		this.endContainer = node;
		this.endOffset = offset;
		if(!this.startContainer){
			this.setStart(node, offset, __internal_common);
		}else{
			this._updateInternal(__internal_common);
		}
	},
	setStartAfter: function(node, offset){
		this._setPoint('setStart', node, offset, 1);
	},
	setStartBefore: function(node, offset){
		this._setPoint('setStart', node, offset, 0);
	},
	setEndAfter: function(node, offset){
		this._setPoint('setEnd', node, offset, 1);
	},
	setEndBefore: function(node, offset){
		this._setPoint('setEnd', node, offset, 0);
	},
	_setPoint: function(what, node, offset, ext){
		var index = dijit.range.getIndex(node, node.parentNode).o;
		this[what](node.parentNode, index.pop()+ext);
	},
	_getIERange: function(){
		var r=(this._body||this.endContainer.ownerDocument.body).createTextRange();
		dijit.range.ie.setRange(r, this.startContainer, this.startOffset, this.endContainer, this.endOffset);
		return r;
	},
	getBookmark: function(body){
		this._getIERange();
		return this._cachedBookmark;
	},
	_select: function(){
		var r = this._getIERange();
		r.select();
	},
	deleteContents: function(){
		var r = this._getIERange();
		r.pasteHTML('');
		this.endContainer = this.startContainer;
		this.endOffset = this.startOffset;
		this.collapsed = true;
	},
	cloneRange: function(){
		var r = new dijit.range.W3CRange([[this.startContainer,this.startOffset],
			[this.endContainer,this.endOffset]]);
		r._body = this._body;
		return r;
	},
	detach: function(){
		this._body = null;
		this.commonAncestorContainer = null;
		this.startContainer = null;
		this.startOffset = 0;
		this.endContainer = null;
		this.endOffset = 0;
		this.collapsed = true;
}
});
} //if(!dijit.range._w3c)

}
