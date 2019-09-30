import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricCriteriaReadonly} from "./sakai-rubric-criteria-readonly.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricReadonly extends RubricsElement {

  constructor() {

    super();

    this.rubricExpanded = true;
  }

  static get properties() {

    return {
      rubric: { type: Object },
    };
  }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubric");
  }

  render() {

    return html`
      <div class="rubric-title" @click="${this.toggleRubric}">
        <div>
          <span class="rubric-name" id="rubric_toggle_shared_${this.rubric.id}" aria-expanded="${this.rubricExpanded}" role="tab" title="${tr("toggle_details")} ${this.rubric.title}" tabindex="0" >
            <span class="fa fa-chevron-right"></span>
            ${this.rubric.title}
          </span>
        </div>

        <div class="hidden-xs"><sakai-rubric-site-title site-id="${this.rubric.metadata.ownerId}"></sakai-rubric-site-title></div>
        <div class="hidden-xs"><sakai-rubric-creator-name creator-id="${this.rubric.metadata.creatorId}"></sakai-rubric-creator-name></div>
        <div class="hidden-xs"><sakai-rubric-modified-date modified="${this.rubric.metadata.modified}"></sakai-rubric-modified-date></div>

        <div class="actions">
          <div class="action-container">
            <span class="hidden-sm hidden-xs sr-only"><sr-lang key="copy" /></span>
            <span role="button" title="${tr("copy_to_site", [this.rubric.title])}" tabindex="0" class="clone fa fa-copy" @click="${this.copyToSite}"></span>
          </div>
        </div>
      </div>

      <div class="collapse-details" role="tabpanel" aria-labelledby="rubric_toggle_${this.rubric.id}" id="collapse_shared_${this.rubric.id}">
        <div class="rubric-details style-scope sakai-rubric">
          <sakai-rubric-criteria-readonly criteria="${JSON.stringify(this.rubric.criterions)}"></sakai-rubric-criteria-readonly>
        </div>
      </div>
    `;
  }

  toggleRubric(e) {

    var titlecontainer = this.querySelector(".rubric-title");

    var collapse = $(`#collapse_shared_${this.rubric.id}`);
    collapse.toggle();

    var icon = $(`#rubric_toggle_shared_${this.rubric.id} span`);

    if (collapse.is(":visible")) {
      this.rubricExpanded = "true";
      titlecontainer.classList.add("active");
      icon.removeClass("fa-chevron-right").addClass("fa-chevron-down");
    } else {
      this.rubricExpanded = "false";
      titlecontainer.classList.remove("active");
      icon.removeClass("fa-chevron-down").addClass("fa-chevron-right");
    }
  }

  cloneRubric(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent('clone-rubric', { detail: this.rubric }));
  }

  copyToSite(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent('copy-to-site', { detail: this.rubric.id }));
  }
}

customElements.define("sakai-rubric-readonly", SakaiRubricReadonly);
