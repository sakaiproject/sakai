/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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

var sakai = sakai ||
{};
var utils = utils ||
{};


var popupDivStack = new Array();
var frameHeightStack = new Array();
var popupDivID = "popupDIV";
var popupFocus = null;
var lastPopupFocus = null;
var popupURL = null;
var popupLoader = null;
var lastPopupURL = null;
var popupWaitingDiv = null;
var popupWaitingDivID = "popupwaitingdiv";
var popupindex = 0;
var asyncLoad = false;
var progressiveLoad = false;
var hasresize = false;
var targetForm = null;
				
function popupCallback(responsestring) {
	if ( popupDivStack[popupindex] != null ) 
	{
		log("popupcallback "+responsestring);
		popupDivStack[popupindex].innerHTML = responsestring;
		popupDivStack[popupindex].style.visibility = "visible";
		popupDivStack[popupindex].style.zIndex="100";
		sizeFrame(popupindex);
	}
}
function sizeFrame(istack) {
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
                var tl = getAbsolutePos(popupDivStack[istack]);
                var sh = popupDivStack[istack].scrollHeight;
                var oh = popupDivStack[istack].offsetHeight;
                var ch = popupDivStack[istack].clientHeight;
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
              //alert(s);
		     }
}
function mmax(n1,n2) {
    if ( n1 ) {
        if ( n2 ) {
            if ( n1 > n2 ) return n1;
            else return n2;
        } else {
            return n1;
        }
    } else {
        return n2;
    }
}
function saveSize(istack) {
		    var f = getFrameStyle(placementid);
		    if ( f != null ) frameHeightStack[istack] = f.height;
		    else frameHeightStack[istack] = 0;
		    //alert("Saved "+frameHeightStack[istack]);
}
function resizeFrame(istack) {
		var frameHeight = frameHeightStack[istack];
        //alert("Size "+istack+" frameHeight "+frameHeight);
		if ( frameHeight != 0 ) {
		    var f = getFrameStyle(placementid);
		    if ( f != null ) {
		        f.height = frameHeight;
		        //alert("Frame restored "+f.height);
		    }
		}
}
function getFrame(id){        
    if (typeof window.name != "undefined" && id != window.name) return 0;        
    var frame = parent.document.getElementById(id);        
    if (frame)        
    {            
        return frame;    
    }
    return null;
 }
function getFrameStyle(id){        
    if (typeof window.name != "undefined" && id != window.name) return 0;        
    var frame = parent.document.getElementById(id);        
    if (frame)        
    {                
                var objToResize = (frame.style) ? frame.style : frame;
                return objToResize;
    }
    return null;
 }

function popupShowWaiting() {
	if ( popupWaitingDiv == null ) 
	{
		popupWaitingDiv = document.getElementById(popupWaitingDivID);
	}
	if ( popupWaitingDiv != null && popupDivStack[popupindex] != null) 
	{
		popupDivStack[popupindex].innerHTML = popupWaitingDiv.innerHTML;
		
		popupDivStack[popupindex].style.visibility = "visible";		
	} 
}
function updatePopupFocus() {

				
					if ( popupDivStack[popupindex] == null ) 
					{
						popupDivStack[popupindex] = document.getElementById(popupDivID+popupindex);
					}
					if ( popupDivStack[popupindex] == null ) 
					{
					    saveSize(popupindex);
						popupDivStack[popupindex]=document.createElement("DIV");
    					    popupDivStack[popupindex].style.visibility="hidden";
    					    popupDivStack[popupindex].style.position="absolute";
    					    popupDivStack[popupindex].style.left="10";
    					    popupDivStack[popupindex].style.top="10";
    					    popupDivStack[popupindex].style.width="100";
						popupDivStack[popupindex].style.height="100";
						popupDivStack[popupindex].style.zIndex="100";
						document.body.appendChild(popupDivStack[popupindex]);
						
					}
					if ( lastPopupFocus != popupFocus ) 
					{
						log("updateFormFocus, change of focus from "+lastPopupFocus+" to "+popupFocus);
						// changed focus
						if ( popupDivStack[popupindex] != null ) 
						{
							// hide the div
							popupDivStack[popupindex].style.visibility="hidden";
							if ( popupFocus != null ) 
							{
								// position the div below the component
								var pos = getAbsolutePos(popupFocus);
								var width =  popupFocus.offsetWidth;
								var height =  popupFocus.offsetHeight;
								pos.y += height;
								log("Width "+width+":"+height+":"+pos.y+":"+pos.x);
								popupDivStack[popupindex].style.width = width;
								popupDivStack[popupindex].style.top = pos.y+"px ";
								popupDivStack[popupindex].style.left = pos.x+"px ";
								popupDivStack[popupindex].style.bgolor = "#cccccc";
							
							}		
						}
						lastPopupFocus = popupFocus;
					} 
					if ( popupFocus == null ) return; // now null so ignore
					
					var url = popupURL+"&puid="+(popupindex+1);
					if ( lastPopupURL != url ) 
					{
						log("popupFocus reload URL "+url);
						lastPopupURL = url;
						popupShowWaiting();
						if ( !targetForm ) {
						    popupLoader.loadXMLDoc(url,"popupCallback"); 
						} else {
						    popupLoader.fullLoadXMLDoc(url,"popupCallback","POST",targetForm);
						}
					}
					
					
					if ( progressiveLoad ) 
					{
						window.setTimeout("updatePopupFocus()",100);
					}
}
function popupClose(downTo) {

	log("Doing popupclose down to "+downTo);
	for ( i = popupindex; i >= (downTo-1); i-- ) 
	{
		if ( popupDivStack[i] != null )
		{
		//	popupDivStack[i].style.left="-1000";
    		//    popupDivStack[i].style.top="-1000";
			popupDivStack[i].style.visibility="hidden";
			document.body.removeChild(popupDivStack[i]);
			popupDivStack[i] = null;
			resizeFrame(i);
			
		}
		lastPopupURL = null;
	}
	popupindex = downTo-1;
	if ( popupindex < 0 ) popupindex = 0;
	popupFocus = null;
	lastPopupFocus = null;
	targetForm = null;
}




	
function ajaxRefPopup(element,url,poplevel) {
    ajaxRefPopupPost(element,url,poplevel,null);
}

function ajaxRefPopupPost(element,url,poplevel,tForm) {

	

	log("Doing popup on "+element+" URL "+url+" Level "+poplevel+" last "+popupindex);
	if ( popupLoader == null ) 
	{
		popupLoader = new AsyncDIVLoader();
 		popupLoader.loaderName = "popupLoader";
						
	}
	popupindex = poplevel;
	popupFocus = element;
	popupURL = url;
	targetForm = tForm;
	log("LOADING");
	if ( asyncLoad ) {
	   log("Doing Async Load");
	   window.setTimeout("updatePopupFocus()",100);
	} else {
       log("Doing Direct Focus");	
	   updatePopupFocus();
	}
	log("Loading DONE");
	
}

function getAbsolutePos(el) {
					var SL = 0, ST = 0;
					var is_div = /^div$/i.test(el.tagName);
					if (is_div && el.scrollLeft)
						SL = el.scrollLeft;
					if (is_div && el.scrollTop)
						ST = el.scrollTop;
					var r = { x: el.offsetLeft - SL, y: el.offsetTop - ST };
					if (el.offsetParent) {
						var tmp = getAbsolutePos(el.offsetParent);
						r.x += tmp.x;
						r.y += tmp.y;
					}
					return r;
}

function showPopupHere(el,divid) {
		var onRight;
        var targetdiv = document.getElementById(divid);
		if ( targetdiv != null ) {
			var pos = getAbsolutePos(el);
			var width =  el.offsetWidth;
			var height =  el.offsetHeight;
			pos.y += height;
			
			//log("Width "+width+":"+height+":"+pos.y+":"+pos.x);
			//targetdiv.style.width = width;
			if (
				  (
					$('.portletBody').width()  - pos.x
				  ) <= 400
				){
				onRight=true;
			}
			
			if (onRight) {
				targetdiv.style.left = (pos.x - 200) + "px ";
			}
			else {
				targetdiv.style.left = pos.x + "px ";
			}
				targetdiv.style.top = pos.y + "px ";
			targetdiv.style.bgolor = "#cccccc";		    
		    targetdiv.style.visibility = "visible";		
		} else {
            	alert(targetdiv.innerHTML);
		}
}
function hidePopup(divid) {
    var targetdiv =  document.getElementById(divid);
	if ( targetdiv != null ) {
        targetdiv.style.visibility = "hidden";		
     }
}
