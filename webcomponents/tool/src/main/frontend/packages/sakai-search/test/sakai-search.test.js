import "../sakai-search.js";
import * as data from "./data.js";
import { expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-search tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.searchUrl, data.searchResults)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

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

    await expect(el).to.be.accessible();
  });
});
