import { SakaiElement } from '../src/SakaiElement.js';
import { expect, fixture, waitUntil } from '@open-wc/testing';
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-element tests", () => {

  window.top.portal = { locale: 'en_GB' };


  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("is subclassed and renders correctly", async () => {

    class MyElement extends SakaiElement {

      render() {
        return html`<h1>CHIPS</h1>`;
      }
    }

    customElements.define("my-element", MyElement);

    const el = await fixture('<my-element></my-element>');
    expect(el.querySelector("h1").innerHTML).to.equal("CHIPS")
  });

  it ("loads translations", async () => {

    class MyElement2 extends SakaiElement {

      static properties = {
        _i18n: { state: true },
      };

      constructor() {

        super();

        this.loadTranslations('myelement2').then(r => this._i18n = r);
        this.loadTranslations({ bundle: "myelement2" }).then(r => this._i18n = r);
      }

      shouldUpdate() {
        return this._i18n;
      }

      render() {

        return html`
          <h1>${this._i18n.greeting} Somebody!</h1>
          <h2>${this.tr("greeting1", [ "Adrian" ])}</h2>
        `;
      }
    }

    customElements.define("my-element2", MyElement2);

    const el = await fixture('<my-element2></my-element2>');
    await waitUntil(() => el._i18n);
    expect(el.querySelector("h1").innerText).to.equal(`${el._i18n.greeting} Somebody!`);
    //expect(el.querySelector("h2").innerText).to.equal(`${greeting1.replace({}, "Adrian")}`);
  });
});
