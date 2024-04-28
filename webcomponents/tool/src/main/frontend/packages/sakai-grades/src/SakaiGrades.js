import { css, html } from "lit";
import "@sakai-ui/sakai-icon";
import { SakaiPageableElement } from "@sakai-ui/sakai-pageable-element";
import { ASSIGNMENT_A_TO_Z, ASSIGNMENT_Z_TO_A, COURSE_A_TO_Z
  , COURSE_Z_TO_A, NEW_HIGH_TO_LOW, NEW_LOW_TO_HIGH
  , AVG_LOW_TO_HIGH, AVG_HIGH_TO_LOW } from "./sakai-grades-constants.js";

export class SakaiGrades extends SakaiPageableElement {

  static properties = { _i18n: { state: true } };

  constructor() {

    super();

    this.showPager = true;
    this.loadTranslations("grades").then(r => this._i18n = r);
  }

  async loadAllData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/grades`
      : "/api/users/me/grades";
    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Failed to get grades from ${url}`);
      })
      .then(data => {

        this.data = data;
        this.sortChanged({ target: { value: NEW_LOW_TO_HIGH } });
      })
      .catch (error => console.error(error));
  }

  sortChanged(e) {

    switch (e.target.value) {
      case ASSIGNMENT_A_TO_Z:
        this.data.sort((g1, g2) => g1.name.localeCompare(g2.name));
        break;
      case ASSIGNMENT_Z_TO_A:
        this.data.sort((g1, g2) => g2.name.localeCompare(g1.name));
        break;
      case COURSE_A_TO_Z:
        this.data.sort((g1, g2) => g1.siteTitle.localeCompare(g2.siteTitle));
        break;
      case COURSE_Z_TO_A:
        this.data.sort((g1, g2) => g2.siteTitle.localeCompare(g1.siteTitle));
        break;
      case NEW_HIGH_TO_LOW:
        this.data.sort((g1, g2) => g2.ungraded - g1.ungraded);
        break;
      case NEW_LOW_TO_HIGH:
        this.data.sort((g1, g2) => g1.ungraded - g2.ungraded);
        break;
      case AVG_LOW_TO_HIGH:
        this.data.sort((g1, g2) => {
          if (g1.noneGradedYet) return 1;
          else if (g2.noneGradedYet) return -1;
          return g1.averageScore - g2.averageScore;
        });
        break;
      case AVG_HIGH_TO_LOW:
        this.data.sort((g1, g2) => g2.averageScore - g1.averageScore);
        break;
      default:
        break;
    }

    this.repage();
  }

  shouldUpdate(changedProperties) {
    return this._i18n && super.shouldUpdate(changedProperties);
  }

  content() {

    return html`
      <div id="topbar">
        <div id="filter">
          <select @change=${this.sortChanged}
              title="${this._i18n.sort_tooltip}"
              aria-label="${this._i18n.sort_tooltip}">
            <option value="${NEW_LOW_TO_HIGH}">${this._i18n.sort_new_low_to_high}</option>
            <option value="${NEW_HIGH_TO_LOW}">${this._i18n.sort_new_high_to_low}</option>
            <option value="${AVG_LOW_TO_HIGH}">${this._i18n.sort_average_low_to_high}</option>
            <option value="${AVG_HIGH_TO_LOW}">${this._i18n.sort_average_high_to_low}</option>
            <option value="${ASSIGNMENT_A_TO_Z}">${this._i18n.sort_assignment_a_to_z}</option>
            <option value="${ASSIGNMENT_Z_TO_A}">${this._i18n.sort_assignment_z_to_a}</option>
            ${this.siteId ? "" : html`
            <option value="${COURSE_A_TO_Z}">${this._i18n.sort_course_a_to_z}</option>
            <option value="${COURSE_Z_TO_A}">${this._i18n.sort_course_z_to_a}</option>
            `}
          </select>
        </div>
      </div>

      <div id="grades">
        <div class="header">${this._i18n.course_assignment}</div>
        <div class="header">${this._i18n.course_average}</div>
        <div class="header">${this._i18n.view}</div>
        ${this.dataPage.map((a, i) => html`
        <div class="assignment cell ${i % 2 === 0 ? "even" : "odd"}">
          <div class="new-count">${a.ungraded} ${this._i18n.new_submissions}</div>
          ${this.siteId ? html`
          <div class="title">${a.name}</div>
          ` : html`
          <div class="course title">${a.siteTitle} / ${a.name}</div>
          `}
        </div>
        <div class="average cell ${i % 2 === 0 ? "even" : "odd"}">${a.noneGradedYet ? "-" : a.averageScore.toFixed(2)}</div>
        <div class="next cell ${i % 2 === 0 ? "even" : "odd"}">
          <a href="${a.url}"
              aria-label="${this._i18n.url_tooltip}"
              title="${this._i18n.url_tooltip}">
            <sakai-icon type="right" size="small">
              aria-label="${this._i18n.url_tooltip}"
              title="${this._i18n.url_tooltip}">
            </sakai-icon>
          </a>
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
      #total {
        flex: 1;
        margin-bottom: 10px;
        font-style: italic;
        font-weight: bold;
        font-size: var(--sakai-grades-title-font-size, 12px);
        margin-left: 18px;
      }
      #filter {
        flex: 1;
        text-align: right;
      }

      #grades {
        display:grid;
        grid-template-columns: 4fr 2fr 0fr;
        grid-auto-rows: minmax(10px, auto);
      }

        #grades > div:nth-child(-n+3) {
          padding-bottom: 14px;
        }
        .header {
          font-weight: bold;
          padding: 0 5px 0 5px;
        }
        .assignment {
          padding: 8px;
        }
        .cell {
          padding: 8px;
          font-size: var(--sakai-grades-title-font-size, 12px);
        }
          .new-count {
            font-size: var(--sakai-grades-count-font-size, 10px);
            font-weight: bold;
            color: var(--sakai-text-color-dimmed, #262626);
          }
          .title {
            font-size: var(--sakai-grades-title-font-size, 12px);
          }
        .average {
          display: flex;
          align-items: center;
          font-size: 16px;
          font-weight: bold;
        }
        .even {
          background-color: var(--sakai-table-even-color);
        }
        .next {
          display: flex;
          text-align: right;
          align-items: center;
        }
    `,
  ];
}

SakaiGrades.roles = [ "instructor" ];
