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

var sakai = sakai || {};
var utils = utils || {};
var wiki = wiki || {};

var popupDivStack = [];
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

  if ( popupDivStack[popupindex] != null ) {
    log("popupcallback "+responsestring);
    popupDivStack[popupindex].innerHTML = responsestring;
    popupDivStack[popupindex].style.visibility = "visible";
    popupDivStack[popupindex].style.zIndex="100";
  }
}

function mmax(n1, n2) {

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

function popupShowWaiting() {

  if ( popupWaitingDiv == null ) {
    popupWaitingDiv = document.getElementById(popupWaitingDivID);
  }
  if ( popupWaitingDiv != null && popupDivStack[popupindex] != null) {
    popupDivStack[popupindex].innerHTML = popupWaitingDiv.innerHTML;
    popupDivStack[popupindex].style.visibility = "visible";
  }
}

function updatePopupFocus() {

  if ( popupDivStack[popupindex] == null ) {
    popupDivStack[popupindex] = document.getElementById(popupDivID + popupindex);
  }
  if ( popupDivStack[popupindex] == null ) {
    popupDivStack[popupindex] = document.createElement("DIV");
    popupDivStack[popupindex].style.visibility="hidden";
    popupDivStack[popupindex].style.position="absolute";
    popupDivStack[popupindex].style.left="10";
    popupDivStack[popupindex].style.top="10";
    popupDivStack[popupindex].style.width="100";
    popupDivStack[popupindex].style.height="100";
    popupDivStack[popupindex].style.zIndex="100";
    document.body.appendChild(popupDivStack[popupindex]);
  }
  if ( lastPopupFocus != popupFocus ) {
    log(`updateFormFocus, change of focus from ${lastPopupFocus} to ${popupFocus}`);
    // changed focus
    if ( popupDivStack[popupindex] != null ) {
      // hide the div
      popupDivStack[popupindex].style.visibility="hidden";
      if ( popupFocus != null ) {
        // position the div below the component
        const pos = getAbsolutePos(popupFocus);
        const width =  popupFocus.offsetWidth;
        const height =  popupFocus.offsetHeight;
        pos.y += height;
        log(`"Width ${width}:${height}:${pos.y}:${pos.x}`);
        popupDivStack[popupindex].style.width = width;
        popupDivStack[popupindex].style.top = pos.y+"px ";
        popupDivStack[popupindex].style.left = pos.x+"px ";
        popupDivStack[popupindex].style.bgolor = "#cccccc";
      }
    }
    lastPopupFocus = popupFocus;
  }
  if ( popupFocus == null ) return; // now null so ignore
  
  const url = popupURL + "&puid=" + (popupindex+1);
  if ( lastPopupURL != url ) {
    log("popupFocus reload URL "+url);
    lastPopupURL = url;
    popupShowWaiting();
    if ( !targetForm ) {
      popupLoader.loadXMLDoc(url,"popupCallback"); 
    } else {
      popupLoader.fullLoadXMLDoc(url,"popupCallback","POST", targetForm);
    }
  }

  if ( progressiveLoad ) {
    window.setTimeout("updatePopupFocus()", 100);
  }
}

function popupClose(downTo) {

  log(`Doing popupclose down to ${downTo}`);
  for ( i = popupindex; i >= (downTo - 1); i-- ) {
    if ( popupDivStack[i] != null ) {
      popupDivStack[i].style.visibility = "hidden";
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

function ajaxRefPopupPost(element, url, poplevel, tForm) {

  log(`Doing popup on ${element} URL ${url} Level ${poplevel} last ${popupindex}`);
  if ( popupLoader == null ) {
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

  let SL = 0, ST = 0;
  const isDiv = /^div$/i.test(el.tagName);
  if (isDiv && el.scrollLeft) {
    SL = el.scrollLeft;
  }
  if (isDiv && el.scrollTop) {
    ST = el.scrollTop;
  }
  const r = { x: el.offsetLeft - SL, y: el.offsetTop - ST };
  if (el.offsetParent) {
    const tmp = getAbsolutePos(el.offsetParent);
    r.x += tmp.x;
    r.y += tmp.y;
  }
  return r;
}

function showPopupHere(el, divid) {

  let onRight;
  const targetdiv = document.getElementById(divid);
  if ( targetdiv != null ) {
    const pos = getAbsolutePos(el);
    const width =  el.offsetWidth;
    const height =  el.offsetHeight;
    pos.y += height;

    if ( ($('.portletBody').width()  - pos.x) <= 400 ) {
      onRight = true;
    }

    if (onRight) {
      targetdiv.style.left = (pos.x - 200) + "px ";
    } else {
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

  const targetDiv =  document.getElementById(divid);
  targetDiv && (targetDiv.style.visibility = "hidden");
}

function contentCallback(content) {

  const div = document.createElement("div");
  div.innerHTML = content;
  document.body.appendChild(div);
}

function newEditComment(el, url, edit) {

  const id = `rwiki-${edit ? "edit" : "new"}comment-modal`;

  // If a popoup div has already been added, remove it.
  const current = document.getElementById(id);
  current && current.parentElement.remove();

  if ( popupLoader == null ) {
    popupLoader = new AsyncDIVLoader();
    popupLoader.loaderName = "popupLoader";
  }

  popupLoader.loadXMLDoc(url,"contentCallback");

  window.setTimeout(() => {

    const modalEl = document.getElementById(id);
    modalEl && (new bootstrap.Modal(modalEl).show());
  }, 500);
}
