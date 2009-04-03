/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-util/util/src/java/org/sakaiproject/util/PresenceObservingCourier.java $
 * $Id: PresenceObservingCourier.java 8204 2006-04-24 19:35:57Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation
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

// add a message to the chat list from chef_chat-List.vm 
function appendMessage(uname, uid, removeable, pdate, ptime, pid, msg, msgId)
{
	var undefined;
	var position = 100000, docheight = 0, frameheight = 300;	  
	var transcript = document.getElementById("topForm:chatList");
	
	// compose the time/date according to user preferences for this session
	var msgTime = "";
	if(window.display_date && window.display_time)
	{
		msgTime = " (" + pdate + " " + ptime + ") " ;
	}
	else if (window.display_date)
	{
		msgTime = " (" + pdate + ") " ;
	}
	else if(window.display_time)
	{
		msgTime = " (" + ptime + ") " ;
	}
	else if(window.display_id)
	{
		msgTime = " (" + pid + ") " ;
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
		newComponentId = $(transcript).children("li").size();
		var builtId = "topForm:chatList:" + newComponentId + ":deleteMessage";
		var tmpdeleteUrl = deleteUrl + msgId;
		deleteHtml = 
			" <a id=\"" + builtId + "\" href=\"#\" onclick=\"location.href='" + tmpdeleteUrl + "'\" title=\"" + deleteMsg + "\" >" +
			"<img src=\"/library/image/sakai/delete.gif\" border=\"0\" alt=\"" + deleteMsg + "\" /></a>";
	}

	newDiv.innerHTML = '<span style="color: ' + color + '">' + uname + '</span>'
		+ deleteHtml
		+ '<span class="chatDate">' + msgTime + '</span>'
		+ msg;
	transcript.appendChild(newDiv);

	// adjust scroll
	var objDiv = document.getElementById("Monitor");
   objDiv.scrollTop = objDiv.scrollHeight;

}

//fix for double click stack traces in IE
doubleclick_disabler = function(){
	this.form.submit();
	this.onclick="return false;";
}
$(document).ready(function(){
   $("input[@type=button], input[@type=submit]").bind("click", doubleclick_disabler);
});
