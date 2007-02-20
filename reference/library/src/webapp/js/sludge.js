/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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

// functions for Sludge applications

function setMainFrameHeightFancy(id)
{
	// only if we have a parent with an element by our id
	if (parent && parent.document && parent.document.getElementById(id))
	{
		// reset the scroll
		parent.window.scrollTo(0,0);

		// the frame is in a parent window this high
		var windowHeight = parent.window.innerHeight;
		
		// the parent's content is taking this height
		var parentContentHeight= parent.document.body.offsetHeight;

		// if the parent's content is larger than its window, we need the overall scroll for the portal
		if (parentContentHeight > windowHeight)
		{
			// if we have a menu, it is this high
			var menuHeight  = 0;
			if (document.getElementById('sludge_menu'))
			{
				menuHeight = document.getElementById('sludge_menu').offsetHeight;
			}
			
			// if we have buttons, they are this high
			var buttonsHeight = 0;
			if (document.getElementById('sludge_buttons'))
			{
				buttonsHeight = document.getElementById('sludge_buttons').offsetHeight;
			}

			// if we have guts, they are this high
			var gutsHeight = 0;
			if (document.getElementById('sludge_guts'))
			{
				gutsHeight = document.getElementById('sludge_guts').offsetHeight;
			}

			var ourHeight = menuHeight + buttonsHeight + gutsHeight;

			// set our frame height
			parent.document.getElementById(id).style.height = ourHeight + "px";
		}
	
		// otherwise we can expand and do inner-scrolling in our guts
		else
		{
			// the frame starts this far down from the top
			var frameTop = parent.document.getElementById(id).offsetTop;
			
			// the parent needs this footer area reserved at the bottom
			var footerHeight = 0;
			if (parent.document.getElementById('footer'))
			{
				footerHeight = parent.document.getElementById('footer').offsetHeight + 50;
			}
	
			// we can extend down from our top to the bottom, leaving room for the footer
			var newHeight = windowHeight - frameTop - footerHeight;
			if (newHeight < 1) newHeight = 1;
	
			// set our frame height
			parent.document.getElementById(id).style.height = newHeight + "px";

			// adjust the guts of our display to fill it between the menu and buttons
			setGutsHeight(newHeight);
		}
	}
	
	// otherwise just adjust the guts of our display to fill it between the menu and buttons
	else
	{
		setGutsHeight(window.innerHeight);
	}
}

function setGutsHeight(frameHeight)
{
	// if we have a menu, it is this high
	var menuHeight  = 0;
	if (document.getElementById('sludge_menu'))
	{
		menuHeight = document.getElementById('sludge_menu').offsetHeight;
	}
	
	// if we have buttons, they are this high
	var buttonsHeight = 0;
	if (document.getElementById('sludge_buttons'))
	{
		buttonsHeight = document.getElementById('sludge_buttons').offsetHeight;
	}

	// the guts can then fill the frame
	var gutsHeight = frameHeight - menuHeight - buttonsHeight;
	if (gutsHeight < 1) gutsHeight = 1;

	// resize the guts and reset its scroll
	if (document.getElementById('sludge_guts'))
	{
		document.getElementById('sludge_guts').style.height=gutsHeight + "px";
		document.getElementById('sludge_guts').scrollTop=0;
	}
}

function trim(s)
{
	return s.replace(/^\s+/g, "").replace(/\s+$/g, "");
}
