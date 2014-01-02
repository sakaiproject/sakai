portal.chat.video.webrtc = {};
portal.chat.video.webrtc.peerConnection = null;
portal.chat.video.webrtc.detectedBrowser = null;
portal.chat.video.webrtc.currentPeerConnectionsMap = {};
portal.chat.video.webrtc.localMediaStream = null;
portal.chat.video.webrtc.debug = true;

// Set this to true if you want to support firefox, but its implementation
// is still immature and some performance problems have been detected.
portal.chat.video.webrtc.firefoxAllowed = true;

portal.chat.video.webrtc.pc_config = {
    "iceServers" : [ {
        "url": "stun:stun.l.google.com:19302"
    } ]
};

portal.chat.video.webrtc.signalService = {

        "signal": function (to, content) {

            jQuery.ajax({
                url : "/direct/portal-chat/new",
                dataType : "text",
                cache : false,
                type : 'POST',
                data : {
                    'to' : to,
                    'message' : content,
                    'video' : true
                },
                success : function (text, status) {
                    if ('OFFLINE' === text) {
                        /* The peer is disconnected you can close the connection */
                        if (portal.chat.video.enabled) {
                            portal.chat.video.setVideoStatus(to, portal.chat.video.messages.pc_video_status_user_hung, "finished");
                            portal.chat.video.closeVideoCall(to);
                        }
                    }
                },
                error : function (xhr, textStatus, error) {

                    if (403 === xhr.status) {
                        portal.chat.handleSecurityError();
                    }

                    alert("Failed to send message. Reason: " + textStatus + ". Error: " + error);
                }
            });
        },
        "send": function (userid, content) {

            this.signal(userid, content);
        },
        "onReceive": function (userid, message, videoAgentType) {

            portal.chat.video.webrtc.onReceive(userid, message, videoAgentType);
        }
    };


portal.chat.video.webrtc.init = function () {

    // First of all we try to detect which navigator is trying to use the
    // videoconference from getUserMedia diferences
    if (navigator.getUserMedia) {
        this.detectedBrowser = "webrtcenabled";
    } else if (this.firefoxAllowed && navigator.mozGetUserMedia) {
        this.detectedBrowser = "firefox";
        navigator.getUserMedia = navigator.mozGetUserMedia;
        portal.chat.video.webrtc.peerConnection = mozRTCPeerConnection;
        RTCSessionDescription = mozRTCSessionDescription;
        RTCIceCandidate = mozRTCIceCandidate;
        
        MediaStream.prototype.getVideoTracks = function () {

            return [];
        };

        MediaStream.prototype.getAudioTracks = function () {

            return [];
        };
        
        
    } else if (navigator.webkitGetUserMedia) {
        this.detectedBrowser = "chrome";
        navigator.getUserMedia = navigator.webkitGetUserMedia;
        portal.chat.video.webrtc.peerConnection = webkitRTCPeerConnection;
        
        if (!webkitRTCPeerConnection.prototype.getLocalStreams) {
            webkitRTCPeerConnection.prototype.getLocalStreams = function () {

                return this.localStreams;
            };
            webkitRTCPeerConnection.prototype.getRemoteStreams = function () {

                return this.remoteStreams;
            };
        }
    } else {
        this.detectedBrowser = "none";
    }

    var webRTCClass = this;

    jQuery.ajax({
        url : '/direct/portal-chat/' + portal.user.id + '/servers.json',
        dataType : "json",
        cache : false,
        success : function (data, status) {

            webRTCClass.pc_config = data.data;
            var iceServers = [];
            
            $.each(webRTCClass.pc_config.iceServers, function (key, value) {

                value.url = webRTCClass.getUrlFromIce(value);
                if (value.url !== "") {
                    iceServers.push(value);
                }
            });
            webRTCClass.pc_config.iceServers = iceServers;
            
            if (webRTCClass.debug) {
                console.log(webRTCClass.pc_config);
            }
        }
    });
};

// This method constructs adecuated URL for current browser
portal.chat.video.webrtc.getUrlFromIce = function (ice) {
    
    //No need to parse stun server if it's not a webrtc capable browser
    if (this.detectedBrowser === 'none') {
        return "";
    }
    
    if (ice.protocol.toLowerCase() === 'stun') {
        if (this.detectedBrowser === 'firefox' && !ice.host.match(/^([0-9]{1,3}\.?){4}(:[0-9]{1,5}){0,1}$/)) {
            // Firefox only support IP's in stun hosts
            return "";
        } else {
            return ice.protocol + ":" + ice.host;
        }
    } else {
        if (this.detectedBrowser === 'firefox') {
            // Firefox only support stun
            return "";
        } else {
            if (parseInt(navigator.userAgent.match(/Chrom(e|ium)\/([0-9]+)\./)[2]) >= 28) {
                return ice.protocol + ":" + ice.host;
            } else {
                return ice.protocol + ":" + encodeURIComponent(ice.username) + "@" + ice.host;
            }
        }
    } 
};

/* Call this process to start a video call */
portal.chat.video.webrtc.doCall = function (userid, videoAgentType, started, success, fail) {

    var callConnection = this.currentPeerConnectionsMap[userid];

    if (callConnection == null) {
        callConnection = this.setupPeerConnection(userid, videoAgentType, success, fail);
        callConnection.isCaller = true;
    }

    var webRTCClass = this;

    if (this.localMediaStream != null) {
        started(userid, webRTCClass.localMediaStream);
        webRTCClass.offerStream(callConnection, userid, success, fail);
    } else {

        navigator.getUserMedia(
            {audio: true, video: true}
            , function (localMediaStream) {

                /* Call started function to fire rendering effects on the screen */
                //Let's check if the connection is currently in the connection map
                if (webRTCClass.currentPeerConnectionsMap[userid] != null){ 
                    webRTCClass.localMediaStream = localMediaStream;
                    started(userid, webRTCClass.localMediaStream);
                    webRTCClass.offerStream(callConnection, userid, success, fail);
                } else {
                    //In the case it does not exits and there is no more connections then stop and close the localvideo
                    var keys = Object.keys(webRTCClass.currentPeerConnectionsMap);

                    if (keys.length < 1 && localMediaStream != null) {
                        localMediaStream.stop();
                        localMediaStream = null;
                    }
                }
            }
            , function () {
                fail(userid)
            });
    }
};

/*
 * Provide this function. It will be called when receiving a incoming call
 */
portal.chat.video.webrtc.onReceiveCall = function (userid) {

    var callTime = null;
    if (portal.chat.video.getCurrentCall(userid)) {
        if (portal.chat.video.getCurrentCallStatus(userid) === "SYNC") {
            // I'm calling to the same user at the same time, discard !!
            portal.chat.video.changeCallStatus(userid, "ANSWERING");
        } else if (portal.chat.video.getCurrentCallStatus(userid) === "ESTABLISHING") {
            // We call each other at the same time
            if (userid < portal.user.id) {
                // I discard the call and go with the answer (do not send bye)
                portal.chat.video.doClose(userid, true);
            } else {
                // I do the call discard the answer
                return;
            }  
        }
        callTime = portal.chat.video.getCurrentCallTime(userid);
        
    } else {
        callTime = new Date().getTime();
        portal.chat.video.createNewCall(userid, {
            'status':'ANSWERING',
            'calltime':callTime
        });
    }
    
    portal.chat.video.openVideoCall (userid,true);
    portal.chat.video.setVideoStatus(userid,portal.chat.video.messages.pc_video_status_incoming_call, "waiting");
    setTimeout('portal.chat.video.doAnswerTimeout("' + userid + '",' + callTime + ')', portal.chat.video.getCallTimeout());
};

/* Call this function to start the answer process to a previous call */

portal.chat.video.webrtc.answerCall = function(userid, videoAgentType, startAnswer, success, fail) {

    var callConnection = this.currentPeerConnectionsMap[userid];

    // Set up the triggered functions
    callConnection.onsuccessconn = success;
    callConnection.onfail = fail;
    callConnection.isCaller = false;

    var webRTCClass = this;

    if (webRTCClass.localMediaStream != null) {
        startAnswer(userid, webRTCClass.localMediaStream);
        webRTCClass.offerStream(callConnection, userid, success, fail);

    } else {
        navigator.getUserMedia(
            {audio: true, video: true}
            , function (localMediaStream) {
                /* Call started function to fire rendering effects on the screen */
                if (webRTCClass.currentPeerConnectionsMap[userid] != null){ 
                    webRTCClass.localMediaStream = localMediaStream;
                    startAnswer(userid, webRTCClass.localMediaStream );
                    webRTCClass.offerStream(callConnection, userid, success, fail);
                } else {
                    //In the case it does not exits and there is no more connections then stop and close the localvideo
                    var keys = Object.keys(webRTCClass.currentPeerConnectionsMap);

                    if (keys.length < 1 && localMediaStream != null) {
                        localMediaStream.stop();
                        localMediaStream = null;
                    }
                }
            }
            , fail);

    }
};

/*
 * That method will be called when caller receive answer onReceiveAnswer =
 * function (userid){ }
 * 
 * /*Call this function to announce you want to hangup, success callback is
 * launched when the pair get the request, fail in other case
 */
portal.chat.video.webrtc.hangUp = function (userid, skipBye) {

    var callConnection = this.currentPeerConnectionsMap[userid];
    if (callConnection != null) {
        var pc = callConnection.rtcPeerConnection;

        if (pc != null) {
            pc.close();
        }
        // If it was the last connection we stop the webcam
        delete this.currentPeerConnectionsMap[userid];
        var keys = Object.keys(this.currentPeerConnectionsMap);

        if (keys.length < 1 && this.localMediaStream != null) {
            this.localMediaStream.stop();
            this.localMediaStream = null;
        }

        if (!skipBye) {
            this.signalService.send(userid, JSON.stringify({"bye": "bye"}));
        }
    }
};

/*
 * Provide this function. It will be called when hangup request is received,
 * or connection is lost
 */
portal.chat.video.webrtc.onHangUp = function (userid) {
};

/*
 * Provide this function. It will be called when a ignore response is
 * received,
 */

portal.chat.video.webrtc.onIgnore = function (userid) {
};

/* Use this helper function to hook the media stream to a especified element */
portal.chat.video.webrtc.attachMediaStream = function (element, stream) {
    if (this.detectedBrowser === "firefox") {
        element.mozSrcObject = stream;
        element.play();
    } else if (this.detectedBrowser === "chrome") {
        element.src = webkitURL.createObjectURL(stream);
    }
    element.play();
};

portal.chat.video.webrtc.offerStream = function (callConnection, to, successCall, failedCall) {

    var pc = callConnection.rtcPeerConnection;

    pc.addStream(this.localMediaStream);

    var webRTCClass = this;

    /*
     * Declare the success function to be launched when remote stream is
     * added
     */

    if (callConnection.isCaller) {
        var constrains = {optional: []};
        
        if (this.detectedBrowser === "firefox"){
            constrains["mandatory"] = {
                    OfferToReceiveAudio: true,
                    OfferToReceiveVideo: true,
                    MozDontOfferDataChannel: true
                };
        }
        
        pc.createOffer(function (desc) {

            // we won't call success, we will wait until peer offers the
            // stream.
            webRTCClass.gotDescription(to, desc);
        }, failedCall, constrains);
    } else {
        pc.createAnswer(function (desc) {

            webRTCClass.gotDescription(to, desc);

            // In this case we have to declare the success, instead
            // on addStream
            successCall(to, callConnection.remoteMediaStream); // In this
        }, failedCall);
    }

};

/* Suport functions */

portal.chat.video.webrtc.isWebRTCEnabled = function () {

    return this.detectedBrowser !== "none";
};

portal.chat.video.webrtc.setupPeerConnection = function (userid, videoAgentType, successConn, failConn) {

    var pc_constrains = {'optional': []};

    if (videoAgentType != null) {
        /*
         * Let's start to defining some compatibility scenarios to perform
         * the negotiation If have videoAgentTupe I supose that I am the
         * caller
         */

        // Case 1 : Me = Chrome other Fireforx
        if (this.detectedBrowser === "chrome"
                && videoAgentType === "firefox") {
            pc_constrains['optional'] = [ {
                'DtlsSrtpKeyAgreement' : 'true'
            } ];
        } else if (this.detectedBrowser === "firefox" && videoAgentType === "chrome") {
            pc_constrains['optional'] = [ {
                'DtlsSrtpKeyAgreement': 'true'
            } ];
            pc_constrains['mandatory'] = {
                'MozDontOfferDataChannel': true
            };
        }
    }

    var pc = new portal.chat.video.webrtc.peerConnection(this.pc_config, pc_constrains);

    // send any ice candidates to the other peer
    var callConnection = new portal.chat.video.webrtc.CallConnection(pc, successConn, failConn);

    callConnection.remoteVideoAgentType = videoAgentType;

    pc.onicechange = function (event) {
        console.info("onicechange +" + pc.iceState);
    };

    pc.onicechange = function (event) {
        console.info("onicechange +" + pc.iceState);
    };

    pc.onstatechange = function (event) {
        console.info("onicechange +" + pc.readyState);
    };
    
    if (this.detectedBrowser === 'chrome' && parseInt(navigator.userAgent.match(/Chrom(e|ium)\/([0-9]+)\./)[2]) >= 27) {
        pc.oniceconnectionstatechange = function (event) {

            if(this.debug) {
                console.info ("oniceconnectionstatechange " + userid + " state " + pc.iceConnectionState);
            }
            
            if (pc.iceConnectionState === "disconnected") {
                portal.chat.video.setVideoStatus(userid, portal.chat.video.messages.pc_video_status_waiting_peer, "waiting");
            
                setTimeout(function () {

                    if (pc.iceConnectionState === "disconnected"){
                        var status = portal.chat.video.getCurrentCallStatus(userid);
                        if (status === "ESTABLISHED"){
                            webRTCClass.onHangUp(userid);
                        }
                    }else if (pc.iceConnectionState === "connected"){
                        portal.chat.video.setVideoStatus(userid,portal.chat.video.messages.pc_video_status_connection_established, "video");
                    }
                }, 5000);
            }
        }
    }
    
    var signalService = this.signalService;
    var webRTCClass = this;

    pc.onicecandidate = function (event) {
        if (event.candidate) {
            signalService.send(userid, JSON.stringify({
                type: 'candidate',
                label: event.candidate.sdpMLineIndex,
                id: event.candidate.sdpMid,
                candidate: event.candidate.candidate
            }));
        }
    };

    pc.onaddstream = function (event) {
        callConnection.remoteMediaStream = event.stream;

        if (callConnection.onsuccessconn != null) {

            /*
             * In this case we have declared what to do in case of success
             * connection (Offer)
             */
            callConnection.onsuccessconn(userid, event.stream);
        }
    };
    this.currentPeerConnectionsMap[userid] = callConnection;

    return callConnection;
};

portal.chat.video.webrtc.gotDescription = function (uuid, desc) {

    var callConnection = this.currentPeerConnectionsMap[uuid];

    if(callConnection) {
        var pc = callConnection.rtcPeerConnection;

        if (callConnection.remoteVideoAgentType === "chrome" && this.detectedBrowser === "firefox") {
            desc.sdp = this.getInteropSDP(desc.sdp);
        }

        if (pc != null) {
            pc.setLocalDescription(desc);
            this.signalService.send(uuid, JSON.stringify({"sdp": desc}));
        }
    } else {
        if(this.debug) {
            console.error("No call connection for uuid'" + uuid + "'.");
        }
        // TODO: Can this ever happen?
    }
};

/*
 * That function is called when the signal service receive a message
 */
portal.chat.video.webrtc.onReceive = function (from, message, videoAgentType) {

    var signal = JSON.parse(message.content);

    if (this.debug) {
        console.log(message);
    }

    if (signal.sdp) {

        if (signal.sdp.type === "offer") {
            this.onReceiveCall(from);
        }

        var callConnection = this.currentPeerConnectionsMap[from];

        if (callConnection == null) {
            callConnection = this.setupPeerConnection(from, videoAgentType);
            this.currentPeerConnectionsMap[from] = callConnection;
        }

        var pc = callConnection.rtcPeerConnection;
        pc.setRemoteDescription(new RTCSessionDescription(signal.sdp));

    } else if (signal.candidate != null) {
        var callConnection = this.currentPeerConnectionsMap[from];
        if (callConnection != null) {
            var pc = callConnection.rtcPeerConnection;
            pc.addIceCandidate(new RTCIceCandidate({
                sdpMLineIndex : signal.label,
                candidate : signal.candidate
            }));
        } else {
            //For now, we send a bye signal in M2 we will try to reconnect.
            this.signalService.send(from, JSON.stringify({"bye" : "bye"}));
        }
    } else if (signal.bye != null) {
        var callConnection = this.currentPeerConnectionsMap[from];
        if (callConnection != null) {
            if (signal.bye === "bye") {
                this.onHangUp(from);
            } else if (signal.bye === "ignore") {
                this.onIgnore(from);
            }
            // In the case of not having a previous connection could be a
            // refuse message
        }
    }
};

portal.chat.video.webrtc.getInteropSDP = function (sdp) {

    var inline = 'a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abc\r\nc=IN';
    return sdp.indexOf('a=crypto') == -1 ? sdp.replace(/c=IN/g, inline) : sdp;
};

portal.chat.video.webrtc.enableLocalVideo = function () {

    this.localMediaStream.getVideoTracks()[0].enabled = true;
};

portal.chat.video.webrtc.disableLocalVideo = function () {

    this.localMediaStream.getVideoTracks()[0].enabled = false;
};

portal.chat.video.webrtc.muteLocalAudio = function () {

    this.localMediaStream.getAudioTracks()[0].enabled = false;
};

portal.chat.video.webrtc.unmuteLocalAudio = function () {

    this.localMediaStream.getAudioTracks()[0].enabled = true;
};

portal.chat.video.webrtc.onIgnore = function (userid) {

    portal.chat.video.closeVideoCall (userid);
    portal.chat.video.setVideoStatus(userid,portal.chat.video.messages.pc_video_status_user_refused, "failed");
};

portal.chat.video.webrtc.onHangUp = function (uuid) {

    //check if the connection you want to close is in fullScreen
    if (portal.chat.video.isFullScreenEnabled(uuid)) {
        portal.chat.video.minimizeVideo();
    }
        
    portal.chat.video.setVideoStatus(uuid, portal.chat.video.messages.pc_video_status_user_hung, "finished");
    portal.chat.video.doClose(uuid);
    $('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
    $('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
    $('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
    $('#pc_connection_' + uuid + '_videoin').hide();
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
	this.startTime = new Date();
};
