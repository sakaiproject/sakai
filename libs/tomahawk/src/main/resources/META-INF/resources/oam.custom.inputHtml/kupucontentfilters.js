/*****************************************************************************
 *
 * Copyright (c) 2003-2004 Kupu Contributors. All rights reserved.
 *
 * This software is distributed under the terms of the Kupu
 * License. See LICENSE.txt for license text. For a list of Kupu
 * Contributors see CREDITS.txt.
 *
 *****************************************************************************/

// $Id: kupucontentfilters.js 928511 2010-03-28 22:53:14Z lu4242 $


//----------------------------------------------------------------------------
// 
// ContentFilters
//
//  These are (or currently 'this is') filters for HTML cleanup and 
//  conversion. Kupu filters should be classes that should get registered to
//  the editor using the registerFilter method with 2 methods: 'initialize'
//  and 'filter'. The first will be called with the editor as its only
//  argument and the latter with a reference to the ownerdoc (always use 
//  that to create new nodes and such) and the root node of the HTML DOM as 
//  its arguments.
//
//----------------------------------------------------------------------------

function NonXHTMLTagFilter() {
    /* filter out non-XHTML tags*/
    
    // A mapping from element name to whether it should be left out of the 
    // document entirely. If you want an element to reappear in the resulting 
    // document *including* it's contents, add it to the mapping with a 1 value.
    // If you want an element not to appear but want to leave it's contents in 
    // tact, add it to the mapping with a 0 value. If you want an element and
    // it's contents to be removed from the document, don't add it.
    if (arguments.length) {
        // allow an optional filterdata argument
        this.filterdata = arguments[0];
    } else {
        // provide a default filterdata dict
        this.filterdata = {'html': 1,
                            'body': 1,
                            'head': 1,
                            'title': 1,
                            
                            'a': 1,
                            'abbr': 1,
                            'acronym': 1,
                            'address': 1,
                            'b': 1,
                            'base': 1,
                            'big': 1,
                            'blockquote': 1,
                            'br': 1,
                            'caption': 1,
                            'cite': 1,
                            'code': 1,
                            'col': 1,
                            'colgroup': 1,
                            'dd': 1,
                            'dfn': 1,
                            'div': 1,
                            'dl': 1,
                            'dt': 1,
                            'em': 1,
                            'h1': 1,
                            'h2': 1,
                            'h3': 1,
                            'h4': 1,
                            'h5': 1,
                            'h6': 1,
                            'i': 1,
                            'img': 1,
                            'kbd': 1,
                            'li': 1,
                            'link': 1,
                            'meta': 1,
                            'ol': 1,
                            'p': 1,
                            'pre': 1,
                            'q': 1,
                            'samp': 1,
                            'script': 1,
                            'small': 1,
                            'span': 1,
                            'strong': 1,
                            'style': 1,
                            'sub': 1,
                            'sup': 1,
                            'table': 1,
                            'tbody': 1,
                            'td': 1,
                            'tfoot': 1,
                            'th': 1,
                            'thead': 1,
                            'tr': 1,
                            'tt': 1,
                            'ul': 1,
                            'u': 1,
                            'var': 1,

                            // even though they're deprecated we should leave
                            // font tags as they are, since Kupu sometimes
                            // produces them itself.
                            'font': 1,
                            'center': 0
                            };
    };
                        
    this.initialize = function(editor) {
        /* init */
        this.editor = editor;
    };

    this.filter = function(ownerdoc, htmlnode) {
        return this._filterHelper(ownerdoc, htmlnode);
    };

    this._filterHelper = function(ownerdoc, node) {
        /* filter unwanted elements */
        if (node.nodeType == 3) {
            return ownerdoc.createTextNode(node.nodeValue);
        } else if (node.nodeType == 4) {
            return ownerdoc.createCDATASection(node.nodeValue);
        };
        // create a new node to place the result into
        // XXX this can be severely optimized by doing stuff inline rather 
        // than on creating new elements all the time!
        var newnode = ownerdoc.createElement(node.nodeName);
        // copy the attributes
        for (var i=0; i < node.attributes.length; i++) {
            var attr = node.attributes[i];
            newnode.setAttribute(attr.nodeName, attr.nodeValue);
        };
        for (var i=0; i < node.childNodes.length; i++) {
            var child = node.childNodes[i];
            var nodeType = child.nodeType;
            var nodeName = child.nodeName.toLowerCase();
            if (nodeType == 3 || nodeType == 4) {
                newnode.appendChild(this._filterHelper(ownerdoc, child));
            };
            if (nodeName in this.filterdata && this.filterdata[nodeName]) {
                newnode.appendChild(this._filterHelper(ownerdoc, child));
            } else if (nodeName in this.filterdata) {
                for (var j=0; j < child.childNodes.length; j++) {
                    newnode.appendChild(this._filterHelper(ownerdoc, 
                        child.childNodes[j]));
                };
            };
        };
        return newnode;
    };
};

//-----------------------------------------------------------------------------
//
// XHTML validation support
//
// This class is the XHTML 1.0 transitional DTD expressed as Javascript
// data structures.
//
function XhtmlValidation(editor) {
    // Support functions
    function asList(s) {
        if (typeof(s)==typeof('') || !s.length) s = [s];
        return s;
    }

    this.Set = function(ary) {
        if (typeof(ary)==typeof('')) ary = [ary];
        if (ary instanceof Array) {
            for (var i = 0; i < ary.length; i++) {
                this[ary[i]] = 1;
            }
        }
        else {
            for (var v in ary) { // already a set?
                this[v] = 1;
            }
        }
    };
    
    this._exclude = function(array, exceptions) {
        var ex;
        if (exceptions.split) {
            ex = exceptions.split("|");
        } else {
            ex = exceptions;
        }
        var exclude = new this.Set(ex);
        var res = [];
        for (var k=0; k < array.length;k++) {
            if (!exclude[array[k]]) res.push(array[k]);
        }
        return res;
    };
    this.setAttrFilter = function(attributes, filter) {
        for (var j = 0; j < attributes.length; j++) {
            var attr = attributes[j];
            this.attrFilters[attr] = filter || this._defaultCopyAttribute;
        }
    };

    this.setTagAttributes = function(tags, attributes) {
        for (var j = 0; j < tags.length; j++) {
            this.tagAttributes[tags[j]] = attributes;
        }
    };

    // define some new attributes for existing tags
    this.includeTagAttributes = function(tags, attributes) {
        for (var j = 0; j < tags.length; j++) {
            var tag = tags[j];
            this.tagAttributes[tag] = this.tagAttributes[tag].concat(attributes);
        }
    };

    this.excludeTagAttributes = function(tags, attributes) {
        var bad = new this.Set(attributes);
        var tagset = new this.Set(tags);
        for (var tag in tagset) {
            var val = this.tagAttributes[tag];
            if (val) {
                for (var i = val.length; i >= 0; i--) {
                    if (bad[val[i]]) {
                        val = val.concat(); // Copy
                        val.splice(i,1);
                    }
                }
            }
            this.tagAttributes[tag] = val;
            // have to store this to allow filtering for nodes on which
            // '*' is set as allowed, this allows using '*' for the attributes
            // but also filtering some out
            this.badTagAttributes[tag] = attributes;
        }
    };

    this.excludeTags = function(badtags) {
        if (typeof(badtags)==typeof('')) badtags = [badtags];
        for (var i = 0; i < badtags.length; i++) {
            delete this.tagAttributes[badtags[i]];
        }
    };

    this.excludeAttributes = function(badattrs) {
        this.excludeTagAttributes(this.tagAttributes, badattrs);
        for (var i = 0; i < badattrs.length; i++) {
            delete this.attrFilters[badattrs[i]];
        }
    };
    var replaceNodes = { 'b': 'strong', 'i': 'em' };
    if (editor.getBrowserName()=="IE") {
        this._getTagName = function(htmlnode) {
            var nodename = htmlnode.nodeName.toLowerCase();
            if (htmlnode.scopeName && htmlnode.scopeName != "HTML") {
                nodename = htmlnode.scopeName+':'+nodename;
            }
            return replaceNodes[nodename]||nodename;
        };
    } else {
        this._getTagName = function(htmlnode) {
            var nodename = htmlnode.nodeName.toLowerCase();
            return replaceNodes[nodename]||nodename;
        };
    };

    // Supporting declarations
    this.elements = new function(validation) {
        // A list of all attributes
        this.attributes = [
            'abbr','accept','accept-charset','accesskey','action','align','alink',
            'alt','archive','axis','background','bgcolor','border','cellpadding',
            'cellspacing','char','charoff','charset','checked','cite','class',
            'classid','clear','code','codebase','codetype','color','cols','colspan',
            'compact','content','coords','data','datetime','declare','defer','dir',
            'disabled','enctype','face','for','frame','frameborder','headers',
            'height','href','hreflang','hspace','http-equiv','id','ismap','label',
            'lang','language','link','longdesc','marginheight','marginwidth',
            'maxlength','media','method','multiple','name','nohref','noshade','nowrap',
            'object','onblur','onchange','onclick','ondblclick','onfocus','onkeydown',
            'onkeypress','onkeyup','onload','onmousedown','onmousemove','onmouseout',
            'onmouseover','onmouseup','onreset','onselect','onsubmit','onunload',
            'profile','prompt','readonly','rel','rev','rows','rowspan','rules',
            'scheme','scope','scrolling','selected','shape','size','span','src',
            'standby','start','style','summary','tabindex','target','text','title',
            'type','usemap','valign','value','valuetype','vlink','vspace','width',
            'xml:lang','xml:space','xmlns'];

        // Core attributes
        this.coreattrs = ['id', 'title', 'style', 'class'];
        this.i18n = ['lang', 'dir', 'xml:lang'];
        // All event attributes are here but commented out so we don't
        // have to remove them later.
        this.events = []; // 'onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup'.split('|');
        this.focusevents = []; // ['onfocus','onblur']
        this.loadevents = []; // ['onload', 'onunload']
        this.formevents = []; // ['onsubmit','onreset']
        this.inputevents = [] ; // ['onselect', 'onchange']
        this.focus = ['accesskey', 'tabindex'].concat(this.focusevents);
        this.attrs = [].concat(this.coreattrs, this.i18n, this.events);

        // entities
        this.special_extra = ['object','applet','img','map','iframe', 'embed'];
        this.special_basic=['br','span','bdo'];
        this.special = [].concat(this.special_basic, this.special_extra);
        this.fontstyle_extra = ['big','small','font','basefont'];
        this.fontstyle_basic = ['tt','i','b','u','s','strike'];
        this.fontstyle = [].concat(this.fontstyle_basic, this.fontstyle_extra);
        this.phrase_extra = ['sub','sup'];
        this.phrase_basic = ['em','strong','dfn','code','q',
                             'samp','kbd','var','cite','abbr','acronym'];
        this.phrase = [].concat(this.phrase_basic, this.phrase_extra);
        this.inline_forms = ['input','select','textarea','label','button'];
        this.misc_inline = ['ins','del', 'script'];
        this.misc = ['noscript'].concat(this.misc_inline);
        this.inline = ['a'].concat(this.special, this.fontstyle, this.phrase, this.inline_forms);

        this.Inline = ['#text', '#comment'].concat(this.inline, this.misc_inline);

        this.heading = ['h1','h2','h3','h4','h5','h6'];
        this.lists = ['ul','ol','dl','menu','dir'];
        this.blocktext = ['pre','hr','blockquote','address','center','noframes'];
        this.block = ['p','div','isindex','fieldset','table'].concat(
                     this.heading, this.lists, this.blocktext);

        this.Flow = ['#text','form'].concat(this.block, this.inline, this.misc);
    }(this);

    this._commonsetting = function(self, names, value) {
        for (var n = 0; n < names.length; n++) {
            self[names[n]] = value;
        }
    };
    
    // The tagAttributes class returns all valid attributes for a tag,
    // e.g. a = this.tagAttributes.head
    // a.head -> [ 'lang', 'xml:lang', 'dir', 'id', 'profile' ]
    this.tagAttributes = new function(el, validation) {
        this.title = el.i18n.concat('id');
        this.html = this.title.concat('xmlns');
        this.head = this.title.concat('profile');
        this.base = ['id', 'href', 'target'];
        this.meta =  this.title.concat('http-equiv','name','content', 'scheme');
        this.link = el.attrs.concat('charset','href','hreflang','type', 'rel','rev','media','target');
        this.style = this.title.concat('type','media','title', 'xml:space');
        this.script = ['id','charset','type','language','src','defer', 'xml:space'];
        this.iframe = [
                      'longdesc','name','src','frameborder','marginwidth',
                      'marginheight','scrolling','align','height','width'].concat(el.coreattrs);
        this.body = ['background','bgcolor','text','link','vlink','alink'].concat(el.attrs, el.loadevents);
        validation._commonsetting(this,
                                  ['p','div'].concat(el.heading),
                                  ['align'].concat(el.attrs));
        this.dl = this.dir = this.menu = el.attrs.concat('compact');
        this.ul = this.menu.concat('type');
        this.ol = this.ul.concat('start');
        this.li = el.attrs.concat('type','value');
        this.hr = el.attrs.concat('align','noshade','size','width');
        this.pre = el.attrs.concat('width','xml:space');
        this.blockquote = this.q = el.attrs.concat('cite');
        this.ins = this.del = this.blockquote.concat('datetime');
        this.a = el.attrs.concat(el.focus,'charset','type','name','href','hreflang','rel','rev','shape','coords','target');
        this.bdo = el.coreattrs.concat(el.events, 'lang','xml:lang','dir');
        this.br = el.coreattrs.concat('clear');
        validation._commonsetting(this,
                                  ['noscript','noframes','dt', 'dd', 'address','center','span','em', 'strong', 'dfn','code',
                                  'samp','kbd','var','cite','abbr','acronym','sub','sup','tt',
                                  'i','b','big','small','u','s','strike', 'fieldset'],
                                  el.attrs);

        this.basefont = ['id','size','color','face'];
        this.font = el.coreattrs.concat(el.i18n, 'size','color','face');
        this.object = el.attrs.concat('declare','classid','codebase','data','type','codetype','archive','standby','height','width','usemap','name','tabindex','align','border','hspace','vspace');
        this.embed=['*'];
        this.param = ['id','name','value','valuetype','type'];
        this.applet = el.coreattrs.concat('codebase','archive','code','object','alt','name','width','height','align','hspace','vspace');
        this.img = el.attrs.concat('src','alt','name','longdesc','height','width','usemap','ismap','align','border','hspace','vspace');
        this.map = this.title.concat('title','name', 'style', 'class', el.events);
        this.area = el.attrs.concat('shape','coords','href','nohref','alt','target', el.focus);
        this.form = el.attrs.concat('action','method','name','enctype',el.formevents,'accept','accept-charset','target');
        this.label = el.attrs.concat('for','accesskey', el.focusevents);
        this.input = el.attrs.concat('type','name','value','checked','disabled','readonly','size','maxlength','src','alt','usemap',el.input,'accept','align', el.focus);
        this.select = el.attrs.concat('name','size','multiple','disabled','tabindex', el.focusevents,el.input);
        this.optgroup = el.attrs.concat('disabled','label');
        this.option = el.attrs.concat('selected','disabled','label','value');
        this.textarea = el.attrs.concat('name','rows','cols','disabled','readonly', el.inputevents, el.focus);
        this.legend = el.attrs.concat('accesskey','align');
        this.button = el.attrs.concat('name','value','type','disabled',el.focus);
        this.isindex = el.coreattrs.concat('prompt', el.i18n);
        this.table = el.attrs.concat('summary','width','border','frame','rules','cellspacing','cellpadding','align','bgcolor');
        this.caption = el.attrs.concat('align');
        this.col = this.colgroup = el.attrs.concat('span','width','align','char','charoff','valign');
        this.thead =  el.attrs.concat('align','char','charoff','valign');
        this.tfoot = this.tbody = this.thead;
        this.tr = this.thead.concat('bgcolor');
        this.td = this.th = this.tr.concat('abbr','axis','headers','scope','rowspan','colspan','nowrap','width','height');
    }(this.elements, this);

    this.badTagAttributes = new this.Set({});

    // Nasty tags should be initialised from Plone's HTML control panel
    // but we have a few tags we know for sure aren't going to work
    // so we can put them in whatever.
    this.nastyTags = new this.Set({'script':1, 'style':1, 'meta':1, 'title':1}); 

    // State array. For each tag identifies what it can contain.
    // I'm not attempting to check the order or number of contained
    // tags (yet).
    this.States = new function(el, validation) {

        var here = this;
        function setStates(tags, value) {
            var valset = new validation.Set(value);

            for (var i = 0; i < tags.length; i++) {
                here[tags[i]] = valset;
            }
        }
        
        setStates(['html'], ['head','body']);
        setStates(['head'], ['title','base','script','style', 'meta','link','object','isindex']);
        setStates([
            'base', 'meta', 'link', 'hr', 'param', 'img', 'area', 'input',
            'br', 'basefont', 'isindex', 'col'], []);

        setStates(['title','style','script','option','textarea'], ['#text']);
        setStates([ 'noscript', 'iframe', 'noframes', 'body', 'div',
            'li', 'dd', 'blockquote', 'center', 'ins', 'del', 'td', 'th'], el.Flow);

        setStates(el.heading, el.Inline);
        setStates([ 'p', 'dt', 'address', 'span', 'bdo', 'caption',
            'em', 'strong', 'dfn','code','samp','kbd','var',
            'cite','abbr','acronym','q','sub','sup','tt','i',
            'b','big','small','u','s','strike','font','label',
            'legend'], el.Inline);

        setStates(['ul', 'ol', 'menu', 'dir', 'ul'], ['li']);
        setStates(['dl'], ['dt','dd']);
        setStates(['pre'], validation._exclude(el.Inline, "img|object|embed|applet|big|small|sub|sup|font|basefont"));
        setStates(['a'], validation._exclude(el.Inline, "a"));
        setStates(['applet', 'object','embed'], ['#text', 'param','form'].concat(el.block, el.inline, el.misc));
        setStates(['map'], ['form', 'area'].concat(el.block, el.misc));
        setStates(['form'], validation._exclude(el.Flow, ['form']));
        setStates(['select'], ['optgroup','option']);
        setStates(['optgroup'], ['option']);
        setStates(['fieldset'], ['#text','legend','form'].concat(el.block,el.inline,el.misc));
        setStates(['button'], validation._exclude(el.Flow, ['a','form','iframe'].concat(el.inline_forms)));
        setStates(['table'], ['caption','col','colgroup','thead','tfoot','tbody','tr']);
        setStates(['thead', 'tfoot', 'tbody'], ['tr']);
        setStates(['colgroup'], ['col']);
        setStates(['tr'], ['th','td']);
    }(this.elements, this);

    // Permitted elements for style.
    this.styleWhitelist = new this.Set(['text-align', 'list-style-type', 'float']);
    this.classBlacklist = new this.Set(['MsoNormal', 'MsoTitle', 'MsoHeader', 'MsoFootnoteText',
        'Bullet1', 'Bullet2', 'Apple-span-style']);

    this.classFilter = function(value) {
        var classes = value.split(' ');
        var filtered = [];
        for (var i = 0; i < classes.length; i++) {
            var c = classes[i];
            if (c && !this.classBlacklist[c]) {
                filtered.push(c);
            }
        }
        return filtered.join(' ');
    };
    this._defaultCopyAttribute = function(name, htmlnode, xhtmlnode) {
        var val = htmlnode.getAttribute(name);
        if (val) xhtmlnode.setAttribute(name, val);
        return !!val;
    };
    // Set up filters for attributes.
    // Filters may return false if nothing was copied, true or
    // undefined if an attribute was copied.
    var filter = this;
    this.attrFilters = new function(validation, editor) {
        var attrs = validation.elements.attributes;
        for (var i=0; i < attrs.length; i++) {
            this[attrs[i]] = validation._defaultCopyAttribute;
        }
        this['class'] = function(name, htmlnode, xhtmlnode) {
            var val = htmlnode.getAttribute('class');
            if (val) val = validation.classFilter(val);
            if (val) xhtmlnode.setAttribute('class', val);
            return !!val;
        };
        // allow a * wildcard to make all attributes valid in the filter
        // note that this is pretty slow on IE
        this['*'] = function(name, htmlnode, xhtmlnode) {
            var res = false;
            var nodeName = filter._getTagName(htmlnode);
            var bad = filter.badTagAttributes[nodeName];
            for (var i=0; i < htmlnode.attributes.length; i++) {
                var attr = htmlnode.attributes[i];
                var name = attr.name;
                if (bad && bad.contains(name)) {
                    continue;
                };
                if (attr.specified) {
                    xhtmlnode.setAttribute(name, attr.value);
                    res = true;
                };
            };
            return res;
        };
        if (editor.getBrowserName()=="IE") {
            this['class'] = function(name, htmlnode, xhtmlnode) {
                var val = htmlnode.className;
                if (val) {
                    val = validation.classFilter(val); 
                    if (val) xhtmlnode.setAttribute('class', val);
                } else {
                    val = htmlnode.getAttribute("class");
                    if (val) val = validation.classFilter(val);                        
                    if (val) xhtmlnode.setAttribute('class', val);
                }
                return !!val;
            };
            this['http-equiv'] = function(name, htmlnode, xhtmlnode) {
                var val = htmlnode.httpEquiv;
                if (val) xhtmlnode.setAttribute('http-equiv', val);
                return !!val;
            };
            this['xml:lang'] = this['xml:space'] = function(name, htmlnode, xhtmlnode) {
                try {
                    var val = htmlnode.getAttribute(name);
                    if (val) xhtmlnode.setAttribute(name, val);
                    return !!val;
                } catch(e) {
                }
            };
        }
        this.alt = function(name, htmlnode, xhtmlnode) {
            var val = htmlnode.getAttribute(name);
            var ok = val || xhtmlnode.tagName=='img';
            if (ok) xhtmlnode.setAttribute(name, val);
            return ok;
        };
        this.rowspan = this.colspan = function(name, htmlnode, xhtmlnode) {
            var val = htmlnode.getAttribute(name);
            var ok = val && val != '1';
            if (ok) xhtmlnode.setAttribute(name, val);
            return ok;
        };
        this.style = function(name, htmlnode, xhtmlnode) {
            var val = htmlnode.style.cssText;
            if (val) {
                var styles = val.split(/; */);
                for (var i = styles.length; i >= 0; i--) if (styles[i]) {
                    var parts = /^([^:]+): *(.*)$/.exec(styles[i]);
                    var name = parts[1].toLowerCase();
                    // myFaces : Remove this right now as the white list is really too small
                    //if (validation.styleWhitelist[name]) {
                        styles[i] = name+': '+parts[2];
                    //} else {
                    //    styles.splice(i,1); // delete
                    //}
                }
                if (styles[styles.length-1]) styles.push('');
                val = styles.join('; ').strip();
            };
            if (val) xhtmlnode.setAttribute('style', val);
            return !!val;
        };
    }(this, editor);

    // Exclude unwanted tags.
    this.excludeTags(['center', 'meta', 'title']);

    if (editor.config && editor.config.htmlfilter) {
        this.filterStructure = editor.config.htmlfilter.filterstructure;
        
        var exclude = editor.config.htmlfilter;
        if (exclude.a) {
            this.excludeAttributes(exclude.a);
        }
        if (exclude.t) {
            this.excludeTags(exclude.t);
        }
        if (exclude.c) {
            var c = asList(exclude.c);
            for (var i = 0; i < c.length; i++) {
                this.excludeTagAttributes(c[i].t, c[i].a);
            }
        }
        if (exclude.xstyle) {
            var s = asList(exclude.xstyle);
            for (var i = 0; i < s.length; i++) {
                this.styleWhitelist[s[i]] = 1;
            }
        }
        if (exclude['class']) {
            var c = asList(exclude['class']);
            for (var i = 0; i < c.length; i++) {
                this.classBlacklist[c[i]] = 1;
            }
        }
    };

    // Copy all valid attributes from htmlnode to xhtmlnode.
    // Returns true if at least one attribute was copied.
    this._copyAttributes = function(htmlnode, xhtmlnode, valid) {
        var name;
        var res = false;
        if (valid.contains('*')) {
            // allow all attributes on this tag
            res = this.attrFilters['*'](name, htmlnode, xhtmlnode);
            if (res===undefined) res = true;
        } else {
            for (var i = 0; i < valid.length; i++) {
                name = valid[i];
                var filter = this.attrFilters[name];
                if (filter) {
                    var f = filter(name, htmlnode, xhtmlnode);
                    res|=(f||f===undefined);
                }
            };
        }
        return res;
    };
    this._xmlCopyAttr = function(srcnode, target) {
        var valid = this.tagAttributes[srcnode.nodeName];
        for (var i = 0; i < valid.length; i++) {
            var val = srcnode.getAttribute(valid[i]);
            if (val) {
                target.setAttribute(valid[i], val);
            }
        };
    };

    this._convertToSarissaNode = function(ownerdoc, htmlnode) {
        var root = this._convertNodes(ownerdoc, htmlnode, null, new this.Set(['html']));
        this._cleanupBr(ownerdoc, root);
        this._cleanupParas(ownerdoc, root);
        return root;
    };

    // Clean up a paragraph. Any direct child which is not allowed in
    // the paragraph is moved to the parent. This may involved
    // splitting the paragraph, or if it is at the beginning or end it
    // may simply mean moving it out of the paragraph.
    this._cleanupPara = function(ownerdoc, para) {
        var permitted = this.States.p;
        var nodes = [[]];
        var idx = 0;
        for (var child = para.firstChild; child; child = child.nextSibling) {
            var nn = child.nodeName.toLowerCase();
            if (permitted[nn] && (nn != 'img' || !(/\bcaptioned\b/i.test(child.getAttribute('class'))))) {
                nodes[idx].push(child);
            } else {
                if (nodes[idx].length) {
                    nodes.push(child);
                } else {
                    nodes[idx] = child;
                }
                nodes.push([]);
                idx = nodes.length-1;
            }
        }
        if (!nodes[idx].length) {
            nodes.splice(idx,1);
        };
        if (nodes.length > 0 && nodes[0] instanceof Array && !nodes[0].length) {
            nodes.splice(0,1);
        }
        if (nodes.length==0 || (nodes.length==1 && nodes[0] instanceof Array)) {
            return; /* No change */
        }
        /* Need to cleanup this paragraph */
        var parentnode = para.parentNode;
        for (var idx = 0; idx < nodes.length; idx++) {
            var n = nodes[idx];
            if (n instanceof Array) {
                var newp = ownerdoc.createElement('p');
                this._xmlCopyAttr(para, newp);
                for (var ln = n.length-1; ln >= 0; ln--) {
                    var nn = n[ln].nodeName.toLowerCase();
                    if (nn=='br' || (nn=='#text' && (/^\s*$/.test(n[ln].nodeValue)))) {
                        n.splice(ln,1);
                    } else { break; }
                }
                if (n.length==0) {
                    continue;
                }
                for (var j = 0; j < n.length; j++) {
                    newp.appendChild(n[j]);
                }
                n = newp;
            }
            parentnode.insertBefore(n,para);
        }
        parentnode.removeChild(para);
    };

    this._cleanupParas = function(ownerdoc, root) {
        var paras = root.getElementsByTagName('p');
        for (var i = paras.length-1; i >= 0; i--) {
            this._cleanupPara(ownerdoc, paras[i]);
        }
    };
    /* Cleanup br tags: br at top level is replaced by a paragraph,
     * br at end of p|div is dropped.
     */
    this._cleanupBr = function(ownerdoc, root) {
        var breaks = root.getElementsByTagName('br');
        // Iterate backwards: removeChild removes node from breaks.
        for (var i = breaks.length-1; i >= 0; i--) {
            var node = breaks[i];
            var parentNode = node.parentNode;
            if (parentNode.tagName=='body') {
                var p = ownerdoc.createElement('p');
                var prev = node.previousSibling;
                if (prev && prev.nodeType==3) {
                    p.appendChild(prev);
                }
                parentNode.insertBefore(p,node);
                parentNode.removeChild(node);
            } else if (!node.nextSibling && (/(p|div)\b/i.test(parentNode.nodeName) && !(node.previousSibling&&node.previousSibling.nodeName=='br'))) {
                parentNode.removeChild(node);
            }
        }
    };

    this._convertNodes = function(ownerdoc, htmlnode, xhtmlparent, permitted) {
        var parentnode = xhtmlparent;
        var nodename = this._getTagName(htmlnode);
        var nostructure = !this.filterstructure;

        // TODO: This permits valid tags anywhere. it should use the state
        // table in xhtmlvalid to only permit tags where the XHTML DTD
        // says they are valid.
        var validattrs = this.tagAttributes[nodename];
        if (validattrs && (nostructure || permitted[nodename])) {
            try {
                var xhtmlnode = ownerdoc.createElement(nodename);
                parentnode = xhtmlnode;
            } catch (e) { };
            
            if (validattrs && xhtmlnode) {
                if (!this._copyAttributes(htmlnode, xhtmlnode, validattrs) && nodename=='span') {
                    parentnode = xhtmlparent;
                    xhtmlnode = null;
                }
            }
        } else {
            // Stripping this tag, maybe we also want to strip the
            // content of the tag.
            if (this.nastyTags[nodename]) { return null; }
        }

        var kids = htmlnode.childNodes;
        var permittedChildren = this.States[parentnode.tagName] || permitted;

        if (kids.length == 0) {
            // TOMAHAWK-1307 #000000 is displayed in inputHtml
            // changed htmlnode.text to htmlnode.textContent
            if (htmlnode.textContent && htmlnode.textContent != "" &&
                (nostructure || permittedChildren['#text'])) {
                var text = htmlnode.textContent;
                var tnode = ownerdoc.createTextNode(text);
                parentnode.appendChild(tnode);
            }
        } else {
            for (var i = 0; i < kids.length; i++) {
                var kid = kids[i];

                if (kid.parentNode !== htmlnode) {
                    if (kid.tagName.toLowerCase()=='body') {
                        if (nodename != 'html') continue;
                    } else if (kid.parentNode.tagName === htmlnode.tagName) {
                        continue; // IE bug: nodes appear multiple places
                    }
                }
                
                if (kid.nodeType == 1) {
                    var newkid = this._convertNodes(ownerdoc, kid, parentnode, permittedChildren);
                    if (newkid != null) {
                        parentnode.appendChild(newkid);
                    };
                } else if (kid.nodeType == 3) {
                    if (nostructure || permittedChildren['#text']) {
                        parentnode.appendChild(ownerdoc.createTextNode(kid.nodeValue));
                    }
                } else if (kid.nodeType == 4) {
                    if (nostructure || permittedChildren['#text']) {
                        parentnode.appendChild(ownerdoc.createCDATASection(kid.nodeValue));
                    }
                } else if (kid.nodeType == 8) {
                    parentnode.appendChild(ownerdoc.createComment(kid.nodeValue));
                }
            }
        } 
        return xhtmlnode;
    };
}
