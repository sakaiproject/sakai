import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-image-editor/sakai-image-editor.js";

export class CourseCardSettings extends SakaiElement {

  static properties = {
    courseId: { attribute: "course-id", type: String },
    courseTitle: { attribute: "course-title", type: String },
    backgroundColor: { attribute: "background-color", type: String },
    foregroundColor: { attribute: "foreground-color", type: String },
    courseImage: { attribute: "course-image", type: String },
    _colorMode: { state: true },
    _imageMode: { state: true },
    _error: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("coursecardsettings");
  }

  connectedCallback() {

    super.connectedCallback();

    this._colorMode = !!this.backgroundColor;
    this._imageMode = !!this.courseImage;
  }

  _backgroundColorChanged(e) {

    this.backgroundColor = e.target.value;
    this.courseImage = undefined;
    this.dispatchEvent(new CustomEvent("background-color-changed", { detail: { "color": e.target.value } }));
  }

  _foregroundColorChanged(e) {

    this.foregroundColor = e.target.value;
    this.dispatchEvent(new CustomEvent("foreground-color-changed", { detail: { "color": e.target.value } }));
  }

  open() {
    bootstrap.Modal.getOrCreateInstance(this.querySelector(".modal")).show();
  }

  _save() {

    const fd = new FormData();
    fd.append("mode", this._colorMode ? "color" : "image");

    if (this._imageMode) {
      if (this.newImageBlob) {
        fd.append("siteImage", this.newImageBlob);
      }

      if (this.foregroundColor) {
        fd.append("foreground", this.foregroundColor);
      }
    } else if (this.backgroundColor || this.foregroundColor) {
      fd.append("background", this.backgroundColor);
      fd.append("foreground", this.foregroundColor);
    }

    const url = `/api/sites/${this.courseId}/card-config`;
    fetch(url, {
      method: "POST",
      body: fd,
    })
    .then(r => {

      if (r.ok) {
        bootstrap.Modal.getInstance(this.querySelector(".modal")).hide();
      } else {
        console.error(`Network error while posting card-config to ${url}`);
        this._error = this._i18n.save_error;
      }
    });
  }

  _setColorMode() {

    this._colorMode = true;
    this._imageMode = false;
  }

  _setImageMode() {

    this._imageMode = true;
    this._colorMode = false;
  }

  _imageEdited(e) {

    this.courseImage = e.detail.url;
    this.newImageBlob = e.detail.blob;
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`
      <div class="modal fade" tabindex="-1" aria-labelledby="course-card-settings-label-${this.courseId}" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h1 class="modal-title fs-5" id="course-card-settings-label-${this.courseId}">${this._i18n.settings}</h1>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this._i18n.close}"></button>
            </div>
            <div class="modal-body">
              <div class="my-2 fw-bolder">${this._i18n.info.replace("{0}", this.courseTitle)}</div>
              <div>
                <label>
                  <input type="radio" name="settings-${this.courseId}" @click=${this._setColorMode} value="color" .checked=${this._colorMode} />
                  ${this._i18n.colors}
                </label>
              </div>
              <div>
                <label>
                  <input type="radio" name="settings-${this.courseId}" @click=${this._setImageMode} value="image" .checked=${this._imageMode} />
                  ${this._i18n.image}
                </label>
              </div>
              <div style="${this._colorMode ? "display: block;" : "display: none;"}">
                <div class="d-flex align-center mt-2">
                  <div><input type="color" id="background-${this.courseId}" .value=${this.backgroundColor} @input=${this._backgroundColorChanged} /></div>
                  <div><label for="background-${this.courseId}">${this._i18n.background_color}</label></div>
                </div>
                <div class="d-flex align-center mt-1">
                  <div><input type="color" id="foreground-${this.courseId}" .value=${this.foregroundColor} @input=${this._foregroundColorChanged} /></div>
                  <div><label for="foreground-${this.courseId}">${this._i18n.foreground_color}</label></div>
                </div>
              </div>
              <div style="${this._imageMode ? "display: block;" : "display: none;"}">
                <sakai-image-editor image-url="${this.courseImage}" @image-edited=${this._imageEdited}></sakai-image-editor>
                <div class="d-flex align-center mt-1">
                  <div><input type="color" id="image-foreground-${this.courseId}" .value=${this.foregroundColor} @input=${this._foregroundColorChanged} /></div>
                  <div><label for="image-foreground-${this.courseId}">${this._i18n.foreground_color}</label></div>
                </div>
              </div>
              ${this._error ? html`
                <div class="sak-banner-error">${this._error}</div>
              ` : nothing}
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${this._i18n.close}</button>
              <button type="button" class="btn btn-primary" @click=${this._save}>${this._i18n.save_changes}</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }
}
