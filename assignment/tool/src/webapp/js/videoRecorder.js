const sakaiVideoRecorder = {
  recorder: null,
  player: document.getElementById('submission-recorder'),

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
    let fileObject = new File([sakaiVideoRecorder.recorder.getBlob()], "video.webm", {type: 'video/webm'});
    sakaiVideoRecorder.getBase64(fileObject).then(
      (data) => document.getElementById('videoResponse').value = data
    );

    sakaiVideoRecorder.recorder.camera.stop();
    sakaiVideoRecorder.recorder.destroy();
    sakaiVideoRecorder.recorder = null;
  },

  bindStartRecordingButton () {
    document.getElementById('btn-start-recording').onclick = function() {
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
        sakaiVideoRecorder.recorder = RecordRTC(camera, {
          type: 'video'
        });
        sakaiVideoRecorder.recorder.startRecording();
        // release camera on stopRecording
        sakaiVideoRecorder.recorder.camera = camera;
        document.getElementById('btn-stop-recording').disabled = false;

        // Important: The default Sakai file size is 20MB so by default is recording around 2:30 of video, increase this value if your instance allows bigger files.
        // TODO: Could be interesting to calculate this depending on the server side property content.upload.max, maybe as future improvement.
        const sleep = (m) => new Promise( (r) => setTimeout(r, m));
        await sleep(150000);

        document.getElementById('btn-stop-recording').disabled = true;
        sakaiVideoRecorder.recorder.stopRecording(sakaiVideoRecorder.stopRecordingCallback);
        document.getElementById('btn-start-recording').disabled = false;
      });
    };
  },

  bindStopRecordingButton () {
    document.getElementById('btn-stop-recording').onclick = function() {
      this.disabled = true;
      sakaiVideoRecorder.recorder.stopRecording(sakaiVideoRecorder.stopRecordingCallback);
      document.getElementById('btn-start-recording').disabled = false;
    };
  }

};

sakaiVideoRecorder.bindStartRecordingButton();
sakaiVideoRecorder.bindStopRecordingButton();
