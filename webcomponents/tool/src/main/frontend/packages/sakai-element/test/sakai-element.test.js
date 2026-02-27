import { SakaiElement } from '../src/SakaiElement.js';
import { elementUpdated, expect, fixture, html, waitUntil } from '@open-wc/testing';
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-element tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  /*
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

      constructor() {

        super();

        this.loadTranslations('myelement2');
        //this.loadTranslations({ bundle: "myelement2" });
      }

      shouldUpdate() {
        return this._i18n;
      }

      render() {

        console.log("render");

        console.log(this._i18n);

        return html`
          <h1>BALLS</h1>
          <h1>${this._i18n.greeting} Somebody!</h1>
          <h2>${this.tr("greeting1", [ "Adrian" ])}</h2>
        `;
      }
    }

    customElements.define("my-element2", MyElement2);

    const el = await fixture('<my-element2></my-element2>');

    await elementUpdated(el);

    console.log(el);

    expect(el.querySelector("h1").innerText).to.equal(`${el._i18n.greeting} Somebody!`);
  });
  */
});
