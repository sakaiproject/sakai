import { SakaiRubric } from "./sakai-rubric.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import "./sakai-rubric-criteria-readonly.js";
import "./sakai-rubric-pdf.js";
import { tr } from "./sakai-rubrics-language.js";

export class SakaiRubricReadonly extends SakaiRubric {

  constructor() {

    super();

    this.rubricExpanded = true;
    this.enablePdfExport = false;
  }

  static get properties() {

    return {
      rubric: { type: Object },
      enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
    };
  }

  shouldUpdate() {
    return this.rubric;
  }

  render() {

    return html`
      <div class="rubric-title" @click="${this.toggleRubric}">
        <div>
          <span class="rubric-name" id="rubric_toggle_shared_${this.rubric.id}" aria-expanded="${this.rubricExpanded}" role="tab" title="${tr("toggle_details")} ${this.rubric.title}" tabindex="0" >
            <span class="fa fa-chevron-right"></span>
            ${this.rubric.title}
          </span>
          ${this.rubric.draft ? html`
            <span
              tabindex="0"
              role="tooltip"
              title="${tr("draft_info")}"
              aria-label="${tr("draft_info")}"
              class="highlight bold icon-spacer"
            >
              ${tr("draft_label")}
            </span>`
            : ""
          }
        </div>

        <div class="d-none d-sm-block rubric-site-title">${this.rubric.siteTitle}</div>
        <div class="d-none d-sm-block rubric-creator-name">${this.rubric.creatorDisplayName}</div>
        <div class="d-none d-sm-block">${this.rubric.formattedModifiedDate}</div>

        <div class="actions">
          <div class="action-container">
            <span class="d-none d-sm-none d-md-block visually-hidden"><sr-lang key="copy" /></span>
            <span role="button" title="${tr("copy_to_site", [this.rubric.title])}" tabindex="0" class="clone fa fa-copy" @click="${this.copyToSite}"></span>
          </div>
          ${this.enablePdfExport ? html`
            <div class="action-container">
              <sakai-rubric-pdf
                  site-id="${this.rubric.ownerId}"
                  rubric-title="${this.rubric.title}"
                  rubric-id="${this.rubric.id}">
              </sakai-rubric-pdf>
            </div>
          ` : ""}
        </div>
      </div>

      <div class="collapse-details" role="tabpanel" aria-labelledby="rubric_toggle_${this.rubric.id}" id="collapse_shared_${this.rubric.id}">
        <div class="rubric-details style-scope sakai-rubric">
          <sakai-rubric-criteria-readonly .criteria="${this.rubric.criteria}" .weighted=${this.rubric.weighted}></sakai-rubric-criteria-readonly>
        </div>
      </div>
    `;
  }

  copyToSite(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent('copy-to-site', { detail: this.rubric.id }));
  }
}

customElements.define("sakai-rubric-readonly", SakaiRubricReadonly);
