import { css, html, nothing } from "lit";
import "@sakai-ui/sakai-icon";
import { SakaiPageableElement } from "@sakai-ui/sakai-pageable-element";
import { SakaiSitePicker } from "@sakai-ui/sakai-site-picker";
import "@sakai-ui/sakai-site-picker/sakai-site-picker.js";

export class SakaiForums extends SakaiPageableElement {

  constructor() {

    super();

    this.showPager = true;
    this.loadTranslations("forums");
  }

  async loadAllData() {

    this.messagesClass = !this.siteId ? "three-col" : "two-col";

    const url = this.siteId ? `/api/sites/${this.siteId}/forums/summary` : "/api/users/current/forums/summary";
    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get forums data from ${url}`);

      })
      .then(data => {

        !this.siteId && (this.sites = data.sites);

        this._allData = data.forums;
        this.data = data.forums;

      })
      .catch (error => console.error(error));
  }

  toggleSite(e) {

    const forum = this.data.find(f => f.siteId === e.target.dataset.siteId);
    forum.hidden = e.target.checked;
    this.requestUpdate();
  }

  sortByMessages() {

    if (this.sortMessages === "01") {
      this.data.sort((a1, a2) => a2.messageCount - a1.messageCount);
      this.sortMessages = "10";
    } else {
      this.data.sort((a1, a2) => a1.messageCount - a2.messageCount);
      this.sortMessages = "01";
    }
    this.repage();
  }

  sortByForums() {

    if (this.sortForums === "01") {
      this.data.sort((a1, a2) => a2.forumCount - a1.forumCount);
      this.sortForums = "10";
    } else {
      this.data.sort((a1, a2) => a1.forumCount - a2.forumCount);
      this.sortForums = "01";
    }
    this.repage();
  }

  sortBySite() {

    if (this.sortSite === "AZ") {
      this.data.sort((a1, a2) => a1.siteTitle.localeCompare(a2.siteTitle));
      this.sortSite = "ZA";
    } else {
      this.data.sort((a1, a2) => a2.siteTitle.localeCompare(a1.siteTitle));
      this.sortSite = "AZ";
    }
    this.repage();
  }

  _filter() {

    this.data = [ ... this._allData ];

    if (this._currentFilter === "sites" && this._selectedSites !== SakaiSitePicker.ALL) {
      this.data = [ ...this.data.filter(s => this._selectedSites.includes(s.siteId)) ];
    }

    this.repage();
  }

  _sitesSelected(e) {

    this._selectedSites = e.detail.value;
    this._currentFilter = "sites";
    this._filter();
  }

  shouldUpdate(changedProperties) {
    return this._i18n && super.shouldUpdate(changedProperties);
  }

  content() {

    return html`

      ${!this.siteId ? html`
      <div id="site-filter">
        <sakai-site-picker
            .sites=${this.sites}
            @sites-selected=${this._sitesSelected}>
        </sakai-site-picker>
      </div>
      ` : nothing}

      <div class="messages ${this.messagesClass}">
        <div class="header">
          <a href="javascript:;"
              @click=${this.sortByMessages}
              title="${this._i18n.sort_by_messages_tooltip}"
              aria-label="${this._i18n.sort_by_messages_tooltip}">
            ${this._i18n.syn_private_heading}
          </a>
        </div>
        <div class="header">
          <a href="javascript:;"
              @click=${this.sortByForums}
              title="${this._i18n.sort_by_forums_tooltip}"
              aria-label="${this._i18n.sort_by_forums_tooltip}">
            ${this._i18n.syn_discussion_heading}
          </a>
        </div>
        ${!this.siteId ? html`
        <div class="header">
          <a href="javascript:;"
              @click=${this.sortBySite}
              title="${this._i18n.sort_by_site_tooltip}"
              aria-label="${this._i18n.sort_by_site_tooltip}">
            ${this._i18n.syn_site_heading}
          </a>
        </div>
        ` : nothing}
      ${this.dataPage.map((m, i) => html`
        <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.messageUrl}">${m.messageCount}</a></div>
        <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.forumUrl}">${m.forumCount}</a></div>
        ${!this.siteId ? html`
        <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.siteUrl}">${m.siteDescription || m.siteTitle}</a></div>
        ` : nothing}
      `)}
      </div>
    `;
  }

  static styles = [
    SakaiPageableElement.styles,
    css`
      a {
        color: var(--link-color);
      }
      a:hover { 
        color: var(--link-hover-color);
      }
      a:active {
        color: var(--link-active-color);
      }
      a:visited {
        color: var(--link-visited-color);
      }
      #site-filter {
        margin-bottom: 0.25rem;
      }
      #site-filter sakai-site-picker::part(select) {
        width: 100%;
      }
      .messages {
        display: grid;
        grid-auto-rows: minmax(10px, auto);
      }
        .three-col {
          grid-template-columns: 1fr 1fr 1fr;
        }
        .two-col {
          grid-template-columns: 1fr 1fr;
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
            color: var(--sakai-text-color-1, #000);
          }
        .cell {
          padding: 8px;
          font-size: var(--sakai-grades-title-font-size);
        }
        .even {
          background-color: var(--sakai-table-even-color);
        }
    `,
  ];
}
