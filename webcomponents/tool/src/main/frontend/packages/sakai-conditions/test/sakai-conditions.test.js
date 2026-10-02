import "../sakai-condition-editor.js";
import "../sakai-condition-picker.js";
import { expect, fixture, html, waitUntil, elementUpdated } from "@open-wc/testing";
import fetchMock from "fetch-mock";
import { mockTranslations } from "../../../test/migrated-components-i18n.js";

const itemUrl = "/api/sites/site/conditions?toolId=sakai.lessonbuildertool&itemId=target";
const score = { id: "score", siteId: "site", itemId: "source", type: "SCORE", operator: "GREATER_THAN", argument: "10" };

async function ready(el) {
  await waitUntil(() => el.renderRoot.querySelector("select:not(:disabled)"));
}

function change(select, value) {
  select.value = value;
  select.dispatchEvent(new Event("change"));
}

describe("Lessons Lit condition controls", () => {
  beforeEach(() => {
    fetchMock.mockGlobal();
    mockTranslations(fetchMock);
  });
  afterEach(() => fetchMock.hardReset());

  it("creates and removes a score condition, while protecting conditions already in use", async () => {
    fetchMock.get(itemUrl, [ { ...score, hasParent: true } ]);
    fetchMock.post("/api/sites/site/conditions", ({ options }) => ({ ...JSON.parse(options.body), id: "created" }));
    fetchMock.delete("/api/sites/site/conditions/created", "created");
    const el = await fixture(html`<sakai-condition-editor site-id="site" tool-id="sakai.lessonbuildertool" item-id="target"></sakai-condition-editor>`);
    await ready(el);
    expect(el.renderRoot.querySelector(".condition-row button").disabled).to.be.true;
    const input = el.renderRoot.querySelector("input");
    input.value = "12.5";
    input.dispatchEvent(new InputEvent("input"));
    await elementUpdated(el);
    el.renderRoot.querySelector(".condition-form button").click();
    await waitUntil(() => el.renderRoot.querySelectorAll(".condition-row").length === 2);
    const request = JSON.parse(fetchMock.callHistory.calls("/api/sites/site/conditions", { method: "POST" })[0].options.body);
    expect(request).to.deep.equal({ type: "SCORE", siteId: "site", toolId: "sakai.lessonbuildertool", itemId: "target", operator: "GREATER_THAN", argument: "12.5" });
    el.renderRoot.querySelectorAll(".condition-row button")[1].click();
    await waitUntil(() => el.renderRoot.querySelectorAll(".condition-row").length === 1);
    await expect(el).to.be.accessible();
  });

  it("rejects negative and nonnumeric score thresholds and recovers from a failed save", async () => {
    fetchMock.get(itemUrl, []);
    fetchMock.post("/api/sites/site/conditions", 500);
    const el = await fixture(html`<sakai-condition-editor site-id="site" tool-id="sakai.lessonbuildertool" item-id="target"></sakai-condition-editor>`);
    await ready(el);
    const input = el.renderRoot.querySelector("input");
    const button = el.renderRoot.querySelector(".condition-form button");
    for (const value of [ "-1", "abc", "" ]) {
      input.value = value;
      input.dispatchEvent(new InputEvent("input"));
      await elementUpdated(el);
      expect(button.disabled).to.be.true;
    }
    input.value = "0";
    input.dispatchEvent(new InputEvent("input"));
    await elementUpdated(el);
    button.click();
    await waitUntil(() => el.renderRoot.querySelector("[role=alert]"));
    expect(button.disabled).to.be.false;
    expect(input.value).to.equal("0");
  });

  it("creates the root and AND/OR parents and adds/removes prerequisite references", async () => {
    const conditions = [ score, { ...score, id: "other", itemId: "other-source" } ];
    fetchMock.get(itemUrl, []);
    fetchMock.get("/api/sites/site/conditions", conditions);
    fetchMock.get("/direct/lessons/lesson/1.json", { contentsList: [
      { id: "source", name: "Essay" }, { id: "other-source", name: "Quiz" },
    ] });
    fetchMock.post("/api/sites/site/conditions", ({ options }) => {
      const condition = JSON.parse(options.body);
      return { ...condition, id: condition.type === "ROOT" ? "root" : condition.operator.toLowerCase() };
    });
    fetchMock.put(/\/api\/sites\/site\/conditions\/(root|and|or)$/, ({ options }) => JSON.parse(options.body));
    const el = await fixture(html`<sakai-condition-picker site-id="site" tool-id="sakai.lessonbuildertool" item-id="target" lesson-id="1"></sakai-condition-picker>`);
    await ready(el);
    change(el.renderRoot.querySelector("select"), "score");
    await elementUpdated(el);
    el.renderRoot.querySelector(".condition-form button").click();
    await waitUntil(() => el.renderRoot.querySelectorAll(".condition-row").length === 1);
    expect(el.renderRoot.querySelector('option[value="score"]').disabled).to.be.true;
    change(el.renderRoot.querySelector("select"), "other");
    change(el.renderRoot.querySelectorAll("select")[1], "OR");
    await elementUpdated(el);
    el.renderRoot.querySelector(".condition-form button").click();
    await waitUntil(() => el.renderRoot.querySelectorAll(".condition-row").length === 2);
    const rootUpdates = fetchMock.callHistory.calls("/api/sites/site/conditions/root", { method: "PUT" });
    expect(JSON.parse(rootUpdates[1].options.body).subConditions.map(parent => parent.operator)).to.deep.equal([ "AND", "OR" ]);
    el.renderRoot.querySelector(".condition-row button").click();
    await waitUntil(() => el.renderRoot.querySelectorAll(".condition-row").length === 1);
    expect(el.renderRoot.querySelector('option[value="score"]').disabled).to.be.false;
    expect(JSON.parse(fetchMock.callHistory.calls("/api/sites/site/conditions/and", { method: "PUT" }).at(-1).options.body).subConditions).to.deep.equal([]);
    await expect(el).to.be.accessible();
  });

  it("renders condition arguments as text and preserves translated emphasis", async () => {
    const el = await fixture(html`<sakai-condition-text .condition=${{ ...score, argument: "<img src=x onerror=alert(1)>" }} item="<b>Essay</b>"></sakai-condition-text>`);
    await waitUntil(() => el.renderRoot.querySelector("b"));
    expect(el.renderRoot.querySelector("img")).not.to.exist;
    expect(el.renderRoot.querySelector("b").textContent).to.equal("Essay");
    expect(el.renderRoot.textContent).to.include("<img src=x onerror=alert(1)>");
  });
});
