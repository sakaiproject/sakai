/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-util/util/src/java/org/sakaiproject/util/PresenceObservingCourier.java $
 * $Id: PresenceObservingCourier.java 8204 2006-04-24 19:35:57Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

// add a message to the chat list from chef_chat-List.vm 
function appendMessage(uname, uid, removeable, pdate, ptime, msg, msgId)
{
	var undefined;
	var position = 100000, docheight = 0, frameheight = 300;	  
	var transcript = (document.all) ? document.all.transcript : document.getElementById("chatList");
	
	// compose the time/date according to user preferences for this session
	var msgTime = "";
	if(window.display_date && window.display_time)
	{
		msgTime = " (" + pdate + " " + ptime + ")" ;
	}
	else if (window.display_date)
	{
		msgTime = " (" + pdate + ")" ;
	}
	else if(window.display_time)
	{
		msgTime = " (" + ptime + ")" ;
	}
	
	// find the user's current scroll-position within the chat log
	if(navigator.appName == "Microsoft Internet Explorer" && navigator.userAgent.indexOf("Win") > -1 )
	{
		// WIN_IE 
		position = document.documentElement.scrollTop;
	}
	else if(navigator.appName == "Microsoft Internet Explorer" && navigator.userAgent.indexOf("Mac") > -1 )
	{
		// MAC_IE 
		position = document.body.scrollTop ;
	}
	else if(window.pageYOffset != undefined)
	{
		// WIN_MZ 
		// WIN_NN
		position = window.pageYOffset;
	}
	else if(document.documentElement)
	{
		position = document.documentElement.scrollTop;
	}
	else if(document.body.scrollTop != undefined)
	{
		position = document.body.scrollTop;
	}
	
	// find the height of the frame containing the chat log
	if(navigator.appName == "Microsoft Internet Explorer" && navigator.userAgent.indexOf("Win") > -1 )
	{
		// WIN_IE
		if(document.documentElement && document.documentElement.clientHeight)
		{
			frameheight = document.documentElement.clientHeight;
		}
		else if(window.contentDocument && window.contentDocument.clientHeight)
		{
			frameheight = window.contentDocument.clientHeight;
		}
	}
	else if(navigator.appName == "Microsoft Internet Explorer" && navigator.userAgent.indexOf("Mac") > -1 )
	{
		// MAC_IE
		frameheight = document.body.clientHeight ;
	}
	else if(window.innerHeight != undefined)
	{
		// WIN_MZ 
		// WIN_NN
		frameheight = window.innerHeight;
	}
	else if(document.body.parentNode != undefined && document.body.parentNode.clientHeight != undefined)
	{
		frameheight = document.body.parentNode.clientHeight;
	}
	
	// find the overall size (height) of the chat log
	if(document.body.offsetHeight)
	{
		// MAC_IE
		// WIN_IE
		// WIN_MZ
		// WIN_NN
		docheight = document.body.offsetHeight;
	}
	else if(document.offsetHeight)
	{
		docheight = document.offsetHeight;
	}
	else if(document.height)
	{
		docheight = document.height;
	}
	
	var newDiv = document.createElement('li');
	var color = ColorMap[uid];
	if(color == null)
	{
		color = Colors[nextColor++];
		ColorMap[uid] = color;
		if(nextColor >= numColors)
		{
			nextColor = 0;
		}
	}
	
	var deleteHtml = "";
	if (removeable == "true")
	{
		deleteHtml = 
			" <a href=\"#\" onclick=\"location='" + deleteUrl + msgId + "'; return false;\" title=\"" + deleteMsg + "\" >" +
			"<img src=\"/library/image/sakai/delete.gif\" border=\"0\" alt=\"" + deleteMsg + "\" /></a>";
	}

	newDiv.innerHTML = '<span style="color: ' + color + '">' + uname + '</span>'
		+ deleteHtml
		+ '<span class="chatDate">' + msgTime + '</span> '
		+ msg;
	transcript.appendChild(newDiv);

	// adjust scroll
	if(position >= docheight - frameheight)
	{
		if(document.body.offsetHeight)
		{
			// MAC_IE
			// WIN_IE
			// WIN_MZ
			// WIN_NN
			position = document.body.offsetHeight;
		}
		else if(document.offsetHeight)
		{
			position = document.offsetHeight;
		}
		else if(document.height)
		{
			position = document.height;
		}
	}
	window.scrollTo(0, position);
}
