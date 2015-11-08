/**
 * Please use 4 spaces for tabs when editing this file. Hard tabs are not portable.
 *
 * Video controller. 
 * It uses webrtc-adapter.js to handle all the communication
 *
 */
(function ($) {

    var msiePattern = /.*MSIE ((\d+).\d+).*/
    if ( msiePattern.test(navigator.userAgent) ) {
        portal.chat.video.enabled=false;
        return;
    }

    portal.chat.video.statuses = {
            QUEUED: 'QUEUED',
            ESTABLISHING: 'ESTABLISHING',
            ESTABLISHED: 'ESTABLISHED',
            CANCELLING: 'CANCELLING',
            ANSWERING: 'ANSWERING',
            ACCEPTED: 'ACCEPTED'
        };
    
    portal.chat.video.currentCalls = [];
    portal.chat.video.callTimeout = portal.chat.video.timeout * 1000 + portal.chat.pollInterval;

    var debug = false;

    if (typeof console === 'undefined') debug = false;

    /* Define the actions executed in each event */

    portal.chat.video.getCurrentCall = function (peerUUID) {

        if (debug) console.debug('video.getCurrentCall(' + peerUUID + ')');

        return this.currentCalls[peerUUID];
    };

    portal.chat.video.queueNewCall = function (peerUUID, proceedable) {

        if (debug) console.debug('video.queueNewCall(' + peerUUID + ')');

        return this.currentCalls[peerUUID] = proceedable;
    };

    portal.chat.video.removeCurrentCall = function (peerUUID) {

        if (debug) console.debug('video.removeCurrentCall(' + peerUUID + ')');

        delete this.currentCalls[peerUUID];
    };

    portal.chat.video.changeCallStatus = function (peerUUID, newStatus) {

        if (debug) console.debug('video.changeCallStatus(' + peerUUID + ', ' + newStatus + ')');

        // TODO: Should we need to check whether there is a current call at this point?
        if (this.currentCalls[peerUUID]) {
            this.currentCalls[peerUUID].status = newStatus;
        }
    };

    portal.chat.video.getCurrentCallStatus = function (peerUUID) {

        if (debug) console.debug('video.getCurrentCallStatus(' + peerUUID + ')');

        var currentStatus = this.currentCalls[peerUUID] ? this.currentCalls[peerUUID].status : null;
        if (debug) console.debug("Current call status: " + currentStatus);
        return currentStatus;
    };

    portal.chat.video.getCurrentCallTime = function (peerUUID) {

        if (debug) console.debug('video.getCurrentCallTime(' + peerUUID + ')');

        var calltime = this.currentCalls[peerUUID] ? this.currentCalls[peerUUID].calltime : null;
        if (debug) console.debug("Current call time: " + calltime);
        return calltime;
    };

    portal.chat.video.getLocalVideoAgent = function () {

        if (debug) console.debug('getLocalVideoAgent');

        return this.webrtc.detectedBrowser;
    };
        
    portal.chat.video.doClose = function (peerUUID, skipBye){

        if (debug) console.debug('video.doClose(' + peerUUID + ', ' + skipBye + ')');

        if (!skipBye) {
            this.removeCurrentCall(peerUUID);
        } else {
            this.changeCallStatus(peerUUID, portal.chat.video.statuses.CANCELLING);
        }
        this.webrtc.hangUp(peerUUID, skipBye);
        var chatDiv = $PBJQ(portal.chat.domSelectors.pcChatWithPre + peerUUID);
        chatDiv.removeClass("video_active");
        portal.chat.video.hideMyVideo();
    };

    portal.chat.video.startCall = function (peerUUID, localMediaStream) {

        if (debug) console.debug('video.startCall(' + peerUUID + ')');

        this.showMyVideo();
        this.webrtc.attachMediaStream(document.getElementById(portal.chat.domNames.pcChatVideoLocalVideo), localMediaStream);
    };

    portal.chat.video.maximizeVideo = function (videoElement) {

        if (debug) console.debug('video.maximizeVideo(' + videoElement + ')');

        if ("chrome" === this.getLocalVideoAgent()) {
            videoElement.webkitRequestFullScreen();
        } else if ("firefox" === this.getLocalVideoAgent()) {
            videoElement.mozRequestFullScreen();
        } else {
            videoElement.requestFullScreen();
        }
    };

    portal.chat.video.isFullScreenEnabled = function (peerUUID) {

        if (debug) console.debug('video.isFullScreenEnabled(' + peerUUID + ')');

        var fullscreenEnabled = document.fullscreenEnabled || document.mozFullScreenEnabled || document.webkitFullscreenEnabled;
        
        if (fullscreenEnabled) {
            var remoteVideo = document.getElementById(portal.chat.domNames.pcChatRemoteVideoPre + peerUUID );
            var fullscreenElement = document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement;
        
            if (fullscreenElement === remoteVideo){
                return true
            }
        }
        
        return false;
    };

    portal.chat.video.minimizeVideo = function () {

        if (debug) console.debug('video.minimizeVideo');

        if ("chrome" === this.getLocalVideoAgent()) {
             document.webkitCancelFullScreen();
        } else if ("firefox" === this.getLocalVideoAgent()) {
            document.mozCancelFullScreen();
        } else {
            document.cancelFullScreen();
        }
    };

    portal.chat.video.successCall = function (peerUUID, remoteMediaStream) {

        if (debug) console.debug('video.successCall(' + peerUUID + ')');
     
        this.webrtc.attachMediaStream(document.getElementById(portal.chat.domNames.pcChatRemoteVideoPre + peerUUID), remoteMediaStream);
        this.setVideoStatus (peerUUID, this.messages.pc_video_status_connection_established, "video");
            
    };

    portal.chat.video.failedCall = function (peerUUID) {

        if (debug) console.debug('video.failedCall(' + peerUUID + ')');
    };

    /**
     * Retrieves the current userid list of active webconnections
     */
    portal.chat.video.getActiveUserIdVideoCalls = function () {

        if (debug) console.debug('video.getActiveUserIdVideoCalls');

        var currentUserIdConnections = {};
        if (this.webrtc != null) {
            currentUserIdConnections = Object.keys(this.webrtc.currentPeerConnectionsMap);
        }
        return currentUserIdConnections;    
    };

    portal.chat.video.hasVideoChatActive = function (peerUUID) {

        if (debug) console.debug('video.hasVideoChatActive(' + peerUUID + ')');

        return this.getCurrentCall(peerUUID);
    };

    portal.chat.video.maximizeVideoCall = function (peerUUID) {

        if (debug) console.debug('video.maximizeVideoCall(' + peerUUID + ')');

        var remoteVideo = document.getElementById(portal.chat.domNames.pcChatRemoteVideoPre + peerUUID);
        this.maximizeVideo (remoteVideo);
    };

    portal.chat.video.disableVideo = function () {

        if (debug) console.debug('video.disableVideo');

        this.webrtc.disableLocalVideo();
        $PBJQ('#enable_local_video').show();
        $PBJQ('#'+portal.chat.domNames.pcChatVideoLocalVideo).hide();
        $PBJQ('#disable_local_video').hide();
    };

    portal.chat.video.enableVideo = function () {

        if (debug) console.debug('video.enableVideo');

        this.webrtc.enableLocalVideo();
        $PBJQ('#disable_local_video').show();
        $PBJQ('#'+portal.chat.domNames.pcChatVideoLocalVideo).show();
        $PBJQ('#enable_local_video').hide();
    };

    portal.chat.video.mute = function () {

        if (debug) console.debug('video.mute');

        this.webrtc.muteLocalAudio();
        $PBJQ('#unmute_local_audio').show();
        $PBJQ('#mute_local_audio').hide();
    };

    portal.chat.video.unmute = function () {

        if (debug) console.debug('video.unmute');

        this.webrtc.unmuteLocalAudio();
        $PBJQ('#mute_local_audio').show();
        $PBJQ('#unmute_local_audio').hide();
    };

    /**
     * Calls proceed on any current calls with a status of 'QUEUED'.
     */
    portal.chat.video.callProceedOnQueuedCalls = function () {

        if (debug) console.debug('callProceedOnQueuedCalls');

        var self = this;

        $.each(Object.keys(this.currentCalls), function (key, value) {

            if (self.currentCalls[value].status === self.statuses.QUEUED) {
                self.currentCalls[value].status = self.statuses.ESTABLISHING;
                self.currentCalls[value].proceed();
            }
        });
    };

    portal.chat.video.hasRemoteVideoAgent = function (peerUUID) {

        if (debug) console.debug('video.hasRemoteVideoAgent(' + peerUUID + ')');

        return this.getRemoteVideoAgent(peerUUID) !== 'none';
    };

    portal.chat.video.getRemoteVideoAgent = function (peerUUID) {

        if (debug) console.debug('video.getRemoteVideoAgent(' + peerUUID + ')');
        
        //Just check video in case we have it in our connections map list       
        if (portal.chat.currentConnectionsMap[peerUUID]){
            return portal.chat.currentConnectionsMap[peerUUID].video ? portal.chat.currentConnectionsMap[peerUUID].video : 'none';
        }
        return 'none';
    };

    portal.chat.video.doTimeout = function (peerUUID, timeLimit) {

        if (debug) console.debug('video.doTimeout(' + peerUUID + ', ' + timeLimit + ')');

        if (this.getCurrentCallStatus(peerUUID) === this.statuses.ESTABLISHING && this.getCurrentCallTime(peerUUID) === timeLimit) {
            this.setVideoStatus(peerUUID, this.messages.pc_video_status_call_timeout, "failed");
            this.doClose(peerUUID);
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_off').show();
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_on').hide();
        }
    };

    portal.chat.video.doAnswerTimeout = function (peerUUID, timeLimit) {

        if (debug) console.debug('video.doAnswerTimeout(' + peerUUID + ', ' + timeLimit + ')');

        if ($PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).is(":visible")
                && (!this.webrtc.currentPeerConnectionsMap[peerUUID] || this.getCurrentCallStatus(peerUUID) === this.statuses.CANCELLING)
                && this.getCurrentCallTime (peerUUID) == timeLimit) {

            this.ignoreVideoCall(peerUUID);
        }
    };

    portal.chat.video.directVideoCall = function (peerUUID) {

        if (debug) console.debug('video.directVideoCall(' + peerUUID + ')');

        portal.chat.toggleChat();
        this.openVideoCall(peerUUID, false);
    };

    portal.chat.video.openVideoCall = function (peerUUID, incoming) {

        if (debug) console.debug('video.openVideoCall(' + peerUUID + ', ' + incoming + ')');

        if (incoming && this.videoOff) {
            return;
        }

        // If a chat window is already open for this sender, show video.
        var messagePanel = $PBJQ(portal.chat.domSelectors.pcChatWithPre+ peerUUID);
        if (!messagePanel.length) {
            // No current chat window for this sender. Create one.
            portal.chat.setupChatWindow(peerUUID, true);
        }

        if (incoming) {
            this.showVideoCall(peerUUID);
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' > ' + portal.chat.domSelectors.pcChatVideoBarLeft).hide();
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).show();
        } else {
            if (!this.getCurrentCall(peerUUID)) {
                this.setVideoStatus(peerUUID, portal.chat.video.messages.pc_video_status_setup, "waiting");
                this.showVideoCall(peerUUID);
                var currentTime = new Date().getTime();

                var self = this;
              
                this.queueNewCall(peerUUID, { 
                    "status": portal.chat.video.statuses.QUEUED,
                    "calltime": currentTime,
                    "proceed": function () {

                        if (debug) console.debug("proceed called on call with '" + peerUUID + "'");

                        self.webrtc.doCall(
                            peerUUID,
                            self.getLocalVideoAgent(),
                            function (peerUUID, localMediaStream) {

                                // onStartedCallback

                                if (self.debug) console.debug('webrtc: doCall onStartedCallback');

                                self.startCall(peerUUID, localMediaStream);  

                                self.setVideoStatus(peerUUID, self.messages.pc_video_status_waiting_peer, "waiting");
                                setTimeout('portal.chat.video.doTimeout("' + peerUUID + '",' + currentTime + ')', self.callTimeout);
                            },
                            function (peerUUID, localMediaStream) {

                                // onConnectedCallback

                                if (self.debug) console.debug('webrtc: doCall onConnectedCallback');

                                self.successCall(peerUUID, localMediaStream);  

                                self.changeCallStatus(peerUUID, self.statuses.ESTABLISHED);
                                self.setVideoStatus(peerUUID, self.messages.pc_video_status_connection_established, "video");
                            },  
                            function (peerUUID) {

                                // onFailedCallback

                                if (self.debug) console.debug('webrtc: doCall onFailedCallback');

                                $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' > ' + portal.chat.domSelectors.pcChatVideoBarLeft).show();
                                $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_off').show();
                                $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_on').hide();
                                self.setVideoStatus(peerUUID, self.messages.pc_video_status_call_not_accepted, "failed");
                                self.doClose(peerUUID);
                            }
                        );
                    }
                });

                // Test if destination is calling me at the same time
                // Forced if pollInterval is too large avoid wait more than 7 seconds to call.
                if (portal.chat.pollInterval > 7000) {
                    portal.chat.getLatestData();
                }
            } else {
                // You're already calling
                this.setVideoStatus(peerUUID, this.messages.pc_video_status_call_in_progress, "waiting");
            }
        }
    };

    portal.chat.video.acceptVideoCall = function (peerUUID) {

        if (debug) console.debug('video.acceptVideoCall(' + peerUUID + ')');

        this.changeCallStatus(peerUUID, this.statuses.ACCEPTED);
        if (!this.webrtc.currentPeerConnectionsMap[peerUUID]) {
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).hide();
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' > ' + portal.chat.domSelectors.pcChatVideoBarLeft).show();
            this.setVideoStatus(peerUUID, this.messages.pc_video_status_answer_timeout, "failed");
            return;
        }
        this.setVideoStatus(peerUUID, this.messages.pc_video_status_setup, "waiting");
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).hide();

        var self = this;
        
        this.webrtc.answerCall(peerUUID, function (peerUUID, localMediaStream) {

            self.showMyVideo();
            self.webrtc.attachMediaStream(document.getElementById(portal.chat.domNames.pcChatVideoLocalVideo), localMediaStream);

            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID +' > ' + portal.chat.domSelectors.pcChatVideoBarLeft).show();
            self.setVideoStatus(peerUUID, self.messages.pc_video_status_setup, "waiting");
        }, function (peerUUID, localMediaStream) {

            self.changeCallStatus(peerUUID, self.statuses.ESTABLISHED);
            self.setVideoStatus(peerUUID, self.messages.pc_video_status_connection_established, "waiting");

            self.successCall(peerUUID, localMediaStream);
        }, function () {

            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' > ' + portal.chat.domSelectors.pcChatVideoBarLeft).show();
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_off').show();
            $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_on').hide();
            self.setVideoStatus(peerUUID, self.messages.pc_video_status_call_failed, "failed");
            self.closeVideoCall(peerUUID);
        });
    };

    portal.chat.video.receiveVideoCall = function (peerUUID) {

        if (debug) console.debug('video.receiveVideoCall(' + peerUUID + ')');

        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).show();
    };

    portal.chat.video.ignoreVideoCall = function (peerUUID) {

        if (debug) console.debug('video.ignoreVideoCall(' + peerUUID + ')');

        this.changeCallStatus(peerUUID, this.statuses.CANCELLED);
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).hide();
        this.setVideoStatus(peerUUID, this.messages.pc_video_status_you_ignored, "finished");
        this.webrtc.signal(peerUUID, JSON.stringify({"bye": "ignore"}));
        this.closeVideoCall(peerUUID);
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' > ' + portal.chat.domSelectors.pcChatVideoBarLeft).show();
    };

    portal.chat.video.showVideoCall = function (peerUUID) {

        if (debug) console.debug('video.showVideoCall(' + peerUUID + ')');

        var chatDiv = $PBJQ(portal.chat.domSelectors.pcChatWithPre+ peerUUID);
        $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID).show();
        
        if (!chatDiv.hasClass(portal.chat.domNames.pcMinimised)) {
            chatDiv.css('height', '512px');
            chatDiv.css('margin-top', '-212px');
        } else {
            chatDiv.css('margin-top', '49px');
        }
        
        chatDiv.addClass('video_active');
        chatDiv.attr('data-height', '512');
        
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID).show();
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).hide();
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_off').hide();
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_on').show();
    };

    portal.chat.video.closeVideoCall = function (peerUUID, ui) {

        if (debug) console.debug('video.closeVideoCall(' + peerUUID + ', ' + ui + ')');

        if (ui) {
            this.setVideoStatus(peerUUID, this.messages.pc_video_status_hangup, "finished");
        }
        this.doClose(peerUUID);
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_off').show();
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_on').hide();
    };

    portal.chat.video.showMyVideo = function () {

        if (debug) console.debug('video.showMyVideo');

        $PBJQ(portal.chat.domSelectors.pcChatLocalVideoContent).show();
        if (!portal.chat.expanded) {
            $PBJQ(portal.chat.domSelectors.pcChatContent).hide();
            portal.chat.toggleChat();
        }
    };

    portal.chat.video.hideMyVideo = function () {

        if (debug) console.debug('video.hideMyVideo');

        if ($PBJQ('.video_active').length < 1) {
            if (portal.chat.expanded) {
                $PBJQ(portal.chat.domSelectors.pcChatContent).show();
                portal.chat.toggleChat();
            }
            $PBJQ(portal.chat.domSelectors.pcChatLocalVideoContent).hide();
        }
    };

    portal.chat.video.setVideoStatus = function (peerUUID, text, state) {

        if (debug) console.debug('video.setVideoStatus(' + peerUUID + ', ' + text + ', ' + state + ')');

        if (state != null) {
            $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID +' > '+ portal.chat.domSelectors.pcChatVideoStatusElement).hide();
            
            if (state === 'video') {
                $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID +' > ' +portal.chat.domSelectors.pcChatVideoRemote).fadeIn();
            } else if (state === 'waiting') {
                $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID +' > '+ portal.chat.domSelectors.pcChatBubbling).show();
            } else if (state === 'failed') {
                $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID +' > ' + portal.chat.domSelectors.pcChatVideoFailed).show();
                setTimeout('portal.chat.setupVideoChatBar("' + peerUUID + '",' + !this.hasRemoteVideoAgent(peerUUID) + ');', 5000);
            } else if (state === 'finished') {
                $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID +' > ' + portal.chat.domSelectors.pcChatVideoFinished).show();
                setTimeout('portal.chat.setupVideoChatBar("' + peerUUID + '",' + !this.hasRemoteVideoAgent(peerUUID) + ');', 5000);
            }
        }
        
        $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID +' > ' + portal.chat.domSelectors.pcChatVideoStatusBar +' > span').text(text);
        $PBJQ(portal.chat.domSelectors.pcChatVideoContentPre + peerUUID +' > ' + portal.chat.domSelectors.pcChatVideoStatusBar).show();
    };

    $PBJQ(document).ready(function () {

        $PBJQ(portal.chat.domSelectors.pcVideoOffCheckOff).click( function () {

            if ($PBJQ(this).prop('checked')) {
                portal.chat.setSetting('videoOff', true, /* localStorage */ true);
                portal.chat.videoOff = true;
            } else {
                portal.chat.setSetting('videoOff', false, /* localStorage */ true);
                portal.chat.videoOff = false;
            }
        });
    });

    portal.chat.video.webrtc.init();
}) ($PBJQ);
