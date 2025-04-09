import "../sakai-document-viewer.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-document-viewer tests", () => {

  const content = { name: "something", ref: "/content/something" };
  window.top.portal = { siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {

    const el = await fixture(html`<sakai-document-viewer content="${JSON.stringify(content)}"></sakai-document-viewer>`);

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.querySelector("div.document-link")).to.exist;
    expect(el.querySelector("a[rel='noopener']")).to.exist;
    expect(el.querySelector("a[rel='noopener']").innerHTML).to.contain(content.name);
  });
});
