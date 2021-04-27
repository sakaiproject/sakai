import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import moment from "../assets/moment/dist/moment.js";
import "./sakai-topic-list.js";

export class SakaiConversations extends SakaiElement {

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
      <div id="conv-desktop">

        <div id="conv-filters-and-list">
          <div id="conv-filters">
            <div id="conv-filter-tags">
              <div>Tags</div>
              <div>
                <select @changed=${this.tagSelected}>
                ${this.data.availableTags.map(tag => html`
                  <option value="${tag}">${tag}</option>
                `)}
                </select>
              </div>
            </div>
            <div id="conv-filter-dunno">
              <div>Filter</div>
              <div>
                <select @changed=${this.filterSelected}>
                ${this.data.availableTags.map(tag => html`
                  <option value="${tag}">${tag}</option>
                `)}
                </select>
              </div>
            </div>
          </div>

          <div id="conv-list">
            <sakai-topic-list data="${JSON.stringify(this.data)}"></sakai-topic-list>
          </div>
        </div>

        <div id="conv-add-post-and-content">

          <div id="conv-add-post">
            <button class="active">New Post</button>
          </div>

          <div id="conv-content">
            ${this.selectedTopic ? html`
            <sakai-topic topic="${JSON.stringify(this.selectedTopic)}"></sakai-topic>
            ` : html`
            BALLS
            `}
          </div>
        </div>
      </div>
    `;
  }
}

if (!customElements.get("sakai-conversations")) {
  customElements.define("sakai-conversations", SakaiConversations);
}
