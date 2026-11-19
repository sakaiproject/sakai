import { SakaiDialog } from '../src/SakaiDialog.js';
import { expect, fixture, html, waitUntil } from '@open-wc/testing';
import * as data from "./data.js";
import fetchMock from "fetch-mock";

describe("sakai-element tests", () => {

  beforeEach(() => {

    fetchMock.mockGlobal();
    fetchMock
      .get(data.baseI18nUrl, data.baseI18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("is subclassed and renders correctly", async () => {

    class MyDialog extends SakaiDialog {

      body() {
        return html`<h1>CHIPS</h1>`;
      }
    }

    customElements.define("my-dialog", MyDialog);

    const el = await fixture('<my-dialog></my-dialog>');
    await waitUntil(() => el._baseI18n);
    await expect(el).to.be.accessible();

    expect(el.renderRoot.querySelector("h1").innerHTML).to.equal("CHIPS");

    // Initially, the dialog is closed
    expect(el.renderRoot.querySelector("dialog[open]")).to.not.exist;
    el.showModal();
    // Now it should be open
    expect(el.renderRoot.querySelector("dialog[open]")).to.exist;
    // Click the close button
    el.renderRoot.querySelector("dialog #header button").click();
    expect(el.renderRoot.querySelector("dialog[open]")).to.not.exist;
  });
});
