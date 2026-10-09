/*****************************************************************************
 *
 * Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupuinspector.js 35854 2006-12-18 15:37:15Z duncan $

/* The Kupu Inspector tool 

    An Kupu Tool (plugin) that will can be used to show and set attributes
    on elements. It will show a list of the current element and all of its
    parents (starting with the body element and working to the current one)
    with input fields for a default set of attributes and, if defined, a
    set for that particular element type.
*/

//----------------------------------------------------------------------------
// Helper classes
//----------------------------------------------------------------------------

function Panel() {
    /* the container (user interface element) of the elements */
    this.elements = [];
    
    this.element = document.createElement('table');
    this.element.style.width = '100%';
    this.tbody = document.createElement('tbody');
    this.element.appendChild(this.tbody);
    
    this.addElement = function(element) {
        this.elements.push(element);
        for (var i=0; i < element.nodes.length; i++) {
            this.tbody.appendChild(element.nodes[i]);
        };
    };
};

function Element(node, panel, visibility) {
    /* an element in the panel (reflecting an element in the document) */
    this.panel = panel;
    this.node = node;
    this.nodes = [];
    this.default_visibility = visibility;
    
    // create a header
    var labelrow = document.createElement('tr');
    var labelcell = document.createElement('th');
    labelcell.style.textDecoration = 'underline';
    labelcell.style.cursor = 'default';
    labelcell.setAttribute('colSpan', '2');
    labelrow.appendChild(labelcell);
    var nodename = node.nodeName.toLowerCase();
    var labeltext = document.createTextNode(nodename);
    labelcell.appendChild(labeltext);
    
    this.nodes.push(labelrow);

    this._displayvar = _SARISSA_IS_IE ? 'block' : 'table-row';
    
    this.addAttribute = function(attr) {
        /* add an attribute */
        
        function changeHandler() {
            var name = this.getAttribute('name');
            var value = this.value;
            if (name == 'className') {
                this.element.className = value;
            } else {
                this.element.setAttribute(name, value);
            };
        };
        
        var row = document.createElement('tr');
        var style = this.default_visibility ? this._displayvar : 'none';
        row.style.display = style;
        var labelcell = document.createElement('td');
        labelcell.style.fontSize = '10px';
        row.appendChild(labelcell);
        var text = document.createTextNode(attr + ': ');
        labelcell.appendChild(text);
        labelcell.style.color = 'blue';
        var inputcell = document.createElement('td');
        inputcell.setAttribute('width', '100%');
        row.appendChild(inputcell);
        var input = document.createElement('input');
        input.setAttribute('type', 'text');
        input.setAttribute('value', attr == 'className' ? node.className : node.getAttribute(attr));
        input.setAttribute('name', attr);
        input.style.width = "100%";
        input.element = this.node;
        addEventHandler(input, 'change', changeHandler, input);
        inputcell.appendChild(input);
        this.nodes.push(row);
    };

    this.addStyle = function(stylename) {
        var row = document.createElement('tr');
        var style = this.default_visibility ? this._displayvar : 'none';
        row.style.display = style;
        var labelcell = document.createElement('td');
        labelcell.style.fontSize = '10px';
        row.appendChild(labelcell);
        var text = document.createTextNode(stylename + ': ');
        labelcell.appendChild(text);
        labelcell.style.color = 'red';
        var inputcell = document.createElement('td');
        //inputcell.setAttribute('width', '100%');
        row.appendChild(inputcell);
        var input = document.createElement('input');
        input.setAttribute('type', 'text');
        input.setAttribute('value', node.style[stylename]);
        input.setAttribute('name', stylename);
        input.style.width = "100%";
        input.element = this.node;
        addEventHandler(input, 'change', function() {this.element.style[this.getAttribute('name')] = this.value;}, input);
        inputcell.appendChild(input);
        this.nodes.push(row);
    };

    this.setVisibility = function(visibility) {
        for (var i=1; i < this.nodes.length; i++) {
            this.nodes[i].style.display = visibility ? this._displayvar : 'none';
        };
    };

    this.setVisible = function() {
        for (var i=0; i < this.panel.elements.length; i++) {
            var el = this.panel.elements[i];
            if (el != this) {
                el.setVisibility(false);
            };
            this.setVisibility(true);
        };
    };

    addEventHandler(labelrow, 'click', this.setVisible, this);
};

//----------------------------------------------------------------------------
// The inspector
//----------------------------------------------------------------------------

function KupuInspector(inspectorelement) {
    /* the Inspector tool, a tool to set attributes on elements */
    
    this.element = getFromSelector(inspectorelement);
    this._lastnode = null;

    this.default_attrs = ['id', 'className'];
    this.special_attrs = {'a': ['href', 'name', 'target'],
                            'img': ['url', 'width', 'height'],
                            'ul': ['type'],
                            'ol': ['type'],
                            'table': ['border', 'cellPadding', 'cellSpacing'],
                            'td': ['align']
                            };
    this.styles = ['background', 'borderWidth', 'borderColor', 
                                'borderStyle', 'color', 'fontSize', 
                                'fontFamily', 'float', 'height', 
                                'lineHeight', 'margin', 'padding', 
                                'textAlign', 'verticalAlign', 'whiteApace', 
                                'width'];
    
    this.updateState = function(selNode, event) {
        /* repopulate the inspector (if required) */
        if (selNode != this._lastnode) {
            // we need to repopulate
            this._lastnode = selNode;
            this._clear();
            var panel = new Panel();
            var currnode = selNode;
            // walk up to the body, add the elements in an array so we can
            // walk through it backwards later on
            var els = [];
            while (currnode.nodeName.toLowerCase() != 'html') {
                // only use element nodes
                if (currnode.nodeType == 1) {
                    els.push(currnode);
                };
                currnode = currnode.parentNode;
            };

            for (var i=0; i < els.length; i++) {
                // now build an element
                var node = els[els.length - i - 1];
                var nodename = node.nodeName.toLowerCase();
                var visibility = (i == els.length - 1);
                var element = new Element(node, panel, visibility);
                
                // walk through the default attrs
                for (var j=0; j < this.default_attrs.length; j++) {
                    var attr = this.default_attrs[j];
                    element.addAttribute(attr);
                };
                // check if there are any special attrs for this type of element
                if (nodename in this.special_attrs) {
                    var sattrs = this.special_attrs[nodename];
                    // add the attrs
                    for (var j=0; j < sattrs.length; j++) {
                        var attr = sattrs[j];
                        element.addAttribute(attr);
                    };
                };
                // and add all applicable styles
                for (var j=0; j < this.styles.length; j++) {
                    var style = this.styles[j];
                    if (style in node.style) {
                        element.addStyle(style);
                    };
                };
                panel.addElement(element);
            };
            this.element.appendChild(panel.element);
        };
    };

    this._clear = function() {
        while (this.element.childNodes.length) {
            this.element.removeChild(this.element.childNodes[0]);
        };
    };
};

KupuInspector.prototype = new KupuTool;
