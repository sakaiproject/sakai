import "../sakai-image-editor.js";
import * as data from "./data.js";
import * as dialogContentData from "../../sakai-dialog-content/test/data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-image-editor tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(dialogContentData.baseI18nUrl, dialogContentData.baseI18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders correctly", async () => {

    const el = await fixture(html`
      <sakai-image-editor image-url="/packages/sakai-image-editor/test/images/orville.jpeg"></sakai-image-editor>
    `);

    await waitUntil(() => el.shadowRoot.getElementById("image"), "Image element not created");

    await expect(el).to.be.accessible();
  });
});
