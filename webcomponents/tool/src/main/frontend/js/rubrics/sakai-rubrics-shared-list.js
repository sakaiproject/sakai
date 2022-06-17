import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import "./sakai-rubric-readonly.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

const rubricName = 'name';
const rubricTitle = 'title';
const rubricCreator = 'creator';
const rubricModified = 'modified';

export class SakaiRubricsSharedList extends RubricsElement {

  constructor() {
    super();
    this.enablePdfExport = false;
  }

  static get properties() {

    return {
      token: { type: String },
      rubrics: { type: Array },
      enablePdfExport: { type: Boolean }
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "token") {
      this.getSharedRubrics(newValue);
    }
  }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubrics");
  }

  render() {

    return html`
      <div role="tablist">
      ${repeat(this.rubrics, r => r.id, r => html`
        <div class="rubric-item" id="rubric_item_${r.id}">
          <sakai-rubric-readonly token="${this.token}" rubric="${JSON.stringify(r)}" @copy-to-site="${this.copyToSite}" ?enablePdfExport="${this.enablePdfExport}"></sakai-rubric-readonly>
        </div>
      `)}
      </div>
    `;
  }

  refresh() {
    this.getSharedRubrics(this.token);
  }

  getSharedRubrics(token) {

    const params = {"projection": "inlineRubric"};

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubrics/search/shared-only", token, { params })
    .then( (data) => {
      this.rubrics = data._embedded.rubrics;

      // To sort the rubrics correctly we need the user and the site names in the arrays, not the ids
      this.rubrics = this.rubrics.map( (rubric) => {
        const metadata = rubric.metadata;
        const creatorId = metadata.creatorId;
        const siteId = metadata.ownerId;
        SakaiRubricsHelpers.getUserDisplayName(sakaiSessionId, creatorId).then( (name) => metadata.creatorName = name);
        SakaiRubricsHelpers.getSiteTitle(sakaiSessionId, siteId).then( (name) => metadata.siteName = name);
        rubric.metadata = metadata;
        return rubric;
      });

    });
  }

  copyToSite(e) {

    const options = { extraHeaders: { "x-copy-source": e.detail, "lang": this.locale  } };
    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", this.token, options)
      .then(() => this.dispatchEvent(new CustomEvent("copy-share-site")));
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

customElements.define("sakai-rubrics-shared-list", SakaiRubricsSharedList);
