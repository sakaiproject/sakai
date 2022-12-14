import { SakaiElement } from "../sakai-element.js";
import { html } from "../assets/lit-element/lit-element.js";
import "../sakai-user-photo.js";

/**
 *
 */
class SuiNotifications extends SakaiElement {

  constructor() {

    super();

    this.announcementNotifications = [];
    this.assignmentsNotifications = [];

    this.loadTranslations("sui-notifications").then(i18n => { this.i18n = i18n; this.requestUpdate(); });
  }

  static get properties() {

    return {
      url: { type: String },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.url) {
      this.loadInitialNotifications();
    }
  }

  loadInitialNotifications() {

    fetch(this.url, {
      credentials: "include",
      cache: "no-cache",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Network error while retrieving notifications");
    })
    .then(data => {

      this.alerts = data.alerts || [];
      this.filterIntoToolNotifications();
      this.registerForNotifications();
      this.fireLoadedEvent();
    });
  }

  registerForNotifications() {

    portal.notifications.setup.then(() => {

      portal.notifications.registerForMessages("notifications", message => {

        this.alerts.push(message);
        this.fireLoadedEvent();
        this.filterIntoToolNotifications();
      });
    });
  }

  filterIntoToolNotifications() {

    this.assignmentsNotifications = [];
    this.announcementNotifications = [];

    this.alerts.forEach(noti => {

      if (noti.event.startsWith("asn")) {
        this.assignmentsNotifications.push(noti);
      } else if (noti.event.startsWith("annc")) {
        this.announcementNotifications.push(noti);
      }
    });

    this.requestUpdate();
  }

  fireLoadedEvent() {
    this.dispatchEvent(new CustomEvent("notifications-loaded", { detail: { count: this.alerts.length }, bubbles: true }));
  }

  clearNotification(e) {

    const notificationId = e.target.dataset.notificationId;

    fetch(`/direct/portal/clearNotification?id=${notificationId}`, { cache: "no-store", credentials: "include" })
      .then(r => {

        if (r.ok) {
          const index = this.alerts.findIndex(a => a.id == notificationId);
          this.alerts.splice(index, 1);
          this.fireLoadedEvent();
          this.filterIntoToolNotifications();
        } else {
          console.error(`Failed to clear notification with id ${notificationId}`);
        }
      });
  }

  clearAllNotifications() {

    fetch("/direct/portal/clearAllNotifications", { cache: "no-store", credentials: "include" })
      .then(r => {

        if (r.ok) {
          this.alerts = [];
          this.fireLoadedEvent();
          this.filterIntoToolNotifications();
        } else {
          console.error("Failed to clear all notifications");
        }
      });
  }

  renderAccordion(type, data) {

    return html`
      <div class="accordion-item">
        <h2 class="accordion-header">
          <button class="accordion-button collapsed"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#${type}-accordion"
              aria-expanded="false"
              aria-controls="${type}-accordion">
            ${this.i18n[type]}<span class="badge bg-secondary ms-1">${data.length}</span>
          </button>
        </h2>
        <div id="${type}-accordion" class="accordion-collapse collapse">
          <div class="accordion-body p-0">
            <ul class="list-unstyled d-flex flex-column align-items-center">
              ${data.map(noti => html`
              <li class="toast fade show mt-2 shadow-sm">
                <div class="toast-header">
                  <sakai-user-photo user-id="${noti.fromUser}" classes="mh-100 me-2" profile-popup="on"></sakai-user-photo>
                  <strong class="me-auto">${noti.fromDisplayName}</strong>
                  <small>${noti.formattedEventDate}</small>
                  <button type="button" class="btn-close" aria-label="Close" data-notification-id="${noti.id}" @click=${this.clearNotification}></button>
                </div>
                <div class="toast-body d-flex justify-content-between">
                  <div>${noti.title}</div>
                  <div>
                    <a href="${noti.url}">
                      <i class="si si-sakai-filled-right-arrow"></i>
                    </a>
                  </div>
                </div>
              </li>
              `)}
            </ul>
          </div>
        </div>
      </div>
    `;
  }

  shouldUpdate() {
    return this.i18n;
  }

  render() {

    return html`
      <div class="accordion" id="sakai-notifications-accordion">
        ${this.assignmentsNotifications.length > 0 ? html`
          ${this.renderAccordion("assignments", this.assignmentsNotifications)}
        ` : ""}
        ${this.announcementNotifications.length > 0 ? html`
          ${this.renderAccordion("announcements", this.announcementNotifications)}
        ` : ""}
      </div>
      ${this.alerts?.length > 0 ? html`
        <div class="text-end my-2">
          <a href="javascript:;" class="text-end" @click=${this.clearAllNotifications}>${this.i18n.clear_all}</a>
        </div>
      ` : html`
      <div class="d-flex justify-content-around">
        <div><strong>${this.i18n.no_notifications}</strong></div>
      </div>
      `}
    `;
  }
}

const tagName = "sui-notifications";
!customElements.get(tagName) && customElements.define(tagName, SuiNotifications);
