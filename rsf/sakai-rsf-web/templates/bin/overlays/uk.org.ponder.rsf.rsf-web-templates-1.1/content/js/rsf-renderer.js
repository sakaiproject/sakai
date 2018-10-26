
var RSF = RSF || {};

(function() {
    
  function computeFullID(component) {
    var togo = "";
    var move = component;
    if (component.children === undefined) { // not a container
      togo = component.ID;
      move = component.parent;
      }
    while (move.parent) {
      var parent = move.parent;
      if (move.fullID !== undefined) {
        togo = move.fullID + togo;
        return togo;
        }
      if (move.noID === undefined) {
        var ID = move.ID;
        var colpos = ID.indexOf(":");        
        var prefix = colpos === -1? ID : ID.substring(0, colpos);
        togo = prefix + ":" + (move.localID === undefined ? "" : move.localID) + ":" + togo;
      }
      move = parent;
    }
    return togo;
  }
  
  function processChild(value, key) {
    var valueType = typeof(value);
    if (!value || valueType === "string" || valueType === "boolean" || valueType == "number") {
      return {componentType: "UIBound", value: value, ID: key};
      }
    else {
      var unzip = unzipComponent(value);
      unzip.ID = key;
      return unzip; 
      }    
    }
  
  function fixChildren(children) {
    if (!(children instanceof Array)) {
      var togo = [];
      for (var key in children) {
        var value = children[key];
        if (value instanceof Array) {
          for (var i = 0; i < value.length; ++ i) {
            var processed = processChild(value[i], key);
            if (processed.componentType === "UIContainer" &&
              processed.localID === undefined) {
              processed.localID = i;
            }
            togo[togo.length] = processed;
            }
          }
        else {
          togo[togo.length] = processChild(value, key);
        } 
      }
      return togo;
    }
    else return children;
  }
  
  function upgradeBound(holder, property) {
    if (holder[property] !== undefined) {
      if (holder[property].value === undefined) {
        holder[property] = {value: holder[property]};
        }
      }
    else {
      holder[property] = {value: null}
      }  
    }
  
  var duckMap = {children: "UIContainer", 
    value: "UIBound", markup: "UIVerbatim", selection: "UISelect",
    choiceindex: "UISelectChoice", functionname: "UIInitBlock"};
  
  function unzipComponent(component) {
    for (var key in duckMap) {
      if (component[key] !== undefined) {
        component.componentType = duckMap[key];
        break;
      }
    }
    if (component.componentType === undefined) {
      component = {componentType: "UIContainer", children: component};
    }
    if (component.componentType === "UIContainer") {
      component.children = fixChildren(component.children);
    }
    else if (component.componentType === "UISelect") {
      upgradeBound(component, "selection");
      upgradeBound(component, "optionlist");
      upgradeBound(component, "optionnames");
    }
    return component;
  }
  
  function fixupTree(tree) {
    if (tree.componentType === undefined) {
      tree = unzipComponent(tree);
      }
    
    if (tree.children) {
    	 tree.childmap = {};
      for (var i = 0; i < tree.children.length; ++ i) {
        var child = tree.children[i];
        if (child.componentType === undefined) {
          child = unzipComponent(child);
          tree.children[i] = child;
          }
        child.parent = tree;
        child.fullID = computeFullID(child);
        var colpos = child.ID.indexOf(":"); 
        if (colpos === -1) {
          tree.childmap[child.ID] = child;
        }
        else {
          var prefix = child.ID.substring(0, colpos);
        	var childlist = tree.childmap[prefix]; 
        	if (!childlist) {
        		childlist = [];
        		tree.childmap[prefix] = childlist;
        	}
       
        	childlist[childlist.length] = child;
        }

        var componentType = child.componentType;
        if (componentType == "UISelect") {
          child.selection.fullID = child.fullID + "-selection";
        }
        else if (componentType == "UIInitBlock") {
          var call = child.functionname + '(';
          for (var j = 0; j < child.arguments.length; ++ j) {
            if (child.arguments[j] instanceof RSF.ComponentReference) {
              // TODO: support more forms of id reference
              child.arguments[j] = child.parent.fullID + child.arguments[j].reference;
            }
            call += '"' + child.arguments[j] + '"'; 
            if (j < child.arguments.length - 1) {
              call += ", ";
            }
          }
          child.markup = call + ")\n";
          child.componentType = "UIVerbatim";
          }
        else if (componentType == "UIBound") {
         // TODO: fetching bound values on fixup, and UISelect names
          if (child.submittingname === undefined && child.valuebinding !== undefined) {
            child.submittingname = child.fullID;
            }
          }
        fixupTree(child);
        }
      }
    return tree;
    }
  var globalmap = {};
  var branchmap = {};
  var seenset = {};
  var collected = {};
  var out = "";
  var debugMode = false;
  var directFossils = {}; // map of submittingname to {EL, submittingname, oldvalue}
  
  function resolveInScope(searchID, defprefix, scope, child) {
    var deflump;
    var scopelook = scope? scope[searchID] : null;
    if (scopelook) {
      for (var i = 0; i < scopelook.length; ++ i) {
        var scopelump = scopelook[i];
        if (!deflump && scopelump.rsfID == defprefix) {
          deflump = scopelump;
        }
        if (scopelump.rsfID == searchID) {
          return scopelump;
        }
      }
    }
    return deflump;
  }
  
  function resolveCall(sourcescope, child) {
    var searchID = child.jointID? child.jointID : child.ID;
    var split = new RSF.SplitID(searchID);
    var defprefix = split.prefix + ':';
    var match = resolveInScope(searchID, defprefix, sourcescope.downmap, child);
    if (match) return match;
    if (child.children) {
      match = resolveInScope(searchID, defprefix, globalmap, child);
      if (match) return match;
    }
    return null;
  }
  
  function noteCollected(template) {
    if (!seenset[template.href]) {
      RSF.aggregateMMap(collected, template.collectmap);
      seenset[template.href] = true;
    }
  }
  
  function resolveRecurse(basecontainer, parentlump) {
    for (var i = 0; i < basecontainer.children.length; ++ i) {
      var branch = basecontainer.children[i];
      if (branch.children) { // it is a branch TODO
        var resolved = resolveCall(parentlump, branch);
        if (resolved) {
          branchmap[branch.fullID] = resolved;
          // on server-side this is done separately
          noteCollected(resolved.parent);
          resolveRecurse(branch, resolved);
        }
      }
    }
  }
  
  function resolveBranches(globalmapp, basecontainer, parentlump) {
    branchmap = {};
    globalmap = globalmapp;
    branchmap[basecontainer.fullID] = parentlump;
    resolveRecurse(basecontainer, parentlump);
  }
  
  function dumpBranchHead(branch, targetlump) {
    var attrcopy = {};
    RSF.assign(attrcopy, targetlump.attributemap);
    adjustForID(attrcopy, branch);
    out += "<" + targetlump.tagname + " ";
    out += RSF.dumpAttributes(attrcopy);
    out += "/>";
  }
  
  function dumpTillLump(lumps, start, limit) {
    for (; start < limit; ++ start) {
      var text = lumps[start].text;
      if (text) { // guard against "undefined" lumps from "justended"
        out += lumps[start].text;
      }
    }
  }

  function dumpScan(lumps, renderindex, basedepth, closeparent, insideleaf) {
    var start = renderindex;
    while (true) {
      if (renderindex === lumps.length)
        break;
      var lump = lumps[renderindex];
      if (lump.nestingdepth < basedepth)
        break;
      if (lump.rsfID) {
        if (!insideleaf) break;
        if (insideleaf && lump.nestingdepth > basedepth + (closeparent?0:1) ) {
          RSF.log("Error in component tree - leaf component found to contain further components - at " +
              lump.toString());
        }
        else break;
      }
      // target.print(lump.text);
      ++renderindex;
    }
    // ASSUMPTIONS: close tags are ONE LUMP
    if (!closeparent && (renderindex == lumps.length || !lumps[renderindex].rsfID))
      --renderindex;
    
    dumpTillLump(lumps, start, renderindex);
    //target.write(buffer, start, limit - start);
    return renderindex;
  }
  
  var trc = {};
  
  /*** TRC METHODS ***/
  
  function closeTag() {
    if (!trc.iselide) {
      out += "</" + trc.uselump.tagname + ">";
    }
  }

  function renderUnchanged() {
  	// TODO needs work since we don't keep attributes in text
    dumpTillLump(trc.uselump.parent.lumps, trc.uselump.lumpindex + 1,
        trc.close.lumpindex + (trc.iselide ? 0 : 1));
  }
  
  function replaceAttributes() {
    if (!trc.iselide) {
      out += RSF.dumpAttributes(trc.attrcopy);
    }
    dumpTemplateBody();
  }

  function replaceAttributesOpen() {
    if (trc.iselide) {
      replaceAttributes();
    }
    else {
      out += RSF.dumpAttributes(trc.attrcopy);
      out += trc.endopen.lumpindex === trc.close.lumpindex ? "/>" : ">";

      trc.nextpos = trc.endopen.lumpindex;
    }
  }

  function dumpTemplateBody() {
    if (trc.endopen.lumpindex === trc.close.lumpindex) {
      if (!trc.iselide) {
        out += "/>";
      }
    }
    else {
      if (!trc.iselide) {
        out += ">";
      }
      dumpTillLump(trc.uselump.parent.lumps, trc.endopen.lumpindex,
          trc.close.lumpindex + (trc.iselide ? 0 : 1));
    }
  }

  function rewriteLeaf(value) {
    if (value && !isPlaceholder(value))
      replaceBody(value);
    else
      replaceAttributes();
  }

  function rewriteLeafOpen(value) {
  	if (trc.iselide) {
      rewriteLeaf(trc.value);
    }
    else {
      if (value)
        replaceBody(value);
      else
        replaceAttributesOpen();
    }
  }
  
  function replaceBody(value) {
    out += RSF.dumpAttributes(trc.attrcopy);
    if (!trc.iselide) {
      out += ">";
    }
    out += RSF.XMLEncode(value.toString());
    closeTag();
  }
  
  /*** END TRC METHODS**/
  function isPlaceholder(value) {
    // TODO: equivalent of server-side "placeholder" system
    return false;
  }
  
  function dumpHiddenField(/** UIParameter **/ todump) {
    out += "<input type=\"hidden\" ";
    var isvirtual = todump.virtual;
    var outattrs = {};
    outattrs[isvirtual? "id" : "name"] = todump.name;
    outattrs.value = todump.value;
    out += RSF.dumpAttributes(outattrs);
    out += " />\n";
  }
  
  function dumpBoundFields(/** UIBound**/ torender) {
    if (torender) {
      if (directFossils && torender.submittingname && torender.valuebinding) {
        directFossils[torender.submittingname] = {
          name: torender.submittingname,
          EL: torender.valuebinding,
          oldvalue: torender.value};
        }
      if (torender.fossilizedbinding) {
        dumpHiddenField(torender.fossilizedbinding);
      }
      if (torender.fossilizedshaper) {
        dumpHiddenField(torender.fossilizedshaper);
      }
    }
  }
    
  RSF.NULL_STRING = "\u25a9null\u25a9"; // TODO:check on Javascript Unicode escapes
    
  function renderComponent(torender) {
    var attrcopy = trc.attrcopy;
    var lumps = trc.uselump.parent.lumps;
    var lumpindex = trc.uselump.lumpindex;
    
    var componentType = torender.componentType;
    
    if (componentType === "UIBound") {
      //if (torender.willinput) {
        if (torender.submittingname !== undefined) {
          attrcopy.name = torender.submittingname;
          }
      //  }
      if (typeof(torender.value) === 'boolean') {
        if (torender.value) {
          attrcopy.checked = "checked";
          }
        else {
          delete attrcopy.checked;
          }
        attrcopy.value = "true";
        rewriteLeaf(null);
        }
      else if (torender.value instanceof Array) {
        // Cannot be rendered directly, must be fake
        renderUnchanged();
        }
      else {
        var value = torender.value;
        if (trc.uselump.tagname === "textarea") {
          if (isPlaceholder(value) && torender.willinput) {
            // FORCE a blank value for input components if nothing from
            // model, if input was intended.
            value = "";
          }
          rewriteLeaf(value);
        }
        else if (trc.uselump.tagname === "input") {
          if (torender.willinput || !isPlaceholder(value)) {
            attrcopy.value = value;
            }
          rewriteLeaf(null);
          }
        else {
          delete attrcopy.name;
          rewriteLeafOpen(value);
          }
        }
      dumpBoundFields(torender);
      }
    else if (componentType === "UISelect") {
      if (attrcopy.id) {
        // TODO: This is an irregularity, should probably remove for 0.8
        attrcopy.id = torender.selection.fullID;
        }
      var ishtmlselect = trc.uselump.tagname === "select";
      var ismultiple = false;

      if (torender.selection.value instanceof Array) {
        ismultiple = true;
        if (ishtmlselect) {
          attrcopy.multiple = "multiple";
          }
        }
      
      if (ishtmlselect) {
        // The HTML submitted value from a <select> actually corresponds
        // with the selection member, not the top-level component.
        if (torender.selection.willinput && torender.selection.submittingname !== undefined) {
          attrcopy.name = torender.selection.submittingname;
        }
      }
      out += RSF.dumpAttributes(attrcopy);
      if (ishtmlselect) {
        out += ">";
        var values = torender.optionlist.value;
        var names = torender.optionnames ? values: torender.optionnames.value;
        for (var i = 0; i < names.length; ++i) {
          out += "<option value=\"";
          var value = values[i];
          if (value === null)
            value = RSF.NULL_STRING;
          out += RSF.XMLEncode(value);
          if (ismultiple? (RSF.indexOf(torender.selection.value, value) !== -1) :
            (torender.selection.value === value)) {
            out += "\" selected=\"selected";
            }
          out += "\">";
          out += RSF.XMLEncode(names[i]);
          out += "</option>\n";
        }
        closeTag();
      }
      else {
        dumpTemplateBody();
      }

      dumpBoundFields(torender.selection);
      dumpBoundFields(torender.optionlist);
      dumpBoundFields(torender.optionnames);
    }
    else if (torender.markup !== undefined) { // detect UIVerbatim
      var rendered = torender.markup;
      if (rendered == null) {
        //TODO, doesn't quite work due to attr folding cf Java code
          out += RSF.dumpAttributes(attrcopy);
          out +=">";
          renderUnchanged(); 
      }
      else {
        if (!trc.iselide) {
          out += RSF.dumpAttributes(attrcopy);
          out += ">";
        }
        out += rendered;
        closeTag();
        }    
      }
      else {
      	
      }
    }
  
  function adjustForID(attrcopy, component) {
    delete attrcopy["rsf:id"];
    if (attrcopy.id) {
      attrcopy.id = component.fullID;
      }
    }
  
  function renderComponentSystem(torendero, lump) {
    var lumpindex = lump.lumpindex;
    var lumps = lump.parent.lumps;
    var nextpos = -1;
    var outerendopen = lumps[lumpindex + 1];
    var outerclose = lump.close_tag;

    nextpos = outerclose.lumpindex + 1;

    var payloadlist = lump.downmap? lump.downmap["payload-component"] : null;
    var payload = payloadlist? payloadlist[0] : null;
    if (torendero == null) {
    	// no support for SCR yet
    }
    else {
    	// else there IS a component and we are going to render it. First make
      // sure we render any preamble.
      var endopen = outerendopen;
      var close = outerclose;
      var uselump = lump;
      if (payload) {
        endopen = lumps[payload.lumpindex + 1];
        close = payload.close_tag;
        uselump = payload;
        dumpTillLump(lumps, lumpindex, payload.lumpindex);
        lumpindex = payload.lumpindex;
      }

      var attrcopy = {};
      RSF.assign(attrcopy, lump.attributemap);
      adjustForID(attrcopy, torendero);
      //decoratormanager.decorate(torendero.decorators, uselump.getTag(), attrcopy);
      var iselide = lump.rsfID.charCodeAt(0) === 126 // "~"
      trc.attrcopy = attrcopy;
      trc.uselump = uselump;
      trc.endopen = endopen;
      trc.close = close;
      trc.nextpos = nextpos;
      trc.iselide = iselide;
      
      // ALWAYS dump the tag name, this can never be rewritten. (probably?!)
      if (!iselide) {
        out += "<" + uselump.tagname;
       }

      renderComponent(torendero);
      // if there is a payload, dump the postamble.
      if (payload != null) {
        // the default case is initialised to tag close
        if (rendercontext.nextpos === nextpos) {
          dumpTillLump(lumps, close.lumpindex + 1, outerclose.lumpindex + 1);
        }
      }
      nextpos = trc.nextpos;
      }
  return nextpos;
  }
  
  function renderContainer(child, targetlump) {
    var t2 = targetlump.parent;
    var firstchild = t2.lumps[targetlump.lumpindex + 1];
    if (child.children !== undefined) {
      dumpBranchHead(child, targetlump);
    }
    else {
      renderComponentSystem(child, targetlump);
    }
    renderRecurse(child, targetlump, firstchild);
  }
  
  function fetchComponent(basecontainer, id, lump) {
    if (id.indexOf("msg=") === 0) {
      var key = id.substring(4);
      return {componentType: "UIBound"};
      // TODO messages
    }
    while (basecontainer) {
      var togo = basecontainer.childmap[id];
      if (togo)
        return togo;
      basecontainer = basecontainer.parent;
    }
    return null;
  }

  function fetchComponents(basecontainer, id) {
    var togo;
    while (basecontainer) {
      togo = basecontainer.childmap[id];
      if (togo)
        break;
      basecontainer = basecontainer.parent;
    }
    return togo;
  }

  function findChild(sourcescope, child) {
    var split = new RSF.SplitID(child.ID);
    var headlumps = sourcescope.downmap[child.ID];
    if (headlumps == null) {
      headlumps = sourcescope.downmap[split.prefix + ":"];
    }
    return headlumps == null ? null : headlumps[0];
  }
  
  function renderRecurse(basecontainer, parentlump, baselump) {
    var renderindex = baselump.lumpindex;
    var basedepth = parentlump.nestingdepth;
    var t1 = parentlump.parent;
    while (true) {
      renderindex = dumpScan(t1.lumps, renderindex, basedepth, true, false);
      if (renderindex === t1.lumps.length) { 
        break;
      }
      var lump = t1.lumps[renderindex];  
      if (lump.nestingdepth < basedepth) {
        break;
      } 
      var id = lump.rsfID;
      if (id.charCodeAt(0) === 126) { // "~"
        id = id.substring(1);
      }
      
      //var ismessagefor = id.indexOf("message-for:") === 0;
      
      if (id.indexOf(':') !== -1) {
        var prefix = RSF.getPrefix(id);
        var children = fetchComponents(basecontainer, prefix);
        
        var finallump = lump.uplump.finallump[prefix];
        var closefinal = finallump.close_tag;
        
        if (children) {
          for (var i = 0; i < children.length; ++ i) {
            var child = children[i];
            if (child.children) { // it is a branch TODO
              var targetlump = branchmap[child.fullID];
              if (targetlump) {
                renderContainer(child, targetlump);
              }
              else if (debugMode){
                out += "Unable to look up branch for component with id " + child.fullID;
              }
            }
            else { // repetitive leaf
              var targetlump = findChild(parentlump, child);
              var renderend = renderComponentSystem(child, targetlump);
              var wasopentag = t1.lumps[renderend].nestingdepth >= targetlump.nestingdepth;
              var newbase = child.children? child : basecontainer;
              if (wasopentag) {
                renderRecurse(newbase, targetlump, t1.lumps[renderend]);
                renderend = targetlump.close_tag.lumpindex + 1;
              }
              if (i !== children.length - 1) {
                // TODO - fix this bug in RSF Server!
                if (renderend < closefinal.lumpindex) {
                  dumpScan(t1.lumps, renderend, targetlump.nestingdepth - 1, false, false);
                }
              }
              else {
                dumpScan(t1.lumps, renderend, targetlump.nestingdepth, true, false);
              }
            }
          }
        }
        
        renderindex = closefinal.lumpindex + 1;
      }
      else {
        var component;
        if (id) {
          component = fetchComponent(basecontainer, id, lump);
        }
        if (component && component.children !== undefined) {
          renderContainer(component);
          renderindex = lump.close_tag.lumpindex + 1;
        }
        else {
          renderindex = renderComponentSystem(component, lump);
        }
      }
      if (renderindex === t1.lumps.length) {
        break;
      }
    }
  }
  
  function renderCollect(collump) {
    dumpTillLump(collump.parent.lumps, collump.lumpindex, collump.close_tag.lumpindex + 1);
  }
  
  function renderCollects() {
    for (var key in collected) {
      var collist = collected[key];
      for (var i = 0; i < collist.length; ++ i) {
        renderCollect(collist[i]);
      }
    }
  }

  RSF.ComponentReference = function(reference) {
      this.reference = reference;
    };
    
  // Explodes a raw "hash" into a list of UIOutput/UIBound entries
  RSF.explode = function(hash, basepath) {
    var togo = [];
    for (var key in hash) {
      var binding = basepath === undefined? key : basepath + "." + key;
      togo[togo.length] = {ID: key, value: hash[key], valuebinding: binding};
    }
    return togo;
  };
  
  RSF.parseEL = function(EL) {
    return EL.split('.');
    };
  
  /** This function implements the RSF "DARApplier" **/
  RSF.setBeanValue = function(root, EL, newValue) {
    var segs = RSF.parseEL(EL);
    for (var i = 0; i < segs.length - 1; ++ i) {
      root = root[segs[i]];
      }
    root[segs[segs.length - 1]] = newValue;
    };
  
  /** "Automatically" apply to whatever part of the data model is
   * relevant, the changed value received at the given DOM node*/
  RSF.applyChange = function(node, newValue) {
    var root = RSF.findData(node, "rsf-binding-root");
    var name = node.name;
    var EL = root.fossils[name].EL;
    RSF.setBeanValue(root.data, EL, newValue);    
    };
    
  RSF.makeBranches = function() {
    var firstBranch;
    var thisBranch;
    for (var i = 0; i < arguments.length; ++ i) {
      var thisarg = arguments[i];
      var nextBranch;
      if (typeof(thisarg) === "string") {
        nextBranch = {ID: thisarg}; 
        }
      else if (thisarg instanceof Array) {
        nextBranch = {ID: thisarg[0], jointID: thisarg[1]};
        }
      else {
        RSF.assign(thisBranch, thisarg);
        nextBranch = thisBranch;
        } 
      if (thisBranch && nextBranch !== thisBranch) {
        if (!thisBranch.children) {
          thisBranch.children = [];
          }
        thisBranch.children[thisBranch.children.length] = nextBranch;
        }
      thisBranch = nextBranch;
      if (!firstBranch) {
        firstBranch = nextBranch;
        }
      }
    
    return firstBranch;
    };

  RSF.transform = function(list) {
	var togo = [];
	for (var i = 0; i < list.length; ++ i) {
	  var transit = list[i];
	  for (var j = 0; j < arguments.length - 1; ++ j) {
	    transit = arguments[j + 1](transit, i);
	    }
	    togo[togo.length] = transit;
      }
    return togo;
    };
    
  RSF.renderTemplates = function(templates, tree, opts, fossilsIn) {
    opts = opts || {};
    debugMode = opts.debugMode;
    directFossils = fossilsIn;

    tree = fixupTree(tree);
    var template = templates[0];
    resolveBranches(templates.globalmap, tree, template.rootlump);
    out = "";
    renderCollects();
    renderRecurse(tree, template.rootlump, template.lumps[template.firstdocumentindex]);
    return out;
    };

  RSF.bindFossils = function(node, data, fossils) {
    RSF.data(node, "rsf-binding-root", {data: data, fossils: fossils});
    },

  // A simple driver for single node self-templating  
  RSF.selfRender = function(node, tree, opts) {
    opts = opts || {};
    if (node.jquery) {
      node = node.get(0);
      }
    var resourceSpec = {base: {resourceText: node.innerHTML, 
                          href: ".", cutpoints: opts.cutpoints}
                        };
    var templates = RSF.parseTemplates(resourceSpec, ["base"], opts);
    var fossils = {};
    var rendered = RSF.renderTemplates(templates, tree, opts, fossils);
    if (opts.renderRaw) {
      rendered = RSF.XMLEncode(rendered);
      rendered = rendered.replace(/\n/g, "<br/>");
      }
    if (opts.bind) {
      RSF.bindFossils(node, opts.bind, fossils);
      }
    node.innerHTML = rendered;
    return templates;
  }
  
})();
