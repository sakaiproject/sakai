import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-editor.js";

export class AddTopic extends SakaiElement {

  static get properties() {

    return {
      aboutReference: { attribute: "about-reference", type: String },
      topic: { type: Object },
    };
  }

  constructor() {

    super();

    this.new = true;

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set topic(value) {

    this._topic = value;
    this.new = false;
  }

  get topic() { return this._topic; }
  
  saveTopic() {

    fetch("/api/topics", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(this.topic),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while posting.");
      }
      return r.json();
    })
    .then(topic => {

      this.dispatchEvent(new CustomEvent("topic-saved", { detail: { topic: this.topic } }));
    })
    .catch (error => {
      console.error(error);
      //TODO: show error message to user here
    });
  }

  shouldUpdate() {
    return this.i18n && (this.topic || this.aboutReference);
  }

  render() {

    return html`
      ${this.new ? html`<h1>ADD</h1>` : html`<h1>UPDATE</h1>`}
    `;
  }
}

if (!customElements.get("add-topic")) {
  customElements.define("add-topic", AddTopic);
}
