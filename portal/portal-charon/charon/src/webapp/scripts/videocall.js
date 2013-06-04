 /*
  *  Video controller. 
  * It uses webrtc-adapter.js to handle all the communication
  */

function VideoCall() {

	this.webRTC = null; // WebRTC handler
	this.signalService = null;
	this.currentCalls = new Array();
	this.callTimeout = 0; // Timeout in msecs
	this.debug = true;
	/* Define the actions executed in each event */

	// Initialize timeout
	this.getCallTimeout = function() {
		if (this.callTimeout==0) {
			this.callTimeout = portalVideoChatTimeout*1000 + portalChatPollInterval;
		}
		return this.callTimeout;
	}

	this.getCurrentCall = function(uuid) {
		return this.currentCalls[uuid];
	} 
	
	this.createNewCall = function(uuid,obj) {
		if (this.debug) console.log("Creo call for: "+uuid);
		return this.currentCalls[uuid] = obj;
	}

	this.removeCurrentCall = function(uuid) {
		if (this.debug) console.log("Remove call for: "+uuid);
		delete this.currentCalls[uuid];
	}

	this.changeCallStatus = function(uuid,value) {
		if (this.currentCalls[uuid]) {
			if (this.debug) console.log("Cambio status: "+uuid+":"+value);
			this.currentCalls[uuid].status = value;
		}
	}
	
	this.getCurrentCallStatus = function(uuid) {
		var val = this.currentCalls[uuid]?this.currentCalls[uuid].status:null;
		if (this.debug) console.log("Current status: "+uuid+":"+val);
		return val;
	}
	
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
						onSuccessConnection (userid);
						videoCallObject.successCall (userid,localMediaStream);
					},onFailConnection);
	
	}
 	
 	this.isVideoEnabled = function (){
 		return this.webRTC.isWebRTCEnabled();
 	}
 	
 	this.getVideoAgent = function (){
 		return this.webRTC.webrtcDetectedBrowser;
 	}
 		
 	this.doClose = function (uuid,skipBye){
 		if (!skipBye) {
 			this.removeCurrentCall(uuid);
 		} else {
 			this.changeCallStatus(uuid,'CANCELLING');
 		}
 		this.webRTC.hangUp(uuid,skipBye);
 		var chatDiv = $("#pc_chat_with_" + uuid);
		chatDiv.removeClass("video_active");
		videoCall.hideMyVideo();
 	}
	
	this.startCall = function (uuid, localMediaStream) {
		this.showMyVideo();
		this.webRTC.attachMediaStream(document.getElementById("pc_chat_local_video"), localMediaStream);
	}

	this.startAnswer = function (uuid, localMediaStream) {
		this.showMyVideo();
		this.webRTC.attachMediaStream(document.getElementById("pc_chat_local_video"), localMediaStream);
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
	
	this.isFullScreenEnabled = function (uuid){
		var fullscreenEnabled = document.fullscreenEnabled || document.mozFullScreenEnabled || document.webkitFullscreenEnabled;
		
		if (fullscreenEnabled){
			var remoteVideo = document.getElementById("pc_chat_" + uuid + "_remote_video");
			var fullscreenElement = document.fullscreenElement || document.mozFullScreenElement || document.webkitFullscreenElement;
		
			if (fullscreenElement == remoteVideo){
				return true
			}
		}
		
		return false;
	}
	
	
	this.minimizeVideo = function (){
		if ("chrome" == this.getVideoAgent()){
			 document.webkitCancelFullScreen();
		}else if ("firefox" == this.getVideoAgent()){
			document.mozCancelFullScreen();
		}else {
			document.cancelFullScreen();
		}
	}
	
	this.successCall = function (uuid, remoteMediaStream) {
	 
		this.webRTC.attachMediaStream(document.getElementById("pc_chat_" + uuid
				+ "_remote_video"), remoteMediaStream);
		videoCall.setVideoStatus (uuid,videoMessages.pc_video_status_connection_established,"video");
			
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
	
	this.onHangUp = function (uuid){ // Just declared
		//check if the connection you want to close is in fullScreen
		
		if (videoCall.isFullScreenEnabled(uuid)){
			videoCall.minimizeVideo();
		}
			
		videoCall.setVideoStatus(uuid,videoMessages.pc_video_status_user_hung,"finished");
		videoCall.doClose(uuid);
		$('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
		$('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
		$('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
	}

	/* It retrieves the current userid list of active webconnections */

	this.getActiveUserIdVideoCalls = function (){
		var currentUserIdConnections = {};
		if (this.webRTC!=null){
			currentUserIdConnections = Object.keys(this.webRTC.currentPeerConnectionsMap);
		}
		return currentUserIdConnections;	
	}
		
	
	this.init = function (){

		this.webRTC = new WebRTC();
		
		this.signalService = new SignalService(portalChat);
		this.webRTC.init(this.signalService);
		this.webRTC.onHangUp = this.onHangUp;
		this.webRTC.onIgnore = function (userid){
			videoCall.closeVideoCall (userid);
			videoCall.setVideoStatus(userid,videoMessages.pc_video_status_user_refused, "failed");
		}
		
		/* This is a way to determine what to do when a webrtc call is received */
	    var videoCallObject = this;
		
	    this.webRTC.onReceiveCall = function(userid) {
			if (videoCall.getCurrentCall(userid)) {
				if (videoCall.getCurrentCallStatus(userid) == "SYNC") {
					// I'm calling to the same user at the same time, discard !!
					videoCall.changeCallStatus(userid, "ANSWERING");
				} else if (videoCall.getCurrentCallStatus(userid) == "ESTABLISHING") {
					// We call each other at the same time
					if (userid<portal.user.id) {
						// I discard the call and go with the answer (do not send bye)
						videoCall.doClose(userid,true);
					} else {
						// I do the call discard the answer
						return;
					}  
				}
			} else {
				videoCall.createNewCall(userid,{'status':'ANSWERING'});
			}
			videoCall.openVideoCall (userid,true);
			videoCall.setVideoStatus(userid,videoMessages.pc_video_status_incomming_call, "waiting");
			setTimeout('videoCall.doAnswerTimeout("'+userid+'")',videoCall.getCallTimeout());
		}
	    
	    
		$('#pc_video_off_checkbox').click(
				function() {
					if ($(this).attr('checked') == 'checked') {
						portalChat.setSetting(
								'videoOff', true,
								true);
						portalChat.videoOff = true;
					} else {
						portalChat.setSetting(
								'videoOff', false);
						portalChat.videoOff = false;
					}
				});

		// Call Time Updater
		setInterval('videoCall.updateVideoTimes();',1000);
		
	}
	
	this.hasVideoChatActive = function (uuid) {
		return this.getCurrentCall(uuid);
	}
	
	this.maximizeVideoCall = function (uuid){
		var remoteVideo = document.getElementById("pc_chat_" + uuid + "_remote_video");
		this.maximizeVideo (remoteVideo);
	}
	
	this.disableVideo = function (){
		this.webRTC.disableLocalVideo();
		$('#enable_local_video').show();
		$('#pc_chat_local_video').hide();
		$('#disable_local_video').hide();
	}
	
	this.enableVideo = function (){
		this.webRTC.enableLocalVideo();
		$('#disable_local_video').show();
		$('#pc_chat_local_video').show();
		$('#enable_local_video').hide();
	}
	
	this.mute = function (){
		this.webRTC.muteLocalAudio();
		$('#unmute_local_audio').show();
		$('#mute_local_audio').hide();
	}
	
	this.unmute = function (){
		this.webRTC.unmuteLocalAudio();
		$('#mute_local_audio').show();
		$('#unmute_local_audio').hide();
	}
	
	this.onvideomessage = function(uuid, message) {
		// message function. It will send to rtc to process it
		this.receiveMessage(uuid, message,this.getRemoteVideoAgent(uuid));
	}
	
	this.currentCallsProceed = function() {
		// Go On with Current Sync Calls
		$.each(Object.keys(this.currentCalls),function(key,value){
			if (videoCall.currentCalls[value].status=="SYNC") {
				videoCall.currentCalls[value].status = "ESTABLISHING";
				videoCall.currentCalls[value].proceed();
			}
		});
	}
	
	this.formatTime = function(time) {
		var hours = time.getHours()-1;
		if (hours<10) hours = '0'+hours;
		if (hours=='00') hours = '';
		else hours = hours+':';
		var minutes = time.getMinutes();
		if (minutes<10) minutes = '0'+minutes;
		minutes = minutes +':';
		var seconds = time.getSeconds();
		if (seconds<10) seconds = '0'+seconds;
		return hours + minutes + seconds;
	}
	
	this.getCallTime = function(uuid) {
		return this.webRTC.currentPeerConnectionsMap[uuid].startTime;
	}
	
	this.updateVideoTimes = function() {
		// Update time for current calls
		$.each(Object.keys(this.webRTC.currentPeerConnectionsMap),function(key,value){
			if (videoCall.getCallTime(value)) {
				var time = new Date(new Date() - videoCall.getCallTime(value));
				$('#pc_connection_'+value+'_time').html(videoCall.formatTime(time));
			}
		});
	}
	
	this.hasVideoAgent = function(uuid) {
		return this.getRemoteVideoAgent(uuid)!='none';
	}

	this.getRemoteVideoAgent = function(uuid) {
		return portalChat.currentConnectionsMap[uuid].video;
	}
	
	this.doTimeout = function(uuid) {
		if (videoCall.getCurrentCallStatus(uuid) == "ESTABLISHING") {
			videoCall.setVideoStatus(uuid,videoMessages.pc_video_status_call_timeout,"failed");
			videoCall.doClose(uuid);
			$('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
			$('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
		}
	}

	this.doAnswerTimeout = function(uuid) {
		if ($('#pc_connection_' + uuid + '_videoin').is(":visible") && (!this.webRTC.currentPeerConnectionsMap[uuid] || this.getCurrentCallStatus(uuid)=="CANCELLING")) {
			this.ignoreVideoCall(uuid);
		}
	}

	this.directVideoCall = function(uuid) {
		portalChat.toggleChat();
		this.openVideoCall(uuid,false);
	}
	
	this.openVideoCall = function(uuid, incomming) {
		if (incomming && this.videoOff)
			return;
		// If a chat window is already open for this sender, show video.
		var messagePanel = $("#pc_chat_with_" + uuid);
		if (!messagePanel.length) {
			// No current chat window for this sender. Create one.
			portalChat.setupChatWindow(uuid, true);
		}

		if (incomming) {
			this.showVideoCall (uuid);
			$('#pc_connection_' + uuid+ '_videochat_bar > .pc_connection_videochat_bar_left ').hide();
			$('#pc_connection_' + uuid + '_videoin').show();
		} else {
			if (!this.getCurrentCall(uuid)) {
			  videoCall.setVideoStatus(uuid,videoMessages.pc_video_status_setup, "waiting");
			  this.showVideoCall (uuid);
			  
			  this.createNewCall(uuid,{ 
					  "status":"SYNC",
					  "proceed":function() {
							videoCall.doCall(
									uuid,
									videoCall.getVideoAgent(uuid),
									function(uuid) {
										videoCall.setVideoStatus(uuid,videoMessages.pc_video_status_waiting_peer, "waiting");
										setTimeout('videoCall.doTimeout("'+uuid+'")',videoCall.getCallTimeout());
									},
									function(uuid) {
										videoCall.changeCallStatus(uuid,"ESTABLISHED");
										videoCall.setVideoStatus(uuid,videoMessages.pc_video_status_connection_established,"video");
									}, 
									function(uuid) {
										videoCall.setVideoStatus(uuid,videoMessages.pc_video_status_call_not_accepted, "failed");
										videoCall.doClose(uuid);
										
									});
					  }
			  });
			  // Test if destination is calling me at the same time
			  // Forced if pollInterval is too large avoid wait more than 7 seconds to call.
			  if (portalChatPollInterval>7000) portalChat.getLatestData();
			} else {
				// You're already calling
				videoCall.setVideoStatus(uuid,videoMessages.pc_video_status_call_in_progress, "waiting");
			}
		}
	}
	
	this.acceptVideoCall = function(uuid) {
		this.changeCallStatus(uuid,"ACCEPTED");
		if (!this.webRTC.currentPeerConnectionsMap[uuid]) {
			$('#pc_connection_' + uuid + '_videoin').hide();
			$('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
			videoCall.setVideoStatus(uuid, videoMessages.pc_video_status_answer_timeout, "failed");
			return;
		}
		videoCall.setVideoStatus(uuid, videoMessages.pc_video_status_setup, "waiting");
		$('#pc_connection_' + uuid + '_videoin').hide();
		
		videoCall.doAnswer(uuid, videoCall.getVideoAgent(uuid), function(
				uuid) {
			$('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
			videoCall.setVideoStatus(uuid, videoMessages.pc_video_status_setup, "waiting");
		}, function(uuid) {
			videoCall.changeCallStatus(uuid,"ESTABLISHED");
			videoCall.setVideoStatus(uuid, videoMessages.pc_video_status_connection_established, "waiting");
		}, function() {
			$('#pc_connection_' + uuid + '_videochat_bar > .pc_connection_videochat_bar_left ').show();
			videoCall.setVideoStatus(uuid, videoMessages.pc_video_status_call_failed, "failed");
			videoCall.closeVideoCall(uuid);
		});
	}

	this.receiveVideoCall = function(uuid) {
		$('#pc_connection_' + uuid + '_videoin').show();
	}

	this.ignoreVideoCall = function(uuid) {
		this.changeCallStatus(uuid,"CANCELLED");
		$('#pc_connection_' + uuid + '_videoin').hide();
		this.setVideoStatus(uuid, videoMessages.pc_video_status_you_ignored, "finished");
		videoCall.refuseCall(uuid);
		videoCall.closeVideoCall(uuid);
		$('#pc_connection_' + uuid+ '_videochat_bar > .pc_connection_videochat_bar_left ').show();
	}

	this.showVideoCall = function(uuid) {
		var chatDiv = $("#pc_chat_with_" + uuid);
		$("#pc_chat_" + uuid + "_video_content").show();
		
		if (!chatDiv.hasClass('pc_minimised')) {
			chatDiv.css('height', '512px');
			chatDiv.css('margin-top', '-212px');
		} else {
			chatDiv.css('margin-top', '49px');
		}
		
		chatDiv.addClass('video_active');
		chatDiv.attr('data-height', '512');
		
		$('#pc_connection_' + uuid + '_videochat_bar').show();
		$('#pc_connection_' + uuid + '_videoin').hide();
		$('#pc_connection_' + uuid + '_videochat_bar .video_off').hide();
		$('#pc_connection_' + uuid + '_videochat_bar .video_on').show();
	}

	this.closeVideoCall = function(uuid,ui) {
		if (ui) videoCall.setVideoStatus(uuid, videoMessages.pc_video_status_hangup, "finished");
		videoCall.doClose(uuid);
		$('#pc_connection_' + uuid + '_videochat_bar .video_off').show();
		$('#pc_connection_' + uuid + '_videochat_bar .video_on').hide();
	}

	this.showMyVideo = function() {
		$('#pc_chat_local_video_content').show();
		if (!portalChat.expanded) {
			$('#pc_content').hide();
			portalChat.toggleChat();
		}
	}

	this.hideMyVideo = function() {
		if ($('.video_active').length < 1) {
			if (portalChat.expanded) {
				$('#pc_content').show();
				portalChat.toggleChat();
			}
		    $('#pc_chat_local_video_content').hide();
		}
	}
	
	this.setVideoStatus = function(uuid, text,visibleElement) {

		if (visibleElement!=null){
			$("#pc_chat_"+ uuid	+ "_video_content > .statusElement").hide();
			
			if (visibleElement==='video'){
				$("#pc_chat_"+ uuid	+ "_video_content > .pc_chat_video_remote").fadeIn();
			}else if (visibleElement==='waiting'){
				$("#pc_chat_"+ uuid	+ "_video_content > .bubblingG").show();
			}else if (visibleElement === 'failed'){
				$("#pc_chat_"+ uuid	+ "_video_content > .pc_chat_video_failed").show();
				setTimeout('portalChat.setupVideoChatBar("'+uuid+'",false,'+$('#pc_chat_with_'+uuid).hasClass('pc_minimised')+');',5000);
			}else if (visibleElement === 'finished'){
				$("#pc_chat_"+ uuid	+ "_video_content > .pc_chat_video_finished").show();
				setTimeout('portalChat.setupVideoChatBar("'+uuid+'",false,'+$('#pc_chat_with_'+uuid).hasClass('pc_minimised')+');',5000);
			}
		}//If any else int visible Element nothing changes
		
		$("#pc_chat_" + uuid	+ "_video_content > .pc_chat_video_statusbar > span").text(text);
		$("#pc_chat_" + uuid + "_video_content > .pc_chat_video_statusbar").show();
		/*	if (!keepDisplayed) {
				$(
					"#pc_chat" + uuid
							+ "_video_content > .pc_chat_video_statusbar")
					.hide(400);
		}*/

	}



	
	
}
/*
 * Create webRTC object to handle the videocalls
 */

// It must be replaced by a genuine signaling method
function SignalService(videoCall) {

	this.send = function(userid, content) {
		portalChat.sendVideoMessageToUser(userid, content);
	}

	this.onReceive = function(userid, content) {
		/*That is a empty function, redeclared on initialization methods, only here to make sense when revising code */
	}
	
}

/*Initialize an object to work with*/

if (typeof VideoCall === 'function') {
	var videoCall = new VideoCall();
	videoCall.init();
}

