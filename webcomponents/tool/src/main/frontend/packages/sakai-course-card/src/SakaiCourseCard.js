import { html, nothing } from "lit";
import { ifDefined } from "lit-html/directives/if-defined.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { pushSetupComplete, registerPushCallback } from "@sakai-ui/sakai-push-utils";
import { markNotificationsViewed } from "@sakai-ui/sakai-notifications";
import "@sakai-ui/sakai-icon";

export class SakaiCourseCard extends SakaiElement {

  static properties = {
    courseData: { type: Object },
  };

  constructor() {

    super();

    this.loadTranslations("coursecard");
  }

  connectedCallback() {

    super.connectedCallback();

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

  shouldUpdate() {
    return this._i18n && this.courseData;
  }

  render() {

    return html`
      <div class="info-block"
          style="background: linear-gradient(var(--sakai-course-card-gradient-start), var(--sakai-course-card-gradient-end)), url(${ifDefined(this.courseData.image)})">
        <div>
          <a class="${!this.courseData.image ? "no-background" : ""}"
              href="${this.courseData.url}"
              title="${this._i18n.visit} ${this.courseData.title}">
            <div class="ms-2">${this.courseData.title}</div>
          </a>
        </div>
        ${this.courseData.shortDescription && this.courseData.shortDescription.trim() ? html`
          <a href="${this.courseData.url}" title="${this._i18n.visit} ${this.courseData.title}">
            <div
              class="code-block"
              title="${this.courseData.shortDescription}"
              style="display: -webkit-box; -webkit-line-clamp: var(--sakai-course-card-description-lines, 2); -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis;">
              ${this.courseData.shortDescription}
            </div>
          </a>
        ` : nothing}
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
