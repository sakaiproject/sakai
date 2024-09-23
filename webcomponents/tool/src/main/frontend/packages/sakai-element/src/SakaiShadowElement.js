import { css, LitElement } from "lit";
import { loadProperties, tr } from "@sakai-ui/sakai-i18n";
import { getGlobalStyleSheets } from "./global-styles.js";

export class SakaiShadowElement extends LitElement {

  static properties = {
    _online: { state: true },
  };

  connectedCallback() {

    super.connectedCallback();

    this._online = navigator.onLine;
  }

  /**
   * Convenience wrapper for sakai-18n.tr.
   *
   * Example:
   *
   * confirm_coolness=This is {} cool
   * let translated = mySakaiElementSubclass.tr("confirm_coolness", ["really"]);
   *
   * @param {string} key The i18n key we want to translate
   * @params {(string[]|Object)} options This can either be an array of replacement strings, or an object
   * which contains token names to values, as well as options like debug: true.
   * @param {boolean} [forceBundle=this.bundle] The bundle to use in preference to this.bundle
   */
  tr(key, options, forceBundle) {
    return tr(forceBundle || this.bundle, key, options);
  }

  loadTranslations(options) {

    if (typeof options === "string") {
      this.bundle = options;
    } else {
      this.bundle = options.bundle;
    }

    // Pass the call on to the imported function
    return loadProperties(options);
  }

  setSetting(component, name, value) {

    const currentString = localStorage.getItem(`${component}-settings`);
    const settings = currentString ? JSON.parse(currentString) : {};
    settings[name] = value;
    localStorage.setItem(`${component}-settings`, JSON.stringify(settings));
  }

  getSetting(component, name) {

    const currentString = localStorage.getItem(`${component}-settings`);
    return !currentString ? null : JSON.parse(currentString)[name];
  }

  static styles = [
    ...getGlobalStyleSheets(),
    css`
      select[multiple], select[size]:not([size='1']) {
        background-image: none;
      }
    `
  ];
}
