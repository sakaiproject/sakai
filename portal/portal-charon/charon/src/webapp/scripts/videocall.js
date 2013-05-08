/*
 * Video controller. 
 * It uses webrtc-adapter.js to handle all the communication
 */

function VideoCall() {

	this.webRTC = null; // WebRTC handler
	this.signalService = null;

	/* Define the actions executed in each event */

	
	this.doCall = function(uuid,videoAgentType,onSuccessStartCall,onSuccessConnection,onFailConnection){

		var videoCallObject = this; 
			
		this.webRTC.doCall(uuid,videoAgentType,
					function (userid,localMediaStream){
						videoCallObject.startCall (userid,localMediaStream);  
						onSuccessStartCall (userid);
					},
					function (userid,localMediaStream){
						videoCallObject.successCall (userid,localMediaStream);  
						onSuccessConnection (userid);
					},onFailConnection);
	}

 	this.doAnswer = function(uuid,videoAgentType,onSuccessStartCall,onSuccessConnection,onFailConnection){

		var videoCallObject = this; 

		this.webRTC.answerCall(uuid,videoAgentType,
					function (userid,localMediaStream){
						videoCallObject.startAnswer (userid,localMediaStream);  
						onSuccessStartCall (userid);
					},
					function (userid,localMediaStream){
						videoCallObject.successCall (userid,localMediaStream);  
						onSuccessConnection (userid);
					},onFailConnection);
	
	}
 	
 	this.isVideoEnabled = function (){
 		return this.webRTC.isWebRTCEnabled();
 	}
 	
 	this.getVideoAgent = function (){
 		return this.webRTC.webrtcDetectedBrowser;
 	}
 		
 	this.doClose = function (uuid){
 		this.webRTC.hangUp(uuid);
 	}
	
	this.startCall = function (uuid, localMediaStream) {
		this.webRTC.attachMediaStream(document.getElementById("pc_chat_" + uuid
				+ "_local_video"), localMediaStream);
	}

	this.startAnswer = function (uuid, localMediaStream) {
		this.webRTC.attachMediaStream(document.getElementById("pc_chat_" + uuid
				+ "_local_video"), localMediaStream);
	}

	this.maximizeVideo = function (videoElement){
		if ("chrome" == this.getVideoAgent()){
			videoElement.webkitRequestFullScreen();
		}else if ("firefox" == this.getVideoAgent()){
			videoElement.mozRequestFullScreen();
		}else {
			videoElement.requestFullScreen();
		}
	}
	
	this.successCall = function (uuid, remoteMediaStream) {
		 this.webRTC.attachMediaStream(document.getElementById("pc_chat_" + uuid
				+ "_remote_video"), remoteMediaStream);
	}

	this.failedCall = function (uuid) {
		
	}

	this.receiveMessage = function (uuid, message,videoAgentType) {
		this.signalService.onReceive(uuid, message,videoAgentType);
	}

	this.refuseCall = function (uuid){
		this.signalService.send(uuid, JSON.stringify({
			"bye" : "ignore"
		}));
	}
	
	this.onHangUp = function (uid){ // Just declared		
	
	}

	/* It retrieves the current userid list of active webconnections */

	this.getActiveUserIdVideoCalls = function (){
		var currentUserIdConnections = {};
		if (this.webRTC!=null){
			currentUserIdConnections = Object.keys(this.webRTC.currentPeerConnectionsMap);
		}
		return currentUserIdConnections;	
	}
		
	
	this.init = function (pChat){

		this.webRTC = new WebRTC();
		
		this.signalService = new SignalService(pChat);
		this.webRTC.init(this.signalService);
		this.webRTC.onHangUp = this.onHangUp;
		this.webRTC.onIgnore = function (userid){
			pChat.closeVideoCall (userid);
			pChat.setVideoStatus(userid,"User refused your request", true);
		}
		
		/* This is a way to determine what to do when a webrtc call is received */
	    var videoCallObject = this;
		
	    this.webRTC.onReceiveCall = function(userid) {
			pChat.setVideoStatus(userid,"You have an incomming call...", true);
			pChat.openVideoCall (userid,true);
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


/*
(function() {
    var
        fullScreenApi = {
            supportsFullScreen: false,
            isFullScreen: function() { return false; },
            requestFullScreen: function() {},
            cancelFullScreen: function() {},
            fullScreenEventName: '',
            prefix: ''
        },
        browserPrefixes = 'webkit moz o ms khtml'.split(' ');
 
    // check for native support
    if (typeof document.cancelFullScreen != 'undefined') {
        fullScreenApi.supportsFullScreen = true;
    } else {
        // check for fullscreen support by vendor prefix
        for (var i = 0, il = browserPrefixes.length; i < il; i++ ) {
            fullScreenApi.prefix = browserPrefixes[i];
 
            if (typeof document[fullScreenApi.prefix + 'CancelFullScreen' ] != 'undefined' ) {
                fullScreenApi.supportsFullScreen = true;
 
                break;
            }
        }
    }
 
    // update methods to do something useful
    if (fullScreenApi.supportsFullScreen) {
        fullScreenApi.fullScreenEventName = fullScreenApi.prefix + 'fullscreenchange';
 
        fullScreenApi.isFullScreen = function() {
            switch (this.prefix) {
                case '':
                    return document.fullScreen;
                case 'webkit':
                    return document.webkitIsFullScreen;
                default:
                    return document[this.prefix + 'FullScreen'];
            }
        }
        fullScreenApi.requestFullScreen = function(el) {
            return (this.prefix === '') ? el.requestFullScreen() : el[this.prefix + 'RequestFullScreen']();
        }
        fullScreenApi.cancelFullScreen = function(el) {
            return (this.prefix === '') ? document.cancelFullScreen() : document[this.prefix + 'CancelFullScreen']();
        }
    }
 
    // jQuery plugin
    if (typeof jQuery != 'undefined') {
        jQuery.fn.requestFullScreen = function() {
 
            return this.each(function() {
                if (fullScreenApi.supportsFullScreen) {
                    fullScreenApi.requestFullScreen(this);
                }
            });
        };
    }
 
    // export api
    window.fullScreenApi = fullScreenApi;
})();*/
