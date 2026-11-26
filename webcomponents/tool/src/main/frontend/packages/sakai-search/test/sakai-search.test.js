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
      .get(data.sitesUrl, data.sites)
      .get(data.toolsUrl, data.tools)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders correctly", async () => {

    const el = await fixture(html`
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

  for (const mixed of [ false, true ]) {
    it(`renders unavailable results without links (${mixed ? "mixed" : "all unavailable"})`, async () => {
      fetchMock.removeRoutes();
      fetchMock.get(data.i18nUrl, data.i18n).get(data.sitesUrl, data.sites).get(data.toolsUrl, data.tools).get(data.searchUrl, [
        ...(mixed ? [ data.searchResults[0] ] : []),
        { censored: true, title: "", url: "", searchResult: "", siteTitle: data.siteTitle },
        ...(mixed ? [ data.searchResults[1] ] : []),
      ]);
      const el = await fixture(html`<sakai-search site-id="${data.siteId}" tool="${data.tool}"></sakai-search>`);
      await waitUntil(() => el._i18n);
      const status = el.querySelector("[role=\"status\"]");
      expect(status).to.exist;
      expect(status.getAttribute("aria-atomic")).to.equal("true");
      expect(status.textContent.trim()).to.equal("");
      el.querySelector("input").value = data.terms;
      el.querySelector("form").requestSubmit();
      await waitUntil(() => el.querySelector(".search-result-unavailable"));
      const notice = el.querySelector(".search-result-unavailable");
      expect(notice.textContent).to.include(data.siteTitle);
      expect(notice.textContent).to.include("1 item matched the search terms but is not available");
      expect(notice.closest("a")).to.be.null;
      expect(el.querySelectorAll(".search-result-link").length).to.equal(mixed ? 2 : 0);
      await el.updateComplete;
      expect(el.querySelector("[role=\"status\"]")).to.equal(status);
      expect(status.textContent.trim()).to.equal(mixed ? "" : "Matching items are not available.");
      const links = el.querySelectorAll(".search-result-link");
      if (mixed) {
        expect(document.activeElement).to.equal(links[0]);
        links[0].dispatchEvent(new KeyboardEvent("keydown", { code: "ArrowDown", bubbles: true }));
        expect(document.activeElement).to.equal(links[1]);
      } else {
        expect(document.activeElement).to.equal(el.querySelector("input"));
      }
      await expect(el).to.be.accessible();
      el.querySelector("button[title=\"Close the search results\"]").click();
      await el.updateComplete;
      expect(status.textContent.trim()).to.equal("");
    });
  }

  it ("filters by the selected site and tool", async () => {

    const el = await fixture(html`<sakai-search></sakai-search>`);
    await waitUntil(() => el._i18n);

    expect(el.querySelector("select")).to.not.exist;

    el.querySelector("input").focus();
    await waitUntil(() => el.querySelectorAll("select").length === 2, "Filters did not render");

    const [ siteSelect, toolSelect ] = el.querySelectorAll("select");
    expect(siteSelect.value).to.equal("");
    expect(Array.from(siteSelect.options).map(o => o.textContent.trim()))
      .to.deep.equal([ "All Pinned Sites", data.siteTitle, "Carrots 101" ]);
    expect(toolSelect.value).to.equal("");
    expect(Array.from(toolSelect.options).map(o => o.value)).to.deep.equal([ "", "assignments", "wiki" ]);

    await expect(el).to.be.accessible();

    const filteredUrl = `/api/search?terms=${data.terms}&site=carrots101&tool=wiki`;
    const pinnedUrl = `/api/search?terms=${data.terms}&pinned=true&tool=wiki`;
    fetchMock.removeRoutes();
    fetchMock.get(data.i18nUrl, data.i18n)
      .get(filteredUrl, data.searchResults)
      .get(pinnedUrl, data.searchResults, { name: "pinned" })
      .get("*", 500);

    siteSelect.value = "carrots101";
    siteSelect.dispatchEvent(new Event("change"));
    toolSelect.value = "wiki";
    toolSelect.dispatchEvent(new Event("change"));
    expect(el.siteId).to.equal("carrots101");
    expect(el.tool).to.equal("wiki");

    el.querySelector("input").value = data.terms;
    el.querySelector("form").requestSubmit();
    await waitUntil(() => el.querySelector(".search-result-link"), "Filtered search did not render results");
    expect(fetchMock.callHistory.called(filteredUrl)).to.be.true;

    siteSelect.value = "";
    siteSelect.dispatchEvent(new Event("change"));
    await el.updateComplete;
    expect(el.siteId).to.be.undefined;
    expect(el.querySelector(".search-result-link")).to.not.exist;

    el.querySelector("form").requestSubmit();
    await waitUntil(() => el.querySelector(".search-result-link"), "Pinned search did not render results");
    expect(fetchMock.callHistory.called("pinned")).to.be.true;
  });

  it ("shows the site and tool it was scoped to in the filters", async () => {

    const el = await fixture(html`<sakai-search site-id="${data.siteId}" tool="assignments"></sakai-search>`);
    await waitUntil(() => el._i18n);
    el.querySelector("input").focus();
    await waitUntil(() => el.querySelectorAll("select").length === 2, "Filters did not render");

    const [ siteSelect, toolSelect ] = el.querySelectorAll("select");
    expect(siteSelect.value).to.equal(data.siteId);
    expect(toolSelect.value).to.equal("assignments");

    el.removeAttribute("site-id");
    el.removeAttribute("tool");
    await el.updateComplete;
    expect(siteSelect.value).to.equal("");
    expect(toolSelect.value).to.equal("");
  });

  it ("shows a scoped site that is not pinned or not one of the user's sites", async () => {

    fetchMock.removeRoutes();
    fetchMock.get(data.i18nUrl, data.i18n)
      .get(data.sitesUrl, data.sites)
      .get(data.toolsUrl, data.tools)
      .get(data.otherSiteUrl, data.otherSite)
      .get("*", 500);
    const el = await fixture(html`<sakai-search site-id="turnips101"></sakai-search>`);
    await waitUntil(() => el._i18n);
    el.querySelector("input").focus();
    await waitUntil(() => el.querySelector("select"), "Filters did not render");

    const siteSelect = el.querySelector("select");
    await waitUntil(() => siteSelect.value === "turnips101", "Unpinned scoped site was not selected");
    expect(fetchMock.callHistory.called(data.otherSiteUrl)).to.be.false;

    el.setAttribute("site-id", data.otherSiteId);
    await waitUntil(() => siteSelect.value === data.otherSiteId, "Visited site was not selected");
    expect(siteSelect.selectedOptions[0].textContent.trim()).to.equal(data.otherSite.title);
  });
});
