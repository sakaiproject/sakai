import { SakaiShadowElement } from "./SakaiShadowElement.js";

export class SakaiElement extends SakaiShadowElement {

  createRenderRoot() {

    // Render to the real dom, not the shadow. We can now pull
    // in Sakai's css and js. This makes any this, and any subclasses,
    // custom elements, not full blown web components
    return this;
  }

  static styles = [ ];
}
