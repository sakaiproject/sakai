import { css, html, nothing } from "lit";
import { styleMap } from "lit/directives/style-map.js";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { pushSetupComplete, registerPushCallback } from "@sakai-ui/sakai-push-utils";
import { markNotificationsViewed } from "@sakai-ui/sakai-notifications";
import "@sakai-ui/sakai-course-card/course-card-settings.js";

export class SakaiCourseCard extends SakaiShadowElement {

  static properties = {
    courseData: { type: Object },
    siteId: { attribute: "site-id", type: String },
    density: { type: String },
    _styles: { state: true },
    _textStyles: { state: true },
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

  disconnectedCallback() {

    super.disconnectedCallback();

    if (this._lastBackgroundImageUrl) {
      URL.revokeObjectURL(this._lastBackgroundImageUrl);
    }
  }

  _init() {

    this._styles = { color: this.courseData.courseCardForegroundColor };
    if (this.courseData.image) {
      this._styles.backgroundImage = `linear-gradient(var(--sakai-course-card-gradient-start), var(--sakai-course-card-gradient-end)), url(${this.courseData.image})`;
    } else {
      this._styles.backgroundColor = this.courseData.courseCardBackgroundColor;
    }
    this._textStyles = { color: this.courseData.courseCardForegroundColor };

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
    this._textStyles = { color: e.detail.color };
    this.requestUpdate();
  }

  _imageSelected(e) {

    this._styles.backgroundImage = `linear-gradient(var(--sakai-course-card-gradient-start), var(--sakai-course-card-gradient-end)), url(${e.detail.url})`;
    this.requestUpdate();
  }

  _openSettingsModal() {

    const el = this.shadowRoot.querySelector("course-card-settings");
    el.showModal();
  }

  _save() {
    document.querySelector(`course-card-settings[course-id='${this.courseData.siteId}']`)._save();
  }

  shouldUpdate() {
    return this._i18n && this.courseData;
  }

  _effectiveDensity() {

    return this.density === "compact" ? "compact" : "comfortable";
  }

  _alertTools() {
    return this.courseData.tools.filter(tool => tool.hasAlerts);
  }

  _renderSettingsModal() {

    return html`
      <course-card-settings id="card-settings-dialog-${this.courseData.siteId}" course-id="${this.courseData.siteId}"
          course-title="${this.courseData.title}"
          background-color="${this.courseData.courseCardBackgroundColor}"
          foreground-color="${this.courseData.courseCardForegroundColor}"
          image-url="${this.courseData.image}"
          @image-selected=${this._imageSelected}
          @background-color-changed=${this._backgroundColorChanged}
          @foreground-color-changed=${this._foregroundColorChanged}>
      </course-card-settings>
    `;
  }

  render() {

    const density = this._effectiveDensity();
    const description = this.courseData.shortDescription?.trim();

    return html`
      ${this._renderSettingsModal()}
      <div class="info-block ${density}${this._alertTools().length === 0 ? " rounded-bottom" : ""}" style=${styleMap(this._styles)}>
        ${this.courseData.canEdit ? html`
          <button type="button"
              class="btn btn-icon settings-button"
              title="${this._i18n.settings_tooltip}"
              @click=${this._openSettingsModal}>
            <i class="bi bi-three-dots-vertical fs-6" style=${styleMap(this._textStyles)}></i>
          </button>
        ` : nothing}
        <div class="info-content ${this.courseData.canEdit ? "has-settings" : ""}">
          <a class="info-title ${!this.courseData.image ? "no-background" : ""}"
              href="${this.courseData.url}"
              title="${this._i18n.visit} ${this.courseData.title}"
              style=${styleMap(this._textStyles)}>
            ${this.courseData.title}
          </a>
          ${description ? html`
            <a class="info-description-link"
                href="${this.courseData.url}"
                title="${this._i18n.visit} ${this.courseData.title}">
              <div class="code-block description-block" title="${description}" style=${styleMap(this._textStyles)}>
                ${description}
              </div>
            </a>
          ` : nothing}
        </div>
      </div>

      ${this._alertTools().length ? html`
      <div class="tool-alerts-block">
        ${this._alertTools().map(tool => html`
          <div class="mx-2">
            <a href="${tool.url}" @click=${this._toolClicked} data-tool-id="${tool.id}" title="${tool.title}" style="position: relative;">
              <i class="si ${tool.iconClass}"></i>
              <span class="portal-notifications-indicator"><span class="visually-hidden">${tool.title}</span></span>
            </a>
          </div>
        `)}
      </div>
      ` : nothing}
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
      :host {
        display: block;
        width: 100%;
      }

      a {
        text-decoration: none;
      }

      .info-block {
        position: relative;
        display: flex;
        flex-direction: column;
        justify-content: center;
        box-sizing: border-box;
        height: 110px;
        background-repeat: no-repeat;
        background-color: var(--sakai-background-color-2);
        background-size: cover;
        background-position: center;
        color: var(--sakai-text-color-inverse, white);
        font-weight: bold;
        padding: 0.75rem 1rem;
        border-top-left-radius: var(--bs-border-radius);
        border-top-right-radius: var(--bs-border-radius);
        border: solid 1px var(--sakai-border-color);

        a {
          color: var(--sakai-text-color-inverse, white);
          background: transparent;
          &:hover {
            color: var(--sakai-text-color-inverse, white);
          }

          &.no-background {
            color: var(--link-color);
            &:hover {
              color: var(--link-color);
            }
          }
        }

        i {
          font-size: 20px;
        }

        &.compact {
          height: 72px;
          padding: 0.5rem 0.75rem;
        }

        &.compact .description-block {
          -webkit-line-clamp: 1;
          line-clamp: 1;
        }

        &.rounded-bottom {
          border-bottom-left-radius: var(--bs-border-radius);
          border-bottom-right-radius: var(--bs-border-radius);
        }
      }

      .info-content {
        display: flex;
        flex-direction: column;
        justify-content: center;
        min-width: 0;
        min-height: 0;
      }

      .info-content.has-settings {
        padding-inline-end: 1.75rem;
      }

      .info-title {
        min-width: 0;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;
        line-height: 1.25;
        background: transparent;
      }

      .info-block.compact .info-title {
        -webkit-line-clamp: 1;
        line-clamp: 1;
      }

      .settings-button {
        position: absolute;
        top: 0.35rem;
        inset-inline-end: 0.35rem;
        z-index: 1;
        background: transparent;
        --bs-btn-bg: transparent;
        --bs-btn-hover-bg: transparent;
        --bs-btn-active-bg: transparent;
        --bs-btn-color: inherit;
      }

      .info-description-link {
        margin-top: 0.35rem;
        min-width: 0;
        background: transparent;
      }

      .code-block {
        color: var(--sakai-text-color-inverse);
        font-size: 12px;
        font-weight: 400;
      }

      .description-block {
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: var(--sakai-course-card-description-lines, 2);
        line-clamp: var(--sakai-course-card-description-lines, 2);
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .tool-alerts-block {
        height: 40px;
        border-color: var(--sakai-border-color);
        color: var(--sakai-text-color-1);
        background-color: var(--sakai-background-color-3);
        display: flex;
        align-items: center;
        border-bottom-right-radius: var(--bs-border-radius);
        border-bottom-left-radius: var(--bs-border-radius);
        padding: 0.5rem;

        .portal-notifications-indicator {
          top: 2px;
          right: 2%;
          padding: 0.25rem;
          border-radius: 50%;
        }
      }
    `,
  ];
}
