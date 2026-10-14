/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupueditor.js 928511 2010-03-28 22:53:14Z lu4242 $

//----------------------------------------------------------------------------
// Main classes
//----------------------------------------------------------------------------

/* KupuDocument
    
    This essentially wraps the iframe.
    XXX Is this overkill?
    
*/

function KupuDocument(iframe) {
    /* Model */
    
    // attrs
    this.editable = iframe; // the iframe
    this.window = this.editable.contentWindow;
    this.document = this.window.document;

    this._browser = _SARISSA_IS_IE ? 'IE' : 'Mozilla';
    var DEPRECATED = { 'contentReadOnly': 'readonly', 'styleWithCSS': 'useCSS' };
    // methods
    this.execCommand = function(command, arg) {
        /* delegate execCommand */
        if (arg === undefined) arg = null;
        try {
            this.document.execCommand(command, false, arg);
        } catch(e) {
            command = DEPRECATED[command];
            if (command) {
                this.document.execCommand(command, false, !arg);
            };
        };
    };
    
    this.reloadSource = function() {
        /* reload the source */
        
        // XXX To temporarily work around problems with resetting the
        // state after a reload, currently the whole page is reloaded.
        // XXX Nasty workaround!! to solve refresh problems...
        document.location = document.location;
    };

    this.getDocument = function() {
        /* returns a reference to the window.document object of the iframe */
        return this.document;
    };

    this.getWindow = function() {
        /* returns a reference to the window object of the iframe */
        return this.window;
    };

    this.getSelection = function() {
        if (this._browser == 'Mozilla') {
            return new MozillaSelection(this);
        } else {
            return new IESelection(this);
        };
    };

    this.getEditable = function() {
        return this.editable;
    };
};

/* KupuEditor

    This controls the document, should be used from the UI.
    
*/

function KupuEditor(document, config, logger) {
    /* Controller */
    
    // attrs
    this.document = document; // the model
    this.config = config; // an object that holds the config values
    this.log = logger; // simple logger object
    this.tools = {}; // mapping id->tool
    this.filters = []; // contentfilters
    this.serializer = new XMLSerializer();
    
    this._designModeSetAttempts = 0;
    this._initialized = false;
    this._wantDesignMode = false;

    // some properties to save the selection, required for IE to remember 
    // where in the iframe the selection was
    this._previous_range = null;

    // this property is true if the content is changed, false if no changes 
    // are made yet
    this.content_changed = false;

    // methods
    this.initialize = function() {
        /* Should be called on iframe.onload, will initialize the editor */
        //DOM2Event.initRegistration();
        this._initializeEventHandlers();
        if (this.getBrowserName() == "IE") {
            var body = this.getInnerDocument().getElementsByTagName('body')[0];
            body.setAttribute('contentEditable', 'true');
            // provide an 'afterInit' method on KupuEditor.prototype
            // for additional bootstrapping (after editor init)
            this._initialized = true;
            if (this.afterInit) {
                this.afterInit();
            };
            this._saveSelection();
        } else {
            this._setDesignModeWhenReady();
        };
    };

    this.setContextMenu = function(menu) {
        /* initialize the contextmenu */
        menu.initialize(this);
    };

    this.registerTool = function(id, tool) {
        /* register a tool */
        this.tools[id] = tool;
        tool.initialize(this);
    };

    this.getTool = function(id) {
        /* get a tool by id */
        return this.tools[id];
    };

    this.registerFilter = function(filter) {
        /* register a content filter method

            the method will be called together with any other registered
            filters before the content is saved to the server, the methods
            can be used to filter any trash out of the content. they are
            called with 1 argument, which is a reference to the rootnode
            of the content tree (the html node)
        */
        this.filters.push(filter);
        filter.initialize(this);
    };

    this.updateStateHandler = function(event) {
        /* check whether the event is interesting enough to trigger the 
        updateState machinery and act accordingly */
        var interesting_codes = [8, 13, 37, 38, 39, 40, 46];
        // unfortunately it's not possible to do this on blur, since that's
        // too late. also (some versions of?) IE 5.5 doesn't support the
        // onbeforedeactivate event, which would be ideal here...
        this._saveSelection();

        if (event.type == 'click' ||
                (event.type == 'keyup' && 
                    interesting_codes.contains(event.keyCode))) {
            // Filthy trick to make the updateState method get called *after*
            // the event has been resolved. This way the updateState methods can
            // react to the situation *after* any actions have been performed (so
            // can actually stay up to date).
            this.updateState(event);
        }
    };
    
    this.updateState = function(event) {
        /* let each tool change state if required */
        // first see if the event is interesting enough to trigger
        // the whole updateState machinery
        var selNode = this.getSelectedNode();
        for (var id in this.tools) {
            try {
                this.tools[id].updateState(selNode, event);
            } catch (e) {
                if (e == UpdateStateCancelBubble) {
                    this.updateState(event);
                    break;
                } else {
                    this.logMessage(
                        'Exception while processing updateState on ' +
                            '${id}: ${msg}', {'id': id, 'msg': e}, 2);
                };
            };
        };
    };
    
    this.saveDocument = function(redirect, synchronous) {
        /* save the document

            the (optional) redirect argument can be used to make the client 
            jump to another URL when the save action was successful.

            synchronous is a boolean to allow sync saving (usually better to
            not save synchronous, since it may make browsers freeze on errors,
            this is used for saveOnPart, though)
        */
        
        // if no dst is available, bail out
        if (!this.config.dst) {
            this.logMessage(_('No destination URL available!'), 2);
            return;
        }
        var sourcetool = this.getTool('sourceedittool');
        if (sourcetool) {sourcetool.cancelSourceMode();};

        // make sure people can't edit or save during saving
        if (!this._initialized) {
            return;
        }
        this._initialized = false;
        
        // set the window status so people can see we're actually saving
        window.status= _("Please wait while saving document...");

        // call (optional) beforeSave() method on all tools
        for (var id in this.tools) {
            var tool = this.tools[id];
            if (tool.beforeSave) {
                try {
                    tool.beforeSave();
                } catch(e) {
                    alert(e);
                    this._initialized = true;
                    return;
                };
            };
        };
        
        // pass the content through the filters
        this.logMessage(_("Starting HTML cleanup"));
        var transform = this._filterContent(this.getInnerDocument().documentElement);

        // serialize to a string
        var contents = this._serializeOutputToString(transform);
        
        this.logMessage(_("Cleanup done, sending document to server"));
        var request = new XMLHttpRequest();
    
        if (!synchronous) {
            request.onreadystatechange = (new ContextFixer(this._saveCallback, 
                                               this, request, redirect)).execute;
            request.open("PUT", this.config.dst, true);
            request.setRequestHeader("Content-type", this.config.content_type);
            request.send(contents);
            this.logMessage(_("Request sent to server"));
        } else {
            this.logMessage(_('Sending request to server'));
            request.open("PUT", this.config.dst, false);
            request.setRequestHeader("Content-type", this.config.content_type);
            request.send(contents);
            this.handleSaveResponse(request,redirect);
        };
    };
    
    this.prepareForm = function(form, id) {
        /* add a field to the form and place the contents in it

            can be used for simple POST support where Kupu is part of a
            form
        */
        var sourcetool = this.getTool('sourceedittool');
        if (sourcetool) {sourcetool.cancelSourceMode();};

        // make sure people can't edit or save during saving
        if (!this._initialized) {
            return;
        }
        this._initialized = false;
        
        // set the window status so people can see we're actually saving
        window.status= _("Please wait while saving document...");

        // call (optional) beforeSave() method on all tools
        for (var tid in this.tools) {
            var tool = this.tools[tid];
            if (tool.beforeSave) {
                try {
                    tool.beforeSave();
                } catch(e) {
                    alert(e);
                    this._initialized = true;
                    return;
                };
            };
        };
        
        // set a default id
        if (!id) {
            id = 'kupu';
        };
        
        // pass the content through the filters
        this.logMessage(_("Starting HTML cleanup"));
        var transform = this._filterContent(this.getInnerDocument().documentElement);
        
        // XXX need to fix this.  Sometimes a spurious "\n\n" text 
        // node appears in the transform, which breaks the Moz 
        // serializer on .xml
        var contents =  this._serializeOutputToString(transform);
        
        this.logMessage(_("Cleanup done, sending document to server"));
        
        // now create the form input, since IE 5.5 doesn't support the 
        // ownerDocument property we use window.document as a fallback (which
        // will almost by definition be correct).
        var document = form.ownerDocument ? form.ownerDocument : window.document;
        var ta = document.createElement('textarea');
        ta.style.visibility = 'hidden';
        var text = document.createTextNode(contents);
        ta.appendChild(text);
        ta.setAttribute('name', id);
        
        // and add it to the form
        form.appendChild(ta);

        // let the calling code know we have added the textarea
        return true;
    };

    this.execCommand = function(command, param) {
        /* general stuff like making current selection bold, italics etc. 
            and adding basic elements such as lists
            */
        if (!this._initialized) {
            this.logMessage(_('Editor not initialized yet!'));
            return;
        };
        if (this.getBrowserName() == "IE") {
            this._restoreSelection();
        } else {
            this.focusDocument();
            if (command != 'styleWithCSS') {
                this.content_changed = true;
                // Done here otherwise it doesn't always work or gets lost
                // after some commands
                this.getDocument().execCommand('styleWithCSS', false);
            };
        };
        this.getDocument().execCommand(command, param);
        this.updateState();
    };

    this.getSelection = function() {
        /* returns a Selection object wrapping the current selection */
        this._restoreSelection();
        return this.getDocument().getSelection();
    };

    this.getSelectedNode = function(allowmulti) {
        /* returns the selected node (read: parent) or none */
        /* if allowmulti is true, returns the parent of all ranges in the
           selection (in the rare case that selection has more than one
           range) */
        return this.getSelection().parentElement(allowmulti);
    };

    this.getNearestParentOfType = function(node, type) {
        /* well the title says it all ;) */
        var type = type.toLowerCase();
        while (node) {
            if (node.nodeName.toLowerCase() == type) {
                return node;
            }   
            var node = node.parentNode;
        }
        return false;
    };

    this.removeNearestParentOfType = function(node, type) {
        var nearest = this.getNearestParentOfType(node, type);
        if (!nearest) {
            return false;
        };
        var parent = nearest.parentNode;
        while (nearest.childNodes.length) {
            var child = nearest.firstChild;
            child = nearest.removeChild(child);
            parent.insertBefore(child, nearest);
        };
        parent.removeChild(nearest);
    };

    this.getDocument = function() {
        /* returns a reference to the document object that wraps the iframe */
        return this.document;
    };

    this.getInnerDocument = function() {
        /* returns a reference to the window.document object of the iframe */
        return this.getDocument().getDocument();
    };

    this.insertNodeAtSelection = function(insertNode, selectNode) {
        /* insert a newly created node into the document */
        if (!this._initialized) {
            this.logMessage(_('Editor not initialized yet!'));
            return;
        };

        this.content_changed = true;

        var browser = this.getBrowserName();
        if (browser != "IE") {
            this.focusDocument();
        };
        
        var ret = this.getSelection().replaceWithNode(insertNode, selectNode);
        this._saveSelection();

        return ret;
    };

    this.focusDocument = function() {
        this.getDocument().getWindow().focus();
    };

    this.logMessage = function(message, severity) {
        /* log a message using the logger, severity can be 0 (message, default), 1 (warning) or 2 (error) */
        this.log.log(message, severity);
    };

    this.registerContentChanger = function(element) {
        /* set this.content_changed to true (marking the content changed) when the 
            element's onchange is called
        */
        addEventHandler(element, 'change', function() {this.content_changed = true;}, this);
    };
    
    // helper methods
    this.getBrowserName = function() {
        /* returns either 'Mozilla' (for Mozilla, Firebird, Netscape etc.) or 'IE' */
        if (_SARISSA_IS_MOZ) {
            return "Mozilla";
        } else if (_SARISSA_IS_IE) {
            return "IE";
        } else {
            throw _("Browser not supported!");
        }
    };
    
    this.handleSaveResponse = function(request, redirect) {
        // mind the 1223 status, somehow IE gives that sometimes (on 204?)
        // at first we didn't want to add it here, since it's a specific IE
        // bug, but too many users had trouble with it...
        if (request.status != '200' && request.status != '204' &&
                request.status != '1223') {
            var msg = _('Error saving your data.\nResponse status: ' + 
                            '${status}.\nCheck your server log for more ' +
                            'information.', {'status': request.status});
            alert(msg);
            window.status = _("Error saving document");
        } else if (redirect) { // && (!request.status || request.status == '200' || request.status == '204'))
            window.document.location = redirect;
            this.content_changed = false;
        } else {
            // clear content_changed before reloadSrc so saveOnPart is not triggered
            this.content_changed = false;
            if (this.config.reload_after_save) {
                this.reloadSrc();
            };
            // we're done so we can start editing again
            window.status= _("Document saved");
        };
        this._initialized = true;
    };

    // private methods
    this._addEventHandler = addEventHandler;

    this._saveCallback = function(request, redirect) {
        /* callback for Sarissa */
        if (request.readyState == 4) {
            this.handleSaveResponse(request, redirect);
        };
    };
    
    this.reloadSrc = function() {
        /* reload the src, called after a save when reload_src is set to true */
        // XXX Broken!!!
        /*
        if (this.getBrowserName() == "Mozilla") {
            this.getInnerDocument().designMode = "Off";
        }
        */
        // XXX call reloadSrc() which has a workaround, reloads the full page
        // instead of just the iframe...
        this.getDocument().reloadSource();
        if (this.getBrowserName() == "Mozilla") {
            this.getInnerDocument().designMode = "On";
        };
        /*
        var selNode = this.getSelectedNode();
        this.updateState(selNode);
        */
    };

    // Fixup Mozilla breaking image src url when dragging images
    this.imageInserted = function(event) {
        var node = event.target;
        if (node && node.nodeType==1) {
            var nodes = (/^img$/i.test(node.nodeName))?[node]:node.getElementsByTagName('img');
            for (var i = 0; i < nodes.length; i++) {
                node = nodes[i];
                var src = node.getAttribute('kupu-src');
                if (src) { node.src = src; };
            };
        };
    };
    // Prevent Mozilla resizing of images
    this.imageModified = function(event) {
        var node = event.target;
        if (node && (/^img$/i.test(node.nodeName))) {
            if (event.attrName=="style" && event.attrChange==1 && (/height|width/.test(event.newValue))) {
                timer_instance.registerFunction(this, this._clearStyle, 1, node);
            }
        };
    };
    // Make sure image size is set on width/height attributes not style.
    this._clearStyle = function(node) {
        var w = node.width;
        var h = node.height;
        node.style.width = "";
        node.style.height = "";
        if (this.okresize) {
            if (w) {node.width = w;};
            if (h) {node.height = h;};
        };
    };
    this._cancelResize = function(evt) {
        return false;
    };

    this._initializeEventHandlers = function() {
        /* attache the event handlers to the iframe */
        var win = this.getDocument().getWindow();
        var idoc = this.getInnerDocument();
        var e = this._addEventHandler;
        var validattrs =  this.xhtmlvalid.tagAttributes.img;
        this.okresize = validattrs.contains('width') && validattrs.contains('height');
        // Set design mode on resize event:
        e(win, 'resize', this._resizeHandler, this);
        // Initialize DOM2Event compatibility
        // XXX should come back and change to passing in an element
        e(idoc, "click", this.updateStateHandler, this);
        e(idoc, "dblclick", this.updateStateHandler, this);
        e(idoc, "keyup", this.updateStateHandler, this);
        e(idoc, "keyup", function() {this.content_changed = true;}, this);
        e(idoc, "mouseup", this.updateStateHandler, this);
        if (this.getBrowserName() == "IE") {
            e(idoc, "selectionchange", this.onSelectionChange, this);
            if (!this.okresize) { e(idoc.documentElement, "resizestart", this._cancelResize, this);};
        } else {
            e(idoc, "DOMNodeInserted", this.imageInserted, this);
            e(idoc, "DOMAttrModified", this.imageModified, this);
        }
    };

    this._resizeHandler = function() {
        // Use the resize event to trigger setting design mode
        if (this._wantDesignMode) {
            this._setDesignModeWhenReady();
        }
    };
    
    this._setDesignModeWhenReady = function() {
        /* Try to set design mode, but if we fail then just wait for a
         * resize event.
         */
        var success = false;
        try {
            this._setDesignMode();
            success = true;
        } catch (e) {
        
        	// BEGIN myFaces special Code
        	// Maybe Kupu is in a hidden parent node.
        	if (this._designModeSetAttempts < 2) {
        		var hiddingElements = new Array();
				for(var currentNode = this.getDocument().getEditable().parentNode ;
					currentNode.getAttribute ;
					currentNode = currentNode.parentNode){

					if( currentNode.style.display=='none' ){
						hiddingElements.push( currentNode );
						currentNode.style.display='block';
					}
				}
				
	        	try{					
					this._setDesignMode();
		            success = true;
		        } catch (e2) {
	        		// NoOp
	        	}
				
				while(hiddingElements.length > 0 ){
					hiddingElements.pop().style.display='none';
				}
	        	
	        }
	        
            // register a function to the timer_instance because 
            // window.setTimeout can't refer to 'this'...
            if( ! success )
	            timer_instance.registerFunction(this, this._setDesignModeWhenReady, 100);
	            
			// END myFaces special code.
        };
        if (success) {
            this._wantDesignMode = false;
            // provide an 'afterInit' method on KupuEditor.prototype
            // for additional bootstrapping (after editor init)
            if (this.afterInit) {
                this.afterInit();
            };
        } else {
            this._wantDesignMode = true; // Enable the resize trigger
        }
    };

    this._setDesignMode = function() {
        this.getInnerDocument().designMode = "On";
        this.execCommand("undo");
        // note the negation: the argument doesn't work as expected...
        this._initialized = true;
    };

    this._saveSelection = function() {
        /* Save the selection, works around a problem with IE where the 
         selection in the iframe gets lost. We only save if the current 
         selection in the document */
        if (this._isDocumentSelected()) {
            var cursel = this.getInnerDocument().selection;
            var currange = cursel.createRange();
            if (cursel.type=="Control" && currange.item(0).nodeName.toLowerCase()=="body") {
                /* This happens when you try to active an embedded
                 * object */
                this._restoreSelection(true);
                return;
            }
            this._previous_range = currange;
        };
    };

    this._restoreSelection = function(force) {
        /* re-selects the previous selection in IE. We only restore if the
        current selection is not in the document.*/
        if (this._previous_range && (force || !this._isDocumentSelected())) {
            try {
                this._previous_range.select();
            } catch (e) { };
        };
    };
    
    if (this.getBrowserName() != "IE") {
        this._saveSelection = function() {};
        this._restoreSelection = function() {};
    }

    this.onSelectionChange = function(event) {
        this._saveSelection();
    };

    this._isDocumentSelected = function() {
        if (this.suspended) return false;

        var editable_body = this.getInnerDocument().getElementsByTagName('body')[0];
        try {
            var selrange = this.getInnerDocument().selection.createRange();
        } catch(e) {
            return false;
        }
        var someelement = selrange.parentElement ? selrange.parentElement() : selrange.item(0);

        while (someelement.nodeName.toLowerCase() != 'body') {
            someelement = someelement.parentNode;
        };
        
        return someelement == editable_body;
    };

    this._clearSelection = function() {
        /* clear the last stored selection */
        this._previous_range = null;
    };

    this._filterContent = function(documentElement) {            
        /* pass the content through all the filters */
        // first copy all nodes to a Sarissa document so it's usable
        var xhtmldoc = Sarissa.getDomDocument();
        var doc = this._convertToSarissaNode(xhtmldoc, documentElement);
        // now pass it through all filters
        for (var i=0; i < this.filters.length; i++) {
            var doc = this.filters[i].filter(xhtmldoc, doc);
        };
        // fix some possible structural problems, such as an empty or missing head, title
        // or script or textarea tags without closing tag...
        this._fixXML(doc, xhtmldoc);
        return doc;
    };

    this.getXMLBody = function(transform) {
        var bodies = transform.getElementsByTagName('body');
        var data = '';
        for (var i = 0; i < bodies.length; i++) {
            data += this.serializer.serializeToString(bodies[i]);
        }
        return this.layoutsource(this.escapeEntities(data));
    };

    this.getHTMLBody = function() {
        var doc = this.getInnerDocument();
        var docel = doc.documentElement;
        var bodies = docel.getElementsByTagName('body');
        var data = '';
        for (var i = 0; i < bodies.length; i++) {
            data += bodies[i].innerHTML;
        }
        return this.layoutsource(this.escapeEntities(data));
    };

    // If we have multiple bodies this needs to remove the extras.
    this.setHTMLBody = function(text) {
        var doc = this.getInnerDocument().documentElement;
        var bodies = doc.getElementsByTagName('body');
        for (var i = 0; i < bodies.length-1; i++) {
            bodies[i].parentNode.removeChild(bodies[i]);
        }
        if (_SARISSA_IS_IE) { /* IE converts certain comments to visible text so strip them */
            text = text.replace(/<!--\[.*?-->/g, '');

        } else { /* Mozilla doesn't understand strong/em */
            var fixups = { 'strong':'b', 'em':'i' };

            text = text.replace(/<(\/?)(strong|em)\b([^>]*)>/gi, function(all,close,tag,attrs) {
                tag = fixups[tag.toLowerCase()];
                return '<'+close+tag+attrs+'>';
            });
        };
        text = text.replace(/<p>(<hr.*?>)<\/p>/g,'$1');
        bodies[bodies.length-1].innerHTML = text;
        /* Mozilla corrupts dragged images, so save the src attribute */
        var nodes = doc.getElementsByTagName('img');
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            node.setAttribute('kupu-src', node.src);
        };
    };

    this._fixXML = function(doc, document) {
        /* fix some structural problems in the XML that make it invalid XTHML */
        // find if we have a head and title, and if not add them
        var heads = doc.getElementsByTagName('head');
        var titles = doc.getElementsByTagName('title');
        if (!heads.length) {
            // assume we have a body, guess Kupu won't work without one anyway ;)
            var body = doc.getElementsByTagName('body')[0];
            var head = document.createElement('head');
            body.parentNode.insertBefore(head, body);
            var title = document.createElement('title');
            var titletext = document.createTextNode('');
            head.appendChild(title);
            title.appendChild(titletext);
        } else if (!titles.length) {
            var head = heads[0];
            var title = document.createElement('title');
            var titletext = document.createTextNode('');
            head.appendChild(title);
            title.appendChild(titletext);
        };
        // create a closing element for all elements that require one in XHTML
        var dualtons = ['a', 'abbr', 'acronym', 'address', 'applet', 
            'b', 'bdo', 'big', 'blink', 'blockquote', 
            'button', 'caption', 'center', 'cite', 
            'comment', 'del', 'dfn', 'dir', 'div',
            'dl', 'dt', 'em', 'embed', 'fieldset',
            'font', 'form', 'frameset', 'h1', 'h2',
            'h3', 'h4', 'h5', 'h6', 'i', 'iframe',
            'ins', 'kbd', 'label', 'legend', 'li',
            'listing', 'map', 'marquee', 'menu',
            'multicol', 'nobr', 'noembed', 'noframes',
            'noscript', 'object', 'ol', 'optgroup',
            'option', 'p', 'pre', 'q', 's', 'script',
            'select', 'small', 'span', 'strike', 
            'strong', 'style', 'sub', 'sup', 'table',
            'tbody', 'td', 'textarea', 'tfoot',
            'th', 'thead', 'title', 'tr', 'tt', 'u',
            'ul', 'xmp'];
        // XXX I reckon this is *way* slow, can we use XPath instead or
        // something to speed this up?
        for (var i=0; i < dualtons.length; i++) {
            var elname = dualtons[i];
            var els = doc.getElementsByTagName(elname);
            for (var j=0; j < els.length; j++) {
                var el = els[j];
                if (!el.hasChildNodes()) {
                    var child = document.createTextNode('');
                    el.appendChild(child);
                };
            };
        };
    };

    this.xhtmlvalid = new XhtmlValidation(this);

    this._convertToSarissaNode = function(ownerdoc, htmlnode) {
        /* Given a string of non-well-formed HTML, return a string of 
           well-formed XHTML.

           This function works by leveraging the already-excellent HTML 
           parser inside the browser, which generally can turn a pile 
           of crap into a DOM.  We iterate over the HTML DOM, appending 
           new nodes (elements and attributes) into a node.

           The primary problems this tries to solve for crappy HTML: mixed 
           element names, elements that open but don't close, 
           and attributes that aren't in quotes.  This can also be adapted 
           to filter out tags that you don't want and clean up inline styles.

           Inspired by Guido, adapted by Paul from something in usenet.
           Tag and attribute tables added by Duncan
        */
        return this.xhtmlvalid._convertToSarissaNode(ownerdoc, htmlnode);
    };

    this._fixupSingletons = function(xml) {
        return xml.replace(/<([^>]+)\/>/g, "<$1 />");
    };
    this._serializeOutputToString = function(transform) {
        // XXX need to fix this.  Sometimes a spurious "\n\n" text 
        // node appears in the transform, which breaks the Moz 
        // serializer on .xml
            
        if (this.config.strict_output) {
            var contents =  '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" ' + 
                            '"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">\n' + 
                            '<html xmlns="http://www.w3.org/1999/xhtml">' +
                            this.serializer.serializeToString(transform.getElementsByTagName("head")[0]) +
                            this.serializer.serializeToString(transform.getElementsByTagName("body")[0]) +
                            '</html>';
        } else {
            var contents = '<html>' + 
                            this.serializer.serializeToString(transform.getElementsByTagName("head")[0]) +
                            this.serializer.serializeToString(transform.getElementsByTagName("body")[0]) +
                            '</html>';
        };

        contents = this.escapeEntities(contents);

        if (this.config.compatible_singletons) {
            contents = this._fixupSingletons(contents);
        };
        
        return contents;
    };
    this.layoutsource = function(data) {
        data = data.replace(
            /\s*(<(p|div|h.|ul|ol|dl|menu|dir|pre|blockquote|address|center|table|thead|tbody|tfoot|tr|th|td))\b/ig, '\n$1');
        data = data.replace(
            /\s*(<\/(p|div|h.|ul|ol|dl|menu|dir|pre|blockquote|address|center|table|thead|tbody|tfoot|tr|th|td)>)\s*/ig, '$1\n');
        data = data.replace(/\<pre\>((?:.|\n)*?)\<\/pre\>/gm, function(s) {
            return s.replace(/<br\b[^>]*>/gi,'\n');
            });
        return data.strip();
    };
    this.escapeEntities = function(xml) {
        // XXX: temporarily disabled
        xml = xml.replace(/\xa0/g, '&nbsp;');
        return xml;
        // Escape non-ascii characters as entities.
//         return xml.replace(/[^\r\n -\177]/g,
//             function(c) {
//             return '&#'+c.charCodeAt(0)+';';
//         });
    };

    this.getFullEditor = function() {
        var fulleditor = this.getDocument().getEditable();
        while (!(/kupu-fulleditor/.test(fulleditor.className))) {
            fulleditor = fulleditor.parentNode;
        }
        return fulleditor;
    };
    // Control the className and hence the style for the whole editor.
    this.setClass = function(name) {
        this.getFullEditor().className += ' '+name;
    };
    
    this.clearClass = function(name) {
        var fulleditor = this.getFullEditor();
        fulleditor.className = fulleditor.className.replace(' '+name, '');
    };

    var busycount = 0;
    this.busy = function() {
        if (busycount <= 0) {
            this.setClass('kupu-busy');
        }
        busycount++;
    };
    this.notbusy = function(force) {
        busycount = force?0:busycount?busycount-1:0;
        if (busycount <= 0) {
            this.clearClass('kupu-busy');
        }
    };

    this.suspendEditing = function() {
        this._previous_range = this.getSelection().getRange();
        this.setClass('kupu-modal');
        for (var id in this.tools) {
            this.tools[id].disable();
        }
        if (this.getBrowserName() == "IE") {
            var body = this.getInnerDocument().getElementsByTagName('body')[0];
            body.setAttribute('contentEditable', 'false');
        } else {
            this.getDocument().execCommand('contentReadOnly', 'true');
        }
        this.suspended = true;
    };
    
    this.resumeEditing = function() {
        if (!this.suspended) {
            return;
        }
        this.clearClass('kupu-modal');
        for (var id in this.tools) {
            this.tools[id].enable();
        }
        if (this.getBrowserName() == "IE") {
            var body = this.getInnerDocument().getElementsByTagName('body')[0];
            body.setAttribute('contentEditable', 'true');
            this._restoreSelection();
        } else {
            var doc = this.getInnerDocument();
            this.getDocument().execCommand('contentReadOnly', 'false');
            doc.designMode = "On";
            this.focusDocument();
            this.getSelection().restoreRange(this._previous_range);
        }
        this.suspended = false;
    };
    this.newElement = function(tagName) {
        return newDocumentElement(this.getInnerDocument(), tagName, arguments);
    };
    this.newText = function(text) {
        return this.getInnerDocument().createTextNode(text);
    };
}


