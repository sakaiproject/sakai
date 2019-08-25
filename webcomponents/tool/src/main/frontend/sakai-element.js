import {LitElement} from "./assets/lit-element/lit-element.js";
import {i18nMixin} from "./sakai-i18n-mixin.js";

class SakaiElement extends i18nMixin(LitElement) {

  createRenderRoot() {

    // Render to the real dom, not the shadow. We can now pull
    // in Sakai's css and js. This makes any this, and any subclasses,
    // custom elements, not full blown web components
    return this;
  }
}


export {SakaiElement};
