import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import Cropper from "cropperjs";
import { getUserId } from "@sakai-ui/sakai-portal-utils";

export class SakaiPictureChanger extends SakaiElement {

  static properties = {

    dialogTitle: { attribute: "dialog-title", type: String },

    _imageUrl: { state: true },
    _uploadError: { state: true },
    _removeError: { state: true },
    _needsSave: { state: true },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("sakai-picture-changer").then(r => this._i18n = r);
  }

  _attachCropper() {

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
    // Add an event listener for the Bootstrap 'show.bs.modal' event
    const modal = this.renderRoot.querySelector("#profile-image-upload");
    if (modal) {
      modal.addEventListener("show.bs.modal", () => {
        this._loadExisting();
      });
    }
  }

  _filePicked(e) {

    if (e.target.files[0]) {
      this.cropper.clear();
      this.cropper.replace(URL.createObjectURL(e.target.files[0]));
      this._needsSave = true;
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

  _refreshProfileImageTagsAndHideDialog() {

    const d = new Date();

    const imageUrl = `/direct/profile/${getUserId()}/image?${d.getTime()}`;
    document.querySelectorAll(".sakai-accountProfileImage")
      .forEach(pic => pic.setAttribute("src", imageUrl));

    const style = `background-image: url(${imageUrl})`;

    document.querySelectorAll(`.sakai-user-photo[data-user-id='${getUserId()}']`).forEach(up => {
      up.setAttribute("style", style);
    });
    // Update the profile image on the page
    const myPhoto = document.getElementById("myPhoto");
    myPhoto && (myPhoto.src = imageUrl);
  }

  _loadExisting() {
    if (this._imageUrl) return;

    const url = `/direct/profile-image/details?_=${Date.now()}`;
    fetch(url, { credentials: "include" }).then(r => r.json()).then(json => {

      if (json.status == "SUCCESS") {
        if (!json.isDefault) {
          this._imageUrl = `${json.url}?_=${Date.now()}`;
          this.updateComplete.then(() => {
            this._attachCropper();
          });
        }
      }
      this.csrfToken = json.csrf_token;
    });
  }

  _save() {

    const base64 = this.cropper.getCroppedCanvas().toDataURL().replace(/^data:image\/(png|jpg);base64,/, "");
    const postBody = new URLSearchParams();
    postBody.append("base64", base64);

    const url = "/direct/profile-image/upload";

    fetch(url, {
      credentials: "include",
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
        this._refreshProfileImageTagsAndHideDialog();
      } else {
        this._uploadError = true;
      }
    })
    .catch (error => console.error(error));
  }

  _remove() {

    const url = "/direct/profile-image/remove";
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      method: "POST",
      body: JSON.stringify({ sakaiCsrfToken: this.csrfToken }),
    })
    .then(r => {

      if (r.ok) {
        this._removeError = false;
        this._needsSave = false;
        this._loadExisting();
        this._refreshProfileImageTagsAndHideDialog();
      } else {
        this._removeError = true;
        throw new Error(`Network error while removing profile image at ${url}`);
      }
    })
    .catch (error => console.error(error));
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`

    <div class="modal fade" id="profile-image-upload" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5>${this.dialogTitle}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>

        </div>
        <div class="modal-body">

            <div id="remove-error" class="sak-banner-error" style="display: ${this._removeError ? "block" : "none"}">${this._i18n.remove_error}</div>
            <div id="upload-error" class="sak-banner-error" style="display: ${this._uploadError ? "block" : "none"}">${this._i18n.upload_error}</div>

            <div id="image-editor-crop-wrapper">
              <div id="cropme">
                <input type="file" accept="image/*" value="Choose an image" @change=${this._filePicked} />
                <img id="image" src="${ifDefined(this._imageUrl)}"/>

                <div id="image-editor-controls-wrapper">
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
              </div>
            </div>
        </div>
        <div class="modal-footer">
          <button class="btn float-start" data-bs-dismiss="modal" @click=${this._save} ?disabled=${!this._needsSave}>${this._i18n.save}</button>
          <button class="btn float-end" @click="${this._remove}">${this._i18n.remove}</button>
          <button class="btn float-start" data-bs-dismiss="modal">${this._i18n.cancel}</button>
        </div>
      </div>
    </div>
  </div>
    `;
  }
}
