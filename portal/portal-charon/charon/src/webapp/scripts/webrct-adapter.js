var RTCPeerConnection = null;
var getUserMedia = null;
var RTCSessionDescription = null;



function webRTC (){


	var attachMediaStream = null;
	var webrtcDetectedBrowser = null;
	var currentConnectionsMap = {};
	var signalService = null;
	var pc_config = {"iceServers": [{"url": "stun:stun.l.google.com:19302"}]};//provisional, can we get a stun server?

	function init(){
		//First of all we try to detect which navigator is trying to use the videoconference from getUserMedia diferences
		if (navigator.mozGetUserMedia) 
			this.webrtcDetectedBrowser = "firefox";
		else if (navigator.webkitGetUserMedia){
			this.webrtcDetectedBrowser = "chrome";
		}else if (navigator.getUserMedia){
			this.webrtcDetectedBrowser = "webrtcenabled";
		}else{
			this.webrtcDetectedBrowser = "nonwebrtc";
		}

		//Setup the generic objects

		//Adapt the getUserMedia with all the prefixs to ensure that any of that will work for you 
		getUserMedia = (
			navigator.getUserMedia || //Opera
			navigator.webkitGetUserMedia || //webkit based browsers like chrome (safari?)
			navigator.mozGetUserMedia || // mozilla firefox
			//navigator.msGetUserMedia //Microsoft -- uncomment when it really works with all RTP
			);

		this.isWebRTCEnabled = function (){
			return webrtcDetecteBrowser != "nonwebrtc";
		}

		//Addapt the window.URL object
		window.URL = (
			window.URL 		||
			window.webkitURL	||
			window.mozURL 		||
			window.msURL
			);

		//Adapt the RTCPeerConnection object
		RTCPeerConnection = (
			RTCPeerConnection	||
			webkitRTCPeerConnection ||
			mozRTCPeerConnection	||

			);
		//Adapt the RTCSessionDescription object
		RTCSessionDescription = (
				mozRTCSessionDescription
		W);

		//Adapt the RTCIceCandidate object
		RTCIceCandidate = (
			mozRTCIceCandidate
		);
	}

	/*Call this process to start a video call*/
	doCall = doCall (userid,started,success,fail){
		var pc = this.currentPeerConnectionsMap[userid];
	
	        if (pc==null){
	       	      pc = this.setupPeerConnection (userid,fail);
		}
		

		this.getUserMedia({audio: true, video: true},
			function (localMediaStream){
				  /* Call started function to fire rendering effects on the screen */	 
				  started (localMediaStream);
				  offerStream (pc,to,localMediaStream,true,fail);	//WebRTC. ?
			},fail);
		}
	}

	/* Provide this function. It will be called when receiving a incoming 
	 call 
	 */ 
	onReceiveCall = function(userid){

	}
	 
	/* Call this function to start the answer process to a previous call */

	answerCall = function (userid,received,success,fail){
		var pc = this.currentPeerConnectionsMap[to];
					
		
		if (this.localMediaStream != null){
			offerStream (pc,to,this.localMediaStream,false);	
		}else{
		      navigator.getUserMedia({audio: true, video: true},			  
		    	  function (localMediaStream){
					  /* Call started function to fire rendering effects on the screen */	 
					  success (localMediaStream);
					  offerStream (pc,to,localMediaStream,false,fail);	//WebRTC. ?
				},fail);
		   });
		}
	}
	
	/*That method will be called when caller receive answer
	onReceiveAnswer = function (userid){

	}

	/*Call this function to announce you want to hangup, success callback 
	 * is launched when the pair get the request, fail in other case
	 */
	hangUp = function (userid,success,fail){

	}

	/*Provide this function. It will be called when hungup request is 
	 *received, or connection is lost
	 */
	onHangUp = function (userid){

	}

	/*Use this helper function to hook the media stream to a especified element*/
	attachMediaStream = function(element, stream) {

	}

	 this.offerStream = function (pc,to,localMediaStream,isCaller,fail){
		                        	  pc.addStream(localMediaStream);
						  this.currentPeerConnectionsMap[to] = pc; 			  
		
						  var webRTCClass = this;
		
						  if (isCaller){
				                pc.createOffer(function (desc){ 
				                	webRTCClass.gotDescription(to,desc);
							});
						  }else{
							pc.createAnswer(function (desc) {
								webRTCClass.gotDescription(to,desc);
							});
					}
					
		    }	
	

	/* Suport functions*/
	
	setupPeerConnection = function (userid,successConn,failCon){
	     var pc = new RTCPeerConnection(pc_config);


            // send any ice candidates to the other peer
             pc.onicecandidate = function (event){
		 if (event.candidate) {
	           signalService.send(userid,JSON.stringify(
			{  type: 'candidate',
        	           label: event.candidate.sdpMLineIndex,
	                   id: event.candidate.sdpMid,
        	           candidate: event.candidate.candidate}
			));
		    }
	     };

             pc.onaddstream = function (evt){
		     /* if evt success*/
		     successCon(userid,event.stream);
	     };
	    
	     return pc;		
	}
	
	
	

	

}
/*
if (navigator.mozGetUserMedia) {
  console.log("This appears to be Firefox");

  webrtcDetectedBrowser = "firefox";

  // The RTCPeerConnection object.
  RTCPeerConnection = mozRTCPeerConnection;

  // The RTCSessionDescription object.
  RTCSessionDescription = mozRTCSessionDescription;

  // The RTCIceCandidate object.
  RTCIceCandidate = mozRTCIceCandidate;

  // Get UserMedia (only difference is the prefix).
  // Code from Adam Barth.
  getUserMedia = navigator.mozGetUserMedia.bind(navigator);

  // Attach a media stream to an element.
  attachMediaStream = function(element, stream) {
    console.log("Attaching media stream");
    element.mozSrcObject = stream;
    element.play();
  };

  reattachMediaStream = function(to, from) {
    console.log("Reattaching media stream");
    to.mozSrcObject = from.mozSrcObject;
    to.play();
  };

  // Fake get{Video,Audio}Tracks
  MediaStream.prototype.getVideoTracks = function() {
    return [];
  };

  MediaStream.prototype.getAudioTracks = function() {
    return [];
  };
} else if (navigator.webkitGetUserMedia) {
  console.log("This appears to be Chrome");

  webrtcDetectedBrowser = "chrome";

  // The RTCPeerConnection object.
  RTCPeerConnection = webkitRTCPeerConnection;
 
  // Get UserMedia (only difference is the prefix).
  // Code from Adam Barth.
  getUserMedia = navigator.webkitGetUserMedia.bind(navigator);

  // Attach a media stream to an element.
  attachMediaStream = function(element, stream) {
    element.src = webkitURL.createObjectURL(stream);
  };

  reattachMediaStream = function(to, from) {
    to.src = from.src;
  };

  // The representation of tracks in a stream is changed in M26.
  // Unify them for earlier Chrome versions in the coexisting period.
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

} else if (){



}else {
  console.log("Browser does not appear to be WebRTC-capable");
}
*/

