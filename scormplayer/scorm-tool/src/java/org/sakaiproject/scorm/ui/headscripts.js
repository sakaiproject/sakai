/**********************************************************************************
 * $URL:  $
 * $Id:  $
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
// set the parent iframe's height to hold our entire contents
function setMainFrameHeight(id)
{
	// some browsers need a moment to finish rendering so the height and scroll are correct
	setTimeout("setMainFrameHeightNow('"+id+"')",1);
}

function setMainFrameHeightNow(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{
		// reset the scroll
		parent.window.scrollTo(0,0);

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 		
		var offsetH = document.body.offsetHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}
	
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

		// here we fudge to get a little bigger
		var newHeight = height + 10;

		// but not too big!
		if (newHeight > 32760) newHeight = 32760;

		// capture my current scroll position
		var scroll = findScroll();

		// resize parent frame (this resets the scroll as well)
		objToResize.height=newHeight + "px";

		// reset the scroll, unless it was y=0)
		if (scroll[1] > 0)
		{
			var position = findPosition(frame);
			parent.window.scrollTo(position[0]+scroll[0], position[1]+scroll[1]);
		}
	}
}

// find the object position in its window
// inspired by http://www.quirksmode.org/js/findpos.html
function findPosition(obj)
{
	var x = 0;
	var y = 0;
	if (obj.offsetParent)
	{
		x = obj.offsetLeft;
		y = obj.offsetTop;
		while (obj = obj.offsetParent)
		{
			x += obj.offsetLeft;
			y += obj.offsetTop;
		}
	}
	return [x,y];
}

// find my scroll
// inspired by http://www.quirksmode.org/viewport/compatibility.html
function findScroll()
{
	var x = 0;
	var y = 0;
	if (self.pageYOffset)
	{
		x = self.pageXOffset;
		y = self.pageYOffset;
	}
	else if (document.documentElement && document.documentElement.scrollTop)
	{
		x = document.documentElement.scrollLeft;
		y = document.documentElement.scrollTop;
	}
	else if (document.body)
	{
		x = document.body.scrollLeft;
		y = document.body.scrollTop;
	}
	
	return [x,y];
}

// find the first form's first text field and place the cursor there
function firstFocus()
{
	if (document.forms.length > 0)
	{
		var f = document.forms[0];
		for (var i=0; i<f.length; i++)
		{
			var e = f.elements[i];
			if ((e.name) && (e.type) && ((e.type=='text') || (e.type=='textarea')) && (!e.disabled))
			{
				e.focus();
				break;
			}
		}
	}
}

// set focus on a particular element in a page.
// the path to that element is given by a series of element id's in an array (param "elements")
// which should be the id's of zero or more frames followed by the id of the element.
function setFocus(elements)
{
	if(typeof elements == "undefined")
	{
		return;
	}

	var focal_point = document;
	for(var i = 0; i < elements.length; i++)
	{
		if(focal_point.getElementById(elements[i]))
		{
			focal_point = focal_point.getElementById(elements[i]);
		}
		
		if(focal_point.contentDocument)
		{
			focal_point = focal_point.contentDocument;
		}
		else if(focal_point.contentWindow)
		{
			focal_point = focal_point.contentWindow;
			if(focal_point.document)
			{
				focal_point = focal_point.document;
			}
		}
		else
		{
			break;
		}
	}
	
	if(focal_point && focal_point.focus)
	{
		focal_point.focus();
	}
}

// return the url with auto=courier appended, sensitive to ? already in there or not
function addAuto(loc)
{
	var str = loc.toString();

	// not if already there
	if (str.indexOf("auto=courier") != -1) return str;
	
	if (str.indexOf("?") != -1)
	{
		// has a ?
		return str + '&auto=courier';
	}
	else
	{
		// has no ?
		return str + '?auto=courier';
	}
}

function showNotif(item, button,formName)
{
	if (button !="noBlock")
	{
		eval("document." + formName + "." + button + ".disabled=true")
	}
	if (item !="noNotif")
	{
		var browserType;
		if (document.all) {browserType = "ie"}
		if (window.navigator.userAgent.toLowerCase().match("gecko")) {browserType= "gecko"}
		if (browserType == "gecko" )
			document.showItem = eval('document.getElementById(item)');
		else if (browserType == "ie")
			document.showItem = eval('document.all[item]');
		else
			document.showItem = eval('document.layers[item]');

			document.showItem.style.visibility = "visible";
	}
	
	for (var i=0;i<document.getElementsByTagName("input").length; i++) 
	{
		if (document.getElementsByTagName("input").item(i).className == "disableme")
		{
			document.getElementsByTagName("input").item(i).disabled = "disabled";
		}
	}		
}