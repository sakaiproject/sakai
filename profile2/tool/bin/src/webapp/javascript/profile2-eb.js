/*
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var CONNECTION_NONE = 0;
var CONNECTION_REQUESTED = 1;
var CONNECTION_INCOMING = 2;
var CONNECTION_CONFIRMED = 3;
	
function friendStatus(requestorId,friendId) {
	
	var status = null;
	
	jQuery.ajax({
    	url : "/direct/profile/" + requestorId + "/friendStatus.json?friendId=" + friendId,
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			status = data.data;
		},
		error : function() {
			status = -1;
		}
	});
	
	return status;
}

function requestFriend(requestorId,friendId) {

    jQuery.ajax( {
        url : "/direct/profile/" + requestorId + "/requestFriend?friendId=" + friendId,
        dataType : "text",
        cache: false,
        success : function(text,status) {
            var div = $('#profile_friend_' + friendId);
            div.html(text);
            div.attr('class','icon connection-request');
        }
    });

    return false;
}

function confirmFriendRequest(requestorId,friendId) {

    jQuery.ajax( {
        url : "/direct/profile/" + requestorId + "/confirmFriendRequest?friendId=" + friendId,
        dataType : "text",
        cache: false,
        success : function(text,status) {            
            var link = "<a href=\"javascript:;\" onClick=\"return removeFriend('" + requestorId + "','" + friendId + "');\">" + text + "</a>";
            var div = $('#profile_friend_' + friendId);
            div.html(link);
            div.attr('class','icon connection-confirmed');
        }
    });

    return false;
}

function removeFriend(removerId,friendId) {

    jQuery.ajax( {
        url : "/direct/profile/" + removerId + "/removeFriend?friendId=" + friendId,
        dataType : "text",
        cache: false,
        success : function(text,status) {
            var link = "<a href=\"javascript:;\" onClick=\"return requestFriend('" + removerId + "','" + friendId + "');\">" + text + "</a>";
            var div = $('#profile_friend_' + friendId);
            div.html(link);
            div.attr('class','icon connection-add');
        }
    });

    return false;
}

function ignoreFriendRequest(removerId,friendId) {

    jQuery.ajax( {
        url : "/direct/profile/" + removerId + "/ignoreFriendRequest?friendId=" + friendId,
        dataType : "text",
        cache: false,
        success : function(text,status) {
            var link = "<a href=\"javascript:;\" onClick=\"return requestFriend('" + removerId + "','" + friendId + "');\">" + text + "</a>";
            var div = $('#profile_friend_' + friendId);
            div.html(link);
            div.attr('class','icon connection-add');
        }
    });

    return false;
}