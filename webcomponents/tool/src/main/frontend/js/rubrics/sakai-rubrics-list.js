import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import "./sakai-rubric.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

const rubricName = 'name';
const rubricTitle = 'title';
const rubricCreator = 'creator';
const rubricModified = 'modified';

export class SakaiRubricsList extends RubricsElement {

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
        ${repeat(this.rubrics, r => r.id, r => html`
          <div class="rubric-item" id="rubric_item_${r.id}">
            <sakai-rubric @clone-rubric="${this.cloneRubric}" @delete-item="${this.deleteRubric}" token="${this.token}" rubric="${JSON.stringify(r)}"></sakai-rubric>
          </div>
        `)}
        </div>
      </div>
      <br>
      <div class="act">
        <button class="active add-rubric" @click="${this.createNewRubric}">
            <span class="add fa fa-plus"></span>
            <sr-lang key="add_rubric">add_rubric</sr-lang>
        </button>
      </div>
    `;
  }

  refresh() {
    this.getRubrics(this.token);
  }

  getRubrics(token, extraParams = {}) {

    var params = {"projection": "inlineRubric"};
    Object.assign(params, extraParams);

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubrics", token, { params })
      .then(data => {

        this.rubrics = data._embedded.rubrics;

        if (data.page.size <= this.rubrics.length){
          this.getRubrics(token, { "size": this.rubrics.length + 25 });
        }
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
    this.updateComplete.then(async() => {
      await this.createRubricUpdateComplete;
      this.querySelector(`#rubric_item_${nr.id} sakai-rubric`).toggleRubric();
    });
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

    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", this.token, {
      extraHeaders: {"x-copy-source": e.detail.id, "lang": this.locale}
    })
    .then(data => this.createRubricResponse(data));
  }

  createNewRubric() {

    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", this.token, {
      extraHeaders: {"x-copy-source" :"default", "lang": this.locale}
    })
    .then(data => this.createRubricResponse(data));
  }

  get createRubricUpdateComplete() {
    return (async () => {
      return await this.querySelector(`#rubric_item_${this.rubrics[this.rubrics.length - 1].id} sakai-rubric`).updateComplete;
    })();
  }

 sortRubrics(rubricType, ascending) {
    switch (rubricType) {
      case rubricName:
        this.rubrics.sort((a, b) => ascending ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));
        break;
      case rubricTitle:
        this.rubrics.sort((a, b) => ascending ? a.metadata.ownerId.localeCompare(b.metadata.ownerId) : b.metadata.ownerId.localeCompare(a.metadata.ownerId));
        break;
      case rubricCreator:
        this.rubrics.sort((a, b) => ascending ? a.metadata.creatorId.localeCompare(b.metadata.creatorId) : b.metadata.creatorId.localeCompare(a.metadata.creatorId));
        break;
      case rubricModified:
        this.rubrics.sort((a, b) => ascending ? a.metadata.modified.localeCompare(b.metadata.modified) : b.metadata.modified.localeCompare(a.metadata.modified));
        break;
    }
    this.requestUpdate('rubrics');
  }

}

customElements.define("sakai-rubrics-list", SakaiRubricsList);
