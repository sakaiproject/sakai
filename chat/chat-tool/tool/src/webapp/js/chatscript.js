/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
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
 
// When user goes between tabs or back from home screen, we monitor and updateChatData
document.addEventListener('visibilitychange', function() {
  if (document.visibilityState == 'hidden' || document.visibilityState == 'visible') {
    chatscript.changePageVisibility(document.visibilityState);
  }
});

var chatscript = {
	url_submit : "/direct/chat-message/",
	keycode_enter : 13,
	pollInterval : 5000,
	currentChatChannelId : null,
	timeoutVar : null,
	pageVisibility : 'visible',

	init : function(){
		var me = this;
		
		me.updateShownText();
		me.scrollChat();
		me.updateChatData();
		
		var textarea = $("#topForm\\:controlPanel\\:message");
		var submitButton = $("#topForm\\:controlPanel\\:submit");
		var resetButton = $("#topForm\\:controlPanel\\:reset");

		submitButton.bind('click', function() {
			var messageBody = textarea.val();
			submitButton.prop("disabled", true);
			var params = {
				"chatChannelId": me.currentChatChannelId,
				"body": messageBody
			}
			// If message body or currentChatChannelId are empty
			if (!me.currentChatChannelId || !messageBody || !messageBody.replace(/\n/g, "").replace(/ /g, "").length) {
				textarea.val("").focus();
				submitButton.prop("disabled", false);
				return false;
			}
			me.sendMessage(params, textarea, submitButton);
		});
		textarea.keydown(function(e) {
			var keycode = e.keyCode;
			if (keycode == me.keycode_enter && !submitButton.prop("disabled")) {
				submitButton.trigger("click");
				return false;
			}
		});
		resetButton.click(function() {
			textarea.val("").focus();
		});


		$(".chatList").on('click', '.chatRemove', function() {
			var messageItem = $(this).parent().parent();
			var messageId = messageItem.attr("data-message-id");
			var ownerDisplayName = messageItem.find(".chatName").text();
			var date = messageItem.find(".chatDate").text();
			var messageBody = messageItem.find(".chatText").html();
			me.showRemoveModal(messageId, ownerDisplayName, date, messageBody);
		});
		$("#deleteButton").click(function() { 
			var messageId = $(this).attr("data-message-id");
			me.deleteMessage(messageId);
		});

		$("#chatListWrapper").scroll(function() {
			if (($(".chatListWrapper")[0].scrollHeight - $(".chatListWrapper").scrollTop()) <= $(".chatListWrapper").height()) {
				setTimeout(function() {
					$(".divisorNewMessages[id!=divisorNewMessages]").fadeOut(300, function() {
						$(".divisorNewMessages[id!=divisorNewMessages]").remove();
					});
				}, 3000);
				$(".scrollBottom").fadeOut(300, function() {
					unreadedMessages = 0;
					me.updateUnreadedMessages();
				});
			}
		});
		$(".scrollBottom").click(function() {
			me.scrollChat();
			setTimeout(function() {
				$(".divisorNewMessages[id!=divisorNewMessages]").fadeOut(300, function() {
					$(".divisorNewMessages[id!=divisorNewMessages]").remove();
				});
			}, 3000);
			$(".scrollBottom").fadeOut(300, function() {
				unreadedMessages = 0;
				me.updateUnreadedMessages();
			});
		});
	},
  changePageVisibility : function(newVisibility) {
    this.pageVisibility = newVisibility;
    if (newVisibility == 'visible') {
      this.updateChatData();
    }
  },
	onAjaxError: function (xhr) {

		var textarea = $("#topForm\\:controlPanel\\:message");
		var submitButton = $("#topForm\\:controlPanel\\:submit");
		if (xhr.status === 404) {
			$("#missingChannel").slideDown('fast');
			textarea.val("").prop("disabled", true);
			submitButton.prop("disabled", true);
			$("#topForm\\:controlPanel\\:reset").prop("disabled", true);
		} else {
			$("#errorSubmit").slideDown('fast');
			submitButton.prop("disabled", false);
		}
		textarea.focus();
	},
	sendMessage : function(params, textarea, submitButton) {
		var me = this;
		var errorSubmit = $("#errorSubmit");
		var missingChannel = $("#missingChannel");
		$.ajax({
			url: me.url_submit + 'new',
			data: params,
			type: "POST",
			beforeSend: function() {
				errorSubmit.slideUp('fast');
			},
			error: function(xhr, textStatus, errorThrown) {
				me.onAjaxError(xhr);
			},
			success: function(data) {
				me.scrollChat();
				me.updateChatData();
				textarea.val("").focus();
				submitButton.prop("disabled", false);
			},
		});
	},
	deleteMessage : function(messageId) {
		var me = this;
		var removeModal = $("#removemodal");
		removeModal.modal("hide");
		$.ajax({
			url: me.url_submit + messageId,
			type: "DELETE",
			success: function(data) {
				removeModal.modal("hide");
				me.updateChatData();
			}
		});
	},
	updateChatData : function () {
		var me = this;
		this.doUpdateChatData();
		if(this.timeoutVar != null) {
			clearTimeout(this.timeoutVar);
		}
		// If the tab is hidden, no use in firing AJAX requests for new chat data
		if(this.pageVisibility != 'hidden') {
			this.timeoutVar = setTimeout(function() {
				me.updateChatData();
			}, this.pollInterval);
		}
	},
	doUpdateChatData : function() {
		var me = this;
		var missingChannel = $("#missingChannel");
		var url = me.url_submit + portal.user.id + "/chatData.json";
		var params = {
			"siteId": portal.siteId,
			"channelId": this.currentChatChannelId
		};
		$.ajax({
			url: url,
			data: params,
			type: "GET",
			cache: false,
			success: function (data) {
				me.addMessages(data.data.messages);
				me.updatePresentUsers(data.data.presentUsers);
				me.deleteMessages(data.data.deletedMessages);
			},
			error: function (xhr, textStatus, errorThrown) {
				me.onAjaxError(xhr);
			}
		});
	},
	addMessages : function (messages) {
		var me = this;
		var scrolledToBottom = true;
		if (($(".chatListWrapper")[0].scrollHeight - $(".chatListWrapper").scrollTop()) > $(".chatListWrapper").height()) {
			scrolledToBottom = false;
		}

		if (messages.length > 0 && !scrolledToBottom) {
			if ($(".divisorNewMessages").length < 2) {
				var divisorNewMessages = $("#divisorNewMessages").clone();
				divisorNewMessages.removeAttr("id");
				divisorNewMessages.removeClass("hide");
				$("#topForm\\:chatList").append(divisorNewMessages);
			}
		}

		for (var i=0; i<messages.length; i++) {
			var lastMessageOwnerId = $("#topForm\\:chatList li[class!='divisorNewMessages']").last().attr("data-owner-id");
			var lastMessageMillis  = $("#topForm\\:chatList li[class!='divisorNewMessages']").last().attr("data-millis");
			var messageId = messages[i].id;
			var ownerId = messages[i].owner;
			var ownerDisplayName = messages[i].ownerDisplayName;

			var messageDate = messages[i].messageDate;
			var messageMillisDiff = messages[i].messageDate - (lastMessageMillis ? lastMessageMillis : 0);
			var localizedDate = messages[i].messageDateString.localizedDate;
			var localizedTime = messages[i].messageDateString.localizedTime;
			var dateID = messages[i].messageDateString.dateID;
			var dateStr = this.renderDate(localizedDate, localizedTime, dateID);

			var messageBody = messages[i].body;
			var removeable = messages[i].removeable;
			var exists = $("#topForm\\:chatList li[data-message-id=" + messageId + "]").length;
			if (!exists) {
				var messageItem = $("#chatListItem").clone();
				messageItem.removeClass("hide");
				messageItem.removeAttr("id");
				messageItem.attr("data-message-id", messageId);
				messageItem.attr("data-owner-id", ownerId);
				messageItem.attr("data-millis", messageDate);
				messageItem.find(".chatUserAvatar").css("background-image", "url(/direct/profile/" + ownerId + "/image)");
				messageItem.find(".chatMessage").attr("data-message-id", messageId);
				messageItem.find(".chatName").attr("id", ownerId);
				messageItem.find(".chatName").text(ownerDisplayName);
				messageItem.find(".chatDate").text(dateStr);
				messageItem.find(".chatText").html(messageBody);
				if (removeable) {
					messageItem.find(".chatRemove").removeClass("hide");
				}
				if (lastMessageOwnerId == ownerId && messageMillisDiff < (5*60*1000)) {
					messageItem.addClass("nestedMessage");
					messageItem.find(".chatMessageDate").text(dateStr);
				}
				$("#topForm\\:chatList").append(messageItem);
				someMessageAdded = true;
				chat2_totalMessages++;
				chat2_shownMessages++;
				this.updateShownText();
				if (scrolledToBottom) {
					this.scrollChat();
					setTimeout(function() {
						$(".divisorNewMessages[id!=divisorNewMessages]").fadeOut(300, function() {
							$(".divisorNewMessages[id!=divisorNewMessages]").remove();
						});
					}, 3000);
					$(".scrollBottom").fadeOut(300, function() {
						unreadedMessages = 0;
						me.updateUnreadedMessages();
					});
				} else {
					unreadedMessages++;
					me.updateUnreadedMessages();
					$(".divisorNewMessages[id!=divisorNewMessages]").find(".newMessages").text(unreadedMessages);
					$(".scrollBottom").fadeIn(300).removeClass("hide");
				}
			}
		}
	},
	deleteMessages : function (messages) {
		for (var i=0; i<messages.length; i++) {
			var messageId = messages[i].id;
			if(messageId != '*') {
				var existsMessage = $("#topForm\\:chatList li[data-message-id=" + messageId + "]").length;
				if (existsMessage > 0) {
					var message = $("#topForm\\:chatList li[data-message-id=" + messageId + "]");
					if (!message.hasClass("nestedMessage") && message.next().hasClass("nestedMessage")) {
						message.next().removeClass("nestedMessage");
					}
					message.remove();
					chat2_totalMessages--;
					chat2_shownMessages--;
					this.updateShownText();
				}
			} else {
				$("#topForm\\:chatList li:not(#chatListItem, #divisorNewMessages)").remove();
				chat2_totalMessages = 0;
				chat2_shownMessages = 0;
				this.updateShownText();
			}
		}
	},
	updatePresentUsers : function (users) {
		$("#presence").empty();
		$("#topForm\\:chatList li .chatUserOnline").removeClass("is-online");
		for (var i=0; i<users.length; i++) {
			var ownerId = users[i].id;
			var userId = ownerId;
			if(ownerId.indexOf(':') > -1) {
				userId = ownerId.substring(ownerId.indexOf(":") + 1)
			}
			var userElement = document.createElement("li");
			userElement.setAttribute("data-user-id", ownerId);
			userElement.innerHTML = users[i].name;
			$("#presence").append(userElement);
			$("#topForm\\:chatList li[data-owner-id=" + userId + "] .chatUserOnline").addClass("is-online");
		}
	},
	showRemoveModal : function(messageId, ownerDisplayName, date, messageBody) {
		var removeModal = $("#removemodal");
		removeModal.find("#owner").text(ownerDisplayName);
		removeModal.find("#date").text(date);
		removeModal.find("#message").html(messageBody);
		removeModal.find("#deleteButton").attr("data-message-id", messageId);
		removeModal.modal("show");
	},
	scrollChat : function () {
		var scrollableChat = $("#chatListWrapper");
		scrollableChat.scrollTop(scrollableChat.prop("scrollHeight"));
	},
	updateShownText : function () {
		var countText = chat2_messageCountTemplate + '';
		countText = countText.replace('*SHOWN*', chat2_shownMessages);
		countText = countText.replace('*TOTAL*', chat2_totalMessages);
		$("#chat2_messages_shown_total").html(countText);
	},
	updateUnreadedMessages : function () {
		var text = chat2_messagesUnreadedTemplate.replace("*UNREADED*", unreadedMessages);
		$(".scrollBottom").attr("title", text);
		$(".scrollBottom .newMessages").text(unreadedMessages);
	},
	renderDate : function(localizedDate, localizedTime, dateID) {
		var msgTime = "";
		if(window.display_date && window.display_time) {
			msgTime = " " + localizedDate + " " + localizedTime + " " ;
		} else if (window.display_date) {
			msgTime = " " + localizedDate + " " ;
		} else if(window.display_time) {
			msgTime = " " + localizedTime + " " ;
		} else if(window.display_id) {
			msgTime = " (" + dateID + ") " ;
		}
		return msgTime;
	}
};
