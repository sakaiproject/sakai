/*
 * Copyright (c) 2008-2012 The Sakai Foundation
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
	
/* alternate method to shrink or grow the iframe */
function resizeFrame(updown){	 
    
	if (top.location != self.location) {
    	//PRFL-672 check context, ie if we are in a remote iframe (Basic LTI)
		try {
			if(parent.document){
				var frame = parent.document.getElementById(window.name);	
			}
		} catch (e) {
			return;
		}
    }	 
    if (frame) {	 
        if (updown == 'shrink') {	 
            var clientH = document.body.clientHeight;	 
        }	 
        else {	 
            var clientH = document.body.clientHeight + 400;	 
        }	
        $(frame).height(clientH);	 
    }	 
    else {	 
        //throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );	 
    }	 
}

$(document).ready(function () {

    // Show the various in place editing buttons when they receive keyboard focus
    $(".edit-image-button").focus(function () { 
        $(this).removeClass("offscreen"); 
    });

    $(".edit-image-button").blur(function () { 
        $(this).addClass("offscreen"); 
    }); 

    $(".edit-button").focus(function () { 
        $(this).removeClass("offscreen"); 
    }); 

    $(".edit-button").blur(function () { 
        $(this).addClass("offscreen"); 
    }); 
});

function doUpdateCK(){
    for (instance in CKEDITOR.instances) {
        CKEDITOR.instances[instance].updateElement();
    }
}
