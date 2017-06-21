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
 *       http://www.opensource.org/licenses/ECL-2.0
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
		msgTime = " " + pdate + " " + ptime + " " ;
	}
	else if (window.display_date)
	{
		msgTime = " " + pdate + " " ;
	}
	else if(window.display_time)
	{
		msgTime = " " + ptime + " " ;
	}
	else if(window.display_id)
	{
		msgTime = " (" + pid + ") " ;
	}


	var newDiv = document.createElement('li');
	newDiv.setAttribute("data-message-id", msgId);
	newDiv.setAttribute("data-owner-id", uid);
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
			" <a class=\"chatRemove\" id=\"" + builtId + "\" href=\"#\" onclick=\"location.href='" + tmpdeleteUrl + "'\" title=\"" + deleteMsg + "\" >" +
			"<span class='fa fa-trash' aria-label='" + deleteMsg + "'></span></a>";
	}

	var isOnline = $("#presence").find("li:contains('" + uname + "')");
	var lastMessageOwnerId = $("#topForm\\:chatList li:last-of-type").attr("data-owner-id");
	if (lastMessageOwnerId == uid) {
		newDiv.className = "prev";
	}

	var innerHTML = '<span class="chatUserAvatar" style="background-image: url(/direct/profile/' + uid +'/image)">' + 
	'<span class="chatUserOnline ';
	if (isOnline.size() > 0) {
		innerHTML += 'is-online'
	}
	innerHTML += '"></span>' +
	'</span> ' +
	'<span class="chatNameDate">' + 
		'<span class="chatName">' + uname + '</span>' +
		'<span class="chatDate">' + msgTime + '</span>' +
	'</span>' +
	'<span class="chatMessage" data-message-id="' + msgId + '">' +
		'<span class="chatMessageDate">' + msgTime + '</span>' +
		'<span class="chatText">' + msg + '</span>' + deleteHtml +
	'</span>';
	newDiv.innerHTML = innerHTML;

	transcript.appendChild(newDiv);

	// adjust scroll
	var objDiv = document.getElementById("Monitor");
    objDiv.scrollTop = objDiv.scrollHeight;

    // update the messages count
    chat2_totalMessages++;
    chat2_shownMessages++;
    updateShownText();

    scrollChat();
}

function updateShownText() {
    var countText = chat2_messageCountTemplate + '';
    countText = countText.replace('*SHOWN*', chat2_shownMessages);
    countText = countText.replace('*TOTAL*', chat2_totalMessages);
    $("#chat2_messages_shown_total").html(countText);
}

// can't just pass a list of <li>'s, because $(string) will only parse a single object
function sortChildren(list) {
    var children = list.children('li');

    // sort uses last name if present, and is case-insensitive.
    children = children.sort(function(a,b){
	    var an = a.innerHTML.toLowerCase();
	    var i = an.indexOf(' ');
	    if (i >= 0)
		an = an.substring(i);
	    var bn = b.innerHTML.toLowerCase();
	    var j = bn.indexOf(' ');
	    if (j >= 0)
		bn = bn.substring(j);

	    if(an > bn) {
		return 1;
	    }
	    if(an < bn) {
		return -1;
	    }
	    // equal, take full name first
	    if (i <= 0 && j >= 0)
		return 1;
	    if (j <= 0 && i >= 0)
		return -1;
	    // equal, now do it on original so uppercase comes first
	    an = a.innerHTML;
	    bn = b.innerHTML;
	    if(an > bn) {
		return 1;
	    }
	    if(an < bn) {
		return -1;
	    }
	    return 0;
	});
    list.empty();
    list.append(children);
    return list;
}

function addUser(user) {
    var existing = $("#presence").find("li:contains('" + user + "')");
    if (existing.size() == 0) {
	$("#presence").append($('<li>' + user + '</li>'));
	var newChildren = sortChildren($("#presence")).children();
	$("#presence").empty();
	$("#presence").append(newChildren);
	$(".chatNameDate:contains(" + user + ")").parent().find(".chatUserOnline").addClass("is-online");
    }
}

function delUser(user) {
    $("#presence").find("li:contains('" + user + "')").remove();
	$(".chatNameDate:contains(" + user + ")").parent().find(".chatUserOnline").removeClass("is-online");
}

function updateUsers() {
    var url = "roomUsers?channel=" + currentChatChannelId;
    $.ajax({
    	    url: url,
	    type: "GET"})
    	.done(function(data) {
		var newChildren = sortChildren($('<ul>' + data + '</ul>')).children();
		$("#presence").empty();
		$("#presence").append(newChildren);
		for (var i=0; i<newChildren.length; i++) {
			var userName = newChildren[i].innerText;	
			$(".chatNameDate:contains(" + userName + ")").parent().find(".chatUserOnline").addClass("is-online");
		}
    	    });
}

function scrollChat() {
	// Scroll chat to last message
	var scrollableChat = $("#chatListWrapper");
	scrollableChat.scrollTop(scrollableChat.prop("scrollHeight"));
}

//Library to ajaxify the Chatroom message submit action
	$(document).ready(function() {
		updateShownText();
		updateUsers();
		scrollChat();

                // the iframe has a src of roomUsers.
                // in frameless situation that will be added to /portal/site ...
                // but the tool will only recognize /portal/tool. Do the mapping

		// fix up the delete links. They use /sakai.chat ... That won't work. without the leading /
		// it works. 
		var urlpath = location.pathname;
		var frameless = false;
		if (urlpath.indexOf('/portal/site') == 0) {
		    var i = urlpath.indexOf('/tool/');
		    if (i >= 0) {
			frameless = true;
		    }
		}
 
		if (frameless) {
		    $('.chatList a[id*="deleteMessage"]').each(function(index) {
			    $(this).attr('onclick', $(this).attr('onclick').replace("'/sakai.chat.deleteMessage.helper","'sakai.chat.deleteMessage.helper"));
			});
		    
		    if (deleteUrl.indexOf('/sakai.chat.deleteMessage.helper') == 0)
			deleteUrl = deleteUrl.substring(1);
		}

 		//resize horizontal chat area to get rid of horizontal scrollbar in IE

	    var options = {
	        //RESTful submit URL
	        url_submit: '/direct/chat-message/new',
	        control_key: 13,
            dom_button_submit_raw: document.getElementById("topForm:controlPanel:submit"),
            dom_button_submit: $(document.getElementById("topForm:controlPanel:submit")),
	        dom_button_reset: $(document.getElementById("topForm:controlPanel:reset")),
	        dom_textarea: $(document.getElementById("topForm:controlPanel:message")),
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
