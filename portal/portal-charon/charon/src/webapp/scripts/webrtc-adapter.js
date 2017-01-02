/**
 * Please use 4 spaces for tabs when editing this file. Hard tabs are not portable.
 */
(function ($) {

    portal.chat.video.webrtc = {};

    // RTCPeerConnection constructor function. Gets set to the correct browser function.
    portal.chat.video.webrtc.PeerConnection = null;

    portal.chat.video.webrtc.detectedBrowser = null;
    portal.chat.video.webrtc.detectedBrowserVersion = null;

    portal.chat.video.webrtc.currentPeerConnectionsMap = {};
    portal.chat.video.webrtc.localMediaStream = null;
    portal.chat.video.webrtc.debug = false;

    if (typeof console === 'undefined') portal.chat.video.webrtc.debug = false;

    // Set this to true if you want to support firefox, but its implementation
    // is still immature and some performance problems have been detected.
    portal.chat.video.webrtc.firefoxAllowed = true;

    /**
     * Default ice server. This will potentially be overwritten by the
     * servers call in init.
     */
    portal.chat.video.webrtc.pc_config = {
            'iceServers': [{'url': 'stun:stun.l.google.com:19302'}]
        };

    portal.chat.video.webrtc.signal = function (peerUUID, content) {

        if (this.debug) console.debug('webrtc.signal(' + peerUUID + ', ' + content + ')');

        var video = portal.chat.video;

        $PBJQ.ajax({
            url: '/direct/portal-chat/new',
            dataType: 'text',
            cache: false,
            type: 'POST',
            data: {
                'to': peerUUID,
                'message': content,
                'siteId': portal.siteId,
                'video': true
            },
            success: function (text, status) {
                if ('OFFLINE' === text) {
                    /* The peer is disconnected you can close the connection */
                    video.setVideoStatus(peerUUID, video.messages.pc_video_status_user_hung, 'finished');
                    video.closeVideoCall(peerUUID);
                }
            },
            error: function (xhr, textStatus, error) {

                if (403 === xhr.status) {
                    portal.chat.handleSecurityError();
                }

                alert('Failed to send signal. Reason: ' + textStatus + '. Error: ' + error);
            }
        });
    };

    portal.chat.video.webrtc.init = function () {
        if (this.debug) console.debug('webrtc.init');

        this.PeerConnection = (window.PeerConnection || window.webkitPeerConnection00 || window.webkitRTCPeerConnection || window.mozRTCPeerConnection);
        navigator.getUserMedia = (navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia);
        RTCIceCandidate = (window.mozRTCIceCandidate || window.RTCIceCandidate);
        RTCSessionDescription = (window.mozRTCSessionDescription || window.RTCSessionDescription); // order is very important: "RTCSessionDescription" defined in Nighly but useless


        if (navigator.webkitGetUserMedia) {

          this.detectedBrowser = 'chrome';
          this.detectedBrowserVersion = parseInt(navigator.userAgent.match(/Chrom(e|ium)\/([0-9]+)\./)[2], 10);

          if (this.detectedBrowserVersion < 26){
              this.detectedBrowser = 'none';
              return "";
          }

          if (!webkitMediaStream.prototype.getVideoTracks) {
                webkitMediaStream.prototype.getVideoTracks = function() {
                    return this.videoTracks;
                };
                webkitMediaStream.prototype.getAudioTracks = function() {
                    return this.audioTracks;
                };
            }

            // New syntax of getXXXStreams method in M26.
            if (!webkitRTCPeerConnection.prototype.getLocalStreams) {
                webkitRTCPeerConnection.prototype.getLocalStreams = function() {
                    return this.localStreams;
                };
                webkitRTCPeerConnection.prototype.getRemoteStreams = function() {
                    return this.remoteStreams;
                };
            }
        } else if (navigator.mozGetUserMedia) {
            this.detectedBrowser = 'firefox';
            this.detectedBrowserVersion = parseInt(navigator.userAgent.match(/Firefox\/([0-9]+)\./)[1], 10);

            //Limit webRTC connections from firefox 25 and above
            if (this.detectedBrowserVersion < 25) {
                this.detectedBrowser = 'none';
                return "";
            }
              
            if (!MediaStream.prototype.getVideoTracks) {
                MediaStream.prototype.getVideoTracks = function() {
                    return [];
                };
            }

            if (!MediaStream.prototype.getAudioTracks) {
                MediaStream.prototype.getAudioTracks = function() {
                    return [];
                };
            }
        } else {
            this.detectedBrowser = 'none';
        }

        var self = this;

        $PBJQ.ajax({
            url: '/direct/portal-chat/' + portal.user.id + '/servers.json',
            dataType: "json",
            cache: false,
            success: function (data, status) {

                var iceServers = [];

                $PBJQ.each(data.data.iceServers, function (index, iceServer) {

                    iceServer.url = self.getUrlFromIce(iceServer);
                    if (iceServer.url !== "") {
                        iceServers.push(iceServer);
                    }
                });

                self.pc_config.iceServers = iceServers;

                if (self.debug) console.log(self.pc_config);
            }
        });

        var self = this;

        // Update the durations for current calls, every second.
        setInterval( function () {

                $PBJQ.each(Object.keys(self.currentPeerConnectionsMap), function (index, peerUUID) {

                    var startTime = self.currentPeerConnectionsMap[peerUUID].startTime;
                    var ms = (new Date()).getTime() - startTime;
                    var secs = ms / 1000;
                    ms = Math.floor(ms % 1000);
                    var mins = secs / 60;
                    secs = Math.floor(secs % 60);
                    var hours = mins / 60;
                    mins = Math.floor(mins % 60);
                    hours = Math.floor(hours % 24);

                    if (hours < 10) hours = '0' + hours;
                    if (mins < 10) mins = '0' + mins;
                    if (secs < 10) secs = '0' + secs;

                    var formattedDuration = hours + ":" + mins + ":" + secs;
                    $PBJQ(portal.chat.domSelectors.pcChatVideoTimePre + peerUUID).html(formattedDuration);
                });
            }, 1000);
    }; // init

    // This method constructs adecuated URL for current browser
    portal.chat.video.webrtc.getUrlFromIce = function (ice) {

        if (this.debug) console.debug('webrtc.getUrlFromIce(' + ice + ')');

        //No need to parse stun server if it's not a webrtc capable browser
        if (this.detectedBrowser === 'none') {
            return "";
        }

        if (ice.protocol.toLowerCase() === 'stun') {
                return ice.protocol + ':' + ice.host;
        } else {
            if (this.detectedBrowser === 'firefox') {
                return ice.protocol + ":" + ice.host;;
            } else if (this.detectedBrowser === 'chrome') {
                if (this.detectedBrowserVersion >= 28) {
                    return ice.protocol + ":" + ice.host;
                } else {
                    return ice.protocol + ':' + encodeURIComponent(ice.username) + '@' + ice.host;
                }
            }
        }
    };

    /**
     * Call this function to start a video call
     */
    portal.chat.video.webrtc.doCall = function (peerUUID, videoAgentType, onStartedCallback, onConnectedCallback, onFailedCallback) {

        if (this.debug) console.debug('webrtc.doCall(' + peerUUID + ', ' + videoAgentType + ')');

        var callConnection = this.currentPeerConnectionsMap[peerUUID];

        if (callConnection == null) {
            callConnection = this.setupPeerConnection(peerUUID, videoAgentType, onConnectedCallback, onFailedCallback);
            callConnection.isCaller = true;
        }

        if (this.localMediaStream != null) {
            onStartedCallback(peerUUID, this.localMediaStream);
            this.offerStream(callConnection, peerUUID, onConnectedCallback, onFailedCallback);
        } else {
            var self = this;
            navigator.getUserMedia(
                { audio: true, video: true }
                , function (localMediaStream) {

                    /* Call onStartedCallback function to fire rendering effects on the screen */
                    //Let's check if the connection is currently in the connection map
                    if (self.currentPeerConnectionsMap[peerUUID] != null){
                        self.localMediaStream = localMediaStream;
                        onStartedCallback(peerUUID, self.localMediaStream);
                        self.offerStream(callConnection, peerUUID, onConnectedCallback, onFailedCallback);
                    } else {
                        //In the case it does not exits and there is no more connections then stop and close the localvideo
                        var keys = Object.keys(self.currentPeerConnectionsMap);

                        if (keys.length < 1 && localMediaStream != null) {
                            localMediaStream.stop();
                            localMediaStream = null;
                        }
                    }
                }
                , function () {
                    onFailedCallback(peerUUID)
                });
        }
    };

    /**
     * Called when receiving a incoming call
     */
    portal.chat.video.webrtc.onReceiveCall = function (peerUUID) {

        if (this.debug) console.debug('webrtc.onReceiveCall(' + peerUUID + ')');

        var video = portal.chat.video;

        var callTime = null;
        if (video.getCurrentCall(peerUUID)) {
            if (video.getCurrentCallStatus(peerUUID) === video.statuses.QUEUED) {
                // I'm calling to the same user at the same time, discard !!
                video.changeCallStatus(peerUUID, video.statuses.ANSWERING);
            } else if (video.getCurrentCallStatus(peerUUID) === video.statuses.ESTABLISHING) {
                // We call each other at the same time
                if (peerUUID < portal.user.id) {
                    // I discard the call and go with the answer (do not send bye)
                    video.doClose(peerUUID, true);
                } else {
                    // I do the call discard the answer
                    return;
                }
            }
            callTime = video.getCurrentCallTime(peerUUID);

        } else {
            callTime = new Date().getTime();
            video.queueNewCall(peerUUID, {
                'status': video.statuses.ANSWERING,
                'calltime': callTime
            });
        }

        video.openVideoCall(peerUUID, true);
        video.setVideoStatus(peerUUID, video.messages.pc_video_status_incoming_call, 'waiting');
        setTimeout('portal.chat.video.doAnswerTimeout("' + peerUUID + '",' + callTime + ')', video.callTimeout);
    };

    /**
     * Call this function to start the answer process to a previous call
     */
    portal.chat.video.webrtc.answerCall = function(peerUUID, startAnswerCallback, onSuccessCallback, fail) {

        if (this.debug) console.debug('webrtc.answerCall(' + peerUUID + ')');

        var callConnection = this.currentPeerConnectionsMap[peerUUID];

        // Set up the triggered functions
        callConnection.onsuccessconn = onSuccessCallback;
        callConnection.onfailconn = fail;
        callConnection.isCaller = false;

        var self = this;

        if (self.localMediaStream != null) {
            startAnswerCallback(peerUUID, self.localMediaStream);
            self.offerStream(callConnection, peerUUID, onSuccessCallback, fail);

        } else {
            navigator.getUserMedia(
                {audio: true, video: true}
                , function (localMediaStream) {
                    /* Call started function to fire rendering effects on the screen */
                    if (self.currentPeerConnectionsMap[peerUUID] != null){
                        self.localMediaStream = localMediaStream;
                        startAnswerCallback(peerUUID, self.localMediaStream );
                        self.offerStream(callConnection, peerUUID, onSuccessCallback, fail);
                    } else {
                        //In the case it does not exits and there is no more connections then stop and close the localvideo
                        var keys = Object.keys(self.currentPeerConnectionsMap);

                        if (keys.length < 1 && localMediaStream != null) {
                            localMediaStream.stop();
                            localMediaStream = null;
                        }
                    }
                }
                , fail);
        }
    };

    /**
     * Call this function to announce you want to hangup, success callback is
     * launched when the pair get the request, fail in other case.
     */
    portal.chat.video.webrtc.hangUp = function (peerUUID, skipBye) {

        if (this.debug) console.debug('webrtc.hangUp(' + peerUUID + ', ' + skipBye + ')');

        var callConnection = this.currentPeerConnectionsMap[peerUUID];
        if (callConnection != null) {
            var peerConnection = callConnection.rtcPeerConnection;

            if (peerConnection != null) {
                peerConnection.close();
            }

            delete this.currentPeerConnectionsMap[peerUUID];

            // If it was the last connection we stop the webcam
            var keys = Object.keys(this.currentPeerConnectionsMap);
            if (keys.length < 1 && this.localMediaStream != null) {
                this.localMediaStream.stop();
                this.localMediaStream = null;
            }

            if (!skipBye) {
                this.signal(peerUUID, JSON.stringify({"bye": "bye"}));
            }
        }
    };

    /**
     * Called when a hangup request is received, or the connection is lost.
     */
    portal.chat.video.webrtc.onHangUp = function (peerUUID) {

        if (this.debug) console.debug('webrtc.onHangUp(' + peerUUID + ')');
    };

    /**
     * Called when a ignore response is received,
     */
    portal.chat.video.webrtc.onIgnore = function (peerUUID) {

        if (this.debug) console.debug('webrtc.onIgnore(' + peerUUID + ')');
    };

    /**
     * Use this helper function to hook the media stream to a specified element
     */
    portal.chat.video.webrtc.attachMediaStream = function (element, stream) {

        if (this.debug) console.debug('webrtc.attachMediaStream');

        if (this.detectedBrowser === 'firefox') {
            element.mozSrcObject = stream;
            element.play();
        } else if (this.detectedBrowser === 'chrome') {
            var url = URL || webkitURL;
            element.src = url.createObjectURL(stream);
        }
        element.play();
    };

    portal.chat.video.webrtc.offerStream = function (callConnection, peerUUID, onSuccessCallback, onFailedCallback) {

        if (this.debug) console.debug('webrtc.offerStream');

        var peerConnection = callConnection.rtcPeerConnection;

        peerConnection.addStream(this.localMediaStream);

        var self = this;
        var mediaConstraints = {
                optional: [],
                mandatory: {
                    OfferToReceiveAudio: true,
                    OfferToReceiveVideo: true
                }
       };



        if (callConnection.isCaller) {

            peerConnection.createOffer(
                function (rtcSessionDescription) {

                    // RTCSessionDescriptionCallback
                    if (self.debug) console.debug('offer created successfully');

                    // we won't call success, we will wait until peer offers the stream.
                    self.gotDescription(peerUUID, rtcSessionDescription);
                }
                , onFailedCallback
                , mediaConstraints);
        } else {
            delete mediaConstraints.optional;
            peerConnection.createAnswer(function (rtcSessionDescription) {

                self.gotDescription(peerUUID, rtcSessionDescription);

                // In this case we have to declare the success, instead
                // on addStream
                onSuccessCallback(peerUUID, callConnection.remoteMediaStream); // In this
            }, onFailedCallback);
        }
    };

    portal.chat.video.webrtc.isVideoEnabled = function () {

        if (this.debug) console.debug('webrtc.isVideoEnabled');

        return this.detectedBrowser !== 'none';
    };

    portal.chat.video.webrtc.setupPeerConnection = function (peerUUID, videoAgentType, onSuccessCallback, onFailedCallback) {

        if (this.debug) console.debug('webrtc.setupPeerConnection(' + peerUUID + ', ' + videoAgentType + ')');

        var pc_constraints = {'optional': [{
            'DtlsSrtpKeyAgreement' : true
        }]};

        var peerConnection = new this.PeerConnection(this.pc_config, pc_constraints);

        // send any ice candidates to the other peer
        var callConnection = new this.CallConnection(peerConnection, onSuccessCallback, onFailedCallback);

        callConnection.remoteVideoAgentType = videoAgentType;

        var self = this;

        peerConnection.onicechange = function (event) {

            if (self.debug) console.debug('onicechange');
        };

        peerConnection.onstatechange = function (event) {

            if (self.debug) console.debug('onstatechange');
        };

        var video = portal.chat.video;

        if ((this.detectedBrowser === 'chrome' && this.detectedBrowserVersion >= 27) || (this.detectedBrowser == 'firefox')) {

            peerConnection.oniceconnectionstatechange = function (event) {

                if (self.debug) {
                    console.debug('oniceconnectionstatechange ' + peerUUID + ' state ' + peerConnection.iceConnectionState);
                }

                if (peerConnection.iceConnectionState === 'disconnected') {
                    video.setVideoStatus(peerUUID, video.messages.pc_video_status_waiting_peer, 'waiting');

                    if (self.debug) console.debug('webrtc: iceConnectionState === disconnected. Waiting 5 seconds before retrying ...');

                    setTimeout(function () {

                        if (self.debug) console.debug('webrtc: Testing iceConnectionState again ...');

                        if (peerConnection.iceConnectionState === 'disconnected') {
                            if (video.getCurrentCallStatus(peerUUID) === video.statuses.ESTABLISHED) {
                                if (self.debug) console.debug('webrtc: iceConnectionState === disconnected still. Calling onHangUp ...');
                                self.onHangUp(peerUUID);
                            }
                        } else if (peerConnection.iceConnectionState === 'connected') {
                            if (self.debug) console.debug('webrtc: iceConnectionState === connected');
                            video.setVideoStatus(peerUUID, video.messages.pc_video_status_connection_established, 'video');
                        }
                    }, 5000);
                }
            }
        }

        var signal = this.signal;

        peerConnection.onicecandidate = function (event) {

            if (event.candidate) {
                signal(peerUUID, JSON.stringify({
                    type: 'candidate',
                    label: event.candidate.sdpMLineIndex,
                    id: event.candidate.sdpMid,
                    candidate: event.candidate.candidate
                }));
            }
        };

        peerConnection.onaddstream = function (event) {

            callConnection.remoteMediaStream = event.stream;

            if (callConnection.onsuccessconn != null) {

                // In this case we have declared what to do in case of success
                callConnection.onsuccessconn(peerUUID, event.stream);
            }
        };

        this.currentPeerConnectionsMap[peerUUID] = callConnection;

        return callConnection;
    };

    portal.chat.video.webrtc.gotDescription = function (peerUUID, rtcSessionDescription) {

        if (this.debug) console.debug('webrtc.gotDescription(' + peerUUID + ', ' + rtcSessionDescription + ')');

        var callConnection = this.currentPeerConnectionsMap[peerUUID];

        if (callConnection) {
            var peerConnection = callConnection.rtcPeerConnection;

            if (peerConnection != null) {
                peerConnection.setLocalDescription(rtcSessionDescription);
                this.signal(peerUUID, JSON.stringify({'sdp': rtcSessionDescription}));
            }
        } else {
            if (this.debug) {
                console.error("No call connection for peerUUID '" + peerUUID + "'.");
            }
            // TODO: Can this ever happen?
        }
    };

    /*
     * Called when a message is received from the signalling server (Sakai)
     */
    portal.chat.video.webrtc.onReceive = function (message) {

        var peerUUID = message.from;

        if (this.debug) console.debug('webrtc.onReceive(' + peerUUID + ')');

        var receivedSignal = JSON.parse(message.content);

        if (this.debug) console.log('SDP: ' +JSON.stringify( receivedSignal.sdp));
        if (this.debug) console.log('Candidate: ' + receivedSignal.candidate);
        if (this.debug) console.log('Bye: ' + receivedSignal.bye);

        var videoAgentType = portal.chat.video.getRemoteVideoAgent(peerUUID);

        if (receivedSignal.sdp && videoAgentType != 'none') {

            if (receivedSignal.sdp.type === 'offer') {
                if (this.debug) console.debug('webrtc: offer');
                this.onReceiveCall(peerUUID);
            }

            var callConnection = this.currentPeerConnectionsMap[peerUUID];

            if (callConnection == null) {
                callConnection = this.setupPeerConnection(peerUUID, videoAgentType);
                this.currentPeerConnectionsMap[peerUUID] = callConnection;
            }

            callConnection.rtcPeerConnection.setRemoteDescription(new RTCSessionDescription(receivedSignal.sdp));

        } else if (receivedSignal.candidate != null && videoAgentType != 'none') {
            var callConnection = this.currentPeerConnectionsMap[peerUUID];
            if (callConnection != null) {
                var peerConnection = callConnection.rtcPeerConnection;
                peerConnection.addIceCandidate(new RTCIceCandidate({
                    sdpMLineIndex : receivedSignal.label,
                    candidate : receivedSignal.candidate
                }));
            } else {
                //For now, we send a bye receivedSignal in M2 we will try to reconnect.
                this.signal(peerUUID, JSON.stringify({"bye" : "bye"}));
            }
        } else if (receivedSignal.bye != null) {
            var callConnection = this.currentPeerConnectionsMap[peerUUID];
            if (callConnection != null) {
                if (receivedSignal.bye === "bye") {
                    this.onHangUp(peerUUID);
                } else if (receivedSignal.bye === "ignore") {
                    this.onIgnore(peerUUID);
                }
                // In the case of not having a previous connection could be a
                // refuse message
            }
        }
    };

    portal.chat.video.webrtc.getInteropSDP = function (sdp) {

        if (this.debug) console.debug('webrtc.getInteropSDP(' + sdp + ')');

        var inline = 'a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abc\r\nc=IN';
        return sdp.indexOf('a=crypto') == -1 ? sdp.replace(/c=IN/g, inline) : sdp;
    };

    portal.chat.video.webrtc.enableLocalVideo = function () {

        if (this.debug) console.debug('webrtc.enableLocalVideo');

        this.localMediaStream.getVideoTracks()[0].enabled = true;
    };

    portal.chat.video.webrtc.disableLocalVideo = function () {

        if (this.debug) console.debug('webrtc.disableLocalVideo');

        this.localMediaStream.getVideoTracks()[0].enabled = false;
    };

    portal.chat.video.webrtc.muteLocalAudio = function () {

        if (this.debug) console.debug('webrtc.muteLocalAudio');

        this.localMediaStream.getAudioTracks()[0].enabled = false;
    };

    portal.chat.video.webrtc.unmuteLocalAudio = function () {

        if (this.debug) console.debug('webrtc.unmuteLocalAudio');

        this.localMediaStream.getAudioTracks()[0].enabled = true;
    };

    portal.chat.video.webrtc.onIgnore = function (peerUUID) {

        if (this.debug) console.debug('webrtc.onIgnore(' + peerUUID + ')');

        portal.chat.video.closeVideoCall(peerUUID);
        portal.chat.video.setVideoStatus(peerUUID, portal.chat.video.messages.pc_video_status_user_refused, "failed");
    };

    portal.chat.video.webrtc.onHangUp = function (peerUUID) {

        if (this.debug) console.debug('webrtc.onHangUp(' + peerUUID + ')');

        var video = portal.chat.video;

        //check if the connection you want to close is in fullScreen
        if (video.isFullScreenEnabled(peerUUID)) {
            video.minimizeVideo();
        }

        video.setVideoStatus(peerUUID, video.messages.pc_video_status_user_hung, "finished");
        video.doClose(peerUUID);
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' > ' + portal.chat.domSelectors.pcChatVideoBarLeft).show();
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_off').show();
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoChatBarPre + peerUUID + ' .video_on').hide();
        $PBJQ(portal.chat.domSelectors.pcChatVideoVideoInPre + peerUUID).hide();
    };

    /**
     * Object to handle connection and call success, fail events
     */
    portal.chat.video.webrtc.CallConnection = function (pc, success, failed) {

        this.rtcPeerConnection = pc;
        this.onsuccessconn = success;
        this.onfailedconn = failed;
        this.isCaller = null;
        this.remoteMediaStream = null;
        this.remoteVideoAgentType = null;
        this.startTime = new Date().getTime();
    };
}) ($PBJQ);
