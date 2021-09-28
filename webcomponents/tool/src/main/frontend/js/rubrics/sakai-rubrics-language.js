import { RubricsElement } from "./rubrics-element.js";
import { html } from "../assets/lit-element/lit-element.js";
import { loadProperties, tr as translate } from "../sakai-i18n.js";

export class SakaiRubricsLanguage extends RubricsElement {

  static get properties() {

    return {
      key: { type: String },
      values: { type: Array }
    };
  }

  render() {
    return html`${this.translate(this.key)}`;
  }

  translate(key) {
    return tr(key, this.values);
  }

  static loadTranslations(cache) {
    return loadProperties({ bundle: "rubrics", cache });
  }
}

export function tr(key, values) {
  return translate("rubrics", key, values);
}

const tagName = "sr-lang";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricsLanguage);
