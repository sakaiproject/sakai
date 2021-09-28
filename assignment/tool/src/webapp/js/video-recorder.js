const sakaiVideoRecorder = {
  recorder: null,
  // Chrome, Safari and Firefox compatibility is related to the mime type and the codec used.
  // See https://caniuse.com/webm for more information.
  // vp8 codec is not supported by firefox for recording.
  // vp9 codec is supported by firefox for recording and playing.
  mimeType: 'video/webm;codecs=vp9',
  player: document.getElementById('submission-preview-player'),
  recordingHiddenInput: document.getElementById('video-submission'),
  recorderSubmissionMimetype: document.getElementById('video-submission-mimetype'),
  recorderStartButton: document.getElementById('btn-start-recording'),
  recorderStopButton: document.getElementById('btn-stop-recording'),

  getBase64 (file) {
    return new Promise( (resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = (error) => reject(error);
    });
  },

  captureCamera (callback) {
    navigator.mediaDevices.getUserMedia({ audio: true, video: true }).then( function(camera) {
      callback(camera);
    }).catch( function(error) {
      alert('Unable to capture your camera. Please check console logs.');
      console.error(error);
    });
  },

  stopRecordingCallback () {
    sakaiVideoRecorder.player.src = sakaiVideoRecorder.player.srcObject = null;
    sakaiVideoRecorder.player.muted = false;
    sakaiVideoRecorder.player.volume = 1;
    sakaiVideoRecorder.player.src = URL.createObjectURL(sakaiVideoRecorder.recorder.getBlob());

    // TODO: Ideally we want a different mechanism to send the file to the backend as it's sent by a classic form post.
    // Would be good to work on formats and browser support, this could be a serious problem with Safari.
    let fileObject = new File([sakaiVideoRecorder.recorder.getBlob()], "video.webm", {type: sakaiVideoRecorder.mimeType});
    sakaiVideoRecorder.getBase64(fileObject).then(
      (data) => sakaiVideoRecorder.recordingHiddenInput.value = data
    );

    sakaiVideoRecorder.recorder.camera.stop();
    sakaiVideoRecorder.recorder.destroy();
    sakaiVideoRecorder.recorder = null;
  },

  bindStartRecordingButton () {
    sakaiVideoRecorder.recorderStartButton.onclick = function() {
      sakaiVideoRecorder.player.style.display = 'block';
      let submissionPlayers = document.querySelectorAll('#submission-player');
      if (submissionPlayers) {
        submissionPlayers.forEach( (submissionPlayer) => submissionPlayer.style.display = 'none');
      }
      this.disabled = true;
      sakaiVideoRecorder.captureCamera(async function(camera) {
        sakaiVideoRecorder.player.muted = true;
        sakaiVideoRecorder.player.volume = 0;
        sakaiVideoRecorder.player.srcObject = camera;
        sakaiVideoRecorder.recorderSubmissionMimetype.value = sakaiVideoRecorder.mimeType;
        sakaiVideoRecorder.recorder = RecordRTC(camera, {
          type: 'video',
          mimeType: sakaiVideoRecorder.mimeType
        });
        sakaiVideoRecorder.recorder.startRecording();
        // release camera on stopRecording
        sakaiVideoRecorder.recorder.camera = camera;
        sakaiVideoRecorder.recorderStopButton.disabled = false;

        // Scroll the view to the stop recording button.
        setTimeout(() => {
          sakaiVideoRecorder.recorderStopButton.scrollIntoView({block: 'center', behavior: "smooth" })
        }, 100);

        // Important: The default Sakai file size is 20MB so by default is recording around 2:30 of video, increase this value if your instance allows bigger files.
        // TODO: Could be interesting to calculate this depending on the server side property content.upload.max, maybe as future improvement.
        const sleep = (m) => new Promise( (r) => setTimeout(r, m));
        await sleep(150000);

        sakaiVideoRecorder.recorderStopButton.disabled = true;
        sakaiVideoRecorder.recorder.stopRecording(sakaiVideoRecorder.stopRecordingCallback);
        sakaiVideoRecorder.recorderStartButton.disabled = false;
      });
    };
  },

  bindStopRecordingButton () {
    sakaiVideoRecorder.recorderStopButton.onclick = function() {
      this.disabled = true;
      sakaiVideoRecorder.recorder.stopRecording(sakaiVideoRecorder.stopRecordingCallback);
      sakaiVideoRecorder.recorderStartButton.disabled = false;
    };
  }

};

sakaiVideoRecorder.bindStartRecordingButton();
sakaiVideoRecorder.bindStopRecordingButton();
