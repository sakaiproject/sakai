import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import { conditionMessage, plainText } from "./condition-utils.js";

export class SakaiConditionText extends SakaiShadowElement {

  static properties = {
    item: { type: String },
    condition: { type: Object },
  };

  constructor() {
    super();
    this.loadTranslations("condition");
  }

  render() {
    if (!this._i18n || !this.condition) { return nothing; }
    const { template, inserts } = conditionMessage(this._i18n, this.condition, plainText(this.item));
    // Render substitutions as text nodes, preserving emphasis without injecting item HTML.
    return html`${template.split(/(\{\d+\})/).map(part => {
      const index = /^\{(\d+)\}$/.exec(part);
      return index ? html`<b>${inserts[Number(index[1])]}</b>` : part;
    })}`;
  }
}
