var RTCPeerConnection = null;

function WebRTC() {

	this.webrtcDetectedBrowser = null;
	this.currentPeerConnectionsMap = {};
	this.signalService = null;
	this.localMediaStream = null;

	this.pc_config = {
		"iceServers" : [ {
			"url" : "stun:stun.l.google.com:19302"
		} ]
	};// provisional, can we get a stun server?

	this.init = function(signalService) {
		// First of all we try to detect which navigator is trying to use the
		// videoconference from getUserMedia diferences
		if (navigator.mozGetUserMedia) {
			this.webrtcDetectedBrowser = "firefox";
			navigator.getUserMedia = navigator.mozGetUserMedia;
			RTCPeerConnection = mozRTCPeerConnection;
			RTCSessionDescription = mozRTCSessionDescription;
			RTCIceCandidate = mozRTCIceCandidate;
		} else if (navigator.webkitGetUserMedia) {
			this.webrtcDetectedBrowser = "chrome";
			navigator.getUserMedia = navigator.webkitGetUserMedia;
			RTCPeerConnection = webkitRTCPeerConnection;
		} else if (navigator.getUserMedia) {
			// this.webrtcDetectedBrowser = "webrtcenabled";
		} else {
			this.webrtcDetectedBrowser = "nonwebrtc";
		}

		this.signalService = signalService; // Use the signal service provided
		var webRTCClass = this;

		this.signalService.onReceive = function(userid, message) {
			webRTCClass.onReceive(userid, message); // Called custom method when
		}
	}

	/* Call this process to start a video call */
	this.doCall = function(userid, started, success, fail) {
		var callConnection = this.currentPeerConnectionsMap[userid];

		if (callConnection == null) {
			callConnection = this.setupPeerConnection(userid, success, fail);
			callConnection.isCaller = true;
		}

		var webRTCClass = this;

		if (this.localMediaStream != null) {
			started(userid, webRTCClass.localMediaStream);
			callConnection.localMediaStream = webRTCClass.localMediaStream;
			webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.
		} else {

			navigator.getUserMedia({
				audio : true,
				video : true
			}, function(localMediaStream) {
				/* Call started function to fire rendering effects on the screen */
				started(userid, localMediaStream);
				webRTCClass.localMediaStream = localMediaStream;
				callConnection.localMediaStream = localMediaStream;
				webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.
				// ?
			}, fail);
		}
	}

	/*
	 * Provide this function. It will be called when receiving a incoming call
	 */
	this.onReceiveCall = function(userid) {

	}

	/* Call this function to start the answer process to a previous call */

	this.answerCall = function(userid, startAnswer, success, fail) {
		var callConnection = this.currentPeerConnectionsMap[userid];

		// Set up the triggered functions
		callConnection.onsuccessconn = success;
		callConnection.onfail = fail;
		callConnection.isCaller = false;

		var webRTCClass = this;

		if (webRTCClass.localMediaStream != null) {
			// offerStream(pc, to, this.localMediaStream, false);
			startAnswer(userid, webRTCClass.localMediaStream);
			callConnection.localMediaStream = webRTCClass.localMediaStream;
			webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.

		} else {
			navigator.getUserMedia({
				audio : true,
				video : true
			}, function(localMediaStream) {
				/* Call started function to fire rendering effects on the screen */
				startAnswer(userid, localMediaStream);
				webRTCClass.localMediaStream = localMediaStream;
				callConnection.localMediaStream = localMediaStream;
				webRTCClass.offerStream(callConnection, userid, success, fail); // WebRTC.
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
	this.hangUp = function(userid) {
		var callConnection = this.currentPeerConnectionsMap[userid];
		var pc = callConnection.rtcPeerConnection;
		
		if (callConnection.localMediaStream != null){
			pc.removeStream(callConnection.localMediaStream);
		}
	
		if (callConnection.remoteMediaStream != null){
			pc.removeStream(callConnection.remoteMediaStream);
		}
	
		pc.close();
		
		//If it was the last connection we stop the webcam
		delete this.currentPeerConnectionsMap[userid];
		var keys = Object.keys(this.currentPeerConnectionsMap);
		
		if (keys.length < 1 && this.localMediaStream != null){ 
			this.localMediaStream.stop();
			this.localMediaStream = null;
		}
		
		
		this.signalService.send(userid, JSON.stringify({
			"bye" : "bye"
		}));
		
	}	

	/*
	 * Provide this function. It will be called when hangup request is received,
	 * or connection is lost
	 */
	this.onHangUp = function(userid) {

	}
	
	/*
	 * Provide this function. It will be called when a ignore response is received,
	 */
	
	this.onIgnore = function (userid){
		
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

		pc.addStream(callConnection.localMediaStream);

		var webRTCClass = this;

		/*
		 * Declare the success function to be launched when remote stream is
		 * added
		 */

		if (callConnection.isCaller) {
			pc.createOffer(function(desc) {

				// we won't call success, we will wait until peer offers the
				// stream.
				webRTCClass.gotDescription(to, desc);
			});
		} else {
			pc.createAnswer(function(desc) {
				webRTCClass.gotDescription(to, desc);

				// In this case we have to declare the success, instead
				// on addStream
				successCall(to, callConnection.remoteMediaStream); // In this
			});
		}

	}

	/* Suport functions */

	this.isWebRTCEnabled = function() {
		return this.webrtcDetectedBrowser != "nonwebrtc";
	}

	this.setupPeerConnection = function(userid, successConn, failConn) {

		var pc = new RTCPeerConnection(this.pc_config);

		// send any ice candidates to the other peer
		var callConnection = new CallConnection(pc, successConn, failConn);

		pc.onicechange = function(event) {
			console.info("onicechange +" + pc.iceState);
		}

		pc.onicechange = function(event) {
			console.info("onicechange +" + pc.iceState);
		}

		pc.onstatechange = function(event) {
			console.info("onicechange +" + pc.readyState);
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
				 * In this case we have declared what to do
				 * in case of success connection (Offer)
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
	this.onReceive = function(from, message) {
		var signal = JSON.parse(message.content);

		if (signal.sdp) {

			var callConnection = this.currentPeerConnectionsMap[from];

			if (callConnection == null) {
				callConnection = this.setupPeerConnection(from);
				this.currentPeerConnectionsMap[from] = callConnection;
			}

			var pc = callConnection.rtcPeerConnection;
			pc.setRemoteDescription(new RTCSessionDescription(signal.sdp));

			if (signal.sdp.type == "offer") {
				this.onReceiveCall(from);
			}

		} else if (signal.candidate != null) {
			var callConnection = this.currentPeerConnectionsMap[from];
			if (callConnection != null) {
				var pc = callConnection.rtcPeerConnection;
				pc.addIceCandidate(new RTCIceCandidate({
					sdpMLineIndex : signal.label,
					candidate : signal.candidate

				}));
			}
		} else if (signal.bye != null) {
			var callConnection = this.currentPeerConnectionsMap[from];
			if (callConnection != null ) {
				if (signal.bye === "bye"){
					this.onHangUp(from);
				}else if (signal.bye === "ignore"){
					this.onIgnore(from);
				}
				//In the case of not having a previous connection could be a refuse message
			
				
			}
		}
	}

}

/* Object to handle connection and call success, fail events */

function CallConnection(pc, success, failed) {
	this.rtcPeerConnection = pc;
	this.onsuccessconn = success;
	this.onfailedconn = failed;
	this.isCaller = null;
	this.localMediaStream = null;
	this.remoteMediaStream = null;
}
