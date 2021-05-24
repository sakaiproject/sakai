import { css, html, LitElement } from "../assets/lit-element/lit-element.js";
import '../sakai-icon.js';
import '../sakai-options-menu.js';
import { loadProperties } from "../sakai-i18n.js";

export class SakaiDashboardWidget extends LitElement {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      userId: { attribute: "user-id", type: String },
      title: String,
      state: String,
      baseI18n: Object,
      i18n: Object,
      data: { type: Array },
      editing: { type: Boolean },
    };
  }

  constructor() {

    super();
    this.data = [];
    this.title = "Widget";
    this.state = "view";
    this.editing = false;
    this.hasOptions = true;
    loadProperties("dashboard-widget").then(r => this.baseI18n = r);
  }

  set siteId(value) {

    this._siteId = value;
    this.loadData();
  }

  get siteId() { return this._siteId; }

  set userId(value) {

    this._userId = value;
    this.loadData();
  }

  get userId() { return this._userId; }

  loadData() {}

  loadTranslations(options) {

    let p = loadProperties(options);
    p.then(r => {
      this.i18n = r;
      this.title = r["widget_title"];
    });
    return p;
  }

  content() {}

  remove(e) {
    this.dispatchEvent(new CustomEvent("remove", { bubbles: true }));
  }

  shouldUpdate(changed) {
    return this.i18n && this.baseI18n && this.title;
  }

  move(direction) {
    this.dispatchEvent(new CustomEvent("move", { detail: { widgetId: this.widgetId, direction: direction }, bubbles: true }));
  }

  moveUp() {
    this.move("up");
  }

  moveDown() {
    this.move("down");
  }

  moveLeft() {
    this.move("left");
  }

  moveRight() {
    this.move("right");
  }

  render() {

    return html`
      <div id="container">
        <div id="title-bar">
          <div id="title">${this.title}</div>
          ${this.editing ? html`
            <div id="widget-mover">
              <div>
                <a href="javascript:;"
                    @click=${this.moveUp}
                    title="${this.baseI18n["up"]}"
                    arial-label="${this.baseI18n["up"]}">
                  <sakai-icon type="up" size="small">
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.moveDown}
                    title="${this.baseI18n["down"]}"
                    arial-label="${this.baseI18n["down"]}">
                  <sakai-icon type="down" size="small">
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.moveLeft}
                    title="${this.baseI18n["left"]}"
                    arial-label="${this.baseI18n["left"]}">
                  <sakai-icon type="left" size="small">
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.moveRight}
                    title="${this.baseI18n["right"]}"
                    arial-label="${this.baseI18n["right"]}">
                  <sakai-icon type="right" size="small">
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.remove}
                    title="${this.baseI18n["remove"]} ${this.title}"
                    aria-label="${this.baseI18n["remove"]} ${this.title}">
                  <sakai-icon type="close" size="small">
                </a>
              </div>
            </div>
          ` : ""}
        </div>
        <div id="content">${this.content()}</div>
      </div>
    `;
  }

  moved() {

    // This may not have been rendered if we are in the remove state.
    let optionsMenu = this.shadowRoot.querySelector("sakai-options-menu");
    if (optionsMenu) {
      optionsMenu.refresh();
    }
  }

  static get styles() {

    return css`

      :host {
        width: 100%;
      }

      #topbar {
        display: flex;
        margin-top: 8px;
        margin-bottom: 20px;
      }

      #container {
        display: flex;
        flex-flow: column;
        height: 100%;
        background-color: var(--sakai-dashboard-widget-bg-color, white);
        border-radius: var(--sakai-course-card-border-radius, 4px);
        border: solid;
        border-width: var(--sakai-dashboard-widget-border-width, 1px);
        border-color: var(--sakai-dashboard-widget-border-color, rgb(224,224,224));
      }

      #title-bar {
        display: flex;
        padding: 10px;
        background-color: var(--sakai-title-bar-bg-color, rgb(244, 244, 244));
        font-weight: var(--sakai-title-bar-font-weight, bold);
      }

        #title-bar sakai-icon[type="close"] {
          color: var(--sakai-close-icon-color, red);
        }

        #title {
          flex: 2;
          margin-left: 12px;
        }
      #content {
        padding: 10px;
        flex-grow: 1;
        border-radius: 0 0 var(--sakai-course-card-border-radius, 4px) var(--sakai-course-card-border-radius, 4px);
      }

      #widget-mover {
        display: flex;
      }
        #widget-mover div {
          padding: 5px;
          flex: 1;
        }

      a {
        text-decoration: none;
        color: var(--link-color);
      }
    `;
  }
}

if (!customElements.get("sakai-dashboard-widget")) {
  customElements.define("sakai-dashboard-widget", SakaiDashboardWidget);
}
