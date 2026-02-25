import { SakaiDialogContent } from "../src/SakaiDialogContent.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-dialog-content tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(data.baseI18nUrl, data.baseI18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  class MyDialog extends SakaiDialogContent {

    title() { return data.title; }
    content() { return data.content; }
  }

  customElements.define("my-dialog", MyDialog);

  it ("renders correctly", async () => {

    const el = await fixture(html`<my-dialog></my-dialog>`);

    await waitUntil(() => el._baseI18n);

    await elementUpdated(el);

    await expect(el).is.accessible();

    expect(el.renderRoot.querySelector("#title").textContent).to.equal(data.title);
    expect(el.renderRoot.querySelector("#content").innerHTML).to.contain(data.content);
  });
});
