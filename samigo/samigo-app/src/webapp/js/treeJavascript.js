/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */




var checkflag = "false";

function checkAll(field) {
if (field != null) {
  if (field.length >0){
// for more than one checkbox
    if (checkflag == "false") {
       for (i = 0; i < field.length; i++) {
           field[i].checked = true;}
       checkflag = "true";
       return "Uncheck all"; }
    else {
       for (i = 0; i < field.length; i++) {
           field[i].checked = false; }
       checkflag = "false";
       return "Check all"; }
  }
  else {
// for only one checkbox
    if (checkflag == "false") {
  field.checked = true;
  checkflag = "true";
  return "Uncheck all"; }
else {
  field.checked = false; 
  checkflag = "false";
  return "Check all"; }

   }
}
}


function uncheck(field){
      field.checked = false; 
  checkflag = "false";
    return "uncheck"; 
}

function flagRows(){
//return;
 var divs = document.getElementsByTagName("A");
  for (var i = 0; i < divs.length; i++) {
     var d = divs[i];
     if (d.className == "folder"){
        d.style.backgroundImage = "url(../../../images/tree/closed.gif)";
     }
   }
  }

function toggleRows(elm) {
 var rows = document.getElementsByTagName("TR");
 elm.style.backgroundImage = "url(../../../images/tree/closed.gif)";
 var newDisplay = "none";
 var thisID = elm.parentNode.parentNode.parentNode.id + "-";
 // Are we expanding or contracting? If the first child is hidden, we expand
  for (var i = 0; i < rows.length; i++) {
   var r = rows[i];
   if (matchStart(r.id, thisID, true)) {
    if (r.style.display == "none") {
     if (document.all) newDisplay = "block"; //IE4+ specific code
     else newDisplay = "table-row"; //Netscape and Mozilla
     elm.style.backgroundImage = "url(../../../images/tree/open.gif)";
    }
    break;
   }
 }

 // When expanding, only expand one level.  Collapse all desendants.
 var matchDirectChildrenOnly = (newDisplay != "none");

 for (var j = 0; j < rows.length; j++) {
   var s = rows[j];
   if (matchStart(s.id, thisID, matchDirectChildrenOnly)) {
     s.style.display = newDisplay;
     var cell = s.getElementsByTagName("TD")[0];
     var tier = cell.getElementsByTagName("DIV")[0];
     var folder = tier.getElementsByTagName("A")[0];
     if (folder.getAttribute("onclick") != null) {
      folder.style.backgroundImage = "url(../../../images/tree/closed.gif)";
     }
   }
 }
}

function matchStart(target, pattern, matchDirectChildrenOnly) {
 var pos = target.indexOf(pattern);
 if (pos != 0) return false;
 if (!matchDirectChildrenOnly) return true;
 if (target.slice(pos + pattern.length, target.length).indexOf("-") >= 0) return
 false;
 return true;
}

function collapseAllRows() {
 var rows = document.getElementsByTagName("TR");
 for (var j = 0; j < rows.length; j++) {
   var r = rows[j];
   if (r.id.indexOf("-") >= 0) {
     r.style.display = "none";
   }
 }
}

function collapseRowsByLevel(i) {
 var rows = document.getElementsByTagName("TR");
 for (var j = 0; j < rows.length; j++) {
   var r = rows[j];
   var rtokens =r.id.split("-");

   if (r.id.indexOf("-") >= 0) {
     if (rtokens.length > i) {
       r.style.display = "none";
     }
   }

 }
}


// below is for simple tree
function toggleBullet(elm) {
 var newDisplay = "none";
 var e = elm.nextSibling; 
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") {
   if (e.style.display == "none") newDisplay = "block";
   break;
  }
  e = e.nextSibling;
 }
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") e.style.display = newDisplay;
  e = e.nextSibling;
 }
}

function collapseAll() {
  var lists = document.getElementsByTagName('OL');
  for (var j = 0; j < lists.length; j++) 
   lists[j].style.display = "none";
  lists = document.getElementsByTagName('ol');
  for (var j = 0; j < lists.length; j++) 
   lists[j].style.display = "none";
  var e = document.getElementById("root");
  e.style.display = "block";
}

function PopupWin(url)
{
   window.open(url,"ha_fullscreen","toolbar=no,location=no,directories=no,status=no,menubar=yes,"+"scrollbars=yes,resizable=yes,width=640,height=480");

}
