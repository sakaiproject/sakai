/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
function setMainFrameHeightNoScroll(id, minHeight, additionalHeight)
{
  if(typeof minHeight == 'undefined') {minHeight=0;}
  if(typeof additionalHeight == 'undefined') {additionalHeight=0;}
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) {return;}
	if (!id) {return;}

	var frame = parent.document.getElementById(id);
	if (frame) {
    // reset the scroll
    //parent.window.scrollTo(0,0);

    var height = 0,
        objToResize = (frame.style) ? frame.style : frame,
        offsetH = document.body.offsetHeight,
        innerDocScrollH = null;

    if (typeof(frame.contentDocument) !== 'undefined' || typeof(frame.contentWindow) !== 'undefined') {
      // very special way to get the height from IE on Windows!
      // note that the above special way of testing for undefined variables is necessary for older browsers
      // (IE 5.5 Mac) to not choke on the undefined variables.
      var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
      innerDocScrollH = (innerDoc !== null) ? innerDoc.body.scrollHeight : null;
    }

    // alert("After innerDocScrollH");

    if (document.all && innerDocScrollH !== null) {
      // IE on Windows only
      height = innerDocScrollH;
    }else{
      // every other browser!
      height = offsetH;
    }

    // here we fudge to get a little bigger
    //gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody
    var newHeight = height + 15;
    
    // Force minimum height if specified
    if(minHeight !== 0 && newHeight < minHeight){
      newHeight = minHeight;
    }
    
    // Add additional height if specified
    if(additionalHeight && additionalHeight !== 0) {
      newHeight = newHeight + additionalHeight;
    }

    // no need to be smaller than...
    objToResize.height=newHeight  + 5 + "px";
	}

}