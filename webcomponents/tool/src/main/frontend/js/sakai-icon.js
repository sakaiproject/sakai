import { html, css, LitElement } from './assets/lit-element/lit-element.js';
import { icon, library } from './assets/@fortawesome/fontawesome-svg-core';
import { faStar, faEllipsisV, faBell, faComments, faBook, faFileAlt } from './assets/@fortawesome/free-solid-svg-icons';

export class SakaiIcon extends LitElement {

  static get styles() {

    return css`
      .sakai-small-icon { width: var(--sakai-small-icon-width, 14px); height: var(--sakai-small-icon-height, 15px) }
      .sakai-medium-icon { width: var(--sakai-medium-icon-width, 24px); height: var(--sakai-medium-icon-height, 24px) }
      .sakai-large-icon { width: var(--sakai-large-icon-width, 32px); height: var(--sakai-large-icon-height, 32px) }

      .alert {
        background-color: var(--sakai-icon-alert-color, red);
        width: var(--sakai-icon-alert-width, 4px);
        height: var(--sakai-icon-alert-width, 4px);
        position: absolute;
        margin-top: -18px;
        margin-left: 15px;
        -webkit-border-radius: calc(var(--sakai-icon-alert-width, 4px) / 2);
        -moz-border-radius: calc(var(--sakai-icon-alert-width, 4px) / 2);
        border-radius: calc(var(--sakai-icon-alert-width, 4px) / 2);
      }
    `;
  }

  static get properties() {

    return {
      hasAlerts: { attribute: "has-alerts", type: Boolean },
      type: String,
      size: String,
    };
  }

  constructor() {

    super();
    this.size = "medium";
  }

  render() {
    return html`${icon(SakaiIcon.lookups.get(this.type), {classes: `sakai-${this.size}-icon`}).node}${this.hasAlerts ? html`<div class="alert"></div>` : ""}`;
  }
}

library.add(faEllipsisV); // Menu
library.add(faStar); // Favourite
library.add(faBell); // General alerts
library.add(faComments); // Forums
library.add(faBook); // Gradebook
library.add(faFileAlt); // Assignments

SakaiIcon.lookups = new Map();
SakaiIcon.lookups.set("favourite", faStar);
SakaiIcon.lookups.set("alert", faBell);
SakaiIcon.lookups.set("menu", faEllipsisV);
SakaiIcon.lookups.set("forums", faComments);
SakaiIcon.lookups.set("gradebook", faBook);
SakaiIcon.lookups.set("assignments", faFileAlt);

if (!customElements.get("sakai-icon")) {
  customElements.define("sakai-icon", SakaiIcon);
}
