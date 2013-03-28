/*
 * Mock video controller. 
 * It uses webrtc-adapter.js to handle all the comunication
*/

var webRTC = null; // WebRTC handler
var mockSignalService = null;

/* Define the actions executed in each event */

function successCall(uuid, remoteMediaStream) {
	alert("call succeded");
	webRTC.attachMediaStream(document.getElementById("remote_video_"+uuid),
			remoteMediaStream);

}

function startCall(uuid, localMediaStream) {

	webRTC.attachMediaStream(document.getElementById("local_video"),
			localMediaStream);

}

function startAnswer (uuid,localMediaStream){
	webRTC.attachMediaStream(document.getElementById("local_video"),
			localMediaStream);
	
}

function failedCall(uuid) {
	alert("call failed");
}


function receiveMessage (uid,message){
	mockSignalService.onReceive (uid,message);
} 

/*
 * Create webRTC object to handle the videocalls
 */

// It must be replaced by a genuine signaling method
function SignalService() {

	this.send = function(userid, content) {
		portalChat.sendVideoMessageToUser(userid, content);
	}

	this.onReceive = null; // To be implemented by consumer  (webRTC) in this case it will consume parameters userid and message
}

$("document").ready(function() {

	webRTC = new WebRTC();
	mockSignalService = new SignalService ();
	webRTC.init(mockSignalService);
	
	/* This is a way to determine what to do when a webrtc call is received*/
	webRTC.onReceiveCall = function (userid){
		alert ("Hello I've received a call from" + userid);
		
		//Just send an automatic answer response when alert is confirmed
		webRTC.answerCall(userid,startAnswer,successCall, failedCall)
		
	}
	
	
	
	
});
