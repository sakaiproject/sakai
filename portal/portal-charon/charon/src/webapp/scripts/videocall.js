/*
 * Mock video controller. 
 * It uses webrtc-adapter.js to handle all the comunication
*/

var webRTC = null; // WebRTC handler
var mockSignalService = null;

/* Define the actions executed in each event */

function successCall(uuid, remoteMediaStream) {
	webRTC.attachMediaStream(document.getElementById("pc_chat_"+uuid+"_remote_video"),remoteMediaStream);

}

function startCall(uuid, localMediaStream) {
	webRTC.attachMediaStream(document.getElementById("pc_chat_"+uuid+"_local_video"),localMediaStream);
}

function startAnswer (uuid,localMediaStream){
	webRTC.attachMediaStream(document.getElementById("pc_chat_"+uuid+"_local_video"),localMediaStream);
}

function failedCall(uuid) {
	alert("Call failed");
}

function receiveMessage(uid,message) {
	mockSignalService.onReceive(uid,message);
} 

/*
 * Create webRTC object to handle the videocalls
 */

// It must be replaced by a genuine signaling method
function SignalService() {

	this.send = function(userid, content) {
		portalChat.sendVideoMessageToUser(userid, content);
	}

	this.onReceive = function(userid, content) {
		portalChat.openVideoCall(userid,true);
	}
}

$("document").ready(function() {
	webRTC = new WebRTC();
	mockSignalService = new SignalService ();
	webRTC.init(mockSignalService);
	
	/* This is a way to determine what to do when a webrtc call is received*/
	webRTC.onReceiveCall = function (userid){
		portalChat.openVideoCall(userid,true);
		//Just send an automatic answer response when alert is confirmed
		webRTC.answerCall(userid,startAnswer,successCall, failedCall)
	}
	
	webRTC.hangUp = function(userid, success, fail) {
		portalChat.closeVideoCall(userid);
	}

	webRTC.onHangUp = function(userid) {
		portalChat.closeVideoCall(userid);
	}
	
	
	
});
