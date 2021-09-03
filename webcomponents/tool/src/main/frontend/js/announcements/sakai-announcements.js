import { css, html } from "../assets/lit-element/lit-element.js";
import '../sakai-icon.js';
import { SakaiPageableElement } from '../sakai-pageable-element.js';

export class SakaiAnnouncements extends SakaiPageableElement {

  constructor() {

    super();
    this.showPager = true;
    this.loadTranslations("announcements").then(r => this.i18n = r);
  }

  set data(value) {

    this._data = value;

    this.sites = [];
    const done = [];
    this._data.forEach(a => {
      a.visible = true;
      if (!done.includes(a.siteTitle)) {
        this.sites.push({id: a.siteId, title: a.siteTitle});
        done.push(a.siteTitle);
      }
    });
  }

  get data() { return this._data; }

  async loadAllData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/announcements`
      : `/api/users/${this.userId}/announcements`;

    return fetch(url)
      .then(r => {
        if (r.ok) {
          return r.json();
        } else {
          throw new Error(`Failed to get announcements from ${url}`);
        }
      })
      .then(data => this.data = data)
      .catch (error => console.error(error));
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

  content() {

    return html`
      <div id="topbar">
        ${!this.siteId ? html`
          <div id="site-filter">
            <select @change=${this.siteChanged}
                title="${this.i18n["site_tooltip"]}"
                aria-label="${this.i18n["site_tooltip"]}">
              <option value="none">${this.i18n["site"]}</option>
              ${this.sites.map(s => html`
              <option value="${s.id}">${s.title}</option>
              `)}
            </select>
          </div>
        ` : ""}
      </div>
      <div id="viewing">${this.i18n["viewing"]}</div>
      <div class="announcements ${this.siteId === "home" ? "home" : "course"}">
        <div class="header">
          <a href="javascript:;"
              title="${this.i18n["sort_by_title_tooltip"]}"
              aria-label="${this.i18n["sort_by_title_tooltip"]}"
              @click=${this.sortByTitle}>
            ${this.i18n["title"]}
          </a>
        </div>
        ${this.siteId === "home" ? html`
          <div class="header">
            <a href="javascript:;"
                title="${this.i18n["sort_by_site_tooltip"]}"
                aria-label="${this.i18n["sort_by_site_tooltip"]}"
                @click=${this.sortBySite}>
              ${this.i18n["site"]}
            </a>
          </div>
        ` : ""}
        <div class="header">${this.i18n["view"]}</div>
      ${this.dataPage.filter(a => a.visible).map((a, i) => html`
        <div class="title cell ${i % 2 === 0 ? "even" : "odd"}">${a.subject}</div>
        ${this.siteId === "home" ? html`
          <div class="site cell ${i % 2 === 0 ? "even" : "odd"}">${a.siteTitle}</div>
        ` : ""}
        <div class="url cell ${i % 2 === 0 ? "even" : "odd"}">
          <a href="${a.url}"
              title="${this.i18n["url_tooltip"]}"
              aria-label="${this.i18n["url_tooltip"]}">
            <sakai-icon type="right" size="small">
          </a>
        </div>
      `)}
      </div>
    `;
  }

  static get styles() {

    return [
      ...super.styles,
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
        #filter {
          flex: 1;
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
              color: var(--sakai-text-color-1, #000);
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
      `,
    ];
  }
}

if (!customElements.get("sakai-announcements")) {
  customElements.define("sakai-announcements", SakaiAnnouncements);
}
