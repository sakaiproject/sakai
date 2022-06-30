import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { repeat } from "/webcomponents/assets/lit-html/directives/repeat.js";
import "./sakai-rubric.js";
import { SharingChangeEvent } from "./sharing-change-event.js";

const rubricName = 'name';
const rubricTitle = 'title';
const rubricCreator = 'creator';
const rubricModified = 'modified';

export class SakaiRubricsList extends RubricsElement {

  constructor() {

    super();

    this.enablePdfExport = false;
  }

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      rubrics: { attribute: false, type: Array },
      enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
    };
  }

  set siteId(value) {

    this._siteId = value;
    this.getRubrics();
  }

  get siteId() { return this._siteId; }

  shouldUpdate() {
    return this.rubrics;
  }

  render() {

    return html`
      <div role="presentation">
        <div role="tablist">
        ${repeat(this.rubrics, r => r.id, r => html`
          <div class="rubric-item" id="rubric_item_${r.id}">
            <sakai-rubric @clone-rubric="${this.cloneRubric}" site-id="${this.siteId}" @delete-item="${this.deleteRubric}" rubric="${JSON.stringify(r)}" ?enable-pdf-export="${this.enablePdfExport}"></sakai-rubric>
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
    this.getRubrics();
  }

  getRubrics() {

    const url = `/api/sites/${this.siteId}/rubrics`;
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while loading rubrics");
    })
    .then(rubrics => this.rubrics = rubrics)
    .catch (error => console.error(error));
  }

  createRubricResponse(nr) {

    nr.new = true;

    // Make sure criterions are set, otherwise lit-html borks in sakai-rubric-criterion.js
    if (!nr.criterions) {
      nr.criterions = [];
    }

    this.rubrics.push(nr);

    const tmp = this.rubrics;
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

    const tmp = this.rubrics;
    this.rubrics = [];
    this.rubrics = tmp;

    this.dispatchEvent(new SharingChangeEvent());

    this.requestUpdate();
  }

  cloneRubric(e) {

    const url = `/api/sites/${this.siteId}/rubrics/${e.detail.id}/copyToSite`;
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while copying rubric");
    })
    .then(rubric => this.createRubricResponse(rubric))
    .catch (error => console.error(error));
  }

  createNewRubric() {

    const url = `/api/sites/${this.siteId}/rubrics/default`;
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while creating rubric");
    })
    .then(rubric => this.createRubricResponse(rubric))
    .catch (error => console.error(error));
  }

  get createRubricUpdateComplete() {

    return (async () => {
      return this.querySelector(`#rubric_item_${this.rubrics[this.rubrics.length - 1].id} sakai-rubric`).updateComplete;
    })();
  }

  sortRubrics(rubricType, ascending) {

    switch (rubricType) {
      case rubricName:
        this.rubrics.sort((a, b) => ascending ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));
        break;
      case rubricTitle:
        this.rubrics.sort((a, b) => ascending ? a.metadata.siteName.localeCompare(b.metadata.siteName) : b.metadata.siteName.localeCompare(a.metadata.siteName));
        break;
      case rubricCreator:
        this.rubrics.sort((a, b) => ascending ? a.metadata.creatorName.localeCompare(b.metadata.creatorName) : b.metadata.creatorName.localeCompare(a.metadata.creatorName));
        break;
      case rubricModified:
        this.rubrics.sort((a, b) => ascending ? a.metadata.modified.localeCompare(b.metadata.modified) : b.metadata.modified.localeCompare(a.metadata.modified));
        break;
    }
    this.requestUpdate('rubrics');
  }
}

const tagName = "sakai-rubrics-list";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricsList);
