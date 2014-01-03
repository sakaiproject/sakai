/*
 * Video controller. 
 * It uses webrtc-adapter.js to handle all the communication
 */
(function () {

    if ($.browser.msie) {
        return;
    }

    portal.chat.video.currentCalls = [];
    portal.chat.video.callTimeout = 0; // Timeout in msecs
    portal.chat.video.debug = false;
    /* Define the actions executed in each event */

    // Initialize timeout
    portal.chat.video.getCallTimeout = function () {

        if (this.callTimeout == 0) {
            this.callTimeout = portal.chat.video.timeout * 1000 + portal.chat.pollInterval;
        }
        return this.callTimeout;
    };

    portal.chat.video.getCurrentCall = function (uuid) {

        return this.currentCalls[uuid];
    };

    portal.chat.video.createNewCall = function (uuid, obj) {

        if (this.debug) {
            console.log("New call to: " + uuid);
        }
        return this.currentCalls[uuid] = obj;
    };

    portal.chat.video.removeCurrentCall = function (uuid) {

        if (this.debug) {
            console.log("Remove call to: " + uuid);
        }
        delete this.currentCalls[uuid];
    };

    portal.chat.video.changeCallStatus = function (uuid, value) {

        if (this.currentCalls[uuid]) {
            if (this.debug) {
                console.log("Change status: " + uuid + ":" + value);
            }
            this.currentCalls[uuid].status = value;
        }
    };

    portal.chat.video.getCurrentCallStatus = function (uuid) {

        var val = this.currentCalls[uuid] ? this.currentCalls[uuid].status : null;
        if (this.debug) {
            console.log("Current status: " + uuid + ":" + val);
        }
        return val;
    };

    portal.chat.video.getCurrentCallTime = function (uuid) {

        var val = this.currentCalls[uuid] ? this.currentCalls[uuid].calltime : null;
        if (this.debug) {
            console.log("Current status: " + uuid + ":" + val);
        }
        return val;
    };

    portal.chat.video.doCall = function (uuid, videoAgentType, onSuccessStartCallCallback, onSuccessConnectionCallback, onFailConnectionCallback) {

        var videoCallObject = this; 

        this.webrtc.doCall(uuid, videoAgentType,
            function (userid, localMediaStream) {

                videoCallObject.startCall(userid, localMediaStream);  
                onSuccessStartCallCallback(userid);
            },
            function (userid, localMediaStream) {

                videoCallObject.successCall(userid, localMediaStream);  
                onSuccessConnectionCallback(userid);
            }, onFailConnectionCallback );
    };

    portal.chat.video.doAnswer = function (uuid, videoAgentType, onSuccessStartCall, onSuccessConnection, onFailConnection) {

        var videoCallObject = this; 

        this.webrtc.answerCall(uuid, videoAgentType,
                    function (userid, localMediaStream) {
                        videoCallObject.startAnswer(userid, localMediaStream);
                        onSuccessStartCall(userid);
                    },
                    function (userid, localMediaStream) {
                        onSuccessConnection(userid);
                        videoCallObject.successCall(userid, localMediaStream);
                    }, onFailConnection);
    };

    portal.chat.video.isVideoEnabled = function () {

        return this.webrtc.isWebRTCEnabled();
    };

    portal.chat.video.getVideoAgent = function () {

        return this.webrtc.detectedBrowser;
    };
        
    portal.chat.video.doClose = function (uuid, skipBye){

        if (!skipBye) {
            this.removeCurrentCall(uuid);
        } else {
            this.changeCallStatus(uuid, 'CANCELLING');
        }
        this.webrtc.hangUp(uuid, skipBye);
        var chatDiv = $("#pc_chat_with_" + uuid);
        chatDiv.removeClass("video_active");
        portal.chat.video.hideMyVideo();
    };

    portal.chat.video.startCall = function (uuid, localMediaStream) {

        this.showMyVideo();
        this.webrtc.attachMediaStream(document.getElementById("pc_chat_local_video"), localMediaStream);
    };

    portal.chat.video.startAnswer = function (uuid, localMediaStream) {

        this.showMyVideo();
        this.webrtc.attachMediaStream(document.getElementById("pc_chat_local_video"), localMediaStream);
    };

    portal.chat.video.maximizeVideo = function (videoElement) {
        if ("chrome" === this.getVideoAgent()) {
            videoElement.webkitRequestFullScreen();
        } else if ("firefox" === this.getVideoAgent()) {
            videoElement.mozRequestFullScreen();
        } else {
            videoElement.requestFullScreen();
        }
    };

    portal.chat.video.isFullScreenEnabled = function (uuid) {

        var fullscreenEnabled = document.fullscreenEnabled || document.mozFullScreenEnabled || document.webkitFullscreenEnabled;
        
        if (fullscreenEnabled) {
            var remoteVideo = document.getElementById("pc_chat_" + uuid + "_remote_video");
            var fullscreenElement = document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement;
        
            if (fullscreenElement === remoteVideo){
                return true
            }
        }
        
        return false;
    };


    portal.chat.video.minimizeVideo = function () {

        if ("chrome" === this.getVideoAgent()) {
             document.webkitCancelFullScreen();
        } else if ("firefox" === this.getVideoAgent()) {
            document.mozCancelFullScreen();
        } else {
            document.cancelFullScreen();
        }
    };

    portal.chat.video.successCall = function (uuid, remoteMediaStream) {
     
        this.webrtc.attachMediaStream(document.getElementById("pc_chat_" + uuid + "_remote_video"), remoteMediaStream);
        portal.chat.video.setVideoStatus (uuid, portal.chat.video.messages.pc_video_status_connection_established, "video");
            
    };

    portal.chat.video.failedCall = function (uuid) {
    };

    portal.chat.video.receiveMessage = function (uuid, message, videoAgentType) {

        this.webrtc.signalService.onReceive(uuid, message, videoAgentType);
    };

    portal.chat.video.refuseCall = function (uuid) {

        this.webrtc.signalService.send(uuid, JSON.stringify({"bye": "ignore"}));
    };

    /* It retrieves the current userid list of active webconnections */

    portal.chat.video.getActiveUserIdVideoCalls = function () {

        var currentUserIdConnections = {};
        if (this.webrtc != null) {
            currentUserIdConnections = Object.keys(this.webrtc.currentPeerConnectionsMap);
        }
        return currentUserIdConnections;	
    };


    portal.chat.video.hasVideoChatActive = function (uuid) {

        return this.getCurrentCall(uuid);
    };

    portal.chat.video.maximizeVideoCall = function (uuid) {

        var remoteVideo = document.getElementById("pc_chat_" + uuid + "_remote_video");
        this.maximizeVideo (remoteVideo);
    };

    portal.chat.video.disableVideo = function () {

        this.webrtc.disableLocalVideo();
        $('#enable_local_video').show();
        $('#pc_chat_local_video').hide();
        $('#disable_local_video').hide();
    };

    portal.chat.video.enableVideo = function () {

        this.webrtc.enableLocalVideo();
        $('#disable_local_video').show();
        $('#pc_chat_local_video').show();
        $('#enable_local_video').hide();
    };

    portal.chat.video.mute = function () {

        this.webrtc.muteLocalAudio();
        $('#unmute_local_audio').show();
        $('#mute_local_audio').hide();
    };

    portal.chat.video.unmute = function () {

        this.webrtc.unmuteLocalAudio();
        $('#mute_local_audio').show();
        $('#unmute_local_audio').hide();
    };

    portal.chat.video.onvideomessage = function (uuid, message) {

        // message function. It will send to rtc to process it
        this.receiveMessage(uuid, message, this.getRemoteVideoAgent(uuid));
    };

    portal.chat.video.currentCallsProceed = function () {

        // Go On with Current Sync Calls
        $.each(Object.keys(this.currentCalls),function (key, value) {
            if (portal.chat.video.currentCalls[value].status === "SYNC") {
                portal.chat.video.currentCalls[value].status = "ESTABLISHING";
                portal.chat.video.currentCalls[value].proceed();
            }
        });
    };

    portal.chat.video.formatTime = function (time) {

        var hours = time.getHours() - 1;
        if (hours < 10) {
            hours = '0' + hours;
        }
        if (hours === '00') {
            hours = '';
        } else {
            hours = hours + ':';
        }
        var minutes = time.getMinutes();
        if (minutes < 10) {
            minutes = '0' + minutes;
        }
        minutes = minutes + ':';
        var seconds = time.getSeconds();
        if (seconds < 10) {
            seconds = '0' + seconds;
        }
        return hours + minutes + seconds;
    };

    portal.chat.video.getCallTime = function (uuid) {

        return this.webrtc.currentPeerConnectionsMap[uuid].startTime;
    };

    portal.chat.video.updateVideoTimes = function () {

        // Update time for current calls
        $.each(Object.keys(this.webrtc.currentPeerConnectionsMap), function (key, value) {
            if (portal.chat.video.getCallTime(value)) {
                var time = new Date(new Date() - portal.chat.video.getCallTime(value));
                $('#pc_connection_' + value + '_time').html(portal.chat.video.formatTime(time));
            }
        });
    };

    portal.chat.video.hasVideoAgent = function (uuid) {

        return this.getRemoteVideoAgent(uuid) !== 'none';
    };

    portal.chat.video.getRemoteVideoAgent = function (uuid) {
    	
    	//Just check video in case we have it in our conections map list   	
    	if (portal.chat.currentConnectionsMap[uuid]){
    		return portal.chat.currentConnectionsMap[uuid].video ? portal.chat.currentConnectionsMap[uuid].video : 'none';
    	}
    	return 'none';
    };

    portal.chat.video.doTimeout = function (uuid, callTime) {

        if (portal.chat.video.getCurrentCallStatus(uuid) === "ESTABLISHING" && portal.chat.video.getCurrentCallTime (uuid) === callTime) {
            portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_call_timeout, "failed");
            portal.chat.video.doClose(uuid);
            $('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
            $('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
        }
    };

    portal.chat.video.doAnswerTimeout = function (uuid, callTime) {

        if ($('#pc_connection_' + uuid + '_videoin').is(":visible") && (!this.webrtc.currentPeerConnectionsMap[uuid] || this.getCurrentCallStatus(uuid) === "CANCELLING") && portal.chat.video.getCurrentCallTime (uuid) == callTime) {
            this.ignoreVideoCall(uuid);
        }
    };

    portal.chat.video.directVideoCall = function (uuid) {

        portal.chat.toggleChat();
        this.openVideoCall(uuid, false);
    };

    portal.chat.video.openVideoCall = function (uuid, incoming) {

        if (incoming && this.videoOff) {
            return;
        }
        // If a chat window is already open for this sender, show video.
        var messagePanel = $("#pc_chat_with_" + uuid);
        if (!messagePanel.length) {
            // No current chat window for this sender. Create one.
            portal.chat.setupChatWindow(uuid, true);
        }

        if (incoming) {
            this.showVideoCall(uuid);
            $('#pc_connection_' + uuid+ '_videochat_bar > .pc_connection_videochat_bar_left ').hide();
            $('#pc_connection_' + uuid + '_videoin').show();
        } else {
            if (!this.getCurrentCall(uuid)) {
              portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_setup, "waiting");
              this.showVideoCall(uuid);
              var callTime = new Date().getTime();
              
              this.createNewCall(uuid, { 
                      "status": "SYNC",
                      "calltime": callTime,
                      "proceed": function () {

                            portal.chat.video.doCall(
                                    uuid,
                                    portal.chat.video.getVideoAgent(uuid),
                                    function (uuid) {

                                        portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_waiting_peer, "waiting");
                                        setTimeout('portal.chat.video.doTimeout("' + uuid + '",' + callTime + ')', portal.chat.video.getCallTimeout());
                                    },
                                    function (uuid) {

                                        portal.chat.video.changeCallStatus(uuid, "ESTABLISHED");
                                        portal.chat.video.setVideoStatus(uuid,portal.chat.video.messages.pc_video_status_connection_established, "video");
                                    }, 	
                                    function (uuid) {

                                        $('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
                                        $('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
                                        $('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
                                        portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_call_not_accepted, "failed");
                                        portal.chat.video.doClose(uuid);
                                    });
                      }
              });
              // Test if destination is calling me at the same time
              // Forced if pollInterval is too large avoid wait more than 7 seconds to call.
              if (portal.chat.pollInterval > 7000) {
                  portal.chat.getLatestData();
              }
            } else {
                // You're already calling
                portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_call_in_progress, "waiting");
            }
        }
    };

    portal.chat.video.acceptVideoCall = function (uuid) {

        this.changeCallStatus(uuid,"ACCEPTED");
        if (!this.webrtc.currentPeerConnectionsMap[uuid]) {
            $('#pc_connection_' + uuid + '_videoin').hide();
            $('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
            portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_answer_timeout, "failed");
            return;
        }
        portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_setup, "waiting");
        $('#pc_connection_' + uuid + '_videoin').hide();
        
        portal.chat.video.doAnswer(uuid, portal.chat.video.getVideoAgent(uuid), function (uuid) {

            $('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
            portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_setup, "waiting");
        }, function (uuid) {

            portal.chat.video.changeCallStatus(uuid, "ESTABLISHED");
            portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_connection_established, "waiting");
        }, function () {

            $('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
            $('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
            $('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
            portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_call_failed, "failed");
            portal.chat.video.closeVideoCall(uuid);
        });
    };

    portal.chat.video.receiveVideoCall = function (uuid) {

        $('#pc_connection_' + uuid + '_videoin').show();
    };

    portal.chat.video.ignoreVideoCall = function (uuid) {

        this.changeCallStatus(uuid,"CANCELLED");
        $('#pc_connection_' + uuid + '_videoin').hide();
        this.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_you_ignored, "finished");
        portal.chat.video.refuseCall(uuid);
        portal.chat.video.closeVideoCall(uuid);
        $('#pc_connection_' + uuid+ '_videochat_bar > .pc_connection_videochat_bar_left ').show();
    };

    portal.chat.video.showVideoCall = function (uuid) {

        var chatDiv = $("#pc_chat_with_" + uuid);
        $("#pc_chat_" + uuid + "_video_content").show();
        
        if (!chatDiv.hasClass('pc_minimised')) {
            chatDiv.css('height', '512px');
            chatDiv.css('margin-top', '-212px');
        } else {
            chatDiv.css('margin-top', '49px');
        }
        
        chatDiv.addClass('video_active');
        chatDiv.attr('data-height', '512');
        
        $('#pc_connection_' + uuid + '_videochat_bar').show();
        $('#pc_connection_' + uuid + '_videoin').hide();
        $('#pc_connection_' + uuid + '_videochat_bar .video_off').hide();
        $('#pc_connection_' + uuid + '_videochat_bar .video_on').show();
    };

    portal.chat.video.closeVideoCall = function (uuid, ui) {

        if (ui) {
            portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_hangup, "finished");
        }
        portal.chat.video.doClose(uuid);
        $('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
        $('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
    };

    portal.chat.video.showMyVideo = function () {

        $('#pc_chat_local_video_content').show();
        if (!portal.chat.expanded) {
            $('#pc_content').hide();
            portal.chat.toggleChat();
        }
    };

    portal.chat.video.hideMyVideo = function () {

        if ($('.video_active').length < 1) {
            if (portal.chat.expanded) {
                $('#pc_content').show();
                portal.chat.toggleChat();
            }
            $('#pc_chat_local_video_content').hide();
        }
    };

    portal.chat.video.setVideoStatus = function (uuid, text, visibleElement) {

        if (visibleElement != null) {
            $("#pc_chat_" + uuid + "_video_content > .statusElement").hide();
            
            if (visibleElement === 'video') {
                $("#pc_chat_" + uuid + "_video_content > .pc_chat_video_remote").fadeIn();
            } else if (visibleElement === 'waiting') {
                $("#pc_chat_"+ uuid	+ "_video_content > .bubblingG").show();
            } else if (visibleElement === 'failed') {
                $("#pc_chat_"+ uuid	+ "_video_content > .pc_chat_video_failed").show();
                setTimeout('portal.chat.setupVideoChatBar("' + uuid + '",' + !portal.chat.video.hasVideoAgent(uuid) + ');', 5000);
            } else if (visibleElement === 'finished') {
                $("#pc_chat_" + uuid + "_video_content > .pc_chat_video_finished").show();
                setTimeout('portal.chat.setupVideoChatBar("' + uuid + '",' + !portal.chat.video.hasVideoAgent(uuid) + ');', 5000);
            }
        }//If any else int visible Element nothing changes
        
        $("#pc_chat_" + uuid	+ "_video_content > .pc_chat_video_statusbar > span").text(text);
        $("#pc_chat_" + uuid + "_video_content > .pc_chat_video_statusbar").show();
    };

    $(document).ready(function () {
        $('#pc_video_off_checkbox').click( function () {

            if ($(this).attr('checked') == 'checked') {
                portal.chat.setSetting('videoOff', true, true);
                portal.chat.videoOff = true;
            } else {
                portal.chat.setSetting('videoOff', false);
                portal.chat.videoOff = false;
            }
        });
    });

    portal.chat.video.webrtc.init();

    // Call Time Updater
    setInterval('portal.chat.video.updateVideoTimes();', 1000);
}) ();
