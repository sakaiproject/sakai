//Capture log
if(typeof console === "undefined") {console = {log: function() { }};}

var animationId = null;
var analyserContext = null;
var canvasWidth, canvasHeight;
var input = null;

  function __log(e, data) {
    log.innerHTML += "\n" + e + " " + (data || '');
  }

  // The "opener" is the window that spawned this popup
  function callOpener(name, arg) {
    
    //Try this window
    if (typeof window[name] === 'function') {
	window[name](arg)
    }

    //Then check the opener
    else if (window.opener != null && typeof window.opener[name] === 'function') {
	window.opener[name](arg)
    }
  }

function audioAnalyzer(time) {
  if (!analyserContext) {
    var canvas = document.getElementById('audio-analyzer');
    $(canvas).show();
    canvasWidth = canvas.width;
    canvasHeight = canvas.height;
    analyserContext = canvas.getContext('2d');
  }

  {
    var barSpacing = 3;
    var barWidth = 2;
    var numBars = Math.round(canvasWidth / barSpacing);
    var freqByteData = new Uint8Array(analyzerNode.frequencyBinCount);
    analyzerNode.getByteFrequencyData(freqByteData); 

    analyserContext.clearRect(0, 0, canvasWidth, canvasHeight);
    var multiplier = analyzerNode.frequencyBinCount / numBars;

    // Draw rectangle for each frequency
    for (var i = 0; i < numBars; ++i) {
      var magnitude = 0;
      var offset = Math.floor( i * multiplier );
      // sum/average the block or miss narrow-bandwidth spikes
      for (var j = 0; j< multiplier; j++) magnitude += freqByteData[offset + j];
      magnitude = magnitude / multiplier;
      var magnitude2 = freqByteData[i * multiplier];
      analyserContext.fillStyle = "hsl( " + Math.round((i*360)/numBars) + ", 70%, 50%)";
      analyserContext.fillRect(i * barSpacing, canvasHeight, barWidth, -magnitude);
    }
  }

  animationId = window.webkitRequestAnimationFrame(audioAnalyzer);
}

  function enableRecording(stream) {
      $('#audio-record').attr('disabled','').fadeTo('slow', 1.0);
      if (stream) {
          //Save the input for later
          input = audio_context.createMediaStreamSource(stream);
          console.log('Media input started.');
      }
  }

  function startUserMedia() {
      if (audio_context && input) {
		  //This command would be useful for preview but causes the audio to echo to no other effect
          //input.connect(audio_context.destination);
          //console.log('Input connected to audio context destination.');

          // this is our audio analysis setup
          analyzerNode = audio_context.createAnalyser();
          analyzerNode.fftSize = 2048;
          input.connect( analyzerNode );

          recorder = new Recorder(input);
          console.log('Recorder initialized.');

          // initialize the audio analysis
          audioAnalyzer();
      }

  }

  function startTimer() {
    timer = setInterval(function() {
      timeRemaining--;
      $('#audio-timer').text( (maxSeconds - timeRemaining) < 10 ? "0" + (maxSeconds - timeRemaining) : (maxSeconds - timeRemaining));
      if (timeRemaining <= 0) {
        clearInterval(timer);
        console.log('MaxSeconds reached');
        stopRecording(this);
      } 
    }, 1000); 
  }

  function startRecording(button) {
	
    //Try to stop/reload previous recording
    if (userMediaSupport) {
      document.getElementById('audio-html5').load();
    }
    else {
        //If this fails it's just because jRecorder hasn't started and they tried to close the window
        try {
            $.jRecorder.stopPreview();
        }
        catch (err) {}
    }

    // disable Save and Submit on parent page
    callOpener ("disableSave", window);
    callOpener ("disableSubmitForGrade", window);

    // Reset time remaining
    timeRemaining = maxSeconds;

    startUserMedia();

    if (userMediaSupport) {
      recorder && recorder.record();
      startTimer();
    }
    else {
      // wait for the callback from the SWF before starting timer
      $.jRecorder.record(maxSeconds);
    }

    // disable the record and play button, enable the stop button
    $('#audio-stop').attr('disabled','').fadeTo('slow', 1.0);
    $('#audio-record').attr('disabled','disabled').fadeTo('slow', 0.5);
    $('#audio-upload').attr('disabled','disabled').fadeTo('slow', 0.5);
    $('#audio-play').attr('disabled', 'disabled').fadeTo('slow', 0.5);
    console.log('Recording...');
  }

  function stopRecording(button) {
    if (userMediaSupport) {
      recorder && recorder.stop();
    }
    else {
		//If this fails it's just because jRecorder hasn't started and they tried to close the window
		try {
			$.jRecorder.stop();
		}
		catch (err) {}
    }

    //Disconnect the stream
    if (input != null) { 
        input.disconnect();
    }

    // enable Save and Submit on parent page
    callOpener ("enableSave", window);
    callOpener ("enableSubmitForGrade", window);

    // reset timers
    clearInterval(timer);

    // disable the stop button, enable the record button
    $('#audio-stop').attr('disabled','disabled').fadeTo('slow', 0.5);
    $('#audio-record').attr('disabled','').fadeTo('slow', 1.0);
    $('#audio-upload').attr('disabled','').fadeTo('slow', 1.0);
    //button.previousElementSibling.disabled = false;
    console.log('Stopped recording.');
    
    // see if a user has attempts remaining
    attemptsRemaining--;
    console.log('Attempts remaining: ' + attemptsRemaining);
    $('#audio-attempts').text(attemptsRemaining);

    //force the user submission!
    if (attemptsRemaining < 1) {
      $('#audio-record').attr('disabled', 'disabled');
      $('#audio-stop').attr('disabled','disabled').fadeTo('slow', 0.5);

	  //This might fail if there's no data
	  try {
		  postDataToServer(button);
	  }
	  catch (err) {}
    }
    else {
        createDownloadLink();
    }
  }

  function postDataToServer (button) {
    console.log('seconds: ' + maxSeconds + ";remain: " + timeRemaining);
    var duration = maxSeconds - timeRemaining;
    var attempts = attemptsAllowed - attemptsRemaining;
    var agentId = navigator.userAgent

    if (postUrl) {
        var url  = postUrl + "&agent=" + agentId + "&lastDuration=" + duration + "&suffix=" + "au" + "&attempts=" + attempts;
    }
    else {
	console.log ("postUrl not set yet");
    }

    // disable all buttons while we upload
    $('#audio-controls button').fadeTo('slow', 0).hide();
    $('#audio-posting').fadeTo('slow', 1.0);

    if (userMediaSupport) {
      recorder && recorder.exportWAV(function(blob) {
        console.log('Blob size: ' + blob.size + ';type=' + blob.type);
        uploadBlob(url,blob);
      });
    }
    else {
      // add params before POSTing audio to server
      $.jRecorder.addParameter('agent', agentId);
      $.jRecorder.addParameter('suffix', 'wav');
      $.jRecorder.addParameter('lastDuration', duration);
      $.jRecorder.addParameter('attempts', attempts);
      $.jRecorder.addParameter('Command','QuickUploadAttachment');

      $.jRecorder.sendData();
    }
  }

  function uploadBlob (url, blob) {
    // plot the upload progress
    $('#audio-levelbar').hide();
    $('#audio-statusbar').css('width', '2px').show();

    var xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.onload = function(e) { /*uploaded*/ };

    // Listen to the upload progress.
    xhr.upload.onprogress = function(e) {
      if (e.lengthComputable) {
        p = (e.loaded / e.total) * 100;
        if (!isNaN(p) && p > 0) {
          $('#audio-statusbar').css('width', ((p/100)*(maxWidth)) + 'px');
        }
        console.log('Progress: ' + p);
      }
    };

    xhr.onreadystatechange = function(e) {
      if ( 4 == this.readyState ) {
        console.log('xhr upload complete'); 
        
        // refresh the parent page
        callOpener ("clickReloadLink", window);

        // close up shop
        finishAndClose(true, this.responseText);
      }
    };

    xhr.send(blob);
  }

  function finishAndClose (success, responseUrl) {

	//Update the Url if it's passed back and this method accepts it
    callOpener ("updateUrl", responseUrl);
    $('#audio-levelbar').hide();
    $('#audio-statusbar').css('width', '2px').show();
    $('#audio-posting').hide();
    $('#audio-finished').fadeTo('slow', 1.0);

    var closeSoon = parseInt(1);
    var closer = setInterval(
      function() {
        $('#audio-statusbar').css('width', ((closeSoon/5)*(maxWidth)) + 'px');
        if (closeSoon > 5) {
            if (CKEDITOR) {
                curdialog = CKEDITOR.dialog.getCurrent()
                //Might have closed the dialog
                if (curdialog) {
					curdialog.hide();
                }
                //Stop timer
                clearInterval(closer);
            }
            else { 
                window.close();
                //Stop timer
                clearInterval(closer);
            }
        }
        closeSoon++;
      }
    , 500);
  }

  function plotLevels (level) {
    if(level == -1) {
      $('#audio-levelbar').css('width',  '2px');
    }
    else {
      //console.log(level);
      $('#audio-levelbar').css("width", (level * (maxWidth/100))+ "px");
    }
  }



  function createDownloadLink() {
    $('#audio-play').attr('disabled', '').fadeTo('slow', 1.0);

    recorder && recorder.exportWAV(function(blob) {
      var url = URL.createObjectURL(blob);
      //var li = document.createElement('li');
      // var au = document.createElement('audio');
      var au = document.getElementById('audio-html5');

      au.src = url;
      // li.appendChild(au);
      //$('#audio-debug-log').append(li);

    });
  }

  function playRecording(button) {
	//Try to stop/reload previous recording
    if (userMediaSupport) {
      document.getElementById('audio-html5').load();
    }
    else {
        //If this fails it's just because jRecorder hasn't started and they tried to close the window
        try {
            $.jRecorder.stopPreview();
        }
        catch (err) {}
    }

    if (userMediaSupport) { 
        document.getElementById('audio-html5').play();
    }
    else {
        try {
        	$.jRecorder.startPreview();
        }
        catch (err) {}
    }
  }

  function drawBuffer(width, height, context, data) {
    var step = Math.ceil( data.length / width );
    var amp = height / 2;
    context.fillStyle = "silver";
    for(var i=0; i < width; i++){
        var min = 1.0;
        var max = -1.0;
        for (j=0; j<step; j++) {
            var datum = data[(i*step)+j]; 
            if (datum < min)
                min = datum;
            if (datum > max)
                max = datum;
        }
        context.fillRect(i,(1+min)*amp,1,Math.max(1,(max-min)*amp));
    }
  }

  
$(document).ready(function() {

    try {
      // webkit shim
      window.AudioContext = window.AudioContext || window.webkitAudioContext;

      //navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;
      //Mozilla and IE don't support everything needed yet, use flash fallback
      navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia;

      window.URL = window.URL || window.webkitURL;
      
      //Experimental Chrome only
      if (AudioContext) {
        audio_context = new AudioContext;
        console.log('Audio context set up.');
      }
      console.log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not present!'));

    } catch (e) {
      console.log('No web audio support in this browser!' + e.message);
      userMediaSupport = false;
    }

    //Maybe no error but still no support 
    if (navigator.getUserMedia === undefined) {
        userMediaSupport = false;
    }

    if (isNaN(attemptsAllowed)) {
      attemptsAllowed = 1;
    }

    if (isNaN(attemptsRemaining)) {
      attemptsRemaining = attemptsAllowed;
      console.log('Settings attempts remaining to ' + attemptsRemaining);
    }

    if (isNaN(maxSeconds)) {
      maxSeconds = 30;
      console.log('maxSeconds was NaN. Setting to ' + maxSeconds);
    }
    
    // Set some initial variables
    timeRemaining = maxSeconds;
    $('#audio-time-allowed').text(timeRemaining);
    $('#audio-max-time').text(maxSeconds);
    $('#audio-attempts').text(attemptsRemaining);

    maxWidth = $('#audio-controls').width();
    if (isNaN(maxWidth) || maxWidth < 100) {
      maxWidth = 100;
    }
    console.log('Width of controls: ' + maxWidth);

    // disable record button until the user grants microphone approval
    $('#audio-record').attr('disabled','disabled').fadeTo('slow', 0.5);

    if (userMediaSupport) {

      navigator.getUserMedia({audio: true}, enableRecording, function(e) {
        console.log('No live audio input: ' + e);
        userMediaSupport = false;

        // Why is the user denying the mic? Reload and try again.
        location.reload();
      });
    
    }
    else {
      
      var flash = document.getElementById('flashrecarea');
      $(flash).show();
      $.jRecorder({
        swf_path : '/library/js/recorder/jRecorder.swf',
        host : postUrl,
        swf_object_path : '/library/js/swfobject',
        //These are in the main body right now because flash couldn't call them
        callback_started_recording: function()      { startTimer(); },
        callback_activityLevel:     function(level) { plotLevels(level); },
        callback_finished_sending:  function(response)  {finishAndClose(true, response) },
        callback_hide_the_flash:    function() {enableRecording() }
      });
    }

});
