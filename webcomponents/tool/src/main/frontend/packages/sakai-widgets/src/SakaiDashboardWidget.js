import { css, html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-pager";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiDashboardWidget extends SakaiShadowElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    title: String,
    state: String,
    editing: { type: Boolean },
    disableLeftAndUp: { attribute: "disable-left-and-up", type: Boolean },
    disableRightAndDown: { attribute: "disable-right-and-down", type: Boolean },
    _baseI18n: { state: true },
  };

  constructor() {

    super();

    this.state = "view";
    this.editing = false;
    this.hasOptions = true;
    loadProperties("dashboard-widget").then(r => this._baseI18n = r);
  }

  set widgetId(value) {

    this._widgetId = value;
    loadProperties(value).then(r => this.title = r.widget_title);
  }

  get widgetId() { return this._widgetId; }

  content() {}

  remove() {
    this.dispatchEvent(new CustomEvent("remove", { bubbles: true }));
  }

  shouldUpdate() {
    return this._baseI18n && this.title;
  }

  move(direction) {
    this.dispatchEvent(new CustomEvent("move", { detail: { widgetId: this.widgetId, direction }, bubbles: true }));
  }

  moveUp() { this.move("up"); }

  moveDown() { this.move("down"); }

  moveLeft() { this.move("left"); }

  moveRight() { this.move("right"); }

  render() {

    return html`
      <div id="container">
        <div id="title-bar" class="d-flex align-items-center p-3">
          <div id="title">${this.title}</div>
          ${this.editing ? html`
            <div id="widget-mover" class="ms-auto">
              <div class="${ifDefined(this.disableLeftAndUp ? "d-none" : undefined)}">
                <button type="button"
                    class="btn btn-icon btn-sm"
                    @click=${this.moveUp}
                    title="${this._baseI18n.up}"
                    arial-label="${this._baseI18n.up}">
                  <span class="si si-up fs-6" aria-hidden="true"></span>
                </button>
              </div>
              <div class="${ifDefined(this.disableRightAndDown ? "d-none" : undefined)}">
                <button type="button"
                    class="btn btn-icon btn-sm"
                    @click=${this.moveDown}
                    title="${this._baseI18n.down}"
                    arial-label="${this._baseI18n.down}">
                  <span class="si si-down fs-6" aria-hidden="true"></span>
                </button>
              </div>
              <div class="${ifDefined(this.disableLeftAndUp ? "d-none" : undefined)}">
                <button type="button"
                    class="btn btn-icon btn-sm d-none d-sm-inline"
                    @click=${this.moveLeft}
                    title="${this._baseI18n.left}"
                    arial-label="${this._baseI18n.left}">
                  <span class="si si-left fs-6" aria-hidden="true"></span>
                </button>
              </div>
              <div class="${ifDefined(this.disableRightAndDown ? "d-none" : undefined)}">
                <button type="button"
                    class="btn btn-icon btn-sm d-none d-sm-inline"
                    @click=${this.moveRight}
                    title="${this._baseI18n.right}"
                    arial-label="${this._baseI18n.right}">
                  <span class="si si-right fs-6" aria-hidden="true"></span>
                </button>
              </div>
              <div>
                <button type="button"
                    class="btn btn-icon btn-sm bg-danger"
                    @click=${this.remove}
                    title="${this._baseI18n.remove} ${this.title}"
                    aria-label="${this._baseI18n.remove} ${this.title}">
                  <span class="si si-close fs-6" aria-hidden="true"></span>
                </button>
              </div>
            </div>
          ` : ""}
        </div>
        <div id="content">${this.content()}</div>
        ${this.showPager ? html`
        <sakai-pager count="${this.count}" current="1" @page-selected=${this.pageClicked}></sakai-pager>
        ` : ""}

      </div>
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
      :host {
        width: 100%;
      }
      a {
        color: var(--link-color);
      }
      a:hover { 
        color: var(--link-hover-color);
      }
      a:active {
        color: var(--link-active-color);
      }
      a:visited {
        color: var(--link-visited-color);
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
        background-color: var(--sakai-title-bar-bg-color, rgb(244, 244, 244));
        font-weight: var(--sakai-title-bar-font-weight, bold);
      }

        #title {
          flex: 2;
          margin-left: 12px;
        }
      #content {
        padding: 10px;
        padding-bottom: 0;
        flex-grow: 1;
        border-radius: 0 0 var(--sakai-course-card-border-radius, 4px) var(--sakai-course-card-border-radius, 4px);
      }

      #widget-mover {
        display: flex;
      }
      #widget-mover .btn {
        --bs-btn-color: var(--sakai-text-color-2);
      }
      #widget-mover div {
        padding: 5px;
        flex: 1;
      }
    `
  ];
}
