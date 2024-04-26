import { css, LitElement } from "lit";
import { loadProperties, tr } from "@sakai-ui/sakai-i18n";

export class SakaiShadowElement extends LitElement {

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

  static styles = css`
    button {
      color: var(--sui-btn-color);
      background: var(--sui-btn-bg-color);
      padding: var(--sui-btn-padding);
      border-radius: var(--sui-btn-border-radius);
      border-width: var(--sui-btn-border-width);
      border-color: var(--sui-btn-border-color);
      box-shadow: var(--sui-btn-box-shadow);
      font-family: var(--sui-btn-font-family);
      font-size: var(--sui-btn-font-size);
      font-weight: var(--sui-btn-font-weight);
      line-height: var(--sui-btn-line-height);
    }

    button:hover {
      background: var(--sui-btn-hover-bg-color);
      border-color: var(--sui-btn-hover-border-color);
      box-shadow: var(--sui-btn-hover-box-shadow);
    }
  `;
}
