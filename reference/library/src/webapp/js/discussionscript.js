/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/js/discussionscript.js,v 1.2 2005/06/01 02:01:38 zqian.umich.edu Exp $
*
***********************************************************************************
@license@
**********************************************************************************/

// 
// <p>The appendMessage function is used in the CHEF chat log to append messages 
// to the end of the transcript of chat messages.  Called in chef_chat-List.vm
//
// @author University of Michigan, CHEF Software Development Team
// @version $Revision: 1.2 $
// 
function getRealTop(el) {
    yPos = el.offsetTop;
    tempEl = el.offsetParent;
    while (tempEl != null) {
        yPos += tempEl.offsetTop;
        tempEl = tempEl.offsetParent;
    }
    return yPos;
}

function scroll(mId)
{

	var position = 0;
	var undefined;
	var docheight = 0, frameheight = 300;	

	if (mId == "")
	{
		// not message selected 
		position = 0;
	}
	else
	{
		for (var loop=0; loop<document.anchors.length; loop++) 
		{
			if (document.anchors[loop].id.indexOf(mId) != -1)
				position = getRealTop(document.anchors[loop]);
		}

		// find the height of the frame containing the discussion
		if(navigator.appName == "Microsoft Internet Explorer" && navigator.userAgent.indexOf("Win") > -1 )
		{
			// WIN_IE
			if(document.documentElement && document.documentElement.clientHeight)
			{
				frameheight = document.documentElement.clientHeight;
			}
			else if(window.contentDocument && window.contentdocument.clientHeight)
			{
				frameheight = window.contentdocument.clientHeight;
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

		// adjust scroll
		if(position < frameheight)
		{
			position = 0;
		}
		window.scrollTo(0, position);
	}

}	// scroll

function updateMainFrameHeight(id)
{
	//run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var obj = parent.document.getElementById(id);
	if (obj)
	{
		// reset the scroll
		parent.window.scrollTo(0,0);

		// to get a good reading from some browsers (ie?) set the height small
		obj.style.height="50px";

		
		// Mozilla way to detect height
		var height = document.body.offsetHeight;
		
		// Internet Explorer way to detect height
		if (document.body.scrollHeight)
		{
			height = document.body.scrollHeight;
		}

		if (parent.frames.length > 0)
		{
			for (var i=0; i<parent.frames.length; i++)
			{
				var k = parent.frames[i];
				var id = k.name;
				
				if (k.frames.length > 0)
				{
					for (var j=0; j < k.frames.length; j++)
					{
						var innerIframe = k.frames[j];
						var innerIframeName = innerIframe.name;
						
						if (innerIframeName == "Control")
						{
							// Mozilla way to detect height
							var iHeight = innerIframe.document.body.offsetHeight;
							
							// Internet Explorer way to detect height
							if (innerIframe.document.body.scrollHeight)
							{
								iHeight = innerIframe.document.body.scrollHeight;
							}

							document.getElementById(innerIframeName).style.height=iHeight + "px";
							height = height + iHeight;
						}
					}
					
				}
			}
		}

		obj.style.height=height + "px";
	}
}	// updateMainFrameHeight

/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/js/discussionscript.js,v 1.2 2005/06/01 02:01:38 zqian.umich.edu Exp $
*
**********************************************************************************/
