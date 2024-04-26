import { html, LitElement } from "lit";
import { OverlayMixin } from "@lion/overlays";

export class SakaiMenu extends OverlayMixin(LitElement) {

  _defineOverlayConfig() {

    return ({
      placementMode: "local",
    });
  }

  _setupOpenCloseListeners() {

    super._setupOpenCloseListeners();

    if (this._overlayInvokerNode) {
      this._overlayInvokerNode.addEventListener("click", this.toggle);
    }
  }

  _teardownOpenCloseListeners() {

    super._teardownOpenCloseListeners();

    if (this._overlayInvokerNode) {
      this._overlayInvokerNode.removeEventListener("click", this.toggle);
    }
  }

  render() {

    return html`
      <slot name="invoker"></slot>
      <slot name="backdrop"></slot>
      <div id="overlay-content-node-wrapper">
        <slot name="content"></slot>
      </div>
      <div>popup is ${this.opened ? "opened" : "closed"}</div>
    `;
  }
}
