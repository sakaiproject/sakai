import { css, html } from "../assets/lit-element/lit-element.js";
import '../sakai-icon.js';
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';

export class SakaiForumsWidget extends SakaiDashboardWidget {

  static get properties() {

    return {
      forums: { type: Array },
      showOptions: Boolean,
    };
  }

  constructor() {

    super();
    this.widgetId = "forums";
    this.messagesClass = "three-col";
    this.loadTranslations({bundle: "org.sakaiproject.api.app.messagecenter.bundle.Messages"});
  }

  loadData() {

    this.messagesClass = this._siteId === "home" ? "three-col" : "two-col";

    let url = this.siteId ? `/api/sites/${this.siteId}/forums` : `/api/users/${this.userId}/forums`;
    fetch(url)
      .then(r => {
        if (r.ok) {
          return r.json();
        } else {
          throw new Error(`Failed to get forums data from ${url}`);
        }
      })
      .then(forums => {
        this.forums = forums;
      })
      .catch (error => console.error(error));
  }

  toggleSite(e) {

    let forum = this.forums.find(f => f.siteId === e.target.dataset.siteId);
    forum.hidden = e.target.checked;
    this.requestUpdate();
  }

  sortByMessages() {

    if (this.sortMessages === "01") {
      this.forums.sort((a1, a2) => a2.messageCount - a1.messageCount);
      this.sortMessages = "10";
    } else {
      this.forums.sort((a1, a2) => a1.messageCount - a2.messageCount);
      this.sortMessages = "01";
    }
    this.requestUpdate();
  }

  sortByForums() {

    if (this.sortForums === "01") {
      this.forums.sort((a1, a2) => a2.forumCount - a1.forumCount);
      this.sortForums = "10";
    } else {
      this.forums.sort((a1, a2) => a1.forumCount - a2.forumCount);
      this.sortForums = "01";
    }
    this.requestUpdate();
  }

  sortBySite() {

    if (this.sortSite === "AZ") {
      this.forums.sort((a1, a2) => a1.siteTitle.localeCompare(a2.siteTitle));
      this.sortSite = "ZA";
    } else {
      this.forums.sort((a1, a2) => a2.siteTitle.localeCompare(a1.siteTitle));
      this.sortSite = "AZ";
    }
    this.requestUpdate();
  }

  set showOptions(value) {

    let old = this._showOptions;
    this._showOptions = value;
    if (this._showOptions) {
      this.messagesClass = this.siteId === "home" ? "four-col" : "three-col";
    } else {
      this.messagesClass = this.siteId === "home" ? "three-col" : "two-col";
    }
    this.requestUpdate("showOptions", old);
  }

  get showOptions() { return this._showOptions; }

  shouldUpdate(changed) {
    return this.forums;
  }

  content() {

    return html`
      <div id="options"><input type="checkbox" @click=${(e) => this.showOptions = e.target.checked}><span>Show Options</span></div>
      <div class="messages ${this.messagesClass}">
        ${this.showOptions ? html`<div class="header">Hide</div>` : ""}
        <div class="header"><a href="javascript:;" @click=${this.sortByMessages} title="${this.i18n["sort_by_messages_tooltip"]}">New Messages</a></div>
        <div class="header"><a href="javascript:;" @click=${this.sortByForums} title="${this.i18n["sort_by_forums_tooltip"]}">New in Forums</a></div>
        ${this.siteId === "home" ? html`
        <div class="header">
          <a href="javascript:;" @click=${this.sortBySite} title="${this.i18n["sort_by_site_tooltip"]}">Site</a>
        </div>
        ` : ""}
      ${this.forums.map((m, i) => html`
        ${!m.hidden || this.showOptions ? html`
        ${this.showOptions ? html`
          <div class="cell options ${i % 2 === 0 ? "even" : "odd"}">
            <input type="checkbox" data-site-id="${m.siteId}" ?checked=${m.hidden} @click=${this.toggleSite} arial-label="Hide this site from this forums/messages view">
          </div>`
        : ""}
        <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.messageUrl}">${m.messageCount}</a></div>
        <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.forumUrl}">${m.forumCount}</a></div>
          ${this.siteId === "home" ? html`
          <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.siteUrl}">${m.siteTitle}</a></div>
          ` : ""}
        ` : ""}
      `)}
      </div>
    `;
  }

  static get styles() {

    return css`
      ${SakaiDashboardWidget.styles}

      #options {
        margin-bottom: 8px;
        margin-top: 10px;
      }

      .messages {
        display: grid;
        grid-auto-rows: minmax(10px, auto);
      }
        .four-col {
          grid-template-columns: 1fr 1fr 1fr 1fr;
        }
        .three-col {
          grid-template-columns: 1fr 1fr 1fr;
        }
        .two-col {
          grid-template-columns: 1fr 1fr;
        }
        #options span {
          margin-left: 5px;
          font-size: 14px;
        }
        .messages > div:nth-child(-n+3) {
          padding-bottom: 14px;
        }
        .header {
          font-weight: bold;
          padding: 0 5px 0 5px;
        }
          .header a {
            text-decoration: none;
            color: black;
          }
        .cell {
          padding: 8px;
          font-size: var(--sakai-grades-title-font-size);
        }
        .even {
          background-color: var(--sakai-table-even-color);
        }
    `;
  }
}

if (!customElements.get("sakai-forums-widget")) {
  customElements.define("sakai-forums-widget", SakaiForumsWidget);
}
