// =========================================================================
//
// tinyxmlsax.js - an XML SAX parser in JavaScript compressed for downloading
//
// version 3.1
//
// =========================================================================
//
// Copyright (C) 2000 - 2002, 2003 Michael Houghton (mike@idle.org), Raymond Irving and David Joham (djoham@yahoo.com)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.

// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Visit the XML for <SCRIPT> home page at http://xmljs.sourceforge.net
//


var whitespace = "\n\r\t ";
// List of closed HTML tags, taken from JQuery 1.2.3
var closedTags = {
  abbr: true, br: true, col: true, img: true, input: true,
  link: true, meta: true, param: true, hr: true, area: true, embed:true
}


XMLP = function(strXML) { 

    this.m_xml = strXML; 
    this.m_iP = 0;
    this.m_iState = XMLP._STATE_PROLOG; 
    this.m_stack = [];
    this.m_attributes = {};
    }
    
  XMLP._NONE = 0; 
  XMLP._ELM_B = 1; 
  XMLP._ELM_E = 2; 
  XMLP._ELM_EMP = 3; 
  XMLP._ATT = 4; 
  XMLP._TEXT = 5; 
  XMLP._ENTITY = 6; 
  XMLP._PI = 7; 
  XMLP._CDATA = 8; 
  XMLP._COMMENT = 9; 
  XMLP._DTD = 10; 
  XMLP._ERROR = 11;
   
  XMLP._CONT_XML = 0; 
  XMLP._CONT_ALT = 1; 
  XMLP._ATT_NAME = 0; 
  XMLP._ATT_VAL = 1; 
  
  XMLP._STATE_PROLOG = 1; 
  XMLP._STATE_DOCUMENT = 2; 
  XMLP._STATE_MISC = 3; 
  XMLP._errs = new Array(); 
  XMLP._errs[XMLP.ERR_CLOSE_PI = 0 ] = "PI: missing closing sequence"; 
  XMLP._errs[XMLP.ERR_CLOSE_DTD = 1 ] = "DTD: missing closing sequence"; 
  XMLP._errs[XMLP.ERR_CLOSE_COMMENT = 2 ] = "Comment: missing closing sequence"; 
  XMLP._errs[XMLP.ERR_CLOSE_CDATA = 3 ] = "CDATA: missing closing sequence"; 
  XMLP._errs[XMLP.ERR_CLOSE_ELM = 4 ] = "Element: missing closing sequence"; 
  XMLP._errs[XMLP.ERR_CLOSE_ENTITY = 5 ] = "Entity: missing closing sequence"; 
  XMLP._errs[XMLP.ERR_PI_TARGET = 6 ] = "PI: target is required"; 
  XMLP._errs[XMLP.ERR_ELM_EMPTY = 7 ] = "Element: cannot be both empty and closing"; 
  XMLP._errs[XMLP.ERR_ELM_NAME = 8 ] = "Element: name must immediatly follow \"<\""; 
  XMLP._errs[XMLP.ERR_ELM_LT_NAME = 9 ] = "Element: \"<\" not allowed in element names"; 
  XMLP._errs[XMLP.ERR_ATT_VALUES = 10] = "Attribute: values are required and must be in quotes"; 
  XMLP._errs[XMLP.ERR_ATT_LT_NAME = 11] = "Element: \"<\" not allowed in attribute names"; 
  XMLP._errs[XMLP.ERR_ATT_LT_VALUE = 12] = "Attribute: \"<\" not allowed in attribute values"; 
  XMLP._errs[XMLP.ERR_ATT_DUP = 13] = "Attribute: duplicate attributes not allowed"; 
  XMLP._errs[XMLP.ERR_ENTITY_UNKNOWN = 14] = "Entity: unknown entity"; 
  XMLP._errs[XMLP.ERR_INFINITELOOP = 15] = "Infinite loop"; 
  XMLP._errs[XMLP.ERR_DOC_STRUCTURE = 16] = "Document: only comments, processing instructions, or whitespace allowed outside of document element"; 
  XMLP._errs[XMLP.ERR_ELM_NESTING = 17] = "Element: must be nested correctly"; 
  
  XMLP.prototype._checkStructure = function(iEvent) {
    var stack = this.m_stack; 
    if (XMLP._STATE_PROLOG == this.m_iState) {
      // disabled original check for text node in prologue
      this.m_iState = XMLP._STATE_DOCUMENT;
      }

    if (XMLP._STATE_DOCUMENT === this.m_iState) {
      if ((XMLP._ELM_B == iEvent) || (XMLP._ELM_EMP == iEvent)) { 
        this.m_stack[stack.length] = this.getName();
        }
      if ((XMLP._ELM_E == iEvent) || (XMLP._ELM_EMP == iEvent)) {
        if (stack.length === 0) {
          //return this._setErr(XMLP.ERR_DOC_STRUCTURE);
          return XMLP._NONE;
          }
        var strTop = stack[stack.length - 1];
        this.m_stack.length--;
        if (strTop === null || strTop !== this.getName()) { 
          return this._setErr(XMLP.ERR_ELM_NESTING);
          }
        }
  
      // disabled original check for text node in epilogue - "MISC" state is disused
    }
  return iEvent;
  }
  
  
XMLP.prototype.getColumnNumber = function() { 
  return SAXStrings.getColumnNumber(this.m_xml, this.m_iP);
  }
  
XMLP.prototype.getContent = function() { 
  return (this.m_cSrc == XMLP._CONT_XML) ? this.m_xml : this.m_cAlt;
  }
  
XMLP.prototype.getContentBegin = function() { return this.m_cB;}
XMLP.prototype.getContentEnd = function() { return this.m_cE;}

XMLP.prototype.getLineNumber = function() { 
  return SAXStrings.getLineNumber(this.m_xml, this.m_iP);
  }
  
XMLP.prototype.getName = function() { 
  return this.m_name;
  }
  
XMLP.prototype.next = function() { 
  return this._checkStructure(this._parse());
  }
  
XMLP.prototype._parse = function() {
  var iP = this.m_iP;
  var xml = this.m_xml; 
  if (iP === xml.length) { return XMLP._NONE;}
  var c = xml.charAt(iP);
  if (c === '<') {
    var c2 = xml.charAt(iP + 1);
    if (c2 === '?') {
      return this._parsePI (iP + 2);
      }
    else if (c2 === '!') {
      if (iP === xml.indexOf("<!DOCTYPE", iP)) { 
        return this._parseDTD (iP + 9);
        }
      else if (iP === xml.indexOf("<!--", iP)) { 
        return this._parseComment(iP + 4);
        }
      else if (iP === xml.indexOf("<![CDATA[", iP)) { 
        return this._parseCDATA (iP + 9);
        }
      }
    else {
      return this._parseElement(iP + 1);
      }
    }
  else {
    return this._parseText (iP);
    }
  }
  
var nameRegex = /([^\s>]+)/g;
var attrRegex = /\s*([\w:]+)=\"([^\"]*)\"\s*/gm;
var closeRegex = /\s*<\//g;

XMLP.prototype._parseElement = function(iB) { 
  var iE, iDE, iRet; 
  var iType, strN, iLast; 
  iDE = iE = this.m_xml.indexOf(">", iB); 
  if (iE == -1) { 
    return this._setErr(XMLP.ERR_CLOSE_ELM);
    }
  if (this.m_xml.charAt(iB) == "/") { 
    iType = XMLP._ELM_E; 
    iB++;
    } 
  else { 
    iType = XMLP._ELM_B;
    }
  if (this.m_xml.charAt(iE - 1) == "/") { 
    if (iType == XMLP._ELM_E) { 
      return this._setErr(XMLP.ERR_ELM_EMPTY);
      }
    iType = XMLP._ELM_EMP; iDE--;
    }

  nameRegex.lastIndex = iB;
  var nameMatch = nameRegex.exec(this.m_xml);
  if (!nameMatch) {
    return this._setErr(XMLP.ERR_ELM_NAME);
    }
  this.m_attributes = {};
  this.m_cAlt = ""; 
  strN = nameMatch[1];
  if (nameRegex.lastIndex < iDE) {
    this.m_iP = nameRegex.lastIndex;
    while (this.m_iP < iDE) {
      attrRegex.lastIndex = this.m_iP;
      var attrMatch = attrRegex.exec(this.m_xml);
      if (!attrMatch) {
        return this._setErr(XMLP.ERR_ATT_VALUES)
        }
      var attrname = attrMatch[1];
      var attrval = attrMatch[2];
      if (!this.m_attributes[attrname]) {
        this.m_attributes[attrname] = attrval;
        }
      else { 
        return this._setErr(XMLP.ERR_ATT_DUP);
      }
      this.m_iP = attrRegex.lastIndex;
      }
    }
  if (strN.indexOf("<") != -1) { 
    return this._setErr(XMLP.ERR_ELM_LT_NAME);
    }
  this.m_name = strN; 
  this.m_iP = iE + 1;
  // Check for corrupted "closed tags" from innerHTML
  if (closedTags[strN]) {
    closeRegex.lastIndex = iE + 1;
    var closeMatch = closeRegex.exec;
    if (closeMatch) {
      var matchclose = this.m_xml.indexOf(strN, closeMatch.lastIndex);
      if (matchclose === closeMatch.lastIndex) {
        return iType; // bail out, a valid close tag is separated only by whitespace
      }
      else {
        return XMLP._ELM_EMP;
      }
    }
  } 
  return iType;
  }
  
XMLP.prototype._parseCDATA = function(iB) { 
  var iE = this.m_xml.indexOf("]]>", iB); 
  if (iE == -1) { return this._setErr(XMLP.ERR_CLOSE_CDATA);}
  this._setContent(XMLP._CONT_XML, iB, iE); 
  this.m_iP = iE + 3; 
  return XMLP._CDATA;
  }
  
XMLP.prototype._parseComment = function(iB) { 
  var iE = this.m_xml.indexOf("-" + "->", iB); 
  if (iE == -1) { 
    return this._setErr(XMLP.ERR_CLOSE_COMMENT);
    }
  this._setContent(XMLP._CONT_XML, iB, iE); 
  this.m_iP = iE + 3; 
  return XMLP._COMMENT;
  }
  
XMLP.prototype._parseDTD = function(iB) { 
  var iE, strClose, iInt, iLast; 
  iE = this.m_xml.indexOf(">", iB); 
  if (iE == -1) { 
    return this._setErr(XMLP.ERR_CLOSE_DTD);
    }
  iInt = this.m_xml.indexOf("[", iB); 
  strClose = ((iInt != -1) && (iInt < iE)) ? "]>" : ">"; 
  while (true) { 
    if (iE == iLast) { 
      return this._setErr(XMLP.ERR_INFINITELOOP);
      }
    iLast = iE; 
    iE = this.m_xml.indexOf(strClose, iB); 
    if(iE == -1) { 
      return this._setErr(XMLP.ERR_CLOSE_DTD);
      }
    if (this.m_xml.substring(iE - 1, iE + 2) != "]]>") { break;}
    }
  this.m_iP = iE + strClose.length; 
  return XMLP._DTD;
  }
  
XMLP.prototype._parsePI = function(iB) { 
  var iE, iTB, iTE, iCB, iCE; 
  iE = this.m_xml.indexOf("?>", iB); 
  if (iE == -1) { return this._setErr(XMLP.ERR_CLOSE_PI);}
  iTB = SAXStrings.indexOfNonWhitespace(this.m_xml, iB, iE); 
  if (iTB == -1) { return this._setErr(XMLP.ERR_PI_TARGET);}
  iTE = SAXStrings.indexOfWhitespace(this.m_xml, iTB, iE); 
  if (iTE == -1) { iTE = iE;}
  iCB = SAXStrings.indexOfNonWhitespace(this.m_xml, iTE, iE); 
  if (iCB == -1) { iCB = iE;}
  iCE = SAXStrings.lastIndexOfNonWhitespace(this.m_xml, iCB, iE); 
  if (iCE == -1) { iCE = iE - 1;}
  this.m_name = this.m_xml.substring(iTB, iTE); 
  this._setContent(XMLP._CONT_XML, iCB, iCE + 1); 
  this.m_iP = iE + 2; 
  return XMLP._PI;
  }
  
XMLP.prototype._parseText = function(iB) { 
  var iE, iEE;
  iE = this.m_xml.indexOf("<", iB);
  if (iE == -1) { iE = this.m_xml.length;}
  this._setContent(XMLP._CONT_XML, iB, iE); 
  this.m_iP = iE; 
  return XMLP._TEXT;
  }
  
XMLP.prototype._setContent = function(iSrc) { 
  var args = arguments; 
  if (XMLP._CONT_XML == iSrc) { 
    this.m_cAlt = null; 
    this.m_cB = args[1]; 
    this.m_cE = args[2];
    } 
  else { 
    this.m_cAlt = args[1]; 
    this.m_cB = 0; 
    this.m_cE = args[1].length;
    }
    
  this.m_cSrc = iSrc;
  }
  
XMLP.prototype._setErr = 
  function(iErr) { 
  var strErr = XMLP._errs[iErr]; 
  this.m_cAlt = strErr; 
  this.m_cB = 0; 
  this.m_cE = strErr.length; 
  this.m_cSrc = XMLP._CONT_ALT; 
  return XMLP._ERROR;
  }
  


SAXStrings = function() { }

SAXStrings.WHITESPACE = " \t\n\r"; 
SAXStrings.QUOTES = "\"'"; 
SAXStrings.getColumnNumber = function(strD, iP) { 
  if (!strD) { return -1;}
  iP = iP || strD.length; 
  var arrD = strD.substring(0, iP).split("\n"); 
  var strLine = arrD[arrD.length - 1]; 
  arrD.length--; 
  var iLinePos = arrD.join("\n").length; 
  return iP - iLinePos;
  } 
  
SAXStrings.getLineNumber = function(strD, iP) { 
  if (!strD) { return -1;}
  iP = iP || strD.length; 
  return strD.substring(0, iP).split("\n").length;
  }
  
SAXStrings.indexOfNonWhitespace = function(strD, iB, iE) {
  if (!strD) return -1;
  iB = iB || 0; 
  iE = iE || strD.length; 
  
  for (var i = iB; i < iE; ++ i) { 
    var c = strD.charAt(i);
    if (c !== ' ' && c !== '\t' && c !== '\n' && c !== '\r') return i;
    }
  return -1;
  }
  
  
SAXStrings.indexOfWhitespace = function(strD, iB, iE) { 
  if (!strD) { return -1;}
    iB = iB || 0; 
    iE = iE || strD.length; 
    for (var i = iB; i < iE; i++) { 
      if (SAXStrings.WHITESPACE.indexOf(strD.charAt(i)) != -1) { return i;}
    }
  return -1;
  }
  
  
SAXStrings.lastIndexOfNonWhitespace = function(strD, iB, iE) { 
    if (!strD) { return -1;}
    iB = iB || 0; iE = iE || strD.length; 
    for (var i = iE - 1; i >= iB; i--) { 
    if (SAXStrings.WHITESPACE.indexOf(strD.charAt(i)) == -1) { 
      return i;
      }
    }
  return -1;
  }
  
SAXStrings.replace = function(strD, iB, iE, strF, strR) { 
  if (!strD) { return "";}
  iB = iB || 0; 
  iE = iE || strD.length; 
  return strD.substring(iB, iE).split(strF).join(strR);
  }

function __unescapeString(str) {
  return str.replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&")
    .replace(/&quot;/g, "\"").replace(/&apos;/g, "'");
  }
  
function __escapeString(str) { var escAmpRegEx = /&/g; var escLtRegEx = /</g; var escGtRegEx = />/g; var quotRegEx = /"/g;
    var aposRegEx = /'/g;

    str = str.replace(escAmpRegEx, "&amp;");
    str = str.replace(escLtRegEx, "&lt;");
    str = str.replace(escGtRegEx, "&gt;");
    str = str.replace(quotRegEx, "&quot;");
    str = str.replace(aposRegEx, "&apos;");

  return str;
}

