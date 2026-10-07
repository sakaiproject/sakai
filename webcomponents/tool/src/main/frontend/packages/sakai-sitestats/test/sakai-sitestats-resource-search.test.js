import "../sakai-sitestats-resource-search.js";
import * as i18n from "./i18n.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

const endpoint = "/reports/resources?siteId=site1";
const resource = (n, label = `Reading ${n}`) => ({ id: `/group/site1/${n}.txt`, label, location: "Resources / Week One" });
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
const metadataCalls = () => fetchMock.callHistory.calls().filter(call => call.url.includes("/reports/resources?"));
const mount = async (initialSelection = [], resources = []) => {
  fetchMock.get(new URL(endpoint, location.origin).href, resources);
  const form = await fixture(html`<form><fieldset><sakai-sitestats-resource-search name="whatResourceIds"
    endpoint=${endpoint} .initialSelection=${initialSelection}></sakai-sitestats-resource-search></fieldset></form>`);
  const el = form.querySelector("sakai-sitestats-resource-search");
  await waitUntil(() => el.renderRoot.querySelector("input"));
  await elementUpdated(el);
  return el;
};

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
    const el = await mount([], [ resource(1) ]);
    expect(el.renderRoot.querySelector("label").textContent).to.equal("Search resources");
    expect(el.renderRoot.querySelector("h3").textContent).to.equal("Selected resources (0)");
    expect(Object.keys(window.sakai.translations.sitestats)
      .filter(key => key.startsWith("resource_search_"))).to.have.length(20);
    expect(fetchMock.callHistory.calls().length).to.equal(1);
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 1);
    expect(rows(el)[0].textContent).to.contain("Reading 1");
  });

  it("loads metadata on the first valid query and reuses it without debounce or query requests", async () => {
    const el = await mount([], [ resource(1), resource(2, "Notes") ]);
    expect(el.renderRoot.querySelector("label").htmlFor).to.equal("resource-query");
    expect(el.renderRoot.querySelector("input").getAttribute("aria-describedby")).to.equal("resource-search-status");
    await query(el, "r");
    expect(metadataCalls()).to.have.length(0);
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 1);
    await query(el, "notes");
    expect(rows(el)[0].textContent).to.contain("Notes");
    await query(el, "");
    expect(rows(el)).to.have.length(0);
    await query(el, "reading");
    expect(rows(el)[0].textContent).to.contain("Reading 1");
    expect(metadataCalls()).to.have.length(1);
    const params = new URL(metadataCalls()[0].url).searchParams;
    expect(params.has("q")).to.be.false;
    expect(params.has("page")).to.be.false;
  });

  it("submits selections natively across queries and prevents duplicates", async () => {
    const el = await mount([], [ resource(1), resource(2), resource(3, "Notes") ]);
    const value = () => new FormData(el.closest("form")).get("whatResourceIds");
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 2);
    rows(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(rows(el)[0].querySelector("button").disabled).to.be.true;
    expect(value()).to.equal(resource(1).id);
    expect(el.renderRoot.activeElement).to.equal(el.renderRoot.querySelector("input"));
    await query(el, "notes");
    rows(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(value()).to.equal([ resource(1).id, resource(3).id ].join("\n"));
    selected(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(value()).to.equal(resource(3).id);
    await query(el, "");
    expect(rows(el)).to.have.length(0);
    expect(selected(el)).to.have.length(1);
    expect(metadataCalls()).to.have.length(1);
  });

  it("caps broad results and matches a literal phrase within a name or location", async () => {
    const broad = Array.from({ length: 21 }, (_, n) => resource(n, `Reading ${n}.txt`));
    const fifthWeek = { ...resource(22, "Reading.txt"), location: "Resources / Week 5" };
    const el = await mount([], [ ...broad, fifthWeek, resource(99, "Résumé [50%]_星") ]);
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 20);
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("Showing the first 20 matches. Refine your search.");
    expect(button(el, "More results")).to.be.undefined;
    expect(button(el, "Previous results")).to.be.undefined;
    rows(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("Refine your search.");
    await query(el, "Week 5 Reading");
    expect(rows(el)).to.have.length(0);
    await query(el, "Week 5");
    expect(rows(el)).to.have.length(1);
    expect(rows(el)[0].textContent).to.contain("Resources / Week 5");
    await query(el, "  READING 5  ");
    expect(rows(el)).to.have.length(1);
    expect(rows(el)[0].textContent).to.contain("Reading 5.txt");
    await query(el, "5 Reading");
    expect(rows(el)).to.have.length(0);
    await query(el, "[50%]_星");
    expect(rows(el)).to.have.length(1);
    expect(rows(el)[0].textContent).to.contain("[50%]_星");
    await query(el, "RÉSUMÉ");
    expect(rows(el)).to.have.length(1);
    await query(el, ".*");
    expect(rows(el)).to.have.length(0);
    expect(selected(el)).to.have.length(1);
    expect(metadataCalls()).to.have.length(1);
  });

  it("keeps the server's order and treats exactly twenty matches as complete", async () => {
    const resources = Array.from({ length: 20 }, (_, n) => resource(19 - n));
    const el = await mount([], resources);
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 20);
    expect(rows(el)[0].textContent).to.contain("Reading 19");
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("20 matching resources shown.");
  });

  it("shares a pending metadata load and filters the latest query when it arrives", async () => {
    let resolve;
    const el = await mount([ resource(99) ], () => new Promise(done => { resolve = done; }));
    await query(el, "old");
    await waitUntil(() => resolve);
    await query(el, "new");
    const remove = selected(el)[0].querySelector("button");
    remove.focus();
    resolve([ resource(1, "Old"), resource(2, "Newest") ]);
    await waitUntil(() => rows(el).length === 1);
    expect(rows(el)[0].textContent).to.contain("Newest");
    expect(el.renderRoot.activeElement).to.equal(remove);
    expect(metadataCalls()).to.have.length(1);
  });

  it("keeps cleared or disabled queries hidden when a pending load finishes", async () => {
    let resolve;
    const el = await mount([], () => new Promise(done => { resolve = done; }));
    await query(el, "reading");
    await waitUntil(() => resolve);
    await query(el, "");
    resolve([ resource(1) ]);
    await pause(30);
    expect(rows(el)).to.have.length(0);
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("Enter at least 2");
    el.closest("fieldset").disabled = true;
    await query(el, "reading");
    expect(rows(el)).to.have.length(0);
    el.closest("fieldset").disabled = false;
    await elementUpdated(el);
    expect(rows(el)).to.have.length(1);
    expect(metadataCalls()).to.have.length(1);
  });

  it("aborts a pending load on disconnect", async () => {
    let resolve;
    const el = await mount([], () => new Promise(done => { resolve = done; }));
    await query(el, "reading");
    await waitUntil(() => resolve);
    const signal = metadataCalls()[0].options.signal;
    el.remove();
    expect(signal.aborted).to.be.true;
    resolve([ resource(1) ]);
    await pause(30);
    expect(rows(el)).to.have.length(0);
  });

  it("discards the previous site's metadata when the endpoint changes", async () => {
    const el = await mount([], [ resource(1, "Reading from site1") ]);
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 1);
    fetchMock.get(new URL("/reports/resources?siteId=site2", location.origin).href,
      [ { ...resource(2, "Reading from site2"), id: "/group/site2/2.txt" } ]);
    el.endpoint = "/reports/resources?siteId=site2";
    await elementUpdated(el);
    await waitUntil(() => rows(el)[0]?.textContent.includes("Reading from site2"));
    expect(el.renderRoot.textContent).not.to.contain("Reading from site1");
    expect(metadataCalls()).to.have.length(2);
  });

  it("ignores a previous site's pending response after the endpoint changes", async () => {
    let resolve;
    const el = await mount([], () => new Promise(done => { resolve = done; }));
    await query(el, "reading");
    await waitUntil(() => resolve);
    fetchMock.get(new URL("/reports/resources?siteId=site2", location.origin).href,
      [ { ...resource(2, "Reading from site2"), id: "/group/site2/2.txt" } ]);
    el.endpoint = "/reports/resources?siteId=site2";
    await waitUntil(() => rows(el)[0]?.textContent.includes("Reading from site2"));
    resolve([ resource(1, "Reading from site1") ]);
    await pause(30);
    expect(rows(el)).to.have.length(1);
    expect(rows(el)[0].textContent).to.contain("Reading from site2");
    expect(el.renderRoot.textContent).not.to.contain("Reading from site1");
    expect(metadataCalls()).to.have.length(2);
  });

  it("distinguishes no matches from failure and retries without losing choices", async () => {
    let attempts = 0;
    const el = await mount([ resource(1) ], () => ++attempts === 1 ? 500 : [ resource(2) ]);
    await query(el, "reading");
    await waitUntil(() => button(el, "Retry search"));
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("Could not");
    expect(selected(el)).to.have.length(1);
    button(el, "Retry search").click();
    await waitUntil(() => rows(el).length === 1);
    await query(el, "none");
    expect(el.renderRoot.querySelector("[role=status]").textContent).to.contain("No matching");
    expect(button(el, "Retry search")).to.be.undefined;
    expect(selected(el)).to.have.length(1);
    expect(metadataCalls()).to.have.length(2);
  });

  it("loads an empty inventory once and keeps subsequent searches local", async () => {
    const el = await mount();
    await query(el, "reading");
    await waitUntil(() => el.renderRoot.querySelector("[role=status]").textContent.includes("No matching"));
    await query(el, "notes");
    expect(rows(el)).to.have.length(0);
    expect(metadataCalls()).to.have.length(1);
  });

  it("finds the last of two thousand resources locally while bounding rows and retaining selected IDs", async () => {
    const saved = Array.from({ length: 2000 }, (_, n) => resource(n, `File ${String(n).padStart(4, "0")}`));
    const el = await mount(saved, saved);
    expect(selected(el)).to.have.length(50);
    button(el, "Next selected resources").click();
    await elementUpdated(el);
    expect(selected(el)[0].textContent).to.contain("File 0050");
    selected(el)[0].querySelector("button").click();
    await elementUpdated(el);
    const ids = new FormData(el.closest("form")).get("whatResourceIds").split("\n");
    expect(ids).to.have.length(1999);
    expect(ids).to.include(resource(1999).id);
    expect(ids).not.to.include(resource(50).id);
    await query(el, "file");
    await waitUntil(() => rows(el).length === 20);
    expect(selected(el)).to.have.length(50);
    await query(el, "file 1999");
    expect(rows(el)).to.have.length(1);
    expect(rows(el)[0].textContent).to.contain("File 1999");
    expect(metadataCalls()).to.have.length(1);
  });

  it("escapes hostile labels and renders removable legacy and unavailable choices", async () => {
    const hostile = "<img src=x onerror=alert(1)>";
    const el = await mount([
      { ...resource(1, hostile), legacyCollection: true },
      { id: "/group/site1/deleted.txt", label: "Unavailable resource", location: "", legacyCollection: false },
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
      el.closest("fieldset").disabled = true;
      await elementUpdated(el);
      expect(input.matches(":disabled")).to.be.true;
      expect(getComputedStyle(input).backgroundColor).to.equal(theme.disabledBackground);
      expect(getComputedStyle(input).color).to.equal(theme.disabledText);
      await waitUntil(() => getComputedStyle(input).borderTopColor === theme.border);
      el.closest("fieldset").disabled = false;
      await elementUpdated(el);
    }
  });

  it("finishes a pending load while disabled and reuses it after enabling the filter", async () => {
    let resolve;
    const el = await mount([ resource(1) ], () => new Promise(done => { resolve = done; }));
    await query(el, "reading");
    await waitUntil(() => resolve);
    el.closest("fieldset").disabled = true;
    await elementUpdated(el);
    resolve([ resource(2) ]);
    await pause(30);
    expect(rows(el)).to.have.length(0);
    expect(el.renderRoot.querySelector("input").matches(":disabled")).to.be.true;
    expect(selected(el)).to.have.length(1);
    el.closest("fieldset").disabled = false;
    await elementUpdated(el);
    expect(rows(el)).to.have.length(1);
    expect(metadataCalls()).to.have.length(1);
  });

  it("omits disabled values and restores the initial selection on form reset", async () => {
    const el = await mount([ resource(1) ], [ resource(2) ]);
    const form = el.closest("form");
    await query(el, "reading");
    await waitUntil(() => rows(el).length === 1);
    rows(el)[0].querySelector("button").click();
    await elementUpdated(el);
    expect(new FormData(form).get("whatResourceIds")).to.equal([ resource(1).id, resource(2).id ].join("\n"));
    el.closest("fieldset").disabled = true;
    await elementUpdated(el);
    expect(new FormData(form).has("whatResourceIds")).to.be.false;
    el.closest("fieldset").disabled = false;
    form.reset();
    await elementUpdated(el);
    expect(new FormData(form).get("whatResourceIds")).to.equal(resource(1).id);
    expect(selected(el)).to.have.length(1);
  });

  it("does not submit a surrounding form and supports the server's effective locale", async () => {
    document.documentElement.lang = "fr-FR";
    const form = await fixture(html`<form><sakai-sitestats-resource-search name="whatResourceIds"
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
