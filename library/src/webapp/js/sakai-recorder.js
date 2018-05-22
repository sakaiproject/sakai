(function() {
    this.SakaiRecorder = function($audioRecordingPopup) {
        this.$audioRecordingPopup = $audioRecordingPopup;
    }

    SakaiRecorder.prototype = {
        init: function(extraOptions) {
            var animationId = null;
            var canvasWidth, canvasHeight;

            // These vars are for the canvas audio analysis
            var timeToNextPlot = 0;
            var barToPlot = 1;
            var secondsPerBar = 100;
            var recordingStopped = false;
            var recordingStarted = false;

            var _self = this;

            this.vars = {
                userMediaSupport: true,
                timer: 0,
                timeRemaining: 0,
                maxWidth: 0,
                timerStartPosition: 0,
                audio_context: null,
                recorder: null,
                input: null,
                analyserContext: null,
                analyserNode: null,
                ckEditor: extraOptions.ckEditor,
                localeLanguage: extraOptions.localeLanguage,
                localeCountry: extraOptions.localeCountry,
                unlimitedString: extraOptions.unlimitedString,
                agentId: extraOptions.agentId,
                maxSeconds: extraOptions.maxSeconds,
                attemptsAllowed: extraOptions.attemptsAllowed,
                attemptsRemaining: extraOptions.attemptsRemaining,
                paramSeq: extraOptions.paramSeq,
                questionId: extraOptions.questionId,
                questionNumber: extraOptions.questionNumber,
                questionTotal: extraOptions.questionTotal,
                assessmentGrading: extraOptions.assessmentGrading,
                postUrl: extraOptions.postUrl,
                deliveryProtocol: extraOptions.deliveryProtocol,
                messagesSecs: extraOptions.messagesSecs,
                recordedOn: extraOptions.recordedOn,
                dateFormat: extraOptions.dateFormat
            }

            try {
                // webkit shim
                window.AudioContext = window.AudioContext || window.webkitAudioContext;

                //navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;
                // IE doesn't support everything needed yet, use flash fallback
                navigator.getUserMedia = navigator.mediaDevices.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

                window.URL = window.URL || window.webkitURL;

                if (AudioContext) {
                    this.vars.audio_context = new AudioContext;
                    console.log('Audio context set up.');
                }
            } catch (e) {
                console.log('No web audio support in this browser!' + e.message);
                this.vars.userMediaSupport = false;
            }

            //Maybe no error but still no support
            if (navigator.getUserMedia === undefined || typeof this.vars.audio_context === 'undefined') {
                this.vars.userMediaSupport = false;
                console.log('Setting this.vars.userMediaSupport to false because of bad browser support.');
            } else {
                console.log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not present!'));
            }

            if (isNaN(this.vars.attemptsAllowed)) {
                this.vars.attemptsAllowed = 1;
            }

            if (isNaN(this.vars.attemptsRemaining)) {
                this.vars.attemptsRemaining = this.vars.attemptsAllowed;
                console.log('Settings attempts remaining to ' + this.vars.attemptsRemaining);
            }

            if (isNaN(this.vars.maxSeconds)) {
                this.vars.maxSeconds = 30;
                console.log('maxSeconds was NaN. Setting to ' + this.vars.maxSeconds);
            }

            // Set some initial variables
            this.vars.timeRemaining = this.vars.maxSeconds;
            timeTaken = 0;
            this.vars.timerStartPosition = this.$audioRecordingPopup.find('#audio-timer').position().left;
            this.$audioRecordingPopup.find('#audio-time-allowed').text(this.vars.timeRemaining);
            this.$audioRecordingPopup.find('#audio-max-time').text(this.vars.maxSeconds);

            if (this.vars.attemptsAllowed > 1000 && typeof this.vars.unlimitedString !== 'undefined') {
                this.$audioRecordingPopup.find('#audio-attempts-allowed').text(this.vars.unlimitedString);
            } else {
                this.$audioRecordingPopup.find('#audio-attempts-allowed').text(this.vars.attemptsAllowed);
            }

            this.updateAttemptsText(this.vars.attemptsRemaining);
            this.vars.maxWidth = this.$audioRecordingPopup.find('#audio-controls').width();
            if (isNaN(this.vars.maxWidth) || this.vars.maxWidth < 100) this.vars.maxWidth = 100;
            console.log('Width of controls: ' + this.vars.maxWidth);

            // We need to move the playback scrubber smoothly
            scrubberWidth = this.$audioRecordingPopup.find('#audio-scrubber').width();
            if (isNaN(scrubberWidth) || scrubberWidth < 100) scrubberWidth = 520;
            scrubberMultiple = (scrubberWidth - 10) / this.vars.maxSeconds;

            // disable record button until the user grants microphone approval
            this.$audioRecordingPopup.find('#audio-record').prop('disabled','disabled').fadeTo('slow', 0.5);
            this.$audioRecordingPopup.find('#audio-play').prop('disabled','disabled').fadeTo('slow', 0.5);
            this.$audioRecordingPopup.find('#mic-check').fadeTo('fast', 0.2);
            if (this.vars.userMediaSupport) {
                navigator.mediaDevices.getUserMedia({audio: true}).then(function(stream) {
                    _self.enableRecording(stream);
                }).catch(function(error) {
                    console.log('No live audio input: ' + error);
                    _self.vars.userMediaSupport = false;

                    // Why is the user denying the mic? Reload and try again.
                    _self.$audioRecordingPopup.find('#sakai-recorder-error').show();
                });

            } else {
                var hasFlashSupport = swfobject.hasFlashPlayerVersion("9.0.18");
                if (!hasFlashSupport) {
                    this.$audioRecordingPopup.find('#audio-visual-container').hide();
                    this.$audioRecordingPopup.find('#audio-browser-plea').show(); // Please upgrade your browser!!
                    this.hideMicCheckButton();
                }
                else {
                    var flash = this.$audioRecordingPopup.find('#flashrecarea');
                    this.$audioRecordingPopup.find(flash).show();
                    $.jRecorder({
                        swf_path : '/library/js/recorder/jRecorder.swf',
                        host : _self.vars.postUrl,
                        swf_object_path : '/library/js/swfobject',
                        //These are in the main body right now because flash couldn't call them
                        callback_started_recording: function() {
                            _self.startTimer();
                        },
                        // I wish this callback worked better but in my testing, it returns high levels no matter my volume
                        // callback_activityLevel:     function(level) { plotLevels(level); }, 
                        callback_finished_sending: function(response) {
                            _self.finishAndClose(true, response);
                        },
                        callback_hide_the_flash: function() {
                            _self.hideMicCheckButton();
                            _self.enableRecording();
                        }
                    });
                }
            }
            this.$audioRecordingPopup.find('#audio-popup-question-number').text(this.vars.questionNumber);
            this.$audioRecordingPopup.find('#audio-popup-question-total').text(this.vars.questionTotal);
        },

        __log: function(e, data) {
            log.innerHTML += "\n" + e + " " + (data || '');
        },

        // The "opener" is the window that spawned this popup
        callOpener: function(name, arg) {
            //Try this window
            if (typeof window[name] === 'function') {
                window[name](arg)
            }
            //Then check the opener
            else if (window.opener != null && typeof window.opener[name] === 'function') {
                window.opener[name](arg)
            }
        },

        hideMicCheckButton: function() {
            this.$audioRecordingPopup.find('#audio-mic-check').hide();
        },

        updateAttemptsText: function(attemptsRemaining) {
            if (isNaN (attemptsRemaining)) return;

            console.log('Attempts remaining: ' + attemptsRemaining);

            if (attemptsRemaining > 1000 && typeof this.vars.unlimitedString !== 'undefined') {
                this.$audioRecordingPopup.find('#audio-attempts').text(this.vars.unlimitedString);
            }
            else {
                this.$audioRecordingPopup.find('#audio-attempts').text(attemptsRemaining);
            }

            if (attemptsRemaining == 1) {
                this.$audioRecordingPopup.find('#audio-last-attempt').show();
            }
        },

        microphoneCheck: function(stream) {
            var _self = this;

            if (this.vars.audio_context) {
                this.$audioRecordingPopup.find('#volumemeter').show();
                var h = this.$audioRecordingPopup.find('#volumemeter').height();
                var w = this.$audioRecordingPopup.find('#volumemeter').width();

                // connect to the user input; the lower the smoothing, the more variation in the meter
                this.vars.analyzerNode = this.vars.audio_context.createAnalyser();
                this.vars.analyzerNode.smoothingTimeConstant = 0.2;
                this.vars.analyzerNode.fftSize = 256;
                this.vars.input.connect( this.vars.analyzerNode );

                var volumeCanvas = this.$audioRecordingPopup.find('#volumemeter');
                var vCtx    = volumeCanvas.getContext('2d');
                vCtx.fillStyle = '#333';
                vCtx.fillRect(0,0,w,h);

                var intervalId = setInterval(function() {
                    // Once the user starts recording, hide the mic check and stop this loop
                    if (recordingStarted) {
                        this.hideMicCheckButton();
                        clearInterval(intervalId);
                    }

                    var freqByteData = new Float32Array(_self.vars.analyzerNode.frequencyBinCount);
                    _self.vars.analyzerNode.getFloatFrequencyData(freqByteData);

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
                }, 500 /* rinse and repeat every half second */);
            }
        },

        audioAnalyzer: function(time) {
            // We are resetting the state of the audio visualization if a user is doing another attempt
            if (time == 0) {
                console.log('Resetting analyzer canvas');
                this.vars.analyserContext = false;
                timeToNextPlot = 0;
                barToPlot = 1;
            }

            if (!this.vars.analyserContext) {
                var canvas = this.$audioRecordingPopup.find('#audio-analyzer')[0];
                this.$audioRecordingPopup.find(canvas).show();
                canvasWidth = canvas.width;
                canvasHeight = canvas.height;
                secondsPerBar = Math.floor ( (this.vars.maxSeconds*1000) / canvasWidth );
                this.vars.analyserContext = canvas.getContext('2d');

                // make the background black
                this.vars.analyserContext.fillStyle = "black";
                this.vars.analyserContext.fillRect ( 0, 0, canvasWidth, canvasHeight);

                // draw a horizontal line down the middle
                this.vars.analyserContext.fillStyle = "blue";
                this.vars.analyserContext.fillRect ( 0, Math.floor(canvasHeight/2)-2, canvasWidth, 2)

                // the waveform will be white
                this.vars.analyserContext.fillStyle = "white";
            }

            {
                // Only plot a bar in the waveform when enough time has passed
                if (time > timeToNextPlot) {
                    var freqByteData = new Uint8Array(this.vars.analyzerNode.frequencyBinCount);
                    this.vars.analyzerNode.getByteFrequencyData(freqByteData);

                    // compute an average volume
                    var values = 0;
                    for (var i = 0; i < freqByteData.length; i++) {
                        values += freqByteData[i];
                    }
                    average = values / freqByteData.length;

                    this.vars.analyserContext.fillRect ( barToPlot, canvasHeight/2, 1, Math.floor(average*1) );
                    this.vars.analyserContext.fillRect ( barToPlot, canvasHeight/2, 1, Math.floor(average*-1) );

                    barToPlot = barToPlot + 1;
                    timeToNextPlot = time + secondsPerBar;
                }
            }

            var _self = this;
            if (!recordingStopped) animationId = window.requestAnimationFrame(function(time) {
                _self.audioAnalyzer(time);
            });
        },

        enableRecording: function(stream) {
            var _self = this;

            // enable the mic check and record buttons
            this.$audioRecordingPopup.find('#mic-check').prop('disabled','').fadeTo('slow', 1.0);
            this.$audioRecordingPopup.find('#audio-record').prop('disabled','').fadeTo('slow', 1.0);

            this.$audioRecordingPopup.find('#audio-record').click(function() {
                _self.startRecording(this);
            });
            this.$audioRecordingPopup.find('#audio-stop').click(function() {
                _self.stopRecording(this);
            });
            this.$audioRecordingPopup.find('#audio-play').click(function() {
                _self.playRecording(this);
            });
            this.$audioRecordingPopup.find('#audio-upload').click(function() {
                _self.postDataToServer(this);
            });

            if (stream) {
                    //Save the input for later
                    this.vars.input = this.vars.audio_context.createMediaStreamSource(stream);
                    console.log('Media input started.');
            }
        },

        startUserMedia: function() {
            if (this.vars.audio_context && this.vars.input) {
                //This command would be useful for preview but causes the audio to echo to no other effect
                //input.connect(this.vars.audio_context.destination);
                //console.log('Input connected to audio context destination.');

                // this is our audio analysis setup
                this.vars.analyzerNode = this.vars.audio_context.createAnalyser();
                this.vars.analyzerNode.fftSize = 2048;
                this.vars.input.connect( this.vars.analyzerNode );

                this.vars.recorder = new Recorder(this.vars.input);
                console.log('Recorder initialized.');

                // initialize the audio analysis
                this.audioAnalyzer(0);
            }
        },

        // This is a recording timer
        startTimer: function() {
            var _self = this;
            this.vars.timer = setInterval(function() {
                _self.vars.timeRemaining--;
                timeTaken = (_self.vars.maxSeconds - _self.vars.timeRemaining);
                timeTakenMins = Math.floor(timeTaken / 60);
                timeTakenSecs = timeTaken - (timeTakenMins * 60);

                if (typeof scrubberMultiple === 'undefined') {
                    scrubberMultiple = 0;
                }

                _self.$audioRecordingPopup.find('#audio-timer').text( timeTakenMins + ":" + (timeTakenSecs < 10 ? "0" : "") + timeTakenSecs );
                _self.$audioRecordingPopup.find('#audio-timer').css("left", _self.vars.timerStartPosition + (timeTaken * scrubberMultiple));

                if (_self.vars.timeRemaining <= 0) {
                    clearInterval(_self.vars.timer);
                    console.log('MaxSeconds reached');
                    _self.stopRecording(this);
                }
            }, 1000);
        },

        // This is a playback timer (just simulating)
        playbackTimer: function(secsToCount) {
            var secsTaken = 0;
            var _self = this;

            this.vars.timer = setInterval(function() {
                secsTaken++;
                minsTaken = Math.floor(secsTaken / 60);
                secsToDisplay = secsTaken - (minsTaken * 60);

                if (typeof scrubberMultiple === 'undefined') {
                    scrubberMultiple = 0;
                }

                _self.$audioRecordingPopup.find('#audio-timer').text( minsTaken + ":" + (secsToDisplay < 10 ? "0" : "") + secsToDisplay );
                _self.$audioRecordingPopup.find('#audio-timer').css("left", _self.vars.timerStartPosition + (secsTaken * scrubberMultiple));

                if (secsTaken >= secsToCount) {
                    clearInterval(_self.vars.timer);
                    console.log('MaxSeconds of playback reached');
                }
            }, 1000);
        },

        startRecording: function(button) {
            recordingStarted = true;
            recordingStopped = false;
            this.hideMicCheckButton();

            //Try to stop/reload previous recording
            if (this.vars.userMediaSupport) {
                this.$audioRecordingPopup.find('#audio-html5')[0].load();
            } else {
                //If this fails it's just because jRecorder hasn't started and they tried to close the window
                try {
                    $.jRecorder.stopPreview();
                }
                catch (err) {}
            }

            // disable Save and Submit on parent page
            this.callOpener ("disableSave", window);
            this.callOpener ("disableSubmitForGrade", window);

            // Reset time remaining
            this.vars.timeRemaining = this.vars.maxSeconds;

            this.startUserMedia();

            if (this.vars.userMediaSupport) {
                this.vars.recorder && this.vars.recorder.record();
                this.startTimer();
            } else {
                // wait for the callback from the SWF before starting timer
                $.jRecorder.record(this.vars.maxSeconds);
            }

            // disable the record and play button, enable the stop button
            this.$audioRecordingPopup.find('#audio-stop').prop('disabled','').fadeTo('slow', 1.0);
            this.$audioRecordingPopup.find('#audio-record').prop('disabled','disabled').fadeTo('slow', 0.5);
            this.$audioRecordingPopup.find('#audio-upload').prop('disabled','disabled').fadeTo('slow', 0.5);
            this.$audioRecordingPopup.find('#audio-play').prop('disabled', 'disabled').fadeTo('slow', 0.5);
            console.log('Recording...');
        },

        stopRecording: function(button) {
            recordingStopped = true;

            if (this.vars.userMediaSupport) {
                this.vars.recorder && this.vars.recorder.stop();
            } else {
                //If this fails it's just because jRecorder hasn't started and they tried to close the window
                try {
                    $.jRecorder.stop();
                } catch (err) {}
            }

            //Disconnect the stream
            if (this.vars.input != null) {
                this.vars.input.disconnect();
            }

            // enable Save and Submit on parent page
            this.callOpener ("enableSave", window);
            this.callOpener ("enableSubmitForGrade", window);

            // reset timers
            clearInterval(this.vars.timer);

            // disable the stop button, enable the record button
            this.$audioRecordingPopup.find('#audio-stop').prop('disabled','disabled').fadeTo('slow', 0.5);
            this.$audioRecordingPopup.find('#audio-record').prop('disabled','').fadeTo('slow', 1.0);
            this.$audioRecordingPopup.find('#audio-upload').prop('disabled','').fadeTo('slow', 1.0);
            //button.previousElementSibling.disabled = false;
            console.log('Stopped recording.');

            // see if a user has attempts remaining
            this.vars.attemptsRemaining--;
            this.updateAttemptsText(this.vars.attemptsRemaining);

            //force the user submission!
            if (this.vars.attemptsRemaining < 1) {
                this.$audioRecordingPopup.find('#audio-record').prop('disabled', 'disabled');
                this.$audioRecordingPopup.find('#audio-stop').prop('disabled','disabled').fadeTo('slow', 0.5);

                //This might fail if there's no data
                try {
                    this.postDataToServer(button);
                }
                catch (err) {}
            }
            else {
                this.createDownloadLink();
            }
        },

        postDataToServer: function(button) {
            console.log('seconds: ' + this.vars.maxSeconds + ";remain: " + this.vars.timeRemaining);
            var duration = this.vars.maxSeconds - this.vars.timeRemaining;
            var _self = this;
            // attempts is what Samigo expects to be attemptsRemaining
            // agentId is the Samigo agentId and is not the browser user agent
            if (typeof(this.vars.agentId) === 'undefined') this.vars.agentId = 'ckeditorAgent';

            if (this.vars.postUrl) {
                var url    = this.vars.postUrl + "&agent=" + this.vars.agentId + "&lastDuration=" + duration + "&suffix=" + "au" + "&attempts=" + this.vars.attemptsRemaining;
            } else {
                console.log ("postUrl not set yet");
            }

            // disable all buttons while we upload
            this.$audioRecordingPopup.find('#audio-record, #audio-stop, #audio-play, #audio-upload').attr("disabled", true).fadeTo('slow', 0.5);
            this.$audioRecordingPopup.find('#audio-posting').fadeTo('slow', 1.0);

            if (this.vars.userMediaSupport) {
                this.vars.recorder && this.vars.recorder.exportWAV(function(blob) {
                    console.log('Blob size: ' + blob.size + ';type=' + blob.type);
                    _self.uploadBlob(url,blob);
                });
            } else {
                // add params before POSTing audio to server
                $.jRecorder.addParameter('agent', this.vars.agentId);
                $.jRecorder.addParameter('suffix', 'wav');
                $.jRecorder.addParameter('lastDuration', duration);
                $.jRecorder.addParameter('attempts', this.vars.attemptsRemaining);
                $.jRecorder.addParameter('Command','QuickUploadAttachment');
                $.jRecorder.sendData();
            }
        },

        uploadBlob: function(url, blob) {
            var _self = this;

            var xhr = new XMLHttpRequest();
            xhr.open('POST', url, true);
            xhr.onload = function(e) { /*uploaded*/ };

            // Listen to the upload progress.
            xhr.upload.onprogress = function(e) {
                if (e.lengthComputable) {
                    p = (e.loaded / e.total) * 100;
                    if (!isNaN(p) && p > 0) {
                        _self.$audioRecordingPopup.find('#audio-statusbar').css('width', ((p/100)*(_self.vars.maxWidth)) + 'px');
                    }
                    console.log('Progress: ' + p);
                }
            };

            xhr.onreadystatechange = function(e) {
                if ( 4 == this.readyState ) {
                    console.log('xhr upload complete');

                    // close up shop
                    _self.finishAndClose(true, this.responseText);
                }
            };

            xhr.send(blob);
        },

        finishAndClose: function(success, responseUrl) {
            var _self = this;

            if (!this.vars.ckEditor) {
                var json = JSON.parse(responseUrl);

                this.$audioRecordingPopup.find('#audio-record').prop('disabled','').fadeTo('slow', 1.0);

                // refresh the parent page
                //this.callOpener("clickReloadLink", window);

                if (json != null) {
                    //Update the Url if it's passed back and this method accepts it
                    this.callOpener("updateUrl", json.mediaId);

                    if (!audio.canPlayType("audio/wav")) {
                        var $audioEmbed = $("#audioEmbed" + this.vars.questionId);
                        var $parent = $audioEmbed.parent();
                        $audioEmbed.clone().attr("src", this.vars.deliveryProtocol + "/samigo-app/servlet/ShowMedia?mediaId=" + json.mediaId).appendTo($parent);
                        $audioEmbed.remove();
                    } else {
                        var audioSrc = $(".audioSrc" + this.vars.questionId);
                        audioSrc.attr("src", this.vars.deliveryProtocol + "/samigo-app/servlet/ShowMedia?mediaId=" + json.mediaId);
                        audioSrc.parent()[0].load();
                    }
                    $("#question" + this.vars.questionId).show();
                    $(".can_you_hear_" + this.vars.questionId + " a").attr("href", this.vars.deliveryProtocol + "/samigo-app/servlet/ShowMedia?mediaId=" + json.mediaId + "&setMimeType=false")
                    var $detailsElem = $("#details" + this.vars.questionId);
                    $detailsElem.text("");
                    var span = $("<span />").attr("className", "recordedOn" + this.vars.questionId).text(json.duration + " " + this.vars.messagesSecs + ", " + this.vars.recordedOn + " " + json.createdDate);
                    $detailsElem.append(span);
                }

                this.startUserMedia();
                _self.$audioRecordingPopup.find('#audio-timer').text("0:00");
                _self.$audioRecordingPopup.find('#audio-timer').removeAttr("style");

                $(".featherlight-close").click();

            } else {

                // refresh the parent page
                this.callOpener ("clickReloadLink", window);

                //Update the Url if it's passed back and this method accepts it
                this.callOpener ("updateUrl", responseUrl);
                this.$audioRecordingPopup.find('#audio-visual-container').hide();
                this.$audioRecordingPopup.find('#audio-statusbar').show().css('width', '2px').show();
                this.$audioRecordingPopup.find('#audio-posting').hide();
                this.$audioRecordingPopup.find('#audio-finished').fadeTo('slow', 1.0);

                var closeSoon = parseInt(1);
                var closer = setInterval(
                  function() {
                    _self.$audioRecordingPopup.find('#audio-statusbar').css('width', ((closeSoon/5)*(_self.vars.maxWidth)) + 'px');
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

        },

        createDownloadLink: function() {
            var _self = this;
            this.$audioRecordingPopup.find('#audio-play').prop('disabled', '').fadeTo('slow', 1.0);

            this.vars.recorder && this.vars.recorder.exportWAV(function(blob) {
                var url = URL.createObjectURL(blob);
                //var li = document.createElement('li');
                // var au = document.createElement('audio');
                var au = _self.$audioRecordingPopup.find('#audio-html5')[0];

                au.src = url;
                // li.appendChild(au);
                //this.$audioRecordingPopup.find('#audio-debug-log').append(li);
            });
        },

        playRecording: function(button) {
            //Try to stop/reload previous recording
            if (this.vars.userMediaSupport) {
                this.$audioRecordingPopup.find('#audio-html5')[0].load();
            } else {
                //If this fails it's just because jRecorder hasn't started and they tried to close the window
                try {
                    $.jRecorder.stopPreview();
                } catch (err) {}
            }

            if (this.vars.userMediaSupport) {
                    this.$audioRecordingPopup.find('#audio-html5')[0].play();
            } else {
                try {
                    $.jRecorder.startPreview();
                } catch (err) {}
            }

            // Start the playback timer
            this.playbackTimer(timeTaken);
        },

        drawBuffer: function(width, height, context, data) {
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
    }
})();
