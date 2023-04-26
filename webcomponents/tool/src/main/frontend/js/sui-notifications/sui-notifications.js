import { SakaiElement } from "../sakai-element.js";
import { html } from "../assets/lit-element/lit-element.js";
import "../sakai-user-photo.js";

/**
 *
 */
class SuiNotifications extends SakaiElement {

  constructor() {

    super();

    this.filteredNotifications = new Map();

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

      this.notifications = data.notifications || [];
      this.filterIntoToolNotifications();
      this.registerForNotifications();
      this.fireLoadedEvent();
    });
  }

  registerForNotifications() {

    portal.notifications.setup.then(() => {

      portal.notifications.registerPushCallback("all", message => {

        this.notifications.push(message);
        this.fireLoadedEvent();
        this.filterIntoToolNotifications();
      });
    });
  }

  filterIntoToolNotifications() {

    this.filteredNotifications.clear();

    this.notifications.forEach(noti => {


      // Grab the first section of the event. This is the tool event prefix.
      const toolEventPrefix = noti.event.substring(0, noti.event.indexOf("."));

      if (toolEventPrefix === "profile") {
        this._decorateProfileNotification(noti);
      } else if (toolEventPrefix === "asn") {
        this._decorateAssignmentNotification(noti);
      } else if (toolEventPrefix === "annc") {
        this._decorateAnnouncementNotification(noti);
      } else if (toolEventPrefix === "commons") {
        this._decorateCommonsNotification(noti);
      }

      if (!this.filteredNotifications.has(toolEventPrefix)) {
        this.filteredNotifications.set(toolEventPrefix, []);
      }

      this.filteredNotifications.get(toolEventPrefix).push(noti);
    });

    this.requestUpdate();
  }

  _decorateProfileNotification(noti) {

    switch (noti.event) {

      case "profile.friend.request":
        noti.title = this.i18n.connection_request_received.replace("{0}", noti.fromDisplayName);
        break;
      case "profile.friend.confirm":
        noti.title = this.i18n.connection_request_accepted.replace("{0}", noti.fromDisplayName);
        break;
      case "profile.message.sent":
        noti.title = this.i18n.message_received.replace("{0}", noti.fromDisplayName);
        break;
      default:
    }
  }

  _decorateAssignmentNotification(noti) {

    if (noti.event === "asn.new.assignment" || noti.event === "asn.revise.access") {
      noti.title = this.i18n.assignmentCreated.replace('{0}', noti.title).replace('{1}', noti.siteTitle);
    } else if (noti.event === "asn.grade.submission") {
      noti.title = this.i18n.assignmentSubmissionGraded.replace('{0}', noti.title).replace('{1}', noti.siteTitle);
    }
  }

  _decorateAnnouncementNotification(noti) {
    if (noti.event === "annc.new") {
      noti.title = this.i18n.announcement.replace('{0}', noti.title).replace('{1}', noti.siteTitle);
    }
  }

  _decorateCommonsNotification(noti) {
    noti.title = this.i18n.academicCommentCreated.replace('{0}', noti.siteTitle);
  }

  fireLoadedEvent() {
    this.dispatchEvent(new CustomEvent("notifications-loaded", { detail: { count: this.notifications.filter(n => !n.viewed).length }, bubbles: true }));
  }

  clearNotification(e) {

    const notificationId = e.target.dataset.notificationId;

    fetch(`/direct/portal/clearNotification?id=${notificationId}`, { cache: "no-store", credentials: "include" })
      .then(r => {

        if (r.ok) {
          const index = this.notifications.findIndex(a => a.id == notificationId);
          this.notifications.splice(index, 1);
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
          this.notifications = [];
          this.fireLoadedEvent();
          this.filterIntoToolNotifications();
        } else {
          console.error("Failed to clear all notifications");
        }
      });
  }

  markAllNotificationsViewed() {

    fetch("/direct/portal/markAllNotificationsViewed", { cache: "no-store", credentials: "include" })
      .then(r => {

        if (r.ok) {
          this.notifications?.forEach(a => a.viewed = true);
          this.requestUpdate();
          this.fireLoadedEvent();
        } else {
          console.error("Failed to mark all notifications as viewed");
        }
      });
  }


  renderAccordion(prefix, notifications) {

    return html`
      <div class="accordion-item rounded-1 mb-2">
        <h2 class="accordion-header mt-0">
          <button class="accordion-button collapsed"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#${prefix}-accordion"
              aria-expanded="false"
              aria-controls="${prefix}-accordion">
            ${this.i18n[prefix]}<span class="badge bg-secondary ms-2">${notifications.length}</span>
          </button>
        </h2>
        <div id="${prefix}-accordion" class="accordion-collapse collapse">
          <div class="accordion-body px-0 py-1 rounded-0">
            <ul class="list-unstyled d-flex flex-column align-items-center py-2">
              ${notifications.map(noti => html`
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
      <div class="accordion py-0">
        ${Array.from(this.filteredNotifications, e => e[0]).map(prefix => html`
          ${this.renderAccordion(prefix, this.filteredNotifications.get(prefix))}
        `)}
      </div>
      ${this.notifications?.length > 0 ? html`
        <div class="text-end my-2">
          ${this.notifications?.filter(a => !a.viewed).length > 0 ? html`
          <button class="btn btn-secondary text-end" @click=${this.markAllNotificationsViewed}>${this.i18n.mark_all_viewed}</button>
          ` : ""}
          <button class="btn btn-secondary text-end" @click=${this.clearAllNotifications}>${this.i18n.clear_all}</button>
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
