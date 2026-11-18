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

  for (const mixed of [false, true]) {
    it(`renders unavailable results without links (${mixed ? "mixed" : "all unavailable"})`, async () => {
      fetchMock.removeRoutes();
      fetchMock.get(data.i18nUrl, data.i18n).get(data.searchUrl, [
        ...(mixed ? [data.searchResults[0]] : []),
        { censored: true, title: "", url: "", searchResult: "", siteTitle: data.siteTitle },
        ...(mixed ? [data.searchResults[1]] : []),
      ]);
      const el = await fixture(html`<sakai-search site-id="${data.siteId}" tool="${data.tool}"></sakai-search>`);
      await waitUntil(() => el._i18n);
      el.querySelector("input").value = data.terms;
      el.querySelector("form").requestSubmit();
      await waitUntil(() => el.querySelector(".search-result-unavailable"));
      const notice = el.querySelector(".search-result-unavailable");
      expect(notice.textContent).to.include(data.siteTitle);
      expect(notice.textContent).to.include("1 item matched the search terms but is not available");
      expect(notice.closest("a")).to.be.null;
      expect(el.querySelectorAll(".search-result-link").length).to.equal(mixed ? 2 : 0);
      await el.updateComplete;
      const links = el.querySelectorAll(".search-result-link");
      if (mixed) {
        expect(document.activeElement).to.equal(links[0]);
        links[0].dispatchEvent(new KeyboardEvent("keydown", { code: "ArrowDown", bubbles: true }));
        expect(document.activeElement).to.equal(links[1]);
      } else {
        expect(document.activeElement).to.equal(el.querySelector("input"));
      }
      await expect(el).to.be.accessible();
    });
  }
});
