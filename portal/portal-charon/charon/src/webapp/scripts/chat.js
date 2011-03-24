function SakaiChat() {

	this.showOfflineConnections = false;
	this.currentConnections = {};
	this.onlineConnections = [];
	this.currentConnectionsMap = {};
	this.currentSiteUsers = {};
	this.currentSiteUsersMap = {};
	this.expanded = false;
	this.offline = false;
	this.currentChats = [];
	this.getLatestDataInterval = null;
	this.connectionErrors = 0;
	this.MAX_CONTENT_HEIGHT = 250;
	this.originalTitle = document.title;

    /**
     *  Utility for rendering trimpath templates. Takes the id of the template,
     *  an object with the data to be mixed in, and the id of the element to render into
     */
	this.renderTemplate = function (templateId,contextObject,outputId) {
   		var templateNode = document.getElementById(templateId);
   		var firstNode = templateNode.firstChild;

   		var template = null;

   		if ( firstNode && firstNode.nodeType === 8 ) {
       		template = firstNode.data.toString();
   		} else {
       		template = templateNode.innerHTML.toString();
   		}

   		var trimpathTemplate = TrimPath.parseTemplate(template,templateId);
   
   		var render = trimpathTemplate.process(contextObject);

   		if (outputId) {
       		document.getElementById(outputId).innerHTML = render;
   		}
   
   		return render;
	}

	this.sendMessageToUser = function (uuid,content) {

		jQuery.ajax({
			url : "/direct/portal-chat/new",
			dataType : "text",
			cache: false,
			type: 'POST',
			data: {
				'uuid':uuid,
				'message':content
			},
			success : function (text,status) {
				var messagePanel = $("#pc_connection_chat_" + uuid + "_messages");

				if('OFFLINE' === text) {
					var toDisplayName = sakaiChat.currentConnectionsMap[uuid].displayName;
					messagePanel.append("<div><br /></div>");
					messagePanel.append("<div><span class=\"pc_displayname\">" + toDisplayName + " is offline</span></div>");
				} else {
					var date = new Date();
					sakaiChat.addToMessageStream(uuid,{'from':portal.user.id,'content':content,'timestamp':date.getTime()});
					var dateString = sakaiChat.formatDate(new Date());
					messagePanel.append("<div><span class=\"pc_messagedate\">(" + dateString + "</span><span class=\"pc_displayname\">You)</span><span class=\"pc_message\">" + content + "</span></div>");
				    $('#pc_editor_for_' + uuid).val('');
				}

				sakaiChat.scrollToBottom(uuid);
			},
			error : function (xhr,textStatus,error) {

		        if(403 == xhr.status) {
                    sakaiChat.handleSecurityError();
                }

				alert("Failed to send message. Reason: " + textStatus + ". Error: " + error);
			}
		});
	}

    /**
     * This is a bad news failure. Clear the getLatestData interval.
     */
    this.handleSecurityError = function () {
	    alert("Sakai security error. Maybe your Sakai session has timed out?");
	    sakaiChat.clearGetLatestDataInterval();
        return;
    }

    /**
     * If one doesn't exist already, creates a window for the chatter represented by uuid.
     */
	this.setupChatWindow  = function (uuid,minimised) {

		var connection = this.currentConnectionsMap[uuid];

        // This should never happen, but sometimes it does ...
        if(!connection) {
            return false;
        }

		// Create the div target

		var id = "pc_chat_with_" + uuid;

		// If we already have a chat window, return.
		if($('#' + id).length) return false;

        // Append a new chat div to the container
		$('#pc_chat_window_container').prepend("<div id=\"" + id + "\" class=\"pc_chat_window\"></div>");

		this.renderTemplate('pc_connection_chat_template',connection,id);

		this.currentChats.push(uuid);

		if(minimised) {
			$('#pc_connection_chat_' + uuid + '_content').hide();
			var chatDiv = $('#' + id);
			chatDiv.css('height','auto');
            chatDiv.addClass('pc_minimised');
		} else {
            $('#' + id).addClass('pc_maximised');
        }

		var storedMessageStream = sessionStorage[uuid];
		var sms = null;
		if(storedMessageStream) {
			sms = JSON.parse(storedMessageStream);
			sms.minimised = minimised;
		} else {
			sms = {'minimised':minimised,'messages':[]};
		}

		$('#pc_editor_for_' + uuid).focus();

		sessionStorage.setItem(uuid,JSON.stringify(sms));

        return false;
	}

	this.closeChatWindow = function (uuid) {
		var removed = -1;
		for(var i=0,j=this.currentChats.length;i<j;i++) {
			if(uuid === this.currentChats[i]) {
				removed = i;
                break;
			}
		}

		$('#pc_chat_with_' + uuid).remove();

		this.currentChats.splice(removed,1);

		sessionStorage.removeItem(uuid);

		return false;
	}

	/**
	 * Show the content, hide the expand link, show the collapse link and set the
	 * expanded flag in the settings cookie.
	 */
	this.toggleChat = function () {

		if(!this.expanded) {

			var pcc = $('#pc_content');

			pcc.show();

			this.setSetting('expanded',true);
			this.expanded = true;

            var pc_users = $('#pc_users');

			if(pc_users.height() > this.MAX_CONTENT_HEIGHT) {
				pc_users.height(this.MAX_CONTENT_HEIGHT);
			}

			$('#pc').css('height','auto');
		} else {

			$('#pc_content').hide();

			$('#pc').css('height','auto');

			this.setSetting('expanded',false);
			this.expanded = false;
		}

		return false;
	}

	this.toggleChatWindow = function (uuid) {

		var chatDiv = $('#pc_chat_with_' + uuid);

		if(chatDiv.length < 1) return;

		var storedMessageStream = sessionStorage[uuid];
		var sms = null;

		if(chatDiv.hasClass('pc_maximised')) {

			$('#pc_connection_chat_' + uuid + '_content').hide();
            chatDiv.addClass('pc_minimised');
            chatDiv.removeClass('pc_maximised');
			chatDiv.css('height','auto');
			if(storedMessageStream) {
				sms = JSON.parse(storedMessageStream);
				sms.minimised = true;
			}
			else {
				sms = {'minimised':true,'messages':[]};
			}

			sessionStorage.setItem(uuid,JSON.stringify(sms));
		}
		else {
			$('#pc_connection_chat_' + uuid + '_content').show();
            chatDiv.removeClass('pc_minimised');
            chatDiv.addClass('pc_maximised');
			chatDiv.css('height','300px');
			$('#pc_chat_with_' + uuid + ' > .pc_connection_chat_title').removeClass('pc_new_message');
			this.scrollToBottom(uuid);
			if(storedMessageStream) {
				sms = JSON.parse(storedMessageStream);
				sms.minimised = false;
			}
			else {
				sms = {'minimised':false,'messages':[]};
			}

			$('#pc_editor_for_' + uuid).focus();
		}

		sessionStorage.setItem(uuid,JSON.stringify(sms));
	}

	this.scrollToBottom = function (uuid) {
		$(document).ready(function () {
			var objDiv = document.getElementById("pc_connection_chat_" + uuid + "_messages");
			objDiv.scrollTop = 1000;
		});
	}

	/**
	 * Creates a nice, human readable date from the passed in Date object
	 */
	this.formatDate = function (date) {
		var minutes = date.getMinutes();
		if(minutes < 10) minutes = '0' + minutes;
		var hours = date.getHours();
		if(hours < 10) hours = '0' + hours;
		return hours + ":" + minutes;
	}

	/**
	 * Append a message to the messages area.
	 */
	this.appendMessage = function (message) {

		var from = message.from;
		var content = message.content;
		var timestamp = message.timestamp;

		// If a chat window is already open for this sender, append to it.
		var messagePanel = $("#pc_chat_with_" + from);

		var flashIt = false;

		if(!messagePanel.length) {
            // No current chat window for this sender. Create one.
			this.setupChatWindow(from,true);
		} else if($('#pc_connection_chat_' + from + '_content').css('display') === 'none') {
            // The sender's chat window is currently minimised so we want to flash it to
            // draw attention to it.
			flashIt = true;
		}

		if(flashIt) {
			$('#pc_chat_with_' + from + ' > .pc_connection_chat_title').addClass('pc_new_message');
		}

		var fromDisplayName = this.currentConnectionsMap[from].displayName;

		messagePanel = $("#pc_connection_chat_" + from + "_messages");

		var dateString = this.formatDate(new Date(timestamp));
		messagePanel.append("<div><span class=\"pc_messagedate\">(" + dateString + "</span><span class=\"pc_displayname\">" + fromDisplayName + ")</span><span class=\"pc_message\">" + content + "</span></div>");

		this.scrollToBottom(from);
	}

	this.updateConnections = function (connections,online) {

        var onlineConnections = [];

	
		for(var i=0,j=connections.length;i<j;i++) {
		    connections[i].online = false;
			for(var k=0,m=online.length;k<m;k++) {
				if(online[k] === connections[i].uuid) {
					connections[i].online = true;
					onlineConnections.push(connections[i]);
				}
			}
		}

		var changed = false;

		if(sakaiChat.currentConnections.length != connections.length) {
			changed = true;
		}
		else {
			for(var i = 0,j=connections.length;i<j;i++) {
				var present = false;
				var statusSame = true;
				for(var k = 0,m=sakaiChat.currentConnections.length;k<m;k++) {
					if(sakaiChat.currentConnections[k].uuid === connections[i].uuid) {
						present = true;
					 	if(sakaiChat.currentConnections[k].online != connections[i].online) {
							statusSame = false;
						}
						break;
					}
				}

				if(!present || !statusSame) {
					changed = true;
					break;
				}
			}
		}

		if(changed) {

		    sessionStorage.pcCurrentConnections = JSON.stringify(connections);
		    sessionStorage.pcOnlineConnections = JSON.stringify(onlineConnections);

			sakaiChat.currentConnections = connections;

			sakaiChat.currentConnectionsMap = {};

			for(var i=0,j=connections.length;i<j;i++) {
				sakaiChat.currentConnectionsMap[connections[i].uuid] = connections[i];
			}

			// Bit of a hack really.
			sakaiChat.currentConnectionsMap[portal.user.id] = {'displayName':'You'};

		    sessionStorage.pcCurrentConnectionsMap = JSON.stringify(sakaiChat.currentConnectionsMap);

			sakaiChat.onlineConnections = onlineConnections;

			if(sakaiChat.showOfflineConnections == true) {
				sakaiChat.renderTemplate('pc_connections_template',{'connections':sakaiChat.currentConnections},'pc_connections');
			} else {
				sakaiChat.renderTemplate('pc_connections_template',{'connections':sakaiChat.onlineConnections},'pc_connections');
			}

			sakaiChat.sortConnections();
        }
	}

    /**
     * If the list supplied differs from the current one, update the site users list
     */
	this.updateSiteUsers = function (siteUsers) {

		var changed = false;

		if(sakaiChat.currentSiteUsers.length != siteUsers.length) {
			changed = true;
		}
		else {
			for(var i = 0,j=siteUsers.length;i<j;i++) {
				var inCurrentData = false;
				for(var k = 0,m=sakaiChat.currentSiteUsers.length;k<m;k++) {
					if(sakaiChat.currentSiteUsers[k].id === siteUsers[i].id) {
						inCurrentData = true;
						break;
					}
				}

				if(!inCurrentData) {
					changed = true;
					break;
				}
			}
		}

		if(changed) {
		    sessionStorage.pcCurrentSiteUsers = JSON.stringify(siteUsers);

			sakaiChat.currentSiteUsers = siteUsers;

            if(sakaiChat.currentSiteUsers.length > 0) {
			    sakaiChat.renderTemplate('pc_site_users_template',{'siteUsers':sakaiChat.currentSiteUsers},'pc_site_users');
            } else {
                $('#pc_site_users').html('');
            }

			for(var i=0,j=sakaiChat.currentSiteUsers.length;i<j;i++) {
                sakaiChat.currentSiteUsers[i].uuid = sakaiChat.currentSiteUsers[i].id;
				sakaiChat.currentConnectionsMap[sakaiChat.currentSiteUsers[i].uuid] = sakaiChat.currentSiteUsers[i];
			}

		    sessionStorage.pcCurrentConnectionsMap = JSON.stringify(sakaiChat.currentConnectionsMap);

			sakaiChat.sortSiteUsers();
        }

	}

	this.sortConnections = function () {

		$(document).ready(function (){
            $("#pc_connections").html($(".pc_connection").sort(function (a, b) {
                var val1 = a.children[0].children[1].innerHTML;
                var val2 = b.children[0].children[1].innerHTML;
                return val1 == val2 ? 0 : val1 < val2 ? -1 : 1;
            }));
		});
	}

	this.sortSiteUsers = function () {

		$(document).ready(function (){
            $("#pc_site_users").html($(".pc_site_user").sort(function (a, b) {
                var val1 = a.children[0].children[1].innerHTML;
                var val2 = b.children[0].children[1].innerHTML;
                return val1 == val2 ? 0 : val1 < val2 ? -1 : 1;
            }));
		});
	}

    this.updateMessages = function (messages) {

		for(var i=0,j=messages.length;i<j;i++) {
			sakaiChat.appendMessage(messages[i]);
			sakaiChat.addToMessageStream(messages[i].from,messages[i]);
		}

		if(messages.length > 0) {

            $('#pc_audio_embed_wrapper').html("<embed id=\"pc_audio_embed\" src=\"/portal/scripts/audio/new_chat_message.mp3\" autostart=\"true\" autoplay=\"true\" width=\"0\" height=\"0\" loop=\"false\"/>");

			var lastMessage = messages[messages.length - 1];
			var fromDisplayName = sakaiChat.currentConnectionsMap[lastMessage.from].displayName;
			if(document.hasFocus() == false) {
				document.title = 'Message from ' + fromDisplayName;
			}
        }
    }

	this.getLatestData = function () {

        // Grab the site id from the url. We need to pass it to the presence code in the chat entity provider.
        var url = document.location.href;
        var match = /site\/([\w-]*)/.exec(url);
        var siteId = '';
        if(match && match.length == 2) siteId = match[1];

		jQuery.ajax({
			url : '/direct/portal-chat/' + portal.user.id + '/latestData.json?siteId=' + siteId + '&online=' + !sakaiChat.offline,
			dataType : "json",
			cache: false,
			success : function (data,status) {
				sakaiChat.updateMessages(data.data.messages);
                sakaiChat.updateConnections(data.data.connections,data.data.online);
                sakaiChat.updateSiteUsers(data.data.presentUsers);
			},
			error : function (xhr,textStatus,error) {

				if(403 == xhr.status) {
                    sakaiChat.handleSecurityError();
                }

				if(sakaiChat.connectionErrors >= 2) {
					sakaiChat.clearGetLatestDataInterval();
					sakaiChat.connectionErrors = 0;
					alert("getLatestMessages: It looks like the chat server is unavailable. Check your network connection.");
				} else { 
					sakaiChat.connectionErrors = sakaiChat.connectionErrors + 1;
				}
			}
		});
	}

	this.addToMessageStream = function (uuid,message) {

		var storedMessageStream = sessionStorage[uuid];
		var sms = null;
		if(storedMessageStream) {
			sms = JSON.parse(storedMessageStream);
			if(sms.messages) {
				sms.messages.push(message);
            } else {
				sms.messages = [message];
            }
		} else {
			sms = {'messages':[message]};
		}
	
		sessionStorage.setItem(uuid,JSON.stringify(sms));
	}

	this.pingConnection = function (userId) {

		jQuery.ajax({
			url : '/direct/portal-chat/' + userId + '/ping',
			dataType : "text",
			cache: false,
			success : function (text,status) {
                $('#pc_pinged_popup_' + userId).show();
                setTimeout(function () { $('#pc_pinged_popup_' + userId).fadeOut(800); },500);
			},
			error : function (xmlHttpRequest,textStatus,error) {

				if(403 == xhr.status) {
                    sakaiChat.handleSecurityError();
                }

				if(sakaiChat.connectionErrors >= 2) {
					sakaiChat.clearGetLatestDataInterval();
					sakaiChat.connectionErrors = 0;
					alert("pingConnection: It looks like the chat server is unavailable. Check your network connection.");
				} else { 
					sakaiChat.connectionErrors = sakaiChat.connectionErrors + 1;
				}
			}
		});
	}

	this.setSetting = function (setting,value) {

		var mySettings = {};
		var mySettingsString = sessionStorage.pcSettings;
		if(mySettingsString) {
			mySettings = JSON.parse(mySettingsString);
		}
		mySettings[setting] = value;
		sessionStorage.pcSettings = JSON.stringify(mySettings);
	}

	this.getSetting = function (setting) {

		var mySettings = {};
		var mySettingsString = sessionStorage.pcSettings;
		if(mySettingsString) {
			mySettings = JSON.parse(mySettingsString);
		}

		return mySettings[setting];
	}

    this.setGetLatestDataInterval = function () {
        if(sakaiChat.getLatestDataInterval === null) {
		    sakaiChat.getLatestDataInterval = window.setInterval(function () {sakaiChat.getLatestData();},5000);
        }
    }

    this.clearGetLatestDataInterval = function () {
	    window.clearInterval(sakaiChat.getLatestDataInterval);
        sakaiChat.getLatestDataInterval = null;
    }

	this.init = function () {

		$(document).ready(function () {
			
            if(portal.loggedIn) {
                // We have a logged in user. Show the chat bar.
                $('#pc').show();

                $('#pc_title').click(function () {
			        sakaiChat.toggleChat();
                });

				var myCurrentConnectionsString = sessionStorage.pcCurrentConnections;
				if(myCurrentConnectionsString) {
					sakaiChat.currentConnections = JSON.parse(myCurrentConnectionsString);
				    sakaiChat.currentConnectionsMap = JSON.parse(sessionStorage.pcCurrentConnectionsMap);
					sakaiChat.onlineConnections = JSON.parse(sessionStorage.pcOnlineConnections);
				}
                      
				var myCurrentPresentUsersString = sessionStorage.pcCurrentSiteUsers;
                if(myCurrentPresentUsersString) {
					sakaiChat.currentSiteUsers = JSON.parse(myCurrentPresentUsersString);
					//sakaiChat.currentSiteUsersMap = JSON.parse(sessionStorage.pcCurrentSiteUsersMap);
				}
                        
				if(!myCurrentConnectionsString && !myCurrentPresentUsersString) {
					sakaiChat.getLatestData();
				}

				if(sakaiChat.getSetting('offline')) {
					$('#pc_go_offline_checkbox').attr('checked','checked');
                    sakaiChat.offline = true;
				} else {
                    sakaiChat.offline = false;
				}

				if(sakaiChat.getSetting('showOfflineConnections')) {
					sakaiChat.showOfflineConnections = true;
			    }

			    sakaiChat.setGetLatestDataInterval();
            } else {
                // Not a logged in user. Clear the cached data in sessionStorage.
		        sessionStorage.removeItem('pcCurrentConnections');
		        sessionStorage.removeItem('pcCurrentConnectionsMap');
		        sessionStorage.removeItem('pcOnlineConnections');
		        sessionStorage.removeItem('pcCurrentSiteUsers');
		        sessionStorage.removeItem('pcCurrentSiteUsersMap');
            }

			$('#pc_showoffline_connections_checkbox').click(function () {
				if($(this).attr('checked') == true) {
					sakaiChat.showOfflineConnections = true;
					sakaiChat.renderTemplate('pc_connections_template',{'connections':sakaiChat.currentConnections},'pc_connections');
					var pc_users = $('#pc_users');
					if(pc_users.height() > sakaiChat.MAX_CONTENT_HEIGHT) pc_users.height(sakaiChat.MAX_CONTENT_HEIGHT);
					sakaiChat.setSetting('showOfflineConnections',true);
				} else {
					sakaiChat.showOfflineConnections = false;
					sakaiChat.renderTemplate('pc_connections_template',{'connections':sakaiChat.onlineConnections},'pc_connections');
					$('#pc_connections').css('height','auto');
					sakaiChat.setSetting('showOfflineConnections',false);
				}

				sakaiChat.sortConnections();
			});
	
			$('#pc_go_offline_checkbox').click(function () {
				if($(this).attr('checked') == true) {
					sakaiChat.setSetting('offline',true);
					sakaiChat.offline = true;

				} else {
					sakaiChat.setSetting('offline',false);
					sakaiChat.offline = false;

					sakaiChat.setGetLatestDataInterval();
				}
			});
	
			// Handle return press in the edit fields
			$('.pc_editor').live('keypress',function (e,ui) {
	
				if(e.keyCode == 13) {
					var editorId = e.target.id;
					var uuid = editorId.split("pc_editor_for_")[1];
					sakaiChat.sendMessageToUser(uuid,e.target.value);
				}
			});
	
			if(sakaiChat.getSetting('expanded') && portal.loggedIn) sakaiChat.toggleChat();
	
			// Clear all of the intervals when the window is closed
			$(window).bind('unload',function () {
				sakaiChat.clearGetLatestDataInterval();
			});

			$(document).bind('focus',function () {
				document.title = sakaiChat.originalTitle;
			});
	
			if(sakaiChat.currentConnections.length > 0) {
				if(sakaiChat.showOfflineConnections) {
					sakaiChat.renderTemplate('pc_connections_template',{'connections':sakaiChat.currentConnections},'pc_connections');
					$('#pc_showoffline_connections_checkbox').attr('checked','checked');
				} else {
					sakaiChat.renderTemplate('pc_connections_template',{'connections':sakaiChat.onlineConnections},'pc_connections');
					$('#pc_showoffline_connections_checkbox').removeAttr('checked');
				}

				var pc_users = $('#pc_users');
				if(pc_users.height() > sakaiChat.MAX_CONTENT_HEIGHT) pc_users.height(sakaiChat.MAX_CONTENT_HEIGHT);
				sakaiChat.sortConnections();
			}

			if(sakaiChat.currentSiteUsers.length > 0) {
				sakaiChat.renderTemplate('pc_site_users_template',{'siteUsers':sakaiChat.currentSiteUsers},'pc_site_users');
				sakaiChat.sortSiteUsers();
			}
	
			// Check if there are any messages streams active. If there are, setup chat windows for each
			for(var i=0,j=sakaiChat.currentConnections.length;i<j;i++) {
				var storedMessageStream = sessionStorage[sakaiChat.currentConnections[i].uuid];
	
				if(storedMessageStream) {
	
					var sms = JSON.parse(storedMessageStream);
	
					sakaiChat.setupChatWindow(sakaiChat.currentConnections[i].uuid,sms.minimised);
	
					// Now we've setup the chat window we can add the messages.
	
					var messagePanel = $("#pc_connection_chat_" + sakaiChat.currentConnections[i].uuid + "_messages");
	
					var messages = sms.messages;
	
					for(var k=0,m=messages.length;k<m;k++) {
						var message = messages[k];
						var dateString = sakaiChat.formatDate(new Date(parseInt(message.timestamp)));
						var fromDisplayName = sakaiChat.currentConnectionsMap[message.from].displayName;
						messagePanel.append("<div><span class=\"pc_messagedate\">(" + dateString + "</span><span class=\"pc_displayname\">" + fromDisplayName + ")</span><span class=\"pc_message\">" + message.content + "</span></div>");
					}

					sakaiChat.scrollToBottom(sakaiChat.currentConnections[i].uuid);
				}
			}
		});


	} //init

	this.init();
}

var sakaiChat = new SakaiChat()
