import "../sakai-image-editor.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture, aTimeout, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-image-editor tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.dialogcontentI18Url, data.dialogcontentI18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {

    let el = await fixture(html`
      <sakai-image-editor image-url="/packages/sakai-image-editor/test/images/orville.jpeg"></sakai-image-editor>
    `);

    await waitUntil(() => el.shadowRoot.getElementById("image"), "Image element not created");
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-image-editor></sakai-image-editor>
    `);

    expect(el).to.be.accessible();
  });
});
