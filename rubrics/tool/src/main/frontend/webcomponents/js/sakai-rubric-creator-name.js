import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricCreatorName extends RubricsElement {

  constructor() {

    super();

    this.creatorId = "";
    this.creatorName = "";
  }

  static get properties() {
    return { creatorId: {attribute: "creator-id", type: String}, creatorName: String };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "creator-id") {
      this.setCreatorName();
    }
  }

  render() {
    return html`${this.creatorName}`;
  }

  setCreatorName() {

    var self = this;
    jQuery.ajax({
      url: '/sakai-ws/rest/sakai/getUserDisplayName?sessionid=' + sakaiSessionId + '&eid=' + this.creatorId
    }).done(function (response) {
      self.creatorName = response;
    }).fail(function () {
      self.creatorName = self.creatorId;
    });
  }
}

customElements.define("sakai-rubric-creator-name", SakaiRubricCreatorName);
