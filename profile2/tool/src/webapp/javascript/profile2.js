/*
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

	
/* get how far the window has been scrolled down the page */
function getScroll() {
	if (document.all) {
        // We are In MSIE.
        return top.document.documentElement.scrollTop;
    } else {
        // In Firefox
        return top.pageYOffset;
    } 
}

/* fix vertical issue with Wicket Modal window in an iframe. puts it 50px below top of viewport rather than vertically centered. */
function fixWindowVertical() { 
	var myWindow=Wicket.Window.get(); 
	if(myWindow) {
		var top = getScroll() + 50; 
		myWindow.window.style.top = top + "px";
	}
	return false;
} 
	
/*
function resizeIframe(iframeID) { 
	
	//Checks that page is in iframe
	if(self==parent) {
		return false;
	}
	// Checks for IE5+. 
	else if(document.getElementById&&document.all)  {

	var FramePageHeight = framePage.scrollHeight + 10; 
	// framePage 
	is the ID of the framed page's BODY tag. The added 10 pixels prevent an 
	unnecessary scrollbar. 

	parent.document.getElementById(iframeID).style.height=FramePageHeight; 
	// "iframeID" is the ID of the inline frame in the parent page. 
	} 
	*/
	
