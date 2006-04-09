/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/js/chatscript.js,v 1.1 2005/04/12 17:44:21 ggolden.umich.edu Exp $
*
***********************************************************************************
@license@
**********************************************************************************/

// 
// <p>The appendMessage function is used in the CHEF chat log to append messages 
// to the end of the transcript of chat messages.  Called in chef_chat-List.vm
//
// @author University of Michigan, CHEF Software Development Team
// @version $Revision: 1.1 $
// 
function appendMessage(uname, uid, removeable, pdate, ptime, msg)
{
	var undefined;
	var position = 100000, docheight = 0, frameheight = 300;	  
	var transcript = (document.all) ? document.all.transcript : document.getElementById("transcript");
	
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
	else if(window.pageYOffset !== undefined)
	{
		// WIN_MZ 
		// WIN_NN
		position = window.pageYOffset;
	}
	else if(document.documentElement)
	{
		position = document.documentElement.scrollTop;
	}
	else if(document.body.scrollTop !== undefined)
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
	else if(document.body.parentNode !== undefined && document.body.parentNode.clientHeight !== undefined)
	{
		frameheight = document.body.parentNode.clientHeight;
	}
	
	// ========================================================================
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
	
	var newDiv = document.createElement('div');
	var blankLine = document.createElement('br');
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
	newDiv.innerHTML = '<span class="chefChatUserName" style="color: ' + color + ';">' 
		+ uname + '</span><span class="chefChatTimeStamp">'
		+ msgTime 		// msgTime should be empty string for hide both
		+ '</span><span class="chefChatMessageBody">: <span class="chefPre">' + msg + '</span></span>\n' ;
	transcript.appendChild(newDiv);
	transcript.appendChild(blankLine);

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
	window.location.reload(false);

}	// appendMessage

/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/js/chatscript.js,v 1.1 2005/04/12 17:44:21 ggolden.umich.edu Exp $
*
**********************************************************************************/
