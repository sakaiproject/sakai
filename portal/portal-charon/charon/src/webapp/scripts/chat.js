function PortalChat() {//

	this.showOfflineConnections = false;
	this.currentConnections = {};
	this.onlineConnections = [];
	this.currentConnectionsMap = {};
	this.currentPeerConnectionsMap = {};
	this.localMediaStream = null;
	this.currentSiteUsers = {};
	this.currentSiteUsersMap = {};
	this.expanded = false;
	this.offline = false;
	this.currentChats = [];
	this.getLatestDataInterval = null;
	this.connectionErrors = 0;
	this.MAX_CONTENT_HEIGHT = 250;
	this.originalTitle = document.title;
	this.connectionsAvailable = true;
	this.videoCall = null;
	this.videoOff = false;

	/**
	 * Utility for rendering trimpath templates. Takes the id of the template,
	 * an object with the data to be mixed in, and the id of the element to
	 * render into
	 */
	this.renderTemplate = function(templateId, contextObject, outputId) {
		var templateNode = document.getElementById(templateId);
		var firstNode = templateNode.firstChild;

		var template = null;

		if (firstNode && firstNode.nodeType === 8) {
			template = firstNode.data.toString();
		} else {
			template = templateNode.innerHTML.toString();
		}

		var trimpathTemplate = TrimPath.parseTemplate(template, templateId);

		var render = trimpathTemplate.process(contextObject);

		if (outputId) {
			document.getElementById(outputId).innerHTML = render;
		}

		return render;
	}

	this.sendMessageToUser = function(to, content) {

		jQuery
				.ajax({
					url : "/direct/portal-chat/new",
					dataType : "text",
					cache : false,
					type : 'POST',
					data : {
						'to' : to,
						'message' : content
					},
					success : function(text, status) {
						var messagePanel = $("#pc_connection_chat_" + to
								+ "_messages");

						if ('OFFLINE' === text) {
							var toDisplayName = portalChat.currentConnectionsMap[to].displayName;
							messagePanel.append("<div><br /></div>");
							messagePanel
									.append("<div><span class=\"pc_displayname\">"
											+ toDisplayName
											+ " is offline</span></div>");
						} else {
							var date = new Date();
							portalChat.addToMessageStream(to, {
								'from' : portal.user.id,
								'content' : content,
								'timestamp' : date.getTime()
							});
							var dateString = portalChat.formatDate(new Date());

							portalChat.appendMessageToChattersPanel({
								'content' : content,
								'panelUuid' : to,
								'from' : portal.user.id,
								'dateString' : dateString,
								'fromDisplayName' : 'You'
							});

							$('#pc_editor_for_' + to).val('');
						}

						portalChat.scrollToBottom(to);
					},
					error : function(xhr, textStatus, error) {

						if (403 == xhr.status) {
							portalChat.handleSecurityError();
						}

						alert("Failed to send message. Reason: " + textStatus
								+ ". Error: " + error);
					}
				});
	}

	this.sendVideoMessageToUser = function(to, content) {

		jQuery.ajax({
			url : "/direct/portal-chat/new",
			dataType : "text",
			cache : false,
			type : 'POST',
			data : {
				'to' : to,
				'peerMessage' : content
			},
			success : function(text, status) {
				if ('OFFLINE' === text) {
					/* The peer is disconnected you can close the connection */
					portalChat.setVideoStatus(to, "User went unexpectly",
							true);
					portalChat.closeVideoCall(to);
				}
			},
			error : function(xhr, textStatus, error) {

				if (403 == xhr.status) {
					portalChat.handleSecurityError();
				}

				alert("Failed to send message. Reason: " + textStatus
						+ ". Error: " + error);
			}
		});
	}

	/**
	 * This is a bad news failure. Clear the getLatestData interval.
	 */
	this.handleSecurityError = function() {
		// alert("Sakai security error. Maybe your Sakai session has timed
		// out?");
		portalChat.clearGetLatestDataInterval();
		return;
	}

	/**
	 * If one doesn't exist already, creates a window for the chatter
	 * represented by uuid.
	 */
	this.setupChatWindow = function(uuid, minimised) {

		var connection = this.currentConnectionsMap[uuid];

		// This should never happen, but sometimes it does ...
		if (!connection) {
			return false;
		}

		// Create the div target

		var id = "pc_chat_with_" + uuid;

		// If we already have a chat window, return.
		if ($('#' + id).length)
			return false;

		// Append a new chat div to the container
		$('#pc_chat_window_container')
				.prepend(
						"<div id=\""
								+ id
								+ "\" class=\"pc_chat_window\" data-height=\"300\"></div>");

		this.renderTemplate('pc_connection_chat_template', connection, id);

		this.currentChats.push(uuid);

		if (minimised) {
			$('#pc_connection_chat_' + uuid + '_content').hide();
			var chatDiv = $('#' + id);
			chatDiv.css('height', 'auto');
			chatDiv.addClass('pc_minimised');
		} else {
			$('#' + id).addClass('pc_maximised');
		}

		var chatSessionString = sessionStorage['pcsession_' + uuid];
		var chatSession;
		if (chatSessionString) {
			chatSession = JSON.parse(chatSessionString);
			// There is currently a message stream for this chatter
			chatSession.minimised = minimised;
		} else {
			// There is no message stream for this chatter. Set up a new one.
			chatSession = {
				'uuid' : uuid,
				'minimised' : minimised,
				'messages' : []
			};
		}

		sessionStorage
				.setItem('pcsession_' + uuid, JSON.stringify(chatSession));

		$('#pc_editor_for_' + uuid).focus();
		// Test if video is enabled
		$('#pc_chat_' + uuid + '_video_content').hide();
		$('#pc_connection_' + uuid + '_videoin').hide();
		$('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
		if (!this.videoCall || !this.videoCall.isVideoEnabled() || !this.hasVideoAgent(uuid)) {
			$('#pc_connection_' + uuid + '_videochat_bar').hide();
		} else {
			if (!minimised) {
				$('#' + id).css('height', '318px');
			}
			$('#' + id).attr('data-height', '318');
		}

		return false;
	}

	this.closeChatWindow = function(uuid) {
		var removed = -1;
		for ( var i = 0, j = this.currentChats.length; i < j; i++) {
			if (uuid === this.currentChats[i]) {
				removed = i;
				break;
			}
		}

		if( $("#pc_chat_" + uuid + "_video_content").is(':visible'))	 {
			this.closeVideoCall(uuid);			
		}
			
		$('#pc_chat_with_' + uuid).remove();
		
		this.currentChats.splice(removed, 1);

		sessionStorage.removeItem('pcsession_' + uuid);

		return false;
	}

	/**
	 * Show the content, hide the expand link, show the collapse link and set
	 * the expanded flag in the settings cookie.
	 */
	this.toggleChat = function() {

		if (!this.expanded) {

			// var pcc = $('#pc_content');
			var pc = $('#pc');

			// pcc.show();
			pc.show();

			// $('#presenceArea').toggle(false);

			this.setSetting('expanded', true);
			this.expanded = true;

			var pc_users = $('#pc_users');

			if (pc_users.height() > this.MAX_CONTENT_HEIGHT) {
				pc_users.height(this.MAX_CONTENT_HEIGHT);
			}

			$('#pc').css('height', 'auto');
		} else {

			// $('#pc_content').hide();
			$('#pc').hide();

			$('#pc').css('height', 'auto');

			this.setSetting('expanded', false);
			this.expanded = false;

			// $('#presenceArea').toggle(true);
		}

		return false;
	}

	this.toggleChatWindow = function(uuid) {

		var chatDiv = $('#pc_chat_with_' + uuid);

		if (chatDiv.length < 1)
			return;

		var chatSessionString = sessionStorage['pcsession_' + uuid];
		var chatSession;

		if (chatDiv.hasClass('pc_maximised')) {

			$('#pc_connection_chat_' + uuid + '_content').hide();
			chatDiv.addClass('pc_minimised');
			chatDiv.removeClass('pc_maximised');
			chatDiv.css('height', 'auto');
			if (chatSessionString) {
				chatSession = JSON.parse(chatSessionString);
				chatSession.minimised = true;
			} else {
				chatSession = {
					'uuid' : uuid,
					'minimised' : true,
					'messages' : []
				};
			}
		} else {
			$('#pc_connection_chat_' + uuid + '_content').show();
			chatDiv.removeClass('pc_minimised');
			chatDiv.addClass('pc_maximised');
			chatDiv.css('height', chatDiv.attr('data-height') + 'px');
			$('#pc_chat_with_' + uuid + ' > .pc_connection_chat_title')
					.removeClass('pc_new_message');
			this.scrollToBottom(uuid);
			if (chatSessionString) {
				chatSession = JSON.parse(chatSessionString);
				chatSession.minimised = false;
			} else {
				chatSession = {
					'uuid' : uuid,
					'minimised' : false,
					'messages' : []
				};
			}

			$('#pc_editor_for_' + uuid).focus();
		}

		sessionStorage
				.setItem('pcsession_' + uuid, JSON.stringify(chatSession));
	}

	this.scrollToBottom = function(uuid) {
		$(document).ready(
				function() {
					var objDiv = document.getElementById("pc_connection_chat_"
							+ uuid + "_messages");
					// Arbitrary. Just nice and big.
					objDiv.scrollTop = 100000;
				});
	}

	/**
	 * Creates a nice, human readable date from the passed in Date object
	 */
	this.formatDate = function(date) {
		var minutes = date.getMinutes();
		if (minutes < 10)
			minutes = '0' + minutes;
		var hours = date.getHours();
		if (hours < 10)
			hours = '0' + hours;
		return hours + ":" + minutes;
	}

	/**
	 * Append a message to the messages area.
	 */
	this.appendMessage = function(message) {

		var from = message.from;
		var content = message.content;
		var timestamp = message.timestamp;

		// If a chat window is already open for this sender, append to it.
		var messagePanel = $("#pc_chat_with_" + from);

		var flashIt = false;

		if (!messagePanel.length) {
			// No current chat window for this sender. Create one.
			this.setupChatWindow(from, true);
		} else if ($('#pc_connection_chat_' + from + '_content').css('display') === 'none') {
			// The sender's chat window is currently minimised so we want to
			// flash it to
			// draw attention to it.
			flashIt = true;
		}

		if (flashIt) {
			$('#pc_chat_with_' + from + ' > .pc_connection_chat_title')
					.addClass('pc_new_message');
		}

		var fromDisplayName = this.currentConnectionsMap[from].displayName;
		var userId = this.currentConnectionsMap[from].uuid;
		messagePanel = $("#pc_connection_chat_" + from + "_messages");

		var dateString = this.formatDate(new Date(timestamp));
		portalChat.appendMessageToChattersPanel({
			'content' : content,
			'panelUuid' : from,
			'from' : from,
			'dateString' : dateString,
			'fromDisplayName' : fromDisplayName
		});

		this.scrollToBottom(from);
	}

	this.updateConnections = function(connections, online) {

		var onlineConnections = [];

		for ( var i = 0, j = connections.length; i < j; i++) {
			connections[i].online = false;
			connections[i].video = 'none';
			for ( var k = 0, m = online.length; k < m; k++) {
				if (online[k].id === connections[i].uuid) {
					connections[i].online = true;
					connections[i].video = online[k].video;
					onlineConnections.push(connections[i]);
				}
			}
		}

		var changed = false;

		if (portalChat.currentConnections.length != connections.length) {
			changed = true;
		} else {
			for ( var i = 0, j = connections.length; i < j; i++) {
				var present = false;
				var statusSame = true;
				for ( var k = 0, m = portalChat.currentConnections.length; k < m; k++) {
					if (portalChat.currentConnections[k].uuid === connections[i].uuid) {
						present = true;
						if (portalChat.currentConnections[k].online != connections[i].online
								|| portalChat.currentConnections[k].video != connections[i].video) {
							if (connections[i].video!='none') {
								if ($(
										'#pc_chat_with_'
												+ portalChat.currentConnections[k].uuid)
										.css('height') != 'auto') {
									$(
											'#pc_chat_with_'
													+ portalChat.currentConnections[k].uuid)
											.css('height', '318px');
								}
								$(
										'#pc_chat_with_'
												+ portalChat.currentConnections[k].uuid)
										.attr('data-height', '318');
								$(
										'#pc_connection_'
												+ portalChat.currentConnections[k].uuid
												+ '_videochat_bar').show();
							} else {
								if ($(
										'#pc_chat_with_'
												+ portalChat.currentConnections[k].uuid)
										.css('height') != 'auto') {
									$(
											'#pc_chat_with_'
													+ portalChat.currentConnections[k].uuid)
											.css('height', '300px');
								}
								$(
										'#pc_chat_with_'
												+ portalChat.currentConnections[k].uuid)
										.attr('data-height', '300');
								$(
										'#pc_connection_'
												+ portalChat.currentConnections[k].uuid
												+ '_videochat_bar').hide();
							}
							statusSame = false;
						}
						break;
					}
				}

				if (!present || !statusSame) {
					changed = true;
					break;
				}
			}
		}

		if (changed) {

			sessionStorage.pcCurrentConnections = JSON.stringify(connections);
			sessionStorage.pcOnlineConnections = JSON
					.stringify(onlineConnections);

			portalChat.currentConnections = connections;

			portalChat.currentConnectionsMap = {};

			for ( var i = 0, j = connections.length; i < j; i++) {
				portalChat.currentConnectionsMap[connections[i].uuid] = connections[i];
			}

			// Bit of a hack really.
			portalChat.currentConnectionsMap[portal.user.id] = {
				'displayName' : 'You'
			};

			sessionStorage.pcCurrentConnectionsMap = JSON
					.stringify(portalChat.currentConnectionsMap);

			portalChat.onlineConnections = onlineConnections;

			if (portalChat.showOfflineConnections == true) {
				portalChat.renderTemplate('pc_connections_template', {
					'connections' : portalChat.currentConnections
				}, 'pc_connections');
			} else {
				portalChat.renderTemplate('pc_connections_template', {
					'connections' : portalChat.onlineConnections
				}, 'pc_connections');
			}

			portalChat.sortConnections();
		}
	}

	/**
	 * If the list supplied differs from the current one, update the site users
	 * list
	 */
	this.updateSiteUsers = function(siteUsers) {

		var changed = false;

		if (portalChat.currentSiteUsers.length != siteUsers.length) {
			changed = true;
		} else {
			for ( var i = 0, j = siteUsers.length; i < j; i++) {
				var inCurrentData = true;
				for ( var k = 0, m = portalChat.currentSiteUsers.length; k < m; k++) {
					if (portalChat.currentSiteUsers[k].id === siteUsers[i].id) {
						if (portalChat.currentSiteUsers[k].video != siteUsers[i].video) {
							inCurrentData = false;
							if (siteUsers[i].video!='none') {
								if ($(
										'#pc_chat_with_'
												+ portalChat.currentSiteUsers[k].id)
										.css('height') != 'auto') {
									$(
											'#pc_chat_with_'
													+ portalChat.currentSiteUsers[k].id)
											.css('height', '318px');
								}
								$(
										'#pc_chat_with_'
												+ portalChat.currentSiteUsers[k].id)
										.attr('data-height', '318');
								$(
										'#pc_connection_'
												+ portalChat.currentSiteUsers[k].id
												+ '_videochat_bar').show();
							} else {
								if ($(
										'#pc_chat_with_'
												+ portalChat.currentSiteUsers[k].id)
										.css('height') != 'auto') {
									$(
											'#pc_chat_with_'
													+ portalChat.currentSiteUsers[k].id)
											.css('height', '300px');
								}
								$(
										'#pc_chat_with_'
												+ portalChat.currentSiteUsers[k].id)
										.attr('data-height', '300');
								$(
										'#pc_connection_'
												+ portalChat.currentSiteUsers[k].id
												+ '_videochat_bar').hide();
							}
						}
						break;
					}
				}

				if (!inCurrentData) {
					changed = true;
					break;
				}
			}
		}

		if (changed) {
			sessionStorage.pcCurrentSiteUsers = JSON.stringify(siteUsers);

			portalChat.currentSiteUsers = siteUsers;

			if (portalChat.currentSiteUsers.length > 0) {
				portalChat.renderTemplate('pc_site_users_template', {
					'siteUsers' : portalChat.currentSiteUsers
				}, 'pc_site_users');
			} else {
				$('#pc_site_users').html('');
			}

			for ( var i = 0, j = portalChat.currentSiteUsers.length; i < j; i++) {
				portalChat.currentSiteUsers[i].uuid = portalChat.currentSiteUsers[i].id;
				portalChat.currentConnectionsMap[portalChat.currentSiteUsers[i].uuid] = portalChat.currentSiteUsers[i];
			}

			sessionStorage.pcCurrentConnectionsMap = JSON
					.stringify(portalChat.currentConnectionsMap);

			portalChat.sortSiteUsers();
		}

	}

	this.sortConnections = function() {

		$(document).ready(function() {
			$("#pc_connections").html($(".pc_connection").sort(function(a, b) {
				var val1 = a.children[0].children[1].innerHTML;
				var val2 = b.children[0].children[1].innerHTML;
				return val1 == val2 ? 0 : val1 < val2 ? -1 : 1;
			}));
		});
	}

	this.sortSiteUsers = function() {

		$(document).ready(function() {
			$("#pc_site_users").html($(".pc_site_user").sort(function(a, b) {
				var val1 = a.children[0].children[1].innerHTML;
				var val2 = b.children[0].children[1].innerHTML;
				return val1 == val2 ? 0 : val1 < val2 ? -1 : 1;
			}));
		});
	}

	this.updateMessages = function(messages) {

		for ( var i = 0, j = messages.length; i < j; i++) {
			portalChat.appendMessage(messages[i]);
			portalChat.addToMessageStream(messages[i].from, messages[i]);
		}

		if (messages.length > 0) {

			var lastMessage = messages[messages.length - 1];
			var fromDisplayName = portalChat.currentConnectionsMap[lastMessage.from].displayName;

			if (document.hasFocus() == false) {
				document.title = 'Message from ' + fromDisplayName;
			}
		}
	}

	this.updateVideoMessages = function(messages) {
		for ( var i = 0, j = messages.length; i < j; i++) {
			this.onvideomessage(messages[i].from, messages[i]);
		}
	}

	this.onvideomessage = function(uuid, message) {
		this.videoCall.receiveMessage(uuid, message,this.getVideoAgent(uuid)); // message function. It
														// will send to rtc to
														// process it
	}

	this.getLatestData = function() {

		// Grab the site id from the url. We need to pass it to the presence
		// code in the chat entity provider.
		var url = document.location.href;
		var match = /site\/([\w-]*)/.exec(url);
		var siteId = '';
		if (match && match.length == 2)
			siteId = match[1];

		var onlineString = portalChat.offline ? 'false' : 'true';
		var videoString = (this.videoCall && !this.videoOff)?this.videoCall.getVideoAgent():'none';

		jQuery
				.ajax({
					url : '/direct/portal-chat/' + portal.user.id
							+ '/latestData.json?auto=true&siteId=' + siteId
							+ '&online=' + onlineString + '&video='
							+ videoString,
					dataType : "json",
					cache : false,
					success : function(data, status) {
						portalChat.updateMessages(data.data.messages);
						portalChat.updateVideoMessages(data.data.videoMessages);
						// SAK-20565. Profile2 may not be installed, so no
						// connections :(
						if (portalChat.connectionsAvailable === true) {
							if (data.data.connectionsAvailable) {
								$('#pc_connections_wrapper').show();
								portalChat
										.updateConnections(
												data.data.connections,
												data.data.online);
							} else {
								$('#pc_connections_wrapper').hide();

								// No point checking again as profile2 can't be
								// installed without a full restart
								portalChat.connectionsAvailable = false;
								portalChat.setSetting('connectionsAvailable',
										false);
							}
						}

						portalChat.updateSiteUsers(data.data.presentUsers);

						var totalChattable = data.data.online.length;

						// SAK-22260. Don't count the same person twice ...
						for ( var i = 0, j = data.data.presentUsers.length; i < j; i++) {
							var presentUser = data.data.presentUsers[i];
							var alreadyIn = false;
							for ( var k = 0, m = data.data.online.length; k < m; k++) {
								if (presentUser.id === data.data.online[k].id) {
									alreadyIn = true;
									break;
								}
							}
							if (alreadyIn == false) {
								totalChattable++;
							}
						}

						if (totalChattable > 0) {
							$('#chattableCount').html(totalChattable + '');
							$('#chattableCount').removeClass('empty').addClass(
									'present');
						} else {
							$('#chattableCount').html(' ');
							$('#chattableCount').removeClass('present')
									.addClass('empty');
						}
					},
					error : function(xhr, textStatus, error) {

						if (403 == xhr.status) {
							portalChat.handleSecurityError();
							return;
						}

						if (portalChat.connectionErrors >= 2) {
							portalChat.clearGetLatestDataInterval();
							portalChat.connectionErrors = 0;
							alert("getLatestMessages: It looks like the chat server is unavailable. Check your network connection.");
						} else {
							portalChat.connectionErrors = portalChat.connectionErrors + 1;
						}
					}
				});
	}

	this.addToMessageStream = function(uuid, message) {

		var chatSessionString = sessionStorage['pcsession_' + uuid];
		var chatSession;
		if (chatSessionString) {
			chatSession = JSON.parse(sessionStorage['pcsession_' + uuid]);
			if (chatSession.messages) {
				chatSession.messages.push(message);
			} else {
				chatSession.messages = [ message ];
			}
		} else {
			chatSession = {
				'uuid' : uuid,
				'minimised' : false,
				'messages' : [ message ]
			};
		}

		sessionStorage
				.setItem('pcsession_' + uuid, JSON.stringify(chatSession));
	}

	this.pingConnection = function(userId) {

		jQuery
				.ajax({
					url : '/direct/portal-chat/' + userId + '/ping',
					dataType : "text",
					cache : false,
					success : function(text, status) {
						$('#pc_pinged_popup_' + userId).show();
						setTimeout(function() {
							$('#pc_pinged_popup_' + userId).fadeOut(800);
						}, 500);
					},
					error : function(xmlHttpRequest, textStatus, error) {

						if (403 == xhr.status) {
							portalChat.handleSecurityError();
						}

						if (portalChat.connectionErrors >= 2) {
							portalChat.clearGetLatestDataInterval();
							portalChat.connectionErrors = 0;
							alert("pingConnection: It looks like the chat server is unavailable. Check your network connection.");
						} else {
							portalChat.connectionErrors = portalChat.connectionErrors + 1;
						}
					}
				});
	}

	this.setSetting = function(setting, value, persistent) {

		var storage = (persistent) ? localStorage : sessionStorage;

		var mySettings = {};
		var mySettingsString = storage.pcSettings;
		if (mySettingsString) {
			mySettings = JSON.parse(mySettingsString);
		}
		mySettings[setting] = value;
		storage.pcSettings = JSON.stringify(mySettings);
	}

	this.getSetting = function(setting, persistent) {

		var storage = (persistent) ? localStorage : sessionStorage;

		var mySettings = {};
		var mySettingsString = storage.pcSettings;
		if (mySettingsString) {
			mySettings = JSON.parse(mySettingsString);
		}

		return mySettings[setting];
	}

	this.setGetLatestDataInterval = function() {
		if (portalChat.getLatestDataInterval === null) {
			portalChat.getLatestDataInterval = window.setInterval(function() {
				portalChat.getLatestData();
			}, 5000);
		}
	}

	this.clearGetLatestDataInterval = function() {
		window.clearInterval(portalChat.getLatestDataInterval);
		portalChat.getLatestDataInterval = null;
	}

	this.appendMessageToChattersPanel = function(params) {
		var content = params['content'];
		var panelUuid = params['panelUuid'];
		var from = params['from'];
		var dateString = params['dateString'];
		var alt = params['alt'];

		var avatarPermitted;
		if ($('#avatarPermitted').length === 1) {
			avatarPermitted = true;
		} else {
			avatarPermitted = false;
		}

		var avatarOrName = "";
		if (avatarPermitted) {
			avatarOrName = "<img src=\"/direct/profile/" + from
					+ "/image\" alt=\"" + alt + "\" title=\"" + alt + "\"/>";
		} else {
			avatarOrName = "<span class=\"pc_displayname\">" + alt + "</span>";
		}

		// Escape markup
		content = content.replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(
				/</g, '&lt;').replace(/"/g, '&quot;');

		// Decode any unicode escapes
		content = JSON.parse('"' + content + '"');

		var messagePanel = $("#pc_connection_chat_" + panelUuid + "_messages");

		messagePanel.append("<li>" + avatarOrName
				+ "<div class=\"pc_message\">" + content
				+ "</div><span class=\"pc_messagedate\">" + dateString
				+ "</span></li>");
	}
	
	this.maximizeVideoCall = function (uuid){
		var remoteVideo = document.getElementById("pc_chat_" + uuid
				+ "_remote_video");
		this.videoCall.maximizeVideo (remoteVideo);
	}
	
	this.disableVideo = function (){
		this.videoCall.disableVideo ();
		$('#enable_local_video').show();
		$('#pc_chat_local_video').hide();
		$('#disable_local_video').hide();
	}
	
	this.enableVideo = function (){
		this.videoCall.enableVideo ();
		
		$('#disable_local_video').show();
		$('#pc_chat_local_video').show();
		$('#enable_local_video').hide();
	}
	
	this.mute = function (){
		this.videoCall.mute ();
		$('#unmute_local_audio').show();
		$('#mute_local_audio').hide();
	}
	
	this.unmute = function (){
		this.videoCall.unmute ();
		$('#mute_local_audio').show();
		$('#unmute_local_audio').hide();
	}

	this.openVideoCall = function(uuid, incomming) {
		if (incomming && this.videoOff)
			return;
		// If a chat window is already open for this sender, show video.
		var messagePanel = $("#pc_chat_with_" + uuid);
		if (!messagePanel.length) {
			// No current chat window for this sender. Create one.
			portalChat.setupChatWindow(uuid, true);
		}

		if (incomming) {
			$('#pc_connection_' + uuid + '_videoin').show();
		} else {
			this.videoCall
					.doCall(
							uuid,
							portalChat.getVideoAgent(uuid),
							function(uuid) {
								portalChat.setVideoStatus(uuid,
										"Waiting for peer...", true);
							},
							function(uuid) {
								portalChat
										.setVideoStatus(
												uuid,
												"Call accepted, establishing connection ",
												true);
								portalChat.showVideoCall(uuid);
							}, function(uuid) {
								portalChat.setVideoStatus(uuid,
										"Call not accepted or failed", true);
								portalChat.closeVideoCall(uuid);
							});
		}
	}

	this.acceptVideoCall = function(uuid) {
		this.videoCall.doAnswer(uuid, portalChat.getVideoAgent(uuid), function(
				uuid) {
			portalChat.setVideoStatus(uuid, "Connecting ...", true);
		}, function(uuid) {
			portalChat.setVideoStatus(uuid, "Connection established", true);
			portalChat.showVideoCall(uuid)
		}, function() {
			portalChat.setVideoStatus(uuid, "Call failed", true);
			portalChat.closeVideoCall(uuid);
		});
	}

	this.receiveVideoCall = function(uuid) {
		$('#pc_connection_' + uuid + '_videoin').show();
	}

	this.ignoreVideoCall = function(uuid) {
		$('#pc_connection_' + uuid + '_videoin').hide();
		this.setVideoStatus(uuid, "You ignored that call", true);
		this.videoCall.refuseCall(uuid);
	}

	this.showVideoCall = function(uuid) {
		var chatDiv = $("#pc_chat_with_" + uuid);
		$("#pc_chat_" + uuid + "_video_content").show();
		if (chatDiv.hasClass('pc_minimised')) {
			portalChat.toggleChatWindow(uuid);
		}
		if (chatDiv.css('height') != 'auto') {
			chatDiv.css('height', '512px');
			chatDiv.css('margin-top', '-192px');
		}
		chatDiv.attr('data-height', '512');
		$('#pc_connection_' + uuid + '_videoin').hide();
		$('#pc_connection_' + uuid + '_videochat_bar .video_off').hide();
		$('#pc_connection_' + uuid + '_videochat_bar .video_on').show();
		portalChat.showMyVideo();
	}

	this.closeVideoCall = function(uuid) {
		portalChat.setVideoStatus(uuid, "You have hung up!", true);
		this.videoCall.doClose(uuid);
		this.hideVideoCall(uuid);
	}

	this.showMyVideo = function() {
		$('#pc_chat_local_video_content').show();
		$('#pc_chat_local_video_content').attr('data-video',($('#pc_chat_local_video_content').attr('data-video')-0)+1);
		if (!portalChat.expanded) {
			portalChat.toggleChat();
		}
	}

	this.hideMyVideo = function() {
		$('#pc_chat_local_video_content').attr('data-video',($('#pc_chat_local_video_content').attr('data-video')-0)-1);
		if ($('#pc_chat_local_video_content').attr('data-video')=='0') {
		    $('#pc_chat_local_video_content').hide();
		}
	}
	
	this.hideVideoCall = function(uuid) {
		$("#pc_chat_" + uuid + "_video_content").hide();
		$("#pc_chat_with_" + uuid).css('height', '318px');
		$("#pc_chat_with_" + uuid).css('margin-top', '0px');
		$("#pc_chat_with_" + uuid).attr('data-height', '318');
		$('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
		$('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
		portalChat.hideMyVideo();
	}

	this.hasVideoAgent = function(uuid) {
		return this.getVideoAgent(uuid)!='none';
	}

	this.getVideoAgent = function(uuid) {
		return portalChat.currentConnectionsMap[uuid].video;
	}

	this.setVideoStatus = function(uuid, text, display) {
		if (display) {
			$(
					"#pc_connection_"
							+ uuid
							+ "_videochat_bar > .pc_chat_video_statusbar > span")
					.text(text);
			$(
					"#pc_connection_" + uuid
							+ "_videochat_bar > .pc_chat_video_statusbar")
					.show();
		} else {
			$(
					"#pc_connection_" + uuid
							+ "_videochat_bar > .pc_chat_video_statusbar")
					.hide();
		}

	}

	this.init = function() {

		$(document)
				.ready(
						function() {

							if (portal.loggedIn) {

								/* Initialize videoChat */
								if (typeof VideoCall === 'function') {
									portalChat.videoCall = new VideoCall();
	
									portalChat.videoCall.onHangUp = function(userid) {
										portalChat.setVideoStatus(userid,
												"User has hung up", true);
										portalChat.videoCall.doClose(userid);
										portalChat.hideVideoCall(userid);
									};
	
									portalChat.videoCall.init(portalChat);
								}

								$('#chatToggle').click(function() {
									portalChat.toggleChat();
									// $('#presenceArea').toggle();
								});

								var myCurrentConnectionsString = sessionStorage.pcCurrentConnections;
								if (myCurrentConnectionsString) {
									portalChat.currentConnections = JSON
											.parse(myCurrentConnectionsString);
									portalChat.currentConnectionsMap = JSON
											.parse(sessionStorage.pcCurrentConnectionsMap);
									portalChat.onlineConnections = JSON
											.parse(sessionStorage.pcOnlineConnections);
								}

								var myCurrentPresentUsersString = sessionStorage.pcCurrentSiteUsers;
								if (myCurrentPresentUsersString) {
									portalChat.currentSiteUsers = JSON
											.parse(myCurrentPresentUsersString);
									// portalChat.currentSiteUsersMap =
									// JSON.parse(sessionStorage.pcCurrentSiteUsersMap);
								}

								if (!myCurrentConnectionsString
										&& !myCurrentPresentUsersString) {
									portalChat.getLatestData();
								}

								if (portalChat.getSetting('offline', true)) {
									$('#pc_go_offline_checkbox').attr(
											'checked', 'checked');
									portalChat.offline = true;
								} else {
									portalChat.offline = false;
								}

								if (portalChat
										.getSetting('showOfflineConnections')) {
									portalChat.showOfflineConnections = true;
								}

								if (portalChat.getSetting('videoOff', false)) {
									$('#pc_video_off_checkbox').attr('checked',
											'checked');
									portalChat.videoOff = true;
								} else {
									portalChat.videoOff = false;
								}

								portalChat.setGetLatestDataInterval();
							} else {
								// Not a logged in user. Clear the cached data
								// in sessionStorage.
								sessionStorage
										.removeItem('pcCurrentConnections');
								sessionStorage
										.removeItem('pcCurrentConnectionsMap');
								sessionStorage
										.removeItem('pcOnlineConnections');
								sessionStorage.removeItem('pcCurrentSiteUsers');
								sessionStorage
										.removeItem('pcCurrentSiteUsersMap');
							}

							$('#pc_showoffline_connections_checkbox')
									.click(
											function() {
												if ($(this).attr('checked') == 'checked') {
													portalChat.showOfflineConnections = true;
													portalChat
															.renderTemplate(
																	'pc_connections_template',
																	{
																		'connections' : portalChat.currentConnections
																	},
																	'pc_connections');
													var pc_users = $('#pc_users');
													if (pc_users.height() > portalChat.MAX_CONTENT_HEIGHT)
														pc_users
																.height(portalChat.MAX_CONTENT_HEIGHT);
													portalChat
															.setSetting(
																	'showOfflineConnections',
																	true);
												} else {
													portalChat.showOfflineConnections = false;
													portalChat
															.renderTemplate(
																	'pc_connections_template',
																	{
																		'connections' : portalChat.onlineConnections
																	},
																	'pc_connections');
													$('#pc_connections').css(
															'height', 'auto');
													portalChat
															.setSetting(
																	'showOfflineConnections',
																	false);
												}

												portalChat.sortConnections();
											});

							$('#pc_go_offline_checkbox')
									.click(
											function() {
												if ($(this).attr('checked') == 'checked') {
													portalChat.setSetting(
															'offline', true,
															true);
													portalChat.offline = true;

												} else {
													portalChat.setSetting(
															'offline', false);
													portalChat.offline = false;

													portalChat
															.setGetLatestDataInterval();
												}
											});

							$('#pc_video_off_checkbox')
									.click(
											function() {
												if ($(this).attr('checked') == 'checked') {
													portalChat.setSetting(
															'videoOff', true,
															true);
													portalChat.videoOff = true;
												} else {
													portalChat.setSetting(
															'videoOff', false);
													portalChat.videoOff = false;
												}
											});

							// Handle return press in the edit fields
							$('.pc_editor')
									.live(
											'keypress',
											function(e, ui) {

												if (e.keyCode == 13) {

													var editorId = e.target.id;
													// do nothing if no value
													if (e.target.value !== '') {
														var uuid = editorId
																.split("pc_editor_for_")[1];
														portalChat
																.sendMessageToUser(
																		uuid,
																		e.target.value);
													}
												}
											});

							if (portalChat.getSetting('expanded')
									&& portal.loggedIn) {
								portalChat.toggleChat();
							}

							var connectionsAvailableSetting = portalChat
									.getSetting('connectionsAvailable');
							if (connectionsAvailableSetting !== undefined
									&& connectionsAvailableSetting === false) {
								// SAK-20565. Profile2 may not be installed,so
								// no connections :(
								$('#pc_connections_wrapper').hide();
							}

							// Clear all of the intervals when the window is
							// closed
							$(window).bind('unload', function() {
								portalChat.clearGetLatestDataInterval();
							});

							$(document).bind('focus', function() {
								document.title = portalChat.originalTitle;
							});

							// Explicitly close presence panel. This also
							// handles clicks bubbled up from the close icon.
							$('#pc_title').click(function(e) {
								e.preventDefault();
								portalChat.toggleChat();
							})

							if (portalChat.currentConnections.length > 0) {
								if (portalChat.showOfflineConnections) {
									portalChat
											.renderTemplate(
													'pc_connections_template',
													{
														'connections' : portalChat.currentConnections
													}, 'pc_connections');
									$('#pc_showoffline_connections_checkbox')
											.attr('checked', 'checked');
								} else {
									portalChat
											.renderTemplate(
													'pc_connections_template',
													{
														'connections' : portalChat.onlineConnections
													}, 'pc_connections');
									$('#pc_showoffline_connections_checkbox')
											.removeAttr('checked');
								}

								var pc_users = $('#pc_users');
								if (pc_users.height() > portalChat.MAX_CONTENT_HEIGHT)
									pc_users
											.height(portalChat.MAX_CONTENT_HEIGHT);
								portalChat.sortConnections();
							}

							if (portalChat.currentSiteUsers.length > 0) {
								portalChat
										.renderTemplate(
												'pc_site_users_template',
												{
													'siteUsers' : portalChat.currentSiteUsers
												}, 'pc_site_users');
								portalChat.sortSiteUsers();
							}

							// Check if there are any messages streams active.
							// If there are, setup chat windows for each
							for ( var key in sessionStorage) {
								// Chat session key start with pcsession_
								if (key.indexOf('pcsession_') != 0) {
									continue;
								}
								var storedMessageStream = sessionStorage[key];

								if (storedMessageStream) {

									var sms = JSON.parse(storedMessageStream);

									portalChat.setupChatWindow(sms.uuid,
											sms.minimised);

									// Now we've setup the chat window we can
									// add the messages.

									var messagePanel = $("#pc_connection_chat_"
											+ sms.uuid + "_messages");

									var messages = sms.messages;

									for ( var k = 0, m = messages.length; k < m; k++) {
										var message = messages[k];
										var userId = portalChat.currentConnectionsMap[message.from].uuid;
										if (userId == undefined) {
											userId = portal.user.id
										}
										var dateString = portalChat
												.formatDate(new Date(
														parseInt(message.timestamp)));
										var fromDisplayName = portalChat.currentConnectionsMap[message.from].displayName;

										portalChat
												.appendMessageToChattersPanel({
													'content' : message.content,
													'panelUuid' : sms.uuid,
													'from' : userId,
													'dateString' : dateString,
													'fromDisplayName' : fromDisplayName
												});
									}

									portalChat.scrollToBottom(sms.uuid);
								}
							}

						});

		// 15 minutes
		$.idleTimer(900000);

		$(document).bind("idle.idleTimer", function() {
			portalChat.clearGetLatestDataInterval();
		}).bind("active.idleTimer", function() {
			portalChat.setGetLatestDataInterval();
		});

	} // init

	this.init();
}

// Portal chat depends on session storage and JSON. If either aren't
// supported in the browser, don't show it.
if (typeof sessionStorage !== 'undefined' && typeof JSON !== 'undefined') {
	var portalChat = new PortalChat();
	$(document).ready(function() {
		$('#footerAppChat').show();
	});
}
