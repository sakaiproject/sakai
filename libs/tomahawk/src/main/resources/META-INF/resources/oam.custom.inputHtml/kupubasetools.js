/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupubasetools.js 928511 2010-03-28 22:53:14Z lu4242 $

//----------------------------------------------------------------------------
//
// Toolboxes
//
//  These are addons for Kupu, simple plugins that implement a certain 
//  interface to provide functionality and control view aspects.
//
//----------------------------------------------------------------------------

//----------------------------------------------------------------------------
// Superclasses
//----------------------------------------------------------------------------

function KupuTool() {
    /* Superclass (or actually more of an interface) for tools 
    
        Tools must implement at least an initialize method and an 
        updateState method, and can implement other methods to add 
        certain extra functionality (e.g. createContextMenuElements).
    */

    this.toolboxes = {};

    // private methods
    addEventHandler = addEventHandler;
};

// methods
KupuTool.prototype.initialize = function(editor) {
    /* Initialize the tool.

        Obviously this can be overriden but it will do
        for the most simple cases
    */
    this.editor = editor;
};

KupuTool.prototype.registerToolBox = function(id, toolbox) {
    /* register a ui box 
    
        Note that this needs to be called *after* the tool has been 
        registered to the KupuEditor
    */
    this.toolboxes[id] = toolbox;
    toolbox.initialize(this, this.editor);
};

KupuTool.prototype.updateState = function(selNode, event) {
    /* Is called when user moves cursor to other element 

        Calls the updateState for all toolboxes and may want perform
        some actions itself
    */
    for (var id in this.toolboxes) {
        this.toolboxes[id].updateState(selNode, event);
    };
};

KupuTool.prototype.enable = function() {
    // Called when the tool is enabled after a form is dismissed.
};

KupuTool.prototype.disable = function() {
    // Called when the tool is disabled (e.g. for a modal form)
};

function KupuToolBox() {
    /* Superclass for a user-interface object that controls a tool */
};

KupuToolBox.prototype.initialize = function(tool, editor) {
    /* store a reference to the tool and the editor */
    this.tool = tool;
    this.editor = editor;
};

KupuToolBox.prototype.updateState = function(selNode, event) {
    /* update the toolbox according to the current iframe's situation */
};

function noContextMenu(object) {
    /* Decorator for a tool to suppress the context menu */
    object.createContextMenuElements = function(selNode, event) {
        return [];
    };
    return object;
}

// Helper function for enabling/disabling tools
function kupuButtonDisable(button) {
    button = button || this.button;
    if (button) {
        button.disabled = "disabled";
        button.className += ' disabled';
    }
};

function kupuButtonEnable(button) {
    button = button || this.button;
    if (button) {
        button.disabled = "";
        button.className = button.className.replace(/ *\bdisabled\b/g, '');
    }
};

//----------------------------------------------------------------------------
// Implementations
//----------------------------------------------------------------------------

function KupuButton(buttonid, commandfunc, tool) {
    /* Base prototype for kupu button tools */
    if (arguments.length) {
        this.buttonid = buttonid;
        this.button = getFromSelector(buttonid);
        this.commandfunc = commandfunc;
        this.tool = tool;
        this.disable = kupuButtonDisable;
        this.enable = kupuButtonEnable;
    };
};

KupuButton.prototype = new KupuTool;

KupuButton.prototype.initialize = function(editor) {
    this.editor = editor;
    if (!this.button) return;
    addEventHandler(this.button, 'click', this.execCommand, this);
};

KupuButton.prototype.execCommand = function() {
    /* exec this button's command */
    this.commandfunc(this, this.editor, this.tool);
};

KupuButton.prototype.updateState = function(selNode, event) {
    /* override this in subclasses to determine whether a button should
        look 'pressed in' or not
    */
};

function KupuStateButton(buttonid, commandfunc, checkfunc, offclass, onclass) {
    /* A button that can have two states (e.g. pressed and
       not-pressed) based on CSS classes */
    this.buttonid = buttonid;
    this.button = getFromSelector(buttonid);
    this.commandfunc = commandfunc;
    this.checkfunc = checkfunc;
    this.offclass = offclass;
    this.onclass = onclass;
    this.pressed = false;

    this.execCommand = function() {
        /* exec this button's command */
        this.button.className = (this.pressed ? this.offclass : this.onclass);
        this.pressed = !this.pressed;
        this.editor.focusDocument();
        this.commandfunc(this, this.editor);
    };

    this.updateState = function(selNode, event) {
        /* check if we need to be clicked or unclicked, and update accordingly 
        
            if the state of the button should be changed, we set the class
        */
        if (!this.button) return;
        var currclass = this.button.className;
        var newclass = null;
        if (this.checkfunc(selNode, this, this.editor, event)) {
            newclass = this.onclass;
            this.pressed = true;
        } else {
            newclass = this.offclass;
            this.pressed = false;
        };
        if (currclass != newclass) {
            this.button.className = newclass;
        };
    };
};

KupuStateButton.prototype = new KupuButton;

/* Same as the state button, but the focusDocument call is delayed.
 * Mozilla&Firefox have a bug on windows which can cause a crash if you
 * change CSS positioning styles on an element which has focus.
 */
function KupuLateFocusStateButton(buttonid, commandfunc, checkfunc,
        offclass, onclass) {
    KupuStateButton.apply(this, [buttonid, commandfunc, checkfunc,
                                 offclass, onclass]);
}

KupuLateFocusStateButton.prototype = new KupuStateButton;

KupuLateFocusStateButton.prototype.execCommand = function() {
    /* exec this button's command */
    this.button.className = (this.pressed ? this.offclass : this.onclass);
    this.pressed = !this.pressed;
    this.commandfunc(this, this.editor);
    this.editor.focusDocument();
};

function KupuRemoveElementButton(buttonid, element_name, cssclass) {
    /* A button specialized in removing elements in the current node
       context. Typical usages include removing links, images, etc. */
    this.button = getFromSelector(buttonid);
    this.element_name = element_name;
    this.onclass = 'invisible';
    this.offclass = cssclass;
    this.pressed = false;
};

KupuRemoveElementButton.prototype = new KupuStateButton;

KupuRemoveElementButton.prototype.commandfunc = function(button, editor) {
    editor.focusDocument();
    editor.removeNearestParentOfType(editor.getSelectedNode(), this.element_name);
    editor.updateState();
};

KupuRemoveElementButton.prototype.checkfunc = function(currnode, button,
        editor, event) {
    var element = editor.getNearestParentOfType(currnode, this.element_name);
    return (element ? false : true);
};

function KupuUI(textstyleselectid) {
    /* View 
    
        This is the main view, which controls most of the toolbar buttons.
        Even though this is probably never going to be removed from the view,
        it was easier to implement this as a plain tool (plugin) as well.
    */
    
    // attributes
    this.tsselect = getFromSelector(textstyleselectid);
    this.paraoptions = [];
    this.tableoptions = [];
    this.styleoptions = [];
    this.tableoffset = 0;
    this.styleoffset = 0;
    this.tablegrp = null;
    this.optionstate = -1;
    this.otherstyle = null;
    this.tablestyles = {};
    this.charstyles = {};
    this.styles = {}; // use an object here so we can use the 'in' operator later on
    this.blocktagre = /^(p|div|h.|ul|ol|dl|menu|dir|pre|blockquote|address|center)$/i;
    this.spanre = /^span\b/i;
    this.tblre = /^thead|tbody|table|t[rdh]\b/i;
};


KupuUI.prototype = new KupuTool;

KupuUI.prototype.initialize = function(editor) {
    /* initialize the ui like tools */
    this.editor = editor;
    this.cleanStyles();
    this.enableOptions(false);
    if (this.tsselect) {
        this._selectevent = addEventHandler(this.tsselect, 'change', this.setTextStyleHandler, this);
    }
};

KupuUI.prototype.getStyles = function() {
    if (!this.paraoptions) {
        this.cleanStyles();
    }
    return [ this.paraoptions, this.tableoptions ];
};

KupuUI.prototype.setTextStyleHandler = function(event) {
    this.setTextStyle(this.tsselect.options[this.tsselect.selectedIndex].value);
};

// event handlers
KupuUI.prototype.basicButtonHandler = function(action) {
    /* event handler for basic actions (toolbar buttons) */
    this.editor.execCommand(action);
    this.editor.updateState();
};

KupuUI.prototype.saveButtonHandler = function() {
    /* handler for the save button */
    this.editor.saveDocument();
};

KupuUI.prototype.saveAndExitButtonHandler = function(redirect_url) {
    /* save the document and, if successful, redirect */
    this.editor.saveDocument(redirect_url);
};

KupuUI.prototype.cutButtonHandler = function() {
    try {
        this.editor.execCommand('Cut');
    } catch (e) {
        if (this.editor.getBrowserName() == 'Mozilla') {
            alert(_('Cutting from JavaScript is disabled on your Mozilla due to security settings. For more information, read http://www.mozilla.org/editor/midasdemo/securityprefs.html'));
        } else {
            throw e;
        };
    };
    this.editor.updateState();
};

KupuUI.prototype.copyButtonHandler = function() {
    try {
        this.editor.execCommand('Copy');
    } catch (e) {
        if (this.editor.getBrowserName() == 'Mozilla') {
            alert(_('Copying from JavaScript is disabled on your Mozilla due to security settings. For more information, read http://www.mozilla.org/editor/midasdemo/securityprefs.html'));
        } else {
            throw e;
        };
    };
    this.editor.updateState();
};

KupuUI.prototype.pasteButtonHandler = function() {
    try {
        this.editor.execCommand('Paste');
    } catch (e) {
        if (this.editor.getBrowserName() == 'Mozilla') {
            alert(_('Pasting from JavaScript is disabled on your Mozilla due to security settings. For more information, read http://www.mozilla.org/editor/midasdemo/securityprefs.html'));
        } else {
            throw e;
        };
    };
    this.editor.updateState();
};

KupuUI.prototype.cleanStyles = function() {
    if (!this.tsselect) return;
    var options = this.tsselect.options;
    var parastyles = this.styles;
    var tablestyles = this.tablestyles;
    var charstyles = this.charstyles;
    
    var normal = ['Normal', 'p|'];
    var td = ['Plain Cell', 'td|'];
    var nostyle = ['(remove style)', ''];

    var opts = [];
    while (options.length) {
        var opt = options[0];
        options[0] = null;
        var v = opt.value;
        if (v.indexOf('|') > -1) {
            var split = v.split('|');
            v = split[0].toLowerCase() + "|" + split[1];
        } else {
            v = v.toLowerCase()+"|";
        };
        var optarray = [opt.text, v];
        if (v=='td|') {
            td = optarray;
        } else if (v=='p|') {
            normal = optarray;
        } else if (v=='') {
            nostyle = optarray;
        } else {
            opts.push([opt.text,v]);
        }
    }
    this.tableoptions.push(td);
    tablestyles[td[1]] = 0;
    this.paraoptions.push(normal);
    parastyles[normal[1]] = 0;

    for (var i = 0; i < opts.length; i++) {
        optarray = opts[i];
        v = optarray[1];

        if (this.spanre.test(v)) {
            charstyles[v] = this.styleoptions.length;
            this.styleoptions.push(optarray);
        } else if (this.tblre.test(v)) {
            tablestyles[v] = this.tableoptions.length;
            this.tableoptions.push(optarray);
        } else {
            parastyles[v] = this.paraoptions.length;
            this.paraoptions.push(optarray);
        };
    };
    this.paraoptions.push(nostyle);
    this.styleoffset = this.paraoptions.length;
    this.tableoffset = this.styleoffset + this.styleoptions.length;
};

// Remove otherstyle and switch to appropriate style set.
KupuUI.prototype.enableOptions = function(inTable) {
    if (!this.tsselect) return;
    var select = this.tsselect;
    var options = select.options;
    if (this.otherstyle) {
        options[0] = null;
        this.otherstyle = null;
    }
    if (this.optionstate == inTable) return; /* No change */

    // while (select.firstChild) select.removeChild(select.firstChild);

    function option(info) {
        return newElement('option', {'value': info[1]}, [info[0]]);
    }
    if (this.optionstate==-1) {
        for (var i = 0; i < this.paraoptions.length; i++) {
            select.appendChild(option(this.paraoptions[i]));
        }
        if (this.styleoptions.length) {
            var grp = document.createElement('optgroup');
            grp.label = 'Character styles';
            for (var i = 0; i < this.styleoptions.length; i++) {
                grp.appendChild(option(this.styleoptions[i]));
            }
            select.appendChild(grp);
        }
    }
    if (inTable) {
        var grp = (this.tablegrp = document.createElement('optgroup'));
        grp.label = 'Table elements';
        for (var i = 0; i < this.tableoptions.length; i++) {
            grp.appendChild(option(this.tableoptions[i]));
        }
        select.appendChild(grp);
    } else {
        while (select.options[this.tableoffset]) {
            select.options[this.tableoffset] = null;
        };
        if (this.tablegrp) {
            select.removeChild(this.tablegrp);
            this.tablegrp = null;
        };
    };
    this.optionstate = inTable;
};

KupuUI.prototype.setIndex = function(currnode, tag, index, styles) {
    var className = currnode.className;
    this.styletag = tag;
    this.classname = className;
    var style = tag+'|'+className;

    if (style in styles) {
        return styles[style];
    } else if (!className && tag in styles) {
        return styles[tag];
    }
    return index;
};

KupuUI.prototype.nodeStyle = function(node) {
    var currnode = node;
    var index = -1;
    this.styletag = undefined;
    this.classname = '';

    // Set the table state correctly
    this.intable = false;

    while(currnode) {
        var tag = currnode.nodeName;
        if (/^body$/i.test(tag)) break;
        if (this.tblre.test(tag)) {
            this.intable = true;
            break;
        };
        currnode = currnode.parentNode;
    };
    currnode = node;
    while (currnode) {
        var tag = currnode.nodeName.toLowerCase();

        if (/^body$/.test(tag)) {
            if (!this.styletag) {
                // Forced style messes up in Firefox: return -1 to
                // indicate no style 
                return -1;
            }
            break;
        }
        if (this.spanre.test(tag)) {
            index = this.setIndex(currnode, tag, index, this.charstyles);
            if (index >= 0) return index+this.styleoffset; // span takes priority
        } else if (this.blocktagre.test(tag)) {
            index = this.setIndex(currnode, tag, index, this.styles);
        } else if (this.tblre.test(tag)) {
            if (index > 0) return index; // block or span takes priority.
            index = this.setIndex(currnode, tag, index, this.tablestyles);
            if (index >= 0 || tag=='table') {
                return index+this.tableoffset; // Stop processing if in a table
            }
        }
        currnode = currnode.parentNode;
    }
    return index;
};

KupuUI.prototype.updateState = function(selNode) {
    /* set the text-style pulldown */

    // first get the nearest style
    // search the list of nodes like in the original one, break if we encounter a match,
    // this method does some more than the original one since it can handle commands in
    // the form of '<style>|<classname>' next to the plain
    // '<style>' commands
    if (!this.tsselect) return;
    var index = undefined;
    var mixed = false;
    var styletag, classname;

    var selection = this.editor.getSelection();

    for (var el=selNode.firstChild; el; el=el.nextSibling) {
        if (el.nodeType==1 && selection.containsNode(el)) {
            var i = this.nodeStyle(el);
            if (index===undefined) {
                index = i;
                styletag = this.styletag;
                classname = this.classname;
            }
            if (index != i || styletag!=this.styletag || classname != this.classname) {
                mixed = true;
                break;
            }
        }
    };

    if (index===undefined) {
        index = this.nodeStyle(selNode);
    }
    this.enableOptions(this.intable);

    if (index < 0 || mixed) {
        if (mixed) {
            var caption = 'Mixed styles';
        } else if (this.styletag) {
            var caption = 'Other: ' + this.styletag + ' '+ this.classname;
        } else {
            var caption = '<no style>';
        }

        var opt = newElement('option');
        opt.text = caption;
        this.otherstyle = opt;
        this.tsselect.options.add(opt,0);
        index = 0;
    }
    this.tsselect.selectedIndex = Math.max(index,0);
};

KupuUI.prototype._cleanNode = function(node, preserveEmpty) {
            /* Clean up a block style node (e.g. P, DIV, Hn)
             * Remove trailing whitespace, then also remove up to one
             * trailing <br>
             * If the node is now empty and no preserveEmpty, remove the node itself.
             */
    function stripspace() {
        var c;
        while ((c = node.lastChild) && c.nodeType==3 && (/^\s*$/.test(c.data))) {
            node.removeChild(c);
        }
    }
    stripspace();
    var c = node.lastChild;
    if (c && c.nodeType==1 && c.tagName=='BR') {
        node.removeChild(c);
    }
    stripspace();
    if (node.childNodes.length==0 && !preserveEmpty) {
        node.parentNode.removeChild(node);
    };
};

KupuUI.prototype._cleanCell = function(eltype, classname, strip) {
    var alttype=eltype=='TD'?'TH':eltype=='TH'?'TD':null;
    
    var selNode = this.editor.getSelectedNode(true);
    var el = this.editor.getNearestParentOfType(selNode, eltype);
    if (!el && alttype) {
        // Maybe changing type
        el = this.editor.getNearestParentOfType(selNode, alttype);
    }

    //either the selection is inside a cell, spans cells, or includes
    //a collection of cells

    //first, if contained in a cell
    
    if (el) {
        if (eltype != el.tagName) {
                // Change node type.
            var node = el.ownerDocument.createElement(eltype);
            var parent = el.parentNode;
            parent.insertBefore(node, el);
            while (el.firstChild) {
                node.appendChild(el.firstChild);
            }
            parent.removeChild(el);
            el = node;
        }
            // now set the classname
        this._setClass(el, classname);
        if (strip && el.childNodes.length==1) {
            var node = el.firstChild;
            if (this.blocktagre.test(node.nodeName)) {
                for (var n = node.firstChild; n;) {
                    var nxt = n.nextSibling;
                    el.insertBefore(n, node); // Move nodes out of block
                    n = nxt;
                };
                nxt = node.nextSibling;
                el.removeChild(node);
                node = nxt;
            };
        };
    } else {
        //otherwise, find all cells that intersect the selection
        var selection = this.editor.getSelection();
        var nodes = selNode.getElementsByTagName(eltype);

        var cellNodes = [];
        for (var i = 0; i < nodes.length; i++) {
            cellNodes.push(nodes.item(i));
        };
        if (alttype) {
            nodes = selNode.getElementsByTagName(alttype);
            for (var i = 0; i < nodes.length; i++) {
                cellNodes.push(nodes.item(i));
            };
        };
        
        for (var i = 0; i < cellNodes.length; i++) {
            el = cellNodes[i];

            if(selection.intersectsNode(el)) {
                if (eltype != el.tagName) {
                    // Change node type.
                    var node = el.ownerDocument.createElement(eltype);
                    var parent = el.parentNode;
                    parent.insertBefore(node, el);
                    while (el.firstChild) {
                        node.appendChild(el.firstChild);
                    };
                    parent.removeChild(el);
                    el = node;
                };
                this._setClass(el, classname);
            }
        }
    }
};

KupuUI.prototype._setClass = function(el, classname) {
    var parent = el.parentNode;
    if (parent.tagName=='DIV') {
        // fixup buggy formatting
        var gp = parent.parentNode;
        if (el != parent.firstChild) {
            var previous = parent.cloneNode(false);
            while (el != parent.firstChild) {
                previous.appendChild(parent.firstChild);
            }
            gp.insertBefore(previous, parent);
            this._cleanNode(previous);
        }
        gp.insertBefore(el, parent);
        this._cleanNode(parent);
    };
    // now set the classname
    if (classname) {
        el.className = classname;
    } else {
        el.removeAttribute("class");
        el.removeAttribute("className");
    }
};

KupuUI.prototype._removeStyle = function() {
    var self = this;
    function needbreak(e) {
        if (isblock && e) {
            if (self.blocktagre.test(e.nodeName) || (/^br$/i.test(e.nodeName))) return;
            parent.insertBefore(ed.newElement('br'), n);
        }
    }
    var n = this.editor.getSelectedNode(true);
    var ed = this.editor;
    while(n) {
        var tag = n.nodeName.toLowerCase();
        var isblock = this.blocktagre.test(tag);
        if (this.tblre.test(tag) && n.className) {
            n.removeAttribute("class");
            n.removeAttribute("className");
            return;
        }
        if (isblock || tag == 'span') {
            var parent = n.parentNode;
            var el;
            needbreak(n.previousSibling);
            while ((el = n.firstChild)) {
                parent.insertBefore(el, n);
            }
            needbreak(n.nextSibling);
            parent.removeChild(n);
            return;
        }
        n = n.parentNode;
    };
};

KupuUI.prototype.setTextStyle = function(style, noupdate) {
    /* parse the argument into a type and classname part
       generate a block element accordingly 
    */
    var classname = '';
    var eltype = style.toUpperCase();
    if (style.indexOf('|') > -1) {
        style = style.split('|');
        eltype = style[0].toUpperCase();
        classname = style[1];
    };

    var doc = this.editor.getDocument();
    var command = eltype;
        // first create the element, then find it and set the classname
    if (this.editor.getBrowserName() == 'IE') {
        command = '<' + eltype + '>';
    };
    if (!style) {
        this._removeStyle();
    } else if (this.tblre.test(eltype)) {
        this._cleanCell(eltype, classname);
    } else if (eltype=='SPAN') {
        doc.execCommand('removeformat', null);
        if (this.editor.getBrowserName()=='IE') {
            // removeformat is broken in IE: it doesn't remove span
            // tags
            var selNode = this.editor.getSelectedNode();
            var selection = this.editor.getSelection();
            var elements = selNode.getElementsByTagName('span');
            for (var i = 0; i < elements.length; i++) {
                var span = elements[i];
                if (selection.containsNode(span)) {
                    var parent = span.parentNode;
                    while (span.firstChild) {
                        parent.insertBefore(span.firstChild, span);
                    };
                    parent.removeChild(span);
                };
            };
        }
        if (classname) {
            doc.execCommand('fontsize', '2');
            // Now convert font tags to spans
            var inner = doc.getDocument();
            var elements = inner.getElementsByTagName('FONT');
            while (elements.length > 0) {
                var font = elements[0];
                var span = inner.createElement('SPAN');
                span.className = classname;
                var parent = font.parentNode;
                parent.replaceChild(span, font);
                while (font.firstChild) {
                    span.appendChild(font.firstChild);
                };
            };
        };
    }
    else {
        doc.execCommand('formatblock', command);

            // now get a reference to the element just added
        var selNode = this.editor.getSelectedNode(true);
        var el = this.editor.getNearestParentOfType(selNode, eltype);
        if (el) {
            this._setClass(el, classname);
        } else {
            var selection = this.editor.getSelection();
            var elements = selNode.getElementsByTagName(eltype);
            for (var i = 0; i < elements.length; i++) {
                el = elements[i];
                if (selection.containsNode(el)) {
                    this._setClass(el, classname);
                }
            }
        }
    }
    if (el) {
        this.editor.getSelection().selectNodeContents(el);
    }
    if (!noupdate) {
        this.editor.updateState();
    }
};

KupuUI.prototype.createContextMenuElements = function(selNode, event) {
    var ret = [];
    ret.push(new ContextMenuElement(_('Cut'), 
                this.cutButtonHandler, this));
    ret.push(new ContextMenuElement(_('Copy'), 
                this.copyButtonHandler, this));
    ret.push(new ContextMenuElement(_('Paste'), 
                this.pasteButtonHandler, this));
    return ret;
};

KupuUI.prototype.disable = function() {
    if (this.tsselect) this.tsselect.disabled = "disabled";
};

KupuUI.prototype.enable = function() {
    if (this.tsselect) this.tsselect.disabled = "";
};

function ColorchooserTool(fgcolorbuttonid, hlcolorbuttonid, colorchooserid) {
    /* the colorchooser */
    
    this.fgcolorbutton = getFromSelector(fgcolorbuttonid);
    this.hlcolorbutton = getFromSelector(hlcolorbuttonid);
    this.ccwindow = getFromSelector(colorchooserid);
    this.command = null;
}

ColorchooserTool.prototype = new KupuTool;

ColorchooserTool.prototype.initialize = function(editor) {
    /* attach the event handlers */
    this.editor = editor;
    if (!(this.fgcolorbutton && this.hlcolorbutton && this.ccwindow)) return;
    this.createColorchooser(this.ccwindow);

    addEventHandler(this.fgcolorbutton, "click", this.openFgColorChooser, this);
    addEventHandler(this.hlcolorbutton, "click", this.openHlColorChooser, this);
    addEventHandler(this.ccwindow, "click", this.chooseColor, this);
    this.hide();
};

ColorchooserTool.prototype.updateState = function(selNode) {
    /* update state of the colorchooser */
    this.hide();
};

ColorchooserTool.prototype.openFgColorChooser = function() {
    /* event handler for opening the colorchooser */
    this.command = "forecolor";
    this.show();
};

ColorchooserTool.prototype.openHlColorChooser = function() {
    /* event handler for closing the colorchooser */
    if (this.editor.getBrowserName() == "IE") {
        this.command = "backcolor";
    } else {
        this.command = "hilitecolor";
    }
    this.show();
};

ColorchooserTool.prototype.chooseColor = function(event) {
    /* event handler for choosing the color */
    var target = _SARISSA_IS_MOZ ? event.target : event.srcElement;
    var cell = this.editor.getNearestParentOfType(target, 'td');
    var ed = this.editor;
    var doc = ed.getDocument();
    ed.execCommand('styleWithCSS', true);
    doc.execCommand(this.command, cell.bgColor);
    ed.execCommand('styleWithCSS', false);
    // this.editor.execCommand(this.command, cell.bgColor);
    this.hide();

    this.editor.logMessage(_('Color chosen'));
};

ColorchooserTool.prototype.show = function(command) {
    /* show the colorchooser */
    this.ccwindow.style.display = "block";
};

ColorchooserTool.prototype.hide = function() {
    /* hide the colorchooser */
    this.ccwindow.style.display = "none";
};

ColorchooserTool.prototype.createColorchooser = function(table) {
    /* create the colorchooser table */
    
    var chunks = ['00', '33', '66', '99', 'CC', 'FF'];
    table.setAttribute('id', 'kupu-colorchooser-table');
    table.style.borderWidth = '2px';
    table.style.borderStyle = 'solid';
    table.style.position = 'absolute';
    table.style.cursor = 'default';
    table.style.display = 'none';

    var tbody = document.createElement('tbody');

    for (var i=0; i < 6; i++) {
        var tr = document.createElement('tr');
        var r = chunks[i];
        for (var j=0; j < 6; j++) {
            var g = chunks[j];
            for (var k=0; k < 6; k++) {
                var b = chunks[k];
                var color = '#' + r + g + b;
                var td = document.createElement('td');
                td.setAttribute('bgColor', color);
                td.style.backgroundColor = color;
                td.style.borderWidth = '1px';
                td.style.borderStyle = 'solid';
                td.style.fontSize = '1px';
                td.style.width = '10px';
                td.style.height = '10px';
                var text = document.createTextNode('\u00a0');
                td.appendChild(text);
                tr.appendChild(td);
            }
        }
        tbody.appendChild(tr);
    }
    table.appendChild(tbody);

    return table;
};

ColorchooserTool.prototype.enable = function() {
    kupuButtonEnable(this.fgcolorbutton);
    kupuButtonEnable(this.hlcolorbutton);
};

ColorchooserTool.prototype.disable = function() {
    kupuButtonDisable(this.fgcolorbutton);
    kupuButtonDisable(this.hlcolorbutton);
};

function PropertyTool(titlefieldid, descfieldid) {
    /* The property tool */

    this.titlefield = getFromSelector(titlefieldid);
    this.descfield = getFromSelector(descfieldid);
};

PropertyTool.prototype = new KupuTool;

PropertyTool.prototype.initialize = function(editor) {
    /* attach the event handlers and set the initial values */
    this.editor = editor;
    addEventHandler(this.titlefield, "change", this.updateProperties, this);
    addEventHandler(this.descfield, "change", this.updateProperties, this);
    
    // set the fields
    var heads = this.editor.getInnerDocument().getElementsByTagName('head');
    if (!heads[0]) {
        this.editor.logMessage(_('No head in document!'), 1);
    } else {
        var head = heads[0];
        var titles = head.getElementsByTagName('title');
        if (titles.length) {
            this.titlefield.value = titles[0].text;
        }
        var metas = head.getElementsByTagName('meta');
        if (metas.length) {
            for (var i=0; i < metas.length; i++) {
                var meta = metas[i];
                if (meta.getAttribute('name') && 
                        meta.getAttribute('name').toLowerCase() == 
                        'description') {
                    this.descfield.value = meta.getAttribute('content');
                    break;
                }
            }
        }
    }
};

PropertyTool.prototype.updateProperties = function() {
    /* event handler for updating the properties form */
    var doc = this.editor.getInnerDocument();
    var heads = doc.getElementsByTagName('HEAD');
    if (!heads) {
        this.editor.logMessage(_('No head in document!'), 1);
        return;
    }

    var head = heads[0];

    // set the title
    var titles = head.getElementsByTagName('title');
    if (!titles) {
        var title = doc.createElement('title');
        var text = doc.createTextNode(this.titlefield.value);
        title.appendChild(text);
        head.appendChild(title);
    } else {
        var title = titles[0];
        // IE6 title has no children, and refuses appendChild.
        // Delete and recreate the title.
        if (title.childNodes.length == 0) {
            title.removeNode(true);
            title = doc.createElement('title');
            title.innerText = this.titlefield.value;
            head.appendChild(title);
        } else {
            title.childNodes[0].nodeValue = this.titlefield.value;
        }
    }
    document.title = this.titlefield.value;

    // let's just fulfill the usecase, not think about more properties
    // set the description
    var metas = doc.getElementsByTagName('meta');
    var descset = 0;
    for (var i=0; i < metas.length; i++) {
        var meta = metas[i];
        if (meta.getAttribute('name') && 
                meta.getAttribute('name').toLowerCase() == 'description') {
            meta.setAttribute('content', this.descfield.value);
            descset = 1;
        }
    }

    if (!descset) {
        var meta = doc.createElement('meta');
        meta.setAttribute('name', 'description');
        meta.setAttribute('content', this.descfield.value);
        head.appendChild(meta);
    }

    this.editor.logMessage(_('Properties modified'));
};

function LinkTool(popupurl, popupwidth, popupheight, popupprops) {
    /* Add and update hyperlinks */
    this.popupurl = popupurl || 'kupupopups/link.html';
    this.popupwidth = popupwidth || 300;
    this.popupheight = popupheight || 200;
    this.popupprops = popupprops || '';
}

LinkTool.prototype = new KupuTool;
    
LinkTool.prototype.initialize = function(editor) {
    this.editor = editor;
};

LinkTool.prototype.createLinkHandler = function(event) {
    /* create a link according to a url entered in a popup */
    var linkWindow = openPopup(this.popupurl, this.popupwidth,
                               this.popupheight, this.popupprops);
    linkWindow.linktool = this;
    linkWindow.focus();
};

LinkTool.prototype.updateLink = function (linkel, url, type, name, target, title, className, bForce) {
    if (type && type == 'anchor') {
        linkel.removeAttribute('href');
        linkel.setAttribute('name', name);
    } else {
        linkel.href = url;
        if (linkel.innerHTML == "" || (bForce && linkel.innerHTML==url)) {
            var doc = this.editor.getInnerDocument();
            while (linkel.firstChild) { linkel.removeChild(linkel.firstChild); };
            linkel.appendChild(doc.createTextNode(title || url));
        }
        if (title) {
            linkel.title = title;
        } else {
            linkel.removeAttribute('title');
        }
        if (target) {
            linkel.setAttribute('target', target);
        }
        else {
            linkel.removeAttribute('target');
        };
        if (className===undefined) {
            linkel.removeAttribute('className');
        } else {
            linkel.className = className;
        }
        linkel.style.color = this.linkcolor;
    };
};

LinkTool.prototype.formatSelectedLink = function(url, type, name, target, title, className, bForce) {
    var currnode = this.editor.getSelectedNode();

    // selection inside link
    var linkel = this.editor.getNearestParentOfType(currnode, 'A');
    if (linkel) {
        this.updateLink(linkel, url, type, name, target, title, className, bForce);
        return true;
    }

    if (currnode.nodeType!=1) return false;

    // selection contains links
    var linkelements = currnode.getElementsByTagName('A');
    var selection = this.editor.getSelection();
    var containsLink = false;
    for (var i = 0; i < linkelements.length; i++) {
        linkel = linkelements[i];
        if (selection.containsNode(linkel)) {
            this.updateLink(linkel, url, type, name, target, title, className, bForce);
            containsLink = true;
        }
    };
    return containsLink;
};

// Can create a link in the following circumstances:
//   The selection is inside a link:
//      just update the link attributes.
//   The selection contains links:
//      update the attributes of the contained links
//   No links inside or outside the selection:
//      create a link around the selection
//   No selection:
//      insert a link containing the title
//
// the order of the arguments is a bit odd here because of backward
// compatibility
LinkTool.prototype.createLink = function(url, type, name, target, title, className) {
    url = url.strip();
    if (!url) {
        this.deleteLink();
        return;
    };
    if (!this.formatSelectedLink(url, type, name, target, title, className)) {
        // No links inside or outside.
        this.editor.execCommand("CreateLink", url);
        if (!this.formatSelectedLink(url, type, name, target, title, className, true)) {
            // Insert link with no text selected, insert the title
            // or URI instead.
            var doc = this.editor.getInnerDocument();
            var linkel = doc.createElement("a");
            linkel.setAttribute('href', url);
            linkel.setAttribute('class', className || 'generated');
            this.editor.getSelection().replaceWithNode(linkel, true);
            this.updateLink(linkel, url, type, name, target, title, className);
        };
    }
};

LinkTool.prototype.deleteLink = function() {
    /* delete the current link */
    var currnode = this.editor.getSelectedNode();
    var linkel = this.editor.getNearestParentOfType(currnode, 'a');
    if (!linkel) {
        this.editor.logMessage(_('Not inside link'));
        return;
    };
    while (linkel.childNodes.length) {
        linkel.parentNode.insertBefore(linkel.childNodes[0], linkel);
    };
    linkel.parentNode.removeChild(linkel);
};

LinkTool.prototype.createContextMenuElements = function(selNode, event) {
    /* create the 'Create link' or 'Remove link' menu elements */
    var ret = [];
    var link = this.editor.getNearestParentOfType(selNode, 'a');
    if (link) {
        ret.push(new ContextMenuElement(_('Delete link'), this.deleteLink, this));
    } else {
        ret.push(new ContextMenuElement(_('Create link'), this.createLinkHandler, this));
    };
    return ret;
};

function LinkToolBox(inputid, buttonid, toolboxid, plainclass, activeclass) {
    /* create and edit links */
    
    this.input = getFromSelector(inputid);
    this.button = getFromSelector(buttonid);
    this.toolboxel = getFromSelector(toolboxid);
    this.plainclass = plainclass;
    this.activeclass = activeclass;
};

LinkToolBox.prototype = new LinkToolBox;
    
LinkToolBox.prototype.initialize = function(tool, editor) {
    /* attach the event handlers */
    this.tool = tool;
    this.editor = editor;
    if (!this.button) return;
    addEventHandler(this.input, "blur", this.updateLink, this);
    addEventHandler(this.button, "click", this.addLink, this);
};

LinkToolBox.prototype.updateState = function(selNode) {
    /* if we're inside a link, update the input, else empty it */
    var linkel = this.editor.getNearestParentOfType(selNode, 'a');
    if (linkel) {
        // check first before setting a class for backward compatibility
        if (this.toolboxel) {
            this.toolboxel.className = this.activeclass;
        };
        this.input.value = linkel.getAttribute('href');
    } else {
        // check first before setting a class for backward compatibility
        if (this.toolboxel) {
            this.toolboxel.className = this.plainclass;
        };
        this.input.value = '';
    }
};

LinkToolBox.prototype.addLink = function(event) {
    /* add a link */
    var url = this.input.value;
    this.editor.focusDocument();
    this.tool.createLink(url);
    this.editor.updateState();
};

LinkToolBox.prototype.updateLink = function() {
    /* update the current link */
    var currnode = this.editor.getSelectedNode();
    var linkel = this.editor.getNearestParentOfType(currnode, 'A');
    if (!linkel) {
        return;
    }

    var url = this.input.value;
    linkel.setAttribute('href', url);

    this.editor.updateState();
};

function ImageTool(popupurl, popupwidth, popupheight, popupprops) {
    /* Image tool to add images */
    this.popupurl = popupurl || 'kupupopups/image.html';
    this.popupwidth = popupwidth || 300;
    this.popupheight = popupheight || 200;
    this.popupprops = popupprops || '';
};

ImageTool.prototype = new KupuTool;

ImageTool.prototype.initialize = function(editor) {
    /* attach the event handlers */
    this.editor = editor;
};

ImageTool.prototype.createImageHandler = function(event) {
    /* create an image according to a url entered in a popup */
    var imageWindow = openPopup(this.popupurl, this.popupwidth,
                                this.popupheight, this.popupprops);
    imageWindow.imagetool = this;
    imageWindow.focus();
};

ImageTool.prototype.newNode = function(name, obj) {
    var ed = this.editor;
    var currobj = ed.getNearestParentOfType(ed.getSelectedNode(), name);
    if (currobj) {
        var p = currobj.parentNode;
        p.insertBefore(obj, currobj);
        p.removeChild(currobj);
        return obj;
    } else {
        return ed.insertNodeAtSelection(obj, 1);
    }
};

ImageTool.prototype.createImage = function(url, alttext, imgclass) {
    /* create an image */
    var img = this.editor.getInnerDocument().createElement('img');
    img.src = url;
    img.setAttribute('kupu-src', url);
    img.removeAttribute('height');
    img.removeAttribute('width');
    if (alttext) {
        img.alt = alttext;
    };
    if (imgclass) {
        img.className = imgclass;
    };
    this.newNode('IMG', img);
    return img;
};

ImageTool.prototype.create_flash = function(url, alttext, className, width, height) {
    var ed = this.editor;
    var obj = ed.newElement('object',
        {src:url, alt:alttext, className:className, width:width,
         height:height, type:'application/x-shockwave-flash',
         'data':url},
        [ed.newElement('param', {name:'movie', value:url})]);
    this.newNode('OBJECT', obj);
};

ImageTool.prototype.setImageClass = function(imgclass) {
    /* set the class of the selected image */
    var currnode = this.editor.getSelectedNode();
    var currimg = this.editor.getNearestParentOfType(currnode, 'IMG');
    if (currimg) {
        currimg.className = imgclass;
    };
};

ImageTool.prototype.createContextMenuElements = function(selNode, event) {
    return [new ContextMenuElement(_('Create image'), this.createImageHandler,
                                   this)];
};

function ImageToolBox(inputfieldid, insertbuttonid, classselectid, toolboxid,
                      plainclass, activeclass) {
    /* toolbox for adding images */

    this.inputfield = getFromSelector(inputfieldid);
    this.insertbutton = getFromSelector(insertbuttonid);
    this.classselect = getFromSelector(classselectid);
    this.toolboxel = getFromSelector(toolboxid);
    this.plainclass = plainclass;
    this.activeclass = activeclass;
};

ImageToolBox.prototype = new KupuToolBox;

ImageToolBox.prototype.initialize = function(tool, editor) {
    this.tool = tool;
    this.editor = editor;
    addEventHandler(this.classselect, "change", this.setImageClass, this);
    addEventHandler(this.insertbutton, "click", this.addImage, this);
};

ImageToolBox.prototype.updateState = function(selNode, event) {
    /* update the state of the toolbox element */
    var imageel = this.editor.getNearestParentOfType(selNode, 'img');
    if (imageel) {
        // check first before setting a class for backward compatibility
        if (this.toolboxel) {
            this.toolboxel.className = this.activeclass;
            this.inputfield.value = imageel.getAttribute('src');
            var imgclass = imageel.className ? imageel.className : 'image-inline';
            selectSelectItem(this.classselect, imgclass);
        };
    } else {
        if (this.toolboxel) {
            this.toolboxel.className = this.plainclass;
        };
    };
};

ImageToolBox.prototype.addImage = function() {
    /* add an image */
    var url = this.inputfield.value;
    var sel_class = this.classselect.options[this.classselect.selectedIndex].value;
    this.editor.focusDocument();
    this.tool.createImage(url, null, sel_class);
    this.editor.updateState();
};

ImageToolBox.prototype.setImageClass = function() {
    /* set the class for the current image */
    var sel_class = this.classselect.options[this.classselect.selectedIndex].value;
    this.editor.focusDocument();
    this.tool.setImageClass(sel_class);
    this.editor.updateState();
};

function TableTool() {
    /* The table tool */
};

TableTool.prototype = new KupuTool;

// XXX There are some awfully long methods in here!!
TableTool.prototype.createContextMenuElements = function(selNode, event) {
    var table =  this.editor.getNearestParentOfType(selNode, 'table');
    if (!table) {
        var ret = [];
        var el = new ContextMenuElement(_('Add table'), this.addPlainTable, this);
        ret.push(el);
        return ret;
    } else {
        var ret = [];
        ret.push(new ContextMenuElement(_('Add row'), this.addTableRow, this));
        ret.push(new ContextMenuElement(_('Delete row'), this.delTableRow, this));
        ret.push(new ContextMenuElement(_('Add column'), this.addTableColumn, this));
        ret.push(new ContextMenuElement(_('Delete column'), this.delTableColumn, this));
        ret.push(new ContextMenuElement(_('Delete Table'), this.delTable, this));
        return ret;
    };
};

TableTool.prototype.addPlainTable = function() {
    /* event handler for the context menu */
    this.createTable(2, 3, 1, 'plain');
};

TableTool.prototype.createTable = function(rows, cols, makeHeader, tableclass) {
    /* add a table */
    if (rows < 1 || rows > 99 || cols < 1 || cols > 99) {
        this.editor.logMessage(_('Invalid table size'), 1);
        return;
    };

    var doc = this.editor.getInnerDocument();
    var table = doc.createElement("table");
    table.className = tableclass;

    // If the user wants a row of headings, make them
    if (makeHeader) {
        var tr = doc.createElement("tr");
        var thead = doc.createElement("thead");
        for (var i=1; i <= cols; i++) {
            var th = doc.createElement("th");
            th.appendChild(doc.createTextNode("Col " + i));
            tr.appendChild(th);
        }
        thead.appendChild(tr);
        table.appendChild(thead);
    }

    var tbody = doc.createElement("tbody");
    for (var i=0; i < rows; i++) {
        var tr = doc.createElement("tr");
        for (var j=0; j < cols; j++) {
            var td = doc.createElement("td");
            var content = doc.createTextNode('\u00a0');
            td.appendChild(content);
            tr.appendChild(td);
        }
        tbody.appendChild(tr);
    }
    table.appendChild(tbody);
    this.editor.insertNodeAtSelection(table);

    this._setTableCellHandlers(table);
    return table;
};

TableTool.prototype._setTableCellHandlers = function(table) {
    // make each cell select its full contents if it's clicked
    addEventHandler(table, 'click', this._selectContentIfEmpty, this);

    var cells = table.getElementsByTagName('td');
    for (var i=0; i < cells.length; i++) {
        addEventHandler(cells[i], 'click', this._selectContentIfEmpty, this);
    };
    
    // select the nbsp in the first cell
    var firstcell = cells[0];
    if (firstcell) {
        var children = firstcell.childNodes;
        if (children.length == 1 && children[0].nodeType == 3 && 
                children[0].nodeValue == '\xa0') {
            var selection = this.editor.getSelection();
            selection.selectNodeContents(firstcell);
        };
    };
};

TableTool.prototype._selectContentIfEmpty = function() {
    var selNode = this.editor.getSelectedNode();
    var cell = this.editor.getNearestParentOfType(selNode, 'td');
    if (!cell) {
        return;
    };
    var children = cell.childNodes;
    if (children.length == 1 && children[0].nodeType == 3 && 
            children[0].nodeValue == '\xa0') {
        var selection = this.editor.getSelection();
        selection.selectNodeContents(cell);
    };
};

TableTool.prototype.addTableRow = function() {
    /* Find the current row and add a row after it */
    var currnode = this.editor.getSelectedNode();
    var currtbody = this.editor.getNearestParentOfType(currnode, "TBODY");
    var bodytype = "tbody";
    if (!currtbody) {
        currtbody = this.editor.getNearestParentOfType(currnode, "THEAD");
        bodytype = "thead";
    }
    var parentrow = this.editor.getNearestParentOfType(currnode, "TR");
    var nextrow = parentrow.nextSibling;

    // get the number of cells we should place
    var colcount = 0;
    for (var i=0; i < currtbody.childNodes.length; i++) {
        var el = currtbody.childNodes[i];
        if (el.nodeType != 1) {
            continue;
        }
        if (el.nodeName.toLowerCase() == 'tr') {
            var cols = 0;
            for (var j=0; j < el.childNodes.length; j++) {
                if (el.childNodes[j].nodeType == 1) {
                    cols++;
                }
            }
            if (cols > colcount) {
                colcount = cols;
            }
        }
    }

    var newrow = this.editor.getInnerDocument().createElement("TR");

    for (var i = 0; i < colcount; i++) {
        var newcell;
        if (bodytype == 'tbody') {
            newcell = this.editor.getInnerDocument().createElement("TD");
        } else {
            newcell = this.editor.getInnerDocument().createElement("TH");
        }
        var newcellvalue = this.editor.getInnerDocument().createTextNode("\u00a0");
        newcell.appendChild(newcellvalue);
        newrow.appendChild(newcell);
    }

    if (!nextrow) {
        currtbody.appendChild(newrow);
    } else {
        currtbody.insertBefore(newrow, nextrow);
    }
};

TableTool.prototype.delTableRow = function() {
    /* Find the current row and delete it */
    var currnode = this.editor.getSelectedNode();
    var parentrow = this.editor.getNearestParentOfType(currnode, "TR");
    if (!parentrow) {
        this.editor.logMessage(_('No row to delete'), 1);
        return;
    }

    // move selection aside
    // XXX: doesn't work if parentrow is the only row of thead/tbody/tfoot
    // XXX: doesn't preserve the colindex
    var selection = this.editor.getSelection();
    if (parentrow.nextSibling) {
        selection.selectNodeContents(parentrow.nextSibling.firstChild);
    } else if (parentrow.previousSibling) {
        selection.selectNodeContents(parentrow.previousSibling.firstChild);
    };

    // remove the row
    parentrow.parentNode.removeChild(parentrow);
};

TableTool.prototype.addTableColumn = function() {
    /* Add a new column after the current column */
    var currnode = this.editor.getSelectedNode();
    var currtd = this.editor.getNearestParentOfType(currnode, 'TD');
    if (!currtd) {
        currtd = this.editor.getNearestParentOfType(currnode, 'TH');
    }
    if (!currtd) {
        this.editor.logMessage(_('No parentcolumn found!'), 1);
        return;
    }
    var currtable = this.editor.getNearestParentOfType(currnode, 'TABLE');
    
    // get the current index
    var tdindex = this._getColIndex(currtd);

    // now add a column to all rows
    // first the thead
    var theads = currtable.getElementsByTagName('THEAD');
    if (theads) {
        for (var i=0; i < theads.length; i++) {
            // let's assume table heads only have ths
            var currthead = theads[i];
            for (var j=0; j < currthead.childNodes.length; j++) {
                var tr = currthead.childNodes[j];
                if (tr.nodeType != 1) {
                    continue;
                }
                var currindex = 0;
                for (var k=0; k < tr.childNodes.length; k++) {
                    var th = tr.childNodes[k];
                    if (th.nodeType != 1) {
                        continue;
                    }
                    if (currindex == tdindex) {
                        var doc = this.editor.getInnerDocument();
                        var newth = doc.createElement('th');
                        var text = doc.createTextNode('\u00a0');
                        newth.appendChild(text);
                        if (tr.childNodes.length == k+1) {
                            // the column will be on the end of the row
                            tr.appendChild(newth);
                        } else {
                            tr.insertBefore(newth, tr.childNodes[k + 1]);
                        }
                        break;
                    }
                    currindex++;
                }
            }
        }
    }

    // then the tbody
    var tbodies = currtable.getElementsByTagName('TBODY');
    if (tbodies) {
        for (var i=0; i < tbodies.length; i++) {
            // let's assume table heads only have ths
            var currtbody = tbodies[i];
            for (var j=0; j < currtbody.childNodes.length; j++) {
                var tr = currtbody.childNodes[j];
                if (tr.nodeType != 1) {
                    continue;
                }
                var currindex = 0;
                for (var k=0; k < tr.childNodes.length; k++) {
                    var td = tr.childNodes[k];
                    if (td.nodeType != 1) {
                        continue;
                    }
                    if (currindex == tdindex) {
                        var doc = this.editor.getInnerDocument();
                        var newtd = doc.createElement('td');
                        var text = doc.createTextNode('\u00a0');
                        newtd.appendChild(text);
                        if (tr.childNodes.length == k+1) {
                            // the column will be on the end of the row
                            tr.appendChild(newtd);
                        } else {
                            tr.insertBefore(newtd, tr.childNodes[k + 1]);
                        }
                        break;
                    }
                    currindex++;
                }
            }
        }
    }
};

TableTool.prototype.delTableColumn = function() {
    /* remove a column */
    var currnode = this.editor.getSelectedNode();
    var currtd = this.editor.getNearestParentOfType(currnode, 'TD');
    if (!currtd) {
        currtd = this.editor.getNearestParentOfType(currnode, 'TH');
    }
    var currcolindex = this._getColIndex(currtd);
    var currtable = this.editor.getNearestParentOfType(currnode, 'TABLE');

    // move selection aside
    var selection = this.editor.getSelection();
    if (currtd.nextSibling) {
        selection.selectNodeContents(currtd.nextSibling);
    } else if (currtd.previousSibling) {
        selection.selectNodeContents(currtd.previousSibling);
    };

    // remove the theaders
    var heads = currtable.getElementsByTagName('THEAD');
    if (heads.length) {
        for (var i=0; i < heads.length; i++) {
            var thead = heads[i];
            for (var j=0; j < thead.childNodes.length; j++) {
                var tr = thead.childNodes[j];
                if (tr.nodeType != 1) {
                    continue;
                }
                var currindex = 0;
                for (var k=0; k < tr.childNodes.length; k++) {
                    var th = tr.childNodes[k];
                    if (th.nodeType != 1) {
                        continue;
                    }
                    if (currindex == currcolindex) {
                        tr.removeChild(th);
                        break;
                    }
                    currindex++;
                }
            }
        }
    }

    // now we remove the column field, a bit harder since we need to take 
    // colspan and rowspan into account XXX Not right, fix theads as well
    var bodies = currtable.getElementsByTagName('TBODY');
    for (var i=0; i < bodies.length; i++) {
        var currtbody = bodies[i];
        for (var j=0; j < currtbody.childNodes.length; j++) {
            var tr = currtbody.childNodes[j];
            if (tr.nodeType != 1) {
                continue;
            }
            var currindex = 0;
            for (var k=0; k < tr.childNodes.length; k++) {
                var cell = tr.childNodes[k];
                if (cell.nodeType != 1) {
                    continue;
                }
                if (currindex == currcolindex) {
                    tr.removeChild(cell);
                    break;
                }
                currindex++;
            }
        }
    }
};

TableTool.prototype.delTable = function() {
    /* delete the current table */
    var currnode = this.editor.getSelectedNode();
    var table = this.editor.getNearestParentOfType(currnode, 'table');
    if (!table) {
        this.editor.logMessage(_('Not inside a table!'));
        return;
    };
    table.parentNode.removeChild(table);
};

TableTool.prototype.setColumnAlign = function(newalign) {
    /* change the alignment of a full column */
    var currnode = this.editor.getSelectedNode();
    var currtd = this.editor.getNearestParentOfType(currnode, "TD");
    var bodytype = 'tbody';
    if (!currtd) {
        currtd = this.editor.getNearestParentOfType(currnode, "TH");
        bodytype = 'thead';
    }
    var currcolindex = this._getColIndex(currtd);
    var currtable = this.editor.getNearestParentOfType(currnode, "TABLE");

    // unfortunately this is not enough to make the browsers display
    // the align, we need to set it on individual cells as well and
    // mind the rowspan...
    for (var i=0; i < currtable.childNodes.length; i++) {
        var currtbody = currtable.childNodes[i];
        if (currtbody.nodeType != 1 || 
                (/^thead|tbody$/i.test(currtbody.nodeName))) {
            continue;
        }
        for (var j=0; j < currtbody.childNodes.length; j++) {
            var row = currtbody.childNodes[j];
            if (row.nodeType != 1) {
                continue;
            }
            var index = 0;
            for (var k=0; k < row.childNodes.length; k++) {
                var cell = row.childNodes[k];
                if (cell.nodeType != 1) {
                    continue;
                }
                if (index == currcolindex) {
                    if (this.editor.config.use_css) {
                        cell.style.textAlign = newalign;
                    } else {
                        cell.setAttribute('align', newalign);
                    }
                    cell.className = 'align-' + newalign;
                }
                index++;
            }
        }
    }
};

TableTool.prototype.setTableClass = function(sel_class) {
    /* set the class for the table */
    var currnode = this.editor.getSelectedNode();
    var currtable = this.editor.getNearestParentOfType(currnode, 'TABLE');

    if (currtable) {
        currtable.className = sel_class;
    }
};

TableTool.prototype._getColIndex = function(currcell) {
    /* Given a node, return an integer for which column it is */
    var prevsib = currcell.previousSibling;
    var currcolindex = 0;
    while (prevsib) {
        if (prevsib.nodeType == 1 && 
                (prevsib.tagName.toUpperCase() == "TD" || 
                    prevsib.tagName.toUpperCase() == "TH")) {
            var colspan = prevsib.colSpan;
            if (colspan) {
                currcolindex += parseInt(colspan);
            } else {
                currcolindex++;
            }
        }
        prevsib = prevsib.previousSibling;
        if (currcolindex > 30) {
            alert("Recursion detected when counting column position");
            return;
        }
    }

    return currcolindex;
};

TableTool.prototype._getColumnAlign = function(selNode) {
    /* return the alignment setting of the current column */
    var align;
    var td = this.editor.getNearestParentOfType(selNode, 'td');
    if (!td) {
        td = this.editor.getNearestParentOfType(selNode, 'th');
    };
    if (td) {
        align = td.getAttribute('align');
        if (this.editor.config.use_css) {
            align = td.style.textAlign;
        };
    };
    return align;
};

TableTool.prototype.fixTable = function(event) {
    /* fix the table so it can be processed by Kupu */
    // since this can be quite a nasty creature we can't just use the
    // helper methods
    
    // first we create a new tbody element
    var currnode = this.editor.getSelectedNode();
    var table = this.editor.getNearestParentOfType(currnode, 'TABLE');
    if (!table) {
        this.editor.logMessage(_('Not inside a table!'));
        return;
    };
    this._fixTableHelper(table);
};

TableTool.prototype._isBodyRow = function(row) {
    for (var node = row.firstChild; node; node=node.nextSibling) {
        if (/^td$/i.test(node.nodeName)) {
            return true;
        }
    }
    return false;
};

TableTool.prototype._cleanCell = function(el) {
    // Remove formatted div or p from a cell
    var nxt, n;
    for (var node = el.firstChild; node;) {
        if (/^div|p$/i.test(node.nodeName)) {
            for (var n = node.firstChild; n;) {
                var nxt = n.nextSibling;
                el.insertBefore(n, node); // Move nodes out of div
                n = nxt;
            }
            nxt = node.nextSibling;
            el.removeChild(node);
            node = nxt;
        } else {
            node = node.nextSibling;
        }
    }
    var c;
    while (el.firstChild && (c = el.firstChild).nodeType==3 && (/^\s+/.test(c.data))) {
        c.data = c.data.replace(/^\s+/, '');
        if (!c.data) {
            el.removeChild(c);
        } else {
            break;
        };
    };
    while (el.lastChild && (c = el.lastChild).nodeType==3 && (/\s+$/.test(c.data))) {
        c.data = c.data.replace(/\s+$/, '');
        if (!c.data) {
            el.removeChild(c);
        } else {
            break;
        };
    };
    el.removeAttribute('colSpan');
    el.removeAttribute('rowSpan');
};

TableTool.prototype._countCols = function(rows, numcols) {
    for (var i=0; i < rows.length; i++) {
        var row = rows[i];
        var currnumcols = 0;
        for (var node = row.firstChild; node; node=node.nextSibling) {
            if (/^(td|th)$/i.test(node.nodeName)) {
                currnumcols += parseInt(node.getAttribute('colSpan') || '1');
            };
        };
        if (currnumcols > numcols) {
            numcols = currnumcols;
        };
    };
    return numcols;
};

TableTool.prototype._cleanRows = function(rows, container, numcols) {
    // now walk through all rows to clean them up
    for (var i=0; i < rows.length; i++) {
        var row = rows[i];
        var doc = this.editor.getInnerDocument();
        var newrow = doc.createElement('tr');
        if (row.className) {
            newrow.className = row.className;
        }
        for (var node = row.firstChild; node;) {
            var nxt = node.nextSibling;
            if (/^(td|th)$/i.test(node.nodeName)) {
                this._cleanCell(node);
                newrow.appendChild(node);
            };
            node = nxt;
        };
        if (newrow.childNodes.length) {
            container.appendChild(newrow);
        };
    };
    // now make sure all rows have the correct length
    for (var row = container.firstChild; row; row=row.nextSibling) {
        var cellname = row.lastChild.nodeName;
        while (row.childNodes.length < numcols) {
            var cell = doc.createElement(cellname);
            var nbsp = doc.createTextNode('\u00a0');
            cell.appendChild(nbsp);
            row.appendChild(cell);
        };
    };
};

TableTool.prototype._fixTableHelper = function(table) {
    /* the code to actually fix tables */
    var doc = this.editor.getInnerDocument();
    var thead = doc.createElement('thead');
    var tbody = doc.createElement('tbody');
    var tfoot = doc.createElement('tfoot');

    var table_classes = this.editor.config.table_classes;
    function cleanClassName(name) {
        var allowed_classes = table_classes['class'];
        for (var i = 0; i < allowed_classes.length; i++) {
            var classname = allowed_classes[i];
            classname = classname.classname || classname;
            if (classname==name) return name;
        };
        return allowed_classes[0];
    }
    if (table_classes) {
        table.className = cleanClassName(table.className);
    } else {
        table.removeAttribute('class');
        table.removeAttribute('className');
    };
    table.removeAttribute('border');
    table.removeAttribute('cellpadding');
    table.removeAttribute('cellPadding');
    table.removeAttribute('cellspacing');
    table.removeAttribute('cellSpacing');

    // now get all the rows of the table, the rows can either be
    // direct descendants of the table or inside a 'tbody', 'thead'
    // or 'tfoot' element

    var hrows = [], brows = [], frows = [];
    for (var node = table.firstChild; node; node = node.nextSibling) {
        var nodeName = node.nodeName.toLowerCase();
        if (/tr/i.test(node.nodeName)) {
            brows.push(node);
        } else if (/thead|tbody|tfoot/i.test(node.nodeName)) {
            var rows = nodeName=='thead' ? hrows : nodeName=='tfoot' ? frows : brows;
            for (var inode = node.firstChild; inode; inode = inode.nextSibling) {
                if (/tr/i.test(inode.nodeName)) {
                    rows.push(inode);
                };
            };
        };
    };
    /* Extract thead and tfoot from tbody */
    while (brows.length && !this._isBodyRow(brows[0])) {
        hrows.push(brows[0]);
        brows.shift();
    }
    while (brows.length && !this._isBodyRow(brows[brows.length-1])) {
        var last = brows[brows.length-1];
        brows.length -= 1;
        frows.unshift(last);
    }
    // now find out how many cells our rows should have
    var numcols = this._countCols(hrows, 0);
    numcols = this._countCols(brows, numcols);
    numcols = this._countCols(frows, numcols);

    // now walk through all rows to clean them up
    this._cleanRows(hrows, thead);
    this._cleanRows(brows, tbody);
    this._cleanRows(frows, tfoot);

    // now remove all the old stuff from the table and add the new
    // tbody
    while (table.firstChild) {
        table.removeChild(table.firstChild);
    }
    if (hrows.length) {
        table.appendChild(thead);
    }
    if (brows.length) {
        table.appendChild(tbody);
    }
    if (frows.length) {
        table.appendChild(tfoot);
    }
};

TableTool.prototype.fixAllTables = function() {
    /* fix all the tables in the document at once */
    var tables = this.editor.getInnerDocument().getElementsByTagName('table');
    for (var i=0; i < tables.length; i++) {
        this._fixTableHelper(tables[i]);
    };
};

function TableToolBox(addtabledivid, edittabledivid, newrowsinputid, 
                    newcolsinputid, makeheaderinputid, classselectid, alignselectid, addtablebuttonid,
                    addrowbuttonid, delrowbuttonid, addcolbuttonid, delcolbuttonid, fixbuttonid,
                    delbuttonid, fixallbuttonid, toolboxid, plainclass, activeclass) {
    /* The table tool */

    // a lot of dependencies on html elements here, but most implementations
    // will use them all I guess
    this.addtablediv = getFromSelector(addtabledivid);
    this.edittablediv = getFromSelector(edittabledivid);
    this.newrowsinput = getFromSelector(newrowsinputid);
    this.newcolsinput = getFromSelector(newcolsinputid);
    this.makeheaderinput = getFromSelector(makeheaderinputid);
    this.classselect = getFromSelector(classselectid);
    this.alignselect = getFromSelector(alignselectid);
    this.addtablebutton = getFromSelector(addtablebuttonid);
    this.addrowbutton = getFromSelector(addrowbuttonid);
    this.delrowbutton = getFromSelector(delrowbuttonid);
    this.addcolbutton = getFromSelector(addcolbuttonid);
    this.delcolbutton = getFromSelector(delcolbuttonid);
    this.fixbutton = getFromSelector(fixbuttonid);
    this.delbutton = getFromSelector(delbuttonid);
    this.fixallbutton = getFromSelector(fixallbuttonid);
    this.toolboxel = getFromSelector(toolboxid);
    this.plainclass = plainclass;
    this.activeclass = activeclass;
};

TableToolBox.prototype = new KupuToolBox;

// register event handlers
TableToolBox.prototype.initialize = function(tool, editor) {
    /* attach the event handlers */
    this.tool = tool;
    this.editor = editor;
    // build the select list of table classes if configured
    if (this.editor.config.table_classes) {
        var classes = this.editor.config.table_classes['class'];
        while (this.classselect.hasChildNodes()) {
            this.classselect.removeChild(this.classselect.firstChild);
        };
        for (var i=0; i < classes.length; i++) {
            var classname = classes[i];
            classname = classname.classname || classname;
            var option = document.createElement('option');
            var content = document.createTextNode(classname);
            option.appendChild(content);
            option.setAttribute('value', classname);
            this.classselect.appendChild(option);
        };
    };
    addEventHandler(this.addtablebutton, "click", this.addTable, this);
    addEventHandler(this.addrowbutton, "click", this.addTableRow, this);
    addEventHandler(this.delrowbutton, "click", this.delTableRow, this);
    addEventHandler(this.addcolbutton, "click", this.addTableColumn, this);
    addEventHandler(this.delcolbutton, "click", this.delTableColumn, this);
    addEventHandler(this.alignselect, "change", this.setColumnAlign, this);
    addEventHandler(this.classselect, "change", this.setTableClass, this);
    addEventHandler(this.fixbutton, "click", this.fixTable, this);
    addEventHandler(this.delbutton, "click", this.delTable, this);
    addEventHandler(this.fixallbutton, "click", this.fixAllTables, this);
    this.addtablediv.style.display = "block";
    this.edittablediv.style.display = "none";
};

TableToolBox.prototype.updateState = function(selNode) {
    /* update the state (add/edit) and update the pulldowns (if required) */
    var table = this.editor.getNearestParentOfType(selNode, 'table');
    if (table) {
        this.addtablediv.style.display = "none";
        this.edittablediv.style.display = "block";

        var align = this.tool._getColumnAlign(selNode);
        selectSelectItem(this.alignselect, align);
        selectSelectItem(this.classselect, table.className);
        if (this.toolboxel) {
            this.toolboxel.className = this.activeclass;
        };
    } else {
        this.edittablediv.style.display = "none";
        this.addtablediv.style.display = "block";
        this.alignselect.selectedIndex = 0;
        this.classselect.selectedIndex = 0;
        if (this.toolboxel) {
            this.toolboxel.className = this.plainclass;
        };
    };
};

TableToolBox.prototype.addTable = function() {
    /* add a table */
    var rows = this.newrowsinput.value;
    var cols = this.newcolsinput.value;
    var makeHeader = this.makeheaderinput.checked;
    var tableclass = this.classselect.options[this.classselect.selectedIndex].value;

    this.tool.createTable(rows, cols, makeHeader, tableclass);
    this.editor.focusDocument();
    this.editor.updateState();
};

TableToolBox.prototype.setColumnAlign = function() {
    /* set the alignment of the current column */
    var newalign = this.alignselect.options[this.alignselect.selectedIndex].value;
    this.editor.focusDocument();
    this.tool.setColumnAlign(newalign);
    this.editor.updateState();
};

TableToolBox.prototype.setTableClass = function() {
    /* set the class for the current table */
    var sel_class = this.classselect.options[this.classselect.selectedIndex].value;
    if (sel_class) {
        this.editor.focusDocument();
        this.tool.setTableClass(sel_class);
        this.editor.updateState();
    };
};

TableToolBox.prototype.addTableRow = function() {
    this.editor.focusDocument();
    this.tool.addTableRow();
    this.editor.updateState();
};

TableToolBox.prototype.delTableRow = function() {
    this.editor.focusDocument();
    this.tool.delTableRow();
    this.editor.updateState();
};

TableToolBox.prototype.addTableColumn = function() {
    this.editor.focusDocument();
    this.tool.addTableColumn();
    this.editor.updateState();
};

TableToolBox.prototype.delTableColumn = function() {
    this.editor.focusDocument();
    this.tool.delTableColumn();
    this.editor.updateState();
};

TableToolBox.prototype.fixTable = function() {
    this.editor.focusDocument();
    this.tool.fixTable();
    this.editor.updateState();
};

TableToolBox.prototype.fixAllTables = function() {
    this.editor.focusDocument();
    this.tool.fixAllTables();
    this.editor.updateState();
};

TableToolBox.prototype.delTable = function() {
    this.editor.focusDocument();
    this.tool.delTable();
    this.editor.updateState();
};

function ListTool(addulbuttonid, addolbuttonid, ulstyleselectid, olstyleselectid) {
    /* tool to set list styles */

    this.addulbutton = getFromSelector(addulbuttonid);
    this.addolbutton = getFromSelector(addolbuttonid);
    this.ulselect = getFromSelector(ulstyleselectid);
    this.olselect = getFromSelector(olstyleselectid);
    
    this.style_to_type = {'decimal': '1',
                            'lower-alpha': 'a',
                            'upper-alpha': 'A',
                            'lower-roman': 'i',
                            'upper-roman': 'I',
                            'disc': 'disc',
                            'square': 'square',
                            'circle': 'circle',
                            'none': 'none'
                            };
    this.type_to_style = {'1': 'decimal',
                            'a': 'lower-alpha',
                            'A': 'upper-alpha',
                            'i': 'lower-roman',
                            'I': 'upper-roman',
                            'disc': 'disc',
                            'square': 'square',
                            'circle': 'circle',
                            'none': 'none'
                            };
};

ListTool.prototype = new KupuTool;

ListTool.prototype.initialize = function(editor) {
    /* attach event handlers */
    this.editor = editor;
    if (this.addulbutton) {
        addEventHandler(this.addulbutton, "click", this.addUnorderedList, this);
    }
    if (this.addolbutton) {
        addEventHandler(this.addolbutton, "click", this.addOrderedList, this);
    }
    if (this.ulselect) {
        addEventHandler(this.ulselect, "change", this.setUnorderedListStyle, this);
        this.ulselect.style.display = "none";
    }
    if (this.olselect) {
        addEventHandler(this.olselect, "change", this.setOrderedListStyle, this);
        this.olselect.style.display = "none";
    }
};

ListTool.prototype._handleStyles = function(currnode, onselect, offselect) {
    if (this.editor.config.use_css) {
        var currstyle = currnode.style.listStyleType;
    } else {
        var currstyle = this.type_to_style[currnode.getAttribute('type')];
    }
    if (onselect) {
        selectSelectItem(onselect, currstyle);
        onselect.style.display = "inline";
    }
    if (offselect) {
        offselect.style.display = "none";
        offselect.selectedIndex = 0;
    }
};

ListTool.prototype.updateState = function(selNode) {
    /* update the visibility and selection of the list type pulldowns */
    // we're going to walk through the tree manually since we want to 
    // check on 2 items at the same time
    for (var currnode=selNode; currnode; currnode=currnode.parentNode) {
        var tag = currnode.nodeName.toLowerCase();
        if (tag == 'ul') {
            this._handleStyles(currnode, this.ulselect, this.olselect);
            return;
        } else if (tag == 'ol') {
            this._handleStyles(currnode, this.olselect, this.ulselect);
            return;
        }
    }
    if (this.ulselect) {
        this.ulselect.selectedIndex = 0;
        this.ulselect.style.display = "none";
    };
    if (this.olselect) {
        this.olselect.selectedIndex = 0;
        this.olselect.style.display = "none";
    };
};

ListTool.prototype.addList = function(command) {
    if (this.ulselect) this.ulselect.style.display = "inline";
    if (this.olselect) this.olselect.style.display = "none";
    this.editor.execCommand(command);
    this.editor.focusDocument();
};

ListTool.prototype.addUnorderedList = function() {
    /* add an unordered list */
    this.addList("insertunorderedlist");
};

ListTool.prototype.addOrderedList = function() {
    /* add an ordered list */
    this.addList("insertorderedlist");
};

ListTool.prototype.setListStyle = function(tag, select) {
    /* set the type of an ul */
    if (!select) return;
    var currnode = this.editor.getSelectedNode();
    var l = this.editor.getNearestParentOfType(currnode, tag);
    var style = select.options[select.selectedIndex].value;
    if (this.editor.config.use_css) {
        l.style.listStyleType = style;
    } else {
        l.setAttribute('type', this.style_to_type[style]);
    }
    this.editor.focusDocument();
};

ListTool.prototype.setUnorderedListStyle = function() {
    /* set the type of an ul */
    this.setListStyle('ul', this.ulselect);
};

ListTool.prototype.setOrderedListStyle = function() {
    /* set the type of an ol */
    this.setListStyle('ol', this.olselect);
};

ListTool.prototype.enable = function() {
    kupuButtonEnable(this.addulbutton);
    kupuButtonEnable(this.addolbutton);
    if (this.ulselect) this.ulselect.disabled = "";
    if (this.olselect) this.olselect.disabled = "";
};

ListTool.prototype.disable = function() {
    kupuButtonDisable(this.addulbutton);
    kupuButtonDisable(this.addolbutton);
    if (this.ulselect) this.ulselect.disabled = "disabled";
    if (this.olselect) this.olselect.disabled = "disabled";
};

function ShowPathTool() {
    /* shows the path to the current element in the status bar */
};

ShowPathTool.prototype = new KupuTool;

ShowPathTool.prototype.updateState = function(selNode) {
    /* calculate and display the path */
    var path = '';
    var url = null; // for links we want to display the url too
    var currnode = selNode;
    var nn;
    while (currnode != null && (nn=currnode.nodeName.toLowerCase()) != '#document') {
        if (nn=='a') {
            url = currnode.getAttribute('href');
        };
        path = '/' + nn + path;
        currnode = currnode.parentNode;
    }
    
    try {
        window.status = url ? 
                (path.toString() + ' - contains link to \'' + 
                    url.toString() + '\'') :
                path;
    } catch (e) {
        this.editor.logMessage(_('Could not set status bar message, ' +
                                'check your browser\'s security settings.'), 1);
    };
};

function ViewSourceTool() {
    /* tool to provide a 'show source' context menu option */
    this.sourceWindow = null;
};

ViewSourceTool.prototype = new KupuTool;
    
ViewSourceTool.prototype.viewSource = function() {
    /* open a window and write the current contents of the iframe to it */
    if (this.sourceWindow) {
        this.sourceWindow.close();
    };
    this.sourceWindow = window.open('#', 'sourceWindow');
    
    //var transform = this.editor._filterContent(this.editor.getInnerDocument().documentElement);
    //var contents = transform.xml; 
    var contents = '<html>\n' + this.editor.getInnerDocument().documentElement.innerHTML + '\n</html>';
    
    var doc = this.sourceWindow.document;
    doc.write('\xa0');
    doc.close();
    var body = doc.getElementsByTagName("body")[0];
    while (body.hasChildNodes()) {
        body.removeChild(body.firstChild);
    };
    var pre = doc.createElement('pre');
    var textNode = doc.createTextNode(contents);
    body.appendChild(pre);
    pre.appendChild(textNode);
};

ViewSourceTool.prototype.createContextMenuElements = function(selNode, event) {
    /* create the context menu element */
    return [new ContextMenuElement(_('View source'), this.viewSource, this)];
};

function DefinitionListTool(dlbuttonid) {
    /* a tool for managing definition lists

        the dl elements should behave much like plain lists, and the keypress
        behaviour should be similar
    */

    this.dlbutton = getFromSelector(dlbuttonid);
};

DefinitionListTool.prototype = new KupuTool;

DefinitionListTool.prototype.initialize = function(editor) {
    /* initialize the tool */
    this.editor = editor;
    if (!this.dlbutton) return;
    addEventHandler(this.dlbutton, 'click', this.createDefinitionList, this);
    addEventHandler(editor.getInnerDocument(), 'keyup', this._keyDownHandler, this);
    addEventHandler(editor.getInnerDocument(), 'keypress', this._keyPressHandler, this);
};

// even though the following methods may seem view related, they belong 
// here, since they describe core functionality rather then view-specific
// stuff
DefinitionListTool.prototype.handleEnterPress = function(selNode) {
    var dl = this.editor.getNearestParentOfType(selNode, 'dl');
    if (dl) {
        var dt = this.editor.getNearestParentOfType(selNode, 'dt');
        if (dt) {
            if (dt.childNodes.length == 1 && dt.childNodes[0].nodeValue == '\xa0') {
                this.escapeFromDefinitionList(dl, dt, selNode);
                return;
            };

            var selection = this.editor.getSelection();
            var startoffset = selection.startOffset();
            var endoffset = selection.endOffset(); 
            if (endoffset > startoffset) {
                // throw away any selected stuff
                selection.cutChunk(startoffset, endoffset);
                selection = this.editor.getSelection();
                startoffset = selection.startOffset();
            };
            
            var ellength = selection.getElementLength(selection.parentElement());
            if (startoffset >= ellength - 1) {
                // create a new element
                this.createDefinition(dl, dt);
            } else {
                var doc = this.editor.getInnerDocument();
                var newdt = selection.splitNodeAtSelection(dt);
                var newdd = doc.createElement('dd');
                while (newdt.hasChildNodes()) {
                    if (newdt.firstChild != newdt.lastChild || newdt.firstChild.nodeName.toLowerCase() != 'br') {
                        newdd.appendChild(newdt.firstChild);
                    };
                };
                newdt.parentNode.replaceChild(newdd, newdt);
                selection.selectNodeContents(newdd);
                selection.collapse();
            };
        } else {
            var dd = this.editor.getNearestParentOfType(selNode, 'dd');
            if (!dd) {
                this.editor.logMessage(_('Not inside a definition list element!'));
                return;
            };
            if (dd.childNodes.length == 1 && dd.childNodes[0].nodeValue == '\xa0') {
                this.escapeFromDefinitionList(dl, dd, selNode);
                return;
            };
            var selection = this.editor.getSelection();
            var startoffset = selection.startOffset();
            var endoffset = selection.endOffset();
            if (endoffset > startoffset) {
                // throw away any selected stuff
                selection.cutChunk(startoffset, endoffset);
                selection = this.editor.getSelection();
                startoffset = selection.startOffset();
            };
            var ellength = selection.getElementLength(selection.parentElement());
            if (startoffset >= ellength - 1) {
                // create a new element
                this.createDefinitionTerm(dl, dd);
            } else {
                // add a break and continue in this element
                var br = this.editor.getInnerDocument().createElement('br');
                this.editor.insertNodeAtSelection(br, 1);
                //var selection = this.editor.getSelection();
                //selection.moveStart(1);
                selection.collapse(true);
            };
        };
    };
};

DefinitionListTool.prototype.handleTabPress = function(selNode) {
};

DefinitionListTool.prototype._keyDownHandler = function(event) {
    var selNode = this.editor.getSelectedNode();
    var dl = this.editor.getNearestParentOfType(selNode, 'dl');
    if (!dl) {
        return;
    };
    if (event.keyCode) {
        if (event.preventDefault) {
            event.preventDefault();
        } else {
            event.returnValue = false;
        };
    };
};

DefinitionListTool.prototype._keyPressHandler = function(event) {
    var selNode = this.editor.getSelectedNode();
    var dl = this.editor.getNearestParentOfType(selNode, 'dl');
    if (!dl) {
        return;
    };
    switch (event.keyCode) {
        case 13:
            this.handleEnterPress(selNode);
            if (event.preventDefault) {
                event.preventDefault();
            } else {
                event.returnValue = false;
            };
            break;
        case 9:
            if (event.preventDefault) {
                event.preventDefault();
            } else {
                event.returnValue = false;
            };
            this.handleTabPress(selNode);
    };
};

DefinitionListTool.prototype.createDefinitionList = function() {
    /* create a new definition list (dl) */
    var selection = this.editor.getSelection();
    var doc = this.editor.getInnerDocument();

    var selection = this.editor.getSelection();
    var cloned = selection.cloneContents();
    // first get the 'first line' (until the first break) and use it
    // as the dt's content
    var iterator = new NodeIterator(cloned);
    var currnode = null;
    var remove = false;
    while ((currnode = iterator.next())) {
        if (currnode.nodeName.toLowerCase() == 'br') {
            remove = true;
        };
        if (remove) {
            var next = currnode;
            while (!next.nextSibling) {
                next = next.parentNode;
            };
            next = next.nextSibling;
            iterator.setCurrent(next);
            currnode.parentNode.removeChild(currnode);
        };
    };

    var dtcontentcontainer = cloned;
    var collapsetoend = false;
    
    var dl = doc.createElement('dl');
    this.editor.insertNodeAtSelection(dl);
    var dt = this.createDefinitionTerm(dl);
    if (dtcontentcontainer.hasChildNodes()) {
        collapsetoend = true;
        while (dt.hasChildNodes()) {
            dt.removeChild(dt.firstChild);
        };
        while (dtcontentcontainer.hasChildNodes()) {
            dt.appendChild(dtcontentcontainer.firstChild);
        };
    };

    var selection = this.editor.getSelection();
    selection.selectNodeContents(dt);
    selection.collapse(collapsetoend);
};

DefinitionListTool.prototype.createDefinitionTerm = function(dl, dd) {
    /* create a new definition term inside the current dl */
    var doc = this.editor.getInnerDocument();
    var dt = doc.createElement('dt');
    // somehow Mozilla seems to add breaks to all elements...
    if (dd) {
        if (dd.lastChild.nodeName.toLowerCase() == 'br') {
            dd.removeChild(dd.lastChild);
        };
    };
    // dd may be null here, if so we assume this is the first element in 
    // the dl
    if (!dd || dl == dd.lastChild) {
        dl.appendChild(dt);
    } else {
        var nextsibling = dd.nextSibling;
        if (nextsibling) {
            dl.insertBefore(dt, nextsibling);
        } else {
            dl.appendChild(dt);
        };
    };
    var nbsp = doc.createTextNode('\xa0');
    dt.appendChild(nbsp);
    var selection = this.editor.getSelection();
    selection.selectNodeContents(dt);
    selection.collapse();

    this.editor.focusDocument();
    return dt;
};

DefinitionListTool.prototype.createDefinition = function(dl, dt, initial_content) {
    var doc = this.editor.getInnerDocument();
    var dd = doc.createElement('dd');
    var nextsibling = dt.nextSibling;
    // somehow Mozilla seems to add breaks to all elements...
    if (dt) {
        if (dt.lastChild.nodeName.toLowerCase() == 'br') {
            dt.removeChild(dt.lastChild);
        };
    };
    while (nextsibling) {
        var name = nextsibling.nodeName.toLowerCase();
        if (name == 'dd' || name == 'dt') {
            break;
        } else {
            nextsibling = nextsibling.nextSibling;
        };
    };
    if (nextsibling) {
        dl.insertBefore(dd, nextsibling);
        //this._fixStructure(doc, dl, nextsibling);
    } else {
        dl.appendChild(dd);
    };
    if (initial_content) {
        for (var i=0; i < initial_content.length; i++) {
            dd.appendChild(initial_content[i]);
        };
    };
    var nbsp = doc.createTextNode('\xa0');
    dd.appendChild(nbsp);
    var selection = this.editor.getSelection();
    selection.selectNodeContents(dd);
    selection.collapse();
};

DefinitionListTool.prototype.escapeFromDefinitionList = function(dl, currel, selNode) {
    var doc = this.editor.getInnerDocument();
    var p = doc.createElement('p');
    var nbsp = doc.createTextNode('\xa0');
    p.appendChild(nbsp);

    if (dl.lastChild == currel) {
        dl.parentNode.insertBefore(p, dl.nextSibling);
    } else {
        for (var i=0; i < dl.childNodes.length; i++) {
            var child = dl.childNodes[i];
            if (child == currel) {
                var newdl = this.editor.getInnerDocument().createElement('dl');
                while (currel.nextSibling) {
                    newdl.appendChild(currel.nextSibling);
                };
                dl.parentNode.insertBefore(newdl, dl.nextSibling);
                dl.parentNode.insertBefore(p, dl.nextSibling);
            };
        };
    };
    currel.parentNode.removeChild(currel);
    var selection = this.editor.getSelection();
    selection.selectNodeContents(p);
    selection.collapse();
    this.editor.focusDocument();
};

DefinitionListTool.prototype._fixStructure = function(doc, dl, offsetnode) {
    /* makes sure the order of the elements is correct */
    var currname = offsetnode.nodeName.toLowerCase();
    var currnode = offsetnode.nextSibling;
    while (currnode) {
        if (currnode.nodeType == 1) {
            var nodename = currnode.nodeName.toLowerCase();
            if (currname == 'dt' && nodename == 'dt') {
                var dd = doc.createElement('dd');
                while (currnode.hasChildNodes()) {
                    dd.appendChild(currnode.childNodes[0]);
                };
                currnode.parentNode.replaceChild(dd, currnode);
            } else if (currname == 'dd' && nodename == 'dd') {
                var dt = doc.createElement('dt');
                while (currnode.hasChildNodes()) {
                    dt.appendChild(currnode.childNodes[0]);
                };
                currnode.parentNode.replaceChild(dt, currnode);
            };
        };
        currnode = currnode.nextSibling;
    };
};

function KupuZoomTool(buttonid, firsttab, lasttab) {
    this.button = getFromSelector(buttonid);
    firsttab = firsttab || 'kupu-tb-styles';
    lasttab = lasttab || 'kupu-logo-button';

    this.initialize = function(editor) {
        this.offclass = 'kupu-zoom';
        this.onclass = 'kupu-zoom-pressed';
        this.pressed = false;
        if (!this.button) return;
        this.baseinitialize(editor);
        addEventHandler(window, "resize", this.onresize, this);
        addEventHandler(window, "scroll", this.onscroll, this);

        /* Toolbar tabbing */
        var lastbutton = getFromSelector(lasttab);
        var firstbutton = getFromSelector(firsttab);
        var iframe = editor.getInnerDocument();
        this.setTabbing(iframe, firstbutton, lastbutton);
        this.setTabbing(firstbutton, null, editor.getDocument().getWindow());
    };
};

KupuZoomTool.prototype = new KupuLateFocusStateButton;
KupuZoomTool.prototype.baseinitialize = KupuZoomTool.prototype.initialize;

KupuZoomTool.prototype.onscroll = function() {
    if (!this.zoomed) return;
    /* XXX Problem here: Mozilla doesn't generate onscroll when window is
     * scrolled by focus move or selection. */
    var top = window.pageYOffset!=undefined ? window.pageYOffset : document.documentElement.scrollTop;
    var left = window.pageXOffset!=undefined ? window.pageXOffset : document.documentElement.scrollLeft;
    if (top || left) window.scrollTo(0, 0);
};

// Handle tab pressed from a control.
KupuZoomTool.prototype.setTabbing = function(control, forward, backward) {
    function TabDown(event) {
        if (event.keyCode != 9 || !this.zoomed) return;

        var target = event.shiftKey ? backward : forward;
        if (!target) return;

        if (event.stopPropogation) event.stopPropogation();
        event.cancelBubble = true;
        event.returnValue = false;

        target.focus();
        return false;
    }
    addEventHandler(control, "keydown", TabDown, this);
};

KupuZoomTool.prototype.onresize = function() {
    if (!this.zoomed) return;

    var editor = this.editor;
    var iframe = editor.getDocument().editable;
    var sourcetool = editor.getTool('sourceedittool');
    var sourceArea = sourcetool?sourcetool.getSourceArea():null;
    var fulleditor = iframe.parentNode;

    if (window.innerWidth) {
        var width = window.innerWidth;
        var height = window.innerHeight;
    } else if (document.documentElement) {
        if (!window._IE_VERSION) {
            _IE_VERSION = /MSIE\s*([0-9.]*)/.exec(navigator.appVersion);
        };
        var kludge = (_IE_VERSION[1]<7)?5:0;
        var width = document.documentElement.offsetWidth-kludge;
        var height = document.documentElement.offsetHeight-kludge;
    } else {
        var width = document.body.offsetWidth-5;
        var height = document.body.offsetHeight-5;
    }
    var offset = fulleditor.offsetTop;
    var nheight = Math.max(height - offset -1/*top border*/, 10) + 'px';
    width = width + 'px';
    fulleditor.style.width = width; /*IE needs this*/
    iframe.style.width = width;
    iframe.style.height = nheight;
    if (sourceArea) {
        sourceArea.style.width = width;
        sourceArea.style.height = nheight;
    }
};

KupuZoomTool.prototype.checkfunc = function(selNode, button, editor, event) {
    return this.zoomed;
};

KupuZoomTool.prototype.commandfunc = function(button, editor) {
    /* Toggle zoom state */
    var zoom = button.pressed;
    this.zoomed = zoom;

    var zoomClass = 'kupu-fulleditor-zoomed';
    var iframe = editor.getDocument().getEditable();

    var body = document.body;
    var html = document.getElementsByTagName('html')[0];
    var doc = editor.getInnerDocument();
    if (zoom) {
        html.style.overflow = 'hidden';
        window.scrollTo(0, 0);
        editor.setClass(zoomClass);
        body.className += ' '+zoomClass;
        doc.body.className += ' '+zoomClass;
        this.onresize();
    } else {
        html.style.overflow = '';
        var fulleditor = iframe.parentNode;
        fulleditor.style.width = '';
        body.className = body.className.replace(/ *kupu-fulleditor-zoomed/, '');
        doc.body.className = doc.body.className.replace(/ *kupu-fulleditor-zoomed/, '');
        editor.clearClass(zoomClass);

        iframe.style.width = '';
        iframe.style.height = '';

        var sourcetool = editor.getTool('sourceedittool');
        var sourceArea = sourcetool?sourcetool.getSourceArea():null;
        if (sourceArea) {
            sourceArea.style.width = '';
            sourceArea.style.height = '';
        };
    }
    // Mozilla needs this. Yes, really!
    doc.designMode=doc.designMode;

    window.scrollTo(0, iframe.offsetTop);
    editor.focusDocument();
};

/* The anchor tool */
function AnchorTool() {};
AnchorTool.prototype = new LinkTool;
var proto = AnchorTool.prototype;

proto.fillStyleSelect = function(select) {
    var ui = this.editor.getTool('ui');
    var options = ui.getStyles()[0];

    for (var i = 1; i < options.length-1; i++) {
        var cur = options[i];
        var opt = document.createElement('option');
        opt.text = cur[0];
        opt.value = cur[1];
        select.options.add(opt);
    }
};

proto.grubParas = function(style1, style2) {
    var doc = this.editor.getInnerDocument();
    var body = doc.body;
    var paras = [];
    var pat = /(.*?)(\|.*|$)/;
    var tag1 = style1.replace(pat, '$1');
    var tag2 = style2.replace(pat, '$1');
    if (tag2 && tag2 != tag1) { tag1 = "*"; }
    if (tag1) {
        var nodes = body.getElementsByTagName(tag1);
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            var style = node.nodeName.toLowerCase() + "|" + node.className;
            if (style==style1) {
                paras.push([node,0]);
            }
            if (style==style2) {
                paras.push([node,1]);
            }
        }
    }
    return paras;
};

proto.getAnchorsInUse = function() {
    var doc = this.editor.getInnerDocument();
    var anchors = doc.getElementsByTagName('a');
    var inuse = {};
    for (var i = 0; i < anchors.length; i++) {
        var m = (/(.*)(#.*)$/.exec(anchors[i].href));
        /* TODO: filter out external links */
        if (m) { inuse[decodeURIComponent(m[2])] = 1; };
    }
    return inuse;
};

proto.removeAnchor = function(node) {
    var anchors = node.getElementsByTagName('a');
    if (anchors.length > 0) {
        var anchor = anchors[0];
        anchor.removeAttribute('name');
        if (anchor.href) return;
        anchor.parentNode.removeChild(anchor);
    };
};

proto.getAnchor = function(node, ifexists) {
    /* Returns the anchor for a node, creating one if reqd. unless
     * ifexists is set*/
    var anchors = node.getElementsByTagName('a');
    for (var i = 0; i < anchors.length; i++) {
        if (anchors[i].name) { return anchors[i].name; }
    }

    if (ifexists) return;

    var anchor = Sarissa.getText(node, true).strip().truncate(40).
        replace(/[^\w]+/g, '-').toLowerCase().replace(/-$/,'') || 'anchor';
    anchor = anchor.replace(/^((?:[^-]*-){0,3}[^-]*)(.*)$/,'$1');

    var unique = 0;
    var existing = this.editor.getInnerDocument().anchors;
    for (var i = 0; i < existing.length; i++) {
        var name = existing[i].name;
        if (name==anchor) {
            if (unique==0) unique = -1;
        } else if (name.length > anchor.length && name.substring(0,anchor.length)==anchor) {
            var tail = name.substring(anchor.length);
            tail = parseInt(tail);
            if (tail <= unique) {
                unique = tail-1;
            }
        }
    }
    if (unique) anchor += unique.toString();
    node.insertBefore(this.editor.newElement('a', {'name': anchor}),
        node.firstChild);
    return anchor;
};

/* IE doesn't have a dump function */
if (window.dump===undefined) {
    var dump = function(msg) { };
};
proto.createContextMenuElements = function() {
    return [];
};

