import { css, html, LitElement } from "lit";
import "@sakai-ui/sakai-icon";
import { loadProperties } from "@sakai-ui/sakai-i18n";
import "@sakai-ui/sakai-pager";

export class SakaiDashboardWidget extends LitElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    title: String,
    state: String,
    _baseI18n: { state: true },
    _i18n: { state: true },
    editing: { type: Boolean },
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

  loadTranslations(options) {

    console.log(options);

    const p = loadProperties(options);
    p.then(r => {

      this._i18n = r;
      this.title = r.widget_title;
    });
    return p;
  }

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
                    title="${this._baseI18n.up}"
                    arial-label="${this._baseI18n.up}">
                  <sakai-icon type="up" size="small"></sakai-icon>
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.moveDown}
                    title="${this._baseI18n.down}"
                    arial-label="${this._baseI18n.down}">
                  <sakai-icon type="down" size="small"></sakai-icon>
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.moveLeft}
                    title="${this._baseI18n.left}"
                    arial-label="${this._baseI18n.left}">
                  <sakai-icon type="left" size="small"></sakai-icon>
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.moveRight}
                    title="${this._baseI18n.right}"
                    arial-label="${this._baseI18n.right}">
                  <sakai-icon type="right" size="small"></sakai-icon>
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.remove}
                    title="${this._baseI18n.remove} ${this.title}"
                    aria-label="${this._baseI18n.remove} ${this.title}">
                  <sakai-icon type="close" size="small"></sakai-icon>
                </a>
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

  static styles = css`

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
      padding-bottom: 0;
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
  `;
}
