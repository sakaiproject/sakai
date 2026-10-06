import "../sakai-sitestats-resource-search.js";
import * as i18n from "./i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

const endpoint = "/reports/resources/search?siteId=site1";
const resource = (n, label = `Reading ${n}`) => ({ id: `/group/site1/${n}.txt`, label, location: "Resources / Week One" });
const response = (items, page = 0, hasNext = false) => ({ items, page, hasNext });
const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
const query = async (el, value) => {
  const input = el.renderRoot.querySelector("input");
  input.value = value;
  input.dispatchEvent(new Event("input", { bubbles: true }));
  await elementUpdated(el);
};
const rows = el => el.renderRoot.querySelectorAll("#resource-results li");
const selected = el => el.renderRoot.querySelectorAll("#selected-resources li");
const button = (el, label) => [ ...el.renderRoot.querySelectorAll("button") ]
  .find(candidate => candidate.textContent.trim() === label);
const mount = async (initialSelection = []) => {
  const el = await fixture(html`<sakai-sitestats-resource-search
    endpoint=${endpoint} .initialSelection=${initialSelection}></sakai-sitestats-resource-search>`);
  await waitUntil(() => el.renderRoot.querySelector("input"));
  await elementUpdated(el);
  return el;
};
const searchRoute = (q, page, body) => fetchMock.get({
  url: `begin:${location.origin}/reports/resources/search`, query: { siteId: "site1", q, page: String(page) },
}, body);

describe("sakai-sitestats-resource-search", () => {
  beforeEach(() => {
    window.sessionStorage.clear();
    window.sakai = undefined;
    document.documentElement.lang = "en-US";
    fetchMock.mockGlobal();
    fetchMock.get(i18n.i18nUrl, i18n.i18n);
  });
  afterEach(() => fetchMock.hardReset());

  it("refreshes translations when an older bundle is cached in storage and a resolved promise", async () => {
    const oldBundle = Object.fromEntries(i18n.i18n.trim().split("\n").slice(0, 7)
      .map(line => line.split(/=(.*)/).slice(0, 2)));
    window.sessionStorage.setItem("sitestats", JSON.stringify(oldBundle));
    window.sakai = { translations: { sitestats: oldBundle,
      existingPromises: { sitestats: Promise.resolve(oldBundle) } } };
    searchRoute("reading", 0, response([ resource(1) ]));
    const el = await mount();
    expect(el.renderRoot.querySelector("label").textContent).to.equal("Search resources by name");
    expect(el.renderRoot.querySelector("h3").textContent).to.equal("Selected resources (0)");
    expect(Object.keys(window.sakai.translations.sitestats)
      .filter(key => key.startsWith("resource_search_"))).to.have.length(21);
    expect(fetchMock.callHistory.calls().length).to.equal(1);
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 1);
    expect(rows(el)[0].textContent).to.contain("Reading 1");
  });

  it("labels native search and debounces without fetching initial inventory", async () => {
    searchRoute("reading", 0, response([ resource(1) ]));
    const el = await mount();
    expect(el.renderRoot.querySelector("label").htmlFor).to.equal("resource-query");
    expect(el.renderRoot.querySelector("input").getAttribute("aria-describedby")).to.equal("resource-search-status");
    await query(el, "r");
    await pause(330);
    expect(fetchMock.callHistory.calls().length).to.equal(1);
    await query(el, "read");
    await pause(80);
    await query(el, "reading");
    await pause(180);
    expect(rows(el)).to.have.length(0);
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("Searching");
    await waitUntil(() => rows(el).length === 1);
    expect(fetchMock.callHistory.calls().length).to.equal(2);
  });

  it("keeps selections across queries, prevents duplicates, and emits a copied full list", async () => {
    searchRoute("reading", 0, response([ resource(1), resource(2) ]));
    searchRoute("notes", 0, response([ resource(3, "Notes") ]));
    const el = await mount();
    const events = [];
    el.parentElement.addEventListener("resource-selection-changed", event => events.push(event));
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 2);
    rows(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(rows(el)[0].querySelector("button").disabled).to.be.true;
    expect(events[0].composed).to.be.true;
    expect(events[0].detail.ids).to.deep.equal([ resource(1).id ]);
    events[0].detail.ids.push("untrusted-mutation");
    expect(el.renderRoot.activeElement).to.equal(el.renderRoot.querySelector("input"));
    await query(el, "notes");
    await waitUntil(() => rows(el).length === 1);
    rows(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(events[1].detail.ids).to.deep.equal([ resource(1).id, resource(3).id ]);
    selected(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(events[2].detail.ids).to.deep.equal([ resource(3).id ]);
    await query(el, "");
    expect(rows(el)).to.have.length(0);
    expect(selected(el)).to.have.length(1);
  });

  it("replaces result pages and resets only the page on a new literal query", async () => {
    searchRoute("reading", 0, response([ resource(1) ], 0, true));
    searchRoute("reading", 1, response([ resource(2) ], 1));
    searchRoute("[50%]_星", 0, response([ resource(3, "[50%]_星") ]));
    let resolvePage;
    searchRoute("slow", 0, response([ resource(4) ], 0, true));
    searchRoute("slow", 1, () => new Promise(resolve => { resolvePage = resolve; }));
    const el = await mount([ resource(99) ]);
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 1);
    button(el, "More results").focus();
    button(el, "More results").click();
    await waitUntil(() => rows(el)[0]?.textContent.includes("Reading 2"));
    expect(rows(el)).to.have.length(1);
    await elementUpdated(el);
    expect(el.renderRoot.activeElement).to.equal(button(el, "Previous results"));
    await query(el, "[50%]_星");
    await waitUntil(() => rows(el)[0]?.textContent.includes("[50%]_星"));
    expect(selected(el)[0].textContent).to.contain("Reading 99");
    await query(el, "slow");
    await waitUntil(() => rows(el)[0]?.textContent.includes("Reading 4"));
    button(el, "More results").focus();
    button(el, "More results").click();
    await waitUntil(() => resolvePage);
    const remove = selected(el)[0].querySelector("button");
    remove.focus();
    resolvePage(response([ resource(5) ], 1));
    await waitUntil(() => rows(el)[0]?.textContent.includes("Reading 5"));
    expect(el.renderRoot.activeElement).to.equal(remove);
  });

  it("supersedes stale requests and cancels pending work on clear or disconnect", async () => {
    let resolveOld;
    searchRoute("old", 0, () => new Promise(resolve => { resolveOld = resolve; }));
    searchRoute("new", 0, response([ resource(2, "Newest") ]));
    const el = await mount();
    await query(el, "old");
    await waitUntil(() => resolveOld);
    await query(el, "new");
    await waitUntil(() => rows(el)[0]?.textContent.includes("Newest"));
    resolveOld(response([ resource(1, "Stale") ]));
    await pause(30);
    expect(rows(el)[0].textContent).to.contain("Newest");
    await query(el, "cancelled");
    await query(el, "");
    await pause(330);
    expect(rows(el)).to.have.length(0);
    await query(el, "disconnected");
    el.remove();
    await pause(330);
    expect(fetchMock.callHistory.calls().length).to.equal(3);
  });

  it("distinguishes no matches from failure and retries without losing choices", async () => {
    let attempts = 0;
    searchRoute("reading", 0, () => ++attempts === 1 ? 500 : response([ resource(2) ]));
    searchRoute("none", 0, response([]));
    const el = await mount([ resource(1) ]);
    await query(el, "reading");
    await waitUntil(() => button(el, "Retry search"));
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("Could not");
    expect(selected(el)).to.have.length(1);
    button(el, "Retry search").click();
    await waitUntil(() => rows(el).length === 1);
    await query(el, "none");
    await waitUntil(() => el.renderRoot.querySelector("[role=status]").textContent.includes("No matching"));
    expect(button(el, "Retry search")).to.be.undefined;
    expect(selected(el)).to.have.length(1);
  });

  it("retains all 2,000 selected IDs while bounding selection and result rows", async () => {
    const saved = Array.from({ length: 2000 }, (_, n) => resource(n));
    searchRoute("reading", 0, response(saved));
    const el = await mount(saved);
    expect(selected(el)).to.have.length(50);
    let ids;
    el.addEventListener("resource-selection-changed", event => { ids = event.detail.ids; });
    button(el, "Next selected resources").click();
    await elementUpdated(el);
    expect(selected(el)[0].textContent).to.contain("Reading 50");
    selected(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(ids).to.have.length(1999);
    expect(ids).to.include(resource(1999).id);
    expect(ids).not.to.include(resource(50).id);
    await query(el, "reading");
    await waitUntil(() => rows(el).length > 0);
    expect(rows(el)).to.have.length(20);
    expect(selected(el)).to.have.length(50);
  });

  it("escapes hostile labels and renders removable legacy and unavailable choices", async () => {
    const hostile = "<img src=x onerror=alert(1)>";
    const el = await mount([
      { ...resource(1, hostile), legacyCollection: true },
      { id: "/group/site1/deleted.txt", label: "Unavailable resource", location: "", unavailable: true },
    ]);
    expect(selected(el)[0].textContent).to.contain(hostile);
    expect(selected(el)[0].textContent).to.contain("Saved folder filter");
    expect(el.renderRoot.querySelector("img")).to.be.null;
    expect(selected(el)[1].querySelector(".resource-location")).to.be.null;
    expect(selected(el)[1].querySelector("button").getAttribute("type")).to.equal("button");
  });

  it("uses host theme colors for normal, focused, and disabled inputs when the theme changes", async () => {
    const el = await mount();
    const input = el.renderRoot.querySelector("input");
    const light = { background: "rgb(255, 255, 255)", text: "rgb(38, 38, 38)",
      border: "rgb(204, 204, 204)", disabledBackground: "rgb(238, 238, 238)",
      disabledText: "rgb(102, 102, 102)", focus: "rgb(0, 95, 204)" };
    const dark = { background: "rgb(28, 41, 53)", text: "rgb(221, 221, 221)",
      border: "rgb(80, 99, 121)", disabledBackground: "rgb(67, 89, 110)",
      disabledText: "rgb(153, 153, 153)", focus: "rgb(97, 181, 255)" };
    for (const theme of [ light, dark, light ]) {
      for (const [ token, value ] of Object.entries({
        "--sakai-background-color-1": theme.background,
        "--sakai-text-color-1": theme.text,
        "--sakai-border-color": theme.border,
        "--sakai-background-color-4": theme.disabledBackground,
        "--sakai-text-color-disabled": theme.disabledText,
        "--focus-outline-color": theme.focus,
      })) el.style.setProperty(token, value);
      input.blur();
      expect(getComputedStyle(input).backgroundColor).to.equal(theme.background);
      expect(getComputedStyle(input).color).to.equal(theme.text);
      await waitUntil(() => getComputedStyle(input).borderTopColor === theme.border);
      input.focus();
      expect(getComputedStyle(input).backgroundColor).to.equal(theme.background);
      expect(getComputedStyle(input).color).to.equal(theme.text);
      await waitUntil(() => getComputedStyle(input).borderTopColor === theme.focus);
      expect(getComputedStyle(input).outlineColor).to.equal(theme.focus);
      expect(getComputedStyle(input).boxShadow).to.equal("none");
      el.disabled = true;
      await elementUpdated(el);
      expect(input.matches(":disabled")).to.be.true;
      expect(getComputedStyle(input).backgroundColor).to.equal(theme.disabledBackground);
      expect(getComputedStyle(input).color).to.equal(theme.disabledText);
      await waitUntil(() => getComputedStyle(input).borderTopColor === theme.border);
      el.disabled = false;
      await elementUpdated(el);
    }
  });

  it("disables all actions and cancels search while retaining selection", async () => {
    const el = await mount([ resource(1) ]);
    await query(el, "reading");
    el.disabled = true;
    await elementUpdated(el);
    await pause(330);
    expect(fetchMock.callHistory.calls().length).to.equal(1);
    expect(el.renderRoot.querySelector("input").matches(":disabled")).to.be.true;
    expect(selected(el)).to.have.length(1);
  });

  it("does not submit a surrounding form and supports the server's effective locale", async () => {
    document.documentElement.lang = "fr-FR";
    const form = await fixture(html`<form><sakai-sitestats-resource-search
      endpoint=${endpoint} .initialSelection=${[ resource(1) ]}></sakai-sitestats-resource-search></form>`);
    const el = form.querySelector("sakai-sitestats-resource-search");
    await waitUntil(() => el.renderRoot.querySelector("input"));
    let submits = 0;
    form.addEventListener("submit", event => { event.preventDefault(); submits++; });
    const enter = new KeyboardEvent("keydown", { key: "Enter", bubbles: true, cancelable: true });
    el.renderRoot.querySelector("input").dispatchEvent(enter);
    expect(enter.defaultPrevented).to.be.true;
    const composing = new KeyboardEvent("keydown", { key: "Enter", isComposing: true, cancelable: true });
    el.renderRoot.querySelector("input").dispatchEvent(composing);
    expect(composing.defaultPrevented).to.be.false;
    selected(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(submits).to.equal(0);
    expect(el.renderRoot.querySelector("h3").textContent).to.contain("0");
  });
});
