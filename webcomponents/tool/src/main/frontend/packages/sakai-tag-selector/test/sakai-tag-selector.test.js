import "../sakai-tag-selector.js";
import { expect, fixture, html, waitUntil, elementUpdated, oneEvent } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import { i18n, i18nUrl } from "./data.js";

const url = "/api/sites/site/tools/samigo/tags/owner";
const options = [ { tagId: "one", tagLabel: "Algebra" }, { tagId: "two", tagLabel: "Biology" } ];


async function ready(el) {
  await waitUntil(() => el.shadowRoot.querySelector("input:not(:disabled)"));
  return el.shadowRoot.querySelector("input");
}

async function search(el, text) {
  const input = await ready(el);
  input.focus();
  input.value = text;
  input.dispatchEvent(new InputEvent("input", { bubbles: true }));
  await elementUpdated(el);
  return input;
}

function key(input, name) {
  input.dispatchEvent(new KeyboardEvent("keydown", { key: name, bubbles: true, cancelable: true }));
}

describe("sakai-tag-selector", () => {
  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock.get(i18nUrl, i18n);
    fetchMock.get(url, options);
  });

  afterEach(() => fetchMock.hardReset());

  it("restores IDs and new labels and submits them through the host form", async () => {
    const form = await fixture(html`<form>
      <sakai-tag-selector site-id="site" tool="samigo" collection-id="owner" input-id="selected"
          selected-temp="one,New label" add-new="true"></sakai-tag-selector>
      <input id="selected" name="tags" type="hidden" value="one,New label">
    </form>`);
    const el = form.querySelector("sakai-tag-selector");
    await ready(el);
    expect(el.selectedTags).to.deep.equal([ { name: "Algebra", code: "one" }, { name: "New label", code: "New label" } ]);
    await search(el, "Bio");
    el.shadowRoot.querySelector("[role=\"option\"]").click();
    expect(new FormData(form).get("tags")).to.equal("one,New label,two");
    el.shadowRoot.querySelector("button[aria-label=\"Deselect: Algebra\"]").click();
    expect(new FormData(form).get("tags")).to.equal("New label,two");
    el.clear();
    expect(new FormData(form).get("tags")).to.equal("");
  });

  it("creates a tag with Enter without submitting the form and emits explicit values", async () => {
    const el = await fixture(html`<sakai-tag-selector site-id="site" tool="samigo" collection-id="owner" add-new="true"></sakai-tag-selector>`);
    const input = await search(el, "New, tag");
    const changed = oneEvent(el, "tags-changed");
    key(input, "Enter");
    expect((await changed).detail.value).to.deep.equal([ { name: "New tag", code: "New tag" } ]);
    await elementUpdated(el);
    expect(el.shadowRoot.querySelectorAll("button.tag")).to.have.length(1);
  });

  it("supports filter-only extras, arrow navigation, Enter and Escape", async () => {
    const el = await fixture(html`<sakai-tag-selector site-id="site" tool="samigo" collection-id="owner"
        add-new="false" extra-options="Group A,Instructor"></sakai-tag-selector>`);
    const input = await search(el, "Group");
    expect(el.shadowRoot.querySelectorAll("[role=\"option\"]")).to.have.length(1);
    key(input, "ArrowDown");
    await elementUpdated(el);
    expect(input.getAttribute("aria-activedescendant")).to.equal("option-0");
    key(input, "Enter");
    expect(el.selectedTags[0].code).to.equal("Group A");
    await search(el, "Unknown");
    key(input, "Enter");
    expect(el.selectedTags).to.have.length(1);
    key(input, "Escape");
    await elementUpdated(el);
    expect(input.getAttribute("aria-expanded")).to.equal("false");
    expect(el.shadowRoot.querySelector("#options").hidden).to.equal(true);
  });

  it("loads associated tags for an existing item", async () => {
    fetchMock.get(`${url}/items/item`, [ options[1] ]);
    const el = await fixture(html`<sakai-tag-selector site-id="site" tool="samigo" collection-id="owner"
        item-id="item" add-new="true"></sakai-tag-selector>`);
    await ready(el);
    expect(el.selectedTags).to.deep.equal([ { name: "Biology", code: "two" } ]);
  });

  it("does not overwrite a saved form value on failure and can retry", async () => {
    fetchMock.removeRoutes();
    fetchMock.get(i18nUrl, i18n);
    fetchMock.get(url, 500);
    const form = await fixture(html`<form>
      <sakai-tag-selector site-id="site" tool="samigo" collection-id="owner"
          input-id="saved" selected-temp="one"></sakai-tag-selector>
      <input id="saved" name="tags" type="hidden" value="one">
    </form>`);
    const el = form.querySelector("sakai-tag-selector");
    await waitUntil(() => el.shadowRoot.querySelector("[role=\"alert\"]"));
    expect(new FormData(form).get("tags")).to.equal("one");
    fetchMock.removeRoutes();
    fetchMock.get(url, options);
    el.shadowRoot.querySelector("[role=\"alert\"] button").click();
    await ready(el);
    expect(el.selectedTags[0].name).to.equal("Algebra");
  });

  it("does not restore saved tags after clearing while a request is pending", async () => {
    let finishLoading;
    fetchMock.get(`${url}/items/saved`, () => new Promise(resolve => { finishLoading = resolve; }));
    const form = await fixture(html`<form>
      <sakai-tag-selector site-id="site" tool="samigo" collection-id="owner" item-id="saved"
          input-id="current-tags" add-new="true"></sakai-tag-selector>
      <input type="hidden" id="current-tags" name="tags" value="one">
    </form>`);
    const el = form.querySelector("sakai-tag-selector");
    await waitUntil(() => finishLoading);
    el.clear();
    expect(new FormData(form).get("tags")).to.equal("");
    finishLoading([options[0]]);
    await ready(el);
    expect(el.selectedTags).to.deep.equal([]);
    expect(new FormData(form).get("tags")).to.equal("");
    await search(el, "Bio");
    el.shadowRoot.querySelector("[role=option]").click();
    expect(new FormData(form).get("tags")).to.equal("two");
  });

  it("keeps catalog labels when fallback options have the same code", async () => {
    const el = await fixture(html`<sakai-tag-selector
        .options=${[{ code: "one", name: "Current label" }]}
        .selectedTags=${[{ code: "one", name: "Old label" }]}
        extra-options="one"></sakai-tag-selector>`);
    await search(el, "");
    const choices = el.shadowRoot.querySelectorAll("[role=option]");
    expect(choices).to.have.length(1);
    expect(choices[0].textContent).to.contain("Current label");
  });

  it("preserves edits when presentation options change or the element reconnects", async () => {
    const form = await fixture(html`<form>
      <sakai-tag-selector site-id="site" tool="samigo" collection-id="owner" input-id="edited"
          selected-temp="one" add-new="true"></sakai-tag-selector>
      <input id="edited" name="tags" type="hidden" value="one">
    </form>`);
    const el = form.querySelector("sakai-tag-selector");
    const input = await search(el, "New tag");
    key(input, "Enter");
    await elementUpdated(el);
    // Any reload would fail: these changes must only affect presentation.
    fetchMock.removeRoutes();
    fetchMock.get(url, 500);
    el.extraOptions = "Group A";
    el.addNew = false;
    await elementUpdated(el);
    expect(input.disabled).to.equal(false);
    await search(el, "Group A");
    expect(el.shadowRoot.querySelector("[role=\"option\"]").textContent).to.contain("Group A");
    el.extraOptions = "Group B";
    await elementUpdated(el);
    expect(el.shadowRoot.querySelectorAll("[role=\"option\"]")).to.have.length(0);
    el.remove();
    form.append(el);
    await elementUpdated(el);
    expect(input.disabled).to.equal(false);
    expect(el.selectedTags.map(tag => tag.code)).to.deep.equal([ "one", "New tag" ]);
    expect(new FormData(form).get("tags")).to.equal("one,New tag");
  });

  it("leaves composing keystrokes to the IME and selects only after composition", async () => {
    const el = await fixture(html`<sakai-tag-selector site-id="site" tool="samigo" collection-id="owner" add-new="true"></sakai-tag-selector>`);
    const input = await search(el, "日本語");
    for (const name of [ "ArrowDown", "ArrowUp", "Escape", "Enter" ]) {
      const event = new KeyboardEvent("keydown", { key: name, isComposing: true, bubbles: true, cancelable: true });
      input.dispatchEvent(event);
      expect(event.defaultPrevented).to.equal(false);
    }
    await elementUpdated(el);
    expect(el.selectedTags).to.deep.equal([]);
    expect(input.value).to.equal("日本語");
    key(input, "Enter");
    expect(el.selectedTags).to.deep.equal([ { name: "日本語", code: "日本語" } ]);
  });

  it("accepts caller-owned options and selection without fetching or emitting changes", async () => {
    fetchMock.removeRoutes();
    fetchMock.get(i18nUrl, i18n);
    const tags = [ { name: "Algebra", code: "one" }, { name: "Biology", code: "two" } ];
    const el = await fixture(html`<sakai-tag-selector .options=${tags} .selectedTags=${[ tags[0] ]}></sakai-tag-selector>`);
    await ready(el);
    const events = [];
    el.addEventListener("tags-changed", event => events.push(event.detail.value));
    el.options = [ ...tags, { name: "Chemistry", code: "three" } ];
    el.selectedTags = [ tags[1] ];
    await elementUpdated(el);
    expect(events).to.have.length(0);
    expect(el.selectedTags).to.deep.equal([ tags[1] ]);
    const input = await search(el, "Algebra");
    key(input, "Enter");
    expect(events).to.deep.equal([ [ tags[1], tags[0] ] ]);
    expect(tags).to.have.length(2);
  });

  it("renders tag labels as text and meets accessible combobox semantics", async () => {
    const el = await fixture(html`<sakai-tag-selector site-id="site" tool="samigo" collection-id="owner"
        extra-options=${"<img src=x onerror=alert(1)>"}></sakai-tag-selector>`);
    const input = await search(el, "img");
    key(input, "ArrowDown");
    await elementUpdated(el);
    expect(el.shadowRoot.querySelector("img")).to.equal(null);
    expect(el.shadowRoot.querySelector("[role=\"option\"]").textContent).to.contain("<img");
    await expect(el).to.be.accessible();
  });
});
