import { css, html } from "../assets/lit-element/lit-element.js";
import '../sakai-icon.js';
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';

export class SakaiAnnouncementsWidget extends SakaiDashboardWidget {

  constructor() {

    super();
    this.widgetId = "announcements";
    this.loadTranslations("announcement");
  }

  set data(value) {

    this._data = value;
    this.sites = this._data.map(a => ({id: a.siteId, title: a.siteTitle}));
    this._data.forEach(a => a.visible = true);
    this.requestUpdate();
  }

  get data() { return this._data; }

  loadData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/announcements`
      : `/api/users/${this.userId}/announcements`;

    fetch(url)
      .then(r => {
        if (r.ok) {
          return r.json();
        } else {
          throw new Error(`Failed to get announcements from ${url}`);
        }
      })
      .then(announcements => {
        this.data = announcements;
      })
      .catch (error => console.error(error));
  }

  searchEntered(e) {

    if (e.target.value.length === 0) {
      this.data.forEach(a => a.visible = true);
    } else {
      const search = e.target.value.toLowerCase();
      this.data.forEach(a => {
        a.visible = !(!a.subject.toLowerCase().includes(search) && !a.author.toLowerCase().includes(search) && !a.siteTitle.toLowerCase().includes(search));
      });
    }
    this.requestUpdate();
  }

  siteChanged(e) {

    if (e.target.value === "none") {
      this.data.forEach(a => a.visible = true);
    } else {
      this.data.forEach(a => a.visible = a.siteId === e.target.value);
    }
    this.requestUpdate();
  }

  sortByTitle() {

    if (this.sortTitle === "AZ") {
      this.data.sort((a1, a2) => a1.subject.localeCompare(a2.subject));
      this.sortTitle = "ZA";
    } else {
      this.data.sort((a1, a2) => a2.subject.localeCompare(a1.subject));
      this.sortTitle = "AZ";
    }
    this.requestUpdate();
  }

  sortBySite() {

    if (this.sortSite === "AZ") {
      this.data.sort((a1, a2) => a1.siteTitle.localeCompare(a2.siteTitle));
      this.sortSite = "ZA";
    } else {
      this.data.sort((a1, a2) => a2.siteTitle.localeCompare(a1.siteTitle));
      this.sortSite = "AZ";
    }
    this.requestUpdate();
  }

  content() {

    return html`
      <div id="topbar">
        ${this.siteId === "home" ? html`
          <div id="site-filter">
            <select @change=${this.siteChanged}>
              <option value="none">${this.i18n["site"]}</option>
              ${this.sites.map(s => html`
              <option value="${s.id}">${s.title}</option>
              `)}
            </select>
          </div>
        ` : ""}
        <div id="search">
          <input type="text" @input=${this.searchEntered} placeholder="${this.i18n["search"]}">
        </div>
      </div>
      <div id="viewing">${this.i18n["viewing"]}</div>
      <div class="announcements ${this.siteId === "home" ? "home" : "course"}">
        <div class="header"><a href="javascript:;" title="${this.i18n["sort_by_title_tooltip"]}" @click=${this.sortByTitle}>${this.i18n["title"]}</a></div>
        ${this.siteId === "home" ? html`
          <div class="header"><a href="javascript:;" title="${this.i18n["sort_by_site_tooltip"]}" @click=${this.sortBySite}>${this.i18n["site"]}</a></div>
        ` : ""}
        <div class="header">${this.i18n["view"]}</div>
      ${this.data.filter(a => a.visible).map((a, i) => html`
        <div class="title cell ${i % 2 === 0 ? "even" : "odd"}">${a.subject}</div>
        ${this.siteId === "home" ? html`
          <div class="site cell ${i % 2 === 0 ? "even" : "odd"}">${a.siteTitle}</div>
        ` : ""}
        <div class="url cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${a.url}"><sakai-icon type="right" size="small"></a></div>
      `)}
      </div>
    `
  }

  static get styles() {

    return css`
      ${SakaiDashboardWidget.styles}
      #filter {
        flex: 1;
      }
      #search {
        flex: 1;
        text-align: right;
      }
      #viewing {
        margin-bottom: 20px;
        font-size: var(--sakai-grades-title-font-size, 14px);
      }
      .announcements {
        display:grid;
        grid-auto-rows: minmax(10px, auto);
      }

      .home {
        grid-template-columns: 4fr 1fr 0fr;
      }

      .course {
        grid-template-columns: 4fr 0fr;
      }
        .announcements > div:nth-child(-n+3) {
          padding-bottom: 14px;
        }
        .header {
          font-weight: bold;
          padding: 0 5px 0 5px;
        }
          .header a {
            text-decoration: none;
            color: var(--sakai-text-color-1);
          }
        .title {
          flex: 2;
        }
        .cell {
          display: flex;
          align-items: center;
          padding: 8px;
          font-size: var(--sakai-grades-title-font-size);
        }
        .even {
          background-color: var(--sakai-table-even-color);
        }
        .site {
          flex: 1;
        }
        .url {
          flex: 1;
        }
    `;
  }
}

if (!customElements.get("sakai-announcements-widget")) {
  customElements.define("sakai-announcements-widget", SakaiAnnouncementsWidget);
}
