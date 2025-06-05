import { css, html, nothing } from "lit";
import "@sakai-ui/sakai-icon";
import { SakaiPageableElement } from "@sakai-ui/sakai-pageable-element";
import { SakaiSitePicker } from "@sakai-ui/sakai-site-picker";
import "@sakai-ui/sakai-site-picker/sakai-site-picker.js";
import { ASSIGNMENT_A_TO_Z, ASSIGNMENT_Z_TO_A, COURSE_A_TO_Z
  , COURSE_Z_TO_A, UNGRADED_MOST_TO_LEAST, UNGRADED_LEAST_TO_MOST
  , SCORE_LOW_TO_HIGH, SCORE_HIGH_TO_LOW } from "./sakai-grades-constants.js";

export class SakaiGrades extends SakaiPageableElement {

  static properties = {
    secret: { type: Boolean },
    _canGradeAny: { state: true },
  };

  constructor() {

    super();

    this.showPager = true;
    this.loadTranslations("grades");
  }

  async loadAllData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/grades` : "/api/users/me/grades";
    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Failed to get grades from ${url}`);
      })
      .then(data => {

        this.data = data.grades;
        !this.siteId && (this.sites = data.sites);
        this._allData = data.grades;
        this.sortChanged({ target: { value: UNGRADED_LEAST_TO_MOST } });
        this._canGradeAny = this.data.some(g => g.canGrade);
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
      case UNGRADED_MOST_TO_LEAST:
        this.data.sort((g1, g2) => g2.ungraded - g1.ungraded);
        break;
      case UNGRADED_LEAST_TO_MOST:
        this.data.sort((g1, g2) => g1.ungraded - g2.ungraded);
        break;
      case SCORE_LOW_TO_HIGH:
        this.data.sort((g1, g2) => {
          if (g1.notGradedYet) return 1;
          else if (g2.notGradedYet) return -1;
          return g1.score - g2.score;
        });
        break;
      case SCORE_HIGH_TO_LOW:
        this.data.sort((g1, g2) => g2.score - g1.score);
        break;
      default:
        break;
    }

    this.repage();
  }

  _filter() {

    this.data = [ ... this._allData ];

    if (this._currentFilter === "sites" && this._selectedSites !== SakaiSitePicker.ALL) {
      this.data = [ ...this.data.filter(g => this._selectedSites.includes(g.siteId)) ];
    }

    this.repage();
  }

  _sitesSelected(e) {

    this._selectedSites = e.detail.value;
    this._currentFilter = "sites";
    this._filter();
  }

  firstUpdated() {

    if (this.dataPage.length === 0 || !this.secret) return;

    this.shadowRoot.getElementById("grades").addEventListener("click", () => {
      this.secret = false;
    }, { once: true });
  }

  shouldUpdate(changedProperties) {
    return this._i18n && super.shouldUpdate(changedProperties);
  }

  content() {

    if (this.dataPage.length === 0) {
      return html`<div class="sak-banner-info">${this._i18n.no_grades}</div>`;
    }

    return html`
      ${!this.siteId ? html`
      <div id="site-filter">
        <sakai-site-picker
            .sites=${this.sites}
            @sites-selected=${this._sitesSelected}>
        </sakai-site-picker>
      </div>
      ` : nothing}
      ${this.secret ? html `
      <div class="score-msg">${this._i18n.score_reveal_msg}</div>
      ` : nothing}
      <div id="topbar">
        <div id="filter">
          <select @change=${this.sortChanged}
              title="${this._i18n.sort_tooltip}"
              aria-label="${this._i18n.sort_tooltip}">
            ${this._canGradeAny ? html`
            <option value="${UNGRADED_LEAST_TO_MOST}">${this._i18n.sort_ungraded_least_to_most}</option>
            <option value="${UNGRADED_MOST_TO_LEAST}">${this._i18n.sort_ungraded_most_to_least}</option>
            ` : nothing }
            <option value="${SCORE_LOW_TO_HIGH}">${this._i18n.sort_score_low_to_high}</option>
            <option value="${SCORE_HIGH_TO_LOW}">${this._i18n.sort_score_high_to_low}</option>
            <option value="${ASSIGNMENT_A_TO_Z}">${this._i18n.sort_assignment_a_to_z}</option>
            <option value="${ASSIGNMENT_Z_TO_A}">${this._i18n.sort_assignment_z_to_a}</option>
            ${this.siteId ? nothing : html`
            <option value="${COURSE_A_TO_Z}">${this._i18n.sort_course_a_to_z}</option>
            <option value="${COURSE_Z_TO_A}">${this._i18n.sort_course_z_to_a}</option>
            `}
          </select>
        </div>
      </div>

      <div id="grades" aria-live="polite">
        <div class="header">${this._i18n.course_assignment}</div>
        <div class="header score">${this._i18n.score}</div>
        <div class="header">${this._i18n.view}</div>
        ${this.dataPage.map((a, i) => html`
        <div class="assignment cell ${i % 2 === 0 ? "even" : "odd"}">
          ${a.canGrade ? html`
          <div class="ungraded-count">${a.ungraded} ${this._i18n.new_submissions}</div>
          ` : nothing}
          ${this.siteId ? html`
          <div class="title">${a.name}</div>
          ` : html`
          <div class="course title">${a.siteTitle} / ${a.name}</div>
          `}
        </div>
        <div class="score cell ${i % 2 === 0 ? "even" : "odd"}${this.secret ? " blurred" : ""}" aria-hidden="${this.secret ? "true" : "false"}">
            ${a.notGradedYet ? "-" : a.score} ${!a.notGradedYet && a.canGrade ? html`${this._i18n.course_score}` : nothing}
        </div>
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

      #site-filter {
        margin-bottom: 12px;
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
          display: flex;
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
        .ungraded-count {
          font-size: var(--sakai-grades-count-font-size, 10px);
          font-weight: bold;
          color: var(--sakai-text-color-dimmed, #262626);
        }
        .title {
          font-size: var(--sakai-grades-title-font-size, 12px);
        }
        .score {
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .score-msg {
          text-align: center;
          color: red;
          background-color: var(--sakai-background-color-2);
        }
        .even {
          background-color: var(--sakai-table-even-color);
        }
        .next {
          display: flex;
          justify-content: right;
          align-items: center;
        }
        .blurred {
          filter: blur(3px);
        }
    `,
  ];
}
