import { html, css } from "../assets/lit-element/lit-element.js";
import { loadProperties } from "../sakai-i18n.js";
import { SakaiDialogContent } from "../sakai-dialog-content.js";
import "../sakai-file-list.js";

class SakaiCalendarDisplayEvent extends SakaiDialogContent {

  static get styles() {

    return css`
      ${SakaiDialogContent.styles}

      #event-title {
        font-weight: bold;
        font-size: 20px;
        margin-bottom: 20px;
      }

      #event-title sakai-icon {
        margin-right: 20px;
      }
    `;
  }

  constructor() {

    super();

    loadProperties("calendar").then(r => this.i18n = r);
  }

  static get properties() {

    return {
      i18n: Object,
      selected: { type: Object },
    };
  }

  shouldUpdate() {
    return this.i18n && this.selected;
  }

  title() {
    return this.selected.title;
  }

  content() {

    return html`
      <div class="label">${this.i18n["gen.date"]}</div>
      <div class="input">${this.selected.startDate}</div>
      <div class="label">${this.i18n["gen.time"]}</div>
      <div class="input">${this.selected.startTime}</div>
      <div class="label">${this.i18n["gen.descr"]}</div>
      <div class="input">${this.selected.description}</div>
      <div class="label">${this.i18n["gen.attach"]}</div>
      <div class="input"><sakai-file-list files="${JSON.stringify(this.selected.attachments)}"></div>
      <div class="label">${this.i18n["new.itemtype"]}</div>
      <div class="input"><sakai-icon type="${this.selected.type}" size="small"></sakai-icon>${this.selected.type}</div>
      <div class="label">Owner</div>
      <div class="input">${this.selected.owner}</div>
      <div class="label">Site</div>
      <div class="input"><a href="${this.selected.siteUrl}">${this.selected.siteTitle}</a></div>
      ${this.selected.viewUrl ? html`
        <div class="label">View</div>
        <div class="input"><a href="${this.selected.viewUrl}">${this.selected.viewText}</a></div>
      ` : ""}
    `;
  }
}

if (!customElements.get("sakai-calendar-display-event", SakaiCalendarDisplayEvent)) {
  customElements.define("sakai-calendar-display-event", SakaiCalendarDisplayEvent);
}
