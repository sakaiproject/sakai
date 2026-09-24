import "../sakai-tag-selector.js";
import { expect, fixture, html, waitUntil, elementUpdated, oneEvent } from "@open-wc/testing";
import fetchMock from "fetch-mock";

const url = "/api/sites/site/tools/samigo/tags/owner";
const options = [ { tagId: "one", tagLabel: "Algebra" }, { tagId: "two", tagLabel: "Biology" } ];
const translations = `search_or_add=Search or add a tag
search_filter=Search using tags
add_new=Add this text as new tag
no_options=No tags for this site
no_results=No tags found
selected=Selected
deselect=Deselect
loading=Loading tags...
load_error=Tags could not be loaded. Your saved selection has not been changed.
retry=Retry
none_selected=No tags selected`;

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
    fetchMock.get(/getI18nProperties.*tag-selector/, translations);
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
    fetchMock.get(/getI18nProperties.*tag-selector/, translations);
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
