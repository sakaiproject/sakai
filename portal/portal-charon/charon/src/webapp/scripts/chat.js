(function ($) {

    "use strict";

    portal.chat.debug = false;

    if (typeof console === 'undefined') portal.chat.debug = false;

    // Portal chat depends on session storage and JSON. If either aren't
    // supported in the browser, return without doing anything.
    if (typeof sessionStorage === 'undefined' || typeof JSON === 'undefined') {
        return;
    }

	portal.chat.showOfflineConnections = false;
	portal.chat.currentConnections = {};
	portal.chat.onlineConnections = [];
	portal.chat.currentConnectionsMap = {};
	portal.chat.currentSiteUsers = {};
	portal.chat.currentSiteUsersMap = {};
	portal.chat.expanded = false;
	portal.chat.offline = false;
	portal.chat.currentChats = [];
	portal.chat.getLatestDataInterval = null;
	portal.chat.connectionErrors = 0;
	portal.chat.MAX_CONTENT_HEIGHT = 250;
	portal.chat.originalTitle = document.title;
    portal.chat.connectionsAvailable = true;
	portal.chat.videoOff = false;
	portal.chat.openWindows = 0;

    /**
     *  Utility for rendering trimpath templates. Takes the id of the template,
     *  an object with the data to be mixed in, and the id of the element to render into
     */
	portal.chat.renderTemplate = function (templateId, contextObject, outputId) {

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
    };

    portal.chat.sendMessageToUser = function (peerUUID, content) {

        $.ajax({
            url : "/direct/portal-chat/new",
            dataType : "text",
            cache: false,
            type: 'POST',
            data: {
                'to': peerUUID,
                'message': content
            },
			success : function (text, status) {
				var messagePanel = $("#pc_connection_chat_" + peerUUID + "_messages");

				if ('OFFLINE' === text) {
					var toDisplayName = portal.chat.currentConnectionsMap[peerUUID].displayName;
					messagePanel.append("<div><br /></div>");
					messagePanel.append("<div><span class=\"pc_displayname\">" + toDisplayName + " is offline</span></div>");
				} else {
					var date = new Date();
					portal.chat.addToMessageStream(peerUUID, {'from': portal.user.id, 'content': content, 'timestamp': date.getTime()});
					var dateString = portal.chat.formatDate(new Date());

                    portal.chat.appendMessageToChattersPanel({'content': content, 'panelUuid': peerUUID, 'from': portal.user.id, 'dateString': dateString, 'fromDisplayName': 'You'});

                    $('#pc_editor_for_' + peerUUID).val('');
				}

				portal.chat.scrollMessageWindowToBottom(peerUUID);
			},
			error : function (xhr, textStatus, error) {

                if (403 === xhr.status) {
                    portal.chat.handleSecurityError();
                }

                alert(portal.chat.translations.server_error_send + textStatus + portal.chat.translations.server_error_send_error + error);
            }
        });
    };

	/**
	 * This is a bad news failure. Clear the getLatestData interval.
	 */
	portal.chat.handleSecurityError = function () {

        portal.chat.clearGetLatestDataInterval();
        return;
    };

    /**
     * If one doesn't exist already, creates a window for the chatter represented by uuid.
     */
	portal.chat.setupChatWindow  = function (peerUUID, minimised) {

		var connection = this.currentConnectionsMap[peerUUID];

        // This should never happen, but sometimes it does ...
        if (!connection) {
            return false;
        }

		// Create the div target

		var id = "pc_chat_with_" + peerUUID;

		// If we already have a chat window, return.
		if ($('#' + id).length) {
            return false;
        }

        // Append a new chat div to the container
		$('#pc_chat_window_scroller').prepend("<div id=\"" + id + "\" class=\"pc_chat_window\" data-height=\"300\"></div>");
		this.openWindows += 1;
		var openSize = ((262 * this.openWindows) + 50);
		$('#pc_chat_window_scroller').css("width", openSize + "px");
		$('#pc_chat_window_container').css("right", "225px");
		if ($("#footerAppTray").position().left < openSize) {
			$("#pc_chat_scroll_bar").show();
		}

		this.renderTemplate('pc_connection_chat_template', connection, id);

		this.currentChats.push(peerUUID);

		if (minimised) {
			$('#pc_connection_chat_' + peerUUID + '_content').hide();
			var chatDiv = $('#' + id);
			chatDiv.css('height', 'auto');
            chatDiv.addClass('pc_minimised');
		} else {
            $('#' + id).addClass('pc_maximised');
        }

		var chatSessionString = sessionStorage['pcsession_' + peerUUID];
        var  chatSession;
		if (chatSessionString) {
            chatSession = JSON.parse(chatSessionString);
            // There is currently a message stream for this chatter
			chatSession.minimised = minimised;
		} else {
            // There is no message stream for this chatter. Set up a new one.
			chatSession = {'peerUUID': peerUUID, 'minimised': minimised, 'messages': []};
		}

		sessionStorage.setItem('pcsession_' + peerUUID, JSON.stringify(chatSession));

		$('#pc_editor_for_' + peerUUID).focus();
		// Test if video is enabled

        if (portal.chat.video.enabled) {
            if (portal.chat.debug) console.debug('Setting up the video chat bar ...');
            this.setupVideoChatBar(peerUUID, !portal.chat.video.webrtc.isVideoEnabled() || !portal.chat.video.hasRemoteVideoAgent(peerUUID), minimised);
        }

        return false;
	};

	portal.chat.setupVideoChatBar = function (peerUUID, notEnabled, minimised) {

        if (portal.chat.debug) console.debug('setupVideoChatBar');

        if (portal.chat.video.enabled && portal.chat.video.getCurrentCallStatus(peerUUID)) {
            if (portal.chat.debug) console.debug('Returning from setupVideoChatBar without doing anything ...');
            return;
        }

        $('#pc_chat_' + peerUUID + '_video_content').hide();
        $('#pc_connection_' + peerUUID + '_videoin').hide();
        $('#pc_connection_' + peerUUID + '_videochat_bar .video_on').hide();
        var chatDiv = $('#pc_chat_with_' + peerUUID);
        var isMinimised = (typeof minimised === "undefined") ? chatDiv.hasClass('pc_minimised') : minimised;
        if (notEnabled) {
			$('#pc_connection_' + peerUUID + '_videochat_bar').hide();
			chatDiv.attr('data-height', '300');
			if (!isMinimised) {
				chatDiv.css('height', '300px');
				chatDiv.css('margin-top', '0px');
			}
		} else {
			$('#pc_connection_' + peerUUID + '_videochat_bar').show();
			if (!isMinimised) {
				chatDiv.css('height', '318px');
				chatDiv.css('margin-top', '-18px');
			} else {
                if (chatDiv.hasClass('video_active')) {
                    chatDiv.css('margin-top', '49px');
                } else {
                    chatDiv.css('margin-top', '241px');
                }
            }
            chatDiv.attr('data-height', '318');
        }

        if (portal.chat.debug) console.debug('Finished setting up video chat bar.');
    };

	portal.chat.closeChatWindow = function (peerUUID) {

		var currentCallStatus = portal.chat.video.enabled ? portal.chat.video.getCurrentCallStatus(peerUUID) : null;
        if (portal.chat.video && currentCallStatus && currentCallStatus !== portal.chat.video.statuses.ESTABLISHED) {
            // Call in progress wait to close
            return;
        }
        var removed = -1;
        for (var i=0,j=this.currentChats.length;i<j;i++) {
            if (peerUUID === this.currentChats[i]) {
                removed = i;
                break;
            }
        }
		
		if (portal.chat.video.enabled) {
			portal.chat.video.setVideoStatus(peerUUID, portal.chat.video.messages.pc_video_status_hangup, "finished");
			portal.chat.video.closeVideoCall(peerUUID);			
		}

		$('#pc_chat_with_' + peerUUID).remove();
		this.openWindows -= 1;
		var openSize = ((262 * this.openWindows) + 50);
		$('#pc_chat_window_scroller').css("width", openSize + "px");
		var right = $("#pc_chat_window_container").css("right");
		right = right.substring(0,right.indexOf("px"))-0;
		if (right!=225) {
			$("#pc_chat_window_container").css("right", (right + 262) + "px");
		}
		if ($("#footerAppTray").position().left > openSize) {
			$("#pc_chat_scroll_bar").hide();
		}

		this.currentChats.splice(removed, 1);

		sessionStorage.removeItem('pcsession_' + peerUUID);

		return false;
	};

	/**
	 * Show the content, hide the expand link, show the collapse link and set the
	 * expanded flag in the settings cookie.
	 */
	portal.chat.toggleChat = function () {

		if (!this.expanded) {
			var pc = $('#pc');

			pc.show();

			this.setSetting('expanded', true);
			this.expanded = true;

            var pc_users = $('#pc_users');

			if (pc_users.height() > this.MAX_CONTENT_HEIGHT) {
				pc_users.height(this.MAX_CONTENT_HEIGHT);
			}

			$('#pc').css('height', 'auto');
		} else {
			$('#pc').hide();

			$('#pc').css('height', 'auto');

			this.setSetting('expanded', false);
			this.expanded = false;
		}

		return false;
	};

	portal.chat.toggleChatWindow = function (peerUUID) {

		var chatDiv = $('#pc_chat_with_' + peerUUID);

		if (chatDiv.length < 1) return;

		var chatSessionString = sessionStorage['pcsession_' + peerUUID];
		var chatSession;

		if (chatDiv.hasClass('pc_maximised')) {
			$('#pc_connection_chat_' + peerUUID + '_content').hide();
            chatDiv.addClass('pc_minimised');
            chatDiv.removeClass('pc_maximised');
			chatDiv.css('height','auto');
			if (chatDiv.hasClass('video_active')) {
				chatDiv.css('margin-top', '49px');
			} else {
				chatDiv.css('margin-top', ((chatDiv.attr('data-height') > 300) ? '241' : '260') + 'px');
			}
			if (chatSessionString) {
                chatSession = JSON.parse(chatSessionString);
                chatSession.minimised = true;
            } else {
                chatSession = {'peerUUID': peerUUID, 'minimised': true, 'messages': []};
            }
        } else {
            $('#pc_connection_chat_' + peerUUID + '_content').show();
            chatDiv.removeClass('pc_minimised');
            chatDiv.addClass('pc_maximised');
            chatDiv.css('height', chatDiv.attr('data-height') + 'px');
            if (chatDiv.hasClass('video_active')) {
                chatDiv.css('margin-top', '-212px');
            } else {
                chatDiv.css('margin-top', ((chatDiv.attr('data-height') > 300) ? '-18':'0') + 'px');
            }
			$('#pc_chat_with_' + peerUUID + ' > .pc_connection_chat_title').removeClass('pc_new_message');
			this.scrollMessageWindowToBottom(peerUUID);
            if (chatSessionString) {
                chatSession = JSON.parse(chatSessionString);
                chatSession.minimised = false;
            } else {
                chatSession = {'peerUUID': peerUUID, 'minimised': false, 'messages': []};
            }

            $('#pc_editor_for_' + peerUUID).focus();
        }

        sessionStorage.setItem('pcsession_' + peerUUID, JSON.stringify(chatSession));
    };

	portal.chat.scrollMessageWindowToBottom = function (uuid) {

		$(document).ready(function () {

			var objDiv = document.getElementById("pc_connection_chat_" + uuid + "_messages");

            if (objDiv != null) {
                // Arbitrary. Just nice and big.
                objDiv.scrollTop = 100000;
            }
		});
	};

	/**
	 * Creates a nice, human readable date from the passed in Date object
	 */
	portal.chat.formatDate = function (date) {

		var minutes = date.getMinutes();
		if (minutes < 10) {
            minutes = '0' + minutes;
        }
		var hours = date.getHours();
		if (hours < 10) {
            hours = '0' + hours;
        }
		return hours + ":" + minutes;
	};

	/**
	 * Append a message to the messages area.
	 */
	portal.chat.appendMessage = function (message) {

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
            // The sender's chat window is currently minimised so we want to flash it to
            // draw attention to it.
			flashIt = true;
		}

		if (flashIt) {
			$('#pc_chat_with_' + from + ' > .pc_connection_chat_title').addClass('pc_new_message');
		}

		var fromDisplayName = this.currentConnectionsMap[from].displayName;
        var userId =this.currentConnectionsMap[from].uuid;
		messagePanel = $("#pc_connection_chat_" + from + "_messages");

		var dateString = this.formatDate(new Date(timestamp));
        portal.chat.appendMessageToChattersPanel({'content': content, 'panelUuid': from, 'from': from, 'dateString': dateString, 'fromDisplayName': fromDisplayName});

		this.scrollMessageWindowToBottom(from);
	};

	portal.chat.updateConnections = function (connections, online) {

        var getOnlineConnections = function (connections, online) {

            var onlineConnections = [];
	
            for (var i=0,j=connections.length;i<j;i++) {
                connections[i].online = false;
                connections[i].video = 'none';
                for (var k=0,m=online.length;k<m;k++) {
                    if (online[k].id === connections[i].uuid) {
                        connections[i].online = true;
                        connections[i].video = online[k].video;
                        onlineConnections.push(connections[i]);
                    }
                }
            }

            return onlineConnections;
        };

        var onlineConnections = getOnlineConnections(connections, online);

		var changed = false;

		if (portal.chat.currentConnections.length != connections.length) {
			changed = true;
		}
		else {
			for (var i = 0,j=connections.length;i<j;i++) {
				var present = false;
				var statusSame = true;
				for (var k = 0,m=portal.chat.currentConnections.length;k<m;k++) {
					if (portal.chat.currentConnections[k].uuid === connections[i].uuid) {
						present = true;
						if (portal.chat.currentConnections[k].online != connections[i].online || portal.chat.currentConnections[k].video != connections[i].video) {
							if (connections[i].video!='none') {
								portal.chat.setupVideoChatBar(portal.chat.currentConnections[k].uuid, false,$('#pc_chat_with_'+ portal.chat.currentConnections[k].uuid).css('height') == 'auto');
							} else {
								if (portal.chat.video.enabled && !portal.chat.video.hasVideoChatActive(portal.chat.currentConnections[k].uuid)) {
									portal.chat.setupVideoChatBar(portal.chat.currentConnections[k].uuid, true);
								}
							}
							statusSame=false;
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
            sessionStorage.pcOnlineConnections = JSON.stringify(onlineConnections);

            portal.chat.currentConnections = connections;

            portal.chat.currentConnectionsMap = {};

            for (var i2=0,j2=connections.length;i2<j2;i2++) {
                portal.chat.currentConnectionsMap[connections[i2].uuid] = connections[i2];
            }

            // Bit of a hack really.
            portal.chat.currentConnectionsMap[portal.user.id] = {'displayName':'You'};

            sessionStorage.pcCurrentConnectionsMap = JSON.stringify(portal.chat.currentConnectionsMap);

            portal.chat.onlineConnections = onlineConnections;

            if (portal.chat.showOfflineConnections === true) {
                portal.chat.renderTemplate('pc_connections_template',{'connections':portal.chat.currentConnections},'pc_connections');
            } else {
                portal.chat.renderTemplate('pc_connections_template',{'connections':portal.chat.onlineConnections},'pc_connections');
            }

            portal.chat.sortConnections();
        }
    };

	/**
     * If the list supplied differs from the current one, update the site users list
     */
	portal.chat.updateSiteUsers = function (siteUsers) {

		var changed = false;

		if (portal.chat.currentSiteUsers.length != siteUsers.length) {
			changed = true;
		}
		else {
			for (var i=0,j=siteUsers.length;i<j;i++) {
				var inCurrentData = true;
				for (var k=0,m=portal.chat.currentSiteUsers.length;k<m;k++) {
					if (portal.chat.currentSiteUsers[k].id === siteUsers[i].id) {
						if (portal.chat.currentSiteUsers[k].video != siteUsers[i].video) {
							inCurrentData = false;
							if (siteUsers[i].video!='none') {
								portal.chat.setupVideoChatBar(portal.chat.currentSiteUsers[k].uuid,
										false,$('#pc_chat_with_'+ portal.chat.currentSiteUsers[k].uuid).css('height') == 'auto');
							} else {
								if (!portal.chat.video.hasVideoChatActive(portal.chat.currentSiteUsers[k].id)) {
									portal.chat.setupVideoChatBar(portal.chat.currentSiteUsers[k].uuid,true);
								}
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

            portal.chat.currentSiteUsers = siteUsers;

            if (portal.chat.currentSiteUsers.length > 0) {
                portal.chat.renderTemplate('pc_site_users_template',{'siteUsers':portal.chat.currentSiteUsers},'pc_site_users');
            } else {
                $('#pc_site_users').html('');
            }

			for (var i2=0,j2=portal.chat.currentSiteUsers.length;i2<j2;i2++) {
                portal.chat.currentSiteUsers[i2].uuid = portal.chat.currentSiteUsers[i2].id;
				portal.chat.currentConnectionsMap[portal.chat.currentSiteUsers[i2].uuid] = portal.chat.currentSiteUsers[i2];
			}

            sessionStorage.pcCurrentConnectionsMap = JSON.stringify(portal.chat.currentConnectionsMap);

            portal.chat.sortSiteUsers();
        }
    };

    portal.chat.sortConnections = function () {

		$(document).ready(function (){

            $("#pc_connections").html($(".pc_connection").sort(function (a, b) {
                var val1 = a.children[0].children[1].innerHTML;
                var val2 = b.children[0].children[1].innerHTML;
                return val1 == val2 ? 0 : val1 < val2 ? -1 : 1;
            }));
		});
	};

    portal.chat.sortSiteUsers = function () {

		$(document).ready(function (){

            $("#pc_site_users").html($(".pc_site_user").sort(function (a, b) {
                var val1 = a.children[0].children[1].innerHTML;
                var val2 = b.children[0].children[1].innerHTML;
                return val1 == val2 ? 0 : val1 < val2 ? -1 : 1;
            }));
		});
	};

    portal.chat.updateMessages = function (messages) {

		for (var i=0,j=messages.length;i<j;i++) {
			portal.chat.appendMessage(messages[i]);
			portal.chat.addToMessageStream(messages[i].from,messages[i]);
		}

		if (messages.length > 0) {

			var lastMessage = messages[messages.length - 1];
			var fromDisplayName = portal.chat.currentConnectionsMap[lastMessage.from].displayName;
			if (document.hasFocus() === false) {
				document.title = 'Message from ' + fromDisplayName;
			}
        }
    };

    portal.chat.updateVideoMessages = function (messages) {

		for (var i=0,j=messages.length; i<j;i++) {
            this.video.webrtc.onReceive(messages[i].from, messages[i]);
		}

		portal.chat.video.callProceedOnQueuedCalls();
	};

	portal.chat.getLatestData = function () {

        var onlineString = portal.chat.offline ? 'false' : 'true';
		var videoAgent = (this.video.enabled && !this.videoOff) ? this.video.getLocalVideoAgent() : 'none';

		$.ajax({
			url : '/direct/portal-chat/' + portal.user.id + '/latestData.json?auto=true&siteId=' + portal.siteId + '&online=' + onlineString + '&videoAgent=' + videoAgent,
			dataType : "json",
			cache: false,
			success : function (data, status) {
				if (data.data.messages) {
                    portal.chat.updateMessages(data.data.messages);
                }
				if (portal.chat.video.enabled && data.data.videoMessages) {
                    portal.chat.updateVideoMessages(data.data.videoMessages);
                }

                // SAK-20565. Profile2 may not be installed, so no connections :(
                if (portal.chat.connectionsAvailable === true) {
                    if (data.data.connectionsAvailable) {
                        $('#pc_connections_wrapper').show();
                        portal.chat.updateConnections(data.data.connections, data.data.online);
                    } else {
                        $('#pc_connections_wrapper').hide();
                        // No point checking again as profile2 can't be installed without a full restart
                    }
                }

                if (data.data.showSiteUsers) {
                    $('.pc_users_wrapper').show();
                    portal.chat.updateSiteUsers(data.data.presentUsers);
                } else {
                    $('.pc_users_wrapper').hide();
                }

                var totalChattable = data.data.online ? data.data.online.length : 0;

                // SAK-22260. Don't count the same person twice ...
                if (data.data.showSiteUsers && data.data.presentUsers) {
                    for (var i=0,j=data.data.presentUsers.length;i<j;i++) {
                        var presentUser = data.data.presentUsers[i];
                        var alreadyIn = false;
                        for (var k=0,m=data.data.online.length;k<m;k++) {
                            if (presentUser.id === data.data.online[k].id) {
                                alreadyIn = true;
                                break;
                            }
                        }
                        if (alreadyIn === false) {
                            totalChattable += 1;
                        }
                    }
                }

                if (totalChattable > 0) {
                    $('#chattableCount').html(totalChattable + '');
                    $('#chattableCount').removeClass('empty').addClass('present');
                } else {
                    $('#chattableCount').html(' ');
                    $('#chattableCount').removeClass('present').addClass('empty');
                }

                $('.pc_user_video_link').toggle(videoAgent !== 'none');
			},
			error : function (xhr,textStatus,error) {

				if (403 == xhr.status) {
                    portal.chat.handleSecurityError();
                    return;
                }

				if (portal.chat.connectionErrors >= 2) {
					portal.chat.clearGetLatestDataInterval();
					portal.chat.connectionErrors = 0;
                    alert(portal.chat.translations.server_unavailable);
				} else { 
					portal.chat.connectionErrors = portal.chat.connectionErrors + 1;
				}
			}
		});
	};

	portal.chat.addToMessageStream = function (peerUUID, message) {

		var chatSessionString = sessionStorage['pcsession_' + peerUUID];
		var chatSession;
		if (chatSessionString) {
            chatSession = JSON.parse(sessionStorage['pcsession_' + peerUUID]);
			if (chatSession.messages) {
                chatSession.messages.push(message);
            } else {
				chatSession.messages = [message];
            }
		} else {
			chatSession = {'peerUUID': peerUUID, 'minimised': false, 'messages': [message]};
		}

		sessionStorage.setItem('pcsession_' + peerUUID, JSON.stringify(chatSession));
	};

	portal.chat.pingConnection = function (connectionUserId) {

		$.ajax({
			url : '/direct/portal-chat/' + connectionUserId + '/ping',
			dataType : "text",
			cache: false,
			success : function (text,status) {
                $('#pc_pinged_popup_' + connectionUserId).show();
                setTimeout(function () { $('#pc_pinged_popup_' + connectionUserId).fadeOut(800); },500);
			},
			error : function (xmlHttpRequest,textStatus,error) {

				if (403 == xhr.status) {
                    portal.chat.handleSecurityError();
                }

				if (portal.chat.connectionErrors >= 2) {
					portal.chat.clearGetLatestDataInterval();
					portal.chat.connectionErrors = 0;
                    alert(portal.chat.translations.server_unavailable);
				} else { 
					portal.chat.connectionErrors = portal.chat.connectionErrors + 1;
				}
			}
		});
	};

    portal.chat.setSetting = function (setting, value, persistent) {

        var storage = (persistent) ? localStorage : sessionStorage;

		var mySettings = {};
		var mySettingsString = storage.pcSettings;
		if (mySettingsString) {
			mySettings = JSON.parse(mySettingsString);
		}
		mySettings[setting] = value;
		storage.pcSettings = JSON.stringify(mySettings);
	};

	portal.chat.getSetting = function (setting, persistent) {

        var storage = (persistent) ? localStorage : sessionStorage;

		var mySettings = {};
		var mySettingsString = storage.pcSettings;
		if (mySettingsString) {
			mySettings = JSON.parse(mySettingsString);
		}

		return mySettings[setting];
	};

    portal.chat.setGetLatestDataInterval = function () {

        if (portal.chat.getLatestDataInterval === null) {
            portal.chat.getLatestDataInterval = window.setInterval(function () {portal.chat.getLatestData();}, portal.chat.pollInterval);
        }
    };

    portal.chat.clearGetLatestDataInterval = function () {

        window.clearInterval(portal.chat.getLatestDataInterval);
        portal.chat.getLatestDataInterval = null;
    };

    portal.chat.appendMessageToChattersPanel = function (params) {

        var content = params.content;
        var panelUuid = params.panelUuid;
        var from = params.from;
        var dateString = params.dateString;
        var alt = params.alt;

        var avatarPermitted;
        if ($('#avatarPermitted').length === 1) {
            avatarPermitted =true;
        } else {
            avatarPermitted =false;
        }

        var avatarOrName = "";
        if (avatarPermitted) {
            avatarOrName = "<img src=\"/direct/profile/" + from + "/image\" alt=\"" + alt + "\" title=\"" + alt + "\"/>";
        } else {
            avatarOrName="<span class=\"pc_displayname\">" + alt + "</span>";
        }

        // Escape markup
        content = content.replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(/</g, '&lt;').replace(/"/g, '&quot;');

        // Decode any unicode escapes
        content = JSON.parse('"' + content + '"');

		var messagePanel = $("#pc_connection_chat_" + panelUuid + "_messages");

		messagePanel.append("<li>"+ avatarOrName + "<div class=\"pc_message\">" + content + "</div><span class=\"pc_messagedate\">" + dateString + "</span></li>");
    };

    $(document).ready(function () {
        
        if (portal.loggedIn) {
            $('#chatToggle').click(function () {
                portal.chat.toggleChat();
            });

            var myCurrentConnectionsString = sessionStorage.pcCurrentConnections;
            if (myCurrentConnectionsString) {
                portal.chat.currentConnections = JSON.parse(myCurrentConnectionsString);
                portal.chat.currentConnectionsMap = JSON.parse(sessionStorage.pcCurrentConnectionsMap);
                portal.chat.onlineConnections = JSON.parse(sessionStorage.pcOnlineConnections);
            }
                  
            var myCurrentPresentUsersString = sessionStorage.pcCurrentSiteUsers;
            if (myCurrentPresentUsersString) {
                portal.chat.currentSiteUsers = JSON.parse(myCurrentPresentUsersString);
            }
                    
            if (!myCurrentConnectionsString && !myCurrentPresentUsersString) {
                portal.chat.getLatestData();
            }

            if (portal.chat.getSetting('offline', true)) {
                $('#pc_go_offline_checkbox').prop('checked', true);
                portal.chat.offline = true;
            } else {
                portal.chat.offline = false;
            }

            if (portal.chat.getSetting('showOfflineConnections')) {
                portal.chat.showOfflineConnections = true;
            }

            if (portal.chat.video.enabled && portal.chat.video.webrtc.isVideoEnabled()) { 
                if (portal.chat.getSetting('videoOff',true)) {
                    $('#pc_video_off_checkbox').prop('checked', true);
                    portal.chat.videoOff = true;
                } else {
                    portal.chat.videoOff = false;
                }
            } else {
                $('#pc_video_off_ctrl').hide();
            }

            portal.chat.setGetLatestDataInterval();
        } else {
            // Not a logged in user. Clear the cached data in sessionStorage.
            sessionStorage.removeItem('pcCurrentConnections');
            sessionStorage.removeItem('pcCurrentConnectionsMap');
            sessionStorage.removeItem('pcOnlineConnections');
            sessionStorage.removeItem('pcCurrentSiteUsers');
            sessionStorage.removeItem('pcCurrentSiteUsersMap');
        }

        $('#pc_showoffline_connections_checkbox').click(function () {

            if ($(this).prop('checked')) {
                portal.chat.showOfflineConnections = true;
                portal.chat.renderTemplate('pc_connections_template',{'connections':portal.chat.currentConnections},'pc_connections');
                var pc_users = $('#pc_users');
                if (pc_users.height() > portal.chat.MAX_CONTENT_HEIGHT) pc_users.height(portal.chat.MAX_CONTENT_HEIGHT);
                portal.chat.setSetting('showOfflineConnections',true);
            } else {
                portal.chat.showOfflineConnections = false;
                portal.chat.renderTemplate('pc_connections_template',{'connections':portal.chat.onlineConnections},'pc_connections');
                $('#pc_connections').css('height','auto');
                portal.chat.setSetting('showOfflineConnections',false);
            }

            portal.chat.sortConnections();
        });

        $('#pc_go_offline_checkbox').click(function () {

            if ($(this).prop('checked')) {
                portal.chat.setSetting('offline', true, true);
                portal.chat.offline = true;
            } else {
                portal.chat.setSetting('offline', false, true);
                portal.chat.offline = false;
                portal.chat.setGetLatestDataInterval();
            }
        });

		// Handle return press in the edit fields
		var keyPressFunction = function (e,ui) {

			if (e.keyCode == 13) {
				var editorId = e.target.id;
                //do nothing if no value
				if (e.target.value !== '') {
                    var uuid = editorId.split("pc_editor_for_")[1];
                    portal.chat.sendMessageToUser(uuid, e.target.value);
                }
			}
		};

		// SAK-25505 - Switch from live() to on()
		if ($(document).on) {
			$(document).on('keypress', '.pc_editor', keyPressFunction);
		} else {
			$('.pc_editor').live('keypress', keyPressFunction);
		}

        if (portal.chat.getSetting('expanded') && portal.loggedIn) {
            portal.chat.toggleChat();
        }

        var connectionsAvailableSetting = portal.chat.getSetting('connectionsAvailable');
        if (connectionsAvailableSetting !== undefined && connectionsAvailableSetting === false) {
            // SAK-20565. Profile2 may not be installed,so no connections :(
           $('#pc_connections_wrapper').hide();
        }

        // Will be set by getLatestData with the right value.
        $('.pc_users_wrapper').hide();

        // Clear all of the intervals when the window is closed
        $(window).bind('unload', function () {
            portal.chat.clearGetLatestDataInterval();
        });

        $(document).bind('focus', function () {
            document.title = portal.chat.originalTitle;
        });

        // Explicitly close presence panel. This also handles clicks bubbled up from the close icon.
        $('#pc_title').click(function (e) {

            if ($(e.target).is('img')) {
                return;
            }
            e.preventDefault();
            if (portal.chat.video) {

                if ($('#pc_content').is(':visible')) {
                    $('#pc_content').hide();
                } else {
                    $('#pc_content').show();
                }
            } else {
                portal.chat.toggleChat();
            }
        });

        $('#pc_title_close').click(function (e){

            e.preventDefault();
            portal.chat.toggleChat();
        });

        if (portal.chat.currentConnections.length > 0) {
            if (portal.chat.showOfflineConnections) {
                portal.chat.renderTemplate('pc_connections_template',{'connections':portal.chat.currentConnections},'pc_connections');
                $('#pc_showoffline_connections_checkbox').prop('checked', true);
            } else {
                portal.chat.renderTemplate('pc_connections_template',{'connections':portal.chat.onlineConnections},'pc_connections');
                $('#pc_showoffline_connections_checkbox').prop('checked', false);
            }

            var pc_users = $('#pc_users');
            if (pc_users.height() > portal.chat.MAX_CONTENT_HEIGHT) pc_users.height(portal.chat.MAX_CONTENT_HEIGHT);
            portal.chat.sortConnections();
        }

        if (portal.chat.currentSiteUsers.length > 0) {
            portal.chat.renderTemplate('pc_site_users_template',{'siteUsers':portal.chat.currentSiteUsers},'pc_site_users');
            portal.chat.sortSiteUsers();
        }

        // Check if there are any messages streams active. If there are, setup chat windows for each
        for (var key in sessionStorage) {
            // Chat session key start with pcsession_
            if (key.indexOf('pcsession_') !== 0) {
                continue;
            }
            var storedMessageStream = sessionStorage[key];

            if (storedMessageStream) {
                var sms = JSON.parse(storedMessageStream);

                portal.chat.setupChatWindow(sms.peerUUID, sms.minimised);

                // Now we've setup the chat window we can add the messages.

                var messagePanel = $("#pc_connection_chat_" + sms.peerUUID + "_messages");

                var messages = sms.messages;

                for (var k=0,m=messages.length;k<m;k++) {
                    var message = messages[k];
                    var userId =portal.chat.currentConnectionsMap[message.from].uuid;
                    if (userId === undefined) {
                        userId=portal.user.id;
                    }
                    var dateString = portal.chat.formatDate(new Date(parseInt(message.timestamp, 10)));
                    var fromDisplayName = portal.chat.currentConnectionsMap[message.from].displayName;

                    portal.chat.appendMessageToChattersPanel({'content': message.content, 'panelUuid': sms.peerUUID, 'from': userId, 'dateString': dateString, 'fromDisplayName': fromDisplayName});
                }

                portal.chat.scrollMessageWindowToBottom(sms.peerUUID);
            }
        }
        
        $("#goright").click(function () {

            var freeSpace = $("#footerAppTray").position().left;
            var openSize = $('#pc_chat_window_scroller').css("width");
            openSize = openSize.substring(0,openSize.indexOf("px")) - 0;
            var right = $("#pc_chat_window_container").css("right");
            right = right.substring(0, right.indexOf("px")) - 0;
            if (openSize > freeSpace) {
                if (right == 225) {
                    return;
                }
                $("#pc_chat_window_container").css("right",(right + 262) + "px");
            }
        });
        
        $("#goleft").click(function () {

            var freeSpace = $("#footerAppTray").position().left;
            var openSize = $('#pc_chat_window_scroller').css("width");
            openSize = openSize.substring(0, openSize.indexOf("px")) - 0;
            var right = $("#pc_chat_window_container").css("right");
            right = right.substring(0, right.indexOf("px")) - 0;
            if (openSize > freeSpace) {
                if (openSize + right - 225 < freeSpace) return;
                $("#pc_chat_window_container").css("right",(right - 262) + "px");
            }
        });
    }); // document.ready

    // 15 minutes
    $.idleTimer(900000);

    $(document).bind("idle.idleTimer", function () {
        portal.chat.clearGetLatestDataInterval();
    }).bind("active.idleTimer", function () {
        portal.chat.setGetLatestDataInterval();
    });

}) (jQuery);

$(document).ready(function () {
    jQuery('#footerAppChat').show();
});
