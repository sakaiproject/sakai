/*
 * Mock video controller. 
 * It uses webrtc-adapter.js to handle all the comunication
 */

function VideoCall() {

	this.webRTC = null; // WebRTC handler
	this.mockSignalService = null;

	/* Define the actions executed in each event */

	this.doCall = function(uuid,onSuccessStartCall,onSuccessConnection,onFailConnection){

		var videoCallObject = this; 
			
		this.webRTC.doCall(uuid,
					function (userid,localMediaStream){
						videoCallObject.startCall (userid,localMediaStream);  
						onSuccessStartCall (userid);
					},
					function (userid,localMediaStream){
						videoCallObject.successCall (userid,localMediaStream);  
						onSuccessConnection (userid);
					},onFailConnection);
	
	
	}

 	this.doAnswer = function(uuid,onSuccessStartCall,onSuccessConnection,onFailConnection){

		var videoCallObject = this; 
			
		this.webRTC.answerCall(uuid,
					function (userid,localMediaStream){
						videoCallObject.startAnswer (userid,localMediaStream);  
						onSuccessStartCall (userid);
					},
					function (userid,localMediaStream){
						videoCallObject.successCall (userid,localMediaStream);  
						onSuccessConnection (userid);
					},onFailConnection);
	
	}

	
	
	this.successCall = function (uuid, remoteMediaStream) {
		 this.webRTC.attachMediaStream(document.getElementById("pc_chat_" + uuid
				+ "_remote_video"), remoteMediaStream);
	}

	this.startCall = function (uuid, localMediaStream) {
		this.webRTC.attachMediaStream(document.getElementById("pc_chat_" + uuid
				+ "_local_video"), localMediaStream);
	}

	this.startAnswer = function (uuid, localMediaStream) {
		this.webRTC.attachMediaStream(document.getElementById("pc_chat_" + uuid
				+ "_local_video"), localMediaStream);
	}

	this.failedCall = function (uuid) {
		alert("Call failed");
	}

	this.receiveMessage = function (uid, message) {
		this.mockSignalService.onReceive(uid, message);
	}

	
	
	this.init = function (pChat){

		this.webRTC = new WebRTC();
		
		this.mockSignalService = new SignalService(pChat);
		this.webRTC.init(this.mockSignalService);

		/* This is a way to determine what to do when a webrtc call is received */
	    var videoCallObject = this;
		
	    this.webRTC.onReceiveCall = function(userid) {
			pChat.setVideoStatus(userid,"You have an incomming call, waiting for a response...", true);
			pChat.openVideoCall (userid,true);
		}

		this.webRTC.hangUp = function(userid, success, fail) {
			pChat.closeVideoCall(userid);
		}

		this.webRTC.onHangUp = function(userid) {
		}
		
	}
}
/*
 * Create webRTC object to handle the videocalls
 */

// It must be replaced by a genuine signaling method
function SignalService(pChat) {

	this.send = function(userid, content) {
		pChat.sendVideoMessageToUser(userid, content);
	}

	this.onReceive = function(userid, content) {
		/*That is a empty function, redeclared on initialization methods, only here to make sense when revising code */
	}
}

