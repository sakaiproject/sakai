import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { repeat } from "lit-html/directives/repeat.js";
import "../sakai-rubric.js";
import { SharingChangeEvent } from "./SharingChangeEvent.js";

const rubricName = "name";
const rubricTitle = "title";
const rubricCreator = "creator";
const rubricModified = "modified";

export class SakaiRubricsList extends RubricsElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },

    _rubrics: { state: true },
  };

  set siteId(value) {

    this._siteId = value;
    this.getRubrics();
  }

  get siteId() { return this._siteId; }

  search(search) {

    this.querySelectorAll("sakai-rubric, sakai-rubric-readonly").forEach(rubric => {

      rubric.classList.remove("d-none");
      rubric.classList.toggle("d-none", !rubric.matches(search));
    });
  }

  render() {

    if (!this._rubrics) {
      return html`
        <div class="sak-banner-warn">${this._i18n.loading}</div>
      `;
    }

    return html`
      <div role="presentation">
        <div role="tablist">
        ${repeat(this._rubrics, r => r.id, r => html`
          <sakai-rubric site-id="${this.siteId}"
              @clone-rubric=${this.cloneRubric}
              @delete-item=${this._rubricDeleted}
              .rubric=${r}
              ?enable-pdf-export=${this.enablePdfExport}>
          </sakai-rubric>
        `)}
        </div>
      </div>
      <br>
      <div class="act">
        <button type="button" class="active add-rubric" @click=${this.createNewRubric}>
          <span class="add fa fa-plus"></span>
          ${this._i18n.add_rubric}
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
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Network error while loading rubrics at ${url}`);
    })
    .then(rubrics => this._rubrics = rubrics)
    .catch (error => console.error(error));
  }

  createRubricResponse(nr) {

    nr.new = true;
    nr.expanded = true;

    // Make sure criterions are set, otherwise lit-html borks in sakai-rubric-criterion.js
    if (!nr.criterions) {
      nr.criterions = [];
    }

    this._rubrics.push(nr);

    this.requestUpdate();
  }

  _rubricDeleted(e) {

    e.stopPropagation();
    this._rubrics.splice(this._rubrics.map(r => r.id).indexOf(e.detail.id), 1);

    const tmp = this._rubrics;
    this._rubrics = [];
    this._rubrics = tmp;

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
      throw new Error(`Network error while creating rubric at ${url}`);
    })
    .then(rubric => this.createRubricResponse(rubric))
    .catch (error => console.error(error));
  }

  sortRubrics(rubricType, ascending) {

    switch (rubricType) {
      case rubricName:
        this._rubrics.sort((a, b) => ascending ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));
        break;
      case rubricTitle:
        this._rubrics.sort((a, b) => ascending ? a.siteTitle.localeCompare(b.siteTitle) : b.siteTitle.localeCompare(a.siteTitle));
        break;
      case rubricCreator:
        this._rubrics.sort((a, b) => ascending ? a.creatorDisplayName.localeCompare(b.creatorDisplayName) : b.creatorDisplayName.localeCompare(a.creatorDisplayName));
        break;
      case rubricModified:
        this._rubrics.sort((a, b) => ascending ? a.modified - b.modified : b.modified - a.modified);
        break;
    }
    this.requestUpdate("_rubrics");
  }
}
