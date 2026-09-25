import { css, html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import Cropper from "cropperjs";
import { cropperStyles } from "./cropperStyles.js";

export class SakaiImageEditor extends SakaiShadowElement {

  static properties = {
    imageUrl: { attribute: "image-url", type: String },
    hideDoneButton: { attribute: "hide-done-button", type: Boolean },
    _filePicked: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("image-editor");
  }

  firstUpdated() {

    const image = this.shadowRoot.getElementById("image");

    this.cropper = new Cropper(image, {
      aspectRatio: 509 / 293,
      checkCrossOrigin: false,
      minContainerWidth: 300,
      minContainerHeight: 300,
      autoCropArea: 1,
      viewMode: 1,
      dragMode: "move",
      crop: () => this._fireImageUpdatedEvent(),
    });

    if (this.cropper.getImageData().top) {
      this.getImageBlob().then(blob => {
        const url = URL.createObjectURL(blob);
        this.dispatchEvent(new CustomEvent("image-selected", { detail: { url }, composed: true, bubbles: true }));
      });
    }
  }

  set imageUrl(value) {

    this.cropper && this.cropper.replace(value);
    const oldValue = this._imageUrl;
    this._imageUrl = value;
    this.requestUpdate("imageUrl", oldValue);
  }

  get imageUrl() { return this._imageUrl; }

  filePicked(e) {

    if (e.target.files[0]) {
      this.cropper.clear();
      const url = URL.createObjectURL(e.target.files[0]);
      this.cropper.replace(url);
      this.dispatchEvent(new CustomEvent("image-selected", { detail: { url }, composed: true, bubbles: true }));
      this._filePicked = true;
    }
  }

  async getImageBlob() {

    const canvas = this.cropper.getCroppedCanvas({
      maxWidth: 1920,
      maxHeight: 1080,
    });

    if (!canvas) {
      console.info("No canvas. Returning null ...");
      return null;
    }
    return new Promise(resolve => canvas.toBlob(resolve, "image/webp", 0.75));
  }

  _done() {

    this.getImageBlob().then(blob => {
      const url = URL.createObjectURL(blob);
      this.dispatchEvent(new CustomEvent("image-edited", { detail: { url, blob }, composed: true, bubbles: true }));
    });
  }

  zoomIn() {

    this.cropper.zoom(0.1);
    this._fireImageUpdatedEvent();
  }

  zoomOut() {

    this.cropper.zoom(-0.1);
    this._fireImageUpdatedEvent();
  }

  up() {

    this.cropper.move(0, -10);
    this._fireImageUpdatedEvent();
  }

  down() {

    this.cropper.move(0, +10);
    this._fireImageUpdatedEvent();
  }


  left() {

    this.cropper.move(+10, 0);
    this._fireImageUpdatedEvent();
  }

  right() {

    this.cropper.move(-10, 0);
    this._fireImageUpdatedEvent();
  }

  rotate() {

    this.cropper.clear();
    this.cropper.rotate(90);
    this.cropper.crop();
    this._fireImageUpdatedEvent();
  }

  _fireImageUpdatedEvent() {
    this.dispatchEvent(new CustomEvent("image-updated"));
  }

  shouldUpdate(changed) {
    return this._i18n && super.shouldUpdate(changed);
  }

  render() {

    return html`
      ${this.hideDoneButton ? html`
        <div class="sak-banner-info">${this._i18n.info_without_done}</div>
      ` : html`
        <div class="sak-banner-info">${this._i18n.info}</div>
      `}
      <input type="file" accept="image/*" aria-label="${this._i18n.image_picker_label}" @change=${this.filePicked} />
      <div><img id="image" src="${this.imageUrl}" /></div>
      ${this.imageUrl || this._filePicked ? html`
        <div id="controls">
          <sakai-button @click=${this.zoomIn} type="small" title="${this._i18n.zoom_in}" arial-label="${this._i18n.zoom_in}">
            <sakai-icon type="add"></sakai-icon>
          </sakai-button>
          <sakai-button @click=${this.zoomOut} type="small" title="${this._i18n.zoom_out}" arial-label="${this._i18n.zoom_out}">
            <sakai-icon type="minus"></sakai-icon>
          </sakai-button>
          <sakai-button @click=${this.up} type="small" title="${this._i18n.pan_up}" arial-label="${this._i18n.pan_up}">
            <sakai-icon type="up"></sakai-icon>
          </sakai-button>
          <sakai-button @click=${this.down} type="small" title="${this._i18n.pan_down}" arial-label="${this._i18n.pan_down}">
            <sakai-icon type="down"></sakai-icon>
          </sakai-button>
          <sakai-button @click=${this.left} type="small" title="${this._i18n.pan_left}" arial-label="${this._i18n.pan_left}">
            <sakai-icon type="left"></sakai-icon>
          </sakai-button>
          <sakai-button @click=${this.right} type="small" title="${this._i18n.pan_right}" arial-label="${this._i18n.pan_right}">
            <sakai-icon type="right"></sakai-icon>
          </sakai-button>
          <sakai-button @click=${this.rotate} type="small" title="${this._i18n.rotate}" arial-label="${this._i18n.rotate}">
            <sakai-icon type="refresh"></sakai-icon>
          </sakai-button>
        </div>
        ${!this.hideDoneButton ? html`
          <button type="button" class="btn btn-primary mt-2" @click=${this._done} primary>${this._i18n.done}</button>
        ` : nothing}
      ` : nothing}
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    cropperStyles,
    css`
      input[type='file'] {
        margin-bottom: 10px;
      }
      #controls {
        margin-top: 10px;
      }
      #controls sakai-button {
        margin: 0;
      }
      #image {
        max-width: 600px;
      }
    `
  ];
}
