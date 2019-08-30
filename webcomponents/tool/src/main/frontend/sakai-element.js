import {LitElement} from "./assets/lit-element/lit-element.js";
import {loadProperties} from "./sakai-i18n.js";

class SakaiElement extends LitElement {

  createRenderRoot() {

    // Render to the real dom, not the shadow. We can now pull
    // in Sakai's css and js. This makes any this, and any subclasses,
    // custom elements, not full blown web components
    return this;
  }

  loadTranslations(options) {

    // Pass the call on to the imported function
    return loadProperties(options);
  }
}


export {SakaiElement};
