import "../sakai-search.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-search tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.searchUrl, data.searchResults, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {

    let el = await fixture(html`
      <sakai-search site-id="${data.siteId}" tool="${data.tool}"></sakai-search>
    `);

    await waitUntil(() => el._i18n);

    expect(el.querySelector("form")).to.exist;
    expect(el.querySelector("input[type='search']")).to.exist;
    document.getElementById("sakai-search-input").value = data.terms;
    document.getElementById("sakai-search-button").click();
    await waitUntil(() => el.querySelector(".search-result-link"), "Element did not render results");
    expect(document.querySelectorAll(".search-result-link").length).to.equal(2);
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-search site-id="${data.siteId}" tool="${data.tool}"></sakai-search>
    `);

    await expect(el).to.be.accessible();
    document.getElementById("sakai-search-input").value = data.terms;
    document.getElementById("sakai-search-button").click();
    await el.updateComplete;
    await waitUntil(() => el.querySelector(".search-result-link"), "Element did not render results");
    await expect(el).to.be.accessible();
  });
});
