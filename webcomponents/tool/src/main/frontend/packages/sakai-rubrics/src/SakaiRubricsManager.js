import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "../sakai-rubrics-list.js";
import "../sakai-rubrics-shared-list.js";

export class SakaiRubricsManager extends RubricsElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
    isSuperUser: { attribute: "is-super-user", type: Boolean },
  };

  constructor() {

    super();

    this.siteRubricsExpanded = "true";
    this.sharedRubricsExpanded = "false";
    this.enablePdfExport = false;
    this.isSuperUser = false;
  }

  firstUpdated() {

    const siteRubricsBlock = this.querySelector("#site_rubrics");

    siteRubricsBlock.addEventListener("show.bs.collapse", () => {
      this.querySelector("#site-rubrics-toggle span.fa").classList.replace("fa-chevron-right", "fa-chevron-down");
    });

    siteRubricsBlock.addEventListener("hide.bs.collapse", () => {
      this.querySelector("#site-rubrics-toggle span.fa").classList.replace("fa-chevron-down", "fa-chevron-right");
    });

    const sharedRubricsBlock = this.querySelector("#shared_rubrics");

    sharedRubricsBlock.addEventListener("show.bs.collapse", () => {
      this.querySelector("#shared-rubrics-toggle span.fa").classList.replace("fa-chevron-right", "fa-chevron-down");
    });

    sharedRubricsBlock.addEventListener("hide.bs.collapse", () => {
      this.querySelector("#shared-rubrics-toggle span.fa").classList.replace("fa-chevron-down", "fa-chevron-right");
    });
  }

  render() {

    return html`

      <h1>${this.tr("manage_rubrics")}</h1>

      <div class="sak-banner-info">${this.tr("locked_message")}</div>

      <div id="rubrics-reorder-info" class="sak-banner-info">${unsafeHTML(this.tr("drag_to_reorder_info"))}</div>

      <div class="row">
        <div class="col-md-4 form-group">
          <label class="label-rubrics" for="rubrics-search-bar">${this.tr("search_rubrics")}</label>
          <input type="text" id="rubrics-search-bar" name="rubrics-search-bar" class="form-control" @keyup=${this.filterRubrics}>
        </div>
      </div>

      <div role="tablist">
        <div class="d-flex align-items-center">
          <button class="btn btn-icon btn-xs me-1"
              id="site-rubrics-toggle"
              data-bs-toggle="collapse"
              data-bs-target="#site_rubrics"
              aria-expanded="true"
              title="${this.tr("toggle_site_rubrics")}"
              aria-label="${this.tr("toggle_site_rubrics")}"
              aria-controls="site_rubrics">
            <span class="collpase-icon fa fa-chevron-down" aria-hidden="true"></span>
          </button>
          <span class="fw-bold">${this.tr("site_rubrics")}</span>
        </div>

        <div class="collapse show" id="site_rubrics">
          <div class="rubric-title-sorting p-3 rounded-top">
            <div>
              <button class="btn p-0"
                  @click=${this.sortRubrics}
                  data-key="site-name">
                ${this.tr("site_name")}
                <span class="collpase-icon fa fa-chevron-up site-name sort-element-site" aria-hidden="true"></span>
              </button>
            </div>
            <div>
              <button class="btn p-0"
                  @click=${this.sortRubrics}
                  data-key="site-title">
                ${this.tr("site_title")}
                <span class="collpase-icon fa fa-chevron-up site-title sort-element-site" aria-hidden="true"></span>
              </button>
            </div>
            <div>
              <button class="btn p-0"
                  @click=${this.sortRubrics}
                  data-key="site-creator">
                ${this.tr("creator_name")}
                <span class="collpase-icon fa fa-chevron-up site-creator sort-element-site" aria-hidden="true"></span>
              </button>
            </div>
            <div>
              <button class="btn p-0"
                  @click=${this.sortRubrics}
                  data-key="site-modified">
                ${this.tr("modified")}
                <span class="collpase-icon fa fa-chevron-up site-modified sort-element-site" aria-hidden="true"></span>
              </button>
            </div>
            <div class="actions">${this.tr("actions")}</div>
          </div>
          <sakai-rubrics-list site-id="${this.siteId}"
              @sharing-change=${this.handleSharingChange}
              @copy-share-site=${this.handleRubricList}
              ?enable-pdf-export=${this.enablePdfExport}>
          </sakai-rubrics-list>
        </div>

        <hr>
        <h3>${this.tr("public_rubrics_title")}</h3>
        <p>${this.tr("public_rubrics_info")}</p>

        <div class="d-flex align-items-center">
          <button class="btn btn-icon btn-xs me-1"
              id="shared-rubrics-toggle"
              data-bs-toggle="collapse"
              data-bs-target="#shared_rubrics"
              aria-expanded="false"
              title="${this.tr("toggle_shared_rubrics")}"
              aria-label="${this.tr("toggle_shared_rubrics")}"
              aria-controls="shared_rubrics">
            <span class="collpase-icon fa fa-chevron-right" aria-hidden="true"></span>
          </button>
          <span class="fw-bold">${this.tr("shared_rubrics")}</span>
        </div>

        <div class="collapse" id="shared_rubrics">
          <div id="sharedlist">
            <div class="rubric-title-sorting p-3 rounded-top">
              <div>
                <button class="btn p-0"
                    @click=${this.sortRubrics}
                    data-key="shared-name">
                  ${this.tr("site_name")}
                  <span class="collpase-icon fa fa-chevron-up shared-name sort-element-shared" aria-hidden="true"></span>
                </button>
              </div>
              <div>
                <button class="btn p-0"
                    @click=${this.sortRubrics}
                    data-key="shared-title">
                  ${this.tr("site_title")}
                  <span class="collpase-icon fa shared-title sort-element-shared" aria-hidden="true"></span>
                </button>
              </div>
              <div>
                <button class="btn p-0"
                    @click=${this.sortRubrics}
                    data-key="shared-creator">
                  ${this.tr("creator_name")}
                  <span class="collpase-icon fa shared-creator sort-element-shared" aria-hidden="true"></span>
                </button>
              </div>
              <div>
                <button class="btn p-0"
                    @click=${this.sortRubrics}
                    data-key="shared-modified">
                  ${this.tr("modified")}
                  <span class="collpase-icon fa shared-modified sort-element-shared" aria-hidden="true"></span>
                </button>
              </div>
              <div class="actions">${this.tr("actions")}</div>
            </div>
            <sakai-rubrics-shared-list site-id="${this.siteId}"
                @copy-share-site=${this.handleRubricList}
                @update-rubric-list=${this.handleRubricList}
                ?enable-pdf-export=${this.enablePdfExport}
                ?is-super-user=${this.isSuperUser}>
            </sakai-rubrics-shared-list>
          </div>
        </div>
      </div>
    `;
  }

  handleSharingChange() {

    this.querySelector("sakai-rubrics-shared-list").refresh();
  }

  handleRubricList() {

    this.querySelector("sakai-rubrics-list").refresh();
  }

  filterRubrics(e) {

    this.querySelectorAll("sakai-rubrics-list, sakai-rubrics-shared-list").forEach(rubricList => {
      rubricList.search(e.target.value.toLowerCase());
    });
  }

  sortRubrics(event) {

    const sortInput = event.currentTarget.dataset.key;

    if (!sortInput) {
      return;
    }

    const [ rubricClass, rubricType ] = sortInput.split("-");

    const arrowUpIcon = "fa-chevron-up";
    const arrowDownIcon = "fa-chevron-down";
    const selector = `.sort-element-${rubricClass}`;
    let ascending = event.currentTarget.querySelector(selector).classList.contains(arrowUpIcon);
    this.querySelectorAll(selector).forEach(item => {

      item.classList.remove(arrowDownIcon);
      item.classList.remove(arrowUpIcon);
    });
    event.currentTarget.querySelector(selector).classList.add(ascending ? arrowDownIcon : arrowUpIcon);
    ascending = !ascending;

    const list = this.querySelector(rubricClass === "site" ? "sakai-rubrics-list" : "sakai-rubrics-shared-list");
    list.sortRubrics(rubricType, ascending);
  }
}
