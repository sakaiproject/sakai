import { html, nothing } from "lit";
import { styleMap } from "lit/directives/style-map.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { pushSetupComplete, registerPushCallback } from "@sakai-ui/sakai-push-utils";
import { markNotificationsViewed } from "@sakai-ui/sakai-notifications";
import "@sakai-ui/sakai-course-card/course-card-settings.js";
import "@sakai-ui/sakai-icon";

export class SakaiCourseCard extends SakaiElement {

  static properties = {
    courseData: { type: Object },
    siteId: { attribute: "site-id", type: String },
    _styles: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("coursecard");
  }

  connectedCallback() {

    super.connectedCallback();

    if (!this.courseData) {
      if (!this.siteId) {
        console.error("Either courseData or site-id must be specified");
        return;
      }

      const url = `/api/sites/${this.siteId}`;
      fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Network error while fetching site data from ${url}`);
      })
      .then(courseData => {

        this.courseData = courseData;
        this._init();
      })
      .catch(error => console.error(error));

    } else {
      this._init();
    }
  }

  _init() {

    this._styles = { color: this.courseData.courseCardForegroundColor };
    if (this.courseData.image) {
      this._styles.backgroundImage = `linear-gradient(var(--sakai-course-card-gradient-start), var(--sakai-course-card-gradient-end)), url(${this.courseData.image})`;
    } else {
      this._styles.backgroundColor = this.courseData.courseCardBackgroundColor;
    }

    pushSetupComplete.then(() => {

      if (Notification.permission !== "granted") return;

      registerPushCallback("notifications", message => {

        if (this.courseData.siteId !== message.siteId) return;

        this.courseData.tools.forEach(t => {

          if (t.id === message.tool && !message.viewed) t.hasAlerts = true;
          this.requestUpdate();
        });
      });
    })
    .catch(error => console.error(error));

    document.body.addEventListener("notifications-cleared", () => {

      this.courseData.tools.forEach(t => t.hasAlerts = false);
      this.requestUpdate();
    });
  }

  _toolClicked(e) {
    markNotificationsViewed(this.courseData.siteId, e.currentTarget.dataset.toolId);
  }

  _backgroundColorChanged(e) {

    delete this._styles.backgroundImage;
    this._styles.backgroundColor = e.detail.color;
    this.requestUpdate();
  }

  _foregroundColorChanged(e) {

    this._styles.color = e.detail.color;
    this.requestUpdate();
  }

  _imageEdited(e) {

    this._styles.backgroundImage = `linear-gradient(var(--sakai-course-card-gradient-start), var(--sakai-course-card-gradient-end)), url(${e.detail.url})`;
    this.requestUpdate();
  }

  _openSettings() {
    this.querySelector("course-card-settings").open();
  }

  shouldUpdate() {
    return this._i18n && this.courseData;
  }

  render() {

    return html`
      <course-card-settings course-id="${this.courseData.siteId}"
          course-title="${this.courseData.title}"
          background-color="${this.courseData.courseCardBackgroundColor}"
          foreground-color="${this.courseData.courseCardForegroundColor}"
          course-image="${this.courseData.image}"
          @image-edited=${this._imageEdited}
          @background-color-changed=${this._backgroundColorChanged}
          @foreground-color-changed=${this._foregroundColorChanged}>
      </course-card-settings>
      <div class="info-block" style=${styleMap(this._styles)}>
        <div class="d-flex">
          <div>
            <a class="${!this.courseData.image ? "no-background" : ""}"
                href="${this.courseData.url}"
                title="${this._i18n.visit} ${this.courseData.title}"
                style=${styleMap(this._styles)}>
              <div class="ms-2">${this.courseData.title}</div>
            </a>
          </div>
          ${this.courseData.shortDescription && this.courseData.shortDescription.trim() ? html`
          <div>
            <a href="${this.courseData.url}" title="${this._i18n.visit} ${this.courseData.title}">
              <div
                class="code-block description-block"
                title="${this.courseData.shortDescription.trim()}"
                style="display: -webkit-box; -webkit-line-clamp: var(--sakai-course-card-description-lines, 2); -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis;">
                ${this.courseData.shortDescription.trim()}
              </div>
            </a>
          </div>
          ` : nothing}
          ${this.courseData.canEdit ? html`
          <div class="ms-auto">
            <button type="button" class="btn btn-icon settings-button" title="${this._i18n.settings_tooltip}" @click=${this._openSettings}>
              <i class="bi bi-three-dots-vertical fs-6" style=${styleMap(this._styles)}></i>
            </button>
          </div>
          ` : nothing}
        </div>
      </div>

      <div class="tool-alerts-block">
        ${this.courseData.tools.filter(tool => tool.hasAlerts).map(tool => html`
          <div class="mx-2">
            <a href="${tool.url}" @click=${this._toolClicked} data-tool-id="${tool.id}" title="${tool.title}" style="position: relative;">
              <i class="si ${tool.iconClass}"></i>
              <span class="portal-notifications-indicator"><span class="visually-hidden">${tool.title}</span></span>
            </a>
          </div>
        `)}
      </div>
    `;
  }
}
