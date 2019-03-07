import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricCriteriaReadonly} from "./sakai-rubric-criteria-readonly.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricReadonly extends SakaiElement {

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
            <span class="fa fa-chevron-down"></span>
            ${this.rubric.title}
          </span>
        </div>

        <div class="hidden-xs"><site-title siteId="${this.rubric.metadata.ownerId}"></site-title></div>
        <div class="hidden-xs"><creator-name creatorId="${this.rubric.metadata.creatorId}"></creator-name></div>
        <div class="hidden-xs"><modified-date modified="${this.rubric.metadata.modified}"></modified-date></div>

        <div class="actions">
          <div class="action-container">
            <span class="hidden-sm hidden-xs sr-only"><sr-lang key="clone_label">clone_label</sr-lang> </span>
            <span role="button" title="${tr("clone")} ${this.rubric.title}" tabindex="0" class="clone fa fa-copy" on-tap="cloneRubric"></span>
          </div>
        </div>
      </div>

      <div role="tabpanel" aria-labelledby="rubric_toggle_${this.rubric.id}" id="collapse_shared_${this.rubric.id}">
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
    this.dispatchEvent(new CustomEvent('copy-to-site', { detail: this.rubric }));
  }
}

customElements.define("sakai-rubric-readonly", SakaiRubricReadonly);
