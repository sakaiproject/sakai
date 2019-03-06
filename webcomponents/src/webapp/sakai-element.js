import {LitElement} from "./assets/lit-element/lit-element.js";

class SakaiElement extends LitElement {

  createRenderRoot() {

    // Render to the real dom, not the shadow. We can now pull
    // in Sakai's css and js. This makes any this, and any subclasses,
    // custom elements, not full blown web components
    return this;
  }

  loadTranslations(options) {

    var defaults = {loader: "org.sakaiproject.i18n.InternationalizedMessages",
                      bundle: `org.sakaiproject.${options.namespace}.bundle.Messages`,
                      namespace: options.namespace};

    options = Object.assign(defaults, options);

    return new Promise((resolve, reject) => {

      portal.i18n.loadProperties({
        resourceClass: options.loader,
        resourceBundle: options.bundle,
        namespace: options.namespace,
        callback: () => resolve(portal.i18n.translations[options.namespace])
      });
    });
  }
}


export {SakaiElement};
