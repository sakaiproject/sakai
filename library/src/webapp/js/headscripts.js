/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation
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
 
var focus_path;

var doubleDeep = false;

function inIframe () {
	try {
		return window.self !== window.top;
	} catch (e) {
		return true;
	}
}

function openWindow(url, name, options)
{
	var win = top.window.open(url, name.replace(/[ -]+/g, ''), options);
	win.focus();
	return win;
}

function sitehelp(whereto) 
{
	umcthelp=window.open(whereto,'umcthelpWindow','toolbar=yes,scrollbars=yes,resizable=yes,menubar=no,status=yes,directories=no,location=no,width=600,height=400');
}

function hideElement(hideMe)
{
	if (hideMe !== 'none')
	{
		var menuItem = document.getElementById(hideMe);
		if(menuItem !== null)
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
	if (value === "") return "";
	if (typeof encodeURIComponent === "function")
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
    if (okURIchars.indexOf(s.charAt(i))===-1)
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
			var checkRadio = ((element.type === "checkbox") || (element.type === "radio"));
			if ((element.name !== '') && (element.name.indexOf("eventSubmit_") === -1))
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

function formSubmitOnEnter(field, event)
{
	var keycode;
	if (window.event)
	{
		keycode = window.event.keyCode;
	}
	else
	{
		keycode = event.which ? event.which : event.keyCode;
	}

	if (keycode === 13)
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
		keycode = event.which ? event.which : event.keyCode;
	}

	if (keycode === 13)
	{
		if (document.getElementById(element) && document.getElementById(element).click)
		{
			document.getElementById(element).click();
		}
		return false;
	}
	
	return true;
}

// De-bounce the resize activity
var MainFrameHeightTimeOut = false;

// set the parent iframe's height to hold our entire contents
// pass -1 for no max
function setMainFrameHeightWithMax(id, maxHeight)
{
	if ( inIframe() ) {
	    // some browsers need a moment to finish rendering so the height and scroll are correct
	    if ( MainFrameHeightTimeOut ) {
		    clearTimeout(MainFrameHeightTimeOut);
		    MainFrameHeightTimeOut = false;
	    }
	    MainFrameHeightTimeOut = setTimeout( function() { setMainFrameHeightNow(id, maxHeight); }, 1000);
    }
}

function setMainFrameHeight(id)
{
	setMainFrameHeightWithMax(id, 32760);  // original default max height for resizing the frame, reason unknown
}

// pass -1 for no max
function setMainFrameHeightNow(id, maxHeight)
{
	// If we have been inlined, do nothing
	if ( ! inIframe() ) return;

	// if this window's name matches the id parameter, we on same origin
	// If they do not match we are in cross origin and so we need to use
	// postMessage() to inform our parent frame
	if (typeof window.name !== "undefined" && id !== window.name) {
		var height = getDocumentHeight();
		try {
			parent.postMessage(JSON.stringify({
				subject: "lti.frameResize",
				height: height,
				windowid: id
			}), "*");
			console.log("lti.frameResize postMessage sent height="+height);
		}
		catch (error)
		{
			console.log("lti.frameResize postMessage failed height="+height);
		}
		return;
	}

	//SAK-21209 check we can access the document, 
	//ie this could be a LTI request and therefore we are not allowed
	try {
		var frame = parent.document.getElementById(id);
	} catch (e) {
		return;
	}
	
	if (frame)
	{
		var objToResize = (frame.style) ? frame.style : frame;
  
    // SAK-11014 revert           if ( false ) {

		var height; 		
		var offsetH = document.body.offsetHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) !== 'undefined' || typeof(frame.contentWindow) !== 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc !== null) ? innerDoc.body.scrollHeight : null;
		}
	
		if (document.all && innerDocScrollH !== null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}
   // SAK-11014 revert		} 

   // SAK-11014 revert             var height = getFrameHeight(frame);

		// here we fudge to get a little bigger
		var newHeight = height + 40;

		// but not too big!
		if (maxHeight > -1 && newHeight > maxHeight) newHeight = maxHeight;

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

// optional hook triggered after the head script fires.

		if (parent.postIframeResize){ 
			parent.postIframeResize(id);
		}
	}
}

/* get height of an iframe document */
function getFrameHeight (frame)
{
   var document = frame.contentWindow.document;
   var doc_height = document.height ? document.height : 0; // Safari uses document.height

if (document.documentElement && document.documentElement.scrollHeight) /* Strict mode */
      return Math.max (document.documentElement.scrollHeight, doc_height);
   else /* quirks mode */
      return Math.max (document.body.scrollHeight, doc_height);
}

/* get height of our window without looking "up" */
function getDocumentHeight ()
{
   var doc_height = document.height ? document.height : 0; // Safari uses document.height

   if (document.documentElement && document.documentElement.scrollHeight) /* Strict mode */
      return Math.max (document.documentElement.scrollHeight, doc_height);
   else /* quirks mode */
      return Math.max (document.body.scrollHeight, doc_height);
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
			if ((e.name) && (e.type) && ((e.type==='text') || (e.type==='textarea')) && (!e.disabled))
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
	if(typeof elements === "undefined")
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

function showNotif(item, button, formName)
{
	if (button !== "noBlock")
	{
		// Replace eval with direct DOM access
		const form = document.forms[formName];
		if (form && form.elements[button]) {
			form.elements[button].disabled = true;
		}
	}
	if (item !== "noNotif")
	{
		// SAK-21041 simplified to use getElementById
		document.showItem = document.getElementById(item);
		if (document.showItem) {
			document.showItem.style.visibility = "visible";
		}
	}
	
	for (var i=0; i<document.getElementsByTagName("input").length; i++) 
	{
		if (document.getElementsByTagName("input").item(i).className === "disableme")
		{
			document.getElementsByTagName("input").item(i).disabled = "disabled";
		}
	}		
}

// stuff to do auto-update using the XMLHttpRequest object
var updateReq = null;
var updateTime = 0;
var updateUrl = "";
var updateWaiting = 0;

function loadXMLDoc(url)
{
	// branch for native XMLHttpRequest object
	if (window.XMLHttpRequest)
	{
		updateReq = new XMLHttpRequest();
		updateReq.onreadystatechange = processReqChange;
		// adjust the url with a unique (time based for sequence, plus random for multi user) value to disable caching
		// and the auto parameter to indicate this is not user activity
		updateReq.open("GET", url + "&auto=true&unq=" + new Date().getTime() + "-" +  Math.random(), true);
		updateReq.send(null);
	} else {
		console.log('XMLHttpRequest not supported. loadXMLDoc(' + url + ') failed.');
	}
}

function processReqChange()
{
	try
	{
		if (updateReq.readyState === 4)
		{
			evalString = updateReq.responseText;
			// alert(updateReq.readyState + " : " + updateReq.status + " : " + evalString);
			try
			{
				eval(evalString);
			}
			catch (err)
			{
				// alert(err);
			}
			swapUpdateIndicator();

			updateReq = null;
			scheduleUpdate();
		}
	}
	catch (error)
	{
		// alert(error);
	}
}

function swapUpdateIndicator()
{
	try
	{
		if (document.getElementById('update_a').style.display==='none')
		{
			document.getElementById('update_a').style.display='block';
			document.getElementById('update_b').style.display='none';
		}
		else
		{
			document.getElementById('update_a').style.display='none';
			document.getElementById('update_b').style.display='block';
		}
	}
	catch (error)
	{
//		alert(error);
	}
}

function checkForUpdate()
{
	updateWaiting = 0;
	if ((updateUrl !== "") && (updateReq === null))
	{
		loadXMLDoc(updateUrl);
	}
}

function scheduleUpdate()
{
	if ((updateTime > 0) && (updateWaiting === 0))
	{
		updateWaiting = setTimeout('checkForUpdate()', updateTime);
	}
}

function updateNow()
{
	if (updateWaiting !== 0)
	{
		clearTimeout(updateWaiting);
	}
	checkForUpdate();
}

function portalWindowRefresh(url)
{
	if (typeof(sakaiPortalWindow) !== "undefined")
	{
		location.replace(url);
	}
	else if (parent && (typeof(parent.sakaiPortalWindow) !== "undefined"))
	{
		parent.location.replace(url);
	}
	else if (parent && parent.parent && (typeof(parent.parent.sakaiPortalWindow) !== "undefined"))
	{
		parent.parent.location.replace(url);
	}
	else if (parent && parent.parent && parent.parent.parent && (typeof(parent.parent.parent.sakaiPortalWindow) !== "undefined"))
	{
		parent.parent.parent.location.replace(url);
	}
	else
	{
		location.replace(url);
	}
}

function privacy_show_popup(id){
	el = document.getElementById("privacy_tool_popup");
	if(el){el.style.display='block';}
	overlaydiv = document.createElement("div");
	overlaydiv.id = "privacy_overlay";
	document.body.appendChild(overlaydiv);
}

function privacy_hide_popup(){
   if(document.getElementById("privacy_overlay")){document.body.removeChild(document.getElementById("privacy_overlay"));}
   if(document.getElementById("privacy_tool_popup")){document.getElementById("privacy_tool_popup").style.display="none";}
}

function browserSafeDocHeight() {
	docHeight = Math.max(document.body.scrollHeight,document.body.offsetHeight) ;
	if (window.innerHeight) {  // all except Explorer
		winHeight = window.innerHeight;
	} else if (document.documentElement && document.documentElement.clientHeight) { // Explorer 6 Strict Mode
		winHeight = document.documentElement.clientHeight;
	} else if (document.body) { // other Explorers
		winHeight =  document.body.clientHeight;
	}
	return Math.max(winHeight,docHeight); 
}

function supports_history_api() {
	return !!(window.history && history.pushState);
}
//Call this to disable the back button in a page context - SAK-23247
function disableBackButton(message) {
	if (supports_history_api()) {
		history.pushState(null, null, '');
		window.addEventListener('popstate', function(event) {
			// If there is a hash in the URL don't do anything.
			// These are used in a11y jumps and on some Samigo features
			if (window.location.hash) {
				return;
			}
			history.pushState(null, null, '');
			window.alert(message);
		});
	}
}

// Load the latest JQuery, compatibility library, BootStrap, and UI - mimic functionality in PortalUtils
function includeLatestJQuery(where) {
	var psp = "/library/js/";
	var webjars = "/library/webjars/";
	var ver = "";
	if ( typeof portal !== 'undefined' ) {
		if (portal.pageScriptPath) psp = portal.pageScriptPath;
		if (portal.pageWebjarsPath) webjars = portal.pageWebjarsPath;
		if (portal.portalCDNQuery) ver = portal.portalCDNQuery;
	}

	if ( window.jQuery ) {
		console.debug('jQuery already loaded '+jQuery.fn.jquery+' in '+where);
		if (typeof jQuery.migrateWarnings == 'undefined') { 
			document.write('\x3Cscript src="'+webjars+'jquery-migrate/1.4.1/jquery-migrate.min.js'+ver+'">'+'\x3C/script>');
			console.debug('Adding jQuery migrate');
		}
		if (typeof jQuery.ui == 'undefined') {
			document.write('\x3Cscript src="'+webjars+'jquery-ui/1.12.1/jquery-ui.min.js'+ver+'">'+'\x3C/script>');
			document.write('\x3Clink rel="stylesheet" href="'+webjars+'jquery-ui/1.12.1/jquery-ui.min.css'+ver+'"/>');
			console.debug('Adding jQuery UI');
		}
	} else {
		document.write('\x3Cscript src="'+webjars+'jquery/1.12.4/jquery.min.js'+ver+'">'+'\x3C/script>');
		document.write('\x3Cscript src="'+webjars+'jquery-migrate/1.4.1/jquery-migrate.min.js'+ver+'">'+'\x3C/script>');
		document.write('\x3Cscript src="'+webjars+'jquery-ui/1.12.1/jquery-ui.min.js'+ver+'">'+'\x3C/script>');
		document.write('\x3Clink rel="stylesheet" href="'+webjars+'jquery-ui/1.12.1/jquery-ui.min.css'+ver+'"/>');
		console.debug(`jQuery+migrate+UI Loaded by ${where} from ${webjars}`);
	}
}

function includeWebjarLibrary(library, options = {}) {
	let webjars = (window.portal && window.portal.pageWebjarsPath) ? window.portal.pageWebjarsPath : '/library/webjars';
	let ver = (window.portal && window.portal.portalCDNQuery) ? window.portal.portalCDNQuery : '';
	let libraryVersion = '';
	const jsReferences = [];
	const cssReferences = [];

	// Include the CSS if requested
	const includeCss = options.includeCss || false;

	switch (library) {
		case 'bootstrap':
			libraryVersion = "5.2.0";
			jsReferences.push('/js/bootstrap.bundle.min.js');
			includeCss && cssReferences.push('/css/bootstrap.min.css');
			break;
		case 'bootstrap-multiselect':
			libraryVersion = "1.1.1";
			jsReferences.push('/js/bootstrap-multiselect.js');
			cssReferences.push('/css/bootstrap-multiselect.css');
			break;
		case 'jquery.tablesorter':
			libraryVersion = "2.27.7";
			jsReferences.push('/dist/js/jquery.tablesorter.combined.min.js');
			jsReferences.push('/dist/js/extras/jquery.tablesorter.pager.min.js');
			jsReferences.push('/dist/js/extras/jquery.metadata.min.js');
			cssReferences.push('/dist/css/theme.jui.min.css');
			cssReferences.push('/dist/css/jquery.tablesorter.pager.min.css');
			break;
		case 'featherlight':
			libraryVersion = "1.7.14";
			jsReferences.push('/release/featherlight.min.js');
			cssReferences.push('/release/featherlight.min.css');
			break;
		case 'momentjs':
			libraryVersion = "2.29.4";
			jsReferences.push('/min/moment-with-locales.min.js');
			break;
		case 'dropzone':
			libraryVersion = "5.9.3";
			jsReferences.push('/dist/min/dropzone.min.js');
			cssReferences.push('/dist/min/dropzone.min.css');
			break;
		case 'select2':
			libraryVersion = "4.0.13";
			jsReferences.push('/js/select2.full.min.js');
			cssReferences.push('/css/select2.min.css');
			break;
		case 'datatables':
			libraryVersion = "1.10.25";
			jsReferences.push('/js/jquery.dataTables.min.js');
			jsReferences.push('/js/dataTables.bootstrap5.min.js');
			cssReferences.push('/css/dataTables.bootstrap5.min.css');
			break;
		case 'datatables-plugins':
			libraryVersion = "1.13.1";
			// any-number plugin
			jsReferences.push('/sorting/any-number.js');
			break;
		case 'datatables-rowgroup':
			libraryVersion = "1.1.3";
			// This webjar has a different convention without version and library name.
			document.write(`<script src="${webjars}/datatables.net-rowgroup/js/dataTables.rowGroup.min.js${ver}"></script>`);
			break;
		case 'ckeditor4':
			libraryVersion = "4.22.1";
			jsReferences.push('/ckeditor.js');
			break;
		case 'awesomplete':
			libraryVersion = "1.1.5";
			jsReferences.push('/awesomplete.min.js');
			cssReferences.push('/awesomplete.css');
			break;
		case 'mathjs':
			libraryVersion = "9.4.4";
			jsReferences.push('/lib/browser/math.js');
			break;
		case 'handlebars':
			libraryVersion = "4.4.0";
			jsReferences.push('/handlebars.runtime.min.js');
			break;
		case 'qtip2':
			libraryVersion = "3.0.3-1";
			jsReferences.push('/jquery.qtip.min.js');
			cssReferences.push('/jquery.qtip.min.css');
			break;
		case 'jstree':
			libraryVersion = "3.3.11";
			jsReferences.push('/jstree.min.js');
			cssReferences.push('/themes/default/style.min.css');
			break;
		case 'multiselect-two-sides':
			libraryVersion = "2.5.5";
			jsReferences.push('/dist/js/multiselect.min.js');
			break;
		case 'fontawesome-iconpicker':
			libraryVersion = "1.4.1";
			jsReferences.push('/dist/js/fontawesome-iconpicker.min.js');
			cssReferences.push('/dist/css/fontawesome-iconpicker.min.css');
			break;
		case 'fullcalendar':
			libraryVersion = "5.10.2";
			jsReferences.push('/main.min.js');
			jsReferences.push('/locales-all.min.js');
			cssReferences.push('/main.min.css');
			break;
		case 'recordrtc':
			libraryVersion = "5.6.2";
			jsReferences.push('/RecordRTC.js');
			break;
		case 'webrtc-adapter':
			libraryVersion = "8.0.0";
			jsReferences.push('/out/adapter.js');
			break;
		case 'video.js':
			libraryVersion = "7.14.0";
			jsReferences.push('/dist/video.min.js');
			cssReferences.push('/dist/video-js.css');
			break;
		case 'wavesurfer.js':
			libraryVersion = "5.1.0";
			jsReferences.push('/dist/wavesurfer.min.js');
			break;
		case 'multifile':
			libraryVersion = "2.2.2";
			jsReferences.push('/jquery.MultiFile.min.js');
			break;
		default:
			if (library.endsWith(".js")) {
				document.write('\x3Cscript src="' + webjars + '/' + library + ver + '">' + '\x3C/script>');
			} else if (library.endsWith(".css")) {
				document.write('\x3Clink rel="stylesheet" type="text/css" href="' + webjars + '/' + library + ver + '" />');
			}
	}

	const frameContext = (window.top === window.self) ? "top" : "iframe";
	const frameName = window.frameElement?.name || window.name || "unnamed";
	window.console && console.log(`Adding webjar library ${library}, version ${libraryVersion} [${frameContext}:${frameName}]`);

	// Add all the library references to the DOM.
	jsReferences.forEach( (jsReference) => document.write(`<script src="${webjars}/${library}/${libraryVersion}${jsReference}${ver}"></script>`));
	cssReferences.forEach( (cssReference) => document.write(`<link rel="stylesheet" href="${webjars}/${library}/${libraryVersion}${cssReference}${ver}"></link>`));

}

// Ensures consistent theming across all Sakai pages by dynamically loading a theme
// switcher script, which applies a user or system-preferred theme class to the document
if (!window.themeClassInit) {
	window.themeClassInit = true;
	document.addEventListener('DOMContentLoaded', () => {
		if (window.top === window.self && ![...document.documentElement.classList].some(c => c.startsWith('sakaiUserTheme-'))) {
			const script = document.createElement('script');
			script.src = '/library/js/portal/portal.theme.switcher.js';
			script.onload = async () => {
				try {
					portal.addCssClassToMarkup(await portal.getCurrentSetTheme());
				} catch (error) {
					console.error('Theme error:', error);
				}
			};
			script.onerror = () => console.error('Failed to load script');
			document.head.appendChild(script);
		}
	});
}

// Return the breakpoint between small and medium sized displays - for morpheus currently the same
function portalSmallBreakPoint() { return 800; } 
function portalMediumBreakPoint() { return 800; } 

// A function to add an icon picker to a text input field
function fontawesome_icon_picker(selector) {
	// Set the input's placeholder value
	$(selector).attr('placeholder', 'Pick an icon...');
	// Set the input to read only
	$(selector).prop('readonly', true);
	// Add the class to make this a form control
	$(selector).addClass('form-control icp icp-auto');
	// Add an input group to the parent to enable the preview icon
	$(selector).parent().addClass("input-group");
	// Add the preview icon
	$(selector).before('<span class="input-group-addon"></span>');
	// Enable the iconpicker
	$(selector).iconpicker({
		'hideOnSelect' : true, 
		'collision': true
	});
	$(selector).parent().on('iconpickerShown', function(event) {
		// Focus on the popover window since this attachs to the input-group
		event.iconpickerInstance.popover.find('input').focus()
	});
}

// Return the correct width for a modal dialog.
function modalDialogWidth() {
	var wWidth = $(window).width();
	var pbr = portalSmallBreakPoint();
	var dWidth = wWidth * 0.8;
	if ( wWidth <= pbr ) { 
		dWidth = pbr * 0.8;
		if ( dWidth > (wWidth * 0.95) ) {
			dWidth = wWidth * 0.95;
		}
	}
	if ( dWidth < 300 ) dWidth = 300; // Should not happen
	return Math.round(dWidth);
}
//
// Return the correct height for a modal dialog.
function modalDialogHeight() {
	var wHeight = $(window).height();
	var dHeight = wHeight * 0.8;
	if ( dHeight < 300 ) dHeight = 300; // Should not happen
	return Math.round(dHeight);
}

// Figure out the maximum z-index
// http://stackoverflow.com/questions/1118198/how-can-you-figure-out-the-highest-z-index-in-your-document
function maxZIndex(elems)
{
    var maxIndex = 0;
    elems = typeof elems !== 'undefined' ? elems : $("*");

    $(elems).each(function(){
        maxIndex = (parseInt(maxIndex) < parseInt($(this).css('z-index'))) ? parseInt($(this).css('z-index')) : maxIndex;
    });

    return maxIndex;
}

// Adapted from
// https://dev.to/mornir/-how-to-easily-copy-text-to-clipboard-a1a
// Added avoiding the scrolling effect by appending the new input
// tag as a child of a nearby element (the parent element)
// Usage:
// <a href="#" onclick="copyToClipboardNoScroll(this, 'texttocopy');return false;">Copy</a>
// <a href="#" onclick="copyToClipboardNoScroll(this, $('#pass').text());return false;">Copy</a>
// <a href="#" onclick="copyToClipboardNoScroll(this, $('#myInput').val());return false;">Copy</a>
function copyToClipboardNoScroll(parent_element, textToCopy) {
  // 1) Add the text to the DOM (usually achieved with a hidden input field)
  const input = document.createElement('input');

  // 1.5) Move off to the left but inline with the current item to avoid scroll effects
  input.style.position = 'absolute';
  input.style.left = '-1000px';
  parent_element.appendChild(input);
  input.value = textToCopy.trim();

  // 2) Select the text
  input.focus();
  input.select();

  // 3) Copy text to clipboard
  const isSuccessful = document.execCommand('copy');

  // 4) Catch errors
  if (!isSuccessful) {
    console.error('Failed to copy text.');
  }

  // Remove the new input tag
  input.remove();
}

// From tsugiscripts.js
function tsugi_window_close(message)
{
    window.close();
    try { window.open('', '_self').close(); } catch(e) {};
    setTimeout(function(){ console.log("Attempting self.close"); self.close(); }, 1000);
    setTimeout(function(){ console.log("Notifying the user."); alert(message); open("about:blank", '_self').close(); }, 2000);
}

// LTI frame management code shared with tsugi-static/js/tsugiscripts.js
var DE_BOUNCE_LTI_FRAME_RESIZE_TIMER = false;
var DE_BOUNCE_LTI_FRAME_RESIZE_HEIGHT = false;

// Adapted from Lumen Learning / Bracken Mosbacker
// element_id is the id of the frame in the parent document
function lti_frameResize(new_height, element_id) {
    if ( self == top ) return;

    if ( !new_height ) {
        new_height = $(document).height() + 10;
    }
    if ( new_height < 100 ) new_height = 100;
    if ( new_height > 5000 ) new_height = 5000;

    if ( DE_BOUNCE_LTI_FRAME_RESIZE_HEIGHT ) {
        delta = new_height - DE_BOUNCE_LTI_FRAME_RESIZE_HEIGHT;
        if ( new_height == 5000 && DE_BOUNCE_LTI_FRAME_RESIZE_HEIGHT >= 5000 ) {
            console.log("maximum lti_frameResize 5000 exceeded");
            return;
        } else if ( new_height > (DE_BOUNCE_LTI_FRAME_RESIZE_HEIGHT + 10) ) {
            // Do the resize for small increases
        } else if ( new_height < (DE_BOUNCE_LTI_FRAME_RESIZE_HEIGHT - 30) ) {
            // Do the resize for large decreases
        } else {
            console.log("lti_frameResize delta "+delta+" is too small, ignored");
            return;
        }
    }

    if ( DE_BOUNCE_LTI_FRAME_RESIZE_TIMER ) {
        clearTimeout(DE_BOUNCE_LTI_FRAME_RESIZE_TIMER);
        DE_BOUNCE_LTI_FRAME_RESIZE_TIMER = false;
    }

    DE_BOUNCE_LTI_FRAME_RESIZE_TIMER = setTimeout(
        function () { lti_frameResizeNow(new_height, element_id); },
        1000
    );
}

function lti_frameResizeNow(new_height, element_id) {
    parms = {
      subject: "lti.frameResize",
      height: new_height
    }
    if ( element_id ) {
        parms.element_id = element_id;
    }
    var parm_str = JSON.stringify(parms);

    console.log("sending "+parm_str);
    parent.postMessage(parm_str, "*");

    DE_BOUNCE_LTI_FRAME_RESIZE_HEIGHT = new_height;
}
