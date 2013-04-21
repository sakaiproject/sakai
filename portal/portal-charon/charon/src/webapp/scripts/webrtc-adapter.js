var RTCPeerConnection = null;

function WebRTC() {

	this.webrtcDetectedBrowser = null;
	this.currentPeerConnectionsMap = {};
	this.signalService = null;
	
	this.pc_config = {
		"iceServers" : [ {
			"url" : "stun:stun.l.google.com:19302"
		} ]
	};// provisional, can we get a stun server?

	this.init = function(signalService) {
		// First of all we try to detect which navigator is trying to use the
		// videoconference from getUserMedia diferences
		if (navigator.mozGetUserMedia){
			this.webrtcDetectedBrowser = "firefox";
			navigator.getUserMedia = navigator.mozGetUserMedia;
			RTCPeerConnection = mozRTCPeerConnection;
			RTCSessionDescription = mozRTCSessionDescription;
			RTCIceCandidate = mozRTCIceCandidate;
		}else if (navigator.webkitGetUserMedia) {
			this.webrtcDetectedBrowser = "chrome";	
			navigator.getUserMedia = navigator.webkitGetUserMedia;
			RTCPeerConnection = webkitRTCPeerConnection;
		} else if (navigator.getUserMedia) {
			// this.webrtcDetectedBrowser = "webrtcenabled";
		} else {
			this.webrtcDetectedBrowser = "nonwebrtc";
		}

		// Setup the generic objects

		// Adapt the getUserMedia with all the prefixs to ensure that any of
		// that will work for you
	/*
	 * navigator.getUserMedia = (navigator.getUserMedia || // Opera
	 * navigator.webkitGetUserMedia || // webkit based browsers like chrome
	 * navigator.mozGetUserMedia // mozilla firefox // navigator.msGetUserMedia
	 * //Microsoft -- uncomment when it really with all browsers );
	 *  // Addapt the window.URL object window.URL = (window.URL ||
	 * window.webkitURL || window.mozURL || window.msURL);
	 *  // Adapt the RTCPeerConnection object
	 * 
	 * RTCPeerConnection = (RTCPeerConnection || webkitRTCPeerConnection ||
	 * mozRTCPeerConnection);
	 */
		/*
		 * RTCSessionDescription = (RTCSessionDescription ||
		 * mozRTCSessionDescription);
		 */

		// Adapt the RTCIceCandidate object
		/* RTCIceCandidate = (mozRTCIceCandidate); */
		this.signalService = signalService; // Use the signal service provided
											// by instantiator
		var webRTCClass = this;
		this.signalService.onReceive  = function (userid,message){
			webRTCClass.onReceive (userid,message); // Called custom method when
													// signalService receives
													// some videomessage
		}
	}

	/* Call this process to start a video call */
	this.doCall = function(userid, started, success, fail) {
		var callConnection = this.currentPeerConnectionsMap[userid];

		if (callConnection == null) {
			callConnection = this.setupPeerConnection(userid,success,fail);
			callConnection.isCaller = true;
		}
		
		var webRTCClass = this;
		navigator.getUserMedia({
			audio : true,
			video : true
		}, function(localMediaStream) {
			/* Call started function to fire rendering effects on the screen */
			started(userid,localMediaStream);
			callConnection.localMediaStream = localMediaStream;
			webRTCClass.offerStream(callConnection, userid,success,fail); // WebRTC. ?
		},fail);
	}

	/*
	 * Provide this function. It will be called when receiving a incoming call
	 */
	this.onReceiveCall = function(userid) {
		
	}

	/* Call this function to start the answer process to a previous call */

	this.answerCall = function(userid,startAnswer, success, fail) {
		var callConnection = this.currentPeerConnectionsMap[userid];
		
		// Set up the triggered functions
		callConnection.onsuccessconn = success;
		callConnection.onfail = fail;
		callConnection.isCaller = false;
		
		
		var webRTCClass = this;
		
		if (this.localMediaStream != null) {
			// offerStream(pc, to, this.localMediaStream, false);
		} else {
			navigator.getUserMedia({
				audio : true,
				video : true
			}, function(localMediaStream) {
				/* Call started function to fire rendering effects on the screen */
				startAnswer(userid,localMediaStream);
				callConnection.localMediaStream = localMediaStream;
				webRTCClass.offerStream(callConnection, userid,success,fail); // WebRTC. ?
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
	this.hangUp = function(userid, success, fail) {

	}

	/*
	 * Provide this function. It will be called when hungup request is received,
	 * or connection is lost
	 */
	this.onHangUp = function(userid) {
			
	}

	/* Use this helper function to hook the media stream to a especified element */
	this.attachMediaStream = function(element, stream) {
			if (this.webrtcDetectedBrowser == "firefox"){
				element.mozSrcObject = stream;
			    element.play();
			}else if (this.webrtcDetectedBrowser = "chrome"){
			    element.src = webkitURL.createObjectURL(stream);
			}
			element.play();
	}

	this.offerStream = function(callConnection, to,successCall,failedCall) {
		var pc = callConnection.rtcPeerConnection;
		
		pc.addStream(callConnection.localMediaStream);

		// this.currentPeerConnectionsMap[to] = pc;

		var webRTCClass = this;

		/*
		 * Declare the success function to be launched when remote stream is
		 * added
		 */
	
		
		if (callConnection.isCaller) {
			pc.createOffer(function(desc) {
				webRTCClass.gotDescription(to, desc); //we won't call success, we will wait until peer offers the stream.
			});
		} else {
			pc.createAnswer(function(desc) {
				webRTCClass.gotDescription(to, desc);
				successCall (to,callConnection.remoteStream); //In this case we have to wait here to declare the success, instead on addStream
			});
		}


	}

	/* Suport functions */

	this.isWebRTCEnabled = function() {
		return webrtcDetecteBrowser != "nonwebrtc";
	}

	this.setupPeerConnection = function(userid, successConn, failConn) {
		
		var pc = new RTCPeerConnection(this.pc_config);
			
		// send any ice candidates to the other peer
		var callConnection = new CallConnection (pc,successConn,failConn);
		
		
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
				callConnection.remoteStream = event.stream
			    
				if (callConnection.onsuccessconn != null) { /* In this case we have declared what to do in case of success connection (Offer)*/
					callConnection.onsuccessconn(userid, event.stream);
				}
				 
		};
		
		this.currentPeerConnectionsMap[userid] = callConnection; 
		
		return callConnection;
	}
	
	this.gotDescription = function (uuid,desc){
		var callConnection = this.currentPeerConnectionsMap[uuid];
		var pc = callConnection.rtcPeerConnection;
		
		
		 if (pc != null){
			 pc.setLocalDescription(desc);
			 this.signalService.send(uuid,JSON.stringify({"sdp":desc }));
		}
	}
	
	/*
	 * That function is called when the signal service receive a message
	 */
	this.onReceive = function (from,message){
		var signal = JSON.parse(message.content);

		if (signal.sdp) {
			
			var callConnection = this.currentPeerConnectionsMap[from];
			
			if (callConnection == null) {
				callConnection = this.setupPeerConnection(from);
				this.currentPeerConnectionsMap[from] = callConnection;
			}
			
			var pc = callConnection.rtcPeerConnection;
			pc.setRemoteDescription(new RTCSessionDescription(signal.sdp));
			
			
			
			
			if (signal.sdp.type=="offer"){
				this.onReceiveCall (from);	
			}else if (signal.sdp.type="response"){
				// this.onReceiveAnswer(userid)
				
			}

		} else if (signal.candidate != null) {
				var callConnection = this.currentPeerConnectionsMap[from];
				var pc = callConnection.rtcPeerConnection;
				pc.addIceCandidate(new RTCIceCandidate({
				sdpMLineIndex : signal.label,
				candidate : signal.candidate
			}));
		}
	}
	
	
}



/* Object to handle connection and call success, fail events */

function CallConnection (pc,success,failed){
	this.rtcPeerConnection =pc;
	this.onsuccessconn = success;
	this.onfailedconn = failed;
	this.isCaller = null;
	this.remoteStream = null;
}

