import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { html, css } from "lit";
import { loadProperties as lp } from "@sakai-ui/sakai-i18n";

export class SakaiDialog extends SakaiShadowElement {

  static properties = { _baseI18n: { state: true } };

  constructor() {

    super();

    lp("dialog").then(r => this._baseI18n = r);
  }

  title() {}

  body() {}

  show() {
    this.renderRoot.querySelector("dialog")?.show();
  }

  showModal() {
    this.renderRoot.querySelector("dialog")?.showModal();
  }

  close() {
    this.renderRoot.querySelector("dialog")?.close();
  }

  shouldUpdate() {
    return this._baseI18n;
  }

  render() {

    return html`
      <dialog aria-labelledby="dialog-title">
        <div id="content">
          <div id="header">
            <h5 id="dialog-title">${this.title()}</h5>
            <div id="close">
              <button type="button"
                  title="${this._baseI18n.close_tooltip}"
                  @click=${this.close}>
                <i class="si si-close"></i>
              </button>
            </div>
          </div>
          <div id="body">
            ${this.body()}
          </div>
        </div>
      </dialog>
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
      dialog {
        padding: 0;
        &[open] {
          border: 0;
          border-radius: 0.5rem;
        }

        &::backdrop {
          backdrop-filter: blur(2px);
        }
      }

      #content {
        background: var(--sakai-modal-content-bg);
      }

      #header {
        display: flex;
        background: var(--sakai-modal-header-bg);
        color: var(--sakai-modal-header-color);
        justify-items: space-between;
        align-items: center;
        border-bottom: solid 1px #dee2e6;
        padding: 1rem 1rem;

        h5 {
          margin: 10px;
        }

        #close {
          margin-left: auto;
          button {
            background-color: var(--sakai-modal-close-btn-bg);
            color: #000000;
            border: 0;
            border-radius: 0.4rem;
            i {
              font-size: 24px;
              font-weight: bold;
            }
          }
        }
      }

      #body {
        padding: 20px;
      }
    `,
  ];
}
