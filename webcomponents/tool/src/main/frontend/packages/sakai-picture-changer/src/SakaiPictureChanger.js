import { html } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import Cropper from "cropperjs";
import { getUserId } from "@sakai-ui/sakai-portal-utils";

export class SakaiPictureChanger extends SakaiElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    _imageUrl: { state: true },
    _uploadError: { state: true },
    _removeError: { state: true },
    _needsSave: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("sakai-picture-changer");
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "user-id") {
      console.log(this.userId);
      this._loadExisting();
    }
  }

  _attachCropper() {

    if (!this._imageUrl) return;

    if (this.cropper) {
      this.cropper.replace(this._imageUrl);
    } else {

      const image = this.querySelector("#image");
      this.cropper = new Cropper(image, {
        autoCrop: true,
        aspectRatio: 1 / 1,
        checkCrossOrigin: false,
        guides: true,
        minContainerWidth: 300,
        minContainerHeight: 300,
        autoCropArea: 1,
        viewMode: 1,
        dragMode: "move",
        cropend: () => this._needsSave = true,
      });
    }
  }

  firstUpdated() {
    this._loadExisting();
  }

  _filePicked(e) {

    if (e.target.files[0]) {
      this._imageUrl = URL.createObjectURL(e.target.files[0]);
      this._needsSave = true;
      this.updateComplete.then(() => {
        if (this.cropper) {
          this.cropper.clear();
          this.cropper.replace(this._imageUrl);
        } else {
          this._attachCropper();
        }
      });
    }
  }

  _zoomIn() {

    this.cropper.zoom(0.1);
    this._needsSave = true;
  }

  _zoomOut() {

    this.cropper.zoom(-0.1);
    this._needsSave = true;
  }

  _up() {

    this.cropper.move(0, -10);
    this._needsSave = true;
  }

  _down() {

    this.cropper.move(0, +10);
    this._needsSave = true;
  }

  _left() {

    this.cropper.move(+10, 0);
    this._needsSave = true;
  }

  _right() {

    this.cropper.move(-10, 0);
    this._needsSave = true;
  }

  _rotate() {

    this.cropper.clear();
    this.cropper.rotate(90);
    this.cropper.crop();
    this._needsSave = true;
  }

  _refreshProfileImageTags() {

    const d = new Date();

    const imageUrl = `/api/users/${this.userId}/profile/image?${d.getTime()}`;
    console.log(imageUrl);

    if (this.userId === getUserId()) {
      document.querySelectorAll(".sakai-accountProfileImage")
        .forEach(pic => pic.setAttribute("src", imageUrl));
    }

    document.querySelectorAll(`.sakai-user-photo[user-id='${this.userId}']`).forEach(up => up.refresh());
  }

  _loadExisting() {

    const url = `/api/users/${this.userId}/profile/image/details?_=${Date.now()}`;
    fetch(url).then(r => r.json()).then(json => {

      if (json.status == "SUCCESS") {
        if (!json.isDefault) {
          this._imageUrl = `${json.url}?_=${Date.now()}`;
          this.updateComplete.then(() => this._attachCropper());
        } else {
          this._imageUrl = null;
        }
      }
    });
  }

  _save() {

    const base64 = this.cropper.getCroppedCanvas({ maxWidth: 600, maxHeight: 600 }).toDataURL("image/png").replace(/^data:image\/(png|jpg|webp);base64,/, "");
    const postBody = new URLSearchParams();
    postBody.append("base64", base64);

    const url = `/api/users/${this.userId}/profile/image`;

    fetch(url, {
      headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
      method: "POST",
      body: postBody,
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while uploading image at ${url}`);
    })
    .then(data => {

      if (data.status == "SUCCESS") {
        this._uploadError = false;
        this._needsSave = false;
        this._loadExisting();
        this._refreshProfileImageTags();
        this.dispatchEvent(new CustomEvent("updated"));
      } else {
        this._uploadError = true;
      }
    })
    .catch (error => console.error(error));
  }

  _cancel() {
    this.dispatchEvent(new CustomEvent("cancel"));
  }

  _remove() {

    const url = `/api/users/${this.userId}/profile/image`;
    fetch(url, { method: "DELETE" })
    .then(r => {

      if (r.ok) {
        this._removeError = false;
        this._needsSave = false;
        this._loadExisting();
        this._refreshProfileImageTags();
        this.dispatchEvent(new CustomEvent("updated"));
      } else {
        this._removeError = true;
        throw new Error(`Network error while removing profile image at ${url}`);
      }
    })
    .catch (error => console.error(error));
  }

  _fireDoneEvent() {
    this.dispatchEvent(new CustomEvent("done"));
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`

      <div>
        <div id="remove-error" class="sak-banner-error" style="display: ${this._removeError ? "block" : "none"}">${this._i18n.remove_error}</div>
        <div id="upload-error" class="sak-banner-error" style="display: ${this._uploadError ? "block" : "none"}">${this._i18n.upload_error}</div>

        <div id="image-editor-crop-wrapper">
          <div id="cropme">
            <input type="file" accept="image/*" value="Choose an image" @change=${this._filePicked} />
            ${this._imageUrl ?
              html`<img id="image" class="max-width-100" src="${this._imageUrl}" alt="${this._i18n.profile_image}" />` :
              html`<div class="text-muted text-center p-3">${this._i18n.no_image}</div>`
            }

            <div id="image-editor-controls-wrapper" class="d-${this._imageUrl ? "block" : "none"}">
              <div id="controls">
                <sakai-button @click=${this._zoomIn} type="small" title="${this._i18n.zoom_in}" arial-label="${this._i18n.zoom_in}">
                  <sakai-icon type="add"></sakai-icon>
                </sakai-button>
                <sakai-button @click=${this._zoomOut} type="small" title="${this._i18n.zoom_out}" arial-label="${this._i18n.zoom_out}">
                  <sakai-icon type="minus"></sakai-icon>
                </sakai-button>
                <sakai-button @click=${this._up} type="small" title="${this._i18n.pan_up}" arial-label="${this._i18n.pan_up}">
                  <sakai-icon type="up"></sakai-icon>
                </sakai-button>
                <sakai-button @click=${this._down} type="small" title="${this._i18n.pan_down}" arial-label="${this._i18n.pan_down}">
                  <sakai-icon type="down"></sakai-icon>
                </sakai-button>
                <sakai-button @click=${this._left} type="small" title="${this._i18n.pan_left}" arial-label="${this._i18n.pan_left}">
                  <sakai-icon type="left"></sakai-icon>
                </sakai-button>
                <sakai-button @click=${this._right} type="small" title="${this._i18n.pan_right}" arial-label="${this._i18n.pan_right}">
                  <sakai-icon type="right"></sakai-icon>
                </sakai-button>
                <sakai-button @click=${this._rotate} type="small" title="${this._i18n.rotate}" arial-label="${this._i18n.rotate}">
                  <sakai-icon type="refresh"></sakai-icon>
                </sakai-button>
              </div>
            </div>
            <div class="d-flex mt-3">
              <button class="btn btn-primary me-1" @click=${this._save} ?disabled=${!this._needsSave}>${this._i18n.save}</button>
              <button class="btn btn-secondary" @click=${this._cancel}>${this._i18n.cancel}</button>
              <button class="btn btn-secondary ms-auto" @click=${this._remove}>${this._i18n.remove}</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }
}
