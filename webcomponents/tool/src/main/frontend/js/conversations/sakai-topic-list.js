import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import moment from "../assets/moment/dist/moment.js";
import "./sakai-topic-summary.js";

export class SakaiTopicList extends SakaiElement {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      data: { type: Object },
    };
  }

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set siteId(value) {

    this._siteId = value;

    fetch(`/api/sites/${this.siteId}/topics`)
      .then(r => r.json())
      .then(data => {

        this.data = data;

        this.data.topics = this.data.topics.map(t => {

          t.lastActivityHuman = moment.duration(t.lastActivity - Date.now(), "milliseconds").humanize(true);
          return t;
        });
      });
  }

  get siteId() { return this._siteId; }

  shouldUpdate() {
    return this.i18n && this.data;
  }

  render() {

    return html`
      <div class="topic-list">
      ${this.data.topics.map(t => html`
        <div>
          <sakai-topic-summary topic="${JSON.stringify(t)}" available-tags=${JSON.stringify(this.data.availableTags)}></sakai-topic-summary>
        </div>
      `)}
      </div>
    `;
  }
}

if (!customElements.get("sakai-topic-list")) {
  customElements.define("sakai-topic-list", SakaiTopicList);
}
