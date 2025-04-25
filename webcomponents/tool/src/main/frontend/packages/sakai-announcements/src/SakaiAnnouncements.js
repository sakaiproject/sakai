import { css, html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-icon";
import { SakaiPageableElement } from "@sakai-ui/sakai-pageable-element";
import { SakaiSitePicker } from "@sakai-ui/sakai-site-picker";
import "@sakai-ui/sakai-site-picker/sakai-site-picker.js";
import {
  TITLE_A_TO_Z,
  TITLE_Z_TO_A,
  SITE_A_TO_Z,
  SITE_Z_TO_A,
  EARLIEST_FIRST,
  LATEST_FIRST
} from "./sakai-announcements-constants.js";

export class SakaiAnnouncements extends SakaiPageableElement {

  constructor() {

    super();

    this.showPager = true;
    this.loadTranslations("announcements").then(r => this._i18n = r);
  }

  set data(value) {

    this._data = value;

    this._data.forEach(a => a.visible = true);

    if (!this.siteId) {
      this._sites = this._data.reduce((acc, a) => {
        if (!acc.some(t => t.siteId === a.siteId)) acc.push({ siteId: a.siteId, title: a.siteTitle });
        return acc;
      }, []);
    }
  }

  get data() { return this._data; }

  async loadAllData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/announcements`
      : "/api/users/me/announcements";

    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get announcements from ${url}`);

      })
      .then(data => this.data = data)
      .catch (error => console.error(error));
  }

  _sitesSelected(e) {

    if (e.detail.value === SakaiSitePicker.ALL) {
      this.data.forEach(a => a.visible = true);
    } else {
      this.data.forEach(a => a.visible = a.siteId === e.detail.value);
    }
    this.requestUpdate();
  }

  _sortChanged(e) {

    switch (e.target.value) {
      case TITLE_A_TO_Z:
        this.data.sort((a1, a2) => a1.subject.localeCompare(a2.subject));
        break;
      case TITLE_Z_TO_A:
        this.data.sort((a1, a2) => a2.subject.localeCompare(a1.subject));
        break;
      case SITE_A_TO_Z:
        this.data.sort((a1, a2) => a1.siteTitle.localeCompare(a2.siteTitle));
        break;
      case SITE_Z_TO_A:
        this.data.sort((a1, a2) => a2.siteTitle.localeCompare(a1.siteTitle));
        break;
      case EARLIEST_FIRST:
        this.data.sort((a1, a2) => {

          if (a1.date < a2.date) return -1;
          if (a1.date < a2.date) return 1;
          return 0;
        });
        break;
      case LATEST_FIRST:
        this.data.sort((a1, a2) => {

          if (a1.date < a2.date) return 1;
          if (a1.date > a2.date) return -1;
          return 0;
        });
        break;
      default:
        console.warn(`Invalid sort option: ${e.target.value}`);
    }

    this.repage();
  }

  content() {

    return html`
      <div id="filter-and-sort-block">
        ${!this.siteId ? html`
        <div id="site-filter">
          <sakai-site-picker
              .sites=${this._sites}
              @sites-selected=${this._sitesSelected}>
          </sakai-site-picker>
        </div>
        ` : nothing }
        <div id="sorting">
          <select aria-label="${this._i18n.announcement_sort_label}" @change=${this._sortChanged}>
            <option value="${EARLIEST_FIRST}">${this._i18n.earliest_first}</option>
            <option value="${LATEST_FIRST}">${this._i18n.latest_first}</option>
            <option value="${TITLE_A_TO_Z}">${this._i18n.title_a_to_z}</option>
            <option value="${TITLE_Z_TO_A}">${this._i18n.title_z_to_a}</option>
            <option value="${SITE_A_TO_Z}">${this._i18n.site_a_to_z}</option>
            <option value="${SITE_Z_TO_A}">${this._i18n.site_z_to_a}</option>
          </select>
        </div>
      </div>
      <div id="viewing">${this._i18n.viewing}</div>
      <div class="announcements ${!this.siteId || this.siteId === "home" ? "home" : "course"}">
        <div class="header">
          <a href="javascript:;"
              title="${this._i18n.sort_by_title_tooltip}"
              aria-label="${this._i18n.sort_by_title_tooltip}"
              @click=${this.sortByTitle}>
            ${this._i18n.title}
          </a>
        </div>
        ${!this.siteId || this.siteId === "home" ? html`
          <div class="header">
            <a href="javascript:;"
                title="${this._i18n.sort_by_site_tooltip}"
                aria-label="${this._i18n.sort_by_site_tooltip}"
                @click=${this.sortBySite}>
              ${this._i18n.site}
            </a>
          </div>
        ` : nothing}
        <div class="header">${this._i18n.view}</div>
        ${this.dataPage.filter(a => a.visible).map(a => html`
          <div class="row">
            <div class="title cell">
              ${a.highlighted ? html`
              <sakai-icon type="favourite" size="small"></sakai-icon>
              ` : nothing}
              <span class="${ifDefined(a.highlighted ? "highlighted" : undefined)}">${a.subject}</span>
            </div>
            ${!this.siteId || this.siteId === "home" ? html`
              <div class="site cell">${a.siteTitle}</div>
            ` : nothing}
            <div class="url cell">
              <a href="${a.url}"
                  title="${this._i18n.url_tooltip}"
                  aria-label="${this._i18n.url_tooltip}">
                <sakai-icon type="right" size="small"></sakai-icon>
              </a>
            </div>
          </div>
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
      margin-bottom: 12px;
      }
      #filter {
        flex: 1;
      }
      #filter-and-sort-block {
        display: flex;
        justify-content: flex-start;
        margin-bottom: 6px;
        gap: 5px;
      }
      #viewing {
        margin-bottom: 20px;
        font-size: var(--sakai-grades-title-font-size, 11px);
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
          font-weight: 600;
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
          background-color: var(--sakai-table-even-blue-color);
        }
        .row {
          display: contents; /* keep layout flow intact */
        }

        .announcements .row > .cell:first-child {
          border-top-left-radius: 8px;
          border-bottom-left-radius: 8px;
        }

        .announcements .row > .cell:last-child {
          border-top-right-radius: 8px;
          border-bottom-right-radius: 8px;
        }

        .announcements .row > .cell {
          background-color: var(--sakai-table-even-blue-color);
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          margin-bottom: 10px;
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
