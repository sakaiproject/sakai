import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

class SakaiRubricGradingButton extends RubricsElement {

  constructor() {

    super();

    this.hasEvaluation = false;
  }

  set token(newValue) {
    this._token = "Bearer " + newValue;
  }

  get token() { return this._token; }

  static get properties() {

    return {
      token: String,
      entityId: {attribute: "entity-id", type: String},
      toolId: {attribute: "tool-id", type: String},
      evaluatedItemId: {attribute: "evaluated-item-id", type: String},
      hasEvaluation: Boolean,
    };
  }

  attributeChangedCallback(name, oldVal, newVal) {

    super.attributeChangedCallback(name, oldVal, newVal);

    if (this.entityId && this.toolId && this.token && this.evaluatedItemId) {
      this.setHasEvaluation();
    }
  }

  render() {

    return html`
      <a href="javascript:;">
        <span class="icon-sakai--sakai-rubrics ${this.hasEvaluation ? "rubric-active" : ""}"></span>
      </a>
    `;
  }

  setHasEvaluation() {

    $.ajax({
      url: `/rubrics-service/rest/evaluations/search/by-tool-item-and-associated-item-and-evaluated-item-ids?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`,
      headers: {"authorization": this.token}
    })
    .done(data => this.hasEvaluation = data._embedded.evaluations && data._embedded.evaluations.length > 0)
    .fail((jqXHR, textStatus, errorThrown) => { console.log(textStatus); console.log(errorThrown); });
  }
}

try {
  customElements.define("sakai-rubric-grading-button", SakaiRubricGradingButton);
} catch (error) { /* That's okay */ }
