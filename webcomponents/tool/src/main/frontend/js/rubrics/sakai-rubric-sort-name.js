import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricSortName extends RubricsElement {

  constructor() {

    super();

    this.userId = "";
    this.sortName = "";
  }

  static get properties() {
    return { userId: {attribute: "user-id", type: String}, sortName: String };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "user-id") {
      this.setSortName();
    }
  }

  render() {
    return html`${this.sortName}`;
  }

  setSortName() {

    var self = this;
    jQuery.ajax({
      url: '/sakai-ws/rest/sakai/getUserSortName?eid=' + this.userId
    }).done(function (response) {
      self.sortName = response;
    }).fail(function () {
      self.sortName = self.userId;
    });
  }
}

customElements.define("sakai-rubric-sort-name", SakaiRubricSortName);
