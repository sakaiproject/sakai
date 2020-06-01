import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { loadProperties, tr as translate } from "/webcomponents/sakai-i18n.js";

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

try {
  customElements.define("sr-lang", SakaiRubricsLanguage);
} catch (error) {
  // Can happen under healthy circumstances
}
