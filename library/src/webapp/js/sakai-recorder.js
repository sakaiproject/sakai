//Capture log
if(typeof console === "undefined") {console = {log: function() { }};}

var animationId = null;
var analyserContext = null;
var canvasWidth, canvasHeight;
var input = null;

// These vars are for the canvas audio analysis
var timeToNextPlot = 0;
var barToPlot = 1;
var secondsPerBar = 100;
var recordingStopped = false;
var recordingStarted = false;

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

function hideMicCheckButton() {
  $('#audio-mic-check').hide();
}

function updateAttemptsText(attemptsRemaining) {
  if (isNaN (attemptsRemaining)) return;

  console.log('Attempts remaining: ' + attemptsRemaining);

  if (attemptsRemaining > 1000 && typeof unlimitedString !== 'undefined') {
    $('#audio-attempts').text(unlimitedString);
  }
  else {
    $('#audio-attempts').text(attemptsRemaining);
  }

  if (attemptsRemaining == 1) {
    $('#audio-last-attempt').show();
  }
}

function microphoneCheck(stream) {
  if (audio_context) {
    $('#volumemeter').show();
    var h = $('#volumemeter').height();
    var w = $('#volumemeter').width();

    // connect to the user input; the lower the smoothing, the more variation in the meter
    analyzerNode = audio_context.createAnalyser();
    analyzerNode.smoothingTimeConstant = 0.2;
    analyzerNode.fftSize = 256;
    input.connect( analyzerNode );

    var volumeCanvas = document.getElementById('volumemeter');
    var vCtx  = volumeCanvas.getContext('2d');
    vCtx.fillStyle = '#333';
	vCtx.fillRect(0,0,w,h);
          
    var intervalId = setInterval(
      function() {
        // Once the user starts recording, hide the mic check and stop this loop
        if (recordingStarted) {
          hideMicCheckButton();
          clearInterval(intervalId);
        }

        var freqByteData = new Float32Array(analyzerNode.frequencyBinCount);
        analyzerNode.getFloatFrequencyData(freqByteData); 

        // compute an average volume
        var values = 0;
        for (var i = 0; i < freqByteData.length; i++) {
          values += freqByteData[i];
        }
        var average = Math.round(values / freqByteData.length) + 120;
        if (navigator.mozGetUserMedia) average = average + 30;

        var grad = vCtx.createLinearGradient(w/10,h*0.2,w/10,h*0.95);
		grad.addColorStop(0,'red');
		grad.addColorStop(-6/-72,'yellow');
		grad.addColorStop(1,'green');
		// fill the grey background
		vCtx.fillStyle = '#555';
		vCtx.fillRect(0,0,w,h);
		vCtx.fillStyle = grad;
		// draw the current volume
		vCtx.fillRect(0,h,w, average*-1);
      }, 500 // rinse and repeat every half second
    );
  }
}

function audioAnalyzer(time) {
  // We are resetting the state of the audio visualization if a user is doing another attempt
  if (time == 0) {
    console.log('Resetting analyzer canvas');
    analyserContext = false;
    timeToNextPlot = 0;
    barToPlot = 1;
  }

  if (!analyserContext) {
    var canvas = document.getElementById('audio-analyzer');
    $(canvas).show();
    canvasWidth = canvas.width;
    canvasHeight = canvas.height;
	secondsPerBar = Math.floor ( (maxSeconds*1000) / canvasWidth );
    analyserContext = canvas.getContext('2d');

    // make the background black
    analyserContext.fillStyle = "black";
    analyserContext.fillRect ( 0, 0, canvasWidth, canvasHeight);

    // draw a horizontal line down the middle
    analyserContext.fillStyle = "blue";
    analyserContext.fillRect ( 0, Math.floor(canvasHeight/2)-2, canvasWidth, 2)

    // the waveform will be white
    analyserContext.fillStyle = "white";
  }

  {
    // Only plot a bar in the waveform when enough time has passed
    if (time > timeToNextPlot) {
      var freqByteData = new Uint8Array(analyzerNode.frequencyBinCount);
      analyzerNode.getByteFrequencyData(freqByteData); 

      // compute an average volume
      var values = 0;
      for (var i = 0; i < freqByteData.length; i++) {
        values += freqByteData[i];
      }
      average = values / freqByteData.length;

      analyserContext.fillRect ( barToPlot, canvasHeight/2, 1, Math.floor(average*1) );
      analyserContext.fillRect ( barToPlot, canvasHeight/2, 1, Math.floor(average*-1) );

      barToPlot = barToPlot + 1;
      timeToNextPlot = time + secondsPerBar;
    }
  }

  if (!recordingStopped) animationId = window.requestAnimationFrame(audioAnalyzer);
}

  function enableRecording(stream) {
      // enable the mic check and record buttons
      $('#mic-check').prop('disabled','').fadeTo('slow', 1.0);
      $('#audio-record').prop('disabled','').fadeTo('slow', 1.0);

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
          audioAnalyzer(0);
      }

  }

  // This is a recording timer
  function startTimer() {
    timer = setInterval(function() {
      timeRemaining--;
      timeTaken = (maxSeconds - timeRemaining);
      timeTakenMins = Math.floor(timeTaken / 60);
      timeTakenSecs = timeTaken - (timeTakenMins * 60);

	  if (typeof scrubberMultiple === 'undefined') {
		  scrubberMultiple = 0;
	  }

      $('#audio-timer').text( timeTakenMins + ":" + (timeTakenSecs < 10 ? "0" : "") + timeTakenSecs );
      $('#audio-timer').css("left", timerStartPosition + (timeTaken * scrubberMultiple));


      if (timeRemaining <= 0) {
        clearInterval(timer);
        console.log('MaxSeconds reached');
        stopRecording(this);
      } 
    }, 1000); 
  }

  // This is a playback timer (just simulating)
  function playbackTimer(secsToCount) {
    var secsTaken = 0;

    timer = setInterval(function() {
      secsTaken++;
      minsTaken = Math.floor(secsTaken / 60);
      secsToDisplay = secsTaken - (minsTaken * 60);

	  if (typeof scrubberMultiple === 'undefined') {
		  scrubberMultiple = 0;
	  }

      $('#audio-timer').text( minsTaken + ":" + (secsToDisplay < 10 ? "0" : "") + secsToDisplay );
      $('#audio-timer').css("left", timerStartPosition + (secsTaken * scrubberMultiple));

      if (secsTaken >= secsToCount) {
        clearInterval(timer);
        console.log('MaxSeconds of playback reached');
      } 
    }, 1000); 
  }

  function startRecording(button) {
    recordingStarted = true;
    recordingStopped = false;
    hideMicCheckButton();
	
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
    $('#audio-stop').prop('disabled','').fadeTo('slow', 1.0);
    $('#audio-record').prop('disabled','disabled').fadeTo('slow', 0.5);
    $('#audio-upload').prop('disabled','disabled').fadeTo('slow', 0.5);
    $('#audio-play').prop('disabled', 'disabled').fadeTo('slow', 0.5);
    console.log('Recording...');
  }

  function stopRecording(button) {
    recordingStopped = true;

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
    $('#audio-stop').prop('disabled','disabled').fadeTo('slow', 0.5);
    $('#audio-record').prop('disabled','').fadeTo('slow', 1.0);
    $('#audio-upload').prop('disabled','').fadeTo('slow', 1.0);
    //button.previousElementSibling.disabled = false;
    console.log('Stopped recording.');
    
    // see if a user has attempts remaining
    attemptsRemaining--;
    updateAttemptsText(attemptsRemaining);

    //force the user submission!
    if (attemptsRemaining < 1) {
      $('#audio-record').prop('disabled', 'disabled');
      $('#audio-stop').prop('disabled','disabled').fadeTo('slow', 0.5);

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
    // attempts is what Samigo expects to be attemptsRemaining
    // agentId is the Samigo agentId and is not the browser user agent
    if (typeof(agentId) === 'undefined') agentId = 'ckeditorAgent';

    if (postUrl) {
      var url  = postUrl + "&agent=" + agentId + "&lastDuration=" + duration + "&suffix=" + "au" + "&attempts=" + attemptsRemaining;
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
      $.jRecorder.addParameter('attempts', attemptsRemaining);
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
        
        // close up shop
        finishAndClose(true, this.responseText);
      }
    };

    xhr.send(blob);
  }

  function finishAndClose (success, responseUrl) {
    // refresh the parent page
    callOpener ("clickReloadLink", window);

	//Update the Url if it's passed back and this method accepts it
    callOpener ("updateUrl", responseUrl);
    $('#audio-visual-container').hide();
    $('#audio-statusbar').show().css('width', '2px').show();
    $('#audio-posting').hide();
    $('#audio-finished').fadeTo('slow', 1.0);

    var closeSoon = parseInt(1);
    var closer = setInterval(
      function() {
        $('#audio-statusbar').css('width', ((closeSoon/5)*(maxWidth)) + 'px');
        if (closeSoon > 5) {
            var ckeditor_loaded = false;
            if (typeof CKEDITOR !== 'undefined') {
                for (var instances in CKEDITOR.instances) {
                  ckeditor_loaded = true;
                }
            }

            if (ckeditor_loaded) {
                curdialog = CKEDITOR.dialog.getCurrent()
                //Might have closed the dialog
                if (curdialog) {
                  curdialog.hide();
                }
            }
            else { 
                window.close();
            }

            //Stop timer
            clearInterval(closer);
        }
        closeSoon++;
      }
    , 500);
  }

  function createDownloadLink() {
    $('#audio-play').prop('disabled', '').fadeTo('slow', 1.0);

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

    // Start the playback timer
    playbackTimer(timeTaken);
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
      // IE doesn't support everything needed yet, use flash fallback
      navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

      window.URL = window.URL || window.webkitURL;
      
      if (AudioContext) {
        audio_context = new AudioContext;
        console.log('Audio context set up.');
      }
    } catch (e) {
      console.log('No web audio support in this browser!' + e.message);
      userMediaSupport = false;
    }

    //Maybe no error but still no support 
    if (navigator.getUserMedia === undefined || typeof audio_context === 'undefined') {
        userMediaSupport = false;
        console.log('Setting userMediaSupport to false because of bad browser support.');
    }
    else {
      console.log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not present!'));
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
    timeTaken = 0;
    timerStartPosition = $('#audio-timer').position().left;
    $('#audio-time-allowed').text(timeRemaining);
    $('#audio-max-time').text(maxSeconds);
    
    if (attemptsAllowed > 1000 && typeof unlimitedString !== 'undefined') {
      $('#audio-attempts-allowed').text(unlimitedString);
    }
    else {
      $('#audio-attempts-allowed').text(attemptsAllowed);
    }

    updateAttemptsText(attemptsRemaining);

    maxWidth = $('#audio-controls').width();
    if (isNaN(maxWidth) || maxWidth < 100) maxWidth = 100;
    console.log('Width of controls: ' + maxWidth);

    // We need to move the playback scrubber smoothly
    scrubberWidth = $('#audio-scrubber').width();
    if (isNaN(scrubberWidth) || scrubberWidth < 100) scrubberWidth = 520;
    scrubberMultiple = (scrubberWidth - 10) / maxSeconds;

    // disable record button until the user grants microphone approval
    $('#audio-record').prop('disabled','disabled').fadeTo('slow', 0.5);
    $('#audio-play').prop('disabled','disabled').fadeTo('slow', 0.5);
    $('#mic-check').fadeTo('fast', 0.2);

    if (userMediaSupport) {

      navigator.getUserMedia({audio: true}, enableRecording, function(e) {
        console.log('No live audio input: ' + e);
        userMediaSupport = false;

        // Why is the user denying the mic? Reload and try again.
        $('#sakai-recorder-error').show();
      });
    
    }
    else {
      var hasFlashSupport = swfobject.hasFlashPlayerVersion("9.0.18");
      if (!hasFlashSupport) {
        $('#audio-visual-container').hide();
        $('#audio-browser-plea').show(); // Please upgrade your browser!!
        hideMicCheckButton();
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
          // I wish this callback worked better but in my testing, it returns high levels no matter my volume
          // callback_activityLevel:     function(level) { plotLevels(level); }, 
          callback_finished_sending:  function(response)  {finishAndClose(true, response) },
          callback_hide_the_flash:    function() { hideMicCheckButton(); enableRecording(); }
        });
      }
    }

});
