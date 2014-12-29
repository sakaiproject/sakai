// Modification to htmlArea to insert Paragraphs instead of
// linebreaks, under Gecko engines, circa January 2004
// By Adam Wright, for The University of Western Australia
//
// Distributed under the same terms as HTMLArea itself.
// This notice MUST stay intact for use (see license.txt).

function EnterParagraphs(editor, params) {
	this.editor = editor;
	// activate only if we're talking to Gecko
	if (HTMLArea.is_gecko)
		this.onKeyPress = this.__onKeyPress;
};

EnterParagraphs._pluginInfo = {
	name          : "EnterParagraphs",
	version       : "1.0",
	developer     : "Adam Wright",
	developer_url : "http://blog.hipikat.org/",
	sponsor       : "The University of Western Australia",
	sponsor_url   : "http://www.uwa.edu.au/",
	license       : "htmlArea"
};

// An array of elements who, in html4, by default, have an inline display and can have children
// we use RegExp here since it should be a bit faster, also cleaner to check
EnterParagraphs.prototype._html4_inlines_re = /^(a|abbr|acronym|b|bdo|big|cite|code|dfn|em|font|i|kbd|label|q|s|samp|select|small|span|strike|strong|sub|sup|textarea|tt|u|var)$/i;

// Finds the first parent element of a given node whose display is probably not inline
EnterParagraphs.prototype.parentBlock = function(node) {
	while (node.parentNode && (node.nodeType != 1 || this._html4_inlines_re.test(node.tagName)))
		node = node.parentNode;
	return node;
};

// Internal function for recursively itterating over a all nodes in a fragment
// If a callback function returns a non-null value, that is returned and the crawl is therefore broken
EnterParagraphs.prototype.walkNodeChildren = function(me, callback) {
	if (me.firstChild) {
		var myChild = me.firstChild;
		var retVal;
		while (myChild) {
			if ((retVal = callback(this, myChild)) != null)
				return retVal;
			if ((retVal = this.walkNodeChildren(myChild, callback)) != null)
				return retVal;
			myChild = myChild.nextSibling;
		}
	}
};

// Callback function to be performed on each node in the hierarchy
// Sets flag to true if we find actual text or an element that's not usually displayed inline
EnterParagraphs.prototype._isFilling = function(self, node) {
	if (node.nodeType == 1 && !self._html4_inlines_re.test(node.nodeName))
		return true;
	else if (node.nodeType == 3 && node.nodeValue != '')
		return true;
	return null;
	//alert(node.nodeName);
};

// Inserts a node deeply on the left of a hierarchy of nodes
EnterParagraphs.prototype.insertDeepLeftText = function(target, toInsert) {
	var falling = target;
	while (falling.firstChild && falling.firstChild.nodeType == 1)
		falling = falling.firstChild;
	//var refNode = falling.firstChild ? falling.firstChild : null;
	//falling.insertBefore(toInsert, refNode);
	falling.innerHTML = toInsert;
};

// Kind of like a macros, for a frequent query...
EnterParagraphs.prototype.isElem = function(node, type) {
	return node.nodeName.toLowerCase() == type.toLowerCase();
};

// The onKeyPress even that does all the work - nicely breaks the line into paragraphs
EnterParagraphs.prototype.__onKeyPress = function(ev) {

	if (ev.keyCode == 13 && !ev.shiftKey && this.editor._iframe.contentWindow.getSelection) {

		var editor = this.editor;

		// Get the selection and solid references to what we're dealing with chopping
		var sel = editor._iframe.contentWindow.getSelection();

		// Set the start and end points such that they're going /forward/ through the document
		var rngLeft = editor._doc.createRange();		var rngRight = editor._doc.createRange();
		rngLeft.setStart(sel.anchorNode, sel.anchorOffset);	rngRight.setStart(sel.focusNode, sel.focusOffset);
		rngLeft.collapse(true);					rngRight.collapse(true);

		var direct = rngLeft.compareBoundaryPoints(rngLeft.START_TO_END, rngRight) < 0;

		var startNode = direct ? sel.anchorNode : sel.focusNode;
		var startOffset = direct ? sel.anchorOffset : sel.focusOffset;
		var endNode = direct ? sel.focusNode : sel.anchorNode;
		var endOffset = direct ? sel.focusOffset : sel.anchorOffset;

		// Find the parent blocks of nodes at either end, and their attributes if they're paragraphs
		var startBlock = this.parentBlock(startNode);		var endBlock = this.parentBlock(endNode);
		var attrsLeft = new Array();				var attrsRight = new Array();

		// If a list, let the browser take over, if we're in a paragraph, gather it's attributes
		if (this.isElem(startBlock, 'li') || this.isElem(endBlock, 'li'))
			return;

		if (this.isElem(startBlock, 'p')) {
			for (var i = 0; i < startBlock.attributes.length; i++) {
				attrsLeft[startBlock.attributes[i].nodeName] = startBlock.attributes[i].nodeValue;
			}
		}
		if (this.isElem(endBlock, 'p')) {
			for (var i = 0; i < endBlock.attributes.length; i++) {
				// If we start and end within one paragraph, don't duplicate the 'id'
				if (endBlock != startBlock || endBlock.attributes[i].nodeName.toLowerCase() != 'id')
					attrsRight[endBlock.attributes[i].nodeName] = endBlock.attributes[i].nodeValue;
			}
		}

		// Look for where to start and end our chopping - within surrounding paragraphs
		// if they exist, or at the edges of the containing block, otherwise
		var startChop = startNode;				var endChop = endNode;

		while ((startChop.previousSibling && !this.isElem(startChop.previousSibling, 'p'))
		       || (startChop.parentNode && startChop.parentNode != startBlock && startChop.parentNode.nodeType != 9))
			startChop = startChop.previousSibling ? startChop.previousSibling : startChop.parentNode;

		while ((endChop.nextSibling && !this.isElem(endChop.nextSibling, 'p'))
		       || (endChop.parentNode && endChop.parentNode != endBlock && endChop.parentNode.nodeType != 9))
			endChop = endChop.nextSibling ? endChop.nextSibling : endChop.parentNode;

		// Set up new paragraphs
		var pLeft = editor._doc.createElement('p');		var pRight = editor._doc.createElement('p');

		for (var attrName in attrsLeft) {
			var thisAttr = editor._doc.createAttribute(attrName);
			thisAttr.value = attrsLeft[attrName];
			pLeft.setAttributeNode(thisAttr);
		}
		for (var attrName in attrsRight) {
			var thisAttr = editor._doc.createAttribute(attrName);
			thisAttr.value = attrsRight[attrName];
			pRight.setAttributeNode(thisAttr);
		}

		// Get the ranges destined to be stuffed into new paragraphs
		rngLeft.setStartBefore(startChop);
		rngLeft.setEnd(startNode,startOffset);
		pLeft.appendChild(rngLeft.cloneContents());		// Copy into pLeft

		rngRight.setEndAfter(endChop);
		rngRight.setStart(endNode,endOffset);
		pRight.appendChild(rngRight.cloneContents());		// Copy into pRight

		// If either paragraph is empty, fill it with a nonbreakable space
		var foundBlock = false;
		foundBlock = this.walkNodeChildren(pLeft, this._isFilling);
		if (foundBlock != true)
			this.insertDeepLeftText(pLeft, '&nbsp;');

		foundBlock = false;
		foundBlock = this.walkNodeChildren(pRight, this._isFilling);
		if (foundBlock != true)
			this.insertDeepLeftText(pRight, '&nbsp;');

		// Get a range for everything to be replaced and replace it
		var rngAround = editor._doc.createRange();

		if (!startChop.previousSibling && this.isElem(startChop.parentNode, 'p'))
			rngAround.setStartBefore(startChop.parentNode);
		else
			rngAround.setStart(rngLeft.startContainer, rngLeft.startOffset);

		if (!endChop.nextSibling && this.isElem(endChop.parentNode, 'p'))
			rngAround.setEndAfter(endChop.parentNode);
		else
			rngAround.setEnd(rngRight.endContainer, rngRight.endOffset);

		rngAround.deleteContents();
		rngAround.insertNode(pRight);
		rngAround.insertNode(pLeft);

		// Set the selection to the start of the (second) new paragraph
		if (pRight.firstChild) {
			while (pRight.firstChild && this._html4_inlines_re.test(pRight.firstChild.nodeName))
				pRight = pRight.firstChild;
			// Slip into any inline tags
			if (pRight.firstChild && pRight.firstChild.nodeType == 3)
				pRight = pRight.firstChild;	// and text, if they've got it

			var rngCaret = editor._doc.createRange();
			rngCaret.setStart(pRight, 0);
			rngCaret.collapse(true);

			sel = editor._iframe.contentWindow.getSelection();
			sel.removeAllRanges();
			sel.addRange(rngCaret);
		}

		// Stop the bubbling
		HTMLArea._stopEvent(ev);
	}
};
