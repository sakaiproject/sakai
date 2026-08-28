/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupumultieditor.js 3450 2004-03-28 11:07:30Z guido $

function KupuMultiEditor(documents, config, logger) {
    /* multiple kupus in one form */
    this.documents = documents; // array of documents
    this.config = config;
    this.log = logger;
    this.tools = {};

    this._designModeAttempts = 0;
    this._initialized = false;

    this._previous_range = null;

    // here's where the current active document will be stored
    this._current_document = documents[0];
    
    this.initialize = function() {
        this._initializeEventHandlers();
        this.getDocument().getWindow().focus();
        if (this.getBrowserName() == 'IE') {
            for (var i=0; i < this.documents.length; i++) {
                var body = this.documents[i].getDocument().getElementsByTagName('body')[0];
                body.setAttribute('contentEditable', 'true');
            };
            // provide an 'afterInit' method on KupuEditor.prototype
            // for additional bootstrapping (after editor init)
            this._initialized = true;
            if (this.afterInit) {
                this.afterInit();
            };
            this._saveSelection();
            this.logMessage(_('Editor initialized'));
        } else {
            this._setDesignModeWhenReady();
        };
    };

    this.updateStateHandler = function(event) {
        /* check whether the event is interesting enough to trigger the 
        updateState machinery and act accordingly */
        var interesting_codes = [8, 13, 37, 38, 39, 40, 46];
        if (event.type == 'click' || event.type == 'dblclick' || 
                event.type == 'select' ||
                (event.type == 'keyup' && 
                    interesting_codes.contains(event.keyCode))) {
            var target = event.target ? event.target : event.srcElement;
            // find the document targeted
            while (target.nodeType != 9) {
                target = target.parentNode;
            };
            var document = null;
            for (var i=0; i < this.documents.length; i++) {
                document = this.documents[i];
                if (document.getDocument() == target) {
                    break;
                };
            };
            if (!document) {
                alert('No document found!');
                return;
            };
            this._current_document = document;
            this.updateState(event);
        };
        // unfortunately it's not possible to do this on blur, since that's
        // too late. also (some versions of?) IE 5.5 doesn't support the
        // onbeforedeactivate event, which would be ideal here...
        if (this.getBrowserName() == 'IE') {
            this._saveSelection();
        };
    };

    this.saveDocument = function() {
        throw('Not supported, use prepareForm to attach the editor to a form');
    };

    this.getDocument = function() {
        /* return the current active document */
        return this._current_document;
    };

    this._initializeEventHandlers = function() {
        /* attache the event handlers to the iframe */
        for (var i=0; i < this.documents.length; i++) {
            var doc = this.documents[i].getDocument();
            this._addEventHandler(doc, "click", this.updateStateHandler, this);
            this._addEventHandler(doc, "keyup", this.updateStateHandler, this);
            if (this.getBrowserName() == "IE") {
                this._addEventHandler(doc, "dblclick", this.updateStateHandler, this);
                this._addEventHandler(doc, "select", this.updateStateHandler, this);
            };
        };
    };

    this._setDesignModeWhenReady = function() {
        this._designModeSetAttempts++;
        if (this._designModeSetAttempts > 25) {
            alert(_('Couldn\'t set design mode. Kupu will not work on this browser.'));
            return;
        };
        var should_retry = false;
        for (var i=0; i < this.documents.length; i++) {
            var document = this.documents[i];
            if (!document._designModeSet) {
                try {
                    this._setDesignMode(document);
                    document._designModeSet = true;
                } catch(e) {
                    should_retry = true;
                };
            };
        };
        if (should_retry) {
            timer_instance.registerFunction(this, this._setDesignModeWhenReady, 100);
        } else {
            // provide an 'afterInit' method on KupuEditor.prototype
            // for additional bootstrapping (after editor init)
            if (this.afterInit) {
                this.afterInit();
            };
            this._initialized = true;
        };
    };

    this._setDesignMode = function(doc) {
        doc.getDocument().designMode = "On";
        doc.execCommand("undo");
    };

    // XXX perhaps we can partially move this to a helper method to approve
    // code reuse?
    this.prepareForm = function(form, idprefix) {
        /* add some fields to the form and place the contents of the iframes 
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

        // set a default id
        if (!idprefix) {
            idprefix = 'kupu';
        };
        
        // pass the content through the filters
        this.logMessage(_("Starting HTML cleanup"));
        var contents = [];
        for (var i=0; i < this.documents.length; i++) {
            var transform = this._filterContent(this.documents[i].getDocument().documentElement);
            contents.push(this._serializeOutputToString(transform));
        };
        
        this.logMessage(_("Cleanup done, sending document to server"));
        
        // now create the form input, since IE 5.5 doesn't support the 
        // ownerDocument property we use window.document as a fallback (which
        // will almost by definition be correct).
        var document = form.ownerDocument ? form.ownerDocument : window.document;
        for (var i=0; i < contents.length; i++) {
            var ta = document.createElement('textarea');
            ta.style.visibility = 'hidden';
            var text = document.createTextNode(contents[i]);
            ta.appendChild(text);
            ta.setAttribute('name', idprefix + '_' + i);
            
            // and add it to the form
            form.appendChild(ta);
        };
    };
};

KupuMultiEditor.prototype = new KupuEditor;
