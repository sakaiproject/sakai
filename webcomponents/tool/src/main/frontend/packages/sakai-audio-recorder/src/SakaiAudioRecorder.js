import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";

export class SakaiAudioRecorder extends SakaiElement {

  static properties = {
    currentRecordingUrl: { attribute: "current-recording-url", type: String },

    _recordingInProgress: { state: true },
    _blobUrl: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("audio-recorder");
  }

  disconnectedCallback() {

    super.disconnectedCallback();

    if (this._blobUrl) {
      URL.revokeObjectURL(this._blobUrl);
    }
  }

  shouldUpdate() {
    return this._i18n;
  }

  _captureMicrophone() {

    if (this._microphone) {
      return Promise.resolve(this._microphone);
    }

    return navigator.mediaDevices.getUserMedia({ audio: { echoCancellation: false } });
  }

  async _setupMediaRecorder() {

    this._microphone = await navigator.mediaDevices.getUserMedia({ audio: { echoCancellation: false } });

    const mediaRecorder = new MediaRecorder(this._microphone);

    this._chunks = [];

    mediaRecorder.ondataavailable = e => this._chunks.push(e.data);

    mediaRecorder.onstop = () => {

      this._blob = new Blob(this._chunks, { type: "audio/ogg; codecs=opus" });
      this._chunks = [];
      this._blobUrl = URL.createObjectURL(this._blob);
      this.dispatchEvent(new CustomEvent("recording-complete", { detail: { blobUrl: this._blobUrl } }));
    };

    return mediaRecorder;
  }

  getBase64() {

    if (!this._blob) return Promise.resolve(null);

    return new Promise((resolve, reject) => {

      const reader = new FileReader();
      reader.readAsDataURL(this._blob);
      const preamble = "data:audio/ogg; codecs=opus;base64,";
      reader.onload = () => resolve(reader.result.substring(preamble.length));
      reader.onerror = error => reject(error);
    });
  }

  async _startRecording(e) {

    e.preventDefault();

    this._mediaRecorder = await this._setupMediaRecorder();

    this._mediaRecorder.start();

    this._recordingInProgress = true;
  }

  _stopRecording() {

    this._recordingInProgress = false;

    this._mediaRecorder.stop();

    this._microphone.getTracks().forEach(track => track.stop());
  }

  render() {

    return html`
      ${this._recordingInProgress ? nothing : html`
      <button type="button" id="start-recording-button" class="recordButton" @click=${this._startRecording}>
        <span>${this._i18n.record}</span>
        <span class="recordIcon"></span>
      </button>
      `}

      ${this._recordingInProgress ? html`
      <button type="button" id="stop-recording-button" class="recordButton" @click=${this._stopRecording}>
        ${this._i18n.stop}
      </button>
      ` : nothing}

      ${this._blobUrl ? html`
        <div class="d-flex align-items-center">
          <div class="me-2">Preview</div>
          <div><audio src="${this._blobUrl}" controls playsinline controlsList='nodownload'></audio></div>
        </div>
      ` : nothing}

      ${this.currentRecordingUrl ? html`
        <div class="d-flex align-items-center">
          <div class="me-2">Current</div>
          <div><audio src="${this.currentRecordingUrl}" controls playsinline controlsList='nodownload'></audio></div>
        </div>
      ` : nothing}

      <input type="hidden" id="audioBase64" aria-hidden="true" />
    `;
  }
}
