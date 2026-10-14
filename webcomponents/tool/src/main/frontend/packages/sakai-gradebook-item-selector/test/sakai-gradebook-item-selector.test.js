import "../sakai-gradebook-item-selector.js";
import { expect, fixture, html, waitUntil, elementUpdated } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import { mockTranslations } from "../../../test/component-i18n.js";

const groups = [
  { name: "Group A", items: [ { id: "a1", name: "Essay" }, { id: "a2", name: "Quiz" } ] },
  { name: "Group B", items: [ { id: "b1", name: "Essay" } ] },
];

async function picker(el) {
  await waitUntil(() => el.renderRoot.querySelector("sakai-tag-selector")?.renderRoot.querySelector("input"));
  return el.renderRoot.querySelector("sakai-tag-selector");
}

async function select(el, name) {
  const child = await picker(el);
  child.renderRoot.querySelector("input").focus();
  await elementUpdated(child);
  [ ...child.renderRoot.querySelectorAll("[role=option]") ].find(option => option.textContent.includes(name)).click();
  await elementUpdated(el);
  await elementUpdated(child);
}

describe("sakai-gradebook-item-selector", () => {
  beforeEach(() => {
    fetchMock.mockGlobal();
    mockTranslations(fetchMock);
    fetchMock.get("/api/sites/site/items/sakai.samigo", groups);
  });

  afterEach(() => fetchMock.hardReset());

  it("waits for translations and loads the current context after skipped updates", async () => {
    let releaseTranslations;
    const translations = new Promise(resolve => { releaseTranslations = resolve; });
    fetchMock.removeRoutes();
    fetchMock.get(/resourcebundle=gb-selector/, () => translations);
    mockTranslations(fetchMock);
    fetchMock.get("/api/sites/site/items/sakai.samigo", groups);
    fetchMock.get("/api/sites/later/items/sakai.samigo", groups);
    const el = await fixture(html`<sakai-gradebook-item-selector site-id="site" app-name="sakai.samigo"></sakai-gradebook-item-selector>`);
    el.setAttribute("site-id", "later");
    el.setAttribute("selected-temp", "b1");
    await elementUpdated(el);
    expect(el.renderRoot.childElementCount).to.equal(0);
    expect(fetchMock.callHistory.calls("/api/sites/site/items/sakai.samigo")).to.have.length(0);
    releaseTranslations("loading=Loading gradebook items");
    const child = await picker(el);
    expect(fetchMock.callHistory.calls("/api/sites/later/items/sakai.samigo")).to.have.length(1);
    expect(child.selectedTags.map(tag => tag.code)).to.deep.equal([ "b1" ]);
  });

  it("loads item selectors without app-name using the compatible default endpoint", async () => {
    fetchMock.get("/api/sites/site/items/undefined", groups);
    const el = await fixture(html`<sakai-gradebook-item-selector site-id="site" selected-temp="a1"></sakai-gradebook-item-selector>`);
    const child = await picker(el);
    expect(fetchMock.callHistory.calls("/api/sites/site/items/undefined")).to.have.length(1);
    expect(child.selectedTags.map(tag => tag.code)).to.deep.equal([ "a1" ]);
    expect(el.renderRoot.querySelector("[role=status]")).not.to.exist;
    await select(el, "Group A - Quiz");
    expect(child.selectedTags.map(tag => tag.code)).to.deep.equal([ "a2" ]);
  });

  it("restores selections and submits at most one item per gradebook", async () => {
    const form = await fixture(html`<form>
      <input type="hidden" id="gradebooks" name="gradebooks" value="a1">
      <sakai-gradebook-item-selector site-id="site" app-name="sakai.samigo" selected-temp="a1" input-id="gradebooks"></sakai-gradebook-item-selector>
    </form>`);
    const el = form.querySelector("sakai-gradebook-item-selector");
    const child = await picker(el);
    expect(child.selectedTags.map(tag => tag.code)).to.deep.equal([ "a1" ]);
    await select(el, "Group A - Quiz");
    expect(new FormData(form).get("gradebooks")).to.equal("a2");
    expect(child.selectedTags.map(tag => tag.code)).to.deep.equal([ "a2" ]);
    await select(el, "Group B - Essay");
    expect(new FormData(form).get("gradebooks")).to.equal("a2,b1");
    child.renderRoot.querySelector(".tag").click();
    await elementUpdated(el);
    expect(new FormData(form).get("gradebooks")).to.equal("b1");
    await expect(el).to.be.accessible();
  });

  it("uses the category and user/group endpoints and supports keyboard search", async () => {
    fetchMock.get("/api/sites/site/categories/user/group", groups);
    const el = await fixture(html`<sakai-gradebook-item-selector site-id="site" is-category="true" user-id="user" group-id="group"></sakai-gradebook-item-selector>`);
    const child = await picker(el);
    const input = child.renderRoot.querySelector("input");
    input.value = "Quiz";
    input.dispatchEvent(new InputEvent("input", { bubbles: true }));
    await elementUpdated(child);
    input.dispatchEvent(new KeyboardEvent("keydown", { key: "ArrowDown", bubbles: true }));
    input.dispatchEvent(new KeyboardEvent("keydown", { key: "Enter", bubbles: true }));
    await elementUpdated(el);
    await elementUpdated(child);
    expect(child.selectedTags.map(tag => tag.code)).to.deep.equal([ "a2" ]);
  });

  it("treats is-category=false as item selection and renders empty items", async () => {
    fetchMock.get("/api/sites/empty/items/sakai.samigo", []);
    const el = await fixture(html`<sakai-gradebook-item-selector site-id="empty" app-name="sakai.samigo" is-category="false"></sakai-gradebook-item-selector>`);
    await waitUntil(() => el.renderRoot.textContent.includes("no existing Gradebook items"));
    expect(el.renderRoot.querySelector("sakai-tag-selector")).not.to.exist;
  });
});
