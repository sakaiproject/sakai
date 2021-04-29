import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import {SakaiRubricReadonly} from "./sakai-rubric-readonly.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

export class SakaiRubricsSharedList extends RubricsElement {

  static get properties() {

    return {
      token: { type: String },
      rubrics: { type: Array },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("token" === name) {
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
          <sakai-rubric-readonly token="${this.token}" rubric="${JSON.stringify(r)}" @copy-to-site="${this.copyToSite}"></sakai-rubric-readonly>
        </div>
      `)}
      </div>
    `;
  }

  refresh() {
    this.getSharedRubrics(this.token);
  }

  getSharedRubrics(token) {

    var params = {"projection": "inlineRubric"};

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubrics/search/shared-only", token, { params })
      .then(data => this.rubrics = data._embedded.rubrics );
  }

  copyToSite(e) {

    var options = { extraHeaders: { "x-copy-source": e.detail, "lang": this.locale  } };
    SakaiRubricsHelpers.post("/rubrics-service/rest/rubrics/", this.token, options)
      .then(data => this.dispatchEvent(new CustomEvent("copy-share-site")));
  }
}

customElements.define("sakai-rubrics-shared-list", SakaiRubricsSharedList);
