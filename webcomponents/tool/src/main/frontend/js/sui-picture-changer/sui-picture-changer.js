import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import Cropper from "../assets/cropperjs/dist/cropper.esm.js";
import { loadProperties } from "../sakai-i18n.js";
import { getUserId } from "../sakai-portal-utils.js";

export class SuiPictureChanger extends SakaiElement {

  static get properties() {

    return {
      imageUrl: { attribute: false, type: String },
      dialogTitle: { attribute: "dialog-title", type: String },
      uploadError: { attribute: false, type: Boolean },
      removeError: { attribute: false, type: Boolean },
      needsSave: { attribute: false, type: Boolean },
    };
  }

  constructor() {

    super();

    loadProperties("sui-picture-changer").then(r => this.i18n = r);
    this._loadExisting();
  }

  _attachCropper() {

    if (this.cropper) {
      this.cropper.replace(this.imageUrl);
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
        dragMode: 'move',
        cropend: () => this.needsSave = true,
      });
    }
  }

  firstUpdated() {
    this._loadExisting();
  }

  _filePicked(e) {

    if (e.target.files[0]) {
      this.cropper.clear();
      this.cropper.replace(URL.createObjectURL(e.target.files[0]));
      this.needsSave = true;
    }
  }

  _zoomIn() {

    this.cropper.zoom(0.1);
    this.needsSave = true;
  }

  _zoomOut() {

    this.cropper.zoom(-0.1);
    this.needsSave = true;
  }

  _up() {

    this.cropper.move(0, -10);
    this.needsSave = true;
  }

  _down() {

    this.cropper.move(0, +10);
    this.needsSave = true;
  }

  _left() {

    this.cropper.move(+10, 0);
    this.needsSave = true;
  }

  _right() {

    this.cropper.move(-10, 0);
    this.needsSave = true;
  }

  _rotate() {

    this.cropper.clear();
    this.cropper.rotate(90);
    this.cropper.crop();
    this.needsSave = true;
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

    const url = `/direct/profile-image/details?_=${Date.now()}`;
    fetch(url, { credentials: "include" }).then(r => r.json()).then(json => {

      if (json.status == "SUCCESS") {
        if (!json.isDefault) {
          this.imageUrl = `${json.url}?_=${Date.now()}`;
          this.updateComplete.then(() => {
            this._attachCropper();
          });
        }
      }
      this.csrfToken = json.csrf_token;
    });
  }

  _save() {

    const base64 = this.cropper.getCroppedCanvas().toDataURL().replace(/^data:image\/(png|jpg);base64,/, '');
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
        this.uploadError = false;
        this.needsSave = false;
        this._loadExisting();
        this._refreshProfileImageTagsAndHideDialog();
      } else {
        this.uploadError = true;
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
      body: JSON.stringify({ sakai_csrf_token: this.csrfToken }),
    })
    .then(r => {

      if (r.ok) {
        this.removeError = false;
        this.needsSave = false;
        this._loadExisting();
        this._refreshProfileImageTagsAndHideDialog();
      } else {
        this.removeError = true;
        throw new Error(`Network error while removing profile image at ${url}`);
      }
    })
    .catch (error => console.error(error));
  }

  shouldUpdate() {
    return this.i18n && this.imageUrl;
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

            <div id="remove-error" class="sak-banner-error" style="display: ${this.removeError ? "block" : "none"}">${this.i18n.remove_error}</div>
            <div id="upload-error" class="sak-banner-error" style="display: ${this.uploadError ? "block" : "none"}">${this.i18n.upload_error}</div>

            <div id="image-editor-crop-wrapper">
              <div id="cropme">
                <input type="file" accept="image/*" value="Choose an image" @change=${this._filePicked} />
                <img id="image" src="${this.imageUrl}"/>

                <div id="image-editor-controls-wrapper">
                  <div id="controls">
                    <sakai-button @click=${this._zoomIn} type="small" title="${this.i18n.zoom_in}" arial-label="${this.i18n.zoom_in}">
                      <sakai-icon type="add"></sakai-icon>
                    </sakai-button>
                    <sakai-button @click=${this._zoomOut} type="small" title="${this.i18n.zoom_out}" arial-label="${this.i18n.zoom_out}">
                      <sakai-icon type="minus"></sakai-icon>
                    </sakai-button>
                    <sakai-button @click=${this._up} type="small" title="${this.i18n.pan_up}" arial-label="${this.i18n.pan_up}">
                      <sakai-icon type="up"></sakai-icon>
                    </sakai-button>
                    <sakai-button @click=${this._down} type="small" title="${this.i18n.pan_down}" arial-label="${this.i18n.pan_down}">
                      <sakai-icon type="down"></sakai-icon>
                    </sakai-button>
                    <sakai-button @click=${this._left} type="small" title="${this.i18n.pan_left}" arial-label="${this.i18n.pan_left}">
                      <sakai-icon type="left"></sakai-icon>
                    </sakai-button>
                    <sakai-button @click=${this._right} type="small" title="${this.i18n.pan_right}" arial-label="${this.i18n.pan_right}">
                      <sakai-icon type="right"></sakai-icon>
                    </sakai-button>
                    <sakai-button @click=${this._rotate} type="small" title="${this.i18n.rotate}" arial-label="${this.i18n.rotate}">
                      <sakai-icon type="refresh"></sakai-icon>
                    </sakai-button>
                  </div>
                </div>
              </div>
            </div>
        </div>
        <div class="modal-footer">
          <button class="btn pull-left" @click=${this._save} ?disabled=${!this.needsSave}>${this.i18n.save}</button>
          <button class="btn pull-right" @click="${this._remove}">${this.i18n.remove}</button>
          <button class="btn pull-left" data-bs-dismiss="modal">${this.i18n.done}</button>
        </div>
      </div>
    </div>
  </div>
    `;
  }
}

const tagName = "sui-picture-changer";
!customElements.get(tagName) && customElements.define(tagName, SuiPictureChanger);
