import { SakaiElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-user-photo/sakai-user-photo.js";
import { callSubscribeIfPermitted, NOT_PUSH_CAPABLE, pushSetupComplete, registerPushCallback, PUSH_PERMISSION_STATES } from "@sakai-ui/sakai-push-utils";
import { getServiceName } from "@sakai-ui/sakai-portal-utils";
import { NOTIFICATIONS, PUSH_DENIED_INFO, PUSH_INTRO, PUSH_SETUP_INFO, PWA_INSTALL_INFO } from "./states.js";
import { markNotificationsViewed } from "./utils.js";

export class SakaiNotifications extends SakaiElement {

  static properties = {

    url: { type: String },
    chromeInfoUrl: { attribute: "chrome-info-url", type: String },
    firefoxInfoUrl: { attribute: "firefox-info-url", type: String },
    safariInfoUrl: { attribute: "safari-info-url", type: String },
    edgeInfoUrl: { attribute: "edge-info-url", type: String },
    _state: { state: true },
    _highlightTestButton: { state: true },
    _browserInfoUrl: { state: true },
    _pushEnabled: { state: true },
  };

  constructor() {

    super();

    /*
      Polyfill for Notification in unsupported environments.
      The Notification object can be undefined when browsing inside an iOS or Android webview.
      See: https://developer.mozilla.org/en-US/docs/Web/API/Notifications_API
    */
    if (typeof window.Notification === "undefined") {
      window.Notification = function() {};
      window.Notification.permission = "denied";
      window.Notification.requestPermission = function(cb) {
        if (cb) cb("denied");
        return Promise.resolve("denied");
      };
    }

    /*
      Polyfill for navigator.setAppBadge in unsupported environments.
      This API may be undefined in several environments
      See: https://developer.mozilla.org/en-US/docs/Web/API/Navigator/setAppBadge
    */
    if (typeof navigator.setAppBadge === "undefined") {
      navigator.setAppBadge = function() {
        return Promise.resolve();
      };
      navigator.clearAppBadge = function() {
        return Promise.resolve();
      };
    }

    window.addEventListener("online", () => this._online = true );

    this._filteredNotifications = new Map();
    this._i18nLoaded = this.loadTranslations("sakai-notifications");
    this._pushEnabled = false; // Default to false, will be set in _registerForNotifications
  }

  connectedCallback() {

    super.connectedCallback();

    this._state = NOTIFICATIONS;

    if (this.chromeInfoUrl && navigator.userAgent.includes("Chrome") && !navigator.userAgent.includes("Edg")) {
      this._browserInfoUrl = this.chromeInfoUrl;
    } else if (this.firefoxInfoUrl && navigator.userAgent.includes("Firefox")) {
      this._browserInfoUrl = this.firefoxInfoUrl;
    } else if (this.safariInfoUrl && navigator.userAgent.includes("Safari") && !navigator.userAgent.includes("Edg")) {
      this._browserInfoUrl = this.safariInfoUrl;
    } else if (this.safariInfoUrl && navigator.userAgent.includes("Edg")) {
      this._browserInfoUrl = this.edgeInfoUrl;
    }

    this._i18nLoaded.then(() => this._loadInitialNotifications());
  }

  _loadInitialNotifications(register = true) {

    console.debug("_loadInitialNotifications");

    fetch(this.url, {
      cache: "no-cache",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while retrieving notifications from ${this.url}`);
    })
    .then(notifications => {

      this.notifications = notifications;
      this._filterIntoToolNotifications();
      this._fireLoadedEvent();
      register && this._registerForNotifications();
    })
    .catch(error => console.error(error));
  }

  _registerForNotifications() {

    console.debug("registerForNotifications");

    pushSetupComplete.then(() => {

      this._pushEnabled = true;

      if (Notification.permission !== "granted") return;

      registerPushCallback("notifications", message => {

        this.notifications.unshift(message);
        this._fireLoadedEvent();
        this._filterIntoToolNotifications();
      });
    })
    .catch(error => {

      this._pushEnabled = false;

      if (error === NOT_PUSH_CAPABLE) {
        this._state = PUSH_SETUP_INFO;
      } else if (error === PUSH_PERMISSION_STATES.PWA_REQUIRED) {
        this._state = PWA_INSTALL_INFO;
      }
    });
  }

  _filterIntoToolNotifications() {

    this._filteredNotifications.clear();

    this.notifications.forEach(noti => {

      const decorated = this._decorateNotification(noti);
      const dot = decorated.event.indexOf(".");
      const toolEventPrefix = dot === -1 ? decorated.event : decorated.event.slice(0, dot);

      !this._filteredNotifications.has(toolEventPrefix) && this._filteredNotifications.set(toolEventPrefix, []);

      this._filteredNotifications.get(toolEventPrefix).push(decorated);
    });

    // Make sure the motd bundle is at the top.
    const entries = Array.from(this._filteredNotifications.entries());
    entries.sort((a, b) => (a[0] === "motd" ? -1 : b[0] === "motd" ? 1 : 0));
    this._filteredNotifications = new Map(entries);

    this._state = NOTIFICATIONS;
    this.requestUpdate();
  }

  _decorateNotification(noti) {

    const decorated = { ...noti };
    const dot = decorated.event.indexOf(".");
    const toolEventPrefix = dot === -1 ? decorated.event : decorated.event.slice(0, dot);

    if (toolEventPrefix === "asn") {
      this._decorateAssignmentNotification(decorated);
    } else if (toolEventPrefix === "annc") {
      this._decorateAnnouncementNotification(decorated);
    } else if (toolEventPrefix === "commons") {
      this._decorateCommonsNotification(decorated);
    } else if (toolEventPrefix === "sam") {
      this._decorateSamigoNotification(decorated);
    } else if (toolEventPrefix === "message") {
      this._decorateMessageNotification(decorated);
    } else if (toolEventPrefix === "lessonbuilder") {
      this._decorateLessonsCommentNotification(decorated);
    } else if (toolEventPrefix === "test") {
      this._decorateTestNotification(decorated);
    }

    return decorated;
  }

  _decorateAssignmentNotification(noti) {

    if (noti.event === "asn.new.assignment" || noti.event === "asn.revise.access") {
      noti.title = this._i18n.assignment_created.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    } else if (noti.event === "asn.grade.submission") {
      noti.title = this._i18n.assignment_submission_graded.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    }
  }

  _decorateAnnouncementNotification(noti) {

    if (noti.event === "annc.new" || noti.event === "annc.available.announcement") {
      noti.title = this._i18n.announcement.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    }
  }

  _decorateCommonsNotification(noti) {

    noti.title = this._i18n.academic_comment_graded.replace("{0}", noti.siteTitle);
  }

  _decorateSamigoNotification(noti) {

    if (noti.event === "sam.assessment.available" || noti.event === "sam.assessment.update.available") {
      noti.title = this._i18n.samigoCreated.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    }
  }

  _decorateMessageNotification(noti) {

    if (noti.event === "message.read.receipt") {
      noti.title = this._i18n.message_read.replace("{0}", noti.title).replace("{1}", noti.siteTitle);
    }
  }

  _decorateLessonsCommentNotification(noti) {

    noti.title = this._i18n.lessons_comment_posted.replace("{0}", noti.siteTitle);
  }

  _decorateTestNotification(noti) {

    noti.body = this._i18n.test_notification_body.replace("{0}", getServiceName());
    noti.bodyShowing = true;
  }

  _fireLoadedEvent() {

    const unviewed = this.notifications.filter(n => !n.viewed).length;
    this.dispatchEvent(new CustomEvent("notifications-loaded", { detail: { count: unviewed }, bubbles: true }));
    navigator.setAppBadge(unviewed);
  }

  _clearNotification(e) {

    const notificationId = e.target.dataset.notificationId;

    const url = `/api/users/me/notifications/${notificationId}/clear`;
    fetch(url, { method: "POST", credentials: "include" })
      .then(r => {

        if (r.ok) {
          const index = this.notifications.findIndex(a => a.id == notificationId);
          this.notifications.splice(index, 1);
          this._fireLoadedEvent();
          this._filterIntoToolNotifications();
        } else {
          throw new Error(`Network error while clearing notification at ${url}`);
        }
      })
      .catch(error => console.error(error));
  }

  _clearAllNotifications() {

    const url = "/api/users/me/notifications/clear";
    fetch(url, { method: "POST", credentials: "include" })
      .then(r => {

        if (r.ok) {
          this.notifications = [];
          this._fireLoadedEvent();
          this._filterIntoToolNotifications();
          this.dispatchEvent(new CustomEvent("notifications-cleared", { bubbles: true }));
        } else {
          throw new Error(`Network error while clearing all notifications at ${url}`);
        }
      })
      .catch(error => console.error(error));
  }

  _clearTestNotifications() {

    this.notifications = [ ...this.notifications.filter(n => !n.event.startsWith("test")) ];
    this._filterIntoToolNotifications();
  }

  _markAllNotificationsViewed() {

    markNotificationsViewed()
      .then(r => {

        if (r.ok) {
          this.notifications?.forEach(a => a.viewed = true);
          this.requestUpdate();
          this._fireLoadedEvent();
        } else {
          throw new Error("Network error while marking all notifications viewed");
        }
      })
      .catch(error => console.error(error));
  }

  _viewMotd(e) {

    const noti = this._filteredNotifications.get(e.target.dataset.prefix).find(n => n.ref === e.target.dataset.ref);

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

  _triggerPushSubscription() {

    callSubscribeIfPermitted().then(permission => {

      switch (permission) {
        case "denied":
          this._state = PUSH_DENIED_INFO;
          break;
        case "granted":
          this._loadInitialNotifications(true);
          this._highlightTestButton = true;
          break;
      }
    });
  }

  _showNotifications() { this._state = NOTIFICATIONS; }

  _enablePush() {

    if (Notification.permission === "denied") {
      this._state = PUSH_DENIED_INFO;
    } else {
      this._state = PUSH_INTRO;
    }
  }

  _sendTestNotification() {

    this._highlightTestButton = false;

    console.debug("Sending test notification...");

    const url = "/api/users/me/notifications/test";
    fetch(url, { method: "POST" })
    .then(r => {

      if (!r.ok) {
        console.error(`Test notification request failed with status ${r.status}: ${r.statusText}`);
        throw Error(`Network error while sending test notification at ${url}`);
      }

      console.debug("Test notification request sent successfully");
    })
    .catch(error => {
      console.error("Test notification error:", error);
    });
  }

  shouldUpdate() {
    return this._i18n;
  }

  _renderAccordion(prefix, notifications) {

    return html`
      <div class="accordion-item rounded-1 mb-2">
        <h2 class="accordion-header mt-0 fs-2">
          <button class="accordion-button ${prefix === "test" ? "" : "collapsed"}"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#${prefix}-accordion"
              aria-expanded="false"
              aria-controls="${prefix}-accordion">
            ${this._i18n[prefix]}<span class="badge bg-secondary ms-2">${notifications.length}</span>
          </button>
        </h2>
        <div id="${prefix}-accordion" class="accordion-collapse collapse ${prefix === "test" ? "show" : ""}">
          <div class="accordion-body px-0 py-1 rounded-0">
            <ul class="list-unstyled d-flex flex-column align-items-center py-2">
              ${notifications.map(noti => html`
              <li class="toast fade show mt-2 shadow-sm">
                <div class="toast-header">
                  <sakai-user-photo user-id="${noti.fromUser}" classes="mh-100 me-2" profile-popup="on"></sakai-user-photo>
                  <strong class="me-auto">${noti.fromDisplayName}</strong>
                  <small>${noti.formattedEventDate}</small>
                  ${prefix !== "motd" && prefix !== "test" ? html`
                    <button type="button"
                        class="btn-close"
                        aria-label="${this._i18n.clear_this_notification}"
                        data-notification-id="${noti.id}"
                        @click=${this._clearNotification}>
                    </button>
                  ` : nothing}
                </div>
                <div class="toast-body">
                  <div class="d-flex justify-content-between">
                    <div class="me-1">${noti.title}</div>
                    <div>
                      ${prefix !== "motd" && prefix !== "test" ? html`
                        <a href="${noti.url}">
                          <i class="si si-sakai-filled-right-arrow"></i>
                        </a>
                      ` : nothing}
                      ${prefix === "motd" ? html`
                        <button type="button"
                            data-ref="${noti.ref}"
                            data-prefix="${prefix}"
                            class="btn btn-link"
                            @click=${this._viewMotd}>
                          ${noti.bodyShowing ? this._i18n.hide : this._i18n.show}
                        </button>
                      ` : nothing}
                    </div>
                  </div>
                  ${noti.bodyShowing ? html`
                    <div class="mt-3">${unsafeHTML(noti.body)}</div>
                  ` : nothing}
                </div>
              </li>
              `)}
            </ul>
          </div>
          ${prefix === "test" ? html`
          <div>
            <button type="button"
                class="btn btn-link shadow-sm ms-2 mb-2"
                @click=${this._clearTestNotifications}>
              ${this._i18n.clear}
            </button>
          </div>
          ` : nothing}
        </div>
      </div>
    `;
  }

  render() {

    return html`
      ${this._state === PUSH_SETUP_INFO ? html`
        <div class="sak-banner-warn sakai-notifications__banner-warn">
          <div class="fw-bold">${this._i18n.push_setup_failure_info}</div>
          <ol class="mt-2">
            <li>${this._i18n.push_setup_failure_info_1.replace("{0}", getServiceName())}</li>
            <li>${this._i18n.push_setup_failure_info_2}</li>
            <li>${this._i18n.push_setup_failure_info_3}</li>
          </ol>
          <div class="fw-bold">${this._i18n.push_setup_failure_info_4.replaceAll("{}", getServiceName())}</div>
        </div>
      ` : nothing}

      ${this._state === PUSH_DENIED_INFO ? html`
        <div class="sak-banner-error sakai-notifications__banner-error">
          <div class="mb-3">${this._i18n.notifications_denied.replace("{0}", getServiceName())}</div>
          <div>${this._i18n.notifications_not_allowed2.replace("{0}", getServiceName())}</div>
          ${this._browserInfoUrl ? html`
          <div class="mt-3">
            <a href="${this._browserInfoUrl}" class="sakai-notifications__title" target="_blank">${this._i18n.browser_info_link_text}</a>
          </div>
          ` : nothing}
        </div>
        <div class="text-center">
          <button @click=${this._showNotifications} class="btn btn-primary mt-4">${this._i18n.show_notifications}</button>
        </div>
      ` : nothing}

      ${this._state === PWA_INSTALL_INFO ? html`
        <div class="sak-banner-info sakai-notifications__banner-info mb-3">
          <div>
            <div class="fw-bold mb-2">${this._i18n.pwa_install_title}</div>
            <div class="mb-1">${this._i18n.pwa_install_instructions}</div>
          </div>
        </div>
      ` : nothing}

      ${this._state === PUSH_INTRO ? html`
        <div class="sak-banner-info sakai-notifications__banner-info">
          <div>
            <div class="mb-1">${this._i18n.notifications_not_allowed.replace("{0}", getServiceName())}</div>
            <div class="sakai-notifications__title">${this._i18n.notifications_not_allowed2.replace("{0}", getServiceName())}</div>
          </div>
        </div>
        <div class="text-center">
          <button @click=${this._triggerPushSubscription} class="btn btn-primary mt-4">${this._i18n.accept_notifications}</button>
        </div>
      ` : nothing}

      ${this._state === NOTIFICATIONS || this._state === PWA_INSTALL_INFO ? html`
        ${Notification.permission !== "granted" && this._online && this._pushEnabled ? html`
          <div class="alert alert-warning">
            <span class="me-1">${this._i18n.push_not_enabled}</span>
            <button type="button"
                class="btn btn-secondary btn-sm"
                aria-label="${this._i18n.enable_push_label}"
                @click=${this._enablePush}>
              ${this._i18n.enable_push}
            </button>
          </div>
        ` : nothing}

        <div class="accordion py-0">
          ${Array.from(this._filteredNotifications, e => e[0]).map(prefix => html`
            ${this._renderAccordion(prefix, this._filteredNotifications.get(prefix))}
          `)}
        </div>

        ${!this.notifications?.length ? html`
          <div class="d-flex justify-content-around">
            <div><strong>${this._i18n.no_notifications}</strong></div>
          </div>
        ` : nothing}

        <div class="d-flex justify-content-between my-2">
          ${this._online && this._pushEnabled && Notification.permission === "granted" ? html`
            <div>
              <button class="btn ${this._highlightTestButton ? "btn-primary" : "btn-secondary"} btn-sm"
                  @click=${this._sendTestNotification}>
                ${this._i18n.test}
              </button>
            </div>
          ` : nothing}

          <div class="ms-auto text-end">

            ${Notification.permission !== "granted" && this._online ? html`
              <button class="btn btn-secondary btn-sm me-2"
                  @click=${this._loadInitialNotifications}>
                ${this._i18n.update}
              </button>
            ` : nothing}

            ${this._online && this.notifications?.filter(a => !a.viewed).length > 0 ? html`
              <button class="btn btn-secondary btn-sm"
                  @click=${this._markAllNotificationsViewed}>
                ${this._i18n.mark_all_viewed}
              </button>
            ` : nothing}
            ${this._online && this.notifications?.length ? html`
              <button id="sakai-notifications-clear-all-button"
                  class="btn btn-secondary btn-sm"
                  @click=${this._clearAllNotifications}>
                ${this._i18n.clear_all}
              </button>
            ` : nothing}
          </div>
        </div>
      ` : nothing}
    `;
  }
}
