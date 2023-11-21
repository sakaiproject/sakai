import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-user-photo";
import { clearAppBadge, setAppBadge } from "@sakai-ui/sakai-portal-utils";

export class SakaiNotifications extends SakaiElement {

  static properties = {

    url: { type: String },
    userId: { attribute: "user-id", type: String },
  };

  constructor() {

    super();

    this.filteredNotifications = new Map();
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "user-id" && (oldValue !== newValue)) {
      this.i18nLoaded = this.loadTranslations("sakai-notifications");
      this.i18nLoaded.then(i18n => {

        this.i18n = i18n;
        this.loadInitialNotifications();
      });
    }
  }

  loadInitialNotifications(register = true) {

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
      if (register) {
        this.registerForNotifications();
      }
      this.fireLoadedEvent();
    });
  }

  registerForNotifications() {

    portal.notifications.setup.then(() => {

      portal.notifications.registerPushCallback("all", message => {

        this.notifications.unshift(message);
        this.fireLoadedEvent();
        this.filterIntoToolNotifications();
      });
    });
  }

  filterIntoToolNotifications(decorate = true) {

    this.filteredNotifications.clear();

    const unviewed = this.notifications.filter(n => !n.viewed).length;
    if (unviewed === 0) clearAppBadge();
    else setAppBadge(unviewed);

    this.notifications.forEach(noti => {

      // Grab the first section of the event. This is the tool event prefix.
      const toolEventPrefix = noti.event.substring(0, noti.event.indexOf("."));

      if (decorate) {
        if (toolEventPrefix === "profile") {
          this._decorateProfileNotification(noti);
        } else if (toolEventPrefix === "asn") {
          this._decorateAssignmentNotification(noti);
        } else if (toolEventPrefix === "annc") {
          this._decorateAnnouncementNotification(noti);
        } else if (toolEventPrefix === "commons") {
          this._decorateCommonsNotification(noti);
        } else if (toolEventPrefix === "sam") {
          this._decorateSamigoNotification(noti);
        } else if (toolEventPrefix === "message") {
          this._decorateMessageNotification(noti);
        }
      }

      if (!this.filteredNotifications.has(toolEventPrefix)) {
        this.filteredNotifications.set(toolEventPrefix, []);
      }

      this.filteredNotifications.get(toolEventPrefix).push(noti);
    });

    // Make sure the motd bundle is at the top.
    const newMap = Array.from(this.filteredNotifications).sort(a => a === "motd" ? 1 : -1);
    this.filteredNotifications = new Map(newMap);

    this.requestUpdate();
  }

  _decorateProfileNotification(noti) {

    this.i18nLoaded.then(() => {

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
    });
  }

  _decorateAssignmentNotification(noti) {

    if (noti.event === "asn.new.assignment" || noti.event === "asn.revise.access") {
      noti.title = this.i18n.assignment_created.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    } else if (noti.event === "asn.grade.submission") {
      noti.title = this.i18n.assignment_submission_graded.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    }
  }

  _decorateAnnouncementNotification(noti) {

    if (noti.event === "annc.new" || noti.event === "annc.available.announcement") {
      this.i18nLoaded.then(() => {
        noti.title = this.i18n.announcement.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
      });
    }
  }

  _decorateCommonsNotification(noti) {
    noti.title = this.i18n.academic_comment_graded.replace("{0}", noti.siteTitle);
  }

  _decorateSamigoNotification(noti) {

    if (noti.event === "sam.assessment.available" || noti.event === "sam.assessment.update.available") {
      noti.title = this.i18n.samigoCreated.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    }
  }

  _decorateMessageNotification(noti) {

    if (noti.event === "message.read.receipt") {
      noti.title = this.i18n.message_read.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    }
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
          this.filterIntoToolNotifications(false);
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
          clearAppBadge();
          this.requestUpdate();
          this.fireLoadedEvent();
        } else {
          console.error("Failed to mark all notifications as viewed");
        }
      });
  }

  _viewMotd(e) {

    const noti = this.filteredNotifications.get(e.target.dataset.prefix).find(n => n.ref === e.target.dataset.ref);

    if (!noti?.body) {
      const url = `/direct${e.target.dataset.ref}.json`;
      fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Network error while retrieving MOTD from ${url}`);
      })
      .then(motd => {

        noti.body = motd.body;
        noti.bodyShowing = true;
        this.requestUpdate();
      })
      .catch(error => console.error(error));
    } else {
      noti.bodyShowing = !noti.bodyShowing;
      this.requestUpdate();
    }
  }

  renderAccordion(prefix, notifications) {

    return html`
      <div class="accordion-item rounded-1 mb-2">
        <h2 class="accordion-header mt-0 fs-2">
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
                <div class="toast-body">
                  <div class="d-flex justify-content-between">
                    <div>${noti.title}</div>
                    <div>
                      ${prefix !== "motd" ? html`
                      <a href="${noti.url}">
                        <i class="si si-sakai-filled-right-arrow"></i>
                      </a>
                      ` : html`
                      <button type="button"
                          data-ref="${noti.ref}"
                          data-prefix="${prefix}"
                          class="btn btn-link"
                          @click=${this._viewMotd}>
                        ${noti.bodyShowing ? this.i18n.hide : this.i18n.show}
                      </button>
                      `}
                    </div>
                  </div>
                  ${noti.bodyShowing ? html`
                    <div class="mt-3">${unsafeHTML(noti.body)}</div>
                  ` : ""}
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
          <button id="sakai-notifications-clear-all-button"
              class="btn btn-secondary text-end"
              @click=${this.clearAllNotifications}>
            ${this.i18n.clear_all}
          </button>
        </div>
      ` : html`
      <div class="d-flex justify-content-around">
        <div><strong>${this.i18n.no_notifications}</strong></div>
      </div>
      `}
    `;
  }
}
