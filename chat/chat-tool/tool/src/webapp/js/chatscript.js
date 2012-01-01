/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
			"<img src=\"/library/image/sakai/delete.gif\" style=\"margin-bottom:-2px;\" border=\"0\" alt=\"" + deleteMsg + "\" /></a>";
	}

	newDiv.innerHTML = '<span style="color: ' + color + '">' + uname + '</span>'
		+ deleteHtml
		+ '<span class="chatDate">' + msgTime + '</span>'
		+ msg;
	transcript.appendChild(newDiv);

	// adjust scroll
	var objDiv = document.getElementById("Monitor");
    objDiv.scrollTop = objDiv.scrollHeight;

    // update the messages count
    chat2_totalMessages++;
    chat2_shownMessages++;
    updateShownText();

}                           

function updateShownText() {
    var countText = chat2_messageCountTemplate + '';
    countText = countText.replace('*SHOWN*', chat2_shownMessages);
    countText = countText.replace('*TOTAL*', chat2_totalMessages);
    $("#chat2_messages_shown_total").html(countText);
}

//Library to ajaxify the Chatroom message submit action
	$(document).ready(function() {
		updateShownText();

		//resize horizontal chat area to get rid of horizontal scrollbar in IE
        if($.browser.msie){
           $(".chatList").width('93%');
        }
	    var options = {
	        //RESTful submit URL
	        url_submit: '/direct/chat-message/new',
	        control_key: 13,
            dom_button_submit_raw: document.getElementById("controlPanel:submit"),
            dom_button_submit: $(document.getElementById("controlPanel:submit")),
	        dom_button_reset: $(document.getElementById("controlPanel:reset")),
	        dom_textarea: $(document.getElementById("controlPanel:message")),
	        channelId: document.getElementById("topForm:chatidhidden").value,
	        enterKeyCheck:''
	    };
        
	    //Bind button submit action
	    options.dom_button_submit.bind('click', function() {
	    	options.dom_button_submit_raw.disabled = true;
	    	var params = [{
	            name:"chatChannelId", value:options.channelId
	            },{
	            name:"body", value:options.dom_textarea.val()
	        }];
	        if(options.channelId == null || options.channelId == "" ||
                options.dom_textarea.val() == null || options.dom_textarea.val() == ""){
                 options.dom_textarea.focus();
                 options.dom_button_submit_raw.disabled = false;
                 return false;
             }
             if(options.dom_textarea.val().replace(/\n/g, "").replace(/ /g, "").length == 0){
                     options.dom_textarea
                        .val("")
                        .focus();
                     options.dom_button_submit_raw.disabled = false;
                   return false;
            }
            $.ajax({
	            url: options.url_submit,
	            data: params,
	            type: "POST",
	            beforeSend: function() {
	                 $("#errorSubmit").slideUp('fast');
                },
	            error: function(xhr, ajaxOptions, thrownError) {
	                $("#errorSubmit").slideDown('fast');
	                options.dom_textarea.focus();
	                options.dom_button_submit_raw.disabled = false;
	                return false;
	            },
	            success: function(data) {
	                //Run dom update from headscripts.js
	               try { updateNow(); } catch (error) {alert(error);}
                    options.dom_textarea
	                    .val("")
	                    .focus();
	                options.dom_button_submit_raw.disabled = false;
	                return false;
	            }
	        });
            return false;
	    });
        //Avoid submitting on mouse click in textarea
        options.dom_textarea.bind('click', function(){
	        return false;
	    });
	    //Bind textarea keypress to submit btn
	    options.dom_textarea.keydown(function(e){
	        var key = e.charCode || e.keyCode || 0;
	        if( options.control_key == key && !options.dom_button_submit_raw.disabled ){
              options.dom_button_submit.trigger('click');
	          return false;
	        }
	    });
	    options.dom_button_reset.bind('click', function(){
	        options.dom_textarea
	                .val("")
	                .focus();
	        return false;
	    });
	});
