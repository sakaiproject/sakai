/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

function changeClass(oldclass, newclass) {

    var spantags = document.getElementsByTagName("SPAN");
    var oldclasses = getElementsByClass(spantags,oldclass);
   // alert("Changin from "+oldclass+" to "+newclass+" for "+oldclasses.length);
    for (i = 0; i < oldclasses.length; i++ ) { 
       oldclasses[i].className = newclass;
    }
}

function changeRoleState(cb,column,enable,disable) {
    if ( cb ) {
        changeClass(column+disable,column+enable);
    } else {
        changeClass(column+enable,column+disable);
    }
}


// FIXME: Internationalize
contractsymbol = '<span class="rwiki_collapse"><img title="hide" alt="hide" src="/sakai-rwiki-tool/images/minus.gif"/><span>Hide </span></span>';
expandsymbol = '<span class="rwiki_expand"><img alt="show" src="/sakai-rwiki-tool/images/plus.gif" title="show"/><span>Show </span></span>';

function getElementsByClass(ellist, classname) {
  var els = new Array();
  for (i=0; i<ellist.length; i++) {
    if (ellist[i].className == classname) {
      els.push(ellist[i]);
    }
  }
  return els;
}

function expandcontent(root, blockname) {
  var block = document.getElementById(blockname);
  var spantags = root.getElementsByTagName("SPAN");
  var showstatespans = getElementsByClass(spantags, "showstate");

  block.style.display = (block.style.display != "block") ? "block" : "none";
  showstatespans[0].innerHTML = block.style.display == "block" ? contractsymbol : expandsymbol;
  window.onload();
}

function hidecontent(rootname, blockname) {
  var root = document.getElementById(rootname);
  var block = document.getElementById(blockname);
  var spantags = root.getElementsByTagName("SPAN");
  var showstatespans = getElementsByClass(spantags, "showstate");

  block.style.display = "none";
  showstatespans[0].innerHTML = expandsymbol;
}

function onload() {
  var allels = document.all? document.all : document.getElementsByTagName("*");
  var expandableContent = getElementsByClass(allels, "expandablecontent");
  for (var i = 0; i < expandableContent.length; i++) {
    expandableContent[i].style.display = "none";
  }
  var allexpandable = getElementsByClass(allels, "expandable");
  var i = 0;
  for (var i = 0; i < allexpandable.length; i++) {
    var spantags = allexpandable[i].getElementsByTagName("SPAN");
    var showstatespans = getElementsByClass(spantags, "showstate");
    showstatespans[0].innerHTML = expandsymbol;
  }

}

function storeCaret(el) {
    if ( el.createTextRange ) 
        el.caretPos = document.selection.createRange().duplicate();
}

function addAttachment(textareaid, formid, editcontrolid, type) {
  var textarea;
  var editcontrol;
  var form;
  var store;
  if ( document.all ) {
    textarea = document.all[textareaid];
    editcontrol = document.all[editcontrolid];
    form = document.all[formid];
  } else {
    textarea = document.getElementById(textareaid);
    editcontrol = document.getElementById(editcontrolid);
    form = document.getElementById(formid);
  }    

  if (typeof(textarea.caretPos) != "undefined" && typeof(textarea.createTextRange) != "undefined")
  {
    var duplicate = textarea.caretPos.duplicate();
    var textareaRange = textarea.createTextRange();

    textareaRange.select();
    textareaRange = document.selection.createRange().duplicate();
    duplicate.select();

    var duplicateText = duplicate.text;
    var length = duplicateText.replace(/\r\n/g,"\n").length;

    duplicate.setEndPoint("StartToStart", textareaRange); 	

    duplicateText = duplicate.text;
    var endPoint = duplicateText.replace(/\r\n/g,"\n").length;

    var startPoint = endPoint - length;
    store = startPoint + ":" + endPoint;
  } else if (typeof(textarea.selectionStart) != "undefined") {
    store = textarea.selectionStart + ":" + textarea.selectionEnd;
  } else {
    store = "0:0";
  }

  
  editcontrol.innerHTML += "<input type='hidden' name='command_attach"+type+"' value='attach" + type + "'/><input type='hidden' name='caretPosition' value='"+ store + "'/>";
  form.submit();
}


function addMarkup(textareaid, contentMU, startMU, endMU) {
    var textarea;
        if ( document.all ) {
            textarea = document.all[textareaid];
		} else {
		    textarea = document.getElementById(textareaid);
		}    

	if (typeof(textarea.caretPos) != "undefined" && textarea.createTextRange)
	{
        
		var caretPos = textarea.caretPos, repText = caretPos.text, temp_length = caretPos.text.length;
		var i=-1;
		while (++i < repText.length && /\s/.test("" + repText.charAt(i))) {
		}

		switch (i) {
		case 0: break;
		case repText.length: startMU = repText + startMU;
		repText = "";
		break;
		default: startMU = repText.substring(0, i) + startMU;
		         repText = repText.substr(i);
		}

		i = repText.length;
		while ( i > 0 && /\s/.test("" + repText.charAt(--i))) {
		}

		switch (i) {
		case repText.length - 1: break;
		case -1: endMU = endMU + repText; break;
		default: endMU = endMU + repText.substr(i + 1);
		         repText = repText.substring(0, i + 1);
		}

		
		if ( repText.length == 0 )
		    repText = contentMU;

		caretPos.text = startMU + repText + endMU;

		textarea.focus(caretPos);
	} 
	// Mozilla text range wrap.
	else if (typeof(textarea.selectionStart) != "undefined")
	{
		var begin = textarea.value.substr(0, textarea.selectionStart);
		var repText = textarea.value.substr(textarea.selectionStart, textarea.selectionEnd - textarea.selectionStart);
		var end = textarea.value.substr(textarea.selectionEnd);
		var newCursorPos = textarea.selectionStart;
		var scrollPos = textarea.scrollTop;
		
		var i=-1;
		while (++i < repText.length && /\s/.test("" + repText.charAt(i))) {
		}
		
		switch (i) {
		case 0: break;
		case repText.length: startMU = repText + startMU;
		repText = "";
		break;
		default: startMU = repText.substring(0, i) + startMU;
		         repText = repText.substr(i);
		}

		i = repText.length;
		while ( i > 0 && /\s/.test("" + repText.charAt(--i))) {
		}

		switch (i) {
		case repText.length - 1: break;
		case -1: endMU = endMU + repText; break;
		default: endMU = endMU + repText.substr(i + 1);
		         repText = repText.substring(0, i + 1);
		}

		
		if ( repText.length == 0 )
		    repText = contentMU;

		textarea.value = begin + startMU + repText + endMU + end;

		if (textarea.setSelectionRange)
		{
			textarea.setSelectionRange(newCursorPos + startMU.length, newCursorPos + startMU.length + repText.length);
			textarea.focus();
		}
		textarea.scrollTop = scrollPos;
	}
	// Just put them on the end, then.
	else
	{
		textarea.value += startMU + contentMU + endMU;
		textarea.focus(textarea.value.length - 1);
	}
}

/*
 * Content-seperated javascript tree widget
 * Copyright (C) 2005 SilverStripe Limited
 * Feel free to use this on your websites, but please leave this message in the fies
 * http://www.silverstripe.com/blog
*/

/*
 * Initialise all trees identified by <ul class="tree">
 */
function autoInit_trees() {
	var candidates = document.getElementsByTagName('ul');
	for(var i=0;i<candidates.length;i++) {
		if(candidates[i].className && candidates[i].className.indexOf('tree') != -1) {
			initTree(candidates[i]);
			candidates[i].className = candidates[i].className.replace(/ ?unformatted ?/, ' ');
		}
	}
}
 
/*
 * Initialise a tree node, converting all its LIs appropriately
 */
function initTree(el) {
	var i,j;
	var spanA, spanB, spanC;
	var startingPoint, stoppingPoint, childUL;
	
	// Find all LIs to process
	for(i=0;i<el.childNodes.length;i++) {
		if(el.childNodes[i].tagName && el.childNodes[i].tagName.toLowerCase() == 'li') {
			var li = el.childNodes[i];

			// Create our extra spans
			spanA = document.createElement('span');
			spanB = document.createElement('span');
			spanC = document.createElement('span');
			spanA.appendChild(spanB);
			spanB.appendChild(spanC);
			spanA.className = 'a ' + li.className.replace('closed','spanClosed');
			spanA.onMouseOver = function() {}
			spanB.className = 'b';
			spanB.onclick = treeToggle;
			spanC.className = 'c';
			
			
			// Find the UL within the LI, if it exists
			stoppingPoint = li.childNodes.length;
			startingPoint = 0;
			childUL = null;
			for(j=0;j<li.childNodes.length;j++) {
			    if ( li.childNodes[j].tagName != null ) {
				if( li.childNodes[j].tagName.toLowerCase() == 'div') {
					startingPoint = j + 1;
					continue;
				}

				if( li.childNodes[j].tagName.toLowerCase() == 'ul') {
					childUL = li.childNodes[j];
					stoppingPoint = j;
					break;					
				}
				}
			}
				
			// Move all the nodes up until that point into spanC
			for(j=startingPoint;j<stoppingPoint;j++) {
				spanC.appendChild(li.childNodes[startingPoint]);
			}
			
			// Insert the outermost extra span into the tree
			if(li.childNodes.length > startingPoint) li.insertBefore(spanA, li.childNodes[startingPoint]);
			else li.appendChild(spanA);
			
			// Process the children
			if(childUL != null) {
				if(initTree(childUL)) {
					addClass(li, 'children', 'closed');
					addClass(spanA, 'children', 'spanClosed');
				}
			}
		}
	}
	
	if(li) {
		// li and spanA will still be set to the last item

		addClass(li, 'last', 'closed');
		addClass(spanA, 'last', 'spanClosed');
		return true;
	} else {
		return false;
	}
		
}
 

/*
 * +/- toggle the tree, where el is the <span class="b"> node
 * force, will force it to "open" or "close"
 */
function treeToggle(el, force) {
	el = this;
	
	while(el != null && (!el.tagName || el.tagName.toLowerCase() != "li")) el = el.parentNode;
	
	// Get UL within the LI
	var childSet = findChildWithTag(el, 'ul');
	var topSpan = findChildWithTag(el, 'span');

	if( force != null ){
		
		if( force == "open"){
			treeOpen( topSpan, el )
		}
		else if( force == "close" ){
			treeClose( topSpan, el )
		}
		
	}
	
	else if( childSet != null) {
		// Is open, close it
		if(!el.className.match(/(^| )closed($| )/)) {		
			treeClose( topSpan, el )
		// Is closed, open it
		} else {			
			treeOpen( topSpan, el )
		}
	}
}


function treeOpen( a, b ){
	removeClass(a,'spanClosed');
	removeClass(b,'closed');
}
	
	
function treeClose( a, b ){
	addClass(a,'spanClosed');
	addClass(b,'closed');
}

/*
 * Find the a child of el of type tag
 */
function findChildWithTag(el, tag) {
	for(var i=0;i<el.childNodes.length;i++) {
		if(el.childNodes[i].tagName != null && el.childNodes[i].tagName.toLowerCase() == tag) return el.childNodes[i];
	}
	return null;
}

/*
 * Functions to add and remove class names
 * Mac IE hates unnecessary spaces
 */
function addClass(el, cls, forceBefore) {
	if(forceBefore != null && el.className.match(new RegExp('(^| )' + forceBefore))) {
		el.className = el.className.replace(new RegExp("( |^)" + forceBefore), '$1' + cls + ' ' + forceBefore);

	} else if(!el.className.match(new RegExp('(^| )' + cls + '($| )'))) {
		el.className += ' ' + cls;
		el.className = el.className.replace(/(^ +)|( +$)/g, '');
	}
}
function removeClass(el, cls) {
	var old = el.className;
	var newCls = ' ' + el.className + ' ';
	newCls = newCls.replace(new RegExp(' (' + cls + ' +)+','g'), ' ');
	el.className = newCls.replace(/(^ +)|( +$)/g, '');
} 

/*
 * Handlers for automated loading
 */ 
 _LOADERS = Array();

function callAllLoaders() {
	var i, loaderFunc;
	for(i=0;i<_LOADERS.length;i++) {
		loaderFunc = _LOADERS[i];
		if(loaderFunc != callAllLoaders) loaderFunc();
	}
}

function appendLoader(loaderFunc) {
	if(window.onload && window.onload != callAllLoaders)
		_LOADERS[_LOADERS.length] = window.onload;

	window.onload = callAllLoaders;

	_LOADERS[_LOADERS.length] = loaderFunc;
}

function setMainFrameHeightNoScroll(id, shouldScroll) {
	if (typeof(shouldScroll) == 'undefined') {
		shouldScroll = true;
	}
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	id = id.replace(/[^a-zA-Z0-9]/g,"x");
	id = "Main" + id;

	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{
		// reset the scroll
		if (shouldScroll) {
		  parent.window.scrollTo(0,0);
		}

		var objToResize = (frame.style) ? frame.style : frame;

                var height = getFrameHeight(frame);

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 10;
		
		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
		
		
		//window.location.hash = window.location.hash;
		if (shouldScroll) {
		  var anchor = document.location.hash;
		  if (anchor != null && anchor.length > 0 && anchor.charAt(0) == '#') {
		    anchor = anchor.substring(1);
		    var coords = getAnchorPosition(anchor);
		    var framey = findPosY(frame);
		    parent.window.scrollTo(coords.x, coords.y + framey);
		  }
		}
	}

}
/* get height of an iframe document */
function getFrameHeight (frame)
{
   var document = frame.contentWindow.document;
   var doc_height = document.height ? document.height : 0; // Safari uses document.height

if (document.documentElement && document.documentElement.scrollHeight) /* Strict mode */
      return Math.max (document.documentElement.scrollHeight, doc_height);
   else /* quirks mode */
      return Math.max (document.body.scrollHeight, doc_height);
}
// This invaluable function taken from QuirksMode @ http://www.quirksmode.org/index.html?/js/findpos.html
// Portable to virtually every browser, with a few caveates.
function findPosY(obj) {
  var curtop = 0;
  if (obj.offsetParent) {
    while (obj.offsetParent) {
      curtop += obj.offsetTop
	obj = obj.offsetParent;
    }
  } else if (obj.y) {
    curtop += obj.y;
  }
  return curtop;
}

function getAnchorPosition( anchorName){ 
 if (document.layers) {
    var anchor = document.anchors[anchorName];
    return { x: anchor.x, y: anchor.y };
  }
  else if (document.getElementById) {
    var anchor = document.anchors[anchorName];
    var coords = {x: 0, y: 0 };
    while (anchor) {
      coords.x += anchor.offsetLeft;
      coords.y += anchor.offsetTop;
      anchor = anchor.offsetParent;
    }
    return coords;
  }
}

appendLoader(autoInit_trees);


function hideSidebar(id) {
  document.getElementById('rwiki_sidebar').style.display='none';
  document.getElementById('rwiki_content').className = 'nosidebar';
  document.getElementById('sidebar_switch_on').style.display='block';
  document.getElementById('sidebar_switch_off').style.display='none';
  $('#content').css('width','99%');
	if ($('#ie8').length) {
		utils.fixIE8TextArea();
	}
  sizeFrameAfterAjax();
}
function showSidebar(id) {
  document.getElementById('rwiki_sidebar').style.display='block';
  document.getElementById('rwiki_content').className = 'withsidebar';
  document.getElementById('sidebar_switch_on').style.display='none';
  document.getElementById('sidebar_switch_off').style.display='block';
  $('#content').css('width','100%');
  sizeFrameAfterAjax();
}
var previewDiv;
function previewContent(contentId,previewId,pageVersionId,realmId,pageNameId,url) {
    try {
	 	var content = document.getElementById(contentId);
	 	var pageVersion = document.getElementById(pageVersionId);
	 	var pageName = document.getElementById(pageNameId);
	 	var realm = document.getElementById(realmId);
	 	previewDiv = document.getElementById(previewId);
	 	var formContent = new Array();
	 	formContent[0] = "content"
	 	formContent[1] = content.value;
	 	formContent[2] = "pageName";
	 	formContent[3] = pageName.value;
	 	formContent[4] = "save";
	 	formContent[5] = "render";
	 	formContent[6] = "action";
	 	formContent[7] = "fragmentpreview";
	 	formContent[8] = "panel";
	 	formContent[9] = "Main";
	 	formContent[10] = "version";
	 	formContent[11] = pageVersion.value;
	 	formContent[12] = "realm";
	 	formContent[13] = realm.value;
	 	var myLoader = new AsyncDIVLoader();
	 	myLoader.loaderName = "previewloader";
	 	previewDiv.innerHTML = "<img src=\"/sakai-rwiki-tool/images/ajaxload.gif\" />";
	 	myLoader.fullLoadXMLDoc(url,"divReplaceCallback","POST",formContent);
 	} catch  (e) {
	 	previewDiv.innerHTML = "<img src=\"/library/image/silk/error.png\" />";
 		alert("Failed to Load preview "+e);
 	}
}

function divReplaceCallback(responsestring) {
	previewDiv.innerHTML = responsestring;
	
        if (typeof jsMath != 'undefined') {
            // re-check whether math equation exists and load jsMath 
            // require jsMath 3.3c or newer
            jsMath.Autoload.ReCheck();
            // restrict jsMath to process new content for efficiency
            jsMath.ProcessBeforeShowing(previewDiv);
        }
	
	sizeFrameAfterAjax(previewDiv);
}

function sizeFrameAfterAjax(el) {
		    var frame = getFrame(placementid);
		    
		    if ( frame != null ) {
		    
                var height;
                var objToResize = (frame.style) ? frame.style : frame;

                var scrollH = document.body.scrollHeight;
                var offsetH = document.body.offsetHeight;
                var clientH = document.body.clientHeight;
                var innerDocScrollH = null;

                if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
                {
                        // very special way to get the height from IE on Windows!
                        // note that the above special way of testing for undefined variables is necessary for older browsers
                        // (IE 5.5 Mac) to not choke on the undefined variables.
                        var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
                        innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
                }

//              alert("After innerDocScrollH");

                if (document.all && innerDocScrollH != null)
                {
                        // IE on Windows only
                        height = innerDocScrollH;
                }
                else
                {
                        // every other browser!
                        height = offsetH;
                }
                
                
                // loop round all elements in this dom and find the max y extent
                var tl = 0;
                var sh = 0;
                if ( el != null ) {
                  tl = getAbsolutePos(el);
                
                  sh = el.scrollHeight;
                  var oh = el.offsetHeight;
                  var ch = el.clientHeight;
                } else {
                  tl = findMaxExtent(document,0);
                  tl = tl+50;
                  sh = 0;
                }
                var bottom = tl.y + sh;
 
                // here we fudge to get a little bigger
                var newHeight = mmax(mmax(mmax(mmax(height,scrollH),clientH),innerDocScrollH),bottom) + 50;

                // no need to be smaller than...
                //if (height < 200) height = 200;
                objToResize.height=newHeight + "px";
		    
                var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + 
                clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " bottom "+ bottom+
                " sh "+ sh +
                " oh "+oh+
                " ch "+ch+
                " Set height to: " + newHeight;
//              window.status = s;
//              alert(s);
//		     } else {
//		      alert(" No placement Fame for "+placementid);
		     }
}
function findMaxExtent(el,y) {
    var ab = getAbsolutePos(el);
    if ( ab.y > y ) y = ab.y;
	for ( i = 0; i < el.childNodes.length; i++ ) {
	    ab = getAbsolutePos(el.childNodes[i]);
	    if ( ab.y > y ) y = ab.y;
	}
	return y;
}

function selectTabs() {
	var work = selectTabs.arguments;
	for ( i = 0; i < work.length-2; i+=3) {
	   
		var el = document.getElementById(work[i]);
		if ( el ) {
		    if ( el.className == work[i+1] ) {
				el.className = work[i+2];
			}
		}
	}
}

var NUMBER_OF_PERMISSIONS =0;
var CREATE = NUMBER_OF_PERMISSIONS++;
var READ = NUMBER_OF_PERMISSIONS++;
var UPDATE = NUMBER_OF_PERMISSIONS++;
var ADMIN = NUMBER_OF_PERMISSIONS++;
var SUPERADMIN = NUMBER_OF_PERMISSIONS++;

function setPermissionDisplay(enabledClass,disabledClass,readSwitch,updateSwitch,adminSwitch) {
	var switches = new Array();

	// lets try something a bit more magical...
	switches[CREATE] = true;
	switches[READ] = readSwitch;
	switches[UPDATE] = updateSwitch;
	switches[ADMIN] = adminSwitch;
	switches[SUPERADMIN] = true;
	

	// for each role row
	for ( rowStart = 0; rowStart < permissionsMatrix.length;  rowStart += NUMBER_OF_PERMISSIONS ) {
		// determine if each permission should be set:
		for ( j = 0; j < NUMBER_OF_PERMISSIONS; j++) {
			permissionNumber = rowStart + j;

			permissionArray = permissionsMatrix[permissionNumber];
			var enabled = false;
			// By checking if the switch is set and the lock is set.
			for (i = 0; (!enabled) && (i < NUMBER_OF_PERMISSIONS); i++) {
				enabled = enabled || (( permissionArray[1].charAt(i) == 'x' ) && ( permissionsMatrix[rowStart + i][0]) && (switches[i]));			  
			}
		  						
			setEnabledElement(permissionsStem + permissionNumber, enabled);
		}
	}
}

function setEnabledElement(elId, enabled) {
	var el = null;
	if ( document.all ) {
		el = document.all[elId];
	} else {
		el = document.getElementById(elId);
	}
	if (el != null) {
		el.innerHTML = enabled ? yes_val : no_val;
		if (el.innerHTML == no_val) {
			$(el).addClass('highlight')
		}
		else {
			$(el).removeClass('highlight')
	}
	} 
}

function setClassName(elId,className) {
	var el = null;
	if ( document.all ) {
		el = document.all[elId];
	} else {
		el = document.getElementById(elId);
	}
	if ( el != null ) {
		el.className = className;
	}
}

