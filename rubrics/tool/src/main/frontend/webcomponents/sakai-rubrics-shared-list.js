import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricReadonly} from "./sakai-rubric-readonly.js";

export class SakaiRubricsSharedList extends SakaiElement {

  constructor() {

    super();

    this.rubrics = [];
  }

  static get properties() {

    return {
      token: { type: String },
      rubrics: { type: Array },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("token" === name) {
      this.getSharedRubrics();
    }
  }

  render() {

    return html`
      <div role="tablist">
      ${this.rubrics.map(r => html`
        <div class="rubric-item" id="rubric_item_${r.id}">
          <sakai-rubric-readonly token="${this.token}" rubric="${JSON.stringify(r)}"></sakai-rubric-readonly>
        </div>
      `)}
      </div>
    `;
  }

  refresh() {
    this.getSharedRubrics();
  }

  getSharedRubrics() {

    $.ajax({
      url: "/rubrics-service/rest/rubrics/search/shared-only?projection=inlineRubric",
      headers: {"authorization": this.token}
    })
    .done(data => {

      this.rubrics = data._embedded.rubrics;
      this.requestUpdate();
    })
    .fail((jqXHR, textStatus, error) => { console.log(textStatus); console.log(error); });
  }

  copyShareToSite(e) {

    $.ajax({
      url: "/rubrics-service/rest/rubrics/",
      headers: { "x-copy-source": e.detail.id, "authorization": this.token },
      contentType: "application/json",
      method: "POST",
      data: "{}"
    })
    .done(data => this.dispatchEvent(new CustomEvent('copy-share-site')) )
    .fail((jqXHR, textStatus, error) => { console.log(textStatus); console.log(error); });
  }
}

customElements.define("sakai-rubrics-shared-list", SakaiRubricsSharedList);
