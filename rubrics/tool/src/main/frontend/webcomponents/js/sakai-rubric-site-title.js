import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricSiteTitle extends RubricsElement {

  constructor() {

    super();

    this.siteId = "";
    this.siteTitle = "";
  }

  static get properties() {
    return { siteId: {attribute: "site-id", type: String}, siteTitle: {attribute: true, type: String} };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("site-id" === name) {
      this.setSiteTitle();
    }
  }

  render() {
    return html`${this.siteTitle}`;
  }

  setSiteTitle() {

    var self = this;
    jQuery.ajax({
      url: '/sakai-ws/rest/sakai/getSiteTitle?sessionid=' + sakaiSessionId + '&siteid=' + this.siteId
    }).done(function (response) {
      self.siteTitle = response;
    }).fail(function () {
      self.siteTitle = self.siteId;
    });
  }
}

customElements.define("sakai-rubric-site-title", SakaiRubricSiteTitle);
