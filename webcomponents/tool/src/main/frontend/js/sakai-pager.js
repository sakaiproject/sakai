import { css } from "./assets/lit-element/lit-element.js";
import { loadProperties } from "./sakai-i18n.js";
import { LionPagination } from "./assets/@lion/pagination/src/LionPagination.js"

export class SakaiPager extends LionPagination {

  constructor() {

    super();

    this.addEventListener("current-changed", (e) => {

      e.stopPropagation();
      this.dispatchEvent(new CustomEvent("page-selected", { detail: { page: this.current }, bubbles: true }));
    });

    loadProperties("pager").then(t => this.i18n = t);
  }

  static get localizeNamespaces() {

    return [{
      'lion-pagination':
      /** @param {string} locale */
      locale => {
        switch (locale) {
          case 'bg-BG':
            return import('./assets/@lion/pagination/translations/bg.js');

          case 'cs-CZ':
            return import('./assets/@lion/pagination/translations/cs.js');

          case 'de-AT':
          case 'de-DE':
            return import('./assets/@lion/pagination/translations/de.js');

          case 'en-AU':
          case 'en-GB':
          case 'en-PH':
          case 'en-US':
            return import('./assets/@lion/pagination/translations/en.js');

          case 'es-ES':
            return import('./assets/@lion/pagination/translations/es.js');

          case 'fr-FR':
          case 'fr-BE':
            return import('./assets/@lion/pagination/translations/fr.js');

          case 'hu-HU':
            return import('./assets/@lion/pagination/translations/hu.js');

          case 'it-IT':
            return import('./assets/@lion/pagination/translations/it.js');

          case 'nl-BE':
          case 'nl-NL':
            return import('./assets/@lion/pagination/translations/nl.js');

          case 'pl-PL':
            return import('./assets/@lion/pagination/translations/pl.js');

          case 'ro-RO':
            return import('./assets/@lion/pagination/translations/ro.js');

          case 'ru-RU':
            return import('./assets/@lion/pagination/translations/ru.js');

          case 'sk-SK':
            return import('./assets/@lion/pagination/translations/sk.js');

          case 'uk-UA':
            return import('./assets/@lion/pagination/translations/uk.js');

          case 'zh-CN':
            return import('./assets/@lion/pagination/translations/zh.js');

          default:
            return import('./assets/@lion/pagination/translations/en.js');
        }
      }
    }, ...super.localizeNamespaces];
  }


  static get properties() {

    return {
      i18n: Object,
    };
  }

  static get styles() {

    return [
      ...super.styles,
      css`
      `,
    ];
  }
}

if (!customElements.get("sakai-pager")) {
  customElements.define("sakai-pager", SakaiPager);
}
