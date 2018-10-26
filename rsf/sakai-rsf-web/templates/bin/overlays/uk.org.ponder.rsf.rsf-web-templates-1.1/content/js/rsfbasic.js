
// RSF.js - primitive definitions for navigating RSF ID-structured documents

// definitions placed in RSF namespace, following approach recommended in 
// http://www.dustindiaz.com/namespace-your-javascript/

var RSF = RSF || {};

(function() {

  var ns4 = (document.layers);
  var ie4 = (document.all && !document.getElementById);
  var ie5 = (document.all && document.getElementById);
  var ns6 = (!document.all && document.getElementById);
  
  RSF.getElementGlob = function (dokkument, id) {
    var obj;
    if(ns4) obj = dokkument.layers[id];
    else if(ie4) obj = dokkument.all[id];
    else if(ie5 || ns6) obj = dokkument.getElementById(id);
    return obj;
    };

  RSF.getElement = function (id) {
    return RSF.getElementGlob(document, id);
    };

    // Gets the value of an element in the current document with the given ID
  RSF.getValue = function (id) {
    return ns4? RSF.getElement(id).document : RSF.getElement(id).firstChild.nodeValue;
    };

  // Gets the value of an element in the same repetitive domain as "baseid" 
  // with the local id of "targetid".
  RSF.getRelativeValue = function (baseid, targetid) {
    colpos = baseid.lastIndexOf(':');
    return RSF.getValue(baseid.substring(0, colpos + 1) + targetid);
    };

  RSF.getBaseID = function(id) {
    colpos = id.lastIndexOf(':');
    return id.substring(0, colpos + 1)
  };

  // Gets the ID of an element in the same repetitive domain as "baseid" 
  // with the local id of "targetid".
  RSF.getRelativeID = function (baseid, targetid) {
    colpos = baseid.lastIndexOf(':');
    return baseid.substring(0, colpos + 1) + targetid;
    };
})();