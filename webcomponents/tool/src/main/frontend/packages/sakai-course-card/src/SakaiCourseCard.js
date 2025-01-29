import { html } from "lit";
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

    this.loadTranslations("coursecard").then(r => this._i18n = r);
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
      <div class="info-block fw-bold rounded-top rounded-top-2 p-4"
          style="background: linear-gradient( rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5) ), url(${ifDefined(this.courseData.image)})">
        <div>
          <a class="${!this.courseData.image ? "no-background" : ""}"
              href="${this.courseData.url}"
              title="${this._i18n.visit} ${this.courseData.title}">
            <div class="ms-2">${this.courseData.title}</div>
          </a>
        </div>
        <a href="${this.courseData.url}" title="${this._i18n.visit} ${this.courseData.title}">
          <div class="code-block">${this.courseData.code}</div>
        </a>
      </div>

      <div class="tool-alerts-block d-flex align-items-center rounded-bottom rounded-bottom-1 p-2">
        ${this.courseData.tools.filter(tool => tool.hasAlerts).map(tool => html`
          <div class="mx-2">
            <a href="${tool.url}" @click=${this._toolClicked} data-tool-id="${tool.id}" title="${tool.title}" style="position: relative;">
              <i class="si ${tool.iconClass}"></i>
              <span class="portal-notifications-indicator p-1 rounded-circle"><span class="visually-hidden">sdfs</span></span>
            </a>
          </div>
        `)}
      </div>
    `;
  }
}
