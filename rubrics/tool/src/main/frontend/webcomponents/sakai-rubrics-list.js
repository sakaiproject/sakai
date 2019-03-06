import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubric} from "./sakai-rubric.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

export class SakaiRubricsList extends SakaiElement {

  static get properties() {

    return {
      token: { type: String },
      rubrics: { type: Array },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("token" === name) {
      this.getRubrics(newValue);
    }
  }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubrics");
  }

  render() {

    return html`
      <div role="presentation">
        <div role="tablist">
        ${this.rubrics.map(r => html`
          <div class="rubric-item" id="rubric_item_${r.id}">
            <sakai-rubric @clone-rubric="${this.cloneRubric}" @delete-item="${this.deleteRubric}" token="${this.token}" rubric="${JSON.stringify(r)}"></sakai-rubric>
          </div>
        `)}
        </div>
      </div>
      <br>
      <button class="btn-primary add-rubric" @click="${this.createNewRubric}">
        <span class="add fa fa-plus"></span>
        <sr-lang key="add_rubric">add_rubric</sr-lang>
      </button>
    `;
  }

  getRubrics(token) {

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubrics?projection=inlineRubric", this.token)
      .then(data => {
        this.rubrics = data._embedded.rubrics;
      });
  }

  createRubricResponse(nr) {

    nr.new = true;

    // Make sure criterions are set, otherwise lit-html borks in sakai-rubric-criterion.js
    if (!nr.criterions) {
      nr.criterions = [];
    }

    this.rubrics.push(nr);

    var tmp = this.rubrics;
    this.rubrics = [];
    this.rubrics = tmp;

    this.requestUpdate();
  }

  deleteRubric(e) {

    e.stopPropagation();
    this.rubrics.splice(this.rubrics.map(r => r.id).indexOf(e.detail.id), 1);

    var tmp = this.rubrics;
    this.rubrics = [];
    this.rubrics = tmp;

    this.dispatchEvent(new SharingChangeEvent());

    this.requestUpdate();
  }

  cloneRubric(e) {

    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", {
      token: this.token,
      extraHeaders: {"x-copy-source": e.detail.id, "lang": portal.locale}
    })
    .then(data => this.createRubricResponse(data));
  }

  createNewRubric() {

    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", {
      token: this.token,
      extraHeaders: {"x-copy-source" :"default", "lang": portal.locale}
    })
    .then(data => this.createRubricResponse(data));
  }
}

customElements.define("sakai-rubrics-list", SakaiRubricsList);
