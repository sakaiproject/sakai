var RSF = RSF || {};

(function() {
   // Since Javascript is not multi-threaded, these working variables may be shared 
   // during a template parse
  var t;
  var parser;
  var tagstack;
  var lumpindex = 0;
  var nestingdepth = 0;
  var justended = false;
  
  var defstart = -1;
  var defend = -1;   
  
  var baseURL;
  
  var debugMode = false;
  
  var cutpoints = []; // list of selector, tree, id
  
  var cutstatus = [];
  
  function init(baseURLin, debugModeIn, cutpointsIn) {
    t.rootlump = RSF.XMLLump(0, -1);
    tagstack = [t.rootlump];
    lumpindex = 0;
    nestingdepth = 0;
    justended = false;
    defstart = -1;
    defend = -1;
    baseURL = baseURLin;
    debugMode = debugModeIn;
    cutpoints = cutpointsIn;
    if (cutpoints) {
      for (var i = 0; i < cutpoints.length; ++ i) {
        cutstatus[i] = [];
        cutpoints[i].tree = RSF.parseSelector(cutpoints[i].selector);
        }
      }
    }
  
  function findTopContainer() {
    for (var i = tagstack.length - 1; i >= 0; --i ) {
      var lump = tagstack[i];
      if (lump.rsfID) {
        return lump;
      }
    }
    return t.rootlump;
  }
  
  function newLump() {
    var togo = RSF.XMLLump(lumpindex, nestingdepth);
    if (debugMode) {
      togo.line = parser.getLineNumber();
      togo.column = parser.getColumnNumber();
    }
    //togo.parent = t;
    t.lumps[lumpindex] = togo;
    ++lumpindex;
    return togo;
  }
  
  function addLump(mmap, ID, lump) {
  	 var list = mmap[ID];
  	 if (!list) {
  	 	 list = [];
  	 	 mmap[ID] = list;
  	 }
  	 list[list.length] = lump;
  }
  
  function checkContribute(ID, lump) {
    if (ID.indexOf("scr=contribute-") !== -1) {
      var scr = ID.substring("scr=contribute-".length);
      addLump(t.collectmap, scr, lump);
      }
    }
  
  function rewriteUrl(url) {
    var po = RSF.parseUri(url);
    if (po.protocol || url.charAt(0) === '/') {
      return url;
    }
    else return baseURL + url;
  }
  
  function debugLump(lump) {
  // TODO expand this to agree with the Firebug "self-selector" idiom
    return "<" + lump.tagname + ">";
    }
  
  function hasCssClass(clazz, totest) {
    if (!totest) return false;
    // algorithm from JQuery
    return (" " + totest + " ").indexOf(" " + clazz + " ") !== -1;
    }
  
  function matchNode(term, headlump) {
    if (term.predList) {
      for (var i = 0; i < term.predList.length; ++ i) {
        var pred = term.predList[i];
        if (pred.id && headlump.attributemap.id !== pred.id) return false;
        if (pred.clazz && !hasCssClass(pred.clazz, headlump.attributemap["class"])) return false;
        if (pred.tag && headlump.tagname !== pred.tag) return false;
        }
      return true;
      }
    }
  
  function tagStartCut(headlump) {
    var togo = null;
    if (cutpoints) {
      for (var i = 0; i < cutpoints.length; ++ i) {
        var cut = cutpoints[i];
        var cutstat = cutstatus[i];
        var nextterm = cutstat.length; // the next term for this node
        if (nextterm < cut.tree.length) {
          var term = cut.tree[nextterm];
          if (nextterm > 0) {
            if (cut.tree[nextterm - 1].child && 
              cutstat[nextterm - 1] !== headlump.nestingdepth - 1) {
              continue; // it is a failure to match if not at correct nesting depth 
              }
            }
          var isMatch = matchNode(term, headlump);
          if (isMatch) {
            cutstat[cutstat.length] = headlump.nestingdepth;
            if (cutstat.length === cut.tree.length) {
              if (togo !== null) {
                throw ("Cutpoint specification error - node " 
                + debugLump(headlump) 
                + " has already matched with rsf:id of " + togo);
                }
              togo = cut.id;
              }
            }
          }
        }
      }
    return togo;
    }
    
  function tagEndCut() {
    if (cutpoints) {
      for (var i = 0; i < cutpoints.length; ++ i) {
        var cutstat = cutstatus[i];
        if (cutstat.length > 0 && cutstat[cutstat.length - 1] == nestingdepth) {
          cutstat.length--;
          }
        }
      }
    }
  
  function processTagStart(isempty, text) {
    ++nestingdepth;
    if (justended) {
      justended = false;
      var backlump = newLump();
      backlump.nestingdepth--;
    }
    if (t.firstdocumentindex === -1) {
      t.firstdocumentindex = lumpindex;
    }
    var headlump = newLump();
    var stacktop = tagstack[tagstack.length - 1];
    headlump.uplump = stacktop;
    var tagname = parser.getName();
    headlump.tagname = tagname;
    // NB - attribute names and values are now NOT DECODED!!
    headlump.attributemap = parser.m_attributes;
    for (var attrname in headlump.attributemap) {
      var attrval = headlump.attributemap[attrname];
      if (attrval === "href" || attrval === "src" || attrval === "codebase" || attrval === "action") {
        attrval = rewriteUrl(attrval);
        headlump.attributemap[attrname] = attrval;
        }
      }
    var ID = headlump.attributemap? headlump.attributemap[RSF.ID_ATTRIBUTE] : null;
    if (ID === null || ID === undefined) {
      ID = tagStartCut(headlump);
      }
    if (ID) {
      checkContribute(ID, headlump);
      headlump.rsfID = ID;
      var downreg = findTopContainer();
      if (!downreg.downmap) {
        downreg.downmap = {};
        }
      addLump(downreg.downmap, ID, headlump);
      addLump(t.globalmap, ID, headlump);
      var colpos = ID.indexOf(":");
      if (colpos !== -1) {
      var prefix = ID.substring(0, colpos);
      if (!stacktop.finallump) {
        stacktop.finallump = {};
        }
      stacktop.finallump[prefix] = headlump;
      }
    }
    
    // TODO: accelerate this by grabbing original template text (requires parser
    // adjustment) as well as dealing with empty tags
    headlump.text = "<" + tagname + RSF.dumpAttributes(headlump.attributemap) + ">";
    tagstack[tagstack.length] = headlump;
    if (isempty) {
      processTagEnd();
    }
  }
  
  function processTagEnd() {
    tagEndCut();
    var endlump = newLump();
    --nestingdepth;
    endlump.text = "</" + parser.getName() + ">";
    var oldtop = tagstack[tagstack.length - 1];
    oldtop.close_tag = t.lumps[lumpindex - 1];
    tagstack.length --;
    justended = true;
  }
  
  function processDefaultTag() {
    if (defstart !== -1) {
      if (t.firstdocumentindex === -1) {
        t.firstdocumentindex = lumpindex;
        }
      var text = parser.getContent().substr(defstart, defend - defstart);
      justended = false;
      var newlump = newLump();
      newlump.text = text; 
      defstart = -1;
    }
  }
  // Public definitions begin here
  
  RSF.ID_ATTRIBUTE = "rsf:id";
  
  RSF.HTML_CONSTANTS = {
    "href": ["a", "link"],
    "src": ["img", "frame", "script", "iframe", "style", "input", "embed"],
    "action": ["form"],
    "codebase": ["applet", "object"]
  };
  
  RSF.getPrefix = function(id) {
   var colpos = id.indexOf(':');
   return colpos === -1? id : id.substring(0, colpos);
   };
  
  RSF.SplitID = function(id) {
    var colpos = id.indexOf(':');
    if (colpos === -1) {
      this.prefix = id;
      }
    else {
      this.prefix = id.substring(0, colpos);
      this.suffix = id.substring(colpos + 1);
     }
  };
  RSF.XMLLump = function (lumpindex, nestingdepth) {
    return {
      //rsfID: "",
      //text: "",
      //downmap: {},
      //attributemap: {},
      //finallump: {},
      nestingdepth: nestingdepth,
      lumpindex: lumpindex,
      parent: t
    };
  };
  
  RSF.XMLViewTemplate = function() {
    return {
      globalmap: {},
      collectmap: {},
      lumps: [],
      firstdocumentindex: -1
    };
  };
  
  /** Accepts a hash of structures with free keys, where each entry has either
   * href or nodeId set - on completion, callback will be called with the populated
   * structure with fetched resource text in the field "resourceText" for each
   * entry.
   */
  RSF.fetchResources = function(resourceSpecs, callback) {
    var complete = true;
    for (var key in resourceSpecs) {
      var resourceSpec = resourceSpecs[key];
      if (resourceSpec.href && !resourceSpec.resourceText) {
        var templateCallback = function () {
          var thisSpec = resourceSpec;
          return {
            success: function(response) {
              thisSpec.resourceText = response.responseText;
              thisSpec.queued = false; 
              RSF.fetchResources(resourceSpecs, callback);
              }
            }
          }();
         if (!resourceSpec.queued) {
           RSF.queueAJAXRequest("fetchResources", "get", resourceSpec.href, "", templateCallback);
           resourceSpec.queued = true;
         }
         complete = false;             
        }
      else if (resourceSpec.nodeId && !resourceSpec.resourceText) {
        var node = document.getElementById(resourceSpec.nodeId);
        // upgrade this to somehow detect whether node is "armoured" somehow
        // with comment or CDATA wrapping
        resourceSpec.resourceText = RSF.getElementText(node);
      }
    }
    if (complete) {
      callback(resourceSpecs);
    }
  };
  
    // TODO: find faster encoder
  RSF.XMLEncode = function (text) {
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;"); 
    };
  
  RSF.dumpAttributes = function(attrcopy) {
    var togo = "";
    for (var attrname in attrcopy) {
      togo += " " + attrname + "=\"" + attrcopy[attrname] + "\"";
      }
    return togo;
    };
  
  RSF.aggregateMMap = function (target, source) {
    for (var key in source) {
      var targhas = target[key];
      if (!targhas) {
        target[key] = [];
      }
      target[key] = target[key].concat(source[key]);
    }
  };
  
  /** Returns a "template structure", with globalmap in the root, and a list
   * of entries {href, template, cutpoints} for each parsed template.
   */
  RSF.parseTemplates = function(resourceSpec, templateList, opts) {
    var togo = [];
    togo.globalmap = {};
    for (var i = 0; i < templateList.length; ++ i) {
      var resource = resourceSpec[templateList[i]];
      var lastslash = resource.href.lastIndexOf("/");
      var baseURL = lastslash === -1? "" : resource.href.substring(0, lastslash + 1);
        
        var template = RSF.parseTemplate(resource.resourceText, baseURL, 
          opts.scanStart && i === 0, resource.cutpoints, opts);
        if (i == 0) {
          RSF.aggregateMMap(togo.globalmap, template.globalmap);
        }
        template.href = resource.href;
        template.baseURL = baseURL;

        togo[i] = template;
        RSF.aggregateMMap(togo.globalmap, template.rootlump.downmap);
      }
      return togo;
    };
  
  RSF.parseTemplate = function(template, baseURL, scanStart, cutpoints_in, opts) {
    t = RSF.XMLViewTemplate();
    opts = opts || {};
    
    init(baseURL, opts.debugMode, cutpoints_in);

    var idpos = template.indexOf(RSF.ID_ATTRIBUTE);
    if (idpos === -1 && !cutpoints) return t;
    if (scanStart) {
      var brackpos = template.indexOf('>', idpos);
      parser = new XMLP(template.substring(brackpos + 1));
    }
    else {
      parser = new XMLP(template); 
      }

    parseloop: while(true) {
      var iEvent = parser.next();
//        if (iEvent === XMLP._NONE) break parseloop;
//        continue;
     
      switch(iEvent) {
        case XMLP._ELM_B:
          processDefaultTag()
          //var text = parser.getContent().substr(parser.getContentBegin(), parser.getContentEnd() - parser.getContentBegin());
          processTagStart(false, "");
          break;
        case XMLP._ELM_E:
          processDefaultTag()
          processTagEnd();
          break;
        case XMLP._ELM_EMP:
          processDefaultTag()
          //var text = parser.getContent().substr(parser.getContentBegin(), parser.getContentEnd() - parser.getContentBegin());    
          processTagStart(true, "");
          break;
        case XMLP._PI:
        case XMLP._DTD:
          defstart = -1;
          continue; // not interested in reproducing these
        case XMLP._TEXT:
        case XMLP._ENTITY:
        case XMLP._CDATA:
        case XMLP._COMMENT:
          if (defstart === -1) {
            defstart = parser.m_cB;
            }
          defend = parser.m_cE;
          break;
        case XMLP._ERROR:
          RSF.setLogging(true);
          var message = "Error parsing template: " + parser.m_cAlt + 
          " at line " + parser.getLineNumber(); 
          RSF.log(message);
          RSF.log("Just read: " + parser.m_xml.substring(parser.m_iP - 30, parser.m_iP));
          RSF.log("Still to read: " + parser.m_xml.substring(parser.m_iP, parser.m_iP + 30));
          throw (message);
          //alert(message);
          break parseloop;
        case XMLP._NONE:
          break parseloop;
        }
      }
    return t;
//       alert("document complete: " + chars.length + " chars");
  
    }
    
  // ******* SELECTOR ENGINE *********  
    
  // selector regexps copied from JQuery
  var chars = "(?:[\\w\u0128-\uFFFF*_-]|\\\\.)";
  var quickChild = new RegExp("^>\\s*(" + chars + "+)");
  var quickID = new RegExp("^(" + chars + "+)(#)(" + chars + "+)");
  var selSeg = new RegExp("^\s*([#.]?)(" + chars + "*)");

  var quickClass = new RegExp("([#.]?)(" + chars + "+)", "g");
  var childSeg = new RegExp("\\s*(>)?\\s*", "g");
  var whiteSpace = new RegExp("^\\w*$");


  RSF.trim = function( text ) {
    return (text || "").replace( /^\s+|\s+$/g, "" );
    };

  RSF.parseSelector = function(selstring) {
    var togo = [];
    selstring = RSF.trim(selstring);
    //ws-(ss*)[ws/>]
    quickClass.lastIndex = 0;
    var lastIndex = 0;
    while (true) {
      var atNode = []; // a list of predicates at a particular node
      while (true) {
        var segMatch = quickClass.exec(selstring);
        if (!segMatch || segMatch.index !== lastIndex) {
          break;
          }
        var thisNode = {};
        var text = segMatch[2];
        if (segMatch[1] === "") {
          thisNode.tag = text;
        }
        else if (segMatch[1] === "#"){
          thisNode.id = text;
          }
        else if (segMatch[1] === ".") {
          thisNode.clazz = text;
          }
        atNode[atNode.length] = thisNode;
        lastIndex = quickClass.lastIndex;
        }
      childSeg.lastIndex = lastIndex;
      var fullAtNode = {predList: atNode};
      var childMatch = childSeg.exec(selstring);
      if (!childMatch || childMatch.index !== lastIndex) {
        var remainder = selstring.substring(lastIndex);
        throw ("Error in selector string - can not match child selector expression at " + remainder);
        }
      if (childMatch[1] === ">") {
        fullAtNode.child = true;
        }
      togo[togo.length] = fullAtNode;
      if (childSeg.lastIndex === selstring.length) {
        break;
        }
      lastIndex = childSeg.lastIndex;
      quickClass.lastIndex = childSeg.lastIndex; 
      }
    return togo;
    }
    
})();
