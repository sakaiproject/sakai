/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

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
//		alert("After objToResize");

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var docElOffsetH = document.documentElement.offsetHeight;
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

//		alert("After innerDocScrollH");
	
		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			if (docElOffsetH > offsetH) {
			  height = docElOffsetH;
			} else {
			  height = offsetH;
			}
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 10;
		
		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
		
		
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;
//		window.status = s;
//		alert(s);
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

		if (parent.postIframeResize){ 
			parent. postIframeResize(id);
		}
	}

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
function installSherlock(name,cat,updateURL,iconURL) {
  if ((typeof window.sidebar == "object") && (typeof
  window.sidebar.addSearchEngine == "function"))
  {
    window.sidebar.addSearchEngine(
    	updateURL,
    	iconURL,
    	name,
		cat); 
  }
	
}

function addSherlockButton(name,cat,updateURL,iconURL)
{
  if ((typeof window.sidebar == "object") && (typeof
  window.sidebar.addSearchEngine == "function"))
  {
    var functionCall="installSherlock('"+name+"','"+cat+"','"+updateURL+"','"+iconURL+"' ); return false; ";
    var tag="<a href=\"#\" id=\"addSherlockButton\" onclick=\""+
    	functionCall+
    	"\" ><img src=\"/library/image/transparent.gif\" "+
    	"border=\"0\"   title=\"Install Browser Search Plugin\" "+
    	" alt=\"Install Browser Search Plugin\" /></a>";
    var sherlockButton=document.getElementById('sherlockButtonHolder');
    if ( sherlockButton) {
    	sherlockButton.innerHTML = tag;
    }
  } 
}
