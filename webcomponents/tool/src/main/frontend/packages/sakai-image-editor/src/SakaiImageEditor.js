import { css, html } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import Cropper from "cropperjs";
import { cropperStyles } from "./cropperStyles.js";

export class SakaiImageEditor extends SakaiShadowElement {

  static properties = { imageUrl: { attribute: "image-url", type: String } };

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
    });
  }

  filePicked(e) {

    if (e.target.files[0]) {
      this.cropper.clear();
      this.cropper.replace(URL.createObjectURL(e.target.files[0]));
    }
  }

  done() {

    const croppedCanvas = this.cropper.getCroppedCanvas({ maxWidth: 1920, maxHeight: 1080 });
    croppedCanvas.toBlob(blob => {
      const url = URL.createObjectURL(blob);
      this.dispatchEvent(new CustomEvent("image-edited", { detail: { url, blob }, composed: true, bubbles: true }));
    }, "image/webp", 0.75);
  }

  zoomIn() { this.cropper.zoom(0.1); }

  zoomOut() { this.cropper.zoom(-0.1); }

  up() { this.cropper.move(0, -10); }

  down() { this.cropper.move(0, +10); }

  left() { this.cropper.move(+10, 0); }

  right() { this.cropper.move(-10, 0); }

  rotate() {

    this.cropper.clear();
    this.cropper.rotate(90);
    this.cropper.crop();
  }

  shouldUpdate(changed) {
    return this._i18n && this.imageUrl && super.shouldUpdate(changed);
  }

  render() {

    return html`
      <input type="file" accept="image/*" aria-label="${this._i18n.image_picker_label}" @change=${this.filePicked} />
      <div><img id="image" src="${this.imageUrl}" /></div>
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
      <button type="button" class="btn btn-primary mt-2" @click=${this.done} primary>${this._i18n.done}</button>
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
        max-width: 100%;
      }
    `
  ];
}
