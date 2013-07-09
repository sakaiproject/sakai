var RTCPeerConnection = null;

function WebRTC() {

	this.webrtcDetectedBrowser = null;
	this.currentPeerConnectionsMap = {};
	this.signalService = null;
	this.localMediaStream = null;
	this.debug = false;
	
	this.firefoxAllowed = false; //Turn that variable to true if you want to support firefox, 
								 //but its implementation is still unmature and some performance has
								 //been detected.
	

	this.pc_config = {
		"iceServers" : [ {
			"url" : "stun:stun.l.google.com:19302"
		} ]
	};// provisional, can we get a stun server?

	this.init = function(signalService) {
		// First of all we try to detect which navigator is trying to use the
		// videoconference from getUserMedia diferences
		if (navigator.getUserMedia) {
			this.webrtcDetectedBrowser = "webrtcenabled";
		}else if (this.firefoxAllowed && navigator.mozGetUserMedia ) {
			this.webrtcDetectedBrowser = "firefox";
			navigator.getUserMedia = navigator.mozGetUserMedia;
			RTCPeerConnection = mozRTCPeerConnection;
			RTCSessionDescription = mozRTCSessionDescription;
			RTCIceCandidate = mozRTCIceCandidate;
			
			MediaStream.prototype.getVideoTracks = function() {
				return [];
			};

			MediaStream.prototype.getAudioTracks = function() {
				return [];
			};
			
			
		} else if (navigator.webkitGetUserMedia) {
			this.webrtcDetectedBrowser = "chrome";
			navigator.getUserMedia = navigator.webkitGetUserMedia;
			RTCPeerConnection = webkitRTCPeerConnection;
			
		    if (!webkitRTCPeerConnection.prototype.getLocalStreams) {
		        webkitRTCPeerConnection.prototype.getLocalStreams = function() {
		            return this.localStreams;
		        };
		        webkitRTCPeerConnection.prototype.getRemoteStreams = function() {
		            return this.remoteStreams;
		        };
		    }
		} else {
			this.webrtcDetectedBrowser = "none";
		}

		this.signalService = signalService; // Use the signal service provided
		var webRTCClass = this;

		this.signalService.onReceive = function(userid, message, videoAgentType) {
			webRTCClass.onReceive(userid, message, videoAgentType); // Called
			// custom
			// method
			// when
		}
		jQuery
		.ajax({
			url : '/direct/portal-chat/' + portal.user.id + '/servers.json',
			dataType : "json",
			cache : false,
			success : function(data, status) {
				webRTCClass.pc_config = data.data;
				var iceServers = [];
				
				$.each(webRTCClass.pc_config.iceServers,function(key,value){
					value.url = webRTCClass.getUrlFromIce(value);
					if (value.url!=""){
						iceServers.push(value);
					}
				});
				webRTCClass.pc_config.iceServers = iceServers;
				
				if (webRTCClass.debug) console.log(webRTCClass.pc_config);
			}
		});
		
	}

	// This method constructs adecuated URL for current browser
	this.getUrlFromIce = function(ice) {
		if (ice.protocol.toLowerCase()=='stun') {
			if (this.webrtcDetectedBrowser=='firefox' && !ice.host.match(/^([0-9]{1,3}\.?){4}(:[0-9]{1,5}){0,1}$/)) {
				return ""; // Firefox only support IP's in stun hosts
			} else {
				return ice.protocol+":"+ice.host;
			}
		} else {
			if (this.webrtcDetectedBrowser=='firefox') {
				return ""; // Firefox only support stun
			} else {
				if (parseInt(navigator.userAgent.match(/Chrom(e|ium)\/([0-9]+)\./)[2]) >= 28) {
					return ice.protocol+":"+ice.host;
				} else {
					return ice.protocol+":"+encodeURIComponent(ice.username)+"@"+ice.host;
				}
				
			}
		} 
	}
	
	/* Call this process to start a video call */
	this.doCall = function(userid, videoAgentType, started, success, fail) {
		var callConnection = this.currentPeerConnectionsMap[userid];

		if (callConnection == null) {
			callConnection = this.setupPeerConnection(userid, videoAgentType,
					success, fail);
			callConnection.isCaller = true;

		}

		var webRTCClass = this;

		if (this.localMediaStream != null) {
			started(userid, webRTCClass.localMediaStream);
			webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.
		} else {

			navigator.getUserMedia({
				audio : true,
				video : true
			}, function(localMediaStream) {
				/* Call started function to fire rendering effects on the screen */
				//Let's check if the connection is currently in the connection map
				if (webRTCClass.currentPeerConnectionsMap[userid] != null){ 
					webRTCClass.localMediaStream = localMediaStream;
					started(userid, webRTCClass.localMediaStream);
					webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.
				// ?
				}else{
					//In the case it does not exits and there is no more connections then stop and close the localvideo
					var keys = Object.keys(webRTCClass.currentPeerConnectionsMap);

					if (keys.length < 1 && localMediaStream != null) {
						localMediaStream.stop();
						localMediaStream = null;
					}
			}

			},function(){
				fail(userid)
			});
		}
	}

	/*
	 * Provide this function. It will be called when receiving a incoming call
	 */
	this.onReceiveCall = function(userid) {

	}

	/* Call this function to start the answer process to a previous call */

	this.answerCall = function(userid, videoAgentType, startAnswer, success,
			fail) {
		var callConnection = this.currentPeerConnectionsMap[userid];

		// Set up the triggered functions
		callConnection.onsuccessconn = success;
		callConnection.onfail = fail;
		callConnection.isCaller = false;

		var webRTCClass = this;

		if (webRTCClass.localMediaStream != null) {
			startAnswer(userid, webRTCClass.localMediaStream);
			webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.

		} else {
			navigator.getUserMedia({
				audio : true,
				video : true
			}, function(localMediaStream) {
				/* Call started function to fire rendering effects on the screen */
				if (webRTCClass.currentPeerConnectionsMap[userid] != null){ 
					webRTCClass.localMediaStream = localMediaStream;
					startAnswer(userid, webRTCClass.localMediaStream );
					webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.
				}else{
					//In the case it does not exits and there is no more connections then stop and close the localvideo
						var keys = Object.keys(webRTCClass.currentPeerConnectionsMap);

						if (keys.length < 1 && localMediaStream != null) {
							localMediaStream.stop();
							localMediaStream = null;
						}
				}
				// ?
			}, fail);

		}
	}

	/*
	 * That method will be called when caller receive answer onReceiveAnswer =
	 * function (userid){ }
	 * 
	 * /*Call this function to announce you want to hangup, success callback is
	 * launched when the pair get the request, fail in other case
	 */
	this.hangUp = function(userid,skipBye) {
		var callConnection = this.currentPeerConnectionsMap[userid];
		if (callConnection != null) {
			var pc = callConnection.rtcPeerConnection;

			if (pc != null) {
				
				/*if (callConnection.remoteMediaStream != null) {
					pc.removeStream(callConnection.remoteMediaStream);
				}*/ //not sure if its necessary 

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
				this.signalService.send(userid, JSON.stringify({"bye" : "bye"}));
			}
		}
	}

	/*
	 * Provide this function. It will be called when hangup request is received,
	 * or connection is lost
	 */
	this.onHangUp = function(userid) {

	}

	/*
	 * Provide this function. It will be called when a ignore response is
	 * received,
	 */

	this.onIgnore = function(userid) {

	}

	/* Use this helper function to hook the media stream to a especified element */
	this.attachMediaStream = function(element, stream) {
		if (this.webrtcDetectedBrowser == "firefox") {
			element.mozSrcObject = stream;
			element.play();
		} else if (this.webrtcDetectedBrowser = "chrome") {
			element.src = webkitURL.createObjectURL(stream);
		}
		element.play();
	}

	this.offerStream = function(callConnection, to, successCall, failedCall) {
		var pc = callConnection.rtcPeerConnection;

		pc.addStream(this.localMediaStream);

		var webRTCClass = this;

		/*
		 * Declare the success function to be launched when remote stream is
		 * added
		 */

		if (callConnection.isCaller) {
			var constrains = {optional:[]};
			
			if (this.webrtcDetectedBrowser == "firefox"){
				constrains["mandatory"] ={
			            OfferToReceiveAudio: true,
			            OfferToReceiveVideo: true,
			            MozDontOfferDataChannel: true
			        }
			}
			
			pc.createOffer(function(desc) {

				// we won't call success, we will wait until peer offers the
				// stream.
				webRTCClass.gotDescription(to, desc);
			},failedCall,constrains);
		} else {
			pc.createAnswer(function(desc) {
				webRTCClass.gotDescription(to, desc);

				// In this case we have to declare the success, instead
				// on addStream
				successCall(to, callConnection.remoteMediaStream); // In this
			},failedCall);
		}

	}

	/* Suport functions */

	this.isWebRTCEnabled = function() {
		return this.webrtcDetectedBrowser != "none";
	}

	this.setupPeerConnection = function(userid, videoAgentType, successConn,
			failConn) {

		var pc_constrains = {
			'optional' : []
		};

		if (videoAgentType != null) {
			/*
			 * Let's start to defining some compatibility scenarios to perform
			 * the negotiation If have videoAgentTupe I supose that I am the
			 * caller
			 */

			// Case 1 : Me = Chrome other Fireforx
			if (this.webrtcDetectedBrowser === "chrome"
					&& videoAgentType === "firefox") {
				pc_constrains['optional'] = [ {
					'DtlsSrtpKeyAgreement' : 'true'
				} ];
			} else if (this.webrtcDetectedBrowser === "firefox"
					&& videoAgentType === "chrome") {
				pc_constrains['optional'] = [ {
					'DtlsSrtpKeyAgreement' : 'true'
				} ];
				pc_constrains['mandatory'] = new Object();
				pc_constrains['mandatory'] = {
					'MozDontOfferDataChannel' : true
				};
			}

		}

		var pc = new RTCPeerConnection(this.pc_config, pc_constrains);

		// send any ice candidates to the other peer
		var callConnection = new CallConnection(pc, successConn, failConn);

		callConnection.remoteVideoAgentType = videoAgentType;

		pc.onicechange = function(event) {
			console.info("onicechange +" + pc.iceState);
		}

		pc.onicechange = function(event) {
			console.info("onicechange +" + pc.iceState);
		}

		pc.onstatechange = function(event) {
			console.info("onicechange +" + pc.readyState);
		}
		
		
		if (this.webrtcDetectedBrowser=='chrome' && parseInt(navigator.userAgent.match(/Chrom(e|ium)\/([0-9]+)\./)[2]) >= 27) {
			pc.oniceconnectionstatechange = function (event){

				if(this.debug) console.info ("oniceconnectionstatechange " + userid + " state " + pc.iceConnectionState);
				
				if (pc.iceConnectionState == "disconnected"){
					videoCall.setVideoStatus(userid,videoMessages.pc_video_status_waiting_peer, "waiting");
				
					setTimeout(function (){
						if (pc.iceConnectionState == "disconnected"){
							var status = videoCall.getCurrentCallStatus(userid);
							if (status == "ESTABLISHED"){
								webRTCClass.onHangUp(userid);
							}
						}else if (pc.iceConnectionState == "connected"){
							videoCall.setVideoStatus(userid,videoMessages.pc_video_status_connection_established,"video");
						}
					},5000);
				}
			}
		}
		
		var signalService = this.signalService;
		var webRTCClass = this;

		pc.onicecandidate = function(event) {
			if (event.candidate) {
				signalService.send(userid, JSON.stringify({
					type : 'candidate',
					label : event.candidate.sdpMLineIndex,
					id : event.candidate.sdpMid,
					candidate : event.candidate.candidate
				}));
			}
		};

		pc.onaddstream = function(event) {
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
	}

	this.gotDescription = function(uuid, desc) {
		var callConnection = this.currentPeerConnectionsMap[uuid];
		var pc = callConnection.rtcPeerConnection;

		if (callConnection.remoteVideoAgentType == "chrome"
				&& this.webrtcDetectedBrowser == "firefox") {
			desc.sdp = this.getInteropSDP(desc.sdp);
		}

		if (pc != null) {
			pc.setLocalDescription(desc);
			this.signalService.send(uuid, JSON.stringify({
				"sdp" : desc
			}));
		}
	}

	/*
	 * That function is called when the signal service receive a message
	 */
	this.onReceive = function(from, message, videoAgentType) {
		var signal = JSON.parse(message.content);

		if (this.debug)
			console.log(message);

		if (signal.sdp) {

			if (signal.sdp.type == "offer") {
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
			}else{
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
	}

	this.getInteropSDP = function(sdp) {
		var inline = 'a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abc\r\nc=IN';
		sdp = sdp.indexOf('a=crypto') == -1 ? sdp.replace(/c=IN/g, inline)
				: sdp;

		return sdp;
	}
	
	this.enableLocalVideo = function (){
		this.localMediaStream.getVideoTracks()[0].enabled=true;
	
	}
	this.disableLocalVideo = function (){
		this.localMediaStream.getVideoTracks()[0].enabled=false;
	}

	this.muteLocalAudio = function (){
		this.localMediaStream.getAudioTracks()[0].enabled=false;
	
	}
	this.unmuteLocalAudio = function (){
		this.localMediaStream.getAudioTracks()[0].enabled=true;
	}


}

/* Object to handle connection and call success, fail events */

function CallConnection(pc, success, failed) {
	this.rtcPeerConnection = pc;
	this.onsuccessconn = success;
	this.onfailedconn = failed;
	this.isCaller = null;
	this.remoteMediaStream = null;
	this.remoteVideoAgentType = null;
	this.startTime = new Date();
}
