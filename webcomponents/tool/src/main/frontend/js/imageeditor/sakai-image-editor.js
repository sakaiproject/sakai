import { css, html } from "../assets/lit-element/lit-element.js";
import { SakaiDialogContent } from "../sakai-dialog-content.js";
import Cropper from "../assets/cropperjs/dist/cropper.esm.js";
import { cropperStyles } from "./cropperStyles.js";

export class SakaiImageEditor extends SakaiDialogContent {

  static get properties() {

    return {
      imageUrl: { attribute: "image-url", type: String },
    };
  }

  constructor() {

    super();
    this.loadProperties("image-editor").then(r => this.i18n = r);
  }

  title() {
    return this.i18n["title"];
  }

  updated() {

    const image = this.shadowRoot.getElementById('image');
    this.cropper = new Cropper(image, {
      //aspectRatio: 1,
      aspectRatio: 509/293,
      checkCrossOrigin: false,
      minContainerWidth: 300,
      minContainerHeight: 300,
      autoCropArea: 1,
      viewMode: 1,
      dragMode: 'move',
    });
  }

  filePicked(e) {

    if (e.target.files[0]) {
      this.cropper.clear();
      this.cropper.replace(URL.createObjectURL(e.target.files[0]));
    }
  }

  done() {

    this.cropper.getCroppedCanvas().toBlob((blob) => {

      const url = URL.createObjectURL(blob);
      this.dispatchEvent(new CustomEvent("image-edited", { detail: { url: url, blob: blob }, bubbles: true }));
    });

    this.close();
  }

  zoomIn() {
    this.cropper.zoom(0.1);
  }

  zoomOut() {
    this.cropper.zoom(-0.1);
  }

  up() {
    this.cropper.move(0, -10);
  }

  down() {
    this.cropper.move(0, +10);
  }

  left() {
    this.cropper.move(+10, 0);
  }

  right() {
    this.cropper.move(-10, 0);
  }

  rotate() {

    this.cropper.clear();
    this.cropper.rotate(90);
    this.cropper.crop();
  }

  content() {
    return html`
      <input type="file" accept="image/*" value="Choose an image" @change=${this.filePicked} />
      <img id="image" src="${this.imageUrl}" width="200" />
      <div id="controls">
        <sakai-button @click=${this.zoomIn} type="small" title="${this.i18n["zoom_in"]}" arial-label="${this.i18n["zoom_in"]}">
          <sakai-icon type="add"></sakai-icon>
        </sakai-button>
        <sakai-button @click=${this.zoomOut} type="small" title="${this.i18n["zoom_out"]}" arial-label="${this.i18n["zoom_out"]}">
          <sakai-icon type="minus"></sakai-icon>
        </sakai-button>
        <sakai-button @click=${this.up} type="small" title="${this.i18n["pan_up"]}" arial-label="${this.i18n["pan_up"]}">
          <sakai-icon type="up"></sakai-icon>
        </sakai-button>
        <sakai-button @click=${this.down} type="small" title="${this.i18n["pan_down"]}" arial-label="${this.i18n["pan_down"]}">
          <sakai-icon type="down"></sakai-icon>
        </sakai-button>
        <sakai-button @click=${this.left} type="small" title="${this.i18n["pan_left"]}" arial-label="${this.i18n["pan_left"]}">
          <sakai-icon type="left"></sakai-icon>
        </sakai-button>
        <sakai-button @click=${this.right} type="small" title="${this.i18n["pan_right"]}" arial-label="${this.i18n["pan_right"]}">
          <sakai-icon type="right"></sakai-icon>
        </sakai-button>
        <sakai-button @click=${this.rotate} type="small" title="${this.i18n["rotate"]}" arial-label="${this.i18n["rotate"]}">
          <sakai-icon type="refresh"></sakai-icon>
        </sakai-button>
      </div>
    `;
  }

  buttons() {

    return html`
      <sakai-button @click=${this.done} primary>Done</sakai-button>
    `;
  }

  shouldUpdate(changed) {
    return this.i18n && this.imageUrl && super.shouldUpdate(changed);
  }

  static get styles() {

    return [super.styles, cropperStyles, css`
      #controls {
        margin-top: 10px;
      }
      #controls sakai-button {
        margin: 0;
      }
    `];
  }
}

if (!customElements.get("sakai-image-editor")) {
  customElements.define("sakai-image-editor", SakaiImageEditor);
}
