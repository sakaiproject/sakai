import { LitElement, html, css } from "lit";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import "@sakai-ui/sakai-button/sakai-button.js";
import { loadProperties as lp } from "@sakai-ui/sakai-i18n";

export class SakaiDialogContent extends LitElement {

  static properties = {

    title: { type: String },
    _baseI18n: { state: true },
  };

  constructor() {

    super();

    lp("dialog-content").then(r => this._baseI18n = r);
  }

  close() {
    this.dispatchEvent(new Event("close-overlay", { bubbles: true }));
  }

  cancel() {
    this.close();
  }

  loadProperties(options) {
    return lp(options);
  }

  content() {}

  buttons() {}

  shouldUpdate() {
    return this._baseI18n;
  }

  render() {

    return html`
      <div id="container">
        <div id="titlebar">
          <div id="title">${this.title()}</div>
          <div id="close">
            <a href="javascript:;"
                @click=${this.close}
                aria-label="${this._baseI18n.close}"
                title="${this._baseI18n.close}">
              <sakai-icon type="close"></sakai-icon>
            </a>
          </div>
        </div>
        <div id="content">
          ${this.content()}
        </div>
        <div id="buttonbar">
          ${this.buttons()}
          <sakai-button @click=${this.cancel}>${this._baseI18n.cancel}</sakai-button>
        </div>
      </div>
    `;
  }

  static styles = css`
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
        background-color: var(--sakai-background-color-2);
        font-family: var(--sakai-font-family);
        min-width: 400px;
        box-shadow: var(--elevation-8dp);
      }
        #titlebar {
          padding: 14px;
          display: flex;
          font-size: 16px;
          align-items: center;
          background-color: var(--sakai-background-color-3);
          border-bottom: var(--sakai-border-color) solid 1px;
        }
          #title {
            flex: 2;
          }
          #close {
            flex: 1;
            text-align: right;
          }
        #content {
          background-color: var(--sakai-modal-content-bg);
          padding: 20px;
        }
          div.label {
            margin-bottom: 4px;
            font-weight: bold;
          }
          div.input {
            margin-bottom: 10px;
          }

      #buttonbar {
        background-color: var(--sakai-modal-content-bg);
        display: flex;
        justify-content: flex-end;
        padding: 12px 4px 12px 16px;
      }
        sakai-button {
          margin-left: 10px;
        }
  `;
}
