/**********************************************************************************
* $HeadURL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/jsf/component/RichTextEditArea.java $
* $Id: RichTextEditArea.java 226 2005-06-23 23:46:26Z esmiley@stanford.edu $
***********************************************************************************
*
 * Copyright (c) 2005 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

var courierRunning = false;

var focus_path;

var ignoreCourier = false;
var doubleDeep = false;

function openWindow(url, title, options)
{
	var win = top.window.open(url, title, options);
	win.focus();
	return win;
}

function sitehelp(whereto)
{
	umcthelp=window.open(whereto,'umcthelpWindow','toolbar=yes,scrollbars=yes,resizable=yes,menubar=no,status=yes,directories=no,location=no,width=600,height=400')
}

function hideElement(hideMe)
{
	if (hideMe != 'none')
	{
		var menuItem = document.getElementById(hideMe);
		if(menuItem != null)
			menuItem.style.display = 'none';
	}
	return true;
}

/* %%% currently disabled, used for paitence display -ggolden
function showSubmitMessage()
{
	var submitDiv = document.getElementById('SubmitMessage');
	var normalDiv = document.getElementById('chefPortletContainer')
	if (submitDiv != null)
	{
		submitDiv.style.display = '';
		if (normalDiv != null)
			normalDiv.style.display = 'none';
	}
	return true;
}
*/

// PLEASE SEE http://www.worldtimzone.com/res/encode/
// before modifying any of this UTF-8 encoding JavaScript!
/* ***************************
** Most of this code was kindly
** provided to me by
** Andrew Clover (and at doxdesk dot com)
** http://and.doxdesk.com/
** in response to my plea in my blog at
** http://worldtimzone.com/blog/date/2002/09/24
** It was unclear whether he created it.
*/
// Uses the JavaScript built-in function encodeURIComponent()
// which properly encodes UTF-8.  If the function isn't available,
// emulates the function (older browsers like IE 5.0)
function myEscape(value)
{
	if (value == "") return "";
	if (typeof encodeURIComponent == "function")
	{
		// Use JavaScript built-in function
		// IE 5.5+ and Netscape 6+ and Mozilla
		return encodeURIComponent(value);
	}
	else
	{
		// Need to mimic the JavaScript version
		// Netscape 4 and IE 4 and IE 5.0
		return encodeURIComponentEmulated(value);
	}
}

function encodeURIComponentEmulated(s) {
  var s = utf8(s);
  var c;
  var enc = "";
  for (var i= 0; i<s.length; i++) {
    if (okURIchars.indexOf(s.charAt(i))==-1)
      enc += "%"+toHex(s.charCodeAt(i));
    else
      enc += s.charAt(i);
  }
  return enc;
}


function utf8(wide) {
  var c, s;
  var enc = "";
  var i = 0;
  while(i<wide.length) {
    c= wide.charCodeAt(i++);
    // handle UTF-16 surrogates
    if (c>=0xDC00 && c<0xE000) continue;
    if (c>=0xD800 && c<0xDC00) {
      if (i>=wide.length) continue;
      s= wide.charCodeAt(i++);
      if (s<0xDC00 || c>=0xDE00) continue;
      c= ((c-0xD800)<<10)+(s-0xDC00)+0x10000;
    }
    // output value
    if (c<0x80) enc += String.fromCharCode(c);
    else if (c<0x800) enc += String.fromCharCode(0xC0+(c>>6),0x80+(c&0x3F));
    else if (c<0x10000) enc += String.fromCharCode(0xE0+(c>>12),0x80+(c>>6&0x3F),0x80+(c&0x3F));
    else enc += String.fromCharCode(0xF0+(c>>18),0x80+(c>>12&0x3F),0x80+(c>>6&0x3F),0x80+(c&0x3F));
  }
  return enc;
}

var hexchars = "0123456789ABCDEF";

function toHex(n) {
  return hexchars.charAt(n>>4)+hexchars.charAt(n & 0xF);
}

var okURIchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";

function buildQueryString(theFormName)
{
	theForm = document.forms[theFormName];
	var qs = '';
	for (var e=0; e<theForm.elements.length; e++)
	{
		var element = theForm.elements[e];
		if ((element.name) && (element.type) && (element.value))
		{
			var checkRadio = ((element.type == "checkbox") || (element.type == "radio"));
			if ((element.name != '') && (element.name.indexOf("eventSubmit_") == -1))
			{
				if ( !checkRadio || element.checked)
				{
					qs += '&' + myEscape(element.name) + '=' + myEscape(element.value);
				}
			}
		}
	}
	return qs;
}

// use if peer w/ courier, both frames in "top" parent
function updCourier(dd, ic)
{
	if (ic) return;

	if (dd)
	{
		parent.updCourier(false, false);
		return;
	}

	if ((!courierRunning) && (window.courier) && (window.courier.location.toString().length > 1))
	{
		courierRunning = true;
		window.courier.location.replace(window.courier.location);
	}
}

function formSubmitOnEnter(field, event)
{
	var keycode;
	if (window.event)
	{
		keycode = window.event.keyCode;
	}
	else
	{
		keycode = event.which ? event.which : event.keyCode
	}

	if (keycode == 13)
	{
		field.form.submit();
		return false;
	}

	return true;
}

// click an element id = element
function clickOnEnter(event, element)
{
	var keycode;
	if (window.event)
	{
		keycode = window.event.keyCode;
	}
	else
	{
		keycode = event.which ? event.which : event.keyCode
	}

	if (keycode == 13)
	{
		if (document.getElementById(element) && document.getElementById(element).click)
		{
			document.getElementById(element).click();
		}
		return false;
	}

	return true;
}

// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function setMainFrameHeight(id)
{
// run the script only if this window's name matches the id parameter
// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var obj = parent.document.getElementById(id);
	if (obj)
	{
// reset the scroll
		parent.window.scrollTo(0,0);

// to get a good reading from some browsers (ie?) set the height small
		obj.style.height="50px";

		var height = document.body.offsetHeight;
// here's the detection of ie/xp
		if ((height == document.body.clientHeight) && (document.body.scrollHeight))
		{
			height = document.body.scrollHeight;
		}
// here we fudge to get a little bigger
		height = height + 50;
// no need to be smaller than...
//		if (height < 200) height = 200;
		obj.style.height=height + "px";
	}
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
}

/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/webapp/js/sakai_tool_headscripts.js,v 1.1 2005/04/14 18:37:07 daisyf.stanford.edu Exp $
*
**********************************************************************************/
