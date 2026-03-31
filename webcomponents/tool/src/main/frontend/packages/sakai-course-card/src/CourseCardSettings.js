import { css, html, nothing } from "lit";
import { SakaiDialog } from "@sakai-ui/sakai-dialog";
import "@sakai-ui/sakai-image-editor/sakai-image-editor.js";

export class CourseCardSettings extends SakaiDialog {

  static properties = {
    courseId: { attribute: "course-id", type: String },
    courseTitle: { attribute: "course-title", type: String },
    backgroundColor: { attribute: "background-color", type: String },
    foregroundColor: { attribute: "foreground-color", type: String },
    imageUrl: { attribute: "image-url", type: String },
    _colorMode: { state: true },
    _imageMode: { state: true },
    _error: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("coursecard");
  }

  connectedCallback() {

    super.connectedCallback();

    this._colorMode = !!this.backgroundColor;
    this._imageMode = !!this.imageUrl;
  }

  close(e, saved) {

    if (!saved) {
      if (this._imageMode) {
        this.dispatchEvent(new CustomEvent("image-selected", { detail: { url: this.imageUrl } }));
        const imageEditor = this.renderRoot.querySelector("sakai-image-editor");
        imageEditor.imageUrl = this.imageUrl;
        URL.revokeObjectURL(this._previewImageUrl);
        this._previewImageUrl = undefined;
      } else {
        this.dispatchEvent(new CustomEvent("background-color-changed", { detail: { "color": this.backgroundColor } }));
        this.dispatchEvent(new CustomEvent("foreground-color-changed", { detail: { "color": this.foregroundColor } }));

        this.renderRoot.querySelector(`#background-${this.courseId}`).value = this.backgroundColor;
        this.renderRoot.querySelector(`#foreground-${this.courseId}`).value = this.foregroundColor;
      }
    }

    super.close();
  }

  _backgroundColorChanged(e) {

    this.newBackgroundColor = e.target.value;
    this.imageUrl = undefined;
    this.dispatchEvent(new CustomEvent("background-color-changed", { detail: { "color": e.target.value } }));
  }

  _foregroundColorChanged(e) {

    this.newForegroundColor = e.target.value;
    this.dispatchEvent(new CustomEvent("foreground-color-changed", { detail: { "color": e.target.value } }));
  }

  _onImageUpdated() { this._imageUpdated = true; }

  _onImageSelected(e) {

    this._previewImageUrl = e.detail.url;
    this._imageUpdated = true;
  }

  async _save() {

    const fd = new FormData();
    fd.append("mode", this._colorMode ? "color" : "image");

    const imageEditor = this.renderRoot.querySelector("sakai-image-editor");

    if (this._imageMode) {
      if (this._imageUpdated) {
        const blob = await imageEditor.getImageBlob(true);

        if (!blob) {
          console.warn("We're setting an image background for the course card but no image has been selected");
          return;
        }

        fd.append("siteImage", blob);
      }

      if (this.foregroundColor) {
        fd.append("foreground", this.newForegroundColor);
      }
    } else if (this.newBackgroundColor || this.newForegroundColor) {
      fd.append("background", this.newBackgroundColor);
      fd.append("foreground", this.newForegroundColor);
    }

    const url = `/api/sites/${this.courseId}/card-config`;
    fetch(url, {
      method: "POST",
      body: fd,
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while posting card-config to ${url}`);
    })
    .then(config => {

      if (config.imageUrl) {
        this.imageUrl = `${config.imageUrl}?salt=${Math.random()}`;
        this.foregroundColor = config.foreground;
        URL.revokeObjectURL(this._previewImageUrl);
        this.dispatchEvent(new CustomEvent("image-selected", { detail: { url: this.imageUrl } }));
      } else {
        this.imageUrl = "";
        this.foregroundColor = config.foreground;
        this.newForegroundColor = undefined;
        this.backgroundColor = config.background;
        this.newBackgroundColor = undefined;
      }
      this.close(undefined, true);
    })
    .catch(error => {

      console.error(error);
      this._error = this._i18n.save_error;
    });
  }

  _setColorMode() {

    this._colorMode = true;
    this._imageMode = false;

    this.imageUrl = "";
    this.dispatchEvent(new CustomEvent("background-color-changed", { detail: { "color": this.backgroundColor } }));
    this.dispatchEvent(new CustomEvent("foreground-color-changed", { detail: { "color": this.foregroundColor } }));
  }

  _setImageMode() {

    this._imageMode = true;
    this._colorMode = false;

    // Grab the current image from the editor and fire an event to indicate that an image was
    // selected (the current selection, effectively).
    this.updateComplete.then(async () => {

      const imageEditor = this.renderRoot.querySelector("sakai-image-editor");
      const blob = await imageEditor.getImageBlob(true);
      if (blob) {
        if (this._previewUrl) {
          URL.revokeObjectURL(this._previewUrl);
        }

        // Check whether we're still in image mode. User could have switched while the blob was
        // loading. Unlikely, but possible.
        if (this._imageMode) {
          this._previewUrl = URL.createObjectURL(blob);
          this.dispatchEvent(new CustomEvent("image-selected", {
            detail: { url: this._previewUrl },
          }));
        }
      }
    });
  }

  showModal() {

    this._error = undefined;
    this._imageUpdated = false;

    super.updateComplete.then(() => super.showModal());
  }

  shouldUpdate() {
    return super.shouldUpdate() && this._i18n;
  }

  title() {
    return this._i18n.info.replace("{0}", this.courseTitle);
  }

  body() {

    return html`
      <h5>${this._i18n.background}</h5>
      <div id="background-info">${this._i18n.background_info}</div>
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
        <div class="d-flex align-items-center mt-2">
          <div><input type="color" id="background-${this.courseId}" .value=${this.backgroundColor} @input=${this._backgroundColorChanged} /></div>
          <div><label for="background-${this.courseId}">${this._i18n.background_color}</label></div>
        </div>
        <div class="d-flex align-items-center mt-1">
          <div><input type="color" id="foreground-${this.courseId}" .value=${this.foregroundColor} @input=${this._foregroundColorChanged} /></div>
          <div><label for="foreground-${this.courseId}">${this._i18n.foreground_color}</label></div>
        </div>
      </div>
      <div style="${this._imageMode ? "display: block;" : "display: none;"}">
        <sakai-image-editor
            image-url="${this.imageUrl || ""}"
            @image-selected=${this._onImageSelected}
            @image-updated=${this._onImageUpdated}
            hide-done-button>
        </sakai-image-editor>
        <div class="d-flex align-items-center mt-1">
          <div><input type="color" id="image-foreground-${this.courseId}" .value=${this.foregroundColor} @input=${this._foregroundColorChanged} /></div>
          <div><label for="image-foreground-${this.courseId}">${this._i18n.foreground_color}</label></div>
        </div>
      </div>
      ${this._error ? html`
        <div class="sak-banner-error">${this._error}</div>
      ` : nothing}
      <div id="button-block">
      <button type="button" class="btn btn-secondary" @click=${this.close}>${this._i18n.close}</button>
      <button type="button" class="btn btn-primary" @click=${this._save}>${this._i18n.save_changes}</button>
      </div>
    `;
  }

  static styles = [
    SakaiDialog.styles,
    css`
      #background-info {
        margin-bottom: 8px;
        font-style: italic;
      }
      #button-block {
        margin-top: 20px;
      }
    `,
  ];
}
