/**********************************************************************************
* $HeadURL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/jsf/component/RichTextEditArea.java $
* $Id: RichTextEditArea.java 226 2005-06-23 23:46:26Z esmiley@stanford.edu $
***********************************************************************************
*
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation
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
function flagRows(constext){
//return;
 var divs = document.getElementsByTagName("A");
  for (var i = 0; i < divs.length; i++) {
     var d = divs[i];
     if (d.className == "folder"){
        d.style.backgroundImage = "url(" + context + "/jsp/aam/images/folder-open.gif)";
     }
   }
  }

function toggleRows(elm, context) {
 var rows = document.getElementsByTagName("TR");
 elm.style.backgroundImage = "url(" + context + "/jsp/aam/images/folder-closed.gif)";
 var newDisplay = "none";
 var thisID = elm.parentNode.parentNode.parentNode.id + "-";
 // Are we expanding or contracting? If the first child is hidden, we expand
  for (var i = 0; i < rows.length; i++) {
   var r = rows[i];
   if (matchStart(r.id, thisID, true)) {
    if (r.style.display == "none") {
     if (document.all) newDisplay = "block"; //IE4+ specific code
     else newDisplay = "table-row"; //Netscape and Mozilla
     elm.style.backgroundImage = "url(" + context + "/jsp/aam/images/folder-open.gif)";
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
      folder.style.backgroundImage = "url(" + context + "/jsp/aam/images/folder-closed.gif)";
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
