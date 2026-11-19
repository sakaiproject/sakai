/*****************************************************************************
 *
 * Copyright (c) 2003-2008 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/
// $Id: kupudrawers.js 928511 2010-03-28 22:53:14Z lu4242 $

function kupu_busy(ed) {
    if (ed.busy) ed.busy();
}
function kupu_notbusy(ed, force) {
    if (ed.notbusy) ed.notbusy(force);
}
function DrawerTool() {
    /* a tool to open and fill drawers
       this tool has to (and should!) only be instantiated once
    */
    this.drawers = {};
    this.current_drawer = null;

    this.initialize = function(editor) {
        this.editor = editor;
        this.isIE = this.editor.getBrowserName() == 'IE';
        // this essentially makes the drawertool a singleton
        window.drawertool = this;
    };

    this.registerDrawer = function(id, drawer, editor) {
        this.drawers[id] = drawer;
        drawer.initialize(editor || this.editor, this);
    };

    this.openDrawer = function(id, args) {
        /* open a drawer */
        if (this.current_drawer) {
            this.closeDrawer();
        };
        var drawer = this.drawers[id];
        if (this.isIE) {
            drawer.editor._saveSelection();
        }
        this.current_drawer = drawer;
        if (args===undefined) args = [];
        if (this.isIE) {
            drawer.initMask(drawer.element);
        }
        drawer.createContent.apply(drawer, args);
        drawer.editor.suspendEditing();
        drawer.fixMask();
    };

    this.updateState = function(selNode) {
    };

    this.closeDrawer = function(button) {
        if (!this.current_drawer) {
            return;
        };
        this.current_drawer.hide();
        this.current_drawer.editor.resumeEditing();
        this.current_drawer = null;
        kupu_notbusy(this.editor, true);
    };
};

DrawerTool.prototype = new KupuTool;

function Drawer(elementid, tool) {
    /* base prototype for drawers */

    this.element = getFromSelector(elementid);
    this.tool = tool;
}
var proto = Drawer.prototype;

proto.initialize = function(editor, drawertool) {
    this.editor = editor;
    this.drawertool = drawertool;
    // on a small screen, the appearance of the drawer could scroll the screen.
    // Here we stored the position, so that we can restore it for the user.
    this.scrollPosition = document.documentElement.scrollTop;

};

proto.createContent = function() {
    /* fill the drawer with some content */
    // here's where any intelligence and XSLT transformation and such
    // is done
    this.element.style.display = 'block';
    this.focusElement();
};

proto.hide = function() {
    if (this.maskframe) {
        this.maskframe.style.display='none';
    }
    this.element.style.display = 'none';
    this.focussed = false;
    document.documentElement.scrollTop = this.scrollPosition; // see initialize
};

proto.focusElement = function() {
    // IE can focus the drawer element, but Mozilla needs more help
    this.focussed = false;
    var iterator = new NodeIterator(this.element);
    var currnode = iterator.next();
    while (currnode) {
        if (currnode.tagName && (currnode.tagName.toUpperCase()=='BUTTON' ||
            (currnode.tagName.toUpperCase()=='INPUT' && !(/nofocus/.test(currnode.className)))
            )) {
            this.focussed = true;
            function focusit() {
                try {
                    currnode.focus();
                }catch(e){};
            }
            timer_instance.registerFunction(this, focusit, 100);
            return;
        }
        currnode = iterator.next();
    }
};

proto.initMask = function(el) {
    var e = (this.maskframe = document.getElementById('kupu-maskframe'));
    if (!this.maskframe) {
        e = this.maskframe = newElement('iframe',
            {'id':'kupu-maskframe','src':"javascript:false;", 'frameBorder':"0", 'scrolling':"no" });
        var style = e.style;
        style.display = 'none';
    }
    el.parentNode.insertBefore(e, el);
};

proto.fixMask = function() {
    var mask = this.maskframe;
    if (mask) {
        if (mask.parentNode != this.element.parentNode) {
            this.element.parentNode.insertBefore(mask, this.element);
        }
        // display the mask to hide SELECT boxes in IE
        var el = this.element;
        var st = mask.style;
        var st1 = el.style;
        st.top=st1.top;
        st.left=st1.left;
        st.width = el.offsetWidth+'px';
        st.height = el.offsetHeight+'px';
        st.left = (el.offsetLeft)+'px';
        st.position = 'absolute';
        st.display = '';
    }
};

proto.switchMode = function(event) {
    event = event || window.event;
    var target = event.currentTarget || event.srcElement;
    var el = target;
    while (!(/^li$/i.test(el.nodeName))) { el = el.parentNode; };
    var thistab = el;
    while (!(/^ul$/i.test(el.nodeName))) { el = el.parentNode; };
    var tabs = el.getElementsByTagName('li');
    for (var i = 0; i < tabs.length; i++) {
        var el = tabs[i];
        var cls = el.className.replace(/\s*selected/g, '');
        if (el===thistab) {
            this.panel.className = 'kupu-panels '+cls;
            cls += ' selected';
        }
        if (el.className != cls) {
            el.className = cls;
        }
    }
    if (this.fillList) this.fillList();
    this.fixMask();
    if (event.preventDefault) { event.preventDefault();}
    event.returnValue = false;
    return false;
};


function DrawerWithAnchors(editor, drawertool, anchorui) {
    Drawer.call(this, editor, drawertool);
    this.anchorui = anchorui;
    this.anchorframe = null;
}
DrawerWithAnchors.prototype = new Drawer;
proto = DrawerWithAnchors.prototype;

proto.initAnchors = function() {
    var limit = 40;
    var anchorframe = this.anchorframe;
    var ed = this.editor;
    function onloadEvent() {
        var state = anchorframe.readyState;
        if (state && !(/complete/.test(state))) {
            if (limit-- && anchorframe.src==src) {
                timer_instance.registerFunction(this, onloadEvent, 500);
            } else {
                kupu_notbusy(ed, true);
            }
            return;
        };
        if(window.drawertool && window.drawertool.current_drawer) {
            window.drawertool.current_drawer.anchorframe_loaded();
        };
        kupu_notbusy(ed);
    };

    var id = 'kupu-linkdrawer-anchors';
    var base = (this.anchorui = getBaseTagClass(this.element, 'div', id));
    if (base) {
        var inp = base.getElementsByTagName('input');
        if (inp.length > 1) {
            inp[1].disabled = true;
        }
        var src = inp[0].value;
        inp[0].value = "";
        if (!src) { return; }
        kupu_busy(ed);
        if (this.anchorframe.readyState) { // IE
            anchorframe.src = src;
            onloadEvent();
        } else { // FF
            this.anchorframe.onload = onloadEvent;
            anchorframe.src = src;
        }
    }
};
proto.anchorSelect = function() {
    return this.anchorui && this.anchorui.getElementsByTagName('select')[0];
};

proto.addSelectEvent = function() {
    var s = this.anchorSelect();
    if (s) {
        addEventHandler(s, 'change', this.selChange, this);
    }
};

proto.hideAnchors = function() {
    this.anchorui.style.display = 'none';
};

proto.anchorText = function(a) {
    // Text inside anchor, or immediate sibling block tag, or parent block.
    var blocktag = /^div|p|body|td|h.$/i;
    var txt = '';
    var prefix = '#' + a.name;

    for (var node = a; node && !txt; node=node.parentNode) {
        var txt = node.textContent || node.innerText || '';
        if (txt || blocktag.test(node.nodeName)) {
            break;
        }

        for (var sibling = node.nextSibling; sibling && !txt; sibling = sibling.nextSibling) {
            if (sibling.nodeName.toLowerCase()=='#text') {
                txt = sibling.data.strip();
            } else {
                txt += sibling.textContent || sibling.innerText ||'';
            };
            txt = txt.strip();
        }
    }
    if (txt) {
        txt = ' (' + (txt||'').substring(0,80).reduceWhitespace().strip()+')';
    }
    return prefix + txt;
};

proto.selChange = function() {};

proto.anchorframe_loaded = function() {
    this.showAnchors('');
};

proto.showAnchors = function(selected) {
    var select = this.anchorSelect();
    if (select == undefined) return;
    var opts = select.options;

    while (opts.length > 1) opts[1] = null;
    try {
        var doc = this.anchorframe.contentWindow.document;
        var anchors = doc.anchors;
    } catch(e) {
        this.hideAnchors();
        return;
    }
    for (var i = 0; i < anchors.length; i++) {
        var a = anchors[i];
        if (a.name) {
            var opt = document.createElement('option');
            opt.text = this.anchorText(anchors[i]);
            var v = anchors[i].name;
            opt.value = v;
            if (v==selected) opt.selected = true;
            select.options.add(opt);
        }
    }
    select.disabled = false;
    if (opts.length > 1) {
        this.anchorui.style.display = '';
    }
};

proto.getFragment = function() {
    var select = this.anchorSelect();
    if (select) {
        var anchor = select.options[select.selectedIndex].value;
        if (anchor) return '#' + anchor;
    }
    return '';
};

function LinkDrawer(elementid, tool) {
    /* Link drawer */
    DrawerWithAnchors.call(this, elementid, tool);

    var input = getBaseTagClass(this.element, 'input', 'kupu-linkdrawer-input');
    var embed = getBaseTagClass(this.element, 'textarea', 'kupu-embed-input');
    var preview = getBaseTagClass(this.element, 'iframe', 'kupu-linkdrawer-preview');
    var watermark = getBaseTagClass(this.element, 'div', 'watermark');
    this.anchorframe = preview;
    this.anchorui = getBaseTagClass(this.element, 'tr', 'kupu-linkdrawer-anchors');
    this.target = '';
    this.panel = getBaseTagClass(this.element, 'div', 'kupu-panels');
    var kuputabs = getBaseTagClass(this.element, 'ul', 'kupu-tabs');
    if (kuputabs) {
	var tabs = kuputabs.getElementsByTagName('a');
	for (var i = 0; i < tabs.length; i++) {
            addEventHandler(tabs[i], 'click', this.switchMode, this);
	}
    }
    if (embed) {
	addEventHandler(embed, 'click', function() { if(embed.defaultValue==embed.value) {embed.select();} });
    }

    this.selChange = function() {
        var anchor = this.getFragment();

        input.value = input.value.replace(/#[^#]*$/, '');
        if (anchor) {
            input.value += anchor;
        }
    };
    this.addSelectEvent();

    this.createContent = function() {
        /* display the drawer */
        var ed = this.editor;
        var currnode = ed.getSelectedNode();
        var linkel = ed.getNearestParentOfType(currnode, 'a');
        input.value = "";

        this.preview();
        if (linkel) {
            input.value = linkel.getAttribute('href');
        } else {
            input.value = 'http://';
        };
        var obj = ed.getNearestParentOfType(currnode, 'object') || ed.getNearestParentOfType(currnode, 'embed');
        if (obj) {
            embed.value = getOuterHtml(obj);
        } else {
            embed.value = embed.defaultValue;
        }
        this.element.style.display = 'block';
        this.hideAnchors();
        this.focusElement();
    };

    this.save = function() {
        /* add or modify a link */
        this.editor.resumeEditing();
        if (this.getMode()) {
            var url = input.value;
            this.tool.createLink(url, null, null, this.target, null, 'external-link');
            input.value = '';
        } else {
            // Import the html
            var doc = this.editor.getInnerDocument();
            var selection = this.editor.getSelection();
            var dummy = doc.createElement("div");
            dummy.innerHTML = embed.value;
            try {
                for (var j=dummy.childNodes.length-1; j >= 0; j--) {
                    var c = dummy.childNodes[j];
                    if (/^\//.test(c.nodeName))
                    {
                        dummy.removeChild(c);
                    }
                }
                while (dummy.firstChild) {
                    var c= dummy.firstChild;
                    selection.replaceWithNode(c, !c.nextSibling);
                };
            } catch(e) {};
        }
        // XXX when reediting a link, the drawer does not close for
        // some weird reason. BUG! Close the drawer manually until we
        // find a fix:
        this.drawertool.closeDrawer();
    };


    function currentAnchor() {
        var bits = input.value.split('#');
        var current = bits.length > 1 ? bits[bits.length-1] : '';
        return current;
    }

    this.getMode = function() {
        return !!(/addlink/.test(this.panel.className));
    };
    this.preview = function() {
        if (this.getMode()) {
            var ok = false;
            watermark.style.display='';
            if (/^http(s?):\x2f\x2f./.test(input.value)) {
                try {
                    preview.src = input.value;
                    ok = true;
                } catch(e) { alert('Preview blew up"'+input.value+'"');};
            } else {
                preview.src = '';
                if (input.value.strip()) {
                    alert(_('Can only preview web urls'));
                }
            }
            if (ok) {
                this.showAnchors(currentAnchor());
                if (this.editor.getBrowserName() == 'IE') {
                    preview.width = "800";
                    preview.height = "365";
                    preview.style.zoom = "60%";
                };
            }
        };
    };

    this.preview_loaded = function() {
        watermark.style.display = (/^http(s?):\x2f\x2f./.test(input.value))?'none':'';
        var here = input.value;
        try {
            var there = preview.contentWindow.location.href;
        } catch(e) { return; }

        if (there && here != there && !(/^about:/.test(there))) {
            input.value = there;
        }
        this.showAnchors(currentAnchor());
    };
    addEventHandler(preview, "load", this.preview_loaded, this);
};

LinkDrawer.prototype = new DrawerWithAnchors;

function TableDrawer(elementid, tool) {
    /* Table drawer */
    this.element = getFromSelector(elementid);
    this.tool = tool;

    this.addpanel = getBaseTagClass(this.element, 'div', 'kupu-tabledrawer-addtable');
    this.editpanel = getBaseTagClass(this.element, 'div', 'kupu-tabledrawer-edittable');
    var editclassselect = getBaseTagClass(this.element, 'select', 'kupu-tabledrawer-editclasschooser');
    var addclassselect = getBaseTagClass(this.element, 'select', 'kupu-tabledrawer-addclasschooser');
    var alignselect = getBaseTagClass(this.element, 'select', 'kupu-tabledrawer-alignchooser');
    var newrowsinput = getBaseTagClass(this.element, 'input', 'kupu-tabledrawer-newrows');
    var newcolsinput = getBaseTagClass(this.element, 'input', 'kupu-tabledrawer-newcols');
    var makeheadercheck = getBaseTagClass(this.element, 'input', 'kupu-tabledrawer-makeheader');

    this.createContent = function() {
        var editor = this.editor;
        var selNode = editor.getSelectedNode();

        function fixClasses(classselect) {
            if (editor.config.table_classes) {
                var classes = editor.config.table_classes['class'];
                while (classselect.hasChildNodes()) {
                    classselect.removeChild(classselect.firstChild);
                };
                for (var i=0; i < classes.length; i++) {
                    var classinfo = classes[i];
                    var caption = classinfo.xcaption || classinfo;
                    var classname = classinfo.classname || classinfo;

                    var option = document.createElement('option');
                    var content = document.createTextNode(caption);
                    option.appendChild(content);
                    option.setAttribute('value', classname);
                    classselect.appendChild(option);
                };
            };
        };
        fixClasses(addclassselect);
        fixClasses(editclassselect);

        var table = editor.getNearestParentOfType(selNode, 'table');
        var show, hide;
        if (!table) {
            // show add table drawer
            show = this.addpanel;
            hide = this.editpanel;
        } else {
            // show edit table drawer
            show = this.editpanel;
            hide = this.addpanel;
            var align = this.tool._getColumnAlign(selNode);
            selectSelectItem(alignselect, align);
            selectSelectItem(editclassselect, table.className);
        };
        hide.style.display = 'none';
        show.style.display = 'block';
        this.element.style.display = 'block';
        this.focusElement();
    };

    this.createTable = function() {
        this.editor.resumeEditing();
        var rows = newrowsinput.value;
        var cols = newcolsinput.value;
        var style = addclassselect.value;
        var add_header = makeheadercheck.checked;
        this.tool.createTable(parseInt(rows), parseInt(cols), add_header, style);
        this.drawertool.closeDrawer();
    };

    this.delTableRow = function() {
        this.editor.resumeEditing();
        this.tool.delTableRow();
        this.editor.suspendEditing();
    };

    this.addTableRow = function() {
        this.editor.resumeEditing();
        this.tool.addTableRow();
        this.editor.suspendEditing();
    };

    this.delTableColumn = function() {
        this.editor.resumeEditing();
        this.tool.delTableColumn();
        this.editor.suspendEditing();
    };

    this.addTableColumn = function() {
        this.editor.resumeEditing();
        this.tool.addTableColumn();
        this.editor.suspendEditing();
    };

    this.fixTable = function() {
        this.editor.resumeEditing();
        this.tool.fixTable();
        this.editor.suspendEditing();
    };

    this.fixAllTables = function() {
        this.editor.resumeEditing();
        this.tool.fixAllTables();
        this.editor.suspendEditing();
    };

    this.delTable = function() {
        this.editor.resumeEditing();
        this.tool.delTable();
        this.drawertool.closeDrawer();
    };

    this.setTableClass = function(className) {
        this.editor.resumeEditing();
        this.tool.setTableClass(className);
        this.editor.suspendEditing();
    };

    this.setColumnAlign = function(align) {
        this.editor.resumeEditing();
        this.tool.setColumnAlign(align);
        this.editor.suspendEditing();
    };
};

TableDrawer.prototype = new Drawer;

function LibraryDrawer(tool, xsluri, libsuri, searchuri, baseelement, selecturi) {
    /* a drawer that loads XSLT and XML from the server
       and converts the XML to XHTML for the drawer using the XSLT

       there are 2 types of XML file loaded from the server: the first
       contains a list of 'libraries', partitions for the data items,
       and the second a list of data items for a certain library

       all XML loading is done async, since sync loading can freeze Mozilla
    */
    this.showupload = '';
    this.showanchors = '';
    this.multiple = false;
    this.currentSelection = [];

    this.init = function(tool, xsluri, libsuri, searchuri, baseelement, selecturi) {
        /* This method is there to thin out the constructor and to be
           able to inherit it in sub-prototypes. Don't confuse this
           method with the component initializer (initialize()).
        */
        // these are used in the XSLT. Maybe they should be
        // parameterized or something, but we depend on so many other
        // things implicitly anyway...
        this.drawerid = 'kupu-librarydrawer';
        this.librariespanelid = 'kupu-librariespanel';
        this.resourcespanelid = 'kupu-resourcespanel';
        this.propertiespanelid = 'kupu-propertiespanel';
        this.breadcrumbsid = 'kupu-breadcrumbs';

        if (baseelement) {
            this.baseelement = getFromSelector(baseelement);
        } else {
            this.baseelement = getBaseTagClass(document.body, 'div', 'kupu-librarydrawer-parent');
        }
        this.anchorframe = getBaseTagClass(this.baseelement, 'iframe', 'kupu-anchorframe');
        var e;
        this.tool = tool;
        this.element = document.getElementById(this.drawerid);
        if (!this.element) {
            e = document.createElement('div');
            e.id = this.drawerid;
            e.className = 'kupu-drawer '+this.drawerid;
            this.baseelement.appendChild(e);
            this.element = e;
        }
        this.shared.xsluri = xsluri;
        this.libsuri = libsuri;
        this.searchuri = searchuri;
        this.selecturi = selecturi;

        // marker that gets set when a new image has been uploaded
        this.shared.newimages = null;

        // the following vars will be available after this.initialize()
        // has been called

        // this will be filled by this._libXslCallback()
        this.shared.xsl = null;
        // this will be filled by this.loadLibraries(), which is called
        // somewhere further down the chain starting with
        // this._libsXslCallback()
        this.xmldata = null;

    };
    if (tool) {
        this.init(tool, xsluri, libsuri, searchuri, baseelement, selecturi);
    }

    this.initialize = function(editor, drawertool) {
        this.editor = editor;
        this.drawertool = drawertool;

        // load the xsl and the initial xml
        this._loadXML(this.shared.xsluri, this._libsXslCallback);
    };

    this.hide = function() {
        var el = this.element;
        el.style.left = el.style.top = '';
        LibraryDrawer.prototype.hide.call(this);
    };




    /*** bootstrapping ***/

    this._libsXslCallback = function(dom) {
        /* callback for when the xsl for the libs is loaded

            this is called on init and since the initial libs need
            to be loaded as well (and everything is async with callbacks
            so there's no way to wait until the XSL is loaded) this
            will also make the first loadLibraries call
        */
        this.shared.xsl = dom;
        Sarissa.getDomDocument(); /* Work round Sarissa initialisation glitch */

        // Change by Paul to have cached xslt transformers for reuse of
        // multiple transforms and also xslt params
        try {
            var xsltproc =  new XSLTProcessor();
            this.shared.xsltproc = xsltproc;
            xsltproc.importStylesheet(dom);
            xsltproc.setParameter("", "ie", this.editor.getBrowserName() == 'IE');
            xsltproc.setParameter("", "drawertype", this.drawertype);
            xsltproc.setParameter("", "drawertitle", this.drawertitle);
            xsltproc.setParameter("", "showupload", this.showupload);
            xsltproc.setParameter("", "showanchors", this.showanchors);
            if (this.target !== undefined) {
                xsltproc.setParameter("", "link_target", this.target);
            }
            if (this.editor.config && !!this.editor.config.captions) {
                xsltproc.setParameter("", "usecaptions", 'yes');
            }
        } catch(e) {
            if (e && e.name && e.message) e = e.name+': '+e.message;
            alert("xlstproc error:" + e);
            return; // No XSLT Processor, maybe IE 5.5?
        }
        if (this.xmldata) {
            this.updateDisplay(this.drawerid);
        };
    };

    this.setTitle = function(t) {
        this.drawertitle = t;
        var xsltproc = this.shared.xsltproc;
        if (xsltproc) {
            xsltproc.setParameter("", "drawertitle", this.drawertitle);
        };
    };

    this.createContent = function() {
        this.removeSelection();
        // Make sure the drawer XML is in the current Kupu instance
        if (this.element.parentNode != this.baseelement) {
            this.baseelement.appendChild(this.element);
        }
        // load the initial XML
        if(!this.xmldata) {
            // Do a meaningful test to see if this is IE5.5 or some other
            // editor-enabled version whose XML support isn't good enough
            // for the drawers
            if (!window.XSLTProcessor) {
               alert("This function requires better XML support in your browser.");
               return;
            }
            this.loadLibraries();
        } else {
            var libraries = this.xmldata.selectSingleNode("/libraries");
            var old = libraries.selectSingleNode("library[@id='kupu-current-selection']");
            if (old) {
                libraries.removeChild(old);
            }
            if (this.shared.newimages) {
                this.reloadCurrent();
                this.shared.newimages = null;
            };
            this.updateDisplay();
            this.initialSelection();
        };

        // display the drawer div
        this.element.style.display = 'block';
    };

    this._singleLibsXslCallback = function(dom) {
        /* callback for then the xsl for single libs (items) is loaded

            nothing special needs to be called here, since initially the
            items pane will be empty
        */
        this.singlelibxsl = dom;
    };

    this.loadLibraries = function() {
        /* load the libraries and display them in a redrawn drawer */
        this._loadXML(this.libsuri, this._libsContentCallback);
    };

    this._libsContentCallback = function(dom) {
        /* this is called when the libs xml is loaded

            does the xslt transformation to set up or renew the drawer's full
            content and adds the content to the drawer
        */
        this.xmldata = dom;
        this.xmldata.setProperty("SelectionLanguage", "XPath");

        // replace whatever is in there with our stuff
        this.updateDisplay(this.drawerid);
        this.initialSelection();
    };

    this.initialSelection = function() {
        if (this.selectedSrc && this.selecturi) {
            this.selectCurrent();
            return;
        }
        var libnode_path = '/libraries/library[@selected]';
        var libnode = this.xmldata.selectSingleNode(libnode_path);
        if (libnode) {
            var id = libnode.getAttribute('id');
            this.selectLibrary(id);
        }
    };

    this.updateDisplay = function(id) {
      /* (re-)transform XML and (re-)display the necessary part
       */
        if(!id) {
            id = this.drawerid;
        };

        var xsltproc = this.shared.xsltproc;
        if (!xsltproc) {
            return;
        }
        for (var k in this.options) {
            xsltproc.setParameter("", k, this.options[k]);
        }
        xsltproc.setParameter("", "multiple", this.multiple ? "yes" : "");
        xsltproc.setParameter("", "showupload", this.showupload ? "yes" : "");
        xsltproc.setParameter("", "showanchors", this.showanchors);

        var doc = this._transformXml();
        var sourcenode = doc.selectSingleNode('//*[@id="'+id+'"]');
        var targetnode = document.getElementById(id);
        if (!sourcenode || !targetnode) return;

        var cls = sourcenode.getAttribute('class');
        if (cls) {
            targetnode.className = cls;
        }
        Sarissa.copyChildNodes(sourcenode, targetnode);
        if (!this.focussed) {
            this.focusElement();
        }
        var el = document.getElementById('kupu-preview-image');
        if (el && el.width=='1') {
            kupuFixImage(el);
        }
        // Mark drawer as having a selection or not
        var el = this.element;
        el.className = el.className.replace(' kupu-has-selection', '');
        if (this.xmldata.selectSingleNode("//*[@selected]//*[@checked]")) {
            this.element.className += ' kupu-has-selection';
        };

        if (this.editor.getBrowserName() == 'IE' && id == this.resourcespanelid) {
            this.updateDisplay(this.drawerid);
        };
        this.fixMask();
    };

    this.updateResources = function() {
        if (this.editor.getBrowserName() == 'IE') {
            this.updateDisplay(this.drawerid);
        } else {
            this.updateDisplay(this.breadcrumbsid);
            this.updateDisplay(this.resourcespanelid);
            this.updateDisplay(this.propertiespanelid);
        }
    };

    this.deselectActiveCollection = function() {
        var librariespanel = document.getElementById(this.librariespanelid);
        if (!librariespanel) return;

        var divs = librariespanel.getElementsByTagName('div');
        for (var i = 0; i < divs.length; i++) {
            var div = divs[i];
            div.className = div.className.replace(/[ -]*selected/,'');
        }
        /* Deselect the currently active collection or library */
        var selected;
        while ((selected = this.xmldata.selectSingleNode('//*[@selected]'))) {
            // deselect selected DOM node
            selected.removeAttribute('selected');
        };
    };

    /*** Load a library ***/

    this.selectLibrary = function(id) {
        /* unselect the currently selected lib and select a new one

            the selected lib (libraries pane) will have a specific CSS class
            (selected)
        */
        // remove selection in the DOM
        this.deselectActiveCollection();
        // as well as visual selection in CSS
        // XXX this is slow, but we can't do XPath, unfortunately
        var divs = this.element.getElementsByTagName('div');
        for (var i=0; i<divs.length; i++ ) {
          if (divs[i].className == 'kupu-libsource-selected') {
            divs[i].className = 'kupu-libsource';
          };
        };

        var libnode_path = '/libraries/library[@id="' + id + '"]';
        var libnode = this.xmldata.selectSingleNode(libnode_path);
        libnode.setAttribute('selected', '1');

        var items_xpath = "items";
        var items_node = libnode.selectSingleNode(items_xpath);

        if (items_node && !this.shared.newimages) {
            // The library has already been loaded before or was
            // already provided with an items list. No need to do
            // anything except for displaying the contents in the
            // middle pane. Newimages is set if we've lately
            // added an image.
            this.useCollection(libnode);
        } else {
            // We have to load the library from XML first.
            var src_uri = libnode.selectSingleNode('src/text()').nodeValue;
            src_uri = src_uri.strip(); // needs kupuhelpers.js
            // Now load the library into the items pane. Since we have
            // to load the XML, do this via a call back
            this._loadXML(src_uri, this._libraryContentCallback, null, false, libnode);
            this.shared.newimages = null;
        };
    };
    this.flagSelectedLib = function(id) {
        // instead of running the full transformations again we get a
        // reference to the element and set the classname...
        var newseldiv = document.getElementById(id);
        if (newseldiv) {
            newseldiv.className = 'kupu-libsource-selected';
        }
    };

    this._libraryContentCallback = function(dom, src_uri, libnode) {
        /* callback for when a library's contents (item list) is loaded

        This is also used as he handler for reloading a standard
        collection.
        */
        //var libnode = this.xmldata.selectSingleNode('//*[@selected]');
        var itemsnode = libnode.selectSingleNode("items");
        var bcnode = libnode.selectSingleNode("breadcrumbs");
        var newitemsnode = dom.selectSingleNode("//items");
        var newbc = dom.selectSingleNode("//breadcrumbs");

        // IE does not support importNode on XML document nodes. As an
        // evil hack, clone the node instead.

        if (this.editor.getBrowserName() == 'IE') {
            newitemsnode = newitemsnode.cloneNode(true);
            if (newbc) newbc = newbc.cloneNode(true);
        } else {
            newitemsnode = this.xmldata.importNode(newitemsnode, true);
            if (newbc) newbc = this.xmldata.importNode(newbc, true);
        }
        if (newbc) {
            if (bcnode) {
                libnode.replaceChild(newbc, bcnode);
            } else {
                libnode.appendChild(newbc);
            };
        };
        if (!itemsnode) {
            // We're loading this for the first time
            libnode.appendChild(newitemsnode);
        } else {
            // User has clicked reload
            libnode.replaceChild(newitemsnode, itemsnode);
        };
        this.useCollection(libnode);
    };

    this.selectBreadcrumb = function(item) {
        var src_uri = item.href;
        if (/\$src\$$/.test(src_uri)) {
            var target = this.xmldata.selectSingleNode('//resource[@selected]/uri/text()');
            if (target) {
                target = target.nodeValue.strip();
                src_uri = src_uri.replace(/\$src\$/, encodeURIComponent(target));
            } else {
                return false;
            };
        };
        this.deselectActiveCollection();
        this.removeSelection();

        // Case 2: We've already loaded the data, but there hasn't
        // been a reference made yet. So, make one :)

        var collnode_path = "/libraries/*[src/text()='" + src_uri + "']";
        var collnode = this.xmldata.selectSingleNode(collnode_path);
        if (collnode) {
            var items_node = collnode.selectSingleNode("items");
            if (items_node) {
                collnode.setAttribute('selected', '1');
                this.useCollection(collnode);
                return;
            }
        };

        // Case 3: We've not loaded the data yet, so we need to load it
        this._loadXML(src_uri, this._collectionContentCallback, null);
        return false;
    };

    this.useCollection = function(collnode) {
        if (this.currentSelection) {
            var leafnodes = collnode.selectNodes("//*[@checked]");
            for (var j = 0; j < leafnodes.length; j++) {
                leafnodes[j].removeAttribute('checked');
            };
            var sel = this.currentSelection;
            for (var i = 0; i < sel.length; i++) {
                var leafnodes = collnode.selectNodes("//*[@id='"+sel[i]+"']");
                for (var j = 0; j < leafnodes.length; j++) {
                    leafnodes[j].setAttribute('checked', '1');
                    if (!this.multiple) {
                        leafnodes[j].setAttribute('selected', '1');
                    };
                };
            };
        };
        collnode.setAttribute('selected', '1');
        this.flagSelectedLib(collnode.getAttribute('id'));
        this.updateResources();
    };
    /*** Load a collection ***/
    this.selectCollection = function(item, tag) {
        var id = item.id;

        tag = tag || 'collection';
        this.deselectActiveCollection();

        // First turn off current selection, if any
        this.removeSelection();

        var leafnode_path = "//"+tag+"[@id='" + id + "']";
        var leafnode = this.xmldata.selectSingleNode(leafnode_path);

        // Case 1: We've already loaded the data, so we just need to
        // refer to the data by id.
        var loadedInNode = leafnode.getAttribute('loadedInNode');
        if (loadedInNode) {
            var collnode_path = "/libraries/*[@id='" + loadedInNode + "']";
            var collnode = this.xmldata.selectSingleNode(collnode_path);
            if (collnode) {
                this.useCollection(collnode);
                return;
            };
        };

        // Case 2: We've already loaded the data, but there hasn't
        // been a reference made yet. So, make one :)
        var src_uri = leafnode.selectSingleNode('src/text()').nodeValue.strip();
        var collnode_path = "/libraries/*[src/text()='" + src_uri + "'][items]";
        var collnode = this.xmldata.selectSingleNode(collnode_path);
        if (collnode) {
            id = collnode.getAttribute('id');
            leafnode.setAttribute('loadedInNode', id);
            this.useCollection(collnode);
            return;
        };

        // Case 3: We've not loaded the data yet, so we need to load it
        // this is just so we can find the leafnode much easier in the
        // callback.
        leafnode.setAttribute('selected', '1');
        src_uri = leafnode.selectSingleNode('src/text()').nodeValue.strip();
        this._loadXML(src_uri, this._collectionContentCallback, null);
    };

    this._collectionContentCallback = function(dom, src_uri) {
        // Unlike with libraries, we don't have to find a node to hook
        // our results into (UNLESS we've hit the reload button, but
        // that is handled in _libraryContentCallback anyway).
        // We need to give the newly retrieved data a unique ID, we
        // just use the time.
        var date = new Date();
        var time = date.getTime();

        // attach 'loadedInNode' attribute to leaf node so Case 1
        // applies next time.
        var leafnode = this.xmldata.selectSingleNode('//*[@selected]');
        if (leafnode) {
            leafnode.setAttribute('loadedInNode', time);
        }
        this.deselectActiveCollection();

        var collnode = dom.selectSingleNode('/collection');
        collnode.setAttribute('id', time);
        collnode.setAttribute('selected', '1');

        var libraries = this.xmldata.selectSingleNode('/libraries');

        // IE does not support importNode on XML document nodes
        if (this.editor.getBrowserName() == 'IE') {
            collnode = collnode.cloneNode(true);
        } else {
            collnode = this.xmldata.importNode(collnode, true);
        }
        libraries.appendChild(collnode);
        this.useCollection(collnode);
    };

    /*** Reloading a collection or library ***/

    this.reloadCurrent = function() {
        // Reload current collection or library
        this.showupload = '';
        var current = this.xmldata.selectSingleNode('//*[@selected]');
        // make sure we're dealing with a collection even though a
        // resource might be selected
        if (current.tagName == "resource") {
            current.removeAttribute("selected");
            current = current.parentNode;
            current.setAttribute("selected", "1");
        };
        var src_node = current.selectSingleNode('src');
        if (!src_node) {
            // simply do nothing if the library cannot be reloaded. This
            // is currently the case w/ search result libraries.
            return;
        };

        var src_uri = src_node.selectSingleNode('text()').nodeValue;
        src_uri = src_uri.strip(); // needs kupuhelpers.js
        this._loadXML(src_uri, this._libraryContentCallback, null, true, current);
    };

    this.removeSelection = function() {
        // Mark the drawer as having no selection
        if (!this.xmldata) return;

        if (!this.multiple) {
            var items = this.xmldata.selectNodes('//resource[@checked]');
            for (var i = 0; i < items.length; i++) {
                items[i].removeAttribute('checked');
            };
        };

        // turn off current selection, if any
        var oldselxpath = '//resource[@selected]';
        var oldselitems = this.xmldata.selectNodes(oldselxpath);
        for (var i = 0; i < oldselitems.length; i++) {
            oldselitems[i].removeAttribute("selected");
            var id = oldselitems[i].getAttribute('id');

            var item = document.getElementById(id);
            if (item) {
                var spans = item.getElementsByTagName('span');
                for (var j = 0; j < spans.length; j++) {
                    var p = spans[j].parentNode;
                    p.className = p.className.replace(/(\s+|^)selected-item/, '');
                }
            }
        }
        this.showupload = '';
    };

    this.selectUpload = function() {
        this.removeSelection();
        this.showupload = 'yes';
        this.updateResources();
    };
    /*** Selecting a resource ***/

    this.selectItem = function (item, event) {
        var id = item.id;
        var newselxpath = '/libraries/*[@selected]//resource[@id="' + id + '"]';
        var src = this.xmldata.selectSingleNode(newselxpath+'/src');
        if (src) {
            event = event || window.event;
            if (event) {
                var target = event.target || event.srcElement;
            }
            if (target.nodeName.toLowerCase()!='input') {
                this.selectCollection(item, 'resource');
                return;
            }
        }

        /* select an item in the item pane, show the item's metadata */

        // First turn off current selection, if any
        this.removeSelection();

        // Grab XML DOM node for clicked "resource" and mark it selected
        var newselitem = this.xmldata.selectSingleNode(newselxpath);
        newselitem.setAttribute("selected", "1");

        var check = true;
        if (this.multiple) {
            if (newselitem.getAttribute('checked')) {
                check = false;
                var sel = this.currentSelection;
                for (var i=0; i < sel.length; i++) {
                    if (sel[i]==id) {
                        sel.splice(i, 1);
                        break;
                    };
                };
            } else {
                this.currentSelection.push(id);
            };
        } else {
            this.currentSelection = [id];
        };
        if (check) {
            newselitem.setAttribute('checked','1');
        } else {
            newselitem.removeAttribute('checked');
        };
        this.updateDisplay(this.propertiespanelid);

        // Don't want to reload the resource panel xml as it scrolls to
        // the top.
        var span = item.getElementsByTagName('span');
        if (span.length > 0) {
            span = span[0];
            var p = span.parentNode;
            p.className += ' selected-item';
            var inp = p.getElementsByTagName('input');
            if (inp) inp[0].checked = check;
        }

        if (this.editor.getBrowserName() == 'IE') {
            var ppanel = document.getElementById(this.propertiespanelid);
            var height = ppanel.clientHeight;
            if (height > ppanel.scrollHeight) height = ppanel.scrollHeight;
            if (height < 260) height = 260;
            document.getElementById(this.resourcespanelid).style.height = height+'px';
        }
        return;
    };


    this.search = function() {
        /* search */
        this.removeSelection();
        var searchvalue = getFromSelector('kupu-searchbox-input').value;
        //XXX make search variable configurable
        var body = 'SearchableText=' + encodeURIComponent(searchvalue);

        // the search uri might contain query parameters in HTTP GET
        // style. We want to do a POST though, so find any possible
        // parameters, trim them from the URI and append them to the
        // POST body instead.
        var chunks = this.searchuri.split('?');
        var searchuri = chunks[0];
        if (chunks[1]) {
            body += "&" + chunks[1];
        };
        this._loadXML(searchuri, this._searchCallback, body);
    };

    this._searchCallback = function(dom) {
        var resultlib = dom.selectSingleNode("/library");

        var items = resultlib.selectNodes("items/*");
        if (!items.length) {
            alert("No results found.");
            return;
        };

        // we need to give the newly retrieved data a unique ID, we
        // just use the time.
        var date = new Date();
        var time = date.getTime();
        resultlib.setAttribute("id", time);

        // deselect the previous collection and mark the result
        // library as selected
        this.deselectActiveCollection();
        resultlib.setAttribute("selected", "1");

        // now hook the result library into our DOM
        if (this.editor.getBrowserName() == 'IE') {
            resultlib = resultlib.cloneNode(true);
        } else {
            resultlib = this.xmldata.importNode(resultlib, true);
        }
        var libraries = this.xmldata.selectSingleNode("/libraries");
        libraries.appendChild(resultlib);

        this.updateDisplay(this.drawerid);
        var newseldiv = getFromSelector(time);
        newseldiv.className = 'kupu-libsource-selected';
    };

    this.selectCurrent = function() {
        var src = this.selectedSrc;
        var body = 'src=' + encodeURIComponent(src);

        // the uri might contain query parameters in HTTP GET
        // style. We want to do a POST though, so find any possible
        // parameters, trim them from the URI and append them to the
        // POST body instead.
        var chunks = this.selecturi.split('?');
        var uri = chunks[0];
        if (chunks[1]) {
            body += "&" + chunks[1];
        };
        this._loadXML(uri, this._selectedCallback, body);
    };

    this._selectedCallback = function(dom) {
        var resultlib = dom.selectSingleNode("/library");
        var id = "kupu-current-selection";
        resultlib.setAttribute("id", id);
        var leafnodes = resultlib.selectNodes("//resource");
        this.currentSelection = [];
        for (var i = 0; i < leafnodes.length; i++) {
            this.currentSelection.push(leafnodes[i].getAttribute('id'));
        };
        // deselect the previous collection and mark the result
        // library as selected
        this.deselectActiveCollection();
        resultlib.setAttribute("selected", "1");

        // now hook the result library into our DOM
        if (this.editor.getBrowserName() == 'IE') {
            resultlib = resultlib.cloneNode(true);
        } else {
            resultlib = this.xmldata.importNode(resultlib, true);
        }
        var libraries = this.xmldata.selectSingleNode("/libraries");
        libraries.appendChild(resultlib);
        this.useCollection(resultlib);
        this.updateDisplay(this.librariespanelid);
        this.flagSelectedLib(id);
        this.updateDisplay(this.breadcrumbsid);
    };

    this.save = function() {
        /* save the element, should be implemented on subclasses */
        throw "Not yet implemented";
    };

    /*** Auxiliary methods ***/

    this._transformXml = function() {
        /* transform this.xmldata to HTML using this.shared.xsl and return it */
	var result = this.shared.xsltproc.transformToDocument(this.xmldata);
        return result;
    };

    this._loadXML = function(uri, callback, body, reload, extra) {
        function _sarissaCallback() {
        /* callback for Sarissa
            when the callback is called because the data's ready it
            will get the responseXML DOM and call user_callback
            with the DOM as the first argument and the uri loaded
            as the second

            note that this method should be called in the context of an
            xmlhttp object
        */
            if (xmlhttp.readyState == 4) {
                if (xmlhttp.status && xmlhttp.status != 200) {
                    var errmessage = 'Error '+xmlhttp.status+' loading '+(uri||'XML');
                    kupu_notbusy(ed, true);
                    alert(errmessage);
                    throw "Error loading XML";
                };
                var dom = xmlhttp.responseXML;
                if (!dom || !dom.documentElement) { /* IE bug! */
                    dom = Sarissa.getDomDocument();
                    dom.loadXML(xmlhttp.responseText);
                }
                callback.apply(self, [dom, uri, extra]);
                kupu_notbusy(ed);
            };
        };
        var self = this;
        var ed = this.editor;
        /* load the XML from a uri
           calls callback with one arg (the XML DOM) when done
           the (optional) body arg should contain the body for the request
         */
	var xmlhttp = new XMLHttpRequest();
        var method = body?'POST':'GET';
        // be sure that body is null and not an empty string or
        // something
        body=body?body:null;

        kupu_busy(ed);
        try {
            xmlhttp.open(method, uri, true);
            xmlhttp.onreadystatechange = _sarissaCallback;
            if (method == "POST") {
                // by default, we would send a 'text/xml' request, which
                // is a dirty lie; explicitly set the content type to what
                // a web server expects from a POST.
                xmlhttp.setRequestHeader('content-type', 'application/x-www-form-urlencoded');
            };
            if (reload) {
                xmlhttp.setRequestHeader("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT");
            }
            xmlhttp.send(body);
        } catch(e) {
            if (e && e.name && e.message) { // Microsoft
                e = e.name + ': ' + e.message;
            }
            kupu_notbusy(ed, true);
            alert(e);
        }
    };

};

LibraryDrawer.prototype = new DrawerWithAnchors;
LibraryDrawer.prototype.shared = {}; // Shared data

function ImageLibraryDrawer(tool, xsluri, libsuri, searchuri, baseelement, selecturi) {
    /* a specific LibraryDrawer for images */

    this.drawertitle = "Insert Image";
    this.drawertype = "image";

    if (tool) {
        this.init(tool, xsluri, libsuri, searchuri, baseelement, selecturi);
    }

    this.createContent = function() {
        function getSel(sel, p, t) {
            var nodes = p.getElementsByTagName(t);
            for (var i = 0; i < nodes.length; i++) {
                if (sel.containsNode(nodes[i])) {
                    return nodes[i];
                };
            };
        }
        var ed = this.editor;
        var sel = ed.getSelection();
        var currnode = ed.getSelectedNode();
        var currimg = ed.getNearestParentOfType(currnode, 'OBJECT') || ed.getNearestParentOfType(currnode, 'IMG') ||
                      getSel(sel, currnode, 'object') || getSel(sel, currnode, 'img');
        this.selectedSrc = currimg?(currimg.data||currimg.src||null):null;
        this.options = {};
        if (currimg) {
            ed.getSelection().selectNodeContents(currimg);
            var className = currimg.className;
            var align = /\bimage-(left|right|inline)\b/.exec(className);
            if (align && align.length > 1) {
                this.options['image-align'] = align[1];
            };
            this.options['image-caption'] = /\bcaptioned\b/.test(className);
            this.options['image-class'] = className.replace(/\b(image-(left|right|inline)|captioned)\b/g,'').strip();
        }
        ImageLibraryDrawer.prototype.createContent.call(this);
    };

    // upload, on submit/insert press
    this.uploadImage = function() {
        var form = document.getElementById('kupu_upload_form');
        if (!form || (form.node_prop_image && form.node_prop_image.value == '')) {
            return;
        }

        if (form.node_prop_title && form.node_prop_title.value == "") {
            alert("Please enter a title for the image you are uploading");
            return;
        };
        this.upload_title = form.node_prop_title ? form.node_prop_title.value : '';
        if (form.node_prop_desc) {
            form.node_prop_desc.value = form.node_prop_desc.value.replace(/^\xa0|\xa0$/g,'');
        }
        form.submit();
    };

    // called for example when no permission to upload for some reason
    this.cancelUpload = function(msg) {
        var s = this.xmldata.selectSingleNode('/libraries/*[@selected]');
        s.removeAttribute("selected");
        this.updateDisplay();
        if (msg != '') {
            alert(msg);
        };
    };

    // called by onLoad within document sent by server
    this.finishUpload = function(uri, media, width, height) {
        this.editor.resumeEditing();
        var sizeselector = document.getElementsByName('image-size-selector');
        if (sizeselector && sizeselector.length > 0) {
            sizeselector = sizeselector[0];
            var index = sizeselector.selectedIndex;
            if (sizeselector.length > 0 && index >= 0) {
                uri += sizeselector.options[index].value;
            }
        }
        var radios = document.getElementsByName('image-align');
        var imgclass = "";
        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked) {
                imgclass = radios[i].value;
            };
        };
        var caption = document.getElementsByName('image-caption');
        if (caption && caption.length>0 && caption[0].checked) {
            imgclass += " captioned";
        };
        var classnames = document.getElementById('kupu-image-class');
        if (classnames && classnames.selectedIndex >= 0) {
            imgclass += " "+classnames.options[classnames.selectedIndex].value;
        } else {
            imgclass += ' image-inline';
        }
        imgclass = imgclass.strip();

        if (media !== undefined && this.tool['create_' + media]) {
            this.tool['create_' + media](uri, this.upload_title, imgclass, width, height);
        } else {
            this.tool.createImage(uri, this.upload_title, imgclass);
        }

        this.shared.newimages = 1;
        this.drawertool.closeDrawer();
    };


    this.save = function() {
        this.editor.resumeEditing();
        /* create an image in the iframe according to collected data
           from the drawer */
        var selxpath = '//resource[@selected]';
        var selnode = this.xmldata.selectSingleNode(selxpath);

        // If no image resource is selected, check for upload
        if (!selnode) {
            var uploadbutton = this.xmldata.selectSingleNode("/libraries/*[@selected]//uploadbutton");
            if (uploadbutton) {
                this.uploadImage();
            };
            return;
        };

        var sizeselector = document.getElementsByName('image-size-selector');
        if (sizeselector && sizeselector.length > 0) {
            sizeselector = sizeselector[0];
            var uri = sizeselector.options[sizeselector.selectedIndex].value;
        } else {
            var uri = selnode.selectSingleNode('uri/text()').nodeValue;
        }
        uri = uri.strip();  // needs kupuhelpers.js
        var alt = getFromSelector('image-alt');
        alt = alt?alt.value:undefined;

        var radios = document.getElementsByName('image-align');
        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked) {
                var imgclass = radios[i].value;
            };
        };

        var caption = document.getElementsByName('image-caption');
        if (caption && caption.length>0 && caption[0].checked) {
            imgclass += " captioned";
        };
        var classnames = document.getElementById('kupu-image-class');
        if (classnames && classnames.selectedIndex >= 0) {
            imgclass += " "+classnames.options[classnames.selectedIndex].value;
        }
        var media = document.getElementById('kupu-media').value;
        var width = document.getElementById('kupu-width').value;
        var height = document.getElementById('kupu-height').value;
        if (this.tool['create_' + media]) {
            this.tool['create_' + media](uri, alt, imgclass, width, height);
        } else {
            this.tool.createImage(uri, alt, imgclass);
        }
        kupu.content_changed = true;
        this.drawertool.closeDrawer();
    };
};

ImageLibraryDrawer.prototype = new LibraryDrawer;
ImageLibraryDrawer.prototype.shared = {}; // Shared data

function LinkLibraryDrawer(tool, xsluri, libsuri, searchuri, baseelement, selecturi) {
    /* a specific LibraryDrawer for links */

    this.drawertitle = "Insert Link";
    this.drawertype = "link";
    this.showanchors = "yes";

    if (tool) {
        this.init(tool, xsluri, libsuri, searchuri, baseelement, selecturi);
    }

    this.createContent = function() {
        var currnode = this.editor.getSelectedNode();
        var curranchor = this.editor.getNearestParentOfType(currnode, 'A');
        this.selectedSrc = curranchor?curranchor.href:null;
        this.options = {};
        if (curranchor) {
            this.options.link_name = curranchor.name || '';
            this.options.link_target = curranchor.target || '';
        }
        LinkLibraryDrawer.prototype.createContent.call(this);
    };

    this.save = function() {
        this.editor.resumeEditing();
        /* create a link in the iframe according to collected data
           from the drawer */
        var selxpath = '//resource[@selected]';
        var selnode = this.xmldata.selectSingleNode(selxpath);
        if (!selnode) {
            return;
        };

        var uri = selnode.selectSingleNode('uri/text()').nodeValue;
        uri = uri.strip() + this.getFragment();
        var title = '';
        title = selnode.selectSingleNode('title/text()').nodeValue;
        title = title.strip();

        // XXX requiring the user to know what link type to enter is a
        // little too much I think. (philiKON)
        var name = getFromSelector('link_name').value;
        var node = getFromSelector('link_target');
        var target = node && node.value;

        this.tool.createLink(uri, null, name, target, title, 'internal-link');
        this.drawertool.closeDrawer();
    };
};

LinkLibraryDrawer.prototype = new LibraryDrawer;
LinkLibraryDrawer.prototype.shared = {}; // Shared data

function AnchorDrawer(elementid, tool) {
    Drawer.call(this, elementid, tool);

    this.initialize = function(editor, tool) {
        Drawer.prototype.initialize.apply(this, [editor, tool]);
        this.panel = getBaseTagClass(this.element, 'div', 'kupu-panels');
        this.style1 = getFromSelector('kupu-bm-sel1');
        this.style2 = getFromSelector('kupu-bm-sel2');
        this.ostyle = getFromSelector('kupu-bm-outcls');
        this.nstyle = getFromSelector('kupu-bm-number');
        var tabs = getBaseTagClass(this.element, 'ul', 'kupu-tabs').getElementsByTagName('a');
        this.paralist = getBaseTagClass(this.element, 'div', 'kupu-bm-paras');
        this.checkall = getFromSelector('kupu-bm-checkall');

        for (var i = 0; i < tabs.length; i++) {
            addEventHandler(tabs[i], 'click', this.switchMode, this);
        }
        addEventHandler(this.checkall, 'click', this.checkAll, this);
        addEventHandler(this.style1, 'change', this.fillList, this);
        addEventHandler(this.style2, 'change', this.fillList, this);
        this.tool.fillStyleSelect(this.style1);
        this.tool.fillStyleSelect(this.style2);
        this.tool.fillStyleSelect(this.ostyle);
    };
    this.getMode = function() { /* tab 0, 1, or 2 */
        if (/kupu-ins-bm/.test(this.panel.className)) return 0;
        if (/kupu-anchor/.test(this.panel.className)) return 1;
        return 2;
    };

    this.checkAll = function() {
        var nodes = this.paralist.getElementsByTagName('input');
        var state = this.checkall.checked;
        for (var i = 0; i < nodes.length; i++) {
            var n = nodes[i];
            if (n.type=="checkbox" && !n.disabled) {
                nodes[i].checked = state;
            };
        };
    };

    this.fillList = function() {
        var el = newElement;
        while (this.paralist.firstChild) {
            this.paralist.removeChild(this.paralist.firstChild);
        }

        this.styleNames = ['', ''];

        var mode = this.getMode();
        var s = ['', ''];
        for (var idx=0; idx < (mode==2?2:1); idx++) {
            var sel = this['style'+(idx+1)];
            var i = sel.selectedIndex;
            if (i >= 0) {
                s[idx] = sel.options[i].value;
                this.styleNames[idx] = sel.options[i].firstChild.data;
            }
        }

        if (mode==1) {
            var inuse = this.tool.getAnchorsInUse();
        }
        var paras = (this.nodelist = this.tool.grubParas(s[0], s[1]));
        for (var i = 0; i < paras.length; i++) {
            var node = paras[i][0];
            var text = Sarissa.getText(node, true).strip().truncate(60);
            if (!text) continue;
            var content = document.createTextNode(text);
            var anchor = '';
            if (mode==1) {
                anchor = this.tool.getAnchor(node, true);
                if (anchor) {
                    anchor = '#'+anchor;
                }
            }
            var checked;
            switch (mode) {
                case 0: checked = i==0; break;
                case 1: checked = !!anchor; break;
                case 2: checked = this.checkall.checked; break;
            }
            var control = el('input', {
                'type': (mode==0)?"radio":"checkbox",
                checked: checked, title:'hello',
                name: "kupu-bm-paralist"});
            if (anchor && inuse && inuse[decodeURIComponent(anchor)]) {
                control.disabled = true;
            }

            var inner = [control, el('span', [content])];
            if (anchor) {
                inner.push(el('a', {href:anchor, className:'kupu-anchor-link',onclick:'return false;',
                    title:_('Right click to copy link')}, [anchor]));
            };
            var div = el('div', {className: "kupu-bm-level" + paras[i][1] },
                [el('label', inner)]);

            this.paralist.appendChild(div);
        };
    };
    this.createContent = function() {
        this.fillList();
        this.element.style.display = 'block';
        this.focusElement();
    };
    this.save = function() {
        var mode = this.getMode();
        var selected = this.paralist.getElementsByTagName('input');
        var ed = this.editor;

        ed.resumeEditing();

        if (mode==2) {
            var toc = ed.newElement('ul');
        };
        var lvl1=0, lvl2=0;
        for (var i = 0; i < selected.length; i++) {
            var nodeinfo = this.nodelist[i];
            var node = nodeinfo[0];
            var level = nodeinfo[1];
            if (selected[i].checked) {
                var a = this.tool.getAnchor(node);
                var caption = Sarissa.getText(node, true).strip().truncate(140);
                switch (mode) {
                case 0:
                    this.tool.createLink('#'+a, null, null, null, caption);
                    break;
                case 1:
                    break;
                case 2:
                    /* Insert TOC entry here */
                    var number;
                    if (level==0) {
                        number = ++lvl1;
                        lvl2 = 0;
                    } else {
                        number = lvl1 + '.' + (++lvl2);
                    };
                    var li = ed.newElement('li', {'className': 'level'+level},
                        [ed.newElement('a', {'href': '#'+a},
                        [ed.newText((this.nstyle.checked?number + ' ':'') + caption)])]);

                    if (level==0) {
                        toc.appendChild(li);
                    } else {
                        if (!toc.lastChild || toc.lastChild.nodeName.toLowerCase() != 'ul') {
                            toc.appendChild(ed.newElement('ul'));
                        }
                        toc.lastChild.appendChild(li);
                    };
                    break;
                };
            } else {
                if (mode==1) {
                    this.tool.removeAnchor(node);
                }
            };
        };
        if (mode==2 && toc.firstChild) {
            var o = this.ostyle.selectedIndex;
            if (o > 0) {
                var ostyle = this.ostyle.options[o].value.split('|');
                if (ostyle[0]=='ul') {
                    toc.className=ostyle[1];
                } else {
                    toc = ed.newElement(ostyle[0], {'className': ostyle[1]}, [toc]);
                };
            }
            var node = ed.getSelection().parentElement();
            if (node.nodeName.toLowerCase() == 'body') {
                node.insertBefore(toc, node.firstChild);
            } else {
                while (node.parentNode.nodeName.toLowerCase() != 'body') {
                    node = node.parentNode;
                }
                node.parentNode.insertBefore(toc, node);
            }
        }
        this.nodelist = null;
        this.drawertool.closeDrawer();
    };
    this.hide = function() {
        this.nodelist = null;
        Drawer.prototype.hide.apply(this, []);
    };
};

AnchorDrawer.prototype = new Drawer;

/* Function to suppress enter key in drawers */
function HandleDrawerEnter(event, clickid) {
    event = event || window.event;
    var key = event.which || event.keyCode;
    var button;
    if (key==13) {
        if (clickid) {
            button = document.getElementById(clickid);
            if (button && !button.disabled) {
                button.click();
            }
        }
        event.cancelBubble = true;
        if (event.stopPropagation) {
            event.stopPropagation();
        }
        event.returnValue = false;
        return false;
    }
    return true;
}


