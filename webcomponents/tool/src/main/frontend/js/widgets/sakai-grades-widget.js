import { css, html } from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";
import "../sakai-icon.js";
import { SakaiDashboardWidget } from "./sakai-dashboard-widget.js";
import "../grades/sakai-grades.js";
import "../sakai-pager.js";

export class SakaiGradesWidget extends SakaiDashboardWidget {

  constructor() {

    super();
    this.widgetId = "grades";
    this.loadTranslations("grades");
  }

  content() {

    return html`

      <sakai-grades
        user-id="${ifDefined(this.userId ? this.userId : undefined)}"
        site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
      >
    `;
  }

  static get styles() {

    return [
      ...super.styles,
      css`

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
            color: var(--sakai-grades-count-color, --sakai-text-color-1);
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
}

if (!customElements.get("sakai-grades-widget")) {
  customElements.define("sakai-grades-widget", SakaiGradesWidget);
}

SakaiGradesWidget.roles = ["instructor"];
