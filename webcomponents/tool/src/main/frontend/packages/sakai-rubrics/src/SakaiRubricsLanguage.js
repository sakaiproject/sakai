import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { loadProperties, tr as translate } from "@sakai-ui/sakai-i18n";

export class SakaiRubricsLanguage extends RubricsElement {

  static properties = {

    key: { type: String },
    values: { type: Array }
  };

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
