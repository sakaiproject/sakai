import "../sakai-document-viewer.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-document-viewer tests", () => {

  const content = { name: "something", ref: "/content/something" };
  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {

    const el = await fixture(html`<sakai-document-viewer content="${JSON.stringify(content)}"></sakai-document-viewer>`);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("div.document-link")).to.exist;
    expect(el.querySelector("a[rel='noopener']")).to.exist;
    expect(el.querySelector("a[rel='noopener']").innerHTML).to.contain(content.name);
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-document-viewer content="${JSON.stringify(content)}"></sakai-document-viewer>`);

    await waitUntil(() => el._i18n);

    await expect(el).to.be.accessible();
  });
});
