import { css, html } from "../assets/lit-element/lit-element.js";
import '../sakai-icon.js';
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';

export class SakaiGradesWidget extends SakaiDashboardWidget {

  constructor() {

    super();
    this.widgetId = "grades";
    this.loadTranslations("gradebookng");
  }

  loadData() {

    let url = this.siteId ? `/api/sites/${this.siteId}/grades`
      : `/api/users/${this.userId}/grades`;
    fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        } else {
          throw new Error(`Failed to get grades from ${url}`);
        }
      })
      .then(grades => this.data = grades)
      .catch (error => console.error(error));
  }


  sortChanged(e) {

    switch (e.target.value) {
      case "assignment_a_to_z":
        this.data.sort((a1, a2) => a1.name.localeCompare(a2.name));
        break;
      case "assignment_z_to_a":
        this.data.sort((a1, a2) => a2.name.localeCompare(a1.name));
        break;
      case "course_a_to_z":
        this.data.sort((a1, a2) => a1.siteTitle.localeCompare(a2.siteTitle));
        break;
      case "course_z_to_a":
        this.data.sort((a1, a2) => a2.siteTitle.localeCompare(a1.siteTitle));
        break;
      case "new_high_to_low":
        this.data.sort((a1, a2) => a2.ungraded - a1.ungraded);
        break;
      case "new_low_to_high":
        this.data.sort((a1, a2) => a1.ungraded - a2.ungraded);
        break;
      case "average_low_to_high":
        this.data.sort((a1, a2) => a1.averageScore - a2.averageScore);
        break;
      case "average_high_to_low":
        this.data.sort((a1, a2) => a2.averageScore - a1.averageScore);
        break;
      default:
        break;
    }
    this.requestUpdate();
  }

  shouldUpdate(changed) {
    return super.shouldUpdate(changed) && this.data;
  }

  content() {

    return html`
      <div id="topbar">
        <div id="filter">
          <select @change=${this.sortChanged}>
            <option value="new_low_to_high">${this.i18n["sort_new_low_to_high"]}</option>
            <option value="new_high_to_low">${this.i18n["sort_new_high_to_low"]}</option>
            <option value="average_low_to_high">${this.i18n["sort_average_low_to_high"]}</option>
            <option value="average_high_to_low">${this.i18n["sort_average_high_to_low"]}</option>
            <option value="assignment_a_to_z">${this.i18n["sort_assignment_a_to_z"]}</option>
            <option value="assignment_z_to_a">${this.i18n["sort_assignment_z_to_a"]}</option>
            <option value="course_a_to_z">${this.i18n["sort_course_a_to_z"]}</option>
            <option value="course_z_to_a">${this.i18n["sort_course_z_to_a"]}</option>
          </select>
        </div>
      </div>

      <div id="grades">
        <div class="header">Course/Assignment</div>
        <div class="header">${this.i18n["course_average"]}</div>
        <div class="header">${this.i18n["view"]}</div>
        ${this.data.map((a, i) => html`
        <div class="assignment cell ${i % 2 === 0 ? "even" : "odd"}">
          <div class="new-count">${a.ungraded} ${this.i18n["new_submissions"]}</div>
          <div class="title">${a.siteTitle} / ${a.name}</div>
        </div>
        <div class="average cell ${i % 2 === 0 ? "even" : "odd"}">${a.averageScore.toFixed(2)}</div>
        <div class="next cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${a.url}"><sakai-icon type="right" size="small"></a></div>
        `)}
      </div>
    `;
  }

  static get styles() {

    return css`
      ${SakaiDashboardWidget.styles}
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
            padding: 0 5px 0 5px;
            color: var(--sakai-grades-count-color, --sakai-text-color-1);
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
    `;
  }
}

if (!customElements.get("sakai-grades-widget")) {
  customElements.define("sakai-grades-widget", SakaiGradesWidget);
}

SakaiGradesWidget.roles = ["instructor"];
