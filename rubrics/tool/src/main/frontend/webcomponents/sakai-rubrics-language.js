import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {loadProperties} from "/webcomponents/sakai-i18n.js";

export class SakaiRubricsLanguage extends SakaiElement {

  static get properties() {

    return {
      key: { type: String },
      values: { type : Array },
    };
  }

  render() {
    return html`${this.translate(this.key)}`;
  }

  translate(key) {

    return tr(key, this.values);

    //if (rubrics && rubrics.i18n) {
    //  var translation = rubrics.i18n[this.key];
    //} else {
    //var translation = window.sakai.translations["rubrics"][this.key];
    //}

    /*
    for (var i in this.values) {
      translation = translation.replace("{}", typeof this.values[i] === "string" ? this.values[i] : "" );
    }

    return translation;
    */
  }

  static loadTranslations() {
    loadProperties({"namespace": "rubrics"});
  }
}

export function tr(key, values) {

  var translation = window.sakai.translations["rubrics"][key];
  for (var i in values) {
    translation = translation.replace("{}", typeof values[i] === "string" ? values[i] : "" );
  }
  return translation;
}

customElements.define("sr-lang", SakaiRubricsLanguage);
