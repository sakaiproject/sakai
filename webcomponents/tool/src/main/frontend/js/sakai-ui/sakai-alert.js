import { css, html, LitElement } from "./assets/lit-element/lit-element.js?version=__buildNumber__";
import { Alert } from "./assets/bootstrap/dist/js/bootstrap.esm.min.js?version=__buildNumber__";
import alert from "./styles/alert.scss?version=__buildNumber__";
export class SakaiAlert extends LitElement {

  static get properties() {

    return {
      href: String,
      sakClass: { type: String }
    };
  }

  constructor() {
    super();
    this.href = "";
    this.sakClass = "";
  }

  clicked() {
    if (this.href) {
      window.parent.location = this.href;
    }
  }
  close() {
    const btsAlert = new Alert(this);
    btsAlert.close();
  }

  focus() {
    this.shadowRoot.querySelector(".alert").focus();
  }


  render() {
    return html`
      <div
        class="alert ${this.sakClass ? this.sakClass : "alert-info"}"
        role="alert"
        >
        <slot></slot>
        <button 
            @click="${this.close}"
            type="button"
            class="btn-close"
            data-bs-dismiss="alert"
            aria-label="Close">
        </button>

  </div>
    `;
  }

  static get styles() {

    return [alert, css`

    `];
  }
}

if (!customElements.get("sakai-alert")) {
  customElements.define("sakai-alert", SakaiAlert);
}
